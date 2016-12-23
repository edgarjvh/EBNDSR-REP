package com.villasoftgps.ebndsrrep;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class Frm_CambiarClave extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_clave);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


    }
}
