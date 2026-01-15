package de.ruu.jasper;

import io.javalin.Javalin;
import io.javalin.http.Context;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * JasperReports Service - Isolierte Umgebung für Report-Generierung
 *
 * Kann als REST API oder CLI verwendet werden:
 * - REST: http://localhost:8090/api/report/generate
 * - CLI:  java -jar jasperreports-service.jar template.jrxml data.json output.pdf
 */
public class ReportService {

    private static final String TEMPLATES_DIR = "/app/templates";
    private static final String OUTPUT_DIR = "/app/output";

    public static void main(String[] args) {
        if (args.length > 0 && "cli".equals(args[0])) {
            runCLI(args);
        } else {
            runServer();
        }
    }

    /**
     * Startet REST API Server auf Port 8090
     */
    private static void runServer() {
        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
        }).start(8090);

        // Health Check
        app.get("/health", ctx -> ctx.result("OK"));

        // Report Generation Endpoint
        app.post("/api/report/generate", ReportService::generateReport);

        // List Templates
        app.get("/api/templates", ReportService::listTemplates);

        System.out.println("🚀 JasperReports Service läuft auf http://localhost:8090");
        System.out.println("📄 API Dokumentation:");
        System.out.println("   POST /api/report/generate - Generate Report");
        System.out.println("   GET  /api/templates - List Templates");
        System.out.println("   GET  /health - Health Check");
    }

    /**
     * CLI Mode für direkte Report-Generierung
     */
    private static void runCLI(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: java -jar jasperreports-service.jar cli <template.jrxml> <data.json> <output.pdf|docx>");
            System.exit(1);
        }

        String templatePath = args[1];
        String dataPath = args[2];
        String outputPath = args[3];

        try {
            // Template kompilieren
            JasperReport report = JasperCompileManager.compileReport(templatePath);

            // Daten laden (TODO: JSON zu Map konvertieren)
            Map<String, Object> parameters = new HashMap<>();

            // Report füllen
            JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());

            // Export
            if (outputPath.endsWith(".pdf")) {
                JasperExportManager.exportReportToPdfFile(print, outputPath);
            } else if (outputPath.endsWith(".docx")) {
                JRDocxExporter exporter = new JRDocxExporter();
                exporter.setExporterInput(new SimpleExporterInput(print));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(new File(outputPath)));
                exporter.exportReport();
            }

            System.out.println("✅ Report generiert: " + outputPath);

        } catch (Exception e) {
            System.err.println("❌ Fehler: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * REST Endpoint: Report generieren
     */
    private static void generateReport(Context ctx) {
        try {
            // Request Body parsen
            var body = ctx.bodyAsClass(ReportRequest.class);

            // Template kompilieren
            String templatePath = TEMPLATES_DIR + "/" + body.template;
            JasperReport report = JasperCompileManager.compileReport(templatePath);

            // Report füllen
            JasperPrint print = JasperFillManager.fillReport(
                report,
                body.parameters != null ? body.parameters : new HashMap<>(),
                new JREmptyDataSource()
            );

            // Output-Datei
            String outputFile = OUTPUT_DIR + "/" + UUID.randomUUID() + "." + body.format;

            // Export
            if ("pdf".equals(body.format)) {
                JasperExportManager.exportReportToPdfFile(print, outputFile);
            } else if ("docx".equals(body.format)) {
                JRDocxExporter exporter = new JRDocxExporter();
                exporter.setExporterInput(new SimpleExporterInput(print));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(new File(outputFile)));
                exporter.exportReport();
            }

            // Response
            ctx.status(200).json(Map.of(
                "success", true,
                "outputFile", outputFile,
                "message", "Report erfolgreich generiert"
            ));

        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * REST Endpoint: Templates auflisten
     */
    private static void listTemplates(Context ctx) {
        try {
            File templatesDir = new File(TEMPLATES_DIR);
            String[] templates = templatesDir.list((dir, name) -> name.endsWith(".jrxml"));

            ctx.json(Map.of(
                "templates", templates != null ? Arrays.asList(templates) : List.of()
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Request Model
     */
    static class ReportRequest {
        public String template;
        public String format = "pdf"; // pdf oder docx
        public Map<String, Object> parameters;
    }
}

