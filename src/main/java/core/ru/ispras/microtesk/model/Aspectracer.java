/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model;


import com.unitesk.aspectrace.TraceMessage;
import com.unitesk.aspectrace.TraceNode;
import com.unitesk.aspectrace.Tracer;
import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.util.InvariantChecks;

import java.io.File;
import java.io.IOException;

import static ru.ispras.microtesk.model.Execution.CALL_STACK;


/**
 * The {@link Aspectracer} class is responsible for printing Aspectrace traces.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Aspectracer {
    private static final String FILE_PREFIX = "MicroTESK";
    private static final String FILE_EXTENSION = "atrace";

    private final String filePath;
    private final String filePrefix;
    private final String fileExtension;

    private int fileCount;

    private static Aspectracer instance = null;
    private static boolean enabled = false;

    public static void initialize(final String filePath, final String filePrefix) {
        // TODO: InvariantChecks.checkTrue(null == instance);
        if (instance != null)
            Logger.message("Tracer is already initialized.", "");
        instance = new Aspectracer(filePath, null != filePrefix ? filePrefix : FILE_PREFIX);
    }

    public static void shutdown() {
        instance = null;
    }

    public static boolean isEnabled() {
        return null != instance && enabled;
    }

    public static void setEnabled(final boolean value) {
        enabled = value;
    }

    public static String createFile() throws IOException {
        if (instance != null) {
            return instance.create();
        }
        return null;
    }

    public static void closeFile() {
        if (instance != null) {
            instance.close();
        }
    }

    public static void addInstrPath(final String mark) {
        if (instance != null) {
            instance.printToTrace(mark);
        }
    }

    private Aspectracer(final String filePath, final String filePrefix) {
        InvariantChecks.checkNotNull(filePath);
        InvariantChecks.checkNotNull(filePrefix);
        this.filePath = filePath;
        this.filePrefix = filePrefix;
        this.fileExtension = FILE_EXTENSION;
        this.fileCount = 0;
    }

    private String create() throws IOException {
        close();
        final String fileName = String.format(
                "%s_%04d.%s",
                filePrefix,
                fileCount++,
                fileExtension
        );

        final File file = new File(filePath, fileName);
        final String fileFullName = file.getAbsolutePath();

        Tracer.getInstance().addXmlTrace(new File(fileFullName));
        return fileName;
    }

    private void close() {
        Tracer.getInstance().endTrace();
    }

    private void printToTrace(final String mark) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < CALL_STACK.size() - 1 ; ++i) {
            buf.append(CALL_STACK.get(i).getName() + ".");
        }
        buf.append(CALL_STACK.get(CALL_STACK.size() - 1).getName());
        if (mark != null)
        {
            buf.append(".");
            buf.append(mark);
        }
        Tracer.traceMessage(new TraceMessage("coverage", new TraceNode("element","aspect", "coverage", "name", buf.toString(), "cs", "instruction paths")));
    }
}
