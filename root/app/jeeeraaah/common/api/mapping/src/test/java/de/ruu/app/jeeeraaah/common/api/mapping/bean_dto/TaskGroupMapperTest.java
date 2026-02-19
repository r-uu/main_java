package de.ruu.app.jeeeraaah.common.api.mapping.bean_dto;

import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupEntity;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link TaskGroupMapper} - bidirectional TaskGroup Bean ↔ DTO mappings.
 */
class TaskGroupMapperTest {

    private ReferenceCycleTracking context;

    @BeforeEach
    void setUp() {
        context = new ReferenceCycleTracking();
    }

    @Test
    void mapperInstanceExists() {
        assertNotNull(TaskGroupMapper.INSTANCE, "Mapper instance should exist");
    }

    @Test
    void beanToDTO_shouldMapBasicFields() {
        // Given
        TaskGroupBean bean = new TaskGroupBean("Test Group");

        // When
        TaskGroupDTO dto = TaskGroupMapper.INSTANCE.toDTO(bean, context);

        // Then
        assertNotNull(dto);
        assertThat(dto.name(), is(equalTo(bean.name())));
    }

    @Test
    void beanToDTO_shouldMapDescriptionField() {
        // Given
        TaskGroupBean bean = new TaskGroupBean("Test Group");
        bean.description("Test Description");

        // When
        TaskGroupDTO dto = TaskGroupMapper.INSTANCE.toDTO(bean, context);

        // Then
        assertNotNull(dto);
        assertThat(dto.description(), is(equalTo(bean.description())));
        assertThat(dto.description().isPresent(), is(true));
        assertThat(dto.description().get(), is(equalTo("Test Description")));
    }

    @Test
    void dtoToBean_shouldMapBasicFields() {
        // Given - First create a bean, then a DTO from it
        TaskGroupBean sourceBean = new TaskGroupBean("Test Group");
        TaskGroupDTO dto = TaskGroupMapper.INSTANCE.toDTO(sourceBean, context);

        // When - Map DTO back to bean with fresh context
        ReferenceCycleTracking newContext = new ReferenceCycleTracking();
        TaskGroupBean bean = TaskGroupMapper.INSTANCE.toBean(dto, newContext);

        // Then
        assertNotNull(bean);
        assertThat(bean.name(), is(equalTo(dto.name())));
    }

    @Test
    void dtoToBean_shouldMapDescriptionField() {
        // Given
        TaskGroupBean sourceBean = new TaskGroupBean("Test Group");
        sourceBean.description("Test Description");
        TaskGroupDTO dto = TaskGroupMapper.INSTANCE.toDTO(sourceBean, context);

        // When
        ReferenceCycleTracking newContext = new ReferenceCycleTracking();
        TaskGroupBean bean = TaskGroupMapper.INSTANCE.toBean(dto, newContext);

        // Then
        assertNotNull(bean);
        assertThat(bean.description(), is(equalTo(dto.description())));
        assertThat(bean.description().isPresent(), is(true));
        assertThat(bean.description().get(), is(equalTo("Test Description")));
    }

    @Test
    void bidirectionalMapping_shouldPreserveData() {
        // Given
        TaskGroupBean originalBean = new TaskGroupBean("Original Group");
        originalBean.description("Original Description");

        // When - Bean → DTO → Bean
        TaskGroupDTO dto = TaskGroupMapper.INSTANCE.toDTO(originalBean, context);
        ReferenceCycleTracking newContext = new ReferenceCycleTracking();
        TaskGroupBean resultBean = TaskGroupMapper.INSTANCE.toBean(dto, newContext);

        // Then
        assertNotNull(resultBean);
        assertThat(resultBean.name(), is(equalTo(originalBean.name())));
        assertThat(resultBean.description(), is(equalTo(originalBean.description())));
    }

    @Test
    void cyclicReferenceDetection_shouldPreventInfiniteLoops() {
        // Given
        TaskGroupEntity<?> mockEntity = createMockTaskGroupEntity(1L, "Test Group");
        TaskGroupBean bean = new TaskGroupBean(mockEntity);

        // When - mapping same bean twice
        TaskGroupDTO dto1 = TaskGroupMapper.INSTANCE.toDTO(bean, context);
        TaskGroupDTO dto2 = TaskGroupMapper.INSTANCE.toDTO(bean, context);

        // Then - should reuse the same DTO from context
        assertThat("Context should prevent duplicate mappings", dto2, is(sameInstance(dto1)));
    }

    @Test
    void emptyOptionalFields_shouldMapToEmptyOptionals() {
        // Given
        TaskGroupBean bean = new TaskGroupBean("Test Group");

        // When
        TaskGroupDTO dto = TaskGroupMapper.INSTANCE.toDTO(bean, context);

        // Then
        assertNotNull(dto);
        assertThat("Description should be empty", dto.description().isPresent(), is(false));
    }
}

