package de.ruu.lib.jasperreports.example;

import de.ruu.lib.jasperreports.client.JasperReportsClient;
import de.ruu.lib.jasperreports.client.JasperReportsClient.ReportFormat;
import de.ruu.lib.jasperreports.model.InvoiceData;

import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Example: How to use the JasperReports Client.
 * <p>
 * This demonstrates the clean separation between:
 * - Model (business data and calculations)
 * - Client (generic report generation API)
 * - Server (isolated JasperReports rendering in Docker)
 */
public class InvoiceExample
{
	public static void main(String[] args) throws Exception
	{
		// 1. Create client
		var client = new JasperReportsClient();

		// Check service availability
		if (!client.isServiceAvailable())
		{
			System.err.println("❌ JasperReports Service is not reachable!");
			System.err.println("   Start the service with: jasper-start"   );
			System.exit(1);
		}

		// 2. Create invoice data (with Builder Pattern)
		InvoiceData invoice = InvoiceData.builder()
				                      .invoiceNumber("INV-2026-001")
				                      .invoiceDate(LocalDate.now())
				                      .dueDate(LocalDate.now().plusDays(30))
				                      .customer("Max Mustermann", "Hauptstraße 1\n12345 Berlin")
				                      .addItem("Software Development", 10, 100.00)
				                      .addItem("Consulting", 5, 150.00)
				                      .addItem("Training", 2, 200.00)
				                      .notes("Payable within 30 days.\nThank you for your order!")
				                      .build();

		System.out.println("📄 Generating invoice...");
		System.out.println("   Number: " + invoice.getInvoiceNumber());
		System.out.println("   Customer: " + invoice.getCustomerName());
		System.out.println("   Subtotal: " + invoice.getSubtotal() + " €");
		System.out.println("   VAT (19%): " + invoice.getTax() + " €");
		System.out.println("   Total: " + invoice.getTotal() + " €");
		System.out.println();

		// 3. Generate PDF
		Path pdf = client.generateReport("invoice.jrxml", invoice, ReportFormat.PDF);
		System.out.println("✅ PDF created: " + pdf);

		// 4. Alternatively: Generate DOCX
		Path docx = client.generateReport("invoice.jrxml", invoice, ReportFormat.DOCX);
		System.out.println("✅ DOCX created: " + docx);

		System.out.println();
		System.out.println("🎉 Done! You can now open the files:");
		System.out.println("   " + pdf.toAbsolutePath());
		System.out.println("   " + docx.toAbsolutePath());
	}
}

