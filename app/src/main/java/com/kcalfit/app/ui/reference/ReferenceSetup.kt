package com.kcalfit.app.ui.reference

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.kcalfit.app.R
import org.json.JSONObject
import java.util.Locale

/** Draft contains no credentials. Bound to the registering email before it is applied to a profile. */
data class SetupDraft(val name: String = "", val goals: String = "", val habits: String = "", val planning: String = "",
    val activity: String = "LIGHTLY_ACTIVE", val sex: String = "", val age: String = "", val country: String = "",
    val postal: String = "", val height: String = "", val weight: String = "", val target: String = "",
    val metric: Boolean = true, val weekly: Double = .2, val email: String = "") {
    val goal: String get() = when { goals.contains("Lose Weight") -> "LOSE_WEIGHT"; goals.contains("Gain Weight") || goals.contains("Gain Muscle") -> "GAIN_WEIGHT"; else -> "MAINTAIN" }
    fun json() = JSONObject().apply {
        put("name",name);put("goals",goals);put("habits",habits);put("planning",planning);put("activity",activity)
        put("sex",sex);put("age",age);put("country",country);put("postal",postal);put("height",height);put("weight",weight)
        put("target",target);put("metric",metric);put("weekly",weekly);put("email",email)
    }.toString()
    companion object {
        fun parse(raw: String): SetupDraft = runCatching { val j=JSONObject(raw);SetupDraft(j.optString("name"),j.optString("goals"),j.optString("habits"),j.optString("planning"),j.optString("activity","LIGHTLY_ACTIVE"),j.optString("sex"),j.optString("age"),j.optString("country"),j.optString("postal"),j.optString("height"),j.optString("weight"),j.optString("target"),j.optBoolean("metric",true),j.optDouble("weekly",.2),j.optString("email")) }.getOrDefault(SetupDraft())
    }
}
class SetupStore(context: Context) {
    private val prefs=context.getSharedPreferences("reference_setup",Context.MODE_PRIVATE)
    fun pending()=SetupDraft.parse(prefs.getString("pending","").orEmpty())
    fun save(draft: SetupDraft) { prefs.edit().putString("pending",draft.json()).apply() }
    fun complete(userId: String, draft: SetupDraft) { prefs.edit().putString("profile_$userId",draft.json()).remove("pending").apply() }
}

