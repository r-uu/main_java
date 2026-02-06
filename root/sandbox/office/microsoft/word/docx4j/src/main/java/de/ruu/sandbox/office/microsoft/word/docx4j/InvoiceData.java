package de.ruu.sandbox.office.microsoft.word.docx4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data model for an invoice.
 *
 * Contains all information needed to generate an invoice:
 * - Invoice metadata (number, date)
 * - Customer information (name, address)
 * - List of invoice items
 * - Automatically calculated total amount
 */
public class InvoiceData
{
	private String invoiceNumber;
	private LocalDate invoiceDate;
	private String customerName;
	private String customerAddress;
	private List<InvoiceItem> items;
	private BigDecimal totalAmount;

	public InvoiceData()
	{
		this.items = new ArrayList<>();
		this.totalAmount = BigDecimal.ZERO;
	}

	// Getter und Setter
	public String getInvoiceNumber() { return invoiceNumber; }
	public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

	public LocalDate getInvoiceDate() { return invoiceDate; }
	public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

	public String getCustomerName() { return customerName; }
	public void setCustomerName(String customerName) { this.customerName = customerName; }

	public String getCustomerAddress() { return customerAddress; }
	public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

	public List<InvoiceItem> getItems() { return items; }
	public void setItems(List<InvoiceItem> items)
	{
		this.items = items;
		calculateTotal();
	}

	public void addItem(InvoiceItem item)
	{
		this.items.add(item);
		calculateTotal();
	}

	public BigDecimal getTotalAmount() { return totalAmount; }

	private void calculateTotal()
	{
		this.totalAmount = items.stream()
				.map(InvoiceItem::getTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/**
	 * Creates sample data with 50 items for multi-page invoice
	 */
	public static InvoiceData createSampleData()
	{
		InvoiceData invoice = new InvoiceData();
		invoice.setInvoiceNumber("RE-2026-001");
		invoice.setInvoiceDate(LocalDate.now());
		invoice.setCustomerName("Musterfirma GmbH");
		invoice.setCustomerAddress("Musterstraße 123\n12345 Musterstadt");

		// 50 items for multi-page invoice with subtotals
		for (int i = 1; i <= 50; i++)
		{
			InvoiceItem item = new InvoiceItem();
			item.setPosition(i);
			item.setDescription("Artikel " + i + " - Beschreibung");
			item.setQuantity(BigDecimal.valueOf(i % 10 + 1));
			item.setUnitPrice(BigDecimal.valueOf(19.99 + i));
			invoice.addItem(item);
		}

		return invoice;
	}
}

