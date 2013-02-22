(*
 * GLISS2 -- lexer of NML language
 * Copyright (c) 2011, IRIT - UPS <casse@irit.fr>
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
{

open Parser
open Lexing
exception BadChar of char
exception BadLine

(* Line count *)
let file = ref ""
let line = ref 1
let line_offset = ref 0
let bitfld = ref false
let lexbuf = ref (Lexing.from_string "")

(* Keyword detection *)
let lexicon = Irg.StringHashtbl.create 211
let keyword id =
	try
		let e=Irg.StringHashtbl.find lexicon id
		in
		match e with
		 EXCEPTION _->EXCEPTION !line
		|LET _->LET !line
		|MEM _->MEM !line
		|MODE _->MODE !line
		|OP _-> OP !line
		|REG _->REG !line
		|VAR _->VAR !line
		|RESOURCE _->RESOURCE !line
		|TYPE _->TYPE !line
		|_->e
	with Not_found -> (ID id)

let keywords = [
	("__attr",		ATTR);
	("action",      ACTION);
	("alias",       ALIAS);
	("ports",       PORTS);
	("bool",        BOOL);
	("canon",	CANON);
	("card",        CARD);
	("case",        CASE);
	("coerce",      COERCE);
	("default",     DEFAULT);
	("else",        ELSE);
	("endif",       ENDIF);
	("enum",        ENUM);
	("error",       ERROR);
	("exception",   EXCEPTION 0);
	("extend",	EXTEND);
	("fix",        	FIX);
	("float",       FLOAT);
	("format",      FORMAT);
	("if",        	IF);
	("image",       IMAGE);
	("initial",     INITIALA);
	("int",        	INT);
	("let",        	LET 0);
	("list",        LIST);
	("macro",       MACRO);
	("mem",        	MEM 0);
	("mode",        MODE 0);
	("not",        	NOT);
	("op",        	OP 0);
	("reg",        	REG 0);
	("var",        	VAR 0);
	("resource",    RESOURCE 0);
	("syntax",      SYNTAX);
	("switch",      SWITCH);
	("then",        THEN);
	("type",        TYPE 0);
	("uses",        USES);
	("volatile",    VOLATILE)
]

(* Is the NOP keyword really required ?
	("nop",        	NOP);
*)


let _ =
	let add (key, token) = Irg.StringHashtbl.add lexicon key token in
	Irg.StringHashtbl.clear lexicon;
	List.iter add keywords


(** Compute column of the current symbol.
	@return		Column number. *)
let get_col _ =
	(Lexing.lexeme_start !lexbuf) - !line_offset + 1

(** Output an error on the given stream with the current (file, line, column) position.
	@param out	Output channel.
	@param msg	Message to output. *)
let output_error out msg =
	Printf.fprintf out "ERROR: %s:%d:%d: %s\n" !file !line (get_col ()) msg


(** Output an error with the current (file, line, column) position.
	@param msg	Message to output. *)
let display_error msg = output_error stderr msg


(** Get the current location as a string.
	@return	Current location string. *)
let current_loc _ = Printf.sprintf "%s:%d:%d" !file !line (get_col ())


(** warning management
	@param msg		Message to display. *)
let display_warning msg=
	Printf.fprintf stderr "WARNING: %s:%d:%d: %s\n" !file !line (get_col ()) msg

(* Lexing add-ons *)
let rec dotdot lexbuf i found =
	if i >= lexbuf.lex_buffer_len then
		if lexbuf.lex_eof_reached then false
		else begin
			let diff = i - lexbuf.lex_start_pos in
			lexbuf.refill_buff lexbuf;
			dotdot lexbuf (lexbuf.lex_start_pos + diff) found
		end
	else
		match lexbuf.lex_buffer.[i] with
		  '\n' -> false
		| '.' -> if found then true else dotdot lexbuf (i + 1) true
		| '<' | '>' | '=' | ';' | '}' -> false
		| _ -> dotdot lexbuf (i + 1) false

let gt lexbuf token size =
	if not !bitfld then token
	else begin
		bitfld := false;
		lexbuf.lex_curr_pos <- lexbuf.lex_curr_pos - size;
		GT
	end

let append s c = s ^ (String.make 1 c)

let appends s c =
	let c =
		match c with
		| 'n' -> '\n'
		| 't' -> '\t'
		| 'r' -> '\r'
		| c -> c in
	append s c


(** Record a new line. *)
let new_line lexbuf =
	incr line;
	line_offset := Lexing.lexeme_end lexbuf

}

let letter	= ['a' - 'z' 'A' - 'Z' '_']
let digit	= ['0' - '9']
let hex		= ['0' - '9' 'a' - 'f' 'A' - 'F']
let alpha	= ['0' - '9' 'a' - 'z' 'A' - 'Z' '_']
let delim	= [' ' '\t']
let newline	= ['\n']
let decint	= digit +
let binint	= '0' ['b' 'B'] ['0' '1']+
let hexint	= '0' ['x' 'X'] hex+
let flt1	= decint '.' decint
let flt2	= decint ['e' 'E'] ['+' '-']? decint
let flt3	= decint '.' decint ['e' 'E'] ['+' '-']? decint
let flt		= flt1 | flt2 | flt3
let id		= letter alpha*

(**)
(*let num=decint|hexint|binint*)
let num=decint|hexint
(**)

rule main = parse

	delim		{ main lexbuf }
|	newline		{ new_line lexbuf; main lexbuf }
|	"//"		{ eof_comment lexbuf }
|	"/*"		{ comment lexbuf }

|	"\""		{ STRING_CONST  (str "" lexbuf) }
|	"'"			{ chr "" lexbuf }
|	"#line"		{ scan_line lexbuf; scan_file lexbuf; main lexbuf }

|num as v 		{	try(
					CARD_CONST (Int32.of_string v)
				)with Failure _-> CARD_CONST_64 (Int64.of_string v)
			}

| binint as v		{	let size = (String.length v) - 2 in
				try(
					BIN_CONST (Int32.of_string v, size)
				)with Failure _-> BIN_CONST_64 (Int64.of_string v, size)
			}

(**)

|	flt as v	{ FIXED_CONST (float_of_string v) }
|	id as v		{  keyword v }
|	">>>"		{ gt lexbuf ROTATE_RIGHT 2 }
|	"<<<"		{ ROTATE_LEFT }
|	">>"		{ gt lexbuf RIGHT_SHIFT 1 }
|	"<<"		{ LEFT_SHIFT }
|	".."		{ DOUBLE_DOT }
|	"::"		{ DOUBLE_COLON }
|	"**"		{ DOUBLE_STAR }
|	">="		{ gt lexbuf GEQ 1 }
|	"=="		{ EQU }
|	"!="		{ NEQ }
|	"&&"		{ AND }
|	"||"		{ OR }
|	"<="		{ LEQ }
|	"<"		{
			 if dotdot lexbuf lexbuf.lex_last_pos false
				then
					begin bitfld := true; BIT_LEFT end
				else
					LT
			}
|	">"     	{ bitfld := false; GT }
|	"$"     	{ DOLLAR }
|	"#"		{ SHARP }
|	"="		{ EQ }
|	"."		{ DOT }
|	"&"		{ AMPERS }
|	"|"		{ PIPE }
|	":"		{ COLON }
|	"!"		{ EXCLAM }
|	";"		{ SEMI }
|	","		{ COMMA }
|	"("		{ LPAREN }
|	")"		{ RPAREN }
|	"["		{ LBRACK }
|	"]"		{ RBRACK }
|	"{"		{ LBRACE }
|	"}"		{ RBRACE }
|	"+"		{ PLUS }
|	"-"		{ MINUS }
|	"*"		{ STAR }
|	"/"		{ SLASH }
|	"%"		{ PERCENT }
|	"~"		{ TILD }
|	"^"		{ CIRC }
|	"@"		{ AROBAS }

|	eof		{ EOF }
|	_ as v		{ raise (BadChar v) }

(* eof_comment *)
and eof_comment = parse
	'\n'	{ new_line lexbuf; main lexbuf }
|	_		{ eof_comment lexbuf }

(* comment *)
and comment = parse
	"*/"	{ main lexbuf }

|	'\n'	{ new_line lexbuf; comment lexbuf }
|	_		{ comment lexbuf }

(* string recognition *)
and str res = parse
	"\""			{ res }
|	"\\" (_	as v)	{ str (appends res v) lexbuf }
|	_ as v			{ str (append res v) lexbuf }

(* character recognition *)
and chr res = parse
	"\'"			{ STRING_CONST res }
|	"\\" (_	as v)	{ chr (appends res v) lexbuf }
|	_ as v			{ chr (append res v) lexbuf }

and scan_line = parse
	digit+ as l	{ line := (int_of_string l) - 1 }
|	delim		{ scan_line lexbuf }
|	_			{ raise BadLine }

and scan_file = parse
	delim		{ scan_file lexbuf }
|	"\""		{ file := (str "" lexbuf) }
|	_			{ raise BadLine }

