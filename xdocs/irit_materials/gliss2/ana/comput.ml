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

(** Compute for each point of the program the computability of a variable,
	that is, one of:
	* STATIC -- statically defined (based only on constants or instruction parameter)
	* DYNAMIC -- definitively depending on program state
	* FUZZY -- sometimes static, sometimes dynamic

	The analysis is described below for a variable v (S is the state
	of other variables):
	* domain = {STATIC, DYNAMIC, FUZZY}
	* order: STATIC < FUZZY, DYNAMIC < FUZZY
	* initial state = DYNAMIC
	* update[v <- e] s = wc(S[v'] / v' in e)
	* join(s1, s2) = wc(s1, s2)

	ws (worse computability is computed by):
	ws(STATIC, STATIC) = STATIC
	ws(DYNAMIC, DYNAMIC) = DYNAMIC
	ws(_, _) = FUZZY
*)


open Irg

(** Defines computability of a variable *)
type compute_t = STATIC | DYNAMIC | FUZZY


(** Join of two computabilities.
	@param c1	First computability.
	@param c2	Second computability. *)
let comp_join c1 c2 =
	match c1, c2 with
	| STATIC, STATIC -> STATIC
	| DYNAMIC, DYNAMIC -> DYNAMIC
	| _ -> FUZZY


(** Test if the c1 includes (is greater than) c2 value.
	@param c1	Including computability.
	@param c2	Included computability. *)
let comp_includes c1 c2 =
	c1 = FUZZY || c1 = c2


(** Output the given computability.
	@param out	Output to output to.
	@param c	Computability ti output. *)
let comp_output out c =
	output_string out
		(match c with
		| STATIC -> "S"
		| DYNAMIC -> "D"
		| FUZZY -> "F")


module ComputeValue = struct
	type t = compute_t
	let undef = DYNAMIC
	let any = FUZZY
	let includes = comp_includes
	let join = comp_join
	let output = comp_output
end


(** State of the computability problem. *)
module State = State.Make(ComputeValue)


(** Computability Analysis
	type: MUST
	ditection: FORWARD

	domain: STATE(COMPUTABILITY)
	initial: { (p, 0, STATIC) / p parameter }
	join: state join
	update (x <- e) s
		if static(e) then s[x -> STATIC] else
		if !fuzzy(e) then s[x -> DYNAMIC] else
		s[x -> FUZZY]
*)
module Domain = struct
	type init = Iter.inst
	type ctx = Iter.inst
	type t = State.t

	let null inst =
		List.fold_left (fun s (name, _) -> State.set s name 0 STATIC) [] (Iter.get_params inst)

	let make inst = inst

	let rec compute_of s expr : compute_t =
		match expr with
		| NONE -> failwith "NONE expression !"
		| COERCE (_, expr) -> compute_of s expr
		| FORMAT _ -> failwith "format not supported here !"
		| CANON_EXPR (_, _, exprs) -> compute_of_list s exprs
		| REF name -> State.get s name 0
		| FIELDOF _ -> failwith "unsupported FIELDOF"
		| ITEMOF (_, name, ix) ->
			(try State.get s name (Sem.to_int (Sem.eval_const ix))
			with Sem.SemError _ -> DYNAMIC)
		| BITFIELD (_, e1, e2, e3) -> compute_of_list s [e1; e2; e3]
		| UNOP (_, _, e) -> compute_of s e
		| BINOP (_, _, e1, e2) -> compute_of_list s [e1; e2]
		| IF_EXPR (_, e1, e2, e3) -> compute_of_list s [e1; e2; e3]
		| SWITCH_EXPR (_, c, cases, def) ->
			compute_of_list s (c::def::(snd (List.split cases)))
		| CONST _ -> STATIC
		| ELINE (_, _, e) -> compute_of s e
		| EINLINE _ -> failwith "unsupported EINLINE"
	and compute_of_list s lst =
		match lst with
		| [] -> failwith "internal error"
		| [e] -> compute_of s e
		| e::t -> comp_join (compute_of s e) (compute_of_list s t)

	let update inst s stat =
		let work l e =
			let comp = compute_of s e in
			let rec set s loc =
				match loc with
				| LOC_NONE 	-> failwith "LOC_NONE unsupported"
				| LOC_REF (_, id, Irg.NONE, _, _) -> State.set s id 0 comp
				| LOC_REF (_, id, ix, _, _) ->
					(try State.set s id (Sem.to_int (Sem.eval_const ix)) comp
					with Sem.SemError _ -> State.set_all s id FUZZY)
				| LOC_CONCAT (_, l1, l2) -> set (set s l1) l2 in
			set s l in

		match stat with
		| Irg.SET (l, e)
		| Irg.SETSPE (l, e) -> work l e
		| Irg.CANON_STAT _ -> s
		| Irg.ERROR _ -> null inst
		| Irg.INLINE _
		| _ -> failwith "bad !"

	let join inst _ s1 s2 = State.join s1 s2
	let includes s1 s2 = State.includes s1 s2
	let observe_in ctx stat s = s
	let observe_out ctx stat s = s
	let disjoin _ _ s = (s, s)

	let output = State.output

end


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
		match State.get comp id ix with
		| DYNAMIC -> true
		| _ -> has_dynamic comp tl



(** Observer for the computability problem. *)
module Obs = Absint.Observer(Domain)

(** Analysis of the computability problem. *)
module Ana = Absint.Forward(Obs)

(** Dump module for the computability problem. *)
module Dump = Absint.Dump(Obs)


(** Analyze for computability the given instruction for the given
	attribute.
	@param inst		Instruction to analyze.
	@param attr		Attribute to handle.
	@return			((statement, domain) table, exit domain) *)
let analyze inst attr =
	let ctx = Obs.make inst in
	let dom = Ana.run ctx attr in
	let comp = fst ctx in
	(comp, dom)


(** Dump the result of the analysis.
	@param comp		Domain table.
	@param dom		Exit domain.
	@param attr		Analyzed attribute. *)
let dump (comp, dom) attr =
	Dump.dump comp attr dom;
