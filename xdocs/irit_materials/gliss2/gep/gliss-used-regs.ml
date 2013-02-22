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

let max_read = ref 0
let max_write = ref 0
let has_custom = ref false
let extends: string list ref = ref []

exception UsedRegsError of string
let no_used_regs = "no_used_regs"


(** Generate a macro register name.
	@param p	Current processor.
	@param i	Register bank identifier.
	@return		Register macro name. *)
let reg_name p i = Printf.sprintf "%s_REG_%s" p (String.uppercase i)


(** Test if s is ended by es.
	@param s	String to test.
	@param es	Ending string.
	@return		True if es ends the string s, false else. *)
let ends_with s es =
	let sl = String.length s in
	let esl = String .length es in
	(*Printf.printf "String.sub \"%s\" %d %d" s (sl - esl) esl;*)
	(sl >= esl) &&
	es = (String.sub s (sl - esl) esl)


(** Collect register information.
	@return (count of register, associative list of (register identifier, templater object)) *)
let collect_register_info _ =
	let process id spec (cnt, lst) =
	let offset o = Templater.TEXT (fun out -> Printf.fprintf out "%d" o) in
	match spec with
	| Irg.REG(_, s, _, _) -> (cnt + s, (id, offset cnt)::lst)
	| _ -> (cnt, lst) in
	Irg.StringHashtbl.fold process Irg.syms (0, [])


(** Generate the number of a register.
	@param lst		Associative list of (register id, register offset).
	@param sym		Current register symbol.
	@param dict		Current dictionary.
	@return			New dictionary. *)
let generate_num lst sym dict =
	match sym with
	| Irg.REG(id, _, _, _) ->
		("used_reg_index", List.assoc id lst) :: dict
	| _ -> failwith "gliss-used-regs:generate_num"


(** Test if the expression is stateless.
	@param expr	Expression to test.
	@return		True if the expression is stateless, false else. *)
let rec stateless expr =
	match expr with
	| Irg.NONE -> true
	| Irg.COERCE (_, e) -> stateless e
	| Irg.FORMAT (_, args) -> List.fold_left (fun v e -> v && (stateless e)) true args
	| Irg.CANON_EXPR (_, _, args)  -> false
	| Irg.REF id -> stateless_id id
	| Irg.FIELDOF _ -> false
	| Irg.ITEMOF (_, id, idx) -> (stateless_id id) && (stateless idx)
	| Irg.BITFIELD (_, e, _, _) -> stateless e
	| Irg.UNOP (_, _, e) -> stateless e
	| Irg.BINOP (_, _, e1, e2) -> (stateless e1) && (stateless e2)
	| Irg.IF_EXPR (_, c, t, e) -> (stateless c) && (stateless t) && (stateless e)
	| Irg.SWITCH_EXPR (_, c, cs, d) -> (stateless c) && (stateless d) && (List.fold_left (fun v (_, e) -> v && (stateless e)) true cs)
	| Irg.CONST _ -> true
	| Irg.ELINE (_, _, e) -> stateless e
	| Irg.EINLINE _ -> false
	| Irg.CAST (_, e) -> stateless e
and stateless_id id =
	(match Irg.get_symbol id with
	| Irg.REG _ | Irg.MEM _  | Irg.VAR _ -> false
	| _ -> true)


(** Add a written register.
	@param rds	List of read registers.
	@param wrs	List of written registers.
	@param id	Register identifier.
	@param num	Register index.
	@return		(read register, written registers) *)
let add_write (rds, wrs) id num =
	if List.mem (id, num) wrs then (rds, wrs)
	else (rds, (id, num)::wrs)


(** Add a read register.
	@param rds	List of read registers.
	@param wrs	List of written registers.
	@param id	Register identifier.
	@param num	Register index.
	@return		(read register, written registers) *)
let add_read (rds, wrs) id num =
	if List.mem (id, num) rds then (rds, wrs)
	else ((id, num)::rds, wrs)


(** Collect used registers in statement.
	@param stat		Current statement.
	@param lst		List of collected used registers (identifier, number). *)
