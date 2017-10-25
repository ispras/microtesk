/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.tools.templgen.printers;

/**
 * The {@code TemplatePrinter} interface is used to create data printers for templates.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */

public interface TemplatePrinter {
  public abstract void templateBegin();

  public abstract void addOperation(String operationName);
  public abstract void addString(String addString);
  public abstract void addText(String addText);
  public abstract void addComment(String addText);

  public abstract void startSequence(String addText);
  public abstract void closeSequence(String addText);
  
  public abstract void templateEnd();
  public abstract void templateClose();
}
