# Capri AI Android (experimental)

Projeto Android independente dentro do repositório USB-Screen-Cast. Compatível a partir do Android 10, com builds ARMv7 e ARM64.

## Implementado nesta primeira versão
- Interface de conversa em português.
- Prompt de personalidade inspirado na Capri (Capricorn).
- Respostas locais de demonstração sem chave.
- Integração opcional com OpenAI Chat Completions (modelo gpt-4o-mini); a chave é inserida pelo usuário e guardada nas preferências privadas do app.
- Leitura das respostas com TextToSpeech do Android.
- Entrada por reconhecimento de voz do Android.
- Bolha flutuante móvel via permissão Sobrepor a outros apps.

## Limites importantes
- A bolha abre a conversa completa; ainda não é uma personagem animada.
- Conversas generativas exigem internet e uma chave de API válida. Sem chave, o app usa respostas locais simples.
- A arte oficial de Capri não está empacotada nesta compilação: imagens promocionais são protegidas pelos titulares. Esta versão não usa arte gerada por IA. A arte oficial e os créditos devem ser integrados com autorização/licença antes de redistribuição.

## Build
Abra a pasta CapriAI no Android Studio ou execute gradle assembleDebug.
O workflow GitHub Actions gera o APK debug para armeabi-v7a e arm64-v8a em cada push que altera CapriAI.
