module de.ruu.sandbox.office.microsoft.word.docx4j
{
	// docx4j Core
	requires org.docx4j.openxml_objects;
	requires org.docx4j.core;

	// Jakarta XML Binding
	requires jakarta.xml.bind;

	// Logging
	requires org.slf4j;

	// Java Base Modules
	requires java.xml;

	// Open package for JAXB reflection
	opens de.ruu.sandbox.office.microsoft.word.docx4j to jakarta.xml.bind;
}

