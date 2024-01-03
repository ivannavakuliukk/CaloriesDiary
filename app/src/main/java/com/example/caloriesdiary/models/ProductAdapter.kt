package com.example.caloriesdiary.models

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.caloriesdiary.R
import java.util.Formatter

class ProductAdapter(private val productList: List<Product>, private val onItemClick: (Product) -> Unit) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productNameTextView: TextView = itemView.findViewById(R.id.product_name)
        val weightTextView: TextView = itemView.findViewById(R.id.weight)
        val caloriesTextView:TextView = itemView.findViewById(R.id.calories)
        val fatTextView:TextView = itemView.findViewById(R.id.fat)
        val proteinTextView:TextView = itemView.findViewById(R.id.protein)
        val carbsTextView:TextView = itemView.findViewById(R.id.carbs)
        val dnTextView:TextView = itemView.findViewById(R.id.dn)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(productList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ProductViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.productNameTextView.text = product.name
        holder.weightTextView.text = product.weight.toInt().toString()
        holder.caloriesTextView.text = product.calories.toInt().toString()
        holder.fatTextView.text = roundToSpecialFormat(product.fat).toString()
        holder.proteinTextView.text = roundToSpecialFormat(product.protein).toString()
        holder.carbsTextView.text = roundToSpecialFormat(product.carbs).toString()
        holder.dnTextView.text = product.percentageFromDn.toString() + "%"
    }

    override fun getItemCount(): Int {
        return productList.size
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
}