/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.randomizer.Variate;
import ru.ispras.fortress.randomizer.VariateBuilder;
import ru.ispras.fortress.randomizer.VariateSingleValue;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.model.memory.Sections;
import ru.ispras.microtesk.model.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.metadata.MetaData;
import ru.ispras.microtesk.model.metadata.MetaGroup;
import ru.ispras.microtesk.model.metadata.MetaModel;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.LabelManager;
import ru.ispras.microtesk.test.engine.EngineContext;
import ru.ispras.microtesk.test.engine.allocator.Allocator;
import ru.ispras.microtesk.test.engine.allocator.AllocatorBuilder;
import ru.ispras.microtesk.test.engine.allocator.ModeAllocator;
import ru.ispras.microtesk.utils.StringUtils;

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

/**
 * The {@link Template} class builds the internal representation of a test template
 * and passes it for further processing.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Template {

  public static enum SectionKind {
    PRE,
    POST,
    MAIN
  }

  public interface Processor {
    void process(ExceptionHandler handler);

    void process(SectionKind section, Block block);

    void process(SectionKind section, Block block, int times);

    void process(DataSection data);

    void finish();
  }

  private final EngineContext context;
  private final boolean isDebugPrinting;

  private final MetaModel metaModel;
  private final DataManager dataManager;
  private final PreparatorStore preparators;
  private final BufferPreparatorStore bufferPreparators;
  private final MemoryPreparatorStore memoryPreparators;
  private final StreamStore streams;
  private final Processor processor;

  // Variates for mode and operation groups
  private final Map<String, Variate<String>> groupVariates;

  // Default situations for instructions and groups
  private final Map<String, Variate<Situation>> defaultSituations;

  private PreparatorBuilder preparatorBuilder;
  private BufferPreparatorBuilder bufferPreparatorBuilder;
  private MemoryPreparatorBuilder memoryPreparatorBuilder;
  private StreamPreparatorBuilder streamPreparatorBuilder;
  private ExceptionHandlerBuilder exceptionHandlerBuilder;
  private final Set<String> definedExceptionHandlers;

  private final Deque<Map<String, Object>> attributes;
  private final Deque<Section> sections;
  private final Deque<BlockBuilder> blockBuilders;
  private AbstractCallBuilder callBuilder;

  private boolean isMainSection;
  private List<AbstractCall> globalPrologue;
  private List<AbstractCall> globalEpilogue;

  private final Set<Block> unusedBlocks;
  private final NumericLabelFactory numericLabelFactory;

  public Template(final EngineContext context, final Processor processor) {
    Logger.debugHeader("Started Processing Template");

    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(processor);

    this.context = context;
    this.isDebugPrinting = context.getOptions().getValueAsBoolean(Option.DEBUG);

    this.metaModel = context.getModel().getMetaData();
    this.dataManager = new DataManager(context);
    this.preparators = context.getPreparators();
    this.bufferPreparators = context.getBufferPreparators();
    this.memoryPreparators = context.getMemoryPreparators();
    this.streams = context.getStreams();
    this.processor = processor;

    this.preparatorBuilder = null;
    this.bufferPreparatorBuilder = null;
    this.memoryPreparatorBuilder = null;
    this.streamPreparatorBuilder = null;
    this.exceptionHandlerBuilder = null;
    this.definedExceptionHandlers = new LinkedHashSet<>();

    this.attributes = new LinkedList<>();
    this.sections = new LinkedList<>();
    this.blockBuilders = new LinkedList<>();
    this.callBuilder = null;

    this.isMainSection = false;
    this.globalPrologue = Collections.emptyList();
    this.globalEpilogue = Collections.emptyList();

    this.groupVariates = newVariatesForGroups(metaModel);
    this.defaultSituations = new HashMap<>();

    this.unusedBlocks = new LinkedHashSet<>();
    this.numericLabelFactory = new NumericLabelFactory();
  }

  public DataManager getDataManager() {
    return dataManager;
  }

  public Set<Block> getUnusedBlocks() {
    return Collections.unmodifiableSet(unusedBlocks);
  }

  public BigInteger getAddressForLabel(final String label) {
    final LabelManager.Target target =
        context.getLabelManager().resolve(Label.newLabel(label, getCurrentBlockId()));

    if (null == target) {
      throw new GenerationAbortedException(
          String.format("The %s label is not defined.", label));
    }

    return BitVector.valueOf(target.getAddress(), 64).bigIntegerValue(false);
  }

  public void beginPreSection() {
    Logger.debugHeader("Started Processing Initialization Section");
    this.callBuilder = new AbstractCallBuilder(new BlockId());
    isMainSection = false;
  }

  public void endPreSection() {
    final Block rootBlock = endCurrentSection().build();
    processor.process(SectionKind.PRE, rootBlock);
    Logger.debugHeader("Ended Processing Initialization Section");
  }

  public void beginPostSection() {
    Logger.debugHeader("Started Processing Finalization Section");
    this.callBuilder = new AbstractCallBuilder(new BlockId());
    isMainSection = false;
  }

  public void endPostSection() {
    final Block rootBlock = endCurrentSection().build();
    processor.process(SectionKind.POST, rootBlock);
    Logger.debugHeader("Ended Processing Finalization Section");
  }

  public void beginMainSection() {
    Logger.debugHeader("Started Processing Main Section");
    this.callBuilder = new AbstractCallBuilder(new BlockId());
    isMainSection = true;
  }

  public void endMainSection() {
    final BlockBuilder rootBlockBuilder = endCurrentSection();
    if (!rootBlockBuilder.isEmpty()) {
      final Block rootBlock = rootBlockBuilder.build();
      processor.process(SectionKind.MAIN, rootBlock);
    }

    Logger.debugHeader("Ended Processing Main Section");
    isMainSection = false;

    processor.finish();

    final Set<Block> unusedBlocks = getUnusedBlocks();
    if (!unusedBlocks.isEmpty()) {
      Logger.warning("Unused blocks have been detected at: %s",
          StringUtils.toString(unusedBlocks, ", ", new StringUtils.Converter<Block>() {
              @Override
              public String toString(final Block o) {
                return o.getWhere().toString();
              }
          }));
    }
  }

  private BlockBuilder currentBlockBuilder() {
    if (!blockBuilders.isEmpty()) {
      return blockBuilders.peek();
    }

    final Section section =
        !sections.isEmpty() ? sections.peek() : Sections.get().getTextSection();

    checkSectionDefined(section,
        context.getOptions().getValueAsString(Option.TEXT_SECTION_KEYWORD));

    final BlockBuilder rootBlockBuilder = new BlockBuilder(true, section);
    rootBlockBuilder.setSequence(true);

    InvariantChecks.checkTrue(blockBuilders.isEmpty());
    this.blockBuilders.push(rootBlockBuilder);

    return rootBlockBuilder;
  }

  private BlockBuilder endCurrentSection() {
    endBuildingCall();
    callBuilder = null;

    final BlockBuilder rootBuilder = currentBlockBuilder();

    blockBuilders.pop();
    InvariantChecks.checkTrue(blockBuilders.isEmpty());

    return rootBuilder;
  }

  private BlockId getCurrentBlockId() {
    return currentBlockBuilder().getBlockId();
  }

  public BlockBuilder beginBlock() {
    endBuildingCall();

    final BlockBuilder parent = currentBlockBuilder();

    final Section section =
        !sections.isEmpty() ? sections.peek() : Sections.get().getTextSection();

    checkSectionDefined(section,
        context.getOptions().getValueAsString(Option.TEXT_SECTION_KEYWORD));

    final BlockBuilder current = parent.isExternal()
        ? new BlockBuilder(false, section) : new BlockBuilder(parent);

    blockBuilders.push(current);
    debug("Begin block: " + current.getBlockId());

    return current;
  }

  public BlockHolder endBlock() {
    InvariantChecks.checkTrue(blockBuilders.size() > 1);
    endBuildingCall();

    debug("End block: " + getCurrentBlockId());

    final BlockBuilder builder = blockBuilders.pop();
    final Block block;

    final boolean isRoot = currentBlockBuilder().isExternal();
    if (isRoot) {
      // A root block is just returned to the caller.
      // Then a new root block builder is created and pushed to the stack.
      block = builder.build(globalPrologue, globalEpilogue);
    } else {
      // A non-root block is added to its parent.
      block = builder.build();
      currentBlockBuilder().addBlock(block);
    }

    return new BlockHolder(block);
  }

  private void processExternalCode() {
    endBuildingCall();

    // No active block builder, no created calls -> nothing to process.
    if (blockBuilders.isEmpty()) {
      return;
    }

    final BlockBuilder rootBuilder = blockBuilders.pop();
    InvariantChecks.checkTrue(rootBuilder.isExternal());

    if (!rootBuilder.isEmpty()) {
      final Block rootBlock = rootBuilder.build();
      processor.process(SectionKind.MAIN, rootBlock);
    }
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
        final BlockBuilder wrapping = currentBlockBuilder();
        throw new GenerationAbortedException(String.format(
            "Running nested blocks is not allowed. Nested is at: %s, wrapping is at: %s",
            block.getWhere(),
            wrapping.getWhere()
            ));
      }
    }

    public BlockHolder add() {
      currentBlockBuilder().addBlock(block);
      markBlockAsUsed();
      return this;
    }

    public BlockHolder add(final int times) {
      for (int index = 0; index < times; index++) {
        currentBlockBuilder().addBlock(block);
      }
      markBlockAsUsed();
      return this;
    }

    public BlockHolder run() {
      checkAllowedToRun();

      processExternalCode();
      processor.process(SectionKind.MAIN, block);

      markBlockAsUsed();
      return this;
    }

    public BlockHolder run(final int times) {
      checkAllowedToRun();

      processExternalCode();
      processor.process(SectionKind.MAIN, block, times);

      markBlockAsUsed();
      return this;
    }
  }

  public void addLabel(final String name, final boolean global) {
    final Label label = global ? Label.newGlobal(name, getCurrentBlockId())
                               : Label.newLabel(name, getCurrentBlockId());

    debug("Label: %s", label);
    callBuilder.addLabel(label);
  }

  public void addNumericLabel(final int index) {
    final Label label = numericLabelFactory.newLabel(index, getCurrentBlockId());
    debug("Label: %s", label);
    callBuilder.addLabel(label);
  }

  public LabelValue newNumericLabelRef(final int index, final boolean forward) {
    return numericLabelFactory.newLabelRef(index, getCurrentBlockId(), forward);
  }

  public void addOutput(final Output output) {
    debug(output.toString());
    callBuilder.addOutput(output);
  }

  public void setCallText(final String text) {
    callBuilder.setText(text);
  }

  public void setRootOperation(final Primitive rootOperation, final Where where) {
    callBuilder.setWhere(where);
    callBuilder.setRootOperation(rootOperation);
  }

  public void endBuildingCall() {
    final AbstractCall call = callBuilder.build();
    debug("Ended building a call (empty = %b, executable = %b)",
        call.isEmpty(), call.isExecutable());

    // The call is empty and there is nowhere to add it: no needed to recreate the builder.
    if (call.isEmpty() && blockBuilders.isEmpty()) {
      return;
    }

    addCall(call);
    this.callBuilder = new AbstractCallBuilder(getCurrentBlockId());
  }

  private void addCall(final AbstractCall call) {
    InvariantChecks.checkNotNull(call);

    if (call.isEmpty()) {
      return;
    }

    if (!attributes.isEmpty()) {
      call.getAttributes().putAll(attributes.peek());
    }

    if (null != preparatorBuilder) {
      preparatorBuilder.addCall(call);
    } else if (null != bufferPreparatorBuilder) {
      bufferPreparatorBuilder.addCall(call);
    } else if (null != memoryPreparatorBuilder) {
      memoryPreparatorBuilder.addCall(call);
    } else if (null != streamPreparatorBuilder) {
      streamPreparatorBuilder.addCall(call);
    } else if (null != exceptionHandlerBuilder) {
      exceptionHandlerBuilder.addCall(call);
    } else {
      currentBlockBuilder().addCall(call);
    }
  }

  public PrimitiveBuilder newOperationBuilder(final String name) {
    debug("Operation: " + name);
    InvariantChecks.checkNotNull(name);

    return new PrimitiveBuilderOperation(name, metaModel, callBuilder);
  }

  public PrimitiveBuilder newAddressingModeBuilder(final String name) {
    debug("Addressing mode: " + name);
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

    addCall(AbstractCall.newFreeAllocatedMode(mode, freeAll));
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
      } else if (!modeName.equals(primitive.getName())) {
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

  public LabelValue newLazyLabel() {
    return LabelValue.newLazy();
  }

  public OutputBuilder newOutput(final String kind, String format) {
    return new OutputBuilder(kind, format);
  }

  public Situation.Builder newSituation(final String name, final boolean testDataProvider) {
    return new Situation.Builder(name, testDataProvider);
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

    debug("Begin preparator: %s", targetName);
    InvariantChecks.checkNotNull(targetName);

    if (null != preparatorBuilder) {
      throw new IllegalStateException(String.format(
          "Nesting is not allowed: The %s block cannot be nested into the %s block.",
          targetName, preparatorBuilder.getTargetName()));
    }

    InvariantChecks.checkTrue(null == preparatorBuilder);
    InvariantChecks.checkTrue(null == bufferPreparatorBuilder);
    InvariantChecks.checkTrue(null == memoryPreparatorBuilder);
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
    debug("End preparator: %s", preparatorBuilder.getTargetName());

    final Preparator preparator = preparatorBuilder.build();
    debug("Registering preparator: %s", preparator);

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
    if (null != preparatorBuilder) {
      return preparatorBuilder.newValue();
    }

    if (null != memoryPreparatorBuilder) {
      return memoryPreparatorBuilder.newDataReference();
    }

    throw new IllegalStateException("The construct cannot be used outside a preparator block.");
  }

  public LazyValue newLazy(final int start, final int end) {
    if (null != preparatorBuilder) {
      return preparatorBuilder.newValue(start, end);
    }

    if (null != memoryPreparatorBuilder) {
      return memoryPreparatorBuilder.newDataReference(start, end);
    }

    throw new IllegalStateException("The construct cannot be used outside a preparator block.");
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

    if (value instanceof LazyValue
        && null == preparatorBuilder
        && null == bufferPreparatorBuilder
        && null == memoryPreparatorBuilder) {
      throw new IllegalStateException(
          "A preparator with a lazy value can be invoked only inside "
              + "a preparator, buffer_preparator or memory_preparator block."
          );
    }

    endBuildingCall();
    debug("Preparator reference: %s", targetMode.getName());

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

    debug("Begin stream preparator(data_source: %s, index_source: %s)",
        dataModeName, indexModeName);

    InvariantChecks.checkTrue(null == preparatorBuilder);
    InvariantChecks.checkTrue(null == bufferPreparatorBuilder);
    InvariantChecks.checkTrue(null == memoryPreparatorBuilder);
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

    debug("End stream preparator");

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
    debug("Begin Stream Method: init");
    streamPreparatorBuilder.beginInitMethod();
  }

  public void beginStreamReadMethod() {
    debug("Begin Stream Method: read");
    streamPreparatorBuilder.beginReadMethod();
  }

  public void beginStreamWriteMethod() {
    debug("Begin Stream Method: write");
    streamPreparatorBuilder.beginWriteMethod();
  }

  public void endStreamMethod() {
    debug("End Stream Method");
    endBuildingCall();
    streamPreparatorBuilder.endMethod();
  }

  public void addStream(
      final String startLabelName,
      final Primitive dataSource,
      final Primitive indexSource,
      final int length) {
    debug("Stream: label=%s, data=%s, source=%s, length=%s",
        startLabelName, dataSource.getName(), indexSource.getName(), length);

    // Stream registers are excluded from random selection.
    ModeAllocator.get().exclude(dataSource);
    ModeAllocator.get().exclude(indexSource);

    streams.addStream(
        Label.newLabel(startLabelName, getCurrentBlockId()),
        dataSource,
        indexSource,
        length
    );

    /*
    // THIS IS CODE TO TEST DATA STREAMS. IT ADDS CALLS FROM DATA STREAMS
    // TO THE CURRENT TEST SEQUENCE.
    final Stream stream = streams.addStream(startLabelName, dataSource, indexSource, length);
    for (final Call call : stream.getInit()) {
      currentBlockBuilder().addCall(call);
    }

    int index = 0;
    while (index < length) {
      for (final Call call : stream.getRead()) {
        currentBlockBuilder().addCall(call);
      }
      index++;

      if (index < length) {
        for (final Call call : stream.getWrite()) {
          currentBlockBuilder().addCall(call);
        }
        index++;
      }
    }
    */
  }

  public BufferPreparatorBuilder beginBufferPreparator(final String bufferId) {
    endBuildingCall();

    debug("Begin buffer preparator: %s", bufferId);
    InvariantChecks.checkNotNull(bufferId);

    InvariantChecks.checkTrue(null == preparatorBuilder);
    InvariantChecks.checkTrue(null == bufferPreparatorBuilder);
    InvariantChecks.checkTrue(null == memoryPreparatorBuilder);
    InvariantChecks.checkTrue(null == streamPreparatorBuilder);
    InvariantChecks.checkTrue(null == exceptionHandlerBuilder);

    bufferPreparatorBuilder = new BufferPreparatorBuilder(bufferId);
    return bufferPreparatorBuilder;
  }

  public MapBuilder newMapBuilder() {
    return new MapBuilder();
  }

  public void endBufferPreparator() {
    endBuildingCall();
    debug("End buffer preparator: %s", bufferPreparatorBuilder.getBufferId());

    final BufferPreparator bufferPreparator = bufferPreparatorBuilder.build();
    bufferPreparators.addPreparator(bufferPreparator);
    bufferPreparatorBuilder = null;
  }

  public MemoryPreparatorBuilder beginMemoryPreparator(final int dataSize) {
    endBuildingCall();
    debug("Begin memory preparator (size: %d)", dataSize);

    InvariantChecks.checkTrue(null == preparatorBuilder);
    InvariantChecks.checkTrue(null == bufferPreparatorBuilder);
    InvariantChecks.checkTrue(null == memoryPreparatorBuilder);
    InvariantChecks.checkTrue(null == streamPreparatorBuilder);
    InvariantChecks.checkTrue(null == exceptionHandlerBuilder);

    memoryPreparatorBuilder = new MemoryPreparatorBuilder(dataSize);
    return memoryPreparatorBuilder;
  }

  public void endMemoryPreparator() {
    endBuildingCall();
    debug("End memory preparator (size: %d)", memoryPreparatorBuilder.getDataSize());

    final MemoryPreparator memoryPreparator = memoryPreparatorBuilder.build();
    memoryPreparators.addPreparator(memoryPreparator);
    memoryPreparatorBuilder = null;
  }

  public LazyValue newAddressReference(final int level) {
    if (null != bufferPreparatorBuilder) {
      return bufferPreparatorBuilder.newAddressReference(level);
    }

    if (null != memoryPreparatorBuilder) {
      return memoryPreparatorBuilder.newAddressReference();
    }

    throw new IllegalStateException(
        "The construct cannot be used outside a buffer_preparator or memory_preparator block.");
  }

  public LazyValue newAddressReference(final int level, final int start, final int end) {
    if (null != bufferPreparatorBuilder) {
      return bufferPreparatorBuilder.newAddressReference(level, start, end);
    }

    if (null != memoryPreparatorBuilder) {
      return memoryPreparatorBuilder.newAddressReference(start, end);
    }

    throw new IllegalStateException(
        "The construct cannot be used outside a buffer_preparator or memory_preparator block.");
  }

  public LazyValue newEntryReference(final int level) {
    checkBufferPreparatorBlock();
    return bufferPreparatorBuilder.newEntryReference(level);
  }

  public LazyValue newEntryReference(final int level, final int start, final int end) {
    checkBufferPreparatorBlock();
    return bufferPreparatorBuilder.newEntryReference(level, start, end);
  }

  public LazyValue newEntryFieldReference(final int level, final String fieldId) {
    checkBufferPreparatorBlock();
    return bufferPreparatorBuilder.newEntryFieldReference(level, fieldId);
  }

  public LazyValue newEntryFieldReference(
      final int level,
      final String fieldId,
      final int start,
      final int end) {
    checkBufferPreparatorBlock();
    return bufferPreparatorBuilder.newEntryFieldReference(level, fieldId, start, end);
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
    debug("Begin exception handler");

    if (definedExceptionHandlers.contains(id)) {
      throw new IllegalStateException(
          String.format("Exception handler %s is already defined.", id));
    }

    InvariantChecks.checkTrue(null == preparatorBuilder);
    InvariantChecks.checkTrue(null == bufferPreparatorBuilder);
    InvariantChecks.checkTrue(null == memoryPreparatorBuilder);
    InvariantChecks.checkTrue(null == streamPreparatorBuilder);
    InvariantChecks.checkTrue(null == exceptionHandlerBuilder);

    final Section section =
        !sections.isEmpty() ? sections.peek() : Sections.get().getTextSection();

    checkSectionDefined(section,
        context.getOptions().getValueAsString(Option.TEXT_SECTION_KEYWORD));

    exceptionHandlerBuilder = new ExceptionHandlerBuilder(id, section, isDebugPrinting);
    return exceptionHandlerBuilder;
  }

  public void endExceptionHandler() {
    endBuildingCall();
    debug("End exception handler");

    final ExceptionHandler handler = exceptionHandlerBuilder.build();
    exceptionHandlerBuilder = null;

    processor.process(handler);
    definedExceptionHandlers.add(handler.getId());
  }

  public DataSectionBuilder beginData(
      final boolean isGlobalArgument,
      final boolean isSeparateFile) {
    endBuildingCall();

    final boolean isGlobalContext =
        currentBlockBuilder().isExternal()
            && preparatorBuilder == null
            && bufferPreparatorBuilder == null
            && memoryPreparatorBuilder == null
            && streamPreparatorBuilder == null
            && exceptionHandlerBuilder == null;

    final boolean isGlobal = isGlobalContext || isGlobalArgument;
    debug("Begin Data (isGlobal=%b, isSeparateFile=%b)", isGlobal, isSeparateFile);

    final Section section =
        !sections.isEmpty() ? sections.peek() : Sections.get().getDataSection();

    checkSectionDefined(section,
        context.getOptions().getValueAsString(Option.DATA_SECTION_KEYWORD));

    return dataManager.beginData(
        getCurrentBlockId(),
        section,
        isGlobal,
        isSeparateFile
        );
  }

  public void endData() {
    debug("End Data");

    final DataSection data = dataManager.endData();
    if (data.isGlobal()) {
      processor.process(data);
    } else {
      endBuildingCall();
      addCall(AbstractCall.newData(data));
    }
  }

  public void generateData(
      final BigInteger address,
      final String labelName,
      final String typeId,
      final int length,
      final String method,
      final boolean isSeparateFile) {
    final DataSection data =
        dataManager.generateData(address, labelName, typeId, length, method, isSeparateFile);
    processor.process(data);
  }

  public void setOrigin(final BigInteger origin, final Where where) {
    // .org directives in external code split it into parts (only for main section)
    if (isMainSection && currentBlockBuilder().isExternal()) {
      processExternalCode();
    }

    debug("Set Origin to 0x%x", origin);
    callBuilder.setOrigin(origin, false);
    callBuilder.setWhere(where);
  }

  public void setRelativeOrigin(final BigInteger delta, final Where where) {
    debug("Set Relative Origin to 0x%x", delta);
    callBuilder.setOrigin(delta, true);
    callBuilder.setWhere(where);
  }

  public void setAlignment(
      final BigInteger value,
      final BigInteger valueInBytes,
      final Where where) {
    debug("Align %d (%d bytes)", value, valueInBytes);
    callBuilder.setAlignment(value, valueInBytes);
    callBuilder.setWhere(where);
  }

  public void beginSection(
      final String name,
      final BigInteger pa,
      final BigInteger va,
      final String args) {
    processExternalCode();
    InvariantChecks.checkNotNull(name);

    Section section = Sections.get().getSection(name, false);
    if (null == section) {
      section = new Section(name, false, pa, va, args);
      Sections.get().addSection(section);
    } else {
      checkSectionRedefined(section, pa, va, args);
    }

    sections.push(section);
  }

  public void beginSectionText(
      final BigInteger pa,
      final BigInteger va,
      final String args) {
    processExternalCode();
    Section section = Sections.get().getTextSection();

    if (null == section) {
      final String name = context.getOptions().getValueAsString(Option.TEXT_SECTION_KEYWORD);
      section = new Section(name, true, pa, va);
      Sections.get().setTextSection(section);
    } else {
      checkSectionRedefined(section, pa, va, args);
    }

    sections.push(section);
  }

  public void beginSectionData(
      final BigInteger pa,
      final BigInteger va,
      final String args) {
    processExternalCode();
    Section section = Sections.get().getDataSection();

    if (null == section) {
      final String name = context.getOptions().getValueAsString(Option.DATA_SECTION_KEYWORD);
      section = new Section(name, true, pa, va);
      Sections.get().setDataSection(section);
    } else {
      checkSectionRedefined(section, pa, va, args);
    }

    sections.push(section);

  }

  public void endSection() {
    processExternalCode();
    sections.pop();
  }

  private static void checkSectionRedefined(
      final Section section,
      final BigInteger pa,
      final BigInteger va,
      final String args) {
    if (null != pa && !section.getBasePa().equals(pa)
        || null != va && !section.getBaseVa().equals(va)
        || null != args && !section.getArgs().equals(args)) {
      Logger.warning("Changing section attributes is not allowed: %s.", section);
    }
  }

  private static void checkSectionDefined(final Section section, final String name) {
    if (null == section) {
      throw new GenerationAbortedException(
          String.format("Section %s is not defined in the template.", name));
    }
  }

  public void beginAttibutes(final MapBuilder builder) {
    final Map<String, Object> currentAttributes;
    if (attributes.isEmpty()) {
      currentAttributes = builder.getMap();
    } else {
      currentAttributes = new HashMap<>(attributes.peek());
      currentAttributes.putAll(builder.getMap());
    }
    attributes.push(currentAttributes);
  }

  public void endAttibutes() {
    attributes.pop();
  }

  public void beginPrologue() {
    endBuildingCall();
    debug("Begin Test Case Level Prologue");

    InvariantChecks.checkTrue(null == preparatorBuilder);
    InvariantChecks.checkTrue(null == bufferPreparatorBuilder);
    InvariantChecks.checkTrue(null == memoryPreparatorBuilder);
    InvariantChecks.checkTrue(null == streamPreparatorBuilder);
    InvariantChecks.checkTrue(null == exceptionHandlerBuilder);

    currentBlockBuilder().setPrologue(true);
  }

  public void endPrologue() {
    endBuildingCall();
    debug("End Test Case Level Prologue");

    final BlockBuilder currentBlockBuilder = currentBlockBuilder();
    currentBlockBuilder.setPrologue(false);

    if (currentBlockBuilder.isExternal()) {
      InvariantChecks.checkTrue(globalPrologue.isEmpty(),
          "Global test case level prologue is already defined");
      globalPrologue = currentBlockBuilder.getPrologue();
    }
  }

  public void beginEpilogue() {
    endBuildingCall();
    debug("Begin Test Case Level Epilogue");

    InvariantChecks.checkTrue(null == preparatorBuilder);
    InvariantChecks.checkTrue(null == bufferPreparatorBuilder);
    InvariantChecks.checkTrue(null == memoryPreparatorBuilder);
    InvariantChecks.checkTrue(null == streamPreparatorBuilder);
    InvariantChecks.checkTrue(null == exceptionHandlerBuilder);

    currentBlockBuilder().setEpilogue(true);
  }

  public void endEpilogue() {
    endBuildingCall();
    debug("End Test Case Level Epilogue");

    final BlockBuilder currentBlockBuilder = currentBlockBuilder();
    currentBlockBuilder.setEpilogue(false);

    if (currentBlockBuilder.isExternal()) {
      InvariantChecks.checkTrue(globalEpilogue.isEmpty(),
          "Global test case level epilogue is already defined");
      globalEpilogue = currentBlockBuilder.getEpilogue();
    }
  }

  public MemoryObjectBuilder newMemoryObjectBuilder(final int size) {
    return new MemoryObjectBuilder(
        size,
        context.getLabelManager(),
        GeneratorSettings.get()
        );
  }

  public Where where(final String file, final int line) {
    return new Where(file, line);
  }

  private void debug(final String format, final Object... args) {
    if (isDebugPrinting) {
      Logger.debug(format, args);
    }
  }
}
