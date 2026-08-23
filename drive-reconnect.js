// ============================================================
// GOOGLE DRIVE — reconexão automática após abrir/recarregar o app
// Carregado pelo Service Worker após o shell principal.
// ============================================================
(function autoReconnectDrive(){
  const tentar = () => {
    try{
      if(typeof driveState === 'undefined' || typeof tentarReconectarDrive !== 'function') return;
      if(!driveState.clientId || !driveState.fileId) return;
      if(driveState.accessToken) return;
      tentarReconectarDrive();
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
