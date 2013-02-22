(*
 * GLISS2 -- semantics check
 * Copyright (c) 2011, IRIT - UPS <casse@irit.fr>
 *
 * This file is part of GLISS2.
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
open Printf

exception SemError of string
exception SemErrorWithFun of string * (unit -> unit)
exception ManyDefaultsInSwitch




(*
transform an old style subpart alias declaration (let ax [1, card(16)] alias eax[16])
into a new style one with bitfield notation (let ax [1, card(16)] alias eax<16..0>)
before inserting the definition in the table
*)
(*let translate_old_style_aliases s =
	let is_array nm =
		match get_symbol nm with
		MEM(_, i, _, _)
		| REG(_, i, _, _)
		| VAR(_, i, _) ->
			i > 1
		| _ ->
			false
	in
	let get_type_length t =
		match t with
		| BOOL -> 1
		| INT n -> n
		| CARD n -> n
		| FIX (n,m) -> n + m
		| FLOAT (n,m) -> n + m
		| ENUM l ->
			let i = List.length l in
			int_of_float (ceil ((log (float i)) /. (log 2.)))
		| RANGE (_, m) ->
			int_of_float (ceil ((log (float (Int32.to_int m))) /. (log 2.)))
		| NO_TYPE
		| STRING
		| UNKNOW_TYPE ->
			failwith "length unknown"
	in
	let b_o =
		match get_symbol "bit_order" with
		UNDEF -> true
		| LET(_, STRING_CONST id) ->
			if (String.uppercase id) = "UPPERMOST" then true
			else if (String.uppercase id) = "LOWERMOST" then false
			else failwith "'bit_order' must contain either 'uppermost' or 'lowermost'"
		| _ -> failwith "'bit_order' must be defined as a string let"
	in
	let rec change_alias_attr mem_attr_l n =
		let t = CARD(32)
		in
		let const c =
			CONST (t, CARD_CONST (Int32.of_int c))
		in
		let sub e1 e2 =
			BINOP (t, SUB, e1, e2)
		in
		match mem_attr_l with
		[] ->
			[]
		| a::b ->
			(match a with
			ALIAS l ->
				(match l with
				LOC_REF(typ, name, i, l, u) ->
					if is_array name then
						a::(change_alias_attr b n)
					else
						if l=NONE && u=NONE then
							if b_o then
								(ALIAS(LOC_REF(typ, name, NONE, i, sub i (sub (const n) (const 1)) (*i-(n-1)*))))::(change_alias_attr b n)
							else
								(ALIAS(LOC_REF(typ, name, NONE, sub i (sub (const n) (const 1)) (*i-(n-1)*), i)))::(change_alias_attr b n)
						else
							a::(change_alias_attr b n)
				| _ ->
					a::(change_alias_attr b n)
				)
			| _ ->
				a::(change_alias_attr b n)
			)
	in
	match s with
	MEM(name, size, typ, m_a_l) ->
		MEM(name, size, typ, change_alias_attr m_a_l (get_type_length typ))
	| REG(name, size, typ, m_a_l) ->
		REG(name, size, typ, change_alias_attr m_a_l (get_type_length typ))
	| _ ->
			s
*)
(** False value. *)
let false_const = (*Irg.*)CARD_CONST Int32.zero
(** True value. *)
let true_const = (*Irg.*)CARD_CONST Int32.one


(** Convert from OCAML boolean to SimNML boolean.
	@param v	OCAML boolean.cstcst
	@return		SimNML boolean. *)
let to_bool v = if v then true_const else false_const


(** Convert constant to int32.
	@param c		Constant to convert.
	@return			Matching int32 value.
	@raise SemError	If the constant is not convertible. *)
let to_int32 c =
	match c with
	  CARD_CONST v -> v
	| _ -> raise (SemError "should evaluate to an int")


(** Convert an expression to a string.
	@param e		Expression to convert.
	@return			Converted to string.
	@raise SemError	If the conversion cannot be done. *)
let rec to_string e =
	match e with
	| Irg.CONST (_, Irg.STRING_CONST (s, _, _)) -> s
	| Irg.ELINE (_, _, e) -> to_string e
	| _ -> raise (SemError "should evaluate to a string")

(** Convert constant to int.
	@param c		Constant to convert.
	@return			Matching int value.			print_expr e1;
	@raise SemError	If the constant is not convertible. *)
let to_int c =
	Int32.to_int (to_int32 c)


(** Test if a constant is true.
	@param c	Constant to test.
	@return		True if constant is true. *)
let is_true c =
	match c with
	  NULL -> false
	| CARD_CONST v -> v <> Int32.zero
	| CARD_CONST_64 v-> v <> Int64.zero
	| STRING_CONST(v, _, _) -> v <> ""
	| FIXED_CONST v -> v <> 0.0





(**)
(* A verifier :
	-Est ce normal que y soit de type Int32 ?
	-Ne devrait on pas utiliser des shift_<left/right>_logical ?
*)

(** Rotate an int32 to the left.
	@param x	Value to rotate.
	@param y	Amount of rotation.
	@return		Result of the rotation.*)
let rotate_left x y =
	let s = Int32.to_int (Int32.logand y (Int32.of_int 0x1f)) in
	Int32.logor (Int32.shift_left x s) (Int32.shift_right x (32 - s))


(** Rotate an int32 to the right.
	@param x	Value to rotate.
	@param y	Amount of rotation.
	@return		Result of the rotation.*)
let rotate_right x y =
	let s = Int32.to_int (Int32.logand y (Int32.of_int 0x1f)) in
	Int32.logor (Int32.shift_right x s) (Int32.shift_left x (32 - s))


(** Evaluate an unary operator.
	@param op	Unary operator.
	@param c	Constant to apply the operator to.
	@return		Result of the operation. *)
let rec eval_unop op c =
	match (op, c) with
	  (NOT, _) ->
	  	CARD_CONST (if (is_true c) then Int32.zero else Int32.one)
	| (BIN_NOT, CARD_CONST v) ->
		CARD_CONST (Int32.lognot v)
	| (NEG, CARD_CONST v) ->
		CARD_CONST (Int32.neg v)
	| (NEG, FIXED_CONST v) ->
		FIXED_CONST (-. v)
	| _ ->
		raise (SemError (Printf.sprintf "bad type operand for '%s'"
			(string_of_unop op)))


(** Evaluate a binary operation.
	@param op	Operation.
	@param v1	First operand.
	@param v2	Second operand.
	@return 	Result. *)
let eval_binop_card op v1 v2 =
	match op with
	  ADD ->
	  	CARD_CONST (Int32.add v1 v2)
	| SUB ->
		CARD_CONST (Int32.sub v1 v2)
	| MUL ->
		(*Irg.*)CARD_CONST (Int32.mul v1 v2)
	| (*Irg.*)DIV		->
		(*Irg.*)CARD_CONST (Int32.div v1 v2)
	| (*Irg.*)MOD		->
		(*Irg.*)CARD_CONST (Int32.rem v1 v2)
	| (*Irg.*)EXP		->
		(*Irg.*)CARD_CONST (Int32.of_float ((Int32.to_float v1) ** (Int32.to_float v2)))
	| (*Irg.*)LSHIFT	->
		(*Irg.*)CARD_CONST (Int32.shift_left v1 (Int32.to_int v2))
	| (*Irg.*)RSHIFT	->
		(*Irg.*)CARD_CONST (Int32.shift_right v1 (Int32.to_int v2))
	| (*Irg.*)LROTATE	->
		(*Irg.*)CARD_CONST (rotate_left v1 v2)
	| (*Irg.*)RROTATE	->
		(*Irg.*)CARD_CONST (rotate_right v1 v2)
	| (*Irg.*)LT		->
		to_bool (v1 < v2)
	| (*Irg.*)GT		->
		to_bool (v1 > v2)
	| (*Irg.*)LE		->
		to_bool (v1 <= v2)
	| (*Irg.*)GE		->
		to_bool (v1 >= v2)
	| (*Irg.*)EQ		->
		to_bool (v1 = v2)
	| (*Irg.*)NE		->
		to_bool (v1 <> v2)
	| (*Irg.*)AND		->
		if (v1 <> Int32.zero) && (v2 <> Int32.zero) then true_const else false_const
	| (*Irg.*)OR		->
		if (v1 <> Int32.zero) || (v2 <> Int32.zero) then true_const else false_const
	| (*Irg.*)BIN_AND	->
		(*Irg.*)CARD_CONST (Int32.logand v1 v2)
	| (*Irg.*)BIN_OR	->
		(*Irg.*)CARD_CONST (Int32.logor v1 v2)
	| (*Irg.*)BIN_XOR	->
		(*Irg.*)CARD_CONST (Int32.logxor v1 v2)
	| _ ->
		raise (SemError (Printf.sprintf "bad type operand for '%s'"
			(string_of_binop op)))


(** Evaluate a fixed binary operation.
	@param op	Operation.
	@param v1	First operand.
	@param v2	Second operand.
	@return 	Result. *)
let eval_binop_fixed op v1 v2 =
	match op with
	  ADD		->
		(*Irg.*)FIXED_CONST (v1 +. v2)
	| SUB		->
		(*Irg.*)FIXED_CONST (v1 -. v2)
	| MUL		->
		(*Irg.*)FIXED_CONST (v1 *. v2)
	| DIV		->
		(*Irg.*)FIXED_CONST (v1 /. v2)
	| EXP		->
		(*Irg.*)FIXED_CONST (v1 ** v2)
	| LT		->
		to_bool (v1 < v2)
	| GT		->
		to_bool (v1 > v2)
	| LE		->
		to_bool (v1 <= v2)
	| GE		->
		to_bool (v1 >= v2)
	| EQ		->
		to_bool (v1 = v2)
	| NE		->
		to_bool (v1 <> v2)
	| AND		->
		if v1 <> 0. && v2 <> 0. then true_const else false_const
	| OR		->
		if v1 <> 0. || v2 <> 0. then true_const else false_const
	| _ ->
		raise (SemError (Printf.sprintf "bad type operand for '%s'"
			(string_of_binop op)))


(** Evaluate a string binary operation.
	@param op	Operation.
	@param v1	First operand.
	@param v2	Second operand.
	@return		Result. *)
let eval_binop_string op v1 v2 =
	match op with
	  LT 		-> to_bool (v1 < v2)
	| GT		-> to_bool (v1 > v2)
	| LE		-> to_bool (v1 <= v2)
	| GE		-> to_bool (v1 >= v2)
	| EQ		-> to_bool (v1 = v2)
	| NE		-> to_bool (v1 <> v2)
	| CONCAT	-> STRING_CONST(v1 ^ v2, false, NO_TYPE)
	| _ ->
		raise (SemError (Printf.sprintf "bad type operand for '%s'"
			(string_of_binop op)))



(** Evaluate a binary operator.
	@param op	Binary operator.
	@param c1	First operand.
	@param c2	Second operand.
	@return		Result of the operation. *)
let eval_binop op c1 c2 =
	match (c1, c2) with
  	  (Irg.CARD_CONST v1, Irg.CARD_CONST v2) ->
		eval_binop_card op v1 v2
	| (Irg.FIXED_CONST v1, Irg.CARD_CONST v2) ->
		eval_binop_fixed op v1 (Int32.to_float v2)
	| (Irg.CARD_CONST v1, Irg.FIXED_CONST v2) ->
		eval_binop_fixed op (Int32.to_float v1) v2
	| (Irg.FIXED_CONST v1, Irg.FIXED_CONST v2) ->
		eval_binop_fixed op v1 v2
	| (Irg.STRING_CONST(v1, b1, t1), Irg.STRING_CONST(v2, b2, t2)) ->
		if not b1 && not b2 then
			eval_binop_string op v1 v2
		else
			raise (SemError "cannot evaluate a canonical const here, value can only be got via C code")
	| _ ->
		raise (SemError (Printf.sprintf "bad type operand for '%s'"
			(string_of_binop op)))


(** Perform the coercition function on the given value.
	@param t		Type to coerce to.
	@param v		Value to coerce.
	@return			Result of coercion.
	@raise SemError	If the coercion is not supported. *)
let eval_coerce t v =
	let mask32 i n = Int32.logand i (Int32.pred (Int32.shift_left Int32.one n)) in
	let mask64 i n = Int64.logand i (Int64.pred (Int64.shift_left Int64.one n)) in
	match t, v with
	| _, NULL -> NULL
	| BOOL, CARD_CONST i -> CARD_CONST (if i = Int32.zero then Int32.zero else Int32.one)
	| BOOL, CARD_CONST_64 i -> CARD_CONST (if i = Int64.zero then Int32.zero else Int32.one)
	| INT n, CARD_CONST i when n <= 32 -> v
	| INT n, CARD_CONST i when n <= 64 -> CARD_CONST_64 (Int64.of_int32 i)
	| INT n, CARD_CONST_64 i when n <= 32 -> CARD_CONST (Int64.to_int32 i)
	| INT n, CARD_CONST_64 i when n <= 64 -> v
	| INT n, FIXED_CONST i when n <= 32 -> CARD_CONST (Int32.of_float i)
	| INT n, FIXED_CONST i when n <= 64 -> CARD_CONST_64 (Int64.of_float i)
	| CARD n, CARD_CONST i when n <= 32 -> CARD_CONST (mask32 i n)
	| CARD n, CARD_CONST i when n <= 64 -> CARD_CONST_64 (mask64 (Int64.of_int32 i) n)
	| CARD n, CARD_CONST_64 i when n <= 32 -> CARD_CONST (mask32 (Int64.to_int32 i) n)
	| CARD n, CARD_CONST_64 i when n <= 64 -> CARD_CONST_64 (mask64 i n)
	| CARD n, FIXED_CONST i when n <= 32 -> CARD_CONST (Int32.of_float (abs_float i))
	| CARD n, FIXED_CONST i when n <= 64 -> CARD_CONST_64 (Int64.of_float (abs_float i))
	| FLOAT _, CARD_CONST i -> FIXED_CONST (Int32.to_float i)
	| FLOAT _, CARD_CONST_64 i -> FIXED_CONST (Int64.to_float i)	
	| _ -> raise (SemError "unsupported constant coerction")


(** Perform the expression switch.
	@param c		Condition.
	@param cases	Cases of the switch.
	@param def		Default value. *)
let rec select c cases def =
	  match cases with
	    [] -> eval_const def
	  | (cp, e)::_ when cp = c -> eval_const e
	  | _::t -> select c t def

(** Evaluate an expression to constant.
	@param expr			Expression to evaluate.
	@raise SemError		If the expression is not constant. *)
and eval_const expr =
	match expr with
	  CONST (_,cst) ->
	  	cst
	| UNOP (_,op, e) ->
		eval_unop op (eval_const e)
	| BINOP (_,op, e1, e2) ->
		eval_binop op (eval_const e1) (eval_const e2)
	| IF_EXPR(_,c, t, e) ->
		if is_true (eval_const c) then eval_const t else eval_const e
	| SWITCH_EXPR (_,c, cases, def) ->
		select c cases def
	| REF id ->
		(match get_symbol id with
		  LET (_, cst) -> cst
		| ENUM_POSS (_,_,v,_) -> CARD_CONST v
		| _ -> raise (SemError (id ^ " is not a constant symbol")))
	| BITFIELD (t, e, u, l) -> raise (SemError "unsupported bitfield")
	| ELINE (_, _, e) -> eval_const e
	| COERCE (t, e) -> eval_coerce t (eval_const e)
	| _ ->
		raise (SemError "this expression should be constant")


(** Find a type by its identifier.
	@param id		Identifier of the looked type.
	@return			Type matching the identifier.
	@raise SemError	If the identifier does not exists or if the named item is
not a type. *)
let type_from_id id =
	try
		match StringHashtbl.find syms id with
		  TYPE (_, te) -> te
		| _ ->	raise (SemError (Printf.sprintf "%s does not named a type" id))
	with Not_found ->
		raise (SemError (Printf.sprintf "unknown identifier \"%s\"" id))


(** Check the matching of a type and a constant.
	@param t	Type to check.
	@param c	Constant to check.
	@return		True if they match, false else. *)
let check_constant_type t c =
	match (t, c) with
	  (BOOL, CARD_CONST v) ->
		(v = Int32.zero) || (v = Int32.one)
	| (CARD _, CARD_CONST _)
	| (INT _, CARD_CONST _)
	| (FIX _, FIXED_CONST _)
	| (FLOAT _, FIXED_CONST _) ->
		true
	| (RANGE (l, u), CARD_CONST v) ->
		((Int32.compare v l) >= 0) && ((Int32.compare v u) <= 0)
	| _ ->
		false


(** Give the size of a memory location
   @author PJ
   @param  loc		a memory location
   @return  the number of bit available of the location
*)
(*let rec  get_location_size loc =
	match loc with
	  LOC_REF id -> (match Irg.get_symbol id with
			UNDEF -> raise (SemError (Printf.sprintf "get_location_size : undeclared memory location :\"%s\"" id))
			|MEM(s,i,t,_)|REG(s,i,t,_)|VAR(s,i,t) ->( match t with
								 NO_TYPE |RANGE _ -> 8  (* maybe à modifier *)
								|INT t|CARD t -> t
								|FIX(n,l)|FLOAT(n,l) -> n+l
								| _ -> raise (SemError "unexpected type")  )
			| _ ->raise (SemError (Printf.sprintf "get_location_size : identifier is not a memory location reference :\"%s\"" id)))

	| LOC_ITEMOF (loc,_) -> (get_location_size loc)
	| LOC_BITFIELD (_,e1,e2) ->(match ((eval_const e2),(eval_const e1))with
					 (CARD_CONST t,CARD_CONST v)-> (Int32.to_int t) - (Int32.to_int v)
					|(FIXED_CONST t,FIXED_CONST v) -> (int_of_float t)-(int_of_float v)
					|(CARD_CONST t,FIXED_CONST v) -> (Int32.to_int t)-(int_of_float v)
					|(FIXED_CONST t,CARD_CONST v) -> (int_of_float t)- (Int32.to_int v)
					|(STRING_CONST t,_)|(_,STRING_CONST t) -> raise (SemError (Printf.sprintf "get_location_size : uncompatible bitfield identifier :\"%s\"" t))
					|(NULL,_)|(_,NULL)->raise (SemError " memory location untyped "))
	| LOC_CONCAT (l1,l2) -> ((get_location_size l1) + (get_location_size l2))*)


(** make the implicit conversion a to b in b op a)
  @author PJ
  @param loc		location
  @param expr_b		expression to cast
  @return           expr_b casted to loc
*)

