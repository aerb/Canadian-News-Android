package com.tastyfish.news;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.graphics.Bitmap;

public class NewsItem {

	private String _title;
	private String _brief;
	private String _author;
	private String _written;
	private String _image;
	private String _link;
	private Bitmap _bmp;

	public NewsItem() {

	}

	public NewsItem(String title, String brief, String author, String written,
			String image, String link, Bitmap bmp) {
		set_title(title);
		set_brief(brief);
		set_author(author);
		this._written = written;
		set_image(image);
		set_link(link);
		set_bmp(bmp);
	}

	public NewsItem copy() {
		NewsItem n = new NewsItem(this._title, this._brief, this._author,
				this._written, this._image, this._link, this._bmp);
		return n;
	}

	public String get_title() {
		return _title;
	}

	public void set_title(String _title) {
		this._title = _title;
	}

	public String get_brief() {
		return _brief;
	}

	public void set_brief(String _brief) {
		this._brief = _brief;
	}

	public String get_author() {
		return _author;
	}

	public void set_author(String _author) {
		this._author = _author;
	}

	public String get_written() {
		return _written;
	}

	public void set_written(String date) {
		SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm Z");
		try {
			Date written = df.parse(date);
			Date current = new Date();
			long Diff = (current.getTime() - written.getTime())
					/ (1000 * 60 * 60);
			if (Diff < 1)
				this._written = "";
			else {
				this._written = String.valueOf(Diff);
				this._written += Diff == 1 ? " hour ago" : " hours ago";
			}
		} catch (ParseException e) {
			this._written = "";
			e.printStackTrace();
		}
	}

	public String get_image() {
		return _image;
	}

	public void set_image(String _image) {
		this._image = _image;
	}

	public String get_link() {
		return _link;
	}

	public void set_link(String _link) {
		this._link = _link;
	}

	public Bitmap get_bmp() {
		return _bmp;
	}

	public void set_bmp(Bitmap _bmp) {
		this._bmp = _bmp;
	}

}
