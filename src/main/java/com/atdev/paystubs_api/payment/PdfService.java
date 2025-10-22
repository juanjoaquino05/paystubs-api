package com.atdev.paystubs_api.payment;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfService {
    public byte[] render(PayrollRecord p, String company, byte[] logoBytes, Map<String, String> labels) throws Exception {
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, baos);
        doc.open();

        // Main table with border
        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);

        // Header with logo and title
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 2});

        // Logo cell
        PdfPCell logoCell;
        if (logoBytes != null) {
            Image logo = Image.getInstance(logoBytes);
            logo.scaleToFit(140, 60);
            logoCell = new PdfPCell(logo);
        } else {
            logoCell = new PdfPCell(new Phrase(company, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24)));
        }
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPadding(10);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(logoCell);

        // Title and employee info cell
        PdfPTable titleInfoTable = new PdfPTable(1);
        titleInfoTable.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell(new Phrase(labels.get("paystub_title") + " " + p.period(),
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPadding(5);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        titleInfoTable.addCell(titleCell);

        PdfPCell nameCell = new PdfPCell(new Phrase(p.fullName(),
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setPadding(5);
        nameCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        titleInfoTable.addCell(nameCell);

        PdfPCell positionCell = new PdfPCell(new Phrase(p.position(),
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        positionCell.setBorder(Rectangle.NO_BORDER);
        positionCell.setPadding(5);
        positionCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        titleInfoTable.addCell(positionCell);

        PdfPCell titleInfoCell = new PdfPCell(titleInfoTable);
        titleInfoCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(titleInfoCell);

        PdfPCell headerCell = new PdfPCell(headerTable);
        headerCell.setBorder(Rectangle.BOTTOM);
        headerCell.setPadding(10);
        mainTable.addCell(headerCell);

        // Content table with salary and discounts
        PdfPTable contentTable = new PdfPTable(4);
        contentTable.setWidthPercentage(100);
        contentTable.setWidths(new float[]{2, 2, 2, 2});

        // Left side - Salary info
        PdfPCell salaryLabelCell = new PdfPCell(new Phrase(labels.get("gross_salary"),
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        salaryLabelCell.setBorder(Rectangle.NO_BORDER);
        salaryLabelCell.setPadding(8);
        contentTable.addCell(salaryLabelCell);

        PdfPCell salaryValueCell = new PdfPCell(new Phrase(money(p.grossSalary()),
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        salaryValueCell.setBorder(Rectangle.NO_BORDER);
        salaryValueCell.setPadding(8);
        contentTable.addCell(salaryValueCell);

        // Right side - Discounts header
        PdfPCell discountsHeaderCell = new PdfPCell(new Phrase("Descuentos",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        discountsHeaderCell.setBorder(Rectangle.NO_BORDER);
        discountsHeaderCell.setPadding(8);
        discountsHeaderCell.setColspan(2);
        discountsHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        contentTable.addCell(discountsHeaderCell);

        // Pago Bruto
        PdfPCell pagoBrutoLabelCell = new PdfPCell(new Phrase(labels.get("gross_payment"),
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        pagoBrutoLabelCell.setBorder(Rectangle.NO_BORDER);
        pagoBrutoLabelCell.setPadding(8);
        contentTable.addCell(pagoBrutoLabelCell);

        PdfPCell pagoBrutoValueCell = new PdfPCell(new Phrase(money(p.grossPayment()),
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        pagoBrutoValueCell.setBorder(Rectangle.NO_BORDER);
        pagoBrutoValueCell.setPadding(8);
        contentTable.addCell(pagoBrutoValueCell);

        // SFS
        PdfPCell sfsLabelCell = new PdfPCell(new Phrase("SFS",
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        sfsLabelCell.setBorder(Rectangle.NO_BORDER);
        sfsLabelCell.setPadding(8);
        contentTable.addCell(sfsLabelCell);

        PdfPCell sfsValueCell = new PdfPCell(new Phrase(money(p.socialDiscountAmount()),
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        sfsValueCell.setBorder(Rectangle.NO_BORDER);
        sfsValueCell.setPadding(8);
        contentTable.addCell(sfsValueCell);

        // Empty row
        PdfPCell emptyCell1 = new PdfPCell(new Phrase(""));
        emptyCell1.setBorder(Rectangle.NO_BORDER);
        emptyCell1.setPadding(8);
        contentTable.addCell(emptyCell1);

        PdfPCell emptyCell2 = new PdfPCell(new Phrase(""));
        emptyCell2.setBorder(Rectangle.NO_BORDER);
        emptyCell2.setPadding(8);
        contentTable.addCell(emptyCell2);

        // AFP
        PdfPCell afpLabelCell = new PdfPCell(new Phrase("AFP",
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        afpLabelCell.setBorder(Rectangle.NO_BORDER);
        afpLabelCell.setPadding(8);
        contentTable.addCell(afpLabelCell);

        PdfPCell afpValueCell = new PdfPCell(new Phrase(money(p.healthDiscountAmount()),
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        afpValueCell.setBorder(Rectangle.NO_BORDER);
        afpValueCell.setPadding(8);
        contentTable.addCell(afpValueCell);

        // Empty row
        PdfPCell emptyCell3 = new PdfPCell(new Phrase(""));
        emptyCell3.setBorder(Rectangle.NO_BORDER);
        emptyCell3.setPadding(8);
        contentTable.addCell(emptyCell3);

        PdfPCell emptyCell4 = new PdfPCell(new Phrase(""));
        emptyCell4.setBorder(Rectangle.NO_BORDER);
        emptyCell4.setPadding(8);
        contentTable.addCell(emptyCell4);

        // ISR
        PdfPCell isrLabelCell = new PdfPCell(new Phrase("ISR",
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        isrLabelCell.setBorder(Rectangle.NO_BORDER);
        isrLabelCell.setPadding(8);
        contentTable.addCell(isrLabelCell);

        PdfPCell isrValueCell = new PdfPCell(new Phrase(money(p.taxesDiscountAmount()),
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        isrValueCell.setBorder(Rectangle.NO_BORDER);
        isrValueCell.setPadding(8);
        contentTable.addCell(isrValueCell);

        // Empty row
        PdfPCell emptyCell5 = new PdfPCell(new Phrase(""));
        emptyCell5.setBorder(Rectangle.NO_BORDER);
        emptyCell5.setPadding(8);
        contentTable.addCell(emptyCell5);

        PdfPCell emptyCell6 = new PdfPCell(new Phrase(""));
        emptyCell6.setBorder(Rectangle.NO_BORDER);
        emptyCell6.setPadding(8);
        contentTable.addCell(emptyCell6);

        // Otros
        PdfPCell otrosLabelCell = new PdfPCell(new Phrase("Otros",
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        otrosLabelCell.setBorder(Rectangle.NO_BORDER);
        otrosLabelCell.setPadding(8);
        contentTable.addCell(otrosLabelCell);

        PdfPCell otrosValueCell = new PdfPCell(new Phrase(money(p.otherDiscountAmount()),
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        otrosValueCell.setBorder(Rectangle.NO_BORDER);
        otrosValueCell.setPadding(8);
        contentTable.addCell(otrosValueCell);

        // Empty row
        PdfPCell emptyCell7 = new PdfPCell(new Phrase(""));
        emptyCell7.setBorder(Rectangle.NO_BORDER);
        emptyCell7.setPadding(8);
        contentTable.addCell(emptyCell7);

        PdfPCell emptyCell8 = new PdfPCell(new Phrase(""));
        emptyCell8.setBorder(Rectangle.NO_BORDER);
        emptyCell8.setPadding(8);
        contentTable.addCell(emptyCell8);

        // Total
        double totalDiscounts = p.socialDiscountAmount() + p.healthDiscountAmount() +
                                p.taxesDiscountAmount() + p.otherDiscountAmount();

        PdfPCell totalLabelCell = new PdfPCell(new Phrase("Total",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        totalLabelCell.setBorder(Rectangle.NO_BORDER);
        totalLabelCell.setPadding(8);
        contentTable.addCell(totalLabelCell);

        PdfPCell totalValueCell = new PdfPCell(new Phrase(money(totalDiscounts),
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        totalValueCell.setBorder(Rectangle.NO_BORDER);
        totalValueCell.setPadding(8);
        contentTable.addCell(totalValueCell);

        PdfPCell contentCell = new PdfPCell(contentTable);
        contentCell.setBorder(Rectangle.BOTTOM);
        contentCell.setPadding(10);
        mainTable.addCell(contentCell);

        // Footer with net payment
        PdfPTable footerTable = new PdfPTable(2);
        footerTable.setWidthPercentage(100);
        footerTable.setWidths(new float[]{1, 1});

        PdfPCell netLabelCell = new PdfPCell(new Phrase(labels.get("net_payment"),
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        netLabelCell.setBorder(Rectangle.NO_BORDER);
        netLabelCell.setPadding(10);
        footerTable.addCell(netLabelCell);

        PdfPCell netValueCell = new PdfPCell(new Phrase(money(p.netPayment()),
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        netValueCell.setBorder(Rectangle.NO_BORDER);
        netValueCell.setPadding(10);
        footerTable.addCell(netValueCell);

        PdfPCell footerCell = new PdfPCell(footerTable);
        footerCell.setBorder(Rectangle.NO_BORDER);
        footerCell.setPadding(10);
        mainTable.addCell(footerCell);

        doc.add(mainTable);
        doc.close();
        return baos.toByteArray();
    }

    private PdfPCell cell(String t) {
        PdfPCell c = new PdfPCell(new Phrase(t));
        c.setPadding(6);
        return c;
    }

    private PdfPCell cellBold(String t) {
        PdfPCell c = new PdfPCell(new Phrase(t, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        c.setPadding(6);
        return c;
    }

    private String money(double v) {
        return String.format("$%,.2f", v);
    }
}
