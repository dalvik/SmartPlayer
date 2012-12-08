package com.sky.drovik.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.sky.drovik.player.R;
public class MaskedTextView extends View {
	private Paint txtPaint;
	/** 阴影渲染在X轴上的位移。 */
	private float moveX = 0;
	private boolean start = false;
	/** 每次渲染的位移像素值。 */
	private static final int MOVE_SPEEND = 1;
	private void init() {
		txtPaint = new Paint();
		txtPaint.setColor(Color.GRAY);
		txtPaint.setAntiAlias(true);
		txtPaint.setTextSize(26);
		// 创建一个线性梯度着色器
		// 参数中colors的数组长度与positions的数组长度相对应<br/>
		// 本例中初始化后相应的纯色位置如下(与平铺模式有关)：<br/>
		// Black: 200 * 0<br/>
		// YELLOW: 200 * 0.3<br/>
		// DKGRAY: 200 * 0.6<br/>
		// WHITE: 200 * 1<br/>
		// 最后一个参数为：着色器的颜色平铺模式。共有三种选择：<br/>
		// 1.MIRROR: 颜色平铺从0→colors[i]→0，边缘过渡。
		// 2.REPEAT: 颜色平铺从0→colors[i]，边缘无过渡。
		// 3.CLAMP:与边框着色有关。
		Shader shader = new LinearGradient(0, 0, 200, 0, new int[] {
				Color.BLACK, Color.YELLOW, Color.DKGRAY, Color.WHITE },
				new float[] { 0, 0.3f, 0.6f, 1 }, Shader.TileMode.MIRROR);
		txtPaint.setShader(shader);
	}
	/** 由xml实例化的UI组件在VM中是调用本构造函数生成新实例的。 */
	public MaskedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public void setStart(boolean start) {
		this.start = start;
		// 由UI主线程调用，开始渲染本UI组件。
		invalidate();
	}
	protected void onDraw(Canvas canvas) {
		moveX += MOVE_SPEEND;
		Matrix matrix = new Matrix();
		if (start) {
			// 阴影渲染沿x轴向右移动dx像素,沿y轴向下移动0像素。
			// 渲染的位移形成动画。
			matrix.setTranslate(moveX, 0);
			invalidate();
		} else {
			matrix.setTranslate(0, 0);
		}
		txtPaint.getShader().setLocalMatrix(matrix);
		canvas.drawText(getContext().getString(R.string.welcome_top_tips_str), 0, 25, txtPaint);
	}
}