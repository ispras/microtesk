/*
 * Copyright 2017-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.memory.MemoryAllocator;
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.model.memory.Sections;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.LabelReference;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CodeAllocator {
  private final Model model;
  private final LabelManager labelManager;
  private final NumericLabelTracker numericLabelTracker;
  private final boolean placeToMemory;

  private Code code;
  private long address;

  public CodeAllocator(
      final Model model,
      final LabelManager labelManager,
      final boolean placeToMemory) {
    InvariantChecks.checkNotNull(model);
    InvariantChecks.checkNotNull(labelManager);

    this.model = model;
    this.labelManager = labelManager;
    this.numericLabelTracker = new NumericLabelTracker();
    this.code = null;
    this.address = 0;
    this.placeToMemory = placeToMemory;
  }

  public void init() {
    InvariantChecks.checkTrue(null == code);
    code = new Code();

    final Section section = Sections.get().getTextSection();
    InvariantChecks.checkNotNull("Section .text is not defined in the template!");
    address = section.getBaseVa().longValue();
    numericLabelTracker.reset();
  }

  public void reset() {
    InvariantChecks.checkNotNull(code);
    code = null;

    final Section section = Sections.get().getTextSection();
    InvariantChecks.checkNotNull("Section .text is not defined in the template!");
    address = section.getBaseVa().longValue();
    numericLabelTracker.reset();
  }

  public Code getCode() {
    InvariantChecks.checkNotNull(code);
    return code;
  }

  public long getAddress() {
    return address;
  }

  public void setAddress(final long address) {
    this.address = address;
  }

  public void allocateSequence(final ConcreteSequence sequence, final int sequenceIndex) {
    final List<ConcreteCall> calls = sequence.getAll();
    allocateCalls(sequence.getSection(), calls, sequenceIndex);

    sequence.setAllocationAddresses(
        !calls.isEmpty() ? calls.get(0).getAddress() : address, address);
  }

  private void allocateCalls(
      final Section section,
      final List<ConcreteCall> calls,
      final int sequenceIndex) {
    if (!calls.isEmpty()) {
      allocate(section, calls, sequenceIndex);
      code.addBreakAddress(address);
    }
  }

  public void allocateHandlers(
      final List<Pair<List<ConcreteSequence>, Map<String, ConcreteSequence>>> handlers) {
    InvariantChecks.checkNotNull(handlers);

    // Saving current address. Exception handler allocation should not modify it.
    final long currentAddress = address;

    for (final Pair<List<ConcreteSequence>, Map<String, ConcreteSequence>> handler: handlers) {
      final Set<Object> handlerSet = new HashSet<>();
      for (final Map.Entry<String, ConcreteSequence> e : handler.second.entrySet()) {
        final String handlerName = e.getKey();
        final ConcreteSequence handlerSequence = e.getValue();

        if (handlerSequence.isEmpty()) {
          Logger.warning("Empty exception handler: %s", handlerName);
          continue;
        }

        final List<ConcreteCall> handlerCalls = handlerSequence.getAll();
        final Section section = handlerSequence.getSection();

        if (!handlerSet.contains(handlerSequence)) {
          // Allocation of handlers must not be taken into account when allocating other code
          final BigInteger pa = section.getPa();
          allocate(section, handlerCalls, Label.NO_SEQUENCE_INDEX);
          section.setPa(pa);
          handlerSet.add(handlerSequence);
        }

        getCode().addHandlerAddress(handlerName, handlerCalls.get(0).getAddress());
      }
    }

    // Restoring initial address. Exception handler allocation should not modify it.
    address = currentAddress;
  }

  private void allocate(
      final Section section,
      final List<ConcreteCall> calls,
      final int sequenceIndex) {
    InvariantChecks.checkNotEmpty(calls);
    InvariantChecks.checkNotNull(section);

    allocateCodeBlocks(section, calls);
    registerLabels(calls, sequenceIndex);
    patchLabels(calls, sequenceIndex, false);
    allocateMemory(section, calls);
  }

  private void allocateCodeBlocks(final Section section, final List<ConcreteCall> calls) {
    Logger.debugHeader("Allocating code");
    Logger.debug("Section: %s%n", section.toString());

    int startIndex = 0;
    int currentIndex = startIndex;

    long startAddress = address;
    long currentAddress = startAddress;

    for (final ConcreteCall call : calls) {
      call.resetExecutionCount();

      call.setAddress(section, currentAddress);
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
        final boolean isAligned = call.getAlignment() != null;
        final boolean isStartAddress = currentAddress == address;
        startAddress = isAligned && !isStartAddress ? currentAddress : callAddress;

        currentAddress = callAddress;
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
    address = currentAddress;
  }

  private void allocateMemory(final Section section, final List<ConcreteCall> calls) {
    if (!placeToMemory) {
      return;
    }

    final MemoryAllocator memoryAllocator = model.getMemoryAllocator();
    InvariantChecks.checkNotNull(memoryAllocator);

    for (final ConcreteCall call : calls) {
      final BitVector virtualAddress = BitVector.valueOf(call.getAddress(), 64);
      final BigInteger physicalAddress =
              section.virtualToPhysical(virtualAddress.bigIntegerValue(false));

      if (call.isExecutable()) {
        final BitVector image = BitVector.valueOf(call.getImage());
        final int imageSize = memoryAllocator.bitsToAddressableUnits(image.getBitSize());

        if (Logger.isDebug()) {
          Logger.debug("0x%016x (PA): %s (0x%s)",
              physicalAddress, call.getText(), image.toHexString(true));
        }

        memoryAllocator.allocateAt(image, physicalAddress);
        section.setPa(physicalAddress.add(BigInteger.valueOf(imageSize)));
      } else {
        section.setPa(physicalAddress);
      }
    }
  }

  private void registerLabels(final List<ConcreteCall> calls, final int sequenceIndex) {
    numericLabelTracker.save();
    for (final ConcreteCall call : calls) {
      for (final Label label : call.getLabels()) {
        if (label.isNumeric()) {
          label.setReferenceNumber(
              numericLabelTracker.nextReferenceNumber(label.getName()));
        }
      }

      labelManager.addAllLabels(call.getLabels(), call.getAddress(), sequenceIndex);
    }
    numericLabelTracker.restore();
  }

  private void patchLabels(
      final List<ConcreteCall> calls,
      final int sequenceIndex,
      final boolean abortOnUndefined) {
    // Resolves all label references and patches the instruction call text accordingly.
    for (final ConcreteCall call : calls) {
      for (final Label label : call.getLabels()) {
        if (label.isNumeric()) {
          numericLabelTracker.nextReferenceNumber(label.getName());
        }
      }

      // Resolves all label references and patches the instruction call text accordingly.
      for (final LabelReference labelRef : call.getLabelReferences()) {
        labelRef.resetTarget();

        final Label source = labelRef.getReference();
        source.setSequenceIndex(sequenceIndex);

        if (source.isNumeric()) {
          final boolean forward = "f".equals(labelRef.getReferenceSuffix());
          source.setReferenceNumber(
              numericLabelTracker.getReferenceNumber(source.getName(), forward));
        }

        final LabelManager.Target target = labelManager.resolve(source);
        if (null != target) { // Label is found
          labelRef.setTarget(target);
          final long address = target.getAddress();
          labelRef.getPatcher().setValue(BigInteger.valueOf(address));
        } else { // Label is not found
          // References to undefined labels are not assigned sequence index as
          // they presumably refer to some global labels.
          // In particular, this applies to jumps to an error handler performed in self-checks.
          source.setSequenceIndex(Label.NO_SEQUENCE_INDEX);
          if (abortOnUndefined) {
            throw new GenerationAbortedException(String.format(
                "Label '%s' passed to '%s' (0x%x) is not defined or%n"
                    + "is not accessible in the scope of the current test sequence.",
                source.getName(), call.getText(), call.getAddress()));
          }
        }
      }
    }
  }
}
