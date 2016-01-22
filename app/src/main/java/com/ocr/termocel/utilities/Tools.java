package com.ocr.termocel.utilities;

import android.content.res.Resources;

/**
 * Created by carlos on 1/21/16.
 */
public class Tools {

    /**
     * Converts dp size into pixels.
     *
     * @param dp dp size to get converted
     * @return Pixel size
     */
    public static float fromDpToPx(float dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

}
