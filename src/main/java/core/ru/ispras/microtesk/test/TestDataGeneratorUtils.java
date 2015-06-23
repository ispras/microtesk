/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.io.File;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.model.api.ICallFactory;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IOperation;
import ru.ispras.microtesk.model.api.instruction.IOperationBuilder;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.settings.ExtensionSettings;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.LazyValue;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.RandomValue;
import ru.ispras.microtesk.test.template.UnknownImmediateValue;
import ru.ispras.microtesk.test.testbase.AddressGenerator;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;
import ru.ispras.testbase.TestBaseRegistry;
import ru.ispras.testbase.generator.DataGenerator;

/**
 * {@link TestDataGeneratorUtils} implements functions shared among test data generators.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class TestDataGeneratorUtils {
  private TestDataGeneratorUtils() {}

  public static TestBase newTestBase(final GeneratorSettings settings) {
    final TestBase testBase = new TestBase();
    final TestBaseRegistry registry = testBase.getRegistry();

    // Register the predefined test data generators.
    registry.registerGenerator("address", new AddressGenerator());

    if (null == settings || null == settings.getExtensions()) {
      return testBase;
    }

    // Register the user-defined test data generators.
    final String home = SysUtils.getHomeDir();
    final File file = new File(new File(new File(new File(home), "lib"), "jars"), "models.jar");

    final URL url;
    try {
      url = file.toURI().toURL();
    } catch (MalformedURLException e1) {
      Logger.error(e1.getMessage());
      return testBase;
    }

    final URL[] urls = new URL[]{url};
    final ClassLoader loader = new URLClassLoader(urls);

    for (final ExtensionSettings ext : settings.getExtensions().getExtensions()) {
      try {
        final Class<?> cls = loader.loadClass(ext.getPath());
        final DataGenerator generator = DataGenerator.class.cast(cls.newInstance());
        registry.registerGenerator(ext.getName(), generator);
      } catch (final Exception e) {
        Logger.error(e.getMessage());
        e.printStackTrace();
      }
    }

    return testBase;
  }

  public static ConcreteCall makeConcreteCall(
      final Call abstractCall,
      final ICallFactory callFactory)
          throws ConfigurationException {
    checkNotNull(abstractCall);
    checkNotNull(callFactory);

    if (!abstractCall.isExecutable()) {
      return new ConcreteCall(abstractCall);
    }

    final Primitive rootOp = abstractCall.getRootOperation();
    checkRootOp(rootOp);

    final IOperation op = makeOp(rootOp, callFactory);
    final InstructionCall executable = callFactory.newCall(op);

    return new ConcreteCall(abstractCall, executable);
  }

  public static BigInteger makeImm(final Argument argument) {
    checkArgKind(argument, Argument.Kind.IMM);
    return (BigInteger) argument.getValue();
  }

  public static BigInteger makeImmRandom(final Argument argument) {
    checkArgKind(argument, Argument.Kind.IMM_RANDOM);
    return ((RandomValue) argument.getValue()).getValue();
  }

  public static BigInteger makeImmUnknown(final Argument argument) {
    checkArgKind(argument, Argument.Kind.IMM_UNKNOWN);
    return ((UnknownImmediateValue) argument.getValue()).getValue();
  }

  public static BigInteger makeImmLazy(final Argument argument) {
    checkArgKind(argument, Argument.Kind.IMM_LAZY);
    return ((LazyValue) argument.getValue()).getValue();
  }

  public static IAddressingMode makeMode(final Argument argument, final ICallFactory callFactory)
      throws ConfigurationException {
    checkArgKind(argument, Argument.Kind.MODE);
    checkNotNull(callFactory);

    final Primitive mode = (Primitive) argument.getValue();
    final IAddressingModeBuilder builder = callFactory.newMode(mode.getName());

    for (Argument arg : mode.getArguments().values()) {
      final String argName = arg.getName();
      switch (arg.getKind()) {
        case IMM:
          builder.setArgumentValue(argName, makeImm(arg));
          break;

        case IMM_RANDOM:
          builder.setArgumentValue(argName, makeImmRandom(arg));
          break;

        case IMM_UNKNOWN:
          builder.setArgumentValue(argName, makeImmUnknown(arg));
          break;

        case IMM_LAZY:
          builder.setArgumentValue(argName, makeImmLazy(arg));
          break;

        default:
          throw new IllegalArgumentException(String.format(
            "Illegal kind of argument %s: %s.", argName, arg.getKind()));
      }
    }

    return builder.getProduct();
  }

  public static IOperation makeOp(final Argument argument, final ICallFactory callFactory)
      throws ConfigurationException {
    checkArgKind(argument, Argument.Kind.OP);
    checkNotNull(callFactory);

    final Primitive abstractOp = (Primitive) argument.getValue();

    return makeOp(abstractOp, callFactory);
  }

  public static IOperation makeOp(final Primitive abstractOp, final ICallFactory callFactory)
      throws ConfigurationException {
    checkOp(abstractOp);
    checkNotNull(callFactory);

    final String name = abstractOp.getName();
    final String context = abstractOp.getContextName();

    final IOperationBuilder builder = callFactory.newOp(name, context);

    for (Argument arg : abstractOp.getArguments().values()) {
      final String argName = arg.getName();
      switch (arg.getKind()) {
        case IMM:
          builder.setArgument(argName, makeImm(arg));
          break;

        case IMM_RANDOM:
          builder.setArgument(argName, makeImmRandom(arg));
          break;

        case IMM_UNKNOWN:
          builder.setArgument(argName, makeImmUnknown(arg));
          break;

        case IMM_LAZY:
          builder.setArgument(argName, makeImmLazy(arg));
          break;

        case MODE:
          builder.setArgument(argName, makeMode(arg, callFactory));
          break;

        case OP:
          builder.setArgument(argName, makeOp(arg, callFactory));
          break;

        default:
          throw new IllegalArgumentException(String.format(
            "Illegal kind of argument %s: %s.", argName, arg.getKind()));
      }
    }

    return builder.build();
  }

  public static void checkOp(final Primitive op) {
    if (Primitive.Kind.OP != op.getKind()) {
      throw new IllegalArgumentException(String.format(
        "%s is not an operation.", op.getName()));
    }
  }

  public static void checkRootOp(final Primitive op) {
    checkOp(op);
    if (!op.isRoot()) {
      throw new IllegalArgumentException(String.format(
        "%s is not a root operation!", op.getName()));
    }
  }

  public static void checkArgKind(final Argument arg, final Argument.Kind expected) {
    if (arg.getKind() != expected) {
      throw new IllegalArgumentException(String.format(
        "Argument %s has kind %s while %s is expected.", arg.getName(), arg.getKind(), expected));
    }
  }
}
