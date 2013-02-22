(*
 * $Id: disasm.ml,v 1.16 2009/07/29 09:26:28 casse Exp $
 * Copyright (c) 2008, IRIT - UPS <casse@irit.fr>
 *
 * This file is part of OGliss.
 *
 * OGliss is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * OGliss is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGliss; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *)

(**
  Here you will find usefull functions used when the simulator is generated with
  a profiling file ([-p path_of_file] option of gep).

  The easiest way to get a profiling is to generate a simulator with gep without "-p" option
  and then launch the simulator with severals benchs (with the "-p" option of the simulator).
  you now have a "proc_name.profile" file which can be use 
  to generate a new simulator with the "-p" option of GEP.
  Simulator generated with profiling file are optimized for the given statistics
  
  The module provide the function read_profiling_file path which read the file
  from the designated path and return the profile stats as a sorted list of instructions name.
*)

(** Read from "in_channel" the corresponding control string "ctrl" 
    Return the string
*)
let fscanf in_channel ctrl = Scanf.fscanf in_channel ctrl (fun s -> s);; 

(** Read and dump an entire line from in_channel *)
let dump_line in_channel =
  let rec aux = function
    | '\n' -> ()
    |  _   -> aux (fscanf in_channel "%c") 
  in aux (fscanf in_channel "%c") 

(** Read and dump a word from in_channel *)
let dump_word in_channel = fscanf in_channel "%s "


let erase_proc_name_from_list l =
  List.map (fun s -> 
	      let i =(String.index s '_')+1
	      in String.sub s i ((String.length s)-i)) l
;;

(** Read a profiling file from path 
    @return List of instructions name sorted by ascending order of call number
*)
let read_profiling_file path =
  let in_channel     = open_in path             in
    try(
      let _ = dump_line in_channel     in (* Dump first line      *)
      let _ = fscanf in_channel  "%s " in (* Dump processor type  *)
      let _ = dump_line in_channel        (* Dump third line      *)     
      in
      let rec read_stats l =   
		let name = dump_word in_channel    in  (* Read instruction name *)
		let _    = fscanf in_channel "%d " in  (* Dump instruction id   *)
		let _    = fscanf in_channel "%d " in  (* Dump instruction stats*)
		let l    = l@[name]
		in
		  try  read_stats l 
		  with End_of_file -> l 
      in 
      let l = read_stats []
      in
		close_in in_channel;
	        List.rev (List.filter (fun a -> a <> "UNKNOWN") (erase_proc_name_from_list l))
    )with 
      | End_of_file -> 
		close_in in_channel;
		failwith "Unexpected end of profiling file exiting\n"
;;





