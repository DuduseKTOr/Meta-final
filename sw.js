// ============================================================
// SERVICE WORKER — Cockpit Motorista 2026
// Estratégia: cache-first para shell, network-first para fontes
// ============================================================

const CACHE_VERSION = 'motorista-v2.6.2';
const SHELL_CACHE = `${CACHE_VERSION}-shell`;
const RUNTIME_CACHE = `${CACHE_VERSION}-runtime`;

// Arquivos essenciais do app (app shell) — ficam disponíveis offline
const SHELL_FILES = [
  './',
  './index.html',
  './drive-reconnect.js',
  './manifest.json',
  './icon-192.png',
  './icon-512.png',
  './icon-maskable-192.png',
  './icon-maskable-512.png',
  './apple-touch-icon.png',
  './favicon-32.png'
];

// ============================================================
// INSTALL — baixa e armazena o shell
// ============================================================
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(SHELL_CACHE)
      .then((cache) => {
        console.log('[SW] Cache shell criado');
        return cache.addAll(SHELL_FILES);
      })
      .then(() => self.skipWaiting())
      .catch((err) => console.error('[SW] Erro no install:', err))
  );
});

// ============================================================
// ACTIVATE — limpa caches antigos
// ============================================================
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(
        keys
          .filter((key) => !key.startsWith(CACHE_VERSION))
          .map((key) => {
            console.log('[SW] Removendo cache antigo:', key);
            return caches.delete(key);
          })
      ))
      .then(() => self.clients.claim())
  );
});

// ============================================================
// FETCH — estratégias de cache
// ============================================================
self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // Ignora requisições que não são GET
  if (request.method !== 'GET') return;

  // Ignora extensões do navegador, chrome-extension, etc.
  if (!url.protocol.startsWith('http')) return;

  // Fontes do Google: stale-while-revalidate
  if (url.hostname.includes('fonts.googleapis.com') ||
      url.hostname.includes('fonts.gstatic.com')) {
    event.respondWith(staleWhileRevalidate(request));
    return;
  }

  // Mesma origem: cache-first com fallback de rede
  if (url.origin === self.location.origin) {
    event.respondWith(cacheFirst(request));
    return;
  }

  // Outras origens: tenta rede, cai pro cache
  event.respondWith(networkFirst(request));
});

// ============================================================
// ESTRATÉGIAS
// ============================================================

// Injeta o hook de reconexão apenas no HTML principal.
// Isso corrige instalações antigas que ainda têm um index.html
// em cache sem precisar alterar o arquivo monolítico do app.
async function injetarReconexaoDrive(response, request) {
  if(!response || !response.ok) return response;
  if(!request.headers.get('accept')?.includes('text/html')) return response;
  const path = new URL(request.url).pathname;
  if(!path.endsWith('/index.html') && path !== '/') return response;

  try{
    const html = await response.clone().text();
    if(html.includes('drive-reconnect.js')) return response;

    const atualizado = html.replace(
      /<\/body>/i,
      '<script src="./drive-reconnect.js" defer><\/script>\n</body>'
    );

    const headers = new Headers(response.headers);
    // O corpo foi reconstruído, então não podemos reutilizar metadados
    // de compressão/tamanho do payload original.
    headers.delete('content-length');
    headers.delete('content-encoding');
    headers.delete('etag');

    return new Response(atualizado, {
      status: response.status,
      statusText: response.statusText,
      headers
    });
  }catch(e){
    console.warn('[SW] Falha ao injetar reconexão do Drive:', e);
    return response;
  }
}

async function cacheFirst(request){
  const cached = await caches.match(request);
  if(cached){
    return injetarReconexaoDrive(cached, request);
  }

  try{
    const fresh = await fetch(request);
    if(fresh.ok){
      const cache = await caches.open(SHELL_CACHE);
      await cache.put(request, fresh.clone());
    }
    return injetarReconexaoDrive(fresh, request);
  }catch(e){
    return cached || new Response('Offline', {status:503});
  }
}

async function networkFirst(request){
  try{
    const fresh = await fetch(request);
    if(fresh.ok){
      const cache = await caches.open(RUNTIME_CACHE);
      await cache.put(request, fresh.clone());
    }
    return fresh;
  }catch(e){
    const cached = await caches.match(request);
    return cached || new Response('Offline', {status:503});
  }
}

async function staleWhileRevalidate(request){
  const cached = await caches.match(request);
  const update = fetch(request).then((fresh) => {
    if(fresh.ok){
      return caches.open(RUNTIME_CACHE).then((cache) => cache.put(request, fresh.clone())).then(() => fresh);
    }
    return fresh;
  }).catch(() => null);
  return cached || (await update) || new Response('', {status:503});
}
