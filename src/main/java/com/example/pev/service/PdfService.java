package com.example.pev.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

@Service
public class PdfService {

    public byte[] generatePdf(String schema, String query, String markdown) {
        // Convert Markdown to HTML
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String htmlBody = renderer.render(document);

        // Build HTML for PDF
        String fullHtml = "<html><head><style>" +
                "body { font-family: sans-serif; padding: 20px; line-height: 1.6; color: #333; }" +
                "section { margin-bottom: 30px; border: 1px solid #eee; padding: 15px; border-radius: 8px; }" +
                "h1 { color: #2c3e50; text-align: center; border-bottom: 2px solid #2c3e50; padding-bottom: 10px; }" +
                "h2 { color: #2980b9; border-bottom: 1px solid #eee; padding-bottom: 5px; margin-top: 0; }" +
                "pre { background: #f8f9fa; padding: 10px; border: 1px solid #e9ecef; border-left: 5px solid #2980b9; white-space: pre-wrap; font-family: monospace; font-size: 0.9em; }" +
                "code { background: #f8f9fa; padding: 2px 4px; border-radius: 3px; font-family: monospace; }" +
                ".label { font-weight: bold; color: #7f8c8d; margin-bottom: 5px; display: block; }" +
                "</style></head><body>" +
                "<h1>PostgreSQL Optimization Report</h1>" +
                
                "<section>" +
                "<h2>1. Input Schema</h2>" +
                "<pre>" + escapeXml(schema) + "</pre>" +
                "</section>" +

                "<section>" +
                "<h2>2. Target Query</h2>" +
                "<pre>" + escapeXml(query) + "</pre>" +
                "</section>" +

                "<section>" +
                "<h2>3. AI Recommendations</h2>" +
                htmlBody +
                "</section>" +
                "</body></html>";

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(fullHtml, "/");
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }
}
