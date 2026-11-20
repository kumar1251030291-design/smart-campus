package com.smartcampus.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smartcampus.enums.BookingStatus;
import com.smartcampus.model.BookingHistoryEntry;
import com.smartcampus.model.BookingRequest;
import com.smartcampus.model.BookingRequestItem;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Builds the booking decision letter that students, club representatives and administrators can download.
 * The letter is generated from the same database rows the web pages show, so every view stays consistent.
 */
@Service
public class BookingDecisionPdfService {
    private static final Color SAGE = new Color(0x65, 0x92, 0x87);
    private static final Color INK = new Color(0x25, 0x3A, 0x35);
    private static final Color PALE = new Color(0xE6, 0xF2, 0xDD);
    private static final Color MUTED = new Color(0x6D, 0x7E, 0x74);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("dd MMM uuuu, hh:mm a", Locale.ENGLISH);

    private final Font title = new Font(Font.HELVETICA, 20, Font.BOLD, INK);
    private final Font heading = new Font(Font.HELVETICA, 12, Font.BOLD, SAGE);
    private final Font label = new Font(Font.HELVETICA, 9, Font.BOLD, MUTED);
    private final Font body = new Font(Font.HELVETICA, 10, Font.NORMAL, INK);
    private final Font bold = new Font(Font.HELVETICA, 10, Font.BOLD, INK);
    private final Font small = new Font(Font.HELVETICA, 8, Font.NORMAL, MUTED);

