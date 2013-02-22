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

let get_expr name =
	match Irg.get_symbol name with
	| Irg.ATTR (Irg.ATTR_EXPR (_, expr)) -> expr
	| _ -> failwith ("no attribute: " ^ name)


(* IsBranchDomain domain *)
module IsBranchDomain = struct
	type init = string
	type ctx = string
	type t = bool
	let null _ = false

	let make pc = pc

	let update c s stat =
		let rec work l =
			match l with
			| Irg.LOC_NONE -> s
			| Irg.LOC_REF (_, id, _, _, _) when id = c -> true
			| Irg.LOC_REF _ -> s
			| Irg.LOC_CONCAT (_, l1, l2) -> (work l1) || (work l2) in
		match stat with
		| Irg.SET (l, _)
		| Irg.SETSPE (l, _) -> work l
		| _ -> s

	let join _ _ s1 s2 = s1 || s2

	let includes d2 d1 = d1 <= d2

	let observe_in _ _ d = d
	let observe_out _ _ d = d

	let disjoin _ _ d = (d, d)

	let output c d =
		output_string c (if d then "true" else "false")
end

(* IsBranch module *)
module IsBranchAna = Forward (IsBranchDomain)

let rec pure_expr expr =
	match expr with
	| Irg.ELINE (_, _, expr) -> pure_expr expr
	| _ -> expr

let _ =
	let process pc _ inst =
		let perform _ =
			IsBranchAna.run pc "action" in
		let res =
			try
				(match Irg.get_symbol "is_branch" with
				| Irg.ATTR (Irg.ATTR_EXPR (_, expr)) ->
					(match Sem.eval_const expr with
					| Irg.CARD_CONST n -> (Int32.compare n Int32.zero) <> 0
					| _ -> perform ())
				| _ -> perform ())
			with Irg.Symbol_not_found _ ->
				perform () in
		Printf.printf "%s = %b\n" (Iter.get_name inst) res in

	App.run
		[]
		"SYNTAX:is_branch NMP-FILE\n\tGenerate table to test if an instruction is a branch."
		(fun info ->
			let pc =
				if info.Toc.npc_name = "" then info.Toc.pc_name else info.Toc.npc_name in
			Printf.printf "PC=%s\n" pc;
			Iter.iter (process pc) ()
		)
