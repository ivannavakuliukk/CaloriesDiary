package com.example.caloriesdiary.models

// Клас - модель Products, використовується для додавання продукту в щоденник
data class Product(
    val name: String,
    val weight: Double,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val percentageFromDn:Int
)