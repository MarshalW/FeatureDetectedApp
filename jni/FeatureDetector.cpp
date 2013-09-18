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

JNIEXPORT void JNICALL Java_marshal_cv_FeatureDetector_putCameraPreview
  (JNIEnv * env, jobject thiz, jbyteArray data, jint width, jint height){
	jbyte* yuv=env->GetByteArrayElements(data,0);
	Mat frame(height, width, CV_8UC1, (unsigned char *)yuv);

	FastFeatureDetector fast(40);
	vector<KeyPoint> v;

	clock_t now = clock();
	fast.detect(frame,v);

	stringstream strm;
	strm << "ORB+FLANN耗时（毫秒）：" << (clock() - now) / 1000;
	LOGI(strm.str().c_str());

	strm.clear();
	strm.str("");

	strm<<"vector.size: "<<v.size();


	LOGI(strm.str().c_str());

	env->ReleaseByteArrayElements(data, yuv, 0);
}

