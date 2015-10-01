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

package ru.ispras.microtesk.mmu.settings;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.settings.AbstractSettings;

/**
 * {@link IntegerValuesSettings} describes which cache policies to be used in tests.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerValuesSettings extends AbstractSettings {
  public static final String TAG = String.format("%s-integerValues", MmuSettings.TAG_PREFIX);

  private final String name;

  private final BigInteger min;
  private final BigInteger max;

  private final Set<BigInteger> include = new LinkedHashSet<>();
  private final Set<BigInteger> exclude = new LinkedHashSet<>();

  public IntegerValuesSettings(final String name, final BigInteger min, final BigInteger max) {
    super(TAG);

    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(min);
    InvariantChecks.checkNotNull(max);

    this.name = name;
    this.min = min;
    this.max = max;
  }

  @Override
  public String getName() {
    return name;
  }

  public BigInteger getMin() {
    return min;
  }

  public BigInteger getMax() {
    return max;
  }

  public Set<BigInteger> getPossibleValues() {
    final Set<BigInteger> values = new LinkedHashSet<>();

    for (BigInteger i = min; i.compareTo(max) <= 0; i = i.add(BigInteger.ONE)) {
      values.add(i);
    }

    return values;
  }

  public Set<BigInteger> getIncludeValues() {
    return include;
  }

  public Set<BigInteger> getExcludeValues() {
    return exclude;
  }

  public Set<BigInteger> getValues() {
    final Set<BigInteger> values = getPossibleValues();

    values.addAll(getIncludeValues());
    values.removeAll(getExcludeValues());

    return values;
  }

  @Override
  public Collection<AbstractSettings> get(final String tag) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(final AbstractSettings section) {
    if (section instanceof IncludeSettings) {
      final IncludeSettings includeSection = (IncludeSettings) section;
      include.add(includeSection.getValue());
    } else if (section instanceof ExcludeSettings) {
      final ExcludeSettings excludeSection = (ExcludeSettings) section;
      exclude.add(excludeSection.getValue());
    }
  }

  @Override
  public String toString() {
    return String.format("%s=([%s, %s], include=%s, exclude=%s)",
        TAG, min, max, include, exclude);
  }
}
