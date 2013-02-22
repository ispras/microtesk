(*
 * $Id: regana.ml,v 1.1 2009/07/31 09:09:43 casse Exp $
 * Copyright (c) 2009, IRIT - UPS <casse@irit.fr>
 *
 * This file is part of Gliss 2.
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
 
(* maximum count of register use *)
let max = ref 10

type index_t =
	| NONE
	| PARAM of string
	| CST of int
type reg_t = string * index_t
type used_t = reg_t list

let void_used : used_t = []

let add_used used id idx =
	if idx = NONE then
		List.fold_left
			(fun lst (idp, idxp) -> if id = idp then lst else (idp, idxp)::lst)
			[(id, idx)]
			used
	else
		List.fold_left
			(fun lst (idp, idxp) -> if id = idp && idx = idxp then lst else (idp, idxp)::lst)
			[(id, idx)]
			used
	

let _ =

	App.run
		[]
		"SYNTAX: regana NMP-FILE\n\tGenerate table of register usage."
		(fun info ->
			let maker = App.maker () in
			let dict = App.make_env info maker in
			
			(* generate regana.c *)
			let dir = "src" in
			App.makedir dir;
			App.make_template "regana.c" (dir ^ "/regana.c") dict;
			
			(* generate regana.h *)
			let dir = "include/" ^ info.Toc.proc in
			App.makedir dir;
			let dict = 
				("REG_USAGE_MAX", Templater.TEXT (fun out -> Printf.fprintf out "%d" !max))
				::dict in
			App.make_template "regana.h" (dir ^ "/regana.h") dict
		)
