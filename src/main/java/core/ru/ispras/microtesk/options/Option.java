/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.options;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.Property;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link Option} enumeration describes options.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public enum Option implements Property {
  ////////////////////////////////////////////////////////////////////////////////////////////////
  // Common Options

  HELP("Shows help message", false),
  VERBOSE("Enables printing diagnostic messages", false),
  OUTPUT_DIR("Directory to place generated files", "./output"),
  REV_ID("Identifier of revision to be used", ""),

  ////////////////////////////////////////////////////////////////////////////////////////////////
  // Tasks

  TRANSLATE("Translates formal specifications", false, null, "task"),
  GENERATE("Generates test programs", false, null, "task"),
  GENERATE_TEMPLATE("Generates test templates", false, null, "task"),
  DISASSEMBLE("Disassembles binary files", false, null, "task"),
  SYMBOLIC_EXECUTE("Performs symbolic execution", false, null, "task"),
  TRANSFORM_TRACE("Transforms traces into templates", false, null, "task"),

  ////////////////////////////////////////////////////////////////////////////////////////////////
  // Translator Options

  INCLUDE("Directory that stores include files", "", TRANSLATE),
  EXTENSION_DIR("Directory that stores user-defined Java code", "", TRANSLATE),
  MODEL_NAME("Name of the constructed microprocessor model", "", TRANSLATE),

  ENABLE_ISA_MIR("Enables MIR output for current model ISA", false, TRANSLATE),

  ////////////////////////////////////////////////////////////////////////////////////////////////
  // Test Program Generation Options

  ARCH_DIRS("Home directories for tested architectures", "", GENERATE),

  RANDOM_SEED("Seed for randomizer", 0, GENERATE),
  SOLVER("Constraint solver engine to be used", "cvc4"),
  SOLVER_DEBUG("Enables debug mode for SMT solvers", false, GENERATE),

  PROGRAM_LENGTH_LIMIT("Maximum program length", 1000, GENERATE),
  TRACE_LENGTH_LIMIT("Maximum execution trace length", 1000, GENERATE),
  BRANCH_EXEC_LIMIT("Maximum execution count for an instruction", 100, GENERATE),
  RATE_LIMIT("Minimum generation rate", 0, GENERATE),


  FETCH_DECODE_ENABLED("Enables allocation, fetching and decoding of instructions", false,
      GENERATE),
  ASSERTS_ENABLED("Enables assertion checks during simulation", false, GENERATE),
  TRACER_LOG("Enables generation of Tracer logs for simulation", false, GENERATE),
  COVERAGE_LOG("Enables coverage trace generation", false, GENERATE),
  SELF_CHECKS("Enables inserting self-checks into test programs", false, GENERATE),
  DEFAULT_TEST_DATA("Enables generation of default test data", false, GENERATE),
  NO_SIMULATION("Disables simulation of generated code", false, GENERATE),
  DEBUG_PRINT("Enables printing detailed debug messages", false, GENERATE),
  COMMENTS_ENABLED("Enables generation of comments", false, GENERATE),
  COMMENTS_DEBUG("Enables generation of detailed comments, depends on --"
      + COMMENTS_ENABLED.getName(), false, GENERATE),
  TIME_STATISTICS("Enables printing time statistics", false, GENERATE),
  GENERATE_BINARY(
      "Enables generating binary files (limited functionality for debugging)", false, GENERATE),

  RESERVE_EXPLICIT(
      "Enables marking all explicitly specified registers as used", false, GENERATE),
  RESERVE_DEPENDENCIES(
      "Enables automated reservation of registers that have dependencies", false, GENERATE),

  BINARY_FILE_EXTENSION("Binary file extension", "bin", GENERATE),
  BINARY_FILE_BIG_ENDIAN("Use big endian for binary files", false),
  CODE_FILE_EXTENSION("Output file extension", "asm", GENERATE),
  CODE_FILE_PREFIX("Output file prefix", "test", GENERATE),
  DATA_FILE_EXTENSION("Data file extension", "asm", GENERATE),
  DATA_FILE_PREFIX("Data file prefix", "data", GENERATE),
  EXCEPT_FILE_PREFIX("Exception handler file prefix", "test_except", GENERATE),

  INDENT_TOKEN("Indentation text", "\t", GENERATE),
  COMMENT_TOKEN("Single-line comment text", "//", GENERATE),
  COMMENT_TOKEN_START("Text that starts a multiline comment", "/*", GENERATE),
  COMMENT_TOKEN_END("Text that ends a multiline comment", "*/", GENERATE),
  SEPARATOR_TOKEN("Text used to create separators", "=", GENERATE),

  ORIGIN_FORMAT("Origin directive format", ".org 0x%x", GENERATE),
  ALIGN_FORMAT("Alignment directive format", ".align %d", GENERATE),
  ALIGN_FORMAT2("Alignment directive format", ".align %d, 0x%02x", GENERATE),
  BYTE_ALIGN_FORMAT("Byte alignment directive format", ".balign %d", GENERATE),
  BYTE_ALIGN_FORMAT2("Byte alignment directive format", ".balign %d, 0x%02x", GENERATE),
  POWER2_ALIGN_FORMAT("Power of 2 alignment directive format", ".p2align %d", GENERATE),
  POWER2_ALIGN_FORMAT2("Power of 2 alignment directive format", ".p2align %d, 0x%02x", GENERATE),
  OPTION_FORMAT("Option directive format", ".option %s", GENERATE),
  GLOBAL_FORMAT("Global directive format", ".globl %s", GENERATE),
  WEAK_FORMAT("Weak directive format", ".weak %s", GENERATE),

  TEXT_SECTION_KEYWORD("Text section directive", ".text", GENERATE),
  DATA_SECTION_KEYWORD("Data section directive", ".data", GENERATE),

  INSTANCE_NUMBER("Number of processing element instances", 1, GENERATE),

  JRUBY_THREAD_POOL_MAX(
      "JRuby: maximum number of threads to allow in pool", Integer.MAX_VALUE, GENERATE),

  ////////////////////////////////////////////////////////////////////////////////////////////////
  // Template Generation Options

  BASE_TEMPLATE_NAME("Name of test template base class", "", GENERATE_TEMPLATE),
  BASE_TEMPLATE_PATH("Path to test template base class file", "", GENERATE_TEMPLATE),
  IGNORED_INSTRUCTIONS("Instructions to be ignored", "", GENERATE_TEMPLATE);

  ////////////////////////////////////////////////////////////////////////////////////////////////

  private static class Static {
    private static final Map<String, Option> NAMES = new HashMap<>();
    private static final Map<String, Option> SHORT_NAMES = new HashMap<>();
  }

  private final String name;
  private final String shortName;
  private final String description;
  private final Object defaultValue;
  private final Option dependency;
  private final String groupName;

  private Option(
      final String description,
      final Object defaultValue) {
    this(description, defaultValue, null);
  }

  private Option(
      final String description,
      final Object defaultValue,
      final Option dependency) {
    this(description, defaultValue, dependency, null);
  }

  private Option(
      final String description,
      final Object defaultValue,
      final Option dependency,
      final String groupName) {
    InvariantChecks.checkNotNull(description);

    this.name = name().toLowerCase().replaceAll("_", "-");
    this.shortName = makeUniqueShortName(name);

    this.description = description;
    this.defaultValue = defaultValue;
    this.dependency = dependency;
    this.groupName = groupName;

    Static.NAMES.put(name, this);
    Static.SHORT_NAMES.put(shortName, this);
  }

  private static String makeUniqueShortName(final String name) {
    final String[] nameTokens = name.split("-");

    final StringBuilder sb = new StringBuilder();
    for (final String token : nameTokens) {
      sb.append(token.charAt(0));
    }

    while (Static.SHORT_NAMES.containsKey(sb.toString())) {
      final String lastToken = nameTokens[nameTokens.length - 1];
      sb.append(lastToken.charAt(0));
    }

    return sb.toString();
  }

  public static Option fromName(final String name) {
    Option option = Static.NAMES.get(name);

    if (null == option) {
      option = Static.SHORT_NAMES.get(name);
    }

    if (null == option) {
      throw new IllegalArgumentException("No option with such name: " + name);
    }

    return option;
  }

  public String getName() {
    return name;
  }

  public String getShortName() {
    return shortName;
  }

  public String getDescription() {
    final StringBuilder sb = new StringBuilder(description);

    if (null != dependency) {
      sb.append(String.format(" [works with -%s]", dependency.getShortName()));
    }

    sb.append(", default=");
    sb.append(defaultValue instanceof String ? "\"" + defaultValue + "\"" : defaultValue);

    return sb.toString();
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public Class<?> getValueClass() {
    return defaultValue.getClass();
  }

  public Option getDependency() {
    return dependency;
  }

  public String getGroupName() {
    return groupName;
  }

  public boolean isFlag() {
    return defaultValue instanceof Boolean;
  }
}
