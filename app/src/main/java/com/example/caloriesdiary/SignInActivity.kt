package com.example.caloriesdiary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

// Активність реєстрації
class SignInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Ініціалізація екземпляру FirebaseAuth
        auth = FirebaseAuth.getInstance()
        val emailEditText: EditText = findViewById(R.id.email_editText)
        val passwordEditText: EditText = findViewById(R.id.password_editText)
        val signInButton: Button = findViewById(R.id.button)

        // Налаштування обробника подій на кнопку "Увійти"
        signInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Перевірки вводу
            if (email.isEmpty()) {
                emailEditText.error = "Введіть email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Введіть пароль"
                return@setOnClickListener
            }
            // Виклик функції для входу користувача
            signInUser(email, password)
        }
    }

    // Функція для входу користувача за допомогою FirebaseAuth
    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Успішно авторизовано, перехід до іншої активності
                    val intent = Intent(this, DiaryActivity::class.java)
                    Toast.makeText(this, "Авторизація успішна", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                } else {
                    // Не вдалося авторизуватись, вивести повідомлення про помилку
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> {
                            // Користувач не знайдений
                            Toast.makeText(this, "Користувача не знайдено", Toast.LENGTH_SHORT).show()
                        }

                        is FirebaseAuthInvalidCredentialsException -> {
                            // Неправильний пароль
                            Toast.makeText(this, "Неправильний пароль", Toast.LENGTH_SHORT).show()
                        }

                        else -> {
                            // Інші помилки аутентифікації
                            Toast.makeText(this, "Помилка аутентифікації", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }
    fun startRegisterActivity(v: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
