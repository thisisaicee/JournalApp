package net.aicee.journalapp;



import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.aicee.journalapp.dataModels.Journal;

public class JournalDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "JournalDetailActivity";

    public static final String EXTRA_JOURNAL_KEY = "journal_key";

    private DatabaseReference databaseReference;
    private ValueEventListener journalListener;
    private String journalKey;


    private TextView authorTextView;
    private TextView titleTextView;
    private TextView contentTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_detail);

        // Get journal key from intent
        journalKey = getIntent().getStringExtra(EXTRA_JOURNAL_KEY);
        if (journalKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_JOURNAL_KEY");
        }

        // Initialize Database
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("journals").child(journalKey);


        // Initialize Views
        authorTextView = findViewById(R.id.journal_author);
        titleTextView = findViewById(R.id.journal_title);
        contentTextView = findViewById(R.id.journal_text);




    }

    @Override
    public void onStart() {
        super.onStart();

        // Add value event listener to the journal

        ValueEventListener journalListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Journal object and use the values to update the UI
                Journal journal = dataSnapshot.getValue(Journal.class);
                // [START_EXCLUDE]
                authorTextView.setText("Written by " + journal.author);
                titleTextView.setText(journal.title);
                contentTextView.setText(journal.body);
                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // if failed, log a message
                Log.w(TAG, "loadJournal:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(JournalDetailActivity.this, "Failed to load journal",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        databaseReference.addValueEventListener(journalListener);
        // [END journal_value_event_listener]

        // Keep copy of journal listener so we can remove it when app stops
        this.journalListener = journalListener;



    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove journal value event listener
        if (journalListener != null) {
            databaseReference.removeEventListener(journalListener);
        }

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

    }




}
