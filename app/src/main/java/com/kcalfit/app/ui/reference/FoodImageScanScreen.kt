package com.kcalfit.app.ui.reference

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.kcalfit.app.CalFitApplication
import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.data.premium.*
import com.kcalfit.app.ui.nakafit.*
import kotlinx.coroutines.*
import java.io.*
import java.util.UUID
import kotlin.math.roundToInt

@Composable fun FoodImageScanScreen(onBack:()->Unit,onSelect:(FoodItemEntity)->Unit,onPremium:()->Unit) {
    val context=LocalContext.current;val scope=rememberCoroutineScope();val api=remember{PremiumApi()}
    var photo by rememberSaveable{mutableStateOf("")};var cameraUri by rememberSaveable{mutableStateOf("")};var requestId by rememberSaveable{mutableStateOf(UUID.randomUUID().toString())}
    var bitmap by remember{mutableStateOf<Bitmap?>(null)};var busy by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)}
    var benefits by remember{mutableStateOf<Entitlement?>(null)};var candidates by remember{mutableStateOf<List<Pair<ScanIngredient,List<FoodItemEntity>>>>(emptyList())}
    var consent by rememberSaveable{mutableStateOf(false)}
    LaunchedEffect(Unit){runCatching{api.entitlement()}.onSuccess{benefits=it}}
    LaunchedEffect(photo){if(photo.isNotBlank())runCatching{withContext(Dispatchers.IO){decodeMealImage(context,Uri.parse(photo))}}.onSuccess{bitmap=it}.onFailure{error="Could not read this image. Choose another photo."}}
    fun chosen(uri:Uri){photo=uri.toString();requestId=UUID.randomUUID().toString();candidates=emptyList();error=null}
    val gallery=rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()){it?.let{uri->chosen(uri)}}
    val camera=rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()){success->if(success)chosen(Uri.parse(cameraUri))}
    fun capture(){val file=File.createTempFile("meal-",".jpg",context.cacheDir);val uri=FileProvider.getUriForFile(context,"${context.packageName}.meal-images",file);cameraUri=uri.toString();camera.launch(uri)}
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){if(it)capture() else error="Camera access was declined. You can choose a gallery photo."}
    NakaPage("Scan food",onBack){
        Text("${benefits?.let{if(it.premium)"Premium scanning" else "${it.freeRemaining} of 5 free scans remaining"}?:"Scan allowance is verified by your account’s server."}")
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
            OutlinedButton({if(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)capture() else permission.launch(Manifest.permission.CAMERA)},enabled=!busy,modifier=Modifier.weight(1f)){Text("Camera")}
            OutlinedButton({gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))},enabled=!busy,modifier=Modifier.weight(1f)){Text("Gallery")}
        }
        bitmap?.let{Image(it.asImageBitmap(),"Selected meal photo",Modifier.fillMaxWidth().height(220.dp))}
        Row(verticalAlignment=Alignment.CenterVertically){Checkbox(consent,{consent=it});Text("Send this photo for AI food identification. Portions are estimates; review before saving.",Modifier.weight(1f))}
        NakaButton(if(busy)"Analyzing…" else "Analyze meal",{
            val selected=bitmap?:return@NakaButton;busy=true;error=null
            scope.launch{try{
                val encoded=withContext(Dispatchers.IO){ByteArrayOutputStream().use{selected.compress(Bitmap.CompressFormat.JPEG,85,it);Base64.encodeToString(it.toByteArray(),Base64.NO_WRAP)}}
                val foods=api.scan(encoded,requestId)
                candidates=foods.map{food->val source=CalFitApplication.instance.repository.foodSearch;val local=source.local(food.name).foods;food to (if(local.isNotEmpty())local else runCatching{source.refresh(food.name).foods}.getOrDefault(emptyList())).take(5)}
                runCatching{api.entitlement()}.onSuccess{benefits=it}
            }catch(e:Exception){if(e is CancellationException)throw e;error=e.message?:"Could not analyze this meal"}finally{busy=false}}
        },enabled=bitmap!=null && consent && !busy)
        if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
        error?.let{Text(it,color=MaterialTheme.colorScheme.error)}
        candidates.forEach{(food,matches)->
            Text("${food.name} · estimated ${food.grams.roundToInt()} g",style=MaterialTheme.typography.titleMedium)
            if(matches.isEmpty())Text("No database match found. Use manual logging to enter verified nutrition.")
            matches.forEach{match->NakaRow(match.name,"${match.calories} kcal per ${match.servingSize} ${match.servingUnit} · Review portion",{
                // Only scale when the catalogue basis is grams. Other bases require manual portion review.
                val ratio=if(match.servingUnit.equals("g",true) && match.servingSize>0)food.grams/match.servingSize else 1.0
                onSelect(match.copy(servingSize=match.servingSize*ratio,calories=(match.calories*ratio).roundToInt(),proteinGrams=match.proteinGrams*ratio,carbsGrams=match.carbsGrams*ratio,fatGrams=match.fatGrams*ratio,fiberGrams=match.fiberGrams*ratio,sugarGrams=match.sugarGrams*ratio,sodiumMg=match.sodiumMg*ratio))
            })}
        }
        if(candidates.isNotEmpty())Text("Choose a matching food to edit and confirm it in Add Food. Scan again or return here to log other items; nothing is saved automatically.")
        NakaButton("Premium and scan allowance",onPremium,outlined=true)
        TextButton(onBack){Text("Continue with manual food logging")}
    }
}
private fun decodeMealImage(context:android.content.Context,uri:Uri):Bitmap {
    val resolver=context.contentResolver
    val options=BitmapFactory.Options().apply{inJustDecodeBounds=true}
    resolver.openInputStream(uri).use{BitmapFactory.decodeStream(it,null,options)}
    require(options.outWidth>0 && options.outHeight>0)
    var sample=1;while(options.outWidth/sample>1280 || options.outHeight/sample>1280)sample*=2
    val bitmap=resolver.openInputStream(uri).use{BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply{inSampleSize=sample})}?:error("Invalid image")
    val orientation=resolver.openInputStream(uri).use{if(it==null)1 else ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION,1)}
    val matrix=Matrix().apply{when(orientation){2->setScale(-1f,1f);3->setRotate(180f);4->setScale(1f,-1f);5->{setRotate(90f);postScale(-1f,1f)};6->setRotate(90f);7->{setRotate(-90f);postScale(-1f,1f)};8->setRotate(-90f)}}
    return Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix,true)
}
