package www.epfl.ch.hypergogetaapp;


import wseemann.media.FFmpegMediaMetadataRetriever;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import static android.R.attr.max;
import static android.R.attr.maxButtonHeight;
import static android.os.Build.VERSION_CODES.M;


public class MainActivity extends AppCompatActivity {

    final static int MAX_WINDOW_SIZE = 8;

    public void pause(){
        isPlaying = false;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonPlay.setText("Play");
                seekBarFirstFrame.setEnabled(true);
                seekBarWindowSize.setEnabled(true);
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //new www.epfl.ch.hypergogetaapp.MainActivity.VideoRenderer();
        GLSurfaceView view = (GLSurfaceView) findViewById(R.id.glView);
        view.setEGLContextClientVersion(2);
        //view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        videoRenderer = new VideoRenderer();
        view.setRenderer(videoRenderer);
        //view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        // Initialize media retriever
        //retriever = new FFmpegMediaMetadataRetriever();
        seekBarFirstFrame = (SeekBar)findViewById(R.id.seekBarFirstFrame);
        buttonPlay = (Button)findViewById(R.id.buttonPlay);

        frameManager = new FrameManager(videoRenderer, this, seekBarFirstFrame);
        isPlaying = false;

        //button play

        buttonPlay.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(!isPlaying) {
                    isPlaying = true;
                    frameManager.play();
                    buttonPlay.setText("Pause");
                    seekBarFirstFrame.setEnabled(false);
                    seekBarWindowSize.setEnabled(false);
                } else {
                    isPlaying = false;
                    frameManager.pause();
                    buttonPlay.setText("Play");
                    seekBarFirstFrame.setEnabled(true);
                    seekBarWindowSize.setEnabled(true);
                }
            }
        });

        //seekbar for first frame
        final TextView textViewFirstFrame = (TextView)findViewById(R.id.textViewFirstFrame);
        seekBarFirstFrame.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long value = seekBar.getProgress() * frameManager.maxNumFrame / 100;
                textViewFirstFrame.setText(Long.toString(value));

                if (value <= frameManager.loadingProgression) {
                    textViewFirstFrame.setBackgroundColor(0x9900FF00);
                } else {
                    textViewFirstFrame.setBackgroundColor(0x99AA0000);
                }

                if (!isPlaying) {
                    // Use this for interactive update
                    if (value <= frameManager.loadingProgression) {
                        frameManager.changeFrameOnScroll((int) value);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Use this for updating frame only when we release the slider
                long value = seekBar.getProgress() * frameManager.maxNumFrame / 100;
                if (value <= frameManager.loadingProgression) {
                    frameManager.changeFrameOnStop((int)value);
                }
            }
        });
        //seekbar for window size
        seekBarWindowSize = (SeekBar)findViewById(R.id.seekBarWindowSize);
        seekBarWindowSize.setMax(MAX_WINDOW_SIZE-1);
        final TextView editTextWindowSize = (TextView)findViewById(R.id.editTextWindowSize);

        seekBarWindowSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress++;
                editTextWindowSize.setText(String.valueOf(progress));
                frameManager.changeWindowSize(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //frameManager.changeWindowSize(seekBar.getProgress());
            }
        });


        //seekbar for sigma
        final SeekBar seekBarSigma = (SeekBar)findViewById(R.id.seekBarSigma);
        seekBarSigma.setMax(100);

        seekBarSigma.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Float value = (1+progress) / 101.f;
                videoRenderer.setSigma(value);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        //seekbar for expS
        final SeekBar seekBarExpS = (SeekBar)findViewById(R.id.seekBarExpS);
        seekBarExpS.setMax(100);

        seekBarExpS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Float value = (1+progress) / 101.f;
                videoRenderer.setExpS(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });



        //seekbar for ExpC
        final SeekBar seekBarExpC = (SeekBar)findViewById(R.id.seekBarExpC);
        seekBarExpC.setMax(100);

        seekBarExpC.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Float value = (1+progress) / 101.f;
                videoRenderer.setExpC(value);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        //seekbar for ExpE
        final SeekBar seekBarExpE = (SeekBar)findViewById(R.id.seekBarExpE);
        seekBarExpE.setMax(100);

        seekBarExpE.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Float value = (1+progress) / 101.f;
                videoRenderer.setExpE(value);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        //seekbar for brightness
        final SeekBar seekBarBrightness = (SeekBar)findViewById(R.id.seekBarBrightness);
        seekBarBrightness.setMax(100);

        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Float value = (1+progress) / 101.f;
                videoRenderer.setBrightness(value);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        //seekbar for brightness
        final SeekBar seekBarContrast = (SeekBar)findViewById(R.id.seekBarContrast);
        seekBarContrast.setMax(100);

        seekBarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Float value = (1+progress) / 101.f;
                videoRenderer.setContrast(value);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        showFileChooser();
    }

    // Not important
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Not important
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

    private int PICK_VIDEO_REQUEST = 2;

    // Start a File Chooser. Called at the very beginning of the app
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a video"),
                    PICK_VIDEO_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
        }
    }

    // Event listener for the FileChooser. Recover the path of the chosen video and give it to the retriever
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_VIDEO_REQUEST) {
                // Get the path of the selected video
                videoUri = data.getData();

                frameManager.setDataSource(videoUri);
                frameManager.start();
            }
        }
    }

    // Path of the selected video
    private Uri videoUri;

    private VideoRenderer videoRenderer;
    private FrameManager frameManager;

    private SeekBar seekBarFirstFrame;
    private SeekBar seekBarWindowSize;
    private Button buttonPlay;

    private Boolean isPlaying;
}
