/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ModelMain.java, Mar 15, 2013 3:33:21 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.debug;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

/**
 * The TemplateRunner class is needed to be able to run and debug test templates in Eclipse.  
 * 
 * @author Andrei Tatarnikov
 */

public final class TemplateRunner
{
    private TemplateRunner() {}

    private static void runTemplate(List<String> argv)
    {
        final ScriptingContainer container = new ScriptingContainer();

        final String scriptsPath = 
            String.format("%s/dist/libs/ruby/template_processor.rb", System.getProperty("user.dir"));

        container.setArgv(argv.toArray(new String[argv.size()]));
        container.runScriptlet(PathType.ABSOLUTE, scriptsPath);
    }

    public static void main(String[] args)
    {
        if (args.length < 3)
        {
            System.out.println("The following arguments are required: <model file>, <design name>, <template file name>");
            return;
        }

        final String       models = String.format("%s/%s", System.getProperty("user.dir"), args[0]);
        final String   designName = args[1];
        final String templatePath = String.format("%s/%s", System.getProperty("user.dir"), args[2]);

        if (!new File(models).exists())
        {
            System.out.printf("The %s file storing models does not exists.", templatePath);
            return; 
        }

        if (!new File(templatePath).exists())
        {
            System.out.printf("The %s template file does not exists.", templatePath);
            return; 
        }

        final List<String> argv = new ArrayList<String>();

        argv.add(models);
        argv.add(designName);
        argv.add(templatePath);

        for (int index = 3; index < args.length; ++index)
            argv.add(args[index]);

        runTemplate(argv);
    }
}
