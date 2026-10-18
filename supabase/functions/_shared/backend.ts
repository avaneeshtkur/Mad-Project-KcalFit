import { createClient } from 'npm:@supabase/supabase-js@2';
export const admin = () => createClient(Deno.env.get('SUPABASE_URL')!, Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!, { auth: { persistSession: false } });
export const json = (value: unknown, status = 200) => new Response(JSON.stringify(value), { status, headers: { 'Content-Type': 'application/json' } });
export async function user(req: Request) {
  const token = req.headers.get('Authorization')?.replace(/^Bearer /, '');
  if (!token) throw new Error('Sign in to continue.');
  const { data, error } = await admin().auth.getUser(token);
  if (error || !data.user) throw new Error('Sign in again to continue.');
  return data.user;
}
export async function body(req: Request, max = 4_500_000) {
  if (req.method !== 'POST') throw new Error('POST required');
  const reader = req.body?.getReader(); if (!reader) throw new Error('Body required');
  const chunks: Uint8Array[] = []; let length = 0;
  while (true) { const { done, value } = await reader.read(); if(done) break; length += value.length; if(length > max) { await reader.cancel(); throw new Error('Image is too large.'); } chunks.push(value); }
  const bytes = new Uint8Array(length); let offset = 0; for(const chunk of chunks) {bytes.set(chunk, offset);offset+=chunk.length;}
  return JSON.parse(new TextDecoder().decode(bytes));
}
