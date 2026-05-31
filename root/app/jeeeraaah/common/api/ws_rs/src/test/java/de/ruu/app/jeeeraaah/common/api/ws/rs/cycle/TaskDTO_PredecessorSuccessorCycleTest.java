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
 * Pure in-memory unit tests for the predecessor/successor cycle-prevention logic in {@link TaskDTO}.
 *
 * <p><strong>No database, no EntityManager, no Docker required.</strong>
 *
 * <hr>
 * <h2>Cycle-guard strategy (DFS on successor edges)</h2>
 * Identical algorithm to {@link TaskBean}: iterative DFS over successor edges before
 * any state mutation. The guard in {@link TaskDTO} is independent of the JPA/Bean
 * guard and provides a second defensive layer at the DTO level.
 */
@DisplayName("TaskDTO — predecessor/successor cycle prevention (in-memory, no DB)")
class TaskDTO_PredecessorSuccessorCycleTest
{
	private TaskGroupDTO group;

	@BeforeEach void setUp() { group = new TaskGroupDTO("test-group"); }

	private TaskDTO task(String name) { return new TaskDTO(group, name); }

	// ═════════════════════════════════════════════════════════════════════╗
	// A — SELF-REFERENCE                                                   ║
	// ═════════════════════════════════════════════════════════════════════╝

	@Nested
	@DisplayName("A — self-reference")
	class A_SelfReference
	{
		@Test
		@DisplayName("addPredecessor(this) is rejected")
		void addPredecessor_selfReference_throw()
		{
			TaskDTO a = task("A");
			assertThatThrownBy(() -> a.addPredecessor(a))
					.isInstanceOf(TaskRelationException.class);
		}

		@Test
		@DisplayName("addSuccessor(this) is rejected")
		void addSuccessor_selfReference_throw()
		{
			TaskDTO a = task("A");
			assertThatThrownBy(() -> a.addSuccessor(a))
					.isInstanceOf(TaskRelationException.class);
		}
	}

	// ═════════════════════════════════════════════════════════════════════╗
	// B — DIRECT CYCLE VIA SUCCESSOR                                       ║
	// ═════════════════════════════════════════════════════════════════════╝

	@Nested
	@DisplayName("B — direct two-node cycle (A → B then B.addSuccessor(A))")
	class B_DirectSuccessorCycle
	{
		@Test
		@DisplayName("B.addSuccessor(A) throws after A.addSuccessor(B)")
		void addSuccessor_directCycle_throw()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");

			a.addSuccessor(b);

			// cross-role guard fires (a is already b's predecessor) before the DFS cycle guard
			assertThatThrownBy(() -> b.addSuccessor(a))
					.isInstanceOf(TaskRelationException.class);
		}

		@Test
		@DisplayName("A.addPredecessor(B) throws after A.addSuccessor(B) — cross-role guard fires")
		void addPredecessor_directCycle_throw()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");

			a.addSuccessor(b);

