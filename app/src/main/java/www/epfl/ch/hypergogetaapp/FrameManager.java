package www.epfl.ch.hypergogetaapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.util.concurrent.ConcurrentHashMap;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by SebastienSpeierer on 4/7/2017.
 */

// This class internally manage the loading and buffer of frames
public class FrameManager extends Thread {
    public FrameManager(VideoRenderer vr, MainActivity mainActivity, SeekBar seekSlider) {
        this.vr = vr;
        this.seekSlider = seekSlider;
        this.mainActivity = mainActivity;
        windowSize = 1;
        seekPosition = 1;
        isPlaying = false;
        cache = new ConcurrentHashMap<Integer, Bitmap>();
        maxWindowSizeSinceLastClear = 1;
    }

    private Bitmap getFrameAt(int frameNumber) {
        Log.d("SS_TAG", "getFrameAt: " + frameNumber );
        Bitmap frame;
        if (cache.containsKey(frameNumber)) {
            frame = cache.get(frameNumber);
        } else {
            Log.d("SS_TAG", "getFrameAt - null");
            frame = null;
        }
        return frame;
    }

    // add only the current frame and render this frame only (also update the guides)
    public void changeFrameOnScroll(int newFirstFrame) {
        // Read the frame number in the text field
        seekPosition = newFirstFrame;

        // update content on VideoRenderer
        vr.clear();

        Bitmap frame = getFrameAt(seekPosition);
        if (frame != null)
            vr.addFrame(frame);
    }

    // Add all the window of frames and render
    public void changeFrameOnStop(int newFirstFrame) {
        // Read the frame number in the text field
        seekPosition = newFirstFrame;

        // update content on VideoRenderer
        vr.clear();

        for (int i = 0; i < windowSize; i++) {
            Bitmap frame = getFrameAt(seekPosition + i);
            if (frame != null)
                vr.addFrame(frame);
        }
        maxWindowSizeSinceLastClear = windowSize;
    }

    public void changeWindowSize(int newWindowSize) {

        // Only add frames if we need more than before
        if (newWindowSize > maxWindowSizeSinceLastClear) {
            for (int i = windowSize; i < newWindowSize; i++) {
                Bitmap frame = getFrameAt(seekPosition + i);
                if (frame != null)
                    vr.addFrame(frame);

            }
            maxWindowSizeSinceLastClear = newWindowSize;
        }

        windowSize = newWindowSize;
        vr.setWindowSize(newWindowSize);
    }

    public void play() {
        isPlaying = true;
    }

    public void pause() {
        isPlaying = false;
    }

    public void setDataSource(Uri uri) {
        LoadingBGTask task = new LoadingBGTask(this, uri, mainActivity);
        task.start();
    }

    public void run() {
        while(true) {
            if (isPlaying) {
                if (seekPosition+1 > loadingProgression) {
                    Log.d("SS_TAG", "We are going too fast: pause");
                    isPlaying = false;
                    mainActivity.pause();
                } else {
                    seekPosition++;

                    seekSlider.setProgress(100 * seekPosition / maxNumFrame);

                    // TODO
                    //vr.addFrameNext(getFrameAt(seekPosition));

                    //vr.clear();
                    vr.addFrame(getFrameAt(seekPosition));
                }
            }

            try {
                this.sleep((long)30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    int seekPosition;


    public ConcurrentHashMap<Integer, Bitmap> cache;

    private int windowSize;

    private Boolean isPlaying;

    public int loadingProgression;

    private int maxWindowSizeSinceLastClear;

    private Boolean hasCleared;

    public int maxNumFrame;

    public SeekBar seekSlider;

    private MainActivity mainActivity;

    // reference to the video renderer
    private VideoRenderer vr;
}