@Composable private fun SetupChoice(title: String, selected: Boolean, onClick: () -> Unit, subtitle: String = "", checkbox: Boolean = false, modifier: Modifier = Modifier) {
    Surface(onClick, modifier.fillMaxWidth(), shape = RoundedCornerShape(9.dp), color = ReferenceStyle.Choice,
        border = if(selected) BorderStroke(2.dp,ReferenceStyle.Blue) else null) {
        Row(Modifier.padding(horizontal=16.dp,vertical=16.dp).heightIn(min=32.dp),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(title,fontSize=19.sp,fontWeight=FontWeight.SemiBold,color=Color.Black)
                if(subtitle.isNotBlank()) Text(subtitle,fontSize=14.sp,color=ReferenceStyle.Muted,lineHeight=18.sp) }
            if(checkbox) Checkbox(selected,null) else RadioButton(selected,null)
        }
    }
}
@Composable fun SetupField(label: String, value: String, onChange: (String)->Unit, modifier: Modifier = Modifier, numeric: Boolean = false) {
    Column(modifier,verticalArrangement=Arrangement.spacedBy(8.dp)) {
        if(label.isNotEmpty()) Text(label,color=ReferenceStyle.Muted,fontWeight=FontWeight.Bold,fontSize=14.sp)
        OutlinedTextField(value,onChange,Modifier.fillMaxWidth(),singleLine=true,shape=RoundedCornerShape(8.dp),
            textStyle=LocalTextStyle.current.copy(fontSize=17.sp,fontWeight=FontWeight.SemiBold),
            keyboardOptions=KeyboardOptions(keyboardType=if(numeric) KeyboardType.Decimal else KeyboardType.Text),
            colors=OutlinedTextFieldDefaults.colors(unfocusedBorderColor=ReferenceStyle.Line,focusedBorderColor=ReferenceStyle.Blue))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun ReferenceSetup(initialName: String = "", onBack: () -> Unit, onComplete: (SetupDraft)->Unit, initialDraft: SetupDraft = SetupDraft(name=initialName), initialStep: Int = 0) {
    var raw by rememberSaveable { mutableStateOf(initialDraft.json()) }
    val draft=SetupDraft.parse(raw)
    fun change(d:SetupDraft) { raw=d.json() }
    var step by rememberSaveable { mutableIntStateOf(initialStep) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var countryMenu by remember { mutableStateOf(false) }
    var sexInfo by remember { mutableStateOf(false) }
    val inverse=step==2 || step==4
    val title=when(step) { 0->"Welcome";1,3,4,5,6->"Goals";9->"Goal";else->"You" }
    val segment=when(step){in 0..5->1;6->2;7->3;8->4;else->5}
    val back={ if(step>0) step-- else onBack();error=null }
    BackHandler(onBack=back)
    val scroll=rememberScrollState()
    LaunchedEffect(step) { scroll.scrollTo(0) }
    Column(Modifier.fillMaxSize().background(if(inverse) Brush.linearGradient(listOf(ReferenceStyle.Blue,ReferenceStyle.Blue,Color(0xFF7D76F8))) else Brush.linearGradient(listOf(Color.White,Color.White))).imePadding().padding(horizontal=16.dp)) {
        CompositionLocalProvider(LocalContentColor provides if(inverse) Color.White else Color.Black) {
            ReferenceHeader(title)
            ReferenceProgress(segment)
            Column(Modifier.weight(1f).verticalScroll(scroll),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                fun toggle(values:String,item:String,limit:Int=99):String { val set=values.split('|').filter{it.isNotBlank()}.toMutableList();if(item in set)set.remove(item) else if(set.size<limit)set.add(item);return set.joinToString("|") }
                @Composable fun heading(text:String) { Text(text,fontSize=21.sp,lineHeight=25.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=6.dp,bottom=16.dp)) }
                when(step) {
                    0->{heading("First, what can we call you?");Text("We’d like to get to know you.",color=ReferenceStyle.Muted,fontWeight=FontWeight.Bold);Spacer(Modifier.height(16.dp));SetupField("Preferred first name",draft.name,{change(draft.copy(name=it))})}
                    1->{heading("Hey, ${draft.name}. 👋 Let’s start with your goals.");Text("Select up to three that are most important to you.",color=ReferenceStyle.Muted)
                        listOf("Lose Weight","Maintain Weight","Gain Weight","Gain Muscle","Modify My Diet","Plan Meals","Manage Stress").forEach { item->SetupChoice(item,item in draft.goals.split('|'),{
                            var goals=draft.goals
                            if(item in listOf("Lose Weight","Maintain Weight","Gain Weight") && item !in goals.split('|')) goals=goals.split('|').filterNot{it in listOf("Lose Weight","Maintain Weight","Gain Weight")}.joinToString("|")
                            change(draft.copy(goals=toggle(goals,item,3)))
                        },checkbox=true) }}
                    2->{Spacer(Modifier.height(260.dp));Text("Great! You’ve just taken a big step on your journey.",fontSize=34.sp,lineHeight=40.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(12.dp));Text("Tracking your food helps you see your habits. Build consistency, learn what works for you, and follow your progress toward your goals.",fontSize=17.sp,lineHeight=25.sp)}
                    3->{heading("Which healthy habits are most important to you?")
                        listOf("Recommended for you" to listOf("Track macros","Track calories","Plan more meals"),"More healthy habits" to listOf("Track nutrients","Meal prep and cook","Eat mindfully","Eat a balanced diet","Eat whole foods","Eat more protein","Eat more fiber","Eat more vegetables","Eat more fruit","Drink more water","Prioritize sleep","Move more","Workout more","Something else","I’m not sure")).forEach{(label,items)->
                            Text(label,color=ReferenceStyle.Muted,modifier=Modifier.padding(top=12.dp));FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {items.forEach{item->FilterChip(item in draft.habits.split('|'),{change(draft.copy(habits=toggle(draft.habits,item)))},label={Text(item,fontWeight=FontWeight.Bold)},shape=CircleShape,colors=FilterChipDefaults.filterChipColors(selectedContainerColor=ReferenceStyle.Selected,selectedLabelColor=ReferenceStyle.Blue))}}
                        }}
                    4->{Spacer(Modifier.height(100.dp));Image(painterResource(R.drawable.reference_habits_illustration),"Person pushing an orange uphill",Modifier.fillMaxWidth().height(230.dp));Text("Excellent choices!",color=Color.White.copy(alpha=.65f),fontSize=21.sp,fontWeight=FontWeight.Bold);Text("Small habits = mighty change.",fontSize=34.sp,lineHeight=40.sp,fontWeight=FontWeight.Bold);Text("We’ll help you bank small wins (and mighty celebrations) on the way to your goals.",fontSize=17.sp,lineHeight=25.sp)}
                    5->{heading("How often do you plan your meals in advance?");listOf("Never","Rarely","Occasionally","Frequently","Always").forEach{item->SetupChoice(item,draft.planning==item,{change(draft.copy(planning=item))})}}
                    6->{heading("What is your baseline activity level?");Text("Not including workouts - we count that separately.",color=ReferenceStyle.Muted,fontWeight=FontWeight.Bold);Spacer(Modifier.height(12.dp))
                        listOf(Triple("SEDENTARY","Not Very Active","Spend most of the day sitting (e.g. bankteller, desk job)"),Triple("LIGHTLY_ACTIVE","Lightly Active","Spend a good part of the day on your feet (e.g. teacher, salesperson)"),Triple("MODERATELY_ACTIVE","Active","Spend a good part of the day doing some physical activity (e.g. food server, postal carrier)"),Triple("VERY_ACTIVE","Very Active","Spend most of the day doing heavy physical activity (e.g. bike messenger, carpenter)")).forEach{(id,label,desc)->SetupChoice(label,draft.activity==id,{change(draft.copy(activity=id))},desc)}}
                    7->{heading("Tell us a little bit about yourself");Text("Please select which sex we should use to calculate your calorie needs:",color=ReferenceStyle.Muted,fontWeight=FontWeight.Bold)
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {listOf("Male","Female").forEach{item->SetupChoice(item,draft.sex==item.uppercase(),{change(draft.copy(sex=item.uppercase()))},modifier=Modifier.weight(1f))}}
                        TextButton({sexInfo=true}) {Text("ⓘ  Which one should I choose?",color=ReferenceStyle.Muted)}
                        SetupField("How old are you?",draft.age,{change(draft.copy(age=it.filter(Char::isDigit).take(3)))},numeric=true)
                        Text("We use sex at birth and age to calculate a goal for you.",color=ReferenceStyle.Muted,fontSize=12.sp)
                        Spacer(Modifier.height(12.dp));Text("Where do you live?",color=ReferenceStyle.Muted,fontWeight=FontWeight.Bold)
                        OutlinedButton({countryMenu=true},Modifier.fillMaxWidth(),shape=RoundedCornerShape(8.dp)) {Text(draft.country.ifBlank{"Select country"},Modifier.weight(1f));Text("▾")}
                        SetupField("ZIP/Postal Code",draft.postal,{change(draft.copy(postal=it))})}
                    8->{heading("Just a few more questions");Text("How tall are you?",color=ReferenceStyle.Muted,fontWeight=FontWeight.Bold)
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){
                            if(draft.metric) SetupField("",draft.height,{change(draft.copy(height=it))},Modifier.weight(1f),true)
                            else {val inches=(draft.height.toDoubleOrNull()?:0.0)/2.54;SetupField("ft",if(inches==0.0)"" else (inches/12).toInt().toString(),{value->change(draft.copy(height=((value.toDoubleOrNull()?:0.0)*30.48+(inches%12)*2.54).toString()))},Modifier.weight(1f),true);SetupField("in",if(inches==0.0)"" else String.format(Locale.US,"%.0f",inches%12),{value->change(draft.copy(height=(((inches/12).toInt()*12+(value.toDoubleOrNull()?:0.0))*2.54).toString()))},Modifier.weight(1f),true)}
                            FilledTonalButton({change(draft.copy(metric=!draft.metric))},shape=RoundedCornerShape(8.dp)){Text(if(draft.metric)"cm" else "ft/in")}}
                        Spacer(Modifier.height(12.dp))
                        listOf("How much do you weigh?" to false,"What is your goal weight?" to true).forEach{(label,isTarget)->
                            Text(label,color=ReferenceStyle.Muted,fontWeight=FontWeight.Bold)
                            val kg=if(isTarget)draft.target else draft.weight
                            Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){SetupField("",if(draft.metric)kg else kg.toDoubleOrNull()?.let{String.format(Locale.US,"%.1f",it*2.2046226218)}.orEmpty(),{v->val value=if(draft.metric)v else v.toDoubleOrNull()?.div(2.2046226218)?.toString().orEmpty();change(if(isTarget)draft.copy(target=value) else draft.copy(weight=value))},Modifier.weight(1f),true);FilledTonalButton({change(draft.copy(metric=!draft.metric))},shape=RoundedCornerShape(8.dp)){Text(if(draft.metric)"kg" else "lb")}}
                            Text(if(isTarget)"You can always change this later." else "It’s ok to estimate, you can update this later.",color=ReferenceStyle.Muted,fontSize=12.sp)
                        }}
                    9->{heading("What is your weekly goal?");val verb=when(draft.goal){"GAIN_WEIGHT"->"Gain";"LOSE_WEIGHT"->"Lose";else->"Maintain"}
                        if(verb=="Maintain") SetupChoice("Maintain my current weight",true,{change(draft.copy(weekly=0.0))})
                        else listOf(.2,.5).forEach{rate->SetupChoice("$verb $rate kilograms per week",draft.weekly==rate,{change(draft.copy(weekly=rate))},if(rate==.2)"(Recommended)" else "")}}
                }
                error?.let{Text(it,color=if(inverse)Color.White else ReferenceStyle.Error)}
            }
            ReferenceNext(back,{
                error=when {
                    step==0 && draft.name.isBlank()->"Enter your preferred name."
                    step==1 && draft.goals.isBlank()->"Select at least one goal."
                    step==5 && draft.planning.isBlank()->"Choose how often you plan meals."
                    step==7 && (draft.sex.isBlank() || (draft.age.toIntOrNull()?:0) !in 13..100)->"Select a sex and enter an age between 13 and 100."
                    step==8 && ((draft.height.toDoubleOrNull()?:0.0) !in 100.0..230.0 || (draft.weight.toDoubleOrNull()?:0.0) !in 30.0..300.0 || (draft.target.toDoubleOrNull()?:0.0) !in 30.0..300.0)->"Enter height 100–230 cm and weights 30–300 kg."
                    else->null }
                if(error==null) {if(step==9)onComplete(draft) else step++}
            },inverse=inverse)
        }
    }
    if(sexInfo) AlertDialog({sexInfo=false},confirmButton={TextButton({sexInfo=false}){Text("Close")}},title={Text("Calorie calculation")},text={Text("The existing calorie equation uses sex at birth. Its result is an estimate; you can edit your calorie goal later.")})
    if(countryMenu) AlertDialog({countryMenu=false},confirmButton={TextButton({countryMenu=false}){Text("Cancel")}},title={Text("Select country")},text={Column(Modifier.heightIn(max=400.dp).verticalScroll(rememberScrollState())){Locale.getISOCountries().map{Locale("",it).displayCountry}.sorted().forEach{country->TextButton({change(draft.copy(country=country));countryMenu=false}){Text(country)}}}})
}
