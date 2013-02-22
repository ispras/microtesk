package ru.ispras.microtesk.model.api.mmu;

import java.util.LinkedHashMap;
import java.util.Map;
 
public class LRU < K, V > extends LinkedHashMap < K, V > {
 
	private static final long serialVersionUID = 7739137878254489062L;
	// Maximum number of items in the cache
	
	private int linenumber; 
     
    public LRU(int linenumber) 
    {
    	super(linenumber+1, 1.0f, true);
        this.linenumber = linenumber;
    }
     
    protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
        return (linenumber() > this.linenumber);
        		}

	private int linenumber() {return 0;}
}

