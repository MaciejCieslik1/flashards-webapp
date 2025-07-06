package com.PAP_team_21.flashcards.entities;

import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class PdfGenerator {
    public byte[] generatePdfFromDeck(Deck deck) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        // Set document margins
        document.setMargins(20, 20, 20, 20);

        // Add title with styling
        Paragraph title = new Paragraph("Deck: " + deck.getName())
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // Create a table with 2 columns
        Table table = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();

        // Add header cells with styling
        Cell wordHeader = new Cell().add(new Paragraph("Word"))
                .setBackgroundColor(new DeviceRgb(0, 102, 204)) // Blue background
                .setFontColor(DeviceGray.WHITE)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(10);
        Cell definitionHeader = new Cell().add(new Paragraph("Definition"))
                .setBackgroundColor(new DeviceRgb(0, 102, 204)) // Blue background
                .setFontColor(DeviceGray.WHITE)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(10);
        table.addHeaderCell(wordHeader);
        table.addHeaderCell(definitionHeader);

        // Add flashcards to the table
        for (Flashcard flashcard : deck.getFlashcards()) {
            String word = flashcard.getFront();
            String definition = flashcard.getBack();

            Cell wordCell = new Cell().add(new Paragraph(word.trim()))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setPadding(5);
            Cell definitionCell = new Cell().add(new Paragraph(definition.trim()))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setPadding(5);

            table.addCell(wordCell);
            table.addCell(definitionCell);
        }

        // Add table to the document
        document.add(table);

        // Close the document
        document.close();

        return byteArrayOutputStream.toByteArray();
    }
}
