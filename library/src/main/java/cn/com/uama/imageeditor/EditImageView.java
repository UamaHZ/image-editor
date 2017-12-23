package cn.com.uama.imageeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.com.uama.imageeditor.path.PathInfo;
import cn.com.uama.imageeditor.path.QuadInfo;

/**
 * Created by liwei on 2017/3/20 17:03
 * Email: liwei@uama.com.cn
 * Description: 用来画圈和添加文字的控件
 */

public class EditImageView extends FrameLayout {

    private static final String TAG = "EditImageView";
    private Bitmap bitmap; // 原始的传入的 bitmap
    private Bitmap editBitmap; // 复制的用来编辑的 bitmap
    private Canvas editCanvas; // 用来编辑 bitmap 的画布

    private float privateScale; // bitmap 相对于 EditImageView 的缩放比例
    private float centerTransX, centerTransY; // bitmap 居中时的 left 和 top
    private int privateWidth, privateHeight; // bitmap 缩放之后的宽度和高度
    private RectF rectF; // 用来把 bitmap 画到 view 居中的位置

    private Paint paint; // 画圈的画笔
    private TextPaint textPaint; // 画字的画笔
    private Path canvasPath; // 正在画圈的画笔，画在 view 的画布上
    private Path currentPath; // 正在画圈的画笔，画在编辑 bitmap 的画布上
    private List<Path> pathStack; // 每一笔的记录，用于撤销
    private PathInfo currentPathInfo;
    private List<PathInfo> pathInfos;

    private boolean isDrawCircleEnabled; // 画圈功能是否打开
    private ViewDragHelper viewDragHelper;

    public EditImageView(Context context) {
        super(context);
        init();
    }