let collect info =
		let variable = ref false in
		let rec collect_stat stat lst =
		match stat with
			| Irg.NOP -> lst
			| Irg.SEQ (s1, s2) -> collect_stat s1 (collect_stat s2 lst)
			| Irg.EVAL id -> collect_call id lst
			| Irg.EVALIND _ -> failwith "gliss-used-regs: collect_stat"
			| Irg.SETSPE (l, e)
			| Irg.SET (l, e) -> collect_loc l (collect_expr e lst)
			| Irg.CANON_STAT (_, args) -> List.fold_left (fun l e -> collect_expr e l) lst args
			| Irg.ERROR _ -> lst
			| Irg.IF_STAT (c, t, e) -> collect_expr c (collect_stat t (collect_stat e lst))
			| Irg.SWITCH_STAT (c, cs, d) -> List.fold_left (fun l (_, s) -> collect_stat s l) (collect_expr c (collect_stat d lst )) cs
			| Irg.LINE (_, _, s) -> collect_stat s lst
			| Irg.INLINE _ -> lst

		and unalias id idx lst =
			match Irg.get_symbol id with
			| Irg.REG (_, _, _, attrs) ->
				(match Toc.get_alias attrs with
				| Irg.LOC_NONE -> collect_reg add_read lst id idx
				| loc -> collect_expr (Toc.unalias_expr id idx Irg.NONE Irg.NONE Irg.BOOL) lst)
			| _ -> lst

		and collect_expr expr lst =
			match expr with
			| Irg.NONE -> lst
			| Irg.COERCE (_, e) -> collect_expr e lst
			| Irg.FORMAT (_, args)
			| Irg.CANON_EXPR (_, _, args) -> List.fold_left (fun l e -> collect_expr e l) lst args
			| Irg.REF id -> unalias id Irg.NONE lst
			| Irg.FIELDOF (_, _, _) -> lst
			| Irg.ITEMOF (_, id, idx) -> unalias id idx (collect_expr idx lst)
			| Irg.BITFIELD (_, b, l, u) -> collect_expr b (collect_expr l (collect_expr u lst))
			| Irg.UNOP (_, _, e) -> collect_expr e lst
			| Irg.BINOP (_, _, e1, e2) -> collect_expr e1 (collect_expr e2 lst)
			| Irg.IF_EXPR (_, c, t, e) -> collect_expr c (collect_expr t (collect_expr e lst))
			| Irg.SWITCH_EXPR (_, c, cs, d) -> collect_expr c (collect_expr d (List.fold_left (fun l (_, e) -> collect_expr e l) lst cs))
			| Irg.CONST _ -> lst
			| Irg.ELINE (_, _, e) -> collect_expr e lst
			| Irg.EINLINE _ -> lst
			| Irg.CAST (_, e) -> collect_expr e lst

		and collect_loc loc lst =
			match loc with
			| Irg.LOC_NONE _ -> lst
			| Irg.LOC_REF (_, id, idx, l, u) ->
				(match Irg.get_symbol id with
				| Irg.REG (_, _, _, attrs) ->
					(match Toc.get_alias attrs with
					| Irg.LOC_NONE ->
						let lst = collect_expr l (collect_expr u (collect_expr idx lst)) in
						collect_reg add_write lst id idx
					| Irg.LOC_REF (t, id, idx, l, u) ->
						collect_stat (Toc.unalias_set info Irg.NOP id idx l u Irg.NONE) lst
					| Irg.LOC_CONCAT (_, l1, l2) ->
						collect_loc l1 (collect_loc l2 lst))
				| _ -> lst)
			| Irg.LOC_CONCAT (_, l1, l2) -> collect_loc l1 (collect_loc l2 lst)

		and collect_reg f lst id idx =
			match Irg.get_symbol id with
			| Irg.REG (_, s, _, _) ->
				if s = 1 then f lst id Irg.NONE else
				if stateless idx then f lst id idx else
				begin
					if not !variable then
						Printf.fprintf
							stderr
							"WARNING: instruction %s contains non-static register numbers: cannot generate safe register usage !\n"
							(Iter.get_user_id info.Toc.inst);
					variable := true;
					lst
				end
			| _ -> lst

		and collect_call name lst =
			if ends_with name no_used_regs then lst else
			if List.mem_assoc name info.Toc.calls then lst else
			let stat = Toc.get_stat_attr name in
			let before = info.Toc.calls in
			info.Toc.calls <- (name, "")::info.Toc.calls;
			let lst = collect_stat stat lst in
			info.Toc.calls <- before;
			lst in
		
	collect_call "action" ([], [])


