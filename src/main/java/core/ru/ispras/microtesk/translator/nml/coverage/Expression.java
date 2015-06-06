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

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.StandardOperation;

import java.util.Collection;

final class Expression {
  public static final Node TRUE = new NodeValue(Data.newBoolean(true));
  public static final Node FALSE = new NodeValue(Data.newBoolean(false));

  public static NodeOperation EQ(Node lhs, Node rhs) {
    return new NodeOperation(StandardOperation.EQ, lhs, rhs);
  }

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

  public static NodeOperation AND(Node... args) {
    return new NodeOperation(StandardOperation.AND, args);
  }

  public static NodeOperation AND(Collection<? extends Node> args) {
    return new NodeOperation(StandardOperation.AND,
                             args.toArray(new Node[args.size()]));
  }

  public static NodeOperation OR(Node... args) {
    return new NodeOperation(StandardOperation.OR, args);
  }

  public static NodeOperation OR(Collection<? extends Node> args) {
    return new NodeOperation(StandardOperation.OR,
                             args.toArray(new Node[args.size()]));
  }

  public static NodeOperation STORE(Node array, Node key, Node value) {
    return new NodeOperation(StandardOperation.STORE, array, key, value);
  }

  public static NodeOperation SELECT(Node array, Node key) {
    return new NodeOperation(StandardOperation.SELECT, array, key);
  }

  public static NodeOperation NOT(Node e) {
    return new NodeOperation(StandardOperation.NOT, e);
  }

  public static NodeOperation newOperation(Enum<?> opId, Collection<? extends Node> args) {
    return new NodeOperation(opId, args.toArray(new Node[args.size()]));
  }
}
