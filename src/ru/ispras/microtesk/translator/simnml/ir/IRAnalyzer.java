/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IRAnalyzer.java, Jan 9, 2013 6:13:18 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.translator.antlrex.log.ELogEntryKind;
import ru.ispras.microtesk.translator.antlrex.log.ESenderKind;
import ru.ispras.microtesk.translator.antlrex.log.ILogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogEntry;
import ru.ispras.microtesk.translator.simnml.ir.instruction.Instruction;
import ru.ispras.microtesk.translator.simnml.ir.instruction.PrimitiveEntry;
import ru.ispras.microtesk.translator.simnml.ir.modeop.Argument;
import ru.ispras.microtesk.translator.simnml.ir.modeop.EArgumentKind;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveOR;

import static ru.ispras.microtesk.translator.simnml.ir.Messages.*;

/**
 * The IRAnalyzer class analyzes the intermediate representation (IR) of a ISA model created 
 * by the Sim-nML parser and tree walker and creates IR for instructions based on links
 * between MODE and OP Sim-nML constructions. 
 * 
 * The analysis starts from the mode entry point. According to Sim-nML conventions, it is
 * an operation (OP-construction) called "instruction" that refers to other operations and
 * addressing mode. The analysis logic walks down the tree of MODE and OP dependencies and,
 * when it reaches a leaf, adds a corresponding instruction IR to the instruction set. 
 *
 * @author Andrei Tatarnikov
 */

public final class IRAnalyzer
{
    /**
     * The name of the model entry point (root operation).
     */

    public static final String ROOT_OPERATION = "instruction";

    private final String fileName;
    private final IR ir;
    private final ILogStore log;

    /**
     * Creates an analyzer object for the specified IR.
     * 
     * @param fileName The specification file name. 
     * @param ir IR collected by Sim-nML parser and tree walker. 
     * @param log Log object that stores information about events and issues that may occur. 
     */
    
