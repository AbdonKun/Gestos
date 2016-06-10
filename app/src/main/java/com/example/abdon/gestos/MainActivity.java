package com.example.abdon.gestos;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener{

    private GestureLibrary gesLib;
    private final File file = new File(Environment.getExternalStorageDirectory(),"gestures");
    private static GestureLibrary almacen;
    RelativeLayout rl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
        TextView reconocido = (TextView)findViewById(R.id.reconocido);
        TextView pregunta = (TextView)findViewById(R.id.pregunta);
        rl = (RelativeLayout) findViewById(R.id.relative);

        GestureOverlayView overlayView = (GestureOverlayView)findViewById(R.id.gesto);
        overlayView.addOnGesturePerformedListener(this);

        gesLib = GestureLibraries.fromFile(file);

        if (!gesLib.load()) {
            finish();
        }
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
            //if (prediction.name == prediction.name){
              //  Toast.makeText(this, "If:  " + prediction.name, Toast.LENGTH_SHORT).show();
            //}
        }
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
