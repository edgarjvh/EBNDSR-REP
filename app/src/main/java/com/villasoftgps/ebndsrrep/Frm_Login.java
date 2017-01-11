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
import java.util.ArrayList;
import clases.Representante;
import clases.Respuesta;
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
    registeredDialog cpd = null;

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

            Respuesta ws = new Respuesta();
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
                        representante.setTelefono1(array.getString("Telefono1"));
                        representante.setTelefono2(array.getString("Telefono2"));
                        representante.setDireccion(array.get("Direccion").toString());
                        representante.setImagen(array.get("Imagen").toString());
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
                        representante.setTelefono1(array.getString("Telefono1"));
                        representante.setTelefono2(array.getString("Telefono2"));
                        representante.setDireccion(array.get("Direccion").toString());
                        representante.setImagen(array.get("Imagen").toString());
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
                    cpd = new registeredDialog(Frm_Login.this, representante);
                    cpd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    cpd.setCanceledOnTouchOutside(false);

                    if(dialogMessage != null) {
                        dialogMessage.dismiss();
                        dialogMessage = null;
                    }

                    cpd.show();

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

                if (cpd != null){
                    cpd.dismiss();
                    cpd = null;
                }

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

                        if (dialogMessage != null){
                            dialogMessage.dismiss();
                            dialogMessage = null;
                        }

                        startActivity(i);
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

                    dismiss();

                    Intent i = new Intent(Frm_Login.this, Frm_Principal.class);

                    sEditor = sPrefs.edit();
                    Gson gson = new Gson();
                    String user = gson.toJson(representante);
                    sEditor.putString(PROPERTY_USER,user);
                    sEditor.apply();

                    if (dialogMessage != null){
                        dialogMessage.dismiss();
                        dialogMessage = null;
                    }

                    startActivity(i);
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

            Respuesta ws = new Respuesta();
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
