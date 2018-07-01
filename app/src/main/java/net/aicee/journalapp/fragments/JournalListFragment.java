package net.aicee.journalapp.fragments;



import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import net.aicee.journalapp.JournalDetailActivity;
import net.aicee.journalapp.R;
import net.aicee.journalapp.dataModels.Journal;
import net.aicee.journalapp.viewHolders.JournalViewHolder;

public abstract class JournalListFragment extends Fragment {

    private static final String TAG = "JournalListFragment";

    // [START define_database_reference]
    private DatabaseReference databaseReference;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<Journal, JournalViewHolder> firebaseRecyclerAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    public JournalListFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.all_journals_fragment, container, false);

        // [START create_database_reference]
        databaseReference = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        recyclerView = rootView.findViewById(R.id.messages_list);
        recyclerView.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query journalsQuery = getQuery(databaseReference);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Journal>()
                .setQuery(journalsQuery, Journal.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Journal, JournalViewHolder>(options) {

            @Override
            public JournalViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new JournalViewHolder(inflater.inflate(R.layout.journal, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(JournalViewHolder journalViewHolder, int position, final Journal model) {
                final DatabaseReference journalRef = getRef(position);

                // Set click listener for the whole journal view
                final String journalKey = journalRef.getKey();
                journalViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch JournalDetailActivity
                        Intent intent = new Intent(getActivity(), JournalDetailActivity.class);
                        intent.putExtra(JournalDetailActivity.EXTRA_JOURNAL_KEY, journalKey);
                        startActivity(intent);
                    }
                });

                // Determine if the current user has liked this journal and set UI accordingly
                if (model.stars.containsKey(getUid())) {
                    journalViewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
                } else {
                    journalViewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                }

                // Bind Journal to ViewHolder, setting OnClickListener for the star button
                journalViewHolder.bindToJournal(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        // Need to write to both places the journal is stored
                        DatabaseReference globalJournalRef = databaseReference.child("journals").child(journalRef.getKey());
                        DatabaseReference userJournalRef = databaseReference.child("user-journals").child(model.uid).child(journalRef.getKey());

                        // Run two transactions
                        onStarClicked(globalJournalRef);
                        onStarClicked(userJournalRef);
                    }
                });
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    // [START journal_stars_transaction]
    private void onStarClicked(DatabaseReference journalRef) {
        journalRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Journal journal = mutableData.getValue(Journal.class);
                if (journal == null) {
                    return Transaction.success(mutableData);
                }

                if (journal.stars.containsKey(getUid())) {
                    // Unstar the journal and remove self from stars
                    journal.starCount = journal.starCount - 1;
                    journal.stars.remove(getUid());
                } else {
                    // Star the journal and add self to stars
                    journal.starCount = journal.starCount + 1;
                    journal.stars.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(journal);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "journalTransaction:onComplete:" + databaseError);
            }
        });
    }
    // [END journal_stars_transaction]


    @Override
    public void onStart() {
        super.onStart();
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.stopListening();
        }
    }


    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

}

