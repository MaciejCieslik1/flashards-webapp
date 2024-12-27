package com.PAP_team_21.flashcards.pdfGenerator;

import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import java.io.ByteArrayOutputStream;

public class PdfGenerator {
    public byte[] generatePdfFromDeck(Deck deck) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        document.add(new Paragraph("Deck: " + deck.getName()).setBold().setFontSize(18));

        Table table = new Table(2); // 2 columns
        table.addHeaderCell(new Cell().add(new Paragraph("Word").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Definition").setBold()));

        for (Flashcard flashcard : deck.getFlashcards()) {
            String word = flashcard.getFront();
            String definition = flashcard.getBack();
            table.addCell(word.trim());
            table.addCell(definition.trim());
        }

        document.add(table);
        document.close();

        return byteArrayOutputStream.toByteArray();
    }
}
