/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.tools.microft;

import java.util.Map;
import javax.json.*;

public class JsonUtil {
  public static JsonString createString(final String s) {
    return Json.createArrayBuilder().add(s).build().getJsonString(0);
  }

  public static void addAll(
      final JsonObjectBuilder builder,
      final Map<String, ? extends JsonValue> map) {
    for (final Map.Entry<String, ? extends JsonValue> entry : map.entrySet()) {
      builder.add(entry.getKey(), entry.getValue());
    }
  }
}
