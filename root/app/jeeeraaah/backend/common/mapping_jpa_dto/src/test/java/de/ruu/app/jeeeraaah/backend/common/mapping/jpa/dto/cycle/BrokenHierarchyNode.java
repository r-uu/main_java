package de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto.cycle;

import java.util.ArrayList;
import java.util.List;

/**
 * A lightweight in-memory tree node that exists solely to <em>document</em> and
 * <em>contrast</em> two approaches to cycle prevention in a parent/child hierarchy:
 *
 * <h2>The broken approach — {@link #addChild_BROKEN}</h2>
 * <p>
 * A tempting but incorrect strategy: <em>"remove the child from its current parent before
 * attaching it to the new parent."</em>
 * <p>
 * This strategy prevents duplicate links but does <strong>not</strong> detect
 * whether the proposed child is already an <em>ancestor</em> of the proposed parent.
 * <p>
 * Failure example:
 * <pre>{@code
 * A.addChild_BROKEN(B)   // B.parent → A  (valid, A is the root)
 * B.addChild_BROKEN(A)   // A has no parent, so the "remove" step is a no-op;
 *                        // then A.parent → B  → CYCLE created silently!
 * }</pre>
 *
 * <h2>The correct approach — {@link #addChild_CORRECT}</h2>
 * <p>
 * Before accepting a proposed child, walk <em>upward</em> from the proposed
 * <em>parent</em> via parent references. If the proposed <em>child</em> is
 * encountered anywhere in that chain, reject the operation.
 * <p>
 * Complexity: O(depth) — no full DFS required, because each node has at most one parent.
 * <p>
 * This is exactly the strategy used in
 * {@link de.ruu.app.jeeeraaah.backend.persistence.jpa.entity.TaskJPA#superTask(
 * de.ruu.app.jeeeraaah.backend.persistence.jpa.entity.TaskJPA)}.
 *
 * <h2>Lazy loading note</h2>
 * <p>
 * This POJO has <strong>no JPA semantics</strong>. Parent references are plain Java
 * object references — no proxy, no lazy loading, no persistence context. See
 * {@link TaskJPA_HierarchyCycle_JpaIntegrationTest} for the discussion of how JPA
 * lazy proxies interact with the direct field access {@code cursor.superTask} used
 * in the real {@code TaskJPA.addSubTask} implementation.
 *
 * @see TaskJPA_HierarchyCycle_InMemoryTest
 * @see TaskJPA_HierarchyCycle_JpaIntegrationTest
 */
public class BrokenHierarchyNode
{
	/** Display name used in assertions and error messages. */
	public final String name;

	/**
	 * Parent reference. {@code null} means this node is a root node (no parent).
	 * Public for direct inspection in tests.
	 */
	public BrokenHierarchyNode parent;

	/** Mutable child list for direct inspection in tests. */
	public final List<BrokenHierarchyNode> children = new ArrayList<>();

	public BrokenHierarchyNode(String name) { this.name = name; }

	// ──────────────────────────────────────────────────────────────────────────
	// BROKEN approach
	// ──────────────────────────────────────────────────────────────────────────

