/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: GeneratorEngine.java,v 1.81 2010/01/13 11:47:08 vorobyev Exp $
 */

package com.unitesk.testfusion.core.engine;

import java.io.PrintStream;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.dependency.Dependencies;
import com.unitesk.testfusion.core.dependency.Dependency;
import com.unitesk.testfusion.core.dependency.DependencyType;
import com.unitesk.testfusion.core.dependency.RegisterDependency;
import com.unitesk.testfusion.core.model.*;
import com.unitesk.testfusion.core.model.register.Register;
import com.unitesk.testfusion.core.situation.*;
import com.unitesk.testfusion.core.template.Section;
import com.unitesk.testfusion.core.tracer.UTT2Tracer;

/**
 * Engine of test program generator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class GeneratorEngine
{
    /** Test template specification. */
    protected Section template;
    
    /** Test program processor. */
    protected TestProgramProcessor testProgramProcessor = new TestProgramProcessor();
    
    /** Archivator of test programs. */
    protected TestProgramArchivator testProgramArchivator = new TestProgramArchivator();

    /** Tracer of test coverage. */
    protected UTT2Tracer coverageTracer = new UTT2Tracer();
    
    /** Test program size. */
    protected int testSize;

    /** Test specification for the target processor (processor under test). */
    protected Test targetTest;
    
    /**
     * Test specification for the control processor (main processor if
     * coprocessor is being tested).
     */
    protected Test controlTest;
    
    /** Test data specification. */
    protected Test dataTest;
    
    /**
     * Number of preparation layers for the control processor. Preparation
     * layers numbered from <code>0</code> to
     * <code>controlProcessorPreparationLayerNumber-1</code>] belong to
     * to the control processor; preparation layers numbered from
     * <code>controlProcessorPreparationLayerNumber-1</code> belong to
     * the target processor.
     */
    protected int controlProcessorPreparationLayerNumber;

    /**
     * Flag that enables/disables checking of test situation
     * <code>construct()</code> method correctness.
     */
    protected boolean checkSituationConstruct = true;
    
    /**
     * Flag that enables/disables checking of dependency
     * <code>construct()</code> method correctness.
     */
    protected boolean checkDependencyConstruct = true;
    
    /**
     * Flag that enables/disables checking of instruction
     * <code>calculate()</code> postcondition.
     */
    protected boolean checkCalculatePostcondition = true;

    /**
     * Flag that enables/disables checking of instruction
     * <code>execute()</code> precondition.
     */
    protected boolean checkExecutePrecondition = true;
    
    /**
     * Flag that enables/disables checking of instruction
     * <code>execute()</code> postcondition.
     */
    protected boolean checkExecutePostcondition = true;

    /**
     * Flags that enables/disables adding of detailed comments into
     * generated test programs. 
     */
    protected boolean addComments = true;
    
    /**
     * Flag that enables/disables processing test program files.
     */
    protected boolean processTestProgramFile = true;

    /**
     * Flag that enables/disables tracing of test coverage.
     */
    protected boolean traceTestCoverage = false;
    
    /**
     * Constructor.
     * 
     * @param <code>testSize</code> the size of test programs.
     */
    public GeneratorEngine(int testSize)
    {
        this.testSize = testSize;
    }

    //**********************************************************************************************
    // Options
    //**********************************************************************************************
    
    /**
     * Enables/disables checking of test situation <code>construct()</code>
     * method correctness.
     * 
     * @param <code>enable</code> the enabling status.
     */
    public void checkSituationConstruct(boolean enable)
    {
        checkSituationConstruct = enable;
    }

    /**
     * Enables/disables checking of dependency <code>construct()</code> method
     * correctness.
     * 
     * @param <code>enable</code> the enabling status.
     */
    public void checkDependencyConstruct(boolean enable)
    {
        checkDependencyConstruct = enable;
    }

    /**
     * Enables/disables checking of instruction <code>calculate()</code> method
     * postcondition.
     * 
     * @param <code>enable</code> the enabling status.
     */
    public void checkCalculatePostcondition(boolean enable)
    {
        checkCalculatePostcondition = enable;
    }

    /**
     * Enables/disables checking of instruction <code>execute()</code> method
     * precondition.
     * 
     * @param <code>enable</code> the enabling status.
     */
    public void checkExecutePrecondition(boolean enable)
    {
        checkExecutePrecondition = enable;
    }

    /**
     * Enables/disables checking of instruction <code>execute()</code> method
     * postcondition.
     * 
     * @param <code>enable</code> the enabling status.
     */
    public void checkExecutePostcondition(boolean enable)
    {
        checkExecutePostcondition = enable;
    }
    
    /**
     * Enables/disables adding of detailed comments into generated test
     * programs. 
     */
    public void addComments(boolean enable)
    {
        addComments = enable; 
    }

    /**
     * Enables/disables processing of test program files.
     */
    public void processTestProgramFile(boolean enable)
    {
        processTestProgramFile = enable; 
    }

    /**
     * Enables/disables tracing of the test coverage.
     * 
     * @param <code>enable</code> the enabling status.
     */
    public void traceTestCoverage(boolean enable)
    {
    	traceTestCoverage = enable;
    }
    
    //**********************************************************************************************
    // Test program size
    //**********************************************************************************************

    /**
     * Returns the size of test programs.
     * 
     * @return the size of test programs.
     */
    public int getTestSize()
    {
        return testSize;
    }

    /**
     * Sets the size of test programs.
     * 
     * @param <code>testSize</code> the size of test programs.
     */
    public void setTestSize(int testSize)
    {
        this.testSize = testSize;
    }
    
    //**********************************************************************************************
    // Test template specification
    //**********************************************************************************************
    
    /**
     * Returns the test template specification.
     * 
     * @return the test template specification.
     */
    public Section getTemplate()
    {
        return template;
    }

    /**
     * Sets the test template specification.
     * 
     * @param <code>template</code> the test template specification.
     */
    public void setTemplate(Section template)
    {
        this.template = template;
    }
    
    //**********************************************************************************************
    // Test program processor
    //**********************************************************************************************
    
    /**
     * Returns the test program processor.
     * 
     * @return the test program processor.
     */
    public TestProgramProcessor getTestProgramProcessor()
    {
        return testProgramProcessor;
    }

    /**
     * Sets the test program processor.
     * 
     * @param <code>testProgramProcessor</code> the test program processor.
     */
    public void setTestProgramProcessor(TestProgramProcessor testProgramProcessor)
    {
        this.testProgramProcessor = testProgramProcessor;
    }

    //**********************************************************************************************
    // Test program archivator
    //**********************************************************************************************

    /**
     * Returns the type of archivator.
     * 
     * @return the type of archivator.
     */
    public int getArchivator()
    {
        return testProgramArchivator.getArchivator();
    }
    
    /**
     * Sets the type of archivator.
     * 
     * @param <code>archivator</code> the type of archivator.
     */
    public void setArchivator(int archivator)
    {
        testProgramArchivator.setArchivator(archivator);
    }
    
    /**
     * Returns the method of compression.
     * 
     * @return the method of compression.
     */
    public int getCompressionMethod()
    {
        return testProgramArchivator.getCompressionMethod();
    }
    
    /**
     * Sets the method of compression.
     * 
     * @param <code>method</code>
     */
    public void setCompressionMethod(int method)
    {
        testProgramArchivator.setCompressionMethod(method);
    }

    //**********************************************************************************************
    // Self-checking test generation 
    //**********************************************************************************************

    /**
     * Checks if self-checking test generation is enabled.
     * 
     * @return <code>true</code> if self-checking test generation is enabled;
     *         <code>false</code> otherwise.
     */
    public boolean isSelfCheck()
    {
        return targetTest.isSelfCheck();
    }
    
    /**
     * Enables/disables self-checking test generation.
     * 
     * @param <code>selfCheck</code> the self-checking status.
     */
    public void setSelfCheck(boolean selfCheck)
    {
        if(controlTest != null)
            { controlTest.setSelfCheck(selfCheck); }
        
        if(targetTest != null)
            { targetTest.setSelfCheck(selfCheck); }
    }
    
    //**********************************************************************************************
    // Test name and output directory 
    //**********************************************************************************************

    /**
     * Sets the file prefixes for target and control processors.
     * 
     * @param <code>targetTestName</code> the file prefix for the target processor.
     * 
     * @param <code>controlTestName</code> the file prefix for the control
     *        processor.
     *        
     * @param <code>dataTestName</code> the file prefix for the test data.
     */
    public void setTestName(String targetTestName, String controlTestName, String dataTestName)
    {
    	if(targetTest != null)
            { targetTest.setFilePrefix(targetTestName); }
        
        if(controlTest != null)
            { controlTest.setFilePrefix(controlTestName); }
        
        if(dataTest != null)
        	{ dataTest.setFilePrefix(dataTestName); }
    }
    
    /**
     * Sets the file prefixes for target and control processors and test data.
     * 
     * @param <code>testName</code> the file prefix.
     */
    public void setTestName(String testName)
    {
        setTestName(testName, testName, testName);
    }
    
    /**
     * Sets the output directories for target and control processors.
     * 
     * @param <code>targetOutputDirectory</code> the output directory for target
     *        the processor.
     * 
     * @param <code>controlOutput</code> the output directory for control the
     *        processor.
     *        
     * @param <code>dataOutput</code> the output directory for test data.
     */
    public void setOutputDirectory(String targetOutputDirectory, String controlOutputDirectory, String dataOutputDirectory)
    {
        if(targetTest != null)
            { targetTest.setOutputDirectory(targetOutputDirectory); }
        
        if(controlTest != null)
            { controlTest.setOutputDirectory(controlOutputDirectory); }
        
        if(dataTest != null)
        	{ dataTest.setOutputDirectory(dataOutputDirectory); }
    }
    
    /**
     * Sets the output directories for target and control processors and 
     * test data.
     * 
     * @param <code>outputDirectory</code> the output directory.
     */
    public void setOutputDirectory(String outputDirectory)
    {
        setOutputDirectory(outputDirectory, outputDirectory, outputDirectory);
    }
    
    //**********************************************************************************************
    // Target test 
    //**********************************************************************************************
    
    /**
     * Returns the test specification of the target processor.
     * 
     * @return the test specification of the target processor.
     */
    public Test getTargetTest()
    {
        return targetTest;
    }

    /**
     * Returns the target processor.
     * 
     * @return the target processor.
     */
    public Processor getTargetProcessor()
    {
        return targetTest.getProcessor();
    }
    
    /**
     * Returns the generation context of the target processor.
     * 
     * @return the generation context of the target processor. 
     */
    public GeneratorContext getTargetContext()
    {
        return targetTest.getContext();
    }
    
    /**
     * Returns the test program template of the target processor.
     * 
     * @return the test program template of the target processor.
     */
    public TestProgramTemplate getTargetTemplate()
    {
        return targetTest.getTemplate();
    }

    /**
     * Sets the test specification of the target processor.
     * 
     * @param <code>test</code> the test specification of the target processor.
     */
    public void setTargetTest(Test test)
    {
        this.targetTest = test;
    }
    
    //**********************************************************************************************
    // Control test 
    //**********************************************************************************************

    /**
     * Returns the test specification of the control processor.
     * 
     * @return the test specification of the control processor.
     */
    public Test getControlTest()
    {
        return controlTest;
    }
    
    /**
     * Returns the control processor.
     * 
     * @return the control processor.
     */
    public Processor getControlProcessor()
    {
        return controlTest != null ? controlTest.getProcessor() : null;
    }
    
    /**
     * Returns the generation context of the control processor.
     * 
     * @return the generation context of the control processor.
     */
    public GeneratorContext getControlContext()
    {
        return controlTest != null ? controlTest.getContext() : null;
    }
    
    /**
     * Returns the test program template of the control processor.
     * 
     * @return the test program template of the control processor.
     */
    public TestProgramTemplate getControlTemplate()
    {
        return controlTest != null ? controlTest.getTemplate() : null;
    }

    /**
     * Sets the test specification of the control processor.
     * 
     * @param <code>test</code> the test specification of the control processor.
     * 
     * @param <code>layerNumber</code> the number of preparation layers for
     *        the control processor.
     */
    public void setControlTest(Test test, int layerNumber)
    {
        this.controlTest = test;
        this.controlProcessorPreparationLayerNumber = layerNumber;
    }

    //**********************************************************************************************
    // Data test 
    //**********************************************************************************************
    
    /**
     * Returns the test data.
     * 
     * @return the test data.
     */
    public Test getDataTest()
    {
        return dataTest;
    }

    /**
     * Returns the test data processor.
     * 
     * @return the test data processor.
     */
    public Processor getDataProcessor()
    {
        return dataTest != null ? dataTest.getProcessor() : null;
    }
    
    /**
     * Returns the generation context of the test data.
     * 
     * @return the generation context of the test data.
     */
    public GeneratorContext getDataContext()
    {
        return dataTest != null ? dataTest.getContext() : null;
    }
    
    /**
     * Returns the test program template of the test data.
     * 
     * @return the test program template of the test data.
     */
    public TestProgramTemplate getDataTemplate()
    {
        return dataTest != null ? dataTest.getTemplate() : null;
    }
    
    /**
     * Sets the test data.
     * 
     * @param <code>test</code> the test data.
     */
    public void setDataTest(Test test)
    {
        this.dataTest = test;
    }
    
    //**********************************************************************************************
    // Generation 
    //**********************************************************************************************

    /**
     * Constructs register dependencies of test action.
     * 
     * @param <code>program</code> the test action.
     */
    protected void constructRegisterDependencies(Program program)
    {
        int i, j, k, size1, size2, size3;
        
        Dependencies deps = template.getRegisteredDependencies();
        GeneratorContext context = targetTest.getContext();
        
        size1 = deps.size();
        for(i = 0; i < size1; i++)
        {
            Dependency dependency = deps.get(i);
            
            if(!dependency.isRegisterDependency())
                { continue; }
            
            RegisterDependency registerDependency = (RegisterDependency)dependency;
            
            size2 = program.countInstruction();
            for(j = 0; j < size2; j++)
            {
                Instruction instruction = program.getInstruction(j);
                Situation situation = instruction.getSituation();
                
                // Do not allocate registers for an instruction with the null situation.
                // Null situation means that this is a special instruction with manually
                // specified registers.
                if(situation == null)
                    { continue; }
                
                size3 = instruction.countOperand();
                for(k = 0; k < size3; k++)
                {
                    Operand operand = instruction.getOperand(k);

                    if(!operand.isRegister())
                        { continue; }
                    
                    registerDependency.construct(operand, context);
                }
            }
        }
    }

    /**
     * Constructs content dependencies of the instruction.
     * 
     * @param <code>instruction</code> the instruction.
     */
    protected void constructContentDependencies(Instruction instruction)
    {
        int i, j, k, size1, size2, size3;
        
        Dependencies deps = template.getRegisteredDependencies();
        Situation situation = instruction.getSituation();
        GeneratorContext context = getTargetContext();
        
        size1 = deps.size();
        for(i = 0; i < size1; i++)
        {
            Dependency dep = deps.get(i);
            DependencyType type = dep.getDependencyType();
            
            if(dep.isRegisterDependency())
                { continue; }
            
            size2 = instruction.countOperand();
            for(j = 0; j < size2; j++)
            {
                Operand operand = instruction.getOperand(j);

                Dependencies dependencies = new Dependencies();

                size3 = operand.countForwardDependency();
                for(k = 0; k < size3; k++)
                {
                    Dependency dependency = operand.getForwardDependency(k);
                    
                    if(dependency.precondition())
                        { dependencies.add(dependency); }
                }
                
                if(type.isApplicableTo(operand, operand))
                    { dep.construct(situation, dependencies, context); }
            }
        }
    }
    
    /**
     * Checks content dependencies postconditions.
     * 
     * @param <code>instruction</code> the instruction.
     */
    protected void checkContentDepsPostconditions(Instruction instruction)
    {
        int i, j, size1, size2;

        size1 = instruction.countOperand();
        for(i = 0; i < size1; i++)
        {
            Operand operand = instruction.getOperand(i);                          

            size2 = operand.countForwardDependency();
            for(j = 0; j < size2; j++)
            {
                Dependency dependency = operand.getForwardDependency(j);
                
                if(dependency.isRegisterDependency())
                    { continue; }
                
                if(dependency.precondition())
                {
                    if(!dependency.postcondition())
                        { throw new ConstructDependencyException(dependency); }
                }
            }
        }
    }
    
    /**
     * Marks the input registers of the instruction as being used.
     * 
     * @param <code>instruction</code> the instruction.
     * 
     * @param <code>context</code> the context of generation.
     */
    protected void useRegisters(Instruction instruction, GeneratorContext context)
    {
        int i, size;
        
        size = instruction.countOperand();
        for(i = 0 ; i < size; i++)
        {
            Operand operand = instruction.getOperand(i);
            OperandType type = operand.getOperandType();
            
            if(operand.isInput() && type.isRegister())
            {
                // Use input registers.
                if(!operand.isFixed())
                {
                    Register register = operand.getRegister();
                    
                    if(register == null)
                    {
                        throw new NullPointerException("Null register in operand: " + operand.toString() + 
                            ", register type: " + operand.getOperandType() +
                            ", content type: " + operand.getContentType());
                    }
                    
                    context.useRegister(register);
                }
            }
        }
    }

    /**
     * Marks the input registers of the program as being used.
     * 
     * @param <code>program</code> the program.
     * 
     * @param <code>context</code> the context of generation.
     */
    protected void useRegisters(Program program, GeneratorContext context)
    {
        int i, size;
        
        size = program.countInstruction();
        for(i = 0 ; i < size; i++)
            { useRegisters(program.getInstruction(i), context); }
    }
    
    /**
     * Initializes dependent input operands of the instruction.
     * 
     * @param <code>instruction</code> the instruction.
     * 
     * @param <code>context</code> the context of generation.
     */
    public final void precalculate(Instruction instruction, GeneratorContext context)
    {
        int i, size;
        
        size = instruction.countOperand();
        for(i = 0 ; i < size; i++)
        {
            Operand operand = instruction.getOperand(i);
            OperandType type = operand.getOperandType();
            
            if(operand.isInput() && type.isRegister())
            {
                // Define values for dependent registers.
                if(context.isDefinedRegister(operand.getRegister()))
                {
                    long value = context.getRegisterValue(operand.getRegister());

                    operand.setLongValue(value);
                }
            }
        }
    }

    /**
     * Initializes the output operands of the instruction.
     * 
     * @param <code>instruction</code> the instruction.
     * 
     * @param <code>context</code> the context of generation.
     */
    public final void postcalculate(Instruction instruction, GeneratorContext context)
    {
        int i, size;
        
        size = instruction.countOperand();
        for(i = 0 ; i < size; i++)
        {
            Operand operand = instruction.getOperand(i);
            
            OperandType operandType = operand.getOperandType();
            ContentType contentType = operand.getContentType();
            
            if(!operandType.isRegister())
                { continue; }
            
            long value = operand.getLongValue();
            
            if(operand.isOutput())
            {
                if(!instruction.isPreException())
                {
                    context.defineRegister(operand.getRegister(), value);
                    
                    if(!contentType.checkType(operand.getLongValue()))
                        { throw new OperandTypeMismatchException(operand); }
                }
            }
            else
                { context.defineRegister(operand.getRegister(), value); }
        }
    }

    /** Resets the state of the processors. */
    protected void resetState()
    {
        Processor targetProcessor = getTargetProcessor();     
        targetProcessor.reset();
        
        if(controlTest != null)
        {
            Processor controlProcessor = getControlProcessor();
            controlProcessor.reset();
        }
    }
    
    /**
     * Initializes context of generation.
     * 
     * @param <code>testAction</code> the number of test action.
     * 
     * @param <code>template</code> the test template.
     * 
     * @param <code>deps</code> the set of dependencies.
     */
    protected void initContext(int testAction, Program template, Dependencies deps)
    {
        GeneratorContext targetContext = getTargetContext();
        
        targetContext.setTestActionNumber(testAction);
        targetContext.setTestAction(template);
        targetContext.setDependencies(deps);
        
        if(controlTest != null)
        {
            GeneratorContext controlContext = getControlContext();
            
            controlContext.setTestActionNumber(testAction);
            controlContext.setTestAction(template);
            controlContext.setDependencies(deps);
        }
    }

    /**
     * Resolves hazards in the test action.
     * 
     * @param <code>program</code> the test action.
     */
    protected void resolveHazards(Program program)
    {
        for(int k = 0; k < program.countInstruction(); k++)
        {
            Instruction instruction = program.getInstruction(k);

            Situation situation = instruction.getSituation();
            
            if(situation == null)
                { continue; }

            if(situation.isHazard(program, k))
            {
                Program resolve = situation.resolveHazard(program, k);
                
                program.insert(resolve, k);
                k += resolve.countInstruction();
            }
        }
    }
    
    /**
     * Checks if the template has the next value.
     * 
     * @return <code>true</code> if the template has the next value;
     *         <code>false</code> otherwise.
     */
    protected boolean hasNext()
    {
        // All iterators should be cloneable!
        boolean hasNext;
        Section copy = template.clone();
        
        copy.next(); hasNext = copy.hasValue();
        
        copy = null;
        
        return hasNext;
    }
    
    /**
     * Writes the test programs.
     * 
     * @param <code>testCount</code> the test count.
     * 
     * @param <code>targetProgram</code> the target program.
     * 
     * @param <code>targetPrefix</code> the prefix of the target program.
     * 
     * @param <code>targetSuffix</code> the suffix of the target program.
     * 
     * @param <code>controlProgram</code> the control program, if a coprocessor is tested.
     * 
     * @param <code>controlPrefix</code> the prefix of the control program.
     * 
     * @param <code>controlSuffix</code> the suffix of the control program.
     *      
     * @param <code>dataProgram</code> the data program.
     * 
     * @param <code>dataPrefix</code> the prefix of the data program.
     * 
     * @param <code>dataSuffix</code> the suffix of the data program.
     */
    protected void writeTestProgram(int testCount, Program targetProgram, Program targetPrefix, Program targetSuffix,
        Program controlProgram, Program controlPrefix, Program controlSuffix,
        Program dataProgram, Program dataPrefix, Program dataSuffix)
    {
    	if(controlTest != null)
        {
            controlTest.writeTest(testCount, controlProgram, controlPrefix, controlSuffix);
            controlProgram.clear();
        }
        
        if(dataTest != null)
        {
        	dataTest.writeTest(testCount, dataProgram, dataPrefix, dataSuffix);
        	dataProgram.clear();
        }
       
        targetTest.writeTest(testCount, targetProgram, targetPrefix, targetSuffix);
        targetProgram.clear();
        
        String controlTestName = controlTest != null ? controlTest.getFileName(testCount) : ""; 
        String targetTestName  = targetTest  != null ? targetTest.getFileName(testCount)  : ""; 
        String dataTestName    = dataTest    != null ? dataTest.getFileName(testCount) : "";

        if(processTestProgramFile)
        { 
        	testProgramProcessor.process(controlTestName, targetTestName, dataTestName, targetTest.getFileNameWithoutExt(testCount), targetTest.getOutputDirectory() );
        }
    }
    
    /**
     * Resolves test template constraints.
     * 
     * @param <code>template</code> the test template.
     * @param <code>processor</code> the processor.
     * @param <code>context</code> the context of generation.
     * 
     * @return the preparation program.
     */
    public Program resolve(Program template, Processor processor, GeneratorContext context)
    {
        return new Program();
    }
    
    /**
     * Generates the set of test programs.
     * 
     * @param <code>out</code> the output stream for generation messages.
     */
    public void generate(PrintStream out)
    {
        int tc = 0, sc = 0, ls = -1;
        boolean consistent = true;

        Program controlProgram = new Program();
        Program controlPrefix  = new Program();
        Program controlSuffix  = new Program();
        
        Program targetProgram  = new Program();
        Program targetPrefix   = new Program();
        Program targetSuffix   = new Program();

        Program dataProgram    = new Program();
        Program dataPrefix     = new Program();
        Program dataSuffix     = new Program();
        
        Preparation controlPreparation = new Preparation();
        Preparation targetPreparation  = new Preparation();
        Preparation dataPreparation    = new Preparation();
        
        Processor controlProcessor = getControlProcessor();
        Processor targetProcessor  = getTargetProcessor();
        
        GeneratorContext controlContext = getControlContext();
        GeneratorContext targetContext  = getTargetContext();
        GeneratorContext dataContext    = getDataContext();

        // Set data program to the target context
        targetContext.setDataProgram(dataProgram);
        
        TestProgramTemplate controlTemplate = getControlTemplate();
        TestProgramTemplate targetTemplate  = getTargetTemplate();
        TestProgramTemplate dataTemplate    = getDataTemplate();
        
        BranchDelaySlot branchDelaySlot = new BranchDelaySlot();
        
        out.println("Generation is started");
        out.flush();

        resetState();

        template.setProcessor(targetProcessor);
        template.setContext(targetContext);
        
        // Start trace
        if(traceTestCoverage)
        { 
        	coverageTracer.startTrace(targetTest.getOutputDirectory() + "/" + targetTest.getFilePrefix() + ".trace"); 
        }
        
        try
        {
            for(targetContext.reset(), template.init(); template.hasValue(); targetContext.reset(), template.next(), template.useRegisters())
            {
                boolean firstTestAction = (sc % testSize == 0);
                boolean lastTestAction  = (sc % testSize == testSize - 1) || !hasNext();
                
                ///////////////////////////////////////////////////////////////////////////////////
                // Get the current test template
                ///////////////////////////////////////////////////////////////////////////////////
                Program program = template.value();
                
                Program controlSituationPrefix = new Program();
                Program targetSituationPrefix  = new Program();
                
                controlPreparation = new Preparation();
                targetPreparation  = new Preparation();

                ///////////////////////////////////////////////////////////////////////////////////
                // Print the file name of the current test program
                ///////////////////////////////////////////////////////////////////////////////////
                if(firstTestAction && sc != ls)
                {
                    // To avoid multiple printing when the first situation happens to be inconsistent.
                    ls = sc;
                    
                    out.println("Generating file: " + targetTest.getFileName(tc));
                    out.flush();
                }

                ///////////////////////////////////////////////////////////////////////////////////
                // Construct the register dependencies
                ///////////////////////////////////////////////////////////////////////////////////

                constructRegisterDependencies(program);

                ///////////////////////////////////////////////////////////////////////////////////
                // Resolve the hazards
                ///////////////////////////////////////////////////////////////////////////////////

                resolveHazards(program);

                ///////////////////////////////////////////////////////////////////////////////////
                // Initialze the context of generation
                ///////////////////////////////////////////////////////////////////////////////////
                
                initContext(sc, program, template.getDependencies());

                ///////////////////////////////////////////////////////////////////////////////////
                // Solve test template constraints
                ///////////////////////////////////////////////////////////////////////////////////
               
                Program templatePreparation = resolve(program, targetProcessor, targetContext);

                ///////////////////////////////////////////////////////////////////////////////////
                // Get the test program prefixes
                ///////////////////////////////////////////////////////////////////////////////////
                if(firstTestAction)
                {
                    targetPrefix = targetTemplate.getTestPrefix(targetContext);
                    
                    if(controlTest != null)
                        { controlPrefix = controlTemplate.getTestPrefix(controlContext); }
                    
                    if(dataTest != null)
                    	{ dataPrefix = dataTemplate.getTestPrefix(dataContext); }
                }
                
                ///////////////////////////////////////////////////////////////////////////////////
                // Append the test situation prefixes
                ///////////////////////////////////////////////////////////////////////////////////
                targetSituationPrefix.append(targetTemplate.createDecoratedComment("Test situation: " + sc));
                targetSituationPrefix.appendAndExecute(targetProcessor, targetTemplate.getTestSituationPrefix(targetContext, firstTestAction, lastTestAction));
                targetSituationPrefix.append(new PseudoInstruction(targetTemplate.getTestSituationLabel(sc) + ":"));

                if(controlTest != null)
                {
                    controlSituationPrefix.append(controlTemplate.createDecoratedComment("Test situation: " + sc));
                    controlSituationPrefix.appendAndExecute(controlProcessor, controlTemplate.getTestSituationPrefix(controlContext, firstTestAction, lastTestAction));
                    controlSituationPrefix.append(new PseudoInstruction(controlTemplate.getTestSituationLabel(sc) + ":"));
                }

                // Mark the input registers as being used
                useRegisters(program, targetContext);

                String traceBuffer = "";
                
                branchDelaySlot.reset();
                for(int k = 0; k < program.countInstruction(); k++)
                {
                    Instruction instruction = program.getInstruction(k);
                    Situation situation = instruction.getSituation();
                 
                    targetContext.setPosition(k);
                    
                    // Special instruction
                    if(situation == null)
                    {
                        branchDelaySlot.process(instruction);
                        continue;
                    }
                    
                    // Calculate values of the dependent input operands
                    precalculate(instruction, targetContext);

                    // Check the precondition before constructing the situation
                    if(!(consistent = situation.precondition(targetProcessor, targetContext)))
                        { System.out.println("WARNING: Precondition violation: " + instruction + ", test action: " + targetContext.getTestActionNumber() + "\n" + targetContext.getTestAction()); break; }

                    // Construct the content dependencies
                    if(instruction.getPosition() > 0)
                        { constructContentDependencies(instruction); }
                    
                    // Construct the situation if it is a constructor
                    // Do nothing if the situation is a constraint
                    boolean isConstructed = situation.construct(targetProcessor, targetContext);
                    
                    // Set the construction status
                    situation.setConstructed(isConstructed);

                    if(!(consistent = situation.isConsistent()))
                        { break; }
                    
                    // Check the dependencies postconditions
                    if(checkDependencyConstruct)
                        { checkContentDepsPostconditions(instruction); }
                    
                    // Check postcondition
                    if(checkSituationConstruct && isConstructed && !situation.calculatePrecondition())
                        { throw new ConstructSituationException(situation); }
                    
                    // Construct and execute the first layer of the preparation program
                    if(situation.countPreparationLayer() > 0)
                    {
                        Program prepare = situation.prepare(targetProcessor, targetContext, 0);

                        Preparation preparation = targetPreparation;
                        Test test = targetTest;
                        
                        if(controlProcessorPreparationLayerNumber > 0)
                        {
                            preparation = controlPreparation;
                            test = controlTest;
                        }

                        // Add situation to trace element
                        if(traceTestCoverage)
                        {
                        	traceBuffer += ("{" + instruction + "; situation=" + situation + "}, "); 
                        }
                        
                        if(!prepare.isEmpty())
                        {
                            TestProgramTemplate template = test.getTemplate();
                            
                            preparation.append(new PseudoInstruction(), 0);

                            preparation.append(template.createComment("Preparation for instruction " +
                                instruction.getName() + "[" + k + "] (0): " + situation), 0);

                            preparation.append(template.createComment(situation.getDescription()), 0);
                        }
                        
                        preparation.appendAndExecute(test.getProcessor(), prepare, 0);
                    }
                    
                    boolean calculated = false;
                    
                    // Calculate the situation
                    if((calculated = branchDelaySlot.process(instruction)))
                        { instruction.calculate(targetProcessor); }
                    else
                    {
                        if(addComments)
                            { instruction.setComment("Not executed: " + branchDelaySlot); }
                    }
                    
                    // Check the postcondition
                    if(checkCalculatePostcondition && calculated && isConstructed && !situation.calculatePostcondition())
                        { throw new CalculatePostconditionViolation(instruction); }
                    
                    // Mark the initialized output registers
                    postcalculate(instruction, targetContext);
                    
                } // instructions of the template
            
                if(!consistent)
                    { continue; }
                
                // Append the prefix program
                controlProgram.append(controlSituationPrefix);
                targetProgram.append(targetSituationPrefix);
                
                // Construct and execute the rest of the preparation program
                int layer = 1;
                int currentLayer = 1;
                boolean done;

                do
                {
                    done = true;

                    for(int k = 0; k < program.countInstruction(); k++)
                    {
                        Instruction instruction = program.getInstruction(k);
                        Situation situation = instruction.getSituation();

                        if(situation == null)
                            { continue; }
                        
                        if(layer >= situation.countPreparationLayer())
                            { continue; }
                        
                        done = false;

                        Program prepare = situation.prepare(targetProcessor, targetContext, layer);
                        
                        Preparation prep = targetPreparation;
                        Test test = targetTest;
                        
                        currentLayer = layer - controlProcessorPreparationLayerNumber;
                        
                        if(controlProcessorPreparationLayerNumber > layer)
                        {
                            prep = controlPreparation;
                            test = controlTest;
                            
                            currentLayer = layer;
                        }
             
                        if(!prepare.isEmpty())
                        {
                            TestProgramTemplate template = test.getTemplate();
                            
                            prep.append(new PseudoInstruction(), currentLayer);

                            prep.append(template.createComment("Preparation for instruction " +
                                instruction.getName() + "[" + k + "] (" + currentLayer + ")"), currentLayer);
                        }
                        
                        prep.appendAndExecute(test.getProcessor(), prepare, currentLayer);
                    }
                    
                    layer++;
                }
                while(!done);
                
                // Add preparation program constructed by the constraint solver 
                targetPreparation.appendAndExecute(targetTest.getProcessor(), templatePreparation, currentLayer);
                
                // Append the preparation program
                controlProgram.append(controlPreparation.getProgram());
                targetProgram.append(targetPreparation.getProgram());
                dataProgram.append(dataPreparation.getProgram());
                
                targetProgram.append(new PseudoInstruction());
                targetProgram.append(targetTemplate.createComment("Dependencies: " + template.getDependencies()));
                targetProgram.append(new PseudoInstruction());

                if(traceTestCoverage)
                {
                	//traceBuffer += "{Dependencies: " + template.getDependencies() + "} ";
                	
                	// Add test action description into trace
                	coverageTracer.traceCoverageElement(traceBuffer);
                }
                
                // Append the test action prefix
                targetProgram.appendAndExecute(targetProcessor, targetTemplate.getTestActionPrefix(targetContext, firstTestAction, lastTestAction));
                targetProgram.append(new PseudoInstruction(targetTemplate.getTestActionLabel(sc) + ":"));
                targetProgram.append(targetTemplate.createDecoratedComment());

                if(controlTest != null)
                {
                    controlProgram.appendAndExecute(controlProcessor, controlTemplate.getTestActionPrefix(controlContext, firstTestAction, lastTestAction));
                    controlProgram.append(new PseudoInstruction(controlTemplate.getTestActionLabel(sc) + ":"));
                    controlProgram.append(controlTemplate.createDecoratedComment());
                }
                
                branchDelaySlot.reset();
                for(int k = 0; k < program.countInstruction(); k++)
                {
                    Instruction instruction = program.getInstruction(k);
                    Situation situation = instruction.getSituation();

                    targetContext.setPosition(k);
                    
                    if(branchDelaySlot.process(instruction))
                    {
                        if(checkExecutePrecondition && situation != null && !situation.executePrecondition(targetProcessor))
                            { throw new ExecutePreconditionViolation(instruction); }
                        
                        // Append and execute the instruction and its pre- and post-actions.
                        if(situation != null)
                            { targetProgram.appendAndExecute(targetProcessor, situation.preAction(targetProcessor, targetContext)); }
                
                        targetProgram.appendAndExecute(targetProcessor, instruction);
                
                        if(situation != null)
                            { targetProgram.appendAndExecute(targetProcessor, situation.postAction(targetProcessor, targetContext)); }

                        if(instruction.isException())
                            { targetProgram.append(targetTemplate.createComment("Exception: " + instruction.getException())); }
                        
                        if(checkExecutePostcondition && situation != null && !situation.executePostcondition(targetProcessor))
                            { throw new ExecutePostconditionViolation(instruction); }
                    }
                    else
                    {
                        if(addComments)
                            { targetProgram.append(targetTemplate.createComment("Not executed: " + branchDelaySlot)); }

                        // Append the instruction.
                        targetProgram.append(instruction);
                    }
                }
                
                ///////////////////////////////////////////////////////////////////////////////////
                // Append the test action suffix
                ///////////////////////////////////////////////////////////////////////////////////
                if(controlTest != null)
                {
                    controlProgram.append(controlTemplate.createDecoratedComment());
                    controlProgram.append(new PseudoInstruction());
                    controlProgram.append(controlTemplate.getTestActionSuffix(controlContext, firstTestAction, lastTestAction));
                }
                
                targetProgram.append(targetTemplate.createDecoratedComment());
                targetProgram.append(new PseudoInstruction());
                targetProgram.append(targetTemplate.getTestActionSuffix(targetContext, firstTestAction, lastTestAction));
                
                ///////////////////////////////////////////////////////////////////////////////////
                // Write the full-length test program
                ///////////////////////////////////////////////////////////////////////////////////
                if(sc++ % testSize == testSize - 1)
                {
                    targetSuffix = targetTemplate.getTestSuffix(targetContext);
                    
                    if(controlTest != null)
                        { controlSuffix = controlTemplate.getTestSuffix(controlContext); }
        
                    if(dataTest != null)
                        { dataSuffix = dataTemplate.getTestSuffix(dataContext); }
                    
                    writeTestProgram(tc++, targetProgram, targetPrefix, targetSuffix, controlProgram, controlPrefix, controlSuffix, dataProgram, dataPrefix, dataSuffix);
                    resetState();
                }

            } // templates
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
        catch(Throwable throwable)
        {
            // throwable.printStackTrace();
        }

        ///////////////////////////////////////////////////////////////////////////////////////////
        // Write the test program that contains the rest of the test
        ///////////////////////////////////////////////////////////////////////////////////////////
        if(!targetProgram.isEmpty())
        {
            // Set the test action number
            initContext(sc - 1, template.value(), template.getDependencies());

            targetSuffix = targetTemplate.getTestSuffix(targetContext);
            
            if(controlTest != null)
                { controlSuffix = controlTemplate.getTestSuffix(controlContext); }
            
            if(dataTest != null)
                { dataSuffix = dataTemplate.getTestSuffix(dataContext); }
            
            writeTestProgram(tc, targetProgram, targetPrefix, targetSuffix, controlProgram, controlPrefix, controlSuffix, dataProgram, dataPrefix, dataSuffix);
        }
        
        out.println("Generation is finished");
        out.println("Test situations: " + sc);

        out.println();
        
        // End trace
        if(traceTestCoverage) { coverageTracer.endTrace(); }
        
        if(testProgramArchivator.getArchivator() != TestProgramArchivator.NONE)
        {
            // Archivate the test packages
            out.println("Archivation is started");
            
            testProgramArchivator.compress(targetTest.getOutputDirectory());

            out.println("Archivation is finished");
        }
        
        out.flush();
    }

    /** Generates test programs. */
    public void generate()
    {
        generate(System.out);
    }
}
