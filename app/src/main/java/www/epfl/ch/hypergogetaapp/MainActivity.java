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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import static android.R.attr.max;
import static android.R.attr.maxButtonHeight;
import static android.os.Build.VERSION_CODES.M;


public class MainActivity extends AppCompatActivity {

    final static int MAX_WINDOW_SIZE = 8;
    // TODO this depends on the video
    final static int MAX_FRAME_NUMBER = 100;
    final static int MAX_ALPHA_VALUE = 100;
    final static int MAX_SIGMA_VALUE = 30;

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
        showFileChooser();

        ImageView imgViewFirstFrame = (ImageView) findViewById(R.id.imageViewFirstFrame);
        ImageView imgViewLastFrame  = (ImageView) findViewById(R.id.imageViewLastFrame);

        frameManager = new FrameManager(videoRenderer, imgViewFirstFrame, imgViewLastFrame);

        //seekbar for first frame
        seekBarFirstFrame = (SeekBar)findViewById(R.id.seekBarFirstFrame);

        final TextView textViewFirstFrame = (TextView)findViewById(R.id.textViewFirstFrame);

        seekBarFirstFrame.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // Use this for interactive update
                long value = seekBar.getProgress() * frameManager.maxNumFrame / 100;
                textViewFirstFrame.setText(Long.toString(value));
                if (value <= frameManager.loadingProgression) {
                    frameManager.changeFrameOnScroll((int)value);
                }

                if(value <= frameManager.loadingProgression) {
                    textViewFirstFrame.setBackgroundColor(0x9900FF00);
                } else if (value <= frameManager.loadingProgressionPreview){
                    textViewFirstFrame.setBackgroundColor(0x99AAFFAA);
                } else {
                    textViewFirstFrame.setBackgroundColor(0x99AA0000);
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
        final SeekBar seekBarWindowSize = (SeekBar)findViewById(R.id.seekBarWindowSize);
        seekBarWindowSize.setMax(MAX_WINDOW_SIZE);
        final TextView editTextWindowSize = (TextView)findViewById(R.id.editTextWindowSize);

        seekBarWindowSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editTextWindowSize.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                frameManager.changeWindowSize(seekBar.getProgress());
            }
        });

        editTextWindowSize.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int value = Integer.parseInt(v.getText().toString());
                if(value > MAX_WINDOW_SIZE){
                    value = MAX_WINDOW_SIZE;
                    editTextWindowSize.setText(String.valueOf(value));
                }

                seekBarWindowSize.setProgress(value);
                frameManager.changeWindowSize(value);
                return true;
            }
        });
        

        //seekbar for sigma
        final SeekBar seekBarSigma = (SeekBar)findViewById(R.id.seekBarSigma);
        seekBarSigma.setMax(MAX_SIGMA_VALUE);
        final TextView editTextSigma = (TextView)findViewById(R.id.editTextSigma);

        seekBarSigma.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editTextSigma.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                frameManager.changeWindowSize(seekBar.getProgress());
            }
        });

        editTextSigma.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int value = Integer.parseInt(v.getText().toString());
                if(value > MAX_SIGMA_VALUE){
                    value = MAX_SIGMA_VALUE;
                    editTextSigma.setText(String.valueOf(value));
                }

                seekBarSigma.setProgress(value);
                frameManager.changeWindowSize(value);
                return true;
            }
        });


        //seekbar for sigma C
        final SeekBar seekBarAlphaC = (SeekBar)findViewById(R.id.seekBarAlphaC);
        seekBarAlphaC.setMax(MAX_ALPHA_VALUE);
        final TextView editTextAlphaC = (TextView)findViewById(R.id.editTextAlphaC);

        seekBarAlphaC.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Double p = (double)progress;
                editTextAlphaC.setText(String.valueOf(p/100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                frameManager.changeWindowSize(seekBar.getProgress());
            }
        });

        editTextAlphaC.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int value = Integer.parseInt(v.getText().toString());
                if(value > MAX_ALPHA_VALUE){
                    value = MAX_ALPHA_VALUE;
                    editTextAlphaC.setText(String.valueOf(value));
                }

                seekBarAlphaC.setProgress(value);
                frameManager.changeWindowSize(value);
                return true;
            }
        });


        //seekbar for sigma E
        final SeekBar seekBarAlphaE = (SeekBar)findViewById(R.id.seekBarAlphaE);
        seekBarAlphaE.setMax(MAX_ALPHA_VALUE);
        final TextView editTextAlphaE = (TextView)findViewById(R.id.editTextAlphaE);

        seekBarAlphaE.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Double p = (double)progress;
                editTextAlphaE.setText(String.valueOf(p/100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                frameManager.changeWindowSize(seekBar.getProgress());
            }
        });

        editTextAlphaE.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int value = Integer.parseInt(v.getText().toString());
                if(value > MAX_ALPHA_VALUE){
                    value = MAX_ALPHA_VALUE;
                    editTextAlphaE.setText(String.valueOf(value));
                }

                seekBarAlphaE.setProgress(value);
                frameManager.changeWindowSize(value);
                return true;
            }
        });


        //seekbar for sigma S
        final SeekBar seekBarAlphaS = (SeekBar)findViewById(R.id.seekBarAlphaS);
        seekBarAlphaS.setMax(MAX_ALPHA_VALUE);
        final TextView editTextAlphaS = (TextView)findViewById(R.id.editTextAlphaS);

        seekBarAlphaS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Double p = (double)progress;
                editTextAlphaS.setText(String.valueOf(p/100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                frameManager.changeWindowSize(seekBar.getProgress());
            }
        });

        editTextAlphaS.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int value = Integer.parseInt(v.getText().toString());
                if(value > MAX_ALPHA_VALUE){
                    value = MAX_ALPHA_VALUE;
                    editTextAlphaS.setText(String.valueOf(value));
                }

                seekBarAlphaS.setProgress(value);
                frameManager.changeWindowSize(value);
                return true;
            }
        });

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

                frameManager.setDataSource(getApplicationContext(), videoUri);
                //frameManager.start();
            }
        }
    }

    // Path of the selected video
    private Uri videoUri;

    private VideoRenderer videoRenderer;
    private FrameManager frameManager;

    private SeekBar seekBarFirstFrame;
}
