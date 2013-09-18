package marshal.cv;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity implements SurfaceHolder.Callback {

	SurfaceView surfaceView;

	Camera camera;
	
	FeatureDetector featureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		this.surfaceView.getHolder().setKeepScreenOn(true);
		this.surfaceView.getHolder().addCallback(this);
		
		featureDetector=new FeatureDetector();
		Log.d("feature_detector",">>>>>>>>>>"+featureDetector.getOpenCvVersion());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Camera.Parameters params = camera.getParameters();

		// 处理预览图片长宽比，这里固定写法仅为支持Galaxy S4
		params.setPictureSize(1280, 720);
		params.setPreviewSize(1920, 1080);
		
		// 处理自动对焦参数
		List<String> focusModes = params.getSupportedFocusModes();

		String CAF_PICTURE = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, CAF_VIDEO = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO, supportedMode = focusModes
				.contains(CAF_PICTURE) ? CAF_PICTURE : focusModes
				.contains(CAF_VIDEO) ? CAF_VIDEO : "";

		if (!supportedMode.equals("")) {
			params.setFocusMode(supportedMode);
		}
		
		camera.setParameters(params);

		surfaceView.getHandler().postDelayed(new Runnable() {

			@Override
			public void run() {
				camera.setOneShotPreviewCallback(new PreviewCallback() {
					@Override
					public void onPreviewFrame(byte[] data, Camera camera) {
						Size size = camera.getParameters().getPreviewSize();
						
						
						featureDetector.putCameraPreview(data, size.width,size.height);
						
						
//						
//						// 从data到Bitmap
//						Size size = camera.getParameters().getPreviewSize();
//						try {
//							YuvImage image = new YuvImage(data,
//									ImageFormat.NV21, size.width, size.height,
//									null);
//							if (image != null) {
//								ByteArrayOutputStream stream = new ByteArrayOutputStream();
//								image.compressToJpeg(new Rect(0, 0, size.width,
//										size.height), 80, stream);
//								Bitmap bmp = BitmapFactory.decodeByteArray(
//										stream.toByteArray(), 0, stream.size());
//
//								stream.close();
//
//								File photo = new File(Environment
//										.getExternalStorageDirectory(),
//										"photo.png");
//
//								if (photo.exists()) {
//									photo.delete();
//								}
//
//								FileOutputStream fos = new FileOutputStream(
//										photo.getPath());
//								bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
//								fos.close();
//							}
//						} catch (Exception ex) {
//							throw new RuntimeException(ex);
//						}
					}
				});
			}
		}, 1000 * 3);

		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		camera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.setPreviewCallback(null);
		camera.stopPreview();
		camera.release();
		camera = null;
	}

}
