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

package ru.ispras.microtesk.translator.simnml;

import static org.junit.Assert.fail;

import java.util.Arrays;

import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.antlrex.log.LogEntry;
import ru.ispras.microtesk.translator.antlrex.log.LogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogStoreListener;
import ru.ispras.microtesk.translator.simnml.ir.IR;

public class LargeAddrTestCase {
  
  public static class LogChecker extends LogStoreListener {
    public LogChecker(LogStore log) {
      super(log);
    }

    @Override
    protected void processLogEntry(LogEntry entry) {
      fail(entry.toString());
    }
  }

  public static class IrChecker implements TranslatorHandler<IR> {
    @Override
    public void processIr(IR ir) {
      System.out.println(ir);
    }
  }

  @Test
  public void test() {
    final SimnMLAnalyzer analyzer = new SimnMLAnalyzer();

    analyzer.setLog(new LogChecker(analyzer.getLog()));
    analyzer.addHandler(new IrChecker());

    try {
      analyzer.start(Arrays.asList("./src/test/nml/large_addr.nml"));
    } catch (RecognitionException e) {
      fail(e.toString());
    }
  }
}
