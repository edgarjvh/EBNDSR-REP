package com.villasoftgps.ebndsrrep;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
import controles.AutoResizeTextView;
import vistas.CustomProgress;

public class Frm_Login extends Activity {

    SharedPreferences sPrefs;
    SharedPreferences.Editor sEditor;
    private static  final String PREF_NAME = "prefSchoolTool";
    private static final String PROPERTY_USER = "user";

    Object response = null;
    String mensaje = "";
    Representante representante;
    CustomProgress dialogMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (sPrefs == null){
            sPrefs = getApplicationContext().getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        }

        if (!sPrefs.getString(PROPERTY_USER,"").equals("")){
            Intent frm = new Intent(Frm_Login.this, Frm_Principal.class);
            startActivity(frm);
            return;
        }

        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final EditText txtCedula = (EditText)findViewById(R.id.txtCedula);
        final EditText txtClave = (EditText)findViewById(R.id.txtClave);
        Button btnIngresar = (Button) findViewById(R.id.btnIngresar);

        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (txtCedula.getText().toString().trim().length() == 0){
                    mensaje = "Debe ingresar la cédula de identidad";
                    mostrarMensaje(false,false,1,mensaje);
                    return;
                }

                if (txtClave.getText().toString().trim().length() == 0){
                    mensaje = "Debe ingresar su contraseña";
                    mostrarMensaje(false,false,1,mensaje);
                    return;
                }
                new AsyncLogin().execute(txtCedula.getText().toString(),txtClave.getText().toString());
            }
        });
    }

    private class AsyncLogin extends AsyncTask<String, Integer, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            publishProgress(0);

            ArrayList<Object>  parametros = new ArrayList<>(3);
            parametros.add(0, "Cedula*" + params[0]);
            parametros.add(1, "Clave*"+ params[1]);
            parametros.add(2, "loginRepresentante");

            respuesta ws = new respuesta();
            response = ws.getData(parametros);

            try {
                JSONObject json = new JSONObject(response.toString());

                String result = json.get("Result").toString();

                switch (result) {
                    case "OK": {
                        JSONObject array = new JSONObject(json.get("Representante").toString());

                        representante = new Representante();
                        representante.setId(array.getInt("Id"));
                        representante.setCedula(array.getInt("Cedula"));
                        representante.setNombres(array.get("Nombres").toString());
                        representante.setApellidos(array.get("Apellidos").toString());
                        representante.setTelefono1(array.getInt("Telefono1"));
                        representante.setTelefono2(array.getInt("Telefono2"));
                        representante.setDireccion(array.get("Direccion").toString());
                        publishProgress(1);
                        return 1;

                    }
                    case "REGISTERED": {
                        JSONObject array = new JSONObject(json.get("Representante").toString());

                        representante = new Representante();
                        representante.setId(array.getInt("Id"));
                        representante.setCedula(array.getInt("Cedula"));
                        representante.setNombres(array.get("Nombres").toString());
                        representante.setApellidos(array.get("Apellidos").toString());
                        representante.setTelefono1(array.getInt("Telefono1"));
                        representante.setTelefono2(array.getInt("Telefono2"));
                        representante.setDireccion(array.get("Direccion").toString());
                        publishProgress(2);
                        return 1;
                    }
                    default:
                        mensaje = json.get("Message").toString();
                        publishProgress(3);
                        return 0;
                }
            } catch (JSONException e) {
                mensaje = e.getMessage();
                publishProgress(4);
                return 0;
            }
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            switch (values[0]){
                case 0:
                    mensaje = getResources().getString(R.string.iniciandoSesion);
                    mostrarMensaje(false,true,0,mensaje);
                    break;
                case 1:
                    mensaje = getResources().getString(R.string.bienvenidoCliente) + "\n" + representante.getNombres() + " " + representante.getApellidos();
                    mostrarMensaje(true, false, 0, mensaje);
                    break;
                case 2:
                    registeredDialog cpd = new registeredDialog(Frm_Login.this, representante);
                    cpd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    cpd.setCanceledOnTouchOutside(false);
                    cpd.show();

                    if(dialogMessage != null) {
                        dialogMessage.dismiss();
                        dialogMessage = null;
                    }
                    break;
                case 3:
                    mostrarMensaje(false,false,2,mensaje);
                    break;
                case 4:
                    mostrarMensaje(false,false,2,mensaje);
                    break;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
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

            if (parametros.size() > 0){
                String property[];
                PropertyInfo pi;

                property = parametros.get(0).toString().split("\\*");
                pi = new PropertyInfo();
                pi.setName(property[0]);
                pi.setValue(property[1]);
                pi.setType(Integer.class);
                request.addProperty(pi);

                property = parametros.get(1).toString().split("\\*");
                pi = new PropertyInfo();
                pi.setName(property[0]);
                pi.setValue(property[1]);
                pi.setType(String.class);
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

                dialogMessage = new CustomProgress(Frm_Login.this,enProgreso,icono,msj);
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
                        Intent i = new Intent(Frm_Login.this, Frm_Principal.class);

                        sEditor = sPrefs.edit();
                        Gson gson = new Gson();
                        String user = gson.toJson(representante);
                        sEditor.putString(PROPERTY_USER,user);
                        sEditor.apply();

                        startActivity(i);

                        if (dialogMessage != null){
                            dialogMessage.dismiss();
                            dialogMessage = null;
                        }
                    }
                };
                timer.start();
            }else{
                if(enProgreso){
                    if(dialogMessage != null){
                        dialogMessage.dismiss();
                        dialogMessage = null;
                    }

                    dialogMessage = new CustomProgress(Frm_Login.this,enProgreso,icono, msj);
                    dialogMessage.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialogMessage.setCanceledOnTouchOutside(true);
                    dialogMessage.show();
                }else{
                    if(dialogMessage != null) {
                        dialogMessage.dismiss();
                        dialogMessage = null;
                    }

                    dialogMessage = new CustomProgress(Frm_Login.this,enProgreso,icono,msj);
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

    public class registeredDialog extends Dialog{

        Activity a;
        Representante representante;

        registeredDialog(Activity a, Representante representante) {
            super(a);
            this.a = a;
            this.representante = representante;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.registereddialog);

            AutoResizeTextView lblApellidos = (AutoResizeTextView)findViewById(R.id.lblApellidos);
            AutoResizeTextView lblNombres = (AutoResizeTextView)findViewById(R.id.lblNombres);
            Button btnSi = (Button) findViewById(R.id.btnSi);
            Button btnNo = (Button) findViewById(R.id.btnNo);

            lblApellidos.setText(representante.getApellidos());
            lblNombres.setText(representante.getNombres());

            btnSi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AsyncEliminarGcm().execute(representante.getId(),1);

                    mensaje = getResources().getString(R.string.bienvenidoCliente) + "\n" + representante.getNombres() + " " + representante.getApellidos();
                    mostrarMensaje(true, false, 0, mensaje);
                    dismiss();
                }
            });

            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }

    private static class AsyncEliminarGcm extends AsyncTask<Object,Integer,Integer>{
        @Override
        protected Integer doInBackground(Object... params) {
            ArrayList<Object>  parametros = new ArrayList<>(2);
            parametros.add(0, "idRepresentante*" + params[0]);
            parametros.add(1, "origen*" + params[1]);
            parametros.add(2, "eliminarGcmRep");

            respuesta ws = new respuesta();
            Object response = ws.getData(parametros);

            try {
                JSONObject jsonObj = new JSONObject(response.toString());
                String result = jsonObj.get("Result").toString();

                Log.d("EJVH EliminaGcmRep", result);

                return 1;
            } catch (JSONException e) {
                return 0;
            }
        }
    }
}