    public EditImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        // 初始化画圈画笔
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED); // 红色
        paint.setStyle(Paint.Style.STROKE); // 描边
        paint.setStrokeJoin(Paint.Join.ROUND); // 圆润
        paint.setStrokeCap(Paint.Cap.ROUND); // 圆润

        // 初始化画字画笔
        textPaint = new TextPaint();
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(Color.WHITE); // 白色

        canvasPath = new Path();
        pathStack = new ArrayList<>();
        pathInfos = new ArrayList<>();

        isDrawCircleEnabled = true;
        viewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child instanceof TextView;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                int topBound = (int) centerTransY;
                int bottomBound = getHeight() - child.getMeasuredHeight() - topBound;
                return Math.min(Math.max(topBound, top), bottomBound);
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                int leftBound = (int) centerTransX;
                int rightBound = getWidth() - child.getMeasuredWidth() - leftBound;
                return Math.min(Math.max(left, leftBound), rightBound);
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return getMeasuredWidth() - child.getMeasuredWidth();
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return getMeasuredHeight() - child.getMeasuredHeight();
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                EditImageInfo.TextInfo textInfo;
                Object tag = changedView.getTag();
                if (tag == null) {
                    textInfo = new EditImageInfo.TextInfo();
                    changedView.setTag(textInfo);
                } else {
                    textInfo = (EditImageInfo.TextInfo) tag;
                }
                textInfo.left = left;
                textInfo.top = top;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev);
    }

    public void setDrawCircleEnabled(boolean enabled) {
        isDrawCircleEnabled = enabled;
    }

    public boolean isDrawCircleEnabled() {
        return isDrawCircleEnabled;
    }

    /**
     * 设置原始 bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        post(new Runnable() {
            @Override
            public void run() {
                setBG();
            }
        });
    }

    /**
     * 初始化一些变量
     */
    private void setBG() {
        // bitmap 宽度
        int bitmapWidth = bitmap.getWidth();
        // bitmap 高度
        int bitmapHeight = bitmap.getHeight();
        // bitmap 和 view 的宽度比
        float wr = (float) bitmapWidth / getWidth();
        // bitmap 和 view 的高度比
        float hr = (float) bitmapHeight / getHeight();
        if (wr > hr) { // bitmap 相对更宽
            privateScale = 1 / wr;
            // bitmap 缩放后宽度为 view 的宽度
            privateWidth = getWidth();
            // bitmap 缩放后高度为原始高度 * 缩放比例
            privateHeight = (int) (bitmapHeight * privateScale);
        } else { // bitmap 相对更高
            privateScale = 1 / hr;
            // bitmap 缩放后宽度为原始宽度 * 缩放比例
            privateWidth = (int) (bitmapWidth * privateScale);
            // bitmap 缩放后高度为 view 的高度
            privateHeight = getHeight();
        }
        // 计算 bitmap 居中时的偏移量
        centerTransX = (getWidth() - privateWidth) / 2f;
        centerTransY = (getHeight() - privateHeight) / 2f;

        // 设置 bitmap 居中的框框
        rectF = new RectF(centerTransX, centerTransY, centerTransX + privateWidth, centerTransY + privateHeight);

        // 设置画圈画笔的宽度为 10px，因为有缩放，所以要考虑缩放比例
        paint.setStrokeWidth(10 / privateScale);
        // 设置文字画笔的大小为 24sp，因为有缩放，所以要考虑缩放比例
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, getResources().getDisplayMetrics()) / privateScale);

        // 初始化编辑 bitmap 的画布
        initCanvas();
        // invalidate the whole view
        invalidate();
    }

    /**
     * 初始化编辑 bitmap 画布
     */
    private void initCanvas() {
        if (editBitmap != null) editBitmap.recycle();
        // 复制原始 bitmap
        editBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
        // 新建画布
        editCanvas = new Canvas(editBitmap);
        drawPathStack();
    }

    private float touchX;
    private float touchY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // capturedView 不为 null 表示当前是要拖动文字
        if (viewDragHelper.getCapturedView() != null) {
            viewDragHelper.processTouchEvent(event);
            return true;
        }
        // 判断画圈功能是否打开
        if (isDrawCircleEnabled) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    float touchDownX, lastTouchX;
                    touchDownX = lastTouchX = touchX = event.getX();
                    float touchDownY, lastTouchY;
                    touchDownY = lastTouchY = touchY = event.getY();

                    touchX += 1;
                    touchY += 1;

                    float x = toX(touchDownX);
                    float y = toY(touchDownY);
                    currentPathInfo = new PathInfo();
                    currentPathInfo.startPoint.x = x;
                    currentPathInfo.startPoint.y = y;
                    currentPath = new Path();
                    currentPath.moveTo(x, y);
                    canvasPath.moveTo(toX4C(touchDownX), toY4C(touchDownY));
                    canvasPath.quadTo(
                            toX4C(lastTouchX),
                            toY4C(lastTouchY),
                            toX4C((touchX + lastTouchX) / 2),
                            toY4C((touchY + lastTouchY) / 2)
                    );
                    invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    lastTouchX = touchX;
                    lastTouchY = touchY;
                    touchX = event.getX();
                    touchY = event.getY();

                    float controlX = toX(lastTouchX);
                    float controlY = toY(lastTouchY);
                    float endX = toX((touchX + lastTouchX) / 2);
                    float endY = toY((touchY + lastTouchY) / 2);
                    QuadInfo quadInfo = new QuadInfo();
                    quadInfo.controlPoint.x = controlX;
                    quadInfo.controlPoint.y = controlY;
                    quadInfo.endPoint.x = endX;
                    quadInfo.endPoint.y = endY;
                    currentPathInfo.quadInfos.add(quadInfo);

                    currentPath.quadTo(
                            controlX,
                            controlY,
                            endX,
                            endY
                    );
                    canvasPath.quadTo(
                            toX4C(lastTouchX),
                            toY4C(lastTouchY),
                            toX4C((touchX + lastTouchX) / 2),
                            toY4C((touchY + lastTouchY) / 2)
                    );
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    lastTouchX = touchX;
                    lastTouchY = touchY;
                    touchX = event.getX();
                    touchY = event.getY();

                    controlX = toX(lastTouchX);
                    controlY = toY(lastTouchY);
                    endX = toX((touchX + lastTouchX) / 2);
                    endY = toY((touchY + lastTouchY) / 2);
                    quadInfo = new QuadInfo();
                    quadInfo.controlPoint.x = controlX;
                    quadInfo.controlPoint.y = controlY;
                    quadInfo.endPoint.x = endX;
                    quadInfo.endPoint.y = endY;
                    currentPathInfo.quadInfos.add(quadInfo);
                    pathInfos.add(currentPathInfo);

                    currentPath.quadTo(
                            controlX,
                            controlY,
                            endX,
                            endY
                    );
                    pathStack.add(currentPath);
                    drawPath(currentPath);
                    canvasPath.reset();
                    invalidate();
                    return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 将触摸的 x 坐标转换为 editCanvas 上的 x 坐标
     */
    private float toX(float touchX) {
        return (touchX - centerTransX) / privateScale;
    }

    /**
     * 将触摸的 y 坐标转换为 editCanvas 上的 y 坐标
     */
    private float toY(float touchY) {
        return (touchY - centerTransY) / privateScale;
    }

    /**
     * 将触摸的 x 坐标转换为 view 的 canvas 上的 x 坐标
     */
    private float toX4C(float x) {
        return x / privateScale;
    }

    /**
     * 将触摸的 y 坐标转换为 view 的 canvas 上的 y 坐标
     */
    private float toY4C(float y) {
        return y / privateScale;
    }

    /**
     * 把所有路径画到画布上
     */
    private void drawPathStack() {
        if (editCanvas == null) return;
        for (Path path : pathStack) {
            editCanvas.drawPath(path, paint);
        }
    }

    /**
     * 画单条路径
     */
    private void drawPath(Path path) {
        if (editCanvas != null) {
            editCanvas.drawPath(path, paint);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            int width = childView.getMeasuredWidth();
            int height = childView.getMeasuredHeight();
            int l, t;
            Object tag = childView.getTag();
            // 如果有对应的位置信息就设置，没有就放到中间位置
            if (tag != null && tag instanceof EditImageInfo.TextInfo) {
                EditImageInfo.TextInfo textInfo = (EditImageInfo.TextInfo) tag;
                l = (int) Math.min(textInfo.left, getMeasuredWidth() - width - centerTransX);
                t = (int) Math.min(textInfo.top, getMeasuredHeight() - height - centerTransY);
            } else {
                l = (getMeasuredWidth() - width) / 2;
                t = (getMeasuredHeight() - height) / 2;
            }
            childView.layout(l, t, l + width, t + height);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (editBitmap == null) return;
        if (bitmap.isRecycled() || editBitmap.isRecycled()) return;

        canvas.save();
        canvas.scale(privateScale, privateScale);
        // 画图片
        canvas.drawBitmap(editBitmap, centerTransX / privateScale, centerTransY / privateScale, paint);
        // 画当前的圈
        canvas.drawPath(canvasPath, paint);
        canvas.restore();
    }

    /**
     * 画文字
     */
    private void drawTexts() {
        for (int i = 0; i < getChildCount(); i++) {
            TextView textView = (TextView) getChildAt(i);
            String text = textView.getText().toString();
            float relativeLeftMargin = textView.getLeft() - centerTransX;
            float relativeTopMargin = textView.getTop() - centerTransY;
            StaticLayout staticLayout = new StaticLayout(text, textPaint, (int) ((privateWidth - relativeLeftMargin) / privateScale), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            editCanvas.save();
            editCanvas.translate(relativeLeftMargin / privateScale, relativeTopMargin / privateScale);
            staticLayout.draw(editCanvas);
            editCanvas.restore();
        }
    }

    public Bitmap getBitmap() {
        // 判断是否编辑过，没有编辑过返回 null
        int childCount = getChildCount();
        // pathStack 为空表示没有画圈，childCount 为 0 表示没有文字
        if (pathStack.size() == 0 && childCount == 0) return null;
        drawTexts();
        return editBitmap;
    }

    /**
     * 撤销
     *
     * @return 撤销成功返回 true，不能撤销返回 false
     */
    public boolean undo() {
        if (pathStack.size() > 0) {
            pathStack.remove(pathStack.size() - 1);
            pathInfos.remove(pathInfos.size() - 1);
            initCanvas();
            drawPathStack();
            invalidate();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 增加文字
     */
    public TextView addText(String text) {
        if (TextUtils.isEmpty(text)) return null;
        return createTextView(text);
    }

    /**
     * 创建一个 TextView 用于展示增加的文字
     */
    private TextView createTextView(String text) {
        final TextView textView = new TextView(getContext());
        textView.setTextSize(24);
        textView.setTextColor(Color.WHITE);
        textView.setText(text);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 把文字的高度和宽度限制在图片范围之内
        lp.leftMargin = lp.rightMargin = (int) centerTransX;
        lp.topMargin = lp.bottomMargin = (int) centerTransY;
        addView(textView, lp);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textClickListener != null) {
                    textClickListener.onClick((TextView) v);
                }
            }
        });
        return textView;
    }

    /**
     * 设置图片上的文字信息
     */
    public void setTextInfos(List<EditImageInfo.TextInfo> textInfos) {
        if (textInfos == null) return;
        removeAllViews();
        for (EditImageInfo.TextInfo textInfo : textInfos) {
            TextView textView = addText(textInfo.text);
            if (textView != null) {
                textView.setTag(textInfo);
            }
        }
        requestLayout();
    }

    /**
     * 获取图片上的文字信息
     */
    public List<EditImageInfo.TextInfo> getTextInfos() {
        int childCount = getChildCount();
        List<EditImageInfo.TextInfo> textInfos = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                EditImageInfo.TextInfo textInfo = new EditImageInfo.TextInfo();
                textInfo.text = textView.getText().toString();
                textInfo.left = textView.getLeft();
                textInfo.top = textView.getTop();
                textInfos.add(textInfo);
            }
        }
        return textInfos;
    }

    /**
     * 文字点击监听接口
     */
    public interface OnTextClickListener {
        void onClick(TextView textView);
    }

    private OnTextClickListener textClickListener;

    /**
     * 设置文字点击监听
     */
    public void setTextClickListener(OnTextClickListener listener) {
        textClickListener = listener;
    }

    /**
     * 设置涂鸦笔画信息
     */
    public void setPathInfos(List<PathInfo> pathInfos) {
        if (pathInfos == null) return;
        pathStack.clear();
        this.pathInfos.clear();
        this.pathInfos.addAll(pathInfos);
        // 将用于保存的（Parcelable）涂鸦信息转化为真正的画笔数据
        for (PathInfo pathInfo : pathInfos) {
            Path path = new Path();

            PointF startPoint = pathInfo.startPoint;
            path.moveTo(startPoint.x, startPoint.y);
            for (QuadInfo quadInfo : pathInfo.quadInfos) {
                PointF controlPoint = quadInfo.controlPoint;
                PointF endPoint = quadInfo.endPoint;
                path.quadTo(controlPoint.x, controlPoint.y,
                        endPoint.x, endPoint.y);
            }

            pathStack.add(path);
        }
    }

    /**
     * 获取涂鸦笔画信息
     */
    public List<PathInfo> getPathInfos() {
        return pathInfos;
    }
}
