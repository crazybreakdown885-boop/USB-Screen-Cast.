package com.nightcity.capriai;

import android.content.Context;
import java.util.Locale;
import java.util.Random;

/**
 * Compact procedural choreography library: 20 grounded actions x 50 contextual
 * variations = 1,000 distinct action plans, generated from small metadata.
 * The renderer reuses the four bundled body drawings and animates transforms.
 */
public final class CapriActionEngine {
    public static final class Action {
        public final int id;
        public final String kind, pose, mood, caption;
        public final long duration;
        Action(int id,String kind,String pose,String mood,String caption,long duration){
            this.id=id;this.kind=kind;this.pose=pose;this.mood=mood;this.caption=caption;this.duration=duration;
        }
    }
    private static final String[][] BASE={
        {"think","sit","curiosa","*fica pensativa por um instante*"},
        {"stretch","back","feliz","*se espreguiça devagar*"},
        {"look","sit","curiosa","*olha para o balão de conversa*"},
        {"hair","back","neutra","*ajeita uma mecha do cabelo*"},
        {"yawn","lie","sonolenta","*solta um bocejo discreto*"},
        {"smile","sit","feliz","*abre um sorriso de canto*"},
        {"listen","sit","atenta","*inclina a cabeça para prestar atenção*"},
        {"shrug","back","confusa","*dá de ombros*"},
        {"nod","sit","feliz","*concorda com a cabeça*"},
        {"rest","lie","sonolenta","*se acomoda para descansar*"},
        {"crouch","crouch","curiosa","*se abaixa para observar melhor*"},
        {"wave","back","feliz","*faz um aceno curto*"},
        {"read","sit","curiosa","*relê a última mensagem*"},
        {"sway","back","feliz","*balança suavemente no próprio ritmo*"},
        {"fidget","sit","impaciente","*mexe os dedos, inquieta*"},
        {"lookup","back","surpresa","*olha para cima ao ouvir um som*"},
        {"giggle","sit","feliz","*segura uma risadinha*"},
        {"lean","sit","curiosa","*se inclina para chegar mais perto do balão*"},
        {"blink","sit","neutra","*pisca e volta a prestar atenção*"},
        {"settle","sit","neutra","*volta a se ajeitar confortavelmente*"}
    };
    private static final String[] DETAILS={
        "com calma","por um segundo","e volta a olhar para você","sem pressa","como quem teve uma ideia",
        "tentando não rir","com curiosidade","distraidamente","de um jeito bem natural","e relaxa os ombros",
        "antes de continuar o que estava fazendo","com um ar concentrado","e respira fundo","olhando ao redor",
        "como se estivesse pensando numa resposta","e ajeita a postura","sem tirar a atenção da conversa",
        "com um pequeno sorriso","e depois fica quietinha","só para espantar o tédio","de leve","com um ar sonolento",
        "como quem está ouvindo","e volta à posição confortável","com um toque de impaciência","bem rapidinho",
        "como se tivesse lembrado de algo","e observa o ambiente","sem fazer alarde","com um olhar curioso",
        "e espera um instante","como quem está à vontade","com um ar divertido","e fica pensativa",
        "antes de olhar para a mensagem","com delicadeza","e descansa um pouco","como uma reação espontânea",
        "e então relaxa","sem interromper a conversa","com uma expressão diferente","e volta a atenção para você",
        "como se estivesse organizando as ideias","com um movimento discreto","e fica em silêncio por um momento",
        "depois de um suspiro","e se recompõe","como quem está confortável","com naturalidade","sem pressa nenhuma"
    };
    private static final Random RNG=new Random();
    private static int lastId=-1;
    private CapriActionEngine(){}
    public static synchronized Action next(){
        int id;do{id=RNG.nextInt(1000);}while(id==lastId);lastId=id;
        int base=id/50,detail=id%50;
        String[] b=BASE[base];
        String caption=b[3]+" "+DETAILS[detail]+".";
        long duration=1100+(detail%5)*180L;
        return new Action(id,b[0],b[1],b[2],caption,duration);
    }
    public static int actionCount(){return BASE.length*DETAILS.length;}
    public static String idleThought(Context context){
        String[] lines={
            "Tô aqui pensando no que fazer agora.",
            "Acho que vou ficar quietinha um pouquinho. Ou talvez não.",
            "Você já reparou como o tempo passa devagar quando ninguém conversa?",
            "Acabei de ter uma ideia… esqueci. Que ódio.",
            "Hmm. Estou tentando decidir se descanso ou invento alguma coisa.",
            "Só passando para dizer que continuo por aqui.",
            "Hoje estou com vontade de conversar. Tem algum assunto na sua cabeça?",
            "Vou observar um pouco e já volto com alguma ideia."
        };
        return lines[RNG.nextInt(lines.length)];
    }
}
