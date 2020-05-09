package ru.ispras.microtesk.mmu.model.sim;

import ru.ispras.microtesk.model.ModelStateManager;
import ru.ispras.microtesk.test.TestEngine;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.Pair;

import java.util.List;

public class BufferInstanceProxy<E, A> implements Buffer<E, A> {
  public static <E, A> Buffer<E, A> of(
      final List<? extends Buffer<E, A>> items) {
    final var item = items.get(0);
    if (items.size() == 1) {
      return item;
    }
    if (item instanceof BufferObserver && item instanceof ModelStateManager) {
      return new ObserverManagerProxy<>(items);
    } else if (item instanceof BufferObserver) {
      return new ObserverProxy<>(items);
    } else if (item instanceof ModelStateManager) {
      return new ManagerProxy<>(items);
    } else {
      return new BufferInstanceProxy<>(items);
    }
  }

  private final List<? extends Buffer<E, A>> items;

  private BufferInstanceProxy(final List<? extends Buffer<E, A>> items) {
    this.items = items;
  }

  public Buffer<E, A> get() {
    return items.get(TestEngine.getInstance().getModel().getActivePE());
  }

  @Override
  public boolean isHit(A address) {
    return get().isHit(address);
  }

  @Override
  public E readEntry(A address) {
    return get().readEntry(address);
  }

  @Override
  public void writeEntry(A address, BitVector newEntry) {
    get().writeEntry(address, newEntry);
  }

  @Override
  public void writeEntry(A address, int lower, int upper, BitVector newData) {
    get().writeEntry(address, lower, upper, newData);
  }

  @Override
  public void resetState() {
    get().resetState();
  }

  private static final class ManagerProxy<E, A>
      extends BufferInstanceProxy<E, A>
      implements Manager {
    ManagerProxy(final List<? extends Buffer<E, A>> items) {
      super(items);
    }

    @Override
    public ModelStateManager asManager() {
      return (ModelStateManager) get();
    }
  }

  private static final class ObserverProxy<E, A>
      extends BufferInstanceProxy<E, A>
      implements Observer {
    ObserverProxy(final List<? extends Buffer<E, A>> items) {
      super(items);
    }

    @Override
    public BufferObserver asObserver() {
      return (BufferObserver) get();
    }
  }

  private static final class ObserverManagerProxy<E, A>
      extends BufferInstanceProxy<E, A>
      implements Manager, Observer {
    ObserverManagerProxy(final List<? extends Buffer<E, A>> items) {
      super(items);
    }

    @Override
    public ModelStateManager asManager() {
      return (ModelStateManager) get();
    }

    @Override
    public BufferObserver asObserver() {
      return (BufferObserver) get();
    }
  }

  private interface Manager extends ModelStateManager {
    @Override
    default void setUseTempState(final boolean value) {
      asManager().setUseTempState(value);
    }

    @Override
    default void resetState() {
      asManager().resetState();
    }

    ModelStateManager asManager();
  }

  private interface Observer extends BufferObserver {
    @Override
    default boolean isHit(BitVector address) {
      return asObserver().isHit(address);
    }

    @Override
    default Pair<BitVector, BitVector> seeEntry(BitVector index, BitVector way) {
      return asObserver().seeEntry(index, way);
    }

    BufferObserver asObserver();
  }
}
