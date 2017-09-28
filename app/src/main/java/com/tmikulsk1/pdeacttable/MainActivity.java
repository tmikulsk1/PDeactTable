package com.tmikulsk1.pdeacttable;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;

import it.beppi.knoblibrary.Knob;

import static org.puredata.android.io.PdAudio.stopAudio;

public class MainActivity extends AppCompatActivity {

    private Knob knob1, knob2, knob3, knob4, knob5, knob6, knob7, knob8;
    private ImageView image1, image2, image3, image4, image5, image6, image7;
    private boolean toggle = false;
    private PdUiDispatcher dispatcher;
    private int pdPatchI = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //FULL SCREEN - add the two lines below
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);


        //INIT PD - initiates PD, load the patch
        try {
            initPD();
            loadPdPatch();
        } catch (IOException e){
            Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
            finish();
        }

        //INIT GUI - initiates the GUI
        initStopped();
        initGui();

        knob8.setOnStateChanged(new Knob.OnStateChanged() {
            @Override
            public void onState(int i) {
                PdBase.sendFloat("mVol", getScale(1, 0, knob8.getState(), 0, 50));
            }
        });

    }

    @Override
    protected void onResume() {
        PdAudio.startAudio(getApplicationContext());
        super.onResume();
    }

    @Override
    protected void onStop() {
        stopAudio();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (pdPatchI != 0){
            PdBase.closePatch(pdPatchI);
            pdPatchI = 0;
        }
        dispatcher.release();
        PdBase.release();
        super.onDestroy();
    }

    public void initPD() throws IOException{
        int sampleRate = AudioParameters.suggestSampleRate();
        PdAudio.initAudio(sampleRate, 0, 2, 8, true);

        dispatcher = new PdUiDispatcher();
        PdBase.setReceiver(dispatcher);
    }

    public void loadPdPatch() throws IOException{
        File dir = getFilesDir();
        IoUtils.extractZipResource(getResources().openRawResource(R.raw.npdeacttable), dir, true);
        File pdPatch = new File(dir, "main_pdeacttable.pd");
        pdPatchI = PdBase.openPatch(pdPatch.getAbsolutePath());
    }

    /**
     *
     */
    public void initGui(){
        knob1 = (Knob) findViewById(R.id.knob1);
        knob2 = (Knob) findViewById(R.id.knob2);
        knob3 = (Knob) findViewById(R.id.knob3);
        knob4 = (Knob) findViewById(R.id.knob4);
        knob5 = (Knob) findViewById(R.id.knob5);
        knob6 = (Knob) findViewById(R.id.knob6);
        knob7 = (Knob) findViewById(R.id.knob7);
        knob8 = (Knob) findViewById(R.id.knob8);

        image1 = (ImageView) findViewById(R.id.image1);
        image2 = (ImageView) findViewById(R.id.image2);
        image3 = (ImageView) findViewById(R.id.image3);
        image4 = (ImageView) findViewById(R.id.image4);
        image5 = (ImageView) findViewById(R.id.image5);
        image6 = (ImageView) findViewById(R.id.image6);
        image7 = (ImageView) findViewById(R.id.image7);

        initKnob(knob1, image1, "p1","s1", "vol1");
        initKnob(knob2, image2, "p2","s2", "vol2");
        initKnob(knob3, image3, "p3","s3", "vol3");
        initKnob(knob4, image4, "p4","s4", "vol4");
        initKnob(knob5, image5, "p5","s5", "vol5");
        initKnob(knob6, image6, "p6","s6", "vol6");
        initKnob(knob7, image7, "p7","s7", "vol7");

    }

    public void initKnob(final Knob knob, final ImageView image, final String sendPlay, final String sendStop, final String sendFloat) {

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (toggle == false) {
                    toggle = true;
                    image.setImageResource(R.drawable.ico2);
                    PdBase.sendBang(sendPlay);

                    knob.setOnStateChanged(new Knob.OnStateChanged() {
                        @Override
                        public void onState(int i) {
                            PdBase.sendFloat(sendFloat, getScale(1, 0, knob.getState(), 0, 50));
                        }
                    });

                    //Toast.makeText(getApplicationContext(), "M: " + sendPlay, Toast.LENGTH_SHORT).show();
                } else {
                    toggle = false;
                    PdBase.sendBang(sendStop);
                    image.setImageResource(R.drawable.ico1);

                    //Toast.makeText(getApplicationContext(), "M: " + sendStop, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    public void initStopped(){
        PdBase.sendBang("s1");
        PdBase.sendBang("s2");
        PdBase.sendBang("s3");
        PdBase.sendBang("s4");
        PdBase.sendBang("s5");
        PdBase.sendBang("s6");
        PdBase.sendBang("s7");
    }
    public float getScale(float maxAllowed, float minAllowed, int unscaledNum, int min, int max){

        float val = ((maxAllowed - minAllowed) * ((float)unscaledNum - (float)min) / ((float)max - (float)min) + minAllowed);
        return val;

    }
}
