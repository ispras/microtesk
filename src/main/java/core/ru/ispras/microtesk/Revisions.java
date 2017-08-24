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

package ru.ispras.microtesk;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.CollectionUtils;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@link Revisions} class stores information about revision dependencies.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Revisions {
  private final Map<String, Set<String>> revisions;

  public Revisions() {
    this.revisions = new HashMap<>();
  }

  public void addRevision(final String revisionId, final Set<String> revisionIncludes) {
    InvariantChecks.checkNotNull(revisionId);
    InvariantChecks.checkNotNull(revisionIncludes);

    final Set<String> revision = new HashSet<>(revisionIncludes);
    revision.add(revisionId);

    if (revisions.containsKey(revisionId)) {
      revisions.put(revisionId, CollectionUtils.uniteSets(revisions.get(revisionId), revision));
    } else {
      revisions.put(revisionId, revision);
    }
  }

  public Set<String> getRevision(final String revisionId) {
    InvariantChecks.checkNotNull(revisionId);

    if (!revisions.containsKey(revisionId)) {
      return Collections.singleton(revisionId);
    }

    final Set<String> revision = revisions.get(revisionId);
    final Set<String> expandedRevision = new HashSet<>();

    expand(revision, expandedRevision);
    return expandedRevision;
  }

  private void expand(final Set<String> revision, final Set<String> expandedRevision) {
    InvariantChecks.checkNotNull(revision);
    InvariantChecks.checkNotNull(expandedRevision);

    for (final String revisionId : revision) {
      final Set<String> includedRevision = revisions.get(revisionId);
      if (null != includedRevision) {
        for (final String includedRevisionId : includedRevision) {
          if (!expandedRevision.contains(includedRevisionId)) {
            expand(includedRevision, expandedRevision);
          }
        }
      } else {
        expandedRevision.add(revisionId);
      }
    }
  }
}
