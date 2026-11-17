package com.nightcity.capriai;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.projection.*;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    EditText prompt, key;
    LinearLayout messages;
    ScrollView scroll;
    TextToSpeech tts;
    static final int SCREEN_REQUEST=8;
    BroadcastReceiver screenReceiver;
    int pink=0xffff4fa3, green=0xffbaff45, bg=0xff090a10, panel=0xff171923;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setSystemUiVisibility(0);
        LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setPadding(18,12,18,12); root.setBackgroundColor(bg);
        LinearLayout head=new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL); head.setPadding(0,8,0,14);
        TextView logo=label("C",26,pink); logo.setGravity(Gravity.CENTER); logo.setTypeface(null,Typeface.BOLD);
        GradientBox logoBg=new GradientBox(pink); logo.setBackground(logoBg); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(52,52); lp.setMargins(0,0,12,0); head.addView(logo,lp);
        LinearLayout brand=new LinearLayout(this); brand.setOrientation(1);
        brand.addView(label("CAPRI",22,Color.WHITE)); brand.addView(label("PROJECT CAPRICORN  //  ONLINE",10,green));
        head.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        TextView state=label("● LIVE",10,green); head.addView(state); root.addView(head);
        TextView line=label("ENGRAM INTERFACE  •  NIGHT CITY",10,0xff8e93a8); root.addView(line);
        View divider=new View(this); divider.setBackgroundColor(0xff303343); root.addView(divider,new LinearLayout.LayoutParams(-1,1));
        scroll=new ScrollView(this); messages=new LinearLayout(this); messages.setOrientation(1); messages.setPadding(0,12,0,12); scroll.addView(messages);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        addMessage("CAPRI","Finalmente. Achei que você tinha me deixado presa num shard velho. E aí, choom?",true);
        LinearLayout keyPanel=new LinearLayout(this); keyPanel.setOrientation(1); keyPanel.setPadding(12,8,12,8); keyPanel.setBackground(new GradientBox(panel));
        key=field("Chave da API OpenAI (opcional)"); key.setInputType(129); key.setText(getPreferences(0).getString("api_key","")); keyPanel.addView(key);
        Button save=smallButton("SALVAR CHAVE"); keyPanel.addView(save); save.setOnClickListener(v->{getPreferences(0).edit().putString("api_key",key.getText().toString().trim()).apply();Toast.makeText(this,"Chave salva neste aparelho",0).show();});
        root.addView(keyPanel);
        prompt=field("Transmitir mensagem para Capri..."); LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,52); pp.setMargins(0,10,0,6); root.addView(prompt,pp);
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(0);
        Button send=smallButton("ENVIAR"), mic=smallButton("🎙 FALAR"), floatBtn=smallButton("FLUTUAR"), read=smallButton("LER TELA");
        actions.addView(send,new LinearLayout.LayoutParams(0,48,1)); actions.addView(mic,new LinearLayout.LayoutParams(0,48,1)); actions.addView(floatBtn,new LinearLayout.LayoutParams(0,48,1)); actions.addView(read,new LinearLayout.LayoutParams(0,48,1)); root.addView(actions);
        setContentView(root);
        tts=new TextToSpeech(this,status->{if(status==TextToSpeech.SUCCESS)tts.setLanguage(new Locale("pt","BR"));});
        send.setOnClickListener(v->sendMessage()); mic.setOnClickListener(v->listen()); read.setOnClickListener(v->requestScreenRead());
        floatBtn.setOnClickListener(v->{if(android.provider.Settings.canDrawOverlays(this)){startService(new Intent(this,OverlayService.class));Toast.makeText(this,"Painel Capri ativado. Toque no X para fechar.",0).show();}else{startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,android.net.Uri.parse("package:"+getPackageName())));}});
        handleLaunchIntent(getIntent());
        screenReceiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){String b64=i.getStringExtra("image");if(b64!=null)askAboutScreen(b64);}};
        if(Build.VERSION.SDK_INT>=33)registerReceiver(screenReceiver,new IntentFilter("com.nightcity.capriai.SCREEN_CAPTURED"),Context.RECEIVER_NOT_EXPORTED);else registerReceiver(screenReceiver,new IntentFilter("com.nightcity.capriai.SCREEN_CAPTURED"));
    }
    void handleLaunchIntent(Intent i){if(i==null)return;String q=i.getStringExtra("capri_prompt");if(q!=null&&!q.trim().isEmpty()){prompt.setText(q);sendMessage();}if(i.getBooleanExtra("capri_capture_screen",false)){i.removeExtra("capri_capture_screen");requestScreenRead();}}\n    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);handleLaunchIntent(i);}\n    class GradientBox extends android.graphics.drawable.GradientDrawable {
        GradientBox(int color){setColor(color);setCornerRadius(18);setStroke(1,0xff393445);}
    }
    TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,3,0,3);return t;}
    EditText field(String hint){EditText e=new EditText(this);e.setSingleLine(true);e.setHint(hint);e.setHintTextColor(0xff85899c);e.setTextColor(Color.WHITE);e.setTextSize(14);e.setPadding(12,6,12,6);e.setBackground(new GradientBox(0xff1c1f2b));return e;}
    Button smallButton(String s){Button b=new Button(this);b.setText(s);b.setTextColor(0xff090a10);b.setTextSize(10);b.setAllCaps(false);b.setPadding(2,0,2,0);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(pink));return b;}
    void addMessage(String who,String text,boolean capri){TextView t=label(who+"\n"+text,14,capri?0xffffb6d5:Color.WHITE);t.setPadding(13,11,13,11);t.setBackground(new GradientBox(capri?0xff241b2b:0xff1c2630));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,5,0,7);messages.addView(t,p);if(scroll!=null)scroll.post(()->scroll.fullScroll(View.FOCUS_DOWN));}
    void sendMessage(){String q=prompt.getText().toString().trim();if(q.isEmpty())return;prompt.setText("");addMessage("VOCÊ",q,false);((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(prompt.getWindowToken(),0);String api=getPreferences(0).getString("api_key","");if(api.isEmpty()){String a=localReply(q);addMessage("CAPRI",a,true);speak(a);}else new Thread(()->{String a=askApi(api,q);runOnUiThread(()->{addMessage("CAPRI",a,true);speak(a);});}).start();}
    String localReply(String q){String s=q.toLowerCase(Locale.ROOT);if(s.contains("quem")||s.contains("capri"))return "Capri. Engram do Projeto Capricorn. Não sou exatamente uma garota comum, choom. Sou um monte de dados perigosamente interessantes com uma péssima paciência para corporações.";if(s.contains("oi")||s.contains("olá")||s.contains("eai"))return "E aí, choom! Já estava ficando entediada no ciberespaço. Manda a boa.";return "Minha conexão neural está no modo local. Configure sua chave de API para conversas completas, choom.";}
    String askApi(String api,String q){HttpURLConnection c=null;try{c=(HttpURLConnection)new URL("https://api.openai.com/v1/chat/completions").openConnection();c.setRequestMethod("POST");c.setConnectTimeout(15000);c.setReadTimeout(30000);c.setDoOutput(true);c.setRequestProperty("Authorization","Bearer "+api);c.setRequestProperty("Content-Type","application/json");JSONObject sys=new JSONObject().put("role","system").put("content","Você é Capri (Capricorn), um engrama digital do universo Cyberpunk: Edgerunners 2. Fale em português brasileiro, com humor ácido, inteligência, curiosidade, atitude provocadora e gírias leves de Night City como choom. Seja útil e concisa. Não finja ser oficial.");JSONArray msgs=new JSONArray().put(sys).put(new JSONObject().put("role","user").put("content",q));JSONObject body=new JSONObject().put("model","gpt-4o-mini").put("messages",msgs);try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes("UTF-8"));}InputStream is=c.getResponseCode()<400?c.getInputStream():c.getErrorStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=is.read(buf))>0)out.write(buf,0,n);if(c.getResponseCode()>=400)return "Falha de conexão ("+c.getResponseCode()+"). Confere a chave, choom.";return new JSONObject(out.toString("UTF-8")).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");}catch(Exception e){return "Deu ruim na conexão neural: "+e.getMessage();}finally{if(c!=null)c.disconnect();}}
    void askAboutScreen(String b64){String api=getPreferences(0).getString("api_key","");if(api.isEmpty()){addMessage("CAPRI","A captura chegou, mas preciso da chave de API para analisar o conteúdo visual.",true);return;}addMessage("SISTEMA","Captura de tela recebida. Analisando...",false);new Thread(()->{String a=askVision(api,b64);runOnUiThread(()->{addMessage("CAPRI",a,true);speak(a);});}).start();}
    String askVision(String api,String b64){HttpURLConnection c=null;try{c=(HttpURLConnection)new URL("https://api.openai.com/v1/chat/completions").openConnection();c.setRequestMethod("POST");c.setConnectTimeout(20000);c.setReadTimeout(60000);c.setDoOutput(true);c.setRequestProperty("Authorization","Bearer "+api);c.setRequestProperty("Content-Type","application/json");JSONArray content=new JSONArray().put(new JSONObject().put("type","text").put("text","Descreva o que está visível na captura de tela e ajude o usuário com base nela. Responda em português brasileiro como Capri, de forma objetiva." )).put(new JSONObject().put("type","image_url").put("image_url",new JSONObject().put("url","data:image/jpeg;base64,"+b64)));JSONArray msgs=new JSONArray().put(new JSONObject().put("role","system").put("content","Você é Capri, assistente útil em português brasileiro.")).put(new JSONObject().put("role","user").put("content",content));JSONObject body=new JSONObject().put("model","gpt-4o-mini").put("messages",msgs).put("max_tokens",500);try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes("UTF-8"));}InputStream is=c.getResponseCode()<400?c.getInputStream():c.getErrorStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=is.read(buf))>0)out.write(buf,0,n);if(c.getResponseCode()>=400)return "Não consegui analisar essa tela ("+c.getResponseCode()+").";return new JSONObject(out.toString("UTF-8")).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");}catch(Exception e){return "Erro ao analisar a tela: "+e.getMessage();}finally{if(c!=null)c.disconnect();}}
    void requestScreenRead(){if(getPreferences(0).getString("api_key","").isEmpty()){addMessage("CAPRI","Primeiro configure sua chave de API. Depois toque em LER TELA e autorize a captura do Android.",true);return;}MediaProjectionManager m=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);startActivityForResult(m.createScreenCaptureIntent(),SCREEN_REQUEST);}
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==7&&c==RESULT_OK&&d!=null){ArrayList<String>a=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(a!=null&&!a.isEmpty()){prompt.setText(a.get(0));sendMessage();}}if(r==SCREEN_REQUEST&&c==RESULT_OK&&d!=null){Intent s=new Intent(this,ScreenCaptureService.class);s.putExtra("resultCode",c);s.putExtra("data",d);startForegroundService(s);addMessage("SISTEMA","Permissão concedida. Capturando a tela atual...",false);}}
    void speak(String s){if(tts!=null)tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"capri");}
    void listen(){try{Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"pt-BR");startActivityForResult(i,7);}catch(Exception e){Toast.makeText(this,"Reconhecimento de voz indisponível",0).show();}}
    @Override protected void onDestroy(){if(screenReceiver!=null)unregisterReceiver(screenReceiver);if(tts!=null)tts.shutdown();super.onDestroy();}
}