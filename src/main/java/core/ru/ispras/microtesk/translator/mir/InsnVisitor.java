/*
    Copyright 2019 ISP RAS (http://www.ispras.ru)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package ru.ispras.microtesk.translator.mir;

import static ru.ispras.microtesk.translator.mir.Instruction.*;

public class InsnVisitor {
  public void visit(final Assignment insn) {
  }

  public void visit(final Concat insn) {
  }

  public void visit(final Extract insn) {
  }

  public void visit(final Sext insn) {
  }

  public void visit(final Zext insn) {
  }

  public void visit(final Branch insn) {
  }

  public void visit(final Return insn) {
  }

  public void visit(final Instruction.Exception insn) {
  }

  public void visit(final Call insn) {
  }

  public void visit(final Invoke insn) {
  }

  public void visit(final Load insn) {
  }

  public void visit(final Store insn) {
  }

  public void visit(final Disclose insn) {
  }

  public void visit(final Conditional insn) { }

  public void visit(final GlobalNumbering.Phi insn) { }
  public void visit(final GlobalNumbering.SsaStore insn) { }
}
