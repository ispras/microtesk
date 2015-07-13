/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.antlrex;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.fortress.util.InvariantChecks.checkTrue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.translator.Translator;

public class Preprocessor {
  private static enum IfDefScope {
    IF_TRUE,
    IF_FALSE,
    ELSE_TRUE,
    ELSE_FALSE
  }

  private final Translator<?> translator; 

  private final IncludeFileFinder finder = new IncludeFileFinder();
  private final Map<String, String> defines = new LinkedHashMap<>();
  private final Deque<IfDefScope> ifdefs = new ArrayDeque<>();

  public Preprocessor(final Translator<?> translator) {
    checkNotNull(translator);
    this.translator = translator;
  }

  public void includeTokensFromFile(final String filename) {
    final CharStream stream = this.tokenStreamFromFile(filename);
    if (null == stream) {
      Logger.error("INCLUDE FILE '" + filename + "' HAS NOT BEEN FOUND.");
      return;
    }

    Logger.message("Included: " + filename);
    translator.startLexer(stream);
  }

  public void includeTokensFromString(final String substitution) {
    final CharStream stream = this.tokenStreamFromString(substitution);
    if (stream != null) {
      translator.startLexer(stream);
    }
  }

  protected CharStream tokenStreamFromFile(final String filename) {
    return finder.openFile(filename);
  }

  protected CharStream tokenStreamFromString(final String s) {
    if (s != null && !s.isEmpty()) {
      return new ANTLRStringStream(s);
    }
    return null;
  }

  public void addPath(final String path) {
    checkNotNull(path);

    finder.addPaths(path);
  }

  public boolean isDefined(final String key) {
    return defines.containsKey(key.trim());
  }

  public boolean underIfElse() {
    return !ifdefs.isEmpty();
  }

  public boolean isHidden() {
    if (underIfElse()) {
      final IfDefScope scope = ifdefs.peek();
      return scope == IfDefScope.IF_FALSE || scope == IfDefScope.ELSE_FALSE;
    }

    return false;
  }

  public void onDefine(final String key, final String val) {
    final int index = val.indexOf("//");
    final String value = index == -1 ? val : val.substring(0, index);

    defines.put(key.trim(), value.trim());
  }

  public void onUndef(final String key) {
    defines.remove(key.trim());
  }

  public void onIfdef(final String key) {
    if (isHidden() || !isDefined(key)) {
      ifdefs.push(IfDefScope.IF_FALSE);
    } else {
      ifdefs.push(IfDefScope.IF_TRUE);
    }
  }

  public void onIfndef(final String key) {
    if (isHidden() || isDefined(key)) {
      ifdefs.push(IfDefScope.IF_FALSE);
    } else {
      ifdefs.push(IfDefScope.IF_TRUE);
    }
  }

  public void onElse() {
    checkTrue(underIfElse());

    final IfDefScope scope = ifdefs.pop();
    checkTrue(scope == IfDefScope.IF_TRUE || scope == IfDefScope.IF_FALSE);

    if (isHidden() || scope == IfDefScope.IF_TRUE) {
      ifdefs.push(IfDefScope.ELSE_FALSE);
    } else {
      ifdefs.push(IfDefScope.ELSE_TRUE);
    }
  }

  public void onEndif() {
    checkTrue(underIfElse());
    ifdefs.pop();
  }

  public String expand(final String key) {
    return defines.get(key.trim());
  }
}
