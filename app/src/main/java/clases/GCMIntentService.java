package clases;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.villasoftgps.ebndsrrep.Frm_Principal;
import com.villasoftgps.ebndsrrep.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GCMIntentService extends IntentService
{
    private static NotificationManager mNotificationManager;
    private Bundle extras;
    private static final String TAG = "EJVH";
    private GoogleCloudMessaging GCM;
    private SharedPreferences sPrefs;
    private static final String PREF_NAME = "prefSchoolTool";
    private static final String PROPERTY_USER = "user";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_EXPIRATION_TIME = "onServerExpirationTimeMs";
    private static final int EXPIRATION_TIME_MS = 1000*3600*24*7;

    private Set<String> setNot;
    private String cedula;
    private String mensaje;
    private String tipoMensaje;
    private String matricula;
    private String fechaHora;
    private String idNotificacion;
    private String gcmId;
    private String mostrado;
    static int timeInADay = 86400000;
    private Date fechaActual = null;

    @Override
    public void onCreate() {
        super.onCreate();
        fechaActual = new Date();
        fechaActual.setTime(System.currentTimeMillis());
        //timer.start();
    }

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        /*Log.d(TAG + " NOTI","0");

        if(fechaActual != null){
            if(fechaActual.getTime() < (System.currentTimeMillis() - 10000)){
                timer.start();
            }
        }

        Log.d(TAG + " NOTI","1");

        //se instancia la herramienta de google cloud messaging
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        Log.d(TAG + " NOTI","2");

        // se declara el tipo de mensaje entrante y se obtienen los extras en caso de existir
        String messageType = gcm.getMessageType(intent);
        extras = intent.getExtras();

        Log.d(TAG + " NOTI","3");

        // se verifica si hay extras en el mensaje entrante
        if (!extras.isEmpty())
        {
            Log.d(TAG + " NOTI","4");

            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
                Log.d(TAG + " NOTI","5");

                cedula = extras.getString("cedula","");
                matricula = extras.getString("matricula");
                fechaHora = extras.getString("fecha","");
                mensaje = extras.getString("mensaje", "");
                gcmId = extras.getString("gcmid", "");
                idNotificacion = extras.getString("idNotificacion", "");
                tipoMensaje = extras.getString("tipoMensaje","");
                mostrado = "0";

                Log.d(TAG + " NOTI","6");

                handleMessage();
            }
        }

        GCMBroadcastReceiver.completeWakefulIntent(intent);*/
    }

    /*private void handleMessage() {
        Log.d(TAG + " NOTI","7");

        // si obtienen las preferencias compartidas
        if (sPrefs == null){
            sPrefs = getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }

        Log.d(TAG + " NOTI","8");
        Log.d(TAG + " NOTI","9 ==> " + cedula);
        Log.d(TAG + " NOTI","9 ==> " + sPrefs.getString(PROPERTY_USER,"cedula nula"));

        // se verifica si la cedula del mensaje es igual a la del usuario logueado
        if(cedula.equals(sPrefs.getString(PROPERTY_USER,""))){
            // ============= VERIFICAR SI LA NOTIFICACION EXISTE EN PREFS ============
            Log.d(TAG + " NOTI","10");

            // se verifica si la lista de notificaciones no es nula
            if(setNot != null){
                Log.d(TAG + " NOTI","11");
                // se verifica si la lista de notificaciones no está vacía
                if(!setNot.isEmpty()){
                    Log.d(TAG + " NOTI","12");

                    for(String noti : setNot){
                        Log.d(TAG + " NOTI","13");
                        String data[] = noti.split("\\*");

                        Log.d(TAG + " idNotificacion",idNotificacion);
                        Log.d(TAG + " data4",data[4]);

                        if(data[4].equals(idNotificacion)){// SI EXISTE EN PREFS
                            Log.d(TAG + " NOTI","14");
                            if (data[5] == "1"){ //si fue mostrado al usuario
                                Log.d(TAG + " NOTI","15");
                                // CAMBIAR STATUS EN SERVIDOR
                                new AsyncActualizarEstadoNotificacion().execute();
                                break;
                            }else{ // si no ha sido mostrado
                                Log.d(TAG + " NOTI","16");
                                //MOSTRAR
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date date = null;
                                try {
                                    sdf.parse(fechaHora);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                //mostrarNotification(cedula, mensaje, String.format("dd-MM-yyyy hh:mm:ss aa", fechaHora), gcmId, idNotificacion);
                                // CAMBIAR STATUS EN SERVIDOR
                                new AsyncActualizarEstadoNotificacion().execute();
                                break;
                            }
                        }else{// SI NO EXISTE EN PREFS
                            Log.d(TAG + " NOTI","17");
                            //MOSTRAR
                            //mostrarNotification(cedula, mensaje, String.format("dd-MM-yyyy hh:mm:ss aa", fechaHora), gcmId, idNotificacion);
                            // CAMBIAR STATUS EN SERVIDOR
                            //new AsyncActualizarEstadoNotificacion().execute();
                            break;
                        }
                    }
                }else{
                    Log.d(TAG + " NOTI","18");
                    //MOSTRAR
                    //mostrarNotification(cedula,mensaje, String.format("dd-MM-yyyy hh:mm:ss aa", fechaHora),gcmId,idNotificacion);
                    // CAMBIAR STATUS EN SERVIDOR
                    //new AsyncActualizarEstadoNotificacion().execute();
                }
            }else{
                Log.d(TAG + " NOTI","19");
                //MOSTRAR
                //mostrarNotification(cedula, mensaje, String.format("dd-MM-yyyy hh:mm:ss aa", fechaHora), gcmId, idNotificacion);
                // CAMBIAR STATUS EN SERVIDOR
                //new AsyncActualizarEstadoNotificacion().execute();
            }
        }else{ // si no esta logueado
            Log.d(TAG + " NOTI","20");
            if(setNot != null){
                Log.d(TAG + " NOTI","21");
                // si hay registros en el array...
                if(!setNot.isEmpty()){
                    Log.d(TAG + " NOTI","22");
                    for(String noti : setNot){
                        Log.d(TAG + " NOTI","23");
                        String data[] = noti.split("\\*");

                        if(data[4].equals(idNotificacion)){// SI EXISTE EN PREFS
                            Log.d(TAG + " NOTI","24");
                            if (data[5] == "1"){ //si fue mostrado al usuario
                                Log.d(TAG + " NOTI","25");
                                // CAMBIAR STATUS EN SERVIDOR
                                new AsyncActualizarEstadoNotificacion().execute();
                                break;
                            }
                        }else{// SI NO EXISTE EN PREFS
                            Log.d(TAG + " NOTI","26");
                            guardarEnPrefs();
                            break;
                        }
                    }
                }else{
                    Log.d(TAG + " NOTI", "27");
                    guardarEnPrefs();
                }
            }else{
                Log.d(TAG + " NOTI","28");
                guardarEnPrefs();
            }
        }
    }*/

    /*private void mostrarNotification(String cedula, String mensaje, String fecha, String gcmId, String idNotificacion)
    {
        Log.d(TAG + " NOTI","29");
        if(sPrefs == null){
            sPrefs = getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }

        //se verifica si el usuario esta loqueado en la aplicación

            Log.d(TAG + " NOTI","30");
            //se verifica si existe un registro gcm, y si coincide con el id recibido
            String regId = sPrefs.getString(PROPERTY_REG_ID,"");

        Log.d(TAG + " REGID",regId);
        Log.d(TAG + " GCMID",gcmId);

            if (!regId.equals("") && regId.equals(gcmId)){
                Log.d(TAG + " NOTI","31");
                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Notification noti = setCustomViewNotification(fecha, mensaje);
                noti.defaults |= Notification.DEFAULT_LIGHTS;
                noti.defaults |= Notification.DEFAULT_VIBRATE;
                noti.defaults |= Notification.DEFAULT_SOUND;
                noti.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
                noti.priority= Notification.PRIORITY_MAX;
                mNotificationManager.notify(0, noti);

                mostrado = "1";
                //Actualizar estatus de la notificacion en la base de datos
                new AsyncActualizarEstadoNotificacion().execute(idNotificacion, "1");
            }else{
                guardarEnPrefs();
            }
    }*/

    /*private Notification setCustomViewNotification(String fecha, String mensaje) {

        // Creates an explicit intent for an ResultActivity to receive.
        // Creamos el Intent que llamará a nuestra Activity
        Intent targetIntent = new Intent(getApplicationContext(),Frm_Principal.class);

        PendingIntent pIntent = PendingIntent.getActivity(getBaseContext(), 0, targetIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Create remote view and set bigContentView.
        RemoteViews expandedView = new RemoteViews(this.getPackageName(), R.layout.custom_notification);
        expandedView.setImageViewResource(R.id.imgNotIcon, R.drawable.icon);
        expandedView.setTextViewText(R.id.lblNotFecha, fecha);
        expandedView.setTextViewText(R.id.lblNotMensaje, mensaje);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setContentTitle("ZUprevención")
                .setContentText("Deslice hacia abajo...")
                .build();

        notification.bigContentView = expandedView;

        return notification;
    }*/

    private class AsyncActualizarEstadoNotificacion extends AsyncTask<String,Integer,String> {
        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG + " NOTI", "A");
            Object data;
            String namespace = "http://cctv.zuprevencion.org/";
            String direccion = "http://cctv.zuprevencion.org:9650/servicioWeb.asmx";
            String metodo = "guardarNotificacionEnviada";
            String soapAction = namespace + metodo;

            Log.d(TAG + " NOTI", "B");

            SoapObject request = new SoapObject(namespace, metodo);
            PropertyInfo pi;

            Log.d(TAG + " NOTI", "C " + idNotificacion);

            pi = new PropertyInfo();
            pi.setName("idNotificacion");
            pi.setValue(idNotificacion);
            pi.setType(String.class);
            request.addProperty(pi);

            Log.d(TAG + " NOTI", "D " + cedula);

            pi = new PropertyInfo();
            pi.setName("cedula");
            pi.setValue(cedula);
            pi.setType(String.class);
            request.addProperty(pi);

            Log.d(TAG + " NOTI", "E " + matricula);

            pi = new PropertyInfo();
            pi.setName("matricula");
            pi.setValue(matricula);
            pi.setType(String.class);
            request.addProperty(pi);

            Log.d(TAG + " NOTI", "F " + tipoMensaje);

            pi = new PropertyInfo();
            pi.setName("tipoMensaje");
            pi.setValue(tipoMensaje);
            pi.setType(String.class);
            request.addProperty(pi);

            Log.d(TAG + " G", fechaHora);

            pi = new PropertyInfo();
            pi.setName("fechaHora");
            pi.setValue(fechaHora);
            pi.setType(String.class);
            request.addProperty(pi);

            Log.d(TAG + " NOTI", "H");

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE httpTransport = new HttpTransportSE(direccion);

            Log.d(TAG + " NOTI", "I");

            try {
                Log.d(TAG + " NOTI", "J");

                httpTransport.call(soapAction, envelope);
                data = envelope.getResponse();
                Log.d(TAG + " NOTI", "K");
            } catch (Exception exception) {
                Log.d(TAG + " NOTI", "L");
                data = exception.toString();
            }

            Log.d(TAG + " NOTI", "M");

            if(data != null){
                Log.d(TAG + " NOTI", data.toString());
                return data.toString();
            }else{
                Log.d(TAG + " NOTI", "N");
                return "0";
            }
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            Log.d(TAG + " NOTI", "32");

            if(sPrefs == null){
                sPrefs = getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            }

            if(data.equals("1")){//SI FUE ACTUALIZADO EN LA BD SE PROCEDE A ELIMINAR DE PREFS
                Log.d(TAG + " NOTI","33");
                //limpiarPrefs(ModoLimpiarPrefs.DEL_NOTI_BY_ID);

            }else{//SI NO FUE ACTUALIZADO EN LA BD SE PROCEDE A GUARDAR LA NOTIFICACION EN PREFS COMO MOSTRADO
                Log.d(TAG + " NOTI","34");
                //guardarEnPrefs();
            }
        }
    }

    private enum ModoLimpiarPrefs{
        DEL_NOTI_BY_ID,
        DEL_NOTI_BY_SHOWED_REPORTED,
        DEL_NOTI_OLDER_3DAYS
    }

    /*private void limpiarPrefs(ModoLimpiarPrefs modo){
        Log.d(TAG + " NOTI","35");
        if(sPrefs == null){
            sPrefs = getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }

        Log.d(TAG + " NOTI","36");

        Set<String> tempSet = sPrefs.getStringSet(PROPERTY_NOTIFICATIONS_LIST,null);

        if(tempSet != null){
            Log.d(TAG + " NOTI","37");
            if(!tempSet.isEmpty()){
                Log.d(TAG + " NOTI","38");
                switch (modo){
                    case DEL_NOTI_BY_ID:
                        Log.d(TAG + " NOTI","39");
                        for(String noti : tempSet){
                            String aTemp[] = noti.split("\\*");

                            if(aTemp[4].equals(idNotificacion)){
                                tempSet.remove(noti);
                                Log.d(TAG + " NOTI", "39A");
                                break;
                            }
                        }

                        sEditor = sPrefs.edit();
                        sEditor.putStringSet(PROPERTY_NOTIFICATIONS_LIST,tempSet);
                        sEditor.commit();

                        break;
                    case DEL_NOTI_OLDER_3DAYS:
                        Log.d(TAG + " NOTI","40");
                        List<String> lista = new ArrayList<>();

                        for(String noti : tempSet){
                            String aTemp[] = noti.split("\\*");
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date fecha = null;

                            try {
                                fecha = format.parse(aTemp[1]);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            if((System.currentTimeMillis() - (timeInADay*3)) > fecha.getTime()){
                                lista.add(noti);
                            }
                        }

                        if(!lista.isEmpty()){
                            for(String obj : lista){
                                tempSet.remove(obj);
                            }
                        }

                        sEditor = sPrefs.edit();
                        sEditor.putStringSet(PROPERTY_NOTIFICATIONS_LIST,tempSet);
                        sEditor.commit();

                        break;
                }
            }
        }
    }*/

    /*private void guardarEnPrefs(){
        Log.d(TAG + " NOTI","41");
        if(sPrefs == null){
            sPrefs = getApplicationContext().getSharedPreferences(LoginActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        }

        Set<String> tempSet = sPrefs.getStringSet(PROPERTY_NOTIFICATIONS_LIST,null);

        if(tempSet == null){
            tempSet = new HashSet<>();
        }

        tempSet.add(cedula + "*" + fechaHora + "*" + mensaje + "*" + gcmId + "*" + idNotificacion + "*" + mostrado);
        sEditor = sPrefs.edit();
        sEditor.putStringSet(PROPERTY_NOTIFICATIONS_LIST, tempSet);
        sEditor.commit();
    }*/

    /*CountDownTimer timer = new CountDownTimer(86400000,10000) {
        @Override
        public void onTick(long millisUntilFinished) {
            fechaActual.setTime(System.currentTimeMillis());

            if(sPrefs == null){
                sPrefs = getApplicationContext().getSharedPreferences(LoginActivity.class.getSimpleName(), Context.MODE_PRIVATE);
            }

            Set<String> curSet = sPrefs.getStringSet(PROPERTY_NOTIFICATIONS_LIST,null);

            if(curSet != null){
                int counter = 0;
                for(String str :curSet){
                    counter++;
                    Log.d("curSet " + counter,str);
                }
            }else{
                Log.d("curSet","null");
            }
        }

        @Override
        public void onFinish() {
            limpiarPrefs(ModoLimpiarPrefs.DEL_NOTI_OLDER_3DAYS);
            restartTimer();
        }
    };*/

    /*private void restartTimer() {
        timer.cancel();
        timer.start();
    }*/
}