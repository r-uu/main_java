package de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ruu.app.jeeeraaah.backend.persistence.jpa.entity.TaskGroupJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.entity.TaskJPA;
import de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException;

/**
 * Pure in-memory unit tests for the super-task / sub-task hierarchy cycle-prevention
 * logic in {@link TaskJPA}.
 *
 * <p><strong>No database, no EntityManager, no Docker required.</strong>
 * Every {@link TaskJPA} is instantiated directly in heap memory; no
 * {@code jakarta.persistence} lifecycle is involved.
 *
 * <hr>
 * <h2>Data model recap</h2>
 * Each {@code TaskJPA} holds:
 * <ul>
 *   <li>one nullable {@code superTask} reference — <em>the parent</em></li>
 *   <li>a set of {@code subTasks}           — <em>the children</em></li>
 * </ul>
 * Together they form a <em>tree</em> (one parent per node, any number of children).
 * A <em>cycle</em> occurs when following the {@code superTask} chain from any node
 * eventually revisits that same node.
 *
 * <hr>
 * <h2>Cycle guard: ancestor walk (O(depth))</h2>
 * {@link TaskJPA#superTask(TaskJPA)} walks <em>upward</em> from the proposed
 * new parent via {@code superTask} pointers. If {@code this} is found anywhere
 * in that chain, the operation is rejected with {@link TaskRelationException}.
 * All checks run <em>before</em> any mutation — the entity graph is left intact
 * on a rejected operation. Because each node has at most one parent, the walk
 * visits at most one path — no DFS required.
 *
 * <hr>
 * <h2>Why "remove from old parent" is NOT sufficient</h2>
 * A common misconception: <em>"just detach the child from its current parent before
 * re-attaching it; that prevents duplicate links and thus prevents cycles."</em>
 * <p>
 * This reasoning is <strong>wrong</strong>:
 * <ol>
 *   <li>The child might currently have <strong>no</strong> parent at all (it is a root
 *       node). In that case there is nothing to remove. Yet the proposed link can still
 *       form a cycle if the child is an ancestor of the proposed parent.</li>
 *   <li>Even if there is an old parent to remove from, detaching only removes a link in
 *       one direction. It says nothing about whether the child is reachable upward from
 *       the new parent.</li>
 * </ol>
 * {@link BrokenHierarchyNode} provides a stand-alone reproduction of both algorithms
 * (broken and correct) as pure POJOs so the contrast can be asserted independently of
 * any JPA plumbing.
 *
 * <hr>
 * <h2>Lazy loading note (in-memory)</h2>
 * In these tests, {@code superTask} is a plain Java object reference.
 * There is no Hibernate proxy, no persistence context, and therefore no lazy loading.
 * For the lazy-loading impact on the ancestor walk see
 * {@link TaskJPA_HierarchyCycle_JpaIntegrationTest}.
 *
 * <hr>
 * <h2>Historical note: why these tests initially fail without a fresh build</h2>
 * When first run against an older build of {@code persistence.jpa} (i.e. before the
 * ancestor-walk guard was compiled and installed into the local Maven repository),
 * all tests in sections B – G that expect a {@link TaskRelationException} fail with
 * <em>"Expecting code to raise a throwable."</em>
 * This failure state is itself a documentation artefact: it reveals exactly which
 * contracts are missing from an implementation that lacks the cycle check.
 * <p>
 * After running {@code mvn install -DskipTests} on the {@code persistence.jpa} module
 * all tests pass, confirming that the ancestor-walk guard is both necessary and
 * sufficient for the in-memory case.
 * <p>
 * <strong>Key lesson for JPMS multi-module projects:</strong> tests in module B that
 * exercise classes from module A always use the <em>installed artifact</em> of A
 * (from {@code ~/.m2/repository}), not A's source directory. Running
 * {@code mvn test -f B/pom.xml} without first reinstalling A may therefore silently
 * test stale, pre-fix code — causing exactly the "my approach doesn't work" confusion
 * that this test package was designed to document.
 */
