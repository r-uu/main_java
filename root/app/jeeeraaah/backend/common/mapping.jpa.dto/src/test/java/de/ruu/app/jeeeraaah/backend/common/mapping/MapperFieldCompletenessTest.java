package de.ruu.app.jeeeraaah.backend.common.mapping;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import de.ruu.app.jeeeraaah.backend.common.mapping.dto.jpa.Map_TaskGroup_DTO_JPA;
import de.ruu.app.jeeeraaah.backend.common.mapping.dto.jpa.Map_Task_DTO_JPA;
import de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto.Map_TaskGroup_JPA_DTO;
import de.ruu.app.jeeeraaah.backend.common.mapping.jpa.dto.Map_Task_JPA_DTO;
import de.ruu.app.jeeeraaah.backend.common.mapping.jpa.lazy.Map_TaskGroup_JPA_Lazy;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskJPA;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupLazy;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;

/**
 * Generic reflection-based tests to verify that all fields are properly mapped in mappers.
 * <p>
 * This test ensures that when new fields are added to entities or DTOs, they are not forgotten
 * in the mapping logic. Each mapper must handle all relevant fields either:
 * <ul>
 * <li>In the {@code @ObjectFactory} constructor call</li>
 * <li>In the {@code mapMutableFields()} helper method</li>
 * <li>Explicitly ignored (e.g., jsonId, serialVersionUID, technical fields)</li>
 * </ul>
 */
class MapperFieldCompletenessTest
{
	/**
	 * Verifies that {@link Map_Task_JPA_DTO} handles all fields from {@link TaskJPA} to {@link TaskDTO}.
	 * <p>
	 * <b>Fields mapped in @ObjectFactory constructor:</b>
	 * <ul>
	 * <li>id</li>
	 * <li>version</li>
	 * <li>name</li>
	 * <li>closed</li>
	 * <li>taskGroup (via reference)</li>
	 * </ul>
	 * 
	 * <b>Fields mapped in mapMutableFields():</b>
	 * <ul>
	 * <li>description (Optional&lt;String&gt; to String)</li>
	 * <li>start (Optional&lt;LocalDate&gt; to LocalDate)</li>
	 * <li>end (Optional&lt;LocalDate&gt; to LocalDate)</li>
	 * </ul>
	 * 
	 * <b>Fields explicitly ignored:</b>
	 * <ul>
	 * <li>jsonId (technical field for JSON serialization)</li>
	 * <li>serialVersionUID (Java serialization)</li>
	 * <li>subTasks (collections handled separately in @AfterMapping)</li>
	 * <li>precedingTasks, followingTasks (collections handled separately)</li>
	 * </ul>
	 */
	@Test
	void testMap_Task_JPA_DTO_mapsAllFields()
	{
		// Fields set in @ObjectFactory constructor
		Set<String> mappedInConstructor = Set.of(
				"id", "version", "name", "closed", "taskGroup"
		);

		// Fields mapped in mapMutableFields() helper method
		Set<String> mappedInHelper = Set.of(
				"description", "start", "end"
		);

		// Fields explicitly ignored (technical fields, collections handled elsewhere)
		Set<String> explicitlyIgnored = Set.of(
				"jsonId",           // UUID for Jackson JSON identity
				"serialVersionUID", // Java serialization
				"subTasks",         // Collection handled in @AfterMapping
				"precedingTasks",   // Collection handled in @AfterMapping (deprecated)
				"followingTasks",   // Collection handled in @AfterMapping (deprecated)
				"predecessors",     // Collection handled in @AfterMapping
				"successors",       // Collection handled in @AfterMapping
				"superTask"         // Parent task reference handled in @AfterMapping
		);

		assertAllFieldsMapped(
				TaskJPA.class,
				TaskDTO.class,
				mappedInConstructor,
				mappedInHelper,
				explicitlyIgnored,
				Map_Task_JPA_DTO.class.getSimpleName()
		);
	}

