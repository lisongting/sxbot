package com.droid.sxbot.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.util.Base64;
import android.util.Size;

import com.droid.sxbot.entity.FaceData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * Created by lisongting on 2017/5/10.
 */
public class ImageUtils {

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    //Rotate Bitmap
    public final static Bitmap rotate(Bitmap b, float degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2,
                    (float) b.getHeight() / 2);

            Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
                    b.getHeight(), m, true);
            if (b != b2) {
                b.recycle();
                b = b2;
            }

        }
        return b;
    }


    public static Bitmap getBitmap(String filePath, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = ImageUtils.calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        if (bitmap != null) {
            try {
                ExifInterface ei = new ExifInterface(filePath);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        bitmap = ImageUtils.rotate(bitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        bitmap = ImageUtils.rotate(bitmap, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        bitmap = ImageUtils.rotate(bitmap, 270);
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }


    public static Bitmap cropBitmap(Bitmap bitmap, Rect rect) {
        int w = rect.right - rect.left;
        int h = rect.bottom - rect.top;
        Bitmap ret = Bitmap.createBitmap(w, h, bitmap.getConfig());
        Canvas canvas = new Canvas(ret);
        canvas.drawBitmap(bitmap, -rect.left, -rect.top, null);
        bitmap.recycle();
        return ret;
    }


    /**
     * 将Bitmap转为Base64编码
     * @param image 图片
     * @param compressFormat  图片格式：JPEG,PNG等
     * @param quality 0-100，100表示最好
     * @return  图片的base64字符串
     */
    public static String encodeBitmapToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }


    //将base64 String解码为Bitmap
    public static Bitmap decodeBase64ToBitmap(String base64Str, int inSampleSize, Size expectSize){
        byte[] bitmapBytes = Base64.decode(base64Str, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inSampleSize = inSampleSize;

        Bitmap origin = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length,options);

        int width = expectSize.getWidth();
        int height = expectSize.getHeight();
        return Bitmap.createScaledBitmap(origin,width,height,true);
    }

    public static Bitmap decodeBase64ToBitmap(String base64Str){
        byte[] bitmapBytes = Base64.decode(base64Str, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length, options);

        //获取到来自服务器的原始图像的宽高
        int width = options.outWidth;
        int height = options.outHeight;
//        options.inSampleSize = calculateInSampleSize(options,width/3,height/3);
        options.inSampleSize = 2;
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length,options);
    }

    public static Bitmap cropFace(FaceData face, Bitmap bitmap) {
        Bitmap bmp;

        float eyesDis = face.eyesDistance();
        PointF mid ;
        mid = face.getMidEye();

        Rect rect = new Rect(
                (int) (mid.x - eyesDis * 1.20f),
                (int) (mid.y - eyesDis * 0.55f),
                (int) (mid.x + eyesDis * 1.20f),
                (int) (mid.y + eyesDis * 1.85f));

        Bitmap.Config config = Bitmap.Config.RGB_565;
        if (bitmap.getConfig() != null) config = bitmap.getConfig();
        bmp = bitmap.copy(config, true);


        bmp = ImageUtils.cropBitmap(bmp, rect);
        return bmp;
    }

    public static RectF getPreviewFaceRectF(PointF pointF, float eyeDistance) {
        return new RectF(
                (int) (pointF.x - eyeDistance * 1.20f),
                (int) (pointF.y - eyeDistance * 1.7f),
                (int) (pointF.x + eyeDistance * 1.20f),
                (int) (pointF.y + eyeDistance * 1.9f));
    }

    //返回一个区域，用于检测人脸是否移动过大
    public static RectF getCheckFaceRectF(PointF pointF, float eyeDistance) {
        return new RectF(
                (pointF.x - eyeDistance * 1.5f),
                (pointF.y - eyeDistance * 1.9f),
                (pointF.x + eyeDistance * 1.5f),
                (pointF.y + eyeDistance * 2.2f));
    }

    public static RectF getDrawFaceRectF(PointF mid,float eyesDis,float scaleX,float scaleY) {
        return new RectF(
                (mid.x - eyesDis * 1.1f) * scaleX,
                (mid.y - eyesDis * 1.3f) * scaleY,
                (mid.x + eyesDis * 1.1f) * scaleX,
                (mid.y + eyesDis * 1.7f) * scaleY);

    }
}
