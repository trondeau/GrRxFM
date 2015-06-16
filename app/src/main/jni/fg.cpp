#include <jni.h>
#include <vector>
#include <string>

// for gnuradio
#include <gnuradio/top_block.h>
#include <gnuradio/filter/firdes.h>
#include <gnuradio/filter/interp_fir_filter_fff.h>
#include <gnuradio/analog/frequency_modulator_fc.h>
#include <grand/opensl_source.h>
#include <osmosdr/sink.h>

JavaVM *vm;
gr::top_block_sptr tb;
gr::grand::opensl_source::sptr src;
gr::filter::interp_fir_filter_fff::sptr interp;
gr::analog::frequency_modulator_fc::sptr fm;
osmosdr::sink::sptr snk;

extern "C" {

JNIEXPORT void JNICALL
Java_org_gnuradio_grtxfm_RunGraph_FgInit(JNIEnv* env, jobject thiz,
                                         int fd, jstring devname)
{
  GR_INFO("fg", "FgInit Called");

  float samp_rate = 32e3;
  float fc = 444e6;
  float gain = 60;

  const char *c_devname = env->GetStringUTFChars(devname, NULL);
  std::stringstream args;
  args << "uhd,fd=" << fd << ",uspfs_path=" << c_devname;
  GR_INFO("fg", boost::str(boost::format("Using UHD args=%1%") % args.str()));

  int resamp = 10;
  std::vector<float> taps = gr::filter::firdes::low_pass_2(resamp, resamp*samp_rate, 10e3, 2e3, 60,
                                                           gr::filter::firdes::WIN_HANN);

  float max_dev = 5e3;
  float k = 2.0f * M_PI * max_dev / (resamp*samp_rate);

  tb = gr::make_top_block("fg");
  src = gr::grand::opensl_source::make(int(samp_rate));
  interp = gr::filter::interp_fir_filter_fff::make(resamp, taps);
  fm = gr::analog::frequency_modulator_fc::make(k);
  snk = osmosdr::sink::make(args.str());
  snk->set_sample_rate(resamp*samp_rate);
  snk->set_center_freq(fc);
  snk->set_gain(gain);

  tb->connect(src, 0, interp, 0);
  tb->connect(interp, 0, fm, 0);
  tb->connect(fm, 0, snk, 0);
}

JNIEXPORT void JNICALL
Java_org_gnuradio_grtxfm_RunGraph_FgStart(JNIEnv* env, jobject thiz)
{
  GR_INFO("fg", "FgStart Called");
  tb->start();
}

JNIEXPORT void JNICALL
Java_org_gnuradio_grtxfm_RunGraph_FgStop(JNIEnv* env, jobject thiz)
{
  GR_INFO("fg", "FgStop Called");
  tb->stop();
  tb->wait();
  GR_INFO("fg", "FgStop Exited");
}

JNIEXPORT jstring JNICALL
Java_org_gnuradio_grtxfm_RunGraph_FgRep(JNIEnv* env, jobject thiz)
{
  return env->NewStringUTF(tb->edge_list().c_str());
}

}
