/*
 * Copyright 2015-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.sim;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.ModelStateManager;
import ru.ispras.microtesk.model.memory.MemoryDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link MmuModel} is a base class for all MMU models.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public class MmuModel implements ModelStateManager {
  private final Map<String, BufferObserver> buffers;
  private final List<ModelStateManager> stateManagers;
  private final MemoryDevice device;
  private final Memory<? extends Struct, ? extends Address> target;
  private final String targetId;

  public MmuModel(
      final MemoryDevice device,
      final String targetId,
      final Memory<? extends Struct, ? extends Address> target) {
    InvariantChecks.checkNotNull(device);
    InvariantChecks.checkNotNull(targetId);
    InvariantChecks.checkNotNull(target);

    this.buffers = new HashMap<>();
    this.stateManagers = new ArrayList<>();
    this.device = device;
    this.target = target;
    this.targetId = targetId;
  }

  protected final void addBuffer(final String bufferId, final Buffer<?,?> buffer) {
    if (buffer instanceof BufferObserver) {
      buffers.put(bufferId, (BufferObserver) buffer);
    }
    if (buffer instanceof ModelStateManager) {
      stateManagers.add((ModelStateManager) buffer);
    }
  }

  public final BufferObserver getBufferObserver(final String bufferId) {
    return buffers.get(bufferId);
  }

  public final MemoryDevice getMmuDevice() {
    return device;
  }

  public final String getStorageDeviceId() {
    return targetId;
  }

  public final void setStorageDevice(final MemoryDevice device) {
    target.setStorage(device);
  }

  @Override
  public void setUseTempState(final boolean value) {
    for (final ModelStateManager stateManager : stateManagers) {
      stateManager.setUseTempState(value);
    }
  }

  @Override
  public void resetState() {
    for (final ModelStateManager stateManager : stateManagers) {
      stateManager.resetState();
    }
  }
}
