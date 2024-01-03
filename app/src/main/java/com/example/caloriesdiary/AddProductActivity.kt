package com.example.caloriesdiary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.auth.FirebaseAuth

class AddProductActivity : AppCompatActivity() {
    private lateinit var selectedDate:String
    private lateinit var selectedMealType:String
    private lateinit var selectedProductName:String

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        // Отримання даних з попередньої активності
        val intent = intent
        selectedProductName = intent.getStringExtra("SELECTED_PRODUCT").toString()
        selectedDate = intent.getStringExtra("SELECTED_DATE").toString()
        selectedMealType = when (intent.getStringExtra("MEAL_TYPE")) {
            "Сніданок" -> "breakfast"
            "Обід" -> "lunch"
            "Вечеря" -> "dinner"
            "Перекус" -> "snack"
            else -> ""
        }

        // Виведення отриманого продукту в textview
        val productNameTextView: TextView = findViewById(R.id.product_name)
        productNameTextView.text = selectedProductName

        // Ініціалізуємо посилання на базу даних
        databaseReference = FirebaseDatabase.getInstance("https://caloriesdiary-b50c3-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("Products")

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
                    Toast.makeText(this@AddProductActivity, "Сталася помилка", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Database error occurred: ${databaseError.message}")
                Toast.makeText(this@AddProductActivity, "Сталася помилка", Toast.LENGTH_SHORT).show()
            }
        })

    }
    fun onSaveClicked(view: View) {
        val weightEditText: EditText = findViewById(R.id.weight_editText)
        val weightInput = weightEditText.text.toString()

        if (weightInput.isNotEmpty()) {
            val weight = weightInput.toDoubleOrNull()
            if (weight != null && weight >= 0) {
                saveProductData(selectedDate, selectedMealType, selectedProductName, weight)
            } else {
                weightEditText.error = "Введіть коректну вагу"
            }
        } else {
            weightEditText.error = "Не залишайте поле пустим"
        }
    }

    private fun saveProductData(date: String, mealType: String, productName: String, weight: Double) {
        databaseReference = FirebaseDatabase.getInstance("https://caloriesdiary-b50c3-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("Diaries")
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser!!.uid
        val diaryRef = databaseReference.child(userId).child(date)
        val mealRef = diaryRef.child(mealType)

        mealRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Якщо щоденник існує, оновлюємо або створюємо новий прийом їжі
                    mealRef.child(productName).child("weight").setValue(weight)
                } else {
                    // Якщо щоденника немає, створюємо новий прийом їжі з обраним продуктом та введеною вагою
                    val newDiaryRef = diaryRef.child(mealType)
                    val productRef = newDiaryRef.child(productName)
                    productRef.child("weight").setValue(weight)
                }
                val intent = Intent(this@AddProductActivity, DiaryActivity::class.java)
                intent.putExtra("SELECTED_DATE", selectedDate)
                startActivity(intent)
                finish()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddProductActivity, "Сталася помилка", Toast.LENGTH_SHORT).show()
            }
        })
    }


    fun backTotSearchProductActivity(v: View) {
        finish()
    }
}