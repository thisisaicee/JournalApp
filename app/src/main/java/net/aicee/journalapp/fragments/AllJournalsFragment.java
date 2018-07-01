package net.aicee.journalapp.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class AllJournalsFragment extends JournalListFragment {

    public AllJournalsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START all_journals_query]
        // Load all journals
        String myUserId = getUid();
        Query allJournalsQuery = databaseReference.child("journals");
        // [END all_journals_query]


        return allJournalsQuery;
    }
}