(** Generate the code for the computing the register use.
	This code must use the macros add_read(i) and read_write(i) to
	add a read/write register of index i.
	@param inst		Current instruction.
	@param out		Current output. *)
let extract_regs inst out =
	let info = Toc.info () in
	info.Toc.out <- out;
	Toc.set_inst info inst;

	(* scan the prepared instructions *)
	let (rds, wrs) = collect info in

	(* records *)
	max_read := max !max_read (List.length rds);
	max_write := max !max_write (List.length wrs);
	
	(* build the instructrions *)
	let proc = String.uppercase info.Toc.proc in
	let gen op (id: string) idx =
		let name = reg_name proc id in
		Irg.CANON_STAT(op, [
			if idx = Irg.NONE then Irg.EINLINE name
			else Irg.CANON_EXPR (Irg.NO_TYPE, name, [idx])]) in
	let stats = Toc.seq
		(Toc.seq_list (List.map (fun (id, idx) -> gen "add_read" id idx) rds))
		(Toc.seq_list (List.map (fun (id, idx) -> gen "add_write" id idx) wrs)) in
	
	(* add help information *)
	(*let rec display_source stat =
		match stat with
		| Irg.LINE (file, line, _) -> Printf.fprintf out "\t/* %s:%d */\n" file line
		| Irg.SEQ (s1, _) -> display_source s1
		| _ -> () in
	display_source (Toc.get_stat_attr "action");*)
	let id = match inst with
		| Irg.AND_OP (n, _, _) -> n
		| _ -> "???" in
	Printf.fprintf out "\t/* %s (%s) */\n" (Iter.get_user_id inst) id;

	(* generate the instructions *)
	info.Toc.indent <- 1;
	let stats = Toc.prepare_stat info stats in
	Toc.declare_temps info;
	Toc.gen_stat info stats;
	Toc.cleanup_temps info;
	Toc.StringHashtbl.clear info.Toc.attrs





(** Generate the register list from a statement passed in "used_regs" attributes.
	These statements may include the following instructions:
	@li "read"(R) -- to design a read register,
	@li "write"(R) -- to design a written register,
	@li "count"(N, M) -- maximum number of read/written registers,
	@param inst		Current instruction.
	@param stat		Statements of the attribute.
	@param out		Stream to output to. *)
