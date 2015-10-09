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

package ru.ispras.microtesk.translator.nml.ir.expression;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.expression.Node;
import ru.ispras.microtesk.translator.nml.ir.location.Location;
import ru.ispras.microtesk.translator.nml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.nml.ir.valueinfo.ValueInfo;

/**
 * The NodeInfo class is used as an additional custom attribute of a Fortress expression node that
 * provides additional information on the node and the subexpression it represents.
 * 
 * It has the following important attributes:
 * 
 * <pre>
 * - Kind (kind of element representing the node, determines the set of maintained attributes).
 * - Source (Location, NamedConstant, Constant, Operator (including conditions), depending on Kind).
 * - ValueInfo (current, resulting, top-level, final value).
 * </pre>
 * 
 * Coercions (explicit casts) can be applied zero or more times to all element kinds. To support
 * them, the following attributes are included:
 * 
 * <pre>
 * - PreviousValueInfo, array of ValueInfo: first is value before final coercion, last is initial
 *   value before first coercion. Value after the final coercion is ValueInfo (current).
 * - CoercionChain, based on PreviousValueInfo, a list of applied coercions.
 * </pre>
 * 
 * Also, operators have an additional attribute related to coercion that goes to the 'source'
 * object:
 * 
 * <pre>
 *  - CastValueInfo (operands are cast to a common type (implicit cast), if their types are different).
 * </pre>
 * 
 * @author Andrei Tatarnikov
 */

public final class NodeInfo {
  /**
   * Identifies the language element that serves as as basis for the given expression node.
   * 
   * @author Andrei Tatarnikov
   */

  public static enum Kind {
    LOCATION (Location.class, Node.Kind.VARIABLE),
    NAMED_CONST (LetConstant.class, Node.Kind.VALUE),
    CONST (SourceConstant.class, Node.Kind.VALUE),
    OPERATOR (SourceOperator.class, Node.Kind.OPERATION);

    private final Class<?> sourceClass;
    private final Node.Kind nodeKind;

    private Kind(Class<?> sourceClass, Node.Kind nodeKind) {
      this.sourceClass = sourceClass;
      this.nodeKind = nodeKind;
    }

    /**
     * Checks whether the specified object can be used as a source for the current element kind.
     * 
     * @param source Source object.
     * @return <code>true</code> if the specified object is a compatible source or
     *         <code>false</code> otherwise.
     * 
     * @throws NullPointerException if the parameter is null.
     */

    boolean isCompatibleSource(Object source) {
      checkNotNull(source);
      return this.sourceClass.isAssignableFrom(source.getClass());
    }

    /**
     * Checks whether the current element kind can be associated with the specified Fortress
     * expression node kind.
     * 
     * @param nodeKind Fortress expression node kind.
     * @return <code>true</code> if the specified Fortress expression node is compatible with the
     *         element kind or <code>false</code> otherwise.
     * 
     * @throws NullPointerException if the parameter is null.
     */

    boolean isCompatibleNode(Node.Kind nodeKind) {
      checkNotNull(nodeKind);
      return this.nodeKind == nodeKind;
    }
  }

  public static enum CoercionType {
    IMPLICIT("coerce"),

    SIGN_EXTEND("sign_extend"),
    ZERO_EXTEND("zero_extend"),

    COERCE("coerce"),
    CAST("cast"),

    INT_TO_FLOAT("int_to_float"),
    FLOAT_TO_INT("float_to_int"),
    FLOAT_TO_FLOAT("float_to_float");

    private final String methodName;
    private CoercionType(String methodName) {
      this.methodName = methodName;
    }

    public String getMethodName() {
      return methodName;
    }
  }

  /**
   * Creates a node information object basing on a Location object.
   * 
   * @param source Location object.
   * @return A new node information object.
   * 
   * @throws NullPointerException if the parameter is null.
   */

  public static NodeInfo newLocation(Location source) {
    checkNotNull(source);
    return new NodeInfo(NodeInfo.Kind.LOCATION, source, ValueInfo.createModel(source.getType()));
  }

  /**
   * Creates a node information object basing on a named constant (LetConstant object).
   * 
   * @param source Named constant.
   * @return A new node information object.
   * 
   * @throws NullPointerException if the parameter is null.
   */

  static NodeInfo newNamedConst(LetConstant source) {
    checkNotNull(source);
    return new NodeInfo(NodeInfo.Kind.NAMED_CONST, source, source.getExpr().getValueInfo());
  }

