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

import java.math.BigInteger;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ru.ispras.fortress.randomizer.Variate;
import ru.ispras.fortress.randomizer.VariateBuilder;
import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaData;
import ru.ispras.microtesk.model.api.metadata.MetaGroup;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;

public final class Template {

  public static enum Section {
    PRE,
    POST,
    MAIN
  }

  public interface Processor {
    void process(Section section, Block block);
    void finish();
  }

  private final MetaModel metaModel;
  private final DataManager dataManager;
  private final PreparatorStore preparators;
  private final Processor processor;

  // Variates for mode and operation groups 
  private final Map<String, Variate<String>> groupVariates;

  private PreparatorBuilder preparatorBuilder;
  private Deque<BlockBuilder> blockBuilders;
  private CallBuilder callBuilder;

  private boolean isMainSection;
  private int openBlockCount;

  public Template(
      MetaModel metaModel,
      DataManager dataManager,
      PreparatorStore preparators,
      Processor processor) {

    printHeader("Started Processing Template");

    checkNotNull(metaModel);
    checkNotNull(dataManager);
    checkNotNull(preparators);
    checkNotNull(processor);

    this.metaModel = metaModel;
    this.dataManager = dataManager;
    this.preparators = preparators;
    this.processor = processor;

    this.preparatorBuilder = null;
    this.blockBuilders = null;
    this.callBuilder = null;

    this.isMainSection = false;
    this.openBlockCount = 0;

    this.groupVariates = newVariatesForGroups(metaModel);
  }

  private void processBlock(Section section, Block block) {
    processor.process(section, block);
  }

  public DataManager getDataManager() {
    return dataManager;
  }

  public Processor getProcessor() {
    return processor;
  }

  public int getAddressForLabel(String label) {
    return dataManager.getMemoryMap().resolve(label);
  }

  public void beginPreSection() {
    printHeader("Started Processing Initialization Section");
    beginNewSection();

    isMainSection = false;
    openBlockCount = 0;
  }

  public void endPreSection() {
    final Block rootBlock = endCurrentSection();
    processBlock(Section.PRE, rootBlock);
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
    processBlock(Section.POST, rootBlock);
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
    processBlock(Section.MAIN, rootBlock);
    printHeader("Ended Processing Main Section");
    isMainSection = false;
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
        processBlock(Section.MAIN, parent.build());
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
      processBlock(Section.MAIN, block);

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

    return new PrimitiveBuilderOperation(
        name, metaModel, callBuilder, dataManager.getMemoryMap());
  }

  public PrimitiveBuilder newAddressingModeBuilder(String name) {
    trace("Addressing mode: " + name);
    checkNotNull(name);

    final MetaAddressingMode metaData = metaModel.getAddressingMode(name);
    if (null == metaData) {
      throw new IllegalArgumentException("No such addressing mode: " + name);
    }

    return new PrimitiveBuilderCommon(
        metaModel, callBuilder, dataManager.getMemoryMap(), metaData);
  }

  public RandomValue newRandom(BigInteger from, BigInteger to) {
    return new RandomValue(from, to);
  }

  public VariateBuilder<?> newVariateBuilder() {
    return new VariateBuilder<>();
  }

  public UnknownImmediateValue newUnknownImmediate() {
    return new UnknownImmediateValue();
  }

  public OutputBuilder newOutput(boolean isRuntime, boolean isComment, String format) {
    return new OutputBuilder(isRuntime, isComment, format);
  }

  public SituationBuilder newSituation(String name) {
    return new SituationBuilder(name);
  }

  public PreparatorBuilder beginPreparator(final String targetName) {
    endBuildingCall();

    trace("Begin preparator: %s", targetName);
    checkNotNull(targetName);

    final MetaAddressingMode targetMode = metaModel.getAddressingMode(targetName);
    if (null == targetMode) {
      throw new IllegalArgumentException(String.format(
          "%s is not an addressing mode and cannot be a target for a preparator.", targetName));
    }

    if (null != preparatorBuilder) {
      throw new IllegalStateException(String.format(
          "Nesting is not allowed: The %s preparator cannot be nested into the %s preparator.",
          targetName, preparatorBuilder.getTargetName()));
    }

    preparatorBuilder = new PreparatorBuilder(targetMode);
    return preparatorBuilder; 
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

  public PrimitiveBuilder newAddressingModeBuilderForGroup(final String name) {
    final Variate<String> variate = getGroupVariate(name);
    return newAddressingModeBuilder(variate.value());
  }

  public MetaOperation chooseMetaOperationFromGroup(final String name) {
    final Variate<String> variate = getGroupVariate(name);
    final String opName = variate.value();

    final MetaOperation metaOperation = metaModel.getOperation(opName);
    if (null == metaOperation) {
      throw new IllegalStateException("No such operation defined: " + opName);
    }

    return metaOperation;
  }

  private static Map<String, Variate<String>> newVariatesForGroups(final MetaModel model) {
    checkNotNull(model);

    final Map<String, Variate<String>> result = new HashMap<>();
    for (final MetaGroup group : model.getAddressingModeGroups()) {
      result.put(group.getName(), newVariateForGroup(group));
    }

    for (final MetaGroup group : model.getOperationGroups()) {
      result.put(group.getName(), newVariateForGroup(group));
    }

    return result;
  }

  private static Variate<String> newVariateForGroup(final MetaGroup group) {
    checkNotNull(group);

    final VariateBuilder<String> builder = new VariateBuilder<>();
    for (final MetaData item : group.getItems()) {
      if (item instanceof MetaGroup) {
        builder.addVariate(newVariateForGroup((MetaGroup) item));
      } else {
        builder.addValue(item.getName());
      }
    }

    return builder.build();
  }

  private Variate<String> getGroupVariate(final String name) {
    checkNotNull(name);

    final Variate<String> variate = groupVariates.get(name);
    if (null == variate) {
      throw new IllegalArgumentException(String.format("The %s group is not defined.", name));
    }

    return variate;
  }

  public void defineGroup(final String name, final Variate<String> variate) {
    checkNotNull(name);
    checkNotNull(variate);

    if (groupVariates.containsKey(name)) {
      throw new IllegalStateException(String.format("%s group is already defined", name));
    }

    groupVariates.put(name, variate);
  }
}
