package com.kcalfit.app.ui.reference

import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.kcalfit.app.R
import com.kcalfit.app.BuildConfig
import com.kcalfit.app.CalFitApplication
import kotlinx.coroutines.launch

@Composable fun ReferenceWelcome(onLogin:()->Unit,onRegister:()->Unit,modifier:Modifier=Modifier) {
    val pager=rememberPagerState {2};val scope=rememberCoroutineScope()
    Column(modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
        Spacer(Modifier.height(40.dp));Text("Welcome to",color=ReferenceStyle.Muted,fontWeight=FontWeight.Bold,fontSize=16.sp)
        KcalfitWordmark(size=30.sp);Spacer(Modifier.height(32.dp))
        HorizontalPager(pager,contentPadding=PaddingValues(horizontal=42.dp),pageSpacing=24.dp) {page->
            Image(painterResource(if(page==0)R.drawable.reference_welcome_runner else R.drawable.reference_welcome_food),
                if(page==0)"Runner outdoors with a protein tracking illustration" else "Meal with a macro tracking illustration",
                Modifier.fillMaxWidth().aspectRatio(703f/857f).clip(RoundedCornerShape(16.dp)),contentScale=ContentScale.Fit)
        }
        Text(if(pager.currentPage==0)"Ready for some wins? Start\ntracking, it’s easy!" else "And make mindful\neating a habit for life",Modifier.padding(20.dp),fontSize=21.sp,lineHeight=25.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center,color=Color.Black)
        Row {repeat(2){i->Box(Modifier.size(32.dp).clickable{scope.launch{pager.animateScrollToPage(i)}},contentAlignment=Alignment.Center){Box(Modifier.size(9.dp).background(if(pager.currentPage==i)ReferenceStyle.Blue else ReferenceStyle.Line,CircleShape))}}}
        Spacer(Modifier.height(24.dp))
        Button(onRegister,Modifier.fillMaxWidth().padding(horizontal=36.dp).heightIn(min=48.dp),shape=CircleShape){Text("Sign Up For Free",fontSize=17.sp,fontWeight=FontWeight.Bold)}
        TextButton(onLogin,Modifier.padding(top=14.dp)){Text("Log In",fontWeight=FontWeight.Bold,fontSize=17.sp)}
        Text("Version ${BuildConfig.VERSION_NAME}",fontSize=14.sp,color=Color.Black);Spacer(Modifier.height(24.dp))
    }
}

