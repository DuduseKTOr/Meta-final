# Cockpit Scanner (laboratório Android)

Projeto Android isolado do PWA principal. O objetivo deste primeiro marco é visualizar, no próprio tablet, o texto que Uber, 99 e inDrive expõem à API de Acessibilidade do Android.

## Limites de segurança

- Somente observa eventos e textos acessíveis de `com.ubercab`, `com.taxis99` e `sinet.startup.inDriver`.
- Não chama `performAction`, `dispatchGesture`, cliques, toques, aceites, recusas ou qualquer controle de outro app.
- Mantém os dados apenas em memória durante a sessão; não grava nem envia capturas pela rede.

## Abrir e compilar

1. Abra esta pasta (`android/cockpit-scanner`) no Android Studio Ladybug ou mais recente.
2. Instale o Android SDK Platform 35 quando o Android Studio solicitar.
3. Aguarde a sincronização do Gradle e selecione **Run > Run 'app'** em um dispositivo Android com API 26 ou superior.

Também é possível usar um Gradle local instalado:

```bash
gradle :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Teste manual no tablet

1. Abra o **Cockpit Scanner** e toque em **ATIVAR SERVIÇO DE LEITURA**.
2. Em Acessibilidade, ative **Cockpit Scanner (somente leitura)** e confirme o aviso do Android.
3. Volte ao app, abra Uber, 99 ou inDrive e navegue apenas como faria normalmente.
4. Retorne ao Scanner: os textos acessíveis e o tipo do evento aparecem na tela.
5. Use **LIMPAR** para apagar o buffer da sessão. Desative o serviço em Acessibilidade para encerrar a observação.

## Critérios deste marco

- A tela mostra qual plataforma e evento originaram a captura.
- Nenhuma automação de interface é executada.
- Texto ausente não gera uma entrada vazia.
- O teste deve ser feito com cada app instalado, porque o texto acessível varia por versão e dispositivo.
