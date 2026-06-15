package com.myproject.inventorymanagement.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.myproject.inventorymanagement.entity.*;
import com.myproject.inventorymanagement.repository.InvoiceItemRepository;
import com.myproject.inventorymanagement.repository.InvoiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAllByOrderByCreatedAtDesc();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }

    @Transactional
    public Invoice createInvoiceFromRequest(StockRequest request, User manager) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNo("INV-" + String.format("%05d", request.getId()) + "-" + System.currentTimeMillis() / 1000);
        invoice.setRequest(request);
        invoice.setType(
                request.getType() == StockRequest.RequestType.IMPORT ? Invoice.Type.IMPORT : Invoice.Type.EXPORT);
        invoice.setUser(manager);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        for (StockRequestItem requestItem : request.getItems()) {
            InvoiceItem invoiceItem = new InvoiceItem();
            invoiceItem.setInvoice(savedInvoice);
            invoiceItem.setProduct(requestItem.getProduct());
            invoiceItem.setQuantity(requestItem.getQuantity());

            invoiceItemRepository.save(invoiceItem);
            savedInvoice.getItems().add(invoiceItem);
        }

        return savedInvoice;
    }

    public byte[] exportToExcel(Long id) throws IOException {
        Invoice invoice = getInvoiceById(id);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Hóa đơn");

            // Styles
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(boldFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);

            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.cloneStyleFrom(borderStyle);
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Title
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("HÓA ĐƠN " + (invoice.getType() == Invoice.Type.IMPORT ? "NHẬP HÀNG" : "XUẤT HÀNG"));
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));

            // Info
            int r = 2;
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue("Số hóa đơn:");
            row.createCell(1).setCellValue(invoice.getInvoiceNo());

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("Mã yêu cầu kho:");
            row.createCell(1).setCellValue(invoice.getRequest().getId());

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("Loại hóa đơn:");
            row.createCell(1)
                    .setCellValue(invoice.getType() == Invoice.Type.IMPORT ? "Nhập kho (IMPORT)" : "Xuất kho (EXPORT)");

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("Người thực hiện:");
            row.createCell(1).setCellValue(invoice.getUser() != null ? invoice.getUser().getUsername() : "Hệ thống");

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("Ngày lập:");
            row.createCell(1)
                    .setCellValue(invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

            r++;

            // Headers
            Row headerRow = sheet.createRow(r++);
            String[] columns = { "STT", "Mã SKU", "Tên sản phẩm", "Số lượng" };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            int stt = 1;
            for (InvoiceItem item : invoice.getItems()) {
                Row dataRow = sheet.createRow(r++);

                Cell c1 = dataRow.createCell(0);
                c1.setCellValue(stt++);
                c1.setCellStyle(centerStyle);

                Cell c2 = dataRow.createCell(1);
                c2.setCellValue(item.getProduct().getSku());
                c2.setCellStyle(borderStyle);

                Cell c3 = dataRow.createCell(2);
                c3.setCellValue(item.getProduct().getName());
                c3.setCellStyle(borderStyle);

                Cell c4 = dataRow.createCell(3);
                c4.setCellValue(item.getQuantity());
                c4.setCellStyle(centerStyle);
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    public byte[] exportToPdf(Long id) {
        Invoice invoice = getInvoiceById(id);
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Set up font for Vietnamese characters
            String[] fontPaths = {
                    "C:\\Windows\\Fonts\\arial.ttf",
                    "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                    "/usr/share/fonts/dejavu/DejaVuSans.ttf"
            };
            BaseFont bf = null;
            for (String path : fontPaths) {
                try {
                    java.io.File file = new java.io.File(path);
                    if (file.exists()) {
                        bf = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                        break;
                    }
                } catch (Exception e) {
                }
            }
            if (bf == null) {
                bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            }

            Font titleFont = new Font(bf, 16, Font.BOLD);
            Font boldFont = new Font(bf, 11, Font.BOLD);
            Font normalFont = new Font(bf, 11, Font.NORMAL);

            // Title
            Paragraph title = new Paragraph(
                    "HÓA ĐƠN " + (invoice.getType() == Invoice.Type.IMPORT ? "NHẬP HÀNG" : "XUẤT HÀNG"), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Info
            document.add(new Paragraph("Số hóa đơn: " + invoice.getInvoiceNo(), normalFont));
            document.add(new Paragraph("Mã yêu cầu kho: " + invoice.getRequest().getId(), normalFont));
            document.add(new Paragraph(
                    "Loại hóa đơn: "
                            + (invoice.getType() == Invoice.Type.IMPORT ? "Nhập kho (IMPORT)" : "Xuất kho (EXPORT)"),
                    normalFont));
            document.add(new Paragraph(
                    "Người thực hiện: " + (invoice.getUser() != null ? invoice.getUser().getUsername() : "Hệ thống"),
                    normalFont));
            document.add(new Paragraph(
                    "Ngày lập: " + invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                    normalFont));

            Paragraph spacing = new Paragraph(" ");
            spacing.setSpacingAfter(15);
            document.add(spacing);

            // Items
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 1f, 2f, 5f, 2f });

            // Headers
            PdfPCell cell1 = new PdfPCell(new Paragraph("STT", boldFont));
            PdfPCell cell2 = new PdfPCell(new Paragraph("Mã SKU", boldFont));
            PdfPCell cell3 = new PdfPCell(new Paragraph("Tên sản phẩm", boldFont));
            PdfPCell cell4 = new PdfPCell(new Paragraph("Số lượng", boldFont));

            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell4.setHorizontalAlignment(Element.ALIGN_CENTER);

            // Style headers
            java.awt.Color headerColor = new java.awt.Color(220, 220, 220);
            cell1.setBackgroundColor(headerColor);
            cell2.setBackgroundColor(headerColor);
            cell3.setBackgroundColor(headerColor);
            cell4.setBackgroundColor(headerColor);

            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);

            int stt = 1;
            for (InvoiceItem item : invoice.getItems()) {
                PdfPCell c1 = new PdfPCell(new Paragraph(String.valueOf(stt++), normalFont));
                PdfPCell c2 = new PdfPCell(new Paragraph(item.getProduct().getSku(), normalFont));
                PdfPCell c3 = new PdfPCell(new Paragraph(item.getProduct().getName(), normalFont));
                PdfPCell c4 = new PdfPCell(new Paragraph(String.valueOf(item.getQuantity()), normalFont));

                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                c4.setHorizontalAlignment(Element.ALIGN_CENTER);

                table.addCell(c1);
                table.addCell(c2);
                table.addCell(c3);
                table.addCell(c4);
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while generating PDF: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }
}
