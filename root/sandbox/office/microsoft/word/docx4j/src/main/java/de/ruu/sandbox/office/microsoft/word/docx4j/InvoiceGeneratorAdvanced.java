package de.ruu.sandbox.office.microsoft.word.docx4j;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Advanced invoice generator with automatic page size calculation.
 *
 * This version automatically calculates the maximum number of items per page
 * based on page dimensions, margins, and row height.
 *
 * Features:
 * - Automatic page break calculation
 * - Configurable page dimensions (A4 default)
 * - Configurable margins and header/footer heights
 * - Configurable row height
 * - Logs calculated values at startup
 *
 * Calculation:
 * Available Height = Page Height - Top Margin - Bottom Margin - Header - Footer
 * Max Items = Available Height / Row Height
 *
 * Example (A4, 1" margins):
 * - Page: 297mm (15840 Twips)
 * - Margins: 2x 1" (2880 Twips)
 * - Header/Footer: ~7.5cm (4320 Twips)
 * - Available: 8640 Twips
 * - Row Height: 6.3mm (360 Twips)
 * - Result: ~24 items per page
 */
public class InvoiceGeneratorAdvanced
{
	private static final Logger log = LoggerFactory.getLogger(InvoiceGeneratorAdvanced.class);
	private static final ObjectFactory factory = new ObjectFactory();

	// Page dimensions in Twips (1 Twip = 1/1440 inch)
	private static final int PAGE_HEIGHT_TWIPS = 15840;   // A4: 297mm = 11.69"
	private static final int TOP_MARGIN_TWIPS = 1440;     // 1 inch = 25.4mm
	private static final int BOTTOM_MARGIN_TWIPS = 1440;  // 1 inch = 25.4mm
	private static final int HEADER_HEIGHT_TWIPS = 2880;  // ~5cm
	private static final int FOOTER_HEIGHT_TWIPS = 1440;  // ~2.5cm
	private static final int ROW_HEIGHT_TWIPS = 360;      // ~6.3mm per row

	// Calculate available height for content rows
	private static final int AVAILABLE_HEIGHT_TWIPS = PAGE_HEIGHT_TWIPS
			- TOP_MARGIN_TWIPS
			- BOTTOM_MARGIN_TWIPS
			- HEADER_HEIGHT_TWIPS
			- FOOTER_HEIGHT_TWIPS;

	// Automatically calculated maximum items per page
	private static final int MAX_ITEMS_PER_PAGE = AVAILABLE_HEIGHT_TWIPS / ROW_HEIGHT_TWIPS;

	static {
		log.info("Automatische Seitenberechnung:");
		log.info("  Seitenhöhe: {} Twips ({}mm)", PAGE_HEIGHT_TWIPS, PAGE_HEIGHT_TWIPS / 56.7);
		log.info("  Verfügbare Höhe für Inhalt: {} Twips ({}mm)", AVAILABLE_HEIGHT_TWIPS, AVAILABLE_HEIGHT_TWIPS / 56.7);
		log.info("  Zeilenhöhe: {} Twips ({}mm)", ROW_HEIGHT_TWIPS, ROW_HEIGHT_TWIPS / 56.7);
		log.info("  → Maximale Zeilen pro Seite: {}", MAX_ITEMS_PER_PAGE);
	}

	public static void main(String[] args)
	{
		try
		{
			log.info("=== docx4j Advanced Invoice Generator ===");

			// Viele Testdaten erstellen
			InvoiceData invoiceData = InvoiceData.createSampleData();
			// Auf 100 Items erweitern für Multi-Page-Test
			for (int i = invoiceData.getItems().size() + 1; i <= 100; i++)
			{
				invoiceData.addItem(new InvoiceItem("Position #" + i, 1, 19.99));
			}

			log.info("Testdaten erstellt: {} Positionen", invoiceData.getItems().size());
			log.info("Erwartete Seitenanzahl: ca. {}", (invoiceData.getItems().size() / MAX_ITEMS_PER_PAGE) + 1);

			// Rechnung generieren
			InvoiceGeneratorAdvanced generator = new InvoiceGeneratorAdvanced();
			String outputPath = "rechnung_advanced.docx";
			generator.generateInvoice(invoiceData, outputPath);

			log.info("✓ Rechnung erstellt: {}", outputPath);
			System.out.println("\n✓ Advanced Rechnung erstellt: " + outputPath);
			System.out.println("  Automatisch berechnete Zeilen pro Seite: " + MAX_ITEMS_PER_PAGE);
		}
		catch (Exception e)
		{
			log.error("Fehler beim Generieren der Rechnung", e);
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void generateInvoice(InvoiceData invoiceData, String outputPath) throws Exception
	{
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();

		// Seiteneinstellungen setzen
		setPageDimensions(wordMLPackage);

		// Kopfbereich
		addHeader(mainDocumentPart, invoiceData);
		addSpacer(mainDocumentPart);

		// Tabelle mit automatischen Seitenumbrüchen
		addItemsTableWithAutoPageBreaks(mainDocumentPart, invoiceData);
		addSpacer(mainDocumentPart);

		// Gesamtsumme
		addTotal(mainDocumentPart, invoiceData);

		// Speichern
		File outputFile = new File(outputPath);
		File parentDir = outputFile.getParentFile();
		if (parentDir != null)
		{
			parentDir.mkdirs();
		}
		wordMLPackage.save(outputFile);
	}

	private void setPageDimensions(WordprocessingMLPackage wordMLPackage)
	{
		// Seiteneinstellungen für A4 mit definierten Rändern
		SectPr sectPr = factory.createSectPr();
		SectPr.PgSz pgSz = factory.createSectPrPgSz();
		pgSz.setW(BigInteger.valueOf(11906)); // A4 Breite: 210mm
		pgSz.setH(BigInteger.valueOf(PAGE_HEIGHT_TWIPS)); // A4 Höhe: 297mm
		sectPr.setPgSz(pgSz);

		SectPr.PgMar pgMar = factory.createSectPrPgMar();
		pgMar.setTop(BigInteger.valueOf(TOP_MARGIN_TWIPS));
		pgMar.setBottom(BigInteger.valueOf(BOTTOM_MARGIN_TWIPS));
		pgMar.setLeft(BigInteger.valueOf(1440)); // 1 Zoll links
		pgMar.setRight(BigInteger.valueOf(1440)); // 1 Zoll rechts
		sectPr.setPgMar(pgMar);

		wordMLPackage.getMainDocumentPart().getJaxbElement().getBody().setSectPr(sectPr);
	}

	private void addItemsTableWithAutoPageBreaks(MainDocumentPart mainDocumentPart, InvoiceData invoiceData)
	{
		Tbl table = factory.createTbl();
		addTableBorders(table);
		addTableHeader(table);

		int itemsOnCurrentPage = 1; // Header zählt mit
		int currentPage = 1;
		BigDecimal pageSubtotal = BigDecimal.ZERO;
		BigDecimal carryOver = BigDecimal.ZERO;

		for (int i = 0; i < invoiceData.getItems().size(); i++)
		{
			InvoiceItem item = invoiceData.getItems().get(i);

			// Check if page break is needed (AUTOMATICALLY calculated!)
			if (itemsOnCurrentPage >= MAX_ITEMS_PER_PAGE)
			{
				log.debug("Seitenumbruch nach {} Zeilen", itemsOnCurrentPage);

				// Subtotal for current page
				addSubtotalRow(table, currentPage, pageSubtotal);
				mainDocumentPart.addObject(table);

				// Seitenumbruch
				addPageBreak(mainDocumentPart);

				// Neue Seite
				currentPage++;
				carryOver = pageSubtotal;
				pageSubtotal = BigDecimal.ZERO;
				itemsOnCurrentPage = 0;

				// Neue Tabelle
				table = factory.createTbl();
				addTableBorders(table);
				addTableHeader(table);
				addCarryOverRow(table, currentPage - 1, carryOver);
				pageSubtotal = carryOver;
				itemsOnCurrentPage += 2; // Header + carry-over
			}

			// Add item
			addItemRow(table, i + 1, item);
			pageSubtotal = pageSubtotal.add(item.getTotal());
			itemsOnCurrentPage++;
		}

		// Letzte Zwischensumme (falls mehrseitig)
		if (currentPage > 1)
		{
			addSubtotalRow(table, currentPage, pageSubtotal);
		}

		mainDocumentPart.addObject(table);
		log.info("Dokument erstellt mit {} Seiten", currentPage);
	}

	private void addHeader(MainDocumentPart mainDocumentPart, InvoiceData invoiceData)
	{
		P title = factory.createP();
		addBoldText(title, "RECHNUNG", 24);
		mainDocumentPart.addObject(title);

		addSpacer(mainDocumentPart);

		P invoiceInfo = factory.createP();
		addText(invoiceInfo, "Rechnungsnummer: " + invoiceData.getInvoiceNumber());
		mainDocumentPart.addObject(invoiceInfo);

		P dateInfo = factory.createP();
		addText(dateInfo, "Datum: " + invoiceData.getInvoiceDate());
		mainDocumentPart.addObject(dateInfo);
	}

	private void addTableBorders(Tbl table)
	{
		TblPr tblPr = factory.createTblPr();
		TblBorders borders = factory.createTblBorders();

		CTBorder border = factory.createCTBorder();
		border.setVal(STBorder.SINGLE);
		border.setSz(BigInteger.valueOf(4));

		borders.setTop(border);
		borders.setBottom(border);
		borders.setLeft(border);
		borders.setRight(border);
		borders.setInsideH(border);
		borders.setInsideV(border);

		tblPr.setTblBorders(borders);
		table.setTblPr(tblPr);
	}

	private void addTableHeader(Tbl table)
	{
		Tr headerRow = factory.createTr();
		addTableCell(headerRow, "Pos.", true);
		addTableCell(headerRow, "Beschreibung", true);
		addTableCell(headerRow, "Menge", true);
		addTableCell(headerRow, "Einzelpreis", true);
		addTableCell(headerRow, "Gesamt", true);
		table.getContent().add(headerRow);
	}

	private void addItemRow(Tbl table, int position, InvoiceItem item)
	{
		Tr row = factory.createTr();
		addTableCell(row, String.valueOf(position), false);
		addTableCell(row, item.getDescription(), false);
		addTableCell(row, String.valueOf(item.getQuantity()), false);
		addTableCell(row, String.format("%.2f €", item.getUnitPrice()), false);
		addTableCell(row, String.format("%.2f €", item.getTotal()), false);
		table.getContent().add(row);
	}

	private void addCarryOverRow(Tbl table, int fromPage, BigDecimal amount)
	{
		Tr row = factory.createTr();
		addTableCell(row, "", true);
		addTableCell(row, "Übertrag von Seite " + fromPage, true);
		addTableCell(row, "", true);
		addTableCell(row, "", true);
		addTableCell(row, String.format("%.2f €", amount), true);
		table.getContent().add(row);
	}

	private void addSubtotalRow(Tbl table, int page, BigDecimal amount)
	{
		Tr row = factory.createTr();
		addTableCell(row, "", true);
		addTableCell(row, "Zwischensumme Seite " + page, true);
		addTableCell(row, "", true);
		addTableCell(row, "", true);
		addTableCell(row, String.format("%.2f €", amount), true);
		table.getContent().add(row);
	}

	private void addTableCell(Tr row, String text, boolean bold)
	{
		Tc cell = factory.createTc();
		P p = factory.createP();

		if (bold)
		{
			addBoldText(p, text, 10);
		}
		else
		{
			addText(p, text);
		}

		cell.getContent().add(p);
		row.getContent().add(cell);
	}

	private void addTotal(MainDocumentPart mainDocumentPart, InvoiceData invoiceData)
	{
		P totalP = factory.createP();
		addBoldText(totalP, String.format("GESAMTSUMME: %.2f €", invoiceData.getTotalAmount()), 14);
		mainDocumentPart.addObject(totalP);
	}

	private void addPageBreak(MainDocumentPart mainDocumentPart)
	{
		P p = factory.createP();
		Br br = factory.createBr();
		br.setType(STBrType.PAGE);
		p.getContent().add(br);
		mainDocumentPart.addObject(p);
	}

	private void addSpacer(MainDocumentPart mainDocumentPart)
	{
		mainDocumentPart.addObject(factory.createP());
	}

	private void addText(P paragraph, String text)
	{
		R run = factory.createR();
		Text t = factory.createText();
		t.setValue(text);
		run.getContent().add(t);
		paragraph.getContent().add(run);
	}

	private void addBoldText(P paragraph, String text, int fontSize)
	{
		R run = factory.createR();
		RPr rPr = factory.createRPr();
		BooleanDefaultTrue bold = factory.createBooleanDefaultTrue();
		rPr.setB(bold);

		if (fontSize > 0)
		{
			HpsMeasure size = factory.createHpsMeasure();
			size.setVal(BigInteger.valueOf(fontSize * 2)); // Halbe Punkte
			rPr.setSz(size);
		}

		run.setRPr(rPr);
		Text t = factory.createText();
		t.setValue(text);
		run.getContent().add(t);
		paragraph.getContent().add(run);
	}
}

