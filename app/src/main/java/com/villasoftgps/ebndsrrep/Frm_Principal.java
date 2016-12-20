package com.villasoftgps.ebndsrrep;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import clases.RegistrarGCM;
import clases.Representante;
import controles.AutoResizeTextView;
import vistas.CustomProgress;
import vistas.LogoutDialog;
import vistas.SpinnerItems;
import vistas.SpinnerItemsArrayAdapter;
import vistas.lvCalendarioItems;
import vistas.lvCalendarioItemsArrayAdapter;
import vistas.lvMensajesItems;
import vistas.lvMensajesItemsArrayAdapter;
import vistas.lvMenuArrayAdapter;
import vistas.lvMenuItems;

public class Frm_Principal extends Activity {

    static SharedPreferences sPrefs;
    SharedPreferences.Editor sEditor;
    private static final String PREF_NAME = "prefSchoolTool";
    private static final String PROPERTY_USER = "user";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_CONVERSATIONS = "conversations";

    Object response = null;
    String mensaje = "";
    lvCalendarioItems CalendarioItems[];
    static ArrayList<lvMensajesItems> MensajesItems;
    static ArrayList<SpinnerItems> spinnerItems;
    ArrayList<lvMenuItems> MenuItems;
    private static Representante representante;
    private ListView lvCalendario;
    private ListView lvMenu;
    private static ListView lvMensajes;
    private CustomProgress dialogMessage;
    DrawerLayout drawerLayout;
    AutoResizeTextView lblSinEventos;
    AutoResizeTextView lblCargandoEventos;
    int value = 0;
    View sinEventos;
    View cargandoEventos;
    static lvMensajesItemsArrayAdapter adapter;
    static SpinnerItemsArrayAdapter spinnerAdapter;
    lvMenuArrayAdapter menuAdapter;
    static Spinner cboDocentes;
    static Frm_Principal principal;
    static Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        principal = Frm_Principal.this;

        if (sPrefs == null){
            sPrefs = getApplicationContext().getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        }

        if (sPrefs.getString(PROPERTY_USER,"").equals("")){
            Intent frm = new Intent(Frm_Principal.this,Frm_Login.class);
            startActivity(frm);
            return;
        }else{
            Gson gson = new Gson();
            String user = sPrefs.getString(PROPERTY_USER,"");
            representante = gson.fromJson(user, Representante.class);
        }

        setContentView(R.layout.activity_principal);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        spinnerItems = new ArrayList<>();
        cboDocentes = (Spinner) findViewById(R.id.cboDocentes);

