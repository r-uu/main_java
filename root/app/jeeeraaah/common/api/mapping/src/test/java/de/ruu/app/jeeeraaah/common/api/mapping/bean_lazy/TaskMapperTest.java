package de.ruu.app.jeeeraaah.common.api.mapping.bean_lazy;
import de.ruu.app.jeeeraaah.common.api.bean.TaskBean;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.lazy.TaskLazy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.time.LocalDate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
/**
 * Tests for {@link TaskMapper} - bidirectional Task Bean ↔ Lazy mappings.
 * <p>
 * Note: Lazy mappings require persisted entities with non-null IDs.
 */
class TaskMapperTest {
    private TaskGroupBean testGroup;
    @BeforeEach
    void setUp() throws Exception {
        testGroup = new TaskGroupBean("Test Group");
        // Use reflection to set ID and version
        Field idField = TaskGroupBean.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testGroup, 1L);
        Field versionField = TaskGroupBean.class.getDeclaredField("version");
        versionField.setAccessible(true);
        versionField.set(testGroup, (short) 1);
    }
    private TaskBean createTaskBean(String name, LocalDate start, LocalDate end, String description) throws Exception {
        TaskBean bean = new TaskBean(testGroup, name);
        if (description != null) bean.description(description);
        if (start != null) bean.start(start);
        if (end != null) bean.end(end);
        // Use reflection to set ID and version
        Field idField = TaskBean.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(bean, 1L);
        Field versionField = TaskBean.class.getDeclaredField("version");
        versionField.setAccessible(true);
        versionField.set(bean, (short) 1);
        return bean;
    }
    @Test
    void mapperInstanceExists() {
        assertNotNull(TaskMapper.INSTANCE, "Mapper instance should exist");
    }
    @Test
    void beanToLazy_shouldMapBasicFields() throws Exception {
        TaskBean bean = createTaskBean("Test Task", null, null, null);
        TaskLazy lazy = TaskMapper.INSTANCE.toLazy(bean);
        assertNotNull(lazy);
        assertThat(lazy.name(), is(equalTo(bean.name())));
    }
    @Test
    void beanToLazy_shouldMapOptionalFields() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(7);
        TaskBean bean = createTaskBean("Test Task", start, end, "Test Description");
        TaskLazy lazy = TaskMapper.INSTANCE.toLazy(bean);
        assertNotNull(lazy);
        assertThat(lazy.description(), is(equalTo(bean.description())));
        assertThat(lazy.start(), is(equalTo(bean.start())));
        assertThat(lazy.end(), is(equalTo(bean.end())));
    }
    @Test
    void lazyToBean_shouldMapBasicFields() throws Exception {
        TaskBean sourceBean = createTaskBean("Test Task", null, null, null);
        TaskLazy lazy = TaskMapper.INSTANCE.toLazy(sourceBean);
        TaskBean bean = TaskMapper.INSTANCE.toBean(testGroup, lazy);
        assertNotNull(bean);
        assertThat(bean.name(), is(equalTo(lazy.name())));
    }
    @Test
    void lazyToBean_shouldMapOptionalFields() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(7);
        TaskBean sourceBean = createTaskBean("Test Task", start, end, "Test Description");
        TaskLazy lazy = TaskMapper.INSTANCE.toLazy(sourceBean);
        TaskBean bean = TaskMapper.INSTANCE.toBean(testGroup, lazy);
        assertNotNull(bean);
        assertThat(bean.description(), is(equalTo(lazy.description())));
        assertThat(bean.start(), is(equalTo(lazy.start())));
        assertThat(bean.end(), is(equalTo(lazy.end())));
    }
    @Test
    void bidirectionalMapping_shouldPreserveData() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(7);
        TaskBean originalBean = createTaskBean("Original Task", start, end, "Original Description");
        TaskLazy lazy = TaskMapper.INSTANCE.toLazy(originalBean);
        TaskBean resultBean = TaskMapper.INSTANCE.toBean(testGroup, lazy);
        assertNotNull(resultBean);
        assertThat(resultBean.name(), is(equalTo(originalBean.name())));
        assertThat(resultBean.description(), is(equalTo(originalBean.description())));
    }
    @Test
    void emptyOptionalFields_shouldMapCorrectly() throws Exception {
        TaskBean sourceBean = createTaskBean("Test Task", null, null, null);
        TaskLazy lazy = TaskMapper.INSTANCE.toLazy(sourceBean);
        TaskBean bean = TaskMapper.INSTANCE.toBean(testGroup, lazy);
        assertNotNull(bean);
        assertThat("Description should be empty", bean.description().isPresent(), is(false));
    }
}
