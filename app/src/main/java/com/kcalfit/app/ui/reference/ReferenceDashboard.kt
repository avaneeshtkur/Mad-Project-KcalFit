package com.kcalfit.app.ui.reference

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.*
import com.kcalfit.app.R
import com.kcalfit.app.domain.home.stepsDisplay
import com.kcalfit.app.ui.viewmodel.CalFitUiState
import com.kcalfit.app.ui.components.CalorieGoalDialog
import com.kcalfit.app.ui.navigation.Screen
import com.kcalfit.app.ui.nakafit.NakaRoutes
import kotlin.math.roundToInt

@Composable fun ReferenceCalorieCard(state:CalFitUiState,modifier:Modifier=Modifier,steps:Long?=null,onSteps:(()->Unit)?=null) {
    val remaining=state.dailyGoal-state.totalCalories+state.caloriesBurned
    val animated by animateIntAsState(kotlin.math.abs(remaining),tween(650),label="Confirmed remaining calories")
    val budget = (state.dailyGoal + state.caloriesBurned).coerceAtLeast(1)
    val foodArc by animateFloatAsState((state.totalCalories.toFloat()/budget).coerceIn(0f,1f),tween(650),label="Confirmed food arc")
    val exerciseArc by animateFloatAsState(state.caloriesBurned.coerceIn(0, remaining.coerceAtLeast(0)).toFloat()/budget,tween(650),label="Exercise calorie credit")
    ReferenceCard(modifier){
        Text("Calories",fontSize=23.sp);Text("Remaining = Goal - Food + Exercise",fontSize=14.sp,color=ReferenceStyle.Muted)
        Row(Modifier.fillMaxWidth().padding(top=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
            Box(Modifier.size(118.dp).semantics { contentDescription = "${kotlin.math.abs(remaining)} calories ${if (remaining < 0) "over goal" else "remaining"}. Goal ${state.dailyGoal}, food ${state.totalCalories}, exercise ${state.caloriesBurned}." },contentAlignment=Alignment.Center){
                Canvas(Modifier.fillMaxSize().padding(5.dp)){
                    // Blue is remaining base calories, orange is food, pale is unused exercise credit.
                    val baseArc = (1 - foodArc - exerciseArc).coerceAtLeast(0f)
                    drawArc(ReferenceStyle.RingTrack,-90f,360f,false,style=Stroke(12.dp.toPx()))
                    drawArc(ReferenceStyle.Blue,-90f,360f*baseArc,false,style=Stroke(12.dp.toPx()))
                    drawArc(ReferenceStyle.Warning,-90f+360f*baseArc,360f*foodArc,false,style=Stroke(12.dp.toPx()))
                }
                Column(horizontalAlignment=Alignment.CenterHorizontally){Text("%,d".format(animated),fontSize=23.sp);Text(if(remaining<0)"Over goal" else "Remaining",fontSize=12.sp)}
            }
            Column(Modifier.padding(end=4.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                listOf(Triple(R.drawable.reference_icon_flag,"Base Goal",state.dailyGoal),Triple(R.drawable.reference_icon_cutlery,"Food",state.totalCalories),Triple(R.drawable.reference_icon_fire,"Exercise",state.caloriesBurned)).forEach{(icon,label,value)->
                    Row(verticalAlignment=Alignment.CenterVertically){ReferenceIcon(icon,null);Spacer(Modifier.width(12.dp));Column{Text(label,fontSize=12.sp);Text("%,d".format(value),fontSize=12.sp)}}
                }
            }
        }
        // Today's steps sit with the top statistics; the step source and its updates are unchanged.
        if(onSteps!=null){
            val display=stepsDisplay(steps)
            HorizontalDivider(Modifier.padding(top=12.dp),color=ReferenceStyle.RingTrack)
            Column(Modifier.fillMaxWidth().clickable(onClick=onSteps).padding(top=10.dp).semantics(mergeDescendants=true){
                contentDescription=if(steps==null)"Steps: not connected. Tap to set up step counting." else "Steps today: ${display.value} ${display.detail}"
            }){
                Row(verticalAlignment=Alignment.CenterVertically){
                    ReferenceIcon(R.drawable.reference_icon_shoe,null,tint=ReferenceStyle.Steps);Spacer(Modifier.width(12.dp))
                    Text("Steps",Modifier.weight(1f).padding(end=8.dp),fontSize=12.sp)
                    Column(horizontalAlignment=Alignment.End){
                        Text(display.value,fontWeight=FontWeight.Bold,fontSize=16.sp)
                        Text(display.detail,fontSize=12.sp,color=ReferenceStyle.Muted)
                    }
                }
                display.progress?.let{progress->LinearProgressIndicator(progress={progress},Modifier.fillMaxWidth().padding(top=8.dp),color=ReferenceStyle.Steps,trackColor=ReferenceStyle.RingTrack)}
            }
        }
    }
}
@Composable fun ReferenceDashboard(state:CalFitUiState,navigate:(String)->Unit,onGoal:(Int)->Unit,premium:Boolean=false,steps:Long?=null,onAddFood:(com.kcalfit.app.data.model.MealType)->Unit) {
    var edit by remember{mutableStateOf(false)}
    val pager=rememberPagerState {4}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom=12.dp)) {
        Row(Modifier.padding(horizontal=16.dp).fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
            IconButton({navigate(Screen.Profile.route)}){Surface(Modifier.size(36.dp),shape=CircleShape,color=ReferenceStyle.Selected){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(state.userProfile?.name?.trim()?.take(1)?.uppercase()?:"",color=ReferenceStyle.Blue,fontSize=18.sp,lineHeight=22.sp,maxLines=1)}}}
            Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){KcalfitWordmark(size=30.sp)
                Surface(onClick={navigate(NakaRoutes.Premium)},color=if(premium)Color.Transparent else Color(0xFFFFDF24),shape=CircleShape){Row(Modifier.padding(horizontal=16.dp,vertical=5.dp),verticalAlignment=Alignment.CenterVertically){Text(if(premium)"Premium" else "Go Premium",color=Color.Black,fontWeight=FontWeight.SemiBold,fontSize=13.sp);ReferenceIcon(R.drawable.reference_icon_crown,null,Modifier.size(16.dp))}}}
            IconButton({navigate(Screen.Chatbot.route)}){Icon(Icons.Default.ChatBubbleOutline,"Open chatbot",Modifier.size(22.dp))}
            IconButton({navigate(NakaRoutes.Notifications)}){Icon(Icons.Default.NotificationsNone,"Notifications")}
        }
        Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){Text("Today",Modifier.weight(1f),fontSize=23.sp);TextButton({edit=true}){Text("Edit",Modifier.background(ReferenceStyle.LightBlue,CircleShape).padding(horizontal=18.dp,vertical=2.dp),color=ReferenceStyle.Blue)}}
        if(premium)ReferenceCard(Modifier.padding(horizontal=16.dp,vertical=8.dp).fillMaxWidth()){
            Text("Logging progress",Modifier.align(Alignment.CenterHorizontally),fontSize=13.sp)
            Text(if(state.selectedDateEntries.isEmpty())"Let’s get started" else "Crushing it!",Modifier.align(Alignment.CenterHorizontally),fontWeight=FontWeight.SemiBold)
            Canvas(Modifier.fillMaxWidth().height(90.dp)){
                val bounds=Size(size.width*.8f,size.height*1.4f);val origin=Offset(size.width*.1f,-size.height*.8f)
                drawArc(ReferenceStyle.Selected,30f,120f,false,origin,bounds,style=Stroke(12.dp.toPx(),cap=StrokeCap.Round))
                drawArc(ReferenceStyle.Blue,150f,-120f*(state.selectedDateEntries.map{it.mealType}.distinct().size/4f).coerceIn(0f,1f),false,origin,bounds,style=Stroke(12.dp.toPx(),cap=StrokeCap.Round))
            }
            Text("You’ve logged ${state.selectedDateEntries.map{it.mealType}.distinct().size} meals and ${state.totalProteinGrams.roundToInt()}g of protein.",fontSize=12.sp)
            TextButton({navigate(Screen.Nutrition.route)}){Text("See how else to boost your progress!",fontSize=12.sp)}
        }
        HorizontalPager(pager,contentPadding=PaddingValues(start=16.dp,end=34.dp),pageSpacing=16.dp){page->
            if(page==0)ReferenceCalorieCard(state,Modifier.fillMaxWidth(),steps){navigate("health_connect")}
            else ReferenceCard(Modifier.fillMaxWidth().heightIn(min=198.dp)){
                val labels=listOf("","Protein","Carbohydrates","Fat");val values=listOf(0.0,state.totalProteinGrams,state.totalCarbsGrams,state.totalFatGrams);val targets=listOf(0,state.targetProteinGrams,state.targetCarbsGrams,state.targetFatGrams)
                Text(labels[page],fontSize=23.sp);Spacer(Modifier.height(20.dp));Text("${values[page].roundToInt()} / ${targets[page]} g",fontSize=28.sp)
                LinearProgressIndicator(progress={(values[page]/targets[page].coerceAtLeast(1)).toFloat().coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth().padding(top=18.dp))
                TextButton({navigate(Screen.Nutrition.route)}){Text("View nutrition")}
            }
        }
        Row(Modifier.align(Alignment.CenterHorizontally).padding(14.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){repeat(4){Box(Modifier.size(7.dp).background(if(pager.currentPage==it)ReferenceStyle.Blue else ReferenceStyle.Selected,CircleShape))}}
        if(!premium){TextButton({navigate(NakaRoutes.Premium)},Modifier.align(Alignment.CenterHorizontally)){Text("Explore kcalfit Premium")}}
        ReferenceCard(Modifier.padding(horizontal=16.dp).padding(bottom=16.dp).fillMaxWidth()){
            Row(verticalAlignment=Alignment.CenterVertically){Text("Diary",Modifier.weight(1f),fontSize=23.sp);TextButton({navigate(Screen.Diary.route)}){Text("View diary")}}
            listOf(com.kcalfit.app.data.model.MealType.BREAKFAST,com.kcalfit.app.data.model.MealType.LUNCH,com.kcalfit.app.data.model.MealType.DINNER).forEach { meal ->
                val calories=state.selectedDateEntries.filter{com.kcalfit.app.data.model.MealType.fromString(it.mealType)==meal}.sumOf{it.calories}
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(meal.displayName,Modifier.weight(1f),fontWeight=FontWeight.SemiBold);Text("$calories cal",color=ReferenceStyle.Muted);IconButton({onAddFood(meal)}){Icon(Icons.Default.Add,"Add ${meal.displayName}")}}
            }
        }
        ReferenceCard(Modifier.padding(horizontal=16.dp).fillMaxWidth().clickable{navigate(Screen.Exercise.route)}){Row{Text("Exercise",Modifier.weight(1f),fontWeight=FontWeight.SemiBold);Icon(Icons.Default.Add,"Log exercise",Modifier.size(20.dp))};Row(verticalAlignment=Alignment.CenterVertically){ReferenceIcon(R.drawable.reference_icon_fire,null);Text("${state.caloriesBurned} cal",Modifier.padding(6.dp),color=ReferenceStyle.Muted)};Row(verticalAlignment=Alignment.CenterVertically){ReferenceIcon(R.drawable.reference_icon_clock,null,tint=ReferenceStyle.Warning);Text("%.2f hr".format(state.exerciseEntries.sumOf{it.durationMinutes}/60.0),Modifier.padding(6.dp),color=ReferenceStyle.Muted)}}
        ReferenceCard(Modifier.padding(16.dp).fillMaxWidth().clickable{navigate(Screen.Weight.route)}){Text("Weight",fontSize=23.sp);Text("${state.latestWeightKg} kg",fontSize=26.sp);Text("Log and view your weight",color=ReferenceStyle.Muted,fontSize=12.sp)}
    }
    if(edit)CalorieGoalDialog(state.dailyGoal,{edit=false},{onGoal(it);edit=false})
}
