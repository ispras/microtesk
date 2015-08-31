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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.translator.generation.PackageInfo;

public final class SysUtils {
  public static final String MICROTESK_HOME = "MICROTESK_HOME";

  private SysUtils() {}

  public static String getHomeDir() {
    return System.getenv(MICROTESK_HOME);
  }

  public static String getCurrentDir() {
    return System.getProperty("user.dir");
  }

  public static IModel loadModel(final String modelName) {
    InvariantChecks.checkNotNull(modelName);

    final String modelClassName = String.format(
        "%s.%s.Model", PackageInfo.MODEL_PACKAGE, modelName);

    return (IModel) loadFromModel(modelClassName);
  }

  public static Object loadFromModel(final String className) {
    InvariantChecks.checkNotNull(className);

    final String homeDir = getHomeDir();
    if (null == homeDir) {
      Logger.error("The %s environment variable is not defined.", MICROTESK_HOME);
      return null;
    }

    final String modelsJarPath = Paths.get(homeDir, "lib", "jars", "models.jar").toString();
    if (null == modelsJarPath) {
      return null;
    }

    final File file = new File(modelsJarPath);
    if (!file.exists()) {
      Logger.error("File %s does not exist.", modelsJarPath);
      return null;
    }

    final URL url;
    try {
      url = file.toURI().toURL();
    } catch (final MalformedURLException e) {
      Logger.error("Failed to create an URL for file %s. Reason: %s", modelsJarPath, e.getMessage());
      return null;
    }

    final URL[] urls = new URL[] {url};
    final ClassLoader cl = new URLClassLoader(urls);

    final Class<?> cls;
    try {
      cls = cl.loadClass(className);
    } catch (final ClassNotFoundException e) {
      Logger.error("Failed to load the %s class from %s. Reason: %s", className, modelsJarPath, e.getMessage());
      return null;
    }

    final Object instance;
    try {
      instance = cls.newInstance();
    } catch (final InstantiationException | IllegalAccessException e) {
      Logger.error("Failed to create an instance of %s. Reason: %s", className, e.getMessage());
      return null;
    }

    return instance;
  }

  public static Plugin loadPlugin(final String className) {
    InvariantChecks.checkNotNull(className);

    final ClassLoader loader = Plugin.class.getClassLoader();
    try {
      final Class<?> pluginClass = loader.loadClass(className);
      return (Plugin) pluginClass.newInstance();
    } catch(final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      Logger.error("Failed to load %s. Reason: %s", className, e.getMessage());
      return null;
    }
  }

  public static URL getResourceUrl(final String resourceName) {
    InvariantChecks.checkNotNull(resourceName);
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    return classLoader.getResource(resourceName);
  }
}