(*nml_cast a b =
	match (a,b) with
	  (INT k,CARD(n,m)) ->  n+m
	| _ -> failwith*)


(** Test if a float number respects the IEE754 specification
    @param f          a nml float
    @return   true if the float is an IEEE754 float, false otherwise
    bit sign is first bit of the mantisse *)
let is_IEEE754_float f = match f with
	(FLOAT(m,e)) ->(match(m+e) with
			 32 -> (e =8)&&(m=24)
			| 64 -> (e = 11)&&(m =53)
			| 80 -> (e = 15)&&(m = 65)
			| _  -> raise (SemError "float number doesn't follow IEEE754 specification "))
	| _ -> raise (SemError "function expect float number but parameter is not ")




(** Get the type associated with an identifiant.
	@param id	The identifiant to type.
	@return A type.
	@raise SemError if the keyword is not defined
*)
let rec get_type_ident id=
	let symb= get_symbol id
	in

	if symb=UNDEF
	then
		raise (SemError (Printf.sprintf "The keyword \"%s\" not defined" id))
	else

	(**)
	(*print_string "Spec : ";
	print_spec symb;*)
	(**)
	match symb with
	 LET (_,c)-> (match c with
			 NULL-> NO_TYPE
			|CARD_CONST _->CARD 32
			|CARD_CONST_64 _->CARD 64
			|STRING_CONST(_, b, t) -> if b then t else STRING
			|FIXED_CONST _->FLOAT (24,8))
	|TYPE (_,t)->(match t with
			ENUM l->(let i = List.length l in
				CARD (int_of_float (ceil ((log (float i)) /. (log 2.)))))
			|_->t)
	|MEM (_,_,t,_)->t
	|REG (_,_,t,_)->t
	|VAR (_,_,t)->t
	|AND_MODE (_,l,e,_)->	(
				 param_stack l;
				 let t= get_type_expr e
				 in
				 param_unstack l;
				 t
				)

	(* --- this was used to check that all the modes composing an OR_MODE where of the same type. But it was abandoned because of compatibility issues --- *)
	(*|OR_MODE (n,l)->let type_mode = get_type_ident (List.hd l)
			in
			if List.for_all (fun a-> if (get_type_ident a)=type_mode then true else false) (List.tl l)
				then type_mode
				else
					let dsp=(fun _-> (	(List.map (fun a->print_string "\n--- ";print_string a;print_string " : ";print_type_expr (get_type_ident a);print_string "\n") l)	);()	)
					in
					raise (SemErrorWithFun ((Printf.sprintf "The or_mode %s is not of consistant type\n" n),dsp))*)
	|OR_MODE _->UNKNOW_TYPE

	|PARAM (n,t)->( rm_symbol n;
			let type_res=(
				match t with
				 TYPE_ID idb->get_type_ident idb
				|TYPE_EXPR tb->tb)	(* ??? *)
			in
			add_param (n,t);
			type_res)
	| ENUM_POSS (_,i,_,_)->get_type_ident i
	| ATTR (ATTR_EXPR (_, expr)) -> get_type_expr expr
	| ATTR _ -> NO_TYPE
	| _ ->NO_TYPE

