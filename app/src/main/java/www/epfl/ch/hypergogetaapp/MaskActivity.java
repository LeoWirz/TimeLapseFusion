package www.epfl.ch.hypergogetaapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

        Intent intent = getIntent();

        //Bitmap bmp = (Bitmap) intent.getParcelableExtra("imagebitmap");
        view.setImageBitmap(image);

        double borders[] = intent.getDoubleArrayExtra("borders");
        view.setBorders(borders);

        //borders = view.getNormalizedBorders();
        //Toast.makeText(getApplicationContext(), String.valueOf(borders[0]) + " " + String.valueOf(borders[1]) + " " +String.valueOf(borders[2]) + " " +String.valueOf(borders[3]), Toast.LENGTH_SHORT).show();

        //button to set the area
        final Button setButton = (Button) findViewById(R.id.set_button);

        setButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("borders", view.getNormalizedBorders(view.getWidth(), view.getHeight()));
                //returnIntent.putIntegerArrayListExtra("borders", view.getNormalizedBorders());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        //button to reset to entire image
        final Button resetButton = (Button) findViewById(R.id.reset_button);

        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.setBorders(new double[]{0, 0, 1, 1});
                view.invalidate();
                Toast.makeText(getApplicationContext(), String.valueOf(view.getWidth()), Toast.LENGTH_LONG).show();
            }
        });

    }

    public static Bitmap image;

}
