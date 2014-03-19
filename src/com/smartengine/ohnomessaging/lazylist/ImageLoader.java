package com.smartengine.ohnomessaging.lazylist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.makeramen.RoundedDrawable;
import com.ohnomessaging.R;


public class ImageLoader {

	private MemoryCache memoryCache = new MemoryCache();
	private FileCache fileCache;
	@SuppressWarnings("unused")
	private Context mContext;
	// private ProgressBar mProgressBar;
	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	private ExecutorService executorService;
	private Handler handler = new Handler();// handler to display images in UI
											// thread
	private Bitmap defaultFrame;
	private int imageWidth;

	// static Bitmap resizedBitmap;

	// int imageWidth;

	// final int stub_id=R.drawable.details_back;

	public ImageLoader(Context context) {
		this.mContext = context;
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
		// this.defaultFrame = defaultFrame;
		this.defaultFrame = BitmapFactory.decodeResource(
				((Activity) context).getResources(),
				R.drawable.ic_contact_picture2);
		this.imageWidth = 0;
	}

	public ImageLoader(Context context, int imageWidth) {
		this.mContext = context;
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
		// this.defaultFrame = defaultFrame;
		this.defaultFrame = BitmapFactory.decodeResource(
				((Activity) context).getResources(),
				R.drawable.ic_contact_picture2);

		this.imageWidth = imageWidth;
	}

	// public ImageLoader(Activity context, ProgressBar mProgressBar){
	// this.mContext = context;
	// fileCache=new FileCache(context);
	// executorService=Executors.newFixedThreadPool(5);
	// this.mProgressBar = mProgressBar;
	// mProgressBar.setVisibility(View.VISIBLE);
	// }

	// public void DisplayImage(String url, ImageView imageView){
	// imageViews.put(imageView, url);
	// Bitmap bitmap = memoryCache.get(url);
	// if(bitmap!=null){
	// imageView.setImageBitmap(resizeBitmap(bitmap));
	// }
	// else
	// {
	// queuePhoto(url, imageView, 140);
	// // imageView.setImageResource(stub_id);
	// }
	// }

	public void DisplayImage(String url, ImageView imageView) {

		imageViews.put(imageView, url);

		// String downloadedMd5checksum = new Md5Hash(url +
		// "_reflected").toHex();
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {
			// Logger.e("ImageLoader", "bitmap is not null");
			if (imageWidth == 0)
				imageView.setImageBitmap(bitmap);
			else {
				Bitmap resizedBitmap = getResizedBannerBitmapWidth(bitmap,
						imageWidth);
				imageView.setImageBitmap(resizedBitmap);
				// resizedBitmap = null;
				// if(resizedBitmap != null)
				// resizedBitmap.recycle();
			}

		} else {
			// Logger.e("ImageLoader", "bitmap is null, starting queryphoto()");
			queuePhoto(url, imageView, 200);
			if (imageWidth == 0)
				imageView.setImageBitmap(defaultFrame);
			else {
				Bitmap resizedBitmap = getResizedBannerBitmapWidth(
						defaultFrame, imageWidth);
				imageView.setImageBitmap(resizedBitmap);
				// resizedBitmap = null;
				// if(resizedBitmap != null)
				// resizedBitmap.recycle();
			}
		}
	}

	/** @author Touhid */
	public Bitmap getRoundedPicFromURL(String url, ImageView imageView) {

		Log.d("getRoundedProfilePic()", "inside with url: " + url);

		// Setting url to check inside the threads run from this method :)
		imageViews.put(imageView, url);

		Bitmap bitmap = memoryCache.get(url);
		Bitmap retBitmap;
		if (bitmap != null) {
			// Logger.e("ImageLoader", "bitmap is not null");
			if (imageWidth == 0) {
				Log.d("getRoundedProfilePic()", "bitmap != null & imgWidth=0");
				retBitmap = getRoundedImage(bitmap, 200);
			} else {
				Log.d("getRoundedProfilePic()", "bitmap != null & imgWidth!=0");
				Bitmap resizedBitmap = getResizedBannerBitmapWidth(bitmap,
						imageWidth);
				retBitmap = getRoundedImage(resizedBitmap, 200);
				// resizedBitmap = null;
				// if(resizedBitmap != null)
				// resizedBitmap.recycle();
			}

		} else {
			// Logger.e("ImageLoader", "bitmap is null, starting queryphoto()");
			// The "fazil" thread starts from this method call 8|
			queuePhoto(url, imageView, 200, true);

			if (imageWidth == 0) {
				Log.d("getRoundedProfilePic()",
						"bitmap = null & imgWidth=0, returning defaultFrame");
				retBitmap = getRoundedImage(defaultFrame, 200);
			} else {
				Log.d("getRoundedProfilePic()",
						"bitmap = null & imgWidth!=0, returning resized defaultFrame");
				Bitmap resizedBitmap = getResizedBannerBitmapWidth(
						defaultFrame, imageWidth);
				retBitmap = getRoundedImage(resizedBitmap, 200);
				// resizedBitmap = null;
				// if(resizedBitmap != null)
				// resizedBitmap.recycle();
			}
		}
		return retBitmap;
	}