	/**
	 * Verifies that {@link Map_TaskGroup_JPA_DTO} handles all fields from {@link TaskGroupJPA} to {@link TaskGroupDTO}.
	 * <p>
	 * <b>Fields mapped in @ObjectFactory constructor:</b>
	 * <ul>
	 * <li>id</li>
	 * <li>version</li>
	 * <li>name</li>
	 * <li>closed</li>
	 * </ul>
	 * 
	 * <b>Fields mapped in mapMutableFields():</b>
	 * <ul>
	 * <li>description (Optional&lt;String&gt; to String)</li>
	 * </ul>
	 * 
	 * <b>Fields explicitly ignored:</b>
	 * <ul>
	 * <li>jsonId (technical field for JSON serialization)</li>
	 * <li>serialVersionUID (Java serialization)</li>
	 * <li>tasks (collection handled separately in @AfterMapping)</li>
	 * </ul>
	 */
	@Test
	void testMap_TaskGroup_JPA_DTO_mapsAllFields()
	{
		Set<String> mappedInConstructor = Set.of(
				"id", "version", "name", "closed"
		);

		Set<String> mappedInHelper = Set.of(
				"description"
		);

		Set<String> explicitlyIgnored = Set.of(
				"jsonId",           // UUID for Jackson JSON identity
				"serialVersionUID", // Java serialization
				"tasks"             // Collection handled in @AfterMapping
		);

		assertAllFieldsMapped(
				TaskGroupJPA.class,
				TaskGroupDTO.class,
				mappedInConstructor,
				mappedInHelper,
				explicitlyIgnored,
				Map_TaskGroup_JPA_DTO.class.getSimpleName()
		);
	}

	/**
	 * Verifies that {@link Map_TaskGroup_JPA_Lazy} handles all fields from {@link TaskGroupJPA} to {@link TaskGroupLazy}.
	 */
	@Test
	void testMap_TaskGroup_JPA_Lazy_mapsAllFields()
	{
		Set<String> mappedInConstructor = Set.of(
				"id", "version", "name", "closed"
		);

		Set<String> mappedInHelper = Set.of(
				"description"
		);

		Set<String> explicitlyIgnored = Set.of(
				"jsonId",           // UUID for Jackson JSON identity  
				"serialVersionUID", // Java serialization
				"tasks"             // Collection handled in @AfterMapping (lazy loaded)
		);

		assertAllFieldsMapped(
				TaskGroupJPA.class,
				TaskGroupLazy.class,
				mappedInConstructor,
				mappedInHelper,
				explicitlyIgnored,
				Map_TaskGroup_JPA_Lazy.class.getSimpleName()
		);
	}

	/**
	 * Verifies that {@link Map_Task_DTO_JPA} handles all fields from {@link TaskDTO} to {@link TaskJPA}.
	 * <p>
	 * Reverse mapping: DTO → JPA
	 * <p>
	 * <b>UPDATE:</b> Since adding the TaskJPA(TaskGroupJPA, TaskEntity) constructor,
	 * id, version, and closed are now set in the constructor via @ObjectFactory,
	 * not in mapMutableFields().
	 */
	@Test
	void testMap_Task_DTO_JPA_mapsAllFields()
	{
		Set<String> mappedInConstructor = Set.of(
				"taskGroup", "name", "id", "version", "closed"  // TaskJPA constructor accepts TaskEntity
		);

		Set<String> mappedInHelper = Set.of(
				"description", "start", "end"
		);

		Set<String> explicitlyIgnored = Set.of(
				"jsonId",           // UUID for Jackson JSON identity
				"serialVersionUID", // Java serialization
				"subTasks",         // Collection handled in @AfterMapping
				"precedingTasks",   // Collection handled in @AfterMapping (deprecated)
				"followingTasks",   // Collection handled in @AfterMapping (deprecated)
				"predecessors",     // Collection handled in @AfterMapping
				"successors",       // Collection handled in @AfterMapping
				"superTask"         // Parent task reference handled in @AfterMapping
		);

		assertAllFieldsMapped(
				TaskDTO.class,
				TaskJPA.class,
				mappedInConstructor,
				mappedInHelper,
				explicitlyIgnored,
				Map_Task_DTO_JPA.class.getSimpleName()
		);
	}

	/**
	 * Verifies that {@link Map_TaskGroup_DTO_JPA} handles all fields from {@link TaskGroupDTO} to {@link TaskGroupJPA}.
	 * <p>
	 * Reverse mapping: DTO → JPA
	 */
	@Test
	void testMap_TaskGroup_DTO_JPA_mapsAllFields()
	{
		Set<String> mappedInConstructor = Set.of(
				"name"  // TaskGroupJPA constructor requires (String)
		);

		Set<String> mappedInHelper = Set.of(
				"id", "version", "closed", "description"
		);

		Set<String> explicitlyIgnored = Set.of(
				"jsonId",           // UUID for Jackson JSON identity
				"serialVersionUID", // Java serialization
				"tasks"             // Collection handled in @AfterMapping
		);

		assertAllFieldsMapped(
				TaskGroupDTO.class,
				TaskGroupJPA.class,
				mappedInConstructor,
				mappedInHelper,
				explicitlyIgnored,
				Map_TaskGroup_DTO_JPA.class.getSimpleName()
		);
	}

