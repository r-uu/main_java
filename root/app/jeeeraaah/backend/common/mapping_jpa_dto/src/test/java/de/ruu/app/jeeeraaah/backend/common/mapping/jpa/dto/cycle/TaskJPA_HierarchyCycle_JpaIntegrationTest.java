package de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto.AbstractJPATest;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.entity.TaskGroupJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.entity.TaskJPA;
import de.ruu.app.jeeeraaah.common.api.domain.TaskRelationException;
import de.ruu.lib.junit.DisabledOnServerNotListening;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUtil;

/**
 * JPA integration tests for the super-task / sub-task hierarchy cycle prevention in
 * {@link TaskJPA}, combining real PostgreSQL persistence with a focus on
 * <strong>lazy-loading behaviour</strong>.
 *
 * <p>Tests are automatically skipped when PostgreSQL is not reachable.
 *
 * <hr>
 * <h2>Lazy loading and the ancestor walk: the core issue</h2>
 *
 * The cycle guard in {@link TaskJPA#addSubTask} walks upward from the proposed
 * parent via <em>direct Java field access</em>:
 *
 * <pre>{@code
 * TaskJPA cursor = this.superTask;   // direct field read
 * while (cursor != null) {
 *     if (cursor.equals(task)) throw ...
 *     cursor = cursor.superTask;     // ← direct field read on cursor
 * }
 * }</pre>
 *
 * This is safe as long as {@code cursor} is a <em>real, fully-initialized</em>
 * {@code TaskJPA} instance. When {@code cursor} is a Hibernate <em>lazy proxy</em>
 * (a CGLIB subclass of {@code TaskJPA}), things change:
 *
 * <ul>
 *   <li>CGLIB intercepts <em>method calls</em> to delegate to the real entity.</li>
 *   <li>CGLIB does <strong>not</strong> intercept <em>direct field reads</em>.</li>
 *   <li>Direct field reads on the proxy object itself always return the proxy's own
 *       field value, which is {@code null} for all JPA-managed association fields
 *       (the actual data lives in the proxy's internalized target, not in the
 *       proxy's own fields).</li>
 * </ul>
 *
 * <strong>Consequence:</strong> if {@code superTask} were mapped as
 * {@code @ManyToOne(fetch = FetchType.LAZY)}, the ancestor walk would silently
 * terminate at the first lazy proxy it encounters ({@code cursor.superTask} returns
 * {@code null} from the proxy field), making the cycle guard ineffective for
 * any chain longer than one hop.
 *
 * <h2>Current mapping: EAGER (safe)</h2>
 *
 * {@code TaskJPA.superTask} is mapped as plain {@code @ManyToOne} <em>without</em>
 * an explicit {@code fetch} attribute. The JPA 3.x specification mandates
 * {@code FetchType.EAGER} as the default for {@code @ManyToOne}. Hibernate honours
 * this default (without bytecode enhancement) by loading the superTask via a
 * join or a secondary SELECT immediately when the owning entity is loaded.
 * <p>
 * Therefore every {@code TaskJPA} returned by {@code entityManager.find()} has its
 * {@code superTask} field set to a real, fully-initialized entity (or {@code null}),
 * and the ancestor walk via direct field access is correct.
 *
 * <h2>Risk: adding {@code fetch = FetchType.LAZY} in the future</h2>
 *
 * If a developer ever annotates {@code superTask} with
 * {@code @ManyToOne(fetch = FetchType.LAZY)}, the direct field access
 * {@code cursor.superTask} in the ancestor walk would become unreliable:
 * <ul>
 *   <li>Every node in the chain except the immediate superTask of the initially
 *       loaded entity would be a proxy.</li>
 *   <li>{@code cursor.superTask} on those proxies returns {@code null}
 *       (proxy field, not entity field).</li>
 *   <li>The walk terminates prematurely — cycles are no longer detected.</li>
 * </ul>
 * <p>
 * The reliable fix is to replace {@code cursor.superTask} (field) with
 * {@code cursor.superTask().orElse(null)} (the getter), which triggers proxy
 * initialization through Hibernate's CGLIB interceptor.
 *
 * @see TaskJPA_HierarchyCycle_InMemoryTest in-memory tests without JPA/DB
 * @see BrokenHierarchyNode broken vs. correct algorithm documentation
 */
