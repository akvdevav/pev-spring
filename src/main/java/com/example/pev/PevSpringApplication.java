package com.example.pev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@SpringBootApplication
@Theme(value = "pev-theme", variant = Lumo.DARK)
@Push
public class PevSpringApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(PevSpringApplication.class, args);
	}

}
