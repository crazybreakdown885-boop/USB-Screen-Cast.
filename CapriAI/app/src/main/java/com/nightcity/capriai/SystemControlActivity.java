package com.nightcity.capriai;

import android.app.*;
import android.os.*;
import android.content.*;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import android.media.AudioManager;

public class SystemControlActivity extends Activity {
    LinearLayout page; android.content.SharedPreferences prefs; int pink=0xffff315f,cyan=0xff35d9f5,bg=0xff080b12;
    int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    TextView label(String s,int z,int c){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setTypeface(Typeface.MONOSPACE);t.setPadding(0,dp(7),0,dp(7));return t;}
    Button button(String s){Button b=new Button(this);b.setText(s);b.setTextSize(12);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(pink));return b;}
    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("capri_settings",0);getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);ScrollView sc=new ScrollView(this);page=new LinearLayout(this);page.setOrientation(1);page.setPadding(dp(16),dp(16),dp(16),dp(24));page.setBackgroundColor(bg);sc.addView(page);page.addView(label("CAPRI // CONTROLES DO SISTEMA",19,cyan));page.addView(label("Ações são executadas somente após seu toque. Algumas abrem o painel oficial do Android.",11,0xffb8c4d0));section("ÁUDIO");AudioManager am=(AudioManager)getSystemService(AUDIO_SERVICE);volume("Mídia",am,AudioManager.STREAM_MUSIC);volume("Chamadas",am,AudioManager.STREAM_VOICE_CALL);volume("Alarmes",am,AudioManager.STREAM_ALARM);section("TELA");Button brightness=button("AJUSTAR BRILHO");page.addView(brightness);brightness.setOnClickListener(v->{if(!Settings.System.canWrite(this)){startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,android.net.Uri.parse("package:"+getPackageName())));return;}int cur=Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,120);SeekBar bar=new SeekBar(this);bar.setMax(255);bar.setProgress(cur);new AlertDialog.Builder(this).setTitle("Brilho da tela").setView(bar).setPositiveButton("Aplicar",(d,w)->{try{Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,bar.getProgress());}catch(Exception ignored){}}).setNegativeButton("Cancelar",null).show();});shortcut("Wi-Fi / Internet","android.settings.panel.action.INTERNET_CONNECTIVITY");shortcut("Bluetooth","android.settings.panel.action.BLUETOOTH");shortcut("Modo avião","android.settings.panel.action.AIRPLANE_MODE");shortcut("Rotação / tela","android.settings.DISPLAY_SETTINGS");shortcut("Bateria","android.settings.BATTERY_SAVER_SETTINGS");shortcut("Permissões dos aplicativos","android.settings.APPLICATION_DETAILS_SETTINGS");shortcut("Acessibilidade","android.settings.ACCESSIBILITY_SETTINGS");section("LIMITES DO ANDROID");page.addView(label("A Capri não pode ativar/desativar livremente Wi-Fi, Bluetooth, modo avião, GPS ou permissões de outros apps em versões modernas do Android. Ela abre os painéis oficiais para você confirmar. Brilho exige autorização especial do Android. Volume pode ser ajustado diretamente.",11,0xffb8c4d0));Button close=button("VOLTAR");page.addView(close);close.setOnClickListener(v->finish());setContentView(sc);}
    void section(String s){page.addView(label("— "+s+" —",13,cyan));}
    void volume(String title,AudioManager am,int stream){page.addView(label(title,11,Color.WHITE));SeekBar s=new SeekBar(this);s.setMax(am.getStreamMaxVolume(stream));s.setProgress(am.getStreamVolume(stream));page.addView(s);s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int p,boolean u){if(u)am.setStreamVolume(stream,p,0);}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});}
    void shortcut(String name,String action){Button b=button(name);page.addView(b);b.setOnClickListener(v->{try{startActivity(new Intent(action));}catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}});}
}
