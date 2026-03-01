module de.ruu.lib.jasperreports.model {
    // Jackson for JSON serialization
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    // Export model classes
    exports de.ruu.lib.jasperreports.model;

    // Open package for reflection (JavaBeans serialization)
    // - Jackson: Server-side serialization
    opens de.ruu.lib.jasperreports.model to com.fasterxml.jackson.databind;
}

