(*
 * $Id$
 * Copyright (c) 2009, IRIT - UPS <casse@irit.fr>
 *
 * This file is part of GLISS v2.
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
open Absint

module type ANALYSIS = sig
	val use: stat -> bool * RegSet.t
end

module ComputeState = Comput.State

(** Build the list of used variables in the given expression.
	Add a special register ("", 0) if some indexes are not constant.
	@param e	Expression to examine.
	@return		Set of all used variables. *)
let used_vars e =
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
			with Sem.SemError _ -> RegSet.add "" 0 s)
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
let used_locs l =
	let rec work (ul, uv) l =
		match l with
		| LOC_NONE -> (ul, uv)
		| LOC_REF (_, id, Irg.NONE, e1, e2) ->
			(RegSet.add id 0 ul, RegSet.union (used_vars e1) (used_vars e2))
		| LOC_REF (_, id, ix, e1, e2) ->
			let ul =
				(try RegSet.add id (Sem.to_int (Sem.eval_const ix)) ul
				with Sem.SemError _ -> RegSet.add "" 0 ul) in
			(ul, RegSet.union (used_vars e1) (used_vars e2))
		| LOC_CONCAT (_, l1, l2) ->
			work (work (ul, uv) l1) l2 in
	work (RegSet.empty, RegSet.empty) l


(** Test if there exists one location given set  required according
	to the use set. Returns also false if it contains an undefined
	variables.
	@param locs		Set of locations.
	@param used		Set of used variables.
	@return			True if the location are required, false else. *)
let rec is_required locs used =
	match locs with
	| [] -> false
	| ("", _)::_ -> false
	| (id, ix)::tl ->
		if RegSet.contains id ix used then true
		else is_required tl used


(** Test if the variable set contains at least one dynamic variable.
 	Returns also true if it contains an undefined variable.
	@param comp		Computability set (as produced by computability analysis)
	@param vars		Set of variables to test.
	@return			True if it contains at least one dynamic variable, false else. *)
let rec has_dynamic comp vars =
	match vars with
	| [] -> false
	| (id, ix)::tl ->
		if id = "" then true else
		match ComputeState.get comp id ix with
		| Comput.DYNAMIC -> true
		| _ -> has_dynamic comp tl


module Domain(A: ANALYSIS) = struct
	type init = ComputeState.t Absint.StatHashtbl.t
	type ctx = ComputeState.t Absint.StatHashtbl.t
	type t = RegSet.t * bool list

	let null _ = (RegSet.empty, false)

	let make ht = ht

	let update ht (set, stk) stat =
		let (used, uset) = A.use stat in
		let set =
			(match stat with
			| Irg.SET (l, e)
			| Irg.SETSPE (l, e) ->
				let (ul, uv) = used_locs l in
				if not (is_required ul set) then set else
				let uv = RegSet.union uv (used_vars e) in
				if has_dynamic (StatHashtbl.find ht stat) uv then set else
				RegSet.union uv (RegSet.diff set ul)
			| _ -> set) in
		(RegSet.union set uset,
		(used || (List.hd stk))::(List.tl stk))

	let join ht stat (set1, stk1) (set2, stk2) =
		match stat with
		| IF_STAT(c, _, _) ->
			let res = (List.hd stk1) || (List.hd stk2) in
			let uset = RegSet.union set1 set2 in
			let cset =
				if not res then RegSet.empty else
				let uv = used_vars c in
				if has_dynamic (StatHashtbl.find ht stat) uv then RegSet.empty
				else uv in
			(RegSet.union uset cset, List.tl stk1)
		| _ -> failwith "bad !"

	let disjoin _ _ (set, stk) = ((set, false::stk), (set, false::stk))

	let includes (set1, stk1) (set2, stk2) =
		(RegSet.includes set1 set2) && ((List.hd set1) >= (List.hd set2))

	let observe ctx s stat = s
end
