package com.villasoftgps.ebndsrrep;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import java.io.File;
import java.util.ArrayList;
import clases.Representante;
import de.hdodenhof.circleimageview.CircleImageView;
import vistas.lvPerfilArrayAdapter;
import vistas.lvPerfilItems;

public class Frm_Perfil extends Activity {

    static SharedPreferences sPrefs;
    private static final String PREF_NAME = "prefSchoolTool";
    private static final String PROPERTY_USER = "user";
    private ListView lvPerfil;
    private ArrayList<lvPerfilItems> data;
    private CircleImageView profile_image;
    private static String APP_DIRECTORY = "myAppProfiles/";
    private static String MEDIA_DIRECTORY = "media";
    private static String PICTURE_NAME = "temporal.jpg";
    private static final int FROM_CAMERA = 100;
    private static final int FROM_GALLERY = 200;
    private static final int FROM_CROPPING = 300;
    private Uri picUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        lvPerfil = (ListView) findViewById(R.id.lvPerfil);
        View header = getLayoutInflater().inflate(R.layout.lvperfilheader,null);
        profile_image = (CircleImageView) header.findViewById(R.id.profile_image);
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] opciones = {"Desde Cámara", "Desde Galería", "Cancelar"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(Frm_Perfil.this);
                builder.setTitle("Elige una opcion");
                builder.setItems(opciones, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int seleccion) {
                        try{
                            switch (seleccion){
                                case 0:
                                    Intent intentCaptured = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                                    intentCaptured.putExtra(MediaStore.EXTRA_OUTPUT,
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
                                    // ******** code for crop image
                                    intentCaptured.putExtra("crop", "true");
                                    intentCaptured.putExtra("aspectX", 0);
                                    intentCaptured.putExtra("aspectY", 0);
                                    intentCaptured.putExtra("outputX", 200);
                                    intentCaptured.putExtra("outputY", 150);

                                    try {
                                        intentCaptured.putExtra("return-data", true);
                                        startActivityForResult(intentCaptured, FROM_CAMERA);
                                    } catch (ActivityNotFoundException e) {
                                        // Do nothing for now
                                    }

                                break;
                                case 1:
                                    Intent intent = new Intent();
                                    // call android default gallery
                                    intent.setType("image/*");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    // ******** code for crop image
                                    intent.putExtra("crop", "true");
                                    intent.putExtra("aspectX", 0);
                                    intent.putExtra("aspectY", 0);
                                    intent.putExtra("outputX", 200);
                                    intent.putExtra("outputY", 150);

                                    try {

                                        intent.putExtra("return-data", true);
                                        startActivityForResult(Intent.createChooser(intent,
                                                "Seleccione el origen"), FROM_GALLERY);

                                    } catch (ActivityNotFoundException e) {
                                    // Do nothing for now
                                    }

                                break;
                                default:
                                    dialog.dismiss();
                                    break;
                            }
                        }
                        catch (ActivityNotFoundException anfe) {
                            Toast toast = Toast.makeText(Frm_Perfil.this, "Este dispositivo no soporta esta acción", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
                builder.show();
            }
        });

        lvPerfil.addHeaderView(header);
        data = new ArrayList<>();

        if (sPrefs == null){
            sPrefs = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        }

        Gson gson = new Gson();
        Representante representante = gson.fromJson(sPrefs.getString(PROPERTY_USER,""),Representante.class);

        data.add(new lvPerfilItems(
                "Datos Personales",
                "Cédula de Identidad",
                Integer.toString(representante.getCedula()),
                "",
                1
        ));

        data.add(new lvPerfilItems(
                "",
                "Nombres y Apellidos",
                representante.getApellidos() + ", " + representante.getNombres(),
                "",
                1
        ));

        String telefono2 = "";

        if (!representante.getTelefono2().equals("")){
            telefono2 += " / " + representante.getTelefono2();
        }

        data.add(new lvPerfilItems(
                "",
                "Teléfonos",
                representante.getTelefono1() + telefono2,
                "",
                1
        ));

        new AsyncRepresentados().execute(representante.getId());
    }

    private class AsyncRepresentados extends AsyncTask<Object,Integer,Integer>{

        @Override
        protected Integer doInBackground(Object... params) {
            ArrayList<Object>  parametros = new ArrayList<>(2);
            parametros.add(0, "idRepresentante*" + params[0]);
            parametros.add(1, "getRepresentados");

            respuesta ws = new respuesta();
            Object response = ws.getData(parametros);

            try {
                JSONObject jsonObj = new JSONObject(response.toString());

                String result = jsonObj.get("Result").toString();
                String representados = jsonObj.get("Representados").toString();
                String institucion = jsonObj.get("Institucion").toString();

                Log.d("EJVH representados", representados);
                Log.d("EJVH institucion", institucion);

                switch (result) {
                    case "OK":

                        if (!representados.equals("")){
                            JSONArray array = jsonObj.getJSONArray("Representados");

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject representado = array.getJSONObject(i);

                                String car;

                                switch (representado.getInt("Grado")){
                                    case 1:
                                        car = "ro";
                                        break;
                                    case 2:
                                        car = "do";
                                        break;
                                    case 3:
                                        car = "ro";
                                        break;
                                    default:
                                        car = "to";
                                        break;
                                }

                                String footer =
                                        "<font color='#045FB4'>Grado: </font> " + "<font color='#2E9AFE'>" + Integer.toString(representado.getInt("Grado")) + car + "</font>" +
                                                "<font color='#045FB4'>, Sección: </font> " + "<font color='#2E9AFE'>\"" + representado.getString("Seccion") + "\"</font>" +
                                                "<font color='#045FB4'>\nDocente: </font> " + "<font color='#2E9AFE'>" + representado.getString("Docente") + "</font>";


                                data.add(new lvPerfilItems(
                                        i == 0 ? "Mis Representados" : "",
                                        "Apellidos y Nombres",
                                        representado.getString("Apellidos") + ", " + representado.getString("Nombres"),
                                        footer,
                                        1
                                ));
                            }
                        }

                        if (!institucion.equals("")){
                            JSONObject _institucion = new JSONObject(jsonObj.get("Institucion").toString());

                            data.add(new lvPerfilItems(
                                    "La Institución",
                                    "Nombre",
                                    _institucion.getString("Nombre"),
                                    "",
                                    1
                            ));

                            data.add(new lvPerfilItems(
                                    "",
                                    "Dirección",
                                    _institucion.getString("Direccion"),
                                    "",
                                    3
                            ));

                            String telefono2 = "";

                            if (!_institucion.getString("Telefono2").equals("")){
                                telefono2 += " / " + _institucion.getString("Telefono2");
                            }

                            data.add(new lvPerfilItems(
                                    "",
                                    "Teléfonos",
                                    _institucion.getString("Telefono1") + telefono2,
                                    "",
                                    1
                            ));

                            data.add(new lvPerfilItems(
                                    "",
                                    "Fundación",
                                    Integer.toString(_institucion.getInt("Fundacion")),
                                    "",
                                    1
                            ));

                            data.add(new lvPerfilItems(
                                    "",
                                    "Director",
                                    _institucion.getString("Director"),
                                    "",
                                    1
                            ));

                            data.add(new lvPerfilItems(
                                    "",
                                    "Etapas",
                                    _institucion.getString("Etapas"),
                                    "",
                                    2
                            ));
                        }

                        publishProgress(1);
                        break;
                    case "NO ROWS":
                        publishProgress(2);
                        break;
                    default:
                        publishProgress(3);
                        break;
                }
                return null;
            } catch (JSONException e) {
                publishProgress(4);
                return 1;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            // cargar datos del colegio

            lvPerfilArrayAdapter adapter = new lvPerfilArrayAdapter(Frm_Perfil.this, data);
            lvPerfil.setAdapter(adapter);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null){
            if (requestCode == FROM_CAMERA) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data");
                    profile_image.setImageBitmap(photo);

                }
            }

            if (requestCode == FROM_GALLERY) {
                Bundle extras2 = data.getExtras();
                if (extras2 != null) {
                    Bitmap photo = extras2.getParcelable("data");
                    profile_image.setImageBitmap(photo);

                }
            }
        }
    }
}
