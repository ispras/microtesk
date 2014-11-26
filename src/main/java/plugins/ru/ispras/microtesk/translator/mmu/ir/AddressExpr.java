package ru.ispras.microtesk.translator.mmu.ir;

import ru.ispras.microtesk.model.api.mmu.Address;

class AddressExp {
  private int width;

  public AddressExp(int width) {
    this.width = width;
  }

  public int getNumber() {
    return width;
  }
}

public class AddressExpr extends Address {
  public AddressExpr(String text, Object object) {}
}
