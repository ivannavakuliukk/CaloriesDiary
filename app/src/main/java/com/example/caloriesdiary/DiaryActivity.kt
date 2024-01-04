package com.example.caloriesdiary

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.caloriesdiary.models.Product
import com.example.caloriesdiary.models.ProductAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.logging.Handler

class DiaryActivity : AppCompatActivity() {
    private lateinit var todayDateTextView: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var selectedDate: String

    private val database = FirebaseDatabase.getInstance("https://caloriesdiary-b50c3-default-rtdb.europe-west1.firebasedatabase.app/").reference

    private var dayCalories = 0.0
    private var dayFats = 0.0
    private var dayProteins = 0.0
    private var dayCarbohydrates = 0.0

    private var dn = 0.0
    private var percentageFromDn = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

        todayDateTextView = findViewById(R.id.todayDateTextView)
        calendarView = findViewById(R.id.calendarView)

        // Встановлення початкової дати на сьогодні, якщо ні з яких активностей дата не передавалась
        val incomingIntent = intent
        val incomingSelectedDate = incomingIntent.getStringExtra("SELECTED_DATE").toString()

        if (incomingSelectedDate.length==10) {
            selectedDate = incomingSelectedDate
            todayDateTextView.text = selectedDate
        }else{ setTodayDate() }
        // Дістаємо id користувача
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser!!.uid
        //ініціалізуємо значення денної норми для користувача
        getDn(userId)
        toggleCalendarVisibility()
        // виводимо всі продукти в таблицях прийомів їжі
        showProductLists(userId, selectedDate)
        // Обробка кліку на сьогоднішню дату
        todayDateTextView.setOnClickListener {
            toggleCalendarVisibility()
        }
        //Слухач на подію вибору дати
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            selectedDate = getDateFromCalendar(year, month, dayOfMonth)
            val todayCalendar = Calendar.getInstance()
            val yesterdayCalendar = Calendar.getInstance()
            yesterdayCalendar.add(Calendar.DAY_OF_MONTH, -1)
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val todayDate = dateFormat.format(todayCalendar.time)
            val yesterdayDate = dateFormat.format(yesterdayCalendar.time)

