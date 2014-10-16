/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

/**
 * The LabelReference class describes a reference to a label. This means a label specified as an
 * argument of a control-transfer instruction. The important point is that a reference is not linked
 * to a specific label, it just provides information used for label lookup during simulation. There
 * may be several labels with the same name located in different blocks. Which one will be chosen
 * for control transfer will be chosen depending on the context (see
 * {@link ru.ispras.microtesk.test.LabelManager}).
 * 
 * @author Andrei Tatarnikov
 */

public final class LabelReference {
  private final Label reference;
  private final Primitive primitive;
  private final String argumentName;
  private final int argumentValue;

  public static final class Target
  {
    private final Label label;
    private final int position;

    private Target(Label label, int position) {
      this.label = label;
      this.position = position;
    }

    public Label getLabel() {
      return label;
    }

    public int getPosition() {
      return position;
    }
  }

  private Target target = null;

  /**
   * Constructs a label reference object.
   * 
   * @param labelName Name of the referred label.
   * @param blockId Identifier of the block from which the reference is made.
   * @param primitive Primitive (OP or MODE) the label reference was passed to as an
   *        argument.
   * @param argumentName Name of the primitive (OP or MODE) argument the label reference is
   *        associated with.
   * @param argumentValue Value assigned (instead of a real address or offset) to the primitive
   *        argument (OP or MODE )the label reference is associated with.
   * 
   * @throws NullPointerException if any of the following arguments is {@code null}: labelName,
   *         blockId or argumentName.
   */

  LabelReference(String labelName, BlockId blockId, Primitive primitive, String argumentName,
      int argumentValue) {
    if (null == labelName) {
      throw new NullPointerException();
    }

    if (null == blockId) {
      throw new NullPointerException();
    }

    if (null == primitive) {
      throw new NullPointerException();
    }

    if (null == argumentName) {
      throw new NullPointerException();
    }

    this.reference = new Label(labelName, blockId);
    this.primitive = primitive;
    this.argumentName = argumentName;
    this.argumentValue = argumentValue;
  }

  /**
   * Return a {@link Label} object that describes a reference to a label with a specific name made
   * from a specific block. There is no correspondence between the returned label and the actual
   * label that will be chosen for control transfer. It just provides context that helps choose the
   * most suitable label.
   * 
   * @return Label object describing a reference to a label.
   */

  public Label getReference() {
    return reference;
  }

  /**
   * The primitive (OP or MODE) the label reference was passed to as an
   * argument.
   * 
   * @return Primitive the label reference was passed to.
   */

  public Primitive getPrimitive() {
    return primitive;
  }

  /**
   * Returns the name of the primitive (OP or MODE) argument the label reference is associated with.
   * 
   * @return Name of the associated primitive argument.
   */

  public String getArgumentName() {
    return argumentName;
  }

  /**
   * Returns the value assigned (instead of a real address or offset) to the primitive (OP or MODE)
   * argument the label reference is associated with.
   * 
   * @return Value assigned to the associated primitive argument.
   */

  public int getArgumentValue() {
    return argumentValue;
  }

  public Target getTarget() {
    return target;
  }

  public void setTarget(Label label, int position) {
    if (null == label) {
      throw new NullPointerException();
    }

    if (position < 0) {
      throw new IllegalArgumentException();
    }

    target = new Target(label, position);
  }

  public void resetTarget() {
    target = null;
  }

  @Override
  public String toString() {
    return String.format("Reference: %s (passed to %s via the %s paramever with value %d)",
      reference, primitive.getName(), argumentName, argumentValue);
  }
}
