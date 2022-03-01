open Hello_world
open Unix
open Parmap

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


let call_vosk (filename, number) : int =
  if (number = -1) then begin
    while (not (Sys.file_exists "COMPLETED.txt")) do 
      let test_output = open_out "TEST.txt" in
      Printf.fprintf test_output "%s" "TEST PLEASE ? NOT WORING?";
      close_out test_output;
      let num_transcripts = int_of_string filename in
      let is_an_int (input : string) : bool =
        Str.string_match (Str.regexp "[0-9]+$") input 0
      in
      let rec sum_durations (transcript_num : int) (sum : int) : int =
        if transcript_num = 0 then
          sum
        else
        if Sys.file_exists ("current_duration"^(string_of_int transcript_num)) &&
           Sys.file_exists ("total_duration"^(string_of_int transcript_num)) then
          
          let icurrent = open_in ("current_duration"^(string_of_int transcript_num)) in
          let line = input_line icurrent in
          close_in icurrent;
          if (is_an_int line) = true then 
            sum_durations (transcript_num - 1) (sum + (int_of_string line))
          else
            let itotal = open_in ("total_duration"^(string_of_int transcript_num)) in
            let total_dur = input_line itotal in
            close_in itotal;
            sum_durations (transcript_num - 1) (sum + (int_of_string total_dur))
        else
          sum_durations (transcript_num - 1) (sum + 0)
      in
      let output_for_sum = open_out "sum_total" in
      Printf.fprintf output_for_sum "the total is: %d\n" (sum_durations num_transcripts 0);
      close_out output_for_sum;
    done;
    let x = 0 in x
  end
  else begin
  let x = transcribe
        filename
        ("transcript" ^ (string_of_int number))
        ("total_duration"^ (string_of_int number))
        ("current_duration" ^ (string_of_int number))
  in x
end

let () =
  let num_cpu = cpu_count () in
  Printf.printf "%s %d\n" "running with pool of size: " num_cpu;
  let rec make_list_of_filenames (cur : int) (xs : (string * int) list)  =
    match cur with
    | 0 -> xs
    | _ -> make_list_of_filenames (cur - 1) (("harvard.wav", cur) :: xs)
  in
  let filenames = make_list_of_filenames 10 [] in
  let filenames = ((string_of_int (List.length filenames)), -1) :: filenames in
  let num = make_model "/home/ooc/mp4grep/model" in
  let int_list = parmap ~ncores:11 call_vosk (L filenames) in
  let another_num = delete_model 1 in ()
                                                              

