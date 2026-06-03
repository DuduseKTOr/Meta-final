// ============================================================
// SERVICE WORKER — Cockpit Motorista 2026
// Estratégia: cache-first para shell, network-first para fontes
// ============================================================

const CACHE_VERSION = 'motorista-v2.6.0';
const SHELL_CACHE = `${CACHE_VERSION}-shell`;
const RUNTIME_CACHE = `${CACHE_VERSION}-runtime`;

// Arquivos essenciais do app (app shell) — ficam disponíveis offline
const SHELL_FILES = [
  './',
  './index.html',
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

// Cache-first: serve do cache, atualiza em background
async function cacheFirst(request) {
  const cached = await caches.match(request);
  if (cached) {
    // Atualiza em background (não bloqueia)
    fetch(request).then((response) => {
      if (response && response.ok) {
        caches.open(SHELL_CACHE).then((c) => c.put(request, response));
      }
    }).catch(() => {});
    return cached;
  }
  // Não está no cache, busca da rede
  try {
    const response = await fetch(request);
    if (response && response.ok) {
      const cache = await caches.open(SHELL_CACHE);
      cache.put(request, response.clone());
    }
    return response;
  } catch (err) {
    // Sem rede e sem cache — retorna fallback para HTML
    if (request.headers.get('accept')?.includes('text/html')) {
      return caches.match('./index.html');
    }
    throw err;
  }
}

// Network-first: tenta rede primeiro, cai pro cache se falhar
async function networkFirst(request) {
  try {
    const response = await fetch(request);
    if (response && response.ok) {
      const cache = await caches.open(RUNTIME_CACHE);
      cache.put(request, response.clone());
    }
    return response;
  } catch (err) {
    const cached = await caches.match(request);
    if (cached) return cached;
    throw err;
  }
}

// Stale-while-revalidate: serve do cache e atualiza em paralelo
async function staleWhileRevalidate(request) {
  const cache = await caches.open(RUNTIME_CACHE);
  const cached = await cache.match(request);
  const fetchPromise = fetch(request).then((response) => {
    if (response && response.ok) cache.put(request, response.clone());
    return response;
  }).catch(() => cached);
  return cached || fetchPromise;
}

// ============================================================
// MENSAGENS — para atualização manual via página
// ============================================================
self.addEventListener('message', (event) => {
  if (event.data?.type === 'SKIP_WAITING') self.skipWaiting();
});
