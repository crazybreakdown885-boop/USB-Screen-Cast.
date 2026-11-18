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
        TextView title=text("CAPRI  //  CONFIGURATION",20,Color.WHITE);title.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);page.addView(title);
        TextView sub=text("MOBILE COMPANION / SYSTEM SETTINGS",10,green);page.addView(sub);
        section("CHAT / MODEL");
        String[] providers={"OpenAI-compatible","Google Gemini","Ollama local","Offline"};
        Spinner provider=new Spinner(this);provider.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,providers));
        String saved=prefs.getString("ai_provider","OpenAI-compatible");provider.setSelection(java.util.Arrays.asList(providers).indexOf(saved)<0?0:java.util.Arrays.asList(providers).indexOf(saved));
        row("AI provider",provider);
        EditText model=field("Model name (e.g. gpt-4o-mini)");model.setText(prefs.getString("ai_model","gpt-4o-mini"));row("Model",model);
        EditText endpoint=field("API endpoint");endpoint.setText(prefs.getString("ai_endpoint","https://api.openai.com/v1"));row("Endpoint",endpoint);
        EditText api=field("API key");api.setInputType(129);api.setText(prefs.getString("api_key",""));row("API key",api);
        section("VOICE");
        check("Text-to-speech enabled","voice_enabled",true);
        seek("Speech speed","voice_rate",50,200,94);
        seek("Voice pitch","voice_pitch",50,200,100);
        check("Speak character replies aloud","voice_replies",true);
        section("CHARACTER");
        seek("Character scale","character_scale",40,180,100);
        seek("Animation intensity","animation_intensity",0,150,100);
        check("Glitch effects","glitch_effects",true);
        check("Always on top / floating companion","always_on_top",true);
        check("Start Capri overlay automatically","overlay_enabled",false);
        section("BEHAVIOR");
        check("Idle remarks (silent bubble)","idle_remarks",false);
        check("Do not disturb","do_not_disturb",false);
        check("Quiet hours (22:00–08:00)","quiet_hours",true);
        check("Hide character while typing","hide_while_typing",false);
        section("MEMORY");
        check("Remember conversation context","memory_enabled",true);
        seek("Conversation turns to keep","history_turns",0,40,10);
        check("Save chat history on this device","save_history",true);
        section("PRIVACY");
        check("Ask before reading the screen","screen_confirm",true);
        check("Allow screen context when I request it","screen_context",true);
        check("Keep screen context active while Capri is open","screen_monitor",false);
        check("Send screen images only after I ask","screen_on_demand",true);
        TextView privacy=text("Screen capture is requested through Android MediaProjection. The system shows its own consent prompt and capture indicator. Images are sent to the selected AI provider only when you request screen analysis.",11,0xffa8a5b5);privacy.setPadding(0,dp(4),0,dp(12));page.addView(privacy);
        section("SYSTEM");
        check("Launch overlay after returning to Capri","restore_overlay",true);
        check("Use immersive full-screen chat","immersive",true);
        Button save=button("SAVE SETTINGS");LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(50));sp.topMargin=dp(18);page.addView(save,sp);
        save.setOnClickListener(v->{
            String p=providers[provider.getSelectedItemPosition()];
            prefs.edit().putString("ai_provider",p).putString("ai_model",model.getText().toString().trim()).putString("ai_endpoint",endpoint.getText().toString().trim()).putString("api_key",api.getText().toString().trim()).apply();
            Toast.makeText(this,"Configurações salvas, choom.",Toast.LENGTH_SHORT).show();finish();
        });
        Button close=button("BACK TO CAPRI");page.addView(close,new LinearLayout.LayoutParams(-1,dp(46)));close.setOnClickListener(v->finish());
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
