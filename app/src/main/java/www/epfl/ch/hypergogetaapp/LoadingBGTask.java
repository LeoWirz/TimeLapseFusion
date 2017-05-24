package www.epfl.ch.hypergogetaapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by SebastienSpeierer on 5/24/2017.
 */

public class LoadingBGTask extends Thread {

    public LoadingBGTask(FrameManager fm, Uri u, Context c) {
        this.fm = fm;
        uri = u;
        ctx = c;
        mmr = new FFmpegMediaMetadataRetriever();
        mmr.setDataSource(ctx, uri);
        fm.maxNumFrame = Integer.valueOf(mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION)) / 30;
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
        Log.d("SS_TAG", "min: " + min);
        Log.d("SS_TAG", "max: " + max);
        // Then load the correct images
        for (int i = 0; i < fm.maxNumFrame; i++) {
            while(!loadFrameAt(i, FFmpegMediaMetadataRetriever.OPTION_CLOSEST)){
                Log.d("SS_TAG", "try again");
                mmr = new FFmpegMediaMetadataRetriever();
                mmr.setDataSource(ctx, uri);
            }
            fm.loadingProgression = i;
        }
    }


    private int min;
    private int max;
    private FrameManager fm;
    private FFmpegMediaMetadataRetriever mmr;
    private Uri uri;
    private Context ctx;
}