@Composable fun ReferenceSignup(onRegister:(String,String,String)->Unit,onBack:()->Unit,onGoogle:()->Unit,errorMessage:String?,isLoading:Boolean) {
    var stage by rememberSaveable {mutableIntStateOf(0)}
    var revisiting by rememberSaveable { mutableStateOf(false) }
    var draftRaw by rememberSaveable {mutableStateOf(SetupDraft().json())}
    val draft=SetupDraft.parse(draftRaw)
    val store=remember {SetupStore(CalFitApplication.instance)}
    var email by rememberSaveable {mutableStateOf("")};var password by remember{mutableStateOf("")}
    var agreed by rememberSaveable{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)}
    var info by remember{mutableStateOf<String?>(null)}
    val back: () -> Unit = {if(stage==2){revisiting=true;stage=1} else if(stage>0)stage-- else onBack()}
    BackHandler(onBack=back)
    if(stage==1) {ReferenceSetup(draft.name,{stage=0}, {draftRaw=it.json();stage=2}, initialDraft=draft, initialStep=if(revisiting)9 else 0);return}
    Column(Modifier.fillMaxSize().background(Color.White).imePadding().padding(horizontal=16.dp)) {
        ReferenceHeader(if(stage==0)"Sign Up" else "Create Account",if(stage==0)back else null)
        if(stage==2)ReferenceProgress(6)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
            if(stage==0) {
                Spacer(Modifier.height(180.dp));Text("Welcome! Let’s customize\nkcalfit for your goals.",fontSize=21.sp,lineHeight=25.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center)
                Spacer(Modifier.height(36.dp));Button({stage=1},Modifier.fillMaxWidth().heightIn(min=48.dp),shape=RoundedCornerShape(4.dp)){Text("Continue",fontSize=17.sp,fontWeight=FontWeight.Bold)}
                Text("OR",Modifier.padding(12.dp),fontSize=17.sp)
                ElevatedButton(onGoogle,Modifier.fillMaxWidth().heightIn(min=48.dp),enabled=!isLoading,shape=RoundedCornerShape(4.dp),colors=ButtonDefaults.elevatedButtonColors(containerColor=Color.White,contentColor=Color.Black)){ReferenceIcon(R.drawable.reference_icon_google,null);Spacer(Modifier.width(8.dp));Text("Continue with Google",fontWeight=FontWeight.Bold)}
                Spacer(Modifier.height(8.dp));ElevatedButton({info="Apple sign-in is not configured for this Android app. Use email or Google to create your account."},Modifier.fillMaxWidth().heightIn(min=48.dp),shape=RoundedCornerShape(4.dp),colors=ButtonDefaults.elevatedButtonColors(containerColor=Color.White,contentColor=Color.Black)){Text("Continue with Apple",fontWeight=FontWeight.Bold)}
                Spacer(Modifier.height(90.dp));Text("We collect the profile details you choose to share to personalize your kcalfit experience.",color=ReferenceStyle.Muted,textAlign=TextAlign.Center)
                TextButton({info="Your email and account profile are handled by Supabase authentication. Food, activity and health logs are stored in the app’s local database. A published privacy-policy URL has not been configured."}){Text("Privacy Policy")}
            } else {
                Text("Almost done! Create your account.",Modifier.fillMaxWidth().padding(vertical=28.dp),fontSize=21.sp,fontWeight=FontWeight.Bold)
                SetupField("Email Address",email,{email=it;error=null})
                Spacer(Modifier.height(20.dp));Text("Password",Modifier.fillMaxWidth(),color=ReferenceStyle.Muted,fontWeight=FontWeight.Bold)
                OutlinedTextField(password,{password=it;error=null},Modifier.fillMaxWidth().padding(top=8.dp),singleLine=true,shape=RoundedCornerShape(8.dp),visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password))
                Text("10 characters minimum",Modifier.fillMaxWidth(),fontSize=12.sp,color=ReferenceStyle.Muted)
                Row(verticalAlignment=Alignment.CenterVertically){Checkbox(agreed,{agreed=it});Text("I agree to kcalfit’s Terms & Conditions and Privacy Policy",fontSize=14.sp)}
                Row{TextButton({info="A published terms-of-service URL is not configured. kcalfit provides tracking tools and estimated nutrition values; verify food portions and adjust your goals as needed."}){Text("Terms & Conditions")};TextButton({info="Your email and profile use Supabase authentication. Food and activity logs are saved locally. No meal image is uploaded until you request analysis."}){Text("Privacy Policy")}}
                Text("Your email address is used for account verification and security updates.",Modifier.padding(16.dp),color=ReferenceStyle.Muted,textAlign=TextAlign.Center)
                (errorMessage?:error)?.let{Text(it,color=ReferenceStyle.Error)}
            }
        }
        if(stage==2)ReferenceNext(back,{
            error=when{!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()->"Enter a valid email address";password.length<10->"Use at least 10 characters";!agreed->"Please review and accept the terms and privacy information";else->null}
            if(error==null){store.save(draft.copy(email=email.trim()));onRegister(draft.name.trim(),email.trim(),password)}
        },label=if(isLoading)"Creating account…" else "Next",enabled=!isLoading)
    }
    info?.let{message->AlertDialog({info=null},confirmButton={TextButton({info=null}){Text("Close")}},text={Text(message)})}
}

@Composable fun ReferenceAccountCreated(goal:Int,onNext:()->Unit) {
    val context=LocalContext.current
    val userId=CalFitApplication.instance.userPreferences.getAuthUserId()
    val prefs=remember(context){context.getSharedPreferences("reference_preferences",android.content.Context.MODE_PRIVATE)}
    var reminders by rememberSaveable{mutableStateOf(prefs.getBoolean("reminders_$userId",false))}
    var news by rememberSaveable{mutableStateOf(prefs.getBoolean("news_$userId",false))}
    Column(Modifier.fillMaxSize().background(Color.White)) {
        ReferenceHeader("Account Created")
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){
            Spacer(Modifier.height(40.dp));Text("Congratulations!",fontSize=22.sp)
            Text("Your custom plan is ready and you’re one step closer to your goal weight.",Modifier.padding(vertical=10.dp),color=ReferenceStyle.Muted,textAlign=TextAlign.Center,lineHeight=24.sp)
            Text("Your daily net goal is:",Modifier.padding(top=20.dp),fontWeight=FontWeight.Bold)
            Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text("%,d".format(goal),color=ReferenceStyle.Success,fontSize=24.sp);Spacer(Modifier.width(8.dp));Surface(shape=CircleShape,color=ReferenceStyle.Line){Text("Calories",Modifier.padding(16.dp,7.dp),fontWeight=FontWeight.Bold)}}
            HorizontalDivider(Modifier.padding(vertical=28.dp))
            Row(verticalAlignment=Alignment.CenterVertically){Checkbox(reminders,{reminders=it;prefs.edit().putBoolean("reminders_$userId",it).apply()});Text("Keep me on track with reminders",Modifier.weight(1f),fontSize=14.sp)}
            Row(verticalAlignment=Alignment.CenterVertically){Checkbox(news,{news=it;prefs.edit().putBoolean("news_$userId",it).apply()});Text("Send me the latest news, innovations and offers from kcalfit",Modifier.weight(1f),fontSize=14.sp)}
            if(reminders||news)Text("Preference saved. Delivery will be available when notification services are connected.",fontSize=12.sp,color=ReferenceStyle.Muted)
        }
        HorizontalDivider();Button(onNext,Modifier.fillMaxWidth().padding(16.dp).heightIn(min=48.dp),shape=CircleShape){Text("Next")}
    }
}
