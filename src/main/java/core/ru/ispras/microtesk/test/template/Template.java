/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.microtesk.utils.PrintingUtils.printHeader;
import static ru.ispras.microtesk.utils.PrintingUtils.trace;

import java.util.Deque;
import java.util.LinkedList;

import ru.ispras.fortress.randomizer.VariateBuilder;
import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaModel;

public final class Template {
  private final MetaModel metaModel;

  private final MemoryMap memoryMap;
  private final DataManager dataManager;
  
  private PreparatorBuilder preparatorBuilder;
  private final PreparatorStore preparators;

  private Deque<BlockBuilder> blockBuilders;
  private CallBuilder callBuilder;

  private boolean isMainSection;
  private int openBlockCount;

  private final TemplateProduct.Builder productBuilder;

  public Template(MetaModel metaModel) {
    printHeader("Started Processing Template");

    checkNotNull(metaModel);
    this.metaModel = metaModel;

    this.memoryMap = new MemoryMap();
    this.dataManager = new DataManager(this.memoryMap);

    this.preparatorBuilder = null;
    this.preparators = new PreparatorStore();

    this.blockBuilders = null;
    this.callBuilder = null;

    this.isMainSection = false;
    this.openBlockCount = 0;

    this.productBuilder = new TemplateProduct.Builder();
  }
  
  public MemoryMap getMemoryMap() {
    return memoryMap;
  }

  public DataManager getDataManager() {
    return dataManager;
  }

  public PreparatorStore getPreparators() {
    return preparators;
  }

  public void beginPreSection() {
    printHeader("Started Processing Initialization Section");
    beginNewSection();
    
    isMainSection = false;
    openBlockCount = 0;
  }

  public void endPreSection() {
    final Block rootBlock = endCurrentSection();
    productBuilder.setPre(rootBlock);
    printHeader("Ended Processing Initialization Section");
  }

  public void beginPostSection() {
    printHeader("Started Processing Finalization Section");
    beginNewSection();

    isMainSection = false;
    openBlockCount = 0;
  }

  public void endPostSection() {
    final Block rootBlock = endCurrentSection();
    productBuilder.setPost(rootBlock);
    printHeader("Ended Processing Finalization Section");
  }

  public void beginMainSection() {
    printHeader("Started Processing Main Section");
    beginNewSection();

    isMainSection = true;
    openBlockCount = 0;
  }

  public void endMainSection() {
    final Block rootBlock = endCurrentSection();
    productBuilder.addToMain(rootBlock);
    printHeader("Ended Processing Main Section");
    isMainSection = false;
  }

  public TemplateProduct getProduct() {
    return productBuilder.build();
  }

  private void beginNewSection() {
    final BlockBuilder rootBlockBuilder = new BlockBuilder();
    rootBlockBuilder.setAtomic(true);

    this.blockBuilders = new LinkedList<BlockBuilder>();
    this.blockBuilders.push(rootBlockBuilder);
    this.callBuilder = new CallBuilder(getCurrentBlockId());
  }

  private Block endCurrentSection() {
    endBuildingCall();

    if (blockBuilders.size() != 1) {
      throw new IllegalStateException();
    }

    final BlockBuilder rootBuilder = blockBuilders.getLast();
    final Block rootBlock = rootBuilder.build();

    blockBuilders = null;
    callBuilder = null;

    return rootBlock;
  }

  private BlockId getCurrentBlockId() {
    return blockBuilders.peek().getBlockId();
  }

  public BlockBuilder beginBlock() {
    if (blockBuilders.isEmpty()) {
      throw new IllegalStateException();
    }

    if (openBlockCount < 0) {
      throw new IllegalStateException();
    }

    endBuildingCall();

    final boolean isRoot = openBlockCount == 0;
    final BlockBuilder parent = blockBuilders.peek();
    final BlockBuilder current;

    if (isMainSection && isRoot) {
      if (parent.isEmpty()) {
        current = parent;
      } else {
        productBuilder.addToMain(parent.build());
        blockBuilders.pop();

        current = new BlockBuilder();
        blockBuilders.push(current);
      }
    } else {
      current = new BlockBuilder(parent);
      blockBuilders.push(current);
    }

    trace("Begin block: " + getCurrentBlockId());
    ++openBlockCount;

    return current;
  }

