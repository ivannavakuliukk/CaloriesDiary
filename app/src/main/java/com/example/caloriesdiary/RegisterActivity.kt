package com.example.caloriesdiary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
// Активність реєстрації, на якій по черзі відображатимуться 3 фрагменти реєстрації
class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Початковий фрагмент
        val initialFragment = Register1Fragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, initialFragment)
            .commit()
    }
}