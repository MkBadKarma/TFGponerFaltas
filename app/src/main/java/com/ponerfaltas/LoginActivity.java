package com.ponerfaltas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText usernameEditText, passwordEditText;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login);

        loginButton.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Check user in both collections
            checkUserInCollection("teacher", email, password);
            checkUserInCollection("student", email, password);
        });
    }

    private void checkUserInCollection(String collectionName, String email, String password) {
        db.collection(collectionName)
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String storedPassword = document.getString("password");

                            if (password.equals(storedPassword)) {
                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(this, authTask -> {
                                            if (authTask.isSuccessful()) {
                                                redirectToCorrectActivity(collectionName);
                                            } else {
                                                showToast("Authentication failed.");
                                                Log.d("LoginActivity", "Authentication failed.");
                                            }
                                        });
                                return;
                            }
                        }
                        Log.d("LoginActivity", "No se encontró la cuenta en la colección: " + collectionName);
                        showToast("No se encontró la cuenta en la colección: " + collectionName);
                    } else {
                        Log.e("LoginActivity", "Error obteniendo documentos: ", task.getException());
                        showToast("Error de base de datos: " + task.getException().getMessage());
                    }
                });
    }

    private void redirectToCorrectActivity(String accountType) {
        Class<? extends AppCompatActivity> targetActivity;
        if (accountType.equals("teacher")) {
            targetActivity = MaestroClases.class;
        } else {
            targetActivity = AlumnoAsignaturas.class;
        }

        Intent intent = new Intent(LoginActivity.this, targetActivity);
        startActivity(intent);
    }


    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}