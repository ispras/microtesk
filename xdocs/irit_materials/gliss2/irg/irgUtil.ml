(*
 * $Id$
 * Copyright (c) 2010, IRIT - UPS <casse@irit.fr>
 *
 * This file is part of GLISS2.
 *
 * GLISS2 is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * GLISS2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GLISS2; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *)

open Irg


(** Check that all OR-operation are defined.
	@raise Irg.Error	If an error is found. *)
let check_or_ops _ =
	let check name op =
		match get_symbol op with
		| Irg.UNDEF -> Irg.error (Printf.sprintf "symbol \"%s\" used in op \"%s\" at %s is not defined" op name (Irg.pos_of name))
		| Irg.AND_OP _ | Irg.OR_OP _ -> ()
		| _ -> Irg.error (Printf.sprintf "op \"%s\" used in \"%s\" at %s should be an op" op name (Irg.pos_of name)) in
	Irg.iter (fun name spec ->
		match spec with
		| Irg.OR_OP (_, ops) -> List.iter (check name) ops
		| _ -> ())


(** Check that all OR-mode are defined.
	@raise Irg.Error	If an error is found. *)
let check_or_modes _ =
	let check name mode =
		match get_symbol mode with
		| Irg.UNDEF -> Irg.error (Printf.sprintf "symbol \"%s\" used in mode \"%s\" at %s is not defined" mode name (Irg.pos_of name))
		| Irg.OR_MODE _ | Irg.AND_MODE _ -> ()
		| _ -> Irg.error (Printf.sprintf "symbol \"%s\" used in \"%s\" at %s should be a mode" mode name (Irg.pos_of name)) in
	Irg.iter (fun name spec ->
		match spec with
		| Irg.OR_MODE (_, modes) -> List.iter (check name) modes
		| _ -> ())


(** Load an NML description either NMP, NML or IRG.
	@param 	path		Path of the file to read from.
	@raise	Sys_error	If there is an error during the read. *)
let load path =

	let run_lexer path lexbuf =
		Lexer.file := path;
		Lexer.line := 1;
		Lexer.line_offset := 0;
		Lexer.lexbuf := lexbuf;
		Parser.top Lexer.main lexbuf;
		check_or_ops ();
		check_or_modes () in		

	(* is it an IRG file ? *)
	if Filename.check_suffix path ".irg" then
		Irg.load path

	(* is it NML ? *)
	else if Filename.check_suffix path ".nml" then
		run_lexer path (Lexing.from_channel (open_in path))

	(* is it NMP ? *)
	else if Filename.check_suffix path ".nmp" then
		let input = run_nmp2nml path in
		begin
			run_lexer path (Lexing.from_channel input);
			match Unix.close_process_in input with
			| Unix.WEXITED n when n = 0 -> ()
			| _ -> raise (Sys_error "ERROR: preprocessing failed.")
		end

	(* else error *)
	else
		raise (Sys_error (Printf.sprintf "ERROR: unknown file type: %s\n" path))
