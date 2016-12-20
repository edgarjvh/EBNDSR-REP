package vistas;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import com.villasoftgps.ebndsrrep.Frm_Login;
import com.villasoftgps.ebndsrrep.R;

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
                sPrefs = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

                SharedPreferences.Editor sEditor = sPrefs.edit();
                sEditor.putString(PROPERTY_USER, "");
                sEditor.putString(PROPERTY_REG_ID, "");
                sEditor.putString(PROPERTY_CONVERSATIONS, "");
                sEditor.apply();

                Intent frm = new Intent(getContext(), Frm_Login.class);
                getContext().startActivity(frm);
                c.finish();
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
