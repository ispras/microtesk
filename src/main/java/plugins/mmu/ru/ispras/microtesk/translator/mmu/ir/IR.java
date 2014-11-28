package ru.ispras.microtesk.translator.mmu.ir;

import java.util.LinkedHashMap;
import java.util.Map;

public final class IR {
  private Map<String, AssociativityExpr> associativity =
      new LinkedHashMap<String, AssociativityExpr>();
  @SuppressWarnings({"rawtypes", "unused"})
  private Map<String, BufferExpr> buffer = new LinkedHashMap<String, BufferExpr>();
  private Map<String, SetsExpr> set = new LinkedHashMap<String, SetsExpr>();
  private Map<String, LineExpr> line = new LinkedHashMap<String, LineExpr>();
  private Map<String, IndexExpr> index = new LinkedHashMap<String, IndexExpr>();
  private Map<String, String> policy = new LinkedHashMap<String, String>();
  private Map<String, AddressExpr> address = new LinkedHashMap<String, AddressExpr>();
  private Map<String, MatchExpr> match = new LinkedHashMap<String, MatchExpr>();
  private Map<String, TagExpr> tag = new LinkedHashMap<String, TagExpr>();

  public IR() {}

  public void add(String name, IndexExpr value) {
    index.put(name, value);
  }

  public void add(String name, AssociativityExpr value) {
    associativity.put(name, value);
  }

  public void add(String name, SetsExpr value) {
    set.put(name, value);
  }

  public void add(String name, LineExpr value) {
    line.put(name, value);
  }

  public void add(String name, String value) {
    policy.put(name, value);
  }

  public void add(String name, AddressExpr value) {
    address.put(name, value);
  }

  public void add(String name, MatchExpr value) {
    match.put(name, value);
  }

  public void add(String name, TagExpr value) {
    tag.put(name, value);
  }

  public Map<String, String> getPolicy() {
    return policy;
  }

  public Map<String, AddressExpr> getAddress() {
    return address;
  }

  public Map<String, LineExpr> getLine() {
    return line;
  }

  public Map<String, TagExpr> getTag() {
    return tag;
  }

  public void add(String name, Object dataExpr) {}

  public void add(String name, @SuppressWarnings("rawtypes") BufferExpr bufferExpr) {}

}
