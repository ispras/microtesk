/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.util.InvariantChecks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * The {@link Config} class provides methods that read configuration files and create objects
 * they described.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Config {
  private Config() {}

  private static final String     CONFIG_URL = "config.xml";
  private static final String         CONFIG = "config";
  private static final String         PLUGIN = "plugin";
  private static final String          CLASS = "class";
  private static final Path    SETTINGS_PATH = Paths.get("etc", "settings.xml");
  private static final String       SETTINGS = "settings";
  private static final String        SETTING = "setting";
  private static final String      ATTR_NAME = "name";
  private static final String     ATTR_VALUE = "value";

  private static final String      REVISIONS = "revisions";
  private static final String       REVISION = "revision";
  private static final String       INCLUDES = "includes";
  private static final String       EXCLUDES = "excludes";

  private static final String ERR_ATTRIBUTE_NOT_DEFINED =
      "The %s attribute is not defined for the %s node.";

  private static final String ERR_SETTINGS_FILE_NOT_EXIST =
      "The configuration file %s does not exist or is not a file.";

  private static final String ERR_NO_ROOT_NODE =
      "Document %s contains no root node called %s.";

  private static final String ERR_FAILED_TO_PARSE = "Failed to parse %s.";

  public static List<Plugin> loadPlugins() {
    final URL configUrl = SysUtils.getResourceUrl(CONFIG_URL);
    final Node config;
    try (final InputStream input = configUrl.openStream()) {
      config = getDocumentRoot(CONFIG, CONFIG_URL, input);
    } catch (final FileNotFoundException e) {
      throw new IllegalStateException(
          String.format("Document %s is not found.", CONFIG_URL), e);
    } catch (final Exception e) {
      throw new IllegalStateException(
          String.format(ERR_FAILED_TO_PARSE, CONFIG_URL), e);
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
            ERR_ATTRIBUTE_NOT_DEFINED, CLASS, PLUGIN));
      }

      final Plugin plugin = SysUtils.loadPlugin(className.getNodeValue());
      result.add(plugin);
    }
    return result;
  }

  private static Node getDocumentRoot(
      final String rootName, final String docName, final InputStream input)
      throws ParserConfigurationException, SAXException, IOException {
    final Node root = parseDocument(input).getFirstChild();
    if (!rootName.equalsIgnoreCase((root.getNodeName()))) {
      throw new IllegalStateException(String.format(
          ERR_NO_ROOT_NODE, docName, rootName));
    }
    return root;
  }

  private static Document parseDocument(final InputStream input)
      throws ParserConfigurationException, SAXException, IOException {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    factory.setIgnoringComments(true);
    factory.setIgnoringElementContentWhitespace(true);
    factory.setValidating(false);

    return factory.newDocumentBuilder().parse(input);
  }

  public static Map<String, String> loadSettings() {
    final String homePath = SysUtils.getHomeDir();
    if (null == homePath) {
      Logger.warning("The %s environment variable is not defined.", SysUtils.MICROTESK_HOME);
      return Collections.emptyMap();
    }

    final Path path = Paths.get(homePath).resolve(SETTINGS_PATH);
    final Node root;
    try (final InputStream input = Files.newInputStream(path)) {
      root = getDocumentRoot(SETTINGS, path.toString(), input);
    } catch (final FileNotFoundException e) {
      Logger.warning(ERR_SETTINGS_FILE_NOT_EXIST, path.toString());
      return Collections.emptyMap();
    } catch (final Exception e) {
      throw new IllegalStateException(
          String.format(ERR_FAILED_TO_PARSE, path.toString()), e);
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
            ERR_ATTRIBUTE_NOT_DEFINED, ATTR_NAME, SETTING));
      }

      final Node value = attributes.getNamedItem(ATTR_VALUE);
      if (null == value) {
        throw new IllegalStateException(String.format(
            ERR_ATTRIBUTE_NOT_DEFINED, ATTR_VALUE, SETTING));
      }

      result.put(name.getNodeValue(), value.getNodeValue());
    }

    return result;
  }

  public static Revisions loadRevisions(final Path path) {
    InvariantChecks.checkNotNull(path);

    final Node root;
    try (final InputStream input = Files.newInputStream(path)) {
      root = getDocumentRoot(REVISIONS, path.toString(), input);
    } catch (final FileNotFoundException e) {
      return new Revisions();
    } catch (final Exception e) {
      throw new IllegalStateException(
          String.format(ERR_FAILED_TO_PARSE, path.toString()), e);
    }
    final var revisions = new Revisions();
    final NodeList revisionList = root.getChildNodes();
    for (int revisionIndex = 0; revisionIndex < revisionList.getLength(); ++revisionIndex) {
      final Node revision = revisionList.item(revisionIndex);
      if (!REVISION.equalsIgnoreCase((revision.getNodeName()))) {
        continue;
      }

      final NamedNodeMap revisionAttributes = revision.getAttributes();
      final Node revisionName = revisionAttributes.getNamedItem(ATTR_NAME);
      if (null == revisionName) {
        throw new IllegalStateException(
            String.format(ERR_ATTRIBUTE_NOT_DEFINED, ATTR_NAME, REVISION));
      }

      final String revisionId = revisionName.getNodeValue();
      final Set<String> revisionIncludes = new LinkedHashSet<>();
      final Set<String> revisionExcludes = new LinkedHashSet<>();

      final NodeList nodeList = revision.getChildNodes();
      for (int nodeIndex = 0; nodeIndex < nodeList.getLength(); ++nodeIndex) {
        final Node node = nodeList.item(nodeIndex);
        final String nodeName = node.getNodeName();

        if (!INCLUDES.equalsIgnoreCase(nodeName) && !EXCLUDES.equalsIgnoreCase(nodeName)) {
          continue;
        }

        final NamedNodeMap nodeAttributes = node.getAttributes();
        final Node attributeNode = nodeAttributes.getNamedItem(ATTR_NAME);
        if (null == attributeNode) {
          throw new IllegalStateException(
              String.format(ERR_ATTRIBUTE_NOT_DEFINED, ATTR_NAME, nodeName));
        }

        final String attributeNodeValue = attributeNode.getNodeValue();
        if (INCLUDES.equalsIgnoreCase(nodeName)) {
          revisionIncludes.add(attributeNodeValue);
        } else {
          revisionExcludes.add(attributeNodeValue);
        }
      }

      revisions.addRevision(revisionId, revisionIncludes, revisionExcludes);
    }

    return revisions;
  }
}
