package com.example.caloriesdiary

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
// Активність, на якій відображається статистика дня харчування
// Використовуються дані передані з активності щоденника
class ReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // отримання даних з попередньої активності, ці дані необхідні для розрахунку
        val incomingIntent = intent
        val selectedDate = incomingIntent.getStringExtra("SELECTED_DATE").toString()
        val percentageDn = incomingIntent.getStringExtra("PERCENTAGE_DN")!!.toFloat()
        val totalFat = incomingIntent.getStringExtra("TOTAL_FAT")!!.toFloat()
        val totalProtein = incomingIntent.getStringExtra("TOTAL_PROTEIN")!!.toFloat()
        val totalCarbs = incomingIntent.getStringExtra("TOTAL_CARBS")!!.toFloat()

        // Змінна необхідна для розрахунку відсоткового співвідношення бжу
        val total = totalFat + totalCarbs + totalProtein

        val dayTextView:TextView = findViewById(R.id.todayDateTextView)
        dayTextView.text = selectedDate

        // Ініціалізація кольорів
        val color1 = ContextCompat.getColor(this, R.color.pink)
        val color2 = ContextCompat.getColor(this, R.color.crimson)
        val color3 = ContextCompat.getColor(this, R.color.gray)
        val customColors = listOf(color1, color2, color3)

        // Кругова діаграма - Співвідношення бжу
        val pieChart: PieChart = findViewById(R.id.pieChart)
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(((totalProtein/total)*100), "Білки"))
        entries.add(PieEntry(((totalFat/total)*100), "Жири"))
        entries.add(PieEntry(((totalCarbs/total)*100), "Вуглеводи"))
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = customColors
        // Встановлення власних значень для стовпчиків
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}%" // Формат значень на стовпчиках
            }
        }
        val data = PieData(dataSet)
        data.setValueTextSize(15f) // Розмір тексту значень на стовпчиках
        pieChart.data = data
        pieChart.description.isEnabled = false // Приховання опису (легенди) діаграми
        pieChart.legend.isEnabled = false
        pieChart.invalidate() // Оновлення діаграми


        // Стовпчикова діаграма - відсоток від денної норми
        val barChart: BarChart = findViewById(R.id.barChart)
        // Приховання осей
        barChart.axisLeft.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.xAxis.isEnabled = false
        // Приховання сітки
        barChart.setDrawGridBackground(false)
        barChart.setDrawBorders(false) // Приховання меж
        val entries2 = ArrayList<BarEntry>()
        entries2.add(BarEntry(0f, floatArrayOf(percentageDn))) // Відсоток від денної норми
        entries2.add(BarEntry(1f, floatArrayOf(100f))) // Для порівняння
        val dataSet2 = BarDataSet(entries2, "")
        dataSet2.colors = customColors
        // Встановлення значень на стовпчиках
        dataSet2.setDrawValues(true)
        // Встановлення описів для стовпчиків
        val labels = listOf("% від ДН:", "ДН:")
        // Встановлення значень для підписів на стовпчиках
        dataSet2.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = entries2.indexOfFirst { it.y == value }
                val label = labels.getOrNull(index) ?: ""
                return "$label${value.toInt()}%" // Об'єднання значення та підпису в один рядок
            }
        }
        val maxPercentage = entries2.maxByOrNull { it.y }?.y ?: 100f // Якщо немає значень, використовуємо 100%
        val minimumValue = 0f // мінімальне значення
        val yAxis = barChart.axisLeft
        // Встановлення максимального та мінімального значень для осі Y
        yAxis.axisMaximum = maxPercentage
        yAxis.axisMinimum = minimumValue
        val data2 = BarData(dataSet2)
        data2.setValueTextSize(14f) // Розмір тексту значень на стовпчиках
        barChart.data = data2
        data2.barWidth = 0.8f // Розмір стовпців
        barChart.setFitBars(true)
        barChart.description.isEnabled = false // Приховання опису (легенди) діаграми
        barChart.legend.isEnabled = false
        barChart.invalidate() // Оновлення діаграми
    }

    fun goBack(v: View){
        finish()
    }
}