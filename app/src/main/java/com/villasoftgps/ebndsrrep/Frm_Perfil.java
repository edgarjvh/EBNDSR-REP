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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import clases.Representante;
import clases.Respuesta;
import de.hdodenhof.circleimageview.CircleImageView;
import vistas.lvPerfilArrayAdapter;
import vistas.lvPerfilItems;

public class Frm_Perfil extends Activity {

    static SharedPreferences sPrefs;
    private static final String PREF_NAME = "prefSchoolTool";
    private static final String PROPERTY_USER = "user";
    Representante representante;
    private ListView lvPerfil;
    private ArrayList<lvPerfilItems> data;
    private CircleImageView profile_image;
    private static final int FROM_CAMERA = 100;
    private static final int FROM_GALLERY = 200;
    private static final int FROM_CROPPING = 300;
    private Intent intCamera;
    private Intent intGallery;
    private Uri uri;
    private File file;
    String imageString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        lvPerfil = (ListView) findViewById(R.id.lvPerfil);
        View header = getLayoutInflater().inflate(R.layout.lvperfilheader, null);
        profile_image = (CircleImageView) header.findViewById(R.id.profile_image);
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] opciones = {"Desde Cámara", "Desde Galería", "Quitar Imagen", "Cancelar"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(Frm_Perfil.this);
                builder.setTitle("Elige una opcion");
                builder.setItems(opciones, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int seleccion) {
                        try {
                            switch (seleccion) {
                                case 0:
                                    intCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    file = new File(Environment.getExternalStorageDirectory(),
                                            "file" + String.valueOf(System.currentTimeMillis() + ".jpg"));
                                    uri = Uri.fromFile(file);
                                    intCamera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                                    intCamera.putExtra("return-data", true);
                                    startActivityForResult(intCamera, FROM_CAMERA);

                                    break;
                                case 1:
                                    intGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    intGallery.setType("image/*");

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                        startActivityForResult(Intent.createChooser(intGallery, "Seleccione una opcion"), FROM_GALLERY);
                                    } else {
                                        startActivityForResult(intGallery, FROM_GALLERY);
                                    }
                                    break;
                                case 2:
                                    profile_image.setImageResource(R.drawable.profile_img);
                                    new AsyncSendImage().execute(representante.getId(), 0, "");
                                    break;
                                default:
                                    dialog.dismiss();
                                    break;
                            }
                        } catch (ActivityNotFoundException anfe) {
                            Toast toast = Toast.makeText(Frm_Perfil.this, "Este dispositivo no soporta esta acción", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
                builder.show();
            }
        });

        if (sPrefs == null) {
            sPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        }

        Gson gson = new Gson();
        representante = gson.fromJson(sPrefs.getString(PROPERTY_USER, ""), Representante.class);

        byte[] decodedBytes = Base64.decode(representante.getImagen(), 0);
        profile_image.setImageBitmap(BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length));

        lvPerfil.addHeaderView(header);
        data = new ArrayList<>();

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

        if (!representante.getTelefono2().equals("")) {
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

    private class AsyncRepresentados extends AsyncTask<Object, Integer, Integer> {

        @Override
        protected Integer doInBackground(Object... params) {
            ArrayList<Object> parametros = new ArrayList<>(2);
            parametros.add(0, "idRepresentante*" + params[0]);
            parametros.add(1, "getRepresentados");

            Respuesta ws = new Respuesta();
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

                        if (!representados.equals("")) {
                            JSONArray array = jsonObj.getJSONArray("Representados");

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject representado = array.getJSONObject(i);

                                String car;

                                switch (representado.getInt("Grado")) {
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

                        if (!institucion.equals("")) {
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

                            if (!_institucion.getString("Telefono2").equals("")) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FROM_CAMERA && resultCode == RESULT_OK) {
            cropImage();
        } else if (requestCode == FROM_GALLERY) {
            if (data != null) {
                uri = data.getData();
                cropImage();
            }
        } else if (requestCode == FROM_CROPPING) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = bundle.getParcelable("data");
                sendImageToServer(bitmap);
            }
        }
    }

    private void cropImage() {
        try {
            Intent intCrop = new Intent("com.android.camera.action.CROP");
            intCrop.setDataAndType(uri, "image/*");

            intCrop.putExtra("crop", "true");
            intCrop.putExtra("outputX", 400);
            intCrop.putExtra("outputY", 400);
            intCrop.putExtra("aspectX", 1);
            intCrop.putExtra("aspectY", 1);
            intCrop.putExtra("scaleUpIfNeeded", true);
            intCrop.putExtra("return-data", true);

            startActivityForResult(intCrop, FROM_CROPPING);
        } catch (ActivityNotFoundException ex) {
            Log.d("EJVH Catch", ex.getMessage());
        }
    }

    private void sendImageToServer(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); //compress to which format you want.
        byte[] byte_arr = stream.toByteArray();
        imageString = Base64.encodeToString(byte_arr, Base64.DEFAULT);
        representante.setImagen(imageString);

        new AsyncSendImage().execute(representante.getId(), 0, imageString);
    }

    private class AsyncSendImage extends AsyncTask<Object, Integer, Integer> {

        @Override
        protected Integer doInBackground(Object... params) {
            ArrayList<Object> parametros = new ArrayList<>(4);
            parametros.add(0, "IdRepresentante*" + params[0]);
            parametros.add(1, "IdDocente*" + params[1]);
            parametros.add(2, "imageString*" + params[2]);
            parametros.add(3, "saveImagenPerfil");

            Respuesta ws = new Respuesta();
            Object response = ws.getData(parametros);

            try {
                JSONObject jsonObj = new JSONObject(response.toString());
                String result = jsonObj.get("Result").toString();

                switch (result) {
                    case "INSERTED":
                        return 1;
                    case "DELETED":
                        return 2;
                    default:
                        return 3;
                }
            } catch (JSONException e) {
                return 4;
            }
        }

        @Override
        protected void onPostExecute(Integer value) {
            super.onPostExecute(value);

            String texto;

            if (value == 1){
                byte[] decodedBytes = Base64.decode(representante.getImagen(), 0);
                profile_image.setImageBitmap(BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length));

                if (sPrefs == null){
                    sPrefs = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
                }

                Gson gson = new Gson();
                SharedPreferences.Editor sEditor = sPrefs.edit();
                sEditor.putString(PROPERTY_USER,gson.toJson(representante));
                sEditor.apply();

                texto = "imagen de perfil actualizada correctamete";
            }
            else if (value == 2){
                texto = "se ha quitado la imagen de perfil correctamente";
            }else{
                texto = "ocurrió un error al actualizar la imagen de perfil";
            }

            Toast.makeText(Frm_Perfil.this, texto, Toast.LENGTH_SHORT).show();
        }
    }
}
