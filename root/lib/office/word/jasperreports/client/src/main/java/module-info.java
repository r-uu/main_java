module de.ruu.lib.jasperreports.client {
    // Dependencies
    requires de.ruu.lib.jasperreports.model;
    requires com.google.gson;
    requires java.net.http;

    // Export client API
    exports de.ruu.lib.jasperreports.client;

    // Open package for Gson reflection (JavaBeans serialization)
    opens de.ruu.lib.jasperreports.client to com.google.gson;
}
