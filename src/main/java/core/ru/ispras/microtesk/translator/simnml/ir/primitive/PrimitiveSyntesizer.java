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

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import static ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveUtils.isJunction;
import static ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveUtils.isLeaf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.microtesk.translator.antlrex.log.LogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogWriter;
import ru.ispras.microtesk.translator.antlrex.log.SenderKind;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveUtils.PathCounter;

/**
 * The PrimitiveSyntesizer class provides facilities to analyze information on relations between
 * operations and to synthesize on its basis the following elements:
 * 
 * <p>1. The list of root operations that includes all operations described by AND rules which have no
 * parents. Such operations serve as entry points in composite operations describing instruction
 * calls.
 * 
 * <p>2. Shortcuts for leaf (have no child operations) and junction (have more than one child
 * operations) operations that allow addressing (instantiating with all required parent operations)
 * these operation in various contexts. A shortcut can be synthesized if there is an unambiguous way
 * to resolve all dependencies of parent operations on the way from an entry operation to a target
 * operation. Shortcuts are added to IR of corresponding target operations.
 * 
 * @author Andrei Tatarnikov
 */

public final class PrimitiveSyntesizer extends LogWriter {
  /**
   * Name for the fake operation (OR rule) that unites all root operations described in the
   * specification (AND rules that have no parents). This identifier is used a context name when a
   * shortcut is addressed from the topmost level of operation nesting in test templates.
   */

  public static final String ROOT_ID = "#root";

  /**
   * The collection of operation primitives to be processed.
   */

  private final Collection<Primitive> operations;

  /**
   * The synthesized list of root primitives (all AND operations that have no parents).
   */

  private final List<Primitive> roots;

  /**
   * The flag that is set when all information has been successfully synthesized.
   */

  private boolean isSyntesized;

  /**
   * Constructs a PrimitiveSyntesizer object.
   * 
   * @param operations A list of operations to be processed.
   * @param fileName Specification file name.
   * @param log log Log object that stores information about events and issues that may occur.
   * 
   * @throws NullPointerException if any of the parameters equals null.
   */

  public PrimitiveSyntesizer(Collection<Primitive> operations, String fileName, LogStore log) {
    super(SenderKind.EMITTER, fileName, log);
    checkNotNull(operations);

    this.operations = operations;
    this.roots = new ArrayList<Primitive>();
    this.isSyntesized = false;
  }

  /**
   * Synthesizes the list of root operations and shortcuts for all "interesting" operations (leafs
   * and junctions) based on the list of all defined operations. For details on building shortcuts,
   * see documentation on the syntesizeShortcuts method.
   * 
   * @return <code>true</code> if the information was successfully synthesized or <code>false</code>
   *         otherwise.
   */

  public boolean syntesize() {
    if (isSyntesized) {
      reportWarning(ALREADY_SYNTHESIZED);
      return true;
    }

    if (operations.isEmpty()) {
      reportError(NO_OPERATIONS);
      return false;
    }

    if (!syntesizeRoots()) {
      return false;
    }

    syntesizeShortcuts();
    isSyntesized = true;

    return true;
  }

  /**
   * Checks whether the information was successfully synthesized.
   * 
   * @return <code>true</code> if the information was successfully synthesized or <code>false</code>
   *         otherwise.
   */

  public boolean isSyntesized() {
    return isSyntesized;
  }

  /**
   * Returns the list of root operations. A root operation is an AND rule that has no parents. The
   * list is synthesized.
   * 
   * @return List of root operations.
   */

  public List<Primitive> getRoots() {
    return roots;
  }

  /**
   * Synthesizes the list of root operations (saves all root operations in the list). A root
   * operation is considered to be an AND-rule operation that has no parents. If the method fails,
   * the list is cleared.
   * 
   * @return <code>true</code> if the list of root operations was successfully synthesized or
   *         <code>false</code> otherwise.
   */

  private boolean syntesizeRoots() {
    for (Primitive op : operations) {
      if (op.getKind() != Primitive.Kind.OP) {
        roots.clear();
        reportError(String.format(NOT_OPERATION, op.getName()));
        return false;
      }

      if (op.isRoot() && !op.isOrRule()) {
        roots.add(op);
      }
    }

    return true;
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
    final PrimitiveOR root = new PrimitiveOR(ROOT_ID, Primitive.Kind.OP, roots);

    // Used by ShortcutBuilder, stores all previous results to avoid
    // redundant traversals.
    final PathCounter pathCounter = new PathCounter();

    for (Primitive op : operations) {
      // Only leafs and junctions: shortcuts for other nodes are redundant.
      if (isLeaf(op) || isJunction(op)) {
        final PrimitiveAND target = (PrimitiveAND) op;
        new ShortcutBuilder(root, target, pathCounter).build();
      }
    }
  }

