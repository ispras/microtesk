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

package ru.ispras.microtesk;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ru.ispras.fortress.util.InvariantChecks;

public final class Config {
  private Config() {}

  private static final String     CONFIG_URL = "config.xml";
  private static final String         CONFIG = "config";
  private static final String         PLUGIN = "plugin";
  private static final String          CLASS = "class";
  private static final String  SETTINGS_PATH = "/etc/settings.xml";
  private static final String       SETTINGS = "settings";
  private static final String        SETTING = "setting";
  private static final String      ATTR_NAME = "name";
  private static final String     ATTR_VALUE = "value";

  private static final String ERR_ATTRIBUTE_NOT_DEFINED =
      "The %s attribute is not defined for a plugin in %s.";

  private static final String ERR_SETTINGS_FILE_NOT_EXIST =
      "The configuration file %s does not exist or is not a file.";

  private static final String ERR_NO_ROOT_NODE =
      "Document %s contains no root node called %s.";

  private static final String ERR_FAILED_TO_PARSE = "Failed to parse %s.";

  public static Plugin loadPlugin(final String className) {
    InvariantChecks.checkNotNull(className);

    final ClassLoader loader = Plugin.class.getClassLoader();
    try {
      final Class<?> pluginClass = loader.loadClass(className);
      return (Plugin) pluginClass.newInstance();
    } catch(ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      return null;
    }
  }
  
  public static List<Plugin> loadPlugins() {
    final URL configUrl = getResource(CONFIG_URL);
    if (null == configUrl) {
      throw new IllegalStateException(String.format("Document %s is not found.", CONFIG_URL));
    }

    final Document configDocument = parseDocument(configUrl);
    if (null == configDocument) {
      throw new IllegalStateException(String.format(ERR_FAILED_TO_PARSE, CONFIG_URL));
    }

    final Node config = configDocument.getFirstChild();
    if (!CONFIG.equalsIgnoreCase((config.getNodeName()))) {
      throw new IllegalStateException(String.format( 
          ERR_NO_ROOT_NODE, CONFIG_URL, CONFIG));
    }

    final List<Plugin> result = new ArrayList<>();
    result.add(new Core());

    final NodeList pluginNodes = config.getChildNodes();
    for (int index = 0; index < pluginNodes.getLength(); ++index) {
      final Node pluginNode = pluginNodes.item(index);
      if (!PLUGIN.equalsIgnoreCase((pluginNode.getNodeName()))) {
        continue;
      }

      final NamedNodeMap attributes = pluginNode.getAttributes();
      final Node className = attributes.getNamedItem(CLASS);
      if (null == className) {
        throw new IllegalStateException(String.format(
            ERR_ATTRIBUTE_NOT_DEFINED, CLASS, CONFIG_URL));
      }

      final Plugin plugin = loadPlugin(className.getNodeValue());
      result.add(plugin);
    }

    return result;
  }

  private static URL getResource(final String name) {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    return classLoader.getResource(name);
  }

  private static Document parseDocument(final URL url) {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    factory.setIgnoringComments(true);
    factory.setIgnoringElementContentWhitespace(true);
    factory.setValidating(false);

    try {
      final DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(new InputSource(url.openStream()));
    } catch (ParserConfigurationException | SAXException | IOException e) {
      return null;
    }
  }

  private static Document parseDocument(final File file) {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    factory.setIgnoringComments(true);
    factory.setIgnoringElementContentWhitespace(true);
    factory.setValidating(false);

    try {
      final DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(file);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      return null;
    }
  }

  public static Map<String, String> loadSettings() {
    final String homePath = SysUtils.getHomeDir();
    if (null == homePath) {
      Logger.warning("The %s environment variable is not defined.", SysUtils.MICROTESK_HOME);
      return Collections.emptyMap();
    }

    final String fileName = homePath + SETTINGS_PATH;
    final File file = new File(fileName);

    if (!file.exists() || !file.isFile()) {
      Logger.warning(ERR_SETTINGS_FILE_NOT_EXIST, file.getPath());
      return Collections.emptyMap();
    }

    final Document document = parseDocument(file);
    if (null == document) {
      throw new IllegalStateException(String.format(ERR_FAILED_TO_PARSE, CONFIG_URL));
    }

    final Node root = document.getFirstChild();
    if (!SETTINGS.equalsIgnoreCase((root.getNodeName()))) {
      throw new IllegalStateException(String.format( 
          ERR_NO_ROOT_NODE, file.getPath(), SETTINGS));
    }

    final Map<String, String> result = new LinkedHashMap<>();

    final NodeList settings = root.getChildNodes();
    for (int index = 0; index < settings.getLength(); ++index) {
      final Node setting = settings.item(index);
      if (!SETTING.equalsIgnoreCase((setting.getNodeName()))) {
        continue;
      }

      final NamedNodeMap attributes = setting.getAttributes();

      final Node name = attributes.getNamedItem(ATTR_NAME);
      if (null == name) {
        throw new IllegalStateException(String.format(
            ERR_ATTRIBUTE_NOT_DEFINED, ATTR_NAME, file.getName()));
      }

      final Node value = attributes.getNamedItem(ATTR_VALUE);
      if (null == value) {
        throw new IllegalStateException(String.format(
            ERR_ATTRIBUTE_NOT_DEFINED, ATTR_VALUE, file.getName()));
      }

      result.put(name.getNodeValue(), value.getNodeValue());
    }

    return result;
  }
}
