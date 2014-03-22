/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * UserTestSituationLoader.java, Mar 22, 2014 1:57:20 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ru.ispras.microtesk.translator.simnml.ir.IR;

public final class UserTestSituationLoader
{
    private final IR             ir;
    private final String testSitDir;
    private final String     outDir;

    private static final String ERR_TEST_SIT_DIR_DOES_NOT_EXIST =
        "The \"%s\" folder does not exist. No user-defied situations will be included.%n";

    private static final String ERR_FAILED_TO_COPY_DIR =
        "Failed to copy \"%s\" to \"%s\". Reason: %s%n";

    public UserTestSituationLoader(IR ir, String testSitDir, String outDir)
    {
        this.ir = ir;
        this.testSitDir = testSitDir;
        this.outDir = outDir;
    }

    public void load()
    {
        // No user-defined situations provided.
        if (null == testSitDir)
            return;

        final File fileTestSitDir = new File(testSitDir); 
        if (!fileTestSitDir.exists() || !fileTestSitDir.isDirectory())
        {
            System.err.printf(ERR_TEST_SIT_DIR_DOES_NOT_EXIST, testSitDir);
            return;
        }

        System.out.println("Including " + testSitDir + "...");
        
        // Copy Resources (XML constraints)
        copyDirectory(testSitDir + "/resources", outDir + "/resources");
        // Copy Java Code
        copyDirectory(testSitDir + "/java", outDir);
    }

    private static void copyDirectory(String source, String target)
    {
        final File sourceFile = new File(source);
        final File targetFile = new File(target);

        if (!sourceFile.exists())
            return;

        try
        {
            copyDirectory(sourceFile, targetFile);
        }
        catch (IOException e)
        {
            System.err.printf(ERR_FAILED_TO_COPY_DIR, source, target, e.getMessage());
        }
    }

    private static void copyDirectory(File source, File target) throws IOException
    {
        if (null == source)
            throw new NullPointerException();

        if (null == target)
            throw new NullPointerException();

        if (source.isDirectory())
        {
            if (!target.exists())
                target.mkdir();

            for (String child : source.list())
                copyDirectory(new File(source, child), new File(target, child));
        }
        else
        {
            copyFile(source, target);
        }
    }

    private static void copyFile(File source, File target) throws IOException
    {
        if (null == source)
            throw new NullPointerException();

        if (null == target)
            throw new NullPointerException();

        InputStream is = null;
        OutputStream os = null;

        try
        {
            is = new FileInputStream(source);
            os = new FileOutputStream(target);
            
            final byte[] buffer = new byte[1024];

            int length;
            while ((length = is.read(buffer)) > 0)
                os.write(buffer, 0, length);
        }
        finally
        {
            if (null != is)
                is.close();

            if (null != os)
                os.close();
        }
    }
}
