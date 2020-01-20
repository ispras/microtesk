/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model;

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.Location;
import ru.ispras.microtesk.test.template.LabelReference;

import java.util.LinkedHashMap;
import java.util.Map;

import static ru.ispras.microtesk.model.Execution.CALL_STACK;
import static ru.ispras.microtesk.translator.nml.ir.primitive.Primitive.Modifier;

/**
 * The {@link IsaPrimitive} class implements base functionality of addressing modes
 * and operations.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public abstract class IsaPrimitive {

    public boolean terminal = true;
    public Modifier modifier;

    /**
     * Stores arguments of the primitive.
     */
    private final Map<String, IsaPrimitive> arguments;

    private LabelReference labelReference = null;

    /**
     * Constructs a primitive and saves the table of its arguments.
     *
     * @param arguments Primitive arguments.
     * @throws IllegalArgumentException if the parameter is {@code null}.
     */
    public IsaPrimitive(final Map<String, IsaPrimitive> arguments) {
        InvariantChecks.checkNotNull(arguments);
        this.arguments = arguments;
    }

    /**
     * Constructs a primitive.
     */
    public IsaPrimitive() {
        this(new LinkedHashMap<String, IsaPrimitive>());
    }

    /**
     * Returns the name of the currently executed primitive or an empty
     * string if no primitive is being executed.
     *
     * @return Name of the executed primitive.
     */
    public static String getCurrentOperation() {
        return CALL_STACK.isEmpty() ? "" : CALL_STACK.get(CALL_STACK.size() - 1).getName();
    }

    /**
     * Returns the primitive name.
     *
     * @return Primitive name.
     */
    public final String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Returns the primitive argument table.
     *
     * @return Primitive arguments.
     */
    public final Map<String, IsaPrimitive> getArguments() {
        return arguments;
    }

    /**
     * Registers an argument in the argument table.
     *
     * @param name  Argument name.
     * @param value Argument.
     * @throws IllegalArgumentException if any of the parameters is {@code null}.
     */
    protected final void addArgument(final String name, final IsaPrimitive value) {
        InvariantChecks.checkNotNull(name);
        InvariantChecks.checkNotNull(value);
        arguments.put(name, value);
    }

    protected final void setLabelReference(final LabelReference labelReference) {
        InvariantChecks.checkNotNull(labelReference);
        InvariantChecks.checkTrue(null == this.labelReference);
        this.labelReference = labelReference;
    }

    /**
     * Performs initialization of temporary variables that can be used in other attributes
     * except for {@code decode} ({@code action}, {@code syntax}, and {@code image}).
     * <p>
     * This attribute is implicitly called inside of the above mentioned attributes.
     * When needed, it can be called explicitly by other primitives.
     *
     * @param temporaryVariables temporary variables.
     */
    public void init(final TemporaryVariables temporaryVariables) {
        // By default, do nothing.
    }

    /**
     * Provides a hint to construct the decoder.
     *
     * @param temporaryVariables temporary variables.
     */
    public void decode(final TemporaryVariables temporaryVariables) {
        // By default, do nothing.
    }

    /**
     * Returns assembly text of the specified primitive. Takes into account labels
     * that might be associated with a specific primitive (mode).
     *
     * @param temporaryVariables temporary variables.
     * @return Assembly text.
     */
    public final String text(final TemporaryVariables temporaryVariables) {
        return null != labelReference ? labelReference.getName() : syntax(temporaryVariables);
    }

    /**
     * Returns assembly format of the specified primitive.
     *
     * <p>Default implementation is provided to allow using primitives that have no explicitly
     * specified syntax attribute. This method does not do any useful work and should never be called.
     * It is needed only to let inherited classes compile.
     *
     * @param temporaryVariables temporary variables.
     * @return Assembly text.
     */
    protected String syntax(final TemporaryVariables temporaryVariables) {
        reportUndefined("syntax", "Empty string will be returned");
        return "";
    }

    /**
     * Returns binary representation of the specified primitive.
     *
     * <p>Default implementation is provided to allow using primitives that have no explicitly
     * specified image attribute. This method does not do any useful work and should never be called.
     * It is needed only to let inherited classes compile.
     *
     * @param temporaryVariables temporary variables.
     * @return Binary text.
     */
    public String image(final TemporaryVariables temporaryVariables) {
        reportUndefined("image", "Empty string will be returned, primitive size will be 0");
        return "";
    }

    /**
     * Runs execution of the current primitive's action.
     *
     * @param processingElement  Processing element instance.
     * @param temporaryVariables Temporary variables.
     */
    public final void execute(
            final ProcessingElement processingElement,
            final TemporaryVariables temporaryVariables) {
        try {
            if (modifier != Modifier.PSEUDO) {
                CALL_STACK.add(this);
            }
            action(processingElement, temporaryVariables);
        } finally {
            if (terminal && modifier != Modifier.PSEUDO) {
                Aspectracer.addInstrPath(null);
            }
            if (modifier != Modifier.PSEUDO) {
                CALL_STACK.remove(this);
            }
            if (CALL_STACK.size() != 0)
            {
                CALL_STACK.get(CALL_STACK.size() - 1).terminal = false;
            }
        }
    }

    /**
     * Runs the action associated with the primitive.
     *
     * <p>Default implementation is provided to allow using primitives that have no explicitly
     * specified action attribute. This method does not do any useful work and should never be called.
     * It is needed only to let inherited classes compile.
     *
     * @param processingElement  Processing element instance.
     * @param temporaryVariables Temporary variables.
     */
    protected void action(
            final ProcessingElement processingElement,
            final TemporaryVariables temporaryVariables) {
        reportUndefined("action", "No action will be performed");
    }

    /**
     * Returns the location the primitive object points to (when initialized with specific
     * parameters). Applicable only to addressing modes that have a return expression.
     *
     * <p>Default implementation is provided to allow using primitives that do not have a return
     * expression. This method does not do any useful work and should never be called. It is needed
     * only to let inherited classes compile.
     *
     * @param processingElement  Processing element instance.
     * @param temporaryVariables Temporary variables.
     * @return Memory location.
     */
    public Location access(
            final ProcessingElement processingElement,
            final TemporaryVariables temporaryVariables) {
        // Processing element is allowed to be null (e.g., access to immediate data).
        reportUndefined("access", "null will be returned");
        return null;
    }

    public final Location access() {
        return access(null, null);
    }

    protected final Location annotate(final Location location, final TemporaryVariables tempVars) {
        return location.setAddressingMode(new IsaAddressingMode(this, tempVars));
    }

    private void reportUndefined(final String attrName, final String message) {
        Logger.error(
                "The '%s' attribute is undefined for the '%s' primitive. %s.",
                attrName,
                getName(),
                message
        );
    }
}