  /**
   * Creates a node information object basing on a constant value.
   * 
   * @param source Object describing the constant (value and radix).
   * @return A new node information object.
   * 
   * @throws NullPointerException if the parameter is null.
   */

  static NodeInfo newConst(SourceConstant source) {
    checkNotNull(source);
    return new NodeInfo(NodeInfo.Kind.CONST, source, ValueInfo.createNative(source.getValue()));
  }

  /**
   * Creates a node information object basing on an operator.
   * 
   * @param source Operator information (identifier, result type and type the parameters should be
   *        cast to).
   * @return A new node information object.
   * 
   * @throws NullPointerException if the parameter is null.
   */

  static NodeInfo newOperator(SourceOperator source) {
    checkNotNull(source);
    return new NodeInfo(NodeInfo.Kind.OPERATOR, source, source.getResultValueInfo());
  }

  private final Kind kind;
  private final Object source;
  private final ValueInfo currentVI;
  private final List<ValueInfo> previousVI;
  private final List<CoercionType> coercionTypes;

  /**
   * Constructs a node information object from the specified attributes.
   * 
   * @param kind Kind of the language element the expression node is based on.
   * @param source Source object that contains information on the source language element.
   * @param current Current value information.
   * @param previous History of value information before coercions were applied (empty, if no
   *        coercions were applied).
   * 
   * @throws IllegalArgumentException if the source is not compatible with the element kind.
   */

  private NodeInfo(
      final Kind kind,
      final Object source,
      final ValueInfo current,
      final List<ValueInfo> previous,
      final List<CoercionType> coercionTypes) {
    if (!kind.isCompatibleSource(source)) {
      throw new IllegalArgumentException(String.format(
        "%s is not proper source for %s.", source.getClass().getSimpleName(), kind));
    }

    this.kind = kind;
    this.source = source;
    this.currentVI = current;
    this.previousVI = Collections.unmodifiableList(previous);
    this.coercionTypes = Collections.unmodifiableList(coercionTypes);
  }

  /**
   * Constructs a node information object from the specified attributes. A shorter version of the
   * main constructor. Designed to construct nodes that do not include any coercions.
   * 
   * @param kind Kind of the language element the expression node is based on.
   * @param source Source object that contains information on the source language element.
   * @param current Current value information.
   * 
   * @throws IllegalArgumentException if the source is not compatible with the element kind.
   */

  private NodeInfo(final Kind kind, final Object source, final ValueInfo current) {
    this(
        kind,
        source,
        current,
        Collections.<ValueInfo>emptyList(),
        Collections.<CoercionType>emptyList()
    );
  }

  public NodeInfo coerceTo(final ValueInfo newValueInfo, final CoercionType coercionType) {
    checkNotNull(newValueInfo);
    checkNotNull(coercionType);

    if (getValueInfo().equals(newValueInfo)) {
      return this;
    }

    final List<ValueInfo> previous = new ArrayList<>(this.previousVI.size() + 1);
    previous.add(getValueInfo());
    previous.addAll(this.previousVI);

    final List<CoercionType> coercions = new ArrayList<>(this.coercionTypes.size() + 1);
    coercions.add(coercionType);
    coercions.addAll(this.coercionTypes);

    return new NodeInfo(getKind(), getSource(), newValueInfo, previous, coercions);
  }

  public Kind getKind() {
    return kind;
  }

  public Object getSource() {
    return source;
  }

  public ValueInfo getValueInfo() {
    return currentVI;
  }

  /**
   * Checks if the node information object contains information about coercions that were applied to
   * associated expression node.
   * 
   * @return <code>true</code> if any coercions were applied to the associated expression node or
   *         <code>false</code> otherwise.
   */

  public boolean isCoersionApplied() {
    return !previousVI.isEmpty();
  }

  /**
   * Returns the history of value information associated with the expression node before coercions
   * were applied. If no coercions were applied, returns an empty list.
   * 
   * @return Previous value information.
   */

  public List<ValueInfo> getPreviousValueInfo() {
    return previousVI;
  }

  public List<ValueInfo> getCoercionChain() {
    if (!isCoersionApplied()) {
      return Collections.<ValueInfo>emptyList();
    }

    final List<ValueInfo> result = new ArrayList<>(previousVI.size());
    result.add(getValueInfo().typeInfoOnly());

    for (int index = 0; index < previousVI.size() - 1; ++index) {
      result.add(previousVI.get(index).typeInfoOnly());
    }

    return Collections.unmodifiableList(result);
  }

  public List<CoercionType> getCoercionTypes() {
    return coercionTypes;
  }
}
