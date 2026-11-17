package com.nightcity.capriai;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    EditText prompt, key;
    LinearLayout messages;
    TextToSpeech tts;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(12,12,16));
        getWindow().setNavigationBarColor(Color.rgb(12,12,16));
        LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setPadding(18,18,18,12); root.setBackgroundColor(Color.rgb(12,12,16));
        root.addView(label("CAPRI // ENGRAM ONLINE",22,0xffff4fa3));
        root.addView(label("Projeto Capricorn • interface experimental",12,0xff9bff70));
        key=new EditText(this); key.setSingleLine(true); key.setHint("Chave API OpenAI (opcional)"); key.setHintTextColor(0xff888888); key.setTextColor(Color.WHITE); key.setInputType(129); key.setText(getPreferences(0).getString("api_key","")); root.addView(key);
        root.addView(label("A chave fica salva somente neste aparelho. Sem chave, uso respostas locais de demonstração.",11,0xffaaaaaa));
        Button save=button("SALVAR CHAVE"); root.addView(save); save.setOnClickListener(v->{getPreferences(0).edit().putString("api_key",key.getText().toString().trim()).apply(); Toast.makeText(this,"Chave salva neste aparelho",0).show();});
        ScrollView scroll=new ScrollView(this); messages=new LinearLayout(this); messages.setOrientation(1); messages.setPadding(0,8,0,8); scroll.addView(messages); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        addMessage("CAPRI","Ah, finalmente alguém me tirou do limbo digital. E aí, choom? O que vamos aprontar?",true);
        prompt=new EditText(this); prompt.setSingleLine(true); prompt.setHint("Fala comigo..."); prompt.setHintTextColor(0xff888888); prompt.setTextColor(Color.WHITE); prompt.setBackgroundColor(0xff25232b); root.addView(prompt);
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(0);
        Button send=button("ENVIAR"), mic=button("FALAR"), overlay=button("FLUTUAR");
        actions.addView(send,new LinearLayout.LayoutParams(0,52,1)); actions.addView(mic,new LinearLayout.LayoutParams(0,52,1)); actions.addView(overlay,new LinearLayout.LayoutParams(0,52,1)); root.addView(actions);
        setContentView(root);
        tts=new TextToSpeech(this,status->{});
        send.setOnClickListener(v->sendMessage()); mic.setOnClickListener(v->listen());
        overlay.setOnClickListener(v->{ if(android.provider.Settings.canDrawOverlays(this)){startService(new Intent(this,OverlayService.class)); Toast.makeText(this,"Capri flutuante ativada",0).show();} else {Intent i=new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,android.net.Uri.parse("package:"+getPackageName())); startActivity(i);} });
    }
    TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,8,0,8);return t;}
    Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(0xff111111);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xffff4fa3));b.setTextSize(11);return b;}
    void addMessage(String who,String text,boolean capri){TextView t=label(who+"\n"+text,15,capri?0xffff9bc8:Color.WHITE);t.setBackgroundColor(capri?0xff29202a:0xff20242a);t.setPadding(12,10,12,10);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,5,0,5);messages.addView(t,p);}
    void sendMessage(){String q=prompt.getText().toString().trim();if(q.isEmpty())return;prompt.setText("");addMessage("VOCÊ",q,false);((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(prompt.getWindowToken(),0);String api=getPreferences(0).getString("api_key","");if(api.isEmpty()){String a=localReply(q);addMessage("CAPRI",a,true);speak(a);}else new Thread(()->{String a=askApi(api,q);runOnUiThread(()->{addMessage("CAPRI",a,true);speak(a);});}).start();}
    String localReply(String q){String s=q.toLowerCase(Locale.ROOT);if(s.contains("quem")||s.contains("capri"))return "Capri. Engram do Projeto Capricorn. Não sou exatamente uma garota comum, choom. Sou um monte de dados perigosamente interessantes com uma péssima paciência para corporações.";if(s.contains("oi")||s.contains("olá")||s.contains("eai"))return "E aí, choom! Já estava ficando entediada no ciberespaço. Manda a boa.";if(s.contains("ajuda"))return "Posso conversar por aqui, ler minhas respostas em voz alta e flutuar sobre outros apps. Para respostas de IA completas, configure uma chave de API acima.";return "Heh. Minha conexão neural está no modo local agora. Configure uma chave de API compatível para eu pensar além dos meus arquivos de demonstração, choom.";}
    String askApi(String api,String q){HttpURLConnection c=null;try{c=(HttpURLConnection)new URL("https://api.openai.com/v1/chat/completions").openConnection();c.setRequestMethod("POST");c.setConnectTimeout(15000);c.setReadTimeout(30000);c.setDoOutput(true);c.setRequestProperty("Authorization","Bearer "+api);c.setRequestProperty("Content-Type","application/json");JSONObject sys=new JSONObject().put("role","system").put("content","Você é Capri (Capricorn), um engrama digital do universo Cyberpunk: Edgerunners 2. Fale em português brasileiro, com humor ácido, inteligência, curiosidade, atitude provocadora e gírias leves de Night City como choom. Seja calorosa sem fingir ser a personagem oficial ou conhecer fatos não publicados. Respostas concisas e úteis.");JSONArray msgs=new JSONArray().put(sys).put(new JSONObject().put("role","user").put("content",q));JSONObject body=new JSONObject().put("model","gpt-4o-mini").put("messages",msgs);try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes("UTF-8"));}InputStream is=c.getResponseCode()<400?c.getInputStream():c.getErrorStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=is.read(buf))>0)out.write(buf,0,n);if(c.getResponseCode()>=400)return "A conexão com o servidor falhou ("+c.getResponseCode()+"). Confere sua chave, choom.";return new JSONObject(out.toString("UTF-8")).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");}catch(Exception e){return "Deu ruim na conexão neural: "+e.getMessage();}finally{if(c!=null)c.disconnect();}}
    void speak(String s){if(tts!=null)tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"capri");}
    void listen(){try{Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"pt-BR");startActivityForResult(i,7);}catch(Exception e){Toast.makeText(this,"Reconhecimento de voz indisponível neste aparelho",0).show();}}
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==7&&c==RESULT_OK&&d!=null){ArrayList<String>a=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(a!=null&&!a.isEmpty()){prompt.setText(a.get(0));sendMessage();}}}
    @Override protected void onDestroy(){if(tts!=null)tts.shutdown();super.onDestroy();}
}