package de.ruu.lib.jpa.se.hibernate.postgres.demo;

import de.ruu.lib.jpa.core.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "simple_type")  // Verwendet standard "public" schema
@Getter
@Accessors(fluent = true) // generate fluent style getters but also implement java bean style getters
// to comply to java bean conventions
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PROTECTED) // required by jpa
public class SimpleTypeEntity extends AbstractEntity<SimpleTypeDTO> implements SimpleType
{
	@Setter private String name;

	SimpleTypeEntity(String name) { this.name  = name; }

	SimpleTypeDTO toDTO()
	{
		// Uses the DTO constructor that internally takes over id/version
		return new SimpleTypeDTO(name, this);
	}

	// Package-private wrapper to take over id/version from a DTO
	void mapIdAndVersionFrom(SimpleTypeDTO dto) { mapIdAndVersion(dto); }
}