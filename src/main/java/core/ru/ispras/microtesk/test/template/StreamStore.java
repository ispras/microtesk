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

package ru.ispras.microtesk.test.template;

import ru.ispras.fortress.util.InvariantChecks;

import java.util.HashMap;
import java.util.Map;

public final class StreamStore implements CodeBlockCollection<StreamPreparator> {
  private Map<String, StreamPreparator> preparators;
  private Map<String, Stream> streams;

  public StreamStore() {
    this.preparators = new HashMap<>();
    this.streams = new HashMap<>();
  }

  public StreamPreparator getPreparator(
      final Primitive data, final Primitive index) {
    return preparators.get(StreamPreparator.getId(data, index));
  }

  @Override
  public void add(StreamPreparator p) {
    addPreparator(p);
  }

  public void addPreparator(final StreamPreparator preparator) {
    InvariantChecks.checkNotNull(preparator);
    preparators.put(preparator.getId(), preparator);
  }

  public Stream getStream(final String startLabelName) {
    return streams.get(startLabelName);
  }

  public void addStream(final Stream stream) {
    InvariantChecks.checkNotNull(stream);
    streams.put(stream.getStartLabelName(), stream);
  }

  public Stream addStream(
      final Label label,
      final Primitive dataSource,
      final Primitive indexSource,
      final int length) {

    final StreamPreparator preparator =
        getPreparator(dataSource, indexSource);

    if (null == preparator) {
      throw new IllegalStateException(String.format(
          "No stream preparator is defined for %s (data) and %s (index) addressing modes.",
          dataSource.getName(), indexSource.getName()));
    }

    final Stream stream = preparator.newStream(
        label, dataSource, indexSource, length);

    addStream(stream);
    return stream;
  }
}
