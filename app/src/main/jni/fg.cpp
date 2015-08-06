#include <jni.h>
#include <vector>
#include <string>

// for gnuradio
#include <gnuradio/top_block.h>
#include <gnuradio/filter/firdes.h>
#include <gnuradio/filter/interp_fir_filter_fff.h>
#include <gnuradio/analog/frequency_modulator_fc.h>
#include <gnuradio/uhd/usrp_sink.h>
#include <grand/opensl_source.h>
//#include <osmosdr/sink.h>

JavaVM *vm;
gr::top_block_sptr tb;
gr::grand::opensl_source::sptr src;
gr::filter::interp_fir_filter_fff::sptr interp;
gr::analog::frequency_modulator_fc::sptr fm;
//osmosdr::sink::sptr snk;
gr::uhd::usrp_sink::sptr snk;

extern "C" {

JNIEXPORT void JNICALL
Java_org_gnuradio_grtxfm_RunGraph_FgInit(JNIEnv* env, jobject thiz,
                                         int fd, jstring devname)
{
  GR_INFO("fg", "FgInit Called");

  float samp_rate = 32e3;

  const char *c_devname = env->GetStringUTFChars(devname, NULL);
  std::stringstream args;
  args << "uhd,fd=" << fd << ",uspfs_path=" << c_devname;
  GR_INFO("fg", boost::str(boost::format("Using UHD args=%1%") % args.str()));

  uhd::stream_args_t stream_args;
  stream_args.cpu_format = "fc32";
  stream_args.otw_format = "sc16";

  int resamp = 10;
  std::vector<float> taps = gr::filter::firdes::low_pass_2(resamp, resamp*samp_rate, 10e3, 2e3, 60,
                                                           gr::filter::firdes::WIN_HANN);

  float max_dev = 100e3;
  float k = 2.0f * M_PI * max_dev / (resamp*samp_rate);

  tb = gr::make_top_block("fg");
  src = gr::grand::opensl_source::make(int(samp_rate));
  interp = gr::filter::interp_fir_filter_fff::make(resamp, taps);
  fm = gr::analog::frequency_modulator_fc::make(k);
  //snk = osmosdr::sink::make(args.str());
  snk = gr::uhd::usrp_sink::make(args.str(), stream_args);
  snk->set_samp_rate(resamp*samp_rate);

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
