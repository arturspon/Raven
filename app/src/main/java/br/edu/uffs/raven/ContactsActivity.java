package br.edu.uffs.raven;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import br.edu.uffs.raven.Helpers.ProfileHelper;
import br.edu.uffs.raven.Models.User;

public class ContactsActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    List<String> usersIds;

    RecyclerView rvContacts;
    FloatingActionButton fabNewChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        rvContacts = findViewById(R.id.rvContacts);
        fabNewChat = findViewById(R.id.fabNewChat);

        getContacts();
    }

    private void getContacts(){
        db.collection("users").document(ProfileHelper.getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        usersIds = user.getContacts();
                        if(usersIds != null && !usersIds.isEmpty()) setupRv();
                    }
                });
    }

    private void setupRv(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvContacts.setLayoutManager(layoutManager);
        rvContacts.setAdapter(new ContactsAdapter(this, usersIds));
    }
}
