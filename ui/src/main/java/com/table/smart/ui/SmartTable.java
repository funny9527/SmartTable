package com.table.smart.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tical on 2018/6/9.
 */

public class SmartTable extends View implements View.OnClickListener {
    final static String TAG = "view";

    private int columnSize = 5;
    private volatile boolean calculate = false;

    //table datas
    private volatile List<List<String>> columnValues = new ArrayList<>();

    //cell width
    private volatile List<List<Integer>> cellValuesList = new ArrayList<>();

    //row max height
    private volatile List<Integer> heightList = new ArrayList<>();

    //title width
    private volatile List<Integer> titleList = new ArrayList<>();

    //title max height
    private volatile int titleHeight = 0;

    private Map<Integer, Integer> pageSizeList = new HashMap<>();

    //table titles
    private String[] titles;

    //cell position
    private int[] cellLeft;

    private Paint paint = new Paint();

    private int slop = ViewConfiguration.getTouchSlop();
    private float downX;
    private float lastX;
    private float downY;

    private int xpos = -1;
    private int ypos = -2;

    //view width
    private int screenWidth;

    //view height
    private int screenHeight;

    private float[] cellWidthList;
    private List<Integer> cellHeightList = new ArrayList<>();

    //cell padding
    private int padding = 10;

    //cell height
    private int lineHeight = 80;

    private int textColor;
    private int titleColor;
    private int lineColor;
    private int copyColor;
    private int textSize = 14;
    private int maxWidth = 800;
    private int border = 1;
    private boolean showCopy = true;

    //page index
    private int pageIndex = 0;
    private int lastIndex = -1;

    private String LOADING_TEXT = "loading...";
    private float loadWidth;
    private ClipboardManager clipboardManager;

    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;

    private Scroller scroller;

    public SmartTable(Context context) {
        super(context);

        init();
    }

