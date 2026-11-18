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
        String saved=prefs.getString("ai_provider","Offline");provider.setSelection(java.util.Arrays.asList(providers).indexOf(saved)<0?0:java.util.Arrays.asList(providers).indexOf(saved));
        row("Provedor de IA",provider);
        TextView offlineInfo=text("OFFLINE: funciona sem chave. Conversa local básica, comandos, notas e voz. IA generativa e visão completa exigem provedor externo.",11,0xffa8a5b5);offlineInfo.setPadding(0,dp(6),0,dp(8));page.addView(offlineInfo);\n        EditText model=field("Nome do modelo (ex.: gpt-4o-mini)");model.setText(prefs.getString("ai_model","gpt-4o-mini"));row("Modelo",model);
        EditText endpoint=field("API endpoint");endpoint.setText(prefs.getString("ai_endpoint","https://api.openai.com/v1"));row("Endereço da API",endpoint);
        EditText api=field("Chave da API");api.setInputType(129);api.setText(prefs.getString("api_key",""));row("Chave da API",api);
        section("VOZ");
        check("Ativar fala da Capri","voice_enabled",true);
        seek("Velocidade da fala","voice_rate",50,200,94);
        seek("Tom da voz","voice_pitch",50,200,100);
        check("Ler respostas em voz alta","voice_replies",true);
        section("PERSONAGEM");\n        check("Mostrar somente olhos, faixa serrilhada e boca","face_only",false);\n        String[] poses={"sit","lie","crouch","back"};String[] poseLabels={"Sentada","Deitada","Agachada","De costas"};Spinner pose=new Spinner(this);pose.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,poseLabels));String poseSaved=prefs.getString("character_pose","sit");int poseIndex=java.util.Arrays.asList(poses).indexOf(poseSaved);pose.setSelection(poseIndex<0?0:poseIndex);row("Pose inicial",pose);
        seek("Tamanho da personagem","character_scale",40,180,100);
        seek("Intensidade das animações","animation_intensity",0,150,100);
        check("Efeitos de glitch","glitch_effects",true);
        check("Personagem sempre visível / sobreposição","always_on_top",true);
        check("Iniciar sobreposição automaticamente","overlay_enabled",false);
        section("COMPORTAMENTO");
        check("Comentários espontâneos (balão silencioso)","idle_remarks",false);
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
            prefs.edit().putString("ai_provider",p).putString("ai_model",model.getText().toString().trim()).putString("ai_endpoint",endpoint.getText().toString().trim()).putString("api_key",api.getText().toString().trim()).putString("character_pose",poses[pose.getSelectedItemPosition()]).apply();
            Toast.makeText(this,"Configurações salvas, choom.",Toast.LENGTH_SHORT).show();finish();
        });
        Button close=button("VOLTAR PARA CAPRI");page.addView(close,new LinearLayout.LayoutParams(-1,dp(46)));close.setOnClickListener(v->finish());
        setContentView(scroll);
    }
    TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setTypeface(Typeface.MONOSPACE);return t;}
    void section(String s){TextView t=text("— "+s+" —",13,cyan);t.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);t.setPadding(0,dp(18),0,dp(7));page.addView(t);}
    EditText field(String hint){EditText e=new EditText(this);e.setSingleLine(true);e.setTextSize(13);e.setTextColor(Color.WHITE);e.setHintTextColor(0xff888595);e.setHint(hint);e.setPadding(dp(10),0,dp(10),0);e.setBackgroundColor(0xff211d29);return e;}
    void row(String label,View v){TextView l=text(label,11,0xffbcb8c8);l.setPadding(0,dp(7),0,dp(3));page.addView(l);page.addView(v,new LinearLayout.LayoutParams(-1,dp(46)));}
    void check(String title,String key,boolean def){CheckBox c=new CheckBox(this);c.setText(title);c.setTextColor(Color.WHITE);c.setTextSize(12);c.setButtonTintList(android.content.res.ColorStateList.valueOf(pink));c.setChecked(prefs.getBoolean(key,def));c.setOnCheckedChangeListener((b,v)->prefs.edit().putBoolean(key,v).apply());page.addView(c);}
    void seek(String title,String key,int min,int max,int def){TextView l=text(title,11,0xffbcb8c8);l.setPadding(0,dp(8),0,0);page.addView(l);SeekBar s=new SeekBar(this);s.setMax(max-min);s.setProgress(Math.max(0,Math.min(max-min,prefs.getInt(key,def)-min)));s.setProgressTintList(android.content.res.ColorStateList.valueOf(pink));s.setThumbTintList(android.content.res.ColorStateList.valueOf(cyan));s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int p,boolean u){if(u)prefs.edit().putInt(key,p+min).apply();}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});page.addView(s);}
    Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.BLACK);b.setTextSize(11);b.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(pink));return b;}
}
