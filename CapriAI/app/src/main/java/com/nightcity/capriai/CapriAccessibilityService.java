package com.nightcity.capriai;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Locale;

/** Opt-in screen context and user-triggered accessibility actions. */
public final class CapriAccessibilityService extends AccessibilityService {
    private static volatile CapriAccessibilityService instance;
    private static final int MAX_TEXT=5200;
    public static boolean isEnabled(Context context){String enabled=Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);if(enabled==null)return false;String target=ComponentName.flattenToString(new ComponentName(context,CapriAccessibilityService.class));for(String item:enabled.split(":"))if(target.equalsIgnoreCase(item))return true;return false;}
    public static boolean globalAction(int action){CapriAccessibilityService s=instance;return s!=null&&s.performGlobalAction(action);}
    public static boolean clickVisibleText(String text){CapriAccessibilityService s=instance;if(s==null||text==null||text.trim().isEmpty())return false;AccessibilityNodeInfo root=s.getRootInActiveWindow();if(root==null)return false;return s.clickNode(root,text.trim().toLowerCase(Locale.ROOT),0);}
    @Override protected void onServiceConnected(){super.onServiceConnected();instance=this;AccessibilityServiceInfo info=getServiceInfo();if(info!=null){info.eventTypes=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED|AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;info.feedbackType=AccessibilityServiceInfo.FEEDBACK_GENERIC;info.notificationTimeout=700;info.flags|=AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS|AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;setServiceInfo(info);}}
    @Override public void onAccessibilityEvent(AccessibilityEvent event){if(event==null||!getSharedPreferences("capri_settings",MODE_PRIVATE).getBoolean("screen_access_enabled",false))return;AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null)return;StringBuilder out=new StringBuilder(1200);collect(root,out,0);String text=out.toString().trim();if(text.length()>MAX_TEXT)text=text.substring(0,MAX_TEXT);getSharedPreferences("capri_settings",MODE_PRIVATE).edit().putString("active_screen_text",text).putString("active_screen_package",String.valueOf(event.getPackageName())).putLong("active_screen_at",System.currentTimeMillis()).apply();}
    private void collect(AccessibilityNodeInfo node,StringBuilder out,int depth){if(node==null||depth>18||out.length()>=MAX_TEXT)return;if(node.isPassword()||node.isEditable())return;append(out,node.getText());append(out,node.getContentDescription());for(int i=0;i<node.getChildCount()&&out.length()<MAX_TEXT;i++){AccessibilityNodeInfo child=node.getChild(i);if(child!=null)collect(child,out,depth+1);}}
    private void append(StringBuilder out,CharSequence value){if(value==null)return;String s=value.toString().trim();if(s.isEmpty()||s.length()>500)return;if(out.length()>0)out.append(" | ");out.append(s);}
    private boolean clickNode(AccessibilityNodeInfo node,String target,int depth){if(node==null||depth>18)return false;if(!node.isPassword()&&!node.isEditable()){if(matches(node.getText(),target)||matches(node.getContentDescription(),target)){if(node.isClickable()&&node.performAction(AccessibilityNodeInfo.ACTION_CLICK))return true;AccessibilityNodeInfo parent=node.getParent();if(parent!=null&&parent.isClickable()&&parent.performAction(AccessibilityNodeInfo.ACTION_CLICK))return true;}}for(int i=0;i<node.getChildCount();i++)if(clickNode(node.getChild(i),target,depth+1))return true;return false;}
    private boolean matches(CharSequence value,String target){return value!=null&&value.toString().toLowerCase(Locale.ROOT).contains(target);}
    @Override public void onInterrupt(){}
    @Override public void onDestroy(){if(instance==this)instance=null;super.onDestroy();}
}