package com.example.caloriesdiary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.database.*
import java.util.Locale

class SearchProductActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var productListView: ListView
    private lateinit var noResultsTextView: TextView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_product)

        // Отримання даних з попередньої активності
        val selectedDate = intent.getStringExtra("SELECTED_DATE")
        val mealType = intent.getStringExtra("MEAL_TYPE")

        // Відображення отриманих даних у текстових полях
        val dateTextView: TextView = findViewById(R.id.date)
        val mealTextView: TextView = findViewById(R.id.meal)
        dateTextView.text = selectedDate
        mealTextView.text = mealType

        autoCompleteTextView = findViewById(R.id.search)
        productListView = findViewById(R.id.productListView)
        noResultsTextView = findViewById(R.id.noResultsTextView)

        databaseReference = FirebaseDatabase.getInstance("https://caloriesdiary-b50c3-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("Products")

        val productsList = mutableListOf<String>()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, productsList)
        productListView.adapter = adapter

        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val userInput = s.toString().lowercase(Locale.ROOT).trim()

                // Оновлення списку з бази даних перед фільтруванням
                loadProductsFromFirebase(userInput)
            }
        })
        // передаємо дані при переході на сторінку додавання продукту
        productListView.setOnItemClickListener { _, _, position, _ ->
            val selectedProduct = adapter.getItem(position)
            val intent = Intent(this, AddProductActivity::class.java)
            intent.putExtra("SELECTED_DATE", selectedDate)
            intent.putExtra("MEAL_TYPE", mealType)
            intent.putExtra("SELECTED_PRODUCT", selectedProduct)
            Log.d("SearchProductActivity", "SELECTED_DATE: $selectedDate, MEAL_TYPE: $mealType, SELECTED_PRODUCT $selectedProduct")
            startActivity(intent)
        }
    }

    private fun loadProductsFromFirebase(userInput: String) {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productsList = mutableListOf<String>()

                for (productSnapshot in dataSnapshot.children) {
                    val productName = productSnapshot.key.toString()
                    productName.let {
                        if (it.lowercase(Locale.ROOT).contains(userInput)) {
                            productsList.add(it)
                        }
                    }
                }

                updateList(productsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Database error occurred: ${databaseError.message}")
            }
        })
    }

    private fun updateList(productsList: MutableList<String>) {
        adapter.clear()

        if (productsList.isEmpty()) {
            noResultsTextView.visibility = View.VISIBLE
        } else {
            noResultsTextView.visibility = View.GONE
            adapter.addAll(productsList)
        }

        adapter.notifyDataSetChanged()
    }

    fun backToDiaryActivity(v: View) {
        finish() // Закриття поточної активності
    }

    fun openAddNewProduct(v:View){
        val intent = Intent(this, AddNewProductActivity::class.java)
        startActivity(intent)
        finish() // Закриття поточної активності
    }
}