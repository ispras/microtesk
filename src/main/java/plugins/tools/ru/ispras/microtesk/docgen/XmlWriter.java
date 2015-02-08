package ru.ispras.microtesk.docgen;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;

public class XmlWriter {

	private FileWriter writer;
	private int level = 0;
	
	public XmlWriter(FileWriter writer) throws IOException
	{
		this.writer = writer;
		writer.write("<?xml version=\"1.0\" encoding=\"unicode\"?>\n");
	}
	
	public void close() throws IOException
	{
		writer.flush();
		writer.close();
	}
	
	public void write(XML xml) throws IOException
	{
		if (xml.type == XmlElementType.INTERMEDIATE)
		{
			writer.write("<" + xml.tag + ">");
			level++;
			
			for (XML sub: xml.subXmls)
			{
				setToCurrentLevel(level);
				write(sub);
			}

			setToCurrentLevel(--level);
			
			writer.write("</" + xml.tag + ">");
			
		} else {
			writer.write("<" + xml.tag + ">" + xml.content + "</" + xml.tag + ">\n");
		}
	}
	
	private void setToCurrentLevel(int level) throws IOException
	{
		writer.write('\r');
		for (int i = 0; i < level; i++)
		{
		    writer.write('\t');	
		}
	}
}
