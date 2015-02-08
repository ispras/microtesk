package ru.ispras.microtesk.docgen;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Stack;

import ru.ispras.microtesk.translator.simnml.ir.IrVisitor;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveOR;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Shortcut;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetString;
import ru.ispras.microtesk.translator.simnml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

/**
 * TODO: Eliminate code duplication in methods.
 * @author Platon
 *
 */
public class XmlDocumenter implements IrVisitor {

	
	public XML xml;
	public Stack<XML> currentScope;
	
	public XmlDocumenter(XML xml, String modelName) throws IOException
	{
		this.xml = xml;
		currentScope = new Stack<XML>();
	}
	
	@Override
	public void onResourcesBegin() {
		XML resources = new XML("resources", XmlElementType.INTERMEDIATE, null);
		
		try {
			currentScope.push(resources);
			xml.addSubEntry(resources);
		} catch (FormatterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onResourcesEnd() {
		currentScope.pop();
	}

	@Override
	public void onLetConstant(LetConstant let) {
		checkScope("resources");
		
		XML letConstant = new XML("let_constant", XmlElementType.LEAF, null);
		try {
			letConstant.assignContent(let.getName());
			currentScope.peek().addSubEntry(letConstant);
		} catch (FormatterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onLetString(LetString let) {
		checkScope("resources");
		
		XML letString = new XML("let_string", XmlElementType.LEAF, null);
		try {
			letString.assignContent(let.getName());
			currentScope.peek().addSubEntry(letString);
		} catch (FormatterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onLetLabel(LetLabel let) {
		checkScope("resources");
		
		XML letLabel = new XML("let_label", XmlElementType.LEAF, null);
		try {
			letLabel.assignContent(let.getName());
			currentScope.peek().addSubEntry(letLabel);
		} catch (FormatterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onType(String name, Type type) {
		checkScope("resources");

		XML proc_type = new XML("type", XmlElementType.LEAF, null);
		try {
			proc_type.assignContent(name);
			currentScope.peek().addSubEntry(proc_type);
		} catch (FormatterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onMemory(String name, MemoryExpr memory) {
		checkScope("resources");

		XML proc_type = new XML("memory", XmlElementType.LEAF, null);
		try {
			proc_type.assignContent(name);
			currentScope.peek().addSubEntry(proc_type);
		} catch (FormatterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onPrimitiveBegin(Primitive item) {
		XML proc_type = new XML("primitive", XmlElementType.LEAF, null);
		try {
			proc_type.assignContent(item.getName());
			xml.addSubEntry(proc_type);
		} catch (FormatterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onPrimitiveEnd(Primitive item) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAlternativeBegin(PrimitiveOR orRule, Primitive item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAlternativeEnd(PrimitiveOR orRule, Primitive item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onArgumentBegin(PrimitiveAND andRule, String argName,
			Primitive argType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onArgumentEnd(PrimitiveAND andRule, String argName,
			Primitive argType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAttributeBegin(PrimitiveAND andRule, Attribute attr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAttributeEnd(PrimitiveAND andRule, Attribute attr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatement(PrimitiveAND andRule, Attribute attr, Statement stmt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onShortcutBegin(PrimitiveAND andRule, Shortcut shortcut) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onShortcutEnd(PrimitiveAND andRule, Shortcut shortcut) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * TODO: rewrite with XmlScopes.
	 * @param scope: the scope that is intended to write.
	 */
	@Deprecated
	private void checkScope(String scope)
	{
		if (currentScope.peek().tag != scope)
		{
			try {
				throw new Exception("Bad model structure");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
