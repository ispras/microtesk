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

package ru.ispras.microtesk.settings;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link AbstractSettingsParser} implements an abstract parser of XML-based settings.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class AbstractSettingsParser {
  public static String getString(final String value) {
    return value;
  }

  public static int getHexInteger(final String value) {
    return Integer.parseInt(value, 16);
  }

  public static int getDecInteger(final String value) {
    return Integer.parseInt(value);
  }

  public static long getHexLong(final String value) {
    return Long.parseLong(value, 16);
  }

  public static long getDecLong(final String value) {
    return Long.parseLong(value);
  }

  public static boolean getBoolean(final String value) {
    return Boolean.parseBoolean(value);
  }

  @SuppressWarnings("unchecked")
  public static <E extends Enum<E>> E getEnum(final Class<E> type, final String value) {
    final EnumSet<E> items = EnumSet.allOf(type);

    for (final Enum<E> item : items) {
      if (item.name().toLowerCase().equals(value.toLowerCase())) {
        return (E) item;
      }
    }

    return null;
  }

  private final String tag;

  private final Stack<AbstractSettingsParser> parserStack = new Stack<>();
  private final Map<String, AbstractSettingsParser> parsers = new HashMap<>();

  private AbstractSettings settings;

  public AbstractSettingsParser(final String tag) {
    InvariantChecks.checkNotNull(tag);
    this.tag = tag;
  }

  public final String getTag() {
    return tag;
  }
  
  public final AbstractSettings getSettings() {
    return settings;
  }

  protected abstract AbstractSettings createSettings(final Map<String, String> attributes);

  public final void addParser(final AbstractSettingsParser parser) {
    InvariantChecks.checkNotNull(parser);
    parsers.put(parser.getTag(), parser);
  }

  private Map<String, String> convertAttributes(final Attributes attrs) {
    final Map<String, String> attributes = new HashMap<>();

    for (int i = 0; i < attrs.getLength(); i++) {
      final String key = attrs.getLocalName(i);
      final String val = attrs.getValue(i);

      attributes.put(key, val);
    }

    return attributes;
  }

  public final void onStart(final String tag, final Attributes attrs) {
    InvariantChecks.checkNotNull(tag);
    InvariantChecks.checkNotNull(attrs);

    if (getTag().equals(tag)) {
      // The main section has started.
      settings = createSettings(convertAttributes(attrs));
    } else if (!parserStack.isEmpty()) {
      // A part of the subsection being processed.
      parserStack.firstElement().onStart(tag, attrs);
    } else if (parsers.containsKey(tag)) {
      // A new subsection has started.
      final AbstractSettingsParser parser = parsers.get(tag);

      parser.onStart(tag, attrs);
      parserStack.push(parser);
    } else {
      throw new IllegalArgumentException(String.format("Unexpected openning tag <%s>", tag));
    }
  }

  public final void onEnd(final String tag) {
    InvariantChecks.checkNotNull(tag);

    if (getTag().equals(tag)) {
      // The main section has completed.
    } else if (!parserStack.isEmpty()) {
      final AbstractSettingsParser topParser = parserStack.firstElement();

      if (tag.equals(topParser.getTag())) {
        // The subsection has completed.
        settings.add(topParser.getSettings());
        parserStack.pop();
      } else {
        topParser.onEnd(tag);
      }
    } else {
      throw new IllegalArgumentException(String.format("Unexpected closing tag </%s>", tag));
    }
  }
}
