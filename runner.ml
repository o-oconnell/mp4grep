open Hello_world
open Unix
open Parmap

let cCACHE_DIR = try getenv "MP4GREP_CACHE" with
    Not_found -> raise (Sys_error "Warning: MP4GREP_CACHE unset")

let cMODEL_DIR = try getenv "MP4GREP_MODEL" with
    Not_found -> raise (Sys_error "Warning: MP4GREP_MODEL unset")

let cpu_count () = 
  try match Sys.os_type with 
  | "Win32" -> int_of_string (Sys.getenv "NUMBER_OF_PROCESSORS") 
  | _ ->
      let i = Unix.open_process_in "getconf _NPROCESSORS_ONLN" in
      let close () = ignore (Unix.close_process_in i) in
      try Scanf.fscanf i "%d" (fun n -> close (); n) with e -> close (); raise e
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
      (* Printf.printf "%s %d" "Current progress is" (get_current_duration_sum p.all_filenames); *)
      (* Printf.printf "%s %d" "Total duration is " (get_max_duration_sum p.all_filenames); *)

      Printf.printf "Percent complete: %f\r" ((float_of_int !current_progress) /. (float_of_int total_duration));
      print_endline " ";
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
    let x = total_duration hd total_dur_file in
    make_total_duration_files tl

let () =
  let num_cpu = cpu_count () in
  let num_cores = num_cpu - 1 in (* one parent *)
  let rec make_list_of_filenames (cur : int) (xs : (string * int) list)  =
    if cur = 0 then xs else make_list_of_filenames (cur - 1) (("harvard.wav", cur) :: xs)
  in
  let filenames = ["harvard.wav"] in
  
  (* CHECK FOR CACHED FILES, DO NOT ATTEMPT TO TRANSCRIBE THEM *)
  let rec avoid_transcribed_files (filenames : string list) : string list =
    match filenames with
    | [] -> []
    | hd :: tl ->
      if is_cached hd then begin
        Printf.printf "%s %s" hd "is cached, not transcribing it.";
        avoid_transcribed_files tl
      end
      
      else
        hd :: avoid_transcribed_files tl
  in
  let filenames = avoid_transcribed_files filenames in

  (* CREATE DURATION FILES. MUST CALL PRIOR TO TRANSCRIBING *)
  make_total_duration_files filenames;
  make_current_duration_files (filenames); 

  let rec make_par_list (xs : string list) : transcribable_or_progress_bar list =
    match xs with
    | [] -> []
    | hd :: tl -> Transcribable ({ filename = hd }) :: make_par_list tl
  in
  
  let transcribes = Progress_bar ({all_filenames = filenames; }) :: make_par_list filenames  in
  
  (* TRANSCRIBE *)
  let num = make_model "/home/ooc/mp4grep/model" in
  let int_list = parmap ~ncores:num_cores transcribe_and_track_progress (L transcribes) in
  let another_num = delete_model 1 in
  ()
                                                              

