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
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.location.LocationFactory;
import ru.ispras.microtesk.translator.simnml.ir.location.LocationAtom;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.simnml.ir.shared.MemoryExpr;

public final class PCAnalyzer
{
    private final LocationFactory  locationFactory;
    private final IR ir;

    private final List<LocationAtom> destLocations;
    private List<LocationAtom> srcLocations;

    public PCAnalyzer(LocationFactory locationFactory, IR ir)
    {
        this.locationFactory = locationFactory;
        this.ir = ir;

        this.destLocations = new ArrayList<LocationAtom>();
        this.locationFactory.setLog(destLocations);

        this.srcLocations = null;
    }
    
    public void startTrackingSource()
    {
        if (!isPCAssignment()) return;
        
        srcLocations = new ArrayList<LocationAtom>();
        locationFactory.setLog(srcLocations);
    }
    
    public int getControlTransferIndex()
    {
        if (null == srcLocations)
            return -1;

        for (LocationAtom location : srcLocations)
        {
            if (location.getSource().getSymbolKind() == ESymbolKind.ARGUMENT)
                return 1;

            if (location.getSource().getSymbolKind() == ESymbolKind.MEMORY && !isPC(location))
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
        for (LocationAtom location : destLocations)
            if (isPC(location))
                return true;

        return false;
    }
    
    private boolean isPC(LocationAtom location)
    {
        assert null != location;
        
        if (!isRegisterLocation(location))
            return false;
        
        if (isExplicitPCAccess(location))
            return true;
        
        return isLabelledAsPC(location);
    }
    
    private boolean isRegisterLocation(LocationAtom location)
    {
        if (location.getSource().getSymbolKind() != ESymbolKind.MEMORY)
            return false;

        assert location.getSource() instanceof LocationAtom.MemorySource; 

        final MemoryExpr memory =
           ((LocationAtom.MemorySource) location.getSource()).getMemory();

        if (memory.getKind() != EMemoryKind.REG)
            return false;

        return true;
    }
    
    private boolean isExplicitPCAccess(LocationAtom location)
    {
        if (!location.getName().equals("PC"))
            return false;

        return true;
    }
    
    private boolean isLabelledAsPC(LocationAtom location)
    {
        if (!ir.getLabels().containsKey("PC"))
            return false;

        final LetLabel label =
            ir.getLabels().get("PC");

        if (!label.getMemoryName().equals(location.getName()))
            return false;

        final int locationIndex;

        final Expr indexExpr = location.getIndex();
        
        if (null != indexExpr)
        {
            if (!indexExpr.getValueInfo().isConstant())
                return false;

            locationIndex = indexExpr.integerValue();
        }
        else
        {
            locationIndex = 0; 
        }

        return label.getIndex() == locationIndex;
    }
}
