package www.epfl.ch.hypergogetaapp;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by SebastienSpeierer on 4/7/2017.
 */

// This class internally manage the loading and buffer of frames
public class FrameManager extends Thread {
    //public FrameManager(VideoRenderer vr, FFmpegMediaMetadataRetriever mmr, ImageView imgViewFirstFrame, ImageView imgViewLastFrame) {
    public FrameManager(VideoRenderer vr, MediaMetadataRetriever mmr, ImageView imgViewFirstFrame, ImageView imgViewLastFrame) {
        this.vr = vr;
        this.mmr = mmr;

        this.imgViewFirstFrame = imgViewFirstFrame;
        this.imgViewLastFrame = imgViewLastFrame;

        firstFrame = 0;
        windowSize = 1;

        cache = new ConcurrentHashMap<Integer, Bitmap>();

        doneLoadingFrames = false;

        // TODO
        totalFrameNumber = 100;
    }

    private Bitmap getFrameAt(int frameNumber) {
        Log.d("SS_TAG", "getFrameAt: " + frameNumber );
        Bitmap frame;
        if (cache.containsKey(frameNumber)) {
            frame = cache.get(frameNumber);
        } else {
            Log.d("SS_TAG", "getFrameAt - null");
            frame = null;
            //frame = mmr.getFrameAtTime((long)frameNumber * ((long)1000000.f / (long)25.f), MediaMetadataRetriever.OPTION_CLOSEST);
            //cache.put(frameNumber, frame);
        }
        return frame;
    }

    private Boolean loadFrameAt(int frameNumber, int option) {
        Log.d("SS_TAG", "loadFrameAt: " + frameNumber );
        Bitmap frame = mmr.getFrameAtTime(frameNumber * ((long)1000000 / (long)30), option);
        Log.d("SS_TAG", "-> frame: " + frame);
        //Bitmap frame = mmr.getFrameAtTime();
        if (frame != null)
            cache.put(frameNumber, frame);

        return frame != null;
    }

    public void changeFirstFrame(int newFirstFrame) {


        // Read the frame number in the text field
        //firstFrame = newFirstFrame;
        firstFrame = newFirstFrame;

        // Retrieve the frame from the video (time in micro seconds)
        Bitmap bmpFirstFrame = getFrameAt(firstFrame);
        Bitmap bmpLastFrame  = getFrameAt(firstFrame + windowSize);

        // Display the frame
        if(bmpFirstFrame != null)
            imgViewFirstFrame.setImageBitmap(bmpFirstFrame);

        if(bmpLastFrame != null)
            imgViewLastFrame.setImageBitmap(bmpLastFrame);

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
        }

        windowSize = newWindowSize;
        vr.setWindowSize(newWindowSize);

        // Display the frame
        Bitmap bmpLastFrame = getFrameAt(firstFrame + windowSize);
        if(bmpLastFrame != null)
            imgViewLastFrame.setImageBitmap(bmpLastFrame);
    }

    public void run() {
        loadingProgression = 0;

        Log.d("SS_TAG", "numberOfFrames: " + maxNumFrame);

        // Then load the correct images
        for (int i = 0; i < maxNumFrame; i++) {
            if (loadFrameAt(i, MediaMetadataRetriever.OPTION_CLOSEST)) {
                loadingProgression = i;
            } else {
                Log.d("SS_TAG", "break");
                // update slider size
                maxNumFrame = i - 1;
                break;
            }
        }
    }

    boolean doneLoadingFrames;
    int totalFrameNumber;

    private ConcurrentHashMap<Integer, Bitmap> cache;

    private int firstFrame;
    private int windowSize;

    public int loadingProgressionPreview;
    public int loadingProgression;

    public int maxNumFrame;

    // reference to the video renderer
    private VideoRenderer vr;

    // Retriever with the current video bound to
    private MediaMetadataRetriever mmr;
    //private FFmpegMediaMetadataRetriever mmr;

    private ImageView imgViewFirstFrame;
    private ImageView imgViewLastFrame;
}
