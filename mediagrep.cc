/* Libs */
#include "mediagrep.h"
#include "transcribe.h"

using namespace std;

// hardcoded values, TODO: remove, make configurable or input
const char* INPUT_WAV = "./test_files/CharlesManson.mkv";
const char* VOSK_MODEL = "./models/vosk-model-small-en-us-0.15";

int main(int argc, const char** argv) {
    // Parse arguments
    // * flags -> need global vars to store feature flags and options
    // * mode
    // * files to grep

    //// Normal mode -> Transcribe, Search, Print ////
    transcript_location cache_paths;
    if (transcribe(VOSK_MODEL, INPUT_WAV, &cache_paths) != 0) {
        // TODO handle error
    }


    // Search step
        // * Feed transcription outputs to searcher
        // * Search

    // Print step
        // Format search output and print

    return 0;
}
