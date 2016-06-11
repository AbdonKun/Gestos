package com.example.abdon.gestos;

import android.app.Dialog;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.gesture.Prediction;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener{

    private GestureLibrary gesLib;
    private final File file = new File(Environment.getExternalStorageDirectory(),"gestures");
    private static GestureLibrary almacen;
    private RelativeLayout rl;
    private com.github.nkzawa.socketio.client.Socket socket;
    TextView pregunta;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView reconocido = (TextView)findViewById(R.id.reconocido);
        pregunta = (TextView)findViewById(R.id.pregunta);
        rl = (RelativeLayout) findViewById(R.id.relative);

        GestureOverlayView overlayView = (GestureOverlayView)findViewById(R.id.gesto);
        overlayView.addOnGesturePerformedListener(this);

        gesLib = GestureLibraries.fromFile(file);

        if (!gesLib.load()) {
            finish();
        }

        irInicio();
        socket.on("nueva pregunta", onNuevaPregunta);
        //Log.d("Log","pase");
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {

        ArrayList<Prediction> predictions = gesLib.recognize(gesture);

        for (Prediction prediction : predictions) {

            if (prediction.score > 2.0) {

                Toast.makeText(this, prediction.name, Toast.LENGTH_SHORT).show();
                Snackbar.make(rl, prediction.name, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    public void irInicio(){
        if (Util.existeConexionInternet()){
            iniciarConectividad();
        } else {
            final Dialog dialog = DialogHelper.crearDialogAlerta(this, R.layout.dialog_alerta, false, "Revise su conexión", "Sin conexion");
            Button aceptar = (Button) dialog.findViewById(R.id.dialog_aceptar);
            aceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    irInicio();
                    dialog.dismiss();
                }
            });
        }
    }

    private void iniciarConectividad() {
        try {
            socket = IO.socket("http://192.168.114.235:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.connect();
        Log.d("CONECTADO", "CONECTADO");
    }

    private Emitter.Listener onNuevaPregunta = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    JSONObject data = (JSONObject) args[0];
                    String pregunta;
                    try {
                        Log.d("CONECTADO", "pregunta");
                        pregunta = data.getString("pregunta");
                    } catch (JSONException e) {
                        return;
                    }

                    // add the message to view
                    agregarPregunta(pregunta);
                }
            });
        }
    };

    public void agregarPregunta(String pregunta){
        this.pregunta.setText(pregunta);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
