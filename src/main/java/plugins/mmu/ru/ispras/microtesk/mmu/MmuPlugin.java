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

package ru.ispras.microtesk.mmu;

import ru.ispras.microtesk.Plugin;
import ru.ispras.microtesk.mmu.translator.MmuTranslator;
import ru.ispras.microtesk.translator.Translator;

/**
 * {@code MmuPlugin} is a MicroTESK plugin responsible for specifying and testing memory management
 * units (MMU) of microprocessors (address translation, caching, etc.).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuPlugin implements Plugin {
  @Override
  public Translator<?> getTranslator() {
    return new MmuTranslator();
  }
}
