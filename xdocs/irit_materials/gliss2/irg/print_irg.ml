(*
 * $Id: print_irg.ml,v 1.6 2009/07/15 12:15:06 dubot Exp $
 * Copyright (c) 2009, IRIT - UPS <casse@irit.fr>
 *
 * This file is part of OGliss.
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


(* argument list *)
let input = ref ""
let insts = ref false
let options = [ ("-i", Arg.Set insts, "display list of generated instructions") ]

(* argument decoding *)
let free_arg arg =
	if !input = "" then input := arg else
	raise (Arg.Bad "only one NML/IRG file required")
let usage_msg = "SYNTAX: gep [options] NML/IRG_FILE\n\tOutput the content of an NML or IRG file."
let _ =
	Arg.parse options free_arg usage_msg;
	if !input = "" then begin
		prerr_string "ERROR: one NML or IRG file must be given !\n";
		Arg.usage options usage_msg;
		exit 1
	end


let _ =
	try
		begin
			IrgUtil.load !input;
			if !insts then
				Iter.iter
					(fun _ spec -> Printf.printf "%d:%s -> \n" (Iter.get_id spec) (Iter.get_name spec); Irg.print_spec spec)
					()
			else
				Irg.StringHashtbl.iter (fun _ s -> Irg.print_spec s) Irg.syms;
		end
	with
	  Parsing.Parse_error ->
		Lexer.display_error "syntax error"; exit 2
	| Lexer.BadChar chr ->
		Lexer.display_error (Printf.sprintf "bad character '%c'" chr); exit 2
	| Sem.SemError msg ->
		Lexer.display_error msg; exit 2
	| Irg.IrgError msg ->
		Lexer.display_error (Printf.sprintf "ERROR: %s" msg); exit 2
	| Sem.SemErrorWithFun (msg, fn) ->
		Lexer.display_error msg; fn (); exit 2
