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
import static ru.ispras.fortress.util.InvariantChecks.checkTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
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
import ru.ispras.microtesk.test.TestSettings;

public final class DataManager {
  private static interface DataDeclItem {
    String getText();
  }

  private static class DataDeclText implements DataDeclItem {
    private final String text;

    DataDeclText(final String text) {
      this.text = text;
    }

    @Override
    public String getText() {
      return text;
    }
  }

  private static final class DataDeclLabel extends DataDeclText {
    DataDeclLabel(final String text) {
      super(text);
    }

    @Override
    public String getText() {
      return getName() + ":";
    }

    public String getName() {
      return super.text;
    }
  }

  private static final class DataDeclComment extends DataDeclText {
    DataDeclComment(final String text) {
      super(text);
    }
  }

  private static final class DataDeclSpace extends DataDeclText {
    private final int count;

    DataDeclSpace(final String text, final int count) {
      super(text);
      this.count = count;
    }

    @Override
    public String getText() {
      return String.format("%s %d", super.getText(), count);
    }
  }
  
  private static final class DataDeclStrings extends DataDeclText {
    final String[] strings;

    DataDeclStrings(final String text, final String[] strings) {
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

  private static final class DataDecl extends DataDeclText {
    private final List<BitVector> values;

    DataDecl(final String text, final List<BitVector> values) {
      super(text);
      this.values = values;
    }

    @Override
    public String getText() {
      final StringBuilder sb = new StringBuilder();
      sb.append(super.getText());

      boolean isFirst = true;
      for (final BitVector value : values) {
        if (isFirst) { 
          isFirst = false;
        } else {
          sb.append(",");
        }

        sb.append(" 0x");
        sb.append(value.toHexString());
      }

      return sb.toString();
    }
  }

  private final Printer printer;
  private final MemoryMap memoryMap;
  private final List<DataDeclItem> globalDataDecls;
  private final Deque<List<DataDeclItem>> dataDeclsStack;

  private String sectionText;
  private MemoryAllocator allocator;
  private List<String> labels;

  private String spaceText;
  private BitVector spaceData;
  private String ztermStrText;
  private String nztermStrText;

  private int dataFileIndex;

  private final Map<String, TypeInfo> typeMap;
  final static class TypeInfo {
    final Type type;
    final String text;

    public TypeInfo(final Type type, final String text) {
      this.type = type;
      this.text = text;
    }
  }

  public DataManager(final Printer printer) {
    this.printer = printer;
    this.dataFileIndex = 0;

    this.memoryMap = new MemoryMap();

    this.globalDataDecls = new ArrayList<>();
    this.dataDeclsStack = new ArrayDeque<>();
    this.dataDeclsStack.push(this.globalDataDecls);

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

  /**
   * Returns a collection of data declaration in the current scope.
   * There can be two scopes: (1) global that holds declarations
   * to be placed to all generated source code files, or
   * (2) local that contains declarations to be placed in a separate
   * data file.
   * 
   * @return List of data declaration used in the current scope.
   */

  private List<DataDeclItem> getDataDecls() {
    return dataDeclsStack.peek();
  }

  public void pushScope() {
    dataDeclsStack.push(new ArrayList<DataDeclItem>());
  }

  public void popScope() {
    if (dataDeclsStack.size() <= 1) {
      throw new IllegalStateException();
    }
    dataDeclsStack.pop();
  }

  public boolean containsDecls() {
    return !getDataDecls().isEmpty();
  }

  public MemoryMap getMemoryMap() {
    return memoryMap;
  }

  public String getDeclText() {
    if (!isInitialized()) {
      return null;
    }

    return getDeclText(false);
  }

  private String getDeclText(final boolean isSeparateFile) {
    final StringBuilder sb = new StringBuilder();

    if (!isSeparateFile) {
      sb.append(sectionText);
    }

    for (final DataDeclItem item : getDataDecls()) {
      if (item instanceof DataDeclLabel) {
        if (isSeparateFile) {
          sb.append(String.format("%n.globl %s", ((DataDeclLabel) item).getName()));
        }
        sb.append(String.format("%n%s", item.getText()));
      } else if (item instanceof DataDeclComment) {
        sb.append(String.format("%n%s%s%s",
            TestSettings.getIndentToken(), TestSettings.getCommentToken(), item.getText()));
      } else {
        sb.append(String.format("%n%s%s",
            TestSettings.getIndentToken(), item.getText()));
      }
    }

    return sb.toString();
  }

  public void defineType(
      final String id,
      final String text,
      final String typeName,
      final int[] typeArgs) {
    checkNotNull(id);
    checkNotNull(text);
    checkNotNull(typeName);
    checkNotNull(typeArgs);

    checkInitialized();

    final Type type = Type.typeOf(typeName, typeArgs);
    Logger.debug("Defining %s as %s ('%s')...", type, id, text);

    typeMap.put(id, new TypeInfo(type, text));
  }

  public void defineSpace(
      final String id,
      final String text,
      final BigInteger fillWith) {
    checkNotNull(id);
    checkNotNull(text);
    checkNotNull(fillWith);

    checkInitialized();
    Logger.debug("Defining space as %s ('%s') filled with %x...", id, text, fillWith);

    spaceText = text;
    spaceData = BitVector.valueOf(fillWith, allocator.getAddressableUnitBitSize());
  }

  public void defineAsciiString(
      final String id,
      final String text,
      final boolean zeroTerm) {
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

    final String text = String.format(TestSettings.getOriginFormat(), value);
    Logger.debug("Setting allocation address: %s", text);

    getDataDecls().add(new DataDeclText(text));
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

    final String text = String.format(TestSettings.getAlignFormat(), value);
    Logger.debug("Setting alignment: %s (%d bytes)", text, valueInBytes);

    getDataDecls().add(new DataDeclText(text));
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
    getDataDecls().add(new DataDeclLabel(id));
  }

  public void addText(final String text) {
    checkNotNull(text);
    checkInitialized();
    getDataDecls().add(new DataDeclText(text));
  }

  public void addComment(final String text) {
    checkNotNull(text);
    checkInitialized();
    getDataDecls().add(new DataDeclComment(text));
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

  public void addData(final String id, final BigInteger[] values) {
    checkNotNull(id);
    checkNotNull(values);
    checkGreaterThanZero(values.length);

    checkInitialized();

    final TypeInfo typeInfo = typeMap.get(id);
    if (null == typeInfo) {
      throw new IllegalStateException(String.format(
          "The %s type is not defined.", id));
    }

    final List<BitVector> dataList = new ArrayList<>(values.length);
    boolean isFirst = true;

    for (final BigInteger value : values) {
      final BitVector data = BitVector.valueOf(value, typeInfo.type.getBitSize());
      dataList.add(data);

      final BigInteger address = allocator.allocate(data);
      if (isFirst) {
        setAllLabelsToAddress(address);
        isFirst = false;
      }
    }

    getDataDecls().add(new DataDecl(typeInfo.text, dataList));
  }

  public void addSpace(final int length) {
    checkGreaterThanZero(length);
    checkInitialized();

    if (null == spaceData) {
      throw new IllegalStateException();
    }

    final BigInteger address = allocator.allocate(spaceData, length);

    setAllLabelsToAddress(address);
    getDataDecls().add(new DataDeclSpace(spaceText, length));
  }

  public void addAsciiStrings(final boolean zeroTerm, final String[] strings) {
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
    getDataDecls().add(new DataDeclStrings(
        (zeroTerm ? ztermStrText : nztermStrText), strings));
  }

  public int newRandom(final int min, final int max) {
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
      final String method,
      final boolean isSeparateFile) {
    checkNotNull(address);
    checkNotNull(label);
    checkNotNull(typeId);
    checkGreaterThanZero(length);
    checkNotNull(method);

    checkInitialized();
    Memory.setUseTempCopies(false);

    if (memoryMap.isDefined(label)) {
      throw new IllegalStateException(String.format("Label %s is redefined", label));
    }

    final TypeInfo typeInfo = typeMap.get(typeId);
    if (null == typeInfo) {
      throw new IllegalStateException(String.format(
          "The %s type is not defined.", typeId));
    }

    final DataGenerator dataGenerator = newDataGenerator(method, typeInfo);

    final MemoryAllocator oldAllocator = allocator;
    try {
      allocator = oldAllocator.newAllocator(address);

      final BitVector bvAddress =
          BitVector.valueOf(address, allocator.getAddressBitSize());

      if (isSeparateFile) {
        pushScope();
      }

      memoryMap.addLabel(label, address);
      addLabel(label);
      addComment(String.format(" Address: 0x%s", bvAddress.toHexString()));

      List<BitVector> dataList = new ArrayList<>(4);
      for (int index = 0; index < length; index++) {
        final BitVector data = dataGenerator.nextData();

        allocator.allocate(data);
        dataList.add(data);

        final boolean isNewDecl =
            (index == length - 1) || (index + 1) % 4 == 0;

        if (isNewDecl) {
          getDataDecls().add(new DataDecl(typeInfo.text, dataList));
          dataList = new ArrayList<>(4);
        }
      }

      if (isSeparateFile) {
        saveDeclsToFile();
      }
    } finally {
      if (isSeparateFile) {
        popScope();
      }
      allocator = oldAllocator;
    }
  }

  public void generateAllData(
      final BigInteger startAddress,
      final Collection<BigInteger> addresses,
      final BigInteger addressMask,
      final boolean printAbsoluteOrg,
      final String method,
      final boolean isSeparateFile) {
    checkNotNull(startAddress);
    checkNotNull(addresses);
    checkNotNull(method);

    checkInitialized();
    Memory.setUseTempCopies(false);

    final List<BigInteger> sortedAddresses = new ArrayList<>(addresses);
    Collections.sort(sortedAddresses);

    final int blockSize = addressMask.not().intValue() + 1;
    final int unitSize = blockSize > 8 ? 8 : blockSize;
    final int unitsInRow = blockSize / unitSize;

    final TypeInfo typeInfo = getTypeInfoForSize(unitSize);
    final DataGenerator dataGenerator = newDataGenerator(method, typeInfo);

    final MemoryAllocator oldAllocator = allocator;
    try {
      allocator = oldAllocator.newAllocator(startAddress);

      if (isSeparateFile) {
        pushScope();
      }

      addComment(String.format(" Address: 0x%x", startAddress));

      BigInteger nextAddress = BigInteger.ZERO.not();
      for (final BigInteger address : sortedAddresses) {
        checkTrue(address.compareTo(startAddress) >= 0);

        if (address.compareTo(nextAddress) != 0) {
          allocator.setCurrentAddress(address);

          final BigInteger printedAddress =
              printAbsoluteOrg ? address : address.subtract(startAddress);

          addText("");
          addText(String.format(TestSettings.getOriginFormat(), printedAddress));
        }

        List<BitVector> dataList = new ArrayList<>(4);
        for (int i = 0; i < unitsInRow; i++) {
          final BitVector data = dataGenerator.nextData();

          allocator.allocate(data);
          dataList.add(data);

          final boolean isNewDecl =
              (i == unitsInRow - 1) || (i + 1) % 4 == 0;

          if (isNewDecl) {
            getDataDecls().add(new DataDecl(typeInfo.text, dataList));
            dataList = new ArrayList<>(4);
          }
        }

        nextAddress = allocator.getCurrentAddress();
      }

      if (isSeparateFile) {
        saveDeclsToFile();
      }
    } finally {
      if (isSeparateFile) {
        popScope();
      }
      allocator = oldAllocator;
    }
  }

  private TypeInfo getTypeInfoForSize(final int typeSizeInBytes) {
    final int bitSize = typeSizeInBytes * 8;
    for (final TypeInfo typeInfo : typeMap.values()) {
      if (bitSize == typeInfo.type.getBitSize()) {
        return typeInfo;
      }
    }

    throw new IllegalArgumentException(String.format(
        "No %d-byte type is defined.", typeSizeInBytes));
  }

  public void saveDeclsToFile() {
    final String fileName = String.format("%s_%04d.%s",
        TestSettings.getDataFilePrefix(), dataFileIndex, TestSettings.getDataFileExtension());

    Logger.debug("Generating data file: %s", fileName);

    PrintWriter writer = null;
    try {
      try {
        writer = printer.newFileWriter(fileName);
        writer.println(getDeclText(true));
      } finally {
        writer.close();
      }
      ++dataFileIndex;
    } catch (final IOException e) {
      new File(fileName).delete();
      throw new GenerationAbortedException(String.format(
          "Failed to generate data file %s. Reason: %s", e.getMessage()));
    }
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
