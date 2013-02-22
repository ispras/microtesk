(*
 * $Id: mkirg.ml,v 1.1 2009/04/24 16:28:30 casse Exp $
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
let nmp = ref ""
let out = ref ""
let insts = ref false
let options = []

(* argument decoding *)
let free_arg arg =
	if !nmp = "" then nmp := arg else
	if !out = "" then out := arg else
	raise (Arg.Bad "only NML and out files required")
let usage_msg = "SYNTAX: gep [options] NML_FILE IRG_FILE\n\tGenerate code for a simulator"

let arg_error msg =
		Printf.fprintf stderr "ERROR: %s\n" msg;
		Arg.usage options usage_msg;
		exit 1

let _ =
	Arg.parse options free_arg usage_msg;
	if !nmp = "" then arg_error "one NML file must be given !\n";
	if !out = "" then arg_error "one IRG file must be given !\n"

let _ =
	try
		begin
			IrgUtil.load !nmp;
			Irg.save !out
		end
	with
	  Parsing.Parse_error ->
		Lexer.display_error "syntax error"; exit 2
	| Irg.Error f ->
		output_string stderr "ERROR: ";
		f stderr;
		output_char stderr '\n';
		exit 2
	| Lexer.BadChar chr ->
		Lexer.display_error (Printf.sprintf "bad character '%c'" chr); exit 2
	| Sem.SemError msg ->
		Lexer.display_error (Printf.sprintf "semantics error : %s" msg); exit 2
	| Irg.Symbol_not_found sym ->
		Lexer.display_error (Printf.sprintf "symbol not found: %s" sym); exit 2
	| Irg.IrgError msg ->
		Lexer.display_error (Printf.sprintf "ERROR: %s" msg); exit 2
	| Sem.SemErrorWithFun (msg, fn) ->
		Lexer.display_error (Printf.sprintf "semantics error : %s" msg);
		fn (); exit 2
	| Irg.RedefinedSymbol s ->
		Lexer.display_error (Printf.sprintf "ERROR: redefined symbol \"%s\", firstly defined at %s" s (Irg.pos_of s)); exit 2
