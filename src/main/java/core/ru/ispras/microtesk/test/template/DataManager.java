/*
 * Copyright 2014-2017 ISP RAS (http://www.ispras.ru)
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

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.ConfigurationException;
import ru.ispras.microtesk.model.api.Model;
import ru.ispras.microtesk.model.api.memory.AddressTranslator;
import ru.ispras.microtesk.model.api.memory.MemoryAllocator;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.LabelManager;
import ru.ispras.microtesk.test.Printer;
import ru.ispras.microtesk.test.Statistics;

/**
 * The {@link DataManager} class create internal representation of data sections.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class DataManager {
  private final Model model;
  private final Options options;
  private final Statistics statistics;

  private final List<DataSection> globalData;
  private final List<DataSection> localData;
  private LabelManager labelManager;

  private DataDirectiveFactory factory;
  private DataDirectiveFactory.Builder factoryBuilder;
  private DataSectionBuilder dataBuilder;

  public DataManager(
      final Model model,
      final Options options,
      final Statistics statistics) {
    InvariantChecks.checkNotNull(model);
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(statistics);

    this.model = model;
    this.options = options;
    this.statistics = statistics;

    this.globalData = new ArrayList<>();
    this.localData = new ArrayList<>();

    this.factory = null;
    this.factoryBuilder = null;
    this.dataBuilder = null;
  }

  private MemoryAllocator getAllocator() {
    return model.getMemoryAllocator();
  }

  public List<DataSection> getGlobalData() {
    return globalData;
  }

  public List<DataSection> getLocalData() {
    return localData;
  }

  public void setLabelManager(final LabelManager labelManager) {
    this.labelManager = labelManager;
  }

  public DataDirectiveFactory.Builder beginConfig(
      final String target,
      final int addressableUnitBitSize,
      final BigInteger baseVirtualAddress) throws ConfigurationException {
    InvariantChecks.checkNotNull(target);
    InvariantChecks.checkGreaterThanZero(addressableUnitBitSize);

    checkReinitialized();

    final BigInteger baseVA = null != baseVirtualAddress ?
        baseVirtualAddress : options.getValueAsBigInteger(Option.BASE_VA);

    final BigInteger basePA =
        AddressTranslator.get().virtualToPhysical(baseVA);

    model.initMemoryAllocator(target, addressableUnitBitSize, basePA);
    factoryBuilder = new DataDirectiveFactory.Builder(options, addressableUnitBitSize);

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

    data.allocate(getAllocator());
    data.registerLabels(globalLabels);

    if (data.isSeparateFile()) {
      saveToFile(data);
      return;
    }

    if (data.isGlobal()) {
      globalData.add(data);
    } else {
      localData.add(data);
    }
  }

  public void resetLocalData() {
    localData.clear();
  }

  public void reallocateGlobalData() {
    getAllocator().resetCurrentAddress();
    for (final DataSection data : globalData) {
      data.allocate(getAllocator());
      data.registerLabels(labelManager);
    }
  }

  public boolean isInitialized() {
    return factory != null;
  }

  public DataSection generateData(
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

    final DataSectionBuilder dataBuilder =
        new DataSectionBuilder(new BlockId(), factory, true, isSeparateFile);

    dataBuilder.setPhysicalAddress(address);

    final DataDirectiveFactory.TypeInfo typeInfo = factory.findTypeInfo(typeId);
    final DataGenerator dataGenerator = DataGenerator.newInstance(method, typeInfo.type);

    final BitVector bvAddress =
        BitVector.valueOf(address, getAllocator().getAddressBitSize());

    dataBuilder.addLabel(labelName);
    dataBuilder.addComment(String.format(" Address: 0x%s", bvAddress.toHexString()));

    for (int index = 0; index < length; index += 4) {
      final int count = Math.min(length - index, 4);
      dataBuilder.addGeneratedData(typeInfo, dataGenerator, count);
    }

    return dataBuilder.build();
  }

  private void saveToFile(final DataSection data) {
    InvariantChecks.checkNotNull(data);

    Printer printer = null;
    try {
      statistics.pushActivity(Statistics.Activity.PRINTING);

      printer = Printer.newDataFile(options, statistics.getDataFiles());
      printer.printDataDirectives(data.getDirectives());

      statistics.incDataFiles();
    } catch (final IOException e) {
      throw new GenerationAbortedException(
          String.format("Failed to generate data file. Reason: %s", e.getMessage()));
    } finally {
      if (null != printer) {
        printer.close();
      }

      statistics.popActivity();
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
