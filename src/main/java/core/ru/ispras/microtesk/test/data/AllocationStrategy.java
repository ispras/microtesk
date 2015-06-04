package ru.ispras.microtesk.test.data;

import java.util.LinkedHashSet;
import java.util.Set;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;


/**
 * {@link AllocationStrategy} defines resource allocation strategies.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public enum AllocationStrategy {
  /** Always returns a free object (throws an exception if all the objects are in use). */
  GET_FREE_OBJECT() {
    @Override public <T> T next(final Set<T> free, final Set<T> used) {
      InvariantChecks.checkNotEmpty(free);

      return Randomizer.get().choose(free);
    }
  },

  /** Returns a free object (if it exists) or a used one (otherwise). */
  TRY_FREE_OBJECT() {
    @Override public <T> T next(final Set<T> free, final Set<T> used) {
      InvariantChecks.checkTrue(!free.isEmpty() || !used.isEmpty());

      return !free.isEmpty() ? Randomizer.get().choose(free) : Randomizer.get().choose(used);
    }
  },

  /** Returns a randomly chosen object. */
  GET_RANDOM_OBJECT() {
    @Override public <T> T next(final Set<T> free, final Set<T> used) {
      InvariantChecks.checkTrue(!free.isEmpty() || !used.isEmpty());

      final Set<T> all = new LinkedHashSet<>(free);
      all.addAll(used);

      return Randomizer.get().choose(all);
    }
  };

  /**
   * Chooses an object.
   * 
   * @param <T> type of objects.
   * @param free the set of free objects.
   * @param used the set of used objects.
   * @return the chosen object or {@code null}.
   */
  public abstract <T> T next(final Set<T> free, final Set<T> used);
}
