package de.ruu.app.jeeeraaah.common.api.bean.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;

/**
 * Pure in-memory unit tests for the super-task / sub-task hierarchy cycle-prevention
 * logic in {@link TaskBean}.
 *
 * <p><strong>No database, no EntityManager, no Docker required.</strong>
 * Every {@link TaskBean} is instantiated directly in heap memory.
 *
 * <hr>
 * <h2>Data model recap</h2>
 * Each {@code TaskBean} holds:
 * <ul>
 *   <li>one nullable {@code superTask} reference — <em>the parent</em></li>
 *   <li>a set of {@code subTasks} — <em>the children</em></li>
 * </ul>
 * Together they form a <em>tree</em> (one parent per node, any number of children).
 * Sub-task membership is exclusively controlled via {@link TaskBean#superTask(TaskBean)}.
 *
 * <hr>
 * <h2>Cycle guard: ancestor walk (O(depth))</h2>
 * {@link TaskBean#superTask(TaskBean)} walks <em>upward</em> from the proposed
 * new parent via {@code superTask} pointers. If {@code this} is found anywhere
 * in that chain, the operation is rejected with {@link IllegalArgumentException}.
 * All checks run <em>before</em> any mutation — the bean graph is left intact
 * on a rejected operation.
 *
 * <hr>
 * <h2>Note: exception type differs from TaskJPA / TaskDTO</h2>
 * {@code TaskBean.superTask()} throws {@link IllegalArgumentException} for both
 * self-reference and cycle detection, whereas {@code TaskJPA} and {@code TaskDTO}
 * throw {@code TaskRelationException}. The cycle-guard algorithmic behaviour is
 * otherwise identical across all three layers.
 */
@DisplayName("TaskBean — sub-task hierarchy cycle prevention (in-memory, no DB)")
class TaskBean_HierarchyCycle_InMemoryTest
{
	private TaskGroupBean group;

	@BeforeEach
	void setUp()
	{
		group = new TaskGroupBean("test-group");
	}

	/** Shortcut: create a new in-memory task in the shared group. */
	private TaskBean task(String name) { return new TaskBean(group, name); }

