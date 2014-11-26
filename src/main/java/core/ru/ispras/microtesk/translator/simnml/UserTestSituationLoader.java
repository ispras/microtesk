/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.simnml;

// import java.io.File;
// import java.io.FileInputStream;
// import java.io.FileOutputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.OutputStream;
// import java.util.Map;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;

// import ru.ispras.microtesk.translator.simnml.ir.IR;
// import ru.ispras.microtesk.translator.simnml.ir.Initializer;
// import ru.ispras.microtesk.translator.simnml.ir.primitive.Instruction;
// import ru.ispras.microtesk.translator.simnml.ir.primitive.Situation;

/**
 * TODO: THIS CLASS IS OBSOLETE. IT IS NOT LONGER USED. IT IS LEFT HERE IN CASE COME PARTS OF ITS
 * CODE CAN BE REUSED (OR AT LEAST THE CONCEPT OF USER-PRODIVED SITUATIONS).
 * 
 * @author Andrei Tatarnikov
 */

@Deprecated
public final class UserTestSituationLoader {
  /*
   * private final String modelName; private final String testSitDir; private final String outDir;
   * private final IR ir;
   * 
   * private static final String JAVA_TEST_SIT_DIR_FRMT =
   * "%s/java/ru/ispras/microtesk/model/%s/situation";
   * 
   * private static final String ERR_TEST_SIT_DIR_DOES_NOT_EXIST =
   * "The \"%s\" folder does not exist. No user-defied situations will be included.%n";
   * 
   * private static final String JAVA_INIT_DIR_FRMT =
   * "%s/java/ru/ispras/microtesk/model/%s/initializer";
   * 
   * private static final String ERR_INIT_DIR_DOES_NOT_EXIST =
   * "The \"%s\" folder does not exist. No user-defied initializers will be included.%n";
   * 
   * private static final String ERR_FAILED_TO_COPY_DIR =
   * "Failed to copy \"%s\" to \"%s\". Reason: %s%n";
   * 
   * public UserTestSituationLoader(String modelName, String testSitDir, String outDir, IR ir) {
   * this.modelName = modelName; this.testSitDir = testSitDir; this.outDir = outDir; this.ir = ir; }
   * 
   * public void load() { // No user-defined situations provided. if (null == testSitDir) return;
   * 
   * final File fileTestSitDir = new File(testSitDir); if (!fileTestSitDir.exists() ||
   * !fileTestSitDir.isDirectory()) { System.err.printf(ERR_TEST_SIT_DIR_DOES_NOT_EXIST,
   * testSitDir); return; }
   * 
   * System.out.println("Adding " + testSitDir + "...");
   * 
   * // Copy Resources (XML constraints) copyDirectory(testSitDir + "/resources", outDir +
   * "/resources"); // Copy Java Code copyDirectory(testSitDir + "/java", outDir + "/java");
   * 
   * // addAllSituationsToIR(); // addAllInitializersToIR(); }
   * 
   * private void addAllSituationsToIR() { final String javaRoot =
   * String.format(JAVA_TEST_SIT_DIR_FRMT, testSitDir, modelName); final File javaRootDir = new
   * File(javaRoot);
   * 
   * System.out.println("Test Situations:");
   * 
   * if (!javaRootDir.exists() || !javaRootDir.isDirectory()) {
   * System.err.printf(ERR_TEST_SIT_DIR_DOES_NOT_EXIST, testSitDir); return; }
   * 
   * for (String file : javaRootDir.list()) { final String REX = "^[\\w]*[_][\\w]+.java$";
   * 
   * final Matcher matcher = Pattern.compile(REX).matcher(file); if (!matcher.matches()) continue;
   * 
   * final String situationFullName = file.replaceAll(".java$", ""); final String situationId =
   * situationFullName.replaceAll("^^[\\w]*[_]", "").toLowerCase(); final String instructionName =
   * situationFullName.replaceAll("[_][\\w]+$", "");
   * 
   * // If it is not assigned to a specific instruction, it is considered shared (linked to all
   * instructions). final boolean sharedSituation = instructionName.isEmpty();
   * addSituationToIR(situationFullName, situationId, sharedSituation, instructionName); } }
   * 
   * private void addSituationToIR(String fullName, String id, boolean isShared, String
   * instructionName) { System.out.printf("  %s (id: %s, instruction: %s)%n", fullName, id, isShared
   * ? "all instructions" : instructionName);
   * 
   * final Situation situation = new Situation(fullName, id, isShared);
   * 
   * if (isShared) { for(Instruction instruction : ir.getInstructions().values())
   * instruction.defineSituation(situation); return; }
   * 
   * final Map<String, Instruction> instructions = ir.getInstructions(); if
   * (!instructions.containsKey(instructionName)) { System.err.printf(
   * "Unable to add the %s situation to the %s instruction. No such instruction is defined.%n",
   * fullName, instructionName); return; }
   * 
   * final Instruction instruction = ir.getInstructions().get(instructionName);
   * instruction.defineSituation(situation); }
   * 
   * private void addAllInitializersToIR() { final String javaRoot =
   * String.format(JAVA_INIT_DIR_FRMT, testSitDir, modelName); final File javaRootDir = new
   * File(javaRoot);
   * 
   * if (!javaRootDir.exists() || !javaRootDir.isDirectory()) {
   * System.err.printf(ERR_INIT_DIR_DOES_NOT_EXIST, testSitDir); return; }
   * 
   * System.out.println("Initializers:");
   * 
   * for (String file : javaRootDir.list()) { final String REX = "^.*Initializer.java$";
   * 
   * final Matcher matcher = Pattern.compile(REX).matcher(file); if (!matcher.matches()) continue;
   * 
   * final String className = file.replaceAll(".java$", ""); System.out.println("  " + className);
   * ir.add(file, new Initializer(className)); } }
   * 
   * private static void copyDirectory(String source, String target) { final File sourceFile = new
   * File(source); final File targetFile = new File(target);
   * 
   * if (!sourceFile.exists()) return;
   * 
   * try { copyDirectory(sourceFile, targetFile); } catch (IOException e) {
   * System.err.printf(ERR_FAILED_TO_COPY_DIR, source, target, e.getMessage()); } }
   * 
   * private static void copyDirectory(File source, File target) throws IOException { if (null ==
   * source) throw new NullPointerException();
   * 
   * if (null == target) throw new NullPointerException();
   * 
   * if (source.isDirectory()) { if (!target.exists()) target.mkdirs();
   * 
   * for (String child : source.list()) copyDirectory(new File(source, child), new File(target,
   * child)); } else { copyFile(source, target); } }
   * 
   * private static void copyFile(File source, File target) throws IOException { if (null == source)
   * throw new NullPointerException();
   * 
   * if (null == target) throw new NullPointerException();
   * 
   * InputStream is = null; OutputStream os = null;
   * 
   * try { is = new FileInputStream(source); os = new FileOutputStream(target);
   * 
   * final byte[] buffer = new byte[1024];
   * 
   * int length; while ((length = is.read(buffer)) > 0) os.write(buffer, 0, length); } finally { if
   * (null != is) is.close();
   * 
   * if (null != os) os.close(); } }
   */
}
