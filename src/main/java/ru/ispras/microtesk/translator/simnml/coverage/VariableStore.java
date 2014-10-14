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

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.DataType;

final class VariableStore
{
    public static final VariableStore EMPTY_STORE = new VariableStore(
        Collections.<String, Integer>emptyMap(),
        Collections.<String, Variable>emptyMap());

    public static final String TEMPORARY_NAME = "var";

    private static final String VERSION_FORMAT =
        String.format("%%s%s%%d", Naming.VERSION_DELIMITER);

    private Map<String, Integer>    versions;
    private Map<String, Variable>   variables;

    public VariableStore()
    {
        this(new HashMap<String, Integer>(), new HashMap<String, Variable>());
    }

    private VariableStore(Map<String, Integer> versions, Map<String, Variable> variables)
    {
        this.versions = versions;
        this.variables = variables;
    }

    private String getVersionedName(String name)
    {
        final String base = getBaseName(name);
        final Integer entry = versions.get(base);
        final int version = (entry == null) ? 0 : entry;

        return String.format(VERSION_FORMAT, base, version);
    }

    public static String getBaseName(String name)
    {
        final int pos = name.indexOf('!');
        if (pos > 0)
            return name.substring(0, pos);
        return name;
    }

    public static String getBaseName(Variable v)
    {
        return getBaseName(v.getName());
    }

    private void updateVersion(String base)
    {
        final Integer entry = versions.get(base);
        final int version = (entry == null) ? 1 : entry + 1;
        versions.put(base, version);
    }

    public Variable createTemporary()
    {
        return createTemporary(DataType.INTEGER);
    }

    public Variable createTemporary(DataType type)
    {
        if (type == null)
            throw new NullPointerException();

        return updateVariable(new Variable(TEMPORARY_NAME, type));
    }

    public Variable getVariable(String name)
    {
        if (name == null)
            throw new NullPointerException();

        return variables.get(getVersionedName(name));
    }

    public Variable storeVariable(Variable in)
    {
        if (in == null)
            throw new NullPointerException();

        final Variable v = new Variable(getVersionedName(in.getName()), in.getData());
        final Variable stored = variables.get(v.getName());

        if (stored == null)
            variables.put(v.getName(), v);
        else if (!v.equals(stored))
            throw new IllegalArgumentException("Variable redefinition: " + v.getName());
        else
            return stored;

        return v;
    }

    public Variable updateVariable(String name)
    {
        final Variable stored = getVariable(name);
        if (stored == null)
            throw new IllegalArgumentException("Updating non-existent variable: " + name);

        updateVersion(name);
        return storeVariable(new Variable(name, stored.getData()));
    }

    public Variable updateVariable(Variable in)
    {
        if (in == null)
            throw new NullPointerException();

        updateVersion(in.getName());
        return storeVariable(in);
    }

    public Map<String, Variable> getVariables()
    {
        return Collections.unmodifiableMap(variables);
    }

    public Map<String, Integer> getVersions()
    {
        return Collections.unmodifiableMap(versions);
    }

    public VariableStore createSnapshot()
    {
        return new VariableStore(getVersions(), getVariables());
    }

    public VariableStore retarget(String source, String target)
    {
        final HashMap<String, Variable> targetVars = new HashMap<String, Variable>();
        final HashMap<String, Integer> targetVersions = new HashMap<String, Integer>();

        for (Map.Entry<String, Variable> entry : variables.entrySet())
            if (Naming.isInNamespace(entry.getKey(), source))
            {
                final String name = Naming.changeNamespace(entry.getKey(), source, target);
                targetVars.put(name, new Variable(name, entry.getValue().getData()));
            }
            else
                targetVars.put(entry.getKey(), entry.getValue());

        for (Map.Entry<String, Integer> entry : versions.entrySet())
        {
            final String name =
                (Naming.isInNamespace(entry.getKey(), source))
                ? Naming.changeNamespace(entry.getKey(), source, target)
                : entry.getKey();

            targetVersions.put(name, entry.getValue());
        }
        return new VariableStore(targetVersions, targetVars);
    }
}
