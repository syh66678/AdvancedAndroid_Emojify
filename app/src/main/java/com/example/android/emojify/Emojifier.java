package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;


/**
 * Created by Administrator on 2017/9/15.
 */

public class Emojifier {

    private static String TAG="faces",LOG_TAG="faces";

    private static final float EMOJI_SCALE_FACTOR=.9f;
    private static final double SMILING_PROB_THRESHOLD=.15;
    private static final double EYE_OPEN_PROB_THRESHOLD=.5;


    public static Bitmap detectFaces(Context context, Bitmap picture){
        FaceDetector detector=new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        Frame frame=new Frame.Builder().setBitmap(picture).build();

        SparseArray<Face> faces=detector.detect(frame);

        Log.d(LOG_TAG, "detectFaces: number of faces ="+faces.size());

        Bitmap resultBitmap=picture;

        if (faces.size() == 0){
            Toast.makeText(context,"No Faces Detected",Toast.LENGTH_SHORT).show();
        }else {
            for (int i = 0; i < faces.size(); i++) {
                Face face=faces.valueAt(i);
                int icons=whichEmoji(face);
                Bitmap emojiBitmap=BitmapFactory.decodeResource(context.getResources(),icons );

                resultBitmap = addBitmapToFace(resultBitmap,emojiBitmap,face);
                //getClassifications(face);
            }
        }

        detector.release();

        return resultBitmap;

    }

    public static int whichEmoji(Face face){
        Log.d(LOG_TAG,"getClassifications:smilingProb = "+ face.getIsSmilingProbability());
        Log.d(TAG, "getClassifications: leftEyeOpenProb = "
                +face.getIsLeftEyeOpenProbability());
        Log.d(TAG, "getClassifications: rightEyeOpenProbability "
                +face.getIsRightEyeOpenProbability() );

        boolean smiling=face.getIsSmilingProbability() > SMILING_PROB_THRESHOLD ;
        boolean leftEye=face.getIsLeftEyeOpenProbability() > EYE_OPEN_PROB_THRESHOLD;
        boolean rightEye=face.getIsRightEyeOpenProbability()>EYE_OPEN_PROB_THRESHOLD;
        int emoji;
        if (smiling){
            if (leftEye && !rightEye){
                emoji=R.drawable.leftwink;
            }else if (rightEye && !leftEye){
                emoji =R.drawable.rightwink;
            }else if (!leftEye){
                emoji=R.drawable.closed_smile;
            }else {
                emoji=R.drawable.smile;
            }
        }else {
            if (leftEye && !rightEye){
                emoji=R.drawable.leftwinkfrown;
            }else if (rightEye && !leftEye){
                emoji =R.drawable.rightwinkfrown;
            }else if (!leftEye){
                emoji=R.drawable.closed_frown;
            }else {
                emoji=R.drawable.frown;
            }
        }

        return emoji;
    }

    /**
     * 将原始照片与表情符号位图相结合
     *
     * @param backgroundBitmap 原始照片
     * @param emojiBitmap      所选表情符号
     * @param face             检测到的面孔
     * @return 最终位图，包括面孔表情符号
     */
    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {

        // 将结果位图初始化为原始图片的可变副本
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());

        // 调整表情符号，使其在面孔上的效果看起来更好
        float scaleFactor = EMOJI_SCALE_FACTOR;

        // 根据面孔宽度判断表情符号的大小，并保持宽高比
        int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
        int newEmojiHeight = (int) (emojiBitmap.getHeight() *
                newEmojiWidth / emojiBitmap.getWidth() * scaleFactor);


        // 调整表情符号
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);

        // 判断表情符号的位置，以便与面孔保持一致
        float emojiPositionX =
                (face.getPosition().x + face.getWidth() / 2) - emojiBitmap.getWidth() / 2;
        float emojiPositionY =
                (face.getPosition().y + face.getHeight() / 2) - emojiBitmap.getHeight() / 3;

        // 创建画布并在上面绘制位图
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);

        return resultBitmap;
    }


}
