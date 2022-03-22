open Defs
open Unix
open Parmap

let cCACHE_DIR = try getenv "MP4GREP_CACHE" with
    Not_found -> raise (Sys_error "Warning: MP4GREP_CACHE unset")

let cMODEL_DIR = try getenv "MP4GREP_MODEL" with
    Not_found -> raise (Sys_error "Warning: MP4GREP_MODEL unset")

(* Default words to print per line with --transcribe/to-files options *)
let cDEFAULT_TRANSCRIBE_WORDS_PER_LINE = 5

let cTIMESTAMP_REGEX = "\\(\\[\\([0-9]*:[0-9]*:[0-9]*\\)\\]\\|\\[\\([0-9]*:[0-9]*\\)\\]\\)"

let _ =
  if Sys.file_exists cCACHE_DIR = false then
    raise (Failure ("Cache directory "^cCACHE_DIR^" not found"))

let _ =
  if (Sys.is_directory cCACHE_DIR) = false then
    raise (Failure ("Cache directory "^cCACHE_DIR^" is not a directory"))

let _ =
  if Sys.file_exists cMODEL_DIR = false then
    raise (Failure ("Model directory "^cMODEL_DIR^" not found"))

let _ =
  if (Sys.is_directory cMODEL_DIR) = false then
    raise (Failure ("Model directory "^cMODEL_DIR^" is not a directory"))

(* TODO: add support for additional extensions *)
let cSUPPORTED_AUDIO_EXTENSIONS = ["wav"] (* "mp3"; "mp4"; "ogg"; "webm"; "mov"; "wav"] *)

let cANSI_RESET = "\x1b[0m"
let cANSI_RED = "\x1b[31m"
let cANSI_GREEN = "\x1b[32m"
let cANSI_YELLOW = "\x1b[33m"
let cANSI_BLUE = "\x1b[34m"

let cDEFAULT_WORDS_BEFORE = 5
let cDEFAULT_WORDS_AFTER = 1
  
let cpu_count () = 
  try match Sys.os_type with 
  | "Win32" -> int_of_string (Sys.getenv "NUMBER_OF_PROCESSORS") 
  | _ ->
      let i = Unix.open_process_in "getconf _NPROCESSORS_ONLN" in
      let close () = ignore (Unix.close_process_in i) in
      try
        let ib = Scanf.Scanning.from_channel i in
        Scanf.bscanf ib "%d" (fun n -> close (); n)
      with
        e -> close(); raise e
  with
  | Not_found | Sys_error _ | Failure _ | Scanf.Scan_failure _ 
  | End_of_file | Unix.Unix_error (_, _, _) -> 1


let get_cache_file (prefix : string) (filename : string) : string =
  let stat_t = Unix.stat filename in
  let hash_int = Hashtbl.hash (stat_t.st_ino) in
  let cache_file = Filename.concat cCACHE_DIR (prefix^(string_of_int hash_int)) in
  cache_file

let get_transcript (filename : string) : string =
  get_cache_file "transcript" filename

let get_timestamp (filename : string) : string =
  get_cache_file "timestamp" filename

let get_current_duration (filename : string) : string =
  get_cache_file "current" filename

let get_total_duration_file (filename : string) : string =
  get_cache_file "total" filename

(* THIS IS CURRENTLY NOT MEMOIZING PROPERLY *)
let get_max_duration_sum (all_input_audio : string list) =
  let rec sum_files (xs : string list) (sum : int) =
    match xs with
    | [] -> sum
    | hd :: tl -> let itotal = open_in (get_total_duration_file hd) in
      let total_dur = input_line itotal in
      seek_in itotal 0;
      close_in itotal;
      sum_files (tl) (sum + (int_of_string total_dur))
  in
  sum_files all_input_audio 0

let get_current_duration_sum (all_input_audio : string list) = 
  let rec sum_files (xs : string list) (sum : int) =
    match xs with
    | [] -> sum
    | hd :: tl -> let itotal = open_in (get_current_duration hd) in
      let total_dur = input_line itotal in
      seek_in itotal 0;
      close_in itotal;
      sum_files (tl) (sum + (int_of_string total_dur))
  in
  sum_files all_input_audio 0

type transcribe_info =
  { filename : string; }

type progress_info =
  { all_filenames : (string list); }
    
type transcribable_or_progress_bar =
  | Transcribable of transcribe_info
  | Progress_bar of progress_info

