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
    
    public static void runTemplate(String designName, String templatePath)
    {
        final ScriptingContainer container = new ScriptingContainer();

        final String scriptsPath =
            System.getProperty("user.dir") + "/parse_templates.rb";

        container.setArgv(new String[] { designName, templatePath });
        container.runScriptlet(PathType.ABSOLUTE, scriptsPath);
    }

    public static void main(String[] args)
    {
        final String TEMPLATE_DIR_FORMAT =
            "%s/src/ru/ispras/microtesk/templates/templates/";

        if (args.length < 2)
        {
            System.out.println("The following arguments are required: <design name>, <template file name>");
            return;
        }

        final String designName = args[0];
        final String templateName = args[1];

        final String templateDir = String.format(TEMPLATE_DIR_FORMAT, System.getProperty("user.dir"));
        final String templatePath = templateDir + templateName;

        if (!new File(templatePath).exists())
        {
            System.out.printf("The %s template file does not exists at %s", templateName, templateDir);
            return; 
        }

        runTemplate(designName, templatePath);
    }
}
