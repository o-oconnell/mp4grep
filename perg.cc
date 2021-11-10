/* Libs */
#include "perg.h"
#include "transcribe.h"

using namespace std;

// hardcoded values, TODO: remove, make configurable or input
const char* INPUT_WAV = "./test_files/OSR_us_000_0061_8k.wav";
const char* VOSK_MODEL = "./models/vosk-model-small-en-us-0.15";

int main(int argc, const char** argv) {
    // Parse arguments
    // * flags -> need global vars to store feature flags and options
    // * mode
    // * files to grip

    transcript_location cache_paths = {
        .text = "./bin/output/text",
        .timestamp  = "./bin/output/timestamps",
    };

    //// Normal mode -> Transcribe, Search, Print ////
    /* Transcription step */ {
        // * convert files to format vosk understands - ffmpeg

        // * transcribe files
        do_transcription(VOSK_MODEL, INPUT_WAV, cache_paths);
    }

    // Search step
        // * Feed transcription outputs to searcher
        // * Search

    // Print step
        // Format search output and print

    return 0;
}
