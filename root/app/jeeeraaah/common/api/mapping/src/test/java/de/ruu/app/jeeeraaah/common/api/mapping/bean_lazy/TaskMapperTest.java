package de.ruu.app.jeeeraaah.common.api.mapping.bean_lazy;

import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.TaskEntity;
import de.ruu.app.jeeeraaah.common.api.domain.TaskGroupEntity;
import de.ruu.app.jeeeraaah.common.api.domain.TaskLazy;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTOLazy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link TaskMapper} - bidirectional Task Bean ↔ Lazy mappings.
 */
class TaskMapperTest {

    private TaskGroupEntity<?> mockGroupEntity;
    private TaskGroupBean testGroup;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void mapperInstanceExists() {
        assertNotNull(TaskMapper.INSTANCE, "Mapper instance should exist");
    }

    @Test
    void beanToLazy_shouldMapBasicFields() {
        // Given
        TaskEntity<?, ?> mockEntity = createMockTaskEntity(1L, "Test Task");
        TaskBean bean = new TaskBean(testGroup, mockEntity);

        // When
        TaskLazy lazy = TaskMapper.INSTANCE.toLazy(bean);

        // Then
        assertNotNull(lazy);
        assertThat(lazy.getId(), is(equalTo(bean.getId())));
        assertThat(lazy.getVersion(), is(equalTo(bean.getVersion())));
        assertThat(lazy.getName(), is(equalTo(bean.getName())));
    }

    @Test
    void beanToLazy_shouldMapOptionalFields() {
        // Given
        TaskEntity<?, ?> mockEntity = createMockTaskEntityWithOptionals(1L, "Test Task");
        TaskBean bean = new TaskBean(testGroup, mockEntity);

        // When
        TaskLazy lazy = TaskMapper.INSTANCE.toLazy(bean);

        // Then
        assertNotNull(lazy);
        assertThat(lazy.getDescription(), is(equalTo(bean.getDescription())));
        assertThat(lazy.getStart(), is(equalTo(bean.getStart())));
        assertThat(lazy.getEnd(), is(equalTo(bean.getEnd())));
    }

    @Test
    void lazyToBean_shouldMapBasicFields() {
        // Given
        TaskDTOLazy dtoLazy = new TaskDTOLazy(1L, 1L, "Test Task", 1L);

        // When
        TaskBean bean = TaskMapper.INSTANCE.toBean(testGroup, dtoLazy);

        // Then
        assertNotNull(bean);
        assertThat(bean.getId(), is(equalTo(dtoLazy.id())));
        assertThat(bean.getVersion(), is(equalTo(dtoLazy.version())));
        assertThat(bean.getName(), is(equalTo(dtoLazy.name())));
    }

    @Test
    void lazyToBean_shouldMapOptionalFields() {
        // Given
        TaskDTOLazy dtoLazy = new TaskDTOLazy(1L, 1L, "Test Task", 1L);
        dtoLazy.setDescription(Optional.of("Test Description"));
        dtoLazy.setStart(Optional.of(LocalDateTime.now()));
        dtoLazy.setEnd(Optional.of(LocalDateTime.now().plusHours(2)));

        // When
        TaskBean bean = TaskMapper.INSTANCE.toBean(testGroup, dtoLazy);

        // Then
        assertNotNull(bean);
        assertThat(bean.getDescription(), is(equalTo(dtoLazy.description())));
        assertThat(bean.getStart(), is(equalTo(dtoLazy.start())));
        assertThat(bean.getEnd(), is(equalTo(dtoLazy.end())));
    }

    @Test
    void emptyOptionalFields_shouldMapToEmptyOptionals() {
        // Given
        TaskEntity<?, ?> mockEntity = createMockTaskEntity(1L, "Test Task");
        TaskBean bean = new TaskBean(testGroup, mockEntity);

        // When
        TaskLazy lazy = TaskMapper.INSTANCE.toLazy(bean);

        // Then
        assertNotNull(lazy);
        assertThat("Description should be empty", lazy.getDescription().isPresent(), is(false));
        assertThat("Start should be empty", lazy.getStart().isPresent(), is(false));
        assertThat("End should be empty", lazy.getEnd().isPresent(), is(false));
    }

    // Helper methods
    private TaskEntity<?, ?> createMockTaskEntity(Long id, String name) {
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

