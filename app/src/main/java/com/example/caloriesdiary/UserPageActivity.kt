package com.example.caloriesdiary

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.caloriesdiary.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.w3c.dom.Text

class UserPageActivity : AppCompatActivity() {
    private lateinit var mDatabase: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_page)

        // Ініціалізація посилання на базу даних Firebase
        mDatabase = FirebaseDatabase.getInstance("https://caloriesdiary-b50c3-default-rtdb.europe-west1.firebasedatabase.app/").reference

        // Ініціалізація елементів TextView для відображення даних
        val emailTextView: TextView = findViewById(R.id.name)
        val dnTextView: TextView = findViewById(R.id.dn)
        val weightTextView: TextView = findViewById(R.id.weight)
        val heightTextView: TextView = findViewById(R.id.height)
        val goalTextView: TextView = findViewById(R.id.goal)
        val lifestyleTextView: TextView = findViewById(R.id.lifestyle)
        val genderTextView: TextView = findViewById(R.id.gender)
        val ageTextView:TextView = findViewById(R.id.age)

        // Отримання і відображення даних користувача
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            val email = it.email
            val userRef = mDatabase.child("Users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val user = dataSnapshot.getValue(User::class.java)
                        user?.let { userData ->
                            emailTextView.text = email
                            genderTextView.text = userData.gender
                            dnTextView.text = userData.dn.toInt().toString()
                            weightTextView.text = userData.weight.toString()
                            heightTextView.text = userData.height.toString()
                            ageTextView.text = userData.age.toString()
                            val goalText = when (userData.goal_coef) {
                                1.0 -> "підтримання ваги"
                                0.8 -> "схуднення"
                                1.2 -> "набір ваги"
                                else -> userData.goal_coef.toString()
                            }
                            goalTextView.text = goalText
                            val activityText = when (userData.activityLevel) {
                                1.0 -> "малорухливий"
                                1.3 -> "трішки активний"
                                1.6 -> "помірно активний"
                                1.7 -> "дуже активний"
                                else -> userData.activityLevel.toString()
                            }
                            lifestyleTextView.text = activityText
                            Log.d("TAG", "Data: $userData")
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("TAG", "onCancelled: ${databaseError.toException()}")
                }

            })
        }
    }

    fun startDiaryActivity(v: View) {
        val intent = Intent(this, DiaryActivity::class.java)
        startActivity(intent)
    }

    fun startEdit1Activity(v: View) {
        val intent = Intent(this, EditUser1Activity::class.java)
        startActivity(intent)
    }
    fun startEdit2Activity(v: View) {
        val intent = Intent(this, EditUser2Activity::class.java)
        startActivity(intent)
    }

    // функція для виходу з облікового запису
    fun signOut(v: View) {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        Toast.makeText(this, "Вихід з акаунту здійснено", Toast.LENGTH_SHORT).show()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // функція для видалення облікового запису користувача
    fun deleteAccount(v: View) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid // Отримання ID поточного користувача

        val confirmDialog = AlertDialog.Builder(this)
        confirmDialog.setTitle("Підтвердження видалення")
        confirmDialog.setMessage("Ви впевнені, що хочете видалити свій обліковий запис?")
        confirmDialog.setPositiveButton("так") { dialog, _ ->
            val databaseReference = FirebaseDatabase.getInstance("https://caloriesdiary-b50c3-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(userId!!)

            // Видалення даних користувача з Realtime Database
            databaseReference.removeValue()
                .addOnSuccessListener {
                    // Дані користувача успішно видалено з бази даних
                    // Тепер видаляємо обліковий запис користувача з бази даних Firebase Authentication
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Обліковий запис користувача успішно видалено
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                Toast.makeText(this, "Видалення акаунту та даних здійснено", Toast.LENGTH_SHORT).show()
                                startActivity(intent)
                                finish()
                            } else {
                                // Видалення облікового запису не вдалося
                                Toast.makeText(this, "Помилка видалення облікового запису", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                .addOnFailureListener { e ->
                    // Помилка видалення даних користувача з бази даних
                    Toast.makeText(this, "Помилка видалення даних: $e", Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }
        confirmDialog.setNegativeButton("ні") { dialog, _ ->
            dialog.dismiss()
        }
        confirmDialog.show()
    }
}