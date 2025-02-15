package com.example.studex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENDER = 1;
    private static final int VIEW_TYPE_RECEIVER = 2;

    private List<MessageData> messages;
    private Context context;
    private UserData otherUser;

    public ChatMessagesAdapter(List<MessageData> messages, Context context, UserData otherUser) {
        this.messages = messages;
        this.context = context;
        this.otherUser = otherUser;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_SENDER) {
            View senderView = inflater.inflate(R.layout.item_message_sender, parent, false);
            return new MessageViewHolder(senderView);
        } else if (viewType == VIEW_TYPE_RECEIVER) {
            View receiverView = inflater.inflate(R.layout.item_message_receiver, parent, false);
            return new MessageViewHolder2(receiverView);
        }

        throw new IllegalArgumentException("Invalid view type: " + viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageData message = messages.get(position);
        String messageDate = message.getTimestamp().substring(0, 16);
        String messageTime = message.getTimestamp().substring(17, 22);

        if (holder instanceof MessageViewHolder) {
            MessageViewHolder senderViewHolder = (MessageViewHolder) holder;
            senderViewHolder.messageTextView.setText(message.getMessage());
            senderViewHolder.timeTextView.setText(messageTime);
            senderViewHolder.dateTextView.setText(messageDate);

            if (position > 0 && !messageDate.equals(messages.get(position - 1).getTimestamp().substring(0, 16))) {
                holder.itemView.findViewById(R.id.chat_your_message_date).setVisibility(View.VISIBLE);
            } else if (position == 0) {
                holder.itemView.findViewById(R.id.chat_your_message_date).setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.chat_your_message_date).setVisibility(View.GONE);
            }

        } else if (holder instanceof MessageViewHolder2) {
            MessageViewHolder2 receiverViewHolder = (MessageViewHolder2) holder;
            receiverViewHolder.messageTextView.setText(message.getMessage());
            receiverViewHolder.timeTextView.setText(messageTime);
            receiverViewHolder.dateTextView.setText(messageDate);
            String authorText = otherUser.getUsername().substring(0, 1).toUpperCase()
                    + otherUser.getUsername().substring(1);
            receiverViewHolder.authorTextView.setText(authorText);
//            receiverViewHolder.authorProfileImage.setImageBitmap(otherUser.());

            if (position > 0 && !messageDate.equals(messages.get(position - 1).getTimestamp().substring(0, 16))) {
                holder.itemView.findViewById(R.id.chat_message_date).setVisibility(View.VISIBLE);
            } else if (position == 0) {
                holder.itemView.findViewById(R.id.chat_message_date).setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.chat_message_date).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        MessageData message = messages.get(position);

        if (message.getAuthor_id().equals(Authentication.getId())) {
            return VIEW_TYPE_SENDER;
        } else {
            return VIEW_TYPE_RECEIVER;
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView timeTextView;
        public TextView dateTextView;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.chat_your_message);
            timeTextView = itemView.findViewById(R.id.chat_your_message_timestamp);
            dateTextView = itemView.findViewById(R.id.chat_your_message_date);
        }
    }

    public static class MessageViewHolder2 extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView timeTextView;
        public TextView dateTextView;
        public TextView authorTextView;

        public MessageViewHolder2(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.chat_message_text);
            timeTextView = itemView.findViewById(R.id.chat_message_timestamp);
            dateTextView = itemView.findViewById(R.id.chat_message_date);
            authorTextView = itemView.findViewById(R.id.chat_author_name);
        }
    }
}
