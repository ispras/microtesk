/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * InstructionBuilder.java, Jan 9, 2013 6:13:18 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.translator.antlrex.log.ELogEntryKind;
import ru.ispras.microtesk.translator.antlrex.log.ESenderKind;
import ru.ispras.microtesk.translator.antlrex.log.ILogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogEntry;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Instruction;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveOR;

/**
 * The InstructionBuilder class build the intermediate representation (IR) of
 * microprocessor instructions by analyzing the IR of addressing modes and operations
 * created by the Sim-nML parser and tree walker. Instructions are composed of
 * OPs and MODEs basing on links between them.
 * 
 * The analysis starts from the instruction entry point. According to Sim-nML conventions,
 * it is an operation (OP-construction) called "instruction" that refers to other operations
 * and addressing modes. The analysis logic walks down the tree of MODE and OP dependencies
 * and, when it reaches a leaf, adds a corresponding instruction IR to the instruction set. 
 *
 * @author Andrei Tatarnikov
 */

public final class InstructionBuilder
{
    public static final String ALREADY_SYNTHESIZED = 
        "Instructions have already been synthesized. No action will be performed.";

    public static final String NO_ROOT_OPERATION_FRMT = 
        "No entry point. The '%s' root operation is not defined.";

    public static final String UNSUPPORTED_ARG_TYPE_FRMT =
        "The '%s' argument of the '%s' primitive has an unsupported kind %s (type name is '%s')";

    public static final String EXCEEDING_OP_ARG_COUNT_FRMT =
        "The '%s' argument of the '%s' primitive cannot be an operation. Only one operation argument is allowed per primitive.";

    /** The name of the model entry point (root operation). */
    public static final String ROOT_OPERATION = "instruction";

    private final Map<String, Primitive>   operations;
    private final Map<String, Instruction> instructions;

    private final String fileName;
    private final ILogStore   log;

    /**
     * Creates an instruction builder for the given IR of Sim-nML operations.
     *
     * @param operations A table or operation IR objects.
     * @param fileName The specification file name. 
     * @param log Log object that stores information about events and issues that may occur. 
     */

    public InstructionBuilder(
        Map<String, Primitive> operations,
        String fileName,
        ILogStore log
        )
    {
        assert null != operations;
        assert null != fileName;
        assert null != log;

        this.operations   = operations;
        this.instructions = new LinkedHashMap<String, Instruction>();
        this.fileName     = fileName;
        this.log          = log;
    }

    /**
     * Reports an error message to the log.
     * 
     * @param message Error message.
     */
    
    private void reportError(String message)
    {
        assert null != log;

        log.append(
            new LogEntry(
                ELogEntryKind.ERROR,
                ESenderKind.EMITTER,
                fileName,
                0,
                0,
                message
                )
            );
    }

    /**
     * Reports an warning message to the log.
     * 
     * @param message Warning message.
     */

    private void reportWarning(String message)
    {
        assert null != log;

        log.append(
            new LogEntry(
                ELogEntryKind.WARNING,
                ESenderKind.EMITTER,
                fileName,
                0,
                0,
                message
                )
            );
    }

    /**
     * Generates IR for microprocessor instructions based on information about MODEs and OPs.
     * 
     * The code of the method walks down the tree of links between OP and MODE primitives
     * starting from the root operation. When it faces an operation OR-rule it replaces the
     * OR-rule primitive with all of its alternatives creating a corresponding amount of
     * instructions. When it encounters a MODE-rule it uses the specified addressing mode
     * as a parameter of the instruction.  
     * 
     * Preconditions:
     * 1. The instruction make should be empty (only a single method run for a single builder instance).
     * 2. The "instruction" root operation should be defined.
     * 3. The "instruction" root operation should not be an OR-rule.
     * 4. An operation cannot have more that one operation child primitive (to avoid ambiguity).  
     * 
     * @return true if the action was successful and false if an error occurred.   
     */

