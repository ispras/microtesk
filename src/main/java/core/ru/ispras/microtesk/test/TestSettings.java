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

import ru.ispras.microtesk.SysUtils;

public final class TestSettings {
  // Settings passed from a template
  private static String indentToken    = "\t";
  private static String commentToken   = "//";
  private static String separatorToken = "=";
  private static String originFormat   = ".org 0x%x";
  private static String alignFormat    = ".align %d";

  // Settings from command line and configuration file
  private static String outDir              = SysUtils.getHomeDir();
  private static int branchExecutionLimit   = 100;
  private static String codeFileExtension   = ".asm";
  private static String codeFilePrefix      = "test";
  private static String dataFileExtension   = ".dat";
  private static String dataFilePrefix      = codeFilePrefix;
  private static String exceptionFilePrefix = codeFilePrefix + "_except"; 
  private static int programLengthLimit     = 1000;
  private static int traceLengthLimit       = 1000;
  private static boolean commentsDebug      = false;
  private static boolean commentsEnabled    = false;
  private static boolean tarmacLog          = false;
  private static boolean selfChecks         = false;

  public static String getIndentToken() {
    return indentToken;
  }

  public static void setIndentToken(final String indentToken) {
    TestSettings.indentToken = indentToken;
  }

  public static String getCommentToken() {
    return commentToken;
  }

  public static void setCommentToken(final String commentToken) {
    TestSettings.commentToken = commentToken;
  }

  public static String getSeparatorToken() {
    return separatorToken;
  }

  public static void setSeparatorToken(final String separatorToken) {
    TestSettings.separatorToken = separatorToken;
  }

  public static String getOriginFormat() {
    return originFormat;
  }

  public static void setOriginFormat(final String originFormat) {
    TestSettings.originFormat = originFormat;
  }

  public static String getAlignFormat() {
    return alignFormat;
  }

  public static void setAlignFormat(final String alignFormat) {
    TestSettings.alignFormat = alignFormat;
  }

  public static String getOutDir() {
    return outDir;
  }

  public static void setOutDir(final String outDir) {
    TestSettings.outDir = outDir;
  }

  public static int getBranchExecutionLimit() {
    return branchExecutionLimit;
  }

  public static void setBranchExecutionLimit(final int branchExecutionLimit) {
    TestSettings.branchExecutionLimit = branchExecutionLimit;
  }

  public static String getCodeFileExtension() {
    return codeFileExtension;
  }

  public static void setCodeFileExtension(final String codeFileExtension) {
    TestSettings.codeFileExtension = codeFileExtension;
  }

  public static String getCodeFilePrefix() {
    return codeFilePrefix;
  }

  public static void setCodeFilePrefix(final String codeFilePrefix) {
    TestSettings.codeFilePrefix = codeFilePrefix;
  }

  public static String getDataFileExtension() {
    return dataFileExtension;
  }

  public static void setDataFileExtension(final String dataFileExtension) {
    TestSettings.dataFileExtension = dataFileExtension;
  }

  public static String getDataFilePrefix() {
    return dataFilePrefix;
  }

  public static void setDataFilePrefix(final String dataFilePrefix) {
    TestSettings.dataFilePrefix = dataFilePrefix;
  }

  public static String getExceptionFilePrefix() {
    return exceptionFilePrefix;
  }

  public static void setExceptionFilePrefix(final String exceptionFilePrefix) {
    TestSettings.exceptionFilePrefix = exceptionFilePrefix;
  }

  public static int getProgramLengthLimit() {
    return programLengthLimit;
  }

  public static void setProgramLengthLimit(final int programLengthLimit) {
    TestSettings.programLengthLimit = programLengthLimit;
  }

  public static int getTraceLengthLimit() {
    return traceLengthLimit;
  }

  public static void setTraceLengthLimit(final int traceLengthLimit) {
    TestSettings.traceLengthLimit = traceLengthLimit;
  }

  public static boolean isCommentsDebug() {
    return commentsDebug;
  }

  public static void setCommentsDebug(final boolean commentsDebug) {
    TestSettings.commentsDebug = commentsDebug;
  }

  public static boolean isCommentsEnabled() {
    return commentsEnabled;
  }

  public static void setCommentsEnabled(final boolean commentsEnabled) {
    TestSettings.commentsEnabled = commentsEnabled;
  }

  public static boolean isTarmacLog() {
    return tarmacLog;
  }

  public static void setTarmacLog(final boolean tarmacLog) {
    TestSettings.tarmacLog = tarmacLog;
  }

  public static boolean isSelfChecks() {
    return selfChecks;
  }

  public static void setSelfChecks(final boolean selfChecks) {
    TestSettings.selfChecks = selfChecks;
  }
}
