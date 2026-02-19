module de.ruu.lib.jpa.core
{
	exports de.ruu.lib.jpa.core;
	exports de.ruu.lib.jpa.core.criteria;
	exports de.ruu.lib.jpa.core.criteria.restriction;

	requires com.fasterxml.jackson.annotation;
	requires de.ruu.lib.util;
	requires jakarta.annotation;
	requires jakarta.json.bind;
	requires jakarta.persistence;
	requires java.sql;
	requires static lombok;
	requires java.desktop;

	// Opens for reflection by JPA providers (Hibernate)
	// - org.hibernate.orm.core: for entity scanning and persistence operations
	opens de.ruu.lib.jpa.core to org.hibernate.orm.core;
}