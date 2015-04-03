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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.mmu.spec.basis.MemoryOperation;

/**
 * This class describes a memory management unit (MMU). The description includes a set of devices
 * and a network (directed acyclic graph with one source and one sink nodes) of actions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class MmuSpecification {
  /**
   * Contains available address types.
   * 
   * <p>Typically, this set includes two types: Virtual Address and Physical Address.</p>
   */
  private Set<MmuAddress> addresses = new LinkedHashSet<>();

  /**
   * Refers to the primary address type of the memory management unit.
   * 
   * <p>Type of addresses used to access the memory (typically, Virtual Address).</p>
   */
  private MmuAddress startAddress;

  /** Contains of devices (buffers) of the memory management unit. */
  private Set<MmuDevice> devices = new LinkedHashSet<>();

  /** Maps actions to out-going transitions. */
  private Map<MmuAction, List<MmuTransition>> actions = new LinkedHashMap<>();

  /** Refers to the initial (root) action of the memory management unit. */
  private MmuAction startAction;

  /**
   * Returns the set of addresses registered in the memory management unit.
   * 
   * @return the set of addresses.
   */
  public Set<MmuAddress> getAddresses() {
    return addresses;
  }

  /**
   * Registers the address type in the memory management unit.
   * 
   * @param address the address to be registered.
   * @throws NullPointerException if {@code address} is null.
   */
  public void registerAddress(final MmuAddress address) {
    InvariantChecks.checkNotNull(address);

    addresses.add(address);
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
   * Sets the primary address type.
   * 
   * @param address the address type to be set.
   * @throws NullPointerException if {@code address} is null.
   */
  public void setStartAddress(final MmuAddress address) {
    InvariantChecks.checkNotNull(address);

    startAddress = address;
  }

  /**
   * Returns the set of devices registered in the memory management unit.
   * 
   * @return the set of devices.
   */
  public Set<MmuDevice> getDevices() {
    return devices;
  }

  /**
   * Registers the device in the memory management unit.
   * 
   * @param device the device to be registered.
   * @throws NullPointerException if {@code device} is null.
   */
  public void registerDevice(final MmuDevice device) {
    InvariantChecks.checkNotNull(device);

    devices.add(device);
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

  /**
   * Returns all possible execution paths.
   * 
   * @return the list of execution paths.
   */
  public List<MmuExecution> getExecutions() {
    final List<MmuExecution> executions = new ArrayList<>();
    final List<MmuTransition> transitions = getTransitions(getStartAction());

    if (transitions != null && !transitions.isEmpty()) {
      for (final MmuTransition transition : transitions) {
        final MmuExecution execution = new MmuExecution(transition.getGuard().getOperation());

        execution.addTransition(transition);
        executions.add(execution);
      }

      // Add all possible execution paths.
      int i = 0;

      while (i < executions.size()) {
        final List<MmuExecution> executionPrefixList = elongateExecutions(executions.get(i));

        if (executionPrefixList != null && executionPrefixList.isEmpty()) {
          executions.remove(i);
          continue;
        }

        if (executionPrefixList != null) {
          executions.remove(i);
          executions.addAll(executionPrefixList);
        } else {
          i++;
        }
      }
    }

    return executions;
  }

  /**
   * Elongates the execution path.
   * 
   * @param execution the execution path to be elongated.
   * @return the list of possible elongations of the execution path.
   */
  private List<MmuExecution> elongateExecutions(final MmuExecution execution) {
    // Get the last transition of the execution path.
    final List<MmuTransition> transitions = execution.getTransitions();
    final MmuTransition lastTransition = transitions.get(transitions.size() - 1);
    final MmuAction target = lastTransition.getTarget();

    // Get the outgoing transitions of this action.
    final List<MmuTransition> targetTransitions = getTransitions(target);

    // Elongate the execution path.
    if (targetTransitions != null && !targetTransitions.isEmpty()) {
      final List<MmuExecution> elongatedExecutionList = new ArrayList<>();

      for (final MmuTransition transition : targetTransitions) {
        final MemoryOperation executionOperation = execution.getOperation();

        final MmuGuard mmuGuard = transition.getGuard();
        final MemoryOperation transitionOperation =
            mmuGuard != null ? mmuGuard.getOperation() : null;

        MemoryOperation operation;

        if (executionOperation == null || transitionOperation == null) {
          operation = executionOperation == null ? transitionOperation : executionOperation;
        } else if (executionOperation.equals(transitionOperation)) {
          operation = executionOperation;
        } else {
          continue;
        }

        final MmuExecution elongatedExecution = new MmuExecution(operation);

        elongatedExecution.addTransitions(execution.getTransitions());
        elongatedExecution.addTransition(transition);
        elongatedExecutionList.add(elongatedExecution);
      }

      return elongatedExecutionList;
    }

    return null;
  }

  @Override
  public String toString() {
    final String newline = System.getProperty("line.separator");

    final StringBuilder builder = new StringBuilder("MMU:");

    builder.append(newline);
    builder.append("Addresses: ");
    builder.append(addresses.size());
    for (final MmuAddress address : addresses) {
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
    for (final MmuDevice device : devices) {
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
