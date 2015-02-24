/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.docgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class XML {

  private Map<String, String> attributes;
  private Stack<XML> nestingStack;
  private String tag;
  private String content = null;
  private List<XML> subXmls;
  private XmlScope scope;

  public XML() {}

  public XML(String tag, Map<String, String> attributes) {
    this.attributes = attributes;
    this.tag = tag;
    this.subXmls = new ArrayList<XML>();
    nestingStack = new Stack<XML>();
    nestingStack.push(this);
  }
  
  private XML(String tag, Map<String, String> attributes, Stack<XML> nestingStack)
  {
    this.attributes = attributes;
    this.tag = tag;
    this.subXmls = new ArrayList<XML>();
    this.nestingStack = nestingStack;
  }

  public XML beginEntry(String tag, Map<String, String> attributes){
      XML xml = new XML(tag, attributes, this.nestingStack); 
      nestingStack.peek().subXmls.add(xml);
      nestingStack.push(xml);
      return xml;
  }
  
  public void closeEntry()
  {
    if (!nestingStack.isEmpty()){
      nestingStack.pop();
    }
  }

  public void assignContent(String content) {
      nestingStack.peek().content = content;
  }
  
  ///////////////////////////////////////////////////////////////////
  //Tag of node
  ///////////////////////////////////////////////////////////////////
  public String getTag() {
    return this.tag;
  }
  
  ///////////////////////////////////////////////////////////////////
  //Content of node
  ///////////////////////////////////////////////////////////////////
  public String getContent() {
    return this.content;
  } 
  
  ///////////////////////////////////////////////////////////////////
  //Sub-XMLs
  ///////////////////////////////////////////////////////////////////
  public List<XML> getSubXmls() {
    return this.subXmls;
  }
  
  ///////////////////////////////////////////////////////////////////
  //Properties for attributes
  ///////////////////////////////////////////////////////////////////
  public Map<String, String> getAttributes() {
    return this.attributes;
  }
  public XmlScope getScope() {
    return this.scope;
  }

  public void setScope(XmlScope scope) {
    this.scope = scope;
  }
}
