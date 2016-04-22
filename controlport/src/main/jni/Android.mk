LOCAL_PATH := $(call my-dir)

GRLIBPATH := /opt/grandroid/lib

include $(CLEAR_VARS)
LOCAL_MODULE                        := libboost_system_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libboost_system.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libgr_pmt_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libgnuradio-pmt.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
NDK_TOOLCHAIN_VERSION := 4.8
LOCAL_MODULE      := pmtjni
LOCAL_CPP_FEATURES += rtti
LOCAL_CPP_FEATURES += exceptions
LOCAL_C_INCLUDES  := /opt/ndk/sources/gnu-libstdc++/system/include
LOCAL_C_INCLUDES  += /opt/ndk/sources/gnu-libstdc++/stlport/stlport
LOCAL_C_INCLUDES  += /opt/grandroid/include
LOCAL_LDLIBS      := -L/opt/ndk/platforms/android-21/arch-arm/usr/lib
LOCAL_LDLIBS      += -llog
LOCAL_LDLIBS      += -landroid
LOCAL_SRC_FILES   := pmtjni.cpp
LOCAL_C_INCLUDES  += $(LIBUSB_ROOT_ABS)
LOCAL_STATIC_LIBRARIES += gr_pmt_static_lib
LOCAL_STATIC_LIBRARIES += boost_system_static_lib

include $(BUILD_SHARED_LIBRARY)
