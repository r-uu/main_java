package de.ruu.app.jeeeraaah.backend.common.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskJPA;
import de.ruu.app.jeeeraaah.common.api.ws.rs.TaskDTO;
import de.ruu.lib.mapstruct.ReferenceCycleTracking;

public class MapTaskMappingTest
{
    @Test
    public void roundtrip_descriptionAndDates_areMapped() {
        TaskGroupJPA group = new TaskGroupJPA("group-1");
        TaskJPA jpa = new TaskJPA(group, "task-1");
        jpa.description("desc-jpa");
        jpa.start(LocalDate.of(2025,12,31));
        jpa.end(LocalDate.of(2026,1,1));

        ReferenceCycleTracking ctx = new ReferenceCycleTracking();
        TaskDTO dto = Mappings.toDTO(jpa, ctx);

        // check forward mapping (JPA -> DTO) using fluent API
        assertEquals("desc-jpa", dto.description().orElse(null));
        assertEquals(LocalDate.of(2025,12,31), dto.start().orElse(null));
        assertEquals(LocalDate.of(2026,1,1), dto.end().orElse(null));

        // change dto fields (fluent setters) and map back to JPA
        dto.description("desc-dto");
        dto.start(LocalDate.of(2024,1,1));
        dto.end(LocalDate.of(2024,12,31));

        TaskJPA jpa2 = Mappings.toJPA(dto, new ReferenceCycleTracking());

        assertEquals("desc-dto", jpa2.description().orElse(null));
        assertEquals(LocalDate.of(2024,1,1), jpa2.start().orElse(null));
        assertEquals(LocalDate.of(2024,12,31), jpa2.end().orElse(null));
    }
}
