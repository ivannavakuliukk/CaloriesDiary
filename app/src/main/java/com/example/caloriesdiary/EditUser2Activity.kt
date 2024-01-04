package com.example.caloriesdiary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.example.caloriesdiary.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
// Активність, яка дозволяє користувачу редагувати особисті дані, відносно цих змін змінюється значення DN
class EditUser2Activity : AppCompatActivity() {
    private lateinit var mDatabase: DatabaseReference
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user2)

        // Ініціалізація посилання на базу даних та поточного користувача Firebase
        mDatabase = FirebaseDatabase.getInstance("https://caloriesdiary-b50c3-default-rtdb.europe-west1.firebasedatabase.app/").reference
        currentUser = FirebaseAuth.getInstance().currentUser!!

        //встановлюєм дані першого спінера
        val genderOptions = arrayOf("ж", "ч")
        val adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)
        val spinnerGender: Spinner = findViewById(R.id.gender_spinner)
        spinnerGender.adapter = adapter1

        //встановлюєм дані другого спінера
        val goalOptions = arrayOf("схуднення", "підтримання ваги", "набір ваги")
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, goalOptions)
        val spinnerGoal: Spinner = findViewById(R.id.goal_spinner)
        spinnerGoal.adapter = adapter2

        //встановлюєм дані третього спінера
        val lifestyleOptions = arrayOf("малорухливий", "трішки активний", "помірно активний", "дуже активний")
        val adapter3 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, lifestyleOptions)
        val spinnerLifestyle: Spinner = findViewById(R.id.lifestyle_spinner)
        spinnerLifestyle.adapter = adapter3

        val weightEditText: EditText = findViewById(R.id.weight_editText)
        val heightEditText: EditText = findViewById(R.id.height_editText)
        val ageEditText:EditText = findViewById(R.id.age_editText)
        val saveButton: Button = findViewById(R.id.button_save)

        // Отримати значення даних користувача з бази даних та встановити їх за замовчуванням
        mDatabase.child("Users").child(currentUser.uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let {
                        weightEditText.setText(it.weight.toString())
                        heightEditText.setText(it.height.toString())
                        ageEditText.setText(it.age.toString())

                        val userGender = it.gender
                        var defaultPosition = genderOptions.indexOf(userGender)
                        spinnerGender.setSelection(defaultPosition)

                        val userGoalCoef = it.goal_coef
                        val userGoal:String = when(userGoalCoef){
                            0.8 -> {"схуднення" }
                            1.0 -> {"підтримання ваги"}
                            else -> {"набір ваги"}
                        }
                        defaultPosition = goalOptions.indexOf(userGoal)
                        spinnerGoal.setSelection(defaultPosition)

                        val userActivityLevel = it.activityLevel
                        val userLifestyle:String = when(userActivityLevel){
                            1.0 -> {"малорухливий"}
                            1.3 -> {"трішки активний"}
                            1.6 -> {"помірно активний"}
                            else -> {"дуже активний"}
                        }
                        defaultPosition = lifestyleOptions.indexOf(userLifestyle)
                        spinnerLifestyle.setSelection((defaultPosition))

                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("TAG", "onCancelled: ${databaseError.toException()}")
            }
        })
        // Обробник натискання кнопки "Зберегти"
        saveButton.setOnClickListener {
            //задаємо всі перевірки вводу користувача
            //перевірка вводу ваги
            val weightText = weightEditText.text.toString()
            val weight: Int = if (weightText.isNotEmpty()) {
                try {
                    val weightValue = weightText.toInt()
                    if (weightValue in 30..300) {
                        // Значення ваги в межах від 30 до 300
                        weightValue
                    } else {
                        // Значення ваги не в межах від 30 до 300
                        weightEditText.error = "Введіть значення від 30 до 300"
                        return@setOnClickListener
                    }
                } catch (e: NumberFormatException) {
                    // Неправильний формат числа
                    weightEditText.error = "Неправильний формат числа"
                    return@setOnClickListener
                }
            } else {
                // якщо поле порожнє
                weightEditText.error = "Не залишайте поле вводу порожнім"
                return@setOnClickListener
            }

            //перевірка вводу росту
            val heightText = heightEditText.text.toString()
            val height: Int = if (heightText.isNotEmpty()) {
                try {
                    val heightValue = heightText.toInt()
                    if (heightValue in 100..220) {
                        // Значення росту в межах від 100 до 220 см
                        heightValue
                    } else {
                        // Значення росту не в межах від 100 до 220 см
                        heightEditText.error = "Введіть значення від 100 до 220 см"
                        return@setOnClickListener
                    }
                } catch (e: NumberFormatException) {
                    // Неправильний формат числа
                    heightEditText.error = "Неправильний формат числа"
                    return@setOnClickListener
                }
            } else {
                // якщо поле порожнє
                heightEditText.error = "Не залишайте поле вводу порожнім"
                return@setOnClickListener
            }

            // перевірка вводу мети
            val selectedGoal = spinnerGoal.selectedItem.toString()
            val goal_coef:Double = when (selectedGoal) {
                "схуднення" -> { 0.8 }
                "підтримання ваги" -> { 1.0 }
                else -> { 1.2 }
            }

            // вік
            val ageText = ageEditText.text.toString()
            val age: Int = if (ageText.isNotEmpty()) {
                try {
                    val ageValue = ageText.toInt()
                    if (ageValue in 10..80) {
                        // Значення віку в межах від 10 до 80
                        ageValue
                    } else {
                        // Значення віку не в межах від 10 до 80
                        ageEditText.error = "Введіть значення від 10 до 80 см"
                        return@setOnClickListener
                    }
                } catch (e: NumberFormatException) {
                    // Неправильний формат числа
                    ageEditText.error = "Неправильний формат числа"
                    return@setOnClickListener
                }
            } else {
                // якщо поле порожнє
                ageEditText.error = "Не залишайте поле вводу порожнім"
                return@setOnClickListener
            }
            // стать
            val gender = spinnerGender.selectedItem.toString()

            // рівень активності
            val selectedLifestyle = spinnerLifestyle.selectedItem.toString()
            val activityLevel:Double = when (selectedLifestyle) {
                "малорухливий" -> { 1.0 }
                "трішки активний" -> { 1.3 }
                "помірно активний" -> {1.6}
                else -> { 1.7 }
            }
            // оновлення введених даних у бд Firebase
            updateUserDataInFirebase(weight, height, goal_coef, age, gender, activityLevel)
        }

    }
    // Функція для оновлення користувацького профілю у базі даних Firebase
    private fun updateUserDataInFirebase(weight: Int, height: Int, goal_coef: Double, age: Int, gender: String, activityLevel: Double) {
        val user = currentUser
        user.let {
            val userId = it.uid
            // Використовуєм клас User для зберігання даних
            val userData = User(weight, height, goal_coef, age, gender, activityLevel)

            mDatabase.child("Users").child(userId).setValue(userData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // закриття активності при успішності операції
                        Toast.makeText(applicationContext, "Дані успішно змінено", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "Сталася помилка", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}