	/** @author Touhid */
	public Bitmap getRoundedImage(Bitmap bitmap, int cornerRadius) {
		Bitmap scaled = Bitmap.createScaledBitmap(bitmap, cornerRadius,
				cornerRadius, false);
		return RoundedDrawable.fromBitmap(scaled)
				.setScaleType(ImageView.ScaleType.CENTER_CROP)
				.setCornerRadius(cornerRadius).setOval(true).toBitmap();
	}

	// was public method _ Edited by Touhid
	private static Bitmap getResizedBannerBitmapWidth(Bitmap bm, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;

		float dstWidth = ((float) width) * scaleWidth;
		float dstHeight = ((float) height) * scaleWidth;

		// Matrix matrix = new Matrix();
		//
		// matrix.postScale(scaleWidth, scaleWidth);

		// Logger.e("ImageLoader -> getResizedBitmapWidth",
		// "width, height ---> " + width + ", " + height);
		// Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
		// matrix, false);
		// if(resizedBitmap != null)
		// resizedBitmap.recycle();
		// Bitmap resizedBitmap = bm;
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, (int) dstWidth,
				(int) dstHeight, false);
		// Logger.e("ImageLoader -> getResizedBitmapWidth",
		// "resizedBitmap width, height " + resizedBitmap.getWidth() + ", " +
		// resizedBitmap.getHeight());
		// return resizedBitmap;
		return resizedBitmap;
	}

	// private Bitmap resizeBitmap(Bitmap bitmap) {
	// Bitmap temp = null;
	// try{
	// Logger.d("ImageLoader",
	// "bitmap size in resizeBitmap"+bitmap.getRowBytes());
	// if (bitmap != null) {
	// WindowManager w = mContext.getWindowManager();
	// // Point size = new Point();
	// int Measuredwidth = 0;
	// int Measuredheight = 0;
	// float witdh,height;
	// if(bitmap.getWidth()>mContext.getWindowManager().getDefaultDisplay().getWidth())
	// {
	// Display d = w.getDefaultDisplay();
	// Measuredwidth = d.getWidth();
	// Measuredheight = d.getHeight();
	// Logger.e("Measuredwidth",""+Measuredwidth);
	// Logger.e("Measuredheight",""+Measuredheight);
	// if(Measuredheight<=800){
	// witdh = 300;
	// height = 500;
	// }
	// else{
	// witdh = Measuredheight;
	// height = Measuredwidth;
	// }
	// float retion=((float)witdh)/((float)bitmap.getWidth());
	// witdh=((float)bitmap.getWidth())*retion;
	// height=((float)bitmap.getHeight())*retion;
	//
	// temp=bitmap.createScaledBitmap(bitmap, (int)(witdh), (int)(height),
	// true);
	// }else
	// temp=bitmap;
	// }
	//
	// } catch(Exception e){
	// temp=bitmap;
	// e.printStackTrace();
	// }
	// return temp;
	// }
	//
	// private Bitmap resizeBitmap_splash(Bitmap bitmap) {
	// Bitmap temp = null;
	// try{
	// // Loggerger.logger("bitmap size"+bitmap.getRowBytes());
	// if (bitmap != null) {
	// WindowManager w = mContext.getWindowManager();
	//
	// int Measuredwidth = 0;
	// int Measuredheight = 0;
	// float width,height;
	//
	// Display d = w.getDefaultDisplay();
	// Measuredwidth = d.getWidth();
	// Measuredheight = d.getHeight();
	// // Loggerger.logger("========", "Measuredwidth = " + Measuredwidth +
	// " & Measuredheight = " + Measuredheight);
	// // Logger.e("Measuredwidth",""+Measuredwidth);
	// // Logger.e("Measuredheight",""+Measuredheight);
	//
	// // Loggerger.logger("========", "bitmap width = " + bitmap.getWidth() +
	// " & bitmap height = " + bitmap.getHeight());
	// float retion=((float)Measuredwidth)/((float)bitmap.getWidth());
	// width=((float)bitmap.getWidth())*retion;
	// height=((float)bitmap.getHeight())*retion;
	// // Loggerger.logger("on screen", "bitmap width = " + width +
	// " & bitmap height = " + height);
	// // Logger.e("witdh",""+witdh);
	// // Logger.e("height",""+height);
	// temp = bitmap.createScaledBitmap(bitmap, (int)(width), (int)(height),
	// true);
	//
	// }
	//
	// } catch(Exception e){
	// temp = bitmap;
	// e.printStackTrace();
	// }
	// return temp;
	// }

	private void queuePhoto(String url, ImageView imageView, int imageQuality) {
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p, imageQuality));
	}

	private void queuePhoto(String url, ImageView imageView, int imageQuality,
			boolean isRounded) {
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p, imageQuality, isRounded));
	}

	// was public _ Edited by Touhid
	private Bitmap getBitmap(String url, int imageQuality) {
		File f = fileCache.getFile(url);

		// from SD cache
		Bitmap b = decodeFile(f, imageQuality);
		if (b != null) {
			// Loggerger.logger("retrieving splash from file cache");
			return b;
		}

		// from web
		try {
			Bitmap bitmap = null;
			Log.d("-------", url);
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl
					.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			conn.disconnect();
			bitmap = decodeFile(f, imageQuality);
			return bitmap;
		} catch (Throwable ex) {
			ex.printStackTrace();
			if (ex instanceof OutOfMemoryError)
				memoryCache.clear();
			return null;
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f, int imageQuality) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream stream1 = new FileInputStream(f);
			BitmapFactory.decodeStream(stream1, null, o);
			stream1.close();

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = imageQuality;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;

			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}
			Log.i("SCALE", "scale = " + scale);

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			FileInputStream stream2 = new FileInputStream(f);
			Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
			stream2.close();
			return bitmap;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	private class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;
		int imageQuality;
		boolean isRounded;

		PhotosLoader(PhotoToLoad photoToLoad, int imageQuality) {
			this.photoToLoad = photoToLoad;
			this.imageQuality = imageQuality;
			isRounded = false;
		}

		public PhotosLoader(PhotoToLoad photoToLoad, int imageQuality,
				boolean _isRounded) {
			this.photoToLoad = photoToLoad;
			this.imageQuality = imageQuality;
			this.isRounded = _isRounded;
		}

		@Override
		public void run() {
			try {
				if (imageViewReused(photoToLoad))
					return;
				Bitmap bmp = getBitmap(photoToLoad.url, imageQuality);

				// String downloadedMd5checksum = new Md5Hash(photoToLoad.url +
				// "_reflected").toHex();
				memoryCache.put(photoToLoad.url, bmp);

				if (imageViewReused(photoToLoad))
					return;
				BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad,
						isRounded);
				handler.post(bd);
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}

	private boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	private class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;
		boolean isRounded;

		// public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
		// bitmap = b;
		// photoToLoad = p;
		// isRounded = false;
		// }

		public BitmapDisplayer(Bitmap b, PhotoToLoad p, boolean _isRounded) {
			bitmap = b;
			photoToLoad = p;
			this.isRounded = _isRounded;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null) {
				// Bitmap circleBitmap = bitmap; // TODO
				Bitmap circleBitmap = bitmap;
				if (isRounded)
					circleBitmap = getRoundedImage(bitmap, 200);
				// Bitmap circleBitmap = getRoundedCornerBitmap(bitmap, 10);

				if (imageWidth == 0)
					photoToLoad.imageView.setImageBitmap(circleBitmap);
				else {
					Bitmap resizedBitmap = getResizedBannerBitmapWidth(
							circleBitmap, imageWidth);
					photoToLoad.imageView.setImageBitmap(resizedBitmap);
					// resizedBitmap = null;
				}
			} else {
				if (imageWidth == 0) {
					photoToLoad.imageView.setImageBitmap(defaultFrame);
				} else {
					Bitmap resizedBitmap = getResizedBannerBitmapWidth(
							defaultFrame, imageWidth);
					photoToLoad.imageView.setImageBitmap(resizedBitmap);
					// resizedBitmap = null;
				}
			}
			// if(bitmap != null)
			// bitmap.recycle();
		}
	}

	@SuppressWarnings("unused")
	private void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

	@SuppressWarnings("unused")
	private static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		// canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
				bitmap.getWidth() / 2, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}
}