  private static final String NO_OPERATIONS =
    "The operation list is empty. No information to be analyzed.";

  private static final String ALREADY_SYNTHESIZED =
    "Internal presentation has already been fully synthesized. No action was be performed.";

  private static final String NOT_OPERATION =
    "Wring input data. The %s primitive is not an operation.";
}


/**
 * The ShortcutBuilder class creates all possible shortcuts for the target operation and registers
 * them into the corresponding object.
 * 
 * @author Andrei Tatarnikov
 */

final class ShortcutBuilder {
  /**
   * A synthetic (does not present in the specification) primitive that unites all root primitives
   * (AND-rule operations that have parents). It is needed to check if there are ambiguous paths
   * that start from the specification root (any of the root primitives).
   */

  private final PrimitiveOR root;

  /**
   * Target operation for a series of shortcuts.
   */

  private final PrimitiveAND target;

  /**
   * Path counter to find ambiguous paths. It caches the results of previous calculations and is
   * shared among all ShortcutBuilder objects to improve performance.
   */

  private final PathCounter pathCounter;

  /**
   * Holds the list of primitives with multiple parents that are found on a way from the target to
   * the root. Such nodes produce ambiguous paths. If there no such nodes, there cannot be multiple
   * paths from the source to the target. If there are such nodes, we need to check that there is
   * only one path from the source to each of these nodes. We don't need to check if where are
   * multiple paths from the source to the target because these nodes are on the way from the source
   * to the target and there is only one path from them to the target (because, otherwise, the
   * traversal would stop and they would not be processed).
   */

  private final Set<String> opsWithMultipleParents;

  /**
   * Constructs a ShortcutBuilder object.
   * 
   * @param root The root primitive, root of all roots, that provides a common topmost starting
   *        point for all paths.
   * @param target Target primitive.
   * @param pathCounter Path counter object that will be used to exclude ambiguous paths.
   * 
   * @throws NullPointerException if any of the parameters equals null.
   */

  public ShortcutBuilder(PrimitiveOR root, PrimitiveAND target, PathCounter pathCounter) {
    checkNotNull(root);
    checkNotNull(target);
    checkNotNull(pathCounter);

    this.root = root;
    this.target = target;
    this.pathCounter = pathCounter;

    // Empty by default
    this.opsWithMultipleParents = new HashSet<String>();
  }

  /**
   * Builds shortcuts for the target primitives and adds them to the list of shortcuts of this
   * primitive.
   */

  public void build() {
    build(target);
  }

  /**
   * Creates shortcuts to the target primitive starting from the entry primitive. Entry is the
   * topmost point of the shortcut path.
   * 
   * <p>Algorithm description:
   * 
   * <p>The method uses the ShortcutCreator class to create and register shortcuts that describe the
   * path from the entry node to the target node. A shortcut can be used in one or more contexts. In
   * the are no suitable contexts, there is no need to create the shortcut. There can be two
   * situations: (1) the entry node is a root node and (2) it is not a root node. In the first case,
   * if there is only one path from the root to the target, a shortcut to be used in the root
   * context is created. The second situation is more complicated. First of all, we check if the
   * entry node has multiple parents and register it in the set of multiple-parent nodes. This is
   * needed to efficiently perform checks for multiple paths. When the method exits, the entry is
   * removed from the set. Then we create shortcuts for the entry using its parents as contexts if
   * there is only one path from them to the target. If a parent is not a junction (a node with
   * multiple arguments than can unite paths to several targets), we invoke the build method
   * recursively for this parent to build shortcuts that start from it.
   * 
   * @param entry Entry point of the shortcuts to be created.
   */

  private void build(PrimitiveAND entry) {
    final ShortcutCreator creator = new ShortcutCreator(entry);

    if (entry.isRoot() && isSinglePathToTarget(root)) {
      creator.addShortcutContext(root.getName());
    } else {
      checkForMultipleParents(entry);
      for (Primitive.Reference ref : entry.getParents()) {
        if (!isSinglePathToTarget(ref.getSource())) {
          continue;
        }

        if (!isJunction(ref.getSource())) {
          build(ref.resolve());
        }

        creator.addShortcutContext(ref.getSource().getName());
      }
      checkForMultipleParentsFinalize(entry);
    }

    creator.createAndRegisterShortcut();
  }

