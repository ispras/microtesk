package ru.ispras.microtesk.translator.simnml.coverage;

import java.util.List;
import java.util.ArrayList;

final class Utility
{
    static <T> List<T> appendList(List<T> lhs, List<T> rhs)
    {
        if (rhs.isEmpty())
            return lhs;

        if (lhs.isEmpty())
            return new ArrayList<T>(rhs);

        lhs.addAll(rhs);

        return lhs;
    }

    static <T> List<T> appendElement(List<T> lhs, T elem)
    {
        if (lhs.isEmpty())
            lhs = new ArrayList<T>();

        lhs.add(elem);

        return lhs;
    }
}
