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

(** This module provides an implementation of state for an abstract
	interpretation, that is, the association of a value with
	a (variale, index) pair index. *)

(** Signature of the values of the states. *)
module type VALUE = sig

	(** type of the value *)
	type t

	(** constant representing undefined value *)
	val undef: t

	(** constant representing any value (lub) *)
	val any: t

	(** includes v1 v2 tests if v1 includes v2 *)
	val includes: t -> t -> bool

	(** join of two values *)
	val join: t -> t -> t

	(** perform output of the value *)
	val output: out_channel -> t -> unit
end


(** Generator of states. *)
module Make(V: VALUE) = struct

	type t = (string * int * V.t) list

	let empty: t = []

	let rec set s id ix v =
		let make s =
			if v = V.any then s else (id, ix, v)::s in

		match s with
		| [] -> make s
		| (idp, ixp, vp)::tl ->
			if idp < id then (idp, ixp, vp)::(set tl id ix v) else
			if idp > id then make s else
			if ixp < ix then (idp, ixp, vp)::(set tl id ix v) else
			if ixp > ix then make s else
			make tl

	let set_all s id v =

		let m =
			match Irg.get_symbol id with
			| REG (_, size, _, _) -> size
			| VAR (_, size, _) -> size
			| PARAM _ -> 1
			| MEM _ -> 0
			| s ->
				Irg.print_spec s;
				failwith "not a register or a variable" in

		let rec set s ix =
			let make s =
				if v = V.any then s else (id, ix, v)::s in
			if ix >= m then s else
			match s with
			| [] -> make (set s (ix + 1))
			| (idp, ixp, vp)::tl ->
				if idp < id then (idp, ixp, vp)::(set tl ix) else
				if idp > id || ix < ixp then make (set s (ix + 1)) else
				make (set tl (ix + 1)) in
		set s 0

	let rec join s1 s2 =
		match s1, s2 with
		| [], _ -> s2
		| _, [] -> s1
		| (id1, ix1, v1)::tl1, (id2, ix2, v2)::tl2 ->
			if id1 < id2 then (id1, ix1, V.join V.undef v1)::(join tl1 s2) else
			if id1 > id2 then (id2, ix2, V.join V.undef v2)::(join s1 tl2) else
			if ix1 < ix2 then (id1, ix1, V.join V.undef v1)::(join tl1 s2) else
			if ix1 > ix2 then (id2, ix2, V.join V.undef v2)::(join s1 tl2) else
			(id1, ix1, V.join v1 v2)::(join tl1 tl2)

	let rec includes s1 s2 =
		match s1, s2 with
		| [], []
		| _, [] -> true
		| [], _ -> false
		| (id1, ix1, v1)::t1, (id2, ix2, v2)::t2 ->
			if id1 = id2 then
				if ix1 = ix2 then (V.includes v1 v2) && (includes t1 t2) else
				if ix1 > ix2 then false else
				includes t1 s2
			else if id1 > id2 then false
			else includes t1 s2

	let rec get s id ix: V.t =
		match s with
		| [] -> V.undef
		| (idp, ixp, v)::_ when id = idp && ix = ixp -> v
		| _::tl -> get tl id ix

	let output out set =
		let rec process set first =
		match set with
		| [] -> output_string out " }"
		| (id, ix, v)::tl ->
			if not first then output_string out ", ";
			if ix = 0 then Printf.fprintf out "%s = " id
			else Printf.fprintf out "%s[%d] = " id ix;
			V.output out v;
			process tl false in
		output_string out "{ "; process set true
end
