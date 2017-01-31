/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.ir.spec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link MmuProgram} represents a system of transitions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuProgram {
  public static final MmuProgram EMPTY =
      new MmuProgram(Collections.<Collection<MmuProgram>>emptyList());

  public static MmuProgram ATOMIC(final MmuTransition transition) {
    return new MmuProgram(transition);
  }

  public static final class Builder {
    private final List<Collection<Object /* Program | Builder */>> statements = new ArrayList<>();
    private Collection<Object> statement = null;

    public void add(final MmuTransition transition) {
      InvariantChecks.checkNotNull(transition);
      add(new MmuProgram(transition));
    }

    public void add(final MmuProgram program) {
      beginSwitch();
        addCase(program);
      endSwitch();
    }

    public void add(final MmuProgram.Builder builder) {
      beginSwitch();
        addCase(builder);
      endSwitch();
    }

    public void beginSwitch() {
      InvariantChecks.checkTrue(statement == null);
      statement = new ArrayList<>();
    }

    public void addCase(final MmuTransition transition) {
      InvariantChecks.checkNotNull(transition);
      addCase(new MmuProgram(transition));
    }

    public void addCase(final MmuProgram program) {
      InvariantChecks.checkNotNull(program);
      statement.add(program);
    }

    public void addCase(final MmuProgram.Builder builder) {
      InvariantChecks.checkNotNull(builder);
      statement.add(builder);
    }

    public void endSwitch() {
      InvariantChecks.checkNotNull(statement);
      InvariantChecks.checkNotEmpty(statement);

      statements.add(statement);
      statement = null;
    }

    public MmuProgram build() {
      final List<Collection<MmuProgram>> realStatements = new ArrayList<>(statements.size());

      for (final Collection<Object> statement : statements) {
        final Collection<MmuProgram> realStatement = new ArrayList<>(statement.size());

        for (final Object object : statement) {
          final MmuProgram realProgram;

          if (object instanceof MmuProgram) {
            realProgram = (MmuProgram) object;
          } else {
            realProgram = ((MmuProgram.Builder) object).build();
          }

          if (statements.size() == 1 && statement.size() == 1) {
            return realProgram;
          }

          realStatement.add(realProgram);
        }
      }

      return new MmuProgram(realStatements);
    }
  }

  private final MmuTransition transition;
  private final List<Collection<MmuProgram>> statements;

  private final Collection<MmuTransition> transitions;

  private final MmuAction source;
  private final MmuAction target;

  private Object label = null;

  private MmuProgram(final MmuTransition transition) {
    InvariantChecks.checkNotNull(transition);

    this.transition = transition;
    this.statements = null;

    this.transitions = Collections.<MmuTransition>singleton(transition);

    this.source = transition.getSource();
    this.target = transition.getTarget();
  }

  private MmuProgram(final List<Collection<MmuProgram>> statements) {
    InvariantChecks.checkNotNull(statements);

    this.transition = null;
    this.statements = Collections.unmodifiableList(statements);

    final Collection<MmuTransition> transitions = new LinkedHashSet<>();
    for (final Collection<MmuProgram> statement : statements) {
      for (final MmuProgram program : statement) {
        transitions.addAll(program.getTransitions());
      }
    }

    this.transitions = Collections.unmodifiableCollection(transitions);

    final int i = 0;
    final int j = statements.size() - 1;

    this.source = !statements.isEmpty() ? statements.get(i).iterator().next().getSource() : null;
    this.target = !statements.isEmpty() ? statements.get(j).iterator().next().getTarget() : null;
  }

  public boolean isAtomic() {
    return transition != null;
  }

  public MmuTransition getTransition() {
    return transition;
  }

  public List<Collection<MmuProgram>> getStatements() {
    return statements;
  }

  public Collection<MmuTransition> getTransitions() {
    return transitions;
  }

  public MmuAction getSource() {
    return source;
  }

  public MmuAction getTarget() {
    return target;
  }

  public Object getLabel() {
    return label;
  }

  public void setLabel(final Object label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return transition != null ? transition.toString() : statements.toString();
  }
}
