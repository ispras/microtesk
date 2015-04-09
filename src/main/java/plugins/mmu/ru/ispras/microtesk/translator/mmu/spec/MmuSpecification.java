/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.mmu.spec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * This class describes a memory management unit (MMU). The description includes a set of devices
 * and a network (directed acyclic graph with one source and one sink nodes) of actions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public class MmuSpecification {
  /**
   * Stores available address types.
   * <p>Typically, includes two types: Virtual Address and Physical Address.</p>
   */
  private Map<String, MmuAddress> addresses = new LinkedHashMap<>();

  /**
   * Refers to the primary address type of the MMU.
   * <p>Address type used to access the memory (typically, Virtual Address).</p>
   */
  private MmuAddress startAddress;

  /** Stores devices (buffers) of the MMU. */
  private Map<String, MmuDevice> devices = new LinkedHashMap<>();

  /** Maps actions to out-going transitions. */
  private Map<MmuAction, List<MmuTransition>> actions = new LinkedHashMap<>();

  /** Refers to the initial (root) action of the memory management unit. */
  private MmuAction startAction;

  /**
   * Registers the address type in the MMU. Address that have the same 
   * name are considered as duplicates and ignored.
   * 
   * @param address the address to be registered.
   * @throws NullPointerException if {@code address} is {@code null}.
   */
  public void registerAddress(final MmuAddress address) {
    InvariantChecks.checkNotNull(address);
    addresses.put(address.getName(), address);
  }

  /**
   * Returns the collection of addresses registered in the MMU.
   * 
   * @return the collection of addresses.
   */
  public Collection<MmuAddress> getAddresses() {
    return Collections.unmodifiableCollection(addresses.values());
  }

  /**
   * Returns an address registered in the MMU by its name.
   * 
   * @param name the name of the address.
   * @return address or {@code null} it is undefined.
   */
  public MmuAddress getAddress(String name) {
    return addresses.get(name);
  }

  /**
   * Sets the primary address type.
   * 
   * @param address the address type to be set.
   * @throws NullPointerException if {@code address} is {@code null}.
   */
  public void setStartAddress(final MmuAddress address) {
    InvariantChecks.checkNotNull(address);
    startAddress = address;
  }

  /**
   * Returns the primary address type.
   * 
   * @return the start address.
   */
  public MmuAddress getStartAddress() {
    return startAddress;
  }

  /**
   * Registers a device in the MMU. Devices are identified by their name.
   * Devices with equal names are considered duplicates and ignored.
   * 
   * @param device the device to be registered.
   * @throws NullPointerException if {@code device} is {@code null}.
   */
  public void registerDevice(MmuDevice device) {
    InvariantChecks.checkNotNull(device);
    devices.put(device.getName(), device);
  }

  /**
   * Returns the collection of devices registered in the MMU.
   * 
   * @return the collection of devices.
   */
  public Collection<MmuDevice> getDevices() {
    return devices.values();
  }

  /**
   * Returns a device registered in the MMU by its name.
   * 
   * @param name the name of the device.
   * @return device or {@code null} it is undefined.
   */
  public MmuDevice getDevice(String name) {
    return devices.get(name);
  }

  /**
   * Returns the set of actions registered in the memory management unit.
   * 
   * @return the set of actions.
   */
  public Set<MmuAction> getActions() {
    return actions.keySet();
  }

  /**
   * Registers the action in the memory management unit.
   * 
   * <p>Actions should be registered before transitions.</p>
   * 
   * @param action the action to be registered.
   * @throws NullPointerException if {@code action} is null.
   */
  public void registerAction(final MmuAction action) {
    InvariantChecks.checkNotNull(action);

    actions.put(action, new ArrayList<MmuTransition>());
  }

  /**
   * Returns the list of transitions for the given action of the memory management unit.
   * 
   * @param action the action.
   * @return the list of transitions.
   * @throws NullPointerException if {@code action} is null.
   */
  public List<MmuTransition> getTransitions(final MmuAction action) {
    InvariantChecks.checkNotNull(action);

    return actions.get(action);
  }

  /**
   * Registers the transition in the memory management unit.
   * 
   * <p>Transitions should be registered after actions.</p>
   * 
   * @param transition the transition to be registered.
   * @throws NullPointerException if {@code transition} is null.
   */
  public void registerTransition(final MmuTransition transition) {
    InvariantChecks.checkNotNull(transition);

    final List<MmuTransition> transitions = actions.get(transition.getSource());
    transitions.add(transition);
  }

  /**
   * Returns the initial (root) action of the memory management unit.
   * 
   * @return the initial action.
   */
  public MmuAction getStartAction() {
    return startAction;
  }

  /**
   * Sets the initial (root) action of the memory management unit.
   * 
   * @param action the initial action.
   * @throws NullPointerException if {@code action} is null.
   */
  public void setStartAction(final MmuAction action) {
    InvariantChecks.checkNotNull(action);

    startAction = action;
  }

  @Override
  public String toString() {
    final String newline = System.getProperty("line.separator");

    final StringBuilder builder = new StringBuilder("MMU:");

    builder.append(newline);
    builder.append("Addresses: ");
    builder.append(addresses.size());
    for (final MmuAddress address : getAddresses()) {
      builder.append(newline);
      builder.append(address);
    }
    builder.append(newline);
    builder.append("Start address: ");
    builder.append(startAddress);
    builder.append(newline);

    builder.append(newline);
    builder.append("Devices: ");
    builder.append(devices.size());
    for (final MmuDevice device : getDevices()) {
      builder.append(newline);
      builder.append("    ");
      builder.append(device);
    }
    builder.append(newline);

    builder.append(newline);
    builder.append("Actions: ");
    builder.append(actions.size());
    builder.append(newline);
    builder.append("Start action: ");
    builder.append(startAction);
    builder.append(newline);

    for (final Map.Entry<MmuAction, List<MmuTransition>> action : actions.entrySet()) {
      builder.append(action.getKey());
      builder.append(": ");
      for (final MmuTransition transition : action.getValue()) {
        builder.append(newline);
        builder.append("    ");
        builder.append(transition);
      }
      builder.append(newline);
    }
    return builder.toString();
  }
}
