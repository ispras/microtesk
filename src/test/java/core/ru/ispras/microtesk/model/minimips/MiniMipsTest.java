/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.minimips;

import org.junit.Assert;

import ru.ispras.microtesk.Logger.EventType;
import ru.ispras.microtesk.test.testutils.TemplateTest;

public class MiniMipsTest extends TemplateTest {
  public MiniMipsTest() {
    super(
        "minimips",
        "src/main/arch/demo/minimips/templates"
        );
  }

  @Override
  public void onEventLogged(final EventType type, final String message) {
    if (EventType.ERROR == type || EventType.WARNING == type) {
      if (!isExpectedError(message)) {
        Assert.fail(message);
      }
    }
  }

  protected boolean isExpectedError(final String message) {
    // FIXME: Temporary. MMU model must be loaded.
    // return false;
    return message.contains(
        "Failed to load the MMU model. Physical memory will be accessed directly.");
  }
}
