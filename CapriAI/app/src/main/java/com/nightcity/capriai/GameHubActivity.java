package com.nightcity.capriai;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

public class GameHubActivity extends Activity {
    LinearLayout root,area;
    int pink=0xffff315f,cyan=0xff35d9f5,green=0xff00ef83,bg=0xff070b12;
    Random rng=new Random();
    int score=0,round=0,expected=0;
    ArrayList<Integer> sequence=new ArrayList<>();
    Handler handler=new Handler(Looper.getMainLooper());
    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    TextView text(String s,int z,int c){TextView t=new TextView(this);t.setText(s);t.setTextColor(c);t.setTextSize(z);t.setGravity(17);t.setTypeface(Typeface.MONOSPACE);t.setPadding(dp(8),dp(8),dp(8),dp(8));return t;}
    Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setTextSize(13);b.setAllCaps(false);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(pink));return b;}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(16),dp(16),dp(16),dp(16));root.setBackgroundColor(bg);TextView title=text("CAPRI // ARCADE",22,cyan);root.addView(title);root.addView(text("MINIJOGOS OFFLINE • SEM INTERNET",11,green));area=new LinearLayout(this);area.setOrientation(1);root.addView(area,new LinearLayout.LayoutParams(-1,0,1));showMenu();setContentView(root);}
    void showMenu(){area.removeAllViews();addGame("⚡ REFLEXO","Toque quando a tela ficar verde.",()->reflex());addGame("🧠 MEMÓRIA","Repita a sequência de cores.",()->memory());addGame("❌ JOGO DA VELHA","Enfrente a Capri.",()->ticTacToe());Button back=btn("VOLTAR PARA CAPRI");root.addView(back);back.setOnClickListener(v->finish());}
    void addGame(String name,String desc,Runnable action){TextView d=text(desc,11,0xffb8c4d0);Button b=btn(name);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(52));p.topMargin=dp(12);area.addView(b,p);area.addView(d);b.setOnClickListener(v->action.run());}
    void reflex(){area.removeAllViews();score=0;TextView status=text("ESPERE A TELA FICAR VERDE...",17,Color.WHITE);area.addView(status);Button tap=btn("AGUARDE...");area.addView(tap,new LinearLayout.LayoutParams(-1,dp(150)));final boolean[] ready={false};final long[] start={0};tap.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff555b66));tap.setOnClickListener(v->{if(!ready[0]){status.setText("Queimou a largada! Tente de novo.");tap.setText("RECOMEÇAR");tap.setOnClickListener(w->reflex());}else{long ms=SystemClock.elapsedRealtime()-start[0];status.setText("Seu tempo: "+ms+" ms");tap.setText("JOGAR NOVAMENTE");tap.setOnClickListener(w->reflex());}});handler.postDelayed(()->{if(!isFinishing()&&tap.getParent()!=null){ready[0]=true;start[0]=SystemClock.elapsedRealtime();tap.setText("TOQUE AGORA!");tap.setBackgroundTintList(android.content.res.ColorStateList.valueOf(green));}},1200+rng.nextInt(3500));}
    void memory(){area.removeAllViews();sequence.clear();round=1;memoryRound();}
    void memoryRound(){area.removeAllViews();TextView status=text("FASE "+round+" • OBSERVE E REPITA",16,cyan);area.addView(status);int next=rng.nextInt(4);sequence.add(next);Button[] cells=new Button[4];LinearLayout row1=new LinearLayout(this);LinearLayout row2=new LinearLayout(this);for(int i=0;i<4;i++){final int k=i;cells[i]=btn(new String[]{"ROSA","AZUL","VERDE","ROXO"}[i]);cells[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(new int[]{pink,0xff2677ee,0xff00a86b,0xff8a45d6}[i]));(i<2?row1:row2).addView(cells[i],new LinearLayout.LayoutParams(0,dp(100),1));cells[i].setEnabled(false);cells[i].setOnClickListener(v->{if(k==sequence.get(expected)){expected++;if(expected==sequence.size()){round++;expected=0;handler.postDelayed(this::memoryRound,550);} }else{status.setText("Fim de jogo • você chegou à fase "+round);for(Button x:cells)x.setEnabled(false);}});}area.addView(row1);area.addView(row2);expected=0;final int[] idx={0};Runnable[] flash={null};flash[0]=()->{if(idx[0]>=sequence.size()){for(Button x:cells)x.setEnabled(true);return;}Button x=cells[sequence.get(idx[0]++)];x.setAlpha(.35f);handler.postDelayed(()->{x.setAlpha(1f);handler.postDelayed(flash[0],260);},420);};handler.postDelayed(flash[0],500);}
    void ticTacToe(){area.removeAllViews();final int[] board=new int[9];final Button[] cells=new Button[9];TextView status=text("SUA VEZ • VOCÊ É X",15,cyan);area.addView(status);GridLayout grid=new GridLayout(this);grid.setColumnCount(3);area.addView(grid);for(int i=0;i<9;i++){final int k=i;Button b=btn(" ");cells[i]=b;grid.addView(b,new ViewGroup.LayoutParams(dp(92),dp(92)));b.setOnClickListener(v->{if(board[k]!=0)return;board[k]=1;b.setText("X");if(win(board,1)){status.setText("Você venceu!");disable(cells);return;}int move=bestMove(board);if(move>=0){board[move]=2;cells[move].setText("O");}if(win(board,2)){status.setText("A Capri venceu. Hehe!");disable(cells);}else if(full(board))status.setText("Empate!");});}}
    int bestMove(int[] b){for(int i=0;i<9;i++)if(b[i]==0){b[i]=2;if(win(b,2)){b[i]=0;return i;}b[i]=0;}for(int i=0;i<9;i++)if(b[i]==0){b[i]=1;if(win(b,1)){b[i]=0;return i;}b[i]=0;}ArrayList<Integer> free=new ArrayList<>();for(int i=0;i<9;i++)if(b[i]==0)free.add(i);return free.isEmpty()?-1:free.get(rng.nextInt(free.size()));}
    boolean win(int[] b,int p){int[][] w={{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};for(int[] x:w)if(b[x[0]]==p&&b[x[1]]==p&&b[x[2]]==p)return true;return false;}
    boolean full(int[] b){for(int x:b)if(x==0)return false;return true;}void disable(Button[] b){for(Button x:b)x.setEnabled(false);}
}
