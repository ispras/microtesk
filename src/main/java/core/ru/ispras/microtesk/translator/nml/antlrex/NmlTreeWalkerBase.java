/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.antlrex;

import java.util.Map;

import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.TreeNodeStream;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.antlrex.ErrorReporter;
import ru.ispras.microtesk.translator.antlrex.TreeParserBase;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.expression.ExprFactory;
import ru.ispras.microtesk.translator.nml.ir.location.LocationFactory;
import ru.ispras.microtesk.translator.nml.ir.primitive.AttributeFactory;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveFactory;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFactory;
import ru.ispras.microtesk.translator.nml.ir.shared.LetFactory;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExprFactory;
import ru.ispras.microtesk.translator.nml.ir.shared.TypeFactory;

public class NmlTreeWalkerBase extends TreeParserBase implements WalkerContext {
  private Ir ir;
  private Map<String, Primitive> thisArgs;
  private Primitive.Holder thisPrimitive;

  public NmlTreeWalkerBase(
      final TreeNodeStream input,
      final RecognizerSharedState state) {
    super(input, state);

    this.ir = null;
    this.thisArgs = null;
    this.thisPrimitive = null;
  }

  @Override
  public final ErrorReporter getReporter() {
    return this;
  }

  public final void assignIR(final Ir ir) {
    this.ir = ir;
  }

  @Override
  public final Ir getIR() {
    return ir;
  }

  protected final void setThisArgs(final Map<String, Primitive> value) {
    InvariantChecks.checkNotNull(value);
    this.thisArgs = value;
  }

  protected final void resetThisArgs() {
    this.thisArgs = null;
  }

  @Override
  public final Map<String, Primitive> getThisArgs() {
    return thisArgs;
  }

  protected final void reserveThis() {
    assert null == thisPrimitive;
    thisPrimitive = new Primitive.Holder();
  }

  protected final void finalizeThis(final Primitive value) {
    assert null != thisPrimitive;
    thisPrimitive.setValue(value);
    thisPrimitive = null;
  }

  @Override
  public final Primitive.Holder getThis() {
    return thisPrimitive;
  }

  /* ====================================================================================== */
  /* Factories of Semantic Elements that Make Up Intermediate Data to Be Used by */
  /* code generators (emitters). */
  /* ====================================================================================== */

  private ExprFactory exprFactory = null;
  private LetFactory letFactory = null;
  private LocationFactory locationFactory = null;
  private TypeFactory typeFactory = null;
  private MemoryExprFactory memoryExprFactory = null;
  private PrimitiveFactory primitiveFactory = null;
  private AttributeFactory attributeFactory = null;
  private StatementFactory statementFactory = null;

  protected final ExprFactory getExprFactory() {
    if (null == exprFactory) {
      exprFactory = new ExprFactory(this);
    }
    return exprFactory;
  }

  protected final LetFactory getLetFactory() {
    if (null == letFactory) {
      letFactory = new LetFactory(this);
    }
    return letFactory;
  }

  protected final LocationFactory getLocationFactory() {
    if (null == locationFactory) {
      locationFactory = new LocationFactory(this);
    }
    return locationFactory;
  }

  protected final TypeFactory getTypeFactory() {
    if (null == typeFactory) {
      typeFactory = new TypeFactory(this);
    }
    return typeFactory;
  }

  protected final MemoryExprFactory getMemoryExprFactory() {
    if (null == memoryExprFactory) {
      memoryExprFactory = new MemoryExprFactory(this);
    }
    return memoryExprFactory;
  }

  protected final PrimitiveFactory getPrimitiveFactory() {
    if (null == primitiveFactory) {
      primitiveFactory = new PrimitiveFactory(this);
    }
    return primitiveFactory;
  }

  protected final AttributeFactory getAttributeFactory() {
    if (null == attributeFactory) {
      attributeFactory = new AttributeFactory(this);
    }
    return attributeFactory;
  }

  protected final StatementFactory getStatementFactory() {
    if (null == statementFactory) {
      statementFactory = new StatementFactory(this);
    }
    return statementFactory;
  }
}
