package ru.ispras.microtesk.tools.templgen.templates;

import java.util.ArrayList;
import java.util.Iterator;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.metadata.MetaModel;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.tools.templgen.printers.TemplatePrinter;

public class GroupTemplate extends GeneratedTemplate{
  private static final int BRANCH_OPERATIONS = 1;
  private static final int STORE_OPERATIONS = 2;
  private static final int LOAD_OPERATIONS = 3;
  private static final int ARITHMETIC_OPERATIONS = 4;

  private final MetaModel templateMetaModel;
  private final TemplatePrinter templatePrinter;

  public GroupTemplate(final MetaModel metaModel, final TemplatePrinter printer) {
    InvariantChecks.checkNotNull(metaModel);

    this.templateMetaModel = metaModel;

    this.templatePrinter = printer;
  }

  protected static ArrayList<MetaOperation> getGroups(Iterable<MetaOperation> operations, int groupType) {
    
    ArrayList<MetaOperation> groupOperations = new ArrayList<MetaOperation>();
    
    for (MetaOperation operation : operations) {
    //  System.out.format("Operation: %s \n", operation.getName());
     // TemplateUtils.printMetaOperation(operation);

    switch(groupType) {
      case BRANCH_OPERATIONS: 
        if (TemplateUtils.isBranchOperation(operation))
          groupOperations.add(operation);
        break;
      case STORE_OPERATIONS:
        if(operation.isStore())
          groupOperations.add(operation);
        break;
      case LOAD_OPERATIONS:
        if(operation.isLoad())
          groupOperations.add(operation);
        break;
      case ARITHMETIC_OPERATIONS:
        int argumentsNumber = TemplateUtils.getArgumentsNumber(operation.getArguments());
        if (//getArgumentNumbers(operation.getArguments(), IsaPrimitiveKind.MODE) == argumentsNumber && 
        !TemplateUtils.isBranchOperation(operation) && argumentsNumber == 3 &&
        !operation.isLoad() && !operation.isStore())
          groupOperations.add(operation);
        break;
      default:
          //
        break;
      }
    }
    
    return groupOperations;
  } 
  
  private void printSequence(int operationsGroup) {
    Iterable<MetaOperation> operationsIterator = templateMetaModel.getOperations();

    ArrayList<MetaOperation> operationGroups = getGroups(operationsIterator, operationsGroup);
    
    Iterator<MetaOperation> iteratorGroup = operationGroups.iterator();

    templatePrinter.startSequence("sequence {");
    
    while(iteratorGroup.hasNext()) {
      MetaOperation operation = iteratorGroup.next();
      //System.out.format("Operation: %s \n", operation.getName());
      if (operation.hasRootShortcuts()) printMetaOperation(templatePrinter, operation);
    }

    templatePrinter.closeSequence("}.run");

  }
  
  @Override
  public boolean generate()
  {
    //RubyTemplatePrinter templatePrinter = new RubyTemplatePrinter();
    templatePrinter.templateBegin();

    templatePrinter.addComment(" BRANCH_OPERATIONS");
    printSequence(BRANCH_OPERATIONS);

    templatePrinter.addComment(" STORE_OPERATIONS");
    printSequence(STORE_OPERATIONS);

    templatePrinter.addComment(" LOAD_OPERATIONS");
    printSequence(LOAD_OPERATIONS);

    templatePrinter.addComment(" ARITHMETIC_OPERATIONS");
    printSequence(ARITHMETIC_OPERATIONS);

    templatePrinter.templateEnd();
    templatePrinter.templateClose();

    //System.out.println(templateMetaModel.getOperationGroups());

    return true;
  }

}
