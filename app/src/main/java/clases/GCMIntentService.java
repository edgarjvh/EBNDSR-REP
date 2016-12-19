package clases;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.fitness.data.Goal;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.villasoftgps.ebndsrrep.Frm_Principal;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import java.lang.reflect.Type;
import java.util.ArrayList;
import vistas.lvMensajesItems;
import static com.google.android.gms.gcm.GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE;

public class GCMIntentService extends IntentService
{
    private static NotificationManager mNotificationManager;
    private static final String TAG = "EJVH";
    private SharedPreferences sPrefs;
    private static final String PREF_NAME = "prefSchoolTool";
    private static final String PROPERTY_USER = "user";
    private static final String PROPERTY_CONVERSATIONS = "conversations";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_EXPIRATION_TIME = "onServerExpirationTimeMs";
    private static final int EXPIRATION_TIME_MS = 1000*3600*24*7;
    private Representante representante;
    private lvMensajesItems mensaje;
    private ArrayList<lvMensajesItems> conversaciones;
    private Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();
        representante = new Representante();
        conversaciones = new ArrayList<>();
        gson = new Gson();
    }

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        //se instancia la herramienta de google cloud messaging
        GoogleCloudMessaging GCM = GoogleCloudMessaging.getInstance(this);

        // se declara el tipo de mensaje entrante y se obtienen los extras en caso de existir
        String messageType = GCM.getMessageType(intent);
        Bundle extras = intent.getExtras();

        // se verifica si hay extras en el mensaje entrante
        if (!extras.isEmpty())
        {
            //noinspection deprecation
            if (MESSAGE_TYPE_MESSAGE.equals(messageType))
            {

                try {
                    JSONObject jsonObj = new JSONObject(extras.getString("mensaje"));
                    String result = jsonObj.get("Result").toString();

                    if (sPrefs == null){
                        sPrefs = getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    }

                    switch (result){
                        case "INCOMING":
                            JSONObject array = new JSONObject(jsonObj.get("Mensaje").toString());

                            if (array.getInt("Via") == 0){ // se valida que sea un docente quien este enviando el msj
                                // se convierte la fecha quitando caracteres no numericos

                                String value = "";
                                if (array.getString("fechaHora").matches("^/Date\\(\\d+\\)/$")) {
                                    value = array.getString("fechaHora").replaceAll("^/Date\\((\\d+)\\)/$", "$1");
                                }

                                mensaje = new lvMensajesItems(
                                        0,
                                        array.getInt("IdMensaje"),
                                        array.getInt("Via"),
                                        array.getInt("IdDocente"),
                                        array.getInt("IdRepresentante"),
                                        array.getInt("Estado"),
                                        Long.parseLong(value),
                                        array.getString("Texto")
                                );

                                if (sPrefs.getString(PROPERTY_CONVERSATIONS,"").equals("")){
                                    conversaciones.add(mensaje);
                                    SharedPreferences.Editor sEditor = sPrefs.edit();
                                    sEditor.putString(PROPERTY_CONVERSATIONS, gson.toJson(conversaciones));
                                    sEditor.apply();
                                }else{
                                    Type type = new TypeToken<ArrayList<lvMensajesItems>>() {}.getType();
                                    conversaciones = gson.fromJson(sPrefs.getString(PROPERTY_CONVERSATIONS,""),type);
                                    conversaciones.add(mensaje);
                                    SharedPreferences.Editor sEditor = sPrefs.edit();
                                    sEditor.putString(PROPERTY_CONVERSATIONS, gson.toJson(conversaciones));
                                    sEditor.apply();
                                }

                                Frm_Principal.actualizarConversaciones();
                            }

                            break;
                        case "UPDATE":

                            /*
                                Result = "UPDATE",
                                IdMensaje = idMensaje,
                                Estado = estado,
                                Via = via,
                                IdDocente = idDocente,
                                IdRepresentante = idRepresentante
                            */

                            if (jsonObj.get("Via").toString().equals("1")){ // si el docente esta confirmando....
                                if (!sPrefs.getString(PROPERTY_CONVERSATIONS,"").equals("")){
                                    Type type = new TypeToken<ArrayList<lvMensajesItems>>() {}.getType();
                                    conversaciones = gson.fromJson(sPrefs.getString(PROPERTY_CONVERSATIONS,""),type);

                                    for (int i = 0; i < conversaciones.size();i++){
                                        if (conversaciones.get(i).getIdMensaje() == Integer.parseInt(jsonObj.get("IdMensaje").toString()) &&
                                            conversaciones.get(i).getIdDocente() == Integer.parseInt(jsonObj.get("IdDocente").toString()) &&
                                            conversaciones.get(i).getIdRepresentante() == Integer.parseInt(jsonObj.get("IdRepresentante").toString())){

                                            conversaciones.get(i).setStatus(Integer.parseInt(jsonObj.get("Estado").toString()));
                                            break;
                                        }
                                    }

                                    SharedPreferences.Editor sEditor = sPrefs.edit();
                                    sEditor.putString(PROPERTY_CONVERSATIONS, gson.toJson(conversaciones));
                                    sEditor.apply();

                                    Frm_Principal.actualizarConversaciones();
                                }
                            }

                            break;
                        default:
                            // ERROR
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //handleMessage();
            }
        }

        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private class AsyncConfirmarRecepcion extends AsyncTask<Object,Integer,Integer>{

        @Override
        protected Integer doInBackground(Object... params) {
            ArrayList<Object>  parametros = new ArrayList<>(3);
            parametros.add(0, "idMensaje*" + params[0]);
            parametros.add(1, "estado*" + params[1]);
            parametros.add(2, "confirmarMensaje");

            respuesta ws = new respuesta();
            Object response = ws.getData(parametros);

            try
            {
                JSONObject jsonObj = new JSONObject(response.toString());
                String result = jsonObj.get("Result").toString();

                switch (result) {
                    case "CONFIRMADO":
                        /*
                                Result = "CONFIRMADO",
                                Via = via,
                                IdDocente = idDocente,
                                IdRepresentante = idRepresentante,
                                IdMensaje = idMensaje,
                                Estado = estado
                        */

                        Log.d("EJVH CONFIRMACION", response.toString());
                        break;
                    default:
                        break;
                }
                return null;
            }
            catch (JSONException e) {
                return null;
            }
        }
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

    /*private class AsyncActualizarEstadoNotificacion extends AsyncTask<String,Integer,String> {
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
    }*/

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

    private class respuesta {
        Object getData(ArrayList<Object> parametros){
            Object data;
            String namespace = "http://schooltool.org/";
            String direccion = "http://154.42.65.212:9600/schooltool.asmx";
            String metodo = parametros.get(parametros.size() - 1).toString();
            String soapAction = namespace + metodo;

            SoapObject request = new SoapObject(namespace, metodo);
            String property[];
            PropertyInfo pi;

            for (int i = 0; i < parametros.size() - 1; i++){
                property = parametros.get(i).toString().split("\\*");
                pi = new PropertyInfo();
                pi.setName(property[0]);
                pi.setValue(property[1]);
                pi.setType(property[1].getClass());
                request.addProperty(pi);
            }

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE httpTransport = new HttpTransportSE(direccion);

            try {
                httpTransport.call(soapAction, envelope);
                data = envelope.getResponse();
            } catch (Exception exception) {
                data = exception.toString();
            }

            return data;
        }
    }
}