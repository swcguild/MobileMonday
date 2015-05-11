package com.swcguild.movingimages2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by warde on 3/23/15.
 */
public class AnimatedView extends ImageView {

    // the Handler allows us to send messages to the thread that will run our
    // animation
    private Handler h;
    private final int FRAME_RATE = 30;
    // holds all the images we want to display
    List<MovingImage> images = new ArrayList<>();
    // thread on which our animation will run
    // invalidate is a method on View (ImageView extends View) - it invalidates the
    // entire view in preparation for it being drawn again
    private Runnable r = new Runnable() {
        public void run() {
            invalidate();
        }
    };

    public AnimatedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Create a Handler so we can send messages to our thread
        h = new Handler();

        // Construct the MovingImage objects that we want to show on the screen
        MovingImage img = new MovingImage();
        img.x = 10;
        img.y = 10;
        img.xVelocity = 5;
        img.yVelocity = 5;
        img.image =
                (BitmapDrawable) context.getResources().getDrawable(R.drawable.android);
        images.add(img);

        img = new MovingImage();
        img.x = 20;
        img.y = 25;
        img.xVelocity = 2;
        img.yVelocity = 7;
        img.image =
                (BitmapDrawable) context.getResources().getDrawable(R.drawable.android);
        images.add(img);

    }

    @Override
    public void onDraw(Canvas c) {

        // go through each MovingImage in images and update its position, change direction if
        // needed and redraw to the screen
        for (MovingImage img : images) {
            // Move the image
            img.x += img.xVelocity;
            img.y += img.yVelocity;

            // Detect collisions with the edge of the screen - reverse direction if needed
            if ((img.x > this.getWidth() - img.image.getBitmap().getWidth()) || (img.x < 0)) {
                img.xVelocity *= -1;
            }

            if ((img.y > this.getHeight() - img.image.getBitmap().getHeight()) || (img.y < 0)) {
                img.yVelocity *= -1;
            }

            // Draw this image
            c.drawBitmap(img.image.getBitmap(), img.x, img.y, null);
            // place our thread on the queue to be run after a delay
            h.postDelayed(r, FRAME_RATE);
        }
    }

}
