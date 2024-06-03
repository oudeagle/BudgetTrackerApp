package com.example.myapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.BabyBlue
import com.example.myapplication.ui.theme.GreenHex
import com.example.myapplication.ui.theme.Lavenderish
import com.example.myapplication.ui.theme.Reddish
import com.google.firebase.Firebase
import com.google.firebase.database.database


data class CategoryAllocation(val category:String, val allocation:Double, val color: Color) //A new data class is created
//for the function below. this class simply encapsulates category and allocation with a third new variable color.




@Composable
fun PieChart(categories: List<CategoryAllocation>) {
    /* This pie chart is rendered in the budgetlist page, and is part of the visual analytics
    * as referenced in the proposal.
    * It is supposed to calculate the total spending in each category of spending. Then
    * divide space on the pie chart to display. */
    val TotalAllocation = categories.sumOf { it.allocation } // Calculate the total spending through the calculation
    // of summing up all values of categories.
    Canvas(modifier = Modifier //Canvas is used to actual draw the piechart.
        .size(200.dp)
        .padding(10.dp)) {
        var startAngle = 0f //initialize the starting angle for the piechart.
        categories.forEachIndexed{ _, category ->
            //Through iterating each category, calculate the part of the pie for each category.
            val sweepAngle = (category.allocation / TotalAllocation * 360f).toFloat()
            translate(left = 250f, top = 50f) {
                drawArc( //Now with both start and end angle, draw the corresponding arc.
                    color = category.color, //Denote the color of the category from the way defined by the data class.
                    startAngle = startAngle, //Start angle
                    sweepAngle = sweepAngle, //The sweep angle, i.e., how much of the pie does this category take.
                    useCenter = true,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height),
                )
                startAngle += sweepAngle //Change the start angle to the new sweep angle, preventing the arc being drawn over
                //each other.
            }
        }
    }
    Box()
    {
        Text(text = "Total Amount Spent: %.2f£".format(TotalAllocation), textAlign = TextAlign.Center, fontSize = 18.sp, modifier = Modifier //A simple
            //text composable showing the total expenditure so far, to be placed below the piechart.
            .wrapContentHeight()
            .padding(100.dp, 25.dp))
    }

}



@Composable
fun HorizontalPieChart(categories: List<CategoryAllocation>) {
    //Simply the variant of the pie chart, when the screen is kept horizontally.
    //The major change to this version is the translation of the pie chart, to fit with the budgetList.
    val TotalAllocation = categories.sumOf { it.allocation }
    Column(modifier = Modifier.padding()){
        Canvas(modifier = Modifier
            .size(200.dp)
            .padding()) {
            var startAngle = 0f
            categories.forEachIndexed{ _, category ->
                val sweepAngle = (category.allocation / TotalAllocation * 360f).toFloat()
                translate(left = 150f, top = 50f) {
                    drawArc(
                        color = category.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height),
                    )
                    startAngle += sweepAngle
                }
            }
        }
        Text(text = "Total Amount Spent: %.2f£".format(TotalAllocation),  fontSize = 18.sp, modifier = Modifier
            .padding(vertical = 20.dp).padding(10.dp))
    }




}

@Preview
@Composable
fun PreviewPieChart() {
    val categories = listOf(
        CategoryAllocation("Housing", 1200.0, GreenHex),
        CategoryAllocation("Food", 500.0, Reddish),
        CategoryAllocation("Transportation", 300.0, BabyBlue),
        CategoryAllocation("Entertainment", 200.0, Lavenderish)
    )
    HorizontalPieChart(categories =categories)
}