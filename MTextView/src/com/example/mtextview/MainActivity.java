package com.example.mtextview;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.TextView;

public class MainActivity extends Activity
{
	private MTextView mTextView;
	private TextView textView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		mTextView = (MTextView) this.findViewById(R.id.mtextview);
		textView = (TextView) this.findViewById(R.id.textview);
	    
        
	    Test();
	    TestNormal();
	}
	

	private void Test()
	{
		mTextView.setBackgroundColor(Color.GREEN);
		String source = "撒反对飞王瑞芳芳vfxdsdf司法所我日 35忍32534太 地方个的服务 34个的服务 34太过分的电饭锅电饭锅打三国杀个的服务 34太过分的电饭锅电饭锅打三国杀太过分的电饭锅电饭锅打三国杀水电费歌曲筒袜上课5乳房炎啊啊。";
		SpannableString ss = new SpannableString(source);
		
		
		int plus = 1;
		for(int i=0;i<source.length()-2;i+=plus)
		{
			plus = (int) (Math.random()*5);
			ImageSpan is = new ImageSpan(MainActivity.this,R.drawable.emoji_29);
			ss.setSpan(is,i,i+1,0);
		}
		mTextView.setMText(ss);
		mTextView.setTextSize(15);
		mTextView.setTextColor(Color.BLACK);
		
		mTextView.invalidate();
		

	}
	private void TestNormal()
	{
		textView.setBackgroundColor(Color.BLUE);
		String source = "撒反对飞王瑞芳芳vfxdsdf司法所我日 35忍32534太 地方个的服务 34个的服务 34太过分的电饭锅电饭锅打三国杀个的服务 34太过分的电饭锅电饭锅打三国杀太过分的电饭锅电饭锅打三国杀水电费歌曲筒袜上课5乳房炎啊啊。";
		SpannableString ss = new SpannableString(source);
		
		int plus = 1;
		for(int i=0;i<source.length()-2;i+=plus)
		{
			plus = (int) (Math.random()*5);
			ImageSpan is = new ImageSpan(MainActivity.this,R.drawable.emoji_29);
			ss.setSpan(is,i,i+1,0);
		}
		textView.setText(ss);
		textView.setTextSize(15);
		textView.setTextColor(Color.BLACK);
	
	}
}
