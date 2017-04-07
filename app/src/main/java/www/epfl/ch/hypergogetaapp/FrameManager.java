package www.epfl.ch.hypergogetaapp;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;
import java.util.HashMap;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST;

/**
 * Created by SebastienSpeierer on 4/7/2017.
 */

// This class internally manage the loading and buffer of frames
public class FrameManager {

    public FrameManager(VideoRenderer vr, MediaMetadataRetriever mmr,  ImageView imgViewFirstFrame, ImageView imgViewLastFrame) {
        this.vr = vr;
        this.mmr = mmr;

        this.imgViewFirstFrame = imgViewFirstFrame;
        this.imgViewLastFrame = imgViewLastFrame;

        firstFrame = 0;
        windowSize = 10;

        // TODO allocate some buffers
        cache = new HashMap<Integer, Bitmap>();

        // init imgViews
        changeFirstFrame(0);

    }

    private Bitmap getFrameAt(int frameNumber) {
        Bitmap frame;
        if (cache.containsKey(frameNumber)) {
            frame = cache.get(frameNumber);
        } else {
            frame = mmr.getFrameAtTime(firstFrame * (long)1000000.f / (long)25, OPTION_CLOSEST);
            cache.put(frameNumber, frame);
        }
        return frame;
    }

    public void changeFirstFrame(int newFirstFrame) {
        // Read the frame number in the text field
        firstFrame = newFirstFrame;

        // Retrieve the frame from the video (time in micro seconds)
        Bitmap bmpFirstFrame = getFrameAt(firstFrame);
        Bitmap bmpLastFrame  = getFrameAt(firstFrame + windowSize);

        // Display the frame
        imgViewFirstFrame.setImageBitmap(bmpFirstFrame);
        imgViewLastFrame.setImageBitmap(bmpLastFrame);

        // update content on VideoRenderer
        vr.clear();

        for (int i = 0; i < windowSize; i++) {
        //    Bitmap frame = getFrameAt(firstFrame + i);
        //    vr.addFrame(frame);
        }
    }

    public void changeWindowSize(int newWindowSize) {
        windowSize = newWindowSize;

        // Display the frame
        //Bitmap bmpLastFrame = getFrameAt(firstFrame + windowSize);
        //imgViewLastFrame.setImageBitmap(bmpLastFrame);

        // Retrieve the frame from the video (time in micro seconds)
        Bitmap bmpFirstFrame = getFrameAt(firstFrame);
        Bitmap bmpLastFrame  = getFrameAt(firstFrame + windowSize);

        // Display the frame
        imgViewFirstFrame.setImageBitmap(bmpFirstFrame);
        imgViewLastFrame.setImageBitmap(bmpLastFrame);

        // update content on VideoRenderer
        vr.clear();

        for (int i = 0; i < windowSize; i++) {
        //    Bitmap frame = getFrameAt(firstFrame + i);
        //    vr.addFrame(frame);
        }

        vr.setWindowSize(newWindowSize);
    }


    HashMap<Integer, Bitmap> cache;

    private int firstFrame;
    private int windowSize;

    // reference to the video renderer
    private VideoRenderer vr;

    // Retriever with the current video bound to
    private MediaMetadataRetriever mmr;

    private ImageView imgViewFirstFrame;
    private ImageView imgViewLastFrame;
}
