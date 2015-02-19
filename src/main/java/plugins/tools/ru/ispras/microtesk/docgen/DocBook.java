package ru.ispras.microtesk.docgen;

import java.util.HashMap;
import java.util.Map;

public class DocBook extends XML {

  public DocBook(String bookName) throws FormatterException {

    super("book", XmlElementType.INTERMEDIATE, new HashMap<String, String>() {
      private static final long serialVersionUID = -6184766306463880507L;
      {
        put("xml:id", "book_root");
      }
    });

    XML bookTitle = new XML("title", XmlElementType.LEAF, null);
    bookTitle.assignContent(bookName);
    super.addSubEntry(bookTitle);
  }

  public DocBook(XML xml) throws FormatterException {
    super(xml.getTag(), xml.getType(), xml.getAttributes());
    this.setScope(xml.getScope());
  }

  public XML addChapter(String title, String id, XmlScope scope) throws FormatterException {
    Map<String, String> attr = new HashMap<>();
    attr.put("xml:id", id);
    XML chapterTitle = new XML("title", XmlElementType.LEAF, null);
    chapterTitle.assignContent(title);
    XML chapter = new XML("chapter", XmlElementType.INTERMEDIATE, attr);
    chapter.addSubEntry(chapterTitle);
    chapter.setScope(scope);
    return super.addSubEntry(chapter);
  }
}
