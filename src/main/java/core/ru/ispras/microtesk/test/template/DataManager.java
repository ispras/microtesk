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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.model.memory.Sections;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.test.engine.EngineContext;

import java.math.BigInteger;

/**
 * The {@link DataManager} class create internal representation of data sections.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class DataManager {
  private final EngineContext engineContext;

  private DataDirectiveFactory.Builder factoryBuilder;
  private DataSectionBuilder dataBuilder;

  protected DataManager(final EngineContext engineContext) {
    InvariantChecks.checkNotNull(engineContext);

    this.engineContext = engineContext;
    this.factoryBuilder = null;
    this.dataBuilder = null;
  }

  public DataDirectiveFactory.Builder beginConfig(
      final String target,
      final int addressableUnitBitSize) throws ConfigurationException {
    InvariantChecks.checkNotNull(target);
    InvariantChecks.checkGreaterThanZero(addressableUnitBitSize);

    checkReinitialized();

    final Model model = engineContext.getModel();
    final Options options = engineContext.getOptions();

    model.initMemoryAllocator(target, addressableUnitBitSize, BigInteger.ZERO);
    factoryBuilder = new DataDirectiveFactory.Builder(options, addressableUnitBitSize);

    return factoryBuilder;
  }

  public void endConfig() {
    checkReinitialized();

    InvariantChecks.checkNotNull(factoryBuilder);
    engineContext.setDataDirectiveFactory(factoryBuilder.build());
    factoryBuilder = null;
  }

  public DataSectionBuilder beginData(
      final BlockId blockId,
      final Section section,
      final boolean isGlobal,
      final boolean isSeparateFile) {
    checkInitialized();
    InvariantChecks.checkTrue(null == dataBuilder);

    final DataDirectiveFactory factory = engineContext.getDataDirectiveFactory();
    dataBuilder = new DataSectionBuilder(blockId, factory, section, isGlobal, isSeparateFile);
    return dataBuilder;
  }

  public DataSection endData() {
    InvariantChecks.checkNotNull(dataBuilder);

    final DataSection data = dataBuilder.build();
    dataBuilder = null;

    return data;
  }

  public boolean isInitialized() {
    return engineContext.getDataDirectiveFactory() != null;
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

    final Section section = Sections.get().getDataSection();
    InvariantChecks.checkNotNull(section, "Data section is not defined in the template!");

    final DataDirectiveFactory factory = engineContext.getDataDirectiveFactory();
    final DataSectionBuilder dataBuilder = new DataSectionBuilder(
        new BlockId(),
        factory,
        section,
        true,
        isSeparateFile
        );

    dataBuilder.setPhysicalAddress(address);

    final DataDirectiveFactory.TypeInfo typeInfo = factory.findTypeInfo(typeId);
    final DataGenerator dataGenerator = DataGenerator.newInstance(method, typeInfo.type);

    dataBuilder.addLabel(labelName);
    dataBuilder.addComment(String.format(" Address: 0x%016x", address));

    for (int index = 0; index < length; index += 4) {
      final int count = Math.min(length - index, 4);
      dataBuilder.addGeneratedData(typeInfo, dataGenerator, count);
    }

    return dataBuilder.build();
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
