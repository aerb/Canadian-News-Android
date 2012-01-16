package com.tastyfish.globe;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import android.app.ListActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

class NewsBundle{
	ViewPager pager;
	NewsPagerAdapter pagerAdapter;
	
	boolean[] newsLoaded;
	ArrayList<List<NewsItem>> newsItems;
	ArrayList<ListView> newsListView;
	TabPageIndicator tabPageIndicator;
	ArrayList<String> titles;
	
}

public class GlobeMailAndroidScratchActivity extends Activity {
  
	ViewPager pager;
	NewsPagerAdapter pagerAdapter;
	
	boolean[] newsLoaded;
	ArrayList<List<NewsItem>> newsItems;
	ArrayList<ListView> newsListView;
	TabPageIndicator tabPageIndicator;
	ArrayList<String> titles;
	
	int runningThreads = 0;
	int currentIndex = 0;
	Stack<NewsItem> q = new Stack<NewsItem>(); 
	
	public String getTitles(int index) {
		return titles.get(index);
	}
	
	public void setTitle(int position, String title){
		if(position >= titles.size())
			titles.ensureCapacity(position + 1);
		titles.add(position, title);
	}

	public ListView getNewsListView(int index) {
		return newsListView.get(index);
	}
	ProgressDialog dialog;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        NewsBundle data = (NewsBundle)getLastNonConfigurationInstance();
        if (data == null){
        	newsItems = new ArrayList<List<NewsItem>>();
        	titles = new ArrayList<String>();
    		for(int i = 0; i < 8; ++i) 
    			titles.add("");
        	
	        for(int i = 0 ; i < 8; ++i) 
	        	newsItems.add(new ArrayList<NewsItem>());
	        newsLoaded = new boolean[8];
        
        
	        newsListView = new ArrayList<ListView>();
	    	for(int i = 0; i < 8; ++i){
	    		newsListView.add(new ListView(this));
	        	NewsAdapter aa = new NewsAdapter(this, R.layout.row, newsItems.get(i));
	        	newsListView.get(i).setAdapter(aa);
	        	newsListView.get(i).setOnItemClickListener(clickListener);
	    	}

	        pager = (ViewPager)this.findViewById(R.id.pager);
	        
	        pagerAdapter = new NewsPagerAdapter(this);
	        pager.setAdapter(pagerAdapter);
	
	        tabPageIndicator = (TabPageIndicator)findViewById(R.id.titles);
	        tabPageIndicator.setOnPageChangeListener(newsFeedChanged);
	        tabPageIndicator.setViewPager(pager);
	    	
	    	dialog = ProgressDialog.show(this , "", "Loading...", true);
	    	Thread feedThread = new Thread(new Runnable(){
	    		public void run() {
	    			loadFeed(0,"http://www.theglobeandmail.com/?service=rss&feed=topstories", "Top Stories");
	    			loadFeed(1,"http://www.theglobeandmail.com/news/national/?service=rss", "National");
	    			loadFeed(2,"http://www.theglobeandmail.com/news/politics/?service=rss", "Politics");
	    			loadFeed(3,"http://www.theglobeandmail.com/report-on-business/?service=rss", "Buisness");
	    			loadFeed(4,"http://www.theglobeandmail.com/news/world/?service=rss", "World");
	    			loadFeed(5,"http://www.theglobeandmail.com/news/technology/?service=rss", "Tech");
	    			loadFeed(6,"http://www.theglobeandmail.com/news/arts/?service=rss", "Art");
	    			loadFeed(7,"http://www.theglobeandmail.com/sports/?service=rss", "Sports");
	    			dialog.dismiss();
	    			handler.sendEmptyMessage(2);
	    			handler.sendEmptyMessage(1);
	    			createImageLoadStack();
	    			addImagesToStack(0,true);
	    		}
	    	});
	    	feedThread.start();
        }
        else{
            this.newsItems = data.newsItems;
            this.newsLoaded = data.newsLoaded;
            this.titles = data.titles;
            
	        newsListView = new ArrayList<ListView>();
	    	for(int i = 0; i < 8; ++i){
	    		newsListView.add(new ListView(this));
	        	NewsAdapter aa = new NewsAdapter(this, R.layout.row, newsItems.get(i));
	        	newsListView.get(i).setAdapter(aa);
	        	newsListView.get(i).setOnItemClickListener(clickListener);
	    	}

	        pager = (ViewPager)this.findViewById(R.id.pager);
	        
	        pagerAdapter = new NewsPagerAdapter(this);
	        pager.setAdapter(pagerAdapter);
	
	        tabPageIndicator = (TabPageIndicator)findViewById(R.id.titles);
	        tabPageIndicator.setOnPageChangeListener(newsFeedChanged);
	        tabPageIndicator.setViewPager(pager);

        }
    }

	public void createImageLoadStack(){
		new Thread(new Runnable(){
			public void run() {
				while(true){
					if(runningThreads < 3 && q.size() > 0){
						++runningThreads;
						final NewsItem n = q.pop();
		    			new Thread(new Runnable(){
		    	    		public void run() {
		    	    			loadImages(n);
		    	    			--runningThreads;
		    	    		}
		    	    	}).start();
					}
					else
					{
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if(q.empty()){
							int i = 0;
							boolean everyThingLoaded = true;
							for(boolean b : newsLoaded){
								if(!b){
									addImagesToStack(i,false);
									everyThingLoaded = false;
									break;
								}
								++i;
							}
							if(everyThingLoaded) return;
						}
					}
				}
			}
		}).start();
	}
	
	public void addImagesToStack(int ind, boolean highPriority){
		if(!newsLoaded[ind]){
			if(highPriority){
				for(NewsItem n : newsItems.get(ind)){
					q.push(n);
				}
			}
			else{
				for(NewsItem n : newsItems.get(ind)){
					q.insertElementAt(n, 0);
				}	
			}
			newsLoaded[ind] = true;
		}
	}
	
    Handler handler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    		switch(msg.what){
    		case 1:
    			pagerAdapter.notifyDataSetChanged();
    			((NewsAdapter)newsListView.get(currentIndex).getAdapter()).notifyDataSetChanged();
//    			for(ListView l : newsListView)
//    				((NewsAdapter)l.getAdapter()).notifyDataSetChanged();
    			break;
    		case 2:
    			tabPageIndicator.notifyDataSetChanged();
    		default:
    			break;
    		}
    	}
    };
    
    public void loadFeed(int feedNumber, String URL, String title){
    	AndroidSaxFeedParser sax = new AndroidSaxFeedParser(URL);
    	List<NewsItem> newNewsItems = sax.parse();
    	
    	this.setTitle(feedNumber, title);
    	
    	newsItems.get(feedNumber).clear();
    	int itemcount = 0;
    	for (NewsItem n : newNewsItems){
    		if(itemcount > 4) break;
    		newsItems.get(feedNumber).add(n);
    		++itemcount;
    	}
    }
    
    OnItemClickListener clickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> news, View view, int pos, long id) {
			final NewsItem item = (NewsItem) news.getAdapter().getItem(pos);

			Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) item.get_link()));
			startActivity(viewIntent);
		}
	};
	
	OnPageChangeListener newsFeedChanged = new OnPageChangeListener(){

		@Override
		public void onPageScrollStateChanged(int arg0) {
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}

		@Override
		public void onPageSelected(int arg0) {
			currentIndex = arg0;
			addImagesToStack(arg0, true);
		}
	};
    
    public void loadImages(NewsItem n){    	
		Document doc = null;
		try {
			doc = Jsoup.connect(n.get_link()).get();
		}catch (SocketTimeoutException e){
			try {
				doc = Jsoup.connect(n.get_link()).get();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		Element masthead = doc != null ? doc.select("div.articleleadphoto").first() : null;
		Elements imgs =  masthead != null?masthead.select("img") : null;
		Element img = null;
		if (imgs != null) 
			img = imgs.first();
	    
		if (img != null){
			String imgSrc = img.attr("src");
			InputStream is = null;
			try {
				URL iUrl;
				iUrl = new URL(imgSrc);
				HttpURLConnection iConn = (HttpURLConnection)iUrl.openConnection();
	            iConn.setDoInput(true);
	            iConn.connect();
	            is = iConn.getInputStream();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	           
	        Bitmap bmImg = BitmapFactory.decodeStream(is);
	        n.set_bmp(bmImg);
	        handler.sendEmptyMessage(1);
		}
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    }
    
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    }
    
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        NewsBundle data = new NewsBundle();
        data.newsItems = this.newsItems;
        data.newsLoaded = this.newsLoaded;
        data.titles = this.titles;
        return data;
    }    
}