    public boolean buildInstructions()
    {
        if (!instructions.isEmpty())
        {
            reportWarning(ALREADY_SYNTHESIZED);
            return true;
        }

        if (!operations.containsKey(ROOT_OPERATION))
        {
            reportError(String.format(NO_ROOT_OPERATION_FRMT, ROOT_OPERATION));
            return false;
        }

        final List<PrimitiveAND> roots = new ArrayList<PrimitiveAND>();
        saveAllOpsToList(operations.get(ROOT_OPERATION), roots);

        boolean result = true;
        for (PrimitiveAND root : roots)
        {
            final PrimitiveAND rootCopy = root.makeCopy(); 
            final Map<String, Primitive> arguments = new LinkedHashMap<String, Primitive>();

            result &= traverseOperationTree(arguments, rootCopy, rootCopy);
        }

        return result;
    }

    /**
     * Returns a table of synthesized instruction IRs.
     * 
     * @return Table of instructions.
     */
    
    public Map<String, Instruction> getInstructions()
    {
        return instructions;
    }

    /**
     * Traverses the tree of operations (OP-constructions) creating
     * a tree of primitives (IR for instruction components). When a leaf 
     * is reached the method creates an instruction description based on
     * the information it has collected.   
     * 
     * @param arguments The table of instruction arguments. Contains all modes
     * encountered during the walk from the root operation to a leaf operation (this
     * path describes components that build up a particular instruction). Passed to
     * the constructor of the Instruction class. 
     *  
     * @param root Describes the root operation (the "instruction" OP-construction).
     * Contains a link to a lower-level OP-primitive if it is declared. Passed to the constructor
     * of the Instruction class.
     * 
     * @param current An operation of the current level. We use this primitive to
     * continue adding nodes to the tree whereas the path from root to current.
     * is considered complete.
     *   
     * @return Returns false if an error occurred or true if the action was successful. 
     */

    private boolean traverseOperationTree(
        Map<String, Primitive> arguments,
        PrimitiveAND root,
        PrimitiveAND current
        )
    {
        final List<PrimitiveAND> opList = new ArrayList<PrimitiveAND>();
        String opName = "";

        int opArgCount = 0;
        for (Map.Entry<String, Primitive> argEntry : current.getArguments().entrySet())
        {
            final String    argName = uniqueName(argEntry.getKey(), arguments.keySet());
            final Primitive argType = argEntry.getValue();
            
            switch (argType.getKind())
            {
                case MODE:
                case IMM:
                {
                    arguments.put(argName, argType);
                    break;
                }

                case OP:
                {
                    if (opArgCount > 0)
                    {
                        reportError(String.format(EXCEEDING_OP_ARG_COUNT_FRMT, argName, current.getName()));
                        return false;
                    }

                    opArgCount++;
                    opName = argName;
                    
                    saveAllOpsToList(argType, opList);
                    break;
                }

                default:
                {
                    reportError(String.format(UNSUPPORTED_ARG_TYPE_FRMT, argName, current.getName(), argType.getKind().name(), argType.getName()));
                    return false;
                }
            }
        }

        if (opList.isEmpty())
        {
            final Instruction instruction = new Instruction(current.getName(), root, arguments);
            instructions.put(instruction.getName(), instruction);
            return true;
        }

        for (PrimitiveAND op : opList)
        {
            final PrimitiveAND newCurrent = op.makeCopy();
            current.getArguments().put(opName, newCurrent);

            if (!traverseOperationTree(
                new LinkedHashMap<String, Primitive>(arguments),
                root == current ? root.makeCopy() : root,
                newCurrent)
                )
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Creates a list of operations (OPs) based on operations linked to the
     * current OP-object with OR-rules (nested links are resolved too). 
     * 
     * @param op An operation or an operation OR-rule.
     * @param opList An out-parameter. Holds the list of operations the "op" parameter refers to.
     */

    private static void saveAllOpsToList(Primitive op, List<PrimitiveAND> opList)
    {
        if (!op.isOrRule())
        {
            opList.add((PrimitiveAND) op);
            return;
        }

        for (Primitive o : ((PrimitiveOR) op).getORs())
           saveAllOpsToList(o, opList);
    }

    /**
     * Makes sure that the specified name is unique for the set of existing
     * names. If such name already exists creates a new name by adding an index
     * to the original name.    
     * 
     * @param name Original name.
     * @param existingNames A set of existing names.
     * @return A unique name.
     */

    private static String uniqueName(String name, Set<String> existingNames)
    {
        String result = name;

        int index = 0;
        while (existingNames.contains(result))
            result = String.format("%s_%d", name, ++index);

        return result;
    }
}
