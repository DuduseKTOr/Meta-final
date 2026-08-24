# Cockpit Scanner (laboratório Android)

Projeto Android isolado do PWA principal. Ele é um laboratório de diagnóstico para verificar se o Android entrega eventos de acessibilidade das versões de **motorista** de Uber, 99 e inDrive.

## Limites de segurança e privacidade

- Observa apenas eventos; nunca chama `performAction`, `dispatchGesture`, cliques, toques, aceites, recusas, intents para outros apps ou qualquer controle de interface.
- Os pacotes reconhecidos são: Uber Driver (`com.ubercab.driver`), 99 Motorista (`com.app99.driver`) e inDrive (`sinet.startup.inDriver`, usado pelos modos passageiro e motorista).
- Para qualquer aplicativo, a tela registra somente pacote, tipo e contagem de eventos. Isso permite descobrir um pacote inesperado sem ler seu conteúdo.
- Para os três apps reconhecidos, registra a disponibilidade da árvore e a quantidade de nós com texto visível. **Não armazena, grava ou envia textos, valores, endereços, nomes, números de telefone ou dados financeiros.**
- Todo diagnóstico fica apenas na memória durante a sessão. Limpar ou fechar o processo remove os dados.

## Diagnóstico da primeira execução

A configuração anterior limitava o serviço aos pacotes de passageiro da Uber e 99, fazendo com que o Android não encaminhasse eventos do Uber Driver nem do 99 Motorista. Agora a configuração recebe eventos de primeiro plano sem filtro de pacote, mas o app trata conteúdo somente para os três pacotes conhecidos e sempre em modo de contagem, sem retenção do texto.

A tela mostra:

- se o serviço está ativo;
- total de eventos e observações de apps reconhecidos;
- último pacote em primeiro plano;
- contagem por pacote, inclusive os ainda não reconhecidos;
- diagnóstico de árvore acessível para cada app de motorista reconhecido.

## Compilação e artefato

O workflow **Build Cockpit Scanner debug APK** monta `app-debug.apk` e publica o artefato `cockpit-scanner-debug-apk` por 14 dias em cada pull request ou execução manual.

Não testar ou instalar este APK em um dispositivo que ainda esteja em recuperação/validação de aplicativo financeiro. A validação em dispositivo só será retomada após o reset e a confirmação do acesso bancário, com uma versão do APK validada pelo CI.
