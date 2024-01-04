package com.example.caloriesdiary

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// Перший фрагмент реєстрації - введення пошти та паролю
class Register1Fragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth // Оголосити змінну mAuth на рівні класу
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register1, container, false)
        mAuth = FirebaseAuth.getInstance()
        // кнопка відкриття другого фрагмента
        val btnOpenSecondFragment: Button = view.findViewById(R.id.button)

        val emailEditText: EditText = view.findViewById(R.id.email_editText) // Отримати посилання на поле вводу електронної пошти
        val passwordEditText: EditText = view.findViewById(R.id.password_editText) // Отримати посилання на поле вводу пароля
        val confirmPasswordEditText: EditText = view.findViewById(R.id.repeat_password_editText) // Отримати посилання на поле повторного введення пароля

        // Обробник натискання на кнопку "Далі"
        btnOpenSecondFragment.setOnClickListener {
            // Отримання введених даних email та пароля
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Перевірка чи емейл є емейлом
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Введіть коректний емейл"
                return@setOnClickListener
            }

            // Перевірка довжини пароля
            if (password.length < 8) {
                passwordEditText.error = "Пароль повинен бути не менше 8 символів"
                return@setOnClickListener
            }

            // Перевірка співпадіння пароля та підтвердження пароля
            if (password != confirmPassword) {
                confirmPasswordEditText.error = "Паролі не співпадають"
                return@setOnClickListener
            }

            // Створення облікового запису користувача
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Якщо реєстрація успішна, створимо запис користувача в базі даних
                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
                        // Переходим до другого фрагмента
                        transaction.replace(R.id.fragmentContainer,
                            Register2Fragment()
                        )
                        transaction.addToBackStack(null)
                        transaction.commit()
                    } else {
                        Toast.makeText(requireContext(), "Сталася помилка", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // відкриття активності реєстрації
        val textOpenNewActivity: TextView = view.findViewById(R.id.text1)
        textOpenNewActivity.setOnClickListener {
            // Виклик нової активності
            val intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent)
        }
        return view
    }

}