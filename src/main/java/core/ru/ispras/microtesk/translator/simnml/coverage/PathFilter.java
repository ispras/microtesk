package ru.ispras.microtesk.translator.simnml.coverage;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.StandardOperation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

final class PathFilter {
  private static final class OpHeader {
    public final Enum<?> opId;
    public int numOperands;

    public OpHeader(Enum<?> opId) {
      this.opId = opId;
      this.numOperands = 0;
    }
  }

  final Deque<OpHeader> headers;
  final List<Node> operands;

  private PathFilter() {
    this.headers = new ArrayDeque<>();
    this.operands = new ArrayList<>();
  }

  private void processNode(Node node) {
    if (nodeIsOperation(node, StandardOperation.AND)) {
      processConjunction(node);
    } else if (nodeIsOperation(node, StandardOperation.OR)) {
      processDisjunction(node);
    }
  }

  private void processConjunction(Node node) {
    final NodeOperation op = (NodeOperation) node;
    push(StandardOperation.AND);
    for (Node child : op.getOperands()) {
      processNode(child);
    }
    pop();
  }

  private void processDisjunction(Node node) {
    final NodeOperation op = (NodeOperation) node;
    push(StandardOperation.OR);
    for (Node child : op.getOperands()) {
      final NodeOperation branch = (NodeOperation) child;
      push(StandardOperation.AND);
      addOperand(branch.getOperand(0));
      for (int i = 1; i < branch.getOperandCount(); ++i) {
        processNode(branch.getOperand(i));
      }
      pop();
    }
    pop();
  }

  private void push(Enum<?> op) {
    this.headers.push(new OpHeader(op));
  }

  private void addOperand(Node operand) {
    this.operands.add(operand);
    this.headers.peek().numOperands++;
  }

  private void pop() {
    final OpHeader header = headers.pop();
    if (header.numOperands == 1) {
      headers.peek().numOperands++;
    } else if (header.numOperands > 1) {
      final List<Node> operands =
          this.operands.subList(this.operands.size() - header.numOperands,
                                this.operands.size());
      final Node op = Expression.newOperation(header.opId, operands);
      operands.clear();
      addOperand(op);
    }
  }

  public static Node filter(Node node) {
    final PathFilter filter = new PathFilter();
    filter.push(StandardOperation.AND);
    filter.processNode(node);
    if (filter.operands.isEmpty()) {
      return Expression.TRUE;
    }
    return filter.operands.get(0);
  }

  private static boolean nodeIsOperation(Node node, Enum<?> opId) {
    return node.getKind() == Node.Kind.OPERATION &&
           ((NodeOperation) node).getOperationId() == opId;
  }
}
