package www.epfl.ch.hypergogetaapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.widget.SeekBar;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static java.lang.Math.max;

/**
 * Created by SebastienSpeierer on 5/24/2017.
 */

public class LoadingBGTask extends Thread {

    public LoadingBGTask(FrameManager fm, Uri u, MainActivity mainActivity) {
        this.fm = fm;
        uri = u;
        this.mainActivity = mainActivity;
        mmr = new FFmpegMediaMetadataRetriever();
        mmr.setDataSource(mainActivity, uri);
        fm.maxNumFrame = max(Integer.valueOf(mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION)) / 30, 600);
        Log.d("SS_TAG", "maxNumFrame: " + fm.maxNumFrame);
    }


    private Boolean loadFrameAt(int frameNumber, int option) {
        Log.d("SS_TAG", "loadFrameAt: " + frameNumber );
        Bitmap frame = mmr.getFrameAtTime(frameNumber * ((long)1000000 / (long)30), option);
        Log.d("SS_TAG", "-> frame: " + frame);
        if (frame != null)
            fm.cache.put(frameNumber, frame);

        return frame != null;
    }

    public void run() {
        int countTries;
        for (int i = 0; i < fm.maxNumFrame; i++) {
            // Once we have loaded the first frame, display it
            if (i==1)
                ((SeekBar)mainActivity.findViewById(R.id.seekBarFirstFrame)).setProgress(1);

            countTries = 0;
            while(!loadFrameAt(i, FFmpegMediaMetadataRetriever.OPTION_CLOSEST)){
                Log.d("SS_TAG", "try again");
                mmr = new FFmpegMediaMetadataRetriever();
                mmr.setDataSource(mainActivity, uri);
                if (countTries++ > 2) {
                    Log.d("SS_TAG", "gave up at: " + i);
                    fm.maxNumFrame = i - 1;
                    return;
                }
            }
            fm.loadingProgression = i;
        }
    }

    private FrameManager fm;
    private FFmpegMediaMetadataRetriever mmr;
    private Uri uri;
    private MainActivity mainActivity;
}