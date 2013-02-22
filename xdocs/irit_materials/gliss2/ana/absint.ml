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

(** Get the statement matching the named attribute.
	@param name		Attribute to get.
	@return			Statement in the attribute (or failwith). *)
let get_stat name =
	match Irg.get_symbol name with
	| Irg.ATTR (Irg.ATTR_STAT (_, stat)) -> stat
	| _ -> failwith ("no attribute: " ^ name)


(** Hashing for statement using strict equality. *)
module StatHash = struct
	type t = Irg.stat
	let equal s1 s2 = s1 == s2
	let hash s = Hashtbl.hash_param 2 2 s
end


(** Hash table for statement based on StatHash *)
module StatHashtbl = Hashtbl.Make(StatHash)


(* unique id for statements *)
let uniq_ids: int StatHashtbl.t = StatHashtbl.create 211
let uniq_id = ref 0
let get_id stat =
	try
		StatHashtbl.find uniq_ids stat
	with Not_found ->
		let id = !uniq_id in
		StatHashtbl.add uniq_ids stat id;
		incr uniq_id;
		id


(* statement numbering *)
let numbers: int StatHashtbl.t = StatHashtbl.create 211
let uniq = ref 0


(** Assign a uniq number to the given statement.
	@param stat		Statement to number. *)
let number_stat stat =
	if not (StatHashtbl.mem numbers stat) then
		begin
			StatHashtbl.add numbers stat !uniq;
			incr uniq
		end


(** Get the number of a statement.
	@param stat		Statement to get number of.
	@return			Statement number (or -1 if unumebered). *)
let number_of stat =
	try
		StatHashtbl.find numbers stat
	with Not_found ->
		-1


(** Give a number to the given attribute.
	@param attr		Attribute to number. *)
let number_attr attr =

	let rec process_call stack name =
		if List.mem name stack then () else
		process_stat (name::stack) (get_stat attr)

	and process_stat stack stat =
		number_stat stat;
		match stat with
		| Irg.SEQ(s1, s2) ->
			process_stat stack s1; process_stat stack s2
		| Irg.IF_STAT(_, s1, s2) ->
			process_stat stack s1; process_stat stack s2
		| Irg.EVAL name ->
			process_call stack name
		(*| Irg.SWITCH ... -> ... *)
		| Irg.LINE (_, _, stat) ->
			process_stat stack stat
		| _ -> () in

	process_call [] attr


(** ordered set for statements *)
module OrderedStat = struct
	type t = Irg.stat
	let compare s1 s2 = (get_id s1) - (get_id s2)
end

(** set of statements *)
module StatSet = Set.Make(OrderedStat)
module StatMap = Map.Make(OrderedStat)


(** Output a statement as a short form.
	@param out	Channel to output to.
	@param stat	Statement to display. *)
let output_stat out stat =
	let n = number_of stat in
	if n = -1 then Irg.print_statement stat
	else Printf.fprintf out "(%d)" n


(** Output a statement as a short form to standard output.
	@param stat	Statement to display. *)
let print_stat stat = output_stat stdout stat


(** Output a set of statement.
	@param out	Channel to output to.
	@param set	Set of statements to display. *)
let output_stat_set out set =
	output_string out "{ ";
	ignore(StatSet.fold (fun stat fst ->
		if not fst then output_string out ", ";
		output_stat out stat;
		false) set true);
	output_string out " }"


(** Print a set of statement to standard ouput.
	@param set	Set of statements to display. *)
let print_stat_set set = output_stat_set stdout set

(** Domain type *)
module type DOMAIN = sig
	type init	(** type of initialization data *)
	type ctx	(** type of context data *)
	type t		(** type of domain data *)

	(** "null ctx" build a null domain from the context ctx.
		@return		Null domain. *)
	val null: ctx -> t

	(** "make init" build a new context from the given initial data.
		@return		Built context. *)
	val make: init -> ctx

	(** "update ctx d l e" perform an update from the domain d
		with an assignment of e to l.
		@return		Update domain. *)
	val update: ctx -> t -> Irg.stat -> t

	(** "includes d1 d2" test if the domain d1 includes the domain d2.
		@return	True if d1 includes d2, false else. *)
	val includes: t -> t -> bool

	(** "observe ctx s d" is called just before processing the statement
		s with the in domain d.
		@return	The domain after the observe operation. *)
	val observe_in: ctx -> Irg.stat -> t -> t

	(** "observe ctx s d" is called just before processing the statement
		s with the out domain d.
		@return	The domain after the observe operation. *)
	val observe_out: ctx -> Irg.stat -> t -> t

	(** "cond_false ctx d e" is called each time a condition e is
		encountered and for the path when the condition is false.
		@return	Domain after condition evaluated to (true, false). *)
	val disjoin: ctx -> Irg.stat -> t -> t * t

	(** "join ctx d1 d2" join two domain coming from two paths.
		@return	Join of domain d1 and d2. *)
	val join: ctx -> Irg.stat -> t -> t -> t

	(** "output channel d" displays the domain value on the given
		channel *)
	val output: out_channel -> t -> unit
