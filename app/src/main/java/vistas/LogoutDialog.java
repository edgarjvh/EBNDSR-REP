package vistas;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import com.google.gson.Gson;
import com.villasoftgps.ebndsrrep.Frm_Login;
import com.villasoftgps.ebndsrrep.Frm_Principal;
import com.villasoftgps.ebndsrrep.R;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import java.util.ArrayList;
import clases.Representante;

public class LogoutDialog extends Dialog {

    private Activity c;
    //private static final String TAG = "EJVH";
    private SharedPreferences sPrefs;
    private static final String PREF_NAME = "prefSchoolTool";
    private static final String PROPERTY_USER = "user";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_CONVERSATIONS = "conversations";

    public LogoutDialog(Activity a){
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.logoutdialog);

        Button btnSi = (Button) findViewById(R.id.btnSi);
        Button btnNo = (Button) findViewById(R.id.btnNo);

        btnSi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Frm_Principal.cerrarSesion();
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
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
