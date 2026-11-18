package com.nightcity.capriai;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Encrypts the user's API key at rest with a key protected by Android Keystore. */
public final class ApiKeyStore {
    private static final String PREFS = "capri_secure";
    private static final String ALIAS = "capri_openai_api_key_v1";
    private static final String GEMINI_ALIAS = "capri_gemini_voice_api_key_v1";
    private ApiKeyStore() {}

    private static SecretKey getOrCreateKey() throws Exception { return getOrCreateKey(ALIAS); }
    private static SecretKey getOrCreateKey(String alias) throws Exception {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        if (ks.containsAlias(ALIAS)) {
            return ((KeyStore.SecretKeyEntry) ks.getEntry(ALIAS, null)).getSecretKey();
        }
        KeyGenerator generator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
        android.security.keystore.KeyGenParameterSpec spec =
            new android.security.keystore.KeyGenParameterSpec.Builder(
                alias,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT |
                android.security.keystore.KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build();
        generator.init(spec);
        return generator.generateKey();
    }

    public static void save(Context context, String value) throws Exception {
        String key = value == null ? "" : value.trim();
        if (key.regionMatches(true, 0, "Bearer ", 0, 7)) key = key.substring(7).trim();
        if (key.length() >= 2 && ((key.startsWith("\"") && key.endsWith("\"")) || (key.startsWith("\'") && key.endsWith("\'")))) key = key.substring(1, key.length()-1).trim();
        key = key.replaceAll("[\\r\\n\\t ]+", "");
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (key.isEmpty()) {
            p.edit().remove("cipher").remove("iv").apply();
            return;
        }
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        byte[] encrypted = cipher.doFinal(key.getBytes(StandardCharsets.UTF_8));
        p.edit()
            .putString("cipher", Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString("iv", Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP))
            .apply();
    }


    public static void saveGemini(Context context,String value) throws Exception {
        String key=value==null?"":value.trim();
        if(key.regionMatches(true,0,"Bearer ",0,7))key=key.substring(7).trim();
        key=key.replaceAll("[\\r\\n\\t ]+","");
        SharedPreferences p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        if(key.isEmpty()){p.edit().remove("gemini_cipher").remove("gemini_iv").apply();return;}
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,getOrCreateKey(GEMINI_ALIAS));
        byte[] encrypted=cipher.doFinal(key.getBytes(StandardCharsets.UTF_8));
        p.edit().putString("gemini_cipher",Base64.encodeToString(encrypted,Base64.NO_WRAP)).putString("gemini_iv",Base64.encodeToString(cipher.getIV(),Base64.NO_WRAP)).apply();
    }
    public static String getGemini(Context context){
        SharedPreferences p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        String encrypted=p.getString("gemini_cipher",""),iv=p.getString("gemini_iv","");
        try{if(!encrypted.isEmpty()&&!iv.isEmpty()){Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,getOrCreateKey(GEMINI_ALIAS),new GCMParameterSpec(128,Base64.decode(iv,Base64.NO_WRAP)));return new String(cipher.doFinal(Base64.decode(encrypted,Base64.NO_WRAP)),StandardCharsets.UTF_8);}}catch(Exception ignored){p.edit().remove("gemini_cipher").remove("gemini_iv").apply();}
        return "";
    }

    public static String get(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String encrypted = p.getString("cipher", "");
        String iv = p.getString("iv", "");
        try {
            if (!encrypted.isEmpty() && !iv.isEmpty()) {
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(),
                    new GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)));
                return new String(cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP)), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {
            p.edit().remove("cipher").remove("iv").apply();
        }
        // One-time migration from versions that stored the key in ordinary preferences.
        SharedPreferences old = context.getSharedPreferences("capri_settings", Context.MODE_PRIVATE);
        String legacy = old.getString("api_key", "");
        if (legacy != null && !legacy.trim().isEmpty()) {
            try {
                save(context, legacy);
                old.edit().remove("api_key").apply();
                return legacy.trim();
            } catch (Exception ignored) { return legacy.trim(); }
        }
        return "";
    }
}
