/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TemplateBuilder.java, Aug 5, 2014 4:41:17 PM Andrei Tatarnikov
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

import java.util.Deque;
import java.util.LinkedList;

import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.iterator.IIterator;

import static ru.ispras.microtesk.utils.PrintingUtils.*;

public final class Template {
  private final MetaModel metaModel;

  private final Deque<BlockBuilder> blockBuilders;
  private CallBuilder callBuilder;

  private IIterator<Sequence<Call>> sequences;
  private final PreparatorStore preparators;

  public Template(MetaModel metaModel) {
    printHeader("Started Processing Template");

    if (null == metaModel) {
      throw new NullPointerException();
    }

    this.metaModel = metaModel;

    this.blockBuilders = new LinkedList<BlockBuilder>();
    this.blockBuilders.push(new BlockBuilder());
    this.callBuilder = new CallBuilder(getCurrentBlockId());

    this.sequences = null;
    this.preparators = new PreparatorStore();
  }

  public IIterator<Sequence<Call>> build() {
    endBuildingCall();

    printHeader("Ended Processing Template");

    if (null != sequences) {
      throw new IllegalStateException("The template is already built.");
    }

    final BlockBuilder rootBuilder = blockBuilders.getLast();
    final Block rootBlock = rootBuilder.build();

    sequences = rootBlock.getIterator();
    return sequences;
  }

  public IIterator<Sequence<Call>> getSequences() {
    if (null == sequences) {
      build();
    }
    return sequences;
  }

  public PreparatorStore getPreparators() {
    return preparators;
  }

  public BlockId getCurrentBlockId() {
    return blockBuilders.peek().getBlockId();
  }

  public BlockBuilder beginBlock() {
    endBuildingCall();

    final BlockBuilder parent = blockBuilders.peek();
    final BlockBuilder current = new BlockBuilder(parent);

    trace("Begin block: " + current.getBlockId());

    blockBuilders.push(current);
    return current;
  }

  public void endBlock() {
    endBuildingCall();

    trace("End block: " + getCurrentBlockId());

    final BlockBuilder builder = blockBuilders.pop();
    final Block block = builder.build();

    blockBuilders.peek().addBlock(block);
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
    blockBuilders.peek().addCall(call);

    trace("Ended building a call (empty = %b, executable = %b)",
      call.isEmpty(), call.isExecutable());

    this.callBuilder = new CallBuilder(getCurrentBlockId());
  }

  public PrimitiveBuilder newOperationBuilder(String name) {
    trace("Operation: " + name);

    if (null == name) {
      throw new NullPointerException();
    }

    return new PrimitiveBuilderOperation(name, metaModel, callBuilder);
  }

  public PrimitiveBuilder newAddressingModeBuilder(String name) {
    trace("Addressing mode: " + name);

    if (null == name) {
      throw new NullPointerException();
    }

    final MetaAddressingMode metaData = metaModel.getAddressingMode(name);
    if (null == metaData) {
      throw new IllegalArgumentException("No such addressing mode: " + name);
    }

    return new PrimitiveBuilderCommon(callBuilder, metaData);
  }

  public RandomValue newRandom(int from, int to) {
    return new RandomValue(from, to);
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
    // TODO
    trace("Begin preparator for '%s'", targetName);  
  }

  public void endPreparator() {
    // TODO
    trace("End preparator");
  }

  public LazyValue newLazy() {
    trace("newLazy");
    // TODO
    return null;
  }

  public LazyValue newLazy(int start, int end) {
    trace("newLazy (%d, %d)", start, end);
    // TODO
    return null;
  }

  public Primitive getPreparatorTarget() {
    trace("getPreparatorTarget");
    // TODO
    return null;
  }
}
