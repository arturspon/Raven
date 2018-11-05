package br.edu.uffs.raven.InsideChat;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import br.edu.uffs.raven.Helpers.ProfileHelper;
import br.edu.uffs.raven.Models.Chat;
import br.edu.uffs.raven.Models.Message;
import br.edu.uffs.raven.R;

public class ChatActivity extends AppCompatActivity {

    // Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Data
    private String chatId;
    private DocumentReference chatRef;

    // RecyclerView
    private MessagesAdapter messagesAdapter;
    private DocumentSnapshot lastDocVisible;
    private static final int QUERY_RESULT_LIMIT = 64;
    private boolean loadingMoreItems, endOfFeedReached;
    private int itemsLoaded = 0;

    // UI
    private RecyclerView rvChat;
    private EditText etMessage;
    private Button btnSendMsg;

    // Toolbar
    private TextView toolbarTitle, toolbarSubtitle;

    // Typing status
    CountDownTimer countDownTimer;
    boolean isCountDownTimerRunning;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarSubtitle = findViewById(R.id.toolbarSubtitle);
        toolbarTitle.setText(getIntent().getStringExtra("username"));

        // Get intent's data
        if(getIntent().getStringExtra("chatId") != null
                && !getIntent().getStringExtra("chatId").isEmpty())
            chatId = getIntent().getStringExtra("chatId");

        if(chatId == null || chatId.isEmpty()){
            Toast.makeText(this, "ERRO!!!!!!!!!!!11", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Referencing
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSendMsg = findViewById(R.id.btnSendMsg);

        chatRef = db.collection("chats").document(chatId);

        btnSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    if(isCountDownTimerRunning)countDownTimer.cancel();
                    typingTimer();
                }else{
                    chatRef.update("typersList", FieldValue.arrayRemove(ProfileHelper.getUserId()));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        getMessages();
        userStateWatcher();
    }

    private void typingTimer(){
        long timeInMillis = 2000;
        chatRef.update("typersList", FieldValue.arrayUnion(ProfileHelper.getUserId()));
        countDownTimer = new CountDownTimer(timeInMillis, timeInMillis/2) {
            @Override
            public void onTick(long millisUntilFinished) {

            }
            @Override
            public void onFinish() {
                chatRef.update("typersList", FieldValue.arrayRemove(ProfileHelper.getUserId()));
            }
        };

        countDownTimer.start();
        isCountDownTimerRunning = true;
    }


    private void userStateWatcher(){
        chatRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        Chat chat = documentSnapshot.toObject(Chat.class);
                        if(chat.getTypersList() != null && !chat.getTypersList().isEmpty()){
                            for(String userTyping : chat.getTypersList()){
                                if(!userTyping.equals(ProfileHelper.getUserId())){
                                    toolbarSubtitle.setText("Digitando...");
                                    break;
                                }
                            }
                        }else{
                            toolbarSubtitle.setText("Online");
                        }
                    }
                });
    }

    private void sendMessage(){
        String newMessage = etMessage.getText().toString();
        if(!newMessage.trim().isEmpty()){
            setBtnState(true);

            DocumentReference msgRef = db.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .document();

            final Message message = new Message();
            message.setId(msgRef.getId());
            message.setText(newMessage);
            message.setUserId(ProfileHelper.getUserId());
            message.setUserName(ProfileHelper.getUserName());

            msgRef.set(message)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                if(messagesAdapter != null){
                                    messagesAdapter.addSingleMessage(message);
                                    messagesAdapter.notifyItemInserted(0);
                                    rvChat.smoothScrollToPosition(0);
                                }else{
                                    ArrayList<Message> messages = new ArrayList<>();
                                    messages.add(message);
                                    setupRv(messages);
                                }
                                etMessage.setText("");
                            }else{
                                Toast.makeText(ChatActivity.this,
                                        "Erro ao enviar mensagem",
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                            setBtnState(false);
                        }
                    });
        }
    }


    private void setupRv(List<Message> messages){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                true);
        layoutManager.setStackFromEnd(true);
        messagesAdapter = new MessagesAdapter(this, messages);
        rvChat.setNestedScrollingEnabled(false);
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(messagesAdapter);
        rvChat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Direction integers: -1 for up, 1 for down, 0 will always return false.
                if (!recyclerView.canScrollVertically(1)) {
                    // Can't scroll down
                }else if(!recyclerView.canScrollVertically(-1)){
                    // Can't scroll up
                    loadMoreMessages();
                }
            }
        });
    }

    private void getMessages(){
        Query query = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(QUERY_RESULT_LIMIT);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            List<Message> messages = new ArrayList<>();
                            for(DocumentSnapshot snap : task.getResult()){
                                messages.add(snap.toObject(Message.class));
                                lastDocVisible = snap;
                            }
                            if(!messages.isEmpty()){
                                setupRv(messages);
                                itemsLoaded = messages.size();
                                rvChat.smoothScrollToPosition(0);
                            }else{
                                Toast.makeText(ChatActivity.this, "Seja o primeiro a mandar uma mensagem!", Toast.LENGTH_SHORT).show();
                            }
                            setupLiveUpdates();
                        }else{
                            Toast.makeText(ChatActivity.this, "Erro na conexão", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadMoreMessages(){
        if(lastDocVisible != null && !loadingMoreItems && !endOfFeedReached){
            loadingMoreItems = true;
            //progressBar.setVisibility(View.VISIBLE);

            db.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .startAfter(lastDocVisible)
                    .limit(QUERY_RESULT_LIMIT)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<Message> messages = new ArrayList<>();
                            if(task.isSuccessful()){
                                for(DocumentSnapshot msg : task.getResult()){
                                    messages.add(msg.toObject(Message.class));
                                    lastDocVisible = msg;
                                }
                            }
                            if(!messages.isEmpty()){
                                messagesAdapter.addMoreMessages(messages);
                                messagesAdapter.notifyItemRangeInserted(itemsLoaded, itemsLoaded+messages.size());
                                itemsLoaded += messages.size();
                            }else{
                                // No more items to load
                                endOfFeedReached = true;
                            }
                            loadingMoreItems = false;
                            //progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void setupLiveUpdates(){
        Date currentTime = Calendar.getInstance().getTime();

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .whereGreaterThanOrEqualTo("createdAt", currentTime)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
                            for(DocumentSnapshot msg : queryDocumentSnapshots.getDocuments()){
                                Message chatMessage = msg.toObject(Message.class);
                                if(!chatMessage.getUserId().equals(ProfileHelper.getUserId())){
                                    if(messagesAdapter != null){
                                        messagesAdapter.addSingleMessage(chatMessage);
                                        messagesAdapter.notifyItemInserted(0);
                                        rvChat.smoothScrollToPosition(0);
                                    }else{
                                        ArrayList<Message> messages = new ArrayList<>();
                                        messages.add(chatMessage);
                                        setupRv(messages);
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void setBtnState(boolean loading){
        if(loading){
            btnSendMsg.setText("Enviando...");
            btnSendMsg.setEnabled(false);
        }else{
            btnSendMsg.setText("Enviar");
            btnSendMsg.setEnabled(true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        chatId = intent.getStringExtra("chatId");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