(** Get the type of an expression
	@param exp 	Expression  to evaluate
	@return		the type of the parameter exp	*)
and get_type_expr exp=
	(**)
	(*print_string "Expr : ";
	print_expr exp;
	print_string "\n";*)
	(**)
	match exp with
	| NONE->NO_TYPE
	| COERCE(t, _) -> t
	| FORMAT (_, _) -> STRING
	| CANON_EXPR (t, _, _) -> t
	| REF id -> get_type_ident id
	| FIELDOF (t, _, _) -> t
	| ITEMOF (t, _, _) -> t
	| BITFIELD (FLOAT(n, m), _, _, _) -> CARD(n + m)
	| BITFIELD (t, _, _, _) -> t
	| UNOP (t, _, _) -> t
	| BINOP (t, _, _, _) -> t
	| IF_EXPR (t, _, _, _) -> t
	| SWITCH_EXPR (t, _, _, _) -> t
	| CONST (t,_)->t
	| ELINE (_, _, e) -> get_type_expr e
	| EINLINE _ -> NO_TYPE
	| CAST(t, _) -> t


(** Give the bit length of a type expression
	@param t		the type expression of which we want the size
	@return 		the bit-length of the expression (as an iteger)
	@raise Failure	this exception is raised when it is not possible
					to determine the length (for expressions of type NO_TYPE,
					STRING or UNKNOW_TYPE)
*)
let get_type_length t =
	match t with
	| BOOL -> 1
	| INT n -> n
	| CARD n -> n
	| FIX (n,m) -> n + m
	| FLOAT (n,m) -> n + m
	| ENUM l ->
		let i = List.length l in
		int_of_float (ceil ((log (float i)) /. (log 2.)))
	| RANGE (_, m) ->
		int_of_float (ceil ((log (float (Int32.to_int m))) /. (log 2.)))
	| NO_TYPE
	| STRING
	| UNKNOW_TYPE ->
		failwith "length unknown"


