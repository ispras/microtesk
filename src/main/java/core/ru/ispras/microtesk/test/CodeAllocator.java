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

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;

import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.memory.MemoryAllocator;
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.model.memory.Sections;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.LabelReference;
import ru.ispras.microtesk.test.template.directive.Directive;
import ru.ispras.microtesk.test.template.directive.DirectiveLabel;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CodeAllocator {
  private final Model model;
  private final LabelManager labelManager;
  private final NumericLabelTracker numericLabelTracker;

  private Code code;
  private Map<String, BigInteger> addresses;

  public CodeAllocator(
      final Model model,
      final LabelManager labelManager,
      final NumericLabelTracker numericLabelTracker) {
    InvariantChecks.checkNotNull(model);
    InvariantChecks.checkNotNull(labelManager);
    InvariantChecks.checkNotNull(numericLabelTracker);

    this.model = model;
    this.labelManager = labelManager;
    this.numericLabelTracker = numericLabelTracker;
  }

  public void init() {
    InvariantChecks.checkTrue(null == code);

    code = new Code();
    addresses = new HashMap<>();
  }

  public void reset() {
    InvariantChecks.checkNotNull(code);

    code = null;
    addresses = null;
  }

  public Code getCode() {
    InvariantChecks.checkNotNull(code);
    return code;
  }

  public BigInteger getAddress(final Section section) {
    return addresses.get(section.getName());
  }

  public void setAddress(final Section section, final BigInteger address) {
    addresses.put(section.getName(), address);
  }

  public void allocateSequence(final ConcreteSequence sequence, final int sequenceIndex) {
    final List<ConcreteCall> calls = sequence.getAll();
    final Section section = sequence.getSection();

    allocateCalls(section, calls, sequenceIndex);

    final BigInteger address = addresses.get(section.getName());
    InvariantChecks.checkNotNull(address);

    sequence.setAllocationAddresses(
        !calls.isEmpty() ? calls.get(0).getAddress().longValue() : address.longValue(),
        address.longValue());
  }

  private void allocateCalls(
      final Section section,
      final List<ConcreteCall> calls,
      final int sequenceIndex) {
    if (!calls.isEmpty()) {
      final BigInteger address = addresses.get(section.getName());
      InvariantChecks.checkNotNull(address);

      allocate(section, calls, sequenceIndex);
      code.addBreakAddress(address.longValue());
    }
  }

  public void allocateHandlers(
      final List<Pair<List<ConcreteSequence>, Map<String, ConcreteSequence>>> handlers) {
    InvariantChecks.checkNotNull(handlers);

    // Saving current addresses. Exception handler allocation should not modify them.
    final Map<String, BigInteger> savedAddresses = new HashMap<>(addresses);

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

        getCode().addHandlerAddress(handlerName, handlerCalls.get(0).getAddress().longValue());
      }
    }

    // Restoring initial addresses. Exception handler allocation should not modify them.
    addresses = savedAddresses;
  }

  private void allocate(
      final Section section,
      final List<ConcreteCall> calls,
      final int sequenceIndex) {
    InvariantChecks.checkNotEmpty(calls);
    InvariantChecks.checkNotNull(section);

    final MemoryAllocator allocator = model.getMemoryAllocator();
    final BigInteger oldBasePa = allocator.getBaseAddress();

    allocator.setBaseAddress(section.getBasePa());

    allocateCodeAndRegisterLabels(section, calls, sequenceIndex);
    patchLabels(calls, sequenceIndex, false);

    allocator.setBaseAddress(oldBasePa);
  }

  private void allocateCodeAndRegisterLabels(
      final Section section, final List<ConcreteCall> calls, final int sequenceIndex) {
    final MemoryAllocator allocator = model.getMemoryAllocator();

    Logger.debugHeader("Allocating code");
    Logger.debug("Section: %s%n", section.toString());

    int startIndex = 0;
    int currentIndex = startIndex;

    final BigInteger initialVa = addresses.containsKey(section.getName())
        ? addresses.get(section.getName())
        : section.getBaseVa();

    BigInteger startVa = initialVa;

    BigInteger currentVa = startVa;
    BigInteger currentPa = section.virtualToPhysical(currentVa);

    numericLabelTracker.save();
    for (final ConcreteCall call : calls) {
      call.resetExecutionCount();

      BigInteger nextPa = currentPa;
      BigInteger nextVa = currentVa;

      for (final Directive directive : call.getDirectives()) {
        // Register labels.
        if (directive.getKind() == Directive.Kind.LABEL) {
          final DirectiveLabel labelDirective = (DirectiveLabel) directive;

          if (labelDirective.isRealLabel()) {
            final Label label = labelDirective.getLabel();

            if (label.isNumeric()) {
              label.setReferenceNumber(numericLabelTracker.nextReferenceNumber(label.getName()));
            }

            labelManager.addLabel(label, nextVa.longValue(), sequenceIndex);
          }
        }

        nextPa = directive.apply(nextPa, allocator);
        nextVa = section.physicalToVirtual(nextPa);

        Logger.debug("Directive: %s", directive.getText());
      }

      if (!nextPa.equals(currentPa)) {
        if (startIndex != currentIndex) {
          // Code representation is based on virtual addresses.
          final CodeBlock block = new CodeBlock(
              calls.subList(startIndex, currentIndex),
              startVa.longValue(),
              currentVa.longValue());

          getCode().registerBlock(block);
          startIndex = currentIndex;
        }

        // TODO: This is a hack: it causes aligned code blocks to be linked together.
        // This is done for the following reason. In MIPS, aligned calls are executed
        // as a single sequence because empty space between them filled with zeros is
        // treated as NOPs. This assumption may be incorrect for other ISAs.
        // This situation must be handled in a more correct way. Probably, using decoder.
        final boolean isAligned = !call.getDirectives().isEmpty();
        final boolean isStartAddress = currentVa.equals(initialVa);
        startVa = isAligned && !isStartAddress ? currentVa : nextVa;

        currentPa = nextPa;
        currentVa = nextVa;
      }

      // Set the instruction call virtual address.
      call.setAddress(currentVa);

      // Allocate the instruction call image in memory.
      if (call.isExecutable()) {
        final BitVector image = BitVector.valueOf(call.getImage());
        final int imageSize = allocator.bitsToAddressableUnits(image.getBitSize());

        if (Logger.isDebug()) {
          Logger.debug("0x%016x (PA): %s (0x%s)",
              currentPa, call.getText(), image.toHexString(true));
        }

        allocator.allocateAt(currentPa, image);

        final BigInteger delta = BigInteger.valueOf(imageSize);
        section.setPa(currentPa.add(delta));

        currentVa = currentVa.add(delta);
        currentPa = currentPa.add(delta);
      } else {
        section.setPa(currentPa);
      }

      currentIndex++;
    } // for calls
    numericLabelTracker.restore();

    final CodeBlock block = new CodeBlock(
        startIndex == 0 ? calls : calls.subList(startIndex, currentIndex),
        startVa.longValue(),
        currentVa.longValue());

    getCode().registerBlock(block);
    addresses.put(section.getName(), currentVa);
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
