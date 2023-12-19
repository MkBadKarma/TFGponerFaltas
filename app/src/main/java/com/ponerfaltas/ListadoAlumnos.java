package com.ponerfaltas;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListadoAlumnos extends AppCompatActivity {

    private static final String TAG = "StudentsInClassActivity";
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_alumnos);

        listView = findViewById(R.id.listview_students);

        String selectedClass = getIntent().getStringExtra("selectedClass");

        if (selectedClass != null) {
            loadStudents(selectedClass);
        } else {
            Log.d(TAG, "Selected class is null");
        }
    }

    private void loadStudents(String selectedClass) {
        FirebaseFirestore.getInstance()
                .collection("student")
                .whereEqualTo("curso", selectedClass)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> studentNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String studentName = document.getString("nombre");
                            if (studentName != null) {
                                studentNames.add(studentName);
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ListadoAlumnos.this, android.R.layout.simple_list_item_1, studentNames);
                        listView.setAdapter(adapter);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }
}
