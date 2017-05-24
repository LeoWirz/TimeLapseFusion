package www.epfl.ch.hypergogetaapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by SebastienSpeierer on 4/7/2017.
 */

// This class internally manage the loading and buffer of frames
public class FrameManager {
    //public FrameManager(VideoRenderer vr, FFmpegMediaMetadataRetriever mmr, ImageView imgViewFirstFrame, ImageView imgViewLastFrame) {
    public FrameManager(VideoRenderer vr) {
        this.vr = vr;
        firstFrame = 0;
        windowSize = 1;

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
        firstFrame = newFirstFrame;

        // update content on VideoRenderer
        vr.clear();
        vr.addFrame(getFrameAt(firstFrame));
    }

    // Add all the window of frames and render
    public void changeFrameOnStop(int newFirstFrame) {
        // Read the frame number in the text field
        firstFrame = newFirstFrame;

        // update content on VideoRenderer
        vr.clear();

        for (int i = 0; i < windowSize; i++) {
            Bitmap frame = getFrameAt(firstFrame + i);
            if (frame != null)
                vr.addFrame(frame);
        }
    }

    public void changeWindowSize(int newWindowSize) {

        // Only add frames if we need more than before
        if (newWindowSize > windowSize) {
            for (int i = windowSize; i < newWindowSize; i++) {
                Bitmap frame = getFrameAt(firstFrame + i);
                if (frame != null)
                    vr.addFrame(frame);
            }
        } else {
            vr.clear();
            for (int i = 0; i < newWindowSize; i++) {
                Bitmap frame = getFrameAt(firstFrame + i);
                if (frame != null)
                    vr.addFrame(frame);
            }
        }

        windowSize = newWindowSize;
        vr.setWindowSize(newWindowSize);
    }

    public void setDataSource(Context ctx, Uri uri) {
        LoadingBGTask task = new LoadingBGTask(this, uri, ctx);
        task.start();
    }

    public ConcurrentHashMap<Integer, Bitmap> cache;

    private int firstFrame;
    private int windowSize;

    public int loadingProgressionPreview;
    public int loadingProgression;

    public int maxNumFrame;

    // reference to the video renderer
    private VideoRenderer vr;
}
