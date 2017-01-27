/*
 * Copyright 2014-2017 ISP RAS (http://www.ispras.ru)
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.randomizer.Variate;
import ru.ispras.fortress.randomizer.VariateBuilder;
import ru.ispras.fortress.randomizer.VariateSingleValue;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaData;
import ru.ispras.microtesk.model.api.metadata.MetaGroup;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.LabelManager;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.allocator.Allocator;
import ru.ispras.microtesk.test.sequence.engine.allocator.AllocatorBuilder;
import ru.ispras.microtesk.test.sequence.engine.allocator.ModeAllocator;

/**
 * The {@link Template} class builds the internal representation of a test template
 * and passes it for further processing.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Template {

  public static enum Section {
    PRE,
    POST,
    MAIN
  }

  public interface Processor {
    void process(ExceptionHandler handler);
    void process(Section section, Block block);
    void process(DataSection data);
    void finish();
  }

  private final EngineContext context;

  private final MetaModel metaModel;
  private final DataManager dataManager;
  private final PreparatorStore preparators;
  private final BufferPreparatorStore bufferPreparators;
  private final StreamStore streams;
  private final Processor processor;

  // Variates for mode and operation groups 
  private final Map<String, Variate<String>> groupVariates;

  // Default situations for instructions and groups
  private final Map<String, Variate<Situation>> defaultSituations;

  private PreparatorBuilder preparatorBuilder;
  private BufferPreparatorBuilder bufferPreparatorBuilder;
  private StreamPreparatorBuilder streamPreparatorBuilder;
  private ExceptionHandlerBuilder exceptionHandlerBuilder;
  private final Set<String> definedExceptionHandlers;

  private final Deque<BlockBuilder> blockBuilders;
  private CallBuilder callBuilder;

  private boolean isMainSection;
  private List<Call> globalPrologue;
  private List<Call> globalEpilogue;

  private final Set<Block> unusedBlocks;

  public Template(final EngineContext context, final Processor processor) {
    Logger.debugHeader("Started Processing Template");

    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(processor);

    this.context = context;

    this.metaModel = context.getModel().getMetaData();
    this.dataManager = context.getDataManager();
    this.preparators = context.getPreparators();
    this.bufferPreparators = context.getBufferPreparators();
    this.streams = context.getStreams();
    this.processor = processor;

    this.preparatorBuilder = null;
    this.bufferPreparatorBuilder = null;
    this.streamPreparatorBuilder = null;
    this.exceptionHandlerBuilder = null;
    this.definedExceptionHandlers = new LinkedHashSet<>();

    this.blockBuilders = new LinkedList<>();
    this.callBuilder = null;

    this.isMainSection = false;
    this.globalPrologue = Collections.emptyList();
    this.globalEpilogue = Collections.emptyList();

    this.groupVariates = newVariatesForGroups(metaModel);
    this.defaultSituations = new HashMap<>();

    this.unusedBlocks = new LinkedHashSet<>();
  }

  private void processBlock(final Section section, final Block block) {
    processor.process(section, block);
  }

  public DataManager getDataManager() {
    return dataManager;
  }

  public Processor getProcessor() {
    return processor;
  }

  public Set<Block> getUnusedBlocks() {
    return Collections.unmodifiableSet(unusedBlocks);
  }

  public BigInteger getAddressForLabel(final String label) {
    final LabelManager.Target target =
        context.getLabelManager().resolve(new Label(label, getCurrentBlockId()));

    if (null == target) {
      throw new GenerationAbortedException(
          String.format("The %s label is not defined.", label));
    }

    return BitVector.valueOf(target.getAddress(), 64).bigIntegerValue(false);
  }

  public void beginPreSection() {
    Logger.debugHeader("Started Processing Initialization Section");
    beginNewSection();
    isMainSection = false;
  }

  public void endPreSection() {
    final Block rootBlock = endCurrentSection().build();
    processBlock(Section.PRE, rootBlock);
    Logger.debugHeader("Ended Processing Initialization Section");
  }

  public void beginPostSection() {
    Logger.debugHeader("Started Processing Finalization Section");
    beginNewSection();
    isMainSection = false;
  }

  public void endPostSection() {
    final Block rootBlock = endCurrentSection().build();
    processBlock(Section.POST, rootBlock);
    Logger.debugHeader("Ended Processing Finalization Section");
  }

  public void beginMainSection() {
    Logger.debugHeader("Started Processing Main Section");
    beginNewSection();
    isMainSection = true;
  }

  public void endMainSection() {
    final BlockBuilder rootBlockBuilder = endCurrentSection();
    if (!rootBlockBuilder.isEmpty()) {
      final Block rootBlock = rootBlockBuilder.build();
      processBlock(Section.MAIN, rootBlock);
    }

    Logger.debugHeader("Ended Processing Main Section");
    isMainSection = false;
  }

  private void beginNewSection() {
    final BlockBuilder rootBlockBuilder = new BlockBuilder(true);
    rootBlockBuilder.setSequence(true);

    InvariantChecks.checkTrue(blockBuilders.isEmpty());
    this.blockBuilders.push(rootBlockBuilder);

    this.callBuilder = new CallBuilder(getCurrentBlockId());
  }

  private BlockBuilder endCurrentSection() {
    endBuildingCall();
    callBuilder = null;

    final BlockBuilder rootBuilder = blockBuilders.pop();
    InvariantChecks.checkTrue(blockBuilders.isEmpty());

    return rootBuilder;
  }

  private BlockId getCurrentBlockId() {
    return blockBuilders.peek().getBlockId();
  }

  public BlockBuilder beginBlock() {
    InvariantChecks.checkTrue(!blockBuilders.isEmpty());
    endBuildingCall();

    final BlockBuilder parent = blockBuilders.peek();
    final BlockBuilder current = parent.isExternal() ?
        new BlockBuilder(false) : new BlockBuilder(parent);

    blockBuilders.push(current);
    Logger.debug("Begin block: " + current.getBlockId());

    return current;
  }

  public BlockHolder endBlock() {
    InvariantChecks.checkTrue(blockBuilders.size() > 1);
    endBuildingCall();

    Logger.debug("End block: " + getCurrentBlockId());

    final BlockBuilder builder = blockBuilders.pop();
    final Block block;

    final boolean isRoot = blockBuilders.peek().isExternal();
    if (isRoot) {
      // A root block is just returned to the caller.
      // Then a new root block builder is created and pushed to the stack.
      block = builder.build(globalPrologue, globalEpilogue);
    } else {
      // A non-root block is added to its parent.
      block = builder.build();
      blockBuilders.peek().addBlock(block);
    }

    return new BlockHolder(block);
  }

  private void processExternalCode() {
    InvariantChecks.checkTrue(blockBuilders.size() == 1);
    endBuildingCall();

    final BlockBuilder rootBuilder = blockBuilders.pop();
    InvariantChecks.checkTrue(rootBuilder.isExternal());

    if (!rootBuilder.isEmpty()) {
      final Block rootBlock = rootBuilder.build();
      processBlock(Section.MAIN, rootBlock);
    }

    final BlockBuilder newRootBuilder = new BlockBuilder(true);
    newRootBuilder.setSequence(true);

    blockBuilders.push(newRootBuilder);
    callBuilder = new CallBuilder(getCurrentBlockId());
  }

  public final class BlockHolder {
    private final Block block;
    private boolean isAddedToUnused;

    private BlockHolder(final Block block) {
      InvariantChecks.checkNotNull(block);
      this.block = block;

      if (block.getBlockId().isRoot()) {
        unusedBlocks.add(block);
        this.isAddedToUnused = true;
      } else {
        this.isAddedToUnused = false;
      }
    }

    private void markBlockAsUsed() {
      if (isAddedToUnused) {
        unusedBlocks.remove(block);
        isAddedToUnused = false;
      }
    }

    private void checkAllowedToRun() {
      if (!isMainSection) {
        throw new GenerationAbortedException(
            "A block is allowed to run only in the main section of a test template.");
      }

      if (!block.getBlockId().isRoot()) {
        throw new GenerationAbortedException(String.format(
            "Running nested blocks is not allowed. At: %s", block.getWhere()));
      }
    }

    public BlockHolder add() {
      blockBuilders.peek().addBlock(block);
      markBlockAsUsed();
      return this;
    }

    public BlockHolder add(final int times) {
      for (int index = 0; index < times; index++) {
        blockBuilders.peek().addBlock(block);
      }
      markBlockAsUsed();
      return this;
    }

    public BlockHolder run() {
      checkAllowedToRun();
      processExternalCode();

      processBlock(Section.MAIN, block);

      markBlockAsUsed();
      return this;
    }

    public BlockHolder run(final int times) {
      checkAllowedToRun();
      processExternalCode();

      for (int index = 0; index < times; index++) {
        processBlock(Section.MAIN, block);
      }

      markBlockAsUsed();
      return this;
    }
  }

  public void addLabel(final String name) {
    final Label label = new Label(name, getCurrentBlockId());
    Logger.debug("Label: " + label.toString());
    callBuilder.addLabel(label);
  }

  public void addOutput(final Output output) {
    Logger.debug(output.toString());
    callBuilder.addOutput(output);
  }

  public void setCallText(final String text) {
    callBuilder.setText(text);
  }

  public void setRootOperation(final Primitive rootOperation) {
    callBuilder.setRootOperation(rootOperation);
  }

  public void endBuildingCall() {
    final Call call = callBuilder.build();
    Logger.debug("Ended building a call (empty = %b, executable = %b)",
        call.isEmpty(), call.isExecutable());

    addCall(call);
    this.callBuilder = new CallBuilder(getCurrentBlockId());
  }

  private void addCall(final Call call) {
    InvariantChecks.checkNotNull(call);

    if (!call.isEmpty()) {
      if (null != preparatorBuilder) {
        preparatorBuilder.addCall(call);
      } else if (null != bufferPreparatorBuilder) {
        bufferPreparatorBuilder.addCall(call);
      } else if (null != streamPreparatorBuilder) {
        streamPreparatorBuilder.addCall(call);
      } else if (null != exceptionHandlerBuilder) {
        exceptionHandlerBuilder.addCall(call);
      } else {
        blockBuilders.peek().addCall(call);
      }
    }
  }

  public PrimitiveBuilder newOperationBuilder(final String name) {
    Logger.debug("Operation: " + name);
    InvariantChecks.checkNotNull(name);

    return new PrimitiveBuilderOperation(name, metaModel, callBuilder);
  }

  public PrimitiveBuilder newAddressingModeBuilder(final String name) {
    Logger.debug("Addressing mode: " + name);
    InvariantChecks.checkNotNull(name);

    final MetaAddressingMode metaData = metaModel.getAddressingMode(name);
    if (null == metaData) {
      throw new IllegalArgumentException("No such addressing mode: " + name);
    }

    return new PrimitiveBuilderCommon(metaModel, callBuilder, metaData);
  }

  public RandomValue newRandom(final BigInteger from, final BigInteger to) {
    return new RandomValue(from, to);
  }

  public RandomValue newRandom(final Variate<?> variate) {
    return new RandomValue(variate);
  }

  public VariateBuilder<?> newVariateBuilder() {
    return new VariateBuilder<>();
  }

  public AllocatorBuilder newAllocatorBuilder(final String strategy) {
    return new AllocatorBuilder(strategy);
  }

  public void freeAllocatedMode(final Primitive mode, final boolean freeAll) {
    InvariantChecks.checkNotNull(mode);

    if (mode.getKind() != Primitive.Kind.MODE) {
      throw new GenerationAbortedException(
          mode.getName() + " is not an addressing mode.");
    }

    addCall(Call.newFreeAllocatedMode(mode, freeAll));
  }

  public UnknownImmediateValue newUnknownImmediate(
      final Where where,
      final Allocator allocator,
      final List<Primitive> retain,
      final List<Primitive> exclude) {
    return new UnknownImmediateValue(
        allocator,
        getModeValues(where, retain),
        getModeValues(where, exclude)
        );
  }

  private static List<Value> getModeValues(final Where where, final List<Primitive> modes) {
    if (null == modes) {
      return null;
    }

    final List<Value> result = new ArrayList<>();
    String modeName = null;

    for (final Primitive primitive : modes) {
      if (primitive.getKind() != Primitive.Kind.MODE) {
        throw new GenerationAbortedException(String.format(
            "%s: %s is not an addressing mode.", where, primitive.getName()));
      }

      if (modeName == null) {
        modeName = primitive.getName();
      } else if (!modeName.equals(primitive.getName())){
        throw new GenerationAbortedException(String.format(
            "Mismatch: all addressing modes must be %s.", modeName));
      }

      for (final Argument arg : primitive.getArguments().values()) {
        if (arg.getValue() instanceof BigInteger) {
          result.add(new FixedValue((BigInteger) arg.getValue()));
        } else if (arg.getValue() instanceof Value) {
          result.add((Value) arg.getValue());
        } else {
          InvariantChecks.checkTrue(false, "Unknown argument type: " + arg);
        }
      }
    }

    return result;
  }

  public OutputBuilder newOutput(final String kind, String format) {
    return new OutputBuilder(kind, format);
  }

  public Situation.Builder newSituation(String name) {
    return new Situation.Builder(name);
  }

  public void setDefaultSituation(final String name, final Situation situation) {
    InvariantChecks.checkNotNull(situation);
    setDefaultSituation(name, new VariateSingleValue<>(situation));
  }

  public void setDefaultSituation(final String name, final Variate<Situation> situation) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(situation);
    defaultSituations.put(name, situation);
  }

  public Variate<Situation> getDefaultSituation(final String name) {
    InvariantChecks.checkNotNull(name);
    return defaultSituations.get(name);
  }

  public PreparatorBuilder beginPreparator(
      final String targetName, final boolean isComparator) {
    endBuildingCall();

    Logger.debug("Begin preparator: %s", targetName);
    InvariantChecks.checkNotNull(targetName);

    if (null != preparatorBuilder) {
      throw new IllegalStateException(String.format(
          "Nesting is not allowed: The %s block cannot be nested into the %s block.",
          targetName, preparatorBuilder.getTargetName()));
    }

    InvariantChecks.checkTrue(null == preparatorBuilder);
    InvariantChecks.checkTrue(null == bufferPreparatorBuilder);
    InvariantChecks.checkTrue(null == streamPreparatorBuilder);
    InvariantChecks.checkTrue(null == exceptionHandlerBuilder);

    final MetaAddressingMode targetMode = metaModel.getAddressingMode(targetName);
    if (null == targetMode) {
      throw new IllegalArgumentException(String.format(
          "%s is not an addressing mode and cannot be a target for a preparator.", targetName));
    }

    preparatorBuilder = new PreparatorBuilder(targetMode, isComparator);
    return preparatorBuilder; 
  }

  public void endPreparator() {
    endBuildingCall();
    Logger.debug("End preparator: %s", preparatorBuilder.getTargetName());

    final Preparator preparator = preparatorBuilder.build();
    Logger.debug("Registering preparator: %s", preparator);

    final Preparator oldPreparator = preparators.addPreparator(preparator);
    if (null != oldPreparator) {
      Logger.warning("%s defined at %s is redefined at %s", 
          oldPreparator, oldPreparator.getWhere(), preparator.getWhere());
    }

    preparatorBuilder = null;
  }

  public void beginPreparatorVariant(final String name, final BigInteger bias) {
    checkPreparatorBlock();
    if (null != bias) {
      preparatorBuilder.beginVariant(name, bias.intValue());
    } else {
      preparatorBuilder.beginVariant(name);
    }
  }

  public void endPreparatorVariant() {
    checkPreparatorBlock();
    preparatorBuilder.endVariant();
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

  public void addPreparatorCall(
      final Primitive targetMode,
      final BigInteger value,
      final String preparatorName,
      final String variantName) {
    addPreparatorCall(targetMode, new FixedValue(value), preparatorName, variantName);
  }

  public void addPreparatorCall(
      final Primitive targetMode,
      final Value value,
      final String preparatorName,
      final String variantName) {
    InvariantChecks.checkNotNull(targetMode);
    InvariantChecks.checkNotNull(value);

    if (value instanceof LazyValue &&
        null == preparatorBuilder &&
        null == bufferPreparatorBuilder) {
      throw new IllegalStateException(
          "A preparator with a lazy value can be invoked only inside " + 
          "a preparator or buffer_preparator block.");
    }

    endBuildingCall();
    Logger.debug("Preparator reference: %s", targetMode.getName()); 

    final MetaAddressingMode metaTargetMode =
        metaModel.getAddressingMode(targetMode.getName());

    InvariantChecks.checkNotNull(
        metaTargetMode, "No such addressing mode: " + targetMode.getName());

    final int valueBitSize =
        metaTargetMode.getDataType().getBitSize();

    callBuilder.setPreparatorReference(
        new PreparatorReference(targetMode, value, valueBitSize, preparatorName, variantName));

    endBuildingCall();
  }

  public StreamPreparatorBuilder beginStreamPreparator(
      final String dataModeName, final String indexModeName) {

    endBuildingCall();

    Logger.debug("Begin stream preparator(data_source: %s, index_source: %s)",
        dataModeName, indexModeName);

    InvariantChecks.checkTrue(null == preparatorBuilder);
    InvariantChecks.checkTrue(null == bufferPreparatorBuilder);
    InvariantChecks.checkTrue(null == streamPreparatorBuilder);
    InvariantChecks.checkTrue(null == exceptionHandlerBuilder);

    final MetaAddressingMode dataMode = metaModel.getAddressingMode(dataModeName);
    if (null == dataMode) {
      throw new IllegalArgumentException(String.format(
          "%s is not an addressing mode.", dataModeName));
    }

    final MetaAddressingMode indexMode = metaModel.getAddressingMode(indexModeName);
    if (null == indexMode) {
      throw new IllegalArgumentException(String.format(
          "%s is not an addressing mode.", indexModeName));
    }

    streamPreparatorBuilder = new StreamPreparatorBuilder(
        context.getLabelManager(), dataMode, indexMode);

    return streamPreparatorBuilder;
  }

  public void endStreamPreparator() {
    endBuildingCall();

    Logger.debug("End stream preparator");

    final StreamPreparator streamPreparator = streamPreparatorBuilder.build();
    streams.addPreparator(streamPreparator);

    streamPreparatorBuilder = null;
  }

  public Primitive getDataSource() {
    checkStreamPreparatorBlock("data_source");
    return streamPreparatorBuilder.getDataSource();
  }

  public Primitive getIndexSource() {
    checkStreamPreparatorBlock("index_source");
    return streamPreparatorBuilder.getIndexSource();
  }

  public LabelValue getStartLabel() {
    checkStreamPreparatorBlock("start_label");
    return streamPreparatorBuilder.getStartLabel();
  }

  private void checkStreamPreparatorBlock(final String keyword) {
    if (null == streamPreparatorBuilder) {
      throw new IllegalStateException(String.format(
          "The %s keyword cannot be used outside a stream preparator block.", keyword));
    }
  }
  
  public void beginStreamInitMethod() {
    Logger.debug("Begin Stream Method: init");
    streamPreparatorBuilder.beginInitMethod();
  }

  public void beginStreamReadMethod() {
    Logger.debug("Begin Stream Method: read");
    streamPreparatorBuilder.beginReadMethod();
  }

  public void beginStreamWriteMethod() {
    Logger.debug("Begin Stream Method: write");
    streamPreparatorBuilder.beginWriteMethod();
  }

  public void endStreamMethod() {
    Logger.debug("End Stream Method");
    endBuildingCall();
    streamPreparatorBuilder.endMethod(); 
  }

  public void addStream(
      final String startLabelName,
      final Primitive dataSource,
      final Primitive indexSource,
      final int length) {

    Logger.debug("Stream: label=%s, data=%s, source=%s, length=%s",
        startLabelName, dataSource.getName(), indexSource.getName(), length);

    // Stream registers are excluded from random selection.
    ModeAllocator.get().exclude(dataSource);
    ModeAllocator.get().exclude(indexSource);

    streams.addStream(
        new Label(startLabelName, getCurrentBlockId()),
        dataSource,
        indexSource,
        length
        );

    /*
    // THIS IS CODE TO TEST DATA STREAMS. IT ADDS CALLS FROM DATA STREAMS
    // TO THE CURRENT TEST SEQUENCE.
    final Stream stream = streams.addStream(startLabelName, dataSource, indexSource, length);
    for (final Call call : stream.getInit()) {
      blockBuilders.peek().addCall(call);
    }

    int index = 0;
    while (index < length) {
      for (final Call call : stream.getRead()) {
        blockBuilders.peek().addCall(call);
      }
      index++;

      if (index < length) {
        for (final Call call : stream.getWrite()) {
          blockBuilders.peek().addCall(call);
        }
        index++;
      }
    }
    */
  }

  public BufferPreparatorBuilder beginBufferPreparator(final String bufferId) {
    endBuildingCall();

    Logger.debug("Begin buffer preparator: %s", bufferId);
    InvariantChecks.checkNotNull(bufferId);

    InvariantChecks.checkTrue(null == preparatorBuilder);
    InvariantChecks.checkTrue(null == bufferPreparatorBuilder);
    InvariantChecks.checkTrue(null == streamPreparatorBuilder);
    InvariantChecks.checkTrue(null == exceptionHandlerBuilder);

    bufferPreparatorBuilder = new BufferPreparatorBuilder(bufferId);
    return bufferPreparatorBuilder;
  }

  public void endBufferPreparator() {
    endBuildingCall();
    Logger.debug("End buffer preparator: %s", bufferPreparatorBuilder.getBufferId());

    final BufferPreparator bufferPreparator = bufferPreparatorBuilder.build();
    bufferPreparators.addPreparator(bufferPreparator);

    bufferPreparatorBuilder = null;
  }

  public LazyValue newAddressReference() {
    checkBufferPreparatorBlock();
    return bufferPreparatorBuilder.newAddressReference();
  }

  public LazyValue newAddressReference(final int start, final int end) {
    checkBufferPreparatorBlock();
    return bufferPreparatorBuilder.newAddressReference(start, end);
  }

  public LazyValue newEntryReference(final int start, final int end) {
    checkBufferPreparatorBlock();
    return bufferPreparatorBuilder.newEntryReference(start, end);
  }

  public LazyValue newEntryFieldReference(final String fieldId) {
    checkBufferPreparatorBlock();
    return bufferPreparatorBuilder.newEntryFieldReference(fieldId);
  }

  public LazyValue newEntryFieldReference(
      final String fieldId, final int start, final int end) {
    checkBufferPreparatorBlock();
    return bufferPreparatorBuilder.newEntryFieldReference(fieldId, start, end);
  }

  private void checkBufferPreparatorBlock() {
    if (null == bufferPreparatorBuilder) {
      throw new IllegalStateException(
          "The construct cannot be used outside a buffer_preparator block.");
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
    InvariantChecks.checkNotNull(model);

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
    InvariantChecks.checkNotNull(group);

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
    InvariantChecks.checkNotNull(name);

    final Variate<String> variate = groupVariates.get(name);
    if (null == variate) {
      throw new IllegalArgumentException(String.format("The %s group is not defined.", name));
    }

    return variate;
  }

  public void defineGroup(final String name, final Variate<String> variate) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(variate);

    if (groupVariates.containsKey(name)) {
      throw new IllegalStateException(String.format("%s group is already defined", name));
    }

    groupVariates.put(name, variate);
  }

  public ExceptionHandlerBuilder beginExceptionHandler(final String id) {
    InvariantChecks.checkNotNull(id);

    endBuildingCall();
    Logger.debug("Begin exception handler");

    if (definedExceptionHandlers.contains(id)) {
      throw new IllegalStateException(
          String.format("Exception handler %s is already defined.", id));
    }

    InvariantChecks.checkTrue(null == preparatorBuilder);
    InvariantChecks.checkTrue(null == bufferPreparatorBuilder);
    InvariantChecks.checkTrue(null == streamPreparatorBuilder);
    InvariantChecks.checkTrue(null == exceptionHandlerBuilder);

    exceptionHandlerBuilder = new ExceptionHandlerBuilder(id);
    return exceptionHandlerBuilder;
  }

  public void endExceptionHandler() {
    endBuildingCall();
    Logger.debug("End exception handler");

    final ExceptionHandler handler = exceptionHandlerBuilder.build();
    exceptionHandlerBuilder = null;

    processor.process(handler);
    definedExceptionHandlers.add(handler.getId());
  }

  public DataSectionBuilder beginData(final boolean isGlobalArgument, final boolean isSeparateFile) {
    endBuildingCall();

    final boolean isGlobalContext =
        blockBuilders.peek().isExternal() &&
        preparatorBuilder == null &&
        bufferPreparatorBuilder == null &&
        streamPreparatorBuilder == null &&
        exceptionHandlerBuilder == null;

    final boolean isGlobal = isGlobalContext || isGlobalArgument;
    Logger.debug("Begin Data (isGlobal=%b, isSeparateFile=%b)", isGlobal, isSeparateFile);
    return dataManager.beginData(getCurrentBlockId(), isGlobal, isSeparateFile);
  }

  public void endData() {
    Logger.debug("End Data");

    final DataSection data = dataManager.endData();
    if (data.isGlobal()) {
      processor.process(data);
    } else {
      endBuildingCall();
      addCall(Call.newData(data));
    }
  }

  public void setOrigin(final BigInteger origin) {
    Logger.debug("Set Origin to 0x%x", origin);
    callBuilder.setOrigin(origin, false);
  }

  public void setRelativeOrigin(final BigInteger delta) {
    Logger.debug("Set Relative Origin to 0x%x", delta);
    callBuilder.setOrigin(delta, true);
  }

  public void setAlignment(final BigInteger value, final BigInteger valueInBytes) {
    Logger.debug("Align %d (%d bytes)", value, valueInBytes);
    callBuilder.setAlignment(value, valueInBytes);
  }

  public void beginPrologue() {
    endBuildingCall();
    Logger.debug("Begin Test Case Level Prologue");

    InvariantChecks.checkTrue(null == preparatorBuilder);
    InvariantChecks.checkTrue(null == bufferPreparatorBuilder);
    InvariantChecks.checkTrue(null == streamPreparatorBuilder);
    InvariantChecks.checkTrue(null == exceptionHandlerBuilder);

    blockBuilders.peek().setPrologue(true);
  }

  public void endPrologue() {
    endBuildingCall();
    Logger.debug("End Test Case Level Prologue");

    final BlockBuilder currentBlockBuilder = blockBuilders.peek();
    currentBlockBuilder.setPrologue(false);

    if (currentBlockBuilder.isExternal()) {
      InvariantChecks.checkTrue(globalPrologue.isEmpty(), "Global test case level prologue is already defined");
      globalPrologue = currentBlockBuilder.getPrologue();
    }
  }

  public void beginEpilogue() {
    endBuildingCall();
    Logger.debug("Begin Test Case Level Epilogue");

    InvariantChecks.checkTrue(null == preparatorBuilder);
    InvariantChecks.checkTrue(null == bufferPreparatorBuilder);
    InvariantChecks.checkTrue(null == streamPreparatorBuilder);
    InvariantChecks.checkTrue(null == exceptionHandlerBuilder);

    blockBuilders.peek().setEpilogue(true);
  }

  public void endEpilogue() {
    endBuildingCall();
    Logger.debug("End Test Case Level Epilogue");

    final BlockBuilder currentBlockBuilder = blockBuilders.peek();
    currentBlockBuilder.setEpilogue(false);

    if (currentBlockBuilder.isExternal()) {
      InvariantChecks.checkTrue(globalEpilogue.isEmpty(), "Global test case level epilogue is already defined");
      globalEpilogue = currentBlockBuilder.getEpilogue();
    }
  }

  public MemoryObjectBuilder newMemoryObjectBuilder(final int size) {
    return new MemoryObjectBuilder(
        size,
        context.getLabelManager(),
        context.getSettings()
        );
  }

  public Where where(final String file, final int line) {
    return new Where(file, line);
  }
}
