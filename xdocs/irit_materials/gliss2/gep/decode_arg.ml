(*
 * GLISS2 -- image argument decoding
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






let asis str chan = output_string chan str


(* length of range interval *)
let range32 u l =
	Int32.add Int32.one (Int32.sub u l)
let range64 u l =
	Int64.add Int64.one (Int64.sub u l)
let mask32 n =
	Int32.sub (Int32.shift_left Int32.one (Int32.to_int n)) Int32.one
let mask64 n =
	Int64.sub (Int64.shift_left Int64.one (Int64.to_int n)) Int64.one



let add e1 e2 = Irg.BINOP (Sem.get_type_expr e1, Irg.ADD, e1, e2)
let sub e1 e2 = Irg.BINOP (Sem.get_type_expr e1, Irg.SUB, e1, e2)
let cst i32 = Irg.CONST (Irg.CARD(32), Irg.CARD_CONST i32)
let cst64 i64 = Irg.CONST (Irg.CARD(64), Irg.CARD_CONST_64 i64)
let csta i = Irg.CONST (Irg.CARD(32), i)
let csti i = Irg.CONST (Irg.CARD(32), Irg.CARD_CONST (Int32.of_int i))
let shl e1 e2 = Irg.BINOP (Sem.get_type_expr e1, Irg.LSHIFT, e1, e2)
let shr e1 e2 = Irg.BINOP (Sem.get_type_expr e1, Irg.RSHIFT, e1, e2)
let and_ e1 e2 = Irg.BINOP(Sem.get_type_expr e1, Irg.BIN_AND, e1, e2)
let or_ e1 e2 = Irg.BINOP(Sem.get_type_expr e1, Irg.BIN_OR, e1, e2)
let getb e1 e2 e3 = Irg.BITFIELD (Sem.get_type_expr e1, e1, e2, e3)
let concat e1 e2 = Irg.BINOP(Irg.CARD ((Sem.get_length_from_expr e1) + (Sem.get_length_from_expr e2)), Irg.CONCAT, e1, e2)

(** A 32-bits integer with all bits to 1. *)
let all_ones = Bitmask.mask_fill 32


(** Build a mask of n bits (initialized to one).
	@param n	Number of bits.
	@return		Generic integer. *)
let mask n = Bitmask.mask_fill n


(** Build a mask with ones from bit m to bit n.
	@param n	Upper mask bound.
	@param m	Lower mask bound.
	@return		Built mask. *)
let mask_range n m = 
(* !!DEBUG!! *)
(*let r =*)
Bitmask.mask_range n m
(*in
Printf.printf "mask_range %d %d = %s\n" n m (Bitmask.to_string r);
r *)


(** Get the mask of bits enclosing the given expression result.
	@param e	Expression to get mask for.
	@return		Matching mask. *)
let mask_of_expr e =
	mask (Sem.get_length_from_expr e)


(**
 * extends the smallest mask with 0s on the left so both resulting masks
 * have same length, then perform logical AND between the 2 same length masks
 *)
let and_mask m1 m2 =
	let (mm1, mm2) = Bitmask.set_same_length m1 m2 true in
	(*let res = *)Bitmask.logand mm1 mm2 (*in
	Printf.printf "******\nand_mask, m1=%s(%d), m2=%s(%d)\nmm1=%s(%d), mm2=%s(%d), res = %s(%d)\n"
		(Bitmask.to_string m1) (Bitmask.length m1) (Bitmask.to_string m2) (Bitmask.length m2)
		(Bitmask.to_string mm1) (Bitmask.length mm1) (Bitmask.to_string mm2) (Bitmask.length mm2)
		(Bitmask.to_string res) (Bitmask.length res);
	res*)


(**
 * extends the smallest mask with 0s on the left so both resulting masks
 * have same length, then perform logical OR between the 2 same length masks
 *)
let or_mask m1 m2 =
	let (mm1, mm2) = Bitmask.set_same_length m1 m2 true in
	(*Printf.printf "or_mask, m1=%s(%d), m2=%s(%d)\nmm1=%s(%d), mm2=%s(%d)\n"
		(Bitmask.to_string m1) (Bitmask.length m1) (Bitmask.to_string m2) (Bitmask.length m2)
		(Bitmask.to_string mm1) (Bitmask.length mm1) (Bitmask.to_string mm2) (Bitmask.length mm2);*)
	Bitmask.logor mm1 mm2


(** Perform OR mask on the masks of the given triplet list (parameter, mask, expression).
	@param l	Triplet list.
	@param m	Initial mask.
	@return		All list mask ORed. *)
let rec or_masks l m =
	match l with
	| [] -> m
	| (_, m', _)::t -> or_masks t (or_mask m m')


(** Scan a decode argument and return the list of parameter assignment.
	@param e	argument expression
	@param m	current mask
	@param y	current reverse expression
	@return		list of triplets (operation parameter, maskn reverse expression).
	@throw		*)
let rec scan_decode_argument e m y =
(*print_string "scan_decode_argument, e=";Irg.print_expr e; Printf.printf ", m=%s(%d), y=" (Bitmask.to_string m) (Bitmask.length m); Irg.print_expr y;print_string "\n";*)
	match e with

	| Irg.NONE -> failwith "scan_decode_argument"

	| Irg.CONST _ -> []

	| Irg.REF x ->
		(match Irg.get_symbol x with
		| Irg.PARAM _ -> [(x, m, y)]
		| _ -> raise (Toc.PreError (fun c -> Printf.fprintf c "unsupported symbol in image: %s" x)))

	| Irg.ELINE (file, line, e) ->
		(try scan_decode_argument e m y
		with Toc.PreError f -> raise (Toc.LocError (file, line, f)))

	| Irg.UNOP (t, Irg.BIN_NOT, e) ->
		scan_decode_argument e m (Irg.UNOP (t, Irg.BIN_NOT, y))

	| Irg.UNOP (t, Irg.NEG, e) ->
		scan_decode_argument e m (Irg.UNOP (t, Irg.NEG, y))

	| Irg.BITFIELD (t, b, u, l) ->
		let uc =
			try Sem.to_int32 (Sem.eval_const u)
			with Sem.SemError _ -> raise (Toc.PreError (asis "upper bitfield bound must be constant")) in
		let lc =
			try Sem.to_int32 (Sem.eval_const l)
			with Sem.SemError _ -> raise (Toc.PreError (asis "lower bitfield bound must be constant")) in
		scan_decode_argument
			b
			(Bitmask.shift_left m (Int32.to_int lc))
			(*(and_mask m (mask_range (Int32.to_int uc) (Int32.to_int lc)))*)
			(shl (and_ y (cst (mask32 (range32 uc lc)))) (cst lc))

	| Irg.BINOP (t, Irg.ADD, e1, e2) ->
		(try
			let k = Sem.eval_const e2 in
			scan_decode_argument e1 m (sub y (csta k))
		with Sem.SemError _ ->
			try
				let k = Sem.eval_const e1 in
				scan_decode_argument e2 m (sub y (csta k))
			with Sem.SemError _ ->
				raise (Toc.PreError (asis "only forms as 'x + k' or 'k + x' are supported in image")))

	| Irg.BINOP (t, Irg.SUB, e1, e2) ->
		(try
			let k = Sem.eval_const e2 in
			scan_decode_argument e1 m (add y (csta k))
		with Sem.SemError _ ->
			try
				let k = Sem.eval_const e1 in
				scan_decode_argument e2 m (sub (csta k) y)
			with Sem.SemError _ ->
				raise (Toc.PreError (asis "only forms as 'x + k' or 'k + x' are supported in image")))

	| Irg.BINOP (t, Irg.RSHIFT, e1, e2) ->
		(try
			let k = Sem.to_int32 (Sem.eval_const e2) in
				scan_decode_argument e1
					(Bitmask.sub (Bitmask.shift_left m (Int32.to_int k)) (Int32.to_int k) (Bitmask.length m))
					(shl y (cst k))
			with Sem.SemError _ ->
				raise (Toc.PreError (asis "only forms as 'x >> k' are supported in image")))

	| Irg.BINOP (t, Irg.LSHIFT, e1, e2) ->
		(try
			let k = Sem.to_int32 (Sem.eval_const e2) in
				scan_decode_argument e1
					(Bitmask.shift_right_logical m (Int32.to_int k))
					(shr y (cst k))
			with Sem.SemError _ ->
				raise (Toc.PreError (asis "only forms as 'x << k' are supported in image")))

	| Irg.BINOP (t, Irg.CONCAT, e1, e2) ->
		let s1 = Sem.get_length_from_expr e1 in
		let s2 = Sem.get_length_from_expr e2 in
		(*Printf.printf "::, s1=%d, s2=%d\n" s1 s2;
		(scan_decode_argument e1
			(and_mask m (Bitmask.shift_left (mask s1) s2))
			(shr (and_ y (cst64 (Int64.shift_left (mask64 (Int64.of_int s1)) s2))) (csti s2))) @
		(scan_decode_argument e2 (and_mask m (mask s2)) (and_ y (cst64 (mask64 (Int64.of_int s2)))))*)
		(scan_decode_argument e1 (and_mask m (mask s1)) (shr (and_ y (cst64 (Int64.shift_left (mask64 (Int64.of_int s1)) s2))) (csti s2)))
		@ (scan_decode_argument e2 (and_mask m (mask s2)) (and_ y (cst64 (mask64 (Int64.of_int s2)))))

	| Irg.BINOP (t, Irg.BIN_AND, e1, e2) ->
		(try
			let k = Sem.to_int32 (Sem.eval_const e2) in
			scan_decode_argument e1 (and_mask m (Bitmask.of_int32 k)) (and_ y (cst k))
		with Sem.SemError _ ->
			try
			let k = Sem.to_int32 (Sem.eval_const e1) in
			scan_decode_argument e2 (and_mask m (Bitmask.of_int32 k)) (and_ y (cst k))
			with Sem.SemError _ ->
				raise (Toc.PreError (asis "only forms as 'x & k' or 'k & x' are supported in image")))
	| Irg.BINOP (t, Irg.BIN_OR, e1, e2) ->
		let l1 = scan_decode_argument e1 m y in
		let l2 = scan_decode_argument e2 m y in
		let m1 = or_masks l1 Bitmask.void_mask in
		let m2 = or_masks l2 Bitmask.void_mask in
		let mr = and_mask m1 m2 in
		if Bitmask.is_null mr then l1 @ l2
		else raise (Toc.PreError (asis "both parts of the OR must be independent in an image"))
	| _
		-> raise (Toc.PreError (asis "unsupported expression"))


(** Decode all arguments with the given initial values.
	@param args		Image arguments.
	@param vals		Value of the image arguments.
	@return			Triplet list of (parameter, mask, expression). *)
let scan_decode_arguments args vals =
(*print_string "scan_decode_args";
print_string ":::args=["; List.iter (fun x -> Irg.print_expr x; print_string ", "; ) args; print_string "]\n";
print_string ":::vals=["; List.iter (fun x -> Irg.print_expr x; print_string ", "; ) vals; print_string "]\n";
*)
	List.fold_left2
		(fun r a v -> r @ (scan_decode_argument a (mask_of_expr a) v))
		[] args vals


(** Build a list of pairs (parameter name, expression to decode it).
	@param params	List of parameter names.
	@param args		Argument value of the image format.
	@param vals		Expression to access the actual argument value.
	@param inst		Currently processed instruction.
	@return			Decoding pairs. *)
let decode_parameters params args vals inst =

	let t = scan_decode_arguments args vals in
	let rec process (p, m, e) (p', m', e') =
		if p <> p' then
			(p, m, e)
		else
		begin
			if Bitmask.is_null (and_mask m m')
			then
				(p, or_mask m m', or_ e e')
			else raise (Toc.Error (Printf.sprintf "some parameter %s bits are redundant in image for instruction %s" p (Iter.get_user_id inst)))
		end in
	List.map
		(fun p ->
			let (p, m, e) = List.fold_left process (p, Bitmask.void_mask, cst Int32.zero) t in
			(*Printf.printf "map pme = %s: %s\n" p (Bitmask.to_string m);*)
			let lm = Bitmask.bit_count m in
			let lm' = Sem.get_type_length (Sem.get_type_ident p) in
			let m' = mask (Sem.get_type_length (Sem.get_type_ident p)) in
			let (mm, mm') = Bitmask.set_same_length m m' true in
			if lm == lm' then (p, e)
			else
				raise (Toc.Error (Printf.sprintf "some bits (%s) of parameter %s are missing (%s)"
					(Bitmask.to_string mm') p (Bitmask.to_string mm))))
		params


(** Test if the given arguments are complex.
	@param args		Argument to tests. *)
let rec is_complex args =
	let rec test arg =
		match arg with
		| Irg.ELINE (_, _, e) -> test e
		| Irg.REF _ -> false
		| _ -> true in
	match args with
	| [] -> false
	| a::t -> (test a) || (is_complex t)


(** Build a list of pairs (parameter name, expression to decode it).
	This function is faster than decode_parametres as it test first
	if parameter are really complex. If not, use a fast way to
	build the pairs.
	@param params	List of parameter names.
	@param args		Argument value of the image format.
	@param vals		Expression to access the actual argument value.
	@param inst		Currently processed instruction.
	@return			Decoding pairs. *)
let decode_fast params args vals inst =
	if (is_complex args) || (List.length args) <> (List.length vals)
	then decode_parameters params args vals inst
	else List.combine params vals



	
