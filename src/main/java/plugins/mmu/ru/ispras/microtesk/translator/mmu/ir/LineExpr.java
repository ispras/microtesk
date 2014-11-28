package ru.ispras.microtesk.translator.mmu.ir;


class LineExp {
  public int t = 30;
  public int d = 256;

  public LineExp(int d, int t) {
    this.t = t;
    this.d = d;
  }

  public int getTag() {
    return t;
  }

  public int getData() {
    return d;
  }
}


public class LineExpr {
  public LineExpr(LengthExpr t, LengthExpr d) {}

  public int LineExp(int tag, int data) {
    LineExp t = new LineExp(30, 256);
    System.out.println(t.getTag());
    return data;
  }

  public static int tag() {
    return 0;
  }
}
