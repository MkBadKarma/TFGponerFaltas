package com.ponerfaltas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;
import java.util.List;

public class MaestroClases extends AppCompatActivity {

    private static final String TAG = "MaestroClases";
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maestro_clases);

        listView = findViewById(R.id.listview);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedClass = (String) parent.getItemAtPosition(position);
            goToStudentsActivity(selectedClass);
        });

        loadClases();
    }

    private void loadClases() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String email = user.getEmail();

            FirebaseFirestore.getInstance()
                    .collection("teacher")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String clasesString = document.getString("clases");
                                if (clasesString != null) {
                                    clasesString = clasesString.trim();
                                    List<String> clases = Arrays.asList(clasesString.split(", "));

                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MaestroClases.this, android.R.layout.simple_list_item_1, clases);
                                    listView.setAdapter(adapter);
                                } else {
                                    Log.d(TAG, "clasesString is null");
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

    private void goToStudentsActivity(String selectedClass) {
        Intent intent = new Intent(MaestroClases.this, ListadoAlumnos.class);
        intent.putExtra("selectedClass", selectedClass);
        startActivity(intent);
    }
}
