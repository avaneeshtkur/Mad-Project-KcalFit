-- Atomic per-account budget; service-role only, never callable by the Android client.
create table if not exists public.coach_rate_limits (
  user_id uuid primary key references auth.users(id) on delete cascade,
  window_start timestamptz not null default now(),
  requests integer not null default 0
);
alter table public.coach_rate_limits enable row level security;
revoke all on public.coach_rate_limits from anon, authenticated;

create or replace function public.kcalfit_coach_allow(uid uuid) returns boolean
language plpgsql security definer set search_path = public as $$
declare used integer;
begin
  insert into public.coach_rate_limits as limits(user_id, window_start, requests)
  values (uid, clock_timestamp(), 1)
  on conflict (user_id) do update set
    requests = case when limits.window_start <= clock_timestamp() - interval '60 seconds' then 1 else limits.requests + 1 end,
    window_start = case when limits.window_start <= clock_timestamp() - interval '60 seconds' then clock_timestamp() else limits.window_start end
  returning requests into used;
  return used <= 10;
end;
$$;
revoke all on function public.kcalfit_coach_allow(uuid) from public, anon, authenticated;
grant execute on function public.kcalfit_coach_allow(uuid) to service_role;
