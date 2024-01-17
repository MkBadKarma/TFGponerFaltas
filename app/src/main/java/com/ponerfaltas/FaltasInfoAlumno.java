package com.ponerfaltas;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FaltasInfoAlumno extends AppCompatActivity {
    private static final String TAG = "FaltasInfoAlumno";
    private TextView textView1, textView2, textView3, textView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faltas_info_alumno);

        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);

        // Obtener la asignatura seleccionada del Intent
        String selectedSubject = getIntent().getStringExtra("selectedSubject");

        loadFaltasInfo(selectedSubject);
    }

    private void loadFaltasInfo(String selectedSubject) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            FirebaseFirestore.getInstance()
                    .collection("student")
                    .whereEqualTo("student_id", userId)
                    .get()
                    .addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful() && !userTask.getResult().isEmpty()) {
                            String userName = userTask.getResult().getDocuments().get(0).getString("nombre");

                            Log.d(TAG, "User ID: " + userId);
                            Log.d(TAG, "User Name: " + userName);

                            FirebaseFirestore.getInstance()
                                    .collection("Faltas")
                                    .whereEqualTo("nombre_alumno", userName)
                                    .whereEqualTo("asignatura", selectedSubject)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            int totalFaltas = 0;

                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String nombreMaestro = document.getString("nombre_maestro");
                                                String asignatura = document.getString("asignatura");
                                                String numeroFaltas = document.getString("numero_faltas");

                                                updateViews(nombreMaestro, asignatura, numeroFaltas);

                                                totalFaltas += Integer.parseInt(numeroFaltas);
                                            }

                                            textView2.setText("Numero de faltas totales: " + totalFaltas);
                                        } else {
                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                        }
                                    });
                        } else {
                            Log.d(TAG, "Error getting user document or user not found");
                        }
                    });
        }
    }




    private void updateViews(String nombreMaestro, String asignatura, String numeroFaltas) {
        // Actualizar las vistas con la información de la falta
        textView3.setText("Maestro: " + nombreMaestro);
        textView4.setText("Asignatura: " + asignatura);
        textView2.setText("Número de faltas: " + numeroFaltas);
    }
}
