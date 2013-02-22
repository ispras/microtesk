/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * FormatAnalyzer.java, Feb 11, 2013 12:14:35 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.modeop;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FormatAnalyzer
{
    private static final String SPEC_FORMAT_HEAD = "[%][\\d]?[";
    private static final String SPEC_FORMAT_TAIL = "]";

    private static class FormatSpec
    {
        private final FormatKind kind;
        private final String regExpToken;

        public FormatSpec(FormatKind kind, String regExpToken)
        {
            this.kind = kind;
            this.regExpToken = regExpToken;
        }

        public FormatKind getKind()
        { 
            return kind;
        }

        public String getRegExpToken()
        {
            return regExpToken;
        }

        public String getRegExp()
        {
            return SPEC_FORMAT_HEAD + regExpToken + SPEC_FORMAT_TAIL;
        }
    }

    private static final FormatSpec[] SUPPORTED_SPECS =
    { 
        new FormatSpec(FormatKind.DECIMAL,     "d"),
        new FormatSpec(FormatKind.BINARY,      "b"),
        new FormatSpec(FormatKind.HEXADECIMAL, "x"),
        new FormatSpec(FormatKind.STRING,      "s"),
    };

    private static final String GENERAL_SPEC_REGEXP = 
        SPEC_FORMAT_HEAD + getAllSpecTokens() + SPEC_FORMAT_TAIL;

    private static String getAllSpecTokens()
    {
        final StringBuffer sb = new StringBuffer();

        for (FormatSpec spec : SUPPORTED_SPECS)
        {
            if (0 != sb.length()) sb.append('|');
            sb.append(spec.getRegExpToken());
        }

        return sb.toString();
    }

    private final String formatString;

    public FormatAnalyzer(String formatString)
    {
        this.formatString = formatString;
    }

    public List<FormatKind> getFormatKinds()
    {
        final List<FormatKind> result = new ArrayList<FormatKind>();

        final Matcher matcher =
            Pattern.compile(GENERAL_SPEC_REGEXP).matcher(formatString);

        while (matcher.find())
        {
            final String foundSpecText = matcher.group();
            result.add(getFormatKind(foundSpecText));
        }

        return result;
    }

    private FormatKind getFormatKind(String specText)
    {
        for (FormatSpec spec : SUPPORTED_SPECS)
        {
            final Matcher matcher =
                Pattern.compile(spec.getRegExp()).matcher(specText);

            if (matcher.matches())
                return spec.getKind();
        }

        assert false : "Not found!";
        return null;
    }
}