(** Give the bit lenght of an expression
	@param e	the expression of wich we want the size
	@return 	the bit-length of the expression (as an iteger)
	@raise Failure	this exception is raised when it is not possible to determine the length (for expressions of type NO_TYPE, STRING or UNKNOW_TYPE)
*)
let get_length_from_expr e=
	get_type_length (get_type_expr e)


(** Check the matching of a unary operation and the type of its operand.
	@param t	Type to check.
	@param uop	Operation to check.
	@return	True if they match, false else.
*)
let check_unop_type t uop =
	match t with
	 UNKNOW_TYPE->true
	|(CARD _|INT _	|FIX (_,_) | FLOAT (_,_))->((uop=NOT)||(uop=NEG)||(uop=BIN_NOT))
	|STRING->false
	|_->(uop=NOT||uop=BIN_NOT)


(** Create a unary operation with a correct type in function of its operand.
	@param e	First operand.
	@param uop	Operation to apply.
	@return	An UNOP expression
	@raise SemErrorWithFun	Raised when the type of the operand is not compatible with the operation
*)
let get_unop e uop=

	let t=get_type_expr e
	in
	if(not (check_unop_type t uop))
		then	let aff=fun _->(
						print_string (string_of_unop uop);
						print_string " ";
						print_string "(";
						print_expr e;
						print_string ") -";
						print_type_expr t;
						print_string "-";
						print_string "\n"
					 )
			in
			raise (SemErrorWithFun ("This unary operation is semantically incorrect",aff))



		else 	(match (uop,t) with
			(_,UNKNOW_TYPE)->UNOP (UNKNOW_TYPE, uop,e)
			|(NEG,CARD n)->UNOP(INT n,uop,e)	(*for the negation of a CARD, the type is INT *)
			|_->UNOP (t,uop,e))



(** Perform automatic-coercition between numeric types, coercing to bigger type.
	@param e1	First expression.
	@param e2	Second expression.
	@return		(coerced first expression, coerced second expression) *)
let num_auto_coerce e1 e2 =
	let t1 = get_type_expr e1 in
	let t2 = get_type_expr e2 in
	if t1 = t2 then (e1, e2) else
	match t1, t2 with

	(* BOOL base *)
	| BOOL, INT _
	| BOOL, CARD _
	| BOOL, FLOAT _
	| BOOL, RANGE _
	| BOOL, ENUM _ -> (COERCE(t2, e1), e2)

	(* INT base *)
	| INT _, BOOL -> (e1, COERCE(t1, e2))
	| INT n1, INT n2 ->
		if n1 > n2 then (e1, COERCE(t1, e2))
		else (COERCE(t2, e1), e2)
	| INT n1, CARD n2 ->
		if n1 >= n2 then (e1, COERCE(t1, e2))
		else (COERCE(INT(n2), e1), COERCE(INT(n2), e2))
	| INT _, FLOAT _ -> (COERCE(t2, e1), e2)
	| INT _, RANGE _ -> (e1, COERCE(t1, e2))
	| INT _, ENUM _ -> (e1, COERCE(t1, e2))

	(* CARD base *)
	| CARD _, BOOL -> (e1, COERCE(t1, e2))
	| CARD n1, INT n2 ->
		if n1 <= n2 then (COERCE(t2, e1), e2)
		else (COERCE(INT(n1), e1), COERCE(INT(n1), e2))
	| CARD n1, CARD n2 ->
		if n1 < n2 then (COERCE(t2, e1), e2)
		else (e1, COERCE(t1, e2))
	| CARD _, FLOAT _ -> (COERCE(t2, e1), e2)
	| CARD _, RANGE _ -> (e1, COERCE(t1, e2))
	| CARD _, ENUM _ -> (e1, COERCE(t1, e2))

	(* FLOAT base *)
	| FLOAT _, BOOL
	| FLOAT _, INT _
	| FLOAT _, CARD _ -> (e1, COERCE(t1, e2))
	| FLOAT (n1, m1), FLOAT (n2, m2) ->
		if n1 + m1 > n2 + m2 then (e1, COERCE(t1, e2))
		else (COERCE(t2, e1), e2)
	| FLOAT _, RANGE _
	| FLOAT _, ENUM _ -> (e1, COERCE(t1, e2))

	| _ -> 
	(* !!DEBUG!! *)
	print_string "num_auto_coerce, t1=";
	Irg.print_type_expr t1;
	print_string ", t2=";
	Irg.print_type_expr t2;
	print_string "\n";
	raise (SemError "forbidden operation")


(** Check the matching of a binary operation and the type of its operands.
	@param t1	First type to check.
	@param t2	Second type to check
	@param bop	Operation to check.
	@return	True if they match, false else.
*)
(*let check_binop_type t1 t2 bop =
	if(t1=NO_TYPE ||t2=NO_TYPE)
	then false
	else

	if (t1=UNKNOW_TYPE||t2=UNKNOW_TYPE)
	then true
	else

	match bop with
	|(ADD|SUB)->(match (t1,t2) with
			((CARD _,CARD _)
			 |(INT _,INT _)
			 |(INT _, CARD _)
			 |(CARD _,INT _)
			 |(FLOAT _,FLOAT _)
			 |(FIX _,FIX _))->true
			|_->false)
	|(MUL|DIV|MOD)-> (match (t1,t2) with
			((CARD _,CARD _)
			 |(INT _,INT _)
			 |(INT _, CARD _)
			 |(CARD _,INT _)
			 |(FLOAT _,FLOAT _)
			 |(FIX _,FIX _)
			 |(FIX _,CARD _)
			 |(CARD _,FIX _)
			 |(FLOAT _,CARD _)
			 |(CARD _,FLOAT _)
			 |(FIX _,INT _)
			 |(INT _,FIX _)
			 |(FLOAT _,INT _)
			 |(INT _,FLOAT _))->true
			|_->false)
	|EXP-> (t1!=BOOL)&&(t2!=BOOL)&&(t1!=STRING)&&(t2!=STRING)
	|(LSHIFT|RSHIFT|LROTATE|RROTATE)->
					(match t1 with
					(CARD _|INT _|FIX _|FLOAT _)->(match t2 with
									(CARD _|INT _)->true
									|_->false)
					|_->false)


	|(LT|GT|LE|GE|EQ|NE)->	(match t1 with
					(CARD _|INT _|FIX _|FLOAT _)->(match t2 with
									(CARD _|INT _|FIX _|FLOAT _)->true
									|_->false)
					|_->false)
	|(AND|OR)->true
	|(BIN_AND|BIN_OR|BIN_XOR)-> (match t1 with
					(BOOL|CARD _|INT _|FIX _|FLOAT _)->(match t2 with
									(BOOL|CARD _|INT _|FIX _|FLOAT _)->true
									|_->false)
					|_->false)
	|CONCAT-> true*)


(** Create an add/sub with a correct type in function of its operands.
	This function is used in get_binop.
	@param e1	First operand.
	@param e2	Second operand.
	@param bop	ADD/SUB
	@return	An ADD/SUB expression
	@raise Failure	Raised when the type of the operands are not compatible with the operation *)
let get_add_sub e1 e2 bop =
	match (get_type_expr e1, get_type_expr e2) with
	| UNKNOW_TYPE, _
	| _, UNKNOW_TYPE ->
		BINOP (UNKNOW_TYPE, bop, e1, e2)
	| _, _ ->
		let (e1, e2) = num_auto_coerce e1 e2 in
		BINOP (get_type_expr e1, bop, e1, e2)


(** Create a mult/div/mod with a correct type in function of its operands.
	This function is used in get_binop.
	@param e1	First operand.
	@param e2	Second operand.
	@param bop	MUL/DIV/MOD
	@return	A MUL/DIV/MOD expression
	@raise Failure	Raised when the type of the operands are not compatible with the operation
*)
let get_mult_div_mod e1 e2 bop=
	let t1 = get_type_expr e1
	and t2 = get_type_expr e2 in
	match (t1,t2) with
	| UNKNOW_TYPE, _
	| _, UNKNOW_TYPE ->
		BINOP (UNKNOW_TYPE, bop, e1, e2)
	| _, _ ->
		let (e1, e2) = num_auto_coerce e1 e2 in
		BINOP (get_type_expr e1, bop, e1, e2)