    public SmartTable(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public SmartTable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        screenHeight = MeasureSpec.getSize(heightSpec);
        if (cellLeft == null) {
            if (screenWidth <= 0) {
                screenWidth = MeasureSpec.getSize(widthSpec);
            }
            setMeasuredDimension(screenWidth, screenHeight);
        } else {
            setMeasuredDimension(cellLeft[cellLeft.length - 1], screenHeight);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SmartTable(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private float[] getMeasured(Paint paint, List<List<String>> values) {
        float[] widthList = new float[columnSize];

        for (int i = 0; i < values.size(); i++) {
            List<String> list = values.get(i);
            List<Integer> vlist = new ArrayList<>();

            float max = 0;
            for (int j = 0; j < list.size(); j++) {
                float width = paint.measureText(list.get(j));
                vlist.add((int) Math.ceil(width / maxWidth));

                if (widthList[j] < width) {
                    widthList[j] = width;
                }

                if (width > max) {
                    max = width;
                }

                if (widthList[j] > maxWidth) {
                    widthList[j] = maxWidth;
                }
            }

            cellValuesList.add(vlist);
            heightList.add((int) Math.ceil(max / maxWidth));
        }

        if (hasTitle()) {
            float max_title_w = 0;
            for (int i = 0; i < titles.length; i++) {
                float width = paint.measureText(titles[i]);
                titleList.add((int) Math.ceil(width / maxWidth));
                if (width > widthList[i]) {
                    widthList[i] = width;
                }

                if (width > max_title_w) {
                    max_title_w = width;
                }

                if (widthList[i] > maxWidth) {
                    widthList[i] = maxWidth;
                }
            }
            titleHeight = (int) Math.ceil(max_title_w / maxWidth);
        }

        cellWidthList = widthList;
        return widthList;
    }

    private void init() {
        paint.setColor(Color.RED);
        paint.setStrokeWidth(border);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);

        scroller = new Scroller(getContext(), new SmoothInterpolator());

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        this.setOnClickListener(this);
    }

    @Override
    public final void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setTextSize(textSize);

        if (columnValues.size() > 0 && lastIndex < 0) {
            if (loadWidth <= 0) {
                loadWidth = paint.measureText(LOADING_TEXT);
                canvas.drawText(LOADING_TEXT, 0, LOADING_TEXT.length(),
                        screenWidth / 2 - loadWidth / 2, screenHeight / 2, paint);
            }
            run();
        } else {
            canvas.clipRect(getScrollX(), 0, screenWidth + getScrollX(), screenHeight);

            if (border > 0) {
                canvas.drawLine(0, 0, cellLeft[cellLeft.length - 1], 0, paint);
            }

            cellHeightList.clear();

            int end = 0;
            if (hasTitle()) {
                end = drawTitle(canvas, Arrays.asList(titles), titleColor);
                cellHeightList.add(end);
            }

            int i = lastIndex;
            while (end <= screenHeight && i < columnValues.size()) {
                end = drawCell(canvas, end, i, textColor);
                if (end <= 0) {
                    break;
                } else {
                    i++;
                    cellHeightList.add(end);
                }
            }

            if (i > lastIndex) {
                pageSizeList.put(pageIndex, i - lastIndex);
            }
        }
    }

    private int drawCell(Canvas canvas, int startY, int index, int color) {
        int total = heightList.get(index);
        int end = startY + (lineHeight + 2 * padding) * total;

        paint.setColor(lineColor);
        if (end <= screenHeight) {

            if (border > 0) {
                canvas.drawLine(0, end,
                        cellLeft[cellLeft.length - 1], end, paint);
                for (int i = 0; i < cellLeft.length; i++) {
                    canvas.drawLine(cellLeft[i], startY,
                            cellLeft[i], end, paint);
                }
            }

            List<String> values = columnValues.get(index);
            for (int column = 0; column < values.size(); column++) {
                String text = values.get(column);

                if (showCopy && xpos == column && ypos == index - lastIndex) {
                    clipboardManager.setText(text);
                    paint.setColor(copyColor);
                    canvas.drawRect(cellLeft[column], startY,
                            cellLeft[column] + 2 * padding + cellWidthList[column], end, paint);
                }

                int size = cellValuesList.get(index).get(column);
                paint.setColor(color);
                for (int j = 0; j < size; j++) {
                    int len = text.length() / size;
                    canvas.drawText(text, len * j, len * (j + 1),
                            cellLeft[column] + padding, startY + lineHeight * (j + 1), paint);
                }
            }

            return end;
        } else {
            return 0;
        }
    }

    private int drawTitle(Canvas canvas, List<String> titles, int color) {
        int total = titleHeight;
        int end = (lineHeight + 2 * padding) * total;

        paint.setColor(lineColor);
        if (end <= screenHeight) {
            if (border > 0) {
                canvas.drawLine(0, end,
                        cellLeft[cellLeft.length - 1], end, paint);
                for (int i = 0; i < cellLeft.length; i++) {
                    canvas.drawLine(cellLeft[i], 0,
                            cellLeft[i], end, paint);
                }
            }

            for (int column = 0; column < titles.size(); column++) {
                String text = titles.get(column);

                if (showCopy && xpos == column && ypos == -1) {
                    clipboardManager.setText(text);
                    paint.setColor(copyColor);
                    canvas.drawRect(cellLeft[column], 0,
                            cellLeft[column] + 2 * padding + cellWidthList[column], end, paint);
                }

                paint.setColor(color);
                int size = titleList.get(column);
                for (int j = 0; j < size; j++) {
                    int len = text.length() / size;
                    canvas.drawText(text, len * j, len * (j + 1),
                            cellLeft[column] + padding,lineHeight * (j + 1), paint);
                }
            }
        }

        return end;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            int mUnboundedScrollX = scroller.getCurrX();
            scrollTo(mUnboundedScrollX, scroller.getCurrY());
            invalidate();
        }
    }

