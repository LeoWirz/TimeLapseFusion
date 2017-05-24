package www.epfl.ch.hypergogetaapp;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mask);

        //final ImageView image = (ImageView) findViewById(R.id.imageToCrop);
        final ImageRectView view = (ImageRectView) findViewById(R.id.dragRectView);
        final TextView text = (TextView) findViewById(R.id.textView4);

        Bitmap bmp = (Bitmap) getIntent().getParcelableExtra("imagebitmap");
        view.setImageBitmap(bmp);

        if (null != view) {
            view.setOnUpCallback(new ImageRectView.OnUpCallback() {
                @Override
                public void onRectFinished(final Rect rect) {

                    double ratio = (double)view.getRight() / (double)view.getDrawable().getIntrinsicWidth();

                    Toast.makeText(getApplicationContext(), "Rect is (" + (int)(rect.left/ratio)  + ", " + (int)(rect.top/ratio) + ", " + (int)(rect.right/ratio) + ", " + (int)(rect.bottom/ratio) + ")",
                            Toast.LENGTH_LONG).show();


                }
            });
        }



    }
}
