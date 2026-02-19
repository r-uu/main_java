package de.ruu.app.jeeeraaah.common.api.mapping.bean_lazy;

import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupEntity;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupLazy;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link TaskGroupMapper} - bidirectional TaskGroup Bean ↔ Lazy mappings.
 */
class TaskGroupMapperTest {

    @Test
    void mapperInstanceExists() {
        assertNotNull(TaskGroupMapper.INSTANCE, "Mapper instance should exist");
    }

    @Test
    void beanToLazy_shouldMapBasicFields() {
        // Given
        TaskGroupBean bean = new TaskGroupBean("Test Group");

        // When
        TaskGroupLazy lazy = TaskGroupMapper.INSTANCE.toLazy(bean);

        // Then
        assertNotNull(lazy);
        assertEquals(bean.name(), lazy.getName());
    }

    @Test
    void beanToLazy_shouldMapDescriptionField() {
        // Given
        TaskGroupBean bean = new TaskGroupBean("Test Group");
        bean.description("Test Description");

        // When
        TaskGroupLazy lazy = TaskGroupMapper.INSTANCE.toLazy(bean);

        // Then
        assertNotNull(lazy);
        assertEquals(bean.description(), lazy.getDescription());
        assertTrue(lazy.getDescription().isPresent());
        assertEquals("Test Description", lazy.getDescription().get());
    }

    @Test
    void lazyToBean_shouldMapBasicFields() {
        // Given
        TaskGroupLazy lazy = new TaskGroupLazy(1L, 1L, "Test Group");

        // When
        TaskGroupBean bean = TaskGroupMapper.INSTANCE.toBean(lazy);

        // Then
        assertNotNull(bean);
        assertEquals(lazy.getId(), bean.getId());
        assertEquals(lazy.getName(), bean.getName());
    }

    @Test
    void lazyToBean_shouldMapDescriptionField() {
        // Given
        TaskGroupLazy lazy = new TaskGroupLazy(1L, 1L, "Test Group");
        lazy.setDescription(Optional.of("Test Description"));

        // When
        TaskGroupBean bean = TaskGroupMapper.INSTANCE.toBean(lazy);

        // Then
        assertNotNull(bean);
        assertEquals(lazy.getDescription(), bean.getDescription());
        assertTrue(bean.getDescription().isPresent());
        assertEquals("Test Description", bean.getDescription().get());
    }

    @Test
    void bidirectionalMapping_shouldPreserveData() {
        // Given
        TaskGroupBean originalBean = new TaskGroupBean("Original Group");
        originalBean.description("Original Description");

        // When - Bean → Lazy → Bean
        TaskGroupLazy lazy = TaskGroupMapper.INSTANCE.toLazy(originalBean);
        TaskGroupBean resultBean = TaskGroupMapper.INSTANCE.toBean(lazy);

        // Then
        assertNotNull(resultBean);
        assertEquals(originalBean.name(), resultBean.name());
        assertEquals(originalBean.description(), resultBean.description());
    }

    @Test
    void emptyOptionalFields_shouldMapToEmptyOptionals() {
        // Given
        TaskGroupLazy lazy = new TaskGroupLazy(1L, Short.valueOf("1"), "Test Group");

        // When
        TaskGroupBean bean = TaskGroupMapper.INSTANCE.toBean(lazy);

        // Then
        assertNotNull(bean);
        assertThat("Description should be empty", bean.description().isPresent(), is(false));
    }

    // Helper methods are removed as we can create beans directly
}

