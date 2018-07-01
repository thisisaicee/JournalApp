package net.aicee.journalapp.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MyJournalsFragment extends JournalListFragment {

    public MyJournalsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All my journals
        return databaseReference.child("user-journals")
                .child(getUid());
    }
}
