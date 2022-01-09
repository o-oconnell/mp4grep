/* Libs */
#include "mediagrep.h"
#include "transcribe.h"
#include "include/termcolor.hpp"
#include "threading.cc"
#include <dirent.h>
#include <stdlib.h>
#include <string.h>
#include <map>
#include <unordered_map>
#include <unordered_set>
#include <string>
#include <fstream>
#include <algorithm>
#include <vector>
#include <set>
#include <iostream>
#include <sstream>
#include <thread>
#include <chrono>
#include <queue>
#include <functional>

using namespace std;

struct parsed_args {
    unordered_map<string, string> args;
    unordered_set<string> flags;
    vector<string> params;
};

const char* TRANSCRIBE_CACHE_DIRECTORY_MG = ((getenv("MEDIAGREP_CACHE")) ? (getenv("MEDIAGREP_CACHE")) : (".cache/"));
const string DEFAULT_MODEL = getenv("MEDIAGREP_MODEL") ? getenv("MEDIAGREP_MODEL") : "model";
const vector<string> AUDIO_TYPES{"mp3", "mp4", "ogg", "webm", "mov", "wav"};

/* SUBROUTINES */
void parse_args(int argc, const char** argv, struct parsed_args& pargs);
void search(int argc, const char** argv);
void transcribe_only(struct workflow& this_workflow, transcript_location cache_paths, string filename);
void transcribe_to_files(struct workflow& this_workflow, transcript_location cache_paths, string filename);
vector<string> get_audio_files(vector<string>& files);
int file_is_valid(const string& filename);
void print_results(struct workflow& this_workflow, transcript_location cache_paths, string filename);
void handle_parsed_params(struct workflow& this_workflow);
void extract_files_from_directories(struct workflow& this_workflow);
void transcribe_all_files(struct workflow& this_workflow);
void search(struct workflow& this_workflow, transcript_location cache_paths, string filename);
int clear_cache();

enum workflow_v { TRANSCRIBE_ONLY, TRANSCRIBE_TO_FILES, SEARCH };

struct workflow {
    struct t_only {
	parsed_args pargs {
	    .args = {{ "--words", "5"},
		     { "--model", DEFAULT_MODEL}},
	    .flags = {"--transcribe"}};
    } transcribe_only;

    struct ttf {
	parsed_args pargs {
	    .args = {{ "--words", "5"},
		     { "--model", DEFAULT_MODEL}},
	    .flags = {"--transcribe-to-files"}};
    } transcribe_to_files;

    struct srch {
	parsed_args pargs {
	    .args = {{ "--before", "5"},
		     { "--after", "1"},
		     { "--model", DEFAULT_MODEL},
	    }};
	string query;
    } search;

    string model;
    vector<string> filenames;
    workflow_v workflow_variant;
};

int main(int argc, const char** argv) {
    auto option_exists = [&](const std::string& option) {
	return std::find(argv, argv + argc, option) != (argv + argc);
    };
    
    struct workflow this_workflow;
    
    /* SET THE WORKFLOW */
    if (option_exists("--help")) {
	printf("Help message goes here\n");
	exit(0);
    } else if (option_exists("--clear-cache")) {
	printf("Clearing cache\n");
	clear_cache();
	exit(0);
    } else if (option_exists("--transcribe")) { 
	this_workflow.workflow_variant = TRANSCRIBE_ONLY;
	parse_args(argc, argv, this_workflow.transcribe_only.pargs);
    } else if (option_exists("--transcribe-to-files")) {
	this_workflow.workflow_variant = TRANSCRIBE_TO_FILES;
	parse_args(argc, argv, this_workflow.transcribe_to_files.pargs);
    } else {
	this_workflow.workflow_variant = SEARCH;
	parse_args(argc, argv, this_workflow.search.pargs);
    }

    // handles the params (not preceded by any options and not flags).
    // for search: puts first param as search string, remaining as files
    // for all others: puts all params as files
    handle_parsed_params(this_workflow);
    
    // replaces any params that are directories with the contained audio files
    // does error handling for invalid audio files
    extract_files_from_directories(this_workflow);
    
    // transcribes everything and searches/prints each result when done transcribing
    transcribe_all_files(this_workflow);
    
    return 0;
}

