package de.ruu.app.jeeeraaah.common.api.ws.rs.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;

/**
 * Pure in-memory unit tests for the super-task / sub-task hierarchy cycle-prevention
 * logic in {@link TaskDTO}.
 *
 * <p><strong>No database, no EntityManager, no Docker required.</strong>
 * Every {@link TaskDTO} is instantiated directly in heap memory.
 *
 * <hr>
 * <h2>Data model recap</h2>
 * Each {@code TaskDTO} holds:
 * <ul>
 *   <li>one nullable {@code superTask} reference — <em>the parent</em></li>
 *   <li>a set of {@code subTasks} — <em>the children</em></li>
 * </ul>
 * Together they form a <em>tree</em> (one parent per node, any number of children).
 * Sub-task membership is exclusively controlled via {@link TaskDTO#superTask(TaskDTO)}.
 *
 * <hr>
 * <h2>Cycle guard: ancestor walk (O(depth))</h2>
 * {@link TaskDTO#superTask(TaskDTO)} walks <em>upward</em> from the proposed
 * new parent via {@code superTask} pointers. If {@code this} is found anywhere
 * in that chain, the operation is rejected with {@link TaskRelationException}.
 * All checks run <em>before</em> any mutation — the DTO graph is left intact
 * on a rejected operation.
 *
 * <hr>
 * <h2>Relationship to the other layers</h2>
 * {@code TaskDTO} throws {@link TaskRelationException} for both self-reference
 * and cycle detection — identical to {@code TaskJPA} and in contrast to
 * {@code TaskBean} which throws {@link IllegalArgumentException}.
 * The cycle-guard algorithm (ancestor walk) is identical across all three layers.
 */
@DisplayName("TaskDTO — sub-task hierarchy cycle prevention (in-memory, no DB)")
class TaskDTO_HierarchyCycle_InMemoryTest
{
	private TaskGroupDTO group;

	@BeforeEach
	void setUp()
	{
		group = new TaskGroupDTO("test-group");
	}

	/** Shortcut: create a new in-memory task DTO in the shared group. */
	private TaskDTO task(String name) { return new TaskDTO(group, name); }

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
			TaskDTO a = task("A");

			assertThatThrownBy(() -> a.superTask(a))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("itself");
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
		 *
		 * <pre>
		 * cursor starts at:  b (the proposed new super task of a)
		 * First iteration:   cursor.superTask = a  → a == this (a)  → throw TaskRelationException
		 * </pre>
		 */
		@Test
		@DisplayName("a.superTask(b) throws after b.superTask(a) succeeded")
		void superTask_directCycle_throw()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");

			// Establish: B's parent is A
			assertThatCode(() -> b.superTask(a)).doesNotThrowAnyException();
			assertThat(b.superTask()).hasValue(a);

			// Now try to make B the parent of A — must be rejected
			assertThatThrownBy(() -> a.superTask(b))
					.isInstanceOf(TaskRelationException.class)
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
			TaskDTO a = task("A");
			TaskDTO b = task("B");

			b.superTask(a); // B.superTask = A

			// Attempt the cycle — ignore the expected exception
			try { a.superTask(b); } catch (TaskRelationException ignored) { }

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
		 * cursor starts at:  c
		 * Iteration 1:       c != a  → cursor = c.superTask = b
		 * Iteration 2:       b != a  → cursor = b.superTask = a
		 * Iteration 3:       a == a  → throw TaskRelationException
		 * </pre>
		 */
		@Test
		@DisplayName("a.superTask(c) throws after B → A, C → B chain was built")
		void superTask_threeNodeCycle_throw()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");
			TaskDTO c = task("C");

			b.superTask(a); // B's parent is A
			c.superTask(b); // C's parent is B

			assertThat(c.superTask()).hasValue(b);
			assertThat(b.superTask()).hasValue(a);

			// Trying to make C the parent of A would close A → B → C → A
			assertThatThrownBy(() -> a.superTask(c))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}

		@Test
		@DisplayName("b.superTask(c) throws — C is a descendant of B")
		void superTask_immediateDescendant_throw()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");
			TaskDTO c = task("C");

			b.superTask(a);
			c.superTask(b);

			// Trying to make C the parent of B — C is already B's child
			assertThatThrownBy(() -> b.superTask(c))
					.isInstanceOf(TaskRelationException.class)
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
		 * Iteration 4:       a == a  → throw TaskRelationException
		 * </pre>
		 * Three iterator steps demonstrate the walk ascends all the way to the root.
		 */
		@Test
		@DisplayName("a.superTask(d) throws after A←B←C←D chain — root ancestor detected")
		void superTask_rootAncestor_throw()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");
			TaskDTO c = task("C");
			TaskDTO d = task("D");

			b.superTask(a);
			c.superTask(b);
			d.superTask(c);

			// A is the root; D is a descendant. Making D the parent of A closes a 4-node cycle.
			assertThatThrownBy(() -> a.superTask(d))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}

		@Test
		@DisplayName("b.superTask(d) throws — mid-chain ancestor is also detected")
		void superTask_midChainAncestor_throw()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");
			TaskDTO c = task("C");
			TaskDTO d = task("D");

			b.superTask(a);
			c.superTask(b);
			d.superTask(c);

			// B is mid-chain; D is a descendant. Making D the parent of B is also a cycle.
			assertThatThrownBy(() -> b.superTask(d))
					.isInstanceOf(TaskRelationException.class)
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
			TaskDTO a = task("A");
			TaskDTO b = task("B");
			TaskDTO c = task("C");
			TaskDTO d = task("D");

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
			TaskDTO a = task("A");
			TaskDTO b = task("B");
			TaskDTO c = task("C");
			TaskDTO d = task("D");

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
			TaskDTO a = task("A");
			TaskDTO b = task("B");

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
			TaskDTO root  = task("root");
			TaskDTO child = task("child");

			assertThat(root.superTask()).isEmpty();

			assertThatCode(() -> child.superTask(root)).doesNotThrowAnyException();
			assertThat(child.superTask()).hasValue(root);
		}

		@Test
		@DisplayName("Re-assigning to same parent is a no-op (no exception)")
		void superTask_sameParentAgain_noOp()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");

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
	 * Documents that {@link TaskDTO#superTask(TaskDTO)} uses a <em>check-first,
	 * mutate-second</em> strategy that is naturally atomic with respect to cycle
	 * detection:
	 *
	 * <ol>
	 *   <li>All validations (self-reference guard, predecessor/successor guard,
	 *       ancestor-walk cycle guard) are performed <strong>before</strong> any
	 *       state change.</li>
	 *   <li>If any check throws, <em>no mutation has occurred</em> — the DTO
	 *       graph remains in its previous consistent state.</li>
	 *   <li>Only when all checks pass does the method remove {@code this} from
	 *       the old parent and add it to the new parent.</li>
	 * </ol>
	 *
	 * <h2>Scenario</h2>
	 * Chain: A → B → C.  We call {@code b.superTask(c)} — attempting to make C the
	 * parent of B. This would create a cycle (C is already a descendant of B).
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
			TaskDTO a = task("A");
			TaskDTO b = task("B");
			TaskDTO c = task("C");

			// Build valid chain: A → B → C  (B.superTask=A, C.superTask=B)
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
					.isInstanceOf(TaskRelationException.class)
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

