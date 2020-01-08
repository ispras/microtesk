package ru.ispras.microtesk.translator.mir;


import ru.ispras.castle.util.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static ru.ispras.microtesk.translator.mir.Instruction.Branch.Branch;

public class GraphvizDrawer extends Pass {

    public GraphvizDrawer(String fileName) {
        this.outputFile = fileName + ".dot";
    }

    @Override
    public MirContext apply(MirContext ctx) {
        Map<BasicBlock, List<BasicBlock>> successors = new HashMap<>();
        for (BasicBlock bb: ctx.blocks) {
            Instruction lastInsn = bb.insns.get(bb.insns.size() - 1);
            if (lastInsn instanceof Branch) {
                successors.put(bb, ((Branch) lastInsn).successors);
            }
        }
        String graph = drawGraph(successors);
        outputDotFile(graph);
        return ctx;
    }

    private String drawGraph(Map<BasicBlock, List<BasicBlock>> successors) {
        String newline = System.getProperty("line.separator");
        GraphMarker marker = new GraphMarker();
        StringBuilder output = new StringBuilder();

        output.append("digraph {").append(newline);

        for (Map.Entry<BasicBlock, List<BasicBlock>> e : successors.entrySet()) {
            String outNode = marker.getMark(e.getKey());
            for (BasicBlock inBlock : e.getValue()) {
                output
                        .append(outNode)
                        .append(" -> ")
                        .append(marker.getMark(inBlock))
                        .append(newline);
            }
        }
        output.append('}');
        return output.toString();
    }

    private void outputDotFile(String graph) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile), UTF_8)) {
            writer.write(graph);
        } catch (IOException e) {
            Logger.error("Couldn't open file for graphviz " + e.getMessage());
        }
    }

    static class GraphMarker {
        String getMark(BasicBlock b) {
            String mark = marks.get(b);
            if (mark == null) {
                lastMark++;
                mark = "Block " + lastMark.toString();
                marks.put(b, mark);
            }
            return mark;
        }

        private Map<BasicBlock, String> marks = new HashMap<>();
        private Integer lastMark = 0;
    }
    private String outputFile;
}
