package de.ruu.app.jeeeraaah.common.api.mapping.bean_lazy;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.lazy.TaskGroupLazy;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
/**
 * Tests for {@link TaskGroupMapper} - bidirectional TaskGroup Bean ↔ Lazy mappings.
 * <p>
 * Note: Lazy mappings require persisted entities with non-null IDs.
 */
class TaskGroupMapperTest {
    private TaskGroupBean createTaskGroupBean(String name, String description) throws Exception {
        TaskGroupBean bean = new TaskGroupBean(name);
        if (description != null) {
            bean.description(description);
        }
        // Use reflection to set ID and version since they have no public setters
        Field idField = TaskGroupBean.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(bean, 1L);
        Field versionField = TaskGroupBean.class.getDeclaredField("version");
        versionField.setAccessible(true);
        versionField.set(bean, (short) 1);
        return bean;
    }
    @Test
    void mapperInstanceExists() {
        assertNotNull(TaskGroupMapper.INSTANCE, "Mapper instance should exist");
    }
    @Test
    void beanToLazy_shouldMapBasicFields() throws Exception {
        TaskGroupBean bean = createTaskGroupBean("Test Group", null);
        TaskGroupLazy lazy = TaskGroupMapper.INSTANCE.toLazy(bean);
        assertNotNull(lazy);
        assertThat(lazy.name(), is(equalTo(bean.name())));
    }
    @Test
    void beanToLazy_shouldMapDescriptionField() throws Exception {
        TaskGroupBean bean = createTaskGroupBean("Test Group", "Test Description");
        TaskGroupLazy lazy = TaskGroupMapper.INSTANCE.toLazy(bean);
        assertNotNull(lazy);
        assertThat(lazy.description(), is(equalTo(bean.description())));
        assertThat(lazy.description().isPresent(), is(true));
        assertThat(lazy.description().get(), is(equalTo("Test Description")));
    }
    @Test
    void lazyToBean_shouldMapBasicFields() throws Exception {
        TaskGroupBean sourceBean = createTaskGroupBean("Test Group", null);
        TaskGroupLazy lazy = TaskGroupMapper.INSTANCE.toLazy(sourceBean);
        TaskGroupBean bean = TaskGroupMapper.INSTANCE.toBean(lazy);
        assertNotNull(bean);
        assertThat(bean.name(), is(equalTo(lazy.name())));
    }
    @Test
    void lazyToBean_shouldMapDescriptionField() throws Exception {
        TaskGroupBean sourceBean = createTaskGroupBean("Test Group", "Test Description");
        TaskGroupLazy lazy = TaskGroupMapper.INSTANCE.toLazy(sourceBean);
        TaskGroupBean bean = TaskGroupMapper.INSTANCE.toBean(lazy);
        assertNotNull(bean);
        assertThat(bean.description(), is(equalTo(lazy.description())));
        assertThat(bean.description().isPresent(), is(true));
        assertThat(bean.description().get(), is(equalTo("Test Description")));
    }
    @Test
    void bidirectionalMapping_shouldPreserveData() throws Exception {
        TaskGroupBean originalBean = createTaskGroupBean("Original Group", "Original Description");
        TaskGroupLazy lazy = TaskGroupMapper.INSTANCE.toLazy(originalBean);
        TaskGroupBean resultBean = TaskGroupMapper.INSTANCE.toBean(lazy);
        assertNotNull(resultBean);
        assertThat(resultBean.name(), is(equalTo(originalBean.name())));
        assertThat(resultBean.description(), is(equalTo(originalBean.description())));
    }
    @Test
    void emptyOptionalFields_shouldMapToEmptyOptionals() throws Exception {
        TaskGroupBean sourceBean = createTaskGroupBean("Test Group", null);
        TaskGroupLazy lazy = TaskGroupMapper.INSTANCE.toLazy(sourceBean);
        TaskGroupBean bean = TaskGroupMapper.INSTANCE.toBean(lazy);
        assertNotNull(bean);
        assertThat("Description should be empty", bean.description().isPresent(), is(false));
    }
}
