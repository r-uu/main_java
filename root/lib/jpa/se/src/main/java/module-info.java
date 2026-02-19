module de.ruu.lib.jpa.se
{
	exports de.ruu.lib.jpa.se;

	// Open for Hibernate persistence provider reflection
	opens de.ruu.lib.jpa.se to org.hibernate.orm.core;

	requires jakarta.annotation;
	requires jakarta.inject;
	requires jakarta.interceptor;
	requires jakarta.persistence;
	requires static lombok;
	requires org.slf4j;
}