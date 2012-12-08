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
	/** ��Ӱ��Ⱦ��X���ϵ�λ�ơ� */
	private float moveX = 0;
	private boolean start = false;
	/** ÿ����Ⱦ��λ������ֵ�� */
	private static final int MOVE_SPEEND = 1;
	private void init() {
		txtPaint = new Paint();
		txtPaint.setColor(Color.GRAY);
		txtPaint.setAntiAlias(true);
		txtPaint.setTextSize(26);
		// ����һ�������ݶ���ɫ��
		// ������colors�����鳤����positions�����鳤�����Ӧ<br/>
		// �����г�ʼ������Ӧ�Ĵ�ɫλ������(��ƽ��ģʽ�й�)��<br/>
		// Black: 200 * 0<br/>
		// YELLOW: 200 * 0.3<br/>
		// DKGRAY: 200 * 0.6<br/>
		// WHITE: 200 * 1<br/>
		// ���һ������Ϊ����ɫ������ɫƽ��ģʽ����������ѡ��<br/>
		// 1.MIRROR: ��ɫƽ�̴�0��colors[i]��0����Ե���ɡ�
		// 2.REPEAT: ��ɫƽ�̴�0��colors[i]����Ե�޹��ɡ�
		// 3.CLAMP:��߿���ɫ�йء�
		Shader shader = new LinearGradient(0, 0, 200, 0, new int[] {
				Color.BLACK, Color.YELLOW, Color.DKGRAY, Color.WHITE },
				new float[] { 0, 0.3f, 0.6f, 1 }, Shader.TileMode.MIRROR);
		txtPaint.setShader(shader);
	}
	/** ��xmlʵ������UI�����VM���ǵ��ñ����캯��������ʵ���ġ� */
	public MaskedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public void setStart(boolean start) {
		this.start = start;
		// ��UI���̵߳��ã���ʼ��Ⱦ��UI�����
		invalidate();
	}
	protected void onDraw(Canvas canvas) {
		moveX += MOVE_SPEEND;
		Matrix matrix = new Matrix();
		if (start) {
			// ��Ӱ��Ⱦ��x�������ƶ�dx����,��y�������ƶ�0���ء�
			// ��Ⱦ��λ���γɶ�����
			matrix.setTranslate(moveX, 0);
			invalidate();
		} else {
			matrix.setTranslate(0, 0);
		}
		txtPaint.getShader().setLocalMatrix(matrix);
		canvas.drawText(getContext().getString(R.string.welcome_top_tips_str), 0, 25, txtPaint);
	}
}