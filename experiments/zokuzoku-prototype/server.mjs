import http from 'node:http';
import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { createMockSvg, checkPrompt } from './shared.mjs';
const ROOT = path.dirname(fileURLToPath(import.meta.url));
const PORT = Number(process.env.PORT || 4173);
const IS_REAL = Boolean(process.env.OPENAI_API_KEY);
const SIZES = { '.html': 'text/html; charset=utf-8', '.css': 'text/css; charset=utf-8', '.js':'text/javascript; charset=utf-8', '.mjs':'text/javascript; charset=utf-8', '.svg':'image/svg+xml' };
const recent = new Map();
function send(res, status, data, type='application/json; charset=utf-8') {
 res.writeHead(status, {'Content-Type':type, 'Cache-Control':'no-store','X-Content-Type-Options':'nosniff','Content-Security-Policy':"default-src 'self' data: blob:; img-src 'self' data: blob:; style-src 'self' 'unsafe-inline'; script-src 'self'; connect-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'", 'Referrer-Policy':'no-referrer'});
 res.end(type.includes('json') ? JSON.stringify(data) : data);
}
async function readJson(req) {
 let body='';
 for await (const ch of req) {body += ch.toString(); if(body.length>16000) throw Object.assign(new Error('入力が大きすぎます'),{status:413});}
 try{return JSON.parse(body)}catch{throw Object.assign(new Error('JSON形式を確認してください'),{status:400})}
}
async function generate(req,res) {
 const ip = req.socket.remoteAddress || 'unknown';
 const now=Date.now(); const slots=(recent.get(ip)||[]).filter(t=>now-t<60000);
 if(slots.length>=5) return send(res,429,{error:'生成回数の上限です。少し時間を置いてください。'});
 slots.push(now);recent.set(ip,slots);
 try {
  const body=await readJson(req);
  const prompt=String(body.prompt||'').trim().slice(0,700);
  const kind=['acrylic','keychain','tote','sticker'].includes(body.kind)?body.kind:'acrylic';
  const style=['soft','retro','minimal','pop'].includes(body.style)?body.style:'soft';
  const result=checkPrompt(prompt);
  if(!result.ok)return send(res,400,{error:result.reason});
  const prefix='Licensed or original fictional animal/mascot goods only. No logos or recognizable existing characters. Generate a single realistic product mockup on a neutral studio background. Product type: '+kind+'. Art direction: '+style+'. Product concept: ';
  if(!IS_REAL) return send(res,200,{mode:'demo',image:createMockSvg(prompt,kind,style),message:'テンプレート画像で体験を再現しています。実際の生成AIは未接続です。'});
  const upstream=await fetch('https://api.openai.com/v1/images/generations', {method:'POST',headers:{'Authorization':`Bearer ${process.env.OPENAI_API_KEY}`,'Content-Type':'application/json'},body:JSON.stringify({model:process.env.OPENAI_IMAGE_MODEL||'gpt-image-1',size:'1024x1024',quality:'low',n:1,prompt:prefix+prompt}),signal:AbortSignal.timeout(95000)});
  const json=await upstream.json();
  if(!upstream.ok)return send(res,502,{error:'画像生成サービスが利用できません。設定・利用枠を確認してください。'});
  const b64=json.data?.[0]?.b64_json;
  if(!b64)return send(res,502,{error:'画像データが返されませんでした。'});
  return send(res,200,{mode:'ai',image:'data:image/png;base64,'+b64,message:'生成AIによる画像です。試作・権利審査前のイメージです。'});
 }catch(err) {return send(res,err.status||500,{error:err.status?err.message:'生成に失敗しました。ネットワークや設定をご確認ください。'});}
}
const server=http.createServer(async(req,res)=>{
 const url = new URL(req.url||'/', 'http://localhost');
 if(req.method==='GET' && url.pathname==='/api/status') return send(res,200,{mode: IS_REAL?'ai':'demo',imageModel:IS_REAL?(process.env.OPENAI_IMAGE_MODEL||'gpt-image-1'):null});
 if(req.method==='POST' && url.pathname==='/api/generate') {
  const origin=req.headers.origin;
  const host=req.headers.host;
  if(origin && new URL(origin).host!==host) return send(res,403,{error:'不正なリクエスト'});
  return generate(req,res);
 }
 if(req.method!=='GET'&&req.method!=='HEAD')return send(res,405,{error:'Method Not Allowed'});
 let requested=url.pathname==='/'?'index.html':url.pathname.slice(1);
 if(!['index.html','app.css','app.js','shared.mjs'].includes(requested))return send(res,404,{error:'Not found'});
 try{const bytes=await fs.readFile(path.join(ROOT,requested));res.writeHead(200,{'Content-Type':SIZES[path.extname(requested)],'X-Content-Type-Options':'nosniff','Cache-Control':'no-cache'});res.end(req.method==='HEAD'?undefined:bytes)}catch{return send(res,404,{error:'Not found'})}
});
if(process.env.NODE_ENV!=='test')server.listen(PORT,()=>console.log(`ZOKUZOKU → http://localhost:${PORT} (${IS_REAL?'AI':'demo'} mode)`));
export {server};