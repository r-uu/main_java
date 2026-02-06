package de.ruu.lib.jpa.se.hibernate.postgres.demo;

import de.ruu.lib.jpa.core.Entity;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class SimpleTypeRoundtripTest
{
	@Test void roundtrip_preserves_id_version_and_name()
	{
		// Arrange
		Long   id      = 42L;
		Short  version = 3;
		String name    = "roundtrip";

		Entity<Long> stub = new Entity<>()
		{
			@Override public Long  id()      { return id;      }
			@Override public Short version() { return version; }
		};

		// Act: Create DTO from name and stub and convert back to entity
		SimpleTypeDTO    dto = new SimpleTypeDTO(name, stub);
		SimpleTypeEntity e1  = dto.toSource();

		// Assert: Entity has name/id/version from DTO/stub
		assertThat(e1             , is(not(nullValue())));
		assertThat(e1.name()      , is(name));
		assertThat(e1.getId()     , is(id));
		assertThat(e1.getVersion(), is(version));

		// Act: Entity -> DTO
		SimpleTypeDTO dto2 = e1.toDTO();

		// Assert: DTO contains same values
		assertThat(dto2             , is(not(nullValue())));
		assertThat(dto2.name()      , is(name));
		assertThat(dto2.getId()     , is(id));
		assertThat(dto2.getVersion(), is(version));
	}

	@Test void roundtrip_without_id_and_version_keeps_nulls()
	{
		String name = "new";

		// DTO ohne id/version
		SimpleTypeDTO    dto    = new SimpleTypeDTO(name);
		SimpleTypeEntity entity = dto.toSource();

		assertThat(entity             , is(not(nullValue())));
		assertThat(entity.name()      , is(name));
		assertThat(entity.getId()     , is(nullValue()));
		assertThat(entity.getVersion(), is(nullValue()));

		// Back to DTO
		SimpleTypeDTO dto2 = entity.toDTO();
		assertThat(dto2             , is(not(nullValue())));
		assertThat(dto2.name()      , is(name));
		assertThat(dto2.getId()     , is(nullValue()));
		assertThat(dto2.getVersion(), is(nullValue()));
	}
}