(** Convert given type to raw card.
	@param e	Expression to convert. *)
let to_card e =
	match get_type_expr e with
	| FLOAT _
	| FIX _ -> CAST(get_type_expr e, e)
	| _ -> e


(** Create a concat with a correct type in function of its operands.
	This function is used in get_binop.
	@param e1	First operand.
	@param e2	Second operand.
	@return	A CONCAT expression
	@raise Failure	Raised when the type of the operands are not compatible with the operation
*)
let rec get_concat e1 e2 =
	(* TODO convert arguments to card *)
	try
		let length = (get_length_from_expr e1) + (get_length_from_expr e2) in
		Irg.BINOP (CARD length, CONCAT, to_card e1, to_card e2)
	with Failure "length unknown" ->
		let dsp= fun _->
			(print_string "op 1 : "; print_expr e1;print_string " type : "; print_type_expr (get_type_expr e1); print_string "\n";
			print_string "op 2 : "; print_expr e2;print_string " type : "; print_type_expr (get_type_expr e2); print_string "\n") in
		raise (SemErrorWithFun ("unable to concatenate these operandes",dsp))


(** Check types in comparison.
	@param bop	Operator.
	@param e1	First operand.
	@param e2	Second operand.
	@return		Checked expression. *)
let get_compare bop e1 e2 =
	let t1 = get_type_expr e1
	and t2 = get_type_expr e2 in
	match (t1,t2) with
	| UNKNOW_TYPE, _
	| _, UNKNOW_TYPE ->
		BINOP (UNKNOW_TYPE, bop, e1, e2)
	| _, _ ->
		let (e1, e2) = num_auto_coerce e1 e2 in
		BINOP (BOOL, bop, e1, e2)


(** Convert an expression to boolean.
	@param e	Expression to convert.
	@return		Converted expression. *)
let to_bool e =
	match get_type_expr e with
	| UNKNOW_TYPE
	| BOOL -> e
	| _ -> COERCE(BOOL, e)


(** Convert an expression to condition.
	@param e	Expression to convert.
	@return		Converted expression. *)
let to_cond e =
	match get_type_expr e with
	| UNKNOW_TYPE
	| _ -> e


(** Check type for a logic operation.
	@param bop	Operator.
	@param e1	First operand.
	@param e2	Second operand.
	@return		Checked expression. *)
let get_logic bop e1 e2 =
	let t1 = get_type_expr e1
	and t2 = get_type_expr e2 in
	match (t1,t2) with
	| UNKNOW_TYPE, _
	| _, UNKNOW_TYPE ->
		BINOP (UNKNOW_TYPE, bop, e1, e2)
	| _, _ ->
		BINOP (BOOL, bop, to_cond e1, to_cond e2)


(** Check type for a binary operation.
	@param bop	Operator.
	@param e1	First operand.
	@param e2	Second operand.
	@return		Checked expression. *)
let get_bin bop e1 e2 =
	let t1 = get_type_expr e1
	and t2 = get_type_expr e2 in
	match (t1,t2) with
	| UNKNOW_TYPE, _
	| _, UNKNOW_TYPE ->
		BINOP (UNKNOW_TYPE, bop, e1, e2)
	| NO_TYPE, _
	| _, NO_TYPE -> BINOP(NO_TYPE, bop, e1, e2)
	| _, _ ->
		let s1 = get_type_length t1 in
		let s2 = get_type_length t2 in
		let e1, e2 =
			if t1 = t2 then (to_card e1, to_card e2) else
			if t1 < t2 then (COERCE(CARD s2, e1), e2)
			else (to_card e1, COERCE(CARD s1, to_card e2)) in
		BINOP(CARD(max s1 s2), bop, e1, e2)


(** Check type for a shift operations.
	@param bop	Operator.
	@param e1	First operand.
	@param e2	Second operand.
	@return		Checked expression. *)
let get_shift bop e1 e2 =
	(* TODO convert arguments to card *)
	let t1 = get_type_expr e1
	and t2 = get_type_expr e2 in
	match (t1,t2) with
	| UNKNOW_TYPE, _
	| _, UNKNOW_TYPE ->
		BINOP (UNKNOW_TYPE, bop, e1, e2)
	| _, _ ->
		let s = get_type_length t1 in
		BINOP(CARD(s), bop, to_card e1, e2)


(** Create a binary operation with a correct type in function of its operands.
	@param e1	First operand.
	@param e2	Second operand.
	@param bop	Operation to apply.
	@return	A BINOP expression.
	@raise SemErrorWithFun	Raised when the type of the operands is not compatible with the operation
*)
let rec get_binop e1 e2 bop =

	let aff _ =
		let t1 = get_type_expr e1 in
		let t2 = get_type_expr e2 in
		print_expr e1;
		print_string ": ";
		print_type_expr t1;
		print_string (" " ^ (string_of_binop bop) ^ " ");
		print_expr e2;
		print_string ": ";
		print_type_expr t2;
		print_string "\n" in

	try
		Irg.ELINE (!Lexer.file, !Lexer.line,
			match bop with
	 		| ADD
	 		| SUB -> get_add_sub e1 e2 bop
			| MUL
			| DIV
			| MOD -> get_mult_div_mod  e1 e2 bop
			| EXP -> BINOP (get_type_expr e1, bop, e1, e2)
			| LSHIFT
			| RSHIFT
			| LROTATE
			| RROTATE -> get_shift bop e1 e2
			| LT
			| GT
			| LE
			| GE
			| EQ
			| NE -> get_compare bop e1 e2
			| AND
			| OR -> get_logic bop e1 e2
			| BIN_AND
			| BIN_OR
			| BIN_XOR -> get_bin bop e1 e2
			| CONCAT-> get_concat e1 e2)
	with SemError msg ->
		raise (SemErrorWithFun (msg, aff))


