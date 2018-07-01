package net.aicee.journalapp;



import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import net.aicee.journalapp.dataModels.Journal;
import net.aicee.journalapp.dataModels.User;

import java.util.HashMap;
import java.util.Map;

public class JournalDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "JournalDetailActivity";

    public static final String EXTRA_JOURNAL_KEY = "journal_key";

    private DatabaseReference databaseReference;
    private ValueEventListener journalListener;
    private String journalKey;
    private static final String REQUIRED = "Required";


    private TextView authorTextView;
    private TextView titleTextView;
    private TextView contentTextView;

    private EditText titleEditText, contentEditText;

    private FloatingActionButton floatingEditButton, floatingDeleteButton;




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
        titleEditText = findViewById(R.id.edit_title);
        contentEditText = findViewById(R.id.edit_content);
        floatingEditButton = findViewById(R.id.edit_journal_button);

        floatingEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editJournal();

            }
        });


        floatingDeleteButton = findViewById(R.id.delete_journal_button);
        floatingDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteJournal();
            }
        });

    }




























    private void deleteJournal(){

        if (getUid() != null){
            Toast.makeText(this, "deleting...", Toast.LENGTH_SHORT).show();



           databaseReference.child("journals").child(getUid()).removeValue();
            databaseReference.child("user-journals").child(getUid()).removeValue();
            finish();
            startActivity(new Intent(this, MainActivity.class));

      }
        else{
            Toast.makeText(this, "you cannot delete a another user's post...", Toast.LENGTH_SHORT).show();

        }






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
              //  authorTextView.setText("Written by " + journal.author);
                titleEditText.setText(journal.title);
                contentEditText.setText(journal.body);
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















    private void editJournal() {
        final String title = titleEditText.getText().toString();
        final String body = contentEditText.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(title)) {
            titleEditText.setError(REQUIRED);
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(body)) {
            contentEditText.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-journals

        Toast.makeText(this, "Editing...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String userId = getUid();
        databaseReference.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(JournalDetailActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new journal
                            writeNewJournal(userId, user.username, title, body);
                        }

                        // Finish this Activity, back to the stream

                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]

                        // [END_EXCLUDE]
                    }
                });
        // [END single_value_read]
    }





    private void writeNewJournal(String userId, String username, String title, String body) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = getUid();
        Journal journal = new Journal(userId, username, title, body);
        Map<String, Object> postValues = journal.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/journals/" + key, postValues);
        childUpdates.put("/user-journals/" + userId + "/" + key, postValues);

        databaseReference.updateChildren(childUpdates);
    }





}
