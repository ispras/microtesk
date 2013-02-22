(*
 * GLISS2 -- disassembly gnerator
 * Copyright (c) 2008, IRIT - UPS <casse@irit.fr>
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

exception CommandError of string

(* library path *)
let paths = [
	Config.install_dir ^ "/lib/gliss/lib";
	Config.source_dir ^ "/lib";
	Sys.getcwd ()]


(* argument list *)
let out = ref "disasm.c"
let command = ref false
let options = [
	("-o", Arg.Set_string out, "output file");
	("-c", Arg.Set command, "generate also the command")
]


(** Generate code to perform disassembly.
	@param out	Output channel.
	@param inst	Current instruction.
	@param expr	Syntax expression.
	@raise Error	If there is an unsupported syntax expression. *)
let rec gen_disasm info inst expr =

	let str text = Irg.CONST (Irg.STRING, Irg.STRING_CONST(text, false, Irg.NO_TYPE)) in

	let format fmt args s i =
		if s >= i then
			Irg.NOP
		else
			let fmt = String.sub fmt s (i - s) in
			Irg.CANON_STAT ("__buffer += sprintf", (Irg.REF "__buffer")::(str fmt)::args) in

	let rec scan fmt args s used i =
		match args with
		| [] -> format fmt (List.rev used) s (String.length fmt)
		| hd::tl ->
			if i >= (String.length fmt) then format fmt used s i else
			if fmt.[i] <> '%' then scan fmt args s used (i + 1) else
			if i + 1 >= String.length fmt then format fmt used s i else
			if fmt.[i + 1] != 's' then
				if fmt.[i + 1] = '%' then scan fmt args s used (i + 2)
				else scan fmt tl s (hd::used) (i + 2)
			else
				Irg.SEQ (format fmt used s i,
					Irg.SEQ(
						process hd,
						scan fmt tl (i + 2) [] (i + 2)))

	and process expr =
		check expr;
		match expr with
		| Irg.FORMAT (fmt, args) ->
			scan fmt args 0 [] 0
		| Irg.CONST (_, Irg.STRING_CONST(s, false, _)) ->
			Irg.CANON_STAT ("__buffer += sprintf", [Irg.REF "__buffer"; str s])
		| Irg.IF_EXPR (_, c, t, e) ->
			Irg.IF_STAT(c, process t, process e)
		| Irg.SWITCH_EXPR(_, c, cases, def) ->
			Irg.SWITCH_STAT(
				c,
				List.map (fun (c, e) -> (c, process e)) cases,
				if def <> Irg.NONE then process def else Irg.NOP)
		| Irg.REF _
		| Irg.NONE
		| Irg.CANON_EXPR _
		| Irg.FIELDOF _
		| Irg.ITEMOF _
		| Irg.BITFIELD _
		| Irg.UNOP _
		| Irg.BINOP _
		| Irg.CONST _
		| Irg.COERCE _
		| Irg.EINLINE _
		| Irg.CAST _ ->
			Toc.error_on_expr (Printf.sprintf "bad syntax expression in instruction %s" (Iter.get_user_id inst)) expr
		| Irg.ELINE (file, line, e) ->
			Toc.locate_error file line (gen_disasm info inst) e

	and check_symbol id =
		match Irg.get_symbol id with
		| Irg.REG _ | Irg.MEM _ -> Toc.error_on_expr (Printf.sprintf "\"%s\" forbidden in syntax attribute" id) expr
		| Irg.LET _ | Irg.PARAM _  | _ -> ()

	and check expr =
		match expr with
		| Irg.NONE -> ()
		| Irg.COERCE (_, expr) -> check expr
		| Irg.FORMAT (_, args)
		| Irg.CANON_EXPR (_, _, args) -> List.iter check args
		| Irg.REF id -> check_symbol id
		| Irg.FIELDOF (_, id, _) -> check_symbol id
		| Irg.ITEMOF (_, id, expr) -> check_symbol id; check expr
		| Irg.BITFIELD (_, b, l, u) -> check b; check l; check u
		| Irg.UNOP (_, _, arg) -> check arg
		| Irg.BINOP (_, _, arg1, arg2) -> check arg1; check arg2
		| Irg.IF_EXPR (_, c, t, e) -> check c; check t; check e
		| Irg.SWITCH_EXPR (_, c, cs, d) -> check c; check d; List.iter (fun (_, e) -> check e) cs
		| Irg.CONST _ -> ()
		| Irg.ELINE (f, l, e) -> Toc.locate_error f l check e
		| Irg.EINLINE _ -> ()
		| Irg.CAST (_, e) -> check e in

	(* !!DEBUG!! *)
	(*print_string "gen_disasm:";
	Irg.print_expr expr;
	print_char '\n';*)
	process expr


(** Perform the disassembling of the given instruction.
	@param inst		Instruction to get syntax from.
	@param out		Output to use. *)
let disassemble inst out info =
	info.Toc.out <- out;
	Toc.set_inst info inst;

	(* get syntax *)
	let syntax =
		try
			match Iter.get_attr inst "syntax" with
			  Iter.STAT _ -> raise (Toc.Error "syntax must be an expression")
			| Iter.EXPR e -> e
		with Not_found -> raise (Toc.Error "no attribute") in

	(* disassemble *)
	let params = Iter.get_params inst in
	Irg.param_stack params;
	let stats = Toc.prepare_stat info (gen_disasm info inst syntax) in
	Toc.declare_temps info;
	Toc.gen_stat info stats;
	Toc.cleanup_temps info;
	Irg.param_unstack params


let _ =
	let display_error msg = Printf.fprintf stderr "ERROR: %s\n" msg in
	try
		App.run
			options
			"SYNTAX: gep [options] NML_FILE\n\tGenerate code for a simulator"
			(fun info ->
				Irg.add_symbol "__buffer" (Irg.VAR ("__buffer", 1, Irg.NO_TYPE));

				(* generate disassemble source *)
				let maker = App.maker () in
				maker.App.get_instruction <- (fun inst dict ->
					("disassemble", Templater.TEXT (fun out -> disassemble inst out info)) :: dict);
				let dict = App.make_env info maker in
				if not !App.quiet then (Printf.printf "creating \"%s\"\n" !out; flush stdout);
				Templater.generate dict "disasm.c" !out;

				(* generate the command *)
				if !command then
					begin
						try
							let path = App.find_lib "disasm/disasm.c" paths in
							App.makedir "disasm";
							App.replace_gliss info
								(path ^ "/" ^ "disasm/disasm.c")
								("disasm/" ^ info.Toc.proc ^ "-disasm.c" );
							Templater.generate_path
								[ ("proc", Templater.TEXT (fun out -> output_string out info.Toc.proc)) ]
								(path ^ "/disasm/Makefile")
								"disasm/Makefile"
						with Not_found ->
							raise (CommandError  "no template to make disasm program")
					end
			)
	with 
	| Toc.Error msg ->
		display_error msg
	| CommandError msg ->
		display_error msg
	| Toc.LocError (file, line, f) ->
		Printf.fprintf stderr "ERROR: %s:%d: " file line;
		f stderr;
		output_char stderr '\n'
	| Toc.PreError f ->
		output_string stderr "ERROR: ";
		f stderr;
		output_char stderr '\n'


