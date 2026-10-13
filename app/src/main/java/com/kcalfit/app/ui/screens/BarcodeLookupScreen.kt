package com.kcalfit.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kcalfit.app.data.model.FoodItemEntity
import com.kcalfit.app.ui.nakafit.*

@Composable
fun BarcodeLookupScreen(onLookup: (String, (FoodItemEntity?) -> Unit) -> Unit,
    onLogFood: (FoodItemEntity) -> Unit, onManualAdd: () -> Unit, onBack: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<FoodItemEntity?>(null) }
    var searched by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    NakaPage("Barcode Lookup",onBack) {
        Spacer(Modifier.height(20.dp))
        Icon(Icons.Default.QrCodeScanner,null,Modifier.size(110.dp),tint=Naka.Violet)
        Text("Enter the barcode printed on your food package",style=MaterialTheme.typography.headlineSmall)
        Text("Camera decoding is not connected. You can search the local catalogue by barcode number.",color=Naka.Muted)
        OutlinedTextField(code,{ code=it.filter(Char::isDigit).take(18);searched=false;result=null },Modifier.fillMaxWidth(),
            label={ Text("Barcode number") },keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
            singleLine=true,shape=RoundedCornerShape(28.dp))
        NakaButton(if(loading) "Searching…" else "Find Food",{
            loading=true;searched=false;result=null
            val requested=code
            onLookup(code) { match -> loading=false;if(code==requested) { result=match;searched=true } }
        },enabled=code.length>=8 && !loading)
        if(searched) {
            if(result==null) Text("No matching food in the local catalogue",color=Naka.Muted)
            else {
                Text(result!!.name,style=MaterialTheme.typography.titleLarge)
                Text("${result!!.calories} kcal per serving",color=Naka.Muted)
                NakaButton("Choose Quantity",{ onLogFood(result!!) })
            }
        }
        NakaButton("Add Food Manually",onManualAdd,outlined=true)
    }
}
