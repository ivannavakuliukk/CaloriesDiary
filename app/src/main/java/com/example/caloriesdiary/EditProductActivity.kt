package com.example.caloriesdiary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Активність для редагування продукту в щоденнику
class EditProductActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_product)

        // ініціалізуємо змінні з даних переданих з попередньої активності
        val incomingIntent = intent
        val selectedProductName = incomingIntent.getStringExtra("SELECTED_PRODUCT").toString()
        val selectedDate = incomingIntent.getStringExtra("SELECTED_DATE").toString()
        val selectedMealType = incomingIntent.getStringExtra("MEAL_TYPE").toString()

        // Виведення отриманого продукту в textview
        val productNameTextView: TextView = findViewById(R.id.product_name)
        productNameTextView.text = selectedProductName

        // Ініціалізуємо посилання на базу даних та таблицю products, дістаємо та виводимо з неї дані про продукт
        var databaseReference = FirebaseDatabase.getInstance("https://caloriesdiary-b50c3-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("Products")
        // Звернення до бази даних та пошук продукту за назвою
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild(selectedProductName)) {
                    val productSnapshot = dataSnapshot.child(selectedProductName)

                    val calories = productSnapshot.child("calories").value.toString()
                    val fat = productSnapshot.child("fat").value.toString()
                    val protein = productSnapshot.child("protein").value.toString()
                    val carbs = productSnapshot.child("carbs").value.toString()

                    // Виведення отриманих даних про продукт в відповідні textview
                    val caloriesTextView: TextView = findViewById(R.id.calories)
                    val fatTextView: TextView = findViewById(R.id.fat)
                    val proteinTextView: TextView = findViewById(R.id.protein)
                    val carbsTextView: TextView = findViewById(R.id.carbs)

                    caloriesTextView.text = calories
                    fatTextView.text = fat
                    proteinTextView.text = protein
                    carbsTextView.text = carbs
                } else {
                    // Якщо продукт не знайдено в базі даних
                    Log.d("ProductSearch", "Product not found in database")
                    Toast.makeText(this@EditProductActivity, "Сталася помилка", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Database error occurred: ${databaseError.message}")
                Toast.makeText(this@EditProductActivity, "Сталася помилка", Toast.LENGTH_SHORT).show()
            }
        })

        // отримуємо посилання на базу даних та необхідні дочірні елементи
        databaseReference = FirebaseDatabase.getInstance("https://caloriesdiary-b50c3-default-rtdb.europe-west1.firebasedatabase.app/")
            .reference.child("Diaries")
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser!!.uid
        val diaryRef = databaseReference.child(userId).child(selectedDate)
        val mealRef = diaryRef.child(selectedMealType).child(selectedProductName)

        // обробка кліку на кнопку button_save
        findViewById<View>(R.id.button_save).setOnClickListener {
            // отримуємо введене користувачем значення ваги
            val weightEditText: EditText = findViewById(R.id.weight_editText)
            val weightInput = weightEditText.text.toString()
            // перевіряємо коректність введення
            if (weightInput.isNotEmpty()) {
                val weight = weightInput.toDoubleOrNull()
                if (weight != null && weight >= 0) {
                    // оновлюємо вагу продукту у базі даних та повідомляємо користувачеві про результат
                    mealRef.child("weight").setValue(weight)
                        .addOnSuccessListener {
                            Toast.makeText(this@EditProductActivity, "Вага продукту оновлена", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@EditProductActivity, DiaryActivity::class.java)
                            intent.putExtra("SELECTED_DATE", selectedDate)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@EditProductActivity, "Сталася помилка під час оновлення ваги продукту", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    weightEditText.error = "Введіть коректну вагу"
                }
            } else {
                weightEditText.error = "Не залишайте поле пустим"
            }
        }

        // обробка кліку на кнопку button_delete
        findViewById<View>(R.id.button_delete).setOnClickListener {
            // видаляємо продукт зі щоденника у базі даних та сповіщаємо користувача про результат
            mealRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this@EditProductActivity, "Продукт видалено зі щоденника", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@EditProductActivity, DiaryActivity::class.java)
                    intent.putExtra("SELECTED_DATE", selectedDate)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this@EditProductActivity, "Сталася помилка під час видалення продукту", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun backToDiaryProductActivity(v: View) {
        finish()
    }
}