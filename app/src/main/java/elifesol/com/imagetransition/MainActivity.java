package elifesol.com.imagetransition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    ImageView slidingImage;
//    ImageView slidingImage2;
    Bitmap firstBitmap;
    boolean loadingHigherRes = false;
    String lowResUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/70/Southernmost_point_key_west.jpg/120px-Southernmost_point_key_west.jpg";
    String hiResUrl = "https://upload.wikimedia.org/wikipedia/commons/7/70/Southernmost_point_key_west.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        slidingImage = (ImageView) findViewById(R.id.imageView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initView();
    }

    private void initView() {
        //Load initial image
        new AsyncGettingBitmapFromUrl().execute(lowResUrl);
    }

//    int currentImageIndex = 0;

    final Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(final Animation animation) {
            // nothing to do here
        }

        @Override
        public void onAnimationEnd(final Animation animation) {
            // launch showing of next image on the end of the animation
            slidingImage.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(final Animation animation) {
            // nothing to do here
        }
    };

    private void displayBitmap(Bitmap bitmap) {
        if (loadingHigherRes) {
//            slidingImage2.setImageBitmap(bitmap);
//            final Animation fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
//            fadeOutAnimation.setDuration(1000);
//            fadeOutAnimation.setStartOffset(0);
//            fadeOutAnimation.setAnimationListener(animationListener);
//            slidingImage.startAnimation(fadeOutAnimation);
            applyNewBitmapByPixels(firstBitmap, bitmap);
            slidingImage.setImageBitmap(firstBitmap);
        } else {
            loadingHigherRes = true;
            firstBitmap = bitmap;
            slidingImage.setImageBitmap(firstBitmap);

            new AsyncGettingBitmapFromUrl().execute(hiResUrl);
        }
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            //Bitmap myBitmap = BitmapFactory.decodeStream(input);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
//            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap myBitmap = getResizedBitmap(BitmapFactory.decodeStream(input, null, options), slidingImage.getWidth(), slidingImage.getHeight());
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, newWidth, newHeight, false);
        bm.recycle();
        return resizedBitmap;
    }

    private void applyNewBitmapByPixels(Bitmap current, Bitmap newBitmap) {
        if (current == null || newBitmap == null) {
            Log.e("BITMAPS", "bitmap should not be null");
            return;
        }

        int width = current.getWidth();
        int height = current.getHeight();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int color = newBitmap.getPixel(i, j);
                current.setPixel(i, j, color);
            }
        }
    }

    private class AsyncGettingBitmapFromUrl extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            return getBitmapFromURL(params[0]);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            //Maybe could update the view with a not fully downloaded bitmap?
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            displayBitmap(bitmap);
        }
    }
}
