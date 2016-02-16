/*
 * Copyright 2014-2016 ISP RAS (http://www.ispras.ru)
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
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.memory.AddressTranslator;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.memory.MemoryAllocator;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.Printer;
import ru.ispras.microtesk.test.TestSettings;

public final class DataManager {
  private final Printer printer;
  private final MemoryMap memoryMap;
  private final List<DataDirective> globalDataDecls;
  private final Deque<List<DataDirective>> dataDeclsStack;

  private AddressTranslator addressTranslator;
  private MemoryAllocator allocator;

  private DataDirectiveFactory factory;
  private DataDirectiveFactory.Builder factoryBuilder;

  private boolean isSeparateFile;
  private int dataFileIndex;

  public DataManager(final Printer printer) {
    this.printer = printer;
    this.memoryMap = new MemoryMap();

    this.globalDataDecls = new ArrayList<>();
    this.dataDeclsStack = new ArrayDeque<>();
    this.dataDeclsStack.push(this.globalDataDecls);

    this.factory = null;
    this.factoryBuilder = null;

    this.dataFileIndex = 0;
    this.isSeparateFile = false;
  }

  public void setSeparateFile(final boolean isSeparateFile) {
    this.isSeparateFile = isSeparateFile;
  }

  public DataDirectiveFactory.Builder beginConfig(
      final String text,
      final String target,
      final int addressableSize,
      final BigInteger baseVirtualAddress) {
    checkNotNull(text);
    checkNotNull(target);

    if (isInitialized()) {
      throw new IllegalStateException("DataManager is already initialized!");
    }

    final Memory memory = Memory.get(target);

    addressTranslator = new AddressTranslator(
        TestSettings.getBaseVirtualAddress(), TestSettings.getBasePhysicalAddress());

    final BigInteger basePhysicalAddressForAllocation =
        null != baseVirtualAddress ?
        addressTranslator.virtualToPhysical(baseVirtualAddress) :
        TestSettings.getBasePhysicalAddress();

    allocator = memory.newAllocator(
        addressableSize, basePhysicalAddressForAllocation);

    factoryBuilder = new DataDirectiveFactory.Builder(
        memoryMap, allocator, addressTranslator, text);

    return factoryBuilder;
  }

  public void endConfig() {
    if (isInitialized()) {
      throw new IllegalStateException("DataManager is already initialized!");
    }
    factory = factoryBuilder.build();
  }

  public boolean isInitialized() {
    return factory != null;
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
  private List<DataDirective> getDataDecls() {
    return dataDeclsStack.peek();
  }

  private void registerDirective(final DataDirective directive) {
    directive.apply();
    getDataDecls().add(directive);
  }

  public void pushScope() {
    dataDeclsStack.push(new ArrayList<DataDirective>());
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
      sb.append(factory.getHeader().getText());
    }

    for (final DataDirective item : getDataDecls()) {
      if (item.needsIndent()) {
        sb.append(String.format("%n%s%s", TestSettings.getIndentToken(), item.getText()));
      } else {
        sb.append(String.format("%n%s", item.getText()));
      }
    }

    return sb.toString();
  }

  public BigInteger getAddress() {
    checkInitialized();
    final BigInteger physicalAddress = allocator.getCurrentAddress();
    final BigInteger virtualAddress = addressTranslator.virtualToPhysical(physicalAddress);
    return virtualAddress;
  }

  /**
   * Sets allocation origin. Inserts the ".org" directive in the test program.
   */
  public void setOrigin(final BigInteger origin) {
    checkInitialized();
    registerDirective(factory.newOrigin(origin));
  }

  /**
   * @param value Alignment amount in addressable units.
   */
  public void align(final BigInteger value, final BigInteger valueInBytes) {
    checkInitialized();
    registerDirective(factory.newAlign(value, valueInBytes));
  }

  public void addLabel(final String id) {
    checkInitialized();
    registerDirective(factory.newLabel(id, isSeparateFile));
  }

  public void addText(final String text) {
    checkInitialized();
    registerDirective(factory.newText(text));
  }

  public void addComment(final String text) {
    checkInitialized();
    registerDirective(factory.newComment(text));
  }

  public void addData(final String id, final BigInteger[] values) {
    checkInitialized();
    registerDirective(factory.newData(id, values));
  }

  public void addSpace(final int length) {
    checkInitialized();
    registerDirective(factory.newSpace(length));
  }

  public void addAsciiStrings(final boolean zeroTerm, final String[] strings) {
    checkInitialized();
    registerDirective(factory.newAsciiStrings(zeroTerm, strings));
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

    final DataDirectiveFactory.TypeInfo typeInfo = factory.findTypeInfo(typeId);
    final DataGenerator dataGenerator = DataGenerator.newInstance(method, typeInfo.type);

    final BigInteger oldAddress = allocator.getCurrentAddress();
    try {
      allocator.setCurrentAddress(address);

      final BitVector bvAddress =
          BitVector.valueOf(address, allocator.getAddressBitSize());

      if (isSeparateFile) {
        pushScope();
      }

      memoryMap.addLabel(label, address);

      registerDirective(factory.newLabel(label, isSeparateFile));
      registerDirective(factory.newComment(String.format(" Address: 0x%s", bvAddress.toHexString())));

      for (int index = 0; index < length; index += 4) {
        final int count = Math.min(length - index, 4);
        registerDirective(factory.newData(typeInfo, dataGenerator, count));
      }

      if (isSeparateFile) {
        saveDeclsToFile();
      }
    } finally {
      if (isSeparateFile) {
        popScope();
      }
      allocator.setCurrentAddress(oldAddress);
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

    final DataDirectiveFactory.TypeInfo typeInfo = factory.findTypeInfo(unitSize);
    final DataGenerator dataGenerator = DataGenerator.newInstance(method, typeInfo.type);

    final BigInteger oldAddress = allocator.getCurrentAddress();
    try {
      allocator.setCurrentAddress(startAddress);

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

        for (int i = 0; i < unitsInRow; i += 4) {
          final int count = Math.min(unitsInRow - i, 4);
          registerDirective(factory.newData(typeInfo, dataGenerator, count));
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
      allocator.setCurrentAddress(oldAddress);
    }
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
}
