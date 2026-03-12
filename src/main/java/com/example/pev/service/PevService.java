package com.example.pev.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PevService {

    private static final Logger log = LoggerFactory.getLogger(PevService.class);
    private final ChatClient chatClient;

    public PevService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String getRecommendation(String schema, String query) {
        log.info("Sending request to Ollama for query optimization analysis...");
        String template = """
                You are a PostgreSQL performance expert.
                Given the following database schema and SQL query, suggest indexes, rewriting strategies, or configuration changes to optimize performance.
                
                Schema:
                {schema}
                
                Query:
                {query}
                
                Provide a detailed analysis and specific SQL commands for creating indexes if necessary.
                """;
        
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of("schema", schema, "query", query));
        
        try {
            String content = chatClient.call(prompt).getResult().getOutput().getContent();
            log.info("Received response from Ollama successfully.");
            return content;
        } catch (Exception e) {
            log.error("Error while calling Ollama: {}", e.getMessage(), e);
            throw e;
        }
    }
}
