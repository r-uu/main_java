package de.ruu.lib.jasperreports.model;

import java.math.BigDecimal;

/**
 * Single invoice line item.
 * <p>
 * Represents one line item in an invoice with:
 * - Description of the item/service
 * - Quantity
 * - Unit price
 * - Automatically calculated total (quantity × unit price)
 * <p>
 * Calculations are performed in the model, not in the report service!
 */
public class InvoiceItem {

	private int        position;
	private String     description;
	private BigDecimal quantity;
	private BigDecimal unitPrice;
	private BigDecimal total;

	public InvoiceItem() {
		this.quantity = BigDecimal.ZERO;
		this.unitPrice = BigDecimal.ZERO;
		this.total = BigDecimal.ZERO;
	}

	public InvoiceItem(String description, int quantity, double unitPrice) {
		this.description = description;
		this.quantity = BigDecimal.valueOf(quantity);
		this.unitPrice = BigDecimal.valueOf(unitPrice);
		calculateTotal();
	}

	// Getters and Setters

	public int getPosition() { return position; }
	public void setPosition(int position) { this.position = position; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public BigDecimal getQuantity() { return quantity; }
	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
		calculateTotal();
	}

	public BigDecimal getUnitPrice() { return unitPrice; }
	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
		calculateTotal();
	}

	/**
	 * Returns the calculated total (quantity × unit price).
	 * This is called by JasperReports template: $F{total}
	 */
	public BigDecimal getTotal() {
		return total;
	}

	/**
	 * Calculates total price for this item.
	 * Called automatically when quantity or unit price changes.
	 */
	private void calculateTotal() {
		this.total = quantity.multiply(unitPrice);
	}
}

