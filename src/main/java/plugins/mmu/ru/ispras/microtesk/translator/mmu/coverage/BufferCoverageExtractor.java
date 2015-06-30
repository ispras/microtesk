/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use buffer file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.translator.mmu.coverage;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.mmu.ir.spec.MmuCondition;
import ru.ispras.microtesk.translator.mmu.ir.spec.MmuDevice;
import ru.ispras.microtesk.translator.mmu.ir.spec.MmuEquality;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
final class BufferCoverageExtractor {
  private final MmuDevice buffer;

  public BufferCoverageExtractor(final MmuDevice buffer) {
    InvariantChecks.checkNotNull(buffer);
    this.buffer = buffer;
  }

  public List<Hazard> getHazards() {
    final List<Hazard> hazards = new ArrayList<>();

    if (buffer.getSets() > 1 && buffer.getIndexExpression() != null) {
      final MmuCondition conditionIndexNoEqual = new MmuCondition();

      final MmuEquality equalityIndexNoEqual =
          new MmuEquality(MmuEquality.Type.NOT_EQUAL, buffer.getIndexExpression());
      conditionIndexNoEqual.addEquality(equalityIndexNoEqual);

      // Index1 != Index2.
      final Hazard hazardIndexNoEqual =
          new Hazard(Hazard.Type.INDEX_NOT_EQUAL, buffer, conditionIndexNoEqual);
      hazards.add(hazardIndexNoEqual);
    }

    if (buffer.getTagExpression() != null) {
      final MmuCondition conditionTagNoEqual = new MmuCondition();

      if (buffer.getSets() > 1 && buffer.getIndexExpression() != null) {
        final MmuEquality equalityIndexEqual =
            new MmuEquality(MmuEquality.Type.EQUAL, buffer.getIndexExpression());
        conditionTagNoEqual.addEquality(equalityIndexEqual);
      }

      final MmuEquality equalityTagNoEqual =
          new MmuEquality(MmuEquality.Type.NOT_EQUAL, buffer.getTagExpression());
      conditionTagNoEqual.addEquality(equalityTagNoEqual);

      if (!buffer.isReplaceable()) {
        // Index1 == Index2 && Tag1 != Tag2.
        final Hazard hazardTagNoEqual =
            new Hazard(Hazard.Type.TAG_NOT_EQUAL, buffer, conditionTagNoEqual);
        hazards.add(hazardTagNoEqual);
      } else {
        final MmuCondition conditionTagNoReplaced = new MmuCondition(conditionTagNoEqual);

        final MmuEquality equalityTagNoReplaced =
            new MmuEquality(MmuEquality.Type.NOT_EQUAL_REPLACED, buffer.getTagExpression());

        conditionTagNoReplaced.addEquality(equalityTagNoReplaced);

        // Index1 == Index2 && Tag1 != Tag2 && Tag1 != Replaced2.
        final Hazard hazardTagNoReplaced =
            new Hazard(Hazard.Type.TAG_NOT_REPLACED, buffer, conditionTagNoReplaced);
        hazards.add(hazardTagNoReplaced);

        final MmuCondition conditionTagReplaced = new MmuCondition(conditionTagNoEqual);

        final MmuEquality equalityTagReplaced =
            new MmuEquality(MmuEquality.Type.EQUAL_REPLACED, buffer.getTagExpression());

        conditionTagReplaced.addEquality(equalityTagReplaced);

        // Index1 == Index2 && Tag1 != Tag2 && Tag1 == Replaced2.
        final Hazard hazardTagReplaced =
            new Hazard(Hazard.Type.TAG_REPLACED, buffer, conditionTagReplaced);
        hazards.add(hazardTagReplaced);
      }

      final MmuCondition conditionTagEqual = new MmuCondition();

      if (buffer.getSets() > 1 && buffer.getIndexExpression() != null) {
        final MmuEquality equalityIndexEqual =
            new MmuEquality(MmuEquality.Type.EQUAL, buffer.getIndexExpression());
        conditionTagEqual.addEquality(equalityIndexEqual);
      }

      final MmuEquality equalityTagEqual =
          new MmuEquality(MmuEquality.Type.EQUAL,buffer.getTagExpression());
      conditionTagEqual.addEquality(equalityTagEqual);

      // Index1 == Index2 && Tag1 == Tag2.
      final Hazard hazardTagEqual =
          new Hazard(Hazard.Type.TAG_EQUAL, buffer, conditionTagEqual);
      hazards.add(hazardTagEqual);
    }

    return hazards;
  }
}
