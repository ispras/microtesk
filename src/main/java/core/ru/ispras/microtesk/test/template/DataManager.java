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

import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThanZero;
import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.memory.MemoryAllocator;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.Printer;

public final class DataManager {
  private static interface DataDeclItem {
    String getText();
  }

  private static class DetaDeclText implements DataDeclItem {
    private final String text;

    DetaDeclText(String text) {
      this.text = text;
    }

    @Override
    public String getText() {
      return text;
    }
  }

  private static final class DetaDeclLabel extends DetaDeclText {
    DetaDeclLabel(String text) {
      super(text);
    }

    @Override
    public String getText() {
      return super.text + ":";
    }
  }

  private static final class DetaDeclSpace extends DetaDeclText {
    private final int count;
    
    DetaDeclSpace(String text, int count) {
      super(text);
      this.count = count;
    }

    @Override
    public String getText() {
      return String.format("%s %d", super.getText(), count);
    }
  }
  
  private static final class DetaDeclStrings extends DetaDeclText {
    final String[] strings;

    DetaDeclStrings(String text, String[] strings) {
      super(text);
      this.strings = strings;
    }

    @Override
    public String getText() {
      final StringBuilder sb = new StringBuilder();
      sb.append(super.getText());

      for (int i = 0; i < strings.length; i++) {
        if (i > 0) {
          sb.append(",");
        }

        sb.append(String.format(" \"%s\"", strings[i]));
      }

      return sb.toString();
    }
  }

  private static final class DetaDecl extends DetaDeclText {
    final BigInteger[] values;

    DetaDecl(String text, BigInteger[] values) {
      super(text);
      this.values = values;
    }

    @Override
    public String getText() {
      final StringBuilder sb = new StringBuilder();
      sb.append(super.getText());

      for (int i = 0; i < values.length; i++) {
        if (i > 0) {
          sb.append(",");
        }

        sb.append(String.format(" 0x%x", values[i]));
      }

      return sb.toString();
    }
  }

  private final Printer printer;
  private final MemoryMap memoryMap;
  private final List<DataDeclItem> dataDecls;

  private String sectionText;
  private MemoryAllocator allocator;
  private List<String> labels;

  private String spaceText;
  private BitVector spaceData;
  private String ztermStrText;
  private String nztermStrText;

  private final String indentToken;
  private final String originFormat;
  private final String alignFormat;
  private final String dataFilePrefix;
  private final String dataFileExtension;
  private int dataFileIndex;

  private final Map<String, TypeInfo> typeMap;
  final static class TypeInfo {
    final Type type;
    final String text;

    public TypeInfo(Type type, String text) {
      this.type = type;
      this.text = text;
    }
  }

  public DataManager(
      final String indentToken,
      final String originFormat,
      final String alignFormat, 
      final Printer printer,
      final String dataFilePrefix,
      final String dataFileExtension) {

    this.indentToken = indentToken;
    this.originFormat = originFormat;
    this.alignFormat = alignFormat;

    this.printer = printer;
    this.dataFilePrefix = dataFilePrefix;
    this.dataFileExtension = dataFileExtension;
    this.dataFileIndex = 0;

    this.memoryMap = new MemoryMap();
    this.dataDecls = new ArrayList<>();

    this.sectionText = null;
    this.allocator = null;
    this.labels = null;

    this.spaceText = null;
    this.spaceData = null;
    this.ztermStrText = null;
    this.nztermStrText = null;

    this.typeMap = new HashMap<>(); 
  }

  public void init(final String text, final String target, final int addressableSize) {
    checkNotNull(text);
    checkNotNull(target);

    if (isInitialized()) {
      throw new IllegalStateException("DataManager is already initialized!");
    }

    sectionText = text;
    final Memory memory = Memory.get(target);
    allocator = memory.newAllocator(addressableSize);
  }

  public boolean isInitialized() {
    return allocator != null;
  }

  public boolean containsDecls() {
    return !dataDecls.isEmpty();
  }

  public MemoryMap getMemoryMap() {
    return memoryMap;
  }

