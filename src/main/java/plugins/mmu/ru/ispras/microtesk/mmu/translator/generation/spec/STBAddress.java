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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Address;

final class STBAddress extends STBCommon {
  private final Address address;

  protected STBAddress(final String packageName, final Address address) {
    super(packageName);

    InvariantChecks.checkNotNull(address);
    this.address = address;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");
    buildHeader(st);
    return st;
  }

  @Override
  protected String getClassName() {
    return address.getId();
  }
}
