/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test;

import static ru.ispras.microtesk.utils.InvariantChecks.checkNotNull;
import static ru.ispras.microtesk.utils.InvariantChecks.checkGreaterThanZero;
import static ru.ispras.microtesk.utils.InvariantChecks.checkGreaterOrEqZero;
import static ru.ispras.microtesk.utils.PrintingUtils.trace;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.memory.MemoryAllocator;
import ru.ispras.microtesk.model.api.type.Type;

public final class DataManager {
  private final MemoryMap memoryMap;
  private boolean isInitialized;

  private String text;
  private String target;
  private int addressableSize;

  private MemoryAllocator allocator;
  private List<String> labels;

  public DataManager() {
    this.memoryMap = new MemoryMap();
    this.isInitialized = false;

    this.text = "";
    this.target = "";
    this.addressableSize = 0;

    this.allocator = null;
    this.labels = null;
  }

  public void init(String text, String target, int addressableSize) {
    checkNotNull(text);
    checkNotNull(target);

    if (isInitialized) {
      throw new IllegalStateException("DataManager is already initialized!");
    }

    this.isInitialized = true;

    this.text = text;
    this.target = target;
    this.addressableSize = addressableSize;

    final Memory memory = Memory.getMemory(target);
    this.allocator = memory.newAllocator(addressableSize);
  }
  
  public String getText() {
    if (!isInitialized) {
      return null;
    }

    return text;
  }

  public void defineType(String id, String text, String typeName, int[] typeArgs) {
    checkNotNull(id);
    checkNotNull(text);
    checkNotNull(typeName);
    checkNotNull(typeArgs);
    checkInitialized();

    final Type type = Type.typeOf(typeName, typeArgs);
    trace("Defining %s as %s ('%s')...", type, id, text);
  }

  public void defineSpace(String id, String text, BigInteger fillWith) {
    checkNotNull(id);
    checkNotNull(text);
    checkInitialized();
   
    trace("Defining space as %s ('%s') filled with %x...", id, text, fillWith);
  }
  
  public void defineAsciiString(String id, String text, boolean zeroTerm) {
    checkNotNull(id);
    checkNotNull(text);
    checkInitialized();

    trace("Defining %snull-terminated ASCII string as %s ('%s')...", zeroTerm ? "" : "not ", id, text);
  }

  public void addLabel(String id) {
    checkNotNull(id);
    checkInitialized();

    trace("Label %s", id);
    
    if (null == labels) {
      labels = new ArrayList<>();
    }

    labels.add(id);
  }

  public int resolveLabel(String id, int index) {
    checkNotNull(id);
    checkGreaterOrEqZero(index);

    checkInitialized();
    trace("Resolving label reference %s(%d)", id, index); 

    return memoryMap.resolve(id, index);
  }

  public void addData(String id, BigInteger[] values) {
    checkInitialized();

    /*
    # for each -> Type.valueOf(value)
    # address = allocator.allocate (values)
    # size = sizeof(value) - in units
    # if label -> save to memoryMap: label, address, size
    */  

  }

  public void addSpace(int length) {
    checkGreaterThanZero(length);
    checkInitialized();
    
  }

  public void addAsciiStrings(boolean zeroTerm, String[] strings) {
    checkInitialized();
  }
  
  private void checkInitialized() {
    if (!isInitialized) {
      throw new IllegalStateException("DataManager is not initialized!");
    }
  }
}

final class MemoryMap {
  private static class Pointer {
    public final int address;
    public final int sizeInAddresableUnits;

    public Pointer(int address, int sizeInAddresableUnits) {
      this.address = address;
      this.sizeInAddresableUnits = sizeInAddresableUnits;
    }
  }

  private final Map<String, Pointer> labels = new HashMap<String, Pointer>();

  public void addLabel(String label, int address, int sizeInAddresableUnits) {
    checkNotNull(label);
    checkGreaterOrEqZero(address);
    checkGreaterThanZero(sizeInAddresableUnits);

    labels.put(label, new Pointer(address, sizeInAddresableUnits));
  }

  public int resolve(String label) {
    return resolve(label, 0);
  }

  public int resolve(String label, int index) {
    checkNotNull(label);
    checkGreaterOrEqZero(index);

    final Pointer pointer = labels.get(label);
    if (null == pointer) {
      throw new IllegalArgumentException(String.format("The %s label is not defined.", label));
    }

    return pointer.address + pointer.sizeInAddresableUnits * index;
  }
}
