package ru.ispras.microtesk.test.data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.randomizer.VariateBiased;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link AllocationStrategyId} defines some resource allocation strategies.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public enum AllocationStrategyId implements AllocationStrategy {
  /** Always returns a free object (throws an exception if all the objects are in use). */
  FREE() {
    @Override 
    public <T> T next(final Set<T> free, final Set<T> used, final Map<String, String> attributes) {
      InvariantChecks.checkNotEmpty(free);

      return Randomizer.get().choose(free);
    }
  },

  /** Returns a free object (if it exists) or a used one (otherwise). */
  TRY_FREE() {
    @Override 
    public <T> T next(final Set<T> free, final Set<T> used, final Map<String, String> attributes) {
      InvariantChecks.checkTrue(!free.isEmpty() || !used.isEmpty());

      return !free.isEmpty() ? Randomizer.get().choose(free) : Randomizer.get().choose(used);
    }
  },

  /** Returns a randomly chosen object. */
  RANDOM() {
    private static final String ATTR_FREE_BIAS = "free-bias";
    private static final String ATTR_USED_BIAS = "used-bias";

    @Override 
    public <T> T next(final Set<T> free, final Set<T> used, final Map<String, String> attributes) {
      InvariantChecks.checkTrue(!free.isEmpty() || !used.isEmpty());

      if (free.isEmpty() || used.isEmpty()) {
        final Set<T> nonEmptySet = free.isEmpty() ? used : free;
        return Randomizer.get().choose(nonEmptySet);
      }

      if (attributes != null && attributes.containsKey(ATTR_FREE_BIAS)
          && attributes.containsKey(ATTR_USED_BIAS)) {
        final List<Set<T>> values = new ArrayList<>();
        values.add(free);
        values.add(used);

        final List<Integer> biases = new ArrayList<>();
        biases.add(Integer.parseInt(attributes.get(ATTR_FREE_BIAS)));
        biases.add(Integer.parseInt(attributes.get(ATTR_USED_BIAS)));

        final VariateBiased<Set<T>> variate = new VariateBiased<>(values, biases);
        return Randomizer.get().choose(variate.value());
      }

      final Set<T> allObjects = new LinkedHashSet<>(free);
      allObjects.addAll(used);

      return Randomizer.get().choose(allObjects);
    }
  };
}
