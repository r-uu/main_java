package de.ruu.sandbox.office.microsoft.word.jasper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Datenmodell für eine Rechnung mit mehreren Positionen.
 */
public class InvoiceData
{
	private String invoiceNumber;
	private LocalDate invoiceDate;
	private String customerName;
	private String customerAddress;
	private List<InvoiceItem> items = new ArrayList<>();

	public InvoiceData(String invoiceNumber, LocalDate invoiceDate, String customerName, String customerAddress)
	{
		this.invoiceNumber = invoiceNumber;
		this.invoiceDate = invoiceDate;
		this.customerName = customerName;
		this.customerAddress = customerAddress;
	}

	public void addItem(InvoiceItem item)
	{
		items.add(item);
	}

	public BigDecimal getTotalAmount()
	{
		return items.stream()
				.map(InvoiceItem::getTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	// Getters
	public String getInvoiceNumber() { return invoiceNumber; }
	public LocalDate getInvoiceDate() { return invoiceDate; }
	public String getCustomerName() { return customerName; }
	public String getCustomerAddress() { return customerAddress; }
	public List<InvoiceItem> getItems() { return items; }

	/**
	 * Einzelne Rechnungsposition.
	 */
	public static class InvoiceItem
	{
		private String description;
		private int quantity;
		private BigDecimal unitPrice;
		private BigDecimal vatRate; // z.B. 0.19 für 19%

		public InvoiceItem(String description, int quantity, BigDecimal unitPrice, BigDecimal vatRate)
		{
			this.description = description;
			this.quantity = quantity;
			this.unitPrice = unitPrice;
			this.vatRate = vatRate;
		}

		public BigDecimal getNetTotal()
		{
			return unitPrice.multiply(BigDecimal.valueOf(quantity));
		}

		public BigDecimal getVatAmount()
		{
			return getNetTotal().multiply(vatRate);
		}

		public BigDecimal getTotal()
		{
			return getNetTotal().add(getVatAmount());
		}

		// Getters
		public String getDescription() { return description; }
		public int getQuantity() { return quantity; }
		public BigDecimal getUnitPrice() { return unitPrice; }
		public BigDecimal getVatRate() { return vatRate; }
	}
}

