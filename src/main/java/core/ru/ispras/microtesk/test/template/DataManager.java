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
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.memory.AddressTranslator;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.memory.MemoryAllocator;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.LabelManager;
import ru.ispras.microtesk.test.Printer;
import ru.ispras.microtesk.test.Statistics;
import ru.ispras.microtesk.test.TestSettings;

public final class DataManager {
  private final Printer printer;
  private final Statistics statistics;

  private final List<DataDirective> globalData;
  private final List<Pair<List<DataDirective>, Integer>> localData;
  private LabelManager labelManager;

  private BigInteger baseVirtualAddress;
  private MemoryAllocator allocator;
  private DataDirectiveFactory factory;
  private int dataFileIndex;
  private int testCaseIndex;

  private DataDirectiveFactory.Builder factoryBuilder;
  private DataSectionBuilder dataBuilder;

  public DataManager(final Printer printer, final Statistics statistics) {
    InvariantChecks.checkNotNull(printer);
    InvariantChecks.checkNotNull(statistics);

    this.printer = printer;
    this.statistics = statistics;

    this.globalData = new ArrayList<>();
    this.localData = new ArrayList<>();

    this.baseVirtualAddress = null;
    this.allocator = null;

    this.factory = null;
    this.dataFileIndex = 0;
    this.testCaseIndex = 0;

    this.factoryBuilder = null;
    this.dataBuilder = null;
  }

  public void setLabelManager(final LabelManager labelManager) {
    this.labelManager = labelManager;
  }

  public DataDirectiveFactory.Builder beginConfig(
      final String text,
      final String target,
      final int addressableUnitBitSize,
      final BigInteger baseVirtualAddress) {
    InvariantChecks.checkNotNull(text);
    InvariantChecks.checkNotNull(target);
    InvariantChecks.checkGreaterThanZero(addressableUnitBitSize);

    checkReinitialized();

    this.baseVirtualAddress = baseVirtualAddress != null ?
        baseVirtualAddress : TestSettings.getBaseVirtualAddress();

    final BigInteger basePhysicalAddressForAllocation =
        null != baseVirtualAddress ?
        AddressTranslator.get().virtualToPhysical(baseVirtualAddress) :
        TestSettings.getBasePhysicalAddress();

    final Memory memory = Memory.get(target);
    allocator = memory.newAllocator(
        addressableUnitBitSize, basePhysicalAddressForAllocation);

    factoryBuilder = new DataDirectiveFactory.Builder(addressableUnitBitSize, text);
    return factoryBuilder;
  }

  public void endConfig() {
    checkReinitialized();

    InvariantChecks.checkNotNull(factoryBuilder);
    factory = factoryBuilder.build();

    factoryBuilder = null;
  }

  public DataSectionBuilder beginData(
      final BlockId blockId,
      final boolean isGlobal,
      final boolean isSeparateFile) {
    checkInitialized();
    InvariantChecks.checkTrue(null == dataBuilder);

    dataBuilder = new DataSectionBuilder(blockId, factory, isGlobal, isSeparateFile);
    return dataBuilder; 
  }

  public DataSection endData() {
    InvariantChecks.checkNotNull(dataBuilder);

    final DataSection data = dataBuilder.build();
    dataBuilder = null;

    return data;
  }

  public void processData(final LabelManager globalLabels, final DataSection data) {
    InvariantChecks.checkNotNull(globalLabels);
    InvariantChecks.checkNotNull(data);

    for (final DataDirective directive : data.getDirectives()) {
      directive.apply(allocator);
    }

    if (data.isGlobal()) {
      for (final Pair<Label, BigInteger> labelInfo : data.getLabelsWithAddresses()) {
        final Label label = labelInfo.first;
        final long address = labelInfo.second.longValue();
        globalLabels.addLabel(label, address);
      }
    }

    if (data.isSeparateFile()) {
      saveToFile(data.getDirectives());
      return;
    }

    if (data.isGlobal()) {
      globalData.addAll(data.getDirectives());
    } else {
      localData.add(new Pair<>(data.getDirectives(), testCaseIndex));
    }
  }

