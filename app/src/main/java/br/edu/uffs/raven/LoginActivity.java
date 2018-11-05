package br.edu.uffs.raven;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import br.edu.uffs.raven.InsideChat.ChatActivity;
import br.edu.uffs.raven.Models.User;

public class LoginActivity extends AppCompatActivity {

    // Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private EditText etEmail, etPassword;
    private Button btnSignUp, btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null) startMain();

        // Referencing
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignIn = findViewById(R.id.btnSignIn);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });
    }

    private void signUp(){
        if(checkFields()){
            btnSignUp.setText("Aguarde...");
            btnSignUp.setEnabled(false);
            auth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                createUserInDb(user);
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        task.getException().getLocalizedMessage(),
                                        Toast.LENGTH_SHORT).show();
                                btnSignUp.setText("Cadastrar");
                                btnSignUp.setEnabled(true);
                            }
                        }
                    });
        }else{
            Toast.makeText(LoginActivity.this,
                    "Preencha e-mail e senha para continuar",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void createUserInDb(final FirebaseUser firebaseUser){
        User user = new User();
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());

        db.collection("users")
                .document(firebaseUser.getUid())
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            startMain();
                        }else{
                            Toast.makeText(LoginActivity.this,
                                    "Erro, tente novamente mais tarde.",
                                    Toast.LENGTH_SHORT).show();
                            btnSignUp.setText("Cadastrar");
                            btnSignUp.setEnabled(true);
                        }
                    }
                });
    }

    private void signIn(){
        if(checkFields()){
            btnSignIn.setText("Entrando...");
            btnSignIn.setEnabled(false);
            auth.signInWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                startMain();
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        task.getException().getLocalizedMessage(),
                                        Toast.LENGTH_SHORT).show();
                                btnSignIn.setText("Entrar");
                                btnSignIn.setEnabled(true);
                            }
                        }
                    });
        }else{
            Toast.makeText(LoginActivity.this,
                    "Preencha e-mail e senha para continuar",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkFields(){
        return !etEmail.getText().toString().trim().isEmpty()
                && !etPassword.getText().toString().trim().isEmpty();
    }

    private void startMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

        // Intent data
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String chatId = bundle.getString("chatId");
            if(chatId != null){
                Intent intent2 = new Intent(this, ChatActivity.class);
                intent2.putExtra("chatId", chatId);
                startActivity(intent2);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }
}
