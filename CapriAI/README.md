# Capri AI Android 0.6.0 (experimental mobile companion)

Adaptação Android independente inspirada no projeto Capri Companion para Windows. Compatível com Android 10 ou superior; builds ARMv7 e ARM64.

## O que esta versão faz
- Janela flutuante sobre outros aplicativos, arrastável e com balão de fala.
- Personagem com animação leve de flutuação, balanço e reação enquanto fala.
- Toque na personagem abre o chat principal.
- Conversa em português com personalidade inspirada na Capri.
- Integração opcional com OpenAI Chat Completions (gpt-4o-mini); a chave é inserida pelo usuário e guardada nas preferências privadas do app.
- Voz de saída pelo TextToSpeech do Android e entrada por reconhecimento de voz.
- Ações Android básicas: abrir Configurações, Câmera, YouTube, WhatsApp/Chrome (quando instalados), pesquisar na web, iniciar timer e guardar notas locais.
- Captura pontual da tela via MediaProjection, sempre com confirmação explícita do Android, para análise visual pela API configurada.
- Tela imersiva e campo de mensagem configurado para redimensionar acima do teclado.

## Como ativar a Capri flutuante
1. Abra o aplicativo e toque em **SOBREPOR**.
2. Autorize "Exibir sobre outros apps" nas configurações do Android.
3. Volte ao Capri AI. A personagem flutuante aparece e pode ser arrastada.
4. Toque na personagem para abrir a conversa; toque em **PARAR** para fechá-la.

## Comandos Android de exemplo
- `abre configurações`
- `abre câmera`
- `abre YouTube`
- `pesquise como configurar ADB`
- `timer 5 minutos`
- `nota: testar o novo modo flutuante`

## Limites e diferenças em relação ao Windows
- Este é um porte móvel experimental, não uma cópia integral do aplicativo Electron.
- O Android não oferece controle irrestrito de mouse/janelas como o Windows. Ações são executadas por intents e APIs permitidas pelo sistema, e podem exigir confirmação ou um aplicativo compatível.
- A personagem usa a arte de referência incluída no projeto e animações leves no overlay; ainda não tem o rig vetorial completo, catálogo de dezenas de gestos, teleporte, leitura contínua da tela ou controle autônomo do aparelho do desktop.
- A IA generativa exige internet e uma chave OpenAI válida. Sem chave, só há respostas locais simples e comandos Android reconhecidos.
- A voz é a voz instalada no Android, não uma clonagem da voz original da personagem.
- A captura da tela é manual, por solicitação, e telas protegidas podem aparecer pretas.

## Privacidade
A chave fica nas preferências privadas do aplicativo. Mensagens e imagens de captura são enviadas à OpenAI apenas quando você usa os recursos correspondentes. Notas ficam armazenadas localmente no aparelho.

## Build
Abra a pasta CapriAI no Android Studio ou execute `gradle assembleDebug`.
O workflow GitHub Actions gera o APK debug para armeabi-v7a e arm64-v8a em cada push que altera CapriAI.
