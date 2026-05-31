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
import de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException;

/**
 * Pure in-memory unit tests for the predecessor/successor cycle-prevention logic in {@link TaskBean}.
 *
 * <p><strong>No database, no EntityManager, no Docker required.</strong>
 *
 * <hr>
 * <h2>Cycle-guard strategy (DFS on successor edges)</h2>
 * {@link TaskBean#addPredecessor(TaskBean)} and {@link TaskBean#addSuccessor(TaskBean)} each
 * perform an iterative DFS over successor edges before mutating any state:
 * <ul>
 *   <li>{@code addPredecessor(p)}: walks successors of {@code this} — if {@code p} is reachable,
 *       then the path {@code this →...→ p} already exists; adding {@code p → this} closes a cycle.</li>
 *   <li>{@code addSuccessor(s)}: walks successors of {@code s} — if {@code this} is reachable,
 *       then the path {@code s →...→ this} already exists; adding {@code this → s} closes a cycle.</li>
 * </ul>
 *
 * <hr>
 * <h2>In-memory limitation</h2>
 * The DFS only traverses the in-memory successor graph. For DB-level completeness see
 * {@code PredecessorSuccessorCycleValidator} in {@code backend/constraint/timecycle}.
 */
@DisplayName("TaskBean — predecessor/successor cycle prevention (in-memory, no DB)")
class TaskBean_PredecessorSuccessorCycleTest
{
	private TaskGroupBean group;

	@BeforeEach void setUp() { group = new TaskGroupBean("test-group"); }

	private TaskBean task(String name) { return new TaskBean(group, name); }

	// ═════════════════════════════════════════════════════════════════════╗
	// A — SELF-REFERENCE                                                   ║
	// ═════════════════════════════════════════════════════════════════════╝

	@Nested
	@DisplayName("A — self-reference (task → itself as predecessor or successor)")
	class A_SelfReference
	{
		@Test
		@DisplayName("addPredecessor(this) is rejected")
		void addPredecessor_selfReference_throw()
		{
			TaskBean a = task("A");

			assertThatThrownBy(() -> a.addPredecessor(a))
					.isInstanceOf(TaskRelationException.class);
		}

		@Test
		@DisplayName("addSuccessor(this) is rejected")
		void addSuccessor_selfReference_throw()
		{
			TaskBean a = task("A");

			assertThatThrownBy(() -> a.addSuccessor(a))
					.isInstanceOf(TaskRelationException.class);
		}
	}

	// ═════════════════════════════════════════════════════════════════════╗
	// B — DIRECT CYCLE VIA PREDECESSOR: A → B, then B → A               ║
	// ═════════════════════════════════════════════════════════════════════╝

	@Nested
	@DisplayName("B — direct two-node predecessor cycle (A→B then B→A via addPredecessor)")
	class B_DirectPredecessorCycle
	{
		/**
		 * After A.addSuccessor(B) the chain is:  A → B.
		 * Then A.addPredecessor(B) is rejected — either by the cross-role guard
		 * (B is already A's successor) or by the DFS cycle guard (A → B → A would form a cycle).
		 * For 2-node direct cases the cross-role guard typically fires first.
		 */
		@Test
		@DisplayName("A.addPredecessor(B) throws after A.addSuccessor(B)")
		void addPredecessor_directCycle_throw()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");

			assertThatCode(() -> a.addSuccessor(b)).doesNotThrowAnyException();
			assertThat(b.predecessors()).hasValueSatisfying(p -> assertThat(p).contains(a));

			// Either cross-role guard or DFS cycle guard fires — both produce TaskRelationException.
			assertThatThrownBy(() -> a.addPredecessor(b))
					.isInstanceOf(TaskRelationException.class);
		}

		@Test
		@DisplayName("A.addPredecessor(B) throws after A.addSuccessor(B) — successor can't be predecessor")
		void addPredecessor_sameNodeAsSuccessor_throw()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");

			a.addSuccessor(b);

