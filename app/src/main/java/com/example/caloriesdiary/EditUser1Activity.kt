package com.example.caloriesdiary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.caloriesdiary.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Активність для редагування значення DN, користувачем власноруч
class EditUser1Activity : AppCompatActivity() {
    private lateinit var mDatabase: DatabaseReference
    private lateinit var currentUser: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user1)

        // Ініціалізація посилання на базу даних та поточного користувача Firebase
        mDatabase = FirebaseDatabase.getInstance("https://caloriesdiary-b50c3-default-rtdb.europe-west1.firebasedatabase.app/").reference
        currentUser = FirebaseAuth.getInstance().currentUser!!

        val dnEditText: EditText = findViewById(R.id.dn_editText)
        val saveButton: Button = findViewById(R.id.button_save)

        // Отримати значення dn користувача з бази даних та встановити його в якості тексту за замовчуванням для EditText
        mDatabase.child("Users").child(currentUser.uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let {
                        dnEditText.setText(it.dn.toInt().toString())
                    }
                }
            }
            // Обробка помилок при зчитуванні з бази даних
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("TAG", "onCancelled: ${databaseError.toException()}")
                Toast.makeText(this@EditUser1Activity, "Сталася помилка", Toast.LENGTH_SHORT).show()
            }
        })

        // Обробник натискання кнопки "Зберегти"
        saveButton.setOnClickListener {
            val newDnText = dnEditText.text.toString()
            // перевірка вводу
            if (newDnText.isBlank()) {
                dnEditText.error = "Введіть значення DN"
                return@setOnClickListener
            }

            val newDN = newDnText.toDoubleOrNull() ?: run {
                dnEditText.error = "Невірний формат числа"
                return@setOnClickListener
            }

            // Збереження нового значення DN користувача у базі даних
            mDatabase.child("Users").child(currentUser.uid).child("dn").setValue(newDN)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Закриття активності після успішності операції
                        Toast.makeText(this@EditUser1Activity, "Дн змінено", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        // Повідомлення про помилку при збереженні
                        Toast.makeText(this@EditUser1Activity, "Помилка збереження даних", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

}