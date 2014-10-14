/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.generation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Stack;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.STMessage;

/**
 * The ClassGenerator class implements logic that generates a source code file from string
 * templates.
 * 
 * @author Andrei Tatarnikov
 */

public final class ClassGenerator implements IClassGenerator {
  private static final STErrorListener errorListener = new STErrorListener() {
    private void trace(String s) {
      System.err.println(s);
    }

    @Override
    public void compileTimeError(STMessage msg) {
      trace("Run-time error: " + msg);
    }

    @Override
    public void runTimeError(STMessage msg) {
      trace("Internal error: " + msg);
    }

    @Override
    public void IOError(STMessage msg) {
      trace("Compile-time error: " + msg);
    }

    @Override
    public void internalError(STMessage msg) {
      trace("I/O error: " + msg);
    }
  };

  private final String outputFile;
  private final String[] templateGroupFiles;
  private final ITemplateBuilder templateBuilder;

  /**
   * Creates a class code generator parameterized with a hierarchy template groups, with a builder
   * that will initialized the class code template and with the full name to the target output file.
   * 
   * @param outputFile The full name of the target output file.
   * @param templateGroupFiles List of template group files. Important: the order is from the root
   *        of the hierarchy to child groups.
   * @param templateBuilder Builder that is responsible for initialization of the template.
   */

  public ClassGenerator(String outputFile, String[] templateGroupFiles,
      ITemplateBuilder templateBuilder) {
    this.outputFile = outputFile;
    this.templateGroupFiles = templateGroupFiles;
    this.templateBuilder = templateBuilder;
  }

  /**
   * Generates the target fail.
   * 
   * @throws IOException It is raised if the methods fails to create the target file.
   */

  @Override
  public void generate() throws IOException {
    final STGroup group = loadTemplateGroups();
    final ST template = templateBuilder.build(group);

    saveTemplate(template);
  }

  /**
   * Loads template groups from the file system and organizes then into a hierarchy.
   * 
   * @return A hierarchy of template groups.
   */

  private STGroup loadTemplateGroups() {
    final Stack<STGroup> groupStack = new Stack<STGroup>();
    for (String groupFile : templateGroupFiles) {
      final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      final URL groupUrl = classLoader.getResource(groupFile);

      final STGroup group = new STGroupFile(groupUrl, "UTF-8", '<', '>');
      if (!groupStack.empty()) {
        final STGroup parentGroup = groupStack.peek();
        group.importTemplates(parentGroup);
      }

      groupStack.push(group);
    }

    return groupStack.peek();
  }

  /**
   * Create a file and saves an initialized template to it.
   * 
   * @param template An initialized file template.
   * @throws IOException It is raised if the methods fails to create the target file.
   */

  private void saveTemplate(ST template) throws IOException {
    final File file = new File(outputFile);

    if (!file.exists()) {
      final File parent = file.getParentFile();
      if (!parent.exists()) {
        parent.mkdirs();
      }
      file.createNewFile();
    }

    template.write(file, errorListener);
  }
}
