package com.example.caloriesdiary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// активність для додавання нових продуктів в базу даних
class AddNewProductActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_product)

        databaseReference =
            FirebaseDatabase.getInstance("https://caloriesdiary-b50c3-default-rtdb.europe-west1.firebasedatabase.app/").reference.child(
                "Products"
            )

        // поля вводу
        val productNameEditText: EditText = findViewById(R.id.name_editText)
        val brandEditText: EditText = findViewById(R.id.brand_editText)
        val caloriesEditText: EditText = findViewById(R.id.calories_editText)
        val proteinEditText: EditText = findViewById(R.id.protein_editText)
        val fatEditText: EditText = findViewById(R.id.fat_editText)
        val carbsEditText: EditText = findViewById(R.id.carbs_editText)

        val addButton: Button = findViewById(R.id.button_save)


        addButton.setOnClickListener {
            // перевірки вводу
            // назва
            val productName = productNameEditText.text.toString().trim()
            if (productName.isEmpty() || productName.length < 5) {
                productNameEditText.error = "Довжина рядку не менше 5 символів"
                return@setOnClickListener
            }
            // бренд
            val brand = brandEditText.text.toString().trim()
            val brandFormatted = if (brand.isNotEmpty()) " ($brand)" else ""
            val productNameWithBrand = "$productName$brandFormatted"

            // калорійність
            val calories = caloriesEditText.text.toString().trim().toDoubleOrNull()
            if (calories == null) {
                caloriesEditText.error = "Неправильний формат вводу"
                return@setOnClickListener
            }
            if (calories < 0) {
                caloriesEditText.error = "Калорійність не може бути менше 0"
                return@setOnClickListener
            }
            // жири
            val fat = fatEditText.text.toString().trim().toDoubleOrNull()
            if (fat == null) {
                fatEditText.error = "Неправильний формат вводу"
                return@setOnClickListener
            }
            if (fat < 0) {
                fatEditText.error = "Кількість жирів не може бути менше 0"
                return@setOnClickListener
            }
            // білки
            val protein = proteinEditText.text.toString().trim().toDoubleOrNull()
            if (protein == null) {
                proteinEditText.error = "Неправильний формат вводу"
                return@setOnClickListener
            }
            if (protein < 0) {
                proteinEditText.error = "Кількість білків не може бути менше 0"
                return@setOnClickListener
            }
            // вуглеводи
            val carbs = carbsEditText.text.toString().trim().toDoubleOrNull()
            if (carbs == null) {
                carbsEditText.error = "Неправильний формат вводу"
                return@setOnClickListener
            }
            if (carbs < 0) {
                carbsEditText.error = "Кількість вуглеводів не може бути менше 0"
                return@setOnClickListener
            }
            addProductToDatabase(productNameWithBrand, calories, fat, protein, carbs)

        }

    }

    // метод для додавання продукту в бд
    private fun addProductToDatabase(
        productName: String,
        calories: Double,
        fat: Double,
        protein: Double,
        carbs: Double
    ) {
        val productDetails = hashMapOf(
            "calories" to calories,
            "fat" to fat,
            "protein" to protein,
            "carbs" to carbs
        )

        // Додаємо продукт до бд, якщо продукту з такою назвою немає
        databaseReference.child(productName).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Якщо продукт з такою назвою вже існує
                    Toast.makeText(this@AddNewProductActivity, "Продукт з такою назвою вже існує", Toast.LENGTH_SHORT).show()
                } else {
                    // Якщо продукту з такою назвою не існує, то додаємо його до бази даних
                    databaseReference.child(productName).setValue(productDetails)
                        .addOnSuccessListener {
                            val intent = Intent(this@AddNewProductActivity, SearchProductActivity::class.java)
                            Toast.makeText(this@AddNewProductActivity, "Продукт додано", Toast.LENGTH_SHORT).show()
                            startActivity(intent)
                        }
                        .addOnFailureListener { error ->
                            Log.e("FirebaseError", "Помилка додавання продукту: ${error.message}")
                            Toast.makeText(this@AddNewProductActivity, "Не вдалося додати продукт", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Помилка читання з бази даних: ${databaseError.message}")
                Toast.makeText(this@AddNewProductActivity, "Помилка зчитування з бази даних", Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun backToSearchProduct(v: View){
        finish() // Закриття поточної активності
    }
}