            when (selectedDate) {
                todayDate -> todayDateTextView.text = "Сьогодні, $selectedDate"
                yesterdayDate -> todayDateTextView.text = "Вчора, $selectedDate"
                else -> todayDateTextView.text = selectedDate
            }
            toggleCalendarVisibility()
            setTotalToZero()
            showProductLists(userId, selectedDate)
        }
    }

    fun startReportActivity(v: View){
        if(percentageFromDn!=0){
            val intent = Intent(this, ReportActivity::class.java)
            intent.putExtra("SELECTED_DATE", selectedDate)
            intent.putExtra("PERCENTAGE_DN", percentageFromDn.toString())
            intent.putExtra("TOTAL_FAT", dayFats.toString())
            intent.putExtra("TOTAL_PROTEIN", dayProteins.toString())
            intent.putExtra("TOTAL_CARBS", dayCarbohydrates.toString())
            startActivity(intent)
        }else{
            Toast.makeText(this, "Додайте щось до щоденника щоб побачити статистику", Toast.LENGTH_SHORT).show()
        }
    }
    // Метод для встановлення сьогоднішньої дати
    @SuppressLint("SetTextI18n")
    private fun setTodayDate() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        selectedDate = dateFormat.format(calendar.time)
        todayDateTextView.text = "Сьогодні, $selectedDate"
    }

    // Метод для отримання дати з календаря
    private fun getDateFromCalendar(year: Int, month: Int, dayOfMonth: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    //Метод для відображення/приховування CalendarView
    private fun toggleCalendarVisibility() {
        if (calendarView.visibility == View.VISIBLE) {
            calendarView.visibility = View.INVISIBLE
        } else {
            calendarView.visibility = View.VISIBLE
        }
        Log.d("CalendarVisibility", "Calendar visibility: ${calendarView.visibility}")
    }

    //Метод для відкриття UserActivity
    fun startUserActivity(v: View) {
        val intent = Intent(this, UserPageActivity::class.java)
        startActivity(intent)
    }


    // метод для відкриття пошукової активності
    fun startSearchProductActivity(view: View) {
        val intent = Intent(this, SearchProductActivity::class.java)
        val mealType = when (view.id) {
            R.id.add_product_breakfast -> "Сніданок"
            R.id.add_product_lunch -> "Обід"
            R.id.add_product_dinner -> "Вечеря"
            R.id.add_product_snack -> "Перекус"
            else -> ""
        }
        // передаємо прийом їжі та дату
        intent.putExtra("SELECTED_DATE", selectedDate)
        intent.putExtra("MEAL_TYPE", mealType)
        // прослідковуєм чи дані передаються
        Log.d("DiaryActivity", "SELECTED_DATE: $selectedDate, MEAL_TYPE: $mealType")
        startActivity(intent)
    }


    // Отримання списку продуктів для певного прийому їжі
    private fun getProductListForMealType(
        userId: String,
        selectedDate: String,
        mealType: String,
        callback: (List<Product>, Double, Double, Double, Double) -> Unit
    ) {
        val productsRef = database.child("Diaries").child(userId).child(selectedDate).child(mealType)
        productsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<Product>()
                // для початку встановлюєм значення total прийому їжі до нуля
                var totalCalories = 0.0
                var totalProteins = 0.0
                var totalFats = 0.0
                var totalCarbohydrates = 0.0

                // Перевірка наявності даних для обраної дати
                if (snapshot.exists()) {
                    snapshot.children.forEach { productSnapshot ->
                        // Якщо дані наявні, отримуєм назву продукту та вагу
                        val productName = productSnapshot.key.toString()
                        val weight =
                            productSnapshot.child("weight").getValue(Double::class.java) ?: 0.0

                        val productInfoRef = database.child("Products").child(productName)
                        // шукаємо відповідний продукт у таблиці з продуктами та отримуємо дані
                        productInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(productInfoSnapshot: DataSnapshot) {
                                val caloriesPer100 = productInfoSnapshot.child("calories")
                                    .getValue(Double::class.java)!!.toDouble()
                                val proteinPer100 = productInfoSnapshot.child("protein")
                                    .getValue(Double::class.java)!!.toDouble()
                                val fatPer100 =
                                    productInfoSnapshot.child("fat").getValue(Double::class.java)!!
                                        .toDouble()
                                val carbsPer100 = productInfoSnapshot.child("carbs")
                                    .getValue(Double::class.java)!!.toDouble()

                                // так, як кбжу в таблиці продуктів вказується на 100 г, треба обрахувати відносно ваги
                                val calories = caloriesPer100 * 0.01 * weight
                                val protein = proteinPer100 * 0.01 * weight
                                val fat = fatPer100 * 0.01 * weight
                                val carbs = carbsPer100 * 0.01 * weight
                                val product =
                                    Product(productName, weight, calories, protein, fat, carbs, countPercentageFromDn(calories))

                                // оновлюєм значення total прийому їжі
                                totalCalories += product.calories
                                totalProteins += product.protein
                                totalFats += product.fat
                                totalCarbohydrates += product.carbs

                                // оновлюєм значення total за цілий день
                                dayCalories += product.calories
                                dayProteins += product.protein
                                dayFats += product.fat
                                dayCarbohydrates += product.carbs
                                // повертаєм список продуктів
                                productList.add(product)
                                if (productList.size == snapshot.childrenCount.toInt()) {
                                    callback(
                                        productList,
                                        totalCalories,
                                        totalProteins,
                                        totalFats,
                                        totalCarbohydrates
                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("FirebaseError", "Database error occurred: ${error.message}")
                            }
                        })
                    }
                } else {
                    // Якщо для обраної дати дані відсутні, викликаємо callback з порожнім списком
                    callback(
                        productList,
                        totalCalories,
                        totalProteins,
                        totalFats,
                        totalCarbohydrates
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Database error occurred: ${error.message}")
            }
        })
    }
    // Використання методу для отримання списку продуктів для сніданку
    private fun getProductListForBreakfast(userId: String, selectedDate: String) {
        val mealType = "breakfast"
        // для відображення використовуємо власний адаптер для RecyclerView
        getProductListForMealType(userId, selectedDate, mealType) { productList, totalCalories, totalProteins, totalFats, totalCarbohydrates ->
            showTotal()
            val recyclerView: RecyclerView = findViewById(R.id.item_list_breakfast)
            val layoutManager = LinearLayoutManager(this)
            recyclerView.layoutManager = layoutManager

            val adapter = ProductAdapter(productList) { selectedItem ->
                // Обробка кліку на елементі списку
                startEditProductActivity(mealType, selectedItem.name)
            }
            recyclerView.adapter = adapter

            // Використання отриманих даних totalCalories, totalProteins, totalFats, totalCarbohydrates
            val totalCaloriesTextView: TextView = findViewById(R.id.total_calories_breakfast)
            totalCaloriesTextView.text = totalCalories.toInt().toString()
            val totalFatTextView: TextView = findViewById(R.id.total_fat_breakfast)
            totalFatTextView.text = roundToSpecialFormat(totalFats).toString()
            val totalProteinTextView: TextView = findViewById(R.id.total_protein_breakfast)
            totalProteinTextView.text = roundToSpecialFormat(totalProteins).toString()
            val totalCarbsTextView: TextView = findViewById(R.id.total_carbs_breakfast)
            totalCarbsTextView.text = roundToSpecialFormat(totalCarbohydrates).toString()
            val totalDnTextView: TextView = findViewById(R.id.total_dn_breakfast)
            totalDnTextView.text = countPercentageFromDn(totalCalories).toString()

        }
    }

    // Використання методу для отримання списку продуктів для обіду
    @SuppressLint("SetTextI18n")
    private fun getProductListForLunch(userId: String, selectedDate: String) {
        val mealType = "lunch"
        getProductListForMealType(userId, selectedDate, mealType) { productList, totalCalories, totalProteins, totalFats, totalCarbohydrates ->
            showTotal()
            val recyclerView: RecyclerView = findViewById(R.id.item_list_lunch)
            val layoutManager = LinearLayoutManager(this)
            recyclerView.layoutManager = layoutManager

            val adapter = ProductAdapter(productList) { selectedItem ->
                // Обробка кліку на елементі списку
                startEditProductActivity(mealType, selectedItem.name)
            }
            recyclerView.adapter = adapter

            // Використання отриманих даних totalCalories, totalProteins, totalFats, totalCarbohydrates
            val totalCaloriesTextView: TextView = findViewById(R.id.total_calories_lunch)
            totalCaloriesTextView.text = totalCalories.toInt().toString()
            val totalFatTextView: TextView = findViewById(R.id.total_fat_lunch)
            totalFatTextView.text = roundToSpecialFormat(totalFats).toString()
            val totalProteinTextView: TextView = findViewById(R.id.total_protein_lunch)
            totalProteinTextView.text = roundToSpecialFormat(totalProteins).toString()
            val totalCarbsTextView: TextView = findViewById(R.id.total_carbs_lunch)
            totalCarbsTextView.text = roundToSpecialFormat(totalCarbohydrates).toString()
            val totalDnTextView: TextView = findViewById(R.id.total_dn_lunch)
            totalDnTextView.text = countPercentageFromDn(totalCalories).toString()+ "%"
        }
    }

    // Використання методу для отримання списку продуктів для вечері
    @SuppressLint("SetTextI18n")
    private fun getProductListForDinner(userId: String, selectedDate: String) {
        val mealType = "dinner"
        getProductListForMealType(userId, selectedDate, mealType) { productList, totalCalories, totalProteins, totalFats, totalCarbohydrates ->
            showTotal()
            val recyclerView: RecyclerView = findViewById(R.id.item_list_dinner)
            val layoutManager = LinearLayoutManager(this)
            recyclerView.layoutManager = layoutManager

            val adapter = ProductAdapter(productList) { selectedItem ->
                // Обробка кліку на елементі списку
                startEditProductActivity(mealType, selectedItem.name)
            }
            recyclerView.adapter = adapter

            // Використання отриманих даних totalCalories, totalProteins, totalFats, totalCarbohydrates
            val totalCaloriesTextView: TextView = findViewById(R.id.total_calories_dinner)
            totalCaloriesTextView.text = totalCalories.toInt().toString()
            val totalFatTextView: TextView = findViewById(R.id.total_fat_dinner)
            totalFatTextView.text = roundToSpecialFormat(totalFats).toString()
            val totalProteinTextView: TextView = findViewById(R.id.total_protein_dinner)
            totalProteinTextView.text = roundToSpecialFormat(totalProteins).toString()
            val totalCarbsTextView: TextView = findViewById(R.id.total_carbs_dinner)
            totalCarbsTextView.text = roundToSpecialFormat(totalCarbohydrates).toString()
            val totalDnTextView: TextView = findViewById(R.id.total_dn_dinner)
            totalDnTextView.text = countPercentageFromDn(totalCalories).toString() + "%"
        }
    }

    // Використання методу для отримання списку продуктів для перекусу
    @SuppressLint("SetTextI18n")
    private fun getProductListForSnack(userId: String, selectedDate: String) {
        val mealType = "snack"
        getProductListForMealType(userId, selectedDate, mealType) { productList, totalCalories, totalProteins, totalFats, totalCarbohydrates ->
            showTotal()
            val recyclerView: RecyclerView = findViewById(R.id.item_list_snack)
            val layoutManager = LinearLayoutManager(this)
            recyclerView.layoutManager = layoutManager

            val adapter = ProductAdapter(productList) { selectedItem ->
                startEditProductActivity(mealType, selectedItem.name)
            }
            recyclerView.adapter = adapter

            // Використання отриманих даних totalCalories, totalProteins, totalFats, totalCarbohydrates
            val totalCaloriesTextView: TextView = findViewById(R.id.total_calories_snack)
            totalCaloriesTextView.text = totalCalories.toInt().toString()
            val totalFatTextView: TextView = findViewById(R.id.total_fat_snack)
            totalFatTextView.text = roundToSpecialFormat(totalFats).toString()
            val totalProteinTextView: TextView = findViewById(R.id.total_protein_snack)
            totalProteinTextView.text = roundToSpecialFormat(totalProteins).toString()
            val totalCarbsTextView: TextView = findViewById(R.id.total_carbs_snack)
            totalCarbsTextView.text = roundToSpecialFormat(totalCarbohydrates).toString()
            val totalDnTextView: TextView = findViewById(R.id.total_dn_snack)
            totalDnTextView.text = countPercentageFromDn(totalCalories).toString() + "%"

        }
    }
    private fun showProductLists(userId: String, selectedDate: String){
        // виводимо продукти в таблиці прийомів їжі
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        getProductListForBreakfast(userId, selectedDate)
        getProductListForLunch(userId, selectedDate)
        getProductListForDinner(userId, selectedDate)
        getProductListForSnack(userId, selectedDate)
        // Затримка на 1 секунди перед хованням ProgressBar
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            // Після 1 секунд встановлюємо видимість на INVISIBLE
            progressBar.visibility = View.INVISIBLE
        }, 400)
    }

    @SuppressLint("SetTextI18n")
    private fun showTotal(){
        // вставка значень тотал в макет
        val dayTotalCaloriesTextView:TextView = findViewById(R.id.day_calories)
        dayTotalCaloriesTextView.text = dayCalories.toInt().toString()
        val dayTotalFatTextView:TextView = findViewById(R.id.day_fat)
        dayTotalFatTextView.text = roundToSpecialFormat(dayFats).toString()
        val dayTotalProteinTextView:TextView = findViewById(R.id.day_protein)
        dayTotalProteinTextView.text = roundToSpecialFormat(dayProteins).toString()
        val dayTotalCarbsTextView:TextView = findViewById(R.id.day_carbs)
        dayTotalCarbsTextView.text = roundToSpecialFormat(dayCarbohydrates).toString()
        val dayTotalDnTextView: TextView = findViewById(R.id.day_dn)
        percentageFromDn = countPercentageFromDn(dayCalories)
        dayTotalDnTextView.text = "$percentageFromDn%"
        // якщо відсоток дн перевищує сто текстове поле стає малиновим
        if(percentageFromDn> 100){
            dayTotalDnTextView.setTextColor(ContextCompat.getColor(this, R.color.crimson))
        }else{
            dayTotalDnTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

    // метод для отримання значення dn користувача
    private fun getDn(userId: String){
        // Отримання посилання на таблицю користувачів
        val usersRef = database.child("Users")
        // Отримання значення поля "dn" для конкретного користувача за його userId
        usersRef.child(userId).child("dn").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dnValue = snapshot.getValue(Double::class.java)
                if (dnValue != null) {
                    dn = dnValue
                    Log.d("UserInfo", "User dn value: $dnValue")
                } else {
                    dn = 0.0
                    Log.d("UserInfo", "No 'dn' value found for the user")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Database error occurred: ${error.message}")
            }
        })

    }

    // допоміжний метод для розрахунку відсотку калорійності від денної норми
    private fun countPercentageFromDn(calories: Double):Int{
        return ((calories * 100)/dn).toInt()
    }

    // функція заокруглення, яка використовується при виведенні чисел.
    // Double заокруглюється до одного знаку після коми
    // Якщо ж після коми 0 - число виводиться типом Int
    private fun roundToSpecialFormat(number: Double): Any {
        val rounded = (number * 10).toInt() / 10.0

        return if (rounded % 1 == 0.0) {
            rounded.toInt()
        } else {
            rounded
        }
    }

    // метод для встановлення всіх тотал до нуля, використовується при виборі користувачем іншої дати
    private fun setTotalToZero(){
        dayCalories = 0.0
        dayFats = 0.0
        dayCarbohydrates = 0.0
        dayProteins = 0.0
    }

    // метод, який відкриває активність редагування продукту і передає туди значення
    private fun startEditProductActivity(mealType:String, selectedProduct: String){
        val intent = Intent(this, EditProductActivity::class.java)
        intent.putExtra("SELECTED_DATE", selectedDate)
        intent.putExtra("MEAL_TYPE", mealType)
        intent.putExtra("SELECTED_PRODUCT", selectedProduct)
        Log.d("SearchProductActivity", "SELECTED_DATE: $selectedDate, MEAL_TYPE: $mealType, SELECTED_PRODUCT $selectedProduct")
        startActivity(intent)
    }
}