package com.kcalfit.app.ui.reference

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import com.android.billingclient.api.*
import com.kcalfit.app.BuildConfig
import com.kcalfit.app.CalFitApplication
import com.kcalfit.app.data.premium.*
import com.kcalfit.app.ui.nakafit.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.security.MessageDigest

@Composable fun PremiumSubscriptionScreen(onBack:()->Unit) {
    val context=LocalContext.current;val scope=rememberCoroutineScope();val api=remember{PremiumApi()}
    var status by remember{mutableStateOf("Checking your account…")};var benefits by remember{mutableStateOf<Entitlement?>(null)}
    var details by remember{mutableStateOf<ProductDetails?>(null)};var ready by remember{mutableStateOf(false)};var verifying by remember{mutableStateOf(false)}
    fun verify(purchases:List<Purchase>){scope.launch{
        verifying=true
        try {for(p in purchases.filter{BuildConfig.PLAY_PREMIUM_PRODUCT_ID in it.products}){
            if(p.purchaseState==Purchase.PurchaseState.PURCHASED)api.call("verify-play-purchase",JSONObject().put("purchase_token",p.purchaseToken))
            else if(p.purchaseState==Purchase.PurchaseState.PENDING)status="Payment is pending. Premium activates after server verification."
        };benefits=api.entitlement();status=if(benefits?.premium==true)"Premium is active" else "No active premium subscription"}
        catch(e:Exception){status=e.message?:"Purchase verification failed. Restore purchases to retry."}finally{verifying=false}
    }}
    val billing=remember {BillingClient.newBuilder(context).setListener{result,purchases->
        when(result.responseCode){BillingClient.BillingResponseCode.OK->verify(purchases.orEmpty());BillingClient.BillingResponseCode.USER_CANCELED->status="Purchase canceled";else->status="Purchase unavailable: ${result.debugMessage}"}
    }.enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).enableAutoServiceReconnection().build()}
    fun restore(){billing.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()){result,purchases->if(result.responseCode==BillingClient.BillingResponseCode.OK)verify(purchases) else status="Could not restore purchases: ${result.debugMessage}"}}
    DisposableEffect(billing){
        billing.startConnection(object:BillingClientStateListener{
            override fun onBillingServiceDisconnected(){ready=false;status="Google Play billing disconnected. Reopen this screen to retry."}
            override fun onBillingSetupFinished(result:BillingResult){ready=result.responseCode==BillingClient.BillingResponseCode.OK
                if(!ready){status="Google Play billing is unavailable on this installation.";return}
                if(BuildConfig.PLAY_PREMIUM_PRODUCT_ID.isBlank()){status="Subscriptions are not configured yet. Your manual diary remains available.";return}
                billing.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(listOf(QueryProductDetailsParams.Product.newBuilder().setProductId(BuildConfig.PLAY_PREMIUM_PRODUCT_ID).setProductType(BillingClient.ProductType.SUBS).build())).build()){response,products->
                    details=products.productDetailsList.firstOrNull();if(response.responseCode!=BillingClient.BillingResponseCode.OK || details==null)status="No subscription offer is available. Check your Play installation and product setup."
                };restore()
            }
        });onDispose{billing.endConnection()}
    }
    LaunchedEffect(Unit){runCatching{api.entitlement()}.onSuccess{benefits=it;status=if(it.premium)"Premium is active" else "${it.freeRemaining} free food scans remaining"}.onFailure{status=it.message?:"Account benefits unavailable"}}
    NakaPage("kcalfit Premium",onBack){
        KcalfitWordmark();Text("Food image scanning",style=MaterialTheme.typography.headlineMedium)
        Text("Five successful scans are free per account. Premium unlocks further scans. Manual food logging stays available.")
        Text(status)
        val product=details;val offer=product?.subscriptionOfferDetails?.firstOrNull{it.offerId==null}?:product?.subscriptionOfferDetails?.firstOrNull()
        if(product!=null && offer!=null){Text(product.title);Text(product.description);offer.pricingPhases.pricingPhaseList.forEach{Text("${it.formattedPrice} / ${it.billingPeriod}")}
            Text("Subscription renews according to the Google Play offer. Manage or cancel through Google Play.")
            NakaButton("Subscribe with Google Play",{
                val uid=CalFitApplication.instance.userPreferences.getAuthUserId();val account=MessageDigest.getInstance("SHA-256").digest(uid.toByteArray()).joinToString(""){"%02x".format(it)}
                val params=BillingFlowParams.newBuilder().setObfuscatedAccountId(account).setProductDetailsParamsList(listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product).setOfferToken(offer.offerToken).build())).build()
                val result=billing.launchBillingFlow(context as Activity,params);if(result.responseCode!=BillingClient.BillingResponseCode.OK)status="Could not open checkout: ${result.debugMessage}"
            },enabled=ready&&!verifying&&benefits?.premium!=true)
        }
        NakaButton("Restore purchases",{restore()},enabled=ready&&!verifying,outlined=true)
        NakaButton("Manage subscription",{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/account/subscriptions?package=${context.packageName}")))},outlined=true)
    }
}
