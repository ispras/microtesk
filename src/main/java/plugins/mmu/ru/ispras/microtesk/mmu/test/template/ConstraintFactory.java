/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.template;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.randomizer.Variate;
import ru.ispras.fortress.randomizer.VariateCollection;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.MemorySettings;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.template.FixedValue;
import ru.ispras.microtesk.test.template.RandomValue;
import ru.ispras.microtesk.test.template.Value;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link ConstraintFactory} class is used by test templates to
 * create memory-related constraints from Ruby code.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class ConstraintFactory {
  private static ConstraintFactory instance = null;

  private ConstraintFactory() {}

  public static ConstraintFactory get() {
    if (null == instance) {
      instance = new ConstraintFactory();
    }
    return instance;
  }

  public VariableConstraint newEqValue(
      final String variableName,
      final BigInteger value) {
    InvariantChecks.checkNotNull(value);

    final Node variableField = getVariableField(variableName);
    final Value variate = new FixedValue(value);
    final Set<BigInteger> values = Collections.<BigInteger>singleton(value);

    return new VariableConstraint(variableField, variate, values);
  }

  public VariableConstraint newEqRange(
      final String variableName,
      final BigInteger min,
      final BigInteger max) {
    InvariantChecks.checkNotNull(min);
    InvariantChecks.checkNotNull(max);
    InvariantChecks.checkGreaterOrEq(max, min);

    final Node variableField = getVariableField(variableName);
    final Value variate = new RandomValue(min, max);
    final Set<BigInteger> values = new LinkedHashSet<>();

    for (BigInteger value = min; value.compareTo(max) <= 0; value = value.add(BigInteger.ONE)) {
      values.add(value);
    }

    return new VariableConstraint(variableField, variate, values);
  }

  public VariableConstraint newEqArray(
      final String variableName,
      final BigInteger[] array) {
    InvariantChecks.checkNotNull(array);

    final Node variableField = getVariableField(variableName);
    final Value variate = new RandomValue(new VariateCollection<>(array));
    final Set<BigInteger> values = new LinkedHashSet<>(Arrays.asList(array));

    return new VariableConstraint(variableField, variate, values);
  }

  public VariableConstraint newEqDist(
      final String variableName,
      final Variate<?> distribution) {
    InvariantChecks.checkNotNull(distribution);

    final Node variableField = getVariableField(variableName);
    final Value variate = new RandomValue(distribution);
    final Set<BigInteger> values = extractValues(distribution);

    return new VariableConstraint(variableField, variate, values);
  }

  public BufferEventConstraint newHit(final String bufferName) {
    final MmuBuffer buffer = getBuffer(bufferName);
    return new BufferEventConstraint(
        buffer, EnumSet.of(BufferAccessEvent.READ, BufferAccessEvent.HIT));
  }

  public BufferEventConstraint newMiss(final String bufferName) {
    final MmuBuffer buffer = getBuffer(bufferName);
    return new BufferEventConstraint(buffer,
        EnumSet.of(BufferAccessEvent.WRITE, BufferAccessEvent.MISS));
  }

  public BufferEventConstraint newRead(final String bufferName) {
    final MmuBuffer buffer = getBuffer(bufferName);
    return new BufferEventConstraint(
        buffer, EnumSet.of(BufferAccessEvent.READ, BufferAccessEvent.HIT));
  }

  public BufferEventConstraint newWrite(final String bufferName) {
    final MmuBuffer buffer = getBuffer(bufferName);
    return new BufferEventConstraint(buffer,
        EnumSet.of(BufferAccessEvent.WRITE, BufferAccessEvent.MISS));
  }

  public BufferEventConstraint newEvent(
      final String bufferName,
      final int hitBias,
      final int missBias) {
    InvariantChecks.checkNotNull(bufferName);

    final List<BufferAccessEvent> events = new ArrayList<>(2);
    if (0 != hitBias) {
      events.add(BufferAccessEvent.HIT);
    }

    if (0 != missBias) {
      events.add(BufferAccessEvent.MISS);
    }

    final Set<BufferAccessEvent> eventSet = events.isEmpty() ?
        EnumSet.noneOf(BufferAccessEvent.class) : EnumSet.copyOf(events);

    final MmuBuffer buffer = getBuffer(bufferName);
    final BufferEventConstraint constraint =
        new BufferEventConstraint(buffer, eventSet);

    return constraint;
  }

  public AccessConstraints newConstraints(final Object[] constraints) {
    InvariantChecks.checkNotNull(constraints);

    final AccessConstraints.Builder builder =
        new AccessConstraints.Builder();

    for (final Object constraint : constraints) {
      InvariantChecks.checkNotNull(constraint); 

      if (constraint instanceof VariableConstraint) {
        builder.addConstraint((VariableConstraint) constraint);
      } else if (constraint instanceof BufferEventConstraint) {
        builder.addConstraint((BufferEventConstraint) constraint);
      } else if (constraint instanceof String) {
        final GeneratorSettings generatorSettings = GeneratorSettings.get();
        final MemorySettings memorySettings = generatorSettings.getMemory();
        final RegionSettings regionSettings = memorySettings.getRegion((String) constraint);

        builder.setRegion(regionSettings);
      } else {
        throw new IllegalArgumentException(
            "Unsupported constraint class: " + constraint.getClass().getName());
      }
    }

    return builder.build();
  }

  private MmuSubsystem getSpecification() {
    return MmuPlugin.getSpecification();
  }

  private Node getVariableField(final String name) {
    InvariantChecks.checkNotNull(name);
    final Pair<String, String> field = splitBitfield(name);

    final String variableName = field.first;
    final String variableField = field.second;

    final MmuSubsystem spec = getSpecification();
    final NodeVariable variable = spec.getVariable(variableName);

    if (null == variable) {
      throw new GenerationAbortedException(String.format(
          "Invalid test template: variable %s is not defined in the MMU model.", name));
    }

    if (null == variableField) {
      return variable;
    }

    final Pair<Integer, Integer> fieldRange = parseRange(variableField);
    return Nodes.bvextract(fieldRange.second, fieldRange.first, variable);
  }

  private MmuBuffer getBuffer(final String name) {
    InvariantChecks.checkNotNull(name);

    final MmuSubsystem spec = getSpecification();
    final MmuBuffer buffer = spec.getBuffer(name);
    if (null == buffer) {
      throw new GenerationAbortedException(String.format(
          "Invalid test template: buffer %s is not defined in the MMU model.", name));
    }

    return buffer;
  }

  private Set<BigInteger> extractValues(final Variate<?> variate) {
    final ValueExtractor extractor = new ValueExtractor();
    extractor.visit(variate);
    return extractor.getValues();
  }

  private static final Pattern FIELD_PATTERN = Pattern.compile("[<][\\d]+([.][.][\\d]+)?[>]");
  private static Pair<String, String> splitBitfield(final String fieldName) {
    InvariantChecks.checkNotNull(fieldName);

    final Matcher matcher = FIELD_PATTERN.matcher(fieldName);
    if (!matcher.find()) {
      return new Pair<>(fieldName, null);
    }

    if (matcher.end() != fieldName.length()) {
      throw new IllegalArgumentException(
          String.format("Incorrent format of %s: only one field is allowed.", fieldName));
    }

    final String variableName = fieldName.substring(0, matcher.start());
    final String field = fieldName.substring(matcher.start() + 1, matcher.end() - 1);
    return new Pair<>(variableName, field);
  }

  private static final Pattern INTEGER_PATTERN = Pattern.compile("\\d+");
  private static Pair<Integer, Integer> parseRange(final String range) {
    InvariantChecks.checkNotNull(range);
    final Matcher matcher = INTEGER_PATTERN.matcher(range);

    final boolean isStartFound = matcher.find(0);
    InvariantChecks.checkTrue(isStartFound, "Range start is not found.");
    final int start = Integer.parseInt(matcher.group());

    final int end;
    if (matcher.end() == range.length()) {
      end = start;
    } else {
      final boolean isEndFound = matcher.find(matcher.end());
      InvariantChecks.checkTrue(isEndFound, "Range end is not found.");
      end = Integer.parseInt(matcher.group());
    }

    return new Pair<>(Math.min(start, end), Math.max(start, end));
  }
}
