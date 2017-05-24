package www.epfl.ch.hypergogetaapp;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by SebastienSpeierer on 5/24/2017.
 */

public class LoadingBGTask extends Thread {

    public LoadingBGTask(FrameManager fm, MediaMetadataRetriever mmr, int min, int max) {
        this.fm = fm;
        this.mmr = mmr;
        this.min = min;
        this.max = max;
    }


    private Boolean loadFrameAt(int frameNumber, int option) {
        Log.d("SS_TAG", "loadFrameAt: " + frameNumber );
        Bitmap frame = mmr.getFrameAtTime(frameNumber * ((long)1000000 / (long)30), option);
        Log.d("SS_TAG", "-> frame: " + frame);
        //Bitmap frame = mmr.getFrameAtTime();
        if (frame != null)
            fm.cache.put(frameNumber, frame);

        return frame != null;
    }



    public void run() {
        // Then load the correct images
        for (int i = min; i < max; i++) {
            if (loadFrameAt(i, MediaMetadataRetriever.OPTION_CLOSEST)) {
                fm.loadingProgression = i;
            } else {
                Log.d("SS_TAG", "break");
                // update slider size
                fm.maxNumFrame = i - 1;
                break;
            }
        }
    }


    private int min;
    private int max;
    private FrameManager fm;
    private MediaMetadataRetriever mmr;
}