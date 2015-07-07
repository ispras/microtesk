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

package ru.ispras.microtesk.mmu.translator.coverage;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuConditionAtom;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
final class BufferCoverageExtractor {
  private final MmuDevice buffer;

  public BufferCoverageExtractor(final MmuDevice buffer) {
    InvariantChecks.checkNotNull(buffer);
    this.buffer = buffer;
  }

  public List<MemoryHazard> getHazards() {
    final List<MemoryHazard> hazards = new ArrayList<>();

    if (buffer.getSets() > 1 && buffer.getIndexExpression() != null) {
      // Index1 != Index2.
      final MemoryHazard hazardIndexNoEqual = new MemoryHazard(MemoryHazard.Type.INDEX_NOT_EQUAL,
          buffer, MmuCondition.neq(buffer.getIndexExpression()));

      hazards.add(hazardIndexNoEqual);
    }

    if (buffer.getTagExpression() != null) {
      final List<MmuConditionAtom> tagNoEqualEqualities = new ArrayList<>();

      if (buffer.getSets() > 1 && buffer.getIndexExpression() != null) {
        tagNoEqualEqualities.add(MmuConditionAtom.eq(buffer.getIndexExpression()));
      }

      tagNoEqualEqualities.add(MmuConditionAtom.neq(buffer.getTagExpression()));

      if (!buffer.isReplaceable()) {
        // Index1 == Index2 && Tag1 != Tag2.
        final MemoryHazard hazardTagNoEqual = new MemoryHazard(MemoryHazard.Type.TAG_NOT_EQUAL,
            buffer, MmuCondition.and(tagNoEqualEqualities));

        hazards.add(hazardTagNoEqual);
      } else {
        // Index1 == Index2 && Tag1 != Tag2 && Tag1 != Replaced2.
        final MemoryHazard hazardTagNoReplaced =
            new MemoryHazard(MemoryHazard.Type.TAG_NOT_REPLACED, buffer,
                MmuCondition.neqReplaced(buffer.getTagExpression()));

        hazards.add(hazardTagNoReplaced);

        // Index1 == Index2 && Tag1 != Tag2 && Tag1 == Replaced2.
        final MemoryHazard hazardTagReplaced = new MemoryHazard(MemoryHazard.Type.TAG_REPLACED,
            buffer, MmuCondition.eqReplaced(buffer.getTagExpression()));

        hazards.add(hazardTagReplaced);
      }

      final List<MmuConditionAtom> tagEqualEqualities = new ArrayList<>();

      if (buffer.getSets() > 1 && buffer.getIndexExpression() != null) {
        tagEqualEqualities.add(MmuConditionAtom.eq(buffer.getIndexExpression()));
      }

      tagEqualEqualities.add(MmuConditionAtom.eq(buffer.getTagExpression()));

      // Index1 == Index2 && Tag1 == Tag2.
      final MemoryHazard hazardTagEqual = new MemoryHazard(MemoryHazard.Type.TAG_EQUAL, buffer,
          MmuCondition.and(tagEqualEqualities));

      hazards.add(hazardTagEqual);
    }

    return hazards;
  }
}
