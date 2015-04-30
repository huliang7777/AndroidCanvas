package com.iguyue.canvas;

import java.util.ArrayList;

import com.iguyue.canvas.widget.TextRollCycleView;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text_roll);
		
		TextRollCycleView view = (TextRollCycleView) findViewById( R.id.text_roll );
		
		ArrayList<String> datas = new ArrayList<String>();
		datas.add( "我是中国大亨 " );
		datas.add( "速度速度撒大大撒 " );
		datas.add( "成为我我我我问问的 " );
		datas.add( "顶顶顶顶服务网 " );
		datas.add( "没上搜搜少女 " );
		datas.add( "我看到买到票是的 " );
		datas.add( "我我我我为全区 " );
		datas.add( "没机会哦便宜过分 " );
		datas.add( "倒萨阿事实上实施 " );
		view.setTexts( datas );
	}
}
