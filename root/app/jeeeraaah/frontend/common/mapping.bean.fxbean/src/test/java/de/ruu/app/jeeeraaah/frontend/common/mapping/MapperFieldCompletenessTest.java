package de.ruu.app.jeeeraaah.frontend.common.mapping;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.frontend.common.mapping.bean.fxbean.Map_TaskGroup_Bean_FXBean;
import de.ruu.app.jeeeraaah.frontend.common.mapping.bean.fxbean.Map_Task_Bean_FXBean;
import de.ruu.app.jeeeraaah.frontend.common.mapping.fxbean.bean.Map_TaskGroup_FXBean_Bean;
import de.ruu.app.jeeeraaah.frontend.common.mapping.fxbean.bean.Map_Task_FXBean_Bean;
import de.ruu.app.jeeeraaah.frontend.ui.fx.model.TaskFXBean;
import de.ruu.app.jeeeraaah.frontend.ui.fx.model.TaskGroupFXBean;

/**
 * Tests to verify that MapStruct mappers with JavaFXFluentAccessorNamingStrategy handle all fields correctly.
 * <p>
 * With the custom AccessorNamingStrategy, MapStruct automatically recognizes:
 * <ul>
 * <li>JavaFX properties: {@code nameProperty()} ↔ {@code name()}</li>
 * <li>Fluent accessors: {@code name()} ↔ {@code getName()}</li>
 * <li>Standard JavaBean accessors: {@code getName()} ↔ {@code setName()}</li>
 * </ul>
 * <p>
 * These tests verify that only constructor-initialized fields, collections (handled in @AfterMapping),
 * and truly unmappable fields need explicit {@code @Mapping(ignore = true)}.
 */
class MapperFieldCompletenessTest
{
	/**
	 * Verifies {@link Map_Task_Bean_FXBean} handles all fields from {@link TaskBean} to {@link TaskFXBean}.
	 * <p>
	 * Thanks to JavaFXFluentAccessorNamingStrategy:
	 * <ul>
	 * <li>name() → nameProperty() - <b>auto-mapped</b></li>
	 * <li>description() → descriptionProperty() - <b>auto-mapped</b></li>
	 * <li>start() → startProperty() - <b>auto-mapped</b></li>
	 * <li>end() → endProperty() - <b>auto-mapped</b></li>
	 * <li>closed() → closedProperty() - <b>auto-mapped</b></li>
	 * </ul>
	 * <p>
	 * Only fields requiring special handling are explicitly ignored in the mapper.
	 */
	@Test
	void testMap_Task_Bean_FXBean_mapsAllFields()
	{
		// Fields set in @ObjectFactory constructor
		Set<String> mappedInConstructor = Set.of(
				"id", "version", "taskGroup"
		);

		// Fields mapped automatically by MapStruct via AccessorNamingStrategy
		Set<String> mappedAutomatically = Set.of(
				"name", "description", "start", "end", "closed"
		);

		// Fields explicitly ignored (collections, special handling)
		Set<String> explicitlyIgnored = Set.of(
				// Collections/relations handled in @AfterMapping
				"subTasks", "predecessors", "successors", "superTask",
				// Not yet implemented
				"preconditionCheckRelationalOperations",
				// JavaFX Property fields - accessed via nameProperty() methods, not directly
				"nameProperty", "descriptionProperty", "startProperty", "endProperty",
				"closedProperty", "taskGroupProperty", "superTaskProperty"
		);

		assertAllFieldsMapped(
				TaskBean.class,
				TaskFXBean.class,
				mappedInConstructor,
				mappedAutomatically,
				explicitlyIgnored,
				Map_Task_Bean_FXBean.class.getSimpleName()
		);
	}

	/**
	 * Verifies {@link Map_Task_FXBean_Bean} handles all fields from {@link TaskFXBean} to {@link TaskBean}.
	 * <p>
	 * Thanks to JavaFXFluentAccessorNamingStrategy, JavaFX properties are automatically mapped.
	 */
	@Test
	void testMap_Task_FXBean_Bean_mapsAllFields()
	{
		// Fields set in @ObjectFactory constructor
		Set<String> mappedInConstructor = Set.of(
				"id", "version", "taskGroup"
		);

		// Fields mapped automatically by MapStruct via AccessorNamingStrategy
		Set<String> mappedAutomatically = Set.of(
				"name", "description", "start", "end", "closed"
		);

		// Fields explicitly ignored
		Set<String> explicitlyIgnored = Set.of(
				// Collections/relations handled in @AfterMapping
				"subTasks", "predecessors", "successors", "superTask",
				// Not yet implemented
				"preconditionCheckRelationalOperations",
				// JavaFX Property fields - accessed via nameProperty() methods, not directly
				"nameProperty", "descriptionProperty", "startProperty", "endProperty",
				"closedProperty", "taskGroupProperty", "superTaskProperty"
		);

		assertAllFieldsMapped(
				TaskFXBean.class,
				TaskBean.class,
				mappedInConstructor,
				mappedAutomatically,
				explicitlyIgnored,
				Map_Task_FXBean_Bean.class.getSimpleName()
		);
	}

