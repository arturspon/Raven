package br.edu.uffs.raven;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import br.edu.uffs.raven.Helpers.ProfileHelper;
import br.edu.uffs.raven.Models.Chat;

public class MainActivity extends AppCompatActivity {

    // Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // UI
    private RecyclerView rvChats;
    private ProgressBar progressBar;
    private Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencing
        rvChats = findViewById(R.id.rvChats);
        progressBar = findViewById(R.id.progressBar);
        btnSignOut = findViewById(R.id.btnSignOut);

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });

        getChats();
    }

    private void getChats(){
        db.collection("chats")
                .whereArrayContains("usersIds", ProfileHelper.getUserId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Chat> chats = new ArrayList<>();
                        if(task.isSuccessful()){
                            for(DocumentSnapshot snap : task.getResult()){
                                chats.add(snap.toObject(Chat.class));
                            }
                        }
                        if(!chats.isEmpty()) setupRvChats(chats);
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void setupRvChats(ArrayList<Chat> chats){
        rvChats.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvChats.setAdapter(new ChatsAdapter(this, chats));
    }
}
