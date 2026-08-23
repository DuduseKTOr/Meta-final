// ============================================================
// GOOGLE DRIVE — reconexão automática após abrir/recarregar o app
// Carregado pelo Service Worker após o shell principal.
//
// Melhoria: guarda a conta Google usada no Drive e envia login_hint
// nas próximas autorizações. Assim, quando o Google já conhece a
// sessão, a seleção de contas pode ser pulada automaticamente.
// ============================================================
(function autoReconnectDrive(){
  const EMAIL_KEY = 'driveAccountEmail';

  async function lembrarConta(accessToken){
    try{
      if(!accessToken) return;
      const resp = await fetch('https://www.googleapis.com/drive/v3/about?fields=user(emailAddress)', {
        headers: { Authorization: `Bearer ${accessToken}` }
      });
      if(!resp.ok) return;
      const data = await resp.json();
      const email = data?.user?.emailAddress;
      if(email) localStorage.setItem(EMAIL_KEY, email);
    }catch(e){
      console.warn('[Drive] não foi possível guardar a conta:', e);
    }
  }

  function instalarLoginHint(){
    try{
      const oauth2 = window.google?.accounts?.oauth2;
      if(!oauth2?.initTokenClient) return false;
      if(oauth2.initTokenClient.__cockpitLoginHintPatched) return true;

      const originalInit = oauth2.initTokenClient.bind(oauth2);
      const patchedInit = function(config){
        const originalCallback = config.callback;
        const wrappedConfig = {
          ...config,
          callback: (response) => {
            if(response?.access_token) void lembrarConta(response.access_token);
            return originalCallback?.(response);
          }
        };

        const client = originalInit(wrappedConfig);
        if(client && !client.__cockpitLoginHintPatched){
          const originalRequest = client.requestAccessToken.bind(client);
          client.requestAccessToken = (overrideConfig = {}) => {
            const email = localStorage.getItem(EMAIL_KEY);
            const nextConfig = { ...overrideConfig };
            if(email && !nextConfig.login_hint) nextConfig.login_hint = email;
            return originalRequest(nextConfig);
          };
          client.__cockpitLoginHintPatched = true;
        }
        return client;
      };

      patchedInit.__cockpitLoginHintPatched = true;
      oauth2.initTokenClient = patchedInit;
      return true;
    }catch(e){
      console.warn('[Drive] login_hint indisponível:', e);
      return false;
    }
  }

  const tentar = async () => {
    try{
      if(typeof driveState === 'undefined' || typeof tentarReconectarDrive !== 'function') return;
      if(!driveState.clientId || !driveState.fileId) return;
      if(driveState.accessToken) return;

      if(typeof carregarGIS === 'function') await carregarGIS();
      instalarLoginHint();
      await tentarReconectarDrive();
    }catch(e){
      console.warn('[Drive] reconexão automática indisponível:', e);
    }
  };

  if(document.readyState === 'loading'){
    document.addEventListener('DOMContentLoaded', () => setTimeout(tentar, 0), {once:true});
  }else{
    setTimeout(tentar, 0);
  }
})();
