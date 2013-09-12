package marshal.cv;

public class FeatureDetector {
	public native String getOpenCvVersion();
	
	static{
		System.loadLibrary("FeatureDetector");
	}
}
