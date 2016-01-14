/*
 * Copyright 2014-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.antlrex.symbols;

public final class ReservedKeywords {
  private ReservedKeywords() {}

  public static final String[] JAVA = {
    // Reserved language keywords.
    "abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package",
    "synchronized", "boolean", "do", "if", "private", "this", "break", "double", "implements",
    "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum",
    "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char",
    "final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile",
    "const", "float", "native", "super", "while",

    // Class names from the "java.lang" package.
    "Boolean", "Integer", "Double"
    // TODO: Add names that can cause conflicts with generated constructs.
  };

  public static final String[] RUBY = {
    // TODO: Reserved language keywords here.
    // TODO: Add names that can cause conflicts with generated constructs.
  };
}