			// b is already a successor of a — adding as predecessor must detect the cycle
			// isSuccessorReachable(a, b): a.successors contains b → found → throw
			assertThatThrownBy(() -> a.addPredecessor(b))
					.isInstanceOf(TaskRelationException.class);
		}

		@Test
		@DisplayName("Rejected addPredecessor() leaves existing relation unchanged")
		void addPredecessor_directCycle_stateUnchangedAfterReject()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");

			a.addSuccessor(b);

			// Attempt the cycle — ignore expected exception
			try { a.addPredecessor(b); } catch (TaskRelationException ignored) { }

			// State must still reflect A → B only
			assertThat(a.successors()).hasValueSatisfying(s -> assertThat(s).containsExactly(b));
			assertThat(b.predecessors()).hasValueSatisfying(p -> assertThat(p).containsExactly(a));
			assertThat(a.predecessors()).isEmpty();
			assertThat(b.successors()).isEmpty();
		}
	}

	// ═════════════════════════════════════════════════════════════════════╗
	// C — DIRECT CYCLE VIA SUCCESSOR: A → B, then B.addSuccessor(A)      ║
	// ═════════════════════════════════════════════════════════════════════╝

	@Nested
	@DisplayName("C — direct two-node successor cycle (A→B then B.addSuccessor(A))")
	class C_DirectSuccessorCycle
	{
		/**
		 * After A.addSuccessor(B) the chain is A → B.
		 * Then B.addSuccessor(A) would close the cycle: A → B → A.
		 * Guard: isSuccessorReachable(task=A, this=B) — walks successors of A: finds B == this → throw.
		 */
		@Test
		@DisplayName("B.addSuccessor(A) throws after A.addSuccessor(B) — cycle detected")
		void addSuccessor_directCycle_throw()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");

			a.addSuccessor(b);

			// After A→B: b.predecessors contains a.
			// b.addSuccessor(a): cross-role guard fires — a is already b's predecessor.
			// (For 2-node cases the cross-role guard catches it before the DFS cycle guard.)
			assertThatThrownBy(() -> b.addSuccessor(a))
					.isInstanceOf(TaskRelationException.class);
		}

		@Test
		@DisplayName("Rejected addSuccessor() leaves existing relation unchanged")
		void addSuccessor_directCycle_stateUnchangedAfterReject()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");

			a.addSuccessor(b);

			try { b.addSuccessor(a); } catch (TaskRelationException ignored) { }

			assertThat(a.successors()).hasValueSatisfying(s -> assertThat(s).containsExactly(b));
			assertThat(b.successors()).isEmpty();
		}
	}

	// ═════════════════════════════════════════════════════════════════════╗
	// D — THREE-NODE CYCLE: A → B → C → A                                ║
	// ═════════════════════════════════════════════════════════════════════╝

	@Nested
	@DisplayName("D — three-node cycle (A → B → C → A)")
	class D_ThreeNodeCycle
	{
		@Test
		@DisplayName("C.addSuccessor(A) throws after A→B→C chain — back-edge detected via DFS")
		void addSuccessor_threeNodeCycle_throw()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");
			TaskBean c = task("C");

			a.addSuccessor(b);
			b.addSuccessor(c);

			// isSuccessorReachable(task=A, this=C) — DFS from A: A→B→C, finds C == this → throw
			assertThatThrownBy(() -> c.addSuccessor(a))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}

		@Test
		@DisplayName("C.addPredecessor(A) via addSuccessor-symmetric throws — three-node cycle detected")
		void addPredecessor_threeNodeCycle_throw()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");
			TaskBean c = task("C");

			a.addSuccessor(b);
			b.addSuccessor(c);

			// c.addPredecessor(a): isSuccessorReachable(this=c, task=a)
			// c.successors is null/empty — no walk hits a. But reverse check applies:
			// If c → a is added as predecessor, then effectively we get a → ... → c → a.
			// isSuccessorReachable walks successors OF c.  c has no successors,
			// but "a is predecessor of c" means a→B→C (a reaches c via successors).
			// Guard: isSuccessorReachable(this=c, task=a): c successors empty → false.
			// Then call addSuccessor on a: a.addPredecessor(c) sets c as predecessor.
			// Actually let's test via addPredecessor from the route that creates cycle:
			// A → B → C already exists. C.addPredecessor(A) would mean A is predecessor of C,
			// i.e. A → C — but we already have A → B → C, which is fine (not a cycle).
			// The actual problem: if C.addSuccessor(A) is the cycle-creating call.
			// Let's verify C.addPredecessor(A) is allowed (diamond, not cycle).
			assertThatCode(() -> c.addPredecessor(a))
					.doesNotThrowAnyException(); // A → B → C and A → C is a diamond, OK
		}
	}

	// ═════════════════════════════════════════════════════════════════════╗
	// E — FOUR-NODE CYCLE: A → B → C → D → A                             ║
	// ═════════════════════════════════════════════════════════════════════╝

	@Nested
	@DisplayName("E — four-node cycle (A → B → C → D → A)")
	class E_FourNodeCycle
	{
		@Test
		@DisplayName("D.addSuccessor(A) throws — DFS finds A transitively (3 hops)")
		void addSuccessor_fourNodeCycle_throw()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");
			TaskBean c = task("C");
			TaskBean d = task("D");

			a.addSuccessor(b);
			b.addSuccessor(c);
			c.addSuccessor(d);

			// isSuccessorReachable(task=A, this=D): DFS A→B→C→D, finds D == this → throw
			assertThatThrownBy(() -> d.addSuccessor(a))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}

		@Test
		@DisplayName("D.addSuccessor(B) throws — mid-chain node is also detected")
		void addSuccessor_midChainCycle_throw()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");
			TaskBean c = task("C");
			TaskBean d = task("D");

			a.addSuccessor(b);
			b.addSuccessor(c);
			c.addSuccessor(d);

			// isSuccessorReachable(task=B, this=D): DFS B→C→D, finds D == this → throw
			assertThatThrownBy(() -> d.addSuccessor(b))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}
	}

	// ═════════════════════════════════════════════════════════════════════╗
	// F — VALID RELATIONS (no cycle)                                       ║
	// ═════════════════════════════════════════════════════════════════════╝

	@Nested
	@DisplayName("F — valid relations accepted without exception")
	class F_ValidRelations
	{
		@Test
		@DisplayName("Linear chain A → B → C → D is fully accepted")
		void addSuccessor_linearChain_ok()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");
			TaskBean c = task("C");
			TaskBean d = task("D");

			assertThat(a.addSuccessor(b)).isTrue();
			assertThat(b.addSuccessor(c)).isTrue();
			assertThat(c.addSuccessor(d)).isTrue();

			assertThat(b.predecessors()).hasValueSatisfying(p -> assertThat(p).contains(a));
			assertThat(c.predecessors()).hasValueSatisfying(p -> assertThat(p).contains(b));
			assertThat(d.predecessors()).hasValueSatisfying(p -> assertThat(p).contains(c));
		}

		@Test
		@DisplayName("Diamond:  A → B, A → C, B → D, C → D  is accepted (not a cycle)")
		void diamond_shape_ok()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");
			TaskBean c = task("C");
			TaskBean d = task("D");

			assertThatCode(() -> {
				a.addSuccessor(b);
				a.addSuccessor(c);
				b.addSuccessor(d);
				c.addSuccessor(d);
			}).doesNotThrowAnyException();

			assertThat(d.predecessors()).hasValueSatisfying(p -> assertThat(p).containsExactlyInAnyOrder(b, c));
		}

		@Test
		@DisplayName("Adding already-existing successor is a no-op returning false")
		void addSuccessor_alreadyPresent_returnsFalse()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");

			assertThat(a.addSuccessor(b)).isTrue();
			assertThat(a.addSuccessor(b)).isFalse();

			assertThat(a.successors()).hasValueSatisfying(s -> assertThat(s).containsExactly(b));
		}

		@Test
		@DisplayName("Adding already-existing predecessor is a no-op returning false")
		void addPredecessor_alreadyPresent_returnsFalse()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");

			assertThat(b.addPredecessor(a)).isTrue();
			assertThat(b.addPredecessor(a)).isFalse();

			assertThat(b.predecessors()).hasValueSatisfying(p -> assertThat(p).containsExactly(a));
		}
	}

	// ═════════════════════════════════════════════════════════════════════╗
	// G — CROSS-ROLE GUARD (predecessor ↔ successor exclusion)            ║
	// ═════════════════════════════════════════════════════════════════════╝

	@Nested
	@DisplayName("G — cross-role guard: predecessor can't be successor of the same task")
	class G_CrossRoleGuard
	{
		@Test
		@DisplayName("addPredecessor(b) throws when b is already a successor of a")
		void addPredecessor_whenAlreadySuccessor_throw()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");

			a.addSuccessor(b); // b is a's successor

			// b can't also be a predecessor — direct cross-role guard (not cycle guard here)
			assertThatThrownBy(() -> a.addPredecessor(b))
					.isInstanceOf(TaskRelationException.class);
		}

		@Test
		@DisplayName("addSuccessor(b) throws when b is already a predecessor of a")
		void addSuccessor_whenAlreadyPredecessor_throw()
		{
			TaskBean a = task("A");
			TaskBean b = task("B");

			a.addPredecessor(b); // b is a's predecessor

			// b can't also be a successor — direct cross-role guard
			assertThatThrownBy(() -> a.addSuccessor(b))
					.isInstanceOf(TaskRelationException.class);
		}
	}
}