			// b is already a's successor — cross-role guard fires before DFS cycle guard
			assertThatThrownBy(() -> a.addPredecessor(b))
					.isInstanceOf(TaskRelationException.class);
		}

		@Test
		@DisplayName("State is preserved after rejected addSuccessor()")
		void addSuccessor_directCycle_stateUnchanged()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");

			a.addSuccessor(b);

			try { b.addSuccessor(a); } catch (TaskRelationException ignored) { }

			assertThat(a.successors()).hasValueSatisfying(s -> assertThat(s).containsExactly(b));
			assertThat(b.successors()).isEmpty();
		}
	}

	// ═════════════════════════════════════════════════════════════════════╗
	// C — THREE-NODE CYCLE: A → B → C → A                                 ║
	// ═════════════════════════════════════════════════════════════════════╝

	@Nested
	@DisplayName("C — three-node cycle (A → B → C → A)")
	class C_ThreeNodeCycle
	{
		@Test
		@DisplayName("C.addSuccessor(A) throws after A → B → C chain")
		void addSuccessor_threeNodeCycle_throw()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");
			TaskDTO c = task("C");

			a.addSuccessor(b);
			b.addSuccessor(c);

			assertThatThrownBy(() -> c.addSuccessor(a))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}

		@Test
		@DisplayName("C.addSuccessor(B) throws — immediate predecessor in chain detected")
		void addSuccessor_immediateAncestor_throw()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");
			TaskDTO c = task("C");

			a.addSuccessor(b);
			b.addSuccessor(c);

			// b is c's direct predecessor → cross-role guard fires before DFS cycle guard
			assertThatThrownBy(() -> c.addSuccessor(b))
					.isInstanceOf(TaskRelationException.class);
		}
	}

	// ═════════════════════════════════════════════════════════════════════╗
	// D — FOUR-NODE CYCLE                                                  ║
	// ═════════════════════════════════════════════════════════════════════╝

	@Nested
	@DisplayName("D — four-node cycle (A → B → C → D → A)")
	class D_FourNodeCycle
	{
		@Test
		@DisplayName("D.addSuccessor(A) throws — root ancestor reached after 3 DFS hops")
		void addSuccessor_rootAncestor_throw()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");
			TaskDTO c = task("C");
			TaskDTO d = task("D");

			a.addSuccessor(b);
			b.addSuccessor(c);
			c.addSuccessor(d);

			assertThatThrownBy(() -> d.addSuccessor(a))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}

		@Test
		@DisplayName("D.addSuccessor(B) throws — mid-chain ancestor also detected")
		void addSuccessor_midChainAncestor_throw()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");
			TaskDTO c = task("C");
			TaskDTO d = task("D");

			a.addSuccessor(b);
			b.addSuccessor(c);
			c.addSuccessor(d);

			assertThatThrownBy(() -> d.addSuccessor(b))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}
	}

	// ═════════════════════════════════════════════════════════════════════╗
	// E — VALID RELATIONS                                                  ║
	// ═════════════════════════════════════════════════════════════════════╝

	@Nested
	@DisplayName("E — valid relations accepted without exception")
	class E_ValidRelations
	{
		@Test
		@DisplayName("Linear chain A → B → C → D is accepted")
		void addSuccessor_linearChain_ok()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");
			TaskDTO c = task("C");
			TaskDTO d = task("D");

			assertThat(a.addSuccessor(b)).isTrue();
			assertThat(b.addSuccessor(c)).isTrue();
			assertThat(c.addSuccessor(d)).isTrue();

			assertThat(b.predecessors()).hasValueSatisfying(p -> assertThat(p).contains(a));
			assertThat(c.predecessors()).hasValueSatisfying(p -> assertThat(p).contains(b));
			assertThat(d.predecessors()).hasValueSatisfying(p -> assertThat(p).contains(c));
		}

		@Test
		@DisplayName("Diamond shape A → {B, C} → D is accepted (not a cycle)")
		void diamond_shape_ok()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");
			TaskDTO c = task("C");
			TaskDTO d = task("D");

			assertThatCode(() -> {
				a.addSuccessor(b);
				a.addSuccessor(c);
				b.addSuccessor(d);
				c.addSuccessor(d);
			}).doesNotThrowAnyException();

			assertThat(d.predecessors()).hasValueSatisfying(p -> assertThat(p).containsExactlyInAnyOrder(b, c));
		}

		@Test
		@DisplayName("Adding already-existing successor is idempotent (returns false)")
		void addSuccessor_alreadyPresent_returnsFalse()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");

			assertThat(a.addSuccessor(b)).isTrue();
			assertThat(a.addSuccessor(b)).isFalse();
		}
	}

	// ═════════════════════════════════════════════════════════════════════╗
	// F — CROSS-ROLE GUARD                                                 ║
	// ═════════════════════════════════════════════════════════════════════╝

	@Nested
	@DisplayName("F — cross-role guard: predecessor can't also be successor")
	class F_CrossRoleGuard
	{
		@Test
		@DisplayName("addPredecessor(b) throws when b is already a successor")
		void addPredecessor_whenAlreadySuccessor_throw()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");

			a.addSuccessor(b);

			assertThatThrownBy(() -> a.addPredecessor(b))
					.isInstanceOf(TaskRelationException.class);
		}

		@Test
		@DisplayName("addSuccessor(b) throws when b is already a predecessor")
		void addSuccessor_whenAlreadyPredecessor_throw()
		{
			TaskDTO a = task("A");
			TaskDTO b = task("B");

			a.addPredecessor(b);

			assertThatThrownBy(() -> a.addSuccessor(b))
					.isInstanceOf(TaskRelationException.class);
		}
	}
}




