package www.epfl.ch.hypergogetaapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import android.app.Activity;
import android.os.Bundle;
import android.widget.SeekBar;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST;
import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {

    final static int MAX_WINDOW_SIZE = 15;
    // TODO this depends on the video
    final static int MAX_FRAME_NUMBER = 100;

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
        retriever = new MediaMetadataRetriever();
        showFileChooser();

        ImageView imgViewFirstFrame = (ImageView) findViewById(R.id.imageViewFirstFrame);
        ImageView imgViewLastFrame  = (ImageView) findViewById(R.id.imageViewLastFrame);

        frameManager = new FrameManager(videoRenderer, retriever, imgViewFirstFrame, imgViewLastFrame);
        frameManager.start();

        //seekbar for first frame
        final SeekBar seekBarFirstFrame = (SeekBar)findViewById(R.id.seekBarFirstFrame);

        // TODO find out number of frames in the video
        seekBarFirstFrame.setMax(MAX_FRAME_NUMBER);

        final EditText editTextFirstFrame = (EditText)findViewById(R.id.editTextFirstFrame);

        seekBarFirstFrame.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editTextFirstFrame.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                frameManager.changeFirstFrame(seekBar.getProgress());
            }
        });

        editTextFirstFrame.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int value = Integer.parseInt(v.getText().toString());
                if(value > MAX_FRAME_NUMBER){
                    value = MAX_FRAME_NUMBER;
                    editTextFirstFrame.setText(String.valueOf(value));
                }

                seekBarFirstFrame.setProgress(value);
                frameManager.changeFirstFrame(value);
                return true;
            }
        });

        //seekbar for window size
        final SeekBar seekBarWindowSize = (SeekBar)findViewById(R.id.seekBarWindowSize);
        seekBarWindowSize.setMax(15);
        final EditText editTextWindowSize = (EditText)findViewById(R.id.editTextWindowSize);

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

                // Give it to the retriever
                retriever.setDataSource(this, videoUri);
            }
        }
    }

    // Path of the selected video
    private Uri videoUri;

    // Handle the video
    private MediaMetadataRetriever retriever;
    private VideoRenderer videoRenderer;
    private FrameManager frameManager;
}