end


(** A domain allowing to observe and record domain per statement. *)
module InObserver(D: DOMAIN) = struct

	(* DOMAIN implementation *)
	type init = D.init
	type ctx = D.t StatHashtbl.t * D.ctx
	type t = D.t
	let null (_, ctx) = D.null ctx
	let make init = (StatHashtbl.create 211, D.make init)
	let update (ht, ctx) d stat = D.update ctx d stat
	let join (ht, ctx) d1 d2 = D.join ctx d1 d2
	let includes = D.includes
	let observe_in (ht, _) s d = StatHashtbl.replace ht s d; d
	let observe_out _ _ d = d
	let disjoin (_, ctx) d e = D.disjoin ctx d e
	let output c d = D.output c d

	(* OBSERVABLE implementation *)
	type map = D.t StatHashtbl.t
	let get_in ht s =
		try Some (StatHashtbl.find ht s)
		with Not_found -> None
	let get_out _ _ = None

	(** Get the value of an analysis.
		@param ht	Result hashtable.
		@param stat	Looked statement.
		@return		Value. *)
	let get ht stat = StatHashtbl.find ht stat
end
module Observer = InObserver


(** A domain allowing to observe and record output domain per statement. *)
module OutObserver(D: DOMAIN) = struct
	type init = D.init
	type ctx = D.t StatHashtbl.t * D.ctx
	type t = D.t
	let null (_, ctx) = D.null ctx
	let make init = (StatHashtbl.create 211, D.make init)
	let update (ht, ctx) d stat = D.update ctx d stat
	let join (ht, ctx) d1 d2 = D.join ctx d1 d2
	let includes = D.includes
	let observe_out (ht, _) s d = StatHashtbl.replace ht s d; d
	let observe_in _ _ d = d
	let disjoin (_, ctx) d e = D.disjoin ctx d e
	let output c d = D.output c d

	(** Get the value of an analysis.
		@param ht	Result hashtable.
		@param stat	Looked statement.
		@return		Value. *)
	let get ht stat = StatHashtbl.find ht stat
end


(** A domain allowing to observe and record domain per statement. *)
module BothObserver(D: DOMAIN) = struct

	(* table declaration *)
	type key =
		| IN of Irg.stat
		| OUT of Irg.stat
	module KeyHash = struct
		type t = key
		let equal s1 s2 = s1 == s2
		let hash s = Hashtbl.hash_param 2 2 s
	end
	module KeyHashtbl = Hashtbl.Make(KeyHash)

	type init = D.init
	type ctx = D.t KeyHashtbl.t * D.ctx
	type t = D.t
	let null (_, ctx) = D.null ctx
	let make init = (KeyHashtbl.create 211, D.make init)
	let update (ht, ctx) d stat = D.update ctx d stat
	let join (ht, ctx) d1 d2 = D.join ctx d1 d2
	let includes = D.includes
	let observe_in (ht, _) s d = KeyHashtbl.replace ht (IN s) d; d
	let observe_in (ht, _) s d = KeyHashtbl.replace ht (OUT s) d; d
	let disjoin (_, ctx) d e = D.disjoin ctx d e
	let output c d = D.output c d

	(** Get the value of an analysis.
		@param ht	Result hashtable.
		@param stat	Looked statement.
		@return		Value. *)
	let get ht stat = StatHashtbl.find ht stat
end


(* Analysis module *)
module Forward (D: DOMAIN) = struct

	(** Perform the analysis.
		@param ctx		Context for domain.
		@param name		Attribute name.
		@return			Last domain data of the analysis. *)
	let run ctx name =

		let rec process stack stat dom =
			let dom = D.observe_in ctx stat dom in
			let dom =
				match stat with
				| Irg.NOP -> dom
				| Irg.EVAL name ->
					process_call stack name dom
				| Irg.SEQ (s1, s2) ->
					process stack s2 (process stack s1 dom)
				| Irg.EVALIND _ -> failwith "unsupported"
				| Irg.SET _
				| Irg.SETSPE _
				| Irg.CANON_STAT _
				| Irg.ERROR _
				| Irg.INLINE _ -> D.update ctx dom stat
				| Irg.IF_STAT (_, s1, s2) ->
					let (dt, df) = D.disjoin ctx stat dom in
					D.join ctx stat (process stack s1 dt) (process stack s2 df)
				| Irg.SWITCH_STAT (_, cases, def) ->
					List.fold_left (fun dom (_, s) -> process stack s dom) (process stack def dom) cases
				| Irg.LINE (_, _, s) -> process stack s dom in
			D.observe_out ctx stat dom

		and process_call stack name dom =
			try
				let old = List.assoc name stack in
				if D.includes old dom then old else
					let newd = D.join ctx Irg.NOP old dom in
					process ((name, newd)::(List.remove_assoc name stack)) (get_stat name) newd
			with Not_found ->
				process ((name, dom)::stack) (get_stat name) dom in

	process_call [] name (D.null ctx)