  public boolean isInitialized() {
    return factory != null;
  }

  public boolean containsDecls() {
    return !globalData.isEmpty() || !localData.isEmpty();
  }

  public void printData(final Printer printer) {
    InvariantChecks.checkNotNull(printer);
    statistics.pushActivity(Statistics.Activity.PRINTING);

    Logger.debugHeader("Printing Data to %s", Printer.getLastFileName());
    printer.printHeaderToFile("Data");

    final String headerText = factory.getHeader().getText();
    printer.printToScreen(TestSettings.getIndentToken() + headerText);
    printer.printToFile(headerText);

    if (!globalData.isEmpty()) {
      printer.printToFile("");
      printer.printSeparatorToFile("Global Data");
    }

    for (final DataDirective item : globalData) {
      final String text = item.getText();
      if (item.needsIndent()) {
        printer.printToScreen(TestSettings.getIndentToken() + text);
        printer.printToFile(text);
      } else {
        printer.printTextNoIndent(text);
      }
    }

    if (!localData.isEmpty()) {
      printer.printToFile("");
      printer.printSeparatorToFile("Test Case Data");
    }

    int currentTestCaseIndex = -1;
    for (final Pair<List<DataDirective>, Integer> pair : localData) {
      final List<DataDirective> data = pair.first;
      final int index = pair.second;

      if (index != currentTestCaseIndex) {
        currentTestCaseIndex = index;
        printer.printSubheaderToFile(String.format("Test Case %d", currentTestCaseIndex));
      }

      for (final DataDirective item : data) {
        final String text = item.getText();
        if (item.needsIndent()) {
          printer.printToScreen(TestSettings.getIndentToken() + text);
          printer.printToFile(text);
        } else {
          printer.printTextNoIndent(text);
        }
      }
    }

    statistics.popActivity();
  }

  public void clearLocalData() {
    localData.clear();
  }

  public void setTestCaseIndex(final int value) {
    testCaseIndex = value;
  }

  public BigInteger getAddress() {
    checkInitialized();
    final BigInteger physicalAddress = allocator.getCurrentAddress();
    final BigInteger virtualAddress = AddressTranslator.get().physicalToVirtual(physicalAddress);
    return virtualAddress;
  }

  public BigInteger getBaseAddress() {
    checkInitialized();
    InvariantChecks.checkNotNull(baseVirtualAddress);
    return baseVirtualAddress;
  }

  public void generateData(
      final BigInteger address,
      final String labelName,
      final String typeId,
      final int length,
      final String method,
      final boolean isSeparateFile) {
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(labelName);
    InvariantChecks.checkNotNull(typeId);
    InvariantChecks.checkGreaterThanZero(length);
    InvariantChecks.checkNotNull(method);

    checkInitialized();

    final DataSectionBuilder dataBuilder = new DataSectionBuilder(
        new BlockId(), factory, true, isSeparateFile);

    final DataDirectiveFactory.TypeInfo typeInfo = factory.findTypeInfo(typeId);
    final DataGenerator dataGenerator = DataGenerator.newInstance(method, typeInfo.type);

    final BigInteger oldAddress = allocator.getCurrentAddress();
    try {
      allocator.setCurrentAddress(address);

      final BitVector bvAddress =
          BitVector.valueOf(address, allocator.getAddressBitSize());

      dataBuilder.addLabel(labelName);
      dataBuilder.addComment(String.format(" Address: 0x%s", bvAddress.toHexString()));

      for (int index = 0; index < length; index += 4) {
        final int count = Math.min(length - index, 4);
        dataBuilder.addGeneratedData(typeInfo, dataGenerator, count);
      }

      processData(labelManager, dataBuilder.build());
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

    final DataSectionBuilder dataBuilder = new DataSectionBuilder(
        new BlockId(), factory, true, isSeparateFile);

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

      processData(labelManager, dataBuilder.build());
    } finally {
      allocator.setCurrentAddress(oldAddress);
    }
  }

  private void saveToFile(final List<DataDirective> data) {
    statistics.pushActivity(Statistics.Activity.PRINTING);

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

    statistics.popActivity();
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
