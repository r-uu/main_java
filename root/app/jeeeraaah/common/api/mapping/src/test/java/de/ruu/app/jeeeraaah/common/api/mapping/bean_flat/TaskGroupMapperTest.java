package de.ruu.app.jeeeraaah.common.api.mapping.bean_flat;
import de.ruu.app.jeeeraaah.common.api.bean.TaskGroupBean;
import de.ruu.app.jeeeraaah.common.api.domain.flat.TaskGroupFlat;
import org.junit.jupiter.api.Test;
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
        TaskGroupBean bean = new TaskGroupBean("Test Group");
        TaskGroupFlat flat = TaskGroupMapper.INSTANCE.toFlat(bean);
        assertNotNull(flat);
        assertThat(flat.name(), is(equalTo(bean.name())));
    }
    @Test
    void beanToFlat_shouldMapDescriptionField() {
        TaskGroupBean bean = new TaskGroupBean("Test Group");
        bean.description("Test Description");
        TaskGroupFlat flat = TaskGroupMapper.INSTANCE.toFlat(bean);
        assertNotNull(flat);
        assertThat(flat.description(), is(equalTo(bean.description())));
        assertThat(flat.description().isPresent(), is(true));
        assertThat(flat.description().get(), is(equalTo("Test Description")));
    }
    @Test
    void flatToBean_shouldMapBasicFields() {
        TaskGroupBean sourceBean = new TaskGroupBean("Test Group");
        TaskGroupFlat flat = TaskGroupMapper.INSTANCE.toFlat(sourceBean);
        TaskGroupBean bean = TaskGroupMapper.INSTANCE.toBean(flat);
        assertNotNull(bean);
        assertThat(bean.name(), is(equalTo(flat.name())));
    }
    @Test
    void flatToBean_shouldMapDescriptionField() {
        TaskGroupBean sourceBean = new TaskGroupBean("Test Group");
        sourceBean.description("Test Description");
        TaskGroupFlat flat = TaskGroupMapper.INSTANCE.toFlat(sourceBean);
        TaskGroupBean bean = TaskGroupMapper.INSTANCE.toBean(flat);
        assertNotNull(bean);
        assertThat(bean.description(), is(equalTo(flat.description())));
        assertThat(bean.description().isPresent(), is(true));
        assertThat(bean.description().get(), is(equalTo("Test Description")));
    }
    @Test
    void bidirectionalMapping_shouldPreserveData() {
        TaskGroupBean originalBean = new TaskGroupBean("Original Group");
        originalBean.description("Original Description");
        TaskGroupFlat flat = TaskGroupMapper.INSTANCE.toFlat(originalBean);
        TaskGroupBean resultBean = TaskGroupMapper.INSTANCE.toBean(flat);
        assertNotNull(resultBean);
        assertThat(resultBean.name(), is(equalTo(originalBean.name())));
        assertThat(resultBean.description(), is(equalTo(originalBean.description())));
    }
    @Test
    void emptyOptionalFields_shouldMapToEmptyOptionals() {
        TaskGroupBean sourceBean = new TaskGroupBean("Test Group");
        TaskGroupFlat flat = TaskGroupMapper.INSTANCE.toFlat(sourceBean);
        TaskGroupBean bean = TaskGroupMapper.INSTANCE.toBean(flat);
        assertNotNull(bean);
        assertThat("Description should be empty", bean.description().isPresent(), is(false));
    }
}