	/**
	 * <strong>BROKEN implementation — intentionally incorrect, for documentation purposes only.</strong>
	 * <p>
	 * The idea: detach {@code child} from its old parent first (to avoid duplicate links),
	 * then attach it to {@code this}. No ancestor-walk cycle check is performed.
	 * <p>
	 * <strong>Why it fails:</strong> consider calling {@code B.addChild_BROKEN(A)} after
	 * {@code A.addChild_BROKEN(B)} has already established {@code B.parent = A}.
	 * <ol>
	 *   <li>A.parent is {@code null} → the "remove from old parent" step is a no-op.</li>
	 *   <li>A.parent is set to B → now {@code A.parent = B} and {@code B.parent = A}.</li>
	 *   <li>Walking the tree from any node in this pair loops forever. CYCLE exists.</li>
	 * </ol>
	 * <p>
	 * <strong>Contrast:</strong> {@link #addChild_CORRECT(BrokenHierarchyNode)} rejects
	 * the same operation in step (1) before any mutation takes place.
	 *
	 * @param child the node to add as a child of {@code this}
	 */
	public void addChild_BROKEN(BrokenHierarchyNode child)
	{
		// ── "Remove from old parent" — the proposed (broken) approach ───────────
		// Intention: prevent duplicate links by detaching from the current parent.
		// Problem:   does NOT verify that child is already an ANCESTOR of this.
		if (child.parent != null)
		{
			child.parent.children.remove(child);
			child.parent = null;
		}

		// ── Blindly attach — no cycle guard at all ──────────────────────────────
		child.parent = this;
		this.children.add(child);
	}

	// ──────────────────────────────────────────────────────────────────────────
	// CORRECT approach
	// ──────────────────────────────────────────────────────────────────────────

	/**
	 * Correct implementation that uses an <em>ancestor walk</em> to prevent cycles.
	 * <p>
	 * Before any mutation, walks upward from {@code this} through parent references.
	 * If {@code child} is encountered anywhere in the ancestor chain, the operation is
	 * rejected with an {@link IllegalArgumentException} — <em>without</em> modifying
	 * any state.
	 * <p>
	 * Complexity: O(depth) — at most one chain traversal, no DFS.
	 *
	 * @param child the node to add as a child of {@code this}
	 * @throws IllegalArgumentException if {@code child} is {@code this}
	 * @throws IllegalArgumentException if adding {@code child} would introduce a cycle
	 */
	public void addChild_CORRECT(BrokenHierarchyNode child)
	{
		if (child == this)
			throw new IllegalArgumentException("A node cannot be its own child");

		// ── Ancestor walk ───────────────────────────────────────────────────────
		// Walk upward from 'this' (the proposed new parent).
		// If 'child' is found in the ancestor chain, the proposed link would close a
		// cycle: child is already above 'this' in the tree.
		BrokenHierarchyNode cursor = this.parent;
		while (cursor != null)
		{
			if (cursor == child)
				throw new IllegalArgumentException(
						"Adding '" + child.name + "' as child of '" + this.name + "' would create a cycle: '"
						+ child.name + "' is already an ancestor of '" + this.name + "'");
			cursor = cursor.parent;
		}

		// ── Cycle-free: safe to attach ──────────────────────────────────────────
		if (child.parent != null)
		{
			child.parent.children.remove(child);
		}
		child.parent = this;
		this.children.add(child);
	}

	// ──────────────────────────────────────────────────────────────────────────
	// Helper
	// ──────────────────────────────────────────────────────────────────────────

	/**
	 * Pure predicate — does <strong>not</strong> modify any state.
	 * <p>
	 * Returns {@code true} if {@code ancestor} is reachable by following parent references
	 * upward from {@code this}.
	 * <p>
	 * <strong>Caution with cyclic graphs:</strong> if the parent chain is itself cyclic
	 * (as produced by {@link #addChild_BROKEN}), this method may loop until the
	 * {@code ancestor} is found in the cycle. It therefore terminates correctly
	 * when {@code ancestor} is indeed part of the cycle. For graphs containing cycles
	 * where {@code ancestor} is <em>not</em> reachable, the method would loop forever —
	 * do not call it on such graphs for a node that is not in the cycle.
	 *
	 * @param ancestor the node to search for in the ancestor chain
	 * @return {@code true} if {@code ancestor} was found
	 */
	public boolean hasAncestor(BrokenHierarchyNode ancestor)
	{
		BrokenHierarchyNode cursor = this.parent;
		while (cursor != null)
		{
			if (cursor == ancestor) return true;
			cursor = cursor.parent;
		}
		return false;
	}

	@Override
	public String toString() { return "Node(" + name + ")"; }
}