    public byte[] decisionLetter(BookingRequest request, List<BookingRequestItem> items, List<BookingHistoryEntry> history,
                                 String requesterEmail) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 44, 44, 36, 36);
        PdfWriter.getInstance(document, out);
        document.open();

        Paragraph brand = new Paragraph("SMART CAMPUS  ·  RESOURCE BOOKING OFFICE", new Font(Font.HELVETICA, 9, Font.BOLD, SAGE));
        document.add(brand);
        Paragraph heading1 = new Paragraph("Booking decision letter", title);
        heading1.setSpacingBefore(6);
        document.add(heading1);
        document.add(new Paragraph("Reference " + request.bookingCode() + "   ·   Generated "
                + LocalDateTime.now().format(STAMP), small));

        document.add(statusBanner(request));

        section(document, "Request");
        PdfPTable facts = new PdfPTable(new float[]{1.3f, 3f});
        facts.setWidthPercentage(100);
        fact(facts, "Event", request.eventTitle());
        fact(facts, "Event type", request.eventType().getDisplayName());
        fact(facts, "Requested by", request.requesterName() + " (" + request.requesterRole().getDisplayName() + ")");
        if (requesterEmail != null) fact(facts, "Email", requesterEmail);
        fact(facts, "Department", request.department());
        if (request.clubName() != null) fact(facts, "Club", request.clubName());
        fact(facts, "Date", request.eventDate().format(DATE));
        fact(facts, "Time", time(request.startTime()) + " – " + time(request.endTime()));
        fact(facts, "Participants", String.valueOf(request.expectedParticipants()));
        fact(facts, "Purpose", request.purpose());
        fact(facts, "Submitted", request.createdAt().format(STAMP));
        document.add(facts);

        section(document, "Resources");
        PdfPTable resources = new PdfPTable(new float[]{3f, 1.4f, 1f});
        resources.setWidthPercentage(100);
        headerCell(resources, "Resource"); headerCell(resources, "Category"); headerCell(resources, "Quantity");
        for (BookingRequestItem item : items) {
            resources.addCell(cell(item.resourceName(), body));
            resources.addCell(cell(item.category().getDisplayName(), body));
            resources.addCell(cell(String.valueOf(item.requestedQuantity()), body));
        }
        if (items.isEmpty()) {
            PdfPCell none = cell("No resources attached.", body);
            none.setColspan(3);
            resources.addCell(none);
        }
        document.add(resources);

        section(document, "Administrator decision");
        document.add(new Paragraph(reasonLabel(request.status()), bold));
        Paragraph reason = new Paragraph(request.decisionNote() == null || request.decisionNote().isBlank()
                ? defaultReason(request.status()) : request.decisionNote(), body);
        reason.setSpacingAfter(6);
        document.add(reason);
        if (request.alternativeDate() != null && request.alternativeStartTime() != null && request.alternativeEndTime() != null) {
            Paragraph alternative = new Paragraph();
            alternative.add(new Chunk("Suggested alternative: ", bold));
            alternative.add(new Chunk(request.alternativeDate().format(DATE) + ", " + time(request.alternativeStartTime())
                    + " – " + time(request.alternativeEndTime()), body));
            document.add(alternative);
            document.add(new Paragraph("Open the booking in Smart Campus and choose \"Accept proposed time\" to take this slot.", small));
        }

        section(document, "Status history");
        PdfPTable timeline = new PdfPTable(new float[]{1.6f, 1.5f, 3f});
        timeline.setWidthPercentage(100);
        headerCell(timeline, "When"); headerCell(timeline, "Status"); headerCell(timeline, "Note");
        for (BookingHistoryEntry entry : history) {
            timeline.addCell(cell(entry.changedAt().format(STAMP), small));
            timeline.addCell(cell(entry.statusAfter().getDisplayName() + "\n" + entry.responsibleName(), body));
            timeline.addCell(cell(entry.note() == null ? "—" : entry.note(), body));
        }
        document.add(timeline);

        Paragraph footer = new Paragraph("This letter reflects the booking record held in Smart Campus at the time it was generated. "
                + "Contact the campus resource office if any detail looks incorrect.", small);
        footer.setSpacingBefore(12);
        document.add(footer);
        document.close();
        return out.toByteArray();
    }

    private PdfPTable statusBanner(BookingRequest request) {
        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);
        banner.setSpacingBefore(10);
        PdfPCell cell = new PdfPCell(new Phrase("Status: " + request.status().getDisplayName(),
                new Font(Font.HELVETICA, 13, Font.BOLD, statusColor(request.status()))));
        cell.setBackgroundColor(PALE);
        cell.setBorderColor(PALE);
        cell.setPadding(10);
        banner.addCell(cell);
        return banner;
    }

    private void section(Document document, String text) {
        Paragraph paragraph = new Paragraph(text, heading);
        paragraph.setSpacingBefore(12);
        paragraph.setSpacingAfter(6);
        document.add(paragraph);
    }

    private void fact(PdfPTable table, String name, String value) {
        table.addCell(cell(name, label));
        table.addCell(cell(value == null ? "—" : value, body));
    }

    private void headerCell(PdfPTable table, String text) {
        PdfPCell cell = cell(text, new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE));
        cell.setBackgroundColor(SAGE);
        table.addCell(cell);
    }

    private PdfPCell cell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4.5f);
        cell.setBorderColor(new Color(0xE2, 0xEB, 0xE1));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private String time(LocalTime value) { return value.format(TIME); }

    private Color statusColor(BookingStatus status) {
        return switch (status) {
            case APPROVED, COMPLETED -> new Color(0x2E, 0x7D, 0x4F);
            case REJECTED, CANCELLED -> new Color(0xB4, 0x3C, 0x3C);
            case ALTERNATIVE_PROPOSED, ADDITIONAL_INFORMATION_REQUIRED -> new Color(0xA8, 0x6A, 0x12);
            default -> INK;
        };
    }

    private String reasonLabel(BookingStatus status) {
        return switch (status) {
            case APPROVED -> "Reason for approval";
            case REJECTED -> "Reason for rejection";
            case ALTERNATIVE_PROPOSED -> "Why an alternative was suggested";
            case ADDITIONAL_INFORMATION_REQUIRED -> "Information requested";
            case CANCELLED -> "Cancellation";
            case COMPLETED -> "Completion note";
            default -> "Review note";
        };
    }

    private String defaultReason(BookingStatus status) {
        return switch (status) {
            case PENDING -> "The request is waiting for administrator review.";
            case UNDER_REVIEW -> "An administrator is reviewing resource availability for this request.";
            case APPROVED -> "The requested resources are available and the request meets campus booking guidelines.";
            case CANCELLED -> "This request was cancelled.";
            default -> "No additional note was recorded.";
        };
    }
}
