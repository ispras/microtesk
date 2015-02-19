/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TemplateProduct {
  public static final class Builder {
    private Block pre;
    private Block post;
    private List<Block> main;

    public Builder() {
      this.pre = null;
      this.post = null;
      this.main = null;
    }

    public void setPre(Block block) {
      checkNotNull(block);

      if (null != this.pre) {
        throw new IllegalStateException("Pre is already initialized");
      }

      this.pre = block;
    }

    public void setPost(Block block) {
      checkNotNull(block);

      if (null != this.post) {
        throw new IllegalStateException("Post is already initialized");
      }
      
      this.post = block;
    }
    
    public void addToMain(Block block) {
      checkNotNull(block);

      if (block.isEmpty()) {
        return;
      }

      if (null == this.main) {
        this.main = new ArrayList<>();
      }
      this.main.add(block);
    }

    public TemplateProduct build() {
      return new TemplateProduct(pre, post, main);
    }
  }

  private final Block pre;
  private final Block post;
  private final List<Block> main;

  private TemplateProduct(Block pre, Block post, List<Block> main) {
    checkNotNull(pre);
    checkNotNull(post);
    checkNotNull(main);

    this.pre = pre;
    this.post = post;
    this.main = Collections.unmodifiableList(main);
  }

  public Block getPre() {
    return pre;
  }

  public Block getPost() {
    return post;
  }

  public List<Block> getMain() {
    return main;
  }
}
