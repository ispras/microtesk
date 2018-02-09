/*
 * Copyright 2012-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model;

/**
 * The {@link ConfigurationException} exception is thrown on an attempt
 * to address an entity that is not defined by the microprocessor model.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class ConfigurationException extends Exception {
  private static final long serialVersionUID = -7710697576919321538L;

  public ConfigurationException() {}

  public ConfigurationException(final String message) {
    super(message);
  }

  public ConfigurationException(final String format, final Object... args) {
    super(String.format(format, args));
  }

  public ConfigurationException(final Throwable cause) {
    super(cause);
  }

  public ConfigurationException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
