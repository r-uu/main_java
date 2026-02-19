package de.ruu.app.jeeeraaah.common.api.mapping.bean_flat;

import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupEntity;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupFlat;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link TaskGroupMapper} - bidirectional TaskGroup Bean ↔ Flat mappings.
 */
class TaskGroupMapperTest {

    @Test
    void mapperInstanceExists() {
        assertNotNull(TaskGroupMapper.INSTANCE, "Mapper instance should exist");
    }

    @Test
    void beanToFlat_shouldMapBasicFields() {
        // Given
        TaskGroupBean bean = new TaskGroupBean("Test Group");

        // When
        TaskGroupFlat flat = TaskGroupMapper.INSTANCE.toFlat(bean);

        // Then
        assertNotNull(flat);
        assertThat(flat.getName(), is(equalTo(bean.name())));
    }

    @Test
    void beanToFlat_shouldMapDescriptionField() {
        // Given
        TaskGroupBean bean = new TaskGroupBean("Test Group");
        bean.description("Test Description");

        // When
        TaskGroupFlat flat = TaskGroupMapper.INSTANCE.toFlat(bean);

        // Then
        assertNotNull(flat);
        assertThat(flat.getDescription(), is(equalTo(bean.getDescription())));
        assertThat(flat.getDescription().isPresent(), is(true));
        assertThat(flat.getDescription().get(), is(equalTo("Test Description")));
    }

    @Test
    void flatToBean_shouldMapBasicFields() {
        // Given
        TaskGroupFlat flat = new TaskGroupFlat(1L, "Test Group");

        // When
        TaskGroupBean bean = TaskGroupMapper.INSTANCE.toBean(flat);

        // Then
        assertNotNull(bean);
        assertThat(bean.getId(), is(equalTo(flat.getId())));
        assertThat(bean.getName(), is(equalTo(flat.getName())));
    }

    @Test
    void flatToBean_shouldMapDescriptionField() {
        // Given
        TaskGroupFlat flat = new TaskGroupFlat(1L, "Test Group");
        flat.setDescription(Optional.of("Test Description"));

        // When
        TaskGroupBean bean = TaskGroupMapper.INSTANCE.toBean(flat);

        // Then
        assertNotNull(bean);
        assertThat(bean.getDescription(), is(equalTo(flat.getDescription())));
        assertThat(bean.getDescription().isPresent(), is(true));
        assertThat(bean.getDescription().get(), is(equalTo("Test Description")));
    }

    @Test
    void bidirectionalMapping_shouldPreserveData() {
        // Given
        TaskGroupBean originalBean = new TaskGroupBean("Original Group");
        originalBean.description("Original Description");

        // When - Bean → Flat → Bean
        TaskGroupFlat flat = TaskGroupMapper.INSTANCE.toFlat(originalBean);
        TaskGroupBean resultBean = TaskGroupMapper.INSTANCE.toBean(flat);

        // Then
        assertNotNull(resultBean);
        assertThat(resultBean.name(), is(equalTo(originalBean.name())));
        assertThat(resultBean.description(), is(equalTo(originalBean.description())));
    }

    @Test
    void emptyOptionalFields_shouldMapToEmptyOptionals() {
        // Given
        TaskGroupFlat flat = new TaskGroupFlat(1L, "Test Group");

        // When
        TaskGroupBean bean = TaskGroupMapper.INSTANCE.toBean(flat);

        // Then
        assertNotNull(bean);
        assertThat("Description should be empty", bean.description().isPresent(), is(false));
    }
}

