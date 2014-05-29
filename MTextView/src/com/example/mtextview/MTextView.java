package com.example.mtextview;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.TextView;

/**
 * @功能 图文混排TextView，请使用{@link #setMText(CharSequence)}
 * @author huangwei
 * @2014年5月27日
 * @下午5:29:27
 */
public class MTextView extends TextView
{
	private Context context;
	/**
	 * 用于测量字符宽度
	 */
	private Paint paint = new Paint();

	private int textColor = Color.BLACK;

	//行距
	private float lineSpacing;
	private int lineSpacingDP = 2;

//	private float lineSpacingMult = 0.5f;

	/**
	 * 最大宽度
	 */
	private int maxWidth;
	/**
	 * 只有一行时的宽度
	 */
	private int oneLineWidth = -1;
	/**
	 * 已绘的行中最宽的一行的宽度
	 */
	private float lineWidthMax = -1;
    /**
     * 存储当前文本内容,每个item为一个字符或者一个ImageSpan
     */
	private ArrayList<Object> obList = new ArrayList<Object>();
    /**
     * 是否使用默认{@link TextView#onMeasure(int, int)}和{@link TextView#onDraw(Canvas)}
     */
	private boolean useDefault = false;
    /**
     * 存储当前文本内容，每个item为一行
     */
	ArrayList<LINE> contentList = new ArrayList<LINE>();
    /**
     * 缓存测量过的数据
     */
	private static HashMap<String, SoftReference<MeasuredData>> measuredData = new HashMap<String,  SoftReference<MeasuredData>>();
	
    private static int hashIndex = 0;
    
	private CharSequence text = "";
	/**
	 * 最小高度
	 */
	private int minHeight;
	/**
	 * 用以获取屏幕高宽
	 */
	private DisplayMetrics displayMetrics;