(** coerce, eventually, the rvalue in a SET or SETSPE statement if needed,
    we deal with int and card
    @param	l	the location of the SET(SPE)
    @param	e	the rvalue expression to be coerced if needed
    @return		the rvalue expression coerced or not
*)
let check_set_stat l e =
	let t2 = get_type_expr e
	in
	let t1 =
		match l with
		LOC_NONE ->
			NO_TYPE
		| LOC_REF(t, _, _, _, _) ->
			t
		| LOC_CONCAT(t, _, _) ->
			t
	in
			(* !!DEBUG!! *)
			(*print_string "check_set_stat\n";
			print_string "\t(loc) "; print_type_expr t1; print_string " : "; print_location l; print_char '\n';
			print_string "\t(exp) "; print_type_expr t2; print_string " : "; print_expr e; print_char '\n';*)
			match t1 with
			INT(n1) ->
				(match t2 with
				INT(n2) ->
					if n1 > n2 then
						begin
						(* !!DEBUG!! *)
						(*Printf.printf "\tcoerce e -> INT(%d)\n" n1;*)
						COERCE(INT(n1), e)
						end
					else
						(* unlike with binops, we can't coerce here *)
						e
				| _ ->
					e
				)
			| CARD(n1) ->
				(match t2 with
				INT(n2) ->
					if n1 > n2 then
						begin
						(* !!DEBUG!! *)
						(*Printf.printf "\tcoerce e -> INT(%d)\n" n1;*)
						COERCE(INT(n1), e)
						end
					else
						e
				| _ ->
					e
				)
			| _ ->
				e


(** Raise a type error message for two operands.
	@param t1	Type of first operand.
	@param t2	Type of second operand. *)
let raise_type_error_two_operand t1 t2 =
	let f _ =
		print_string "first operand : ";
		Irg.print_type_expr t1;
		print_string "\n";
		print_string "second operand : ";
		Irg.print_type_expr t2;
		print_string "\n" in
	raise (SemErrorWithFun ("incompatible type of 'if' parts", f))


(** Check if the possible expressions of the conditionnal branchs of an
	if-then-else expression give a valid if-then-else expression.
	It check if the types of the differents possibility are compatible
	(for this, it use the same compatibility rule than the addition).
	@param e1	the then-expression
	@param e2	the else-expression
	@return		Type of the result, raise an exception else.
*)
let check_if_expr e1 e2=
	let t1 = get_type_expr e1 in
	let t2 = get_type_expr e2 in
	match (t1, t2) with
	| (CARD _,CARD _)
	| (INT _,INT _)
	| (INT _, CARD _)
	| (CARD _,INT _)
	| (FLOAT _,FLOAT _)
	| (FIX _,FIX _)
	| (STRING, STRING)
	(* added to debug x86,a lot of unknown types (mode values) are encountered *)
	| (UNKNOW_TYPE, UNKNOW_TYPE)
		-> t1
	| (UNKNOW_TYPE, STRING)
	| (STRING, UNKNOW_TYPE)
		-> STRING
	| _ ->
	(* !!DEBUG!! *)
	(*print_string "e1="; Irg.print_expr e1; print_string "#e2="; Irg.print_expr e2; print_string "#\n";*)

	raise_type_error_two_operand t1 t2


(** Get the interval value of an integer type.
	@param t	Type to get interval of.
	@return		(minimum, maximum) or (0, 0) for non integer type. *)
let interval_of t =
	match t with
	| BOOL -> (0, 1)
	| CARD(n) -> (0, (1 lsl n) - 1)
	| INT(n) -> (-(1 lsl (n - 1)), (1 lsl (n - 1)) - 1)
	| _ -> (0, 0)


(** Check if the given parameters of a switch expression give a valid switch expression.
	It check that all the cases are of the same type than the condition,
	that all the cases and the default return an expression of the same type,
	that all pssibilites are covered by the cases.

	TODO : Allow compatibles types (instead of strictly the same type) to be presents in the conditional part of the cases

	@param test	the condition of the switch.
	@param list_case	the couple list of the cases.
	@param default	the default of the switch.(NONE if no default)
	@return the type of the switch
	@raise SemError	Raised when the parameters are incorrect
*)
let check_switch_expr test list_case default=


	(* --- this part is a definition of many subfunctions used in the verification --- *)
	let rec is_param_of_type_enum e =	(* check if an expression if a param of type eum *)
 		match e with
		| REF i ->
			(match (get_symbol i) with
			| PARAM (n,t)->	rm_symbol n;
				let value =
					(match t with
					| TYPE_ID ti->
						(match (get_symbol ti) with
						| TYPE (_,t)->
							(match t with
							| ENUM _-> true
							| _ ->false)
						|_->false)
					| TYPE_EXPR te->
						(match te with
						| ENUM _->true	(*Possible ?*)
						| _ -> false)) in
				add_param (n,t); value
			|_->false)
		| ELINE (_, _, e) -> is_param_of_type_enum e
		| _ -> false

	and get_list_poss_from_enum_expr e = (* Get a list of all possibility of the enum which is the type of the expression *)
		let rec temp id =
			(match get_symbol id with
			| TYPE (_,t)->
				(match t with
				| ENUM l->l
				| _ -> failwith "get_list_poss_from_enum_expr : expr is not an enum")
			| PARAM (n,t) ->
				(rm_symbol n;
				let value =
					(match t with
					| TYPE_ID s->temp s
					| TYPE_EXPR tb->
						(match tb with
						| ENUM l->l
						|_-> failwith "get_list_poss_from_enum_expr : expr is not an enum")) in
				add_param (n,t); value)
			|_->failwith "get_list_poss_from_enum_expr : expr is not an enum") in
		match e with
		| REF id -> temp id
		|_ -> failwith "get_list_poss_from_enum_expr : expr is not an enum"

	and is_enum_poss e =	(* check if the expression is an ENUM_POSS *)
		match e with
		| REF s->
			(match (get_symbol s) with
			| ENUM_POSS _->true
			| _ ->false)
		| ELINE(_, _, e) -> is_enum_poss e
		| _ -> false

	(* Return a couple composed of the enum that the expression refer to and of the value of the expression *)
	and get_enum_poss_info e =
		match e with
		| REF s->
			(match (get_symbol s) with
				| ENUM_POSS (_,r,t,_)->(r,t)
				| _ -> failwith ("get_enum : expression is not an enum poss"))
		| ELINE (_, _, e) -> get_enum_poss_info e
		| _ ->failwith "get_enum : expression is not an enum poss" in

	(* Return the enum that the expression refer to *)
	let rec get_enum_poss_type e =
		get_type_ident (fst (get_enum_poss_info e))

	(* Get the id of the enum_poss refered by e*)
	and get_enum_poss_id e=
		match e with
		| REF s->
			(match (get_symbol s) with
			| ENUM_POSS (_,_,_,_)->s
			|_->failwith ("get_enum_poss_id : expression is not an enum poss"))
		| ELINE (_, _, e) -> get_enum_poss_id e
		| _ -> failwith "get_enum_poss_id : expression is not an enum poss" in

	(* --- end of definition of the "little" subfunction.
			Now we can start the declaration of the three "big" subfonctions who will each check one condition to validate the switch ---*)


	(* This part check if all the cases of a switch are of the type of the expression to be tested*)
	let check_switch_cases =
		let t = get_type_expr test in
		(*!!DEBUG!!*)
		(*print_string "**check_switch_cases, test:";Irg.print_type_expr t; print_string "\n";*)
		let rec sub_fun list_c =
			match list_c with
			| [] -> true
			| (c,_)::l->
				if(is_enum_poss c)
				then (get_enum_poss_type c = t) && (sub_fun l)
				else (get_type_expr c = t) && (sub_fun l) in
		let rec is_int lst =
			match lst with
			| [] -> true
			| (hd, _)::tl ->
				(match get_type_expr hd with
				| BOOL | CARD _ | INT _ | RANGE _ -> is_int tl
				| _ -> false) in
		match t with
		| ENUM _ -> sub_fun list_case
		(* UNKNOW_TYPE corresponds to an expr like x.item not yet instantiated *)
		| BOOL | CARD _ | INT _ | RANGE _ | UNKNOW_TYPE -> is_int list_case
		| _ -> (*!!DEBUG!!*)(*print_string "check_switch, t_=";Irg.print_type_expr t;*) false

	(* This part check if all the possible result of a switch expression are of the same type *)
	and check_switch_return_type =
		let type_default = get_type_expr default in
		let rec sub_fun list_c t=
			match list_c with
			| [] -> true
			| (_,e)::l -> 
			(*!!DEBUG!!*)
			(*print_string "**check_switch_return_type, case:";Irg.print_type_expr (get_type_expr e); print_string "\n";*)
			(get_type_expr e)=t  && sub_fun l  t in
		(*!!DEBUG!!*)
		(*print_string "**check_switch_return_type, default:";Irg.print_type_expr type_default; print_string "\n";*)
		if type_default = NO_TYPE
		then sub_fun list_case (get_type_expr (snd (List.hd list_case)))
		else sub_fun list_case type_default

	(* This part check if all the possibles values of the expression to test are covered *)
	and check_switch_all_possibilities =
		(* a default is needed to be sure that all possibilities are covered, except for ENUM where you can enumerate all the possibilities*)
		if (not (default = NONE)) then true
		else if is_param_of_type_enum test  then
			(* l is the id list of the enum type used *)
			let l = get_list_poss_from_enum_expr test	in
			(* cond_list is the list of id of the enum type who are presents in the swith *)
			let cond_list = List.map get_enum_poss_id (List.map fst list_case) in
			(* check that all element of l are contained in cond_list *)
			List.for_all (fun e->List.exists (fun a->a=e) cond_list) l
		else
			let min, max = interval_of (get_type_expr test) in
			if (min, max) = (0, 0) then
				raise (SemError ("bad type for 'switch' test: only integer types supported"))
			else
				let vals = List.sort compare
					(List.map (fun (case, _) -> to_int (eval_const case)) list_case) in
				let vals = List.map
					(fun v ->
						if v >= min || v <= max then v else
						raise (SemError (sprintf "%d out of switch bounds" v)))
					vals in
				let rec test i l =
					if i > max then true
					else if l = [] || i <> (List.hd l) then
						raise (SemError (sprintf "uncomplete switch: %d is lacking" i))
					else
						test (i + 1) (List.tl l) in
				test min vals in

	(* --- And finally we apply all these three subfunctions to check the switch --- *)
	if not check_switch_cases then
		raise (SemError "the cases of a functional switch must be consistent with the expression to test")
	else if not check_switch_return_type then
		raise (SemError "the return values of a functional switch must be of the sames type")
	else if not check_switch_all_possibilities then
		raise (SemError "the cases of a functional switch must cover all possibilities or contain a default")
	else if (get_type_expr default != NO_TYPE)
		then get_type_expr default
		else get_type_expr (snd (List.hd list_case))



(** Check is the given id refer to a valid memory location
	To allow compatibility with older versions, is_loc_mode and is_loc_spe must be used in conjunction with this function
	@param id	the id to check
	@return True if the id is a valid memory location, false otherwise *)
let rec is_location id=
	let sym =
		try Irg.get_symbol id
		with Irg.Symbol_not_found _ -> raise (SemError (Printf.sprintf "unknown symbol: %s" id))
	and is_location_param id=
		let sym=Irg.get_symbol id
		in
		match sym with
			 (MEM (_,_,_,_)|REG (_,_,_,_)|VAR(_,_,_))->true
			|_->false
	in
	(**)
	(*print_spec sym;*)
	(**)
	match sym with
	 (MEM (_,_,_,_)|REG (_,_,_,_)|VAR(_,_,_))->true
	|PARAM (n,t)->	(rm_symbol n;
			let value=(match t with
			 TYPE_ID idb-> is_location_param idb
			|TYPE_EXPR _->false)
			in
			add_param (n,t);value)
	|_->false

(** Check is the given id refer to a MODE.
	This is needed for compatibility with some versions of GLISS v1 where assignements in modes where used.
	This function is defined to be used in complement of is_location
	@param id	the id to check
	@return True if the id refer to a MODE false otherwise
*)
let rec is_loc_mode id =
	let sym=Irg.get_symbol id
	and is_location_param id=
		let sym=Irg.get_symbol id
		in
		match sym with
			(AND_MODE(_,_,_,_)|OR_MODE(_,_))->true
			|_->false
	in
	match sym with
	PARAM (n,t)->	(rm_symbol n;
			let value=(match t with
			 TYPE_ID idb-> is_location_param idb
			| TYPE_EXPR _->false
			)
			in
			add_param (n,t);value)
	|_->false

(** Check is the given id refer to a parameter.
	This is needed for compatibility with some versions of GLISS v1 where assignements to parameter (namely in the predecode attribute) was allowed
	This function is defined to be used in complement of is_location
	@param id	the id to check
	@return True if the id refer to a parameter false otherwise
*)
let rec is_loc_spe id=
	let sym=Irg.get_symbol id
	and is_location_param id=
		let sym=Irg.get_symbol id
		in
		match sym with
			 TYPE _->true
			|_->false
	in
	match sym with
	 PARAM (n,t)->(rm_symbol n;
			let value=(match t with
			 TYPE_ID idb-> is_location_param idb
			| TYPE_EXPR _->true
			)
			in
			add_param (n,t);value)
	|_->false


(** Check if the given location is a parameter.
	This possibility is not allowed in the nML standard. But it was with some versions of GLISS v1 (in the predecode attribute).
	The locations which verify this condition are the ones allowed in is_loc_spe.
	We keep it here for compatibility with previous versions only.
	@param id	the id to check
	@return True if the id refer to a parameter false otherwise
*)
let is_setspe loc=
	match loc with
	LOC_REF (_, id, Irg.NONE, _, _) -> (let symb= get_symbol id
			in
			match  symb with
				|PARAM _->true
				|_->false
		   )
	|_->false




(* this is the regular expression whitch represent a call to a parameter in a format *)
let reg_exp = Str.regexp "%[0-9]*[ldbxsfu%]"	
	(* 	The expression %0b was used with some versions to avoid a bug of Gliss v1 ,
		so we allow this kind of expression here for compatibility *)

(** this function is used to find all references to parameters in a string passed to a format
	@param str	The string to treat
	@return 	A list of string matching reg_exp
 *)
let get_all_ref str =
	let str_list = Str.full_split reg_exp str in
	let rec temp str_l res_l=
		match str_l with
		| [] -> res_l
		| (Str.Text _)::l -> temp l res_l
		| (Str.Delim s)::l when s = "%%" -> temp l res_l
		| (Str.Delim s)::l -> temp l (s::res_l) in
	temp str_list []


(** Create a FORMAT operation and check if it is well written
	@param str	The string to print
	@param exp_list	The list of parameters to be used as variables in str
	@return A FORMAT expression
	@raise SemError 	Raised when the parameters are incorrect
*)
let build_format str exp_list=

	let ref_list = get_all_ref str in

	if (not (List.length ref_list = List.length exp_list)) || List.length exp_list = 0	(* it is not allowed to use format for printing a string without at least one variable *)
		then
			raise (SemError (Printf.sprintf "incorrect number of parameters in format"))
		else
			let test_list = List.map2 (fun e_s e_i ->	(* here we check if all variables are given a parameter of the good type *)
					if (get_type_expr e_i=UNKNOW_TYPE) then true else
					match Str.last_chars e_s 1 with
					| "d" -> (match (get_type_expr e_i) with (CARD _|INT _)->true | _ -> false)
					| "u" -> (match (get_type_expr e_i) with (CARD _|INT _) -> true | _ -> false)
					| "b" -> true
					| "x" -> (match (get_type_expr e_i) with (CARD _|INT _)->true | _ -> false)
					| "s" -> true
					| "f" -> (match (get_type_expr e_i) with (FLOAT _) -> true | _ -> false)
					| "l" -> (match (get_type_expr e_i) with INT _ | CARD _ -> true | _ -> false)
					| _ -> failwith "internal error : build_format"
				) ref_list exp_list
			in
			if not (List.for_all (fun e -> e) test_list)
			then raise (SemError (Printf.sprintf "incorrect type in this format "))
			else FORMAT (str, (List.rev exp_list))

(*

let rec get_length id =
	let sym=get_symbol id
	in
	match sym with
	UNDEF->failwith "get_length : undefined symbol" (* a changer *)
	|LET (_,const)->(match const with
				 NULL->failwith "get_length : id refer to a NULL const" (* a changer *)
				|CARD_CONST _->32	(* a changer *)
				|CARD_CONST_64 _->64
				|STRING_CONST _->let dsp= fun _->(Printf.printf "the id %s refer to a STRING const\n" id;())
						 in
						 raise (SemErrorWithFun ("",dsp))
				|FIXED_CONST _ ->32	(* a changer *)
			)
	|MEM (_,i,_,_)->i
	|REG (_,i,_,_)->i
	|VAR (_,i,_)->i
	|PARAM (s,_)->get_length s
	|_->let dsp =fun _->(Printf.printf "the id %s do no refer to a location\n" id;())
	    in
	    raise (SemErrorWithFun ("",dsp))
*)



(** This function check if the paramters of a canonical function are correct
	@param name	The name of the canonical function
	@param list_param	The list of parameters given to the canonical function
	@return 			Fixed list of parameters (possibly with casting).
*)
let check_params name list_param =

	let check_type p t =
		let pt = get_type_expr p in
		if t = pt then p else Irg.COERCE(t, p) in

	let canon = get_canon name in
	if canon.name = UNKNOW then list_param else
	try
		List.map2 check_type list_param canon.type_param
	with Invalid_argument _ ->
		raise (SemError "bad number of arguments")


(** This function build a canonical expression, after checked if the parameters are corrects.
	@param name	The name of the canonical function
	@param list_param	The list of parameters given to the canonical function
	@return a CANON_EXPR expression
	@raise SemError 	Raised when the parameters are incorrects
*)
let build_canonical_expr name param =
	let e = get_canon name in
	CANON_EXPR (e.type_res, name , check_params name param)


(** This function build a canonical statement, after checked if the parameters are corrects.
	If the function have a return type different of NO_TYPE (except of course UNKNOW_TYPE, when the function is unknow), then a warning is displayed

	@param name	The name of the canonical function
	@param list_param	The list of parameters given to the canonical function
	@return a CANON_STAT expression
	@raise SemError 	Raised when the parameters are incorrects
*)
let build_canonical_stat name param =
	let e = get_canon name in
	if not (e.type_res = NO_TYPE || e.type_res = UNKNOW_TYPE) then
		Lexer.display_warning (Printf.sprintf "the result of the canonical function %s is not used" name);
	CANON_STAT (name , check_params name param)


(** Get type of a location.
	@param loc	Location to get type of.
	@return		Type of the location. *)
let get_loc_type loc =
	match loc with
	| LOC_NONE -> NO_TYPE
	| LOC_REF (FLOAT (n, m), _, _, l, _) when l <> NONE -> CARD(n + m)
	| LOC_REF (t, _, _, _, _) -> t
	| LOC_CONCAT (t, _, _) -> t


(** Get the type of location reference.
	@param name			Location reference name.
	@return				Type of the matching location.
	@raise SemError		Raised when the reference does not exist,
						or is not a location. *)
let get_loc_ref_type name =
	match Irg.get_symbol name with
	| Irg.UNDEF -> raise (SemError (name ^ " is undefined"))
	| Irg.MEM (_, _, t, _) -> t
	| Irg.REG (_, _, t, _) -> t
	| Irg.VAR (_, _, t) -> t
	| Irg.PARAM _ -> Irg.UNKNOW_TYPE
	| _ -> raise (SemError (name ^ " is not a location"))


(* list of undefined canonical type *)
let undef_canons: string list ref = ref []


(** Test if a canonical function is defined.
	Display an error if it not defined.
	@param name	Name of the canonical function. *)
let test_canonical name =
	if not (Irg.is_defined_canon name)
	&& not (List.mem name !undef_canons)
	then
		begin
			undef_canons := name :: !undef_canons;
			Lexer.display_warning
				(Printf.sprintf "the canonical function %s is not defined" name)
		end


(** Test if the symbol exists and is a data symbol
	(constant, memory, variable, register, parameter)
	@param name		Name of the symbol.
	@param indexed	Test if the data is indexed.
	@raise			SemError if either the symbol does not exists,
					or it is not data. *)
let test_data name indexed =
	let v = Irg.get_symbol name in
	match v with
	| Irg.UNDEF -> raise (SemError (Printf.sprintf "the identifier \"%s\" is undefined" name))

	(* never indexed *)
	| Irg.LET _
	| Irg.PARAM _
	| Irg.ATTR _
	| Irg.ENUM_POSS _ ->
		if indexed
		then raise (SemError (Printf.sprintf "data \"%s\" can not be indexed" name))
		else ()

	(* may be indexed *)
	| Irg.MEM _				(* for compatibility with GLISS v1 *)
	| Irg.REG _
	| Irg.VAR _ -> ()
	| _ -> raise (SemError (Printf.sprintf "the idenfifier \"%s\" does not design a data" name))


(**	used to return the expression associated to an expr attr, UNDEF if no expression extractable
	@param	name	Name of the attribute, it is supposed to have been stacked before
	@return		if "name" is an expression attribute, return its expression,
			if not, UNDEF is returned
*)
let get_data_expr_attr name =
	match Irg.get_symbol name with
	| Irg.ATTR a ->
		(match a with
		| Irg.ATTR_EXPR(_, e) -> e
		| _ -> NONE)
	| _ -> NONE


(** Build a set expression and check the types (possibly introducing
	casts).
	@param loc		Assigned location.
	@param expr		Assigned expression.
	@return			Built statement. *)
let make_set loc expr =
	let ltype = get_loc_type loc in
	let etype = get_type_expr expr in
	if ltype = etype then Irg.SET (loc, expr) else
	match ltype, etype with

	(* HKC-SET *)
	| _, NO_TYPE
	| NO_TYPE, _ -> SET (loc, expr)
	(* /HKC-SET *)

	| BOOL, INT _
	| BOOL, CARD _

	| INT _, BOOL
	| INT _, INT _
	| INT _, CARD _

	| CARD _, BOOL
	| CARD _, INT _
	| CARD _, CARD _

	| FLOAT _, FLOAT _ ->
		Irg.SET (loc, Irg.CAST(ltype, expr))

	| FLOAT _, CARD _
	| FLOAT _, INT _
	| INT _, FLOAT _
	| CARD _, FLOAT _ ->
		Lexer.display_warning
			("assigning float/int to int/float is ambiguous.\n"
			^ "\tAs default, resolved to bit to bit assignment.\n"
			^ "\tUse instead either field notation for bit to bit assignment\n"
			^ "\tor explicit coerce for value conversion.");
			let res = Irg.SET (loc, Irg.CAST(ltype, expr)) in
			Irg.output_statement stderr res;
			output_char stderr '\n';
			res

	| CARD _, UNKNOW_TYPE
	| INT _, UNKNOW_TYPE ->
		(* !!DEBUG!! Lexer.display_warning "unknown in right of set";*)
		Irg.SET (loc, Irg.CAST(ltype, expr))

	| _ ->
		Irg.print_location loc;
		print_string " = ";
		Irg.print_expr expr;
		print_char '\n';
		raise (SemErrorWithFun ("unsuppored assignment",
			fun _ ->
				print_string "LHS type:";
				Irg.print_type_expr ltype;
				print_string "\nRHS type:";
				Irg.print_type_expr etype;
				print_char '\n'
			))


(** This function is used to modify parameters of a format called into a specific attribute
	to add the .attribute at all reference parameters (resolving to %s).
	@param a		Attribute name.
	@param e		Expression to transform.
	@return			Transformed expression. *)
let change_string_dependences a e =

	let rec process e = match e with
		| REF name-> FIELDOF (STRING, name, a)
		| ELINE (f, l, e) -> ELINE (f, l, process e)
		| _ -> e in

	let rec temp r_l e_l =
		match r_l with
		| []->[]
		| r::l -> if (Str.last_chars r 1 = "s")
				then (process (List.hd e_l))::(temp l (List.tl e_l))
				else (List.hd e_l)::(temp l (List.tl e_l)) in
	
	let rec look e =
		match e with
		| ELINE (f, l, e) -> ELINE (f, l, look e)
		| FORMAT (f, args) ->
			let r_list = List.rev (get_all_ref f) in
			FORMAT (f, temp r_list args)
		| IF_EXPR (tp, c, t, e) ->
			IF_EXPR (tp, c, look t, look e)
		| SWITCH_EXPR (tp, c, cs, d) ->
			SWITCH_EXPR (tp, c, List.map (fun (c, a) -> (c, look a)) cs, look d)
		| _ -> e in
	
	look e


(** Get an attribute and evaluates to integer.
	@param id		Attribute ID.
	@param attrs	Attribute list.
	@param def		Default value if attribute is not defined.
	@return			Integer value of the attribute, false else.
	@raise IrgError	If attribute exists but is not of good type.
	@raise SemError	If the attribute is a not a constant integer. *)
let attr_int id attrs def =
	let e = Irg.attr_expr id attrs Irg.NONE in
	if e = Irg.NONE then def else
	let cst = eval_const e in
	to_int cst


(** Add a specification to the IRG representation.
	@param name			Name of the specification.
	@param spec			Specification to add.
	@raise Irg.Error	If the symbol already exists. *)
let add_spec name spec =
	if Irg.is_defined name
	then raise (Irg.Error (fun out -> Printf.fprintf out "%s: symbol %s already defined at %s" (Lexer.current_loc ()) name (pos_of name)))
	else Irg.add_symbol name spec

