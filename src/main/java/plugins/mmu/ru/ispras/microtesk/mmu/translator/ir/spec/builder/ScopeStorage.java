package ru.ispras.microtesk.mmu.translator.ir.spec.builder;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class ScopeStorage<T> {
  private static final class Scope<T> {
    public final String name;
    public final String path;
    public final Map<String, T> variables;
    public final boolean readOnly;

    public Scope(final String name, final String globalPath) {
      this.name = name;
      this.path = dotConc(globalPath, name);
      this.variables = new HashMap<>();
      this.readOnly = false;
    }

    public Scope(final String globalPath, final Map<String, T> vars) {
      this.name = "";
      this.path = globalPath;
      this.variables = Collections.unmodifiableMap(new HashMap<>(vars));
      this.readOnly = true;
    }
  }

  private final Deque<Scope<T>> scopes = new ArrayDeque<>();
  private final Map<String, T> variables = new HashMap<>();

  public ScopeStorage() {
    scopes.push(new Scope<T>("", ""));
  }

  public void newScope(final String name) {
    final Scope<T> scope = scopes.peek();
    scopes.push(new Scope<T>(name, scope.path));
  }

  public void newScope(final Map<String, T> variables) {
    final Scope<T> scope = scopes.peek();
    scopes.push(new Scope<T>(scope.path, variables));
  }

  public void popScope() {
    scopes.pop();
  }

  public void put(final String name, final T var) {
    final Scope<T> scope = scopes.peek();
    scope.variables.put(name, var);
    variables.put(dotConc(scope.path, name), var);
  }

  public T get(final String name) {
    for (final Scope<T> scope : scopes) {
      final T var = scope.variables.get(name);
      if (var != null) {
        return var;
      }
    }
    return null;
  }

  public String newPath(final String name) {
    return dotConc(scopes.peek().path, name);
  }

  public static String dotConc(final String lhs, final String rhs) {
    if (lhs.isEmpty()) {
      return rhs;
    }
    return lhs + "." + rhs;
  }
}