    public IRAnalyzer(String fileName, IR ir, ILogStore log)
    {
        this.fileName = fileName;
        this.ir = ir;
        this.log = log;
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
     * The code of the method walks down the tree of links between MODE and OP primitives
     * starting from the root operation. When it faces an operation OR-rule it replaces the
     * OR-rule primitive with all of its alternatives creating a corresponding amount of
     * instructions. When it encounters a MODE-rule it uses the specified addressing mode
     * as a parameter of the instruction.  
     * 
     * Preconditions:
     * 1. The IR should not contain information about instructions.
     * 2. The "instruction" root operation should be defined.
     * 3. The "instruction" root operation should not be an OR-rule.
     * 4. An operation cannot have more that one operation parameter (to avoid ambiguity).  
     * 
     * @return true if the action was successful and false if an error occurred.   
     */

    public boolean synthesizeInstructions()
    {
        if (!ir.getInstructions().isEmpty())
        {
            reportWarning(ALREADY_SYNTHESIZED);
            return true;
        }

        if (!ir.getOps().containsKey(ROOT_OPERATION))
        {
            reportError(String.format(NO_ROOT_OPERATION_FRMT, ROOT_OPERATION));
            return false;
        }

        final Primitive root = ir.getOps().get(ROOT_OPERATION);
        if (root.isOrRule())
        {
            reportError(ROOT_OPERATION_CANT_BE_OR_RULE);
            return false;
        }
        
        final PrimitiveAND rootOp = (PrimitiveAND)root; 

        final Map<String, PrimitiveEntry> instructionArgs =
            new LinkedHashMap<String, PrimitiveEntry>();

        final PrimitiveEntry rootPrimitive =
            new PrimitiveEntry(rootOp.getName(), EArgumentKind.OP);

        return traverseOperationTree(
            rootOp.getArgs().values(),
            instructionArgs,
            rootPrimitive,
            rootPrimitive
            );
    }
    
    /**
     * Traverses the tree of operations (OP-constructions) creating
     * a tree of primitives (IR for instruction components). When a leaf 
     * is reached the method creates an instruction description based on
     * the information it has collected.   
     *  
     * @param curOpArgs Collection of arguments to be added to the primitive of
     * the current level. The arguments can be addressing modes or operations.
     * 
     * @param instructionArgs The table of instruction arguments. Contains all modes
     * encountered during the walk from the root operation to a leaf operation (this
     * path describes components that build up a particular instruction). Passed to
     * the constructor of the Instruction class. 
     *  
     * @param rootPrimitive Describes the root operation (the "instruction" OP-construction).
     * Contains a link to a lower-level OP-primitive if it is declared. Passed to the constructor
     * of the Instruction class.
     * 
     * @param curPrimitive A primitive of the current level. We use this primitive to
     * continue adding nodes to the tree whereas the path from rootPrimitive to curPrimitive
     * is considered complete.
     *   
     * @return Returns false if an error occurred or true if the action was successful. 
     */

    private boolean traverseOperationTree(
        Collection<Argument> curOpArgs,
        Map<String, PrimitiveEntry> instructionArgs,
        PrimitiveEntry rootPrimitive,
        PrimitiveEntry curPrimitive
        )
    {
        final List<Primitive> opList = new ArrayList<Primitive>();
        String opName = "";

        int opArgCount = 0;
        for (Argument arg : curOpArgs)
        {
            switch (arg.getKind())
            {
                case MODE:
                {
                    final String argName = 
                        uniqueName(arg.getName(), instructionArgs.keySet());

                    final PrimitiveEntry modePrimitive =
                        new PrimitiveEntry(arg.getTypeText(), EArgumentKind.MODE);

                    curPrimitive.addArgument(argName, modePrimitive);
                    instructionArgs.put(argName, modePrimitive);

                    break;
                }
                
                case TYPE:
                {
                    final String argName = 
                        uniqueName(arg.getName(), instructionArgs.keySet());

                    final PrimitiveEntry typePrimitive =
                        new PrimitiveEntry(arg.getTypeText(), EArgumentKind.TYPE);
                    
                    curPrimitive.addArgument(argName, typePrimitive);
                    instructionArgs.put(argName, typePrimitive);
                    
                    break;
                }

                case OP:
                {
                    if (opArgCount > 0)
                    {
                        reportError(String.format(EXCEEDING_OP_ARG_COUNT_FRMT, 
                            arg.getName(), curPrimitive.getName()));
                        return false;
                    }

                    opArgCount++;

                    opName = arg.getName();
                    saveAllOpsToList(arg.getOp(), opList);

                    final PrimitiveEntry opPrimitive =
                        new PrimitiveEntry(arg.getTypeText(), EArgumentKind.OP);

                    curPrimitive.addArgument(arg.getName(), opPrimitive);

                    break;
                }

                default:
                {
                    reportError(
                        String.format(UNSUPPORTED_ARGUMENT_TYPE_FRMT,
                            arg.getName(),
                            curPrimitive.getName(),
                            arg.getKind().name(),
                            arg.getTypeText()
                            )
                        );

                    return false;
                }
            }
        }

        if (opList.isEmpty())
        {
            final Instruction instruction =
                new Instruction(curPrimitive.getName(), rootPrimitive, instructionArgs);

            ir.add(instruction.getName(), instruction);
            return true;
        }

        for (Primitive op : opList)
        {
            final PrimitiveEntry childPrimitive =
                new PrimitiveEntry(op.getName(), EArgumentKind.OP);

            curPrimitive.resetArgument(opName, childPrimitive);

            if (!traverseOperationTree(
                    ((PrimitiveAND) op).getArgs().values(),
                    new LinkedHashMap<String, PrimitiveEntry>(instructionArgs),
                    rootPrimitive,
                    childPrimitive
                    )
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

    private static void saveAllOpsToList(Primitive op, List<Primitive> opList)
    {
        if (!op.isOrRule())
        {
            opList.add(op);
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

        int index = 1;
        while (existingNames.contains(name))
            result = String.format("%s_%d", name, index);

        return result;
    }
}

final class Messages
{
    private Messages() {}

    public static final String ALREADY_SYNTHESIZED = 
        "Instructions have already been synthesized. No action will be performed.";

    public static final String NO_ROOT_OPERATION_FRMT = 
        "No entry point. The '%s' root operation is not defined.";

    public static final String ROOT_OPERATION_CANT_BE_OR_RULE =
        "The root operation cannot be an OR-rule.";

    public static final String UNSUPPORTED_ARGUMENT_TYPE_FRMT =
        "The '%s' argument of the '%s' primitive has an unsupported kind %s (type name is '%s')";

    public static final String EXCEEDING_OP_ARG_COUNT_FRMT =
        "The '%s' argument of the '%s' primitive cannot be an operation. Only one operation argument is allowed per primitive.";
}
