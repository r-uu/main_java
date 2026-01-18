package de.ruu.lib.jasperreports.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic invoice data model for report generation.
 * <p>
 * Responsibilities:
 * - Structure all invoice data
 * - Perform calculations (subtotal, tax, total)
 * - Can be serialized to JSON for JasperReports Service
 * <p>
 * JasperReports accesses bean properties:
 * - $F{description}, $F{quantity}, $F{unitPrice}, $F{total} for items (fields)
 * - $P{invoiceNumber}, $P{customerName}, $P{subtotal}, etc. for parameters
 */
public class InvoiceData {

    private String            invoiceNumber;
    private LocalDate         invoiceDate;
    private LocalDate         dueDate;
    private String            customerName;
    private String            customerAddress;
    private List<InvoiceItem> items = new ArrayList<>();
    private String            notes;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.19"); // 19% VAT

    // Getters/Setters

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Business Logic: Calculations are performed here in the model

    public InvoiceData addItem(String description, int quantity, double unitPrice) {
        items.add(new InvoiceItem(description, quantity, unitPrice));
        return this;
    }

    /**
     * Calculates subtotal (all items without tax).
     * This method is called by JasperReports template.
     */
    public BigDecimal getSubtotal() {
        return items.stream()
            .map(InvoiceItem::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates tax (19% of subtotal).
     * This method is called by JasperReports template.
     */
    public BigDecimal getTax() {
        return getSubtotal().multiply(TAX_RATE);
    }

    /**
     * Calculates grand total (subtotal + tax).
     * This method is called by JasperReports template.
     */
    public BigDecimal getTotal() {
        return getSubtotal().add(getTax());
    }

    // Builder Pattern

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final InvoiceData invoice = new InvoiceData();

        public Builder invoiceNumber(String invoiceNumber) {
            invoice.setInvoiceNumber(invoiceNumber);
            return this;
        }

        public Builder invoiceDate(LocalDate date) {
            invoice.setInvoiceDate(date);
            return this;
        }

        public Builder dueDate(LocalDate date) {
            invoice.setDueDate(date);
            return this;
        }

        public Builder customer(String name, String address) {
            invoice.setCustomerName(name);
            invoice.setCustomerAddress(address);
            return this;
        }

        public Builder addItem(String description, int quantity, double unitPrice) {
            invoice.addItem(description, quantity, unitPrice);
            return this;
        }

        public Builder notes(String notes) {
            invoice.setNotes(notes);
            return this;
        }

        public InvoiceData build() {
            return invoice;
        }
    }
}

