/*
 * FeatureDetector.cpp
 *
 *  Created on: 2013年9月12日
 *      Author: marshal
 */

#include <jni.h>
#include <iostream>

#include <opencv2/opencv.hpp>

#include <android/log.h>

#include "marshal_cv_FeatureDetector.h"

using namespace cv;
using namespace std;

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "feature_detector", __VA_ARGS__))

static vector<Point2f> trackPrevPoints, trackNewPoints;

//Takes a descriptor and turns it into an xy point
void keypoints2points(const vector<KeyPoint>& in, vector<Point2f>& out) {
	out.clear();
	out.reserve(in.size());
	for (size_t i = 0; i < in.size(); ++i) {
		out.push_back(in[i].pt);
	}
}

void printWord(string s) {
	stringstream strm;
	strm << ">>>>>>print word: " << s;
	LOGI(strm.str().c_str());
}

//这部分是假的，并不真从OpenCV获取版本信息，仅用于验证java和cpp集成是否正确
JNIEXPORT jstring JNICALL Java_marshal_cv_FeatureDetector_getOpenCvVersion(
		JNIEnv* env, jobject thiz) {
	CvPoint* P1 = new CvPoint();
	P1->x = 32;
	delete P1;

	char* pat = "v2.4.6";
	jclass strClass = (env)->FindClass("java/lang/String");
	jmethodID ctorID = (env)->GetMethodID(strClass, "<init>",
			"([BLjava/lang/String;)V");
	jbyteArray bytes = (env)->NewByteArray(strlen(pat));
	(env)->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte*) pat);
	jstring encoding = (env)->NewStringUTF("UTF8");
	return (jstring) (env)->NewObject(strClass, ctorID, bytes, encoding);
}

JNIEXPORT void JNICALL Java_marshal_cv_FeatureDetector_putCameraPreview(
		JNIEnv * env, jobject thiz, jbyteArray currFrame, jbyteArray prevFrame,
		jint width, jint height) {
	jbyte* yuv = env->GetByteArrayElements(currFrame, 0);
	Mat frame(height, width, CV_8UC1, (unsigned char *) yuv);

	jbyte* yuv2 = env->GetByteArrayElements(prevFrame, 0);
	Mat frame2(height, width, CV_8UC1, (unsigned char *) yuv2);

	/*
	 //使用GoodFeaturesToTrack
	 clock_t now = clock();
	 vector<Point2f> corners;
	 goodFeaturesToTrack(frame, corners, 40, 0.001, 10);

	 stringstream strm;
	 strm << "GoodFeatureToTrack 耗时（毫秒）：" << (clock() - now) / 1000;
	 LOGI(strm.str().c_str());

	 strm.clear();
	 strm.str("");
	 strm << "vector.size: " << corners.size();
	 LOGI(strm.str().c_str());
	 */

	//使用FAST算法获取角点
	FastFeatureDetector fast(150);
	vector<KeyPoint> v;

	clock_t now = clock();
	fast.detect(frame, v);

	stringstream strm;
	strm << "FAST耗时（毫秒）：" << (clock() - now) / 1000;
	LOGI(strm.str().c_str());

	strm.clear();
	strm.str("");
	strm << "vector.size: " << v.size();
	LOGI(strm.str().c_str());

	keypoints2points(v, trackPrevPoints);

	vector<uchar> status;
	vector<float> err;
	Size winSize(20, 20);
	TermCriteria termcrit(CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 10, 0.1);
	Point2f newCenter(0, 0), prevCenter(0, 0), currShift;

	calcOpticalFlowPyrLK(frame, frame2, trackPrevPoints, trackNewPoints, status,
			err, winSize, 3, termcrit, 0);

	strm.clear();
	strm.str("");
	strm << "new vector.size: " << trackNewPoints.size();
	LOGI(strm.str().c_str());

	strm.clear();
	strm.str("");
	strm << "calcOpticalFlowPyrLK耗时: " << (clock() - now) / 1000;
	LOGI(strm.str().c_str());

	size_t i, k;
	for (i = k = 0; i < trackNewPoints.size(); i++) {
		if (!status[i])
			continue;

		prevCenter += trackPrevPoints[i];
		newCenter += trackNewPoints[i];
		trackNewPoints[k] = trackNewPoints[i];
		k++;
	}
	trackNewPoints.resize(k);

	strm.clear();
	strm.str("");
	strm << ">>>>>>>>k: " << k << " -- prevCenter: "
			<< prevCenter * (1.0 / (float) k) << " || " << " newCenter: "
			<< newCenter * (1.0 / (float) k) << " || 平均偏移量："
			<< (newCenter * (1.0 / (float) k) - prevCenter * (1.0 / (float) k));
	LOGI(strm.str().c_str());

	string s = "你好";
	string* a = &s;
	printWord(*a);

	//测试一下对point对象运算符重载（减号）
	Point2f p1(0, 0), p2(1, 1), p3;
	p3 = p2 - p1;
	strm.clear();
	strm.str("");
	strm << ">>>>>>>>p3: " << p3;
	LOGI(strm.str().c_str());

	//使用完毕要释放内存，照着做的，什么机制？
	env->ReleaseByteArrayElements(currFrame, yuv, 0);
	env->ReleaseByteArrayElements(prevFrame, yuv2, 0);
}

