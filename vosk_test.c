#define CAML_NAME_SPACE
#include "include/vosk_api.h"
#include <caml/mlvalues.h>
#include <caml/fail.h>
#include <stdio.h>

CAMLprim value callable_from_ocaml(value int_v) {
    vosk_set_log_level(Int_val(int_v));
    FILE *wavin;
    char buf[3200];
    int nread, final;

    VoskModel *model = vosk_model_new("/home/ooc/mp4grep/model");
    VoskRecognizer *recognizer = vosk_recognizer_new(model, 16000.0);

    wavin = fopen("harvard.wav", "rb");
    fseek(wavin, 44, SEEK_SET);
    while (!feof(wavin)) {
         nread = fread(buf, 1, sizeof(buf), wavin);
         final = vosk_recognizer_accept_waveform(recognizer, buf, nread);
         if (final) {
             printf("%s\n", vosk_recognizer_result(recognizer));
         } else {
             printf("%s\n", vosk_recognizer_partial_result(recognizer));
         }
    }
    printf("%s\n", vosk_recognizer_final_result(recognizer));

    vosk_recognizer_free(recognizer);
    vosk_model_free(model);
    fclose(wavin);

    printf("just set the vosk log level!\n");
    return Val_int(12);
}

/* int main() { */
/*     FILE *wavin; */
/*     char buf[3200]; */
/*     int nread, final; */

/*     VoskModel *model = vosk_model_new("/home/ooc/mp4grep/model"); */
/*     VoskRecognizer *recognizer = vosk_recognizer_new(model, 16000.0); */

/*     wavin = fopen("harvard.wav", "rb"); */
/*     fseek(wavin, 44, SEEK_SET); */
/*     while (!feof(wavin)) { */
/*          nread = fread(buf, 1, sizeof(buf), wavin); */
/*          final = vosk_recognizer_accept_waveform(recognizer, buf, nread); */
/*          if (final) { */
/*              printf("%s\n", vosk_recognizer_result(recognizer)); */
/*          } else { */
/*              printf("%s\n", vosk_recognizer_partial_result(recognizer)); */
/*          } */
/*     } */
/*     printf("%s\n", vosk_recognizer_final_result(recognizer)); */

/*     vosk_recognizer_free(recognizer); */
/*     vosk_model_free(model); */
/*     fclose(wavin); */
/*     return 0; */
/* } */

