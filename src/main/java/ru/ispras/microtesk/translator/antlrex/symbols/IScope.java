/*
 * Copyright (c) 2012 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IScope.java, Dec 10, 2012 6:12:59 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.translator.antlrex.symbols;

public interface IScope<Kind extends Enum<Kind>> {
  public void define(ISymbol<Kind> symbol);

  public ISymbol<Kind> resolve(String name);
  public ISymbol<Kind> resolveMember(String name);
  public ISymbol<Kind> resolveNested(String ... names);

  public IScope<Kind> getOuterScope();
  public ISymbol<Kind> getAssociatedSymbol();
}
