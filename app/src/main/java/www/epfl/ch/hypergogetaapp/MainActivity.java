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


public class MainActivity extends AppCompatActivity {

    final static int MAX_WINDOW_SIZE = 8;
    // TODO this depends on the video
    final static int MAX_FRAME_NUMBER = 100;
    final static int MAX_ALPHA_VALUE = 1;
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
                    frameManager.changeFirstFrame((int)value);
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
                //frameManager.changeFirstFrame(seekBar.getProgress());
            }
        });

        //seekbar for window size
        final SeekBar seekBarWindowSize = (SeekBar)findViewById(R.id.seekBarWindowSize);
        seekBarWindowSize.setMax(MAX_WINDOW_SIZE);
        final TextView texViewtWindowSize = (TextView)findViewById(R.id.textTextWindowSize);

        seekBarWindowSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                texViewtWindowSize.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                frameManager.changeWindowSize(seekBar.getProgress());
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
