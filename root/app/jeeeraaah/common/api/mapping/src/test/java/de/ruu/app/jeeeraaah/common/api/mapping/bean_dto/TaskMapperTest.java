package de.ruu.app.jeeeraaah.common.api.mapping.bean_dto;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskEntity;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupEntity;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskGroupDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link TaskMapper} - bidirectional Task Bean ↔ DTO mappings.
 */
class TaskMapperTest {

    private ReferenceCycleTracking context;
    private TaskGroupEntity<?> mockGroupEntity;
    private TaskGroupBean testGroup;
    private TaskGroupDTO testGroupDTO;

    @BeforeEach
    void setUp() {
        context = new ReferenceCycleTracking();

        // Create mock group entity
        mockGroupEntity = new TaskGroupEntity<>() {
            private Long id = 1L;
            private Long version = 1L;
            private String name = "Test Group";

            @Override public Long getId() { return id; }
            @Override public Long getVersion() { return version; }
            @Override public String getName() { return name; }
            @Override public Optional<String> getDescription() { return Optional.of("Test Description"); }
        };

        testGroup = new TaskGroupBean(mockGroupEntity);
        testGroupDTO = new TaskGroupDTO(mockGroupEntity);
    }

    @Test
    void mapperInstanceExists() {
        assertNotNull(TaskMapper.INSTANCE, "Mapper instance should exist");
    }

    @Test
    void beanToDTO_shouldMapBasicFields() {
        // Given
        TaskEntity<?, ?> mockEntity = createMockTaskEntity(1L, "Test Task", testGroup);
        TaskBean bean = new TaskBean(testGroup, mockEntity);

        // When
        TaskDTO dto = TaskMapper.INSTANCE.toDTO(bean, context);

        // Then
        assertNotNull(dto);
        assertThat(dto.id(), is(equalTo(bean.getId())));
        assertThat(dto.version(), is(equalTo(bean.getVersion())));
        assertThat(dto.name(), is(equalTo(bean.getName())));
    }

    @Test
    void beanToDTO_shouldMapOptionalFields() {
        // Given
        TaskEntity<?, ?> mockEntity = createMockTaskEntityWithOptionals(1L, "Test Task");
        TaskBean bean = new TaskBean(testGroup, mockEntity);

        // When
        TaskDTO dto = TaskMapper.INSTANCE.toDTO(bean, context);

        // Then
        assertNotNull(dto);
        assertEquals(bean.getDescription(), dto.description());
        assertEquals(bean.getStart(), dto.start());
        assertEquals(bean.getEnd(), dto.end());
    }

    @Test
    void dtoToBean_shouldMapBasicFields() {
        // Given
        TaskDTO dto = new TaskDTO(testGroupDTO, createMockTaskEntity(1L, "Test Task", testGroup));

        // When
        TaskBean bean = TaskMapper.INSTANCE.toBean(dto, context);

        // Then
        assertNotNull(bean);
        assertEquals(dto.id(), bean.getId());
        assertEquals(dto.version(), bean.getVersion());
        assertEquals(dto.name(), bean.getName());
    }

    @Test
    void dtoToBean_shouldMapOptionalFields() {
        // Given
        TaskEntity<?, ?> mockEntity = createMockTaskEntityWithOptionals(1L, "Test Task");
        TaskDTO dto = new TaskDTO(testGroupDTO, mockEntity);

        // When
        TaskBean bean = TaskMapper.INSTANCE.toBean(dto, context);

        // Then
        assertNotNull(bean);
        assertEquals(dto.description(), bean.getDescription());
        assertEquals(dto.start(), bean.getStart());
        assertEquals(dto.end(), bean.getEnd());
    }

    @Test
    void bidirectionalMapping_shouldPreserveData() {
        // Given
        TaskEntity<?, ?> mockEntity = createMockTaskEntityWithOptionals(1L, "Original Task");
        TaskBean originalBean = new TaskBean(testGroup, mockEntity);

        // When - Bean → DTO → Bean
        TaskDTO dto = TaskMapper.INSTANCE.toDTO(originalBean, context);
        ReferenceCycleTracking newContext = new ReferenceCycleTracking();
        TaskBean resultBean = TaskMapper.INSTANCE.toBean(dto, newContext);

        // Then
        assertNotNull(resultBean);
        assertEquals(originalBean.getId(), resultBean.getId());
        assertEquals(originalBean.getName(), resultBean.getName());
        assertEquals(originalBean.getDescription(), resultBean.getDescription());
    }

    @Test
    void cyclicReferenceDetection_shouldPreventInfiniteLoops() {
        // Given
        TaskEntity<?, ?> mockEntity = createMockTaskEntity(1L, "Test Task", testGroup);
        TaskBean bean = new TaskBean(testGroup, mockEntity);

        // When - mapping same bean twice
        TaskDTO dto1 = TaskMapper.INSTANCE.toDTO(bean, context);
        TaskDTO dto2 = TaskMapper.INSTANCE.toDTO(bean, context);

        // Then - should reuse the same DTO from context
        assertThat(dto2, "Context should prevent duplicate mappings", is(sameInstance(dto1)));
    }

    // Helper methods
    private TaskEntity<?, ?> createMockTaskEntity(Long id, String name, TaskGroupBean group) {
        return new TaskEntity<>() {
            @Override public Long getId() { return id; }
            @Override public Long getVersion() { return 1L; }
            @Override public String getName() { return name; }
            @Override public Optional<String> getDescription() { return Optional.empty(); }
            @Override public Optional<LocalDateTime> getStart() { return Optional.empty(); }
            @Override public Optional<LocalDateTime> getEnd() { return Optional.empty(); }
        };
    }

    private TaskEntity<?, ?> createMockTaskEntityWithOptionals(Long id, String name) {
        return new TaskEntity<>() {
            @Override public Long getId() { return id; }
            @Override public Long getVersion() { return 1L; }
            @Override public String getName() { return name; }
            @Override public Optional<String> getDescription() { return Optional.of("Test Description"); }
            @Override public Optional<LocalDateTime> getStart() { return Optional.of(LocalDateTime.now()); }
            @Override public Optional<LocalDateTime> getEnd() { return Optional.of(LocalDateTime.now().plusHours(2)); }
        };
    }
}

