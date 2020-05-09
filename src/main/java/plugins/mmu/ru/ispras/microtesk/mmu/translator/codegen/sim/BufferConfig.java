package ru.ispras.microtesk.mmu.translator.codegen.sim;

import ru.ispras.castle.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

final class BufferConfig {
  final Map<String, List<BitSet>> instanceMap;
  final int nunits;

  BufferConfig(int nunits, Map<String, List<BitSet>> instanceMap) {
    this.instanceMap = instanceMap;
    this.nunits = nunits;
  }

  public static BufferConfig emptyConfig() {
    return new BufferConfig(1, Collections.emptyMap());
  }

  public static BufferConfig fromPath(final Path path) {
    final List<String> lines;
    try {
      lines = Files.readAllLines(path);
    } catch (IOException e) {
      return emptyConfig();
    }
    int nunits = 1;
    final Map<String, List<BitSet>> instanceMap = new java.util.TreeMap<>();

    for (int lineNo = 0; lineNo < lines.size(); ++lineNo) {
      final var line = lines.get(lineNo).replaceAll("#.*", "");
      final var args = Arrays.asList(line.split(",")).stream()
        .filter(Predicate.not(String::isBlank))
        .map(String::strip)
        .collect(Collectors.toList());
      if (args.size() > 1) {
        parseBufferInstance(path, lineNo, args, instanceMap);
      } else if (args.size() == 1) try {
        nunits = Integer.parseUnsignedInt(args.get(0));
      } catch (NumberFormatException e) {
        Logger.warning("%s:%d: incorrect core number setting format: '%s'",
          path, lineNo, args.get(0));
      }
    }
    for (final var key : instanceMap.keySet()) {
      instanceMap.put(key, immutableList(instanceMap.get(key)));
    }
    return new BufferConfig(nunits, immutableMap(instanceMap));
  }

  private static void parseBufferInstance(
      final Path path,
      final int lineNo,
      List<String> args,
      Map<String, List<BitSet>> instanceMap) {
    final var type = args.get(0);
    final var pins = new BitSet();
    for (int i = 1; i < args.size(); ++i) try {
      final var arg = args.get(i);
      pins.set(Integer.parseUnsignedInt(arg));
    } catch (NumberFormatException e) {
      Logger.warning("%s:%d: incorrect pinned core id setting format: '%s'",
        path, lineNo, args.get(i));
    }
    if (!pins.isEmpty()) {
      instanceMap.computeIfAbsent(type, x -> new java.util.ArrayList<>()).add(pins);
    } else {
      Logger.warning("%s:%d: no pinned core ids found for '%s', discard entry",
        path, lineNo, type);
    }
  }

  private static <T> List<T> immutableList(final List<T> list) {
    switch (list.size()) {
    case 0: return Collections.emptyList();
    case 1: return Collections.singletonList(list.get(0));
    default: return Collections.unmodifiableList(list);
    }
  }

  private static <K, V> Map<K, V> immutableMap(final Map<K, V> map) {
    switch (map.size()) {
    case 0: return Collections.emptyMap();
    case 1:
      final var key = map.keySet().iterator().next();
      return Collections.singletonMap(key, map.get(key));

    default: return Collections.unmodifiableMap(map);
    }
  }
}