  public String getDeclText() {
    if (!isInitialized()) {
      return null;
    }

    final StringBuilder sb = new StringBuilder(sectionText);
    for (DataDeclItem item : dataDecls) {
      if (item instanceof DetaDeclLabel) {
        sb.append(String.format("%n%s", item.getText()));
      } else {
        sb.append(String.format("%n%s%s", indentToken, item.getText()));
      }
    }

    return sb.toString();
  }

  public void defineType(String id, String text, String typeName, int[] typeArgs) {
    checkNotNull(id);
    checkNotNull(text);
    checkNotNull(typeName);
    checkNotNull(typeArgs);

    checkInitialized();

    final Type type = Type.typeOf(typeName, typeArgs);
    Logger.debug("Defining %s as %s ('%s')...", type, id, text);

    typeMap.put(id, new TypeInfo(type, text));
  }

  public void defineSpace(String id, String text, BigInteger fillWith) {
    checkNotNull(id);
    checkNotNull(text);
    checkNotNull(fillWith);

    checkInitialized();
    Logger.debug("Defining space as %s ('%s') filled with %x...", id, text, fillWith);

    spaceText = text;
    spaceData = BitVector.valueOf(fillWith, allocator.getAddressableUnitBitSize());
  }

  public void defineAsciiString(String id, String text, boolean zeroTerm) {
    checkNotNull(id);
    checkNotNull(text);
    checkInitialized();

    Logger.debug("Defining %snull-terminated ASCII string as %s ('%s')...", zeroTerm ? "" : "not ", id, text);

    if (zeroTerm) {
      ztermStrText = text;
    } else {
      nztermStrText = text;
    }
  }

  /**
   * Sets allocation address. Inserts the ".org" directive in the test program.
   */

  public void setAddress(final BigInteger value) {
    checkNotNull(value);

    final String text = String.format(originFormat, value);
    Logger.debug("Setting allocation address: %s", text);

    dataDecls.add(new DetaDeclText(text));
    allocator.setCurrentAddress(value);
  }

  public BigInteger getAddress() {
    return allocator.getCurrentAddress();
  }

  /**
   * @param value Alignment amount in addressable units.
   */

  public void align(final BigInteger value, final BigInteger valueInBytes) {
    checkNotNull(value);

    final String text = String.format(alignFormat, value);
    Logger.debug("Setting alignment: %s (%d bytes)", text, valueInBytes);

    dataDecls.add(new DetaDeclText(text));
    allocator.align(valueInBytes);
  }

  public void addLabel(final String id) {
    checkNotNull(id);
    checkInitialized();

    Logger.debug("Label %s", id);

    if (null == labels) {
      labels = new ArrayList<>();
    }

    labels.add(id);
    dataDecls.add(new DetaDeclLabel(id));
  }

  private void setAllLabelsToAddress(final BigInteger address) {
    checkInitialized();

    if (null != labels) {
      for (String label : labels) {
        memoryMap.addLabel(label, address);
      }
      labels = null;
    }
  }

  public void addData(String id, BigInteger[] values) {
    checkNotNull(id);
    checkNotNull(values);
    checkGreaterThanZero(values.length);

    checkInitialized();

    final TypeInfo typeInfo = typeMap.get(id);
    if (null == typeInfo) {
      throw new IllegalStateException(String.format(
          "The %s type is not defined.", id));
    }

    final BigInteger address = allocator.allocate(
        BitVector.valueOf(values[0], typeInfo.type.getBitSize()));

    for (int i = 1; i < values.length; i++) {
      allocator.allocate(
          BitVector.valueOf(values[i], typeInfo.type.getBitSize()));
    }

    setAllLabelsToAddress(address);
    dataDecls.add(new DetaDecl(typeInfo.text, values));
  }

  public void addSpace(int length) {
    checkGreaterThanZero(length);
    checkInitialized();

    if (null == spaceData) {
      throw new IllegalStateException();
    }

    final BigInteger address = allocator.allocate(spaceData, length);

    setAllLabelsToAddress(address);
    dataDecls.add(new DetaDeclSpace(spaceText, length));
  }