let file_exists (filename : string) =
  try Sys.file_exists filename
  with
  | Sys_error(e) -> false

let is_cached (filename : string) =
  try Sys.file_exists (get_transcript filename)
  with
  | Sys_error(e) -> false

let transcribe_and_track_progress (inp : transcribable_or_progress_bar) : int = 
  (* PROGRESS BAR THREAD *)
  
  match inp with
  | Progress_bar p ->
    let current_progress = ref 0 in
    let total_duration = get_max_duration_sum p.all_filenames in
    (* Printf.printf "%s %d" "TOTAL DURATION" total_duration; *)
    (* print_endline "!"; *)
    
        while (!current_progress < total_duration) do
          sleep 2;
          current_progress := (get_current_duration_sum p.all_filenames);
          let ratio = (float_of_int !current_progress) /. (float_of_int total_duration) in
          let percent = 100. *. ratio in
          
          let max_bar_len = 25 in
          let current_bar_len = (int_of_float (ratio *. (float_of_int max_bar_len))) in

          Printf.printf "Progress: %.2f%%|" percent;
          Printf.printf "%s" cANSI_YELLOW;
          for marks = 0 to current_bar_len do
            print_char '#'
          done;
          for marks = (current_bar_len + 1) to max_bar_len do
            Printf.printf "%s" " ";
          done;
          Printf.printf "%s" cANSI_RESET;
          Printf.printf "%s" "|";
            
          print_endline "";
          let return_to_start = "\x1b[1F" in

          Printf.printf "%s" return_to_start;
        done;
        
    let x = 0 in x
  | Transcribable t ->
    Printf.printf "%s %s" "transcribing" t.filename;
    
    let x = transcribe
        t.filename
        (get_transcript t.filename)
        (get_timestamp t.filename)
        (get_total_duration_file t.filename)
        (get_current_duration t.filename)
    in x


let is_audio_file (file : string) : bool =
  let segs = file |> Str.split (Str.regexp "\\.") in
  let ext = List.nth segs ((List.length segs) - 1) in
  if List.mem ext cSUPPORTED_AUDIO_EXTENSIONS then
    true
  else
    false

let get_audio_files_from (input : string) (input_tail : string list) : string list =
  let audio_file_lst = ref input_tail in
  if Sys.is_directory input then begin
    Sys.readdir input |>
    Array.iter (fun name ->
        if is_audio_file name then begin
          audio_file_lst := (name :: !audio_file_lst);
        end;);
    !audio_file_lst
  end
  else
    input :: input_tail


let rec make_current_duration_files (input_audio : string list) : unit =
  match input_audio with
  | [] -> ()
  | hd :: tl -> 
    let ocurrent = open_out (get_current_duration hd) in
    Printf.fprintf ocurrent "%s" "0";
    close_out ocurrent;
    make_current_duration_files tl

let rec make_total_duration_files (input_audio : string list) =
  match input_audio with
  | [] -> ()
  | hd :: tl ->
    let total_dur_file = get_total_duration_file hd in
    let _ = total_duration hd total_dur_file in
    make_total_duration_files tl

type search_params =
  { query : string option;
    files : (string list) option;
    before : int option;
    after : int option; }

type transcribe_params =
  { files : (string list) option;
    words_per_line : int option; }
  
type workflow =
  | Search of search_params
  | Transcribe of transcribe_params
  | Transcribe_to_files of transcribe_params
  | Clear_cache
  | Help
  | Parse_fail

let consume_int (xs : string list) (arg : string) =
    match xs with
    | [] -> raise (Failure ("No argument found for "^arg))
    | hd :: tl ->
      try
        (int_of_string hd, tl)
      with
      | Failure _ -> raise (Failure (arg^" expects an int"))

let check_required_search_params (params : search_params) : search_params =
  if params.query = None then
    raise (Failure "No search query provided")
  else if params.files = None then
        raise (Failure "No files to search provided")
  else
    params
      
