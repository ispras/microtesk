/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.analysis;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.antlrex.log.LogWriter;
import ru.ispras.microtesk.translator.antlrex.log.SenderKind;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.analysis.PrimitiveUtils.PathCounter;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveReference;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The {@link PrimitiveSyntesizer} class provides facilities to analyze information on relations
 * between operations and to synthesize on its basis the following elements:
 *
 * <p>Shortcuts for leaf (have no child operations) and junction (have more than one child
 * operations) operations that allow addressing (instantiating with all required parent operations)
 * these operation in various contexts. A shortcut can be synthesized if there is an unambiguous way
 * to resolve all dependencies of parent operations on the way from an entry operation to a target
 * operation. Shortcuts are added to IR of corresponding target operations.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class PrimitiveSyntesizer extends LogWriter implements TranslatorHandler<Ir> {
  /**
   * Name for the fake operation (OR rule) that unites all root operations described in the
   * specification (AND rules that have no parents). This identifier is used a context name when a
   * shortcut is addressed from the topmost level of operation nesting in test templates.
   */
  public static final String ROOT_ID = "#root";

  /**
   * The internal representation (IR) of the ISA.
   */
  private Ir ir;

  /**
   * Constructs a PrimitiveSyntesizer object.
   *
   * @param translator Translator that constructed the internal representation.
   * @throws IllegalArgumentException if any of the parameters equals null.
   */
  public PrimitiveSyntesizer(final Translator<Ir> translator) {
    super(SenderKind.EMITTER, "", translator != null ? translator.getLog() : null);
    InvariantChecks.checkNotNull(translator);
  }

  @Override
  protected String getSourceName() {
    return ir.getModelName();
  }

  @Override
  public void processIr(final Ir ir) {
    InvariantChecks.checkNotNull(ir);
    this.ir = ir;

    if (ir.getOps().isEmpty()) {
      reportError(NO_OPERATIONS);
      return;
    }

    syntesizeShortcuts();
  }

  /**
   * Synthesizes shortcuts for leaf and junction operations and adds the them to the corresponding
   * operations. Only leafs (no childs) and junctions (more than one child) are considered
   * interesting because there is no need to create shortcuts for intermediate nodes.
   *
   * <p>Implementation details:
   *
   * <p>The method iterates over the collection of operations provided by the client and uses the
   * ShortcutBuilder class to build shortcuts for leaf (no childs) and junction (more than one
   * child) primitives. Other operations are ignored as they are considered intermediate (they are
   * not a final point in an unambiguous path). See documentation on the ShortcutBuilder class for
   * more details.
   */
  private void syntesizeShortcuts() {
    // Fake primitive, root of all roots, needed to provide a common context.
    final PrimitiveOR root = new PrimitiveOR(ROOT_ID, Primitive.Kind.OP, ir.getRoots());

    // Used by ShortcutBuilder, stores all previous results to avoid
    // redundant traversals.
    final PathCounter pathCounter = new PathCounter();

    for (final Primitive op : ir.getOps().values()) {
      // Only leafs and junctions: shortcuts for other nodes are redundant.
      if (PrimitiveUtils.isLeaf(op) || PrimitiveUtils.isJunction(op)) {
        final PrimitiveAND target = (PrimitiveAND) op;
        new ShortcutBuilder(root, target, pathCounter).build();
      }
    }
  }

  private static final String NO_OPERATIONS =
      "The operation list is empty. No information to be analyzed.";
}
