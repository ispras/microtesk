/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.situation;

import java.net.URL;

import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.xml.XMLConstraintLoader;
import ru.ispras.fortress.solver.xml.XMLNotLoadedException;

/**
 * The XMLBasedConstraintFactory class provides functionality to recover a constraint from an XML
 * file. It uses an URL to load the file because this is the way to access files placed to a JAR.
 * 
 * @author Andrei Tatarnikov
 */

public final class XMLBasedConstraintFactory implements IConstraintFactory {
  private final String xmlFileName;

  /**
   * Constructs a constraint factory object basing on the location of the XML file that stores a
   * constraint description.
   * 
   * @param xmlFileName Location of the XML file relative to the root of the project's binary
   *        storage (e.g. "xml/arm/ADD_Normal.xml"). This is done so to be able to load XML files
   *        packed into JAR files.
   * 
   * @throws NullPointerException if the parameter is <code>null</code>.
   */

  public XMLBasedConstraintFactory(String xmlFileName) {
    if (null == xmlFileName) {
      throw new NullPointerException();
    }

    this.xmlFileName = xmlFileName;
  }

  /**
   * Loads a constraint from the specified XML file.
   * 
   * @return A new constraint loaded from the XML file or <code>null</code> if loading fails for
   *         some reason.
   */

  @Override
  public Constraint create() {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    final URL url = classLoader.getResource(xmlFileName);

    try {
      final Constraint result = XMLConstraintLoader.loadFromURL(url);
      return result;
    } catch (XMLNotLoadedException e) {
      e.printStackTrace();
    }

    return null;
  }
}
