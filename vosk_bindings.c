#define CAML_NAME_SPACE
#include "include/vosk_api.h"
#include "include/jsmn.h"
#include <caml/mlvalues.h>
#include <caml/fail.h>
#include <string.h>
#include <stdio.h>

VoskModel *model;
int write_vosk_json_to_files(const char* vosk_json, const char* text_s, const char* timestamp_s, int total_chars_printed);

CAMLprim value make_model(value str_model) {
    model = vosk_model_new(String_val(str_model));
    return Val_int(0);
}

CAMLprim value delete_model(value int_v) {
    vosk_model_free(model);
    return Val_int(0);
}

CAMLprim value total_duration(value audio_file, value duration_file) {
    FILE* wavin = fopen(String_val(audio_file), "rb");
    fseek(wavin, 0, SEEK_END);
    int size = ftell(wavin);
    FILE *total_dur = fopen(String_val(duration_file), "w");
    fprintf(total_dur, "%d\n", size);
    fclose(total_dur);
    return Val_int(0);
}

CAMLprim value transcribe(value audio,
			  value transcript,
			  value timestamp,
			  value total_duration,
			  value current_duration) {

    FILE *wavin, *output;
    char buf[3200];
    int nread, final;

    VoskRecognizer *recognizer = vosk_recognizer_new(model, 16000.0);
    vosk_recognizer_set_words(recognizer, 1);
    
    wavin = fopen(String_val(audio), "rb");
    output = fopen(String_val(transcript), "w");

    FILE *current_dur = fopen(String_val(current_duration), "w");    

    fseek(wavin, 0, SEEK_SET);

    int chars_printed = 0;
    fseek(wavin, 44, SEEK_SET);
    while (!feof(wavin)) {

         nread = fread(buf, 1, sizeof(buf), wavin);
         final = vosk_recognizer_accept_waveform(recognizer, buf, nread);
         if (final) {
             chars_printed = write_vosk_json_to_files(vosk_recognizer_final_result(recognizer),
						      String_val(transcript),
						      String_val(timestamp),
						      chars_printed);
         } else {
             /* fprintf(output, "%s\n", vosk_recognizer_partial_result(recognizer)); */
         }

	 fseek(current_dur, 0, SEEK_SET);
	 fprintf(current_dur, "%ld\n", ftell(wavin));
    }
    /* fprintf(output, "%s\n", vosk_recognizer_final_result(recognizer)); */

    fseek(current_dur, 0, SEEK_SET);
    /* fprintf(current_dur, "%s\n", "DONE"); */
    
    vosk_recognizer_free(recognizer);

    
    fclose(wavin);
    fclose(current_dur);
    
    return Val_int(1);
}


/* Writes the json that vosk outputs to the cached files.
    Parses inputs formatted like this:
    {
      "result" : [{
          "conf" : 0.280911,
          "end" : 7.408000,
          "start" : 7.348000,
          "word" : "i"
        }, {
          "conf" : 0.280911,
          "end" : 7.557229,
          "start" : 7.408000,
          "word" : "think"
      }]
    }
*/

int jsoneq (const char* vosk_json, jsmntok_t* tokens, int tok_index, const char *s) {
    jsmntok_t tok = tokens[tok_index];
    if ((tok.type == JSMN_STRING)
        && ((int)strlen(s) == tok.end - tok.start)
        && (strncmp(vosk_json + tok.start, s, tok.end - tok.start) == 0)) {
	return 1;
    }
    return 0;
}

int write_vosk_json_to_files(const char* vosk_json, const char* text_s, const char* timestamp_s, int total_chars_printed) {
    const int VOSK_JSON_MAX_TOKENS = 1024;
    jsmntok_t tokens[VOSK_JSON_MAX_TOKENS];

    FILE *text = fopen(text_s, "a");
    FILE *timestamp = fopen(timestamp_s, "a");

    int tokens_read;
    {
        jsmn_parser parser;
        jsmn_init(&parser);
        tokens_read = jsmn_parse(&parser, vosk_json, strlen(vosk_json), tokens, VOSK_JSON_MAX_TOKENS);
        if ((tokens_read <= 0) || (tokens[0].type != JSMN_OBJECT)) {
	    printf("%s\n", "JSON PARSE FAILURE. EXITING");
	    exit(1);
        }
    }


    char conversion_buffer[16];  // small buffer to put strings from vosk into while converting them to timestamp format.


    for (int i = 1; i < tokens_read; i++) {
        /* SKIP IRRELEVANT FIELDS */
        if (jsoneq(vosk_json, tokens, i, "result")) {
            if (tokens[i + 1].type != JSMN_ARRAY) {
		printf("%s\n", "JSON PARSE FAILURE. EXITING");
		exit(1);
            }
            /* LOOP OVER WORD STRUCTS */
            for (int j = 0, obj = 0; obj < tokens[i + 1].size; j++) {
                if (tokens[i + 1 + j].type != JSMN_OBJECT) {
                    continue; // shouldn't happen
                }

                /* LOOP THROUGH STRUCT FIELDS */
                for (int k = 0, field = 0; field < tokens[i + 1 + j].size; k++) {
                    if (jsoneq(vosk_json, tokens, i + 1 + j + 1 + k, "word")) {
                        jsmntok_t* word_token = &tokens[i + 1 + j + 1 + k + 1];
			const char* WRITE_FORMAT = "%.*s ";

			char format_buf[500];
			snprintf(format_buf, 500, WRITE_FORMAT, word_token->end-word_token->start, vosk_json+word_token->start);
			
			fprintf(text, "%s", format_buf);
			total_chars_printed += strlen(format_buf); 
			/* printf("just printed||%s||, the total chars is now %d", format_buf, total_chars_printed); */
                    }

                    if (jsoneq(vosk_json, tokens, i + 1 + j + 1 + k, "start")) {
                        jsmntok_t* start_timestamp_token = &tokens[i + 1 + j + 1 + k + 1];

			    /* SLICE OUT NUMBER FROM VOSK OUTPUT */
			    strncpy(conversion_buffer, vosk_json+start_timestamp_token->start, start_timestamp_token->end-start_timestamp_token->start);
			    conversion_buffer[start_timestamp_token->end-start_timestamp_token->start] = '\0';

			    /* CONSTANTS FOR TIME UNITS */
			    const int SECOND = 1;
			    const int MINUTE = 60*SECOND;
			    const int HOUR = 60*MINUTE;

			    /* GET TIME UNITS FROM JSON STRING */
			    float raw = atof(conversion_buffer);
			    int total_seconds = (int)raw; if (total_seconds > raw) total_seconds--; // fast replacement for floor()

			    
			    int hours = total_seconds / HOUR;
			    total_seconds = total_seconds % HOUR;

			    int minutes = total_seconds / MINUTE;
			    total_seconds = total_seconds % MINUTE;
			    
			    int seconds = total_seconds;
			    
			    /* FORMAT AS TIMESTAMP */
			    if (hours > 0) {
				fprintf(timestamp, "[%.2i:%.2i:%.2i](%d)", hours, minutes, seconds, total_chars_printed);
			    } else {
				fprintf(timestamp, "[%.2i:%.2i](%d)", minutes, seconds, total_chars_printed);
			    }
                    }

                    /* ignoring end time and confidence values for now... */

                    field++;
                    k++; // skip
                }
                obj++;
                j += tokens[i + 1 + j].size;
            }
            break; // break if we already looped over results;
        }
    }

    fclose(text);
    fclose(timestamp);
    return total_chars_printed;
}
