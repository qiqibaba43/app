package com.example.contactsapp_experimentalweek;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LettersAdapter extends RecyclerView.Adapter<LettersAdapter.LettersViewHolder> {
    private static List<String> letters;
    private LayoutInflater CInflater;
    private Context context;
    private static OnItemClickListener onItemClickListener;
    public LettersAdapter(Context context) {
        CInflater = LayoutInflater.from(context);
        this.context = context;
        letters=new ArrayList<>();
    }
    public void setLetters(List<String> letters) {
        this.letters = letters;
    }
    public interface OnItemClickListener {
        void onItemClick(String letter);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    @NonNull
    @Override
    public LettersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_letters, parent, false);
        return new LettersViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull LettersViewHolder holder, int position) {
        holder.textViewLetter.setText(letters.get(position));
    }
    @Override
    public int getItemCount() {
        return letters.size();
    }
    public static class LettersViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewLetter;
        public LettersViewHolder(View itemView) {
            super(itemView);
            textViewLetter = itemView.findViewById(R.id.textViewLetter);
            // 设置点击事件监听器
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(letters.get(getAdapterPosition()));
                    }
                }
            });
        }
    }
}
