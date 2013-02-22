(*
 * GLISS2 -- argument decoding test
 * Copyright (c) 2010, IRIT - UPS <casse@irit.fr>
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

let add e1 e2 = Irg.BINOP (Sem.get_type_expr e1, Irg.ADD, e1, e2)
let sub e1 e2 = Irg.BINOP (Sem.get_type_expr e1, Irg.SUB, e1, e2)
let cst i32 = Irg.CONST (Irg.CARD(32), Irg.CARD_CONST i32)
let csta i = Irg.CONST (Irg.CARD(32), i)
let csti i = Irg.CONST (Irg.CARD(32), Irg.CARD_CONST (Int32.of_int i))
let shl e1 e2 = Irg.BINOP (Sem.get_type_expr e1, Irg.LSHIFT, e1, e2)
let shr e1 e2 = Irg.BINOP (Sem.get_type_expr e1, Irg.RSHIFT, e1, e2)
let and_ e1 e2 = Irg.BINOP(Sem.get_type_expr e1, Irg.BIN_AND, e1, e2)
let or_ e1 e2 = Irg.BINOP(Sem.get_type_expr e1, Irg.BIN_OR, e1, e2)
let getb e1 i2 i3 = Irg.BITFIELD (Irg.CARD(abs (i2 - i3) + 1), e1, csti i2, csti i3)
let concat e1 e2 = Irg.BINOP(Irg.CARD ((Sem.get_length_from_expr e1) + (Sem.get_length_from_expr e2)), Irg.CONCAT, e1, e2)

let test params args =
	Printf.printf "################################\nargument: ";
	List.iter (fun e -> Irg.print_expr e; print_string ", ") args;
	print_char '\n';
	let cnt = ref 0 in
	let vals = List.map (fun _ -> incr cnt; Irg.EINLINE (Printf.sprintf "p%d" !cnt)) args in
	let r = Decode_arg.decode_parameters params args vals in
	List.iter
		(fun (n, e) ->
			Printf.printf "\tparameter %s: " n;
			Irg.print_expr e;
			print_char '\n')
		r;
	print_char '\n'

let c16 = Irg.CARD(16)
let c8  = Irg.CARD(8)
let c32 = Irg.CARD(32)
let ref_x = Irg.REF "x"
let ref_y = Irg.REF "y"
let ref_z = Irg.REF "z"
let ref_a = Irg.REF "a"


let _ =
	Irg.add_symbol "a" (Irg.PARAM ("a", Irg.TYPE_EXPR (Irg.CARD(8))));
	test ["a"] [
		getb ref_a 7 4;
		getb ref_a 3 2;
		getb ref_a 1 0
	]


let _ =
	Irg.dump_type := false;
	Irg.add_symbol "x" (Irg.PARAM ("x", Irg.TYPE_EXPR c16));
	Irg.add_symbol "y" (Irg.PARAM ("y", Irg.TYPE_EXPR c32));
	Irg.add_symbol "z" (Irg.PARAM ("z", Irg.TYPE_EXPR c8));
	test ["x"] [ref_x];
	test ["x"] [add ref_x (csti 1)];
	test ["x"] [shl ref_x (csti 4); getb ref_x 15 12];
	test ["x"] [shl (add ref_x (csti 1)) (csti 4); getb ref_x 15 12];
	test ["x"] [shl ref_x (csti 8); shr ref_x (csti 8)];
	test ["x"] [getb ref_x 15 8; getb ref_x 7 0];
	test ["x"] [add (getb ref_x 15 8) (csti 3); getb ref_x 7 0];
	test ["x"; "y"] [ref_x; ref_y];
	test ["x"; "z"] [ref_x; ref_z];
	test ["x"; "z"; "y"] [ref_x; ref_z; ref_y];
	test ["x"; "y"] [add ref_x (csti 2); sub ref_y (csti 3)];
	test ["x"; "y"] [concat ref_x ref_y];
	test ["x"; "z"] [concat ref_x ref_z];
	test ["x"; "y"] [concat (getb ref_x 15 10) ref_y; getb ref_x 9 0]
