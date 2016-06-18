package tk.gringraz.detectorgestosandroid;

import android.app.Dialog;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Creado por GRINGRAZ el 10-06-2016.
 */
public class MainActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener{

    @BindView(R.id.gestures) GestureOverlayView overlayView;
    @BindView(R.id.pregunta) TextView pregunta;
    @BindView(R.id.reconocido) TextView reconocido;

    private GestureLibrary gesLib;
    private com.github.nkzawa.socketio.client.Socket socket;
    private final File file = new File(Environment.getExternalStorageDirectory(),"gestures");
    private OnRespuestaListener listener;
    private HashMap<String, String> preguntasRespuestas;
    private ArrayList<String> preguntas;
    private int index;
    private Pregunta preguntaPojo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        preguntas = new ArrayList<>();

        try {
            socket = IO.socket("http://186.64.123.115:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        overlayView.addOnGesturePerformedListener(this);
        gesLib = GestureLibraries.fromFile(file);
        //armarPreguntasRespuestas();

        evaluarRespuesta(new OnRespuestaListener() {
            @Override
            public boolean onRespuestaCorrecta() {
                index++;
                comprobarUltimaPregunta();
                reconocido.setText(R.string.respuesta_correcta);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pregunta.setText(preguntas.get(index));
                        reconocido.setText(" ");
                    }
                }, 1000);
                return true;
            }

            @Override
            public boolean onRespuestaIncorrecta() {
                reconocido.setText(R.string.respuesta_incorrecta);
                return false;
            }
        });

        //irInicio();
        socket.on("nueva pregunta", onPreguntas);
        socket.connect();
        socket.emit("obtener preguntas");

    }

    public void comprobarUltimaPregunta(){
        if (index == 5){
            reconocido.setText(R.string.respuesta_correcta_final);
            Toast.makeText(MainActivity.this, "FIN", Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 2000);
        }
    }

    public void armarPreguntasRespuestas(){
        preguntas = new ArrayList<>();
        preguntas.add("1 + 0 = ?");
        preguntas.add("1 + 1 = ?");
        preguntas.add("1 + 2 = ?");
        preguntas.add("1 + 3 = ?");
        preguntas.add("1 + 4 = ?");

        preguntasRespuestas = new HashMap<>();
        preguntasRespuestas.put(preguntas.get(0), "1");
        preguntasRespuestas.put(preguntas.get(1), "2");
        preguntasRespuestas.put(preguntas.get(2), "3");
        preguntasRespuestas.put(preguntas.get(3), "4");
        preguntasRespuestas.put(preguntas.get(4), "5");

        pregunta.setText(preguntas.get(index));
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        Log.d("Path", "dsfsdf");
        ArrayList<Prediction> predictions = gesLib.recognize(gesture);
        for (Prediction prediction : predictions) {
            if (index <= preguntas.size()-1){
                if (prediction.name.equals(preguntasRespuestas.get(preguntas.get(index)))) {
                    if (listener.onRespuestaCorrecta()) return;
                } else if (!listener.onRespuestaIncorrecta()) return;
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
            socket = IO.socket("http://186.64.123.115:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.connect();
        agendarEventos();
    }

    private void agendarEventos() {
        if(socket != null) {
            socket.on("nueva pregunta", onPreguntas);
            socket.emit("obtener preguntas");
        }
    }

    public Emitter.Listener onPreguntas = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JsonArray jsonArray = new JsonParser().parse(args[0].toString()).getAsJsonArray();

                    Gson gson = new Gson();

                    for (JsonElement preguntaJson : jsonArray) {
                        preguntaPojo = gson.fromJson(preguntaJson, Pregunta.class);
                        preguntas.add(preguntaPojo.getPregunta());
                    }
                  agregarPregunta(preguntas.get(0));
                }
            });
        }
    };

    private void evaluarRespuesta(OnRespuestaListener listener){
        this.listener = listener;
    }

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

    private interface OnRespuestaListener{
        boolean onRespuestaCorrecta();
        boolean onRespuestaIncorrecta();
    }
}