#define CAML_NAME_SPACE
#include "include/vosk_api.h"
#include <caml/mlvalues.h>
#include <caml/fail.h>
#include <stdio.h>

VoskModel *model;
CAMLprim value make_model(value str_model) {
    printf("The model passed was %s\n", String_val(str_model));
    model = vosk_model_new(String_val(str_model));
    return Val_int(0);
}

CAMLprim value delete_model(value int_v) {
    vosk_model_free(model);
    return Val_int(0);
}

CAMLprim value transcribe(value audio,
			  value transcript,
			  value total_duration,
			  value current_duration) {

    FILE *wavin, *output;
    char buf[3200];
    int nread, final;

    VoskRecognizer *recognizer = vosk_recognizer_new(model, 16000.0);
    
    wavin = fopen(String_val(audio), "rb");
    output = fopen(String_val(transcript), "w");

    fseek(wavin, 0, SEEK_END);
    int size = ftell(wavin);
    FILE *total_dur = fopen(String_val(total_duration), "w");
    fprintf(total_dur, "%d\n", size);
    fclose(total_dur);

    FILE *current_dur = fopen(String_val(current_duration), "w");

    fseek(wavin, 0, SEEK_SET);

    fseek(wavin, 44, SEEK_SET);
    while (!feof(wavin)) {
	
         nread = fread(buf, 1, sizeof(buf), wavin);
         final = vosk_recognizer_accept_waveform(recognizer, buf, nread);
         if (final) {
             fprintf(output, "%s\n", vosk_recognizer_result(recognizer));
         } else {
             fprintf(output, "%s\n", vosk_recognizer_partial_result(recognizer));
         }

	 fseek(current_dur, 0, SEEK_SET);
	 fprintf(current_dur, "%ld\n", ftell(wavin));
    }
    fprintf(output, "%s\n", vosk_recognizer_final_result(recognizer));

    fseek(current_dur, 0, SEEK_SET);
    fprintf(current_dur, "%s\n", "DONE");
    
    vosk_recognizer_free(recognizer);

    fclose(wavin);
    fclose(output);
    fclose(current_dur);
    
    return Val_int(1);
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

