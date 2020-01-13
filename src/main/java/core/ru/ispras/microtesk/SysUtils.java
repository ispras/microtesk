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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.ModelBuilder;
import ru.ispras.microtesk.translator.codegen.PackageInfo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;

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
   * Returns the path to MicroTESK home folder, which is stored
   * in the {@code MICROTESK_HOME} environment variable.
   *
   * @return Path to MicroTESK home folder.
   * @throws IllegalStateException if the {@code MICROTESK_HOME} environment variable
   *         is not defined.
   */
  public static String getHomeDir() {
    final String homeDir = System.getenv(MICROTESK_HOME);
    if (null == homeDir) {
      throw new IllegalStateException(String.format(
          "The %s environment variable is not defined.", MICROTESK_HOME));
    }
    return homeDir;
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
   * Returns absolute path to the {@code models.jar} file that stores compiled microprocessor
   * models. The file's relative file is {@code $MICROTESK_HOME/lib/jars/models.jar}.
   *
   * @return Absolute path to the {@code models.jar} file.
   * @throws IllegalStateException if the {@code MICROTESK_HOME} environment variable
   *         is not defined.
   */
  public static Path getModelsJarPath() {
    return Paths.get(getHomeDir(), "lib", "jars", "models.jar");
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

    Model model = LOADED_MODELS.get(modelName);
    if (model == null) {
      final String modelClassName = String.format(
          "%s.%s.Model", PackageInfo.MODEL_PACKAGE, modelName);

      final ModelBuilder builder = (ModelBuilder) loadFromModel(modelClassName);
      if (builder != null) {
        model = builder.build();
        LOADED_MODELS.put(modelName, model);
      }
    }
    return model;
  }

  private static final Map<String, Model> LOADED_MODELS = new java.util.HashMap<>();

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
    try {
      final URL url = getModelsJarPath().toUri().toURL();
      final ClassLoader cl = new URLClassLoader(new URL[] { url });
      return cl.loadClass(className).newInstance();
    } catch (final MalformedURLException
        | ClassNotFoundException
        | InstantiationException
        | IllegalAccessException e) {
      throw new IllegalArgumentException(
          String.format("Invalid class name '%s': %s", className, e.getMessage()),
          e);
    }
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
    } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
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

  /**
   * Searching for path to the settings file for the architecture specified.
   *
   * <p>The path is extracted from a string that stores a map of architecture names and
   * corresponding paths. The string has the following format:
   * {@code name1=path1:name2=path2:...:nameN=pathN}
   *
   * @param archDirs String that stores a map of architectures and their directories.
   * @param archName Architecture name.
   * @return Path to arch-specific settings file or {@code null} if no such path is found.
   *
   * @throws IllegalArgumentException if any of the arguments is {@code null}.
   * @throws IllegalStateException if the {@code MICROTESK_HOME} environment variable is undefined.
   */
  public static Path searchArchSettingsPath(
      final String archDirs, final String archName) {
    InvariantChecks.checkNotNull(archDirs);
    InvariantChecks.checkNotNull(archName);

    final var pattern = "\\W*" + Pattern.quote(archName) + "=([^:]*)";
    final var matcher = Pattern.compile(pattern).matcher(archDirs);
    if (matcher.find()) {
      final var path = Paths.get(matcher.group(1));
      if (path.isAbsolute()) {
        return path;
      }
      return Paths.get(getHomeDir()).resolve(path);
    }
    return null;
  }
}
