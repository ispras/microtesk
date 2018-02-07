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

package ru.ispras.microtesk;

import ru.ispras.fortress.util.CollectionUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The {@link Revisions} class stores information about revision dependencies.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Revisions {
  // Key: revision ID, Value: includes/excludes
  private final Map<String, Pair<Set<String>, Set<String>>> revisions;
  private final Map<String, Set<String>> expandedRevisions;

  public Revisions() {
    this.revisions = new LinkedHashMap<>();
    this.expandedRevisions = new LinkedHashMap<>();
  }

  protected void addRevision(
      final String revisionId,
      final Set<String> includes,
      final Set<String> excludes) {
    InvariantChecks.checkNotNull(revisionId);
    InvariantChecks.checkNotNull(includes);
    InvariantChecks.checkNotNull(excludes);

    if (revisions.containsKey(revisionId)) {
      final Pair<Set<String>, Set<String>> revision = revisions.get(revisionId);
      revisions.put(revisionId, new Pair<>(CollectionUtils.uniteSets(revision.first, includes),
                                           CollectionUtils.uniteSets(revision.second, excludes)));
    } else {
      revisions.put(revisionId, new Pair<>(includes, excludes));
    }
  }

  public Set<String> getRevision(final String revisionId) {
    InvariantChecks.checkNotNull(revisionId);

    if (!revisions.containsKey(revisionId)) {
      return Collections.singleton(revisionId);
    }

    return expand(revisionId);
  }

  private Set<String> expand(final String revisionId) {
    InvariantChecks.checkNotNull(revisionId);

    if (expandedRevisions.containsKey(revisionId)) {
      return expandedRevisions.get(revisionId);
    }

    final Set<String> expandedRevision = new LinkedHashSet<>();
    expandedRevision.add(revisionId);

    final Pair<Set<String>, Set<String>> revision = revisions.get(revisionId);
    if (null != revision) {
      for (final String includeRevisionId : revision.first) {
        expandedRevision.addAll(expand(includeRevisionId));
      }

      for (final String excludeRevisionId : revision.second) {
        expandedRevision.removeAll(expand(excludeRevisionId));
      }
    }

    expandedRevisions.put(revisionId, expandedRevision);
    return expandedRevision;
  }

  @Override
  public String toString() {
    return revisions.toString();
  }
}