	public MTextView(Context context)
	{
		super(context);
		this.context = context;
		paint.setAntiAlias(true);
		lineSpacing = dip2px(context, lineSpacingDP);
		minHeight = dip2px(context, 30);
		
		displayMetrics = new DisplayMetrics();
	}
	public MTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.context = context;
		paint.setAntiAlias(true);
		lineSpacing = dip2px(context, lineSpacingDP);
		minHeight = dip2px(context, 30);
		displayMetrics = new DisplayMetrics();
	}
	@Override
	public void setMaxWidth(int maxpixels)
	{
		super.setMaxWidth(maxpixels);
		maxWidth = maxpixels;
	}

	@Override
	public void setMinHeight(int minHeight)
	{
		super.setMinHeight(minHeight);
		this.minHeight = minHeight;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		if (useDefault)
		{
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		
		int width = 0, height = 0;

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		switch (widthMode)
		{
		case MeasureSpec.EXACTLY:
			width = widthSize;
			break;
		case MeasureSpec.AT_MOST:
			width = widthSize;
			break;
		case MeasureSpec.UNSPECIFIED:
			((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			width = displayMetrics.widthPixels;
			break;
		default:
			break;
		}
		if (maxWidth > 0)
			width = Math.min(width, maxWidth);

		paint.setTextSize(this.getTextSize());
		paint.setColor(textColor);
		int realHeight = measureContentHeight((int) width);
		
		//如果实际行宽少于预定的宽度，减少行宽以使其内容横向居中
		int leftPadding = getCompoundPaddingLeft();
		int rightPadding = getCompoundPaddingRight();
		width = Math.min(width, (int) lineWidthMax + leftPadding+ rightPadding);

		
		if (oneLineWidth > -1)
		{
			width = oneLineWidth;
		}
		switch (heightMode)
		{
		case MeasureSpec.EXACTLY:
			height = heightSize;
			break;
		case MeasureSpec.AT_MOST:
			height = realHeight;
			break;
		case MeasureSpec.UNSPECIFIED:
			height = realHeight;
			break;
		default:
			break;
		}
		
		height += getCompoundPaddingTop() + getCompoundPaddingBottom();
		
		height = Math.max(height,minHeight);

		setMeasuredDimension(width, height);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		if (useDefault)
		{
			super.onDraw(canvas);
			return;
		}

		int width;

		Object ob;

		int leftPadding = getCompoundPaddingLeft();
		int topPadding = getCompoundPaddingTop();

		float height = 0 + topPadding + lineSpacing;
		//只有一行时
		if(oneLineWidth != -1)
		{
			height = getMeasuredHeight() /2 - contentList.get(0).height/2;
		}

		for (int i = 0; i < contentList.size(); i++)
		{
			//绘制一行
			float realDrawedWidth = 0 + leftPadding;
			LINE line = contentList.get(i);
			for (int j = 0; j < line.line.size(); j++)
			{
				ob = line.line.get(j);
				width = line.widthList.get(j);

				if (ob instanceof String)
				{
					canvas.drawText((String) ob, realDrawedWidth, height + line.height, paint);
					realDrawedWidth += width;
				}
				else if (ob instanceof ImageSpan)
				{
					ImageSpan is = (ImageSpan) ob;
					Drawable d = is.getDrawable();

					int left = (int) (realDrawedWidth);
					int top = (int) height;
					int right = (int) (realDrawedWidth + width);
					int bottom = (int) (height + line.height);
					d.setBounds(left, top, right, bottom);
					d.draw(canvas);
					realDrawedWidth += width;
				}

			}
			height += line.height + lineSpacing;
		}

	}

	@Override
	public void setTextColor(int color)
	{
		super.setTextColor(color);
		textColor = color;
	}

	/**
	 * 用于带ImageSpan的文本内容所占高度测量
	 * @param width 预定的宽度
	 * @return 所需的高度
	 */
	private int measureContentHeight(int width)
	{
		int cachedHeight = getCachedData(text.toString(), width);
		
		if(cachedHeight > 0)
		{
			return cachedHeight;
		}

		// 已绘的宽度
		float obWidth = 0;
		float obHeight = 0;

		float textSize = this.getTextSize();
		//行高
		float lineHeight = textSize;
		//计算出的所需高度
		float height = lineSpacing;

		int leftPadding = getCompoundPaddingLeft();
		int rightPadding = getCompoundPaddingRight();

		float drawedWidth = 0;

		width = width - leftPadding - rightPadding;

		oneLineWidth = -1;

		contentList.clear();

		StringBuilder sb;

		LINE line = new LINE();

		for (int i = 0; i < obList.size(); i++)
		{
			Object ob = obList.get(i);

			if (ob instanceof String)
			{

				obWidth = paint.measureText((String) ob);
				obHeight = textSize;
			}
			else if (ob instanceof ImageSpan)
			{
				Rect r = ((ImageSpan) ob).getDrawable().getBounds();
				obWidth = r.right - r.left;
				obHeight = r.bottom - r.top;
				if (obHeight > lineHeight)
					lineHeight = obHeight;
			}

			//这一行满了，存入contentList,新起一行
			if (width - drawedWidth < obWidth)
			{
				contentList.add(line);

				if (drawedWidth > lineWidthMax)
				{
					lineWidthMax = drawedWidth;
				}
				drawedWidth = 0;
				height += line.height + lineSpacing;

				lineHeight = obHeight;

				line = new LINE();
			}

			drawedWidth += obWidth;

			if (ob instanceof String && line.line.size() > 0 && (line.line.get(line.line.size() - 1) instanceof String))
			{
				int size = line.line.size();
				sb = new StringBuilder();
				sb.append(line.line.get(size - 1));
				sb.append(ob);
				ob = sb.toString();
				obWidth = obWidth + line.widthList.get(size - 1);
				line.line.set(size - 1, ob);
				line.widthList.set(size - 1, (int) obWidth);
				line.height = (int) lineHeight;

			}
			else
			{
				line.line.add(ob);
				line.widthList.add((int) obWidth);
				line.height = (int) lineHeight;
			}

		}
		if (line != null && line.line.size() > 0)
		{
			contentList.add(line);
			height += lineHeight + lineSpacing;
		}
		if (contentList.size() <= 1)
		{
			oneLineWidth = (int) drawedWidth + leftPadding + rightPadding;
			height = lineSpacing + lineHeight + lineSpacing;
		}

		cacheData(width,(int) height);
		return (int) height;
	}
    /**
     * 获取缓存的测量数据，避免多次重复测量
     * @param text 
     * @param width
     * @return height
     */
	@SuppressWarnings("unchecked")
	private int getCachedData(String text, int width)
	{
		SoftReference<MeasuredData> cache = measuredData.get(text);
		if(cache == null)
			return -1;
		MeasuredData md = cache.get();
		if (md != null && md.textSize == this.getTextSize() && width == md.width)
		{
			lineWidthMax = md.lineWidthMax;
			contentList = (ArrayList<LINE>) md.contentList.clone();
			oneLineWidth = md.oneLineWidth;
			
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<contentList.size();i++)
			{
				LINE line = contentList.get(i);
			 sb.append(line.toString());   
			}
			return md.measuredHeight;
		}
		else
			return -1;
	}

	/**
	 * 缓存已测量的数据
	 * @param width 
	 * @param height
	 */
	@SuppressWarnings("unchecked")
	private void cacheData(int width, int height)
	{
		MeasuredData md = new MeasuredData();
		md.contentList = (ArrayList<LINE>) contentList.clone();
		md.textSize = this.getTextSize();
		md.lineWidthMax = lineWidthMax;
		md.oneLineWidth = oneLineWidth;
		md.measuredHeight = height;
		md.width = width;
		md.hashIndex = ++hashIndex;
		
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<contentList.size();i++)
		{
			LINE line = contentList.get(i);
		 sb.append(line.toString());   
		}

		SoftReference<MeasuredData> cache = new SoftReference<MeasuredData>(md);
		measuredData.put(text.toString(),cache);
	}
    /**
     * 用本函数代替{@link #setText(CharSequence)}
     * @param cs
     */
	public void setMText(CharSequence cs)
	{
		text = cs;

		obList.clear();
		//	contentList.clear();

		ArrayList<IS> isList = new ArrayList<MTextView.IS>();
		useDefault = false;
		if (cs instanceof SpannableString)
		{
			SpannableString ss = (SpannableString) cs;
			ImageSpan[] imageSpans = ss.getSpans(0, ss.length(), ImageSpan.class);
			for (int i = 0; i < imageSpans.length; i++)
			{
				int s = ss.getSpanStart(imageSpans[i]);
				int e = ss.getSpanEnd(imageSpans[i]);
				IS iS = new IS();
				iS.is = imageSpans[i];
				iS.start = s;
				iS.end = e;
				isList.add(iS);
			}
		}

		String str = cs.toString();

		for (int i = 0, j = 0; i < cs.length();)
		{
			if (j < isList.size())
			{
				IS is = isList.get(j);
				if (i < is.start)
				{
					Integer cp = str.codePointAt(i);
					//支持增补字符
					if (Character.isSupplementaryCodePoint(cp))
					{
						i += 2;
					}
					else
					{
						i++;
					}

					obList.add(new String(Character.toChars(cp)));

				}
				else if (i >= is.start)
				{
					obList.add(is.is);
					j++;
					i = is.end;
				}
			}
			else
			{
				Integer cp = str.codePointAt(i);
				if (Character.isSupplementaryCodePoint(cp))
				{
					i += 2;
				}
				else
				{
					i++;
				}

				obList.add(new String(Character.toChars(cp)));
			}
		}

		requestLayout();
	}

	public void setUseDefault(boolean useDefault)
	{
		this.useDefault = useDefault;
		if (useDefault)
		{
			this.setText(text);
			this.setTextColor(textColor);
		}
	}

	public static int px2sp(Context context, float pxValue)
	{
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue)
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
    /**
     * @功能: 存储ImageSpan及其开始结束位置
     * @author huangwei
     * @2014年5月27日
     * @下午5:21:37
     */
	class IS
	{
		public ImageSpan is;
		public int start;
		public int end;
	}
    /**
     * @功能: 存储测量好的一行数据
     * @author huangwei
     * @2014年5月27日
     * @下午5:22:12
     */
	class LINE
	{
		public ArrayList<Object> line = new ArrayList<Object>();
		public ArrayList<Integer> widthList = new ArrayList<Integer>();
		public int height;
		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder("height:"+height+"   ");
			for(int i=0;i<line.size();i++)
			{
				sb.append(line.get(i)+":"+widthList.get(i));
			}
			return sb.toString();
		}
		
		
	}
    /**
     * @功能: 缓存的数据
     * @author huangwei
     * @2014年5月27日
     * @下午5:22:25
     */
	class MeasuredData
	{
		public int measuredHeight;
		public float textSize;
		public int width;
		public float lineWidthMax;
		ArrayList<LINE> contentList;
		public int oneLineWidth;
		public int hashIndex;

	}

}