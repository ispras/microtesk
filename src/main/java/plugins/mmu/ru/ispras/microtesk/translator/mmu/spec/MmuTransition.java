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

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link MmuTransition} describes a transition, which is a link between two actions.
 * 
 * <p>The description includes the source and the target actions as well as the guard condition that
 * activates the transition.</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuTransition {
  /** Flag indicating whether the transition is enabled, i.e. included into the specification. */
  private boolean enabled = true;

  /** Source action. */
  private final MmuAction source;
  /** Target action. */
  private final MmuAction target;
  /** Guard condition or {@code null} if the transition is interpreted as {@code goto}. */
  private final MmuGuard guard;

  /**
   * Constructs a guarded transition.
   * 
   * @param source the source action.
   * @param target the target action.
   * @param guard the guard condition or {@code null}.
   */
  public MmuTransition(final MmuAction source, final MmuAction target, final MmuGuard guard) {
    InvariantChecks.checkNotNull(source);
    InvariantChecks.checkNotNull(target);

    this.source = source;
    this.target = target;
    this.guard = guard;
  }

  /**
   * Constructs a transition with no guard.
   * 
   * @param source the source action.
   * @param target the target action.
   */
  public MmuTransition(final MmuAction source, final MmuAction target) {
    this(source, target, null);
  }

  /**
   * Checks whether the transition is enabled.
   * 
   * @return {@code true} if the transition is enabled; {@code false} otherwise.
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets the enabling flag.
   * 
   * @param enabled the flag indicating whether the transition is enabled.
   */
  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Returns the source action of the transition.
   * 
   * @return the source action.
   */
  public MmuAction getSource() {
    return source;
  }

  /**
   * Returns the target action of the transition.
   * 
   * @return the target action.
   */
  public MmuAction getTarget() {
    return target;
  }

  /**
   * Returns the guard condition of the transition.
   * 
   * @return the guard condition.
   */
  public MmuGuard getGuard() {
    return guard;
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder();

    string.append("Transition: {source: ");
    string.append(source);
    string.append(" -> guard:[");
    if (this.guard != null) {
      string.append(this.guard.toString());
    } else {
      string.append("null");
    }
    string.append("] -> target: ");
    string.append(target);
    string.append("}");

    return string.toString();
  }
}
