/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test;

import java.math.BigInteger;
import java.util.EnumMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.SysUtils;

public final class TestEngineSettings {

  public static enum Setting {
    INDENT_TOKEN("\t"),
    SEPARATOR_TOKEN("="),

    SL_COMMENT_TOKEN("//"),
    ML_COMMENT_START_TOKEN("/*"),
    ML_COMMENT_END_TOKEN("*/"),

    ORIGIN_FORMAT(".org 0x%x"),
    ALIGN_FORMAT(".align %d"),

    BASE_VIRTUAL_ADDRESS(BigInteger.ZERO),
    BASE_PHYSICAL_ADDRESS(BigInteger.ZERO),

    OUTPUT_DIRECTORY(SysUtils.getHomeDir()),
    CODE_FILE_EXTENSION(".asm"),
    CODE_FILE_PREFIX("test"),
    DATA_FILE_EXTENSION(".dat"),
    DATA_FILE_PREFIX("test"),
    EXCEPTION_FILE_PREFIX("test_except"),

    BRANCH_EXECUTION_LIMIT(100),
    PROGRAM_LENGTH_LIMIT(1000),
    TRACE_LENGTH_LIMIT(1000),

    COMMENTS_DEBUG(false),
    COMMENTS_ENABLED(false),

    SIMULATION_DISABLED(false),
    TARMAC_LOG(false),
    SELF_CHECKS(false),
    DEFAULT_TEST_DATA(false);

    private final Object defaultValue;

    private Setting(final Object value) {
      InvariantChecks.checkNotNull(value);
      this.defaultValue = value;
    }

    public Object getDefaultValue() {
      return defaultValue;
    }

    public Class<?> getValueClass() {
      return defaultValue.getClass();
    }
  }

  private final Map<Setting, Object> settings;

  public TestEngineSettings() {
    this.settings = new EnumMap<>(Setting.class);
  }

  public void setSetting(final Setting setting, final Object value) {
    InvariantChecks.checkNotNull(setting);
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkTrue(setting.getValueClass().equals(value.getClass()));
    settings.put(setting, value);
  }

  public void setSetting(final String settingName, final Object value) {
    InvariantChecks.checkNotNull(settingName);
    final Setting setting = Setting.valueOf(settingName.toUpperCase());
    setSetting(setting, value);
  }

  public Object getSetting(final Setting setting) {
    InvariantChecks.checkNotNull(setting);
    final Object value = settings.get(setting);
    return null != value ? value : setting.getDefaultValue();
  }

  public Object getSetting(final String settingName) {
    InvariantChecks.checkNotNull(settingName);
    final Setting setting = Setting.valueOf(settingName.toUpperCase());
    return getSetting(setting);
  }

  public String getSettingAsString(final Setting setting) {
    return getSetting(setting).toString();
  }

  public String getSettingAsString(final String settingName) {
    return getSetting(settingName).toString();
  }

  public int getSettingAsInteger(final Setting setting) {
    final Object value = getSetting(setting);
    InvariantChecks.checkTrue(value instanceof Integer);
    return (Integer) value;
  }

  public int getSettingAsInteger(final String settingName) {
    final Object value = getSetting(settingName);
    InvariantChecks.checkTrue(value instanceof Integer);
    return (Integer) value;
  }

  public BigInteger getSettingAsBigInteger(final Setting setting) {
    final Object value = getSetting(setting);
    InvariantChecks.checkTrue(value instanceof BigInteger);
    return (BigInteger) value;
  }

  public BigInteger getSettingAsBigInteger(final String settingName) {
    final Object value = getSetting(settingName);
    InvariantChecks.checkTrue(value instanceof BigInteger);
    return (BigInteger) value;
  }

  public boolean getSettingAsBoolean(final Setting setting) {
    final Object value = getSetting(setting);
    InvariantChecks.checkTrue(value instanceof Boolean);
    return (Boolean) value;
  }

  public boolean getSettingAsBoolean(final String settingName) {
    final Object value = getSetting(settingName);
    InvariantChecks.checkTrue(value instanceof Boolean);
    return (Boolean) value;
  }

  @Override
  public String toString() {
    return "TestEngineSettings [settings=" + settings + "]";
  }
}
