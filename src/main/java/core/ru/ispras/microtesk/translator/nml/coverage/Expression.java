/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.coverage;

import java.util.ArrayList;
import java.util.Collection;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.StandardOperation;

final class Expression {
  public static final Node TRUE = NodeValue.newBoolean(true);
  public static final Node FALSE = NodeValue.newBoolean(false);

  public static NodeOperation EXTRACT(NodeValue bot, NodeValue top, Node bv) {
    return new NodeOperation(StandardOperation.BVEXTRACT, top, bot, bv);
  }

  public static NodeOperation EXTRACT(int bot, NodeValue top, Node bv) {
    return EXTRACT(NodeValue.newInteger(bot), top, bv);
  }

  public static NodeOperation EXTRACT(NodeValue bot, int top, Node bv) {
    return EXTRACT(bot, NodeValue.newInteger(top), bv);
  }

  public static NodeOperation EXTRACT(int bot, int top, Node bv) {
    return EXTRACT(NodeValue.newInteger(bot), NodeValue.newInteger(top), bv);
  }

  public static NodeOperation CONCAT(Node... args) {
    for (int i = 0; i < args.length / 2; ++i) {
      final Node tmp = args[i];
      args[i] = args[args.length - i - 1];
      args[args.length - i - 1] = tmp;
    }
    return new NodeOperation(StandardOperation.BVCONCAT, args);
  }

  public static NodeOperation newOperation(Enum<?> opId, Collection<? extends Node> args) {
    return new NodeOperation(opId, new ArrayList<>(args));
  }
}