let compile_regs inst stat out =
	let info = Toc.info () in
	info.Toc.out <- out;
	Toc.set_inst info inst;
	let proc = String.uppercase info.Toc.proc in

	let rec process canon r =
		let error _ = Toc.error "argument should be a register !" in
		match r with
		| Irg.REF id -> 
			(match Irg.get_symbol id with
			| Irg.REG _ -> Irg.CANON_STAT (canon, [Irg.EINLINE (reg_name proc id)])
			| _ -> error ())
		| Irg.ITEMOF (_, id, idx) ->
			(match Irg.get_symbol id with
			| Irg.REG _ -> Irg.CANON_STAT (canon, [Irg.CANON_EXPR (Irg.NO_TYPE, reg_name proc id, [idx])])
			| _ -> error())
		| Irg.ELINE (_, _, e) -> process canon e
		| _ -> error() in
				
	let count rcnt wcnt =
		try
			let cnt = Sem.to_int (Sem.eval_const rcnt) in
			if cnt > !max_read then max_read := cnt;
			let cnt = Sem.to_int (Sem.eval_const wcnt) in
			if cnt > !max_write then max_write := cnt;
		with Sem.SemError msg ->
			Toc.error (Printf.sprintf "can not evaluate count: %s" msg) in

	let rec scan_stat stat =
		match stat with
		| Irg.NOP
		| Irg.EVALIND _
		| Irg.ERROR _
		| Irg.SETSPE _
		| Irg.INLINE _
		| Irg.SET _ -> stat
		| Irg.EVAL id ->
			scan_call id; stat
		| Irg.SEQ (s1, s2) ->
			Irg.SEQ(scan_stat s1, scan_stat s2)
		| Irg.CANON_STAT ("read", args) ->
			Toc.seq_list (List.map (process "add_read") args)
		| Irg.CANON_STAT ("write", args) ->
			Toc.seq_list (List.map (process "add_write") args)
		| Irg.CANON_STAT ("count", [rcnt; wcnt]) ->
			count rcnt wcnt; Irg.NOP
		| Irg.CANON_STAT ("count", _) ->
			Toc.error "\"count\" canonical must be passed two integer constant arguments !"
		| Irg.CANON_STAT _ -> stat
		| Irg.IF_STAT (c, t, e) ->
			Irg.IF_STAT (c, scan_stat t, scan_stat e)
		| Irg.SWITCH_STAT (c, cs, d) ->
			Irg.SWITCH_STAT (c, List.map (fun (c, s) -> (c, scan_stat s)) cs, scan_stat d)
		| Irg.LINE (f, l, s) ->
			Toc.locate_error f l (fun _ -> Irg.LINE (f, l, scan_stat s)) ()
	
	and scan_call name =
		if 	not (List.mem_assoc name info.Toc.calls) then
		begin
			let stat = Toc.StringHashtbl.find info.Toc.attrs name in
			let before = info.Toc.calls in
			info.Toc.calls <- (name, "")::info.Toc.calls;
			let stat = scan_stat stat in
			Toc.StringHashtbl.add info.Toc.attrs name stat;
			info.Toc.calls <- before
		end in
	
	info.Toc.indent <- 1;
	Toc.find_recursives info "used_regs";
	Toc.prepare_call info "used_regs";
	scan_call "used_regs";
	Toc.declare_temps info;
	Toc.gen_call info "used_regs";
	Toc.cleanup_temps info;
	Toc.StringHashtbl.clear info.Toc.attrs


(** Generate the $(used_regs) attribute.
	@param inst		Current instruction.
	@param out		Stream to output to. *)
let gen_used_regs inst out =
	try
		match Irg.get_symbol "used_regs" with
		| Irg.ATTR (Irg.ATTR_STAT (_, stat)) -> compile_regs inst stat out
		| _ -> raise (Toc.Error "when defined, used_regs attribute must contain statements")
	with Irg.Symbol_not_found _ ->
		extract_regs inst out


(** Build the instruction dictionary adding the definition of $(use_regs).
	@param inst		Current instruction.
	@param dict		Input dictionary.
	@return			Output dictionary. *)
let get_instruction inst dict =
	("used_regs", Templater.TEXT (gen_used_regs inst)) :: dict


let _ =
	(*let display_error msg = Printf.fprintf stderr "ERROR: %s\n" msg in*)
	(*try*)
		App.run
			[ ("-e", Arg.String (fun arg -> extends := arg::!extends), "extension files") ]
			"SYNTAX: gliss-used-regs [options] NML_FILE\n\tGenerate functions to retrieve register use."
			(fun info ->

				(* download the extensions *)
				List.iter IrgUtil.load !extends;

				(* generate used registers *)
				let maker = App.maker () in
				let (cnt, lst) = collect_register_info () in
				maker.App.get_register <- generate_num lst;
				maker.App.get_instruction <- get_instruction;
				let dict = 
					("used_regs_count", Templater.TEXT (fun out -> Printf.fprintf out "%d" cnt)) ::
					("used_regs_read_max", Templater.TEXT (fun out -> Printf.fprintf out "%d" !max_read)) ::
					("used_regs_write_max", Templater.TEXT (fun out -> Printf.fprintf out "%d" !max_write)) ::
					(App.make_env info maker) in

				Templater.generate dict "used_regs.c" (info.Toc.spath ^ "/used_regs.c");
				Templater.generate dict "used_regs.h" (info.Toc.hpath ^ "/used_regs.h")
			)
	(*with 
	| CommandError msg ->
		display_error msg*)


