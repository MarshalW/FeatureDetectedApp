LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    := FeatureDetector 
LOCAL_SRC_FILES := FeatureDetector.cpp
APP_STL:=stlport_static
include $(BUILD_SHARED_LIBRARY)