package com.example.studex;

import androidx.annotation.Nullable;

public class MessageData {
    private String id;
    private String timestamp;
    private String message;
    private String author_id;
    private String chat_id;

    private String date;
    private String time;

    public MessageData(String message, String author_id, String chat_id, @Nullable String id, @Nullable String timestamp) {
        this.id = id;
        this.timestamp = timestamp;
        this.message = message;
        this.author_id = author_id;
        this.chat_id = chat_id;

        try {
            this.date = setDate();
            this.time = setTime();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public MessageData(String message, String sender_id, String chat_id) {
        this.message = message;
        this.author_id = sender_id;
        this.chat_id = chat_id;

        try {
            this.date = setDate();
            this.time = setTime();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String setTime() {
        return timestamp.substring(17, 22);
    }

    public String getTime() {
        return time;
    }

    public String setDate() {
        return timestamp.substring(0, 16);
    }
    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }
}
