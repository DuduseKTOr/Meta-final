# Cockpit Scanner (laboratório Android)

Projeto Android isolado do PWA principal. O objetivo deste primeiro marco é visualizar, no próprio tablet, o texto que Uber, 99 e inDrive expõem à API de Acessibilidade do Android.

## Limites de segurança

- Somente observa eventos e textos acessíveis de `com.ubercab`, `com.taxis99` e `sinet.startup.inDriver`.
- Não chama `performAction`, `dispatchGesture`, cliques, toques, aceites, recusas ou qualquer controle de outro app.
- Mantém os dados apenas em memória durante a sessão; não grava nem envia capturas pela rede.
- Este laboratório não interpreta ainda uma oferta como corrida nem cria lançamentos no Cockpit. Essa será uma etapa posterior, depois de validarmos os dados reais expostos por cada plataforma.

## Abrir e compilar

1. Abra esta pasta (`android/cockpit-scanner`) no Android Studio Ladybug ou mais recente.
2. Instale o Android SDK Platform 35 quando o Android Studio solicitar.
3. Aguarde a sincronização do Gradle e selecione **Run > Run 'app'** em um dispositivo Android com API 26 ou superior.

Também é possível usar um Gradle local instalado:

```bash
gradle :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

O workflow do GitHub Actions também gera o artefato `cockpit-scanner-debug-apk` para facilitar a instalação no tablet após uma execução bem-sucedida.

## Teste manual no tablet

1. Instale e abra o **Cockpit Scanner**.
2. Toque em **ATIVAR SERVIÇO DE LEITURA**.
3. Em Acessibilidade, ative **Cockpit Scanner (somente leitura)** e confirme o aviso do Android.
4. Volte ao Scanner e confirme que o serviço está ativo.
5. Abra **somente uma plataforma por vez**: Uber, depois 99, depois inDrive.
6. Em cada plataforma, deixe aparecer uma tela de oferta ou resultado e navegue apenas como faria normalmente.
7. Retorne ao Scanner: os textos acessíveis e o tipo do evento aparecem na tela, identificados pela plataforma.
8. Use **LIMPAR** entre os testes para manter cada coleta separada.
9. Desative o serviço em Acessibilidade ao terminar.

## O que queremos descobrir

Para cada plataforma, registre se o Scanner conseguiu expor:

- valor da oferta ou do resultado;
- distância;
- duração/tempo estimado;
- origem/destino, quando houver;
- outros textos relevantes da tela.

Não é necessário aceitar uma corrida apenas para testar a leitura. Se uma oferta aparecer naturalmente durante o uso, ela já serve para o primeiro diagnóstico.

## Critérios deste marco

- A tela mostra qual plataforma e evento originaram a captura.
- Nenhuma automação de interface é executada.
- Texto ausente não gera uma entrada vazia.
- As capturas ficam somente em memória no aparelho durante a sessão.
- O teste deve ser feito com cada app instalado, porque o texto acessível varia por versão e dispositivo.
