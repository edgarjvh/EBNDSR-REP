package com.villasoftgps.ebndsrrep;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        lvPerfil = (ListView) findViewById(R.id.lvPerfil);
        View header = getLayoutInflater().inflate(R.layout.lvperfilheader,null);
        CircleImageView profile_image = (CircleImageView) header.findViewById(R.id.profile_image);
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Frm_Perfil.this,"Click en imagen",Toast.LENGTH_LONG).show();
            }
        });

        lvPerfil.addHeaderView(header);

        data = new ArrayList<>();

        data.add(new lvPerfilItems(
                "Datos Personales",
                "Cédula de Identidad",
                "18370323",
                ""
        ));

        data.add(new lvPerfilItems(
                "",
                "Nombres y Apellidos",
                "EDGAR JOSÉ VILLASMIL HERNÁNDEZ",
                ""
        ));

        if (sPrefs == null){
            sPrefs = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        }

        Gson gson = new Gson();
        Representante representante = gson.fromJson(sPrefs.getString(PROPERTY_USER,""),Representante.class);

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

                switch (result) {
                    case "OK":
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
                                    footer
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
}
