package com.example.pev.ui;

import com.example.pev.service.PdfService;
import com.example.pev.service.PevService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

@Route(value = "chat", layout = MainLayout.class)
@PageTitle("AI Advisor | PEV2 AI")
@JsModule("./scripts/copy-code.js")
public class ChatView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(ChatView.class);
    private final PevService pevService;
    private final PdfService pdfService;

    private final Div outputContainer = new Div();
    private final ProgressBar progressBar = new ProgressBar();
    private final Span statusLabel = new Span();
    private final Anchor downloadPdfLink = new Anchor();
    private final VerticalLayout resultsCard = new VerticalLayout();

    public ChatView(PevService pevService, PdfService pdfService) {
        this.pevService = pevService;
        this.pdfService = pdfService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName(LumoUtility.Background.CONTRAST_5);

        H2 title = new H2("PostgreSQL AI Optimization Dashboard");
        title.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.TextColor.PRIMARY);

        // --- Left Panel: Configuration ---
        TextArea schemaArea = new TextArea("Database Schema");
        schemaArea.setPlaceholder("Paste DDL here...");
        schemaArea.setWidthFull();
        schemaArea.setHeight("40%"); // Fixed relative height
        schemaArea.setTooltipText("Provide the table definitions (CREATE TABLE...)");

        TextArea queryArea = new TextArea("Target SQL Query");
        queryArea.setPlaceholder("Paste SQL query here...");
        queryArea.setWidthFull();
        queryArea.setHeight("40%"); // Fixed relative height

        Button analyzeBtn = new Button("Run AI Analysis", VaadinIcon.MAGIC.create());
        analyzeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        analyzeBtn.setWidthFull();

        VerticalLayout configPanel = new VerticalLayout(new H3("Configuration"), schemaArea, queryArea, analyzeBtn);
        configPanel.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM, LumoUtility.Padding.MEDIUM, LumoUtility.BoxShadow.SMALL);
        configPanel.setHeightFull();
        configPanel.setPadding(true);
        configPanel.setSpacing(true);
        
        // Ensure the text areas don't push the button out of view
        configPanel.setFlexGrow(1, schemaArea);
        configPanel.setFlexGrow(1, queryArea);
        configPanel.setFlexGrow(0, analyzeBtn);

        // --- Right Panel: Results ---
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        statusLabel.setText("Ready for analysis");
        statusLabel.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        outputContainer.setSizeFull();
        outputContainer.setId("recommendation-container");
        outputContainer.addClassName("markdown-output");
        outputContainer.getStyle().set("min-height", "400px");
        outputContainer.setText("Submit a query to see recommendations...");

        Button downloadBtn = new Button("Export to PDF Report", VaadinIcon.DOWNLOAD.create());
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        downloadPdfLink.add(downloadBtn);
        downloadPdfLink.setVisible(false);
        downloadPdfLink.getElement().setAttribute("download", true);

        resultsCard.add(new H3("AI Recommendation Report"), statusLabel, progressBar, outputContainer, downloadPdfLink);
        resultsCard.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM, LumoUtility.Padding.LARGE, LumoUtility.BoxShadow.MEDIUM);
        resultsCard.setSizeFull();

        SplitLayout splitLayout = new SplitLayout(configPanel, resultsCard);
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(30);

        add(title, splitLayout);

        UI ui = UI.getCurrent();

        analyzeBtn.addClickListener(e -> {
            String schema = schemaArea.getValue();
            String query = queryArea.getValue();

            if (schema.isBlank() || query.isBlank()) {
                Notification.show("Input required: Schema and Query", 3000, Notification.Position.MIDDLE);
                return;
            }

            // UI Reset
            progressBar.setVisible(true);
            statusLabel.setText("Consulting PostgreSQL Expert AI...");
            outputContainer.getElement().setProperty("innerHTML", "");
            analyzeBtn.setEnabled(false);
            downloadPdfLink.setVisible(false);

            new Thread(() -> {
                try {
                    String recommendation = pevService.getRecommendation(schema, query);
                    
                    // Convert Markdown to HTML
                    Parser parser = Parser.builder().build();
                    Node document = parser.parse(recommendation);
                    HtmlRenderer renderer = HtmlRenderer.builder().build();
                    String htmlOutput = renderer.render(document);

                    // Generate PDF bytes in the background
                    byte[] pdfData = pdfService.generatePdf(schema, query, recommendation);
                    
                    ui.access(() -> {
                        outputContainer.getElement().setProperty("innerHTML", htmlOutput);
                        statusLabel.setText("Analysis Complete");
                        progressBar.setVisible(false);
                        analyzeBtn.setEnabled(true);
                        
                        // Register Resource safely
                        StreamResource resource = new StreamResource("pg_optimization_report.pdf", 
                            () -> new ByteArrayInputStream(pdfData));
                        downloadPdfLink.setHref(resource);
                        downloadPdfLink.setVisible(true);

                        // Trigger Copy buttons JS
                        ui.getPage().executeJs("setTimeout(() => window.addCopyButtons('recommendation-container'), 100)");
                        Notification.show("Analysis successful!", 2000, Notification.Position.TOP_END);
                    });
                } catch (Exception ex) {
                    log.error("Analysis process failed", ex);
                    ui.access(() -> {
                        statusLabel.setText("Analysis failed");
                        progressBar.setVisible(false);
                        analyzeBtn.setEnabled(true);
                        outputContainer.getElement().setProperty("innerHTML", 
                            "<div style='color:var(--lumo-error-color); padding: 1em; border: 1px solid; border-radius: 4px;'>" +
                            "<b>Error:</b> " + ex.getMessage() + "</div>");
                    });
                }
            }).start();
        });
    }
}
