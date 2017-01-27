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

package ru.ispras.microtesk.options;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@link Option} enumeration describes options.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public enum Option {
  ////////////////////////////////////////////////////////////////////////////////////////////////
  // Common Options

  HELP("help", "Shows help message", false),
  VERBOSE("verbose", "Enables printing diagnostic messages", false),
  TRANSLATE("translate", "Translates formal specifications", false, null, "task"),
  GENERATE("generate", "Generates test programs", false, null, "task"),
  DISASSEMBLE("disassemble", "Disassembles binary files", false, null, "task"),
  SYMBOLIC_EXECUTE("symbolic-execute", "Performs symbolic execution", false, null, "task"),
  OUTDIR("output-dir", "Directory to place generated files", "./output"),

  ////////////////////////////////////////////////////////////////////////////////////////////////
  // Translator Options

  INCLUDE("include", "Directory that stores include files", "", TRANSLATE),
  EXTDIR("extension-dir", "Directory that stores user-defined Java code", "", TRANSLATE),

  ////////////////////////////////////////////////////////////////////////////////////////////////
  // Test Program Generation Options

  ARCH_DIRS("arch-dirs", "Home directories for tested architectures", "", GENERATE),

  RANDOM("random-seed", "Seed for randomizer", 0, GENERATE),
  SOLVER("solver", "Constraint solver engine to be used", "cvc4"),
  SOLVER_DEBUG("solver-debug", "Enables debug mode for SMT solvers", false, GENERATE),

  CODE_LIMIT("program-length-limit", "Maximum program length", 1000, GENERATE),
  TRACE_LIMIT("trace-length-limit", "Maximum execution trace length", 1000, GENERATE),
  BRANCH_LIMIT("branch-exec-limit", "Maximum execution count for an instruction", 100, GENERATE),
  RATE_LIMIT("rate-limit", "Minimum generation rate", 0, GENERATE),

  TARMAC_LOG("tarmac-log", "Enables generation of Tarmac logs for simulation", false, GENERATE),
  SELF_CHECKS("self-checks", "Enables inserting self-checks into test programs", false, GENERATE),
  DEFAULT_TEST_DATA("default-test-data", "Enables generation of default test data", false, GENERATE),
  NO_SIMULATION("no-simulation", "Disables simulation of generated code", false, GENERATE),
  COMMENTS_ENABLED("comments-enabled", "Enables generation of comments", false, GENERATE),
  COMMENTS_DEBUG("comments-debug", "Enables generation of detailed comments, depends on --" +
      COMMENTS_ENABLED.getName(), false, GENERATE),
  TIME_STATISTICS("time-statistics", "Enables printing time statistics", false, GENERATE),
  RESERVE_EXPLICIT("reserve-explicit", "Enables reservation of explicitly specified mode arguments",
      false, GENERATE),
  GENERATE_BINARY("generate-binary", "Enables generating binary files of test programs. " + 
      "Limited functionality. Required for debugging.", false, GENERATE),

  BIN_EXT("binary-file-extension", "Binary file extension", "bin", GENERATE),
  CODE_EXT("code-file-extension", "Output file extension", "asm", GENERATE),
  CODE_PRE("code-file-prefix", "Output file prefix", "test", GENERATE),
  DATA_EXT("data-file-extension", "Data file extension", "dat", GENERATE),
  DATA_PRE("data-file-prefix", "Data file prefix", "asm", GENERATE),
  EXCEPT_PRE("exception-file-prefix", "Exception handler file prefix", "test_except", GENERATE),

  INDENT_TOKEN("indent-token", "Indentation text", "\t", GENERATE),
  COMMENT_TOKEN("comment-token", "Single-line comment text", "//", GENERATE),
  COMMENT_TOKEN_START("comment-token-start", "Text that starts a multiline comment", "/*", GENERATE),
  COMMENT_TOKEN_END("comment-token-end", "Text that ends a multiline comment", "*/", GENERATE),
  SEPARATOR_TOKEN("separator-token", "Text used to create separators", "=", GENERATE),
  ORIGIN_FORMAT("origin-format" , "Origin directive format", ".org 0x%x", GENERATE),
  ALIGN_FORMAT("align-format", "Alignment directive format", ".align %d", GENERATE),
  CODE_SECTION_KEYWORD("code-section-keyword" , "Code section directive", ".text", GENERATE),
  DATA_SECTION_KEYWORD("data-section-keyword", "Data section directive", ".data", GENERATE),

  BASE_VA("base-virtual-address", "Base VA for memory allocation", BigInteger.ZERO, GENERATE),
  BASE_PA("base-physical-address", "Base PA for memory allocation", BigInteger.ZERO, GENERATE),
  BASE_VA_DATA("base-virtual-address-data", "Base VA for data memory allocation",
      BASE_VA.getDefaultValue(), GENERATE),

  INSTANCE_NUMBER("instance-number", "Number of processing element instances", 1, GENERATE);

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
      final String name,
      final String description,
      final Object defaultValue) {
    this(name, description, defaultValue, null);
  }

  private Option(
      final String name,
      final String description,
      final Object defaultValue,
      final Option dependency) {
    this(name, description, defaultValue, dependency, null);
  }

  private Option(
      final String name,
      final String description,
      final Object defaultValue,
      final Option dependency,
      final String groupName) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(description);

    if (Static.NAMES.containsKey(name)) {
      throw new IllegalArgumentException(String.format("--%s is already used!", name));
    }

    this.name = name;
    this.shortName = makeUniqueShortName(name);

    this.description = description;
    this.defaultValue = defaultValue;
    this.dependency = dependency;
    this.groupName = groupName; 

    Static.SHORT_NAMES.put(shortName, this);
    Static.NAMES.put(name, this);
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