/* Make directory for cache if it doesnt exist. */
int clear_cache() {
    const char* clear_cache_command_template = "rm -ir %s";
    char command[16+PATH_LENGTH];
    sprintf(command, clear_cache_command_template, TRANSCRIBE_CACHE_DIRECTORY_MG);
    return system(command);
}

void handle_parsed_params(struct workflow& this_workflow) {
    switch (this_workflow.workflow_variant) {
    case(TRANSCRIBE_ONLY):
	/* MUST HAVE AT LEAST ONE FILE TO TRANSCRIBE */
	if (this_workflow.transcribe_only.pargs.params.empty()) {
	    printf("No files provided.\n");
	    exit(0);
	}
	this_workflow.filenames = this_workflow.transcribe_only.pargs.params;
	this_workflow.model = this_workflow.transcribe_only.pargs.args["--model"];
	break;
    case(TRANSCRIBE_TO_FILES):
	/* MUST HAVE AT LEAST ONE FILE TO TRANSCRIBE */
	if (this_workflow.transcribe_to_files.pargs.params.empty()) {
	    printf("No files provided.\n");
	    exit(0);
	}
	this_workflow.model = this_workflow.transcribe_only.pargs.args["--model"];
	this_workflow.filenames = this_workflow.transcribe_to_files.pargs.params;
	break;
    case(SEARCH):
	/* MUST HAVE SEARCH STRING & AT LEAST ONE FILE */
	string search;
	vector<string> files;
	
	for (unsigned i = 0; i < this_workflow.search.pargs.params.size(); ++i) {
	    if (search.empty()) {
		search = this_workflow.search.pargs.params[i];
	    } else {
		files.push_back(this_workflow.search.pargs.params[i]);
	    }
	}
	
	if (search.empty()) {
	    printf("No search string provided. Exiting.\n");
	    exit(0);
	}
	
	if (files.empty()) {
	    printf("No files to search provided. Exiting.\n");
	    exit(0);
	}
	
	this_workflow.search.query = search;
	this_workflow.model = this_workflow.search.pargs.args["--model"];
	this_workflow.filenames = files;
	break;
    }
}

void extract_files_from_directories(struct workflow& this_workflow) {
    this_workflow.filenames = get_audio_files(this_workflow.filenames);
}

