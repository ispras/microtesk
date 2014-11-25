/*
 * Copyright 2013 ISP RAS (http://www.ispras.ru), UniTESK Lab (http://www.unitesk.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.ispras.microtesk.model.api.mmu.policy;

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

