# Capri AI Android 0.2.0 (experimental)

Projeto Android independente dentro do repositório USB-Screen-Cast. Compatível a partir do Android 10, com builds ARMv7 e ARM64.

## Implementado nesta primeira versão
- Interface de conversa em português.
- Prompt de personalidade inspirado na Capri (Capricorn).
- Respostas locais de demonstração sem chave.
- Integração opcional com OpenAI Chat Completions (modelo gpt-4o-mini); a chave é inserida pelo usuário e guardada nas preferências privadas do app.
- Leitura das respostas com TextToSpeech do Android.
- Entrada por reconhecimento de voz do Android.
- Painel flutuante translúcido e móvel, com campo de texto, enviar, abrir app, ler tela e fechar.\n- Captura pontual da tela usando MediaProjection, sempre após confirmação explícita do Android; a imagem é enviada à API configurada para análise.

## Limites importantes
- O painel flutuante envia a mensagem para a conversa principal; não é uma personagem animada.
- Conversas generativas exigem internet e uma chave de API válida. Sem chave, o app usa respostas locais simples.
- O ícone atual é um símbolo neon original do app, não um retrato de Capri. Não encontrei uma imagem oficial com licença pública de reutilização; por isso, não copiei arte protegida para o APK. A imagem oficial de apresentação está no perfil @edgerunners e pertence aos titulares. Esta versão não usa arte gerada por IA.\n- A leitura da tela é manual, uma captura por solicitação; telas protegidas podem ficar pretas ou inacessíveis. A captura é enviada à API OpenAI usando a chave configurada.

## Build
Abra a pasta CapriAI no Android Studio ou execute gradle assembleDebug.
O workflow GitHub Actions gera o APK debug para armeabi-v7a e arm64-v8a em cada push que altera CapriAI.
