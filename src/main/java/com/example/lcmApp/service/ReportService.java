package com.example.lcmApp.service;

import com.example.lcmApp.dto.MaterialDto;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReportService {

    public byte[] generateMaterialReport(List<MaterialDto> materials) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Используем стандартные шрифты PDF — они всегда есть и поддерживают кириллицу
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);

            document.add(new Paragraph("Отчёт: заказываемые материалы", titleFont));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.getDefaultCell().setBorder(PdfPCell.BOX);

            // Заголовки БЕЗ цвета фона — это убирает проблему с BaseColor
            PdfPCell c1 = new PdfPCell(new Phrase("Материал", headerFont));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);

            PdfPCell c2 = new PdfPCell(new Phrase("Ед. изм.", headerFont));
            c2.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c2);

            PdfPCell c3 = new PdfPCell(new Phrase("Количество", headerFont));
            c3.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c3);

            for (MaterialDto m : materials) {
                table.addCell(new Phrase(m.getName(), normalFont));
                table.addCell(new Phrase(m.getUnit(), normalFont));
                table.addCell(new Phrase(String.valueOf(m.getVolume()), normalFont));
            }

            document.add(table);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return baos.toByteArray();
    }
}
