package ru.ispras.microtesk.translator.simnml.coverage;

import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeVariable;

final class Version {
  public static boolean hasVersion(Node node) {
    return node.getKind() == Node.Kind.VARIABLE &&
           node.getUserData() != null &&
           node.getUserData() instanceof Integer;
  }

  public static int getVersion(Node node) {
    return (Integer) node.getUserData();
  }

  public static NodeVariable bakeVersion(NodeVariable node) {
    final String name = String.format("%s!%d", node.getName(), getVersion(node));
    return new NodeVariable(new Variable(name, node.getData()));
  }

  public static boolean hasBakedVersion(Node node) {
    if (node.getKind() != Node.Kind.VARIABLE) {
      return false;
    }
    final NodeVariable var = (NodeVariable) node;
    final Pair<String, String> pair = Utility.splitOnLast(var.getName(), '!');
    try {
      final int version = Integer.valueOf(pair.second);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  public static NodeVariable undoVersion(NodeVariable node) {
    final Pair<String, Integer> pair = splitVersionedName(node.getName());

    final NodeVariable out = new NodeVariable(new Variable(pair.first, node.getData()));
    out.setUserData(pair.second);

    return out;
  }

  private static Pair<String, Integer> splitVersionedName(String name) {
    final Pair<String, String> pair = Utility.splitOnLast(name, '!');
    return new Pair<>(pair.first, Integer.valueOf(pair.second));
  }
}
