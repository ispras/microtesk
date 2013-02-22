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

(** This module provides an implementation of set for registers
	including blurred indexed registers.
	Implements State.VALUE signature. *)

type t = (string * int) list

let empty: t = []

let size id =
	match Irg.get_symbol id with
	| REG (_, size, _, _) -> size
	| VAR (_, size, _) -> size
	| _ -> failwith "not a register or a variable"

let rec add id ix set =
	match set with
	| [] -> [(id, ix)]
	| (idp, ixp)::tl ->
		if idp < id then (idp, ixp)::(add id ix tl) else
		if idp > id then (id, ix)::set else
		if ixp < ix then (idp, ixp)::(add id ix tl) else
		if ixp > ix then (id, ix)::set else
		set

let add_all id set =

	let m = size id in

	let rec add ix set =
		if ix >= m then set else
		match set with
		| [] -> (id, ix)::(add (ix + 1) set)
		| (idp, ixp)::tl ->
			if idp < id then (idp, ixp)::(add ix tl) else
			if idp > id then (id, ix)::(add (ix + 1) set) else
			if ix < ixp then (id, ix)::(add (ix + 1) set) else
			(idp, ixp)::(add (ix + 1) set) in
	add 0 set

let rec remove id ix set =
	match set with
	| [] -> []
	| (idp, ixp)::tl ->
		if idp < id then (idp, ixp)::(remove id ix tl) else
		if idp > id then set else
		if ixp < ix then (idp, ixp)::(remove id ix tl) else
		if ixp > ix then set else
		tl

let rec remove_all id set =
	match set with
	| [] -> []
	| (idp, ixp)::tl ->
		if idp < id then (idp, ixp)::(remove_all id tl) else
		if idp > id then set else
		remove_all id tl

let rec inter s1 s2 =
	match s1, s2 with
	| [], _ -> s2
	| _, [] -> s1
	| (id1, ix1)::t1, (id2, ix2)::t2 ->
		if id1 < id2 then (id1, ix1)::(inter t1 s2) else
		if id1 > id2 then (id2, ix2)::(inter s1 t2) else
		if ix1 < ix2 then (id1, ix1)::(inter t1 s2) else
		if ix1 > ix2 then (id2, ix2)::(inter s1 t2) else
		(id1, ix1)::(inter t1 t2)

let rec union s1 s2 =
	match s1, s2 with
	| [], _ -> s2
	| _, [] -> s1
	| (id1, ix1)::tl1, (id2, ix2)::tl2 ->
		if id1 < id2 then (id1, ix1)::(union tl1 s2) else
		if id1 > id2 then (id2, ix2)::(union s1 tl2) else
		if ix1 < ix2 then (id1, ix1)::(union tl1 s2) else
		if ix1 > ix2 then (id2, ix2)::(union s1 tl2) else
		(id1, ix1)::(union tl1 tl2)

let rec diff s1 s2 =
	match s1, s2 with
	| [], _ -> s2
	| _, [] -> s1
	| (id1, ix1)::tl1, (id2, ix2)::tl2 ->
		if id1 < id2 then (id1, ix1)::(diff tl1 s2) else
		if id1 > id2 then diff s1 tl2 else
		if ix1 < ix2 then (id1, ix1)::(diff tl1 s2) else
		if ix1 > ix2 then diff s1 tl2 else
		diff tl1 tl2


let rec contains id ix s =
	match s with
	| [] -> false
	| (idp, ixp)::_ when id = idp && ix = ixp -> true
	| _::tl -> contains id ix tl

let contains_all id m s =
	let rec contains s ix =
		if ix = m then true else
		match s with
		| [] -> false
		| (idp, ixp)::tl when id = idp ->
			if ixp <> ix then false else
			contains tl (ix + 1)
		| _::tl ->
			contains tl ix in
	contains s 0

let rec includes s1 s2 =
	match s1, s2 with
	| [], []
	| _, [] -> true
	| [], _ -> false
	| (id1, ix1)::t1, (id2, ix2)::t2 ->
		if id1 = id2 then
			if ix1 = ix2 then includes t1 t2 else
			if ix1 > ix2 then false else
			includes t1 s2
		else if id1 > id2 then false
		else includes t1 s2

let output out set =
	let rec process set first =
	match set with
	| [] -> output_string out " }"
	| (id, ix)::tl ->
		if not first then output_string out ", ";
		Printf.fprintf out "%s[%d]" id ix;
		process tl false in
	output_string out "{ "; process set true





