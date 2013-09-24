package marshal.cv;

public class FeatureDetector {
	public native String getOpenCvVersion();

	public native void putCameraPreview(byte[] currFrame,byte[] prevFrame, int width, int height);

	static {
		System.loadLibrary("FeatureDetector");
	}
}