let consume_search_args (xs : string list) : search_params =
  let rec param_create (xs : string list) (params : search_params) : search_params =
    match xs with
    | [] -> check_required_search_params params
    | hd :: tl -> match hd with
      | "-before" | "--before" -> let before_val, tl = consume_int tl "-before" in
        param_create tl { query = params.query;
                          files = params.files;
                          before = Some(before_val);
                          after = params.after; }
      | "-after" | "--after" -> let after_val, tl = consume_int tl "-after" in
        param_create tl { query = params.query;
                          files = params.files;
                          before = params.before;
                          after = Some(after_val); }
      | _ -> if params.query = None then
          param_create tl { query = Some(hd);
                            files = params.files;
                            before = params.before;
                            after = params.after; }
        else
          match params.files with
          | Some x -> param_create tl { query = params.query;
                                        files = Some (get_audio_files_from hd x);
                                        before = params.before;
                                        after = params.after; }
          | None ->param_create tl { query = params.query;
                                     files = Some(get_audio_files_from hd []);
                                     before = params.before;
                                     after = params.after; }
  in param_create xs { query = None;
                       files = None;
                       before = Some(cDEFAULT_WORDS_BEFORE);
                       after = Some(cDEFAULT_WORDS_AFTER); }

let check_required_transcribe_params (params : transcribe_params) : transcribe_params =
  if params.files = None then
    raise (Failure "No files to transcribe provided")
  else
    params

let consume_transcribe_args (xs : string list) : transcribe_params =
  let rec param_create (xs : string list) (params : transcribe_params) : transcribe_params =
    match xs with
    | [] -> check_required_transcribe_params params
    | hd :: tl ->
      match hd with
      | "--words" | "-words" | "--words-per-line" | "-words-per-line" -> let words_val, tl
        = consume_int tl "--words/-words/--words-per-line/-words-per-line" in
        if words_val < 1 then begin
          raise (Failure "Words per line must be greater than zero")
        end;
        param_create tl { files = params.files;
                          words_per_line = Some(words_val); }
      | _ ->
        match params.files with
        | Some x -> param_create tl { files = Some (get_audio_files_from hd x);
                                      words_per_line = params.words_per_line; }
        | None ->param_create tl { files = Some(get_audio_files_from hd []);
                                   words_per_line = params.words_per_line; }
                                     
  in param_create xs { files = None;
                       words_per_line = Some(cDEFAULT_TRANSCRIBE_WORDS_PER_LINE); }