    @Override
    public final boolean onTouchEvent(MotionEvent event) {
        acquireVelocityTrackerAndAddMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                lastX = downX;
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float curX = event.getX();
                float dx = curX - downX;
                if (Math.abs(dx) >= slop && cellLeft[cellLeft.length - 1] > screenWidth) {
                    scrollBy(-(int) (curX - downX), 0);
                    downX = curX;
                }
                break;
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (cellLeft[cellLeft.length - 1] > screenWidth) {
                    if (getScrollX() < 0) {
                        scroller.startScroll(getScrollX(), 0, -getScrollX(), 0);
                    } else if (getScrollX() > cellLeft[cellLeft.length - 1] - screenWidth) {
                        scroller.startScroll(getScrollX(), 0, cellLeft[cellLeft.length - 1] - screenWidth - getScrollX(), 0);
                    } else {
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                        int velocityX = (int) velocityTracker.getXVelocity();
                        int distance = Math.abs(velocityX) / 10;

                        if (event.getX() - lastX > slop) {
                            if (getScrollX() - distance >= 0) {
                                scroller.startScroll(getScrollX(), 0, -distance, 0);
                            } else {
                                scroller.startScroll(getScrollX(), 0, -getScrollX(), 0);
                            }
                        } else if (event.getX() - lastX < -slop) {
                            if (getScrollX() + distance <= cellLeft[cellLeft.length - 1] - screenWidth) {
                                scroller.startScroll(getScrollX(), 0, distance, 0);
                            } else {
                                scroller.startScroll(getScrollX(), 0, cellLeft[cellLeft.length - 1] - screenWidth - getScrollX(), 0);
                            }
                        }
                    }
                    releaseVelocityTracker();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * goto next page
     */
    public void nextPage() {
        if (columnValues.size() <= 0 || pageSizeList.size() <= 0) {
            return;
        }

        int size = pageSizeList.get(pageIndex);
        int index = lastIndex + size;

        if (index >= columnValues.size()) {
            return;
        }

        lastIndex = index;
        pageIndex++;
        invalidate();
    }

    /**
     * goto previous page
     */
    public void previous() {
        if (pageIndex <= 0) {
            return;
        }

        if (pageIndex == 1) {
            --pageIndex;
            lastIndex = 0;
            invalidate();
        } else {
            int index = lastIndex - pageSizeList.get(pageIndex - 1);
            if (index < 0) {
                return;
            }

            --pageIndex;
            lastIndex = index;
            invalidate();
        }
    }

    /**
     * set datas to show
     * @param datas datas to show
     * @param titles table titles
     */
    public void setData(final List<List<String>> datas, final String[] titles) {
        if (datas == null || datas.size() <= 0) {
            return;
        }

        this.columnValues = datas;
        this.titles = titles;
        this.columnSize = datas.get(0).size();
        requestLayout();
    }

    @SuppressLint("StaticFieldLeak")
    private void run() {
        if (calculate) {
            return;
        }

        calculate = true;
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... strings) {
                cellLeft = new int[columnSize + 1];
                cellLeft[0] = 0;

                float[] list = getMeasured(paint, columnValues);

                int i = 0;
                for (float value : list) {
                    float width = value + 2 * padding;
                    cellLeft[i + 1] = (int) width + cellLeft[i];
                    i++;
                }

//                pageSize = screenHeight / (lineHeight + 2 * padding) - 1;
                lastIndex = 0;
                return "";
            }

            @Override
            public void onPostExecute(String params) {
                requestLayout();
                calculate = false;
            }
        }.execute();
    }

    /**
     * 正文字体颜色
     * @param textColor
     */
    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    /**
     * 标题颜色
     * @param titleColor
     */
    public void setTitleColor(int titleColor) {
        this.titleColor = titleColor;
    }

    public void setMaxCellWidth(int width) {
        this.maxWidth = width;
    }

    public void setCopyColor(int color) {
        copyColor = color;
    }

    /**
     * 边框颜色
     * @param lineColor
     */
    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public void setRowHeight(int height) {
        this.lineHeight = height;
    }

    public void setTextSize(int size) {
        this.textSize = size;
    }

    public void setBorder(int border) {
        this.border = border;
    }

    public void setShowCopy(boolean copy) {
        showCopy = copy;
    }

    @Override
    public void onClick(View v) {
        float offset = downX + getScrollX();
        for (int i = cellLeft.length - 1; i >= 0; i--) {
            if (cellLeft[i] <= offset) {
                xpos = i;
                break;
            }
        }

        for (int j = 0; j < cellHeightList.size(); j++) {
            if (cellHeightList.get(j) > downY) {
                if (hasTitle()) {
                    ypos = j - 1;
                } else {
                    ypos = j;
                }
                break;
            }
        }

        if (xpos >= 0 && ypos >= -1) {
            postInvalidate();
        }
    }

    private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private static class SmoothInterpolator implements Interpolator {
        private float mTension = 1.3f;
        public SmoothInterpolator() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * ((mTension + 1) * t + mTension) + 1.0f;
        }
    }

    private boolean hasTitle() {
        return titles != null && titles.length > 0;
    }
}
