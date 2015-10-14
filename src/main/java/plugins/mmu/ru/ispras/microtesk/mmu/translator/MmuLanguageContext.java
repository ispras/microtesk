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

package ru.ispras.microtesk.mmu.translator;

import org.antlr.runtime.tree.CommonTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public enum MmuLanguageContext {
  BUFFER_TYPE("register", "memory") {
    @Override
    public CheckResult checkKeywords(final Collection<CommonTree> keywords) {
      return atMostNUnique(1, keywords);
    }
  };
  
  public static final class CheckResult {
    private final String message;
    private final CommonTree source;
    private final List<String> result;

    public CheckResult(final List<String> keywords) {
      this.message = "Passed";
      this.source = null;
      this.result = keywords;
    }

    public CheckResult(final CommonTree source, final String message) {
      this.message = message;
      this.source = source;
      this.result = Collections.emptyList();
    }

    public boolean isSuccess() {
      return source == null;
    }

    public String getMessage() {
      return message;
    }

    public CommonTree getSource() {
      return source;
    }

    public List<String> getResult() {
      return result;
    }
  }

  public abstract CheckResult checkKeywords(final Collection<CommonTree> keywords);

  private final Collection<String> expected;

  private MmuLanguageContext(final String... keywords) {
    this.expected = Arrays.asList(keywords);
  }

  protected final CheckResult atMostNUnique(int n, final Collection<CommonTree> input) {
    final ArrayList<String> observed = new ArrayList<>(input.size());
    for (final CommonTree node : input) {
      final String keyword = node.getText();
      if (!expected.contains(keyword)) {
        return new CheckResult(node, "Unexpected token: " + keyword);
      }
      if (observed.contains(keyword)) {
        return new CheckResult(node, "Duplicate token: " + keyword);
      }
      if (observed.size() >= n) {
        return new CheckResult(node, String.format("Too many tokens: max %d expected", n));
      }
      observed.add(keyword);
    }
    return new CheckResult(observed);
  }
}
