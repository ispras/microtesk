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

package ru.ispras.microtesk.translator.nml.ir.analysis;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalkerFlow;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFunctionCall;

public final class ExceptionDetector implements TranslatorHandler<Ir> {
  @Override
  public void processIr(final Ir ir) {
    final IrWalkerFlow walker = new IrWalkerFlow(ir, new Visitor());
    walker.visit();
  }

  private static final class Visitor extends IrVisitorDefault {
    private PrimitiveAND primitive;
    private Shortcut shortcut;
    private PrimitiveInfo info;

    public Visitor() {
      this.primitive = null;
      this.shortcut = null;
      this.info = null;
    }

    private PrimitiveInfo getInfo() {
      InvariantChecks.checkNotNull(info);
      return info;
    }

    @Override
    public void onPrimitiveBegin(final Primitive item) {
      InvariantChecks.checkFalse(item.isOrRule());

      if (this.primitive == null) {
        InvariantChecks.checkTrue(this.shortcut == null);
        InvariantChecks.checkTrue(this.info == null);

        this.primitive = (PrimitiveAND) item;
        this.info = this.primitive.getInfo();
      } else {
        InvariantChecks.checkNotNull(this.shortcut);
        InvariantChecks.checkTrue(this.info == this.shortcut.getInfo());
      }
    }

    @Override
    public void onPrimitiveEnd(final Primitive item) {
      InvariantChecks.checkFalse(item.isOrRule());

      if (this.shortcut == null) {
        InvariantChecks.checkNotNull(this.primitive);
        InvariantChecks.checkTrue(this.info == this.primitive.getInfo());

        this.primitive = null;
        this.info = null;
      } else {
        InvariantChecks.checkTrue(this.primitive != item);
        InvariantChecks.checkTrue(this.info == this.shortcut.getInfo());
      }

      if (isStatus(Status.SKIP)) {
        setStatus(Status.OK);
      }
    }

    @Override
    public void onShortcutBegin(final PrimitiveAND andRule, final Shortcut shortcut) {
      InvariantChecks.checkTrue(this.primitive == andRule);
      InvariantChecks.checkTrue(this.shortcut == null);

      this.shortcut = shortcut;
      this.info = shortcut.getInfo();
    }

    @Override
    public void onShortcutEnd(final PrimitiveAND andRule, final Shortcut shortcut) {
      InvariantChecks.checkTrue(this.primitive == andRule);
      InvariantChecks.checkTrue(this.shortcut == shortcut);

      this.shortcut = null;
      this.info = andRule.getInfo();
    }

    @Override
    public void onAttributeEnd(final PrimitiveAND andRule, final Attribute attr) {
      if (isStatus(Status.SKIP) &&
          this.primitive == andRule &&
          attr.getName().equals(Attribute.ACTION_NAME)) {
       setStatus(Status.OK);
      }
    }

    @Override
    public void onFunctionCall(final StatementFunctionCall stmt) {
      if (isException(stmt)) {
        getInfo().setCanThrowException(true);
        setStatus(Status.SKIP);
      }
    }

    private static boolean isException(final StatementFunctionCall stmt) {
      return "exception".equals(stmt.getName());
    }
  }
}