void transcribe_all_files(struct workflow& this_workflow) {
    
    // spin up new thread for each filename in this_workflow.filenames
    vector<struct progress_bar_wrapper> wrappers(this_workflow.filenames.size());
    vector<struct transcript_location> locations;
    for (int i = 0; i < this_workflow.filenames.size(); ++i) {
	locations.push_back(transcript_location());
    }
    
    std::vector<const char*> filenames_chars;
    for (std::string const &str : this_workflow.filenames) {
	filenames_chars.push_back(str.c_str());
    }
    
    /* CREATE ALL OF THE THREADS */
    vector<thread> all_threads;
    std::unique_ptr<thread_pool> pool;

    unsigned const max_concurrency = std::thread::hardware_concurrency();
    unsigned const num_files = this_workflow.filenames.size();

    if (num_files >= max_concurrency) {
	pool = std::unique_ptr<thread_pool>(new thread_pool());
    }
    
    for (unsigned i = 0; i < this_workflow.filenames.size(); ++i) {
	
	/* GET THE TOTAL DURATION OF THE FILE */
	int duration = 0;
	{
	    std::string s(this_workflow.filenames[i]);
	    std::size_t last_slash = s.find_last_of("/");
	    
	    string duration_tmpfile = string(TRANSCRIBE_CACHE_DIRECTORY_MG)
		+ "/"
		+ (last_slash < (s.length() - 1) ? s.substr(last_slash + 1).c_str() : s.c_str())
		+ "_duration_tmp";
	    
	    string duration_str = "ffprobe -i "
		+ this_workflow.filenames[i]
		+" -show_entries format=duration -v quiet -of csv=\"p=0\" " + ">" + duration_tmpfile;
	    system(duration_str.c_str());

	    ifstream duration_file(duration_tmpfile);
	    string duration_line;
	    getline(duration_file, duration_line);

	    duration = atoi(duration_line.c_str());
	    duration_file.close();
	}

	wrappers[i].current = 0;
	wrappers[i].total = duration;
	wrappers[i].filename = this_workflow.filenames[i];
	wrappers[i].index_into_vector_transcript_location = i;
	
	if (num_files >= max_concurrency) {
	    std::function<void()> bound_transcribe =
		std::bind(transcribe,
			  std::cref(this_workflow.model),
			  std::cref(this_workflow.filenames[i]),
			  &locations[i],
			  &wrappers[i]);
	
	    pool->submit(bound_transcribe);
	} else {
	    all_threads.emplace_back(transcribe,
				     std::cref(this_workflow.model),
				     std::cref(this_workflow.filenames[i]),
				     &locations[i],
				     &wrappers[i]
				     );
	}
    }

    auto max_progress_first = [](progress_bar_wrapper* a, progress_bar_wrapper* b) {
	return ((double) a->current / a->total) < ((double) b->current / b->total);
    };

    std::priority_queue<progress_bar_wrapper*, vector<progress_bar_wrapper*>, decltype(max_progress_first)> pq(max_progress_first);

    for (auto &entry : wrappers) {
	pq.push(&entry);
    }

    while (!pq.empty()) {

	int MAX_PROGRESS_BAR_LEN = 25;
	if (pq.top()->current < pq.top()->total) {
	    int prog_bar_length = ((double) pq.top()->current / pq.top()->total) * MAX_PROGRESS_BAR_LEN;

	    printf("\x1B[0J");
	    printf("Transcribing in multiple threads... most completed file: %s\n", pq.top()->filename.c_str());
	    
	    printf("Progress: %.2f%%|", ((double) pq.top()->current / pq.top()->total) * 100);
	    std::cout << termcolor::yellow;
	    for (int i = 0; i < prog_bar_length; ++i) {
		printf("#");
	    }
	    std::cout << termcolor::reset;
	    
	    for (int i = 0; i < MAX_PROGRESS_BAR_LEN - prog_bar_length; ++i) {
		printf(" ");
	    }
	    printf("|");
	    
	    printf("\n");
	    printf("\r");
	    printf("\x1B[2A");
	    
	} else if (pq.top()->current == pq.top()->total) {
	    /* PRINT 100% progress bar or clear the line and then print the result */

	    // Clear the line
	    printf("\x1B[0J");
	    
	    print_results(this_workflow, locations[pq.top()->index_into_vector_transcript_location], pq.top()->filename);
	    
	    pq.pop();
	} else {
	    std::cout << "Error, progress bar current > progress bar max" << '\n';
	}

	std::this_thread::sleep_for(std::chrono::milliseconds(100));
    }

    if (num_files < max_concurrency) {
	for (auto &thrd : all_threads) {
	    thrd.join();
	}
    }
}

void parse_args(int argc, const char** argv, struct parsed_args& pargs) {
    vector<string> args(argv, argv + argc);
    set<int> used_args{0}; 
    unordered_map<string, string>& valid_args = pargs.args;
    const unordered_set<string>& valid_flags = pargs.flags;

    // Skip the first arg, the command.
    for (unsigned i = 1; i < args.size(); ++i) {

	/* VALID ARGUMENTS WITH VALUES */
	if (valid_flags.find(args[i]) == valid_flags.end() 
	    && valid_args.find(args[i]) != valid_args.end()
	    && i + 1 < args.size()
	    && used_args.find(i + 1) == used_args.end()) {
	    
	    valid_args[args[i]] = args[i + 1];

	    ++i; // SKIP THE VALUE
	}
	
	/* VALID ARGUMENTS WITHOUT VALUES */
	else if (valid_flags.find(args[i]) == valid_flags.end()
		 && valid_args.find(args[i]) != valid_args.end()
		 && (i + 1 >= args.size() || used_args.find(i + 1) != used_args.end())) {
	    printf("Missing value for argument %s\n", args[i].c_str());
	    exit(1);
	}

	/* VALID FLAGS */
	else if (valid_flags.find(args[i]) != valid_flags.end()) {
	    used_args.insert(i);
	}

	/* TREAT EVERYTHING ELSE AS A PARAM */ 
	else {
	    pargs.params.push_back(args[i]);
	}
    }
}

