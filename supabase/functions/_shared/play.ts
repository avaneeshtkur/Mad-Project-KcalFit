import { importPKCS8, SignJWT } from 'npm:jose@5';
import { admin } from './backend.ts';
const packageName = 'com.kcalfit.app';
async function accessToken() {
  const raw = Deno.env.get('GOOGLE_PLAY_SERVICE_ACCOUNT'); if(!raw) throw new Error('Subscription verification is not configured.');
  const account = JSON.parse(raw); const key = await importPKCS8(account.private_key, 'RS256');
  const assertion = await new SignJWT({ scope: 'https://www.googleapis.com/auth/androidpublisher' }).setProtectedHeader({ alg:'RS256' }).setIssuer(account.client_email).setAudience('https://oauth2.googleapis.com/token').setIssuedAt().setExpirationTime('5m').sign(key);
  const response = await fetch('https://oauth2.googleapis.com/token', { method:'POST', body:new URLSearchParams({grant_type:'urn:ietf:params:oauth:grant-type:jwt-bearer',assertion}),signal:AbortSignal.timeout(15000) });
  if(!response.ok) throw new Error('Subscription verification unavailable.'); return (await response.json()).access_token;
}
export async function verifyPurchase(uid:string, token:string) {
  const access = await accessToken();
  const endpoint = `https://androidpublisher.googleapis.com/androidpublisher/v3/applications/${packageName}/purchases/subscriptionsv2/tokens/${encodeURIComponent(token)}`;
  const response = await fetch(endpoint,{headers:{Authorization:`Bearer ${access}`},signal:AbortSignal.timeout(15000)});
  if(!response.ok) throw new Error('Purchase could not be verified.');
  const purchase = await response.json();
  const accountHash = Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256',new TextEncoder().encode(uid)))).map(b=>b.toString(16).padStart(2,'0')).join('');
  if(purchase.externalAccountIdentifiers?.obfuscatedExternalAccountId!==accountHash) throw new Error('Purchase is not associated with this account.');
  const product = Deno.env.get('PLAY_PREMIUM_PRODUCT_ID');
  const line = purchase.lineItems?.find((item:{productId:string})=>item.productId===product);
  if(!line) throw new Error('Unknown subscription product.');
  const valid = ['SUBSCRIPTION_STATE_ACTIVE','SUBSCRIPTION_STATE_IN_GRACE_PERIOD','SUBSCRIPTION_STATE_CANCELED'].includes(purchase.subscriptionState) && Date.parse(line.expiryTime)>Date.now();
  const db=admin();const {data:existing,error:readError}=await db.from('kcalfit_play_subscriptions').select('user_id').eq('purchase_token',token).maybeSingle();if(readError)throw readError;
  if(existing && existing.user_id!==uid) throw new Error('Purchase already belongs to another account.');
  if(valid && purchase.acknowledgementState==='ACKNOWLEDGEMENT_STATE_PENDING') {
    const ack=await fetch(`https://androidpublisher.googleapis.com/androidpublisher/v3/applications/${packageName}/purchases/subscriptions/${encodeURIComponent(product!)}/tokens/${encodeURIComponent(token)}:acknowledge`,{method:'POST',headers:{Authorization:`Bearer ${access}`,'Content-Type':'application/json'},body:'{}',signal:AbortSignal.timeout(15000)});
    if(!ack.ok)throw new Error('Purchase acknowledgement is pending. Restore purchases to retry.');
  }
  const {error}=await db.from('kcalfit_play_subscriptions').upsert({purchase_token:token,user_id:uid,product_id:product,valid_until:valid?line.expiryTime:new Date(0).toISOString(),verified_at:new Date().toISOString()});if(error)throw error;
  return valid;
}
export async function premium(uid:string) {
  const {data,error}=await admin().from('kcalfit_play_subscriptions').select('purchase_token').eq('user_id',uid).gt('valid_until',new Date().toISOString());if(error)throw error;
  for(const row of data??[]) if(await verifyPurchase(uid,row.purchase_token))return true;
  return false;
}
