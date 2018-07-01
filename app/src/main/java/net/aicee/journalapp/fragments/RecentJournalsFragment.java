package net.aicee.journalapp.fragments;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class RecentJournalsFragment extends JournalListFragment {

    public RecentJournalsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_journals_query]
        // Load last 20 journals
        Query recentJournalsQuery = databaseReference.child("journals").limitToFirst(20);
        // [END recent_journals_query]

        return recentJournalsQuery;
    }
}
