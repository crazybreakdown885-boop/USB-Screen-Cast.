package com.nightcity.capriai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.*;
import android.content.SharedPreferences;

public class CapriStudioActivity extends Activity {
    private SharedPreferences prefs;
    private CapriCharacterView preview;
    private LinearLayout page;
    private final int bg=0xff0b0911, panel=0xff171321, pink=0xfff2154c, cyan=0xff28dff4, lime=0xff22f27a;
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    @Override public void onCreate(Bundle b){
        super.onCreate(b);prefs=getSharedPreferences("capri_settings",0);getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);page=new LinearLayout(this);page.setOrientation(1);page.setPadding(dp(16),dp(16),dp(16),dp(26));page.setBackgroundColor(bg);scroll.addView(page);
        TextView title=label("ESTÚDIO DA CAPRI",22,Color.WHITE);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);page.addView(title);
        page.addView(label("VISUAL • EXPRESSÕES • MOVIMENTOS",10,lime));
        preview=new CapriCharacterView(this);LinearLayout.LayoutParams pv=new LinearLayout.LayoutParams(-1,dp(270));pv.topMargin=dp(12);pv.bottomMargin=dp(12);page.addView(preview,pv);
        section("PALETA DE CORES");page.addView(label("Toque numa cor para aplicar na personagem.",12,0xffc2bdcc));
        addPalette("Cabelo / detalhes",new int[]{0xff8a3dff,0xfff2154c,0xff20dff2,0xffff9d32,0xffe7e7ef},"capri_hair_color");
        addPalette("Roupa principal",new int[]{0xfff2154c,0xff20dff2,0xff8a3dff,0xff252333,0xff22f27a},"capri_primary_color");
        addPalette("Olhos / brilho",new int[]{0xff22f27a,0xff20dff2,0xffffcf33,0xffff78bd,0xff1d6cff},"capri_accent_color");
        section("ROUPAS");String[] clothes={"Original Capri","Jaqueta neon","Moletom roxo","Casual escuro","Bunny girl (fofinha)"};String[] values={"original","neon","hoodie","casual","bunny"};
        Spinner outfit=new Spinner(this);outfit.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,clothes));String saved=prefs.getString("outfit_style","original");int ix=java.util.Arrays.asList(values).indexOf(saved);outfit.setSelection(ix<0?0:ix);page.addView(outfit,new LinearLayout.LayoutParams(-1,dp(48)));
        outfit.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> a){}public void onItemSelected(android.widget.AdapterView<?> a,android.view.View v,int p,long id){prefs.edit().putString("outfit_style",values[p]).apply();refresh();}});
        section("CHAPÉUS E ACESSÓRIOS");String[] hats={"Sem chapéu","Boina roxa","Touca neon","Boné Capri","Orelhas de coelhinha"};String[] hatValues={"none","beret","beanie","cap","bunny"};Spinner hat=new Spinner(this);hat.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,hats));String savedHat=prefs.getString("capri_hat_style","none");int hi=java.util.Arrays.asList(hatValues).indexOf(savedHat);hat.setSelection(hi<0?0:hi);page.addView(hat,new LinearLayout.LayoutParams(-1,dp(48)));hat.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> a){}public void onItemSelected(android.widget.AdapterView<?> a,android.view.View v,int p,long id){prefs.edit().putString("capri_hat_style",hatValues[p]).apply();refresh();}});
        TextView clothesNote=label("As peças são desenhadas por camadas sobre a personagem. A bunny girl é um visual fofo, sem conteúdo sexual.",10,0xff9993a7);clothesNote.setPadding(0,dp(5),0,dp(8));page.addView(clothesNote);
        section("ROSTOS E EXPRESSÕES");page.addView(label("Toque para ver a expressão no rosto da Capri.",12,0xffc2bdcc));
        String[] faceNames={"Neutra","Feliz","Curiosa","Surpresa","Triste","Irritada","Apaixonada","Envergonhada","Sonolenta","Confusa"};String[] moods={"neutra","feliz","curiosa","surpresa","triste","irritada","apaixonada","envergonhada","sonolenta","confusa"};
        for(int i=0;i<moods.length;i++){final String mood=moods[i];Button x=button(faceNames[i]);page.addView(x,new LinearLayout.LayoutParams(-1,dp(42)));x.setOnClickListener(v->{preview.setMood(mood);preview.react();});}
        section("ANIMAÇÕES E POSES");page.addView(label("Teste poses existentes. A personagem não troca de pose sozinha nesta galeria.",11,0xffc2bdcc));
        String[] poseNames={"Sentada","Deitada","Agachada","De costas"};String[] poses={"sit","lie","crouch","back"};
        for(int i=0;i<poses.length;i++){final String pose=poses[i];Button x=button("▶  "+poseNames[i]);page.addView(x,new LinearLayout.LayoutParams(-1,dp(42)));x.setOnClickListener(v->{preview.setPose(pose);preview.animate().cancel();preview.setRotation(-3f);preview.animate().rotation(0).translationY(-dp(8)).setDuration(350).withEndAction(()->preview.animate().translationY(0).setDuration(400).start()).start();});}
        section("MOVIMENTOS EXTRAS");String[] extraNames={"Dancinha","Giro","Pulinho","Reverência","Espiadinha"};for(int i=0;i<extraNames.length;i++){final int action=i;Button x=button("✦  "+extraNames[i]);page.addView(x,new LinearLayout.LayoutParams(-1,dp(42)));x.setOnClickListener(v->{preview.animate().cancel();switch(action){case 0:preview.animate().rotation(8).translationY(-dp(12)).setDuration(250).withEndAction(()->preview.animate().rotation(-8).translationY(0).setDuration(250).start()).start();break;case 1:preview.animate().rotationBy(360).setDuration(800).start();break;case 2:preview.animate().translationY(-dp(24)).setDuration(220).withEndAction(()->preview.animate().translationY(0).setDuration(260).start()).start();break;case 3:preview.animate().translationY(dp(15)).rotation(4).setDuration(300).withEndAction(()->preview.animate().translationY(0).rotation(0).setDuration(300).start()).start();break;default:preview.animate().translationX(-dp(14)).rotation(-5).setDuration(260).withEndAction(()->preview.animate().translationX(0).rotation(0).setDuration(260).start()).start();}});}
        Button reset=button("RESTAURAR VISUAL PADRÃO");LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(48));rp.topMargin=dp(16);page.addView(reset,rp);reset.setOnClickListener(v->{prefs.edit().remove("capri_primary_color").remove("capri_hair_color").remove("capri_accent_color").remove("capri_eye_color").remove("capri_hat_style").putString("outfit_style","original").apply();refresh();Toast.makeText(this,"Visual padrão restaurado.",Toast.LENGTH_SHORT).show();});
        Button close=button("VOLTAR");page.addView(close,new LinearLayout.LayoutParams(-1,dp(46)));close.setOnClickListener(v->finish());setContentView(scroll);
    }
    private void addPalette(String title,int[] colors,String key){TextView t=label(title,12,0xffc2bdcc);t.setPadding(0,dp(8),0,dp(4));page.addView(t);LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(4),0,dp(8));for(int color:colors){final int chosen=color;TextView swatch=new TextView(this);GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(30));d.setStroke(dp(2),Color.WHITE);swatch.setBackground(d);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(42),dp(42));lp.setMargins(0,0,dp(12),0);row.addView(swatch,lp);swatch.setOnClickListener(v->{prefs.edit().putInt(key,chosen).apply();refresh();Toast.makeText(this,"Cor aplicada.",Toast.LENGTH_SHORT).show();});}page.addView(row);}
    private void refresh(){if(preview!=null)preview.refreshStyle();}
    private void section(String s){TextView t=label("✦  "+s,14,cyan);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setPadding(0,dp(16),0,dp(6));page.addView(t);}
    private TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setTypeface(Typeface.DEFAULT);return t;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setTextSize(12);b.setAllCaps(false);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(panel));return b;}
}