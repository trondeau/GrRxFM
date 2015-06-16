LOCAL_PATH := $(call my-dir)

#Build module for static library

GRLIBPATH := /opt/grandroid/lib

######################################################################
#   REQUIRED BOOST LIBRARIES
######################################################################

include $(CLEAR_VARS)
LOCAL_MODULE                        := libboost_exception_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libboost_exception.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libboost_system_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libboost_system.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libboost_filesystem_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libboost_filesystem.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libboost_serialization_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libboost_serialization.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libboost_thread_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libboost_thread.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libboost_program_options_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libboost_program_options.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libboost_date_time_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libboost_date_time.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libboost_chrono_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libboost_chrono.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libboost_atomic_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libboost_atomic.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libboost_regex_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libboost_regex.a
include $(PREBUILT_STATIC_LIBRARY)


######################################################################
#   FFTW
######################################################################

include $(CLEAR_VARS)
LOCAL_MODULE                        := libfftw3f_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libfftw3f.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libfftw3f_threads_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libfftw3f_threads.a
include $(PREBUILT_STATIC_LIBRARY)



######################################################################
#   ControlPort support (thrift)
######################################################################

include $(CLEAR_VARS)
LOCAL_MODULE                        := libthrift_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libthrift.a
include $(PREBUILT_STATIC_LIBRARY)

#include $(CLEAR_VARS)
#LOCAL_MODULE                        := libcrypto_static_lib
#LOCAL_SRC_FILES                     := $(GRLIBPATH)/libcrypto_threads.a
#include $(PREBUILT_STATIC_LIBRARY)
#
#include $(CLEAR_VARS)
#LOCAL_MODULE                        := libssl_static_lib
#LOCAL_SRC_FILES                     := $(GRLIBPATH)/libssl_threads.a
#include $(PREBUILT_STATIC_LIBRARY)



######################################################################
#   RTL-SDR, UHD and required LIBUSB
######################################################################

LIBPATH := /opt/grandroid/lib
include $(CLEAR_VARS)
LOCAL_MODULE                        := libusb_shared_lib
LOCAL_SRC_FILES                     := $(LIBPATH)/libusb1.0.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := librtlsdr_static_lib
LOCAL_SRC_FILES                     := $(LIBPATH)/librtlsdr.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libuhd_static_lib
LOCAL_SRC_FILES                     := $(LIBPATH)/libuhd.a
include $(PREBUILT_STATIC_LIBRARY)


######################################################################
#   GR OSMOSDR
######################################################################

include $(CLEAR_VARS)
LOCAL_MODULE                        := libgr_osmosdr_static_lib
LOCAL_SRC_FILES                     := $(LIBPATH)/libgnuradio-osmosdr.a
include $(PREBUILT_STATIC_LIBRARY)



######################################################################
#   GNU Radio Libraries
######################################################################

include $(CLEAR_VARS)
LOCAL_MODULE                        := libvolk_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libvolk.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libgr_pmt_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libgnuradio-pmt.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libgr_runtime_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libgnuradio-runtime.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libgr_blocks_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libgnuradio-blocks.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libgr_fft_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libgnuradio-fft.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libgr_filter_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libgnuradio-filter.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libgr_analog_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libgnuradio-analog.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libgr_digital_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libgnuradio-digital.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libgr_uhd_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libgnuradio-uhd.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE                        := libgrand_static_lib
LOCAL_SRC_FILES                     := $(GRLIBPATH)/libgnuradio-grand.a
include $(PREBUILT_STATIC_LIBRARY)


######################################################################
#  DEFINE THE LIBRARIES TO PULL INTO THE APPLICATION
######################################################################

GR_WHOLE_STATIC_LIBRARIES += uhd_static_lib
GR_STATIC_LIBRARIES := grand_static_lib
GR_STATIC_LIBRARIES += gr_digital_static_lib
GR_STATIC_LIBRARIES += gr_analog_static_lib
GR_STATIC_LIBRARIES += gr_filter_static_lib
GR_STATIC_LIBRARIES += gr_fft_static_lib
GR_STATIC_LIBRARIES += gr_osmosdr_static_lib
GR_STATIC_LIBRARIES += gr_uhd_static_lib
GR_STATIC_LIBRARIES += gr_blocks_static_lib
GR_STATIC_LIBRARIES += gr_runtime_static_lib
GR_STATIC_LIBRARIES += gr_pmt_static_lib
GR_STATIC_LIBRARIES += volk_static_lib
GR_STATIC_LIBRARIES += libthrift_static_lib
GR_STATIC_LIBRARIES += fftw3f_static_lib
GR_STATIC_LIBRARIES += fftw3f_threads_static_lib
GR_STATIC_LIBRARIES += boost_exception_static_lib
GR_STATIC_LIBRARIES += boost_chrono_static_lib
GR_STATIC_LIBRARIES += boost_date_time_static_lib
GR_STATIC_LIBRARIES += boost_program_options_static_lib
GR_STATIC_LIBRARIES += boost_filesystem_static_lib
GR_STATIC_LIBRARIES += boost_serialization_static_lib
GR_STATIC_LIBRARIES += boost_regex_static_lib
GR_STATIC_LIBRARIES += boost_system_static_lib
GR_STATIC_LIBRARIES += boost_thread_static_lib
GR_STATIC_LIBRARIES += boost_atomic_static_lib
GR_STATIC_LIBRARIES += rtlsdr_static_lib
GR_SHARED_LIBRARIES += usb_shared_lib