let unwrap (x : 'a option) : 'a =
  match x with
  | Some(contents) -> contents
  | None -> raise (Failure "Could not unwrap.")
              
let ignore_cached_files (file : string) : bool =
  if is_cached file then begin
      Printf.printf "File %s %s\n" file "is cached, not transcribing.\n";
      false
    end
  else true

let ignore_nonexistent_files (file : string) : bool =
  if file_exists file then true
  else begin
    Printf.printf "File %s does not exist, not transcribing.\n" file;
    false
  end

let transcribe_files (audiofiles_to_transcribe : string list) =
  let num_cpu = cpu_count () in
  let num_cores = num_cpu - 1 in (* one parent *)
  
  make_total_duration_files audiofiles_to_transcribe;
  make_current_duration_files audiofiles_to_transcribe;
  
  let rec make_par_list (xs : string list) : transcribable_or_progress_bar list =
    match xs with
    | [] -> []
    | hd :: tl -> Transcribable ({ filename = hd }) :: make_par_list tl
  in
  
  let transcribes = Progress_bar ({all_filenames = audiofiles_to_transcribe; })
                    :: make_par_list audiofiles_to_transcribe  in
  
  (* TRANSCRIBE *)
  let _ = make_model cMODEL_DIR in
  let _ = parmap ~ncores:num_cores transcribe_and_track_progress (L transcribes) in
  let _ = delete_model 1 in
  ()

let read_whole_file (filename : string) : string  =
  let ch = open_in filename in
  let s = really_input_string ch (in_channel_length ch) in
  close_in ch;
  s
  
let get_byte_to_timestamp_table (audio_file : string) = 
  let tbl = Hashtbl.create 100 in
  let timestamps_file = read_whole_file (get_timestamp audio_file) in
  
  let last_timestamp_reached = ref false in
  let previous_byte = ref ((String.length timestamps_file) - 1) in
  let previous_timestamp = ref ((String.length timestamps_file) - 1) in
  let timestamp_reg = Str.regexp cTIMESTAMP_REGEX in
  let byte_reg = Str.regexp "([0-9]*)" in
  let paren_reg = Str.regexp "[\\(\\)]" in
  
  while (!last_timestamp_reached = false
         && !previous_byte > 0
         && !previous_timestamp > 0) do
    try
      previous_timestamp := (Str.search_backward timestamp_reg
                               timestamps_file !previous_timestamp) -1;
      let timestamp_str = Str.matched_string timestamps_file in

      previous_byte := (Str.search_backward byte_reg
                          timestamps_file !previous_byte) -1;
      let byte_str = Str.matched_string timestamps_file in

      let byte_num = byte_str
                     |> Str.global_replace paren_reg ""
                     |> int_of_string in

      Hashtbl.add tbl byte_num timestamp_str;
    with
      Not_found -> last_timestamp_reached := true;
  done;
  tbl
  
let get_transcription (words_per_line : int) (audio_file : string) =
  let all_words = read_whole_file (get_transcript audio_file) in
  let line_start = ref 0 in
  let char_pos = ref 0 in
  let byte_to_timestamp = get_byte_to_timestamp_table audio_file in
  let line_lst = ref [] in
    
  while (!char_pos < (String.length all_words)) do
    let space_count = ref 0 in
    
    while (!space_count < words_per_line && !char_pos < (String.length all_words)) do
      if all_words.[!char_pos] = ' ' then begin
        space_count := !space_count + 1;
      end;
      char_pos := !char_pos + 1;
    done;
    
    let line_words = String.sub all_words !line_start (!char_pos - !line_start) in
    let timestamp = Hashtbl.find byte_to_timestamp !line_start in
    let line = timestamp^":"^line_words in
    line_lst := (line :: !line_lst);
    line_start := !char_pos;
  done;
  (audio_file, (List.rev !line_lst))
  
let do_transcribe (args : transcribe_params) =
  let filenames = unwrap args.files in
  let words_per_line = unwrap args.words_per_line in

  let lst_of_lsts =  List.map (get_transcription words_per_line) filenames in
  let _ = List.map (fun (audio_file, lst) ->
      Printf.printf "%s\n" audio_file;
      List.iter (Printf.printf "%s\n") lst) lst_of_lsts in
  ()

let do_transcribe_to_files (args : transcribe_params) =
  let filenames = unwrap args.files in
  let words_per_line = unwrap args.words_per_line in

  let lst_of_lsts =  List.map (get_transcription words_per_line) filenames in
  let _ = List.map (fun (audio_file, lst) ->
      let output_filename = audio_file^"_transcript.txt" in
      Printf.printf "Printing transcript of %s to %s\n" audio_file output_filename;
      let out_chan = open_out output_filename in
      List.iter (Printf.fprintf out_chan "%s\n") lst;
      close_out out_chan;) lst_of_lsts in
  ()

let do_search (args : search_params) =
  let query = unwrap args.query in
  let filenames = unwrap args.files in
  let before = unwrap args.before in
  let after = unwrap args.after in
  
  (* Does not print anything *)
  let audiofiles_to_search = filenames
                             |> List.filter file_exists 
  in

  (* Gives the user feedback about the status of their files *)
  let audiofiles_to_transcribe = filenames
                                 |> List.filter ignore_nonexistent_files
                                 |> List.filter ignore_cached_files
  in

  transcribe_files audiofiles_to_transcribe;
     
  let search_transcript (orig_search : string) (audio_file : string) =
    let file =  get_transcript audio_file in
    let str_to_search = read_whole_file file in

    let no_extra_words_reg = String.trim orig_search
                           |> String.trim 
    in
    

    (* LOAD TIMESTAMPS INTO HASHTABLE *)

    let tbl = get_byte_to_timestamp_table audio_file in
      
      (* FIND ALL THE MATCHES AND PUT IN RESULT LIST *)
    (* Str.search_forward/backward has side effect: *)
    (* The next call to Str.matched_string will return the matched string *)
    let last_match_reached = ref false in
    let match_lst = ref [] in
    let previous_match_pos = ref ((String.length str_to_search) - 1) in
    while (!last_match_reached = false && !previous_match_pos > 0) do
      try
        previous_match_pos := (Str.search_backward (Str.regexp no_extra_words_reg)
                                 str_to_search !previous_match_pos) - 1;

        let match_ = Str.matched_string str_to_search in

        (* ASSUMPTION : Startpos and endpos are initialized to 
           the first character of the matched string.
           TO ACHIEVE THIS: strip the input search string of spaces before/after
        *)

        let start_of_match = !previous_match_pos + 1 in
        let startpos = ref start_of_match in
        let endpos = ref start_of_match in

        (* Iterate backwards until words_after + 1 spaces are found *)
        let spaces_prior = ref 0 in
        let spaces_after = ref 0 in
        while ((!endpos < (String.length str_to_search))
               && (!spaces_after < (after + 1))) do
          endpos := !endpos + 1;
          if str_to_search.[!endpos] = ' ' then begin
            spaces_after := !spaces_after + 1;
          end
        done;

        (* Iterate forwards until words_after + 1 spaces are found *)
        while ((!startpos > 0)
               && (!spaces_prior < (before + 1))) do
          startpos := !startpos - 1;
          if str_to_search.[!startpos] = ' ' then begin
            spaces_prior := !spaces_prior + 1;
          end
        done;

        (* Make sure that the starting position is always
           not a space, to index into the timestamps *)
        if str_to_search.[!startpos] = ' ' then begin
          startpos := !startpos + 1;
        end;
        
        let portion_match = (String.sub str_to_search
                               start_of_match
                               (String.length match_)) in
          
        let portion_prior = (String.sub str_to_search
                               !startpos
                               (start_of_match - !startpos)) in
        
        let portion_after = (String.sub str_to_search
                               (start_of_match + (String.length match_))
                               (!endpos - (start_of_match + (String.length match_)))) in
        
        let timestamp = (Hashtbl.find tbl (!startpos)) in

        let res = cANSI_GREEN^audio_file^cANSI_RESET
                  ^":"
                  ^cANSI_BLUE^timestamp^cANSI_RESET
                  ^":"
                  ^portion_prior
                  ^cANSI_RED^portion_match^cANSI_RESET
                  ^portion_after
        in
        
        match_lst := (res) :: !match_lst;
      with
        Not_found -> last_match_reached := true;
    done;
    !match_lst
  in
  
  let results = List.map (search_transcript query) audiofiles_to_search
                |> List.concat
  in
  
  let () = List.iter (Printf.printf "%s\n") results in
  ()

let print_help_message () =
  print_endline "Search: mp4grep [options] [search string] [files/directories]";
  print_endline "Search options: ";
  print_endline "  --before [int value] <- words before a match to print";
  print_endline "  --after [int value] <- words after a match to print";


  print_endline "Transcribing: mp4grep --transcribe[-to-files] [options] [files/directories]";
  print_endline " ^--transcribe prints output to screen";
  print_endline " ^--transcribe-to-files prints output to text files";
  print_endline "Transcribe options: ";
  print_endline "  --words <- words per line to print";
  print_endline "Clear the cache: mp4grep --clear-cache";
  print_endline "Help message: mp4grep --help";
  ()

exception Is_not_directory
exception Not_deleting_cache
  
let clear_cache () =
  let remove_directory_contents (path : string) =
    let user_wants_to_remove_cache = ref false in
    
    while (!user_wants_to_remove_cache = false) do
      Printf.printf "Do you want to remove the contents of the cache directory (%s)? [Y/n] " cCACHE_DIR;
      let input = read_line () in
      match input.[0] with
      | 'Y' -> user_wants_to_remove_cache := true;
      | _ -> raise Not_deleting_cache
    done;
    
    try
      match Sys.is_directory path with
      | true ->
        Sys.readdir path |>
        Array.iter (fun name ->
            let filename = Filename.concat path name in
            Printf.printf "Removing: %s\n" filename;
            Sys.remove filename;); 
      | false -> raise Is_not_directory
    with
    | Is_not_directory -> raise (Failure ("Tried to remove contents of "^path^" which is not a directory"))
  in
  remove_directory_contents cCACHE_DIR;
  Printf.printf "%s\n" "Cleared cache."; ()

let () =
  let lexemes = Sys.argv
              |> Array.to_list
              |> List.tl (* ignore the first param *)
  in

  let wkflow = match lexemes with
    | [] -> raise (Sys_error "Not enough arguments")
    | hd :: tl -> match hd with
      | "--transcribe" | "-transcribe" -> Transcribe (consume_transcribe_args tl)
      | "--transcribe-to-files" | "-transcribe-to-files" -> Transcribe_to_files (consume_transcribe_args tl)
      | "--clear-cache" | "-clear-cache" -> Clear_cache
      | "--help" | "-help" | "-h" -> Help
      | _ -> Search (consume_search_args lexemes)
  in

  let _ =
    match wkflow with
    | Search (args) -> do_search args
    | Transcribe (args) -> do_transcribe args
    | Transcribe_to_files (args) -> do_transcribe_to_files args
    | Clear_cache -> clear_cache ()
    | Help -> print_help_message ()
    | _ -> Printf.printf "Parse fail"
  in
  ()
