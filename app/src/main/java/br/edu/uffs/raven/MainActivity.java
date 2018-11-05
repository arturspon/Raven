package br.edu.uffs.raven;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import br.edu.uffs.raven.Helpers.ProfileHelper;
import br.edu.uffs.raven.InsideChat.ChatActivity;
import br.edu.uffs.raven.Models.Chat;
import br.edu.uffs.raven.Models.User;

public class MainActivity extends AppCompatActivity {

    // Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // UI
    private SwipeRefreshLayout srl;
    private RecyclerView rvChats;
    private ProgressBar progressBar;
    private Button btnSignOut;
    private FloatingActionButton fabNewChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencing
        rvChats = findViewById(R.id.rvChats);
        progressBar = findViewById(R.id.progressBar);
        btnSignOut = findViewById(R.id.btnSignOut);
        fabNewChat = findViewById(R.id.fabNewChat);
        srl = findViewById(R.id.srl);

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });

        fabNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionsDialog();
            }
        });

        getChats();

        // If user don't have username, open dialog.
        checkUsername();

        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getChats();
            }
        });

        FirebaseMessaging.getInstance().subscribeToTopic(ProfileHelper.getUserId());
    }

    private void optionsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione uma opção:");
        String[] options = new String[2];
        options[0] = "Iniciar novo chat";
        options[1] = "Ver contatos";
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        newChatDialog();
                        break;
                    case 1:
                        startActivity(new Intent(MainActivity.this, ContactsActivity.class));
                        break;
                }
            }
        });
        builder.show();
    }

    private void newChatDialog(){
        View view = getLayoutInflater().inflate(R.layout.dialog_new_chat, null);
        final EditText etEmail = view.findViewById(R.id.etEmail);
        final Button btnStartChat = view.findViewById(R.id.btnStartChat);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email do destinatário");
        builder.setView(view);
        builder.setNegativeButton("Cancelar", null);

        final Dialog dialog = builder.create();

        btnStartChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!etEmail.getText().toString().trim().isEmpty()){
                    etEmail.setEnabled(false);
                    btnStartChat.setEnabled(false);
                    btnStartChat.setText("Aguarde...");
                    db.collection("users")
                            .whereEqualTo("email", etEmail.getText().toString())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()){
                                        User userSearched = null;
                                        for(DocumentSnapshot snap : task.getResult()){
                                            userSearched = snap.toObject(User.class);
                                        }
                                        final User finalUserSearched = userSearched;
                                        if(userSearched != null){
                                            db.collection("chats")
                                                    .whereArrayContains("usersIds", ProfileHelper.getUserId())
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if(task.isSuccessful()) {
                                                                List<Chat> chats = new ArrayList<>();
                                                                for (DocumentSnapshot snap : task.getResult()) {
                                                                    chats.add(snap.toObject(Chat.class));
                                                                }
                                                                boolean chatFound = false;
                                                                for(Chat chat : chats){
                                                                    if(chat.getGroupId() == null
                                                                            && chat.getUsersIds().contains(finalUserSearched.getId())){
                                                                        Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
                                                                        chatIntent.putExtra("chatId", chat.getId());
                                                                        startActivity(chatIntent);
                                                                        chatFound = true;
                                                                        dialog.dismiss();
                                                                        break;
                                                                    }
                                                                }
                                                                if(!chatFound){
                                                                    final DocumentReference newChatRef = db.collection("chats").document();
                                                                    final Chat chatToBeCreated = new Chat();
                                                                    chatToBeCreated.setId(newChatRef.getId());

                                                                    db.collection("users")
                                                                            .document(ProfileHelper.getUserId())
                                                                            .get()
                                                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                @Override
                                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                    List<String> usersIds;
                                                                                    User user = documentSnapshot.toObject(User.class);
                                                                                    if(user.getContacts() != null && !user.getContacts().contains(finalUserSearched.getId())){
                                                                                        usersIds = user.getContacts();
                                                                                        usersIds.add(finalUserSearched.getId());
                                                                                        db.collection("users").document(ProfileHelper.getUserId())
                                                                                                .update("contacts", usersIds);
                                                                                    }else{
                                                                                        usersIds = new ArrayList<>();
                                                                                        usersIds.add(finalUserSearched.getId());
                                                                                        user.setContacts(usersIds);
                                                                                        db.collection("users").document(ProfileHelper.getUserId())
                                                                                                .set(user);
                                                                                    }
                                                                                    final List<String> usersName = new ArrayList<>();
                                                                                    usersName.add(ProfileHelper.getUserName());
                                                                                    usersName.add(finalUserSearched.getName());

                                                                                    List<String> usersId = new ArrayList<>();
                                                                                    usersId.add(ProfileHelper.getUserId());
                                                                                    usersId.add(finalUserSearched.getId());

                                                                                    chatToBeCreated.setUsersName(usersName);
                                                                                    chatToBeCreated.setUsersIds(usersId);

                                                                                    newChatRef.set(chatToBeCreated)
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if(task.isSuccessful()){
                                                                                                        Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
                                                                                                        chatIntent.putExtra("chatId", newChatRef.getId());
                                                                                                        chatIntent.putExtra("username", usersName.get(1));
                                                                                                        dialog.dismiss();
                                                                                                        startActivity(chatIntent);
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }
                                                                            });

                                                                }
                                                            }
                                                        }
                                                    });
                                        }else{
                                            Toast.makeText(MainActivity.this,
                                                    "Usuário não encontrado",
                                                    Toast.LENGTH_SHORT)
                                                    .show();
                                            etEmail.setEnabled(true);
                                            btnStartChat.setEnabled(true);
                                            btnStartChat.setText("Iniciar chat!");
                                        }
                                    }else{
                                        Toast.makeText(MainActivity.this,
                                                "Erro na conexão",
                                                Toast.LENGTH_SHORT)
                                                .show();
                                        etEmail.setEnabled(true);
                                        btnStartChat.setEnabled(true);
                                        btnStartChat.setText("Iniciar chat!");
                                    }
                                }
                            });
                }else{
                    Toast.makeText(MainActivity.this,
                            "Digite um email para continuar",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        dialog.show();
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
                        srl.setRefreshing(false);
                    }
                });
    }

    private void setupRvChats(ArrayList<Chat> chats){
        rvChats.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvChats.setAdapter(new ChatsAdapter(this, chats));
    }

    private void checkUsername(){
        db.collection("users")
                .document(ProfileHelper.getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.getString("name") == null
                                || documentSnapshot.getString("name").trim().isEmpty())
                            setUsernameDialog();
                    }
                });
    }

    private void setUsernameDialog(){
        View dialogView =
                getLayoutInflater().inflate(R.layout.dialog_set_username, null);
        final EditText etUsername = dialogView.findViewById(R.id.etUsername);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Definir nome de usuario");
        builder.setCancelable(false);
        builder.setView(dialogView);

        final Dialog dialog = builder.create();

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!etUsername.getText().toString().trim().isEmpty()){
                    db.collection("users")
                            .document(ProfileHelper.getUserId())
                            .update("name", etUsername.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(MainActivity.this,
                                                "Nome de usuário definido com sucesso!",
                                                Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }else{
                                        Toast.makeText(MainActivity.this,
                                                "Erro, tente mais tarde.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else{
                    Toast.makeText(MainActivity.this,
                            "Preencha seu nome de usuário para continuar",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        dialog.show();

    }
}
