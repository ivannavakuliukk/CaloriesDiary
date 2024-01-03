package com.example.caloriesdiary.models

data class Product(
    val name: String,
    val weight: Double,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val percentageFromDn:Int
)