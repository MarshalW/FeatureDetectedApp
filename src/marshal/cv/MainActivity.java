package marshal.cv;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

public class MainActivity extends Activity implements SurfaceHolder.Callback {

	String[] states = { "停止..", "活动.." };

	int moveCount;

	SurfaceView surfaceView;

	Camera camera;

	FeatureDetector featureDetector;

	LooperThread looperThread;

	boolean isMoved;

	TextView textView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		// this.surfaceView.getHolder().setKeepScreenOn(true);
		this.surfaceView.getHolder().addCallback(this);

		textView = (TextView) findViewById(R.id.moveState);

		featureDetector = new FeatureDetector();
		Log.d("feature_detector",
				">>>>>>>>>> on create, " + featureDetector.getOpenCvVersion());
	}

	@Override
	protected void onResume() {
		super.onResume();
		looperThread = new LooperThread();
		looperThread.start();
		Log.d("feature_detector", ">>>>>>>>>>on resume");

	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("feature_detector", ">>>>>>>>>>on pause");
		looperThread.mHandler.post(new Runnable() {
			@Override
			public void run() {
				Looper.myLooper().quit();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d("feature_detector", ">>>>>>>>>>on start");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d("feature_detector", ">>>>>>>>>>on stop");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (width < height) {// 如果不是横屏情况下，啥也不做
			return;
		}
		Log.d("feature_detector", ">>>>>>surface changed ..");
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
				checkMoved();
			}
		}, 3 * 1000);

		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		camera.startPreview();
	}

	private void setMoved(boolean moved) {
		this.isMoved = moved;

		if (!this.isMoved) {
			moveCount++;
			if (moveCount > 5) {
				textView.setText(states[0]);
			}
		} else {
			moveCount = 0;
			textView.setText(states[1]);
		}

		checkMoved();
	}

	private void checkMoved() {
		looperThread.mHandler.post(new Runnable() {

			@Override
			public void run() {
				Log.d("feature_detector", ">>>>set on shot preview, thread: "
						+ Thread.currentThread());
				camera.setOneShotPreviewCallback(new PreviewCallback() {
					@Override
					public void onPreviewFrame(byte[] data, Camera camera) {
						Size size = camera.getParameters().getPreviewSize();
						setMoved(featureDetector.isOpticalFlowMoved(data,
								size.width, size.height));
						Log.d("feature_detector", ">>>>>>moved? " + isMoved
								+ ", w,h: " + size.width + ", " + size.height);
					}
				});
			}
		});
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("feature_detector", ">>>>>>surface created ..");
		camera = Camera.open();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("feature_detector", ">>>>>>surface destroyed ..");
		camera.setPreviewCallback(null);
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	class LooperThread extends Thread {
		public Handler mHandler;

		@Override
		public void run() {
			Looper.prepare();
			mHandler = new Handler();
			Looper.loop();
			Log.d("feature_detector", ">>>>>>thread quit.");
		}
	}

}
