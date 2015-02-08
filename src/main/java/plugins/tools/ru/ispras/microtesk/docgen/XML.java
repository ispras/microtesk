package ru.ispras.microtesk.docgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XML {
	
	//TODO: Encapsulate fields
	public Map<String, String> attributes;
	public String tag;
	public String content;
	public XmlElementType type;
	public List<XML> subXmls;
	
	public XML(String tag, XmlElementType type, Map<String, String> attributes)
	{
		this.type = type;
		this.attributes = attributes;
		this.tag = tag;
		this.subXmls = new ArrayList<XML>();
	}
	
	public void addSubEntry(XML xml) throws FormatterException
	{
		if (type != XmlElementType.INTERMEDIATE)
		{
			throw new FormatterException("Unable to add subentry");
		} else {
			subXmls.add(xml);
		}
	}
	
	public void assignContent(String content) throws FormatterException
	{
		if (type != XmlElementType.LEAF)
		{
			throw new FormatterException("Unable to add subentry");
		} else {
			this.content = content;
		}
	}
}