	/**
	 * Verifies that {@link Map_TaskGroup_Bean_FXBean} handles all fields from {@link TaskGroupBean} to {@link TaskGroupFXBean}.
	 * <p>
	 * <b>Fields mapped in @ObjectFactory constructor:</b>
	 * <ul>
	 * <li>id</li>
	 * <li>version</li>
	 * <li>name</li>
	 * </ul>
	 * 
	 * <b>Fields mapped automatically by MapStruct:</b>
	 * <ul>
	 * <li>description (Optional&lt;String&gt; to SimpleObjectProperty&lt;String&gt;)</li>
	 * </ul>
	 * 
	 * <b>Fields explicitly ignored:</b>
	 * <ul>
	 * <li>nameProperty, descriptionProperty (JavaFX properties - internal implementation)</li>
	 * <li>tasks (collection handled separately)</li>
	 * </ul>
	 */
	@Test
	void testMap_TaskGroup_Bean_FXBean_mapsAllFields()
	{
		// Fields set in @ObjectFactory constructor
		Set<String> mappedInConstructor = Set.of(
				"id", "version"
		);

		// Fields mapped automatically by MapStruct via AccessorNamingStrategy
		Set<String> mappedAutomatically = Set.of(
				"name", "description"
		);

		// Fields explicitly ignored
		Set<String> explicitlyIgnored = Set.of(
				"tasks", // Collection handled separately
				// JavaFX Property fields - accessed via nameProperty() methods, not directly
				"nameProperty", "descriptionProperty"
		);

		assertAllFieldsMapped(
				TaskGroupBean.class,
				TaskGroupFXBean.class,
				mappedInConstructor,
				mappedAutomatically,
				explicitlyIgnored,
				Map_TaskGroup_Bean_FXBean.class.getSimpleName()
		);
	}

	/**
	 * Verifies {@link Map_TaskGroup_FXBean_Bean} handles all fields from {@link TaskGroupFXBean} to {@link TaskGroupBean}.
	 */
	@Test
	void testMap_TaskGroup_FXBean_Bean_mapsAllFields()
	{
		// Fields set in @ObjectFactory constructor
		Set<String> mappedInConstructor = Set.of(
				"id", "version"
		);

		// Fields mapped automatically by MapStruct via AccessorNamingStrategy
		Set<String> mappedAutomatically = Set.of(
				"name", "description"
		);

		// Fields explicitly ignored
		Set<String> explicitlyIgnored = Set.of(
				"tasks", // Collection handled separately
				// JavaFX Property fields - accessed via nameProperty() methods, not directly
				"nameProperty", "descriptionProperty"
		);

		assertAllFieldsMapped(
				TaskGroupFXBean.class,
				TaskGroupBean.class,
				mappedInConstructor,
				mappedAutomatically,
				explicitlyIgnored,
				Map_TaskGroup_FXBean_Bean.class.getSimpleName()
		);
	}

	/**
	 * Core assertion method using reflection to verify field mapping completeness.
	 * 
	 * @param sourceClass         the source class being mapped FROM
	 * @param targetClass         the target class being mapped TO
	 * @param mappedInConstructor fields mapped via @ObjectFactory constructor
	 * @param mappedAutomatically fields mapped automatically by MapStruct or in helper methods
	 * @param explicitlyIgnored   fields that are intentionally not mapped (technical fields, collections)
	 * @param mapperName          name of the mapper interface for error messages
	 */
	private void assertAllFieldsMapped(
			Class<?> sourceClass,
			Class<?> targetClass,
			Set<String> mappedInConstructor,
			Set<String> mappedAutomatically,
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
		allAccountedFields.addAll(mappedAutomatically);
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
			errorMessage.append("  2. Added to mappedAutomatically (if mapped by MapStruct or helper method)%n");
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
			errorMessage.append("  2. Added to mappedAutomatically (if set by MapStruct or helper method)%n");
			errorMessage.append("  3. Added to explicitlyIgnored (if intentionally not set)%n");
		}

		if (errorMessage.length() > 0)
		{
			fail(errorMessage.toString());
		}

		// Verify there are no duplicates across sets
		Set<String> duplicates = findDuplicates(mappedInConstructor, mappedAutomatically, explicitlyIgnored);
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
