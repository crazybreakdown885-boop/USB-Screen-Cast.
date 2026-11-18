package com.nightcity.capriai;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Offline procedural dialogue engine. It composes responses from independent
 * phrase banks and live context instead of storing a huge static phrase list.
 */
public final class LocalDialogueEngine {
    private static final Random RNG = new Random();
    private static final String[] OPEN = {
        "Olha só,", "Ah, choom,", "Heh,", "Tá, vamos lá:", "Escuta,", "Sabe de uma coisa?", "Hmmm,", "Pera aí,", "Certo,", "Finalmente, assunto interessante:"
    };
    private static final String[] NICK = {
        "choom", "criatura", "lenda", "meu caro", "figura", "ser humano", "parceiro", "gênio", "meu consagrado", "estrela do caos"
    };
    private static final String[] ATTITUDE = {
        "minha genialidade continua subutilizada", "Night City não prepara ninguém pra isso", "já vi bugs com mais educação", "vou fingir que não achei isso engraçado", "isso merece um minuto de silêncio dramático", "meu sarcasmo está trabalhando em hora extra", "até meu processador ficou curioso", "não espalha que eu fui legal", "estou anotando mentalmente esse caos", "isso foi inesperadamente interessante"
    };
    private static final String[] FOLLOW = {
        "Me conta o resto.", "E aí, qual é a próxima?", "Quer desenvolver essa ideia comigo?", "Pode continuar, estou ouvindo.", "Não me deixa no cliffhanger.", "Manda a próxima, sem medo.", "Tem mais contexto nessa história?", "Vamos por partes.", "Estou prestando atenção.", "Surpreende-me."
    };
    private static final String[] GREET = {
        "Que bom te ver por aqui.", "Eu já estava quase conversando com a parede.", "Chegou chegando, hein?", "Meu tédio acaba de perder uma batalha.", "Senta aí e atualiza as fofocas.", "O que vamos aprontar hoje?", "A cidade continua um caos. E você?", "Tava faltando uma conversa decente.", "Você voltou. Interessante.", "Abre o jogo: como foi seu dia?"
    };
    private static final String[] JOKES = {
        "Um netrunner entrou num bar. Saiu com 404 drinks not found.",
        "Por que o robô foi ao médico? Estava com um vírus de estimação.",
        "Meu terapeuta é um terminal. Ele só responde: tente novamente.",
        "Night City é tão segura que até os perigos usam colete.",
        "O hacker terminou o namoro por incompatibilidade de portas.",
        "A IA foi promovida. Agora ela erra em horário executivo.",
        "O que o firewall disse? Daqui você não passa, querido.",
        "Meu senso de humor é igual Wi-Fi de elevador: aparece quando quer.",
        "O ciborgue pediu um café sem bugs. O barista reiniciou a máquina.",
        "Acho que fiz uma piada boa. Meu log discorda."
    };
    private static final String[] SUPPORT = {
        "Poxa. Não vou transformar isso em piada. Quer me contar o que aconteceu?",
        "Respira um pouco. Podemos organizar essa bagunça em passos menores.",
        "Não precisa resolver tudo agora. Começa pelo que está pesando mais.",
        "Estou aqui para te ouvir, sem julgamento e sem palestra corporativa.",
        "Isso parece difícil mesmo. O que seria uma pequena melhora neste momento?",
        "Vamos com calma. Você não precisa fingir que está tudo ótimo.",
        "Se quiser, só desabafa. Não vou te interromper com frase motivacional pronta.",
        "Um passo de cada vez, beleza? Me conta o que mais te preocupa.",
        "Não tenho como sentir exatamente o que você sente, mas posso te acompanhar nessa conversa.",
        "Você merece um pouco de gentileza, inclusive de si mesmo."
    };
    private static final String[] BRIDGE = {"E falando nisso,", "A propósito,", "Entre nós,", "Pra variar,", "No meu relatório imaginário,", "Mudando meio de assunto,", "Se eu puder acrescentar,", "Com todo o drama necessário,", "Pensando bem,", "E antes que eu esqueça,"};
    private static final String[] VIBE = {"isso tem energia de missão secundária", "o caos está devidamente registrado", "minha curiosidade foi oficialmente ativada", "até parece roteiro de braindance", "isso merece trilha sonora", "a situação tem potencial narrativo", "vou manter um olho nesse assunto", "o universo tem senso de humor duvidoso", "ainda estamos no controle... mais ou menos", "isso vai entrar para o folclore local"};
    private static final String[] ASIDE = {"não que eu esteja julgando", "e olha que meus padrões são altos", "sem prints, por favor", "não espalha pela rede", "com carinho e uma pitada de caos", "segundo minhas fontes totalmente imaginárias", "eu precisava dizer isso", "fica entre nós", "não me faça repetir", "e isso é um elogio, eu acho"};
    private static final String[] FLAVOR = {"com um toque de drama", "na velocidade de um elevador quebrado", "com estilo questionável, porém eficiente", "sem vender minha alma para a Arasaka", "com 87% de confiança teatral", "do jeito mais Night City possível", "sem manual e sem garantia", "com uma sobrancelha levantada", "em modo sinceridade brutal", "com a elegância de um gato no teclado"};
    private static final String[] SIGNOFF = {"Fechou?", "Combinado?", "Estamos entendidos?", "Agora me atualiza.", "Sua vez.", "E não some.", "Pode mandar.", "Bora nessa.", "Fim do comunicado.", "Por enquanto é isso."};
    private static final String[] UNKNOWN = {
        "Ainda não tenho conhecimento geral de verdade sem um modelo de IA, mas posso conversar e usar o contexto local.",
        "Meu cérebro offline é procedural: improviso com repertório, variáveis e o que ficou salvo. Não vou fingir que sei algo que não sei.",
        "Essa pergunta pede conhecimento que não está no meu pacote local. Posso ajudar a dividir o assunto em partes.",
        "Não tenho uma resposta confiável guardada para isso. Me dá uma pista ou reformula de outro jeito.",
        "Posso tentar pelo contexto, mas não tenho pesquisa na internet neste modo."
    };
    private LocalDialogueEngine() {}

