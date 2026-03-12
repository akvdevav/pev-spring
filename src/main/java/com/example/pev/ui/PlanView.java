package com.example.pev.ui;

import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Plan Visualizer | PEV2 AI")
public class PlanView extends VerticalLayout {

    public PlanView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        IFrame iframe = new IFrame("/PEV2/index.html");
        iframe.setSizeFull();
        iframe.getElement().setAttribute("frameborder", "0");
        
        add(iframe);
    }
}
