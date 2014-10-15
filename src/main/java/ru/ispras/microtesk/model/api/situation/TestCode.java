/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

import java.util.Map;

import ru.ispras.fortress.solver.Environment;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;

/**
 * Private code. Presents here only for testing purposes. Probably, it will be removed.
 * 
 * @author Andrei Tatarnikov
 */

class TestCode {
  private static void initializeSolverEngine() {
    if (Environment.isUnix()) {
      Environment.setSolverPath("tools/z3/unix/z3");
    } else if (Environment.isWindows()) {
      Environment.setSolverPath("tools/z3/windows/z3.exe");
    } else if (Environment.isOSX()) {
      Environment.setSolverPath("tools/z3/osx/z3");
    } else {
      assert false : String.format("Please set up paths for the external engine. Platform: %s",
        Environment.getOSName());
    }
  }

  public static void main(String[] arg) {
    initializeSolverEngine();

    try {
      testAddOverflowSituation();
      testAddNormalSituation();
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
  }

  private static void testAddOverflowSituation() throws ConfigurationException {
    System.out.println("Add Overflow Situation");

    final AddOverflowSituation situation = new AddOverflowSituation();
    printResult(situation.solve());

    /*
     * System.out.println("Add Overflow Situation (from XML)");
     * 
     * final ADD_Overflow situationFromXml = new ADD_Overflow();
     * printResult(situationFromXml.solve());
     */
  }

  private static void testAddNormalSituation() throws ConfigurationException {
    System.out.println("Add Normal Situation");

    final AddNormalSituation situation = new AddNormalSituation();
    printResult(situation.solve());

    /*
     * System.out.println("Add Normal Situation (from XML)");
     * 
     * final ADD_Normal situationFromXml = new ADD_Normal(); printResult(situationFromXml.solve());
     */
  }

  private static void printResult(Map<String, Data> result) {
    for (Map.Entry<String, Data> entry : result.entrySet()) {
      System.out.println(entry.getKey() + " = " + entry.getValue().getRawData().toBinString());
    }
  }
}
