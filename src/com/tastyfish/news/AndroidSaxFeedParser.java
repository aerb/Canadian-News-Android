package com.tastyfish.news;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.text.Html;
import android.util.Xml;

public class AndroidSaxFeedParser{

	static final String CHANNEL = "channel";
	static final String PUB_DATE = "pubDate";
	static final  String DESCRIPTION = "description";
	static final  String LINK = "link";
	static final  String TITLE = "title";
	static final  String ITEM = "item";

	private final URL feedUrl;
	
	static final String RSS = "rss";
	public AndroidSaxFeedParser(String feedUrl) {
		try {
			this.feedUrl = new URL(feedUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected InputStream getInputStream() {
		try {
			return feedUrl.openConnection().getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<NewsItem> parse() {
		final NewsItem it = new NewsItem();
		RootElement root = new RootElement(RSS);
		final List<NewsItem> messages = new ArrayList<NewsItem>();
		Element channel = root.getChild(CHANNEL);
		Element item = channel.getChild(ITEM);
		item.setEndElementListener(new EndElementListener(){
			public void end() {
				messages.add(it.copy());
			}
		});
		item.getChild(TITLE).setEndTextElementListener(new EndTextElementListener(){
			public void end(String body) {
				it.set_title(Html.fromHtml(body).toString());
			}
		});
		item.getChild(LINK).setEndTextElementListener(new EndTextElementListener(){
			public void end(String body) {
				it.set_link(body);
			}
		});
		item.getChild(DESCRIPTION).setEndTextElementListener(new EndTextElementListener(){
			public void end(String body) {
				it.set_brief(body);
			}
		});

		item.getChild(PUB_DATE).setEndTextElementListener(new EndTextElementListener(){
			public void end(String body) {
				it.set_written(body);
			}
		});

		try {
			Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(feedUrl);		
		} catch (SAXException e) {	
			e.printStackTrace();
			System.out.println(feedUrl);		
		} catch (Exception e) {
			System.out.println(feedUrl);			
		}

		return messages;
	}
}
