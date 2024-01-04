package com.example.caloriesdiary.models

// Клас- модель User, використовується для додавання користувача в бд, редагування даних
data class User(
    val weight: Int,
    val height: Int,
    val goal_coef: Double,
    val age: Int,
    val gender: String,
    val activityLevel: Double,
    var dn:Double
) {
    // Конструктор за замовчуванням (без аргументів) для Firebase
    constructor() : this(0, 0, 0.0, 0, "", 0.0, 0.0)
    // Конструктор для зберігання в базі даних
    constructor(
        weight: Int,
        height: Int,
        goal_coef: Double,
        age: Int,
        gender: String,
        activityLevel: Double
    ) : this(weight, height, goal_coef, age, gender, activityLevel, 0.0){
        // Ініціалізуємо 'dn' через метод calculateDN() тільки після того, як інші властивості були ініціалізовані
        dn = calculateDN()
    }

    // Обрахування денної норми користувача за формулою відносно інших даних
    fun calculateDN(): Double {
            return if (gender == ('ж').toString()){
                (655 + (9.5 * weight) + (1.8*height) - (4.7*age)*activityLevel)*goal_coef
            }else{
                (66 + (13.7 * weight) + (5*height) - (6.76*age)*activityLevel)*goal_coef
            }
        }
}