end


(* Analysis module for backward analysis *)
module Backward (D: DOMAIN) = struct

	(** Perform the analysis.
		@param ctx		Context for domain.
		@param name		Attribute name.
		@return			Last domain data of the analysis. *)
	let run ctx name =

		let rec process stack stat dom =
			let dom = D.observe_in ctx stat dom in
			let dom =
				match stat with
				| Irg.NOP -> dom
				| Irg.EVAL name ->
					process_call stack name dom
				| Irg.SEQ (s1, s2) ->
					process stack s1 (process stack s2 dom)
				| Irg.EVALIND _ -> failwith "unsupported"
				| Irg.SET _
				| Irg.SETSPE _
				| Irg.CANON_STAT _
				| Irg.ERROR _
				| Irg.INLINE _ ->
					D.update ctx dom stat
				| Irg.IF_STAT (_, s1, s2) ->
					let (dt, df) = D.disjoin ctx stat dom in
					D.join ctx stat (process stack s1 dt) (process stack s2 df)
				| Irg.SWITCH_STAT (_, cases, def) ->
					List.fold_left (fun dom (_, s) -> process stack s dom) (process stack def dom) cases
				| Irg.LINE (_, _, s) -> process stack s dom in
			D.observe_out ctx stat dom

		and process_call stack name dom =
			try
				let old = List.assoc name stack in
				if D.includes old dom then old else
					let newd = D.join ctx Irg.NOP old dom in
					process ((name, newd)::(List.remove_assoc name stack)) (get_stat name) newd
			with Not_found ->
				process ((name, dom)::stack) (get_stat name) dom in

	process_call [] name (D.null ctx)
end


(** Domain providing dumping facility. *)
module type OBSERVABLE = sig
	type map
	type t
	val get_in: map -> Irg.stat -> t option
	val get_out: map -> Irg.stat -> t option
	val output: out_channel -> t -> unit
end


(** Module providing display for dumping a state. *)
module Dump(O: OBSERVABLE) = struct

	let rec indent n =
		if n = 0 then () else (print_char '\t'; indent (n - 1))

	let rec indent_num n stat nums =
		indent n;
		if nums then
			try Printf.printf "(%d)" (number_of stat)
			with Not_found -> ()

	let dump_dom_msg msg n d =
		try
			indent (n + 1);
			print_string msg;
			O.output stdout d;
			print_string "\n"
		with Not_found -> ()

	let dump_dom = dump_dom_msg ""

	let dump_in n ht stat =
		match O.get_in ht stat with
		| None -> ()
		| Some d -> dump_dom_msg "IN=" n d

	let dump_out n ht stat =
		match O.get_out ht stat with
		| None -> ()
		| Some d -> dump_dom_msg "OUT=" n d

	let rec dump_result n stack ht name nums =
		if List.mem name stack then
			begin
				indent n;
				Printf.printf "goto %s\n" name
			end
		else
			begin
				indent n;
				Printf.printf "%s:\n" name;
				dump_stat (n + 1) (name::stack) ht (get_stat name) nums
			end

	and dump_stat n stack ht stat nums =
		match stat with
		| Irg.NOP -> ()
		| Irg.SEQ (s1, s2) ->
			dump_stat n stack ht s1 nums;
			dump_stat n stack ht s2 nums
		| Irg.IF_STAT (c, s1, s2) ->
			dump_in n ht stat;
			indent_num n stat nums;
			print_string "if ";
			Irg.print_expr c;
			print_string " then\n";
			dump_stat (n + 1) stack ht s1 nums;
			indent n;
			print_string "else\n";
			dump_stat (n + 1) stack ht s2 nums;
			indent n;
			print_string "endif\n";
			dump_out n ht stat
		| Irg.SWITCH_STAT (c, cases, def) ->
			dump_in n ht stat;
			dump_out n ht stat
		| Irg.LINE (_, _, stat) -> dump_stat n stack ht stat nums
		| Irg.EVAL name ->
			dump_in n ht stat;
			dump_result n stack ht name nums;
			dump_out n ht stat
		| _ ->
			dump_in n ht stat;
			indent_num (n - 2) stat nums;
			Irg.print_statement stat;
			dump_out n ht stat

	(** Dump the full analysis result, that is, each result state
		at the program points.
		@param ht	Observation result.
		@param name	Name of the attribute (usually "action")
		@param out	Final state. *)
	let dump ht name out =
		dump_result 1 [] ht name false;
		print_char '\t';
		O.output stdout out;
		print_char '\n'

	let dump_numbered ht name out =
		dump_result 1 [] ht name true;
		print_char '\t';
		O.output stdout out;
		print_char '\n'

end