@DisabledOnServerNotListening(propertyNameHost = "database.host", propertyNamePort = "database.port")
@DisplayName("TaskJPA — hierarchy cycle prevention (JPA integration, requires PostgreSQL)")
class TaskJPA_HierarchyCycle_JpaIntegrationTest extends AbstractJPATest
{
	private final PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();

	/** Helper: persist a new group and return it. */
	private TaskGroupJPA group(String name)
	{
		return persistAndFlush(new TaskGroupJPA(name));
	}

	/** Helper: create (but do not persist) a task in the given group. */
	private TaskJPA task(TaskGroupJPA g, String name)
	{
		return new TaskJPA(g, name);
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// A — superTask EAGER loading verification
	// ═══════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("A — @ManyToOne superTask is EAGER by JPA default (prerequisite for safe ancestor walk)")
	class A_EagerLoadingVerification
	{
		/**
		 * Verifies that after {@code entityManager.clear()} and {@code entityManager.find()},
		 * the {@code superTask} field is already loaded (not a proxy) without any explicit
		 * fetch call.
		 * <p>
		 * This is the fundamental prerequisite for the direct-field-access ancestor walk
		 * in {@link TaskJPA#addSubTask} to work correctly.
		 */
		@Test
		@DisplayName("After clear() + find(), superTask is LOADED without explicit prefetch")
		void find_afterClearAndPersist_superTaskIsEagerlyLoaded()
		{
			TaskGroupJPA g       = group("group-eager");
			TaskJPA      parent  = new TaskJPA(g, "parent");
			TaskJPA      child   = new TaskJPA(g, "child");

			child.superTask(parent);
			persistAndFlush(parent);
			persistAndFlush(child);

			clearPersistenceContext();

			// Reload — no explicit join/fetch hint; JPA default: EAGER
			TaskJPA reloaded = find(TaskJPA.class, child.id());

			assertThat(persistenceUtil.isLoaded(reloaded, "superTask"))
					.as("superTask must be loaded by default (@ManyToOne → EAGER)")
					.isTrue();

			assertThat(reloaded.superTask())
					.as("superTask must be present after eager load")
					.isPresent();

			assertThat(reloaded.superTask().orElseThrow().name())
					.as("superTask name must match persisted value")
					.isEqualTo("parent");
		}

		/**
		 * Shows that the EAGER load is transitive: loading the innermost entity of a
		 * A → B → C chain populates the superTask of each intermediate node, so the
		 * entire ancestor chain is available in memory without extra queries.
		 *
		 * <p>This is crucial because the ancestor walk in {@code addSubTask} traverses the
		 * chain step by step ({@code cursor = cursor.superTask}). If any step required a
		 * lazy load, the walk would silently stop at the first uninitialized proxy with
		 * direct field access.
		 */
		@Test
		@DisplayName("After clear() + find(C) in A→B→C chain, full ancestor chain is loaded")
		void find_deepChain_fullAncestorChainIsEagerlyLoaded()
		{
			TaskGroupJPA g = group("group-chain");
			TaskJPA      a = new TaskJPA(g, "A");
			TaskJPA      b = new TaskJPA(g, "B");
			TaskJPA      c = new TaskJPA(g, "C");

			a.addSubTask(b);
			b.addSubTask(c);

			persistAndFlush(a);
			persistAndFlush(b);
			persistAndFlush(c);

			clearPersistenceContext();

			// Load only the deepest node
			TaskJPA reloadedC = find(TaskJPA.class, c.id());

			// Verify the chain is fully loaded without explicit prefetch
			assertThat(persistenceUtil.isLoaded(reloadedC, "superTask"))
					.as("C.superTask (B) must be eagerly loaded")
					.isTrue();

			TaskJPA reloadedB = reloadedC.superTask().orElseThrow();

			assertThat(persistenceUtil.isLoaded(reloadedB, "superTask"))
					.as("B.superTask (A) must also be eagerly loaded (transitive)")
					.isTrue();

			assertThat(reloadedB.superTask().orElseThrow().name())
					.as("B.superTask should be A")
					.isEqualTo("A");
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// B — Cycle detection on reloaded entities
	// ═══════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("B — Cycle detection works on entities reloaded from DB (eager superTask)")
	class B_CycleDetectionAfterReload
	{
		/**
		 * Persists A → B, clears the persistence context, reloads B, then verifies
		 * that trying to add A as a child of reloaded B throws a
		 * {@link TaskRelationException}.
		 * <p>
		 * This confirms the ancestor walk correctly traverses the eagerly-loaded
		 * {@code superTask} chain of a freshly-reloaded entity.
		 */
		@Test
		@DisplayName("Direct cycle A→B→A: rejected on reloaded B after clear()+find()")
		void directCycle_rejectedOnReloadedEntity()
		{
			TaskGroupJPA g      = group("group-reload-direct");
			TaskJPA      a      = new TaskJPA(g, "A");
			TaskJPA      b      = new TaskJPA(g, "B");
			Long         aId;
			Long         bId;

			a.addSubTask(b);
			persistAndFlush(a);
			persistAndFlush(b);
			aId = a.id();
			bId = b.id();

			clearPersistenceContext();

			TaskJPA reloadedA = find(TaskJPA.class, aId);
			TaskJPA reloadedB = find(TaskJPA.class, bId);

			// Sanity: reloaded B must have A as superTask (via eager load)
			assertThat(reloadedB.superTask()).hasValueSatisfying(st ->
					assertThat(st.name()).isEqualTo("A"));

			// Cycle attempt: B adopts A — ancestor walk: cursor = B.superTask = A → A==A → throw
			assertThatThrownBy(() -> reloadedB.addSubTask(reloadedA))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}

		/**
		 * Persists A → B → C, clears, reloads all three, then verifies that C cannot
		 * adopt A (root ancestor).
		 * <p>
		 * The walk must traverse two hops: {@code C.superTask = B}, {@code B.superTask = A}.
		 * Both are eagerly loaded, so the direct field access succeeds.
		 */
		@Test
		@DisplayName("Three-node cycle A→B→C→A: rejected on reloaded C after clear()+find()")
		void threeNodeCycle_rejectedOnReloadedEntity()
		{
			TaskGroupJPA g = group("group-reload-3");
			TaskJPA      a = new TaskJPA(g, "A");
			TaskJPA      b = new TaskJPA(g, "B");
			TaskJPA      c = new TaskJPA(g, "C");

			a.addSubTask(b);
			b.addSubTask(c);

			persistAndFlush(a);
			persistAndFlush(b);
			persistAndFlush(c);

			Long aId = a.id(), bId = b.id(), cId = c.id();
			clearPersistenceContext();

			TaskJPA reloadedA = find(TaskJPA.class, aId);
			TaskJPA reloadedC = find(TaskJPA.class, cId);

			assertThatThrownBy(() -> reloadedC.addSubTask(reloadedA))
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}

		/**
		 * Persists A → B → C → D, clears, reloads D and A, then:
		 * <ol>
		 *   <li>Verifies D cannot adopt A (walks three hops: D→C→B→A).</li>
		 *   <li>Verifies D cannot adopt B (mid-chain ancestor).</li>
		 * </ol>
		 */
		@Test
		@DisplayName("Four-node cycle: root and mid-chain ancestor both rejected after clear()+find()")
		void fourNodeCycle_bothRootAndMidChain_rejected()
		{
			TaskGroupJPA g = group("group-reload-4");
			TaskJPA      a = new TaskJPA(g, "A");
			TaskJPA      b = new TaskJPA(g, "B");
			TaskJPA      c = new TaskJPA(g, "C");
			TaskJPA      d = new TaskJPA(g, "D");

			a.addSubTask(b);
			b.addSubTask(c);
			c.addSubTask(d);

			persistAndFlush(a);
			persistAndFlush(b);
			persistAndFlush(c);
			persistAndFlush(d);

			Long aId = a.id(), bId = b.id(), dId = d.id();
			clearPersistenceContext();

			TaskJPA reloadedA = find(TaskJPA.class, aId);
			TaskJPA reloadedB = find(TaskJPA.class, bId);
			TaskJPA reloadedD = find(TaskJPA.class, dId);

			assertThatThrownBy(() -> reloadedD.addSubTask(reloadedA))
					.as("D.addSubTask(A) must be rejected — A is root ancestor of D")
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");

			assertThatThrownBy(() -> reloadedD.addSubTask(reloadedB))
					.as("D.addSubTask(B) must be rejected — B is a mid-chain ancestor of D")
					.isInstanceOf(TaskRelationException.class)
					.hasMessageContaining("cycle");
		}

		/**
		 * Verifies that a valid hierarchy restructuring is accepted after reload:
		 * detach C from B and attach it to A instead (A → {B, C}).
		 *
		 * <p>This is the non-cycle path that must still work correctly to verify
		 * the guard does not produce false positives.
		 */
		@Test
		@DisplayName("Valid restructure: moving C from B to A (A→{B,C}) works after reload")
		void validRestructure_moveChildToOtherParent_accepted()
		{
			TaskGroupJPA g = group("group-restructure");
			TaskJPA      a = new TaskJPA(g, "A");
			TaskJPA      b = new TaskJPA(g, "B");
			TaskJPA      c = new TaskJPA(g, "C");

			a.addSubTask(b);
			b.addSubTask(c);

			persistAndFlush(a);
			persistAndFlush(b);
			persistAndFlush(c);

			Long aId = a.id(), cId = c.id();
			clearPersistenceContext();

			TaskJPA reloadedA = find(TaskJPA.class, aId);
			TaskJPA reloadedC = find(TaskJPA.class, cId);

			// Detach C from B, attach to A — forming A → {B, C}
			reloadedC.superTask(reloadedA);

			assertThat(reloadedC.superTask())
					.as("C.superTask should now be A")
					.hasValueSatisfying(st -> assertThat(st.name()).isEqualTo("A"));
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// C — Lazy loading risk: direct field access vs. getter method
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Documents the interaction between Hibernate lazy proxies and direct field
	 * access, as used in the {@link TaskJPA#addSubTask} cycle guard.
	 *
	 * <p>These tests do <em>not</em> modify the {@code @ManyToOne} mapping.
	 * Instead, they demonstrate the raw proxy behaviour using
	 * {@code entityManager.getReference()}, which always returns an
	 * <strong>uninitialized proxy</strong> — regardless of the fetch type.
	 */
	@Nested
	@DisplayName("C — Lazy proxy demonstration: direct field access vs. getter method")
	class C_LazyProxyDirectFieldAccessRisk
	{
		/**
		 * {@code entityManager.getReference()} returns an uninitialized Hibernate proxy.
		 * Accessing the proxy's {@code superTask} field directly would yield {@code null}
		 * (proxy's own uninitialised field), whereas calling the getter method
		 * {@code superTask()} triggers proxy initialization and yields the actual value.
		 *
		 * <p><strong>This is the exact failure mode that would occur in the ancestor walk
		 * if {@code superTask} were mapped as {@code FetchType.LAZY}:</strong>
		 * <ol>
		 *   <li>The first node in the walk is the loaded entity — its field is fine.</li>
		 *   <li>Its superTask, however, would be a lazy proxy.</li>
		 *   <li>{@code cursor.superTask} (direct field on the proxy) → {@code null}.</li>
		 *   <li>The loop exits without walking further — cycle undetected.</li>
		 * </ol>
		 *
		 * <p>The safe fix would be to replace direct field access with the getter:
		 * <pre>{@code
		 * // Current (risky for FetchType.LAZY):
		 * cursor = cursor.superTask;           // direct field — fails on proxies
		 *
		 * // Safe alternative:
		 * cursor = cursor.superTask().orElse(null);  // getter — triggers proxy init
		 * }</pre>
		 */
		@Test
		@DisplayName("getReference() proxy: getter triggers init; direct field access via reflection returns null")
		void proxy_getterTriggersInit_directFieldReturnsNull() throws Exception
		{
			TaskGroupJPA g      = group("group-proxy");
			TaskJPA      parent = new TaskJPA(g, "proxy-parent");
			TaskJPA      child  = new TaskJPA(g, "proxy-child");

			child.superTask(parent);
			persistAndFlush(parent);
			persistAndFlush(child);

			Long childId = child.id();
			clearPersistenceContext();

			// getReference() ALWAYS returns an uninitialized proxy — simulates lazy loading
			TaskJPA proxy = entityManager.getReference(TaskJPA.class, childId);

			// Verify the proxy is not yet initialized
			assertThat(persistenceUtil.isLoaded(proxy))
					.as("proxy returned by getReference() must be uninitialized")
					.isFalse();

			// ── Direct field access via reflection ──────────────────────────────────
			//
			// In the ancestor walk:  cursor = cursor.superTask;
			//
			// 'cursor' is the proxy object (a CGLIB subclass of TaskJPA).
			// The 'superTask' field declared in TaskJPA lives in the PROXY object too.
			// The proxy's own 'superTask' field is null (the real data is inside the
			// proxy's internalized target, populated only after initialization).
			// Therefore direct field access on the proxy ALWAYS returns null.
			//
			// This mirrors what would happen if @ManyToOne used FetchType.LAZY:
			//   cursor.superTask  → null (proxy field)   → walk stops prematurely
			//   cursor.superTask() → A   (getter triggers proxy init) → walk continues
			//
			java.lang.reflect.Field superTaskField = TaskJPA.class.getDeclaredField("superTask");
			superTaskField.setAccessible(true);
			Object directFieldValue = superTaskField.get(proxy);

			assertThat(directFieldValue)
					.as("direct field access on uninitialized proxy MUST return null "
							+ "(proxy's own field is null; the real value is in the proxy target)")
					.isNull();

			// ── Getter method triggers proxy initialization ─────────────────────────
			// cursor.superTask().orElse(null) → CGLIB intercepts the method call
			// → proxy is initialized from DB → returns the real entity value
			assertThat(proxy.superTask())
					.as("getter superTask() must trigger proxy initialization and return the parent")
					.isPresent();

			assertThat(proxy.superTask().orElseThrow().name())
					.as("after proxy init via getter, superTask name must be correct")
					.isEqualTo("proxy-parent");

			// The proxy is now initialized
			assertThat(persistenceUtil.isLoaded(proxy))
					.as("proxy must be initialized after getter call")
					.isTrue();
		}

		/**
		 * Simulation of the cycle-detection failure that would arise with
		 * {@code FetchType.LAZY} on {@code superTask}.
		 *
		 * <p>Setup: persist A → B. Then obtain B through a real {@code find()} (superTask
		 * eagerly loaded = current safe behaviour). Confirm cycle detection works.
		 *
		 * <p>Contrast: if the same B were obtained via {@code getReference()} (uninitialized
		 * proxy), adding A as B's child would <em>not</em> trigger the cycle check
		 * correctly through the current direct-field-access implementation.
		 * We document this by showing the proxy's superTask field is null.
		 */
		@Test
		@DisplayName("Simulation: why FetchType.LAZY on superTask would break cycle detection")
		void simulation_lazyFetchWouldBreakCycleDetection() throws Exception
		{
			TaskGroupJPA g = group("group-lazy-sim");
			TaskJPA      a = new TaskJPA(g, "A");
			TaskJPA      b = new TaskJPA(g, "B");

			a.addSubTask(b);
			persistAndFlush(a);
			persistAndFlush(b);

			Long aId = a.id(), bId = b.id();
			clearPersistenceContext();

			// ── Safe path (EAGER, current mapping): cycle correctly detected ─────────
			TaskJPA eagerB = find(TaskJPA.class, bId);   // B loaded with superTask = A (eager)
			TaskJPA eagerA = find(TaskJPA.class, aId);

			// Confirm B.superTask (A) is loaded via direct field (simulates cursor.superTask)
			java.lang.reflect.Field superTaskField = TaskJPA.class.getDeclaredField("superTask");
			superTaskField.setAccessible(true);

			TaskJPA directFieldValueOnEagerB = (TaskJPA) superTaskField.get(eagerB);
			assertThat(directFieldValueOnEagerB)
					.as("EAGER: cursor.superTask field on a real (non-proxy) entity returns A")
					.isNotNull();

			// Therefore the ancestor walk reaches A and rejects the cycle
			assertThatThrownBy(() -> eagerB.addSubTask(eagerA))
					.as("EAGER: cycle A→B→A correctly rejected")
					.isInstanceOf(TaskRelationException.class);

			// ── Risky path (simulated LAZY via getReference): field returns null ─────
			clearPersistenceContext();

			// Simulate what FetchType.LAZY would do: give us a proxy for B
			TaskJPA proxyB   = entityManager.getReference(TaskJPA.class, bId);
			TaskJPA freshA   = find(TaskJPA.class, aId);

			// With a proxy, direct field access returns null (proxy's uninitialized field)
			TaskJPA directFieldValueOnProxyB = (TaskJPA) superTaskField.get(proxyB);
			assertThat(directFieldValueOnProxyB)
					.as("LAZY (proxy): cursor.superTask field on a proxy is null — "
							+ "the ancestor walk would stop HERE and miss A entirely")
					.isNull();

			// NOTE: We do NOT call proxyB.addSubTask(freshA) here, because the proxy
			// would be initialized by the equals() call inside addSubTask, which would
			// then correctly detect the cycle anyway (equals triggers full init).
			//
			// The real danger is a scenario where cursor is the *superTask of the loaded entity*,
			// i.e. the entity returned by entityManager.find() has its own superTask field
			// pointing to a proxy (which happens exactly when @ManyToOne is LAZY).
			// In that case:
			//   loaded entity (real)  → superTask (proxy) → proxy.superTask (field) = null
			//   The walk stops at the second hop, missing any ancestor beyond the first.
			//
			// The safe fix:  cursor = cursor.superTask().orElse(null)
			//                         ^^^^^^^^^^^^^^^^^^^
			//                         getter triggers proxy initialization
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// D — subTasks: OneToMany is LAZY by default (contrast with superTask)
	// ═══════════════════════════════════════════════════════════════════════════

	@Nested
	@DisplayName("D — subTasks (@OneToMany) are LAZY by default — contrast with EAGER superTask")
	class D_SubTasksLazyContrast
	{
		/**
		 * Demonstrates the asymmetry between the two sides of the bidirectional relation:
		 * <ul>
		 *   <li>{@code superTask} ({@code @ManyToOne}) → <strong>EAGER</strong> (JPA default)</li>
		 *   <li>{@code subTasks}  ({@code @OneToMany}) → <strong>LAZY</strong>  (JPA default)</li>
		 * </ul>
		 * The ancestor walk traverses {@code superTask} — the EAGER side — so it is safe.
		 * Traversing {@code subTasks} (e.g., for downward DFS) would require explicit loading.
		 */
		@Test
		@DisplayName("superTask (ManyToOne) is EAGER; subTasks (OneToMany) is LAZY after find()")
		void superTask_vs_subTasks_fetchTypeDifference()
		{
			TaskGroupJPA g      = group("group-fetch-contrast");
			TaskJPA      parent = new TaskJPA(g, "parent");
			TaskJPA      child  = new TaskJPA(g, "child");

			parent.addSubTask(child);
			persistAndFlush(parent);
			persistAndFlush(child);

			Long parentId = parent.id(), childId = child.id();
			clearPersistenceContext();

			TaskJPA reloadedChild  = find(TaskJPA.class, childId);
			TaskJPA reloadedParent = find(TaskJPA.class, parentId);

			// superTask (ManyToOne) — should be EAGER
			assertThat(persistenceUtil.isLoaded(reloadedChild, "superTask"))
					.as("superTask (@ManyToOne) must be EAGERLY loaded — safe for ancestor walk")
					.isTrue();

			// subTasks (OneToMany) — should be LAZY
			assertThat(persistenceUtil.isLoaded(reloadedParent, "subTasks"))
					.as("subTasks (@OneToMany) must be LAZILY loaded — NOT safe for direct field walk")
					.isFalse();
		}
	}
}

