package com.smartcampus.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

final class PdfResponses {
    private PdfResponses() { }

    static ResponseEntity<byte[]> inline(byte[] pdf, String bookingCode) {
        String fileName = "booking-decision-" + (bookingCode == null ? "letter" : bookingCode) + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(fileName).build().toString())
                .body(pdf);
    }
}
