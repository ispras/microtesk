/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.vmem;

import org.junit.Assert;

import ru.ispras.castle.util.Logger.EventType;
import ru.ispras.microtesk.test.testutils.TemplateTest;

public abstract class VmemTest extends TemplateTest {
  public VmemTest() {
    super(
        "vmem",
        "src/main/arch/demo/vmem/templates"
        );
  }

  @Override
  public void onEventLogged(final EventType type, final String message) {
    if (EventType.ERROR == type || EventType.WARNING == type) {
      Assert.fail(message);
    }
  }
}
