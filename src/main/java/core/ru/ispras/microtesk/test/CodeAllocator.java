/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.test;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.DataSection;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.LabelReference;

final class CodeAllocator {
  private final EngineContext engineContext;
  private Code code;

  public CodeAllocator(final EngineContext engineContext) {
    InvariantChecks.checkNotNull(engineContext);

    this.engineContext = engineContext;
    this.code = null;
  }

  public void init() {
    InvariantChecks.checkTrue(null == code);
    code = new Code();
  }

  public void reset() {
    InvariantChecks.checkNotNull(code);
    code = null;
  }

  public Code getCode() {
    InvariantChecks.checkNotNull(code);
    return code;
  }

  public void allocateSequence(final TestSequence sequence, final int sequenceIndex) {
    InvariantChecks.checkFalse(sequence.isEmpty());
    allocate(sequence.getAll(), sequenceIndex);
  }

  public void allocateHandlers(final List<Map<String, TestSequence>> handlers) {
    InvariantChecks.checkNotNull(handlers);

    // Saving current address. Exception handler allocation should not modify it.
    final long address = engineContext.getAddress();

    for (final Map<String, TestSequence> handler: handlers) {
      final Set<Object> handlerSet = new HashSet<>();
      for (final Map.Entry<String, TestSequence> e : handler.entrySet()) {
        final String handlerName = e.getKey();
        final TestSequence handlerSequence = e.getValue();

        if (handlerSequence.isEmpty()) {
          Logger.warning("Empty exception handler: %s", handlerName);
          continue;
        }

        final List<ConcreteCall> handlerCalls = e.getValue().getAll();
        getCode().addHandlerAddress(handlerName, handlerCalls.get(0).getAddress());

        if (!handlerSet.contains(handlerSequence)) {
          allocate(handlerCalls, Label.NO_SEQUENCE_INDEX);
          handlerSet.add(handlerSequence);
        }
      }
    }

    // Restoring initial address. Exception handler allocation should not modify it.
    engineContext.setAddress(address);
  }

  private void allocate(final List<ConcreteCall> calls, final int sequenceIndex) {
    InvariantChecks.checkNotEmpty(calls);

    allocateDataSections(calls, sequenceIndex);
    allocateCodeBlocks(calls);

    registerLabels(calls, sequenceIndex);
    patchLabels(calls, sequenceIndex, false);
  }

  private void allocateDataSections(final List<ConcreteCall> calls, final int sequenceIndex) {
    final LabelManager labelManager = engineContext.getLabelManager();
    for (final ConcreteCall call : calls) {
      if (call.getData() != null) {
        final DataSection data = call.getData();
        data.setSequenceIndex(sequenceIndex);
        engineContext.getDataManager().processData(labelManager, data);
      }
    }
  }

  private void allocateCodeBlocks(final List<ConcreteCall> calls) {
    int startIndex = 0;
    int currentIndex = startIndex;

    long startAddress = engineContext.getAddress();
    long currentAddress = startAddress;

    for (final ConcreteCall call : calls) {
      call.setAddress(currentAddress);
      final long callAddress = call.getAddress();

      if (callAddress != currentAddress) {
        if (startIndex != currentIndex) {
          final CodeBlock block = new CodeBlock(
              calls.subList(startIndex, currentIndex), startAddress, currentAddress);

          getCode().registerBlock(block);
          startIndex = currentIndex;
        }

        // TODO: This is a hack: it causes aligned code blocks to be linked together.
        // This is done for the following reason. In MIPS, aligned calls are executed
        // as a single sequence because empty space between them filled with zeros is
        // treated as NOPs. This assumption may be incorrect for other ISAs.
        // This situation must be handled in a more correct way. Probably, using decoder.
        startAddress = call.getAlignment() != null ? currentAddress : callAddress;
        currentAddress = startAddress;
      }

      currentAddress += call.getByteSize();
      currentIndex++;
    }

    final CodeBlock block = new CodeBlock(
        startIndex == 0 ? calls : calls.subList(startIndex, currentIndex),
        startAddress,
        currentAddress
        );

    getCode().registerBlock(block);
    engineContext.setAddress(currentAddress);
  }

  private void registerLabels(final List<ConcreteCall> calls, final int sequenceIndex) {
    final LabelManager labelManager = engineContext.getLabelManager();
    for (final ConcreteCall call : calls) {
      labelManager.addAllLabels(call.getLabels(), call.getAddress(), sequenceIndex);
    }
  }

  private void patchLabels(
      final List<ConcreteCall> calls,
      final int sequenceIndex,
      final boolean abortOnUndefined) {
    // Resolves all label references and patches the instruction call text accordingly.
    final LabelManager labelManager = engineContext.getLabelManager();
    for (final ConcreteCall call : calls) {
      // Resolves all label references and patches the instruction call text accordingly.
      for (final LabelReference labelRef : call.getLabelReferences()) {
        labelRef.resetTarget();

        final Label source = labelRef.getReference();
        source.setSequenceIndex(sequenceIndex);

        final LabelManager.Target target = labelManager.resolve(source);

        final String uniqueName;
        final String searchPattern;
        final String patchedText;

        if (null != target) { // Label is found
          labelRef.setTarget(target);

          uniqueName = target.getLabel().getUniqueName();
          final long address = target.getAddress();

          if (null != labelRef.getArgumentValue()) {
            searchPattern = String.format("<label>%d", labelRef.getArgumentValue());
          } else {
            labelRef.getPatcher().setValue(BigInteger.ZERO);
            searchPattern = "<label>0";
          }

          patchedText = call.getText().replace(searchPattern, uniqueName);
          labelRef.getPatcher().setValue(BigInteger.valueOf(address));
        } else { // Label is not found
          if (abortOnUndefined) {
            throw new GenerationAbortedException(String.format(
                "Label '%s' passed to '%s' (0x%x) is not defined or%n" +
                "is not accessible in the scope of the current test sequence.",
                source.getName(), call.getText(), call.getAddress()));
          }

          uniqueName = source.getName();
          searchPattern = "<label>0";

          patchedText = call.getText().replace(searchPattern, uniqueName);
        }

        call.setText(patchedText);
      }

      // Kill all unused "<label>" markers.
      if (null != call.getText()) {
        call.setText(call.getText().replace("<label>", ""));
      }
    }
  }
}
