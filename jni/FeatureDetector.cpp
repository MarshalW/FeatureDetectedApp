/*
 * FeatureDetector.cpp
 *
 *  Created on: 2013年9月12日
 *      Author: marshal
 */

#include <jni.h>
#include <iostream>

#include <opencv2/opencv.hpp>

#include "marshal_cv_FeatureDetector.h"

using namespace std;

JNIEXPORT jstring JNICALL Java_marshal_cv_FeatureDetector_getOpenCvVersion(
		JNIEnv* env, jobject) {
	CvPoint* P1 = new CvPoint();
	P1->x = 32;
	delete P1;

	char* pat = "hello";
	jclass strClass = (env)->FindClass("java/lang/String");
	jmethodID ctorID = (env)->GetMethodID(strClass, "<init>",
			"([BLjava/lang/String;)V");
	jbyteArray bytes = (env)->NewByteArray(strlen(pat));
	(env)->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte*) pat);
	jstring encoding = (env)->NewStringUTF("UTF8");
	return (jstring) (env)->NewObject(strClass, ctorID, bytes, encoding);
}

