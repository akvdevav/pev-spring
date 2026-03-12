package com.example.pev.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.page.WebStorage;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Postgres Explain Visualizer - AI Assistant");
        logo.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.MEDIUM);

        Button themeToggle = new Button(VaadinIcon.ADJUST.create(), click -> {
            var element = getElement().executeJs("return document.documentElement.getAttribute('theme')");
            element.then(String.class, theme -> {
                boolean isDark = Lumo.DARK.equals(theme);
                String newTheme = isDark ? "" : Lumo.DARK;
                getElement().executeJs("document.documentElement.setAttribute('theme', $0)", newTheme);
            });
        });
        themeToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        themeToggle.addClassName(LumoUtility.Margin.Left.AUTO);
        themeToggle.addClassName(LumoUtility.Margin.Right.MEDIUM);
        themeToggle.setTooltipText("Toggle Dark/Light mode");

        addToNavbar(new DrawerToggle(), logo, themeToggle);
    }


    private void createDrawer() {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Plan Visualizer", PlanView.class, VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("AI Query Advisor", ChatView.class, VaadinIcon.MAGIC.create()));

        addToDrawer(nav);
    }
}
