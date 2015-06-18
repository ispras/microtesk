package ru.ispras.microtesk.test;

import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.template.Call;

public interface Adapter<T> {
  TestSequence adapt(Sequence<Call> abstractSequence, T solution);
}
