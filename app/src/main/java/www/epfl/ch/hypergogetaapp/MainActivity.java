package www.epfl.ch.hypergogetaapp;


import wseemann.media.FFmpegMediaMetadataRetriever;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.Toast;

import java.util.ArrayList;

import static android.R.attr.max;
import static android.R.attr.maxButtonHeight;
import static android.os.Build.VERSION_CODES.M;
import static android.provider.AlarmClock.EXTRA_MESSAGE;


public class MainActivity extends AppCompatActivity {

    final static int MAX_WINDOW_SIZE = 100;

    public void pause() {
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

    int leftMaskBorder = 0;
    int topMaskBorder = 0;
    int rightMaskBorder = 0;
    int bottomMaskBorder = 0;

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
        seekBarFirstFrame = (SeekBar) findViewById(R.id.seekBarFirstFrame);
        buttonPlay = (Button) findViewById(R.id.buttonPlay);

        frameManager = new FrameManager(videoRenderer, this, seekBarFirstFrame);
        isPlaying = false;


        buttonPlay.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isPlaying) {
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
        final TextView textViewFirstFrame = (TextView) findViewById(R.id.textViewFirstFrame);
        //seekbar for first frame
        seekBarFirstFrame = (SeekBar) findViewById(R.id.seekBarFirstFrame);

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
                    frameManager.changeFrameOnStop((int) value);
                }
            }
        });
        //seekbar for window size
        seekBarWindowSize = (SeekBar) findViewById(R.id.seekBarWindowSize);
        seekBarWindowSize.setMax(MAX_WINDOW_SIZE - 1);
        final TextView editTextWindowSize = (TextView) findViewById(R.id.editTextWindowSize);

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
            }
        });


        //seekbar for sigma
        final SeekBar seekBarSigma = (SeekBar) findViewById(R.id.seekBarSigma);
        seekBarSigma.setMax(100);


        seekBarSigma.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Float value = (1 + progress) / 101.f;
                videoRenderer.setSigma(value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //seekbar for expS
        final SeekBar seekBarExpS = (SeekBar) findViewById(R.id.seekBarExpS);
        seekBarExpS.setMax(100);

        seekBarExpS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Float value = ((1 + progress) / 101.f) * 2;
                videoRenderer.setExpS(value);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        //seekbar for ExpC
        final SeekBar seekBarExpC = (SeekBar) findViewById(R.id.seekBarExpC);
        seekBarExpC.setMax(100);


        seekBarExpC.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Float value = ((1 + progress) / 101.f) * 2;
                videoRenderer.setExpC(value);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //seekbar for ExpE
        final SeekBar seekBarExpE = (SeekBar) findViewById(R.id.seekBarExpE);
        seekBarExpE.setMax(100);

        seekBarExpE.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Float value = ((1 + progress) / 101.f) * 2;
                videoRenderer.setExpE(value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });

        //seekbar for brightness
        final SeekBar seekBarBrightness = (SeekBar) findViewById(R.id.seekBarBrightness);
        seekBarBrightness.setMax(100);

        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Float value = (1 + progress) / 101.f;
                videoRenderer.setBrightness(value);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //seekbar for brightness
        final SeekBar seekBarContrast = (SeekBar) findViewById(R.id.seekBarContrast);
        seekBarContrast.setMax(100);

        seekBarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Float value = (1 + progress) / 101.f;
                videoRenderer.setContrast(value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        showFileChooser();

        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // launch mask activity
                Intent intent = new Intent(MainActivity.this, MaskActivity.class);

                //TODO get image
                //Bitmap image = ((BitmapDrawable)imgViewFirstFrame.getDrawable()).getBitmap();

                //intent.putExtra("imagebitmap", image);

                ArrayList<Integer> list = new ArrayList<Integer>();
                list.add(leftMaskBorder);
                list.add(topMaskBorder);
                list.add(rightMaskBorder);
                list.add(bottomMaskBorder);
                intent.putIntegerArrayListExtra("borders", list);

                //startActivityForResult(intent, CHOOSE_MASK_AREA);
                startActivity(intent);
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
    private int CHOOSE_MASK_AREA = 3;

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

            if (requestCode == CHOOSE_MASK_AREA) {
                //get the borders here
                ArrayList<Integer> test = data.getIntegerArrayListExtra("borders");
                leftMaskBorder = test.get(0);
                topMaskBorder = test.get(1);
                rightMaskBorder = test.get(2);
                bottomMaskBorder = test.get(3);
                Toast.makeText(getApplicationContext(), String.valueOf(leftMaskBorder), Toast.LENGTH_SHORT).show();
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
