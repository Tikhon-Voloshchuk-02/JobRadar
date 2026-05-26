package com.jobradar.application.dto.ai;

import java.util.List;

public class OpenAiChatRequest {

    private String model;
    private List<Message> messages;
    private double temperature;

    public OpenAiChatRequest(String model,
                             List<Message> messages,
                             double temperature) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
    }

    public String getModel() { return model; }

    public List<Message> getMessages() { return messages; }

    public double getTemperature() { return temperature; }

    public static class Message {

        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }

        public String getContent() { return content; }
    }
}
