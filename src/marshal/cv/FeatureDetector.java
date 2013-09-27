package marshal.cv;

public class FeatureDetector {
	public native String getOpenCvVersion();

	public native boolean isOpticalFlowMoved(byte[] frame, int width, int height);

	static {
		System.loadLibrary("FeatureDetector");
	}
}
