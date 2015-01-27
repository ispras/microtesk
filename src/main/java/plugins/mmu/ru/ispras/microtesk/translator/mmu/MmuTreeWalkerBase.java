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

package ru.ispras.microtesk.translator.mmu;

import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.TreeNodeStream;

import ru.ispras.microtesk.translator.antlrex.IErrorReporter;
import ru.ispras.microtesk.translator.antlrex.TreeParserBase;
import ru.ispras.microtesk.translator.mmu.ir.Ir;

public class MmuTreeWalkerBase extends TreeParserBase {
  private Ir ir;

  public MmuTreeWalkerBase(TreeNodeStream input, RecognizerSharedState state) {
    super(input, state);
    this.ir = null;
  }

  public final IErrorReporter getReporter() {
    return this;
  }

  public final void assignIR(Ir ir) {
    this.ir = ir;
  }

  public final Ir getIR() {
    return ir;
  }
}
