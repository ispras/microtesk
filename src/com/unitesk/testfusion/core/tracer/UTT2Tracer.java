/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: UTT2Tracer.java,v 1.1 2009/12/15 17:33:53 vorobyev Exp $
 */

package com.unitesk.testfusion.core.tracer;

import com.unitesk.aspectrace.Tracer;
import com.unitesk.aspectrace.coverage.*;
import com.unitesk.aspectrace.test.TestTracer;
import com.unitesk.coverage.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class UTT2Tracer
{
	protected final static String TEST_ASPECT_NAME = "test";
	
	public UTT2Tracer()
	{
	}

	public void startTrace(String filename)
	{
		// Precondition
		if(filename.equals("") ) { throw new IllegalStateException("Filename is empty"); }
	
		try 
		{
			Tracer.getInstance().addXmlTrace(filename);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		addTraceStartTag();
	}
	
	public void endTrace()
	{
		addTraceEndTag();
		
		Tracer.getInstance().endTrace();
	}
	
	protected void addTraceStartTag()
	{
		TestTracer.traceTestStart(TEST_ASPECT_NAME);
	}
	
	protected void addTraceEndTag()
	{
		TestTracer.traceTestEnd(TEST_ASPECT_NAME, true);
	}
	
	public void traceCoverageElement(String situationDescription)
	{
		SituationDescription sd = new SituationDescription();
		sd.description = situationDescription;
		
		CoverageTracer.traceCoverageElement(sd.getElement() );
	}

	@Coverage(comb=CoverageCombinator.UNION)
	public static class SituationDescription extends CoverageBase
	{
		public String description;
	}
}