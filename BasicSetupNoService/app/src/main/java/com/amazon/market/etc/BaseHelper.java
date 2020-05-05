package com.amazon.market.etc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

import com.amazon.market.R;
import com.amazon.market.etc.callback.BottomSheetMediaSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BaseHelper {
    public static boolean isEmpty(String string) {
        if (string == null || string.trim().length() == 0) { return true; }
        return false;
    }
    public static void selectImageSource(Activity activity, final BottomSheetMediaSelectionListener bottomSheetMediaSelectionListener) {
        View modalBottomSheetView = activity.getLayoutInflater().inflate(R.layout.modal_bottomsheet_media_selection, null);

        final BottomSheetDialog bottomSheetModalDialog = new BottomSheetDialog(activity);
        bottomSheetModalDialog.setContentView(modalBottomSheetView);
        bottomSheetModalDialog.setCanceledOnTouchOutside(true);
        bottomSheetModalDialog.setCancelable(true);

        AppCompatButton btnCancel = (AppCompatButton) modalBottomSheetView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetModalDialog.dismiss();
            }
        });

        AppCompatButton btnCamera = (AppCompatButton) modalBottomSheetView.findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetModalDialog.dismiss();
                bottomSheetMediaSelectionListener.onCameraSelection();
            }
        });

        AppCompatButton btnGallery = (AppCompatButton) modalBottomSheetView.findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetModalDialog.dismiss();
                bottomSheetMediaSelectionListener.onMediaStorageSelection();
            }
        });

        bottomSheetModalDialog.show();

    }
    public static String  saveSmallerImage(
            String pathOfInputImage, int dstWidth, int dstHeight) {
        try {
            int inWidth = 0;
            int inHeight = 0;

            InputStream in = new FileInputStream(pathOfInputImage);

            // decode image size (decode metadata only, not the whole image)
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();
            in = null;

            // save width and height
            inWidth = options.outWidth;
            inHeight = options.outHeight;

            // decode full image pre-resized
            in = new FileInputStream(pathOfInputImage);
            options = new BitmapFactory.Options();
            // calc rought re-size (this is no exact resize)
            options.inSampleSize = Math.max(inWidth / dstWidth, inHeight
                    / dstHeight);
            // decode full image
            Bitmap roughBitmap = BitmapFactory.decodeStream(in, null, options);

            // calc exact destination size
            Matrix m = new Matrix();
            RectF inRect = new RectF(0, 0, roughBitmap.getWidth(),
                    roughBitmap.getHeight());
            RectF outRect = new RectF(0, 0, dstWidth, dstHeight);
            m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER);
            float[] values = new float[9];
            m.getValues(values);

            // resize bitmap
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(roughBitmap,
                    (int) (roughBitmap.getWidth() * values[0]),
                    (int) (roughBitmap.getHeight() * values[4]), true);

            // save image
            try {
                try {
                    File file = new File(pathOfInputImage);
                    boolean deleted = file.delete();
                } catch (Exception e) {}

                FileOutputStream out = new FileOutputStream(pathOfInputImage);
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
                return pathOfInputImage;
            }
            catch (Exception e) {
                Log.e("Image", e.getMessage(), e);
            }
        }
        catch (IOException e) {
            Log.e("Image", e.getMessage(), e);
        }
        catch (Exception e) {
            Log.e("Image", e.getMessage());
        }
        return "";

    }


}