	/**
	 * Core assertion method using reflection to verify field mapping completeness.
	 * 
	 * @param sourceClass         the source class being mapped FROM
	 * @param targetClass         the target class being mapped TO
	 * @param mappedInConstructor fields mapped via @ObjectFactory constructor
	 * @param mappedInHelper      fields mapped in mapMutableFields() or similar helper
	 * @param explicitlyIgnored   fields that are intentionally not mapped (technical fields, collections)
	 * @param mapperName          name of the mapper interface for error messages
	 */
	private void assertAllFieldsMapped(
			Class<?> sourceClass,
			Class<?> targetClass,
			Set<String> mappedInConstructor,
			Set<String> mappedInHelper,
			Set<String> explicitlyIgnored,
			String mapperName)
	{
		// Get all declared fields from source class
		Set<String> sourceFields = getAllDeclaredFieldNames(sourceClass);

		// Get all declared fields from target class  
		Set<String> targetFields = getAllDeclaredFieldNames(targetClass);

		// All fields that should be accounted for
		Set<String> allAccountedFields = new HashSet<>();
		allAccountedFields.addAll(mappedInConstructor);
		allAccountedFields.addAll(mappedInHelper);
		allAccountedFields.addAll(explicitlyIgnored);

		// Find unmapped source fields
		Set<String> unmappedSourceFields = new HashSet<>(sourceFields);
		unmappedSourceFields.removeAll(allAccountedFields);

		// Find unmapped target fields
		Set<String> unmappedTargetFields = new HashSet<>(targetFields);
		unmappedTargetFields.removeAll(allAccountedFields);

		// Build detailed error message if there are unmapped fields
		StringBuilder errorMessage = new StringBuilder();

		if (!unmappedSourceFields.isEmpty())
		{
			errorMessage.append(String.format(
					"%n%s: Found %d unmapped SOURCE fields in %s:%n  %s%n%n",
					mapperName,
					unmappedSourceFields.size(),
					sourceClass.getSimpleName(),
					unmappedSourceFields.stream().sorted().collect(Collectors.joining(", "))
			));
			errorMessage.append("These fields must be either:%n");
			errorMessage.append("  1. Added to mappedInConstructor (if mapped via @ObjectFactory)%n");
			errorMessage.append("  2. Added to mappedInHelper (if mapped in mapMutableFields())%n");
			errorMessage.append("  3. Added to explicitlyIgnored (if intentionally not mapped)%n");
		}

		if (!unmappedTargetFields.isEmpty())
		{
			errorMessage.append(String.format(
					"%n%s: Found %d unmapped TARGET fields in %s:%n  %s%n%n",
					mapperName,
					unmappedTargetFields.size(),
					targetClass.getSimpleName(),
					unmappedTargetFields.stream().sorted().collect(Collectors.joining(", "))
			));
			errorMessage.append("These fields must be either:%n");
			errorMessage.append("  1. Added to mappedInConstructor (if set via @ObjectFactory)%n");
			errorMessage.append("  2. Added to mappedInHelper (if set in mapMutableFields())%n");
			errorMessage.append("  3. Added to explicitlyIgnored (if intentionally not set)%n");
		}

		if (errorMessage.length() > 0)
		{
			fail(errorMessage.toString());
		}

		// Verify there are no duplicates across sets
		Set<String> duplicates = findDuplicates(mappedInConstructor, mappedInHelper, explicitlyIgnored);
		assertTrue(
				duplicates.isEmpty(),
				String.format(
						"%s: Found duplicate field declarations across mapping categories: %s",
						mapperName,
						duplicates
				)
		);
	}

	/**
	 * Gets all declared field names from a class, excluding static and synthetic fields.
	 * 
	 * @param clazz the class to inspect
	 * @return set of field names
	 */
	private Set<String> getAllDeclaredFieldNames(Class<?> clazz)
	{
		return Arrays.stream(clazz.getDeclaredFields())
				.filter(f -> !Modifier.isStatic(f.getModifiers()))
				.filter(f -> !f.isSynthetic())
				.map(Field::getName)
				.collect(Collectors.toSet());
	}

	/**
	 * Finds duplicate field names across multiple sets.
	 * 
	 * @param sets variable number of field name sets
	 * @return set of field names that appear in multiple sets
	 */
	@SafeVarargs
	private Set<String> findDuplicates(Set<String>... sets)
	{
		Set<String> seen = new HashSet<>();
		Set<String> duplicates = new HashSet<>();

		for (Set<String> set : sets)
		{
			for (String field : set)
			{
				if (!seen.add(field))
				{
					duplicates.add(field);
				}
			}
		}

		return duplicates;
	}
}
