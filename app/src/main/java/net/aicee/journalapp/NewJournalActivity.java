package net.aicee.journalapp;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import net.aicee.journalapp.dataModels.Journal;
import net.aicee.journalapp.dataModels.User;

import java.util.HashMap;
import java.util.Map;

public class NewJournalActivity extends BaseActivity {

    private static final String TAG = "NewJournalActivity";
    private static final String REQUIRED = "Required";

    // [START declare_database_ref]
    private DatabaseReference databaseReference;
    // [END declare_database_ref]

    private EditText titleEditTextField;
    private EditText contentEditTextField;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_journal);

        // [START initialize_database_ref]
        databaseReference = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        titleEditTextField = findViewById(R.id.title);
        contentEditTextField = findViewById(R.id.content);
        floatingActionButton = findViewById(R.id.button_submit_journal);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitJournal();
            }
        });
    }

    private void submitJournal() {
        final String title = titleEditTextField.getText().toString();
        final String body = contentEditTextField.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(title)) {
            titleEditTextField.setError(REQUIRED);
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(body)) {
            contentEditTextField.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-journals
        setEditingEnabled(false);
        Toast.makeText(this, "Submitting...", Toast.LENGTH_SHORT).show();

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
                            Toast.makeText(NewJournalActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new journal
                            writeNewJournal(userId, user.username, title, body);
                        }

                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                });
        // [END single_value_read]
    }

    private void setEditingEnabled(boolean enabled) {
        titleEditTextField.setEnabled(enabled);
        contentEditTextField.setEnabled(enabled);
        if (enabled) {
            floatingActionButton.setVisibility(View.VISIBLE);
        } else {
            floatingActionButton.setVisibility(View.GONE);
        }
    }

    // [START write_fan_out]
    private void writeNewJournal(String userId, String username, String title, String body) {
        // Create new journals
        String key = databaseReference.child("journals").push().getKey();
        Journal journal = new Journal(userId, username, title, body);

        Map<String, Object> values = journal.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/journals/" + key, values);
        childUpdates.put("/user-journals/" + userId + "/" + key, values);

        databaseReference.updateChildren(childUpdates);
    }
    // [END write_fan_out]
}