	// ═══════════════════════════════════════════════════════════════════════════
	// A — SELF-REFERENCE
	// ═══════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("A — self-reference (task → itself)")
	class A_SelfReference
	{
		@Test
		@DisplayName("superTask(this) is rejected immediately")
		void superTask_selfReference_throw()
		{
			TaskBean a = task("A");

			assertThatThrownBy(() -> a.superTask(a))
					.isInstanceOf(IllegalArgumentException.class);
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// B — DIRECT CYCLE: A → B → A
	// ═══════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("B — direct two-node cycle (A → B → A)")
	class B_DirectCycle
	{
		/**
		 * Setup:  {@code b.superTask(a)} establishes B as a child of A.
		 * Attack:  {@code a.superTask(b)} tries to make B the parent of A.
		 * <p>
		 * Expected: the ancestor walk in {@code a.superTask(b)} finds A
		 * at {@code cursor = b.superTask} and throws.
		 */
		@Test
		@DisplayName("a.superTask(b) throws after b.superTask(a) succeeded")
		void superTask_directCycle_throw()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");

			// Establish: B's parent is A (A → B)
			assertThatCode(() -> b.superTask(a)).doesNotThrowAnyException();
			assertThat(b.superTask()).hasValue(a);

			// Now try to make B the parent of A — must be rejected (A is B's ancestor)
			assertThatThrownBy(() -> a.superTask(b))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("cycle");
		}

		/**
		 * State remains clean after a rejected {@code superTask()} call: the original
		 * parent/child relation is not modified by the failed call.
		 */
		@Test
		@DisplayName("Rejected superTask() leaves existing hierarchy unchanged")
		void superTask_directCycle_stateUnchangedAfterReject()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");

			b.superTask(a); // B.superTask = A

			// Attempt the cycle — ignore the expected exception
			try { a.superTask(b); } catch (IllegalArgumentException ignored) { }

			// State must still reflect: B's parent is A, A has no parent
			assertThat(b.superTask()).hasValue(a);
			assertThat(a.superTask()).isEmpty();
			assertThat(a.subTasks()).hasValueSatisfying(s -> assertThat(s).containsExactly(b));
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// C — THREE-NODE CYCLE: A → B → C → A
	// ═══════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("C — three-node cycle (A → B → C → A)")
	class C_ThreeNodeCycle
	{
		/**
		 * Build chain B.superTask(A), C.superTask(B) first, then try a.superTask(c).
		 *
		 * <pre>
		 * cursor starts at:  c (proposed new super task of a)
		 * Iteration 1:       c != a  → cursor = c.superTask = b
		 * Iteration 2:       b != a  → cursor = b.superTask = a
		 * Iteration 3:       a == a  → throw IllegalArgumentException
		 * </pre>
		 */
		@Test
		@DisplayName("a.superTask(c) throws after B → A, C → B chain was built")
		void superTask_threeNodeCycle_throw()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");
			TaskBean c = task("C");

			b.superTask(a); // B's parent is A
			c.superTask(b); // C's parent is B

			assertThat(c.superTask()).hasValue(b);
			assertThat(b.superTask()).hasValue(a);

			// Trying to make C the parent of A would close A → B → C → A
			assertThatThrownBy(() -> a.superTask(c))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("cycle");
		}

		@Test
		@DisplayName("b.superTask(c) throws — C is a descendant of B (B is C's ancestor)")
		void superTask_immediateDescendant_throw()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");
			TaskBean c = task("C");

			b.superTask(a);
			c.superTask(b);

			// Trying to make C the parent of B — C is already B's child
			assertThatThrownBy(() -> b.superTask(c))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("cycle");
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// D — FOUR-NODE CYCLE: A → B → C → D → A (and D → B)
	// ═══════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("D — four-node cycle (A → B → C → D → A  /  D → B)")
	class D_FourNodeCycle
	{
		/**
		 * Build chain B→A, C→B, D→C, then try a.superTask(d).
		 *
		 * <pre>
		 * cursor starts at:  d
		 * Iteration 1:       d != a  → cursor = d.superTask = c
		 * Iteration 2:       c != a  → cursor = c.superTask = b
		 * Iteration 3:       b != a  → cursor = b.superTask = a
		 * Iteration 4:       a == a  → throw IllegalArgumentException
		 * </pre>
		 */
		@Test
		@DisplayName("a.superTask(d) throws after A←B←C←D chain — root ancestor detected")
		void superTask_rootAncestor_throw()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");
			TaskBean c = task("C");
			TaskBean d = task("D");

			b.superTask(a);
			c.superTask(b);
			d.superTask(c);

			// A is the root; D is a descendant. Making D the parent of A closes a 4-node cycle.
			assertThatThrownBy(() -> a.superTask(d))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("cycle");
		}

		@Test
		@DisplayName("b.superTask(d) throws — mid-chain ancestor is also detected")
		void superTask_midChainAncestor_throw()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");
			TaskBean c = task("C");
			TaskBean d = task("D");

			b.superTask(a);
			c.superTask(b);
			d.superTask(c);

			// B is mid-chain; D is a descendant. Making D the parent of B is also a cycle.
			assertThatThrownBy(() -> b.superTask(d))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("cycle");
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// E — VALID HIERARCHIES (accepted without exception)
	// ═══════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("E — valid hierarchies are accepted without exception")
	class E_ValidHierarchy
	{
		@Test
		@DisplayName("Linear chain A → B → C → D is accepted")
		void superTask_linearChain_ok()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");
			TaskBean c = task("C");
			TaskBean d = task("D");

			assertThatCode(() -> {
				b.superTask(a);
				c.superTask(b);
				d.superTask(c);
			}).doesNotThrowAnyException();

			assertThat(b.superTask()).hasValue(a);
			assertThat(c.superTask()).hasValue(b);
			assertThat(d.superTask()).hasValue(c);
		}

		@Test
		@DisplayName("Tree with siblings:  A → {B, C},  B → D  is accepted")
		void superTask_treeWithSiblings_ok()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");
			TaskBean c = task("C");
			TaskBean d = task("D");

			b.superTask(a);
			c.superTask(a);
			d.superTask(b);

			assertThat(b.superTask()).hasValue(a);
			assertThat(c.superTask()).hasValue(a);
			assertThat(d.superTask()).hasValue(b);

			assertThat(a.subTasks()).hasValueSatisfying(s -> assertThat(s).containsExactlyInAnyOrder(b, c));
		}

