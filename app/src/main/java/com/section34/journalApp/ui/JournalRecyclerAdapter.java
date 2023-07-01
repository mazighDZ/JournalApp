package com.section34.journalApp.ui;


import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.section34.journalApp.R;
import com.section34.journalApp.model.Journal;

import java.util.List;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.MyViewHolder> {
private Context context;

private List<Journal> journalList;

    public JournalRecyclerAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
     View view = LayoutInflater.from(context).inflate(R.layout.journal_row, parent , false);

        return new MyViewHolder(view , context);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Journal journal = journalList.get(position);
        String imageUrl ;

        holder.title.setText(journal.getTitle());
        holder.descreption.setText(journal.getDescription());
        holder.name.setText(journal.getUserName());
         imageUrl = journal.getImageUri();
         // showing timeAgo
         String timeAgo = (String) DateUtils.getRelativeTimeSpanString(
                 journal.getTimeAdded().getSeconds()*1000);

         holder.dateAdded.setText(timeAgo);

         //using Glide library to display image

        Glide.with(context)
                .load(imageUrl)
                .centerCrop()
                .into(holder.image);


    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    //viewHolder

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView  title,descreption , dateAdded , name ;
        public ImageView image;
        public ImageView shareButton;
        String userId;
        String userName;

        public MyViewHolder(@NonNull View itemView , Context ctx) {
            super(itemView);
        context = ctx;
        title = itemView.findViewById(R.id.journal_title);
        descreption = itemView.findViewById(R.id.journal_description);
        dateAdded = itemView.findViewById(R.id.journal_timestamp_list);
        image = itemView.findViewById(R.id.journal_image_list);
        name = itemView.findViewById(R.id.journal_row_username);
        shareButton =itemView.findViewById(R.id.journal_row_share_btn);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sharing Post
            }
        });

        }
    }
}
