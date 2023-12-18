package com.ponerfaltas;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlumnoAsignaturas extends AppCompatActivity {

    private static final String TAG = "AlumnoAsignaturas";
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumno_asignaturas);

        listView = findViewById(R.id.listview);

        loadClases();
    }

    private void loadClases() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String email = user.getEmail();

            FirebaseFirestore.getInstance()
                    .collection("student")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String clasesString = document.getString("clase");
                                if (clasesString != null) {
                                    clasesString = clasesString.trim();
                                    List<String> clases = Arrays.asList(clasesString.split(", "));

                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AlumnoAsignaturas.this, android.R.layout.simple_list_item_1, clases);
                                    listView.setAdapter(adapter);
                                } else {
                                    Log.d(TAG, "claseString is null");
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    });
        } else {
            Log.d(TAG, "User is not authenticated");
        }
    }
}

