/*
 * Copyright 2019 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template.directive;

import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;

public class DirectiveAlignPower2 extends DirectiveAlign {
  DirectiveAlignPower2(
      final Options options,
      final int alignment,
      final int alignmentInBytes,
      final int fillWith) {
    super(options, alignment, alignmentInBytes, fillWith);
  }

  @Override
  public String getText() {
    return fillWith == -1
        ? String.format(options.getValueAsString(Option.POWER2_ALIGN_FORMAT), alignment)
        : String.format(options.getValueAsString(Option.POWER2_ALIGN_FORMAT2), alignment, fillWith);
  }
}
