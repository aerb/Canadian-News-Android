package com.tastyfish.news;

import java.util.List;

import com.tastyfish.news.R;
import com.tastyfish.news.R.id;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NewsAdapter extends ArrayAdapter<NewsItem> {
	private LayoutInflater mInflater;
	
	Context context;
	int resource;
	
	public NewsAdapter(Context _context, int _resource, List<NewsItem> _items) {
		super(_context, _resource, _items);
		mInflater = (LayoutInflater)_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		resource = _resource;
		context = _context;
	}
	
	static class ViewHolder {
		TextView title;
		TextView score;
		ImageView image;
		TextView date;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		NewsItem item = getItem(position);
		
		if (convertView == null) {
			convertView = mInflater.inflate(resource, parent, false);
			holder = new ViewHolder();
			holder.title = (TextView)convertView.findViewById(R.id.title);
			holder.score = (TextView)convertView.findViewById(R.id.score);
			holder.image = (ImageView)convertView.findViewById(R.id.lilimage);
			holder.date = (TextView)convertView.findViewById(R.id.date);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		holder.title.setText(item.get_title());
		holder.score.setText(item.get_written());
		if(item.get_bmp() != null)
			holder.image.setImageBitmap(item.get_bmp());
			
		holder.date.setText("");

		return convertView;
	}
}