  /**
   * Checks if the given Primitive has multiple parents (more than one). If it has, adds it to the
   * set of the set of primitives with multiple parents (opsWithMultipleParents) that can cause
   * ambiguity.
   * 
   * <p>This check is performed because if there are no primitives with multiple parents on the way
   * from the target to the source, there can be only one path from the source to the target and
   * there is no need to perform a check for multiple paths. Otherwise, there are multiple paths and
   * we need to look for the point where they start and exclude this point (node) from the shortcut
   * path to avoid ambiguities.
   * 
   * @param entry Primitive to be checked.
   */

  private void checkForMultipleParents(PrimitiveAND entry) {
    if (entry.getParentCount() > 1)
      opsWithMultipleParents.add(entry.getName());
  }

  /**
   * Removes the specified primitive from the set of primitives with multiple parents (if it
   * presents there). This method is called when the build method finishes traversing a certain
   * subpaths. Nodes that were saved in the set during the traversal of some subpath, may not
   * present in other subpaths at all. Therefore, there is no need use them in further checks. If
   * they appear on the path again, the will be added to set a new and all corresponding checks will
   * be performed.
   * 
   * @param entry Primitive to be removed.
   */

  private void checkForMultipleParentsFinalize(PrimitiveAND entry) {
    if (!opsWithMultipleParents.isEmpty()) {
      opsWithMultipleParents.remove(entry.getName());
    }
  }

  /**
   * Checks whether there is only a single path from the source to the target. In fact, the
   * implementation is slightly more intricate. It checks if there is a single path to all nodes in
   * the set of nodes that have multiple parents. There modes lay on the way from the source to the
   * target on have only on path to the target. This is done for performance reasons: intermediate
   * nodes with multiple parents is likely to present on a greater number of paths than terminal
   * nodes (targets). Because the previous results are cached by the path counter, it works faster
   * for such (more "popular") nodes.
   * 
   * @param source Source primitive.
   * @return <code>true</code> it there is a single path from the source to the target or false
   *         otherwise.
   * 
   * @throws IllegalStateException if the number of possible paths from the source to the target is
   *         less than 1. This is an invariant. At least one path always exists (because the build
   *         method passes it before checking that there is only one path).
   */

  private boolean isSinglePathToTarget(Primitive source) {
    for (String ambiguousTarget : opsWithMultipleParents) {
      final int count = pathCounter.getPathCount(source, ambiguousTarget);

      if (count > 1) {
        return false;
      }

      if (count < 1) {
        throw new IllegalStateException();
      }
    }

    return true;

    /*
     * // This is an old more straightforward, more reliable
     * // implementation, but less efficient implementation.
     * // It is left here in case some issues with the current arise.
     * 
     * // The canHaveMultiplePaths is set to true by checkForMultipleParents
     * // when it faces a node with multiple parents.
     * 
     * if (!canHaveMultiplePaths) {
     *   return true;
     * }
     * 
     * final int count = 
     *   pathCounter.getPathCount(source, target.getName());
     * 
     * if (count < 1) { 
     *   throw new IllegalStateException();
     * }
     * 
     * return count == 1;
     */
  }

  /**
   * The ShortcutCreator class responsible for creating and registering shortcuts that describe
   * paths starting from a common entry primitive.
   * 
   * @author Andrei Tatarnikov
   */

  private final class ShortcutCreator {
    private final PrimitiveAND entry;
    private final List<String> contextNames;

    /**
     * Constructs a shortcut creator for the given entry.
     * 
     * @param entry Entry primitive for the shortcut to be created.
     */

    private ShortcutCreator(PrimitiveAND entry) {
      this.entry = entry;
      this.contextNames = new ArrayList<String>();
    }

    /**
     * Add a context in which the shortcut can be used. If the entry refers to the same primitive as
     * the target, no context is added since there is no need for such a shortcut (in this case, the
     * path consists only of the target).
     * 
     * @param name Context name.
     */

    private void addShortcutContext(String name) {
      if (entry != target) {
        contextNames.add(name);
      }
    }

    /**
     * Creates and registers a shortcut if there are contexts in which it can be used.
     */

    private void createAndRegisterShortcut() {
      if (!contextNames.isEmpty()) { 
        final Shortcut shortcut = new Shortcut(entry, target, contextNames);
        target.addShortcut(shortcut);
      }
    }
  }
}
