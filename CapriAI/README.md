# Capri AI Android 0.9.2 — versão de lançamento

O APK publicado na release `capri-v0.9.2` é gerado exclusivamente pelo módulo `CapriAI`.

Adaptação Android independente e experimental da personagem Capri. Compatível com Android 10 ou superior e builds ARMv7/ARM64.

## Recursos nesta versão

- Janela flutuante arrastável com personagem e balão de fala.
- Personagem na tela principal, com animação leve.
- Expressões faciais básicas (feliz, triste, surpresa, irritada, curiosa e neutra), escolhidas por palavras/contexto da resposta.
- Sincronização do humor da conversa com a personagem flutuante.
- Interface e configurações em português brasileiro.
- Conversa com API compatível com OpenAI; a chave é inserida pelo usuário.
- **A chave da API aparece em texto visível nas configurações**, sem ser substituída por pontos. O botão **TESTAR CONEXÃO DA API** valida a chave antes de iniciar a conversa.
- Voz de saída pelo TextToSpeech do Android e entrada por reconhecimento de voz.
- Comandos Android básicos, como abrir configurações/câmera, pesquisar, timer e notas locais.
- Captura pontual de tela via MediaProjection, sempre com confirmação explícita do Android.

## Ativar a Capri flutuante

1. Abra o aplicativo e toque em **SOBREPOR**.
2. Autorize **Exibir sobre outros apps** nas configurações do Android.
3. Volte ao Capri AI.
4. Arraste a personagem para posicioná-la; toque nela para abrir o chat.
5. Use **PARAR** para encerrar a sobreposição.

## Configurar a API

A chave é fornecida manualmente pelo usuário nas configurações e permanece visível no campo para facilitar a conferência. A assinatura do ChatGPT não inclui créditos da API. Erros HTTP 401 normalmente indicam chave inválida/revogada, projeto sem autorização ou configuração incorreta.

O aplicativo não pode obter automaticamente uma chave privada de outro app por meio de uma permissão Android. Não coloque chaves em repositórios públicos.

## Limites conhecidos

- Esta é uma adaptação móvel experimental, não paridade completa com o Capri Companion para Windows.
- Expressões usam heurísticas simples sobre o texto; não são reconhecimento emocional avançado.
- A voz depende do mecanismo TTS instalado no Android.
- Captura de tela é iniciada pelo usuário e autorizada pelo sistema; telas protegidas podem aparecer pretas.
- Android limita o controle de outros apps; ações dependem de intents e permissões disponíveis.

## Compilar

```bash
gradle assembleDebug
```

O workflow `.github/workflows/capri-ai-android.yml` gera o APK debug para ARMv7 e ARM64.

## Histórico

### Versão 0.9.0
- Inicialização padrão no modo Offline, sem exigir chave de API.
- O modo Offline oferece respostas locais básicas e comandos Android; não equivale a um modelo generativo completo.
- Sobreposição reposicionável por arraste e movimento livre automático, com deslocamento suave pela tela e posição salva entre sessões.
- Teleporte visual com desaparecimento e reaparecimento em outro ponto da tela.
- Mudança automática entre poses e modo rosto/corpo; movimento, teleporte e metamorfose podem ser desligados nas configurações.
- Teste de conexão da API diretamente nas configurações e mensagens de erro HTTP mais específicas.
- Se uma chave estiver salva mas o provedor estiver Offline, as configurações passam automaticamente para OpenAI-compatible ao salvar.
- Toque na personagem sobreposta alterna entre corpo completo e modo rosto minimalista com faixa serrilhada, olhos e boca.
- Poses disponíveis nas configurações: sentada, deitada, agachada e de costas.
- Ícone do aplicativo usa o retrato da Capri.
- Interface segue o estilo de terminal neon da personagem.

### Versão 0.9.2
- Campo da chave API configurado para mostrar letras e números em texto normal, sem pontos.


## 0.9.11 development update

- Offline arcade: reflex, memory sequence, and tic-tac-toe against Capri.
- Android controls hub: media/call/alarm volume, brightness after Android's special user approval, and shortcuts to official connectivity, display, battery, accessibility, and app-management panels.
- Neural speech: Gemini TTS with selectable built-in voices, optional custom `voice_...` ID, expressive Brazilian Portuguese delivery, and a separate API key encrypted with Android Keystore. Device-default TTS is not used by default.
- Character art import: select a licensed PNG/WebP from device storage; imported artwork replaces the procedural geometric portrait.
