package com.kcalfit.app.ui.onboarding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kcalfit.app.CalFitApplication
import com.kcalfit.app.ui.reference.*
@Composable fun OnboardingScreen(initialName: String = "", onOnboardingCompleted: (String,Int,String,Double,Double,Double,String,String,Int)->Unit, modifier: Modifier=Modifier) {
    val store=remember {SetupStore(CalFitApplication.instance)}
    val draft=remember {store.pending()}
    val email=CalFitApplication.instance.userPreferences.getUserEmail()
    fun finish(d:SetupDraft) {store.save(d.copy(email=email));onOnboardingCompleted(d.name,d.age.toInt(),d.sex,d.height.toDouble(),d.weight.toDouble(),d.target.toDouble(),d.activity,d.goal,3)}
    if(draft.email.isNotBlank() && draft.email.equals(email,true) && draft.height.toDoubleOrNull()!=null) {
        LaunchedEffect(Unit){finish(draft)}
    } else ReferenceSetup(initialName,onBack={},onComplete=::finish)
}
