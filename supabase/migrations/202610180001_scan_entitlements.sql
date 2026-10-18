-- REVIEW AND APPLY MANUALLY. No deployment is performed by this change.
create table if not exists public.kcalfit_scan_accounts (
  user_id uuid primary key references auth.users(id) on delete cascade,
  successful_free integer not null default 0 check(successful_free between 0 and 5)
);
create table if not exists public.kcalfit_scan_requests (
  request_id uuid primary key,
  user_id uuid not null references auth.users(id) on delete cascade,
  status text not null check(status in ('reserved','succeeded','failed')),
  uses_free boolean not null,
  created_at timestamptz not null default now(),
  lease uuid not null default gen_random_uuid(),
  result jsonb
);
create index if not exists kcalfit_scan_user_status on public.kcalfit_scan_requests(user_id,status);
create table if not exists public.kcalfit_play_subscriptions (
  purchase_token text primary key,
  user_id uuid not null references auth.users(id) on delete cascade,
  product_id text not null,
  valid_until timestamptz not null,
  verified_at timestamptz not null default now()
);
alter table public.kcalfit_scan_accounts enable row level security;
alter table public.kcalfit_scan_requests enable row level security;
alter table public.kcalfit_play_subscriptions enable row level security;
revoke all on public.kcalfit_scan_accounts,public.kcalfit_scan_requests,public.kcalfit_play_subscriptions from anon,authenticated;
grant all on public.kcalfit_scan_accounts,public.kcalfit_scan_requests,public.kcalfit_play_subscriptions to service_role;

create or replace function public.kcalfit_reserve_scan(uid uuid,rid uuid,has_premium boolean) returns jsonb
language plpgsql security definer set search_path = public as $$
declare used integer; pending integer; prior public.kcalfit_scan_requests;
begin
 insert into kcalfit_scan_accounts(user_id) values(uid) on conflict do nothing;
 select successful_free into used from kcalfit_scan_accounts where user_id=uid for update;
 select * into prior from kcalfit_scan_requests where request_id=rid;
 if found then
   if prior.user_id<>uid then raise exception 'Request belongs to another account'; end if;
   if prior.status='succeeded' then return jsonb_build_object('cached',prior.result); end if;
   if prior.status='reserved' and prior.created_at>now()-interval '2 minutes' then return jsonb_build_object('busy',true); end if;
 end if;
 update kcalfit_scan_requests set status='failed' where user_id=uid and status='reserved' and created_at<=now()-interval '2 minutes';
 select count(*) into pending from kcalfit_scan_requests where user_id=uid and status='reserved' and uses_free;
 if not has_premium and used+pending>=5 then return jsonb_build_object('limit',true); end if;
 -- Also cap premium concurrency to avoid unlimited provider requests.
 if (select count(*) from kcalfit_scan_requests where user_id=uid and status='reserved')>=2 then return jsonb_build_object('busy',true); end if;
 insert into kcalfit_scan_requests(request_id,user_id,status,uses_free) values(rid,uid,'reserved',not has_premium)
 on conflict(request_id) do update set status='reserved',uses_free=not has_premium,created_at=now(),lease=gen_random_uuid(),result=null;
 return jsonb_build_object('reserved',true,'lease',(select lease from kcalfit_scan_requests where request_id=rid));
end $$;
create or replace function public.kcalfit_finish_scan(uid uuid,rid uuid,attempt uuid,payload jsonb) returns void
language plpgsql security definer set search_path = public as $$
declare reserved public.kcalfit_scan_requests;
begin
 perform 1 from kcalfit_scan_accounts where user_id=uid for update;
 select * into reserved from kcalfit_scan_requests where request_id=rid and user_id=uid for update;
 if reserved.request_id is null or reserved.lease<>attempt then raise exception 'Invalid reservation'; end if;
 if reserved.status='succeeded' then return; end if;
 if reserved.status<>'reserved' or reserved.created_at<=now()-interval '2 minutes' then raise exception 'Reservation expired'; end if;
 if payload is null then update kcalfit_scan_requests set status='failed' where request_id=rid; return; end if;
 if reserved.uses_free then update kcalfit_scan_accounts set successful_free=successful_free+1 where user_id=uid; end if;
 update kcalfit_scan_requests set status='succeeded',result=payload where request_id=rid;
end $$;
revoke all on function public.kcalfit_reserve_scan(uuid,uuid,boolean),public.kcalfit_finish_scan(uuid,uuid,uuid,jsonb) from public,anon,authenticated;
grant execute on function public.kcalfit_reserve_scan(uuid,uuid,boolean),public.kcalfit_finish_scan(uuid,uuid,uuid,jsonb) to service_role;
