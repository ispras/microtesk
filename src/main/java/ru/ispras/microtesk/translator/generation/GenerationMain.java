package ru.ispras.microtesk.translator.generation;

import java.io.IOException;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

public class GenerationMain
{
    private static final String processorName = "TESTV4";

    private static final String  rootOutput = String.format("%s/%s", getModelOutDir(DEFAULT_OUTDIR), processorName);
    private static final String rootPackage = String.format("%s.%s", MODEL_PACKAGE, processorName);
    private static final String      output = String.format("%s/%s.java", rootOutput, processorName);

    private static class Builder implements ITemplateBuilder
    {
        @Override
        public ST build(STGroup group)
        {
            final ST tHeader = group.getInstanceOf("header");
            //tHeader.add("file", "design.nml");

            final ST tPackage = group.getInstanceOf("package");
            tPackage.add("pack", rootPackage);

            final ST tImports = group.getInstanceOf("imports");
            tImports.add("names", new String[] { "org.stringtemplate.v4.ST", "org.stringtemplate.v4.STGroup" });

            // modificators, name, base, interfaces, members
            final ST tClass = group.getInstanceOf("class");

            tClass.add("modifs",   "public");
            tClass.add("modifs",   "abstract");

            tClass.add("name",     "TestClass");
            tClass.add("ext",      "MyBaseClass");
            tClass.add("impls",    "IMyBaseClass");
            tClass.add("impls",    "IMyBaseClass2");

            final ST tClass2 = group.getInstanceOf("class");

            tClass2.add("modifs",  "public");
            tClass2.add("modifs",  "abstract");

            tClass2.add("name",    "TestClass");
            tClass2.add("ext",     "MyBaseClass");
            tClass2.add("impls",   "IMyBaseClass");
            tClass2.add("impls",   "IMyBaseClass2");

            tClass.add("members", tClass2);

            return tHeader;
        }
    }

    public static void main(String[] args)
    {
        final IClassGenerator cg = new ClassGenerator
        (
            output,
            new String[] { "stg/JavaCommon.stg" },
            new Builder()
        );

        try
        {
            cg.generate();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
