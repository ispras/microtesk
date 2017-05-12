/*
 * Copyright 2015-2016 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.ModelBuilder;
import ru.ispras.microtesk.translator.generation.PackageInfo;

/**
 * The {@link SysUtils} class provides utility methods to interact with the environment.
 *  
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class SysUtils {
  /** Name of the environment variable that stores the path to MicroTESK home folder. */
  public static final String MICROTESK_HOME = "MICROTESK_HOME";

  private SysUtils() {}

  /**
   * Returns the path to MicroTESK home folder.
   * 
   * @return Path to MicroTESK home folder or {@code null} if the MICROTESK_HOME
   *         variable that stores this information is not defined in the system.
   */
  public static String getHomeDir() {
    return System.getenv(MICROTESK_HOME);
  }

  /**
   * Results current directory path.
   * 
   * @return Results current directory path.
   */
  public static String getCurrentDir() {
    return System.getProperty("user.dir");
  }

  /**
   * Loads a model with the specified name from {@code models.jar}.
   * 
   * @param modelName Model name.
   * @return New model instance.
   * 
   * @throws IllegalArgumentException if the model name is {@code null} or
   *         if for some reason it cannot be loaded.
   */
  public static Model loadModel(final String modelName) {
    InvariantChecks.checkNotNull(modelName);

    final String modelClassName = String.format(
        "%s.%s.Model", PackageInfo.MODEL_PACKAGE, modelName);

    final ModelBuilder modelBuilder =
        (ModelBuilder) loadFromModel(modelClassName);

    return null != modelBuilder ? modelBuilder.build() : null;
  }

  /**
   * Loads a class with the specified name from {@code models.jar}.
   * 
   * @param className Name of the class to be loaded.
   * @return New instance of the specified class.
   * 
   * @throws IllegalArgumentException if the class name is {@code null} or
   *         if for some reason the class cannot be loaded.
   */
  public static Object loadFromModel(final String className) {
    InvariantChecks.checkNotNull(className);

    final String homeDir = getHomeDir();
    if (null == homeDir) {
      throw new IllegalArgumentException(String.format(
          "The %s environment variable is not defined.", MICROTESK_HOME));
    }

    final String modelsJarPath = Paths.get(homeDir, "lib", "jars", "models.jar").toString();
    final File file = new File(modelsJarPath);
    if (!file.exists()) {
      throw new IllegalArgumentException(String.format(
          "File %s does not exist.", modelsJarPath));
    }

    final URL url;
    try {
      url = file.toURI().toURL();
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException(String.format(
          "Failed to create an URL for file %s. Reason: %s", modelsJarPath, e.getMessage()));
    }

    final URL[] urls = new URL[] {url};
    final ClassLoader cl = new URLClassLoader(urls);

    final Class<?> cls;
    try {
      cls = cl.loadClass(className);
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException(String.format(
          "Failed to load the %s class from %s. Reason: %s", className, modelsJarPath, e.getMessage()));
    }

    final Object instance;
    try {
      instance = cls.newInstance();
    } catch (final InstantiationException | IllegalAccessException e) {
      throw new IllegalArgumentException(String.format(
          "Failed to create an instance of %s. Reason: %s", className, e.getMessage()));
    }

    return instance;
  }

  /**
   * Loads a plug-in implemented by the specified class from {@code microtesk.jar}.
   * 
   * @param className Name of the plug-in class.
   * @return New plug-in instance.
   * 
   * @throws IllegalArgumentException if the class name is {@code null} or
   *         if  for some reason the class cannot be loaded.
   */
  public static Plugin loadPlugin(final String className) {
    InvariantChecks.checkNotNull(className);

    final ClassLoader loader = Plugin.class.getClassLoader();
    try {
      final Class<?> pluginClass = loader.loadClass(className);
      return (Plugin) pluginClass.newInstance();
    } catch(final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new IllegalArgumentException(
          String.format("Failed to load %s. Reason: %s", className, e.getMessage()));
    }
  }

  /**
   * Returns an URL for the specified resource file stored in {@code microtesk.jar}.
   * 
   * @param resourceName Resource file name.
   * @return URL for the specified resource file.
   * 
   * @throws IllegalArgumentException if the argument is {@code null}.
   */
  public static URL getResourceUrl(final String resourceName) {
    InvariantChecks.checkNotNull(resourceName);
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    return classLoader.getResource(resourceName);
  }
}
