package com.kcalfit.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.ui.nakafit.*

@Composable
fun BarcodeLookupScreen(onLookup: (String, (FoodItemEntity?, String?) -> Unit) -> Unit,
    onLogFood: (FoodItemEntity) -> Unit, onManualAdd: () -> Unit, onBack: () -> Unit, onScanImage: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<FoodItemEntity?>(null) }
    var searched by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var scanning by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scanner = remember(context) { GmsBarcodeScanning.getClient(context, GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E, Barcode.FORMAT_CODE_128, Barcode.FORMAT_ITF)
        .enableAutoZoom().build()) }
    fun lookup(value: String) {
        code=value;loading=true;searched=false;result=null;error=null
        onLookup(value) { match, failure -> loading=false;if(code==value){result=match;searched=failure==null;error=failure} }
    }
    NakaPage("Scan food",onBack) {
        Spacer(Modifier.height(20.dp))
        Icon(Icons.Default.QrCodeScanner,null,Modifier.size(110.dp),tint=Naka.Violet)
        Text("Scan a barcode or food photo",style=MaterialTheme.typography.headlineSmall)
        Text("Find packaged-food nutrition, or upload a meal photo for AI analysis. Review your portion and meal before saving.",color=Naka.Muted)
        NakaButton(if(scanning) "Opening scanner…" else "Scan barcode", {
            scanning=true;error=null
            scanner.startScan().addOnSuccessListener { barcode ->
                scanning=false
                val value=barcode.rawValue.orEmpty()
                if(value.length in 8..18 && value.all(Char::isDigit)) lookup(value)
                else error="No food barcode detected. Try again or enter the number below."
            }.addOnCanceledListener { scanning=false }
                .addOnFailureListener { scanning=false;error="Camera scanner is unavailable. Update Google Play services, or enter the barcode below." }
        },enabled=!loading && !scanning)
        NakaButton("Scan or upload food photo",onScanImage,enabled=!loading && !scanning,outlined=true)
        OutlinedTextField(code,{ code=it.filter(Char::isDigit).take(18);searched=false;result=null },Modifier.fillMaxWidth(),
            label={ Text("Barcode number") },keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
            singleLine=true,shape=RoundedCornerShape(28.dp))
        NakaButton(if(loading) "Searching…" else "Find Food",{
            lookup(code)
        },enabled=code.length>=8 && !loading && !scanning)
        error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
        if(searched) {
            if(result==null) Text("No matching product found. Try a food photo or add the label values manually.",color=Naka.Muted)
            else {
                Text(result!!.name,style=MaterialTheme.typography.titleLarge)
                Text("${result!!.calories} kcal per ${result!!.servingSize} ${result!!.servingUnit}",color=Naka.Muted)
                NakaButton("Choose Quantity",{ onLogFood(result!!) })
            }
        }
        NakaButton("Add Food Manually",onManualAdd,outlined=true)
    }
}