vector<string> get_audio_files(vector<string>& files) {
    
    vector<string> output;
    for (string file : files) {
	DIR *dir;
	struct dirent *ent;
	
	if ((dir = opendir(file.c_str())) != NULL) {
	    /* ADD EVERY AUDIO FILE IN DIRECTORY TO OUTPUT */
	    while ((ent = readdir(dir)) != NULL) {
		if (file_is_valid(string(ent->d_name))) {
		    output.push_back(string(ent->d_name));
		}
	    }
	    closedir(dir);
	} else if (file_is_valid(file)) {
	    output.push_back(file);
	} else {
	    printf("Invalid audio file: %s\n", file.c_str());
	}
    }
    return output;
}

int file_is_valid(const string& filename) {
    DIR *dir;

    /* DIRECTORIES ARE NOT VALID AUDIO FILES */
    if ((dir = opendir(filename.c_str())) != NULL) {
	return 0;
    }

    /* DETERMINE VALIDITY BASED ON FILE EXTENSION */
    istringstream iss(filename);
    vector<string> split_on_period;
    string tok;
    while (std::getline(iss, tok, '.')) {
	if (!tok.empty())
	    split_on_period.push_back(tok);
    }
	    
    for (string valid_ext : AUDIO_TYPES) {
	if (split_on_period[split_on_period.size() - 1] == valid_ext) {
	    return 1;
	}
    }
    return 0;
}

void print_results(struct workflow& this_workflow, transcript_location cache_paths, string filename) {
    switch (this_workflow.workflow_variant) {
    case(SEARCH):
	search(this_workflow, cache_paths, filename);
	break;
    case(TRANSCRIBE_ONLY):
	transcribe_only(this_workflow, cache_paths, filename);
	break;
    case(TRANSCRIBE_TO_FILES):
	transcribe_to_files(this_workflow, cache_paths, filename);
    default:
	break;
    }
    
}

void transcribe_to_files(struct workflow& this_workflow, transcript_location cache_paths, string filename) {
    /* SETUP TRANSCRIPT AND TIMESTAMP INPUT */
    ifstream text(cache_paths.text),  timestamp(cache_paths.timestamp);
    int word_number = 0, words_per_line = atoi(this_workflow.transcribe_to_files.pargs.args["--words"].c_str());
    string word_tmp, timestamp_tmp, line, line_timestamp;
    
    /* SETUP OUTPUT FILE */
    string output_file = filename + "_transcript.txt";
    ofstream outstream;
    outstream.open(output_file, ios_base::app);
    
    /* READ FROM TRANSCRIPTS AND TIMESTAMPS */
    while (getline(text, word_tmp) && getline(timestamp, timestamp_tmp)) {
	
	/* APPEND LINES WITH words_per_line WORDS TO FILE */
	if (word_number % words_per_line == 0 && word_number < words_per_line) {
	    line_timestamp = timestamp_tmp;
	} else if (word_number % words_per_line == 0 && word_number >= words_per_line) {
	    outstream << "[" << line_timestamp << "]:" << line << '\n';
	    line.clear();
	    line_timestamp = timestamp_tmp;
	}
	
	word_number++;
	line += word_tmp + ' ';
    }
    
    outstream << "[" << line_timestamp << "]:" << line << '\n';
    outstream.close();
    
    /* CLOSE TRANSCRIPT AND TIMESTAMP CACHE FILES */
    text.close();
    timestamp.close();
}

