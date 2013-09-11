package marshal.cv;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity implements SurfaceHolder.Callback {

	SurfaceView surfaceView;

	Camera camera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		this.surfaceView.getHolder().setKeepScreenOn(true);
		this.surfaceView.getHolder().addCallback(this);
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
		camera.setDisplayOrientation(90);

		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Camera.Parameters params = camera.getParameters();

		// 处理预览图片长宽比，这里固定写法仅为支持Galaxy S4
		params.setPictureSize(1280, 720);

		// 处理自动对焦参数
		List<String> focusModes = params.getSupportedFocusModes();

		String CAF_PICTURE = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, CAF_VIDEO = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO, supportedMode = focusModes
				.contains(CAF_PICTURE) ? CAF_PICTURE : focusModes
				.contains(CAF_VIDEO) ? CAF_VIDEO : "";

		if (!supportedMode.equals("")) {
			params.setFocusMode(supportedMode);
		}

		camera.setParameters(params);
		camera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
        camera.release();
        camera = null;
	}

}