  public void endBlock() {
    if (blockBuilders.isEmpty()) {
      throw new IllegalStateException();
    }

    endBuildingCall();
    trace("End block: " + getCurrentBlockId());

    final boolean isRoot = openBlockCount == 1;

    final BlockBuilder builder = blockBuilders.pop();
    final Block block = builder.build();

    if (isMainSection && isRoot) {
      productBuilder.addToMain(block);

      final BlockBuilder newBuilder = new BlockBuilder();
      newBuilder.setAtomic(true);
      blockBuilders.push(newBuilder);
    } else {
      blockBuilders.peek().addBlock(block);
    }

    --openBlockCount;
  }

  public void addLabel(String name) {
    final Label label = new Label(name, getCurrentBlockId());
    trace("Label: " + label.toString());
    callBuilder.addLabel(label);
  }

  public void addOutput(Output output) {
    trace(output.toString());
    callBuilder.addOutput(output);
  }

  public void setRootOperation(Primitive rootOperation) {
    callBuilder.setRootOperation(rootOperation);
  }

  public void endBuildingCall() {
    final Call call = callBuilder.build();
    trace("Ended building a call (empty = %b, executable = %b)",
        call.isEmpty(), call.isExecutable());

    if (null == preparatorBuilder) {
      blockBuilders.peek().addCall(call);
    } else {
      preparatorBuilder.addCall(call);
    }

    this.callBuilder = new CallBuilder(getCurrentBlockId());
  }

  public PrimitiveBuilder newOperationBuilder(String name) {
    trace("Operation: " + name);
    checkNotNull(name);

    return new PrimitiveBuilderOperation(name, metaModel, callBuilder, memoryMap);
  }

  public PrimitiveBuilder newAddressingModeBuilder(String name) {
    trace("Addressing mode: " + name);
    checkNotNull(name);

    final MetaAddressingMode metaData = metaModel.getAddressingMode(name);
    if (null == metaData) {
      throw new IllegalArgumentException("No such addressing mode: " + name);
    }

    return new PrimitiveBuilderCommon(callBuilder, memoryMap, metaData);
  }

  public RandomValue newRandom(int from, int to) {
    return new RandomValue(from, to);
  }

  public VariateBuilder<?> newVariateBuilder() {
    return new VariateBuilder<>();
  }

  public UnknownValue newUnknown() {
    return new UnknownValue();
  }

  public OutputBuilder newOutput(boolean isRuntime, String format) {
    return new OutputBuilder(isRuntime, format);
  }

  public SituationBuilder newSituation(String name) {
    return new SituationBuilder(name);
  }

  public void beginPreparator(String targetName) {
    endBuildingCall();

    trace("Begin preparator: %s", targetName);
    checkNotNull(targetName);

    if (null == metaModel.getAddressingMode(targetName)) {
      throw new IllegalArgumentException(String.format(
        "%s is not an addressing mode and cannot be a target for a preparator.", targetName));
    }

    if (null != preparatorBuilder) {
      throw new IllegalStateException(String.format(
        "Nesting is not allowed: The %s preparator cannot be nested into the %s preparator.",
        targetName, preparatorBuilder.getTargetName()));
    }

    preparatorBuilder = new PreparatorBuilder(targetName);
  }

  public void endPreparator() {
    endBuildingCall();
    trace("End preparator: %s", preparatorBuilder.getTargetName());

    final Preparator preparator = preparatorBuilder.build();
    preparators.addPreparator(preparator);

    preparatorBuilder = null;
  }

  public LazyValue newLazy() {
    checkPreparatorBlock();
    return preparatorBuilder.newValue();
  }

  public LazyValue newLazy(int start, int end) {
    checkPreparatorBlock();
    return preparatorBuilder.newValue(start, end);
  }

  public Primitive getPreparatorTarget() {
    checkPreparatorBlock();
    return preparatorBuilder.getTarget();
  }

  private void checkPreparatorBlock() {
    if (null == preparatorBuilder) {
      throw new IllegalStateException(
        "The construct cannot be used outside a preparator block.");
    }
  }
}
