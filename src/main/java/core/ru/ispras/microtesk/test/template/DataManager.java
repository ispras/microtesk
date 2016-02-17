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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
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
  private final List<DataDirective> globalData;

  private AddressTranslator addressTranslator;
  private MemoryAllocator allocator;

  private DataDirectiveFactory factory;
  private int dataFileIndex;

  private DataDirectiveFactory.Builder factoryBuilder;
  private DataBuilder dataBuilder;

  public DataManager(final Printer printer) {
    this.printer = printer;
    this.memoryMap = new MemoryMap();
    this.globalData = new ArrayList<>();

    this.addressTranslator = null;
    this.allocator = null;

    this.factory = null;
    this.dataFileIndex = 0;

    this.factoryBuilder = null;
    this.dataBuilder = null;
  }

  public DataDirectiveFactory.Builder beginConfig(
      final String text,
      final String target,
      final int addressableSize,
      final BigInteger baseVirtualAddress) {
    InvariantChecks.checkNotNull(text);
    InvariantChecks.checkNotNull(target);
    InvariantChecks.checkGreaterThanZero(addressableSize);

    checkReinitialized();

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
    checkReinitialized();

    InvariantChecks.checkNotNull(factoryBuilder);
    factory = factoryBuilder.build();
  }

  public DataBuilder beginData(final boolean isGlobal, final boolean isSeparateFile) {
    checkInitialized();
    InvariantChecks.checkTrue(null == dataBuilder);

    Memory.setUseTempCopies(false);
    dataBuilder = new DataBuilder(factory, isGlobal, isSeparateFile);

    return dataBuilder; 
  }

  public void endData() {
    checkInitialized();
    processData(dataBuilder);
  }

  private void processData(final DataBuilder dataBuilder) {
    InvariantChecks.checkNotNull(dataBuilder);

    final List<DataDirective> data = dataBuilder.build();
    if (dataBuilder.isSeparateFile()) {
      saveToFile(data);
    } else {
      globalData.addAll(data);
    }
  }

  public boolean isInitialized() {
    return factory != null;
  }

  public boolean containsDecls() {
    return !globalData.isEmpty();
  }

  public MemoryMap getMemoryMap() {
    return memoryMap;
  }

  public String getDeclText() {
    final StringBuilder sb = new StringBuilder();
    sb.append(factory.getHeader().getText());

    for (final DataDirective item : globalData) {
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

  public int newRandom(final int min, final int max) {
    return Randomizer.get().nextIntRange(min, max);
  }

  public void generateData(
      final BigInteger address,
      final String label,
      final String typeId,
      final int length,
      final String method,
      final boolean isSeparateFile) {
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(label);
    InvariantChecks.checkNotNull(typeId);
    InvariantChecks.checkGreaterThanZero(length);
    InvariantChecks.checkNotNull(method);

    checkInitialized();
    Memory.setUseTempCopies(false);

    if (memoryMap.isDefined(label)) {
      throw new IllegalStateException(String.format("Label %s is redefined", label));
    }

    final DataBuilder dataBuilder = new DataBuilder(factory, true, isSeparateFile);

    final DataDirectiveFactory.TypeInfo typeInfo = factory.findTypeInfo(typeId);
    final DataGenerator dataGenerator = DataGenerator.newInstance(method, typeInfo.type);

    final BigInteger oldAddress = allocator.getCurrentAddress();
    try {
      allocator.setCurrentAddress(address);

      final BitVector bvAddress =
          BitVector.valueOf(address, allocator.getAddressBitSize());

      memoryMap.addLabel(label, address);

      dataBuilder.addLabel(label);
      dataBuilder.addComment(String.format(" Address: 0x%s", bvAddress.toHexString()));

      for (int index = 0; index < length; index += 4) {
        final int count = Math.min(length - index, 4);
        dataBuilder.addGeneratedData(typeInfo, dataGenerator, count);
      }

      processData(dataBuilder);
    } finally {
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
    InvariantChecks.checkNotNull(startAddress);
    InvariantChecks.checkNotNull(addresses);
    InvariantChecks.checkNotNull(method);

    checkInitialized();
    Memory.setUseTempCopies(false);

    final DataBuilder dataBuilder = new DataBuilder(factory, true, isSeparateFile);

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
      dataBuilder.addComment(String.format(" Address: 0x%x", startAddress));

      BigInteger nextAddress = BigInteger.ZERO.not();
      for (final BigInteger address : sortedAddresses) {
        InvariantChecks.checkTrue(address.compareTo(startAddress) >= 0);

        if (address.compareTo(nextAddress) != 0) {
          allocator.setCurrentAddress(address);

          final BigInteger printedAddress =
              printAbsoluteOrg ? address : address.subtract(startAddress);

          dataBuilder.addText("");
          dataBuilder.addText(String.format(TestSettings.getOriginFormat(), printedAddress));
        }

        for (int i = 0; i < unitsInRow; i += 4) {
          final int count = Math.min(unitsInRow - i, 4);
          dataBuilder.addGeneratedData(typeInfo, dataGenerator, count);
        }

        nextAddress = allocator.getCurrentAddress();
      }

      processData(dataBuilder);
    } finally {
      allocator.setCurrentAddress(oldAddress);
    }
  }

  private void saveToFile(final List<DataDirective> data) {
    final String fileName = String.format("%s_%04d.%s",
        TestSettings.getDataFilePrefix(), dataFileIndex, TestSettings.getDataFileExtension());

    Logger.debug("Generating data file: %s", fileName);

    PrintWriter writer = null;
    try {
      try {
        writer = printer.newFileWriter(fileName);
        for (final DataDirective item : data) {
          if (item.needsIndent()) {
            writer.println(TestSettings.getIndentToken() + item.getText());
          } else {
            writer.println(item.getText());
          }
        }
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

  private void checkInitialized() {
    if (!isInitialized()) {
      throw new IllegalStateException("DataManager is not initialized!");
    }
  }

  private void checkReinitialized() {
    if (isInitialized()) {
      throw new IllegalStateException("DataManager is already initialized!");
    }
  }
}
