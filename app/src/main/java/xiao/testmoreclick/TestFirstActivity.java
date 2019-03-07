package xiao.testmoreclick;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class TestFirstActivity extends AppCompatActivity {
    private String TAG = "TestFirstActivity";

    private ImageView ivTest;
    private Bitmap bitmap;
    private int screenWidth, screenHeight;//屏幕宽高
    private float distance;
    private float preDistance;
    private int mode;
    private PointF mid = new PointF();//两指中点
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_first_act);
        initUI();
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_test);

        ivTest.setImageMatrix(matrix);

        matrix.setScale(0.5f, 0.5f); //显示先缩小一些
        center();//缩小后居中

        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bitmap != null) {
            bitmap.recycle();
        }
    }

    private void initUI() {
        ivTest = findViewById(R.id.iv_test);

        ivTest.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView view= (ImageView) v;
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.e(TAG, "ACTION_DOWN");
                        mode = 1;
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.e(TAG, "ACTION_UP");
                        mode = 0;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.e(TAG, "ACTION_MOVE");
                        //当两指缩放，计算缩放比例
                        if (mode == 2) {
                            distance = getDistance(event);
                            if (distance > 10f) {
                                matrix.set(savedMatrix);
                                float scale = distance / preDistance;
                                matrix.postScale(scale, scale, mid.x, mid.y);//缩放比例和中心点坐标
                            }
                        }

                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        Log.e(TAG, "ACTION_POINTER_DOWN");
                        preDistance = getDistance(event);
                        if (preDistance > 10f) {
                            mid = getMid(event);
                            savedMatrix.set(matrix);
                            mode = 2;
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        Log.e(TAG, "ACTION_POINTER_UP");
                        mode = 0;
                        break;
                }
                view.setImageMatrix(matrix);
                center();  //回弹，令
                return true;
            }
        });

    }

    private PointF getMid(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }

    private float getDistance(MotionEvent event) {
        float x = event.getX(1) - event.getX(0);
        float y = event.getY(1) - event.getY(0);
        Log.e(TAG, "x1 = " + event.getX(1) + ",y1=" + event.getY(1));
        Log.e(TAG, "x0 = " + event.getX(0) + ",y0=" + event.getY(0));
        float distance = (float) Math.sqrt(x * x + y * y);//两点间的距离
        return distance;

    }

    private void center() {
        Matrix m = new Matrix();
        m.set(matrix);
        //绘制图片矩形
        //这样rect.left，rect.right,rect.top,rect.bottom分别就就是当前屏幕离图片的边界的距离
        RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        m.mapRect(rect);
        float height = rect.height();
        float width = rect.width();
        float deltaX = 0, deltaY = 0;
        //屏幕的宽高
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm); //获取屏幕分辨率
        screenWidth = dm.widthPixels;  //屏幕宽度
        screenHeight = dm.heightPixels;  //屏幕高度
        //获取ActionBar的高度
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        //计算Y到中心的距离
        if (height < screenHeight) {
            deltaY = (screenHeight - height) / 2 - rect.top - actionBarHeight;
        } else if (rect.top > 0) {
            deltaY = -rect.top;
        } else if (rect.bottom < screenHeight) {
            deltaY = ivTest.getHeight() - rect.bottom;
        }
        //计算X到中心的距离
        if (width < screenWidth) {
            deltaX = (screenWidth - width) / 2 - rect.left;
        } else if (rect.left > 0) {
            deltaX = -rect.left;
        } else if (rect.right < screenWidth) {
            deltaX = screenWidth - rect.right;
        }
        matrix.postTranslate(deltaX, deltaY);
    }


}
