/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.utils.StringUtils;

final class Version {
  public static boolean hasVersion(final Node node) {
    return ExprUtils.isVariable(node) &&
           node.getUserData() != null &&
           node.getUserData() instanceof Integer;
  }

  public static int getVersion(final Node node) {
    return (Integer) node.getUserData();
  }

  public static NodeVariable bakeVersion(final NodeVariable node) {
    final String name = String.format("%s!%d", node.getName(), getVersion(node));
    return new NodeVariable(new Variable(name, node.getData()));
  }

  public static boolean hasBakedVersion(final Node node) {
    if (!ExprUtils.isVariable(node)) {
      return false;
    }

    final NodeVariable var = (NodeVariable) node;
    final Pair<String, String> pair = StringUtils.splitOnLast(var.getName(), '!');

    try {
      /*final int version = */Integer.valueOf(pair.second);
    } catch (final NumberFormatException e) {
      return false;
    }

    return true;
  }

  public static NodeVariable undoVersion(final NodeVariable node) {
    final Pair<String, Integer> pair = splitVersionedName(node.getName());

    final NodeVariable out = new NodeVariable(new Variable(pair.first, node.getData()));
    out.setUserData(pair.second);

    return out;
  }

  private static Pair<String, Integer> splitVersionedName(final String name) {
    final Pair<String, String> pair = StringUtils.splitOnLast(name, '!');
    return new Pair<>(pair.first, Integer.valueOf(pair.second));
  }
}
