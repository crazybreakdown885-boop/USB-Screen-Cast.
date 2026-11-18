# Capri AI para Android

Projeto Android da personagem Capri: assistente conversacional com interface em português, personagem animada, expressões por contexto, voz e janela flutuante.

## Baixar

Os APKs oficiais ficam na seção **Releases** deste repositório. A versão atual do código é **0.8.0**.

## Recursos

- Personagem Capri na tela principal e em sobreposição arrastável.
- Balão de diálogo e expressões faciais básicas que reagem ao texto da conversa.
- Interface e configurações em português brasileiro.
- Conversa com provedor compatível com a API da OpenAI, usando chave fornecida pelo próprio usuário.
- Leitura de respostas por Text-to-Speech e entrada por reconhecimento de voz.
- Captura pontual da tela somente após autorização explícita do Android.
- Comandos Android básicos e modo offline limitado.

## Requisitos

- Android 10 ou superior.
- Arquiteturas ARMv7 ou ARM64.
- Permissão de sobreposição para exibir a Capri sobre outros aplicativos.
- Internet e uma chave de API válida para respostas generativas online.

## Compilar

Abra a pasta `CapriAI` no Android Studio ou entre nela e execute:

```bash
gradle assembleDebug
```

O GitHub Actions compila o APK e publica o artefato/release nas atualizações do projeto.

## Privacidade e limites

A chave de API é informada pelo usuário e armazenada nas preferências privadas do aplicativo. A autorização de captura de tela é solicitada pelo Android; o app não lê chaves de outros aplicativos nem acessa a tela silenciosamente. A análise visual envia a imagem à API configurada quando o recurso é usado.

Esta é uma adaptação Android independente e experimental, não uma cópia integral do companion de desktop. As expressões atuais são selecionadas por regras simples de palavras/contexto, não por um sistema avançado de reconhecimento emocional.
