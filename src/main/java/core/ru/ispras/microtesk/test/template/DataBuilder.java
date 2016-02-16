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

package ru.ispras.microtesk.test.template;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

public class DataBuilder {
  private final List<DataDirective> directives;
  private final DataDirectiveFactory directiveFactory;
  private final boolean isSeparateFile;

  protected DataBuilder(
      final DataDirectiveFactory directiveFactory,
      final boolean isSeparateFile) {
    InvariantChecks.checkNotNull(directiveFactory);

    this.directives = new ArrayList<>();
    this.directiveFactory = directiveFactory;
    this.isSeparateFile = isSeparateFile;
  }

  private void registerDirective(final DataDirective directive) {
    directive.apply();
    directives.add(directive);
  }

  /**
   * Sets allocation origin. Inserts the ".org" directive in the test program.
   */
  public void setOrigin(final BigInteger origin) {
    registerDirective(directiveFactory.newOrigin(origin));
  }

  /**
   * @param value Alignment amount in addressable units.
   */
  public void align(final BigInteger value, final BigInteger valueInBytes) {
    registerDirective(directiveFactory.newAlign(value, valueInBytes));
  }

  public void addLabel(final String id) {
    registerDirective(directiveFactory.newLabel(id, isSeparateFile));
  }

  public void addText(final String text) {
    registerDirective(directiveFactory.newText(text));
  }

  public void addComment(final String text) {
    registerDirective(directiveFactory.newComment(text));
  }

  public void addData(final String id, final BigInteger[] values) {
    registerDirective(directiveFactory.newData(id, values));
  }

  public void addSpace(final int length) {
    registerDirective(directiveFactory.newSpace(length));
  }

  public void addAsciiStrings(final boolean zeroTerm, final String[] strings) {
    registerDirective(directiveFactory.newAsciiStrings(zeroTerm, strings));
  }

  public List<DataDirective> build() {
    return directives;
  }
}
