package ru.ispras.microtesk.docgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XML {

  // TODO: Encapsulate fields
  private Map<String, String> attributes;

  public Map<String, String> getAttributes() {
    return this.attributes;
  }

  private String tag;

  public String getTag() {
    return this.tag;
  }

  private String content;

  public String getContent() {
    return this.content;
  }

  private XmlElementType type;

  public XmlElementType getType() {
    return this.type;
  }

  private List<XML> subXmls;

  public List<XML> getSubXmls() {
    return this.subXmls;
  }

  private XmlScope scope;

  public XmlScope getScope() {
    return this.scope;
  }

  public void setScope(XmlScope scope) {
    this.scope = scope;
  }

  public XML() {}

  public XML(String tag, XmlElementType type, Map<String, String> attributes) {
    this.type = type;
    this.attributes = attributes;
    this.tag = tag;
    this.subXmls = new ArrayList<XML>();
  }

  public XML addSubEntry(XML xml) throws FormatterException {
    if (type != XmlElementType.INTERMEDIATE) {
      throw new FormatterException("Unable to add subentry");
    } else {
      subXmls.add(xml);
      return xml;
    }
  }

  public void assignContent(String content) throws FormatterException {
    if (type != XmlElementType.LEAF) {
      throw new FormatterException("Unable to add subentry");
    } else {
      this.content = content;
    }
  }
}
