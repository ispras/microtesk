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

package ru.ispras.microtesk.translator;

import static org.junit.Assert.fail;
import ru.ispras.microtesk.translator.antlrex.log.LogEntry;
import ru.ispras.microtesk.translator.antlrex.log.LogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogStoreListener;
import ru.ispras.microtesk.utils.FileUtils;

/**
 * The {@code TranslatorTest} class is a base class to be implemented by units test for translators.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <Ir> The type of the internal representation (IR) created by the translator.
 */

public abstract class TranslatorTest<Ir> {

  private final class LogChecker extends LogStoreListener {
    public LogChecker(final LogStore log) {
      super(log);
    }

    @Override
    protected void processLogEntry(final LogEntry entry) {
      checkLogEntry(entry);
    }
  }

  private final class IrChecker implements TranslatorHandler<Ir> {
    @Override
    public void processIr(final Ir ir) {
      checkIr(ir);
    }
  }

  private final IrChecker irChecker = new IrChecker(); 

  protected void checkLogEntry(final LogEntry entry) {
    fail(entry.toString());
  }

  protected void checkIr(final Ir ir) {
    System.out.println(ir);
  }

  protected void translate(
      final Translator<Ir> translator,
      final String... fileNames) {

    for (final String fileName : fileNames) {
      final String fileDir = FileUtils.getFileDir(fileName);
      if (null != fileDir) {
        translator.addPath(fileDir);
      }
    }

    translator.setLog(new LogChecker(translator.getLog()));
    translator.addHandler(irChecker);
    translator.start(fileNames);
  }
}
