package com.kcalfit.app.ui.reference

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kcalfit.app.R
import com.kcalfit.app.data.model.*
import com.kcalfit.app.ui.viewmodel.CalFitUiState
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable fun ReferenceDate(date:String,onDate:(String)->Unit,weekly:Boolean=false) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
        IconButton({onDate(LocalDate.parse(date).minusDays(if(weekly)7 else 1).toString())}){Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft,"Previous ${if(weekly)"week" else "day"}")}
        Text(if(date==LocalDate.now().toString())if(weekly)"Last 7 Days" else "Today" else date,Modifier.weight(1f),textAlign=androidx.compose.ui.text.style.TextAlign.Center,fontSize=16.sp)
        IconButton({onDate(LocalDate.parse(date).plusDays(if(weekly)7 else 1).toString())}){Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight,"Next ${if(weekly)"week" else "day"}")}
    }
}
@Composable fun ReferenceDiary(state:CalFitUiState,onBack:()->Unit,onDate:(String)->Unit,onAdd:(MealType)->Unit,onFood:(FoodEntry)->Unit,onDelete:(FoodEntry)->Unit,onNutrition:()->Unit) {
    var deletion by remember{mutableStateOf<FoodEntry?>(null)}
    var menu by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxSize()){
        ReferenceHeader("Diary",onBack){IconButton(onNutrition){ReferenceIcon(R.drawable.reference_icon_pie,"Nutrition")};Box{IconButton({menu=true}){Icon(Icons.Default.MoreVert,"Diary menu")};DropdownMenu(menu,{menu=false}){DropdownMenuItem(text={Text("Nutrition summary")},onClick={menu=false;onNutrition()});DropdownMenuItem(text={Text("Add a snack")},onClick={menu=false;onAdd(MealType.SNACK)})}}}
        ReferenceDate(state.selectedDate,onDate)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())){
            HorizontalDivider(thickness=7.dp,color=Color(0xFFCFCDDA))
            Column(Modifier.fillMaxWidth().clickable(onClick=onNutrition).padding(16.dp)){
                Row{Text("Calories remaining",Modifier.weight(1f),fontWeight=FontWeight.SemiBold,fontSize=14.sp);Icon(Icons.Default.ChevronRight,null)}
                Row(Modifier.fillMaxWidth().padding(top=18.dp),horizontalArrangement=Arrangement.SpaceBetween){listOf(state.dailyGoal to "Goal",state.totalCalories to "Food",state.caloriesBurned to "Exercise",(state.dailyGoal-state.totalCalories+state.caloriesBurned) to "Remaining").forEachIndexed{i,(value,label)->if(i>0)Text(listOf("","−","+","=")[i],color=ReferenceStyle.Muted);Column(horizontalAlignment=Alignment.CenterHorizontally){Text("%,d".format(value),fontSize=14.sp);Text(label,fontSize=12.sp,color=ReferenceStyle.Muted,modifier=Modifier.padding(top=8.dp))}}}
            }
            MealType.entries.forEach{meal->
                val entries=state.selectedDateEntries.filter{MealType.fromString(it.mealType)==meal}
                HorizontalDivider(thickness=7.dp,color=Color(0xFFCFCDDA))
                Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text(meal.displayName,Modifier.weight(1f),fontWeight=FontWeight.SemiBold);Text(entries.sumOf{it.calories}.toString())}
                entries.forEach{food->Row(Modifier.fillMaxWidth().clickable{onFood(food)}.padding(start=16.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(food.foodName);Text(food.quantity,fontSize=12.sp,color=ReferenceStyle.Muted)};Text(food.calories.toString());IconButton({deletion=food}){Icon(Icons.Default.DeleteOutline,"Delete ${food.foodName}")}}}
                HorizontalDivider(color=ReferenceStyle.Choice)
                TextButton({onAdd(meal)},Modifier.fillMaxWidth()){Text("ADD FOOD",Modifier.weight(1f),fontSize=12.sp);Icon(Icons.Default.Add,"Add ${meal.displayName}")}
            }
        }
    }
    deletion?.let{food->AlertDialog({deletion=null},title={Text("Delete ${food.foodName}?")},text={Text("Only this diary entry will be removed.")},confirmButton={TextButton({onDelete(food);deletion=null}){Text("Delete")}},dismissButton={TextButton({deletion=null}){Text("Cancel")}})}
}