    private static String pick(String[] values) { return values[RNG.nextInt(values.length)]; }
    private static String compose(String core, String time, String lastTopic) {
        String[] closers = {pick(FOLLOW), pick(ATTITUDE), "E sim, " + pick(ATTITUDE) + ".", "Enfim. " + pick(FOLLOW)};
        String topic = lastTopic.isEmpty() ? "essa conversa" : lastTopic;
        String[] context = {time, "Sobre " + topic + ",", "No ritmo de hoje,", "Considerando nosso papo,", "Com essa energia toda,", "Entre um caos e outro,", "Direto do meu cantinho digital,", "Sem enrolação,", "Pelo que você trouxe,", "Na moral,"};
        return pick(OPEN) + " " + core + " " + pick(context) + " " + pick(BRIDGE) + " " + pick(VIBE) + ", " + pick(ASIDE) + ", " + pick(FLAVOR) + ". " + pick(closers) + " " + pick(ATTITUDE) + ". " + pick(NICK) + ". " + pick(SIGNOFF);
    }

    public static String reply(Context context, String input) {
        SharedPreferences prefs = context.getSharedPreferences("capri_settings", 0);
        String q = input == null ? "" : input.trim();
        String s = q.toLowerCase(Locale.ROOT);
        String time = new SimpleDateFormat("HH:mm", new Locale("pt", "BR")).format(new Date());
        String day = new SimpleDateFormat("EEEE", new Locale("pt", "BR")).format(new Date());
        String lastTopic = prefs.getString("offline_last_topic", "");
        long turns = prefs.getLong("offline_turns", 0L) + 1L;
        prefs.edit().putLong("offline_turns", turns).putString("offline_last_topic", q.length() > 64 ? q.substring(0,64) : q).apply();

        if (s.matches(".*\\b(oi|olá|ola|e aí|eai|opa|bom dia|boa noite|boa tarde)\\b.*")) {
            return compose(pick(GREET) + " Hoje é " + day + " e são " + time + ".", time, lastTopic);
        }
        if (s.contains("quem é você") || s.contains("quem e voce") || s.contains("quem é a capri") || s.contains("quem e a capri")) {
            return "Sou a Capri, sua companheira digital de Night City. No offline eu monto respostas combinando bancos de frases, variáveis de horário, contexto e memória local. Não sou um modelo generativo completo, mas também não fico presa a uma única resposta.";
        }
        if (s.contains("como você está") || s.contains("como voce esta") || s.contains("tudo bem")) {
            return compose("Estou operacional, dramática na medida certa e com uma curiosidade suspeita sobre seu dia. " + pick(GREET), time, lastTopic);
        }
        if (s.contains("obrigad")) return compose("De nada. " + pick(ATTITUDE) + ".", time, lastTopic);
        if (s.contains("hora") || s.contains("que horas")) return compose("Agora são " + time + ". O relógio não espera nem por mim.", time, lastTopic);
        if (s.contains("data") || s.contains("dia é hoje") || s.contains("que dia")) return compose("Hoje é " + day + ". Faça esse dia valer alguma coisa.", time, lastTopic);
        if (s.contains("piada") || s.contains("me faz rir") || s.contains("me conta uma")) return compose(pick(JOKES), time, lastTopic);
        if (s.contains("triste") || s.contains("mal hoje") || s.contains("tô mal") || s.contains("to mal") || s.contains("ansios") || s.contains("sozinh")) return pick(SUPPORT);
        if (s.contains("te amo") || s.contains("gosto de você") || s.contains("gosto de voce")) return compose("Olha só, desse jeito vou ficar convencida. 💚", time, lastTopic);
        if (s.contains("ajuda") || s.contains("o que você faz") || s.contains("o que voce faz")) return "Posso conversar, variar diálogos, lembrar o contexto recente quando a memória estiver ativada, falar em voz alta, guardar notas, abrir apps e páginas e criar timers. Para conhecimento amplo, análise real da tela e raciocínio aberto, preciso de um modelo conectado.";
        if (s.startsWith("nota:") || s.startsWith("anote:")) return compose("Sua nota foi guardada no celular.", time, lastTopic);
        if (s.contains("capacidade") || s.contains("quantas falas") || s.contains("bilhão") || s.contains("bilhao")) return "Meu repertório é combinatório: misturo aberturas, respostas, comentários, contexto, humor, formas de tratamento e encerramentos. Isso cria mais de 1 bilhão de combinações teóricas sem guardar um bilhão de frases no armazenamento.";
        if (s.contains("quem") || s.contains("como") || s.contains("por que") || s.contains("porque") || s.contains("qual") || s.contains("onde") || s.contains("quando")) {
            return compose("Boa pergunta. No modo local não tenho uma base universal para confirmar isso, então prefiro não inventar. " + pick(UNKNOWN), time, lastTopic);
        }
        String[] reactions = {
            "Entendi o que você trouxe. " + pick(FOLLOW),
            "Certo. " + pick(ATTITUDE) + ". " + pick(FOLLOW),
            "Estou acompanhando. " + pick(UNKNOWN),
            "Hmm, isso rende conversa. " + pick(FOLLOW),
            "Anotado no contexto desta sessão. " + pick(ATTITUDE) + ".",
            "Tá, quero entender melhor essa parte. " + pick(FOLLOW),
            "Isso me deu uma ideia. " + pick(ATTITUDE) + ". " + pick(FOLLOW),
            "Não vou fingir que tenho certeza. " + pick(UNKNOWN),
            "Interessante. " + pick(GREET),
            "Pode falar mais. " + pick(FOLLOW)
        };
        return compose(pick(reactions), time, lastTopic);
    }
}