		@Test
		@DisplayName("Setting superTask to null detaches the node from its parent (valid)")
		void superTask_setToNull_detachesFromParent()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");

			b.superTask(a);
			assertThat(b.superTask()).hasValue(a);
			assertThat(a.subTasks()).hasValueSatisfying(s -> assertThat(s).contains(b));

			// Detach B from A
			assertThatCode(() -> b.superTask(null)).doesNotThrowAnyException();
			assertThat(b.superTask()).isEmpty();
			assertThat(a.subTasks()).hasValueSatisfying(s -> assertThat(s).doesNotContain(b));
		}

		@Test
		@DisplayName("Root task with no super task can always accept a child")
		void rootTask_noSuperTask_canAlwaysAcceptChildren()
		{
			TaskBean root  = task("root");
			TaskBean child = task("child");

			assertThat(root.superTask()).isEmpty();

			assertThatCode(() -> child.superTask(root)).doesNotThrowAnyException();
			assertThat(child.superTask()).hasValue(root);
		}

		@Test
		@DisplayName("Re-assigning to same parent is a no-op (no exception)")
		void superTask_sameParentAgain_noOp()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");

			b.superTask(a);

			// Setting the same parent again must be a no-op, no exception
			assertThatCode(() -> b.superTask(a)).doesNotThrowAnyException();
			assertThat(b.superTask()).hasValue(a);
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// F — STATE CONSISTENCY: check-first guarantees atomic rejection
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Documents that {@link TaskBean#superTask(TaskBean)} uses a <em>check-first,
	 * mutate-second</em> strategy that is naturally atomic with respect to cycle
	 * detection:
	 *
	 * <ol>
	 *   <li>All validations (self-reference guard, ancestor-walk cycle guard) are
	 *       performed <strong>before</strong> any state change.</li>
	 *   <li>If any check throws, <em>no mutation has occurred</em> — the bean
	 *       graph remains in its previous consistent state.</li>
	 *   <li>Only when all checks pass does the method remove {@code this} from
	 *       the old parent and add it to the new parent.</li>
	 * </ol>
	 *
	 * <h2>Scenario</h2>
	 * Chain: A → B → C (B's parent is A; C's parent is B).
	 * We call {@code b.superTask(c)} — attempting to make C the parent of B.
	 * This would create a cycle (C is already a descendant of B).
	 * <ul>
	 *   <li>Ancestor walk starts at {@code c} (the proposed new super task).</li>
	 *   <li>First step: {@code c.superTask = b} — <em>found</em> → cycle detected → throws.</li>
	 *   <li>No mutation has happened → B is still a child of A, and C is still a child of B.</li>
	 * </ul>
	 */
	@Nested
	@DisplayName("F — STATE CONSISTENCY: superTask() check-first leaves state intact on cycle exception")
	class F_SuperTaskSetterStateConsistency
	{
		@Test
		@DisplayName("b.superTask(c) throws — state is fully preserved (no orphaning)")
		void superTask_cycleThrows_statePreserved()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");
			TaskBean c = task("C");

			// Build valid chain: A → B → C  (B.parent=A, C.parent=B)
			b.superTask(a);
			c.superTask(b);

			// Pre-condition checks
			assertThat(b.superTask()).as("initial: B.superTask is A").hasValue(a);
			assertThat(c.superTask()).as("initial: C.superTask is B").hasValue(b);
			assertThat(a.subTasks()).hasValueSatisfying(s ->
					assertThat(s).as("initial: A.subTasks contains B").contains(b));

			// Attempt to make C the parent of B:
			// Ancestor walk from C: C.superTask = B → B == this (B) → cycle detected → throw.
			// Because the check runs before any mutation, state must be fully preserved.
			assertThatThrownBy(() -> b.superTask(c))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("cycle");

			// ── Check-first: state is fully preserved after the throw ─────────────
			assertThat(b.superTask())
					.as("B.superTask is still A — no mutation occurred before the throw")
					.hasValue(a);

			assertThat(a.subTasks())
					.as("A.subTasks still contains B — no orphaning occurred")
					.hasValueSatisfying(s -> assertThat(s).contains(b));

			// C is unaffected — the failed operation made no change to C
			assertThat(c.superTask())
					.as("C.superTask is still B (unchanged by the failed operation)")
					.hasValue(b);
		}
	}
}

