package net.aicee.journalapp;



import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import net.aicee.journalapp.dataModels.Journal;
import net.aicee.journalapp.dataModels.User;
import net.aicee.journalapp.MainActivity;

import java.util.HashMap;
import java.util.Map;

public class JournalDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "JournalDetailActivity";

    public static final String EXTRA_JOURNAL_KEY = "journal_key";
    public static final String EXTRA_USER_JOURNAL_KEY = "journal_key";

    private DatabaseReference databaseReference;
    private ValueEventListener journalListener;
    private String journalKey;
    private String userJournalKey;
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



        Toast.makeText(this, "Submitting...", Toast.LENGTH_SHORT).show();




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
                            databaseReference.child("journals").child(journalKey).setValue(journalKey);
                            databaseReference.child("journals").child("title").setValue(titleEditText.getText());
                            databaseReference.child("journals").child("body").setValue(contentEditText.getText());
                            databaseReference.child("user-journals").child(getUid()).child(journalKey).setValue(journalKey);
                            databaseReference.child("user-journals").child(getUid()).child("title").setValue(titleEditText.getText());
                            databaseReference.child("user-journals").child(getUid()).child("body").setValue(contentEditText.getText());
                        }

                        // Finish this Activity, back to the stream

                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]

                    }
                });
                        // [END_EXCLUDE]
        // [END single_value_read]






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

                if (journal == null){
                    titleEditText.setText("");
                    contentEditText.setText("");
                }else {

                    titleEditText.setText(journal.title);
                    contentEditText.setText(journal.body);

                    // [END_EXCLUDE]

                }
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



    // [START write_fan_out]
    private void writeNew(String userId, String username, String title, String body) {
        // Create new journals
        String key = journalKey;
        Journal journal = new Journal(userId, username, title, body);

        Map<String, Object> values = journal.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/journals/" + key, values);
        childUpdates.put("/user-journals/" + userId + "/" + key, values);
        databaseReference.updateChildren(childUpdates);
    }
    // [END write_fan_out]






   private void deleteJournal(){
        String a = FirebaseDatabase.getInstance().getReference("journals").child(getUid()).toString();
            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(getUid())){
            DatabaseReference journalsReferece = FirebaseDatabase.getInstance().getReference("journals").child(journalKey);
            DatabaseReference userJournalsReference = FirebaseDatabase.getInstance().getReference("user-journals").child(getUid()).child(journalKey);
           journalsReferece.removeValue();
           userJournalsReference.removeValue();

           startActivity(new Intent(JournalDetailActivity.this, MainActivity.class));
           finish();
           Toast.makeText(this, "Journal Deleted", Toast.LENGTH_LONG).show();

            }
            else{
                Toast.makeText(this, "You do not have the permission to delete another users journal", Toast.LENGTH_LONG).show();
            }
        }


    }
