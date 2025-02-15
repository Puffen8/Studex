package com.example.studex;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

public class ChatData implements Serializable {
    private String id;
    private String listing_id;
    private String buyer_id;
    private String seller_id;

    private List<MessageData> messages;


    public ChatData(@Nullable String id, String listing_id, String buyer_id, String seller_id, List<MessageData> messages) {
        this.id = id;
        this.listing_id = listing_id;
        this.buyer_id = buyer_id;
        this.seller_id = seller_id;
        this.messages = messages;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getListing_id() {
        return listing_id;
    }

    public void setListing_id(String listing_id) {
        this.listing_id = listing_id;
    }

    public String getBuyer_id() {
        return buyer_id;
    }

    public void setBuyer_id(String buyer_id) {
        this.buyer_id = buyer_id;
    }

    public String getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
    }

    public List<MessageData> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageData> messages) {
        this.messages = messages;
    }
}