        cboDocentes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cboDocentes.post(new Runnable() {
                    @Override
                    public void run() {
                        View view = cboDocentes.getSelectedView();

                        if (view != null){
                            TextView lblIdDocente = (TextView) view.findViewById(R.id.lblIdDocente);
                            int idDocente = Integer.parseInt(lblIdDocente.getText().toString());

                            if (!sPrefs.getString(PROPERTY_CONVERSATIONS,"").equals("")){
                                Type type = new TypeToken<ArrayList<lvMensajesItems>>() {}.getType();
                                gson = new Gson();
                                ArrayList<lvMensajesItems> items = gson.fromJson(sPrefs.getString(PROPERTY_CONVERSATIONS,""),type);

                                MensajesItems.clear();

                                for (int x = 0; x < items.size(); x++){
                                    if (items.get(x).getIdDocente() == idDocente && items.get(x).getIdRepresentante() == representante.getId()){
                                        MensajesItems.add(items.get(x));
                                    }
                                }

                                adapter = new lvMensajesItemsArrayAdapter(Frm_Principal.this,MensajesItems);
                                lvMensajes.setAdapter(adapter);

                                if (MensajesItems.size() > 0){
                                    lvMensajes.setSelection(MensajesItems.size() - 1);
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        MensajesItems = new ArrayList<>();

        MenuItems = new ArrayList<>();

        MenuItems.add(0,new lvMenuItems(R.drawable.profile_icon,"Ver mi Perfil"));
        MenuItems.add(1,new lvMenuItems(R.drawable.logout_icon,"Cerrar Sesión"));

        lvCalendario = (ListView)findViewById(R.id.lvCalendario);
        lvMensajes = (ListView)findViewById(R.id.lvMensajes);
        lvMenu = (ListView)findViewById(R.id.lvMenu);

        adapter = new lvMensajesItemsArrayAdapter(Frm_Principal.this,MensajesItems);
        lvMensajes.setAdapter(adapter);

        View lvMenuHeader = getLayoutInflater().inflate(R.layout.lvmenuheader,null);
        menuAdapter = new lvMenuArrayAdapter(this,MenuItems);
        lvMenu.addHeaderView(lvMenuHeader);
        lvMenu.setAdapter(menuAdapter);

        lvMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 1: // ver mi perfil

                        break;
                    case 2: // cerrar sesion
                        LogoutDialog cpd = new LogoutDialog(Frm_Principal.this);
                        cpd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        cpd.setCanceledOnTouchOutside(false);
                        cpd.show();
                        break;
                    default:
                        break;
                }
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        RadioGroup rgroupOpcionesCalendario = (RadioGroup) findViewById(R.id.rgroupOpcionesCalendario);
        Button btnMenu = (Button)findViewById(R.id.btnMenu);
        final Button btnEnviarMensaje = (Button)findViewById(R.id.btnEnviarMensaje);

        final EditText txtMensaje = (EditText)findViewById(R.id.txtMensaje);

        txtMensaje.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (txtMensaje.getText().toString().length() == 0){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        btnEnviarMensaje.setBackground(getResources().getDrawable(R.drawable.send_message_icon_disabled));
                    }else{
                        btnEnviarMensaje.setBackgroundDrawable(getResources().getDrawable(R.drawable.send_message_icon_disabled));
                    }
                    btnEnviarMensaje.setEnabled(false);
                }else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        btnEnviarMensaje.setBackground(getResources().getDrawable(R.drawable.send_message_icon));
                    }else{
                        btnEnviarMensaje.setBackgroundDrawable(getResources().getDrawable(R.drawable.send_message_icon));
                    }
                    btnEnviarMensaje.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnEnviarMensaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!txtMensaje.getText().toString().equals("")){
                    View viewDocente = cboDocentes.getSelectedView();

                    if (viewDocente != null){
                        TextView lblRegistrado = (TextView) viewDocente.findViewById(R.id.lblRegistrado);
                        TextView lblIdDocente = (TextView) viewDocente.findViewById(R.id.lblIdDocente);
                        int idDocente = Integer.parseInt(lblIdDocente.getText().toString());
                        int registrado = Integer.parseInt(lblRegistrado.getText().toString());

                        if (registrado == 0){
                            mostrarMensaje(false,1,"Este docente no ha sido registrado en el sistema\nMensaje no enviado!");
                            return;
                        }

                        if (!sPrefs.getString(PROPERTY_CONVERSATIONS,"").equals("")){
                            Type type = new TypeToken<ArrayList<lvMensajesItems>>() {}.getType();
                            gson = new Gson();
                            ArrayList<lvMensajesItems> newMensaje = gson.fromJson(sPrefs.getString(PROPERTY_CONVERSATIONS,""),type);

                            long fechaEnvio = System.currentTimeMillis();

                            // se crea el nuevo objeto mensaje
                            newMensaje.add(new lvMensajesItems(
                                    newMensaje.size() + 1, // tempID
                                    0,                     // idMensaje
                                    representante.getId(), // idRepresentante
                                    idDocente,             // idDocente
                                    1,                     // via
                                    3,                     // status 3 = pbar visible
                                    fechaEnvio,
                                    txtMensaje.getText().toString())
                                    );

                            SharedPreferences.Editor sEditor = sPrefs.edit();
                            sEditor.putString(PROPERTY_CONVERSATIONS,gson.toJson(newMensaje));
                            sEditor.apply();

                            actualizarConversaciones();

                            /*
                            parametros.add(0, "tempId*" + params[0]);
                            parametros.add(1, "via*" + params[1]);
                            parametros.add(2, "idRepresentante*" + params[2]);
                            parametros.add(3, "idDocente*" + params[3]);
                            parametros.add(4, "texto*" + params[4]);
                            parametros.add(5, "fechaHora*" + params[5]);
                            parametros.add(6, "enviarMensaje");
                            */

                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",new Locale("es","ES"));

                            new AsyncEnviarMensaje().execute(
                                    newMensaje.size(),
                                    1,
                                    representante.getId(),
                                    idDocente,
                                    txtMensaje.getText().toString(),
                                    df.format(new Date(fechaEnvio))
                            );
                        }
                        else
                        {
                            ArrayList<lvMensajesItems> newMensaje = new ArrayList<>();

                            long fechaEnvio = System.currentTimeMillis();

                            // se crea el nuevo objeto mensaje
                            newMensaje.add(new lvMensajesItems(
                                    newMensaje.size() + 1, // tempID
                                    0,                     // idMensaje
                                    representante.getId(), // idRepresentante
                                    idDocente,             // idDocente
                                    1,                     // via
                                    3,                     // status 3 = pbar visible
                                    fechaEnvio,
                                    txtMensaje.getText().toString())
                            );

                            gson = new Gson();
                            SharedPreferences.Editor sEditor = sPrefs.edit();
                            sEditor.putString(PROPERTY_CONVERSATIONS,gson.toJson(newMensaje));
                            sEditor.apply();

                            actualizarConversaciones();

                            /*
                            parametros.add(0, "tempId*" + params[0]);
                            parametros.add(1, "via*" + params[1]);
                            parametros.add(2, "idRepresentante*" + params[2]);
                            parametros.add(3, "idDocente*" + params[3]);
                            parametros.add(4, "texto*" + params[4]);
                            parametros.add(5, "fechaHora*" + params[5]);
                            parametros.add(6, "enviarMensaje");
                            */

                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",new Locale("es","ES"));

                            new AsyncEnviarMensaje().execute(
                                    newMensaje.size(),
                                    1,
                                    representante.getId(),
                                    idDocente,
                                    txtMensaje.getText().toString(),
                                    df.format(new Date(fechaEnvio))
                            );
                        }
                    }

                    txtMensaje.setText(null);
                }
            }
        });

        lvCalendario.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView descripcion = (TextView)view.findViewById(R.id.lblDescripcion);
                mostrarMensaje(false,1,descripcion.getText().toString());
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarOcultarMenu();
            }
        });

        String nombre = "Bienvenido(a)\n" + "<font color='#0808e1'>" +
                representante.getApellidos() + ", " +
                representante.getNombres() + "</font>";

        AutoResizeTextView lblNombre = (AutoResizeTextView)findViewById(R.id.lblNombre);
        lblNombre.setText(Html.fromHtml(nombre), TextView.BufferType.SPANNABLE);

        lblNombre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(lvMenu)){
                    drawerLayout.closeDrawers();
                }
            }
        });

        Resources res = getResources();

        TabHost tabs=(TabHost)findViewById(R.id.tabhost);
        tabs.setup();

        TabHost.TabSpec spec=tabs.newTabSpec("calendario");
        spec.setContent(R.id.tab1);
        spec.setIndicator("",
                res.getDrawable(R.drawable.calendar_icon));
        tabs.addTab(spec);

        spec=tabs.newTabSpec("mensajes");
        spec.setContent(R.id.tab2);

        spec.setIndicator("",
                res.getDrawable(R.drawable.message_icon));
        tabs.addTab(spec);

        tabs.setCurrentTab(0);

        sinEventos = findViewById(R.id.emptyView);
        cargandoEventos = findViewById(R.id.loadingView);
        lblSinEventos = (AutoResizeTextView) sinEventos.findViewById(R.id.lblSinEventos);
        lblCargandoEventos = (AutoResizeTextView) cargandoEventos.findViewById(R.id.lblCargandoEventos);
        lvCalendario.setEmptyView(cargandoEventos);

        rgroupOpcionesCalendario.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rbtn = (RadioButton)radioGroup.findViewById(i);

                switch (rbtn.getText().toString()){
                    case "Esta Semana" :
                        mensaje = "Buscando eventos para esta semana. Por favor espere...";
                        value = 0;
                        break;
                    case "Este Mes" :
                        mensaje = "Buscando eventos para este mes. Por favor espere...";
                        value = 1;
                        break;
                    case "Este Año" :
                        mensaje = "Buscando eventos para este año. Por favor espere...";
                        value = 2;
                        break;
                }

                new AsyncCalendario().execute(representante.getId(),value);
            }
        });

        mensaje = "Buscando eventos para esta semana. Por favor espere...";
        new AsyncCalendario().execute(representante.getId(), 0);
        new AsyncCargarDocentes().execute(representante.getId());
        new RegistrarGCM(Frm_Principal.this,representante);
    }

    public static void actualizarConversaciones() {
        principal.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View view = cboDocentes.getSelectedView();

                if (view != null){
                    TextView lblIdDocente = (TextView) view.findViewById(R.id.lblIdDocente);
                    int idDocente = Integer.parseInt(lblIdDocente.getText().toString());

                    if (!sPrefs.getString(PROPERTY_CONVERSATIONS,"").equals("")){
                        Type type = new TypeToken<ArrayList<lvMensajesItems>>() {}.getType();
                        gson = new Gson();
                        ArrayList<lvMensajesItems> items = gson.fromJson(sPrefs.getString(PROPERTY_CONVERSATIONS,""),type);

                        MensajesItems.clear();

                        for (int x = 0; x < items.size(); x++){
                            if (items.get(x).getIdDocente() == idDocente && items.get(x).getIdRepresentante() == representante.getId()){
                                MensajesItems.add(items.get(x));
                            }
                        }

                        adapter = new lvMensajesItemsArrayAdapter(principal,MensajesItems);
                        lvMensajes.setAdapter(adapter);

                        if (MensajesItems.size() > 0){
                            lvMensajes.setSelection(MensajesItems.size() - 1);
                        }
                    }
                }
            }
        });
    }

    public static void actualizarRegistroDocente(final int idDocente, final int registrado){
        principal.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (spinnerItems.size() > 0){
                    for (int i = 0; i < spinnerItems.size(); i++){
                        if (spinnerItems.get(i).getIdDocente() == idDocente){
                            spinnerItems.get(i).setRegistrado(registrado);
                            spinnerAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }
        });
    }

    public static void cerrarSesion(){
        sPrefs = principal.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();

        Representante representante = gson.fromJson(sPrefs.getString(PROPERTY_USER,""),Representante.class);
        new AsyncEliminarGcm().execute(representante.getId(),0);

        SharedPreferences.Editor sEditor = sPrefs.edit();
        sEditor.putString(PROPERTY_USER, "");
        sEditor.putString(PROPERTY_REG_ID, "");
        sEditor.putString(PROPERTY_CONVERSATIONS, "");
        sEditor.apply();

        Intent frm = new Intent(principal, Frm_Login.class);
        principal.startActivity(frm);
    }

    private static class AsyncEliminarGcm extends AsyncTask<Object,Integer,Integer>{
        @Override
        protected Integer doInBackground(Object... params) {
            ArrayList<Object>  parametros = new ArrayList<>(3);
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

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (integer == 1){
                principal.finish();
            }
        }
    }

    private void ocultarTeclado(){
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(lvMenu)){
            mostrarOcultarMenu();
        }else{
            moveTaskToBack(true);
        }
    }

    public void mostrarOcultarMenu(){
        if (drawerLayout.isDrawerOpen(lvMenu)) {
            drawerLayout.closeDrawers();
        } else {
            drawerLayout.openDrawer(lvMenu);
        }
    }

    private class AsyncEnviarMensaje extends AsyncTask<Object,Integer,Integer>{

        @Override
        protected Integer doInBackground(Object... params) {
            ArrayList<Object>  parametros = new ArrayList<>(7);
            parametros.add(0, "tempId*" + params[0]);
            parametros.add(1, "via*" + params[1]);
            parametros.add(2, "idRepresentante*" + params[2]);
            parametros.add(3, "idDocente*" + params[3]);
            parametros.add(4, "texto*" + params[4]);
            parametros.add(5, "fechaHora*" + params[5]);
            parametros.add(6, "enviarMensaje");

            respuesta ws = new respuesta();
            response = ws.getData(parametros);

            try
            {
                JSONObject jsonObj = new JSONObject(response.toString());
                String result = jsonObj.get("Result").toString();

                switch (result) {
                    case "OK":
                        /*
                        Result = "OK",
                        TempId = tempId,
                        IdDocente = idDocente,
                        IdRepresentante = idRepresentante,
                        IdMensaje = idMensaje,
                        Estado = 0
                        */

                        Type type = new TypeToken<ArrayList<lvMensajesItems>>() {}.getType();
                        gson = new Gson();
                        ArrayList<lvMensajesItems> items = gson.fromJson(sPrefs.getString(PROPERTY_CONVERSATIONS,""),type);

                        for (int i = 0; i < items.size(); i++){
                            Log.d("EJVH " + Integer.toString(i),
                                    "idRep: " + jsonObj.get("IdRepresentante").toString() + " - " + Integer.toString(items.get(i).getIdRepresentante()) + "; " +
                                    "idDoc: " + jsonObj.get("IdDocente").toString() + " - " + Integer.toString(items.get(i).getIdDocente()) + "; " +
                                    "tempId: " + jsonObj.get("TempId").toString() + " - " + Integer.toString(items.get(i).getTempId()));

                            if (items.get(i).getIdRepresentante() == Integer.parseInt(jsonObj.get("IdRepresentante").toString()) &&
                                    items.get(i).getIdDocente() == Integer.parseInt(jsonObj.get("IdDocente").toString()) &&
                                    items.get(i).getTempId() == Integer.parseInt(jsonObj.get("TempId").toString())){

                                items.get(i).setIdMensaje(Integer.parseInt(jsonObj.get("IdMensaje").toString()));
                                items.get(i).setStatus(Integer.parseInt(jsonObj.get("Estado").toString()));
                                break;
                            }
                        }

                        gson = new Gson();
                        SharedPreferences.Editor sEditor = sPrefs.edit();
                        sEditor.putString(PROPERTY_CONVERSATIONS,gson.toJson(items));
                        sEditor.apply();

                        actualizarConversaciones();
                        publishProgress(1);
                        break;
                    default:
                        mensaje = jsonObj.get("Message").toString();
                        publishProgress(2);
                        break;
                }
                return null;
            }
            catch (JSONException e) {
                mensaje = e.getMessage();
                publishProgress(4);
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    private class AsyncCargarDocentes extends AsyncTask<Object,Integer, Integer>{

        @Override
        protected Integer doInBackground(Object... params) {
            publishProgress(0);
            ArrayList<Object>  parametros = new ArrayList<>(2);
            parametros.add(0, "idRepresentante*" + params[0]);
            parametros.add(1, "getDocentes");

            respuesta ws = new respuesta();
            response = ws.getData(parametros);

            try
            {
                JSONObject jsonObj = new JSONObject(response.toString());

                String result = jsonObj.get("Result").toString();

                switch (result) {
                    case "OK":
                        JSONArray array = jsonObj.getJSONArray("Docentes");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject docente = array.getJSONObject(i);

                            spinnerItems.add(new SpinnerItems(
                                    docente.getInt("Registrado"),
                                    docente.getInt("Id"),
                                    docente.getString("Apellidos"),
                                    docente.getString("Nombres")
                            ));
                        }
                        publishProgress(1);
                        break;
                    case "NO ROWS":
                        mensaje = jsonObj.get("Message").toString();
                        publishProgress(2);
                        break;
                    default:
                        mensaje = jsonObj.get("Message").toString();
                        publishProgress(3);
                        break;
                }
                return null;
            }
            catch (JSONException e) {
                mensaje = e.getMessage();
                publishProgress(4);
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if (values[0] == 1){
                spinnerAdapter = new SpinnerItemsArrayAdapter(Frm_Principal.this,spinnerItems);
                cboDocentes.setAdapter(spinnerAdapter);
                cboDocentes.setSelection(0);

                cboDocentes.post(new Runnable() {
                    @Override
                    public void run() {
                        View view = cboDocentes.getSelectedView();

                        if (view != null){
                            TextView lblIdDocente = (TextView) view.findViewById(R.id.lblIdDocente);
                            int idDocente = Integer.parseInt(lblIdDocente.getText().toString());

                            if (!sPrefs.getString(PROPERTY_CONVERSATIONS,"").equals("")){
                                Type type = new TypeToken<ArrayList<lvMensajesItems>>() {}.getType();
                                gson = new Gson();
                                ArrayList<lvMensajesItems> items = gson.fromJson(sPrefs.getString(PROPERTY_CONVERSATIONS,""),type);

                                MensajesItems.clear();

                                for (int x = 0; x < items.size(); x++){
                                    if (items.get(x).getIdDocente() == idDocente && items.get(x).getIdRepresentante() == representante.getId()){
                                        MensajesItems.add(items.get(x));
                                    }
                                }

                                adapter = new lvMensajesItemsArrayAdapter(Frm_Principal.this,MensajesItems);
                                lvMensajes.setAdapter(adapter);

                                if (MensajesItems.size() > 0){
                                    lvMensajes.setSelection(MensajesItems.size() - 1);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    private class AsyncCalendario extends AsyncTask<Object, Integer, Integer> {

        @Override
        protected Integer doInBackground(Object... params) {
            publishProgress(0);
            ArrayList<Object>  parametros = new ArrayList<>(4);
            parametros.add(0, "Id*" + params[0]);
            parametros.add(1, "TipoCalendario*"+ params[1]);
            parametros.add(2, "getCalendario");

            respuesta ws = new respuesta();
            response = ws.getData(parametros);

            Log.d("EJVH", response.getClass().toString());
            Log.d("EJVH", response.toString());

            try {
                JSONObject jsonObj = new JSONObject(response.toString());

                String result = jsonObj.get("Result").toString();

                switch (result) {
                    case "OK":
                        JSONArray array = jsonObj.getJSONArray("Calendario");
                        CalendarioItems = new lvCalendarioItems[array.length()];

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject evento = array.getJSONObject(i);
                            CalendarioItems[i] = new lvCalendarioItems(
                                    evento.getString("Header"),
                                    evento.getInt("Representado"),
                                    evento.getInt("Antiguedad"),
                                    evento.getString("Titulo"),
                                    evento.getString("Fecha"),
                                    evento.getString("Descripcion"));
                        }
                        publishProgress(1);
                        break;
                    case "NO ROWS":
                        mensaje = jsonObj.get("Message").toString();
                        publishProgress(2);
                        break;
                    default:
                        mensaje = "Error de conexión";
                        publishProgress(3);
                        break;
                }
                return null;
            } catch (JSONException e) {
                mensaje = "Error de conexión";
                publishProgress(4);
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            switch (values[0]){
                case 0:
                    lblCargandoEventos.setText(mensaje);
                    sinEventos.setVisibility(View.GONE);
                    cargandoEventos.setVisibility(View.VISIBLE);
                    lvCalendario.setEmptyView(cargandoEventos);
                    lvCalendario.setAdapter(null);
                    break;
                case 1:
                    lvCalendarioItemsArrayAdapter adapter = new lvCalendarioItemsArrayAdapter(Frm_Principal.this,R.layout.lvcalendarioitemlayout,CalendarioItems);
                    lvCalendario.setAdapter(adapter);
                    if (dialogMessage != null){
                        dialogMessage.dismiss();
                        dialogMessage = null;
                    }
                    break;
                case 2 :
                    switch (value){
                        case 0:
                            mensaje = "No hay eventos para esta semana";
                            break;
                        case 1:
                            mensaje = "No hay eventos para este mes";
                            break;
                        case 2:
                            mensaje = "No hay eventos para este año";
                            break;
                    }
                    lblSinEventos.setText(mensaje);
                    cargandoEventos.setVisibility(View.GONE);
                    sinEventos.setVisibility(View.VISIBLE);
                    lvCalendario.setEmptyView(sinEventos);
                    lvCalendario.setAdapter(null);

                    if (dialogMessage != null){
                        dialogMessage.dismiss();
                        dialogMessage = null;
                    }
                    break;
                default:
                    /*switch (value){
                        case 0:
                            mensaje = "No hay eventos para esta semana";
                            break;
                        case 1:
                            mensaje = "No hay eventos para este mes";
                            break;
                        case 2:
                            mensaje = "No hay eventos para este año";
                            break;
                    }*/
                    lblSinEventos.setText(mensaje);
                    cargandoEventos.setVisibility(View.GONE);
                    sinEventos.setVisibility(View.VISIBLE);
                    lvCalendario.setEmptyView(sinEventos);
                    lvCalendario.setAdapter(null);
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

    private void mostrarMensaje(Boolean enProgreso,int icono, String msj){
        if(enProgreso){
            if(dialogMessage != null){
                dialogMessage.dismiss();
                dialogMessage = null;
            }
            dialogMessage = new CustomProgress(Frm_Principal.this,enProgreso,icono,msj);
            dialogMessage.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialogMessage.setCanceledOnTouchOutside(true);
            dialogMessage.show();
        }else{
            if(dialogMessage != null){
                dialogMessage.dismiss();
                dialogMessage = null;
            }

            dialogMessage = new CustomProgress(Frm_Principal.this,enProgreso,icono,msj);
            dialogMessage.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialogMessage.setCanceledOnTouchOutside(true);
            dialogMessage.show();
            CountDownTimer timer = new CountDownTimer(5000,1000) {
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
}
