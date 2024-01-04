package com.example.caloriesdiary

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.example.caloriesdiary.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

// Третій фрагмент активності реєстрації - тут користувач обирає спосіб життя
class Register3Fragment : Fragment() {

    private lateinit var mDatabase: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register3, container, false)

        mDatabase = FirebaseDatabase.getInstance("https://caloriesdiary-b50c3-default-rtdb.europe-west1.firebasedatabase.app/").reference
        // кнопка відкриття попереднього - другого фрагмента
        val btnOpenFirstFragment: Button = view.findViewById(R.id.button_back)
        btnOpenFirstFragment.setOnClickListener {
            // Отримати активність і замінити фрагмент на інший
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, Register2Fragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
        // отримуєм агрументи передані з попередньої активності
        val weight= arguments?.getInt("weight")!!
        val height = arguments?.getInt("height")!!
        val goal_coef = arguments?.getDouble("goal_coef")!!
        val age = arguments?.getInt("age")!!
        val gender =  arguments?.getString("gender")!!


        // додаємо radiobutton з описами
        val radioGroup: RadioGroup = view.findViewById(R.id.radioGroup)

        // кнопка завершення реєсрації
        val buttonOpenDiaryActivity: Button = view.findViewById(R.id.button_forward)
        buttonOpenDiaryActivity.setOnClickListener {
            val selectedId: Int = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val radioButton: RadioButton = view.findViewById(selectedId)
                val activityLevel = when (radioButton.text.toString()) {
                    "Малорухливий" -> 1.0
                    "Трішки активний" -> 1.3
                    "Помірно активний" -> 1.6
                    else -> 1.7
                }
                // створюєм користувача, додаєм дані з поточної та попередніх активностей
                updateUserDataInFirebase(weight, height, goal_coef, age, gender, activityLevel)
            } else {
                // Відобразити повідомлення про помилку, наприклад:
                Toast.makeText(requireContext(), "Будь ласка, оберіть рівень активності", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }

        return view
    }

    //Метод збереження користувача до таблиці Users
    private fun updateUserData(userId: String, userData:User) {
        mDatabase.child("Users").child(userId).setValue(userData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Коритувача додано", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), DiaryActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Сталася помилка", Toast.LENGTH_SHORT).show()
                }
            }
    }

    //Метод де ми використовуєм клас User для збереження даних
    private fun updateUserDataInFirebase(weight: Int, height:Int, goal_coef: Double, age: Int, gender:String, activityLevel:Double) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            val userData = User(weight, height, goal_coef, age, gender, activityLevel)
            updateUserData(userId, userData)
        }
    }

}