  public void addAsciiStrings(boolean zeroTerm, String[] strings) {
    checkNotNull(strings);
    checkGreaterThanZero(strings.length);
    checkInitialized();

    if (zeroTerm && (null == ztermStrText)) {
      throw new IllegalStateException();
    }

    if (!zeroTerm && (null == nztermStrText)) {
      throw new IllegalStateException();
    }

    final BigInteger address = allocator.allocateAsciiString(strings[0], zeroTerm);
    for (int index = 1; index < strings.length; index++) {
      allocator.allocateAsciiString(strings[index], zeroTerm);
    }

    setAllLabelsToAddress(address);
    dataDecls.add(new DetaDeclStrings((zeroTerm ? ztermStrText : nztermStrText), strings));
  }

  public int newRandom(int min, int max) {
    return Randomizer.get().nextIntRange(min, max);
  }

  private void checkInitialized() {
    if (!isInitialized()) {
      throw new IllegalStateException("DataManager is not initialized!");
    }
  }

  public void generateData(
      final BigInteger address,
      final String label,
      final String typeId,
      final int length,
      final String method) {
    checkNotNull(address);
    checkNotNull(label);
    checkNotNull(typeId);
    checkGreaterThanZero(length);
    checkNotNull(method);

    checkInitialized();
    Memory.setUseTempCopies(false);

    final String fileName = String.format(
        "%s_%04d.%s", dataFilePrefix, dataFileIndex, dataFileExtension);

    Logger.debug("Generating data file: %s", fileName);

    final TypeInfo typeInfo = typeMap.get(typeId);
    if (null == typeInfo) {
      throw new IllegalStateException(String.format(
          "The %s type is not defined.", typeId));
    }

    final MemoryAllocator localAllocator = new MemoryAllocator(
        allocator.getMemoryStorage(), allocator.getAddressableUnitBitSize(), address);

    if (memoryMap.isDefined(label)) {
      Logger.warning("Label %s is redefined", label);
    }

    memoryMap.addLabel(label, address);

    final BitVector bvAddress = BitVector.valueOf(address, localAllocator.getAddressBitSize());
    final String typeText = indentToken + typeInfo.text;

    final DataGenerator dataGenerator = newDataGenerator(method, typeInfo);
    PrintWriter writer = null;
    try {
      writer = printer.newFileWriter(fileName);
      writer.println();
      writer.println(".globl " + label);
      printer.printCommentToFile(writer, String.format("0x%s", bvAddress.toHexString()));
      writer.println(label + ":");

      try {
        for (int index = 0; index < length; index++) {
          final BitVector data = dataGenerator.nextData();
          localAllocator.allocate(data);

          final boolean nextLine = index % 4 == 0;
          if (nextLine && index > 0) {
            writer.println();
          }

          writer.print(nextLine ? typeText : ",");
          writer.print(" 0x" + data.toHexString());
        }
        writer.println();
      } finally {
        writer.close();
      }
    } catch (final Exception e) {
      new File(fileName).delete();
      throw new GenerationAbortedException(String.format(
          "Failed to generate data file %s. Reason: %s", e.getMessage()));
    }

    dataFileIndex++;
  }
  
  private static DataGenerator newDataGenerator(
      final String name, final TypeInfo typeInfo) {

    if ("zero".equalsIgnoreCase(name)) {
      return new DataGeneratorZero(typeInfo);
    }

    if ("random".equalsIgnoreCase(name)) {
      return new DataGeneratorRandom(typeInfo);
    }

    throw new IllegalArgumentException(
        "Unknown data generation method: " + name);
  }

  private interface DataGenerator {
    BitVector nextData();
  }

  private static final class DataGeneratorZero implements DataGenerator {
    private final BitVector data; 

    public DataGeneratorZero(final TypeInfo typeInfo) {
      this.data = BitVector.newEmpty(typeInfo.type.getBitSize());
    }

    @Override
    public BitVector nextData() {
      return data;
    }
  }

  private static final class DataGeneratorRandom implements DataGenerator {
    private final Type type; 

    public DataGeneratorRandom(final TypeInfo typeInfo) {
      this.type = typeInfo.type;
    }

    @Override
    public BitVector nextData() {
      final BitVector data = BitVector.newEmpty(type.getBitSize());
      Randomizer.get().fill(data);
      return data;
    }
  }
}
