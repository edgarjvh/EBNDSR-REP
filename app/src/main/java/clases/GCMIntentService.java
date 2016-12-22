package clases;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.google.android.gms.fitness.data.Goal;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.villasoftgps.ebndsrrep.Frm_Principal;
import com.villasoftgps.ebndsrrep.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import java.lang.reflect.Type;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import vistas.lvMensajesItems;
import vistas.lvMensajesItemsArrayAdapter;

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

                                if (!sPrefs.getString(PROPERTY_USER,"").equals("")){
                                    representante = gson.fromJson(sPrefs.getString(PROPERTY_USER,""),Representante.class);

                                    if (representante.getId() == array.getInt("IdRepresentante")){
                                        confirmarRecepcion(array.getInt("IdMensaje"),1);

                                        SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss aaa",new Locale("es","ES"));
                                        Date fecha = new Date(Long.parseLong(value));

                                        mostrarNotification(array.getString("Texto"),df.format(fecha),array.getString("NombreDocente"));
                                    }
                                }
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
                        case "UNREGISTERED":
                            gson = new Gson();
                            representante = gson.fromJson(sPrefs.getString(PROPERTY_USER,""),Representante.class);

                            if (representante.getId() == Integer.parseInt(jsonObj.get("IdRepresentante").toString())){
                                Frm_Principal.cerrarSesion();
                            }
                            break;

                        case "DOCREGISTERED":
                            Log.d("EJVH DOCREGISTERED", "idDocente: " + jsonObj.get("IdDocente").toString());
                            Frm_Principal.actualizarRegistroDocente(Integer.parseInt(jsonObj.get("IdDocente").toString()), 1);
                            break;

                        case "DOCUNREGISTERED":
                            Log.d("EJVH DOCUNREGISTERED", "idDocente: " + jsonObj.get("IdDocente").toString());
                            Frm_Principal.actualizarRegistroDocente(Integer.parseInt(jsonObj.get("IdDocente").toString()), 0);
                            break;
                        default:
                            // ERROR
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void confirmarRecepcion (int idMensaje, int estado){
        ArrayList<Object>  parametros = new ArrayList<>(3);
        parametros.add(0, "idMensaje*" + idMensaje);
        parametros.add(1, "estado*" + estado);
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
        }
        catch (JSONException e) {

        }

    }

    private void mostrarNotification(String mensaje, String fecha, String nombreDocente)
    {
        if(sPrefs == null){
            sPrefs = getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }

            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification noti = setCustomViewNotification(fecha, mensaje, nombreDocente);
            noti.defaults |= Notification.DEFAULT_LIGHTS;
            noti.defaults |= Notification.DEFAULT_VIBRATE;
            noti.defaults |= Notification.DEFAULT_SOUND;
            noti.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                noti.priority= Notification.PRIORITY_MAX;
            }
        mNotificationManager.notify(0, noti);
    }

    private Notification setCustomViewNotification(String fecha, String mensaje, String nombreDocente) {
        Intent targetIntent = new Intent(getApplicationContext(),Frm_Principal.class);

        //putextras para que al seleccionar la notificacion nos dirija a la conversacion

        PendingIntent pIntent = PendingIntent.getActivity(getBaseContext(), 0, targetIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Create remote view and set bigContentView.
        RemoteViews expandedView = new RemoteViews("com.villasoftgps.ebndsrrep", R.layout.custom_notification);
        expandedView.setImageViewResource(R.id.imgNotIcon, R.drawable.representante_icon);
        expandedView.setTextViewText(R.id.lblNombreDocente,nombreDocente);
        expandedView.setTextViewText(R.id.lblFecha, fecha);
        expandedView.setTextViewText(R.id.lblMensaje, mensaje);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.representante_icon)
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setContentTitle("Módulo Representantes")
                .setContentText("Deslice hacia abajo...")
                .build();

         notification.bigContentView = expandedView;


        return notification;
    }

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