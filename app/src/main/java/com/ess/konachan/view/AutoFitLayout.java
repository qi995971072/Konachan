package com.ess.konachan.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

import com.ess.konachan.R;

/**
 * 以360dp * 640dp屏幕为基准，根据xml里设置的宽度和高度自动进行缩放适配
 * version 1.0
 * 缺点：暂时只支持适配一个子View的尺寸
 * 属性：具体styles配置见最下方
 *       scaleWidth(boolean)：layout_width为定值，且此属性为true时，自动适配View的宽度
 *       scaleHeight(boolean)：layout_height为定值，且此属性为true时，自动适配View的高度
 *       relativeTo(int)：需layout_width与layout_height均为定值，此时设置为相对于某一边，
 *                        则此边相对于屏幕缩放，另一边按照原始宽高比缩放（此时scaleWidth
 *                        与scaleHeight默认生效）
 */

public class AutoFitLayout extends FrameLayout {

    private final static int NONE = 0;  // 两边均以屏幕尺寸为基准进行缩放
    private final static int RELATIVE_TO_WIDTH = 1;  // 以宽度为基准等比缩放高度
    private final static int RELATIVE_TO_HEIGHT = 2; // 以高度为基准等比缩放宽度

    private boolean mScaleWidth;   // 是否缩放宽度，默认为false
    private boolean mScaleHeight;  // 是否缩放高度，默认为false
    private int mRelative;    // 以哪边为基准等比缩放，默认为NONE

    private static float sWidthRatio;  // 屏幕宽度与360dp的比例
    private static float sHeightRatio; // 屏幕高度与640dp的比例

    public AutoFitLayout(Context context) {
        this(context, null);
    }

    public AutoFitLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AutoFitLayout);
        mScaleWidth = typedArray.getBoolean(R.styleable.AutoFitLayout_scaleWidth, false);
        mScaleHeight = typedArray.getBoolean(R.styleable.AutoFitLayout_scaleHeight, false);
        mRelative = typedArray.getInt(R.styleable.AutoFitLayout_relativeTo, NONE);
        typedArray.recycle();

        if (sWidthRatio <= 0) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            sWidthRatio = px2dp(dm, dm.widthPixels) / 360f;
            sHeightRatio = px2dp(dm, dm.heightPixels) / 640f;
        }
    }

    private float px2dp(DisplayMetrics dm, float px) {
        float scale = dm.density;
        return px / scale + 0.5f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // UNSPECIFIED: 父容器不对View有任何限制，要多大给多大，一般用于系统内部，表示一种测量的状态
        // EXACTLY: 100dp match_parent
        // AT_MOST: wrap_content
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // 获取原始宽高值
        int selfWidth = MeasureSpec.getSize(widthMeasureSpec);
        int selfHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 根据不同配置属性进行适配
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY
                && mRelative == RELATIVE_TO_WIDTH) {
            // 宽高定值，宽度按屏幕缩放，高度按比例缩放
            float scale = selfHeight / 1f / selfWidth;
            selfWidth *= sWidthRatio;
            selfHeight = (int) (selfWidth * scale);
        } else if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY
                && mRelative == RELATIVE_TO_HEIGHT) {
            // 宽高定值，高度按屏幕缩放，宽度按比例缩放
            float scale = selfWidth / 1f / selfHeight;
            selfHeight *= sHeightRatio;
            selfWidth = (int) (selfHeight * scale);
        } else {
            if (widthMode == MeasureSpec.EXACTLY && mScaleWidth) {
                // 宽度按屏幕缩放，无视高度
                selfWidth *= sWidthRatio;
            } else {
                // 无缩放，未设置mScaleWidth或layout_width="wrap_content"
                selfWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            }

            if (heightMode == MeasureSpec.EXACTLY && mScaleHeight) {
                // 高度按屏幕缩放，无视宽度
                selfHeight *= sHeightRatio;
            } else {
                // 无缩放，未设置mScaleHeight或layout_height="wrap_content"
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            }
        }

        // 保存缩放结果
        setMeasuredDimension(selfWidth, selfHeight);

        // 计算子View的宽度和高度
        int childWidth = selfWidth - getPaddingLeft() - getPaddingRight();
        int childHeight = selfHeight - getPaddingBottom() - getPaddingTop();

        // 作为父容器,还需要测绘子View
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
        measureChildren(childWidthMeasureSpec, childHeightMeasureSpec);
    }
}

/* <resources>
		<declare-styleable name="AutoFitLayout">
            <attr name="scaleWidth" format="boolean"/>
            <attr name="scaleHeight" format="boolean"/>
            <attr name="relativeTo" format="enum">
                <enum name="none" value="0"/>
                <enum name="width" value="1"/>
                <enum name="height" value="2"/>
            </attr>
        </declare-styleable>
	</resources>
*/
