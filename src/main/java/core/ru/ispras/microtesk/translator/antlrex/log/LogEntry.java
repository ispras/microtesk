/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.antlrex.log;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import ru.ispras.microtesk.utils.FileUtils;

/**
 * The LogEntry class stores information about a translation issue registered in the log.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class LogEntry {

  /**
   * The enumeration describes categories of events or exceptions (usually a record is added
   * to the log due to a runtime exception) that can occur during translation.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */

  public static enum Kind {
    /**
     * Signifies a severe translation error. Usually it means that some part of the 
     * translated specification was incorrect, which cause translation to fail. In other
     * words, the translator was unable to produce any meaningful output.
     */
    ERROR,

    /**
     * Signifies a minor translation error. This usually means a small issue in the 
     * specification which can potentially cause incorrect results.
     */
    WARNING,

    /**
     * Signifies an informational message that highlights some issue in the specification (or in
     * the tool) that requires the user's attention, but is not necessarily an error.
     */
    MESSAGE
  }

  private final Kind kind;
  private final SenderKind sender;

  private final String source;
  private final int line;
  private final int position;
  private final String message;

  private static final String FORMAT = "%s %d:%d %s (%s): \"%s\"";

  /**
   * Creates a LogEntry object.
   * 
   * @param kind The severity level of the issue.
   * @param sender The subsystem that detected the issue.
   * @param source A source file that caused translation issues.
   * @param line The number of the problematic line in the source file.
   * @param position The position in the problematic line in the source file.
   * @param message The text message containing a description of the issue.
   */

  public LogEntry(
      final Kind kind,
      final SenderKind sender,
      final String source,
      final int line,
      final int position,
      final String message) {

    checkNotNull(kind);
    checkNotNull(sender);
    checkNotNull(source);
    checkNotNull(message);

    this.kind = kind;
    this.sender = sender;
    this.source = source;
    this.line = line;
    this.position = position;
    this.message = message;
  }

  /**
   * Return the textual representation of the entry.
   */

  @Override
  public String toString() {
    return String.format(
        FORMAT,
        FileUtils.getShortFileName(getSource()),
        getLine(),
        getPosition(),
        getKind().toString(),
        getSender().toString(),
        getMessage()
        );
  }

  /**
   * Returns an identifier that signifies the severity level of the issue.
   * 
   * @return The severity level of the issue.
   */

  public Kind getKind() {
    return kind;
  }

  /**
   * Returns an identifier of the subsystem that detected an issue.
   * 
   * @return Identifier of the subsystem that detected an issue.
   */

  public SenderKind getSender() {
    return sender;
  }

  /**
   * Returns the name of the source file that caused a translation issue.
   * 
   * @return Source file name.
   */

  public String getSource() {
    return source;
  }

  /**
   * Returns the number of the problematic line in the source file.
   * 
   * @return The line number.
   */

  public int getLine() {
    return line;
  }

  /**
   * Returns the position in the problematic line at which the issue was detected.
   * 
   * @return The position of the text in the problematic line that caused the issue.
   */

  public int getPosition() {
    return position;
  }

  /**
   * Returns the issue description.
   * 
   * @return The issue description.
   */

  public String getMessage() {
    return message;
  }
}
