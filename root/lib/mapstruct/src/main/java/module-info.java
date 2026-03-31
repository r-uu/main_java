module de.ruu.lib.mapstruct
{
	exports de.ruu.lib.mapstruct;

	requires static lombok;

	// Make MapStruct available transitively to consumers using its annotations
	requires transitive org.mapstruct;
	requires de.ruu.lib.util;

	opens de.ruu.lib.mapstruct;
}