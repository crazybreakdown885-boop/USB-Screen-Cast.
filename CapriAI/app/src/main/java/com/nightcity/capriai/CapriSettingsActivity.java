package com.nightcity.capriai;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;

public class CapriSettingsActivity extends Activity {
    android.content.SharedPreferences prefs;
    LinearLayout page;
    int bg=0xff0b0911,pink=0xfff2154c,cyan=0xff28dff4,green=0xff22f27a;
    int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("capri_settings",0);
        getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        page=new LinearLayout(this);page.setOrientation(1);page.setPadding(dp(16),dp(14),dp(16),dp(24));page.setBackgroundColor(bg);scroll.addView(page);
        TextView title=text("CAPRI  //  CONFIGURAÇÕES",20,Color.WHITE);title.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);page.addView(title);
        TextView sub=text("COMPANHEIRA MÓVEL / AJUSTES",10,green);page.addView(sub);
        section("CONVERSA / MODELO");
        String[] providers={"OpenAI-compatible","Google Gemini","Ollama local","Offline"};
        Spinner provider=new Spinner(this);provider.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,providers));
        String saved=prefs.getString("ai_provider","Offline");if("Offline".equals(saved)&&!ApiKeyStore.get(this).trim().isEmpty())saved="OpenAI-compatible";provider.setSelection(java.util.Arrays.asList(providers).indexOf(saved)<0?0:java.util.Arrays.asList(providers).indexOf(saved));
        row("Provedor de IA",provider);
        TextView offlineInfo=text("OFFLINE: funciona sem chave. Conversa local básica, comandos, notas e voz. IA generativa e visão completa exigem provedor externo.",11,0xffa8a5b5);offlineInfo.setPadding(0,dp(6),0,dp(8));page.addView(offlineInfo);
        EditText model=field("Nome do modelo (ex.: gpt-4o-mini)");model.setText(prefs.getString("ai_model","gpt-4o-mini"));row("Modelo",model);
        EditText endpoint=field("API endpoint");endpoint.setText(prefs.getString("ai_endpoint","https://api.openai.com/v1"));row("Endereço da API",endpoint);
        EditText api=field("Chave da API");
        api.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        api.setTransformationMethod(android.text.method.HideReturnsTransformationMethod.getInstance());
        api.setText(ApiKeyStore.get(this));row("Chave secreta da API (somente sua)",api);
        Button testApi=button("TESTAR CONEXÃO DA API");page.addView(testApi,new LinearLayout.LayoutParams(-1,dp(44)));
        TextView apiHint=text("Cole sua chave secreta da plataforma OpenAI. Ela será criptografada no armazenamento do Android. Não use sua senha do ChatGPT. A API pode gerar cobranças separadas.",10,0xffa8a5b5);apiHint.setPadding(0,dp(4),0,dp(8));page.addView(apiHint);
        Button createKey=button("CRIAR / OBTER MINHA CHAVE OPENAI");page.addView(createKey,new LinearLayout.LayoutParams(-1,dp(44)));
        createKey.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://platform.openai.com/api-keys")));}catch(Exception ignored){}});
        testApi.setOnClickListener(v->{
            String selected=providers[provider.getSelectedItemPosition()];
            String secret=normalizeApiKey(api.getText().toString());
            if("Offline".equals(selected)&&!secret.isEmpty()){provider.setSelection(0);selected="OpenAI-compatible";}
            if("Offline".equals(selected)){Toast.makeText(this,"Selecione um provedor online primeiro.",Toast.LENGTH_LONG).show();return;}
            if(!"Ollama local".equals(selected)&&secret.isEmpty()){api.setError("Cole sua chave completa aqui");return;}
            testApi.setEnabled(false);testApi.setText("TESTANDO...");
            final String p=selected,k=secret,m=model.getText().toString().trim(),url=endpoint.getText().toString().trim();
            new Thread(()->{
                String result=runApiTest(p,k,m,url);
                runOnUiThread(()->{testApi.setEnabled(true);testApi.setText("TESTAR CONEXÃO DA API");new AlertDialog.Builder(this).setTitle(result.startsWith("Conexão OK")?"API CONECTADA":"FALHA NA API").setMessage(result).setPositiveButton("OK",null).show();});
            }).start();
        });
        section("VOZ / PORTUGUÊS BRASILEIRO");
        TextView voiceInfo=text("Capri fala junto com as mensagens. O app seleciona automaticamente a melhor voz pt-BR instalada, priorizando vozes de maior qualidade e que funcionam offline. A voz exata depende do mecanismo de fala do Android.",11,0xffa8a5b5);voiceInfo.setPadding(0,dp(4),0,dp(8));page.addView(voiceInfo);
        Button voiceSettings=button("ESCOLHER / BAIXAR VOZ DO ANDROID");page.addView(voiceSettings,new LinearLayout.LayoutParams(-1,dp(44)));
        voiceSettings.setOnClickListener(v->{try{startActivity(new Intent("com.android.settings.TTS_SETTINGS"));}catch(Exception e){startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));}});
        check("Ativar fala da Capri","voice_enabled",true);
        seek("Velocidade da fala","voice_rate",50,200,92);
        seek("Tom da voz","voice_pitch",50,200,108);
        check("Ler respostas em voz alta","voice_replies",true);
        section("PERSONAGEM");
        check("Mostrar somente olhos, faixa serrilhada e boca","face_only",false);
        check("Capri se movimenta livremente pela tela","free_roam",true);
        check("Usar teleporte com efeito visual","teleport_enabled",true);
        check("Mudar pose e forma automaticamente","auto_morph",true);
        String[] poses={"sit","lie","crouch","back"};String[] poseLabels={"Sentada","Deitada","Agachada","De costas"};Spinner pose=new Spinner(this);pose.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,poseLabels));String poseSaved=prefs.getString("character_pose","sit");int poseIndex=java.util.Arrays.asList(poses).indexOf(poseSaved);pose.setSelection(poseIndex<0?0:poseIndex);row("Pose inicial",pose);
        seek("Tamanho da personagem","character_scale",40,180,100);
        seek("Intensidade das animações","animation_intensity",0,150,100);
        check("Efeitos de glitch","glitch_effects",true);
        check("Personagem sempre visível / sobreposição","always_on_top",true);
        check("Iniciar sobreposição automaticamente","overlay_enabled",false);
        section("COMPORTAMENTO");
        check("Comentários espontâneos (balão silencioso)","idle_remarks",false);
        check("Animações e atividades espontâneas (assobiar, jogar, ouvir música, sentar no diálogo)","idle_activities",true);
        check("Não perturbe","do_not_disturb",false);
        check("Horário silencioso (22:00–08:00)","quiet_hours",true);
        check("Ocultar personagem ao digitar","hide_while_typing",false);
        section("MEMÓRIA");
        check("Lembrar contexto da conversa","memory_enabled",true);
        seek("Mensagens anteriores para lembrar","history_turns",0,40,10);
        check("Salvar histórico neste celular","save_history",true);
        section("PRIVACIDADE");
        check("Pedir autorização antes de ler a tela","screen_confirm",true);
        check("Permitir análise da tela quando eu pedir","screen_context",true);
        check("Manter leitura da tela ativa com Capri aberta","screen_monitor",false);
        check("Enviar imagens da tela somente quando eu pedir","screen_on_demand",true);
        TextView privacy=text("Screen capture is requested through Android MediaProjection. The system shows its own consent prompt and capture indicator. Images are sent to the selected Provedor de IA only when you request screen analysis.",11,0xffa8a5b5);privacy.setPadding(0,dp(4),0,dp(12));page.addView(privacy);
        section("SISTEMA");
        check("Reabrir sobreposição ao voltar para Capri","restore_overlay",true);
        check("Usar conversa em tela cheia","immersive",true);
        Button save=button("SALVAR CONFIGURAÇÕES");LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(50));sp.topMargin=dp(18);page.addView(save,sp);
        save.setOnClickListener(v->{
            String p=providers[provider.getSelectedItemPosition()];
            String enteredKey=normalizeApiKey(api.getText().toString());
            if("Offline".equals(p)&&!enteredKey.isEmpty())p="OpenAI-compatible";
            try {
                ApiKeyStore.save(this,enteredKey);
                prefs.edit().remove("api_key").putString("ai_provider",p).putString("ai_model",model.getText().toString().trim()).putString("ai_endpoint",endpoint.getText().toString().trim()).putString("character_pose",poses[pose.getSelectedItemPosition()]).apply();
                Toast.makeText(this,enteredKey.isEmpty()?"Chave removida. Modo offline disponível.":"Chave salva com criptografia. Capri pronta para conectar online.",Toast.LENGTH_LONG).show();finish();
            } catch(Exception ex) {
                new AlertDialog.Builder(this).setTitle("Não foi possível proteger a chave").setMessage(ex.getMessage()).setPositiveButton("OK",null).show();
            }
        });
        Button close=button("VOLTAR PARA CAPRI");page.addView(close,new LinearLayout.LayoutParams(-1,dp(46)));close.setOnClickListener(v->finish());
        setContentView(scroll);
    }
    String normalizeApiKey(String value){String k=value==null?"":value.trim();if(k.regionMatches(true,0,"Bearer ",0,7))k=k.substring(7).trim();if(k.length()>=2&&((k.startsWith("\"")&&k.endsWith("\""))||(k.startsWith("\'")&&k.endsWith("\'"))))k=k.substring(1,k.length()-1).trim();return k.replaceAll("[\\r\\n\\t ]+","");}
    String runApiTest(String provider,String api,String model,String endpoint){
        java.net.HttpURLConnection c=null;
        try{
            String chosen=model;
            if("Google Gemini".equals(provider)){
                if(chosen.isEmpty())chosen="gemini-2.0-flash";
                java.net.URL u=new java.net.URL("https://generativelanguage.googleapis.com/v1beta/models/"+chosen+":generateContent?key="+java.net.URLEncoder.encode(api,"UTF-8"));
                c=(java.net.HttpURLConnection)u.openConnection();c.setRequestMethod("POST");c.setConnectTimeout(12000);c.setReadTimeout(25000);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");
                org.json.JSONObject body=new org.json.JSONObject().put("contents",new org.json.JSONArray().put(new org.json.JSONObject().put("parts",new org.json.JSONArray().put(new org.json.JSONObject().put("text","Responda somente: OK"))))).put("generationConfig",new org.json.JSONObject().put("maxOutputTokens",8));
                try(java.io.OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes("UTF-8"));}
            }else{
                String base=endpoint.trim();if(base.isEmpty())base="Ollama local".equals(provider)?"http://127.0.0.1:11434":"https://api.openai.com/v1";
                if("Ollama local".equals(provider)){if(!base.endsWith("/api/chat"))base=base.replaceAll("/+$","")+"/api/chat";}
                else if(!base.endsWith("/chat/completions"))base=base.replaceAll("/+$","")+"/chat/completions";
                c=(java.net.HttpURLConnection)new java.net.URL(base).openConnection();c.setRequestMethod("POST");c.setConnectTimeout(12000);c.setReadTimeout(25000);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");
                if(!api.isEmpty())c.setRequestProperty("Authorization","Bearer "+api);
                String selectedModel=chosen.isEmpty()?("Ollama local".equals(provider)?"llama3.2":"gpt-4o-mini"):chosen;
                org.json.JSONObject body=new org.json.JSONObject().put("model",selectedModel).put("messages",new org.json.JSONArray().put(new org.json.JSONObject().put("role","user").put("content","Responda somente: OK"))).put("max_completion_tokens",8);
                if("Ollama local".equals(provider))body.put("stream",false);
                try(java.io.OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes("UTF-8"));}
            }
            int status=c.getResponseCode();java.io.InputStream is=status<400?c.getInputStream():c.getErrorStream();
            java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] buf=new byte[2048];int n;while(is!=null&&(n=is.read(buf))>0)out.write(buf,0,n);
            String raw=out.toString("UTF-8");
            if(status>=200&&status<300)return "Conexão OK! A API respondeu com HTTP "+status+".\nModelo: "+(chosen.isEmpty()?"padrão":chosen);
            String detail=raw.replaceAll("\\s+"," ").trim();if(detail.length()>420)detail=detail.substring(0,420);
            if(status==401)return "HTTP 401 — a chave foi recusada. Confira se copiou a chave secreta inteira, se ela está ativa e se pertence ao projeto correto.\n"+detail;
            if(status==403)return "HTTP 403 — a chave não tem permissão para esse recurso/projeto.\n"+detail;
            if(status==404)return "HTTP 404 — endpoint ou nome do modelo não encontrado. Para OpenAI use https://api.openai.com/v1 e gpt-4o-mini.\n"+detail;
            if(status==429)return "HTTP 429 — limite de requisições ou créditos/faturamento da API. A assinatura ChatGPT não inclui créditos da API.\n"+detail;
            if(status==400)return "HTTP 400 — a API recebeu a chave, mas rejeitou o modelo ou os parâmetros. Confira o nome do modelo.\n"+detail;
            return "HTTP "+status+" — a API respondeu com erro.\n"+detail;
        }catch(Exception e){return "Não consegui alcançar a API: "+e.getClass().getSimpleName()+": "+e.getMessage()+"\nConfira a internet e o endereço do endpoint.";}
        finally{if(c!=null)c.disconnect();}
    }
    TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setTypeface(Typeface.MONOSPACE);return t;}
    void section(String s){TextView t=text("— "+s+" —",13,cyan);t.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);t.setPadding(0,dp(18),0,dp(7));page.addView(t);}
    EditText field(String hint){EditText e=new EditText(this);e.setSingleLine(true);e.setTextSize(13);e.setTextColor(Color.WHITE);e.setHintTextColor(0xff888595);e.setHint(hint);e.setPadding(dp(10),0,dp(10),0);e.setBackgroundColor(0xff211d29);return e;}
    void row(String label,View v){TextView l=text(label,11,0xffbcb8c8);l.setPadding(0,dp(7),0,dp(3));page.addView(l);page.addView(v,new LinearLayout.LayoutParams(-1,dp(46)));}
    void check(String title,String key,boolean def){CheckBox c=new CheckBox(this);c.setText(title);c.setTextColor(Color.WHITE);c.setTextSize(12);c.setButtonTintList(android.content.res.ColorStateList.valueOf(pink));c.setChecked(prefs.getBoolean(key,def));c.setOnCheckedChangeListener((b,v)->prefs.edit().putBoolean(key,v).apply());page.addView(c);}
    void seek(String title,String key,int min,int max,int def){TextView l=text(title,11,0xffbcb8c8);l.setPadding(0,dp(8),0,0);page.addView(l);SeekBar s=new SeekBar(this);s.setMax(max-min);s.setProgress(Math.max(0,Math.min(max-min,prefs.getInt(key,def)-min)));s.setProgressTintList(android.content.res.ColorStateList.valueOf(pink));s.setThumbTintList(android.content.res.ColorStateList.valueOf(cyan));s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int p,boolean u){if(u)prefs.edit().putInt(key,p+min).apply();}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});page.addView(s);}
    Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.BLACK);b.setTextSize(11);b.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(pink));return b;}
}
