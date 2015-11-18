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

package ru.ispras.microtesk.mmu.translator.ir;

import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

public final class Operation {
  private final String id;
  private final Address address;
  private final Variable addressArg;
  private final List<Stmt> stmts;

  public Operation(
      final String id,
      final Address address,
      final Variable addressArg,
      final List<Stmt> stmts) {
    InvariantChecks.checkNotNull(id);
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(addressArg);
    InvariantChecks.checkNotNull(stmts);

    this.id = id;
    this.address = address;
    this.addressArg = addressArg;
    this.stmts = Collections.unmodifiableList(stmts);
  }

  public String getId() {
    return id;
  }

  public Address getAddress() {
    return address;
  }

  public Variable getAddressArg() {
    return addressArg;
  }

  public List<Stmt> getStmts() {
    return stmts;
  }

  @Override
  public String toString() {
    return String.format(
        "op %s(%s), stmts=%s",
        getId(),
        getAddressArg(),
        stmts
        );
  }
}
