package com.example.caloriesdiary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

// Другий фрагмент активності реєстрації - користувач може ввести: вагу, ріст, вік, мету, стать
class Register2Fragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register2, container, false)
        // кнопка відкриття попереднього - першого фрагмента
        val btnOpenFirstFragment: Button = view.findViewById(R.id.button_back)
        btnOpenFirstFragment.setOnClickListener {
            // Отримати активність і замінити фрагмент на інший
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, Register1Fragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
        //додаємо дані в перший спінер
        val spinner: Spinner = view.findViewById(R.id.spinner)
        val items = arrayOf("схуднення", "підтримання ваги", "набір ваги")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)
        spinner.adapter = adapter

        //додаємо дані в другий спінер
        val spinner2: Spinner = view.findViewById(R.id.spinner_gender)
        val items2 = arrayOf("ж", "ч")
        val adapter2 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items2)
        spinner2.adapter = adapter2

        // кнопка відкриття наступного - третього фрагмента, передавання даних в наступний фрагмент
        val btnOpenThirdFragment: Button = view.findViewById(R.id.button_forward)
        btnOpenThirdFragment.setOnClickListener {
            // перевірки вводу
            // вага
            val weightEditText: EditText = view.findViewById(R.id.weight_editText)
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

            // ріст
            val heightEditText: EditText = view.findViewById(R.id.height_editText)
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

            // мета
            val selectedGoal = spinner.selectedItem.toString()
            val goal_coef:Double = when (selectedGoal) {
                "схуднення" -> {
                    0.8
                }
                "підтримання ваги" -> {
                    1.0
                }
                else -> {
                    1.2
                }
            }

            // вік
            val ageEditText:EditText = view.findViewById((R.id.age_editText))
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
            val gender = spinner2.selectedItem.toString()

            // створюємо bundle щоб передати дані в наступний фрагмент
            val bundle = Bundle()
            bundle.putInt("weight", weight)
            bundle.putInt("height", height)
            bundle.putDouble("goal_coef", goal_coef)
            bundle.putInt("age", age)
            bundle.putString("gender", gender)

            val fragment = Register3Fragment()
            fragment.arguments = bundle

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, fragment)
            transaction.addToBackStack(null)
            transaction.commit()

        }
        return view
    }


}