#include <jni.h>
#include <cstdlib>

extern "C" {

JNIEXPORT void JNICALL
Java_org_gnuradio_grtxfm_GrTxFM_SetTMP(JNIEnv* env,
                                       jobject thiz,
                                       jstring tmpname)
{
  const char *tmp_c;
  tmp_c = env->GetStringUTFChars(tmpname, NULL);
  setenv("TMP", tmp_c, 1);
}

}