@DisplayName("TaskJPA — sub-task hierarchy cycle prevention (in-memory, no DB)")
class TaskJPA_HierarchyCycle_InMemoryTest
{
	private TaskGroupJPA group;

	@BeforeEach
	void setUp()
	{
		// A single group is enough; it has no effect on the cycle-detection logic itself.
		group = new TaskGroupJPA("test-group");
	}

	/** Shortcut: create a new in-memory task in the shared group. */
	private TaskJPA task(String name) { return new TaskJPA(group, name); }

	// ═══════════════════════════════════════════════════════════════════════════
	// A — SELF-REFERENCE
	// ═══════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("A — self-reference (task → itself)")
	class A_SelfReference
	{
		@Test
		@DisplayName("addSubTask(this) is rejected immediately")
		void addSubTask_selfReference_throw()
		{
			TaskJPA a = task("A");

			assertThatThrownBy(() -> a.addSubTask(a))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("itself");
		}

		@Test
		@DisplayName("superTask(this) is rejected immediately")
		void superTask_selfReference_throw()
		{
			TaskJPA a = task("A");

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
		 * Setup:  {@code A.addSubTask(B)} establishes B as a child of A.
		 * Attack:  {@code B.addSubTask(A)} tries to make A a child of B.
		 * <p>
		 * Expected: the ancestor walk in {@code B.addSubTask(A)} immediately finds A
		 * at {@code cursor = B.superTask} and throws.
		 *
		 * <pre>
		 * cursor starts at:  B.superTask = A
		 * First iteration:   cursor (= A) == task (= A)  → throw TaskRelationException
		 * </pre>
		 */
		@Test
		@DisplayName("B.addSubTask(A) throws after A.addSubTask(B) succeeded")
		void addSubTask_directCycle_throw()
		{
			TaskJPA a = task("A");
			TaskJPA b = task("B");

			// Establish A → B (valid)
			assertThatCode(() -> a.addSubTask(b)).doesNotThrowAnyException();
			assertThat(b.superTask()).hasValue(a);

			// B adopts A — must be rejected: A is already B's ancestor (its superTask)
			assertThatThrownBy(() -> b.addSubTask(a))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}

		@Test
		@DisplayName("a.superTask(b) throws after b.superTask(a): superTask() delegates to addSubTask()")
		void superTask_setter_directCycle_throw()
		{
			TaskJPA a = task("A");
			TaskJPA b = task("B");

			// Link via superTask() setter on B
			assertThatCode(() -> b.superTask(a)).doesNotThrowAnyException();
			assertThat(b.superTask()).hasValue(a);

			// superTask() internally calls task.addSubTask(this), so the cycle guard fires
			assertThatThrownBy(() -> a.superTask(b))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}

		/**
		 * State remains clean after a rejected {@code addSubTask()}: the original
		 * parent/child relation is not modified by the failed call.
		 */
		@Test
		@DisplayName("Rejected addSubTask() leaves existing hierarchy unchanged")
		void addSubTask_directCycle_stateUnchangedAfterReject()
		{
			TaskJPA a = task("A");
			TaskJPA b = task("B");

			a.addSubTask(b);

			// Attempt the cycle — ignore the expected exception
			try { b.addSubTask(a); } catch (TaskRelationException ignored) { }

			// State must still reflect the original A → B link only
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
		 * Build chain A → B → C first, then try C.addSubTask(A).
		 *
		 * <pre>
		 * cursor starts at:  C.superTask = B
		 * Iteration 1:       B != A  → cursor = B.superTask = A
		 * Iteration 2:       A == A  → throw TaskRelationException
		 * </pre>
		 */
		@Test
		@DisplayName("C.addSubTask(A) throws after A → B → C chain was built")
		void addSubTask_threeNodeCycle_throw()
		{
			TaskJPA a = task("A");
			TaskJPA b = task("B");
			TaskJPA c = task("C");

			a.addSubTask(b);
			b.addSubTask(c);

			assertThat(c.superTask()).hasValue(b);
			assertThat(b.superTask()).hasValue(a);

			assertThatThrownBy(() -> c.addSubTask(a))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}

		@Test
		@DisplayName("C.addSubTask(B) throws — B is immediate ancestor of C")
		void addSubTask_immediateAncestor_throw()
		{
			TaskJPA a = task("A");
			TaskJPA b = task("B");
			TaskJPA c = task("C");

			a.addSubTask(b);
			b.addSubTask(c);

			// C tries to adopt its own parent B
			assertThatThrownBy(() -> c.addSubTask(b))
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
		 * Build chain A → B → C → D, then try D.addSubTask(A).
		 *
		 * <pre>
		 * cursor starts at:  D.superTask = C
		 * Iteration 1:       C != A  → cursor = C.superTask = B
		 * Iteration 2:       B != A  → cursor = B.superTask = A
		 * Iteration 3:       A == A  → throw TaskRelationException
		 * </pre>
		 * Three iterator steps demonstrate that the walk ascends all the way to the root.
		 */
		@Test
		@DisplayName("D.addSubTask(A) throws after A → B → C → D chain — root ancestor detected")
		void addSubTask_rootAncestor_throw()
		{
			TaskJPA a = task("A");
			TaskJPA b = task("B");
			TaskJPA c = task("C");
			TaskJPA d = task("D");

			a.addSubTask(b);
			b.addSubTask(c);
			c.addSubTask(d);

			assertThatThrownBy(() -> d.addSubTask(a))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}

		@Test
		@DisplayName("D.addSubTask(B) throws — mid-chain ancestor is also detected")
		void addSubTask_midChainAncestor_throw()
		{
			TaskJPA a = task("A");
			TaskJPA b = task("B");
			TaskJPA c = task("C");
			TaskJPA d = task("D");

			a.addSubTask(b);
			b.addSubTask(c);
			c.addSubTask(d);

			// B is mid-chain, not the root — the walk must still find it
			assertThatThrownBy(() -> d.addSubTask(b))
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
		void addSubTask_linearChain_ok()
		{
			TaskJPA a = task("A");
			TaskJPA b = task("B");
			TaskJPA c = task("C");
			TaskJPA d = task("D");

			assertThat(a.addSubTask(b)).isTrue();
			assertThat(b.addSubTask(c)).isTrue();
			assertThat(c.addSubTask(d)).isTrue();

			assertThat(b.superTask()).hasValue(a);
			assertThat(c.superTask()).hasValue(b);
			assertThat(d.superTask()).hasValue(c);
		}

		@Test
		@DisplayName("Tree with siblings:  A → {B, C},  B → D  is accepted")
		void addSubTask_treeWithSiblings_ok()
		{
			TaskJPA a = task("A");
			TaskJPA b = task("B");
			TaskJPA c = task("C");
			TaskJPA d = task("D");

			assertThat(a.addSubTask(b)).isTrue();
			assertThat(a.addSubTask(c)).isTrue();
			assertThat(b.addSubTask(d)).isTrue();

			assertThat(b.superTask()).hasValue(a);
			assertThat(c.superTask()).hasValue(a);
			assertThat(d.superTask()).hasValue(b);

			// Siblings C and D from different branches are NOT related
			assertThat(c.superTask()).hasValue(a);
			assertThat(d.superTask()).hasValue(b);
		}

		@Test
		@DisplayName("Adding a child that already belongs to this node returns false (no-op, no exception)")
		void addSubTask_alreadyPresent_returnsFalseNoException()
		{
			TaskJPA a = task("A");
			TaskJPA b = task("B");

			assertThat(a.addSubTask(b)).isTrue();

			// Second add of the same child: idempotent no-op
			assertThat(a.addSubTask(b)).isFalse();
			assertThat(b.superTask()).hasValue(a); // relation still intact
		}

		@Test
		@DisplayName("Root task with no super task:  addSubTask returns true for any valid child")
		void rootTask_noSuperTask_canAlwaysAcceptChildren()
		{
			TaskJPA root  = task("root");
			TaskJPA child = task("child");

			assertThat(root.superTask()).isEmpty();
			assertThat(root.addSubTask(child)).isTrue();
			assertThat(child.superTask()).hasValue(root);
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// F — BROKEN APPROACH DOCUMENTED
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * This nested class contrasts the <em>broken</em> "remove-from-old-parent" strategy
	 * against the <em>correct</em> ancestor-walk strategy, using {@link BrokenHierarchyNode}
	 * (a pure POJO) and — finally — {@link TaskJPA} itself to verify it rejects the
	 * same cycle that the broken approach would silently allow.
	 */
	@Nested
	@DisplayName("F — broken 'remove-from-old-parent' approach does NOT prevent cycles")
	class F_BrokenApproachDocumentation
	{
		/**
		 * Shows that {@link BrokenHierarchyNode#addChild_BROKEN} silently creates an
		 * {@code A ↔ B} cycle when the proposed child has no pre-existing parent.
		 *
		 * <pre>
		 * Step 1:  A.addChild_BROKEN(B)
		 *          → B.parent is null → "remove" step no-op
		 *          → B.parent = A,  A.children = [B]
		 *
		 * Step 2:  B.addChild_BROKEN(A)
		 *          → A.parent is null → "remove" step no-op again
		 *          → A.parent = B,  B.children = [A]
		 *
		 * Result:  A.parent = B  AND  B.parent = A  → CYCLE
		 * </pre>
		 */
		@Test
		@DisplayName("BROKEN: direct cycle A ↔ B is created silently when A has no old parent")
		void brokenApproach_directCycle_createdSilently()
		{
			BrokenHierarchyNode a = new BrokenHierarchyNode("A");
			BrokenHierarchyNode b = new BrokenHierarchyNode("B");

			// Step 1: valid — A becomes parent of B
			a.addChild_BROKEN(b);
			assertThat(b.parent).as("B.parent should be A after step 1").isSameAs(a);

			// Step 2: CRITICAL
			// A.parent == null → the "remove" guard is a no-op.
			// The broken implementation blindly sets A.parent = B.
			b.addChild_BROKEN(a);

			// Cycle is silently present now:
			assertThat(a.parent)
					.as("A.parent is now B — CYCLE created without any exception")
					.isSameAs(b);
			assertThat(b.parent)
					.as("B.parent is still A")
					.isSameAs(a);

			// The cycle is observable via hasAncestor():
			//   a.hasAncestor(a): cursor = a.parent (B) → not A; cursor = B.parent (A) → IS A → true
			assertThat(a.hasAncestor(a))
					.as("A is now reachable from itself via the cycle → infinite-loop territory!")
					.isTrue();
		}

		/**
		 * Shows that {@link BrokenHierarchyNode#addChild_BROKEN} also fails for a longer
		 * chain: A → B → C, then C.addChild_BROKEN(A).
		 *
		 * <pre>
		 * After building A → B → C:
		 *   A.parent = null,  B.parent = A,  C.parent = B
		 *
		 * C.addChild_BROKEN(A):
		 *   → A.parent is null → "remove" step no-op
		 *   → A.parent = C,  C.children gets A
		 *
		 * Result: A.parent = C, B.parent = A, C.parent = B → CYCLE A → B → C → A
		 * </pre>
		 */
		@Test
		@DisplayName("BROKEN: three-node cycle A → B → C → A is also created silently")
		void brokenApproach_threeNodeCycle_createdSilently()
		{
			BrokenHierarchyNode a = new BrokenHierarchyNode("A");
			BrokenHierarchyNode b = new BrokenHierarchyNode("B");
			BrokenHierarchyNode c = new BrokenHierarchyNode("C");

			a.addChild_BROKEN(b);  // A → B
			b.addChild_BROKEN(c);  // B → C

			// A has no parent → "remove" is a no-op → A.parent = C → CYCLE
			c.addChild_BROKEN(a);

			assertThat(a.parent).as("A.parent is now C — CYCLE A → B → C → A").isSameAs(c);
			assertThat(a.hasAncestor(a)).as("A reachable from itself via three-node cycle").isTrue();
		}

		/**
		 * Shows that {@link BrokenHierarchyNode#addChild_CORRECT} correctly rejects the
		 * same direct cycle that {@link BrokenHierarchyNode#addChild_BROKEN} allows.
		 */
		@Test
		@DisplayName("CORRECT ancestor-walk: same A ↔ B cycle is detected and rejected")
		void correctApproach_directCycle_rejected()
		{
			BrokenHierarchyNode a = new BrokenHierarchyNode("A");
			BrokenHierarchyNode b = new BrokenHierarchyNode("B");

			a.addChild_CORRECT(b); // valid

			assertThatThrownBy(() -> b.addChild_CORRECT(a))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("cycle");

			// State is clean: the failed call made no mutations
			assertThat(a.parent).as("A must still have no parent").isNull();
			assertThat(b.parent).as("B.parent is still A").isSameAs(a);
		}

		/**
		 * Verifies that {@link TaskJPA#addSubTask(TaskJPA)} also rejects the exact same
		 * scenario that the broken approach would silently allow.
		 * <p>
		 * This is the definitive test confirming that {@link TaskJPA} uses the
		 * ancestor-walk strategy internally and <strong>not</strong> a simple
		 * "remove-from-old-parent" approach.
		 */
		@Test
		@DisplayName("TaskJPA.addSubTask(): rejects the exact cycle that the broken approach would allow")
		void taskJPA_addSubTask_rejectsCycleThatBrokenApproachAllows()
		{
			TaskJPA a = task("A");
			TaskJPA b = task("B");

			// Same scenario as the broken-approach test above
			a.addSubTask(b); // valid

			// If TaskJPA used the broken "remove-from-old-parent" approach, this would silently
			// create a cycle. With the ancestor walk it must throw.
			assertThatThrownBy(() -> b.addSubTask(a))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// G — STATE CONSISTENCY: check-first guarantees atomic rejection
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Documents that {@link TaskJPA#superTask(TaskJPA)} uses a <em>check-first,
	 * mutate-second</em> strategy that is naturally atomic with respect to cycle
	 * detection:
	 *
	 * <ol>
	 *   <li>All validations (self-reference guard, predecessor/successor guard,
	 *       ancestor-walk cycle guard) are performed <strong>before</strong> any
	 *       state change.</li>
	 *   <li>If any check throws, <em>no mutation has occurred</em> — the entity
	 *       graph remains in its previous consistent state.</li>
	 *   <li>Only when all checks pass does the method remove {@code this} from
	 *       the old parent and add it to the new parent.</li>
	 * </ol>
	 *
	 * <p>Contrast this with the "remove-first" design described in
	 * {@link BrokenHierarchyNode}, which would detach the child from its current
	 * parent before verifying the cycle guard, thereby orphaning the entity on a
	 * failed operation.
	 *
	 * <h2>Scenario</h2>
	 * Chain: A → B → C.  We call {@code B.superTask(C)} — attempting to make C the
	 * parent of B. This would create a cycle (B is already an ancestor of C).
	 * <ul>
	 *   <li>Ancestor walk starts at {@code C} (the proposed new super task).</li>
	 *   <li>First step: {@code C.superTask = B} — <em>found</em> → cycle detected → throws.</li>
	 *   <li>No mutation has happened → B is still a child of A, and C is still a child of B.</li>
	 * </ul>
	 */
	@Nested
	@DisplayName("G — STATE CONSISTENCY: superTask() check-first leaves state intact on cycle exception")
	class G_SuperTaskSetterStateConsistency
	{
		@Test
		@DisplayName("B.superTask(C) throws — state is fully preserved (no orphaning)")
		void superTask_cycleThrows_statePreserved()
		{
			TaskJPA a = task("A");
			TaskJPA b = task("B");
			TaskJPA c = task("C");

			// Build valid chain A → B → C
			a.addSubTask(b);
			b.addSubTask(c);

			// Pre-condition checks
			assertThat(b.superTask()).as("initial: B.superTask is A").hasValue(a);
			assertThat(c.superTask()).as("initial: C.superTask is B").hasValue(b);
			assertThat(a.subTasks()).hasValueSatisfying(s ->
					assertThat(s).as("initial: A.subTasks contains B").contains(b));

			// Attempt to make C the parent of B:
			// Ancestor walk from C: C.superTask = B → B == B (this) → cycle detected → throw.
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


