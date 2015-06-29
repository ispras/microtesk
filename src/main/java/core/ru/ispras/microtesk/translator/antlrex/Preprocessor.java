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

package ru.ispras.microtesk.translator.antlrex;

public interface Preprocessor {
  boolean isDefined(String key);
  boolean underIfElse();
  boolean isHidden();
  void onDefine(String key, String val);
  void onUndef(String key);
  void onIfdef(String key);
  void onIfndef(String key);
  void onElse();
  void onEndif();
  String expand(String key);

  void includeTokensFromFile(String fileName);
  void includeTokensFromString(String substitution);
}
