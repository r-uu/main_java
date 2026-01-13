package de.ruu.sandbox.office.microsoft.word.jasper;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Generator für Rechnungen im DOCX-Format mit JasperReports.
 *
 * Features:
 * - Mehrseitige Rechnungen
 * - Automatische Zwischensummen pro Seite
 * - Überträge ("Übertrag von Seite X")
 * - Export nach DOCX und PDF
 */
public class InvoiceGenerator
{
	private static final String TEMPLATE_PATH = "/templates/invoice_template.jrxml";

	/**
	 * Generiert eine Rechnung im DOCX-Format.
	 *
	 * @param invoiceData Die Rechnungsdaten
	 * @param outputPath Pfad zur Ausgabedatei
	 * @throws Exception bei Fehlern
	 */
	public void generateDocx(InvoiceData invoiceData, Path outputPath) throws Exception
	{
		JasperPrint jasperPrint = generateJasperPrint(invoiceData);

		// Export zu DOCX
		try (OutputStream outputStream = Files.newOutputStream(outputPath))
		{
			JRDocxExporter exporter = new JRDocxExporter();
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
			exporter.exportReport();
		}

		System.out.println("Rechnung erstellt: " + outputPath);
	}

	/**
	 * Generiert eine Rechnung im PDF-Format.
	 */
	public void generatePdf(InvoiceData invoiceData, Path outputPath) throws Exception
	{
		JasperPrint jasperPrint = generateJasperPrint(invoiceData);
		JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath.toString());
		System.out.println("Rechnung erstellt: " + outputPath);
	}

	private JasperPrint generateJasperPrint(InvoiceData invoiceData) throws Exception
	{
		// Template laden
		InputStream templateStream = getClass().getResourceAsStream(TEMPLATE_PATH);
		if (templateStream == null)
		{
			throw new IllegalStateException("Template nicht gefunden: " + TEMPLATE_PATH);
		}

		// Template kompilieren
		JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

		// Parameter setzen
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("INVOICE_NUMBER", invoiceData.getInvoiceNumber());
		parameters.put("INVOICE_DATE", invoiceData.getInvoiceDate());
		parameters.put("CUSTOMER_NAME", invoiceData.getCustomerName());
		parameters.put("CUSTOMER_ADDRESS", invoiceData.getCustomerAddress());
		parameters.put("TOTAL_AMOUNT", invoiceData.getTotalAmount());

		// Datenquelle erstellen
		JRBeanCollectionDataSource dataSource =
				new JRBeanCollectionDataSource(invoiceData.getItems());

		// Report füllen
		return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
	}

	/**
	 * Beispiel-Main-Methode zum Testen.
	 */
	public static void main(String[] args) throws Exception
	{
		// Beispiel-Rechnung erstellen
		InvoiceData invoice = new InvoiceData(
				"RE-2026-001",
				LocalDate.now(),
				"Max Mustermann GmbH",
				"Musterstraße 123\n12345 Musterstadt"
		);

		// Viele Positionen hinzufügen (für mehrseitige Rechnung)
		for (int i = 1; i <= 50; i++)
		{
			invoice.addItem(new InvoiceData.InvoiceItem(
					"Position " + i + ": Beispiel-Artikel mit längerer Beschreibung",
					i,
					new BigDecimal("19.99"),
					new BigDecimal("0.19") // 19% MwSt
			));
		}

		// Generator erstellen und Rechnung generieren
		InvoiceGenerator generator = new InvoiceGenerator();

		Path docxPath = Paths.get("target/rechnung_beispiel.docx");
		Path pdfPath = Paths.get("target/rechnung_beispiel.pdf");

		generator.generateDocx(invoice, docxPath);
		generator.generatePdf(invoice, pdfPath);

		System.out.println("\n======================");
		System.out.println("Rechnungen erstellt!");
		System.out.println("======================");
		System.out.println("DOCX: " + docxPath.toAbsolutePath());
		System.out.println("PDF:  " + pdfPath.toAbsolutePath());
	}
}

