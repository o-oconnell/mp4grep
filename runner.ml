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


(* THIS IS CURRENTLY NOT MEMOIZING PROPERLY *)
let get_total_duration (num_audio_files : int) =
  let cache = Hashtbl.create 10 in
  let rec sum_files (cur_suffix : int) (sum : int) =
    if cur_suffix = 0 then sum else begin 
      let itotal = open_in ("total_duration"^(string_of_int cur_suffix)) in
      let total_dur = input_line itotal in
      seek_in itotal 0;
      close_in itotal;
      sum_files (cur_suffix - 1) (sum + (int_of_string total_dur))
    end in
  try Hashtbl.find cache num_audio_files
  with Not_found -> begin
      let result = sum_files num_audio_files 0 in
      Hashtbl.add cache num_audio_files result; result
    end


let transcribe_and_track_progress (filename_or_file_count, file_id_or_flag) : int =
  if (file_id_or_flag = -1) then begin
    
    (* Wait until every file has had its total duration computed *)
    let all_file_durations_computed = ref false in
    let file_count = int_of_string filename_or_file_count in
    while (!all_file_durations_computed = false) do
      all_file_durations_computed := true;
      for i = 1 to file_count do
        let itotal = open_in ("total_duration"^(string_of_int i)) in
        let line = input_line itotal in
        seek_in itotal 0;
        close_in itotal;
        
        (* Strange semantics for if with no else *)
        if (int_of_string line) = 0 then begin
          Printf.printf "%s %s\n" "not done for" ("total_duration"^(string_of_int i));
          let set_false () =
            all_file_durations_computed := false; ()
          in
          set_false ();
        end
        
      done;
      sleep 2;
      print_endline "still not done";
      Printf.printf "%s %b" "value of bool" !all_file_durations_computed
    done;

    let current_progress = ref 0 in
    let total_duration = get_total_duration (int_of_string filename_or_file_count) in
    Printf.printf "%s %d" "TOTAL DURATION" total_duration;
    print_endline "!";
    while (!current_progress < total_duration) do
      let num_transcripts = int_of_string filename_or_file_count in
      let is_an_int (input : string) : bool =
        Str.string_match (Str.regexp "[0-9]+$") input 0
      in
      let rec sum_durations (transcript_num : int) (sum : int) : int =
        if transcript_num = 0 then 
          sum
        else begin
          if (Sys.file_exists ("current_duration"^(string_of_int transcript_num)) &&
              Sys.file_exists ("total_duration"^(string_of_int transcript_num))) then
            
            let icurrent = open_in ("current_duration"^(string_of_int transcript_num)) in
            let line = input_line icurrent in
            seek_in icurrent 0;
            close_in icurrent;
            
            if (is_an_int line) = true then 
              sum_durations (transcript_num - 1) (sum + (int_of_string line))
            else begin
              let itotal = open_in ("total_duration"^(string_of_int transcript_num)) in
              let total_dur = input_line itotal in
              seek_in itotal 0;
              close_in itotal;
              sum_durations (transcript_num - 1) (sum + (int_of_string total_dur))
            end
          else 
            sum_durations (transcript_num - 1) (sum + 0)
        end
      in
      sleep 2;
      current_progress := (sum_durations num_transcripts 0);
      Printf.printf "%s %d" "Current progress is" (sum_durations num_transcripts 0);
      Printf.printf "%s %d" "Total duration is " (get_total_duration 10);
      Printf.printf "Percentage completion is %f" ((float_of_int !current_progress) /. (float_of_int total_duration));
      print_endline " ";
    done;
    let x = 0 in x
  end
  else begin
    let x = transcribe
        filename_or_file_count
        ("transcript" ^ (string_of_int file_id_or_flag))
        ("total_duration" ^ (string_of_int file_id_or_flag))
        ("current_duration" ^ (string_of_int file_id_or_flag))
    in x
  end

let rec make_all_tempfiles (num_files : int) : unit = 
  if num_files = 0 then () else
    let ocurrent = open_out ("current_duration"^(string_of_int num_files)) in
    (* let ototal = open_out ("total_duration"^(string_of_int num_files)) in *)
    Printf.fprintf ocurrent "%s" "0";
    (* Printf.fprintf ototal "%s" "0"; *)
    close_out ocurrent;
    (* close_out ototal; *)
    make_all_tempfiles (num_files - 1); ()


let rec save_durations_to_files (audio_file_inputs : string list) (id : int) =
  match audio_file_inputs with
  | [] -> ()
  | hd :: tl -> let x = total_duration hd ("total_duration"^(string_of_int id)) in save_durations_to_files tl (id + 1);
    ()

let () =
  let num_cpu = cpu_count () in
  let num_cores = num_cpu - 1 in (* one parent *)
  let rec make_list_of_filenames (cur : int) (xs : (string * int) list)  =
    if cur = 0 then xs else make_list_of_filenames (cur - 1) (("harvard.wav", cur) :: xs)
  in
  let filenames = ["Week1_second_class.mp4.wav";  "Week3_first_class.mp4.wav";  "Week4_second_class.mp4.wav";  "Week6_first_class.mp4.wav";   "Week7_second_class.mp4.wav"; "Week1_third_class.mp4.wav"; "Week3_second_class.mp4.wav"; "Week5_first_class.mp4.wav"; "Week6_second_class.mp4.wav"; "Week7_third_class.mp4.wav"; "Week2_first_class.mp4.wav"; "Week3_third_class.mp4.wav"; "Week5_second_class.mp4.wav"; "Week6_third_class.mp4.wav"; "Week8_first_class.mp4.wav"; "Week2_third_class.mp4.wav"; "Week4_first_class.mp4.wav"; "Week5_third_class.mp4.wav"; "Week7_first_class.mp4.wav"] in
  let rec make_tuples_from_filenames (sl : string list) (cur : int) : (string * int) list =
    match sl with
    | [] -> []
    | hd :: tl -> (hd, cur) :: make_tuples_from_filenames tl (cur + 1)
  in

  let () = save_durations_to_files filenames 1 in(* careful! this must be called before prepending below *)
  let filenames = make_tuples_from_filenames filenames 1 in
  make_all_tempfiles (List.length filenames); (* careful! this must be called before prepending below *)
  let filenames = ((string_of_int (List.length filenames)), -1) :: filenames in
  let num = make_model "/home/ooc/mp4grep/model" in
  let int_list = parmap ~ncores:num_cores transcribe_and_track_progress (L filenames) in
  let another_num = delete_model 1 in ()
                                                              

