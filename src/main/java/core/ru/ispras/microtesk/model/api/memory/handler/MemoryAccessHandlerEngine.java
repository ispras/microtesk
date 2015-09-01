/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.memory.handler;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.model.api.memory.MemoryStorage;

public final class MemoryAccessHandlerEngine implements MemoryAccessHandler {
  private static final MemoryAccessHandlerEngine handlerEngine = new MemoryAccessHandlerEngine();
  private static boolean isHandlingEnabled = false;

  public static MemoryAccessHandler getGlobalHandler() {
    return isHandlingEnabled ? getHandlerEngine() : null;
  }

  private static MemoryAccessHandlerEngine getHandlerEngine() {
    return handlerEngine;
  }

  public static void setHandlingEnabled(boolean value) {
    isHandlingEnabled = value;
  }
  
  private final Map<MemoryStorage, MemoryAccessHandler> handlers;

  MemoryAccessHandlerEngine() {
    handlers = new HashMap<MemoryStorage, MemoryAccessHandler>();
  }

  void registerHandler(MemoryStorage storage, MemoryAccessHandler handler) {
    checkNotNull(storage);
    checkNotNull(handler);
    handlers.put(storage, handler);
  }

  @Override
  public List<MemoryRegion> onLoad(List<MemoryRegion> regions) {
    checkNotNull(regions);
    trace("onLoad:", regions);
    
    
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void onStore(List<MemoryRegion> regions) {
    checkNotNull(regions);
    trace("onStore:", regions);

  }
  
  private void trace(String message, List<MemoryRegion> regions) {
    final StringBuilder sb = new StringBuilder(message);
    
    boolean isFirst = true;
    for (MemoryRegion region : regions) {
      if (isFirst) {
        isFirst = false;
      } else {
        sb.append(";");
      }

      sb.append(String.format(
          " %s[%d]", region.getTarget().getId(), region.getIndex()));

      if (region.hasData()) {
        sb.append(String.format(" with data: %s", region.getData()));
      }
    }

    System.out.println(sb);
  }
}
