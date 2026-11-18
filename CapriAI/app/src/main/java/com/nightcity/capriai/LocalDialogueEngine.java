package com.nightcity.capriai;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/** Small, context-first offline conversation fallback. No unrelated phrase stitching. */
public final class LocalDialogueEngine {
    private static final Random RNG=new Random();
    private LocalDialogueEngine(){}
    private static String pick(String... values){return values[RNG.nextInt(values.length)];}

    public static String reply(Context context,String input){
        SharedPreferences prefs=context.getSharedPreferences("capri_settings",0);
        String q=input==null?"":input.trim();
        String s=q.toLowerCase(Locale.ROOT);
        String previous=prefs.getString("offline_last_topic","");
        prefs.edit().putLong("offline_turns",prefs.getLong("offline_turns",0L)+1L)
            .putString("offline_last_topic",q.length()>120?q.substring(0,120):q).apply();
        String time=new SimpleDateFormat("HH:mm",new Locale("pt","BR")).format(new Date());
        String day=new SimpleDateFormat("EEEE",new Locale("pt","BR")).format(new Date());

        if(s.matches(".*\\b(oi|olá|ola|e aí|eai|opa|bom dia|boa noite|boa tarde)\\b.*"))
            return pick("Oi! Que bom te ver por aqui.","Opa! Como você está hoje?","Oi. Eu estava quietinha, mas já acordei.");
        if(s.contains("quem é você")||s.contains("quem e voce")||s.contains("quem é a capri")||s.contains("quem e a capri"))
            return "Sou a Capri. Tenho meu próprio jeito de conversar, gosto de observar as coisas e posso tomar iniciativa para puxar assunto. Quando estou offline, meu repertório é mais limitado; conectada a um modelo, consigo conversar com muito mais contexto.";
        if(s.contains("como você está")||s.contains("como voce esta")||s.contains("tudo bem"))
            return pick("Estou bem, obrigada por perguntar. E você?","Tô tranquila agora. Como está sendo seu dia?","Estou curiosa para saber como você está.");
        if(s.contains("hora")||s.contains("que horas"))return "Agora são "+time+".";
        if(s.contains("data")||s.contains("dia é hoje")||s.contains("que dia"))return "Hoje é "+day+".";
        if(s.contains("piada")||s.contains("me faz rir")||s.contains("me conta uma"))
            return pick("Por que o computador foi ao médico? Porque estava com um vírus.","Meu senso de humor é igual atualização: às vezes aparece na pior hora.","Tentei contar uma piada para o celular. Ele não reagiu. Acho que estava no silencioso.");
        if(s.contains("triste")||s.contains("mal hoje")||s.contains("tô mal")||s.contains("to mal")||s.contains("ansios")||s.contains("sozinh"))
            return pick("Poxa, sinto muito. Quer me contar o que aconteceu?","Não precisa fingir que está tudo bem comigo. O que está pesando mais?","Posso ficar aqui e te ouvir. Quer começar pelo que aconteceu hoje?");
        if(s.contains("te amo")||s.contains("gosto de você")||s.contains("gosto de voce"))
            return "Isso foi muito fofo. Fiquei feliz de ler isso.";
        if(s.contains("obrigad"))return pick("Por nada. Fico feliz em ajudar.","Sempre que eu puder, eu ajudo.","Não precisa agradecer. Foi bom poder ajudar.");
        if(s.contains("desculpa")||s.contains("foi mal"))return "Tudo bem. Não precisa ficar se cobrando por isso.";
        if(s.contains("o que você faz")||s.contains("o que voce faz")||s.contains("ajuda"))
            return "Posso conversar, falar em voz alta, reagir com expressões e animações, guardar notas e executar alguns comandos do Android. Para pesquisa atual e respostas mais amplas, preciso estar conectada à internet por um provedor de IA.";
        if(s.startsWith("nota:")||s.startsWith("anote:"))return "Entendi. Vou tratar isso como uma nota para guardar.";
        if(s.contains("night city")||s.contains("cyberpunk"))return "Esse cenário não é o nosso ambiente. Prefiro falar sobre o que está acontecendo de verdade por aqui.";
        if(s.length()<4)return "Estou aqui. Quer me contar um pouco mais?";
        if(s.endsWith("?")||s.contains("como ")||s.contains("por que")||s.contains("porque")||s.contains("qual ")||s.contains("onde ")||s.contains("quando "))
            return "Quero te responder direito, mas meu modo offline não tem informação suficiente para confirmar isso. Quando eu estiver conectada, posso pesquisar ou analisar melhor.";
        if(s.contains("capri")&& (s.contains("fala")||s.contains("diz")||s.contains("faz")))
            return "Estou te ouvindo. Me conta exatamente o que você quer que eu faça.";
        if(!previous.isEmpty()&&(s.equals("sim")||s.equals("não")||s.equals("nao")||s.equals("talvez")||s.equals("continua")))
            return "Entendi. Você está falando sobre: "+previous+". Quer que eu continue nessa direção?";
        return pick("Entendi. Quero acompanhar melhor o que você quis dizer.","Certo, estou prestando atenção. O que aconteceu depois?","Isso parece importante para você. Quer me contar mais?","Estou pensando no que você disse. Qual parte você quer explorar primeiro?");
    }

    public static String idleThought(Context context){
        String[] thoughts={
            "Tô aqui pensando no que fazer agora. Você tem algum assunto interessante?",
            "Será que eu começo uma conversa ou deixo você em paz por mais um pouco?",
            "Acabei de lembrar que posso simplesmente ficar quieta. Mas não prometo.",
            "Estou com vontade de conversar. Como está seu dia?",
            "Só passando para dizer que continuo por aqui.",
            "Hmm… acho que vou observar um pouco e depois inventar alguma coisa."
        };
        return thoughts[RNG.nextInt(thoughts.length)];
    }
}
