/*
 * Copyright 2016-2021 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.tools.symexec;

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.solver.engine.smt.Cvc4Solver;
import ru.ispras.fortress.solver.engine.smt.SmtTextBuilder;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;

import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.TemporaryVariables;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.tools.Disassembler;
import ru.ispras.microtesk.tools.Disassembler.Output;
import ru.ispras.microtesk.tools.symexec.ControlFlowInspector.Range;
import ru.ispras.microtesk.translator.mir.BasicBlock;
import ru.ispras.microtesk.translator.mir.DestructCssa;
import ru.ispras.microtesk.translator.mir.ForwardPass;
import ru.ispras.microtesk.translator.mir.Instruction;
import ru.ispras.microtesk.translator.mir.Instruction.Branch;
import ru.ispras.microtesk.translator.mir.Instruction.Return;
import ru.ispras.microtesk.translator.mir.Mir2Node;
import ru.ispras.microtesk.translator.mir.MirArchive;
import ru.ispras.microtesk.translator.mir.MirBlock;
import ru.ispras.microtesk.translator.mir.MirBuilder;
import ru.ispras.microtesk.translator.mir.MirContext;
import ru.ispras.microtesk.translator.mir.MirPassDriver;
import ru.ispras.microtesk.translator.mir.MirText;
import ru.ispras.microtesk.translator.mir.Operand;
import ru.ispras.microtesk.translator.mir.Pass;
import ru.ispras.microtesk.translator.mir.Static;
import ru.ispras.microtesk.translator.mir.StoreAnalysis;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

public final class SymbolicExecutor {
  private SymbolicExecutor() {}

  public static boolean execute(
      final Options options,
      final String modelName,
      final String fileName) {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(modelName);
    InvariantChecks.checkNotNull(fileName);

    Logger.message("Analyzing file: %s...", fileName);
    final DisassemblerOutputFactory outputFactory = new DisassemblerOutputFactory();
    if (!Disassembler.disassemble(options, modelName, fileName, outputFactory)) {
      Logger.error("Failed to disassemble " + fileName);
    }

    final DisassemblerOutput output = outputFactory.getOutput();
    InvariantChecks.checkNotNull(output);

    final Model model = outputFactory.getModel();
    final List<IsaPrimitive> instructions = output.getInstructions();
    InvariantChecks.checkNotNull(instructions);

    final BodyInfo info = writeMir(outputFactory.getModel(), instructions);
    inspectControlFlow(model, info);
    compileBasicBlocks(fileName, info);
    writeControlFlow(fileName + ".json", info);
    writeBasicBlockSmt(fileName, info);
    writeMir(fileName, info);

    return true;
  }

  private static void compileBasicBlocks(final String fileName, final BodyInfo info) {
    final MirPassDriver driver =
        MirPassDriver.newOptimizing().setStorage(info.storage);
    final StoreAnalysis analysis = new StoreAnalysis();
    final Mir2Node smtOutput = new Mir2Node();

    final String pcName =
      info.archive.getManifest().getJsonObject("program_counter").getString("name");

    int index = 0;
    for (final Range range : info.bbRange) {
      final String name = String.format("bb_%d", index++);
      final MirContext mir =
        FormulaBuilder.buildMir(name, info.archive, info.bodyMir.subList(range.start, range.end));
      final MirContext opt = driver.apply(mir);
      analysis.apply(opt);

      info.bbMir.add(opt);
      info.bbCond.add(analysis.getCondition(pcName));
      info.bbModified.add(analysis.modifiedMap());
      info.bbIndexed.add(analysis.versionMap());

      Logger.debug(MirText.toString(opt));
    }
  }

  private static void writeMir(final String name, final BodyInfo info) {
    final MirContext mir = composeMir(name, info);
    try (final java.io.BufferedWriter writer =
        Files.newBufferedWriter(
            Paths.get(name + ".mir"), java.nio.charset.StandardCharsets.UTF_8)) {
      writer.write(MirText.toString(mir));
    } catch (final java.io.IOException e) {
      Logger.error(e.getMessage());
    }
  }

  private static MirContext composeMir(final String name, final BodyInfo info) {
    final MirContext mir = new MirContext(name, MirBuilder.VOID_TO_VOID_TYPE);

    final int nblocks = info.bbMir.size();
    final List<MirBlock> intros = new java.util.ArrayList<>(nblocks);
    final List<MirBlock> outros = new java.util.ArrayList<>(nblocks);

    int bbIndex = 0;
    final var destruct = new DestructCssa();
    for (final MirContext body : info.bbMir) {
      final var inoutMap = info.bbInOut.get(bbIndex);
      final var linkPair =
          wrapInline(destruct.apply(body), mir, inoutMap.values());
      intros.add(linkPair.first);
      outros.add(linkPair.second);
    }
    final var ranges = info.bbRange;
    for (int i = 0; i < nblocks; ++i) {
      final var range = ranges.get(i);
      final int taken = getLinkIndex(range, range.nextTaken, ranges);
      final int other = getLinkIndex(range, range.nextOther, ranges);

      final var outro = outros.get(i);
      if (taken >= 0 && taken == other) {
        outro.jump(intros.get(taken).bb);
      } else if (taken >= 0) {
        final Operand guard = outro.getLocal(evalGuardId(i, info));
        outro.append(new Branch(guard, intros.get(taken).bb, intros.get(other).bb));
      } else {
        outro.append(new Return(null));
      }
    }
    return mir;
  }

  private static Pair<MirBlock, MirBlock> wrapInline(
      final MirContext callee,
      final MirContext mir,
      final Collection<Pair<Static, Static>> inout) {
    final MirBlock inbb = mir.newBlock();
    final int start = mir.blocks.size();
    final int end = start + callee.blocks.size();
    Pass.inlineContext(mir, callee);
    final MirBlock outbb = mir.newBlock();

    /*
    for (final var pair : inout) {
      inbb.assign(pair.first, pair.first.newVersion(0));
      outbb.assign(pair.second.newVersion(0), pair.second);
    }
    */

    final BasicBlock entry = mir.blocks.get(start);
    inbb.jump(entry);
    for (final BasicBlock bb : mir.blocks.subList(start, end)) {
      final int index = bb.insns.size() - 1;
      final Instruction insn = bb.insns.get(index);

      if (insn instanceof Return) {
        bb.insns.set(index, new Branch(outbb.bb));
      }
    }
    return new Pair<>(inbb, outbb);
  }

  private static BodyInfo writeMir(final Model model, final List<IsaPrimitive> insnList) {
    final Path path = Paths.get(SysUtils.getHomeDir(), "gen", model.getName() + ".zip");
    final MirArchive archive = MirArchive.open(path);
    final MirPassDriver driver =
      MirPassDriver.newDefault().setStorage(archive.loadAll());

    final BodyInfo info = new BodyInfo(insnList, archive);

    int index = 0;
    for (final IsaPrimitive insn : insnList) {
      final String name = String.format("insn_%d.action", index++);
      final MirContext mir =
        FormulaBuilder.buildMir(name, model, archive, Collections.singletonList(insn));
      final MirContext opt = driver.apply(mir);
      info.bodyMir.add(opt);
      info.storage.put(opt.name, opt);
    }

    final String pcName =
      archive.getManifest().getJsonObject("program_counter").getString("name");
    final List<Pass> ssaSeq = MirPassDriver.ssaOptimizeSequence();
    ForwardPass.class.cast(ssaSeq.get(1)).initValues(
        Collections.singletonMap(pcName, BigInteger.ZERO));
    final StoreAnalysis analysis = new StoreAnalysis();
    final MirPassDriver ssaDriver = new MirPassDriver(ssaSeq);
    for (final MirContext mir : info.bodyMir) {
      final MirContext opt = ssaDriver.apply(mir);
      analysis.apply(opt);

      info.offsets.add(analysis.getOutputValues(pcName));
      info.branchCond.add(analysis.getCondition(pcName));
    }

    return info;
  }

  static class BodyInfo {
    final List<IsaPrimitive> body;
    final MirArchive archive;
    final Map<String, MirContext> storage;
    final List<MirContext> bodyMir = new java.util.ArrayList<>();
    final List<Operand> branchCond = new java.util.ArrayList<>();
    final List<Collection<Operand>> offsets = new java.util.ArrayList<>();

    final List<Range> bbRange = new java.util.ArrayList<>();
    final List<MirContext> bbMir = new java.util.ArrayList<>();
    final List<Operand> bbCond = new java.util.ArrayList<>();
    final List<Map<String, Static>> bbModified = new java.util.ArrayList<>();
    final List<Map<String, Static>> bbIndexed = new java.util.ArrayList<>();
    final List<Map<String, Pair<Static, Static>>> bbInOut = new java.util.ArrayList<>();
    final Map<String, Integer> modBase = new java.util.HashMap<>();

    public BodyInfo(final List<IsaPrimitive> body, final MirArchive archive) {
      this.body = body;
      this.archive = archive;
      this.storage = new java.util.HashMap<>(archive.loadAll());
    }
  }

  private static void writeBasicBlockSmt(final String fileName, final BodyInfo info) {
    final Mir2Node smtOutput = new Mir2Node();
    for (final MirContext mir : info.bbMir) {
      smtOutput.apply(mir);

      final String path = String.format("%s.%s.smt2", fileName, mir.name);
      writeSmt(path, smtOutput.getFormulae());
    }
  }

  private static void writeSmt(
      final String fileName,
      final Collection<? extends Node> formulas) {
    final Cvc4Solver solver = new Cvc4Solver();

    SmtTextBuilder.saveToFile(fileName, Collections.emptyList(), formulas, solver.getOperations());
  }

  private static void inspectControlFlow(final Model model, final BodyInfo info) {
    final ControlFlowInspector inspector = new ControlFlowInspector(model, info);
    final List<Range> ranges = inspector.inspect();
    info.bbRange.addAll(ranges);
  }

  private static void writeControlFlow(final String fileName, final BodyInfo info) {
    final JsonBuilderFactory factory =
        Json.createBuilderFactory(Collections.<String, Object>emptyMap());

    final JsonArrayBuilder blocks = factory.createArrayBuilder();

    int bbIndex = 0;
    final List<Range> ranges = info.bbRange;
    for (final Range range : ranges) {
      final JsonObjectBuilder builder = factory.createObjectBuilder()
        .add("range", factory.createArrayBuilder().add(range.start).add(range.end))
        .add("target_taken", getLinkIndex(range, range.nextTaken, ranges))
        .add("target_other", getLinkIndex(range, range.nextOther, ranges));
      if (range.isEmpty()) {
        builder.add("link_addr", range.addrTaken);
      }

      writeSmtBinding(bbIndex, info, builder, factory);
      blocks.add(builder);
      ++bbIndex;
    }
    final JsonObjectBuilder jsonDoc = factory.createObjectBuilder()
      .add("blocks", blocks);

    final JsonWriterFactory writerFactory = Json.createWriterFactory(
        Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));
    try (final JsonWriter writer =
      writerFactory.createWriter(Files.newOutputStream(Paths.get(fileName)))) {
      writer.writeObject(jsonDoc.build());
    } catch (final java.io.IOException e) {
      Logger.error(e.getMessage());
    }
  }

  private static int getLinkIndex(
      final Range src, final int target, final List<Range> ranges) {
    final List<Range> candidates = selectRanges(target, ranges);

    if (candidates.size() == 1) {
      return ranges.indexOf(candidates.get(0));
    } else if (candidates.size() > 1) {
      for (final Range dst : candidates) {
        if (src.isEmpty() != dst.isEmpty()) {
          return ranges.indexOf(dst);
        }
      }
      throw new IllegalStateException();
    } else {
      return -1;
    }
  }

  private static List<Range> selectRanges(final int start, final List<Range> ranges) {
    int from = -1, to = -1;
    for (int i = 0; i < ranges.size(); ++i) {
      if (ranges.get(i).start == start) {
        if (from < 0) {
          from = i;
        }
        to = i;
      }
    }
    if (from >= 0) {
      return ranges.subList(from, to + 1);
    }
    return Collections.emptyList();
  }

  private static void writeSmtBinding(
      final int index,
      final BodyInfo info,
      final JsonObjectBuilder parent,
      final JsonBuilderFactory factory) {
    final String cond;
    final Range range = info.bbRange.get(index);
    if (range.nextTaken == range.nextOther) {
      cond = "true";
    } else {
      cond = String.format("%%%d", evalGuardId(index, info));
    }
    parent.add("condition_smt", cond);
    parent.add("hwstate_mod_smt", newHwstateMapping(index, info, factory));
  }

  private static int evalGuardId(final int index, final BodyInfo info) {
    final Operand guard = info.bbCond.get(index);
    // TODO enfoce guards to be Local instance
    int version = Integer.valueOf(guard.toString().substring(1));
    for (int i = 0; i < index; ++i) {
      version += info.bbMir.get(i).locals.size() - 1;
    }
    return version;
  }

  private static JsonArrayBuilder newHwstateMapping(
      final int index, final BodyInfo info, final JsonBuilderFactory factory) {
    final JsonArray hwstate =
        info.archive.getManifest().getJsonArray("hwstate");
    final Map<String, Static> modified = info.bbModified.get(index);
    final Map<String, Static> lastVersioned = info.bbIndexed.get(index);

    final Map<String, Pair<Static, Static>> inout = new java.util.TreeMap<>();
    info.bbInOut.add(inout); // FIXME

    final JsonArrayBuilder mapping = factory.createArrayBuilder();

    for (int i = 0; i < hwstate.size(); ++i) {
      final JsonObject state = hwstate.getJsonObject(i);
      final String name = state.getString("name");
      final Static mem = lastVersioned.get(name);
      if (mem != null) {
        final Integer base = info.modBase.get(mem.name);
        final Static modmem = modified.get(mem.name);

        final int inputVer = (base != null) ? base : 1;
        final int outputVer =
            (modmem != null) ? inputVer + modmem.version - 1 : inputVer;

        info.modBase.put(mem.name, inputVer + mem.version);

        final Static input = mem.newVersion(inputVer);
        final Static output = mem.newVersion(outputVer);
        inout.put(name, new Pair<>(input, output));

        mapping.add(factory.createObjectBuilder()
          .add("asm", state)
          .add("smt_in", input.toString())
          .add("smt_out", output.toString())
          .add("smt_type", Mir2Node.stringOf(mem.getType())));
      }
    }
    return mapping;
  }

  private static final class DisassemblerOutput implements Disassembler.Output {
    private final TemporaryVariables tempVars;
    private final List<IsaPrimitive> instructions;

    private DisassemblerOutput(final TemporaryVariables tempVars) {
      InvariantChecks.checkNotNull(tempVars);

      this.tempVars = tempVars;
      this.instructions = new ArrayList<>();
    }

    @Override
    public void add(final IsaPrimitive primitive) {
      final String text = primitive.text(tempVars);
      Logger.debug(text);
      instructions.add(primitive);
    }

    @Override
    public void close() {
      // Nothing
    }

    public List<IsaPrimitive> getInstructions() {
      return instructions;
    }
  }

  private static final class DisassemblerOutputFactory implements Disassembler.OutputFactory {
    private DisassemblerOutput output = null;
    private Model model = null;

    @Override
    public Output createOutput(final Model model) {
      InvariantChecks.checkNotNull(model);
      final TemporaryVariables tempVars = model.getTempVars();

      this.output = new DisassemblerOutput(tempVars);
      this.model = model;

      return output;
    }

    public DisassemblerOutput getOutput() {
      return output;
    }

    public Model getModel() {
      return model;
    }
  }
}
