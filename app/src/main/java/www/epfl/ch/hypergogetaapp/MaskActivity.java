package www.epfl.ch.hypergogetaapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mask);

        final ImageRectView view = (ImageRectView) findViewById(R.id.dragRectView);

        //Bitmap bmp = (Bitmap) getIntent().getParcelableExtra("imagebitmap");
        //view.setImageBitmap(bmp);

        ArrayList<Integer> borders = getIntent().getIntegerArrayListExtra("borders");
        view.setBorders(borders.get(0), borders.get(1), borders.get(2), borders.get(3));

        /*
        if (null != view) {
            view.setOnUpCallback(new ImageRectView.OnUpCallback() {
                @Override
                public void onRectFinished(final Rect rect) {

                    double ratio = (double) view.getRight() / (double) view.getDrawable().getIntrinsicWidth();

                    Toast.makeText(getApplicationContext(), "Rect is (" + (int) (rect.left / ratio) + ", " + (int) (rect.top / ratio) + ", " + (int) (rect.right / ratio) + ", " + (int) (rect.bottom / ratio) + ")",
                            Toast.LENGTH_SHORT).show();

                }
            });
        }
        */

        //set the area
        final Button setButton = (Button) findViewById(R.id.set_button);

        setButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putIntegerArrayListExtra("borders", view.getBorders());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        //reset to entire image
        final Button resetButton = (Button) findViewById(R.id.reset_button);

        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.setBorders(0, 0, view.getRight(), view.getBottom());
                view.invalidate();
            }
        });

    }
}
