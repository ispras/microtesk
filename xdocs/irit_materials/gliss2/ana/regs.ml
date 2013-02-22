(*
 * $Id$
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

open Absint
open Irg

let eat f = (fun _ -> f)
let swap f = (fun a b -> f b a)


(* CODE GENERATION

	REQUIRED RESULTS
	* computability analysis [ok]
		kind of content of register: STATIC, DYNAMIC, FUZZY
		static_expr: expr -> BOOL (all used register are static)
		dynamic_expr: expr -> BOOL (one of used register is dynamic)
	* fuzzy analysis
		may have a register a fuzzy value
		maybe_fuzzy: ID x expr -> BOOL (in one state, it is fuzzy)
		fuzzname: ID -> ID (name of the associated fuzzy boolean)

	* involvement analysis
	    * register transfer structure
			- fuzzy_used
			- assigned_regs
			- fuzzy_set
			- used_regs
			regtrans: stat -> transfer_structure
	    * dependence analysis -- analysis dependents
	    	dep: stat -> BOOL x 2^reg
		* use-def analysis
			usedef: stat -> reg -> 2^stat
		* containment analysis -- which if contains an assignment
			contain: stat -> 2^stat
		involved: 2^stat
			transitivie closure of set of s /
			- ]s' . dep(s') = (T, regs) /\ s in usedef(s', r)
			- ]s' . s' in involed /\ s in usedef(s', r) /\ r in regtrans(used_regs(s'))
			- ]s' . s' in involved /\ s in contain(s')

	T(s) =
		{ Ts(s); Tu(s) }

	Ts: stat -> stat
		problem specific generation (usually canonical calls)

	Tu: stat -> stat
		usage specific generation

		Tu[s] =
			if involved(s) then Tus[s] else NOP

		let protect e s1 s2 =
			let cond = AND{x[i] in e / maybe_fuzzy(x, i)} fuzzname(x)[i] in
			IF(cond, s1, s2)

		Tus[x[i] <- e] =
			let all_fuzzy x =
					foreach i in 0..|x|-1 do
						fuzzname(x)[i] <- true

			let assign x i e =
				SEQ(
					x[i] <- e,
					if maybe_fuzzy(x, i) then fuzzname(x)[i] <- false else NOP
				)

			let assign_fuzzy x i =
				fuzzname(x)[i] <- true

			if not static_expr(i) then
				all_fuzzy(x)
			else if static_expr(e) then
				assign(x, i, e)
			else if dynamic_expr(e)
				assign_fuzzy(x, i)
			else
				protect(e, assign(x, i, e), assign_fuzzy(x, i)

		Tus[IF(c, s1, s2)] =
			let join s1 s2 =
				SEQ(
					join_begin,
					Tu[s1],
					join_next,
					Tu[s2],
					join_end
				)

			if dynamic_expr(c) then
				join s1 s2
			else if static_expr(c) then
				IF(c, Tu[s1], Tu[s2])
			else
				protect(c,
					IF(c, Tu[s1], Tu[s2]),
					join s1 s2)

		Tus[s1; s2] = Tu[s1]; Tu[s2]

		Tus[ID(e1, ..., en) =
			let je = join(e1, ..., en) in
			if dynamic_expr(ej) then NOP else
			if static_expr(ej) then ID(e1, ..., en) else
			protect(ej, ID(e1, ..., en))

	ANALYSIS INTERFACE
		Ts: stat -> stat	analysis dependent generation
		prolog: () -> stat	before a join first statement
		next: () -> stat	before a join second statement
		epilog: () -> stat	after the join

	EXAMPLE: target_address
		Ts[PC <- e] =
			if static(e) then {_result <- ONE(e)} else {_result <- ANY}
		Ts[*] = NOP
		prolog = { }
		next = { _result_i <- _result}
		epilog =
			IF(_result == NONE, { _result <- _result_i; },
			IF(_result_i == NONE, { },
			{ _result = ANY }))

*)


(* pair hashtable *)
module PairOrdering = struct
	type t = string * int
	let compare t1 t2 = compare t1 t2
end
module PairMap = Map.Make(PairOrdering)


(** Build the list of used variables in the given expression.
	Add a special register (r, -1) if some indexes are not constant.
	@param e	Expression to examine.
	@return		Set of all used variables. *)
let used_in_expr e =
	let rec work s e =
		match e with
		| NONE -> s
		| COERCE (_, e) -> work s e
		| FORMAT (_, es) -> work_list s es
		| CANON_EXPR (_, _, es) -> work_list s es
		| REF id -> RegSet.add id 0 s
		| FIELDOF _ -> s
		| ITEMOF (_, id, ix) ->
			(try RegSet.add id (Sem.to_int (Sem.eval_const ix)) s
			with Sem.SemError _ -> work (RegSet.add_all id s) ix)
		| BITFIELD (_, e1, e2, e3) -> work_list s [e1; e2; e3]
		| UNOP (_, _, e) -> work s e
		| BINOP (_, _, e1, e2) -> work_list s [e1; e2]
		| IF_EXPR (_, e1, e2, e3) -> work_list s [e1; e2; e3]
		| SWITCH_EXPR (_, c, cs, d) ->
			work_list s (c::d::(fst (List.split cs)))
		| CONST _ -> s
		| ELINE (_, _, e) -> work s e
		| EINLINE _ -> s
	and work_list s es =
		match es with
		| [] -> s
		| e::t -> work (work_list s t) e in
	work RegSet.empty e


(** Build the list of used variables in the given location.
	Add a special register ("", 0) if some indexes are not constant.
	@param l	Expression to examine.
	@return		(all located variables, all used variables). *)
let used_in_locs l =
	let rec work (ul, uv) l =
		match l with
		| LOC_NONE -> (ul, uv)
		| LOC_REF (_, id, Irg.NONE, e1, e2) ->
			(RegSet.add id 0 ul, RegSet.union (used_in_expr e1) (used_in_expr e2))
		| LOC_REF (_, id, ix, e1, e2) ->
			let ul =
				(try RegSet.add id (Sem.to_int (Sem.eval_const ix)) ul
				with Sem.SemError _ -> RegSet.add_all id ul) in
			(ul, RegSet.union (used_in_expr e1) (used_in_expr e2))
		| LOC_CONCAT (_, l1, l2) ->
			work (work (ul, uv) l1) l2 in
	work (RegSet.empty, RegSet.empty) l


(** Compute the worst compatibility for each variable reference
	all over the full program.
	@param comps	Hashtable of all computed computabilities.
	@return			Map of (variable reference, worst computability). *)
let compute_fuzzy comps =

	let process set (id, ix, comp) =
		if comp = Comput.FUZZY
		then RegSet.add id ix set
		else set in

	Absint.StatHashtbl.fold
		(fun _ comp map -> List.fold_left process map comp)
		comps
		RegSet.empty


(** Compute the container stack of each instruction.
	@param attr		Attribute containing action.
	@return			Map containing (statement, container stack) pairs. *)
let compute_contain attr =

	let rec process_call stack attr cont map =
		if List.mem attr stack then map
		else process_stat (attr::stack) (get_stat attr) cont map

	and process_stat stack stat cont map =
		let map = Absint.StatMap.add stat cont map in
		let cont = stat::cont in
		match stat with
		| Irg.SEQ (s1, s2) ->
			process_stat stack s2 cont (process_stat stack s1 cont map)
		| Irg.IF_STAT (_, s1, s2) ->
			process_stat stack s2 cont (process_stat stack s1 cont map)
		| Irg.LINE (_, _, stat) ->
			process_stat stack stat cont map
		(* !!TODO!! SWITCH_STAT *)
		| EVAL name ->
			process_call stack name cont map
		| _ -> map in

	process_call [] attr [] Absint.StatMap.empty


(** Compute the code of the dependent part of the analysis.
	@param f	Function to compute dependent part.
	@param attr	Attribute to work on. *)
let compute_deps f attr =

	let rec process_call stack attr map =
		if List.mem attr stack then map
		else process_stat (attr::stack) (get_stat attr) map

	and process_stat stack stat map =
		let dep = f stat in
		let map =
			if dep = NOP then map
			else StatMap.add stat (f stat) map in
		match stat with
		| IF_STAT (_, s1, s2)
		(*| SWITCH_STAT of expr * (expr * stat) list * stat*)
		| SEQ (s1, s2) ->
			process_stat stack s2 (process_stat stack s1 map)
		| LINE (_, _, s) ->
			process_stat stack s map
		| _ -> map in

	process_call [] attr Absint.StatMap.empty



(* extension system *)
let extend_map os ds map =
	Absint.StatMap.add ds (Absint.StatMap.find os map) map

let extend_hashtbl os ds ht =
	Absint.StatHashtbl.add ht ds
		(Absint.StatHashtbl.find ht os);
	ht

let extend_both f1 f2 os ds (d1, d2) =
	(f1 os ds d1, f2 os ds d2)

let rec extend f os ds d =
	let d = f os ds d in
	match ds with
	| Irg.SEQ(s1, s2) ->
		extend f os s2 (extend f os s1 d)
	| Irg.IF_STAT(_, s1, s2) ->
		extend f os s2 (extend f os s1 d)
	(*| Irg.SWITCH_STAT ... !!TODO!! *)
	| Irg.LINE (_, _, s) -> extend f os s d
	| _ -> d



(** Add to the given set the registers used inside it.
	@param expr		Expression to process.
	@param set		Register set.
	@return			Set augmented with used registers. *)
let rec add_expr expr set =
	match expr with
	| ELINE (_, _, e)
	| UNOP (_, _, e)
	| COERCE (_, e) -> add_expr e set
	| FORMAT (_, args)
	| CANON_EXPR (_, _, args) -> add_multi args set
	| REF id -> RegSet.add id 0 set
	| ITEMOF (_, id, ix) ->
		(try RegSet.add id (Sem.to_int (Sem.eval_const ix)) set
		with Sem.SemError _ -> RegSet.add_all id set)
	| BINOP (_, _, e1, e2) -> add_multi [e1; e2] set
	| IF_EXPR (_, e1, e2, e3)
	| BITFIELD (_, e1, e2, e3) -> add_multi [e1; e2; e3] set
	| SWITCH_EXPR(_, c, cs, d) ->
		add_multi (c::d::(snd (List.split cs))) set
	| _ -> set


(** Perform add_expr on the passed list of expressions.
	@param exprs	Expression to process.
	@param set		Set to insert into.
	@return			Resulting register set. *)
and add_multi exprs set =
	List.fold_left (fun s e -> add_expr e s) set exprs


(** Add registers used in the location to the passed register set.
	@param loc	Location to work on.
	@param set	Set to insert into.
	@return		Resulting set. *)
let rec add_loc loc set =
	match loc with
	| LOC_NONE -> set
	| LOC_CONCAT (_, l1, l2) ->
		add_loc l2 (add_loc l1 set)
	| LOC_REF (_, _, ix, lo, up) ->
		add_multi [ix; lo; up] set



(** Compute statement involved in the slicing.
	@param comps	Computability information.
	@param conts	Container information.
	@param uds		Use-def information.
	@param deps		Problem-dependent code.
	@return			Set of used statements. *)
let compute_involved comps conts uds deps =
	let comps, (conts, uds) =
		Absint.StatMap.fold
			(extend (extend_both extend_hashtbl (extend_both extend_map extend_hashtbl)))
			deps
			(comps, (conts, uds)) in

	let member_of stat (_, set) =
		Absint.StatSet.mem stat set in

	let add stat (todo, set) =
		(*print_string "adding "; Absint.print_stat stat; print_string "\n";*)
		(stat::todo, Absint.StatSet.add stat set) in

	let smart_add stat base =
		if stat = NOP then base else
		if member_of stat base then base else add stat base in

	let add_conts conts base =
		(*List.iter (fun cont -> Printf.printf "adding container (%d)\n" (Absint.number_of cont)) conts;*)
		List.fold_right smart_add conts base in

	let process_trans stat base set =
		let ud = Absint.StatHashtbl.find uds stat in
		List.fold_left
			(fun base (r, i) ->
				try Usedefs.StatSet.fold smart_add (Usedefs.State.get ud r i) base
				with Not_found -> base)
			base
			set in

	let process_expr stat base expr =
		process_trans stat base (add_expr expr RegSet.empty) in

	let process_loc stat base loc =
		process_trans stat base (add_loc loc RegSet.empty) in

	let process_stat stat base =
		(*print_string "adding container\n";*)
		let base =
			try add_conts (StatMap.find stat conts) base
			with Not_found -> base in
		(*print_string "adding definitions\n";*)
		match stat with
		| CANON_STAT (_, args) ->
			List.fold_left (process_expr stat) base args
		| SET (l, e)
		| SETSPE (l, e) ->
			process_expr stat (process_loc stat base l) e
		(* !!TODO!! SWITCH_STAT *)
		| _ -> base in

	let rec process_dep (stat: Irg.stat) (todo, set) =
		(*print_string "process_dep ";
		Absint.print_stat stat;
		print_string "\n";*)
		let base = (todo, Absint.StatSet.add stat set) in
		match stat with
		| NOP -> failwith "nop forbidden !"
		| IF_STAT (_, s1, s2)
		| SEQ (s1, s2) -> process_dep s2 (process_dep s1 base)
		| LINE (_, _, s) -> process_dep s base
		(* !!TODO!! SWITCH_STAT *)
		| _ -> process_stat stat base in

	let rec process (todo, set) =
		(*print_string "todo = ";
		List.iter (fun stat -> Printf.printf "(%d) " (Absint.number_of stat)) todo;
		print_string "\n";*)
		match todo with
		| [] -> (todo, set)
		| stat::todo ->
			(*Printf.printf "PROCESSING (%d)\n" (Absint.number_of stat);*)
			let base = (todo, set) in
			let base = match stat with
				| IF_STAT(c, s1, s2) ->
					if member_of stat base
					then process_expr stat base c
					else base
				(* !!TODO!! SWITCH_STAT *)
				| _ -> process_stat stat base in
			process base in

	let base = ([], Absint.StatSet.empty) in
	let base = Absint.StatMap.fold (eat process_dep) deps base in
	(*print_string "initial = ";
	Absint.print_stat_set (snd base);*)
	snd (process base)


(** Transoformation signature. *)
type vars = (string * string) PairMap.t
module type TRANSFORMATION = sig

	val use: Toc.info_t -> stat-> stat

	val merge: Toc.info_t -> stat * stat * stat

end


(** Factory for transformations.
	@param T	Transformation to apply. *)
module Make(T: TRANSFORMATION) = struct

	let seq s1 s2 =
		if s1 = NOP then s2 else
		if s2 = NOP then s1 else
		SEQ(s1, s2)

	let seqs ss = List.fold_left seq NOP ss

	let transform info inst =
		Printf.printf "===> %s <===\n" (Iter.get_name inst);

		(* STEP 0: preparation *)
		print_string "STEP 0: preparation\n\n";
		Toc.set_inst info inst;
		Toc.find_recursives info "action";
		Toc.prepare_call info "action";

		(* move symbols from info IRG map *)
		Toc.StringHashtbl.iter
			(fun name stat -> Irg.add_attr (ATTR_STAT (name, stat)))
			info.Toc.attrs;
		Absint.number_attr "action";

		(* STEP 1: computability *)
		print_string "STEP 1: computability analysis\n";
		let comps, dom = Comput.analyze inst "action" in
		Comput.dump (comps, dom) "action";
		print_string "\n\n";

		(* STEP 2: fuzzy analysis *)
		print_string "STEP 2: fuzzy analysis\n";
		let fuzzy = compute_fuzzy comps in
		List.iter (fun (r, i) -> Printf.printf "%s[%d], " r i) fuzzy;
		print_string "\n\n";

		(* STEP 3: containment analysis *)
		print_string "STEP 3: containment analysis\n";
		let conts = compute_contain "action" in
		Absint.StatMap.iter
			(fun stat cont ->
				Printf.printf "(%d) in [" (Absint.number_of stat);
				List.iter
					(fun stat -> Printf.printf "(%d)" (Absint.number_of stat))
					cont;
				print_string "]\n"
			)
			conts;
		print_string "\n";

		(* STEP 4: use-def analysis *)
		print_string "STEP 4: use-defs analysis\n";
		let uds, dom = Usedefs.analyze "action" in
		Usedefs.dump (uds, dom) "action";
		print_string "\n\n";

		(* STEP 5: involvement analysis *)
		print_string "STEP 5: involvment analysis\n";
		let deps = compute_deps (T.use info) "action" in
		let invs = compute_involved comps conts uds deps in
		Absint.print_stat_set invs;
		print_string "\n\n";

		(* STEP 6: transform the code *)
		print_string "STEP 6: transform the code\n\n";
		let compute_worst comp regs =
			let worst c1 c2  =
				match c1, c2 with
				| Comput.STATIC, c
				| c, Comput.STATIC
				| Comput.FUZZY, c
				| c, Comput.FUZZY -> c
				| _, _ -> Comput.DYNAMIC in
			List.fold_left
				(fun c (r, i) -> worst c (Comput.State.get comp r i))
				Comput.STATIC regs in

		let fuzzy_cond comp rds =
			List.fold_left
				(fun e (r, i) ->
					if not (RegSet.contains r i fuzzy) then e else
					let ef = EINLINE (Printf.sprintf "__f_test(%s, %d)" r i) in
					if e = NONE then ef else BINOP (BOOL, AND, e, ef))
				NONE rds in

		let protect (stat: Irg.stat) (rds: RegSet.t) do_s do_f do_d =
			let comp = Absint.StatHashtbl.find comps stat in
			match compute_worst comp rds with
			| Comput.STATIC -> do_s stat
			| Comput.FUZZY -> IF_STAT (fuzzy_cond comp rds, do_s stat, do_f stat)
			| Comput.DYNAMIC -> do_d stat in

		let rec fuzzy_clear stat loc =
			let comp = Absint.StatHashtbl.find comps stat in
			let clear id ix = CANON_STAT ("__f_clear", [EINLINE(id); ix]) in
			let clear_all id =
				let rec work id ix n =
					if ix = n then NOP else
					seq
						(clear id (CONST (CARD(32), CARD_CONST (Int32.of_int ix))))
						(work id (ix + 1) n)  in
				work id 0 (RegSet.size id) in
			match loc with
			| LOC_NONE ->
				NOP
			| LOC_CONCAT(_, l1, l2) ->
				seq (fuzzy_clear stat l1) (fuzzy_clear stat l2)
			| LOC_REF(_, id, ix, lo, up) ->
				let regs = add_expr ix RegSet.empty in
				match compute_worst comp regs with
				| Comput.STATIC -> clear id ix
				| Comput.FUZZY ->
					IF_STAT (fuzzy_cond comp regs, clear id ix, clear_all id)
				| Comput.DYNAMIC -> clear_all id in

		let rec fuzzy_set stat loc =
			match loc with
			| LOC_NONE ->
				NOP
			| LOC_CONCAT(_, l1, l2) ->
				seq (fuzzy_set stat l1) (fuzzy_set stat l2)
			| LOC_REF(_, id, ix, _, _) ->
				CANON_STAT ("__f_set", [EINLINE(id); ix]) in

		let rec scan_expr expr =
			match expr with
			| COERCE (_, e) -> scan_expr e
			| FORMAT (_, args)
			| CANON_EXPR (_, _, args) -> scan_multi args
			| IF_EXPR (_, e1, e2, e3)
			| BITFIELD (_, e1, e2, e3) -> scan_multi [e1; e2; e3]
			| ELINE (_, _, e)
			| UNOP (_, _, e) -> scan_expr e
			| BINOP (_, _, e1, e2) -> scan_multi [e1; e2]
			| SWITCH_EXPR (_, e1, cs, e2) ->
				scan_multi (e1::e2::(snd (List.split cs)))
			| _ -> ()
		and scan_multi exprs =
			List.iter scan_expr exprs in
(*| 	REF of string
| 	ITEMOF of type_expr * string * expr*)

		let rec trans_stat stat =
			seq
				(try Absint.StatMap.find stat deps with Not_found -> NOP)
				(if not (Absint.StatSet.mem stat invs) then NOP else
				match stat with
				| SEQ (s1, s2) ->
					seq (trans_stat s1) (trans_stat s2)
				| IF_STAT (c, s1, s2) ->
					let s1t = trans_stat s1 in
					let s2t = trans_stat s2 in
					let ift = IF_STAT (c, s1t, s2t) in
					protect stat (add_expr c RegSet.empty)
						(eat ift)
						(fun _ ->
							let (start, next, stop) = T.merge info in
							seqs [start;  s1; next; s2; stop])
						(eat NOP)
				| LINE (f, l, s) -> LINE(f, l, trans_stat s)
				| SET(l, e)
				| SETSPE(l, e) ->
					protect stat (add_expr e (add_loc l RegSet.empty))
						(fun stat -> seq stat (fuzzy_set stat l))
						(fun stat -> fuzzy_clear stat l)
						(fun stat -> fuzzy_clear stat l)
				| EVAL name ->
					trans_call name; stat
				(* !!TODO!! SWITCH_STAT *)
				| _ -> stat)

		and trans_call name =
			let stat = get_stat name in
			let stat = trans_stat stat in
			print_statement stat;
			Irg.add_attr (ATTR_STAT (name, stat)) in

		trans_call "action";
		print_string "\n";

		(* move symbols from IRG map to info *)
		Toc.StringHashtbl.iter
			(fun name _ ->
				Toc.StringHashtbl.replace info.Toc.attrs name (get_stat name);
				Irg.rm_symbol name)
			info.Toc.attrs;

		(* STEP 7: generate the code *)
		print_string "STEP 7: generate the code\n\n";
		Toc.declare_temps info;
		Toc.gen_call info "action";

		(* final cleanup *)
		Toc.cleanup_temps info;
		Toc.StringHashtbl.clear info.Toc.attrs

end


(* write analysis *)
module WriteTransformer = struct
	let uniq_id = ref 0
	let new_id _ =
		incr uniq_id;
		Printf.sprintf "__wregs_%d" !uniq_id

	let is_reg name =
		match get_symbol name with
		| REG _ -> true
		| _ -> false

	let rec look l =
		match l with
		| LOC_NONE -> NOP
		| LOC_CONCAT (_, l1, l2) ->
			let s1 = look l1 in
			let s2 = look l2 in
			if s1 = NOP then s2 else
			if s2 = NOP then s1 else
			SEQ (s1, s2)
		| LOC_REF (_, id, ix, _, _) ->
			if not (is_reg id) then NOP else
			CANON_STAT("__wregs_add", [EINLINE (String.uppercase id); ix])

	let use _ stat =
		match stat with
		| SET (l, e)
		| SETSPE (l, e) -> look l
		| _ -> NOP

	let merge (info: Toc.info_t) =
		let before = new_id () in
		let second = new_id () in
		(
			INLINE (Printf.sprintf "__wregs_copy(%s, _result);" before),
			INLINE (Printf.sprintf "%s = _result; _result = %s;" second before),
			INLINE (Printf.sprintf "__wregs_join(_result, %s); __wregs_free(%s); " second second)
		)

end


module Transformer = Make(WriteTransformer)
let _ =

	App.run
		[]
		"SYNTAX: regs NMP_FILE\n\tgenerate code to get used registers\n"
		(fun info ->
			Iter.iter
				(fun _ inst -> Transformer.transform info inst)
				()

		)
