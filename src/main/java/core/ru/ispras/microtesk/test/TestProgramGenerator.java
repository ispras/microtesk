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

package ru.ispras.microtesk.test;

public class TestProgramGenerator {
  private String modelName;
  private String fileName;
  private int randomSeed;
  private boolean isRandomSeedSet;

  public TestProgramGenerator() {
    this.modelName = "";
    this.fileName = "";
    this.randomSeed = 0;
    this.isRandomSeedSet = false;;
  }

  public void setModelName(String value) {
    modelName = value;
  }

  public void setRandomSeed(int value) {
    randomSeed = value;
    isRandomSeedSet = true;
  }

  public void setFileName(String value) {
    fileName = value;
  }

  public void generate(String... templateFiles) {
    System.out.println(modelName);
    System.out.println(fileName);
    System.out.println(isRandomSeedSet);
    System.out.println(randomSeed);
  }
}
