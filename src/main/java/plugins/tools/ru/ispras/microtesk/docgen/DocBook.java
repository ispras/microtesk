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

import java.util.HashMap;
import java.util.Map;

public class DocBook {

  private XML xml;

  public DocBook(String bookName){
    this.xml = new XML("book", formAttribute("xml:id", "book_root"));
    xml.beginEntry("title", null);
    xml.assignContent(bookName);
    xml.closeEntry();
  }

  public static Map<String, String> formAttribute(String key, String value) {
    return new HashMap<String, String>() {
      private static final long serialVersionUID = 42;
      {
        put("xml:id", "book_root");
      }
    };
  }

  public XML beginChapter(String title, String id, XmlScope scope){
    xml.beginEntry("chapter", formAttribute("xml:id", id));
    xml.beginEntry("title", null);
    xml.assignContent(title);
    xml.closeEntry();
    return null;
  }
  
  public void closeChapter()
  {
    //TODO: add SCOPE CHECK!!!
    xml.closeEntry();
  }
  
  public XML addParagraph(String content)
  {
    xml.beginEntry("para", null);
    xml.assignContent(content);
    xml.closeEntry();
    return null;
  }
  
  public XML extractXML()
  {
    return this.xml;
  }
}
