/*
    Copyright 2019 ISP RAS (http://www.ispras.ru)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package ru.ispras.microtesk.translator.mir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MirContext {
  public final String name;
  public final List<BasicBlock> blocks = new ArrayList<>();
  public final BasicBlock landingPad = new BasicBlock();

  public final List<MirTy> locals = new ArrayList<>();
  public final Map<Integer, LocalInfo> localInfo = new HashMap<>();

  public MirContext(final String name, final FuncTy signature) {
    this.name = name;

    locals.add(signature);
    locals.addAll(signature.params);

    localInfo.put(0, new LocalInfo(0, ".self"));
  }

  public MirBlock newBlock() {
    final MirBlock block = new MirBlock(this, new BasicBlock());
    blocks.add(block.bb);

    return block;
  }

  public FuncTy getSignature() {
    return (FuncTy) locals.get(0);
  }

  public void renameParameter(final int index, final String name) {
    if (index >= 0 && index < getSignature().params.size()) {
      localInfo.put(index + 1, new LocalInfo(index + 1, name));
    } else {
      throw new IndexOutOfBoundsException();
    }
  }
}

final class LocalInfo {
  public final int id;
  public final String name;

  public LocalInfo(final int id, final String name) {
    this.id = id;
    this.name = name;
  }
}
