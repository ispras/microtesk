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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * {@link SettingsParser} implements a SAX-based parser of XML-based generator settings.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class SettingsParser extends DefaultHandler {
  public static GeneratorSettings parse(
      final String fileName, final AbstractSettingsParser<?> parser) {
    final SettingsParser settingsParser = new SettingsParser(fileName, parser);
    return settingsParser.parse();
  }

  public static GeneratorSettings parse(final String fileName) {
    final SettingsParser settingsParser = new SettingsParser(fileName);
    return settingsParser.parse();
  }

  private final AbstractSettingsParser<?> parser;
  private final String fileName;

  private SettingsParser(final String fileName, final AbstractSettingsParser<?> parser) {
    this.parser = parser;
    this.fileName = fileName;
  }

  private SettingsParser(final String fileName) {
    this(fileName, new GeneratorSettingsParser());
  }

  public GeneratorSettings parse() {
    try {
      final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
      final SAXParser saxParser = saxFactory.newSAXParser();

      saxParser.parse(fileName, this);
    } catch (final Exception exception) {
      throw new IllegalStateException(exception.getMessage());
    }

    return (GeneratorSettings) parser.getSettings();
  }

  @Override
  public void startElement(final String uri, final String localName, final String qName,
      final Attributes attrs) {
    parser.onStart(qName, attrs);
  }

  @Override
  public void endElement(final String uri, final String localName, final String qName) {
    parser.onEnd(qName);
  }
}
