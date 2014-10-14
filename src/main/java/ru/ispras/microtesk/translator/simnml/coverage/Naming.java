/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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
