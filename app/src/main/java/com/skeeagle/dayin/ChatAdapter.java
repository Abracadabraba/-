package com.skeeagle.dayin;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
    private final List<ChatMessage> items;

    public ChatAdapter(List<ChatMessage> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ChatMessage m = items.get(position);
        holder.container.setGravity(m.isLeft ? Gravity.START : Gravity.END);
        holder.bubble.setBackgroundResource(m.isLeft ? R.drawable.bg_bubble_left : R.drawable.bg_bubble_right);
        holder.original.setText(m.originalText + "  [" + m.originalLangName + "]");
        holder.translated.setText(m.translatedText + "  [" + m.translatedLangName + "]");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        LinearLayout container;
        LinearLayout bubble;
        TextView original;
        TextView translated;

        VH(View v) {
            super(v);
            container = v.findViewById(R.id.messageContainer);
            bubble = v.findViewById(R.id.bubble);
            original = v.findViewById(R.id.originalText);
            translated = v.findViewById(R.id.translatedText);
        }
    }
}
