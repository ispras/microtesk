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
  private final boolean separateFile;

  protected DataBuilder(
      final DataDirectiveFactory directiveFactory,
      final boolean isSeparateFile) {
    InvariantChecks.checkNotNull(directiveFactory);

    this.directives = new ArrayList<>();
    this.directiveFactory = directiveFactory;
    this.separateFile = isSeparateFile;
  }

  public boolean isSeparateFile() {
    return separateFile;
  }

  private void addDirective(final DataDirective directive) {
    directive.apply();
    directives.add(directive);
  }

  /**
   * Sets allocation origin. Inserts the ".org" directive in the test program.
   */
  public void setOrigin(final BigInteger origin) {
    addDirective(directiveFactory.newOrigin(origin));
  }

  /**
   * @param value Alignment amount in addressable units.
   */
  public void align(final BigInteger value, final BigInteger valueInBytes) {
    addDirective(directiveFactory.newAlign(value, valueInBytes));
  }

  public void addLabel(final String id) {
    addDirective(directiveFactory.newLabel(id, separateFile));
  }

  public void addText(final String text) {
    addDirective(directiveFactory.newText(text));
  }

  public void addComment(final String text) {
    addDirective(directiveFactory.newComment(text));
  }

  public void addData(final String id, final BigInteger[] values) {
    addDirective(directiveFactory.newData(id, values));
  }

  public void addSpace(final int length) {
    addDirective(directiveFactory.newSpace(length));
  }

  public void addAsciiStrings(final boolean zeroTerm, final String[] strings) {
    addDirective(directiveFactory.newAsciiStrings(zeroTerm, strings));
  }

  public List<DataDirective> build() {
    return directives;
  }
}
