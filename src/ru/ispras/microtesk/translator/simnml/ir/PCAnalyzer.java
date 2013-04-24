/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * PCAnalyzer.java, Apr 24, 2013 3:47:03 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.model.api.memory.EMemoryKind;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.ir.expression2.EExprKind;
import ru.ispras.microtesk.translator.simnml.ir.expression2.Expr;
import ru.ispras.microtesk.translator.simnml.ir.expression2.LocationExprFactory;
import ru.ispras.microtesk.translator.simnml.ir.expression2.LocationInfo;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.simnml.ir.shared.MemoryExpr;

public final class PCAnalyzer
{
    private final LocationExprFactory locationFactory;
    private final IR ir;

    private final List<LocationInfo> destLocations;
    private List<LocationInfo> srcLocations;

    public PCAnalyzer(LocationExprFactory locationFactory, IR ir)
    {
        this.locationFactory = locationFactory;
        this.ir = ir;

        this.destLocations = new ArrayList<LocationInfo>();
        this.locationFactory.setLog(destLocations);

        this.srcLocations = null;
    }
    
    public void startTrackingSource()
    {
        if (!isPCAssignment()) return;
        
        srcLocations = new ArrayList<LocationInfo>();
        locationFactory.setLog(srcLocations);
    }
    
    public int getControlTransferIndex()
    {
        if (null == srcLocations)
            return -1;

        for (LocationInfo li : srcLocations)
        {
            if (li.getSymbolKind() == ESymbolKind.ARGUMENT)
                return 1;

            if (li.getSymbolKind() == ESymbolKind.MEMORY && !isPC(li))
                return 1;
        } 

        return 0;
    }

    public void finalize()
    {
        locationFactory.resetLog();
    }
    
    private boolean isPCAssignment()
    {
        for (LocationInfo li : destLocations)
            if (isPC(li))
                return true;

        return false;
    }
    
    private boolean isPC(LocationInfo locationInfo)
    {
        assert null != locationInfo;
        
        if (!isRegisterLocation(locationInfo))
            return false;
        
        if (isExplicitPCAccess(locationInfo))
            return true;
        
        return isLabelledAsPC(locationInfo);
    }
    
    private boolean isRegisterLocation(LocationInfo locationInfo)
    {
        if (locationInfo.getSymbolKind() != ESymbolKind.MEMORY)
            return false;

        if (!ir.getMemory().containsKey(locationInfo.getName()))
            return false;

        final MemoryExpr memory =
            ir.getMemory().get(locationInfo.getName());

        if (memory.getKind() != EMemoryKind.REG)
            return false;

        return true;
    }
    
    private boolean isExplicitPCAccess(LocationInfo locationInfo)
    {
        if (!locationInfo.getName().equals("PC"))
            return false;

        return true;
    }
    
    private boolean isLabelledAsPC(LocationInfo locationInfo)
    {
        if (!ir.getLabels().containsKey("PC"))
            return false;

        final LetLabel label =
            ir.getLabels().get("PC");

        if (!label.getMemoryName().equals(locationInfo.getName()))
            return false;

        final int locationIndex;

        final Expr indexExpr = locationInfo.getIndex();
        if (null != indexExpr)
        {
            if (indexExpr.getKind() != EExprKind.JAVA_STATIC)
                return false;

            if (null == indexExpr.getValue())
                return false;

            locationIndex =
                ((Number) indexExpr.getValue()).intValue();
        }
        else
        {
            locationIndex = 0; 
        }

        return label.getIndex() == locationIndex;
    }
}
