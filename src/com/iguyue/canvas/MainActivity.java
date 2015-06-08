package com.iguyue.canvas;

import java.util.ArrayList;

import com.iguyue.canvas.widget.EdgeBoundListView;
import com.iguyue.canvas.widget.MultiListView;
import com.iguyue.canvas.widget.SpringBackListView;
import com.iguyue.canvas.widget.SpringListView;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_multi_list_view);
		
//		TextRollCycleView view = (TextRollCycleView) findViewById( R.id.text_roll );
		
//		SpringListView listView = (SpringListView) findViewById( R.id.list_view );
//		SpringBackListView listView = (SpringBackListView) findViewById( R.id.list_view );
//		EdgeBoundListView listView = (EdgeBoundListView) findViewById( R.id.list_view );
		MultiListView listView = (MultiListView) findViewById( R.id.list_view );
		
		final ArrayList<String> datas = new ArrayList<String>();
		datas.add( "我是中国大亨我是中国大亨我是中国大亨我是中国大亨我是中国大亨我是中国大亨我是中国大亨我是中国大亨速度速度撒大大撒" );
		datas.add( "速度速度撒大大撒 " );
		datas.add( "成为我我我我问问的 " );
		datas.add( "顶顶顶顶服务网 " );
		datas.add( "没上搜搜少女 " );
		datas.add( "我看到买到票是的 " );
		datas.add( "我我我我为全区 " );
		datas.add( "没机会哦便宜过分 " );
		datas.add( "倒萨阿事实上实施 " );
		datas.add( "我是中国大亨 " );
		datas.add( "速度速度撒大大撒 " );
		datas.add( "成为我我我我问问的 " );
		datas.add( "顶顶顶顶服务网 " );
		datas.add( "没上搜搜少女 " );
		datas.add( "我看到买到票是的 " );
		datas.add( "我我我我为全区 " );
		datas.add( "没机会哦便宜过分 " );
		datas.add( "倒萨阿事实上实施 " );
		datas.add( "我是中国大亨 " );
		datas.add( "速度速度撒大大撒 " );
		datas.add( "成为我我我我问问的 " );
		datas.add( "顶顶顶顶服务网 " );
		datas.add( "没上搜搜少女 " );
		datas.add( "我看到买到票是的 " );
		datas.add( "我我我我为全区 " );
		datas.add( "没机会哦便宜过分 " );
		datas.add( "倒萨阿事实上实施 " );
		datas.add( "我是中国大亨 " );
		datas.add( "速度速度撒大大撒 " );
		datas.add( "成为我我我我问问的 " );
		datas.add( "顶顶顶顶服务网 " );
		datas.add( "没上搜搜少女 " );
		datas.add( "我看到买到票是的 " );
		datas.add( "我我我我为全区 " );
		datas.add( "没机会哦便宜过分 " );
		datas.add( "倒萨阿事实上实施 " );
		datas.add( "我是中国大亨 " );
		datas.add( "速度速度撒大大撒 " );
		datas.add( "成为我我我我问问的 " );
		datas.add( "顶顶顶顶服务网 " );
		datas.add( "没上搜搜少女 " );
		datas.add( "我看到买到票是的 " );
		datas.add( "我我我我为全区 " );
		datas.add( "没机会哦便宜过分 " );
		datas.add( "倒萨阿事实上实施 " );
		datas.add( "我是中国大亨 " );
		datas.add( "速度速度撒大大撒 " );
		datas.add( "成为我我我我问问的 " );
		datas.add( "顶顶顶顶服务网 " );
		datas.add( "没上搜搜少女 " );
		datas.add( "我看到买到票是的 " );
		datas.add( "我我我我为全区 " );
		datas.add( "没机会哦便宜过分 " );
		datas.add( "倒萨阿事实上实施 " );
//		view.setTexts( datas );
		
		MoonAdadpter moonAdadpter = new MoonAdadpter( datas );
		listView.setAdapter( moonAdadpter );
		
		listView.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText( getApplicationContext(), "Click->" + position + "->" + datas.get(position), Toast.LENGTH_SHORT).show();
			}
		});
		
		listView.setOnItemLongClickListener( new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText( getApplicationContext(), "LongClick->" + position + "->" + datas.get(position), Toast.LENGTH_SHORT).show();
				return true;
			}
		});
	}
	
	private class MoonAdadpter extends BaseAdapter
	{
		private ArrayList<String> datas;
		
		public MoonAdadpter(ArrayList<String> datas) 
		{
			this.datas = datas;
		}

		@Override
		public int getCount() {
			return datas.size();
		}

		@Override
		public Object getItem(int position) {
			return datas.get(position);
		}

		@Override
		public long getItemId(int position) 
		{
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			ViewHolder holder;
	        if ( convertView == null ) 
	        {
	        	holder = new ViewHolder();
	        	convertView = LayoutInflater.from( getApplicationContext() ).inflate( R.layout.list_item, null );
	        	holder.tvContent = (TextView) convertView.findViewById( R.id.tv_content );
	        	convertView.setTag( holder );
	        }
	        else
	        {
	        	holder = (ViewHolder) convertView.getTag();
	        }
			
	        holder.tvContent.setText( position + "->" + datas.get( position ) );

			return convertView;
		}
	}
	
	static class ViewHolder 
	{
		TextView tvContent;
	};
}
