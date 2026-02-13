package de.ruu.app.jeeeraaah.common.api.mapping;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import de.ruu.app.jeeeraaah.common.api.domain.TaskBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.mapping.bean.dto.Map_Task_Bean_DTO;
import de.ruu.app.jeeeraaah.common.api.mapping.bean.dto.Map_TaskGroup_Bean_DTO;
import de.ruu.app.jeeeraaah.common.api.mapping.dto.bean.Map_Task_DTO_Bean;
import de.ruu.app.jeeeraaah.common.api.mapping.dto.bean.Map_TaskGroup_DTO_Bean;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;

/**
 * Generic reflection-based tests to verify that all fields are properly mapped in API layer mappers (DTO ↔ Bean).
 * <p>
 * This test ensures that when new fields are added to DTOs or Beans, they are not forgotten
 * in the mapping logic.
 */
class MapperFieldCompletenessTest
{
	/**
	 * Verifies that {@link Map_Task_DTO_Bean} handles all fields from {@link TaskDTO} to {@link TaskBean}.
	 */
	@Test
	void testMap_Task_DTO_Bean_mapsAllFields()
	{
		Set<String> mappedInConstructor = Set.of(
				"id", "version", "name", "closed", "taskGroup"
		);

		Set<String> mappedInHelper = Set.of(
				"description", "start", "end"
		);

		Set<String> explicitlyIgnored = Set.of(
				"jsonId",           // UUID for Jackson JSON identity
				"serialVersionUID", // Java serialization
				"subTasks",         // Collection handled in @AfterMapping
				"precedingTasks",   // Collection handled in @AfterMapping
				"followingTasks"    // Collection handled in @AfterMapping
		);

		assertAllFieldsMapped(
				TaskDTO.class,
				TaskBean.class,
				mappedInConstructor,
				mappedInHelper,
				explicitlyIgnored,
				Map_Task_DTO_Bean.class.getSimpleName()
		);
	}

	/**
	 * Verifies that {@link Map_TaskGroup_DTO_Bean} handles all fields from {@link TaskGroupDTO} to {@link TaskGroupBean}.
	 */
	@Test
	void testMap_TaskGroup_DTO_Bean_mapsAllFields()
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
				TaskGroupDTO.class,
				TaskGroupBean.class,
				mappedInConstructor,
				mappedInHelper,
				explicitlyIgnored,
				Map_TaskGroup_DTO_Bean.class.getSimpleName()
		);
	}

	/**
	 * Verifies that {@link Map_Task_Bean_DTO} handles all fields from {@link TaskBean} to {@link TaskDTO}.
	 * <p>
	 * Reverse mapping: Bean → DTO
	 */
	@Test
	void testMap_Task_Bean_DTO_mapsAllFields()
	{
		Set<String> mappedInConstructor = Set.of(
				"id", "version", "name", "closed", "taskGroup"
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
				TaskBean.class,
				TaskDTO.class,
				mappedInConstructor,
				mappedInHelper,
				explicitlyIgnored,
				Map_Task_Bean_DTO.class.getSimpleName()
		);
	}

	/**
	 * Verifies that {@link Map_TaskGroup_Bean_DTO} handles all fields from {@link TaskGroupBean} to {@link TaskGroupDTO}.
	 * <p>
	 * Reverse mapping: Bean → DTO
	 */
	@Test
	void testMap_TaskGroup_Bean_DTO_mapsAllFields()
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
				TaskGroupBean.class,
				TaskGroupDTO.class,
				mappedInConstructor,
				mappedInHelper,
				explicitlyIgnored,
				Map_TaskGroup_Bean_DTO.class.getSimpleName()
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
		Set<String> sourceFields = getAllDeclaredFieldNames(sourceClass);
		Set<String> targetFields = getAllDeclaredFieldNames(targetClass);

		Set<String> allAccountedFields = new HashSet<>();
		allAccountedFields.addAll(mappedInConstructor);
		allAccountedFields.addAll(mappedInHelper);
		allAccountedFields.addAll(explicitlyIgnored);

		Set<String> unmappedSourceFields = new HashSet<>(sourceFields);
		unmappedSourceFields.removeAll(allAccountedFields);

		Set<String> unmappedTargetFields = new HashSet<>(targetFields);
		unmappedTargetFields.removeAll(allAccountedFields);

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

	private Set<String> getAllDeclaredFieldNames(Class<?> clazz)
	{
		return Arrays.stream(clazz.getDeclaredFields())
				.filter(f -> !Modifier.isStatic(f.getModifiers()))
				.filter(f -> !f.isSynthetic())
				.map(Field::getName)
				.collect(Collectors.toSet());
	}

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
