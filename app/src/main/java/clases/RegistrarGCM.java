package clases;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("ALL")
public class RegistrarGCM {
    private GoogleCloudMessaging GCM;
    private SharedPreferences sPrefs;
    private String PREF_NAME = "prefSchoolTool";
    private static final String PROPERTY_USER = "user";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_EXPIRATION_TIME = "onServerExpirationTimeMs";
    private static final int EXPIRATION_TIME_MS = 1000*3600*24*7;
    private static final String SENDER_ID = "682250253357";
    private static final String SERVER_API = "AIzaSyBT6gPrznb0eP_hwa4ZM8HvG2auAz6ayn0";
    private Context context;
    private String tipoUsuario;
    private Representante representante;

    public RegistrarGCM(Context context, Object usuario){
        this.context = context;
        this.representante = (Representante)usuario;
        this.sPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        registrarGcm();
    }

    // ======================== SE INICIA EL PROCESO DE REGISTRO ================
    private void registrarGcm(){
        GCM = GoogleCloudMessaging.getInstance(context);
        String regID = obtenerRegistroGcm(context);

        if (regID.equals("")){
            new AsyncRegistrarGcm().execute();
        }
    }

    // ========================= SE VALIDA SI EL USUARIO YA SE HA REGISTRADO EN ESTE DISPOSITIVO
    private String obtenerRegistroGcm(Context context){
        String prefs_regID = sPrefs.getString(PROPERTY_REG_ID,"");
        int prefs_version = sPrefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        Long prefs_expiration = sPrefs.getLong(PROPERTY_EXPIRATION_TIME, -1);

        if(prefs_regID.equals("")){
            return "";
        }else if(prefs_version != currentVersion){ // si es una version distinta
            return "";
        }else if (System.currentTimeMillis() > prefs_expiration){ // si la fecha de expiracion es menor a la fecha actual
            return "";
        }else{
            Gson gson = new Gson();
            Representante _representante = gson.fromJson(sPrefs.getString(PROPERTY_USER,""),Representante.class);

            if (representante.getId() == _representante.getId()){
                return prefs_regID;
            }else{
                return "";
            }
        }
    }

    // ======================== PROCEDIMIENTO ASINCRONO DE REGISTRO
    private class AsyncRegistrarGcm extends AsyncTask<String,Integer,String> {
        @Override
        protected String doInBackground(String... params) {
            try{
                if(GCM == null){
                    GCM = GoogleCloudMessaging.getInstance(context);
                }

                String regID = GCM.register(SENDER_ID);

                ArrayList<Object> parametros = new ArrayList<>(8);
                parametros.add(0, "tipo*" + 1);
                parametros.add(1, "id*" + representante.getId());
                parametros.add(2, "regID*" + regID);
                parametros.add(3, "apiServidor*"+ SERVER_API);
                parametros.add(4, "dispositivo*" + getDeviceName());
                parametros.add(5, "versionAndroid*"+ Build.VERSION.RELEASE);
                parametros.add(6, "versionApp*"+ getAppVersion(context));
                parametros.add(7, "SaveGcmId");

                Respuesta ws = new Respuesta();
                Object response = ws.getData(parametros);

                JSONObject json = null;

                try {
                    json = new JSONObject(response.toString());
                    String result = json.get("Result").toString();

                    //Guardamos los datos del registro
                    if(result.equals("OK"))
                    {
                        setRegistrationId(context, regID);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
            return null;
        }
    }

    private int getAppVersion(Context context){
        try
        {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            throw new RuntimeException("Error al obtener versión: " + e);
        }
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

//        String phrase = "";
        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
//                phrase += Character.toUpperCase(c);
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
//            phrase += c;
            phrase.append(c);
        }

        return phrase.toString();
    }

    private void setRegistrationId(Context context, String regId){
        if (sPrefs == null){
            sPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }

        int appVersion = getAppVersion(context);
        Gson gson = new Gson();
        SharedPreferences.Editor sEditor = sPrefs.edit();
        sEditor.putString(PROPERTY_USER, gson.toJson(representante));
        sEditor.putString(PROPERTY_REG_ID, regId);
        sEditor.putInt(PROPERTY_APP_VERSION, appVersion);
        sEditor.putLong(PROPERTY_EXPIRATION_TIME, System.currentTimeMillis() + EXPIRATION_TIME_MS);
        sEditor.apply();
    }
}