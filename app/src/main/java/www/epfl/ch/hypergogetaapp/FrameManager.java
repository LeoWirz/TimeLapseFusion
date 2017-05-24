package www.epfl.ch.hypergogetaapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.util.concurrent.ConcurrentHashMap;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by SebastienSpeierer on 4/7/2017.
 */

// This class internally manage the loading and buffer of frames
public class FrameManager extends Thread {
    public FrameManager(VideoRenderer vr, SeekBar seekSlider) {
        this.vr = vr;
        this.seekSlider = seekSlider;
        windowSize = 1;
        seekPosition = 1;
        isPlaying = false;
        cache = new ConcurrentHashMap<Integer, Bitmap>();
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
        vr.addFrame(getFrameAt(seekPosition));
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
    }

    public void changeWindowSize(int newWindowSize) {

        // Only add frames if we need more than before
        if (newWindowSize > windowSize) {
            for (int i = windowSize; i < newWindowSize; i++) {
                Bitmap frame = getFrameAt(seekPosition + i);
                if (frame != null)
                    vr.addFrame(frame);
            }
        } else {
            vr.clear();
            for (int i = 0; i < newWindowSize; i++) {
                Bitmap frame = getFrameAt(seekPosition + i);
                if (frame != null)
                    vr.addFrame(frame);
            }
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

    public void setDataSource(Context ctx, Uri uri) {
        LoadingBGTask task = new LoadingBGTask(this, uri, ctx);
        task.start();
    }

    public void run() {
        while(true) {
            if (isPlaying) {
                seekPosition++;
                seekSlider.setProgress(100 * seekPosition / maxNumFrame);

                // TODO
                //vr.addFrameNext(getFrameAt(seekPosition));

                vr.clear();
                vr.addFrame(getFrameAt(seekPosition));
            }

            try {
                this.sleep((long)25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    int seekPosition;


    public ConcurrentHashMap<Integer, Bitmap> cache;

    private int windowSize;

    private Boolean isPlaying;

    public int loadingProgressionPreview;
    public int loadingProgression;

    public int maxNumFrame;

    public SeekBar seekSlider;

    // reference to the video renderer
    private VideoRenderer vr;
}
