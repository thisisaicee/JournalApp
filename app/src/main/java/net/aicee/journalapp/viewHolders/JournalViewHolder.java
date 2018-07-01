package net.aicee.journalapp.viewHolders;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.aicee.journalapp.R;
import net.aicee.journalapp.dataModels.Journal;

public class JournalViewHolder extends RecyclerView.ViewHolder {

    public TextView TitleTextView;
    public TextView AuthorTextView;
    public ImageView starView;
    public TextView numStarsView;
    public TextView ContentTextView;

    public JournalViewHolder(View itemView) {
        super(itemView);

        TitleTextView = itemView.findViewById(R.id.journal_title);
        AuthorTextView = itemView.findViewById(R.id.journal_author);
        starView = itemView.findViewById(R.id.star);
        numStarsView = itemView.findViewById(R.id.journal_num_stars);
        ContentTextView = itemView.findViewById(R.id.journal_text);
    }

    public void bindToJournal(Journal journal, View.OnClickListener starClickListener) {
        TitleTextView.setText(journal.title);
        AuthorTextView.setText("Written by " + journal.author);
        numStarsView.setText(String.valueOf(journal.starCount));
        ContentTextView.setText(journal.body);

        starView.setOnClickListener(starClickListener);
    }
}
