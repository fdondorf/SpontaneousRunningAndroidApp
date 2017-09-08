package org.spontaneous.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by fdondorf on 03.07.2017.
 */

public class ImageUtil {

    /**
     * Sets the given image to the given image view
     * @param view
     * @param base64EncodedImage
     * @param windowManager
     */
    public static void setImageToView(ImageView view, String base64EncodedImage, WindowManager windowManager) {

        if (base64EncodedImage != null) {
            byte[] image = Base64.decode(base64EncodedImage, Base64.DEFAULT);

            Bitmap bm = BitmapFactory.decodeByteArray(image, 0, image.length);
            DisplayMetrics dm = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(dm);

            view.setMinimumHeight(dm.heightPixels);
            view.setMinimumWidth(dm.widthPixels);
            view.setImageBitmap(bm);
        }
    }
}
