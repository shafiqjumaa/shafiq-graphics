const CACHE='playhub-v2.6-fixed-game-paths';
const LOCAL=['./','./index.html','./games/tower.html','./games/ships.html','./games/xo.html','./games/numbers.html','./games/race.html','./games/paper.html','./games/conquest.html'];
self.addEventListener('install',event=>{event.waitUntil(caches.open(CACHE).then(c=>c.addAll(LOCAL).catch(()=>{})));self.skipWaiting()});
self.addEventListener('activate',event=>{event.waitUntil(caches.keys().then(names=>Promise.all(names.filter(n=>n.startsWith('playhub-')&&n!==CACHE).map(n=>caches.delete(n)))));self.clients.claim()});
self.addEventListener('fetch',event=>{
 const request=event.request;if(request.method!=='GET'||new URL(request.url).origin!==location.origin)return;
 event.respondWith(fetch(request,{cache:'no-store'}).then(response=>{if(response.ok){const copy=response.clone();caches.open(CACHE).then(cache=>cache.put(request,copy)).catch(()=>{});}return response;}).catch(()=>caches.match(request)));
});