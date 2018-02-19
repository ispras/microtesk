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

package ru.ispras.microtesk.tools.transform;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.options.Options;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public final class TraceTransformer {
  public static final String TAG = "[test]";
  public static final String COMMENT = "#";

  public static final class Message {
    private final String tag;
    private final String source;
    private final String event;
    private final String target;

    public Message(final String tag, final String source, final String event, final String target) {
      InvariantChecks.checkNotNull(tag);
      InvariantChecks.checkNotNull(source);
      InvariantChecks.checkNotNull(event);
      InvariantChecks.checkNotNull(target);

      this.tag = tag;
      this.source = source;
      this.event = event;
      this.target = target;
    }

    public String getTag() {
      return tag;
    }

    public String getSource() {
      return source;
    }

    public String getEvent() {
      return event;
    }

    public String getTarget() {
      return target;
    }

    public String getText() {
      return String.format("%s; # %s", event, toString());
    }

    @Override
    public String toString() {
      return String.format("%s %s %s %s", tag, source, event, tag);
    }
  }

  private static Message parse(final String line) {
    InvariantChecks.checkNotNull(line);

    final StringTokenizer tokenizer = new StringTokenizer(line);

    if (!tokenizer.hasMoreTokens()) {
      return null;
    }

    InvariantChecks.checkTrue(tokenizer.hasMoreTokens());
    final String tag = tokenizer.nextToken();

    if (!TAG.equals(tag)) {
      return null;
    }

    InvariantChecks.checkTrue(tokenizer.hasMoreTokens());
    final String source = tokenizer.nextToken();

    InvariantChecks.checkTrue(tokenizer.hasMoreTokens());
    final String event = tokenizer.nextToken();

    InvariantChecks.checkTrue(tokenizer.hasMoreTokens());
    final String target = tokenizer.nextToken();

    return new Message(tag, source, event, target);
  }

  private TraceTransformer() {}

  public static boolean execute(
      final Options options,
      final String modelName,
      final String templateName,
      final String traceName) {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(modelName);
    InvariantChecks.checkNotNull(templateName);
    InvariantChecks.checkNotNull(traceName);

    // Maps a processing element identifier into the corresponding trace.
    final Map<String, List<Message>> traces = new HashMap<>();

    try {
      final Path tracePath = Paths.get(traceName);
      final BufferedReader traceReader =
          new BufferedReader(new InputStreamReader(Files.newInputStream(tracePath)));

      while (true) {
        final String line = traceReader.readLine();

        if (line == null) {
          break;
        }

        final Message message = parse(line);

        // Skip incorrect messages.
        if (message == null) {
          continue;
        }

        // Add the message to the corresponding trace.
        List<Message> trace = traces.get(message.getSource());
        if (trace == null) {
          traces.put(message.getSource(), trace = new ArrayList<>());
        }

        trace.add(message);
      }

      final Path inputPath = Paths.get(templateName);
      final BufferedReader inputReader =
          new BufferedReader(new InputStreamReader(Files.newInputStream(inputPath)));

      final Path outputPath = Paths.get(traceName + ".rb");
      final BufferedWriter outputWriter =
          new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outputPath)));

      while (true) {
        final String line = inputReader.readLine();

        if (line == null) {
          break;
        }

        outputWriter.write(String.format("%s\n", line));

        final int i = line.indexOf(COMMENT);

        if (i != -1) {
          final String indent = line.substring(0, i);

          for (final Map.Entry<String, List<Message>> entry : traces.entrySet()) {
            final String source = entry.getKey();

            if (line.contains(source)) {
              final List<Message> trace = entry.getValue();

              for (final Message message : trace) {
                outputWriter.write(String.format("%s%s\n", indent, message.getText()));
              }

              break;
            }
          }
        }
      }

      inputReader.close();
      outputWriter.close();
    } catch (final IOException e) {
      Logger.error("IO error: %s", e.getMessage());
      return false;
    }

    return true;
  }
}
