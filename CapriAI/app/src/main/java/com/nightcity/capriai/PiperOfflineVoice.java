package com.nightcity.capriai;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import com.k2fsa.sherpa.onnx.*;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** On-device Brazilian Portuguese neural voice. Download once, then synthesize offline. */
public final class PiperOfflineVoice {
    private static final String MODEL_DIR="vits-piper-pt_BR-faber-medium";
    private static final String ARCHIVE_URL="https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-pt_BR-faber-medium.tar.bz2";
    private static final ExecutorService WORKER=Executors.newSingleThreadExecutor();
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    public interface DownloadListener{void onProgress(int percent);void onComplete(String error);}
    public interface PlaybackListener{void onStart();void onFinish(String error);}
    private PiperOfflineVoice(){}
    private static File root(Context c){return new File(c.getFilesDir(),"capri_voice");}
    private static File modelRoot(Context c){return new File(root(c),MODEL_DIR);}
    public static boolean isReady(Context c){File d=modelRoot(c);File model=new File(d,"pt_BR-faber-medium.onnx"),tokens=new File(d,"tokens.txt"),data=new File(d,"espeak-ng-data");return model.isFile()&&model.length()>20_000_000L&&tokens.isFile()&&data.isDirectory();}
    public static void download(Context context,DownloadListener listener){Context app=context.getApplicationContext();WORKER.execute(()->{String error=null;File archive=new File(app.getCacheDir(),"capri-ptbr-voice.tar.bz2");try{HttpURLConnection c=(HttpURLConnection)new URL(ARCHIVE_URL).openConnection();c.setConnectTimeout(20000);c.setReadTimeout(180000);c.setInstanceFollowRedirects(true);c.setRequestProperty("User-Agent","CapriAI-Android/1.0");int status=c.getResponseCode();if(status<200||status>=300)throw new IOException("Download HTTP "+status);int length=c.getContentLength();try(InputStream in=new BufferedInputStream(c.getInputStream());FileOutputStream out=new FileOutputStream(archive)){byte[] buf=new byte[32768];int n;long total=0;while((n=in.read(buf))!=-1){out.write(buf,0,n);total+=n;if(length>0){int pct=(int)Math.min(85,total*85L/length);MAIN.post(()->listener.onProgress(pct));}}}finally{c.disconnect();}File base=root(app);if(!base.exists()&&!base.mkdirs())throw new IOException("Não consegui criar a pasta da voz.");long extracted=0;try(InputStream file=new BufferedInputStream(new FileInputStream(archive));BZip2CompressorInputStream bz=new BZip2CompressorInputStream(file);TarArchiveInputStream tar=new TarArchiveInputStream(bz)){TarArchiveEntry entry;while((entry=tar.getNextTarEntry())!=null){String name=entry.getName().replace('\\','/');if(name.startsWith("/")||name.contains("../"))throw new IOException("Arquivo inválido no pacote de voz.");File dest=new File(base,name);if(!dest.getCanonicalPath().startsWith(base.getCanonicalPath()+File.separator))throw new IOException("Caminho inválido no pacote de voz.");if(entry.isDirectory()){if(!dest.exists()&&!dest.mkdirs())throw new IOException("Falha ao criar pasta da voz.");continue;}if(!entry.isFile())continue;File parent=dest.getParentFile();if(parent!=null&&!parent.exists()&&!parent.mkdirs())throw new IOException("Falha ao criar pasta.");try(FileOutputStream out=new FileOutputStream(dest)){byte[] buf=new byte[32768];int n;while((n=tar.read(buf))!=-1){out.write(buf,0,n);extracted+=n;if(extracted>220_000_000L)throw new IOException("Pacote de voz excedeu o limite de segurança.");}}}}if(!isReady(app))throw new IOException("O modelo baixado está incompleto. Tente novamente.");}catch(Exception e){error=e.getClass().getSimpleName()+": "+e.getMessage();}finally{if(archive.exists())archive.delete();}final String result=error;MAIN.post(()->listener.onComplete(result));});}
    public static void speak(Context context,String text,PlaybackListener listener){Context app=context.getApplicationContext();WORKER.execute(()->{String error=null;try{if(!isReady(app))throw new IOException("A voz offline ainda não foi baixada.");File dir=modelRoot(app);OfflineTtsVitsModelConfig vits=new OfflineTtsVitsModelConfig();vits.setModel(new File(dir,"pt_BR-faber-medium.onnx").getAbsolutePath());vits.setTokens(new File(dir,"tokens.txt").getAbsolutePath());vits.setDataDir(new File(dir,"espeak-ng-data").getAbsolutePath());OfflineTtsModelConfig model=new OfflineTtsModelConfig();model.setVits(vits);model.setNumThreads(1);model.setDebug(false);OfflineTtsConfig config=new OfflineTtsConfig();config.setModel(model);config.setMaxNumSentences(1);OfflineTts tts=new OfflineTts(config);GeneratedAudio audio;try{audio=tts.generate(text==null?"":text.length()>650?text.substring(0,650):text,0,1.04f);}finally{tts.release();}float[] samples=audio.getSamples();int rate=audio.getSampleRate();if(samples==null||samples.length==0)throw new IOException("A voz não gerou áudio.");MAIN.post(listener::onStart);play(samples,rate);}catch(Exception e){error=e.getClass().getSimpleName()+": "+e.getMessage();}final String result=error;MAIN.post(()->listener.onFinish(result));});}
    private static void play(float[] samples,int rate)throws Exception{int min=AudioTrack.getMinBufferSize(rate,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);int buffer=Math.max(8192,min);AudioTrack track=new AudioTrack(AudioManager.STREAM_MUSIC,rate,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT,buffer,AudioTrack.MODE_STREAM);try{track.play();short[] chunk=new short[2048];int offset=0;while(offset<samples.length){int count=Math.min(chunk.length,samples.length-offset);for(int i=0;i<count;i++){float v=Math.max(-1f,Math.min(1f,samples[offset+i]));chunk[i]=(short)(v*32767f);}int written=track.write(chunk,0,count);if(written<=0)throw new IOException("Falha ao reproduzir áudio: "+written);offset+=written;}Thread.sleep(Math.max(100L,(long)samples.length*1000L/rate+100L));track.stop();}finally{track.release();}}
}