package com.villasoftgps.ebndsrrep;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import java.util.ArrayList;
import clases.Representante;
import vistas.CustomProgress;

public class Frm_CambiarClave extends Activity {

    static SharedPreferences sPrefs;
    private static final String PREF_NAME = "prefSchoolTool";
    private static final String PROPERTY_USER = "user";
    Representante representante;
    CustomProgress dialogMessage = null;
    String mensaje = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_clave);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        sPrefs = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        Gson gson = new Gson();
        representante = gson.fromJson(sPrefs.getString(PROPERTY_USER,""),Representante.class);

        final EditText txtClaveActual = (EditText)findViewById(R.id.txtClaveActual);
        final EditText txtClaveNueva = (EditText)findViewById(R.id.txtClaveNueva);
        final EditText txtConfirmarClave = (EditText)findViewById(R.id.txtConfirmarClave);
        Button btnCambiarClave = (Button)findViewById(R.id.btnCambiarClave);

        btnCambiarClave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mensaje = "";

                if (txtClaveActual.getText().toString().equals("")){
                    mensaje = "Debe ingresar su contraseña actual";
                    txtClaveActual.requestFocus();
                    mostrarMensaje(false,false,1,mensaje);
                    return;
                }

                if (txtClaveNueva.getText().toString().equals("")){
                    mensaje = "Debe ingresar su nueva contraseña";
                    txtClaveNueva.requestFocus();
                    mostrarMensaje(false,false,1,mensaje);
                    return;
                }

                if (txtConfirmarClave.getText().toString().equals("")){
                    mensaje = "Debe confirmar su nueva contraseña";
                    txtConfirmarClave.requestFocus();
                    mostrarMensaje(false,false,1,mensaje);
                    return;
                }

                if(!txtClaveNueva.getText().toString().equals(txtConfirmarClave.getText().toString())){
                    mensaje = "La confirmación de la contraseña no coinciden";
                    txtClaveNueva.requestFocus();
                    mostrarMensaje(false,false,1,mensaje);
                    return;
                }

                Log.d("EJVH ID CLAVE", Integer.toString(representante.getId()));

                new AsyncCambiarClave().execute(
                        representante.getId(),
                        0,
                        txtClaveActual.getText().toString(),
                        txtClaveNueva.getText().toString()
                );
            }
        });

    }

    private class AsyncCambiarClave extends AsyncTask<Object,Integer,Integer>{

        @Override
        protected Integer doInBackground(Object... params) {
            publishProgress(0);
            ArrayList<Object> parametros = new ArrayList<>(5);
            parametros.add(0, "idRepresentante*" + params[0]);
            parametros.add(1, "idDocente*" + params[1]);
            parametros.add(2, "claveActual*" + params[2]);
            parametros.add(3, "claveNueva*" + params[3]);
            parametros.add(4, "cambiarClave");

            respuesta ws = new respuesta();
            Object response = ws.getData(parametros);

            try {
                JSONObject jsonObj = new JSONObject(response.toString());
                String result = jsonObj.get("Result").toString();

                Log.d("EJVH Result", result);
                switch (result) {
                    case "OK":
                        publishProgress(1);
                        return null;
                    case "NO PASS":
                        publishProgress(2);
                        return null;
                    case "NO ROWS":
                        publishProgress(3);
                        return null;
                    default:
                        publishProgress(4);
                        return null;
                }
            } catch (JSONException e) {
                mensaje = e.getMessage();
                publishProgress(5);
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            switch (values[0]){
                case 0:
                    mensaje = "Cambiando contraseña. Por favor espere...";
                    mostrarMensaje(false,true,0,mensaje);
                    break;
                case 1:
                    mensaje = "Contraseña cambiada exitosamente";
                    mostrarMensaje(true,false,0,mensaje);
                    break;
                case 2:
                    mensaje = "Contraseña actual incorrecta";
                    mostrarMensaje(false,false,1,mensaje);
                    break;
                case 3:
                    mensaje = "Representante no existe";
                    mostrarMensaje(false,false,1,mensaje);
                    break;
                case 4:
                    mensaje = "Error";
                    mostrarMensaje(false,false,2,mensaje);
                    break;
                default:
                    //mensaje = "Error de conexión";
                    mostrarMensaje(false,false,2,mensaje);
                    break;
            }
        }
    }

    private static class respuesta {
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

    @SuppressWarnings("ConstantConditions")
    private void mostrarMensaje(Boolean esBienvenida, Boolean enProgreso, int icono, String msj){
        try{
            if(esBienvenida){
                if(dialogMessage != null) {
                    dialogMessage.dismiss();
                    dialogMessage = null;
                }

                dialogMessage = new CustomProgress(Frm_CambiarClave.this,enProgreso,icono,msj);
                dialogMessage.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialogMessage.setCanceledOnTouchOutside(false);
                dialogMessage.show();

                CountDownTimer timer = new CountDownTimer(3000,1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @SuppressLint("CommitPrefEdits")
                    @Override
                    public void onFinish() {
                        if(dialogMessage != null){
                            dialogMessage.dismiss();
                            dialogMessage = null;
                        }
                        finish();
                    }
                };
                timer.start();
            }else{
                if(enProgreso){
                    if(dialogMessage != null){
                        dialogMessage.dismiss();
                        dialogMessage = null;
                    }

                    dialogMessage = new CustomProgress(Frm_CambiarClave.this,enProgreso,icono, msj);
                    dialogMessage.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialogMessage.setCanceledOnTouchOutside(true);
                    dialogMessage.show();
                }else{
                    if(dialogMessage != null) {
                        dialogMessage.dismiss();
                        dialogMessage = null;
                    }

                    dialogMessage = new CustomProgress(Frm_CambiarClave.this,enProgreso,icono,msj);
                    dialogMessage.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialogMessage.setCanceledOnTouchOutside(true);
                    dialogMessage.show();

                    CountDownTimer timer = new CountDownTimer(3000,1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                        }

                        @Override
                        public void onFinish() {
                            if(dialogMessage != null){
                                dialogMessage.dismiss();
                                dialogMessage = null;
                            }
                        }
                    };
                    timer.start();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
