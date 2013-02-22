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

open Irg

(** Test if the expression is dynamic, that is, every accessed state
	is dynamic.
	@param s	Set containing dynamic states.
	@param expr	Expression to test.
	@return		True if the the expression is static, false else. *)
let rec is_stat s expr =

	let rec look_list exprs =
		List.fold_left (fun v e -> v && look e) true exprs

	and look_indexed id ix size =
		(look ix) &&
		(try
			RegSet.contains id (Sem.to_int (Sem.eval_const ix)) s
		with Sem.SemError _ ->
			RegSet.contains_all id size s)

	and look expr =
		match expr with
		| NONE -> true
		| COERCE (_, e) -> look e
		| FORMAT (_, args) -> look_list args
		| CANON_EXPR _ -> false
		| BITFIELD (_, e1, e2, e3) -> look_list [e1; e2; e3]
		| UNOP (_, _, e) -> look e
		| BINOP (_, _, e1, e2) -> look_list [e1; e2]
		| IF_EXPR (_, e1, e2, e3) -> look_list [e1; e2; e3]
		| SWITCH_EXPR (_, e1, cases, e2) -> look_list (e1::e2::(snd (List.split cases)))
		| CONST _ -> true
		| ELINE (_, _, e) -> look e
		| EINLINE _ -> false
		| FIELDOF _ -> failwith "too bad !"
		| REF id ->
			(match Irg.get_symbol id with
			| MEM _ -> false
			| REG _
			| VAR _ -> RegSet.contains id 0 s
			| PARAM _
			| LET _ -> true
			| _ -> failwith "too bad !")
		| ITEMOF (_, id, ix) ->
			(match Irg.get_symbol id with
			| MEM _ -> false
			| REG (_, size, _, _) -> look_indexed id ix size
			| VAR (_, size, _) -> look_indexed id ix size
			| PARAM _
			| LET _ -> true
			| _ -> failwith "too bad !") in

	look expr


(** Compute the set of assigned references.
	@param s	Set of static references.
	*)
let do_set s loc expr =
	let stat = is_stat s expr in
	let rec look loc s =
		match loc with
		| LOC_NONE 	-> s
		| LOC_CONCAT (_, l1, l2) -> look l1 (look l2 s)
		| LOC_REF (_, id, ix, u, l) ->
			let stat = stat && (is_stat s ix) && (is_stat s u) && (is_stat s l) in
			try
				let ix = if ix = NONE then 0 else Sem.to_int (Sem.eval_const ix) in
				if stat then
					begin
						(*Printf.printf "add %s[%d]\n" id ix;*)
						RegSet.add id ix s
					end
				else
					begin
						(*Printf.printf "remove %s[%d]\n" id ix;*)
						RegSet.remove id ix s
					end
			with Sem.SemError _ ->
				if stat then
					begin
						(*Printf.printf "add_all %s\n" id;*)
						s
					end
				else
					begin
						(*Printf.printf "remove_all %s\n" id;*)
						RegSet.remove_all id s
					end in
	look loc s


(** Static Value Analysis
	type: MUST
	ditection: FORWARD

	domain: parameters U variables U registers
	initial: set of parameters
	join: intersection
	update (x <- e)
		if e contains only static values
		then add x to the set
		else remove x from the set.

	Computes for each which state items contains static values, that
	is values only computed from the parameters or constants; not
	from state (register and memory before the instruction). *)
module Domain = struct
	type init = Iter.inst
	type ctx = Iter.inst
	type t = RegSet.t

	let null inst =
		List.fold_left (fun set (name, _) -> RegSet.add name 0 set) [] (Iter.get_params inst)

	let make inst = inst

	let update inst set loc expr = do_set set loc expr

	let updateSpecial inst set stat =
		match stat with
		| Irg.CANON_STAT _ -> set
		| Irg.ERROR _ -> null inst
		| Irg.INLINE _
		| _ -> failwith "bad !"

	let join inst _ s1 s2 = RegSet.inter s1 s2
	let includes s1 s2 = RegSet.includes s1 s2
	let observe ctx set stat = set
	let disjoin _ _ s = (s, s)

end


