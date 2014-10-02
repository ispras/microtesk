package ru.ispras.microtesk.translator.simnml.coverage;

final class Naming
{
    public static String VERSION_DELIMITER = "!";
    public static String NESTING_DELIMITER = ".";

    public static boolean isInNamespace(String name, String ns)
    {
        final int pos = ns.length();
        return name.equals(ns)
            || name.startsWith(ns) && name.indexOf(NESTING_DELIMITER, pos) == pos;
    }

    public static String changeNamespace(String name, String prev, String target)
    {
        return name.replaceFirst(prev, target);
    }

    public static String addNamespace(String name, String namespace)
    {
        return namespace + NESTING_DELIMITER + name;
    }
}
