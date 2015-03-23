package ru.ispras.microtesk.translator.simnml.coverage.ssa;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.solver.constraint.Constraint;

import java.util.Iterator;

final class PathIterator implements Iterator<Constraint> {
  final PathConstraintBuilder builder;
  final Iterator<? extends Node> conditions;

  PathIterator(PathConstraintBuilder builder,
               Iterator<? extends Node> conditions) {
    this.builder = builder;
    this.conditions = conditions;
  }

  @Override
  public boolean hasNext() {
    return conditions.hasNext();
  }

  @Override
  public Constraint next() {
    return builder.build(conditions.next());
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}

public final class Paths implements Iterable<Constraint> {
  final PathConstraintBuilder builder;
  final Iterable<? extends Node> conditions;

  Paths(PathConstraintBuilder builder, Iterable<? extends Node> conditions) {
    this.builder = builder;
    this.conditions = conditions;
  }

  @Override
  public Iterator<Constraint> iterator() {
    return new PathIterator(builder, conditions.iterator());
  }
}
