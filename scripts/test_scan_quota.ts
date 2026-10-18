// Runs the actual PostgreSQL migration in an isolated in-memory PostgreSQL engine.
// No production connection or user data is used.
import { PGlite } from 'npm:@electric-sql/pglite@0.3.14';
const db = new PGlite();
function assert(value:unknown,message:string){if(!value)throw new Error(message);}
await db.exec(`create schema auth; create table auth.users(id uuid primary key); create role anon; create role authenticated; create role service_role;`);
await db.exec(await Deno.readTextFile(new URL('../supabase/migrations/202610180001_scan_entitlements.sql',import.meta.url)));
const uid=crypto.randomUUID(), other=crypto.randomUUID();
await db.query('insert into auth.users values($1),($2)',[uid,other]);
async function reserve(id=crypto.randomUUID(),premium=false,user=uid){const r=await db.query<{slot:{lease?:string,limit?:boolean,busy?:boolean,cached?:unknown}}>('select kcalfit_reserve_scan($1,$2,$3) as slot',[user,id,premium]);return {id,...r.rows[0].slot};}
async function finish(r:{id:string,lease?:string},payload:unknown={foods:[{name:'rice',grams:100}]}){await db.query('select kcalfit_finish_scan($1,$2,$3,$4)',[uid,r.id,r.lease,payload==null?null:JSON.stringify(payload)]);}
let checks=0;
const first=await reserve();const second=await reserve();assert((await reserve()).busy,'Concurrency must be bounded');checks++;
await finish(first,null);const free=await db.query<{successful_free:number}>('select successful_free from kcalfit_scan_accounts where user_id=$1',[uid]);assert(free.rows[0].successful_free===0,'Failed scan must not consume quota');checks++;
await finish(second);await finish(second);const repeated=await reserve(second.id);assert(repeated.cached,'Successful retry must return cached result');checks++;
let rejected=false;try{await reserve(second.id,false,other);}catch{rejected=true}assert(rejected,'Cross-account idempotency keys must be rejected');checks++;
const expired=await reserve();await db.query("update kcalfit_scan_requests set created_at=now()-interval '3 minutes' where request_id=$1",[expired.id]);const replacement=await reserve(expired.id);assert(replacement.lease!==expired.lease,'Retry needs a new lease');checks++;
rejected=false;try{await finish(expired);}catch{rejected=true}assert(rejected,'Old attempt must not finalize a new reservation');checks++;
await finish(replacement);
for(let i=0;i<3;i++){const next=await reserve();assert(next.lease,'Quota should permit remaining successful scans');await finish(next);}
assert((await reserve()).limit,'Sixth successful free scan must be blocked');checks++;
const paid=await reserve(undefined,true);assert(paid.lease,'Verified premium must allow further scans');await finish(paid);checks++;
const count=await db.query<{successful_free:number}>('select successful_free from kcalfit_scan_accounts where user_id=$1',[uid]);assert(count.rows[0].successful_free===5,'Free count must stay exactly five after retries and premium');checks++;
assert((await reserve(undefined,false,other)).lease,'Accounts must have separate quotas');checks++;
const privileges=await db.query<{allowed:boolean}>("select has_function_privilege('authenticated','public.kcalfit_reserve_scan(uuid,uuid,boolean)','EXECUTE') as allowed");assert(!privileges.rows[0].allowed,'Client role must not self-grant premium or reserve directly');checks++;
console.log(`PASS: ${checks} backend quota/idempotency/ownership/lease/privilege assertions`);
await db.close();
