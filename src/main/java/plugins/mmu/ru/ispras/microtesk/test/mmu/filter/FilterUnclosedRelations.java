/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.mmu.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.test.mmu.Template;
import ru.ispras.microtesk.translator.mmu.coverage.Dependency;
import ru.ispras.microtesk.translator.mmu.coverage.Hazard;
import ru.ispras.microtesk.utils.function.Predicate;

/**
 * Filters off test templates with unclosed equality relations (e.g. {@code ADDR_EQUAL},
 * {@code INDEX_EQUAL}, {@code TAG_EQUAL}, etc.).
 * 
 * <p>NOTE: Templates with unclosed equality relations are (1) redundant and (2) may cause
 * problems in constraint solving.</p>
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class FilterUnclosedRelations implements Predicate<Template> {
  @Override
  public boolean test(final Template template) {
    final Map<String, Map<Integer, Set<Integer>>> relations = new LinkedHashMap<>();

    for (int i = 0; i < template.size() - 1; i++) {
      for (int j = i + 1; j < template.size(); j++) {
        final Dependency dependency = template.getDependency(i, j);

        if (dependency != null) {
          update(relations, i, j, dependency);
        }
      }
    }

    if (!areClosed(relations)) {
      return false;
    }

    return true;
  }

  private static void update(final Map<String, Map<Integer, Set<Integer>>> relations,
      final int i, final int j, final Dependency dependency) {

    for (final Hazard hazard : dependency.getHazards()) {
      final List<String> hazardNames = new ArrayList<>();

      switch (hazard.getType()) {
        case ADDR_EQUAL:
          hazardNames.add(String.format("%s.%s", hazard.getAddress(), "ADDR_EQUAL"));
          hazardNames.add(String.format("%s.%s", hazard.getDevice(), "TAG_EQUAL"));
          hazardNames.add(String.format("%s.%s", hazard.getDevice(), "INDEX_EQUAL"));
          break;
        case TAG_EQUAL:
          hazardNames.add(String.format("%s.%s", hazard.getDevice(), "TAG_EQUAL"));
          hazardNames.add(String.format("%s.%s", hazard.getDevice(), "INDEX_EQUAL"));
          break;
        case TAG_NOT_EQUAL:
        case TAG_REPLACED:
        case TAG_NOT_REPLACED:
          hazardNames.add(String.format("%s.%s", hazard.getDevice(), "INDEX_EQUAL"));
          break;
        default:
          break;
      }

      for (final String hazardName : hazardNames) {
        Map<Integer, Set<Integer>> relation = relations.get(hazardName);
        if (relation == null) {
          relations.put(hazardName, relation = new LinkedHashMap<>());
        }

        Set<Integer> linksI = relation.get(i);
        if (linksI == null) {
          relation.put(i, linksI = new LinkedHashSet<>());
        }

        Set<Integer> linksJ = relation.get(j);
        if (linksJ == null) {
          relation.put(j, linksJ = new LinkedHashSet<>());
        }

        linksI.add(j);
        linksJ.add(i);
      }
    }
  }

  private static boolean areClosed(final Map<String, Map<Integer, Set<Integer>>> relations) {
    for (final Map<Integer, Set<Integer>> relation : relations.values()) {
      for (final Set<Integer> links : relation.values()) {
        final ArrayList<Integer> arrayOfLinks = new ArrayList<Integer>(links);

        for (int i = 0; i < arrayOfLinks.size() - 1; i++) {
          final int itemI = arrayOfLinks.get(i);

          for (int j = i + 1; j < arrayOfLinks.size(); j++) {
            final int itemJ = arrayOfLinks.get(j);

            if (!relation.get(itemI).contains(itemJ)) {
              return false;
            }
          }
        }
      }
    }

    return true;
  }
}