void transcribe_only(struct workflow& this_workflow, transcript_location cache_paths, string filename) {

    printf("Transcription of %s\n", filename.c_str());

    /* SETUP TRANSCRIPT AND TIMESTAMP INPUT */
    ifstream text(cache_paths.text),  timestamp(cache_paths.timestamp);
    int word_number = 0, words_per_line = atoi(this_workflow.transcribe_only.pargs.args["--words"].c_str());
    string word_tmp, timestamp_tmp, line, line_timestamp;
	
    /* READ FROM TRANSCRIPTS AND TIMESTAMPS */
    while (getline(text, word_tmp) && getline(timestamp, timestamp_tmp)) {

	/* PRINT LINES WITH words_per_line WORDS */
	if (word_number % words_per_line == 0 && word_number < words_per_line) {
	    line_timestamp = timestamp_tmp;
	} else if (word_number % words_per_line == 0 && word_number >= words_per_line) {
	    printf("[%s]:%s\n", line_timestamp.c_str(), line.c_str());
	    line.clear();
	    line_timestamp = timestamp_tmp;
	}

	word_number++;
	line += word_tmp + ' ';
    }

    printf("[%s]:%s\n", line_timestamp.c_str(), line.c_str());

    /* CLOSE TRANSCRIPT AND TIMESTAMP CACHE FILES */
    text.close();
    timestamp.close();

}

void search(struct workflow& this_workflow, transcript_location cache_paths, string filename) {
    /* SETUP TRANSCRIPT AND TIMESTAMP INPUT */
    ifstream text(cache_paths.text),  timestamp(cache_paths.timestamp);
    string transcript, line, timestampLine;
    map<int, string> indexToTimestamp;
	
    while (getline(text, line) && getline(timestamp, timestampLine)) {
	/* MAP STARTING INDEX OF EACH WORD TO A TIMESTAMP */
	indexToTimestamp[transcript.length()] = timestampLine;

	/* READ TRANSCRIPT INTO MEMORY */
	transcript += line + ' ';
    }

    size_t found, findstart = 0;
    int before = atoi(this_workflow.search.pargs.args["--before"].c_str());
    int after = atoi(this_workflow.search.pargs.args["--after"].c_str());

    /* FIND START INDEX OF EACH MATCHING WORD */
    while ((found = transcript.find(this_workflow.search.query, findstart)) != string::npos) {

	/* FIND START INDEX OF FIRST WORD TO PRINT */
	map<int, string>::const_iterator first_word = --indexToTimestamp.upper_bound(found);
	for (int i = 0; i < before && first_word != indexToTimestamp.begin(); ++i) {
	    first_word--;
	}
	int first_word_start = first_word->first;

	/* TIMESTAMP OF FIRST WORD */
	string timestamp = first_word->second;

	/* START INDEX OF LAST WORD */
	map<int, string>::const_iterator last_word = indexToTimestamp.lower_bound(found + this_workflow.search.query.length());
	for (int i = 0; i < after && last_word != indexToTimestamp.end(); ++i) {
	    last_word++;
	}
	int last_word_start = last_word->first;

	/* PRINT MATCH LINE */
	string before_highlight = transcript.substr(first_word_start, found - first_word_start);
	string highlight = transcript.substr(found, this_workflow.search.query.length());
	// TODO: gives an extra space after match when the last word in the file is matched
	string after_highlight = transcript.substr(found + this_workflow.search.query.length(),
						   last_word_start - (found + this_workflow.search.query.length()) - 1);

	std::cout << termcolor::green << filename
		  << termcolor::reset << ":"
		  << termcolor::blue << "[" << timestamp << "]"
		  << termcolor::reset << ":" << before_highlight
		  << termcolor::red << highlight
		  << termcolor::reset << after_highlight
		  << '\n';
	    
	findstart = found + 1;
    }

    /* CLOSE TRANSCRIPT AND TIMESTAMP CACHE FILES */
    text.close();
    timestamp.close();
}
