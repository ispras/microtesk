/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.generation;

/**
 * The PackageInfo class holds information on package structure and names of generated Java classes.
 */

public final class PackageInfo {
  private PackageInfo() {}

  /**
   * Extension for Java-files.
   */

  public static final String JAVA_EXT = ".java";

  /**
   * Path to the folder that stores common templates (building blocks) to be reused in other
   * templates.
   */

  public static final String COMMON_TEMPLATE_DIR = "stg/";

  /**
   * Path to the folder that stores string templates (building blocks) for generating model classes
   * based on Sim-nML specifications.
   */

  public static final String SIMNML_TEMPLATE_DIR = "stg/simnml/";

  /**
   * The root folder for generated models.
   */

  public static final String DEFAULT_OUTDIR = "./output";

  /**
   * The name of the root package for generated models.
   */

  public static final String MODEL_PACKAGE = "ru.ispras.microtesk.model";

  /**
   * The folder where the root package for generated models is located.
   */

  public static final String getModelOutDir(String outDir) {
    return outDir + "/" + nameToPath(MODEL_PACKAGE);
  }

  /**
   * Format string for the package where the specified model is located.
   * 
   * Format parameters: model name.
   */

  public static final String MODEL_PACKAGE_FORMAT = MODEL_PACKAGE + ".%s";

  /**
   * Format string for the main class file of the specified model.
   * 
   * Format parameters: model name.
   */

  public static String getModelFileFormat(String outDir) {
    return getModelOutDir(outDir) + "/%s/Model.java";
  }

  /**
   * Format string for the name of the package where we store information on the microprocessor
   * state and other global data (context).
   * 
   * Format parameters: model name.
   */

  public static final String SHARED_PACKAGE_FORMAT = MODEL_PACKAGE + ".%s.shared";

  /**
   * Format string for the name of the class that holds information on the microprocessor state and
   * other global data (context).
   * 
   * Format parameters: model name.
   */

  public static final String SHARED_CLASS_FORMAT = SHARED_PACKAGE_FORMAT + ".Shared";

  /**
   * Format string for the name of the class file that holds information on the microprocessor state
   * and other global data (context).
   * 
   * Format parameters: model name.
   */

  public static String getSharedFileFormat(String outDir) {
    return outDir + "/" + nameToPath(SHARED_CLASS_FORMAT) + JAVA_EXT;
  }

  /**
   * Format string for the name of the package that stores modes (Sim-nML).
   * 
   * Format parameters: model name.
   */

  public static final String MODE_PACKAGE_FORMAT = MODEL_PACKAGE + ".%s.mode";

  /**
   * Format string for the name of the class of a particular mode (Sim-nML).
   * 
   * Format parameters: model name, mode name.
   */

  public static final String MODE_CLASS_FORMAT = MODE_PACKAGE_FORMAT + ".%s";

  /**
   * Format string for the file name of the class of a particular mode (Sim-nML).
   * 
   * Format parameters: model name, mode name.
   */

  public static String getModeFileFormat(String outDir) {
    return outDir + "/" + nameToPath(MODE_CLASS_FORMAT) + JAVA_EXT;
  }

  /**
   * Format string for the name of the package that stores ops (Sim-nML).
   * 
   * Format parameters: model name.
   */

  public static final String OP_PACKAGE_FORMAT = MODEL_PACKAGE + ".%s.op";

  /**
   * Format string for the name of the class of a particular op (Sim-nML).
   * 
   * Format parameters: model name, op name.
   */

  public static final String OP_CLASS_FORMAT = OP_PACKAGE_FORMAT + ".%s";

  /**
   * Format string for the file name of the class of a particular op (Sim-nML).
   * 
   * Format parameters: model name, op name.
   */

  public static String getOpFileFormat(String outDir) {
    return outDir + "/" + nameToPath(OP_CLASS_FORMAT) + JAVA_EXT;
  }

  public static final String SITUATION_PACKAGE_FORMAT = MODEL_PACKAGE + ".%s.situation";
  public static final String SITUATION_CLASS_FORMAT = SITUATION_PACKAGE_FORMAT + ".%s";

  public static String getSituationFileFormat(String outDir) {
    return outDir + "/" + nameToPath(SITUATION_CLASS_FORMAT) + JAVA_EXT;
  }

  public static final String INITIALIZER_PACKAGE_FORMAT = MODEL_PACKAGE + ".%s.initializer";
  public static final String INITIALIZER_CLASS_FORMAT = INITIALIZER_PACKAGE_FORMAT + ".%s";

  public static String getInitializerFileFormat(String outDir) {
    return outDir + "/" + nameToPath(INITIALIZER_CLASS_FORMAT) + JAVA_EXT;
  }

  /**
   * Converts a package or class name to a corresponding path string.
   * 
   * @param name The name of a package or a class.
   * @return The path for the source name.
   */

  private static String nameToPath(String name) {
    return name.replace('.', '/');
  }
}
