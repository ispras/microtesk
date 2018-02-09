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

package ru.ispras.microtesk.test.template;

import ru.ispras.fortress.randomizer.Variate;

import java.math.BigInteger;

public interface PrimitiveBuilder {
  Primitive build();

  void setContext(String contextName);

  void setSituation(Situation situation);

  void setSituation(Variate<Situation> situation);

  void addArgument(BigInteger value);

  void addArgument(String value);

  void addArgument(RandomValue value);

  void addArgument(Primitive value);

  void addArgument(PrimitiveBuilder value);

  void addArgument(UnknownImmediateValue value);

  void addArgument(LazyValue value);

  void addArgument(LabelValue value);

  void setArgument(String name, BigInteger value);

  void setArgument(String name, String value);

  void setArgument(String name, RandomValue value);

  void setArgument(String name, Primitive value);

  void setArgument(String name, PrimitiveBuilder value);

  void setArgument(String name, UnknownImmediateValue value);

  void setArgument(String name, LazyValue value);

  void setArgument(String name, LabelValue value);
}
