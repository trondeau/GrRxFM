#include <jni.h>
#include <vector>
#include <string>

// for gnuradio
#include <gnuradio/top_block.h>
#include <gnuradio/filter/firdes.h>
#include <gnuradio/uhd/usrp_source.h>
#include <gnuradio/filter/fir_filter_ccf.h>
#include <gnuradio/filter/fir_filter_fff.h>
#include <gnuradio/analog/quadrature_demod_cf.h>
#include <gnuradio/blocks/ctrlport_probe2_f.h>
#include <grand/opensl_sink.h>
//#include <osmosdr/source.h>
#include <gnuradio/analog/sig_source_f.h>

JavaVM *vm;
gr::top_block_sptr tb;
//osmosdr::source::sptr src;
gr::uhd::usrp_source::sptr src;
gr::filter::fir_filter_ccf::sptr chan_filt;
gr::filter::fir_filter_fff::sptr audio_filt;
gr::analog::quadrature_demod_cf::sptr demod;
gr::blocks::ctrlport_probe2_f::sptr vsnk;
gr::grand::opensl_sink::sptr snk;
gr::analog::sig_source_f::sptr sig;

extern "C" {

JNIEXPORT void JNICALL
Java_org_gnuradio_grrxfm_RunGraph_FgInit(JNIEnv* env, jobject thiz,
                                         int fd, jstring devname)
{
  GR_INFO("fg", "FgInit Called");

  float samp_rate = 320e3;

  const char *usbfs_path = env->GetStringUTFChars(devname, NULL);
  std::stringstream args;
  args << "uhd,fd=" << fd << ",usbfs_path=" << usbfs_path;
  GR_INFO("fg", boost::str(boost::format("Using UHD args=%1%") % args.str()));

  uhd::stream_args_t stream_args;
  stream_args.cpu_format = "fc32";
  stream_args.otw_format = "sc16";

  std::vector<float> chan_taps = gr::filter::firdes::low_pass_2(1, samp_rate, 150e3, 50e3, 60,
                                                                gr::filter::firdes::WIN_HANN);
  int resamp = 10;
  std::vector<float> audio_taps = gr::filter::firdes::low_pass_2(1, samp_rate, 10e3, 2e3, 60,
                                                                 gr::filter::firdes::WIN_HANN);

  float max_dev = 75e3;
  float fm_demod_gain = samp_rate / (2.0f * M_PI * max_dev);
  float audio_rate = samp_rate / resamp;

  tb = gr::make_top_block("fg");
  src = gr::uhd::usrp_source::make(args.str(), stream_args);
  chan_filt = gr::filter::fir_filter_ccf::make(1, chan_taps);
  demod = gr::analog::quadrature_demod_cf::make(fm_demod_gain);
  audio_filt = gr::filter::fir_filter_fff::make(resamp, audio_taps);
  vsnk = gr::blocks::ctrlport_probe2_f::make("rxdata", "Received FM Signal", 2048, 0);

  snk = gr::grand::opensl_sink::make(int(audio_rate));
  //snk = osmosdr::sink::make(args.str());

  src->set_samp_rate(samp_rate);

  tb->connect(src, 0, chan_filt, 0);
  tb->connect(chan_filt, 0, demod, 0);
  tb->connect(demod, 0, audio_filt, 0);
  tb->connect(audio_filt, 0, snk, 0);

  //tb->connect(audio_filt, 0, snk, 0);
  sig = gr::analog::sig_source_f::make(audio_rate, gr::analog::GR_SIN_WAVE, 400, 0.5);
  tb->connect(sig, 0, vsnk, 0);
}

JNIEXPORT void JNICALL
Java_org_gnuradio_grrxfm_RunGraph_FgStart(JNIEnv* env, jobject thiz)
{
  GR_INFO("fg", "FgStart Called");
  tb->start();
}

JNIEXPORT void JNICALL
Java_org_gnuradio_grrxfm_RunGraph_FgStop(JNIEnv* env, jobject thiz)
{
  GR_INFO("fg", "FgStop Called");
  tb->stop();
  tb->wait();
  GR_INFO("fg", "FgStop Exited");
}

JNIEXPORT jstring JNICALL
Java_org_gnuradio_grrxfm_RunGraph_FgRep(JNIEnv* env, jobject thiz)
{
  return env->NewStringUTF(tb->edge_list().c_str());
}

}
