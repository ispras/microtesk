package ru.ispras.microtesk.test.data;

import java.util.Map;
import java.util.Set;

/**
 * {@link AllocationStrategy} defines an interface of resource allocation strategies.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface AllocationStrategy {

  /**
   * Chooses an object.
   * 
   * @param <T> type of objects.
   * @param free the set of free objects.
   * @param used the set of used objects.
   * @param attributes the parameters.
   * @return the chosen object or {@code null}.
   */
  <T> T next(final Set<T> free, final Set<T> used, final Map<String, String> attributes);
}
