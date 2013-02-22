(*
 * $Id: irg.ml,v 1.39 2009/10/21 14:40:50 barre Exp $
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

exception Error of (out_channel -> unit)


(** Deprecated *)
exception RedefinedSymbol of string

(** Deprecated *)
exception IrgError of string


(** Raise the error exception.
	@param f	Function to display error. *)
let error_with_fun f = raise (Error f)


(** Raise an error exception with the given message.
	@param msg	Message to display. *)
let error msg = raise (Error (fun out -> output_string out msg))


(** May be set to true to dump line information during expression/statement
	output. *)
let dump_lines = ref false
let dump_type = ref true

(** Type expression *)
type type_expr =
	  NO_TYPE
	| BOOL
	| INT of int
	| CARD of int
	| FIX of int * int
	| FLOAT of int * int
	| RANGE of int32 * int32
	| STRING
	| ENUM of string list
	| UNKNOW_TYPE		(* Used for OR_MODE only. The evaluation is done in a dynamic way *)

(** Use of a type *)
type typ =
	  TYPE_ID of string
	| TYPE_EXPR of type_expr


(* Expressions *)
type unop =
	  NOT
	| BIN_NOT
	| NEG

type binop =
	  ADD
	| SUB
	| MUL
	| DIV
	| MOD
	| EXP
	| LSHIFT
	| RSHIFT
	| LROTATE
	| RROTATE
	| LT
	| GT
	| LE
	| GE
	| EQ
	| NE
	| AND
	| OR
	| BIN_AND
	| BIN_OR
	| BIN_XOR
	| CONCAT

type const =
	  NULL
	| CARD_CONST of Int32.t
	| CARD_CONST_64 of Int64.t
	| STRING_CONST of string * bool	* type_expr	(** bool indicates if const is canonical, if bool is true then 3rd field is the type of canonical const *)
	| FIXED_CONST of float

type expr =
	  NONE									(* null expression *)
	| COERCE of type_expr * expr						(* explicit coercition *)
	| FORMAT of string * expr list						(* format expression *)
	| CANON_EXPR of type_expr * string * expr list				(* canonical expression *)
	| REF of string								(* attribute / state item access *)
	| FIELDOF of type_expr * string * string				(* attribute access *)
	| ITEMOF of type_expr * string * expr					(* state item array access *)
	| BITFIELD of type_expr * expr * expr * expr				(* bit field access *)
	| UNOP of type_expr * unop * expr					(* unary operation (negation, not, etc) *)
	| BINOP of type_expr * binop * expr * expr				(* binary operation (arithmetic, logic, shift, etc) *)
	| IF_EXPR of type_expr * expr * expr * expr				(* if expression *)
	| SWITCH_EXPR of type_expr * expr * (expr * expr) list * expr		(* switch expression *)
	| CONST of type_expr * const						(* constant value *)
	| ELINE of string * int * expr						(* source/line information (file, line, expression) *)
	| EINLINE of string							(** inline source in expression (for internal use only) *)
	| CAST of type_expr * expr						(** binary cast (target type, expression *)

(** Statements *)
type location =
	| LOC_NONE												(** null location *)
	| LOC_REF of type_expr * string * expr * expr * expr	(** (type, memory name, index, lower bit, upper bit) *)
	| LOC_CONCAT of type_expr * location * location		(** concatenation of locations *)


(** argument of attributes *)
type attr_arg =
	| ATTR_ID of string * attr_arg list
	| ATTR_VAL of const


(** A statement in an action. *)
type stat =
	  NOP
	| SEQ of stat * stat
	| EVAL of string
	| EVALIND of string * string
	| SET of location * expr
	| CANON_STAT of string * expr list
	| ERROR of string	(* a changer : stderr ? *)
	| IF_STAT of expr * stat * stat
	| SWITCH_STAT of expr * (expr * stat) list * stat
	| SETSPE of location * expr		(** Used for allowing assigment of parameters (for exemple in predecode attribute).
					   				This is NOT in the nML standard and is only present for compatibility *)
	| LINE of string * int * stat	(** Used to memorise the position of a statement *)
	| INLINE of string				(** inline source in statement (for internal use only) *)


(** attribute specifications *)
type attr =
	  ATTR_EXPR of string * expr
	| ATTR_STAT of string * stat
	| ATTR_USES
	| ATTR_LOC of string * location

(* 2 kinds of canonicals, functions and constants *)
type canon_type =
	| CANON_FUNC
	| CANON_CNST


(** Specification of an item *)
type spec =
	  UNDEF
	| LET of string * const (* typed with construction *)
	| TYPE of string * type_expr
	| MEM of string * int * type_expr * attr list
	| REG of string * int * type_expr * attr list
	| VAR of string * int * type_expr
	| AND_MODE of string * (string * typ) list * expr * attr list
	| OR_MODE of string * string list
	| AND_OP of string * (string * typ) list * attr list
	| OR_OP of string * string list
	| RES of string
	| EXN of string
	| PARAM of string * typ
	| ATTR of attr
	| ENUM_POSS of string*string*Int32.t*bool	(*	Fields of ENUM_POSS :
								the first parameter is the symbol of the enum_poss,
								the second is the symbol of the ENUM where this ENUM_POSS is defined (must be completed - cf function "complete_incomplete_enum_poss"),
								the third is the value of this ENUM_POSS,
								the fourth is a flag to know if this ENUM_POSS is completed already (cf function "complete_incomplete_enum_poss")	*)
	| CANON_DEF of string * canon_type * type_expr * type_expr list	(** declaration of a canonical: name of canonical, type (fun or const name), return type, args type *)

(** Get the name from a specification.
	@param spec		Specification to get name of.
	@return			Name of the specification. *)
let name_of spec =
	match spec with
	  UNDEF -> "<undef>"
	| LET (name, _) -> name
	| TYPE (name, _) -> name
	| MEM (name, _, _, _) -> name
	| REG (name, _, _, _) -> name
	| VAR (name, _, _) -> name
	| AND_MODE (name, _, _, _) -> name
	| OR_MODE (name, _) -> name
	| AND_OP (name, _, _) -> name
	| OR_OP (name, _) -> name
	| RES (name) -> name
	| EXN (name) -> name
	| PARAM (name, _) -> name
	| ENUM_POSS (name, _, _, _) -> name
	| ATTR(a) ->
		(match a with
		| ATTR_EXPR(name, _) -> name
		| ATTR_STAT(name, _) -> name
		| ATTR_USES -> "<ATTR_USES>"
		| ATTR_LOC(name, _) -> name)
	| CANON_DEF(name, _, _, _) -> name


(* Symbol table *)
module HashString =
struct
	type t = string
	let equal (s1 : t) (s2 : t) = s1 = s2
	let hash (s : t) = Hashtbl.hash s
end
module StringHashtbl = Hashtbl.Make(HashString)


(* Position *)

type pos_type = {ident:string; file : string; line : int}

(* This table is used to record the positions of the declaration of all symbols *)
let pos_table : pos_type StringHashtbl.t = StringHashtbl.create 211

(** Add a symbol to the localisation table.
	@param v_name	Name of the symbol to add.
	@param v_file	Name of the file where the symbol is declared
	@param v_line	Approximate line number of the declaration.
*)
let add_pos v_name v_file v_line =
	StringHashtbl.add pos_table v_name {ident=v_name;file=v_file;line=v_line}


(** Return string identifying file and line definition of the given symbol.
	@param sym	Required symbol.
	@return		File and line information about the symbol. *)
let pos_of sym =
	try
		let p = StringHashtbl.find pos_table sym in
		Printf.sprintf "%s:%d" p.file p.line
	with Not_found ->
		"<no line>"


(** table of symbols of the current loaded NMP or IRG file. *)
let syms : spec StringHashtbl.t = StringHashtbl.create 211
let _ =
	StringHashtbl.add syms "__IADDR" (PARAM ("__IADDR", TYPE_EXPR (CARD(32))));
	StringHashtbl.add syms "__ISIZE" (PARAM ("__ISIZE", TYPE_EXPR (CARD(32))))


exception Symbol_not_found of string

(** Get the symbol matching the given name or UNDEF if not found.
	@param n	Symbol to look for.
	@return		Symbol found or Irg.UNDEF (if not found). *)
let get_symbol n =
	try
		StringHashtbl.find syms n
	with Not_found ->
		(*if n = "bit_order" then*)
			UNDEF
		(*else*)
		(* !!DEBUG!! *)
		(*failwith ("ERROR: irg.ml::get_symbol, " ^ n ^ " not found, probably not defined in nmp sources, please check include files.")*)
		(*raise (Symbol_not_found(n))*)

(** Get processor name of the simulator *)
let get_proc_name () = match get_symbol "proc" with
	| LET(_, STRING_CONST(name, _, _)) -> name
	| _                         ->
		failwith ("Unable to find 'proc_name'."^
				  "'proc' must be defined as a string let")


(** Add a symbol to the namespace.
	@param name	Name of the symbol to add.
	@param sym	Symbol to add.
	@raise RedefinedSymbol	If the symbol is already defined. *)
let add_symbol name sym =



	(*
	transform an old style subpart alias declaration (let ax [1, card(16)] alias eax[16])
	into a new style one with bitfield notation (let ax [1, card(16)] alias eax<16..0>)
	before inserting the definition in the table
	*)
	let translate_old_style_aliases s =
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
			| LET(_, STRING_CONST(id, _, _)) ->
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
				ATTR_LOC("alias", l) ->
					(match l with
					LOC_REF(typ, name, i, l, u) ->
						if is_array name then
							a::(change_alias_attr b n)
						else
							if l=NONE && u=NONE then
								if b_o then
									(ATTR_LOC("alias", LOC_REF(typ, name, NONE, i, sub i (sub (const n) (const 1)) (*i-(n-1)*))))::(change_alias_attr b n)
								else
									(ATTR_LOC("alias", LOC_REF(typ, name, NONE, sub i (sub (const n) (const 1)) (*i-(n-1)*), i)))::(change_alias_attr b n)
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
	in

	(*if StringHashtbl.mem syms name
	then raise (Error (fun out -> Printf.fprintf out "ERROR: %s: symbol %s already defined at %s" (Lexer.current_loc ()) name (pos_of name)))
	else*) StringHashtbl.add syms name (translate_old_style_aliases sym)


(**	Check if a given name is defined in the namespace
		@param name	The name to check *)
let is_defined name = StringHashtbl.mem syms name

(**	Add a parameter in the namespace.
		This function don't raise RedefinedSymbol if the name already exits.
		It is used to temporary overwrite existing symbols with the same name than a parameter

		@param name	Name of the parameter to add.
		@param t	Type of the parameter to add.	*)
let add_param (name,t) =
	StringHashtbl.add syms name (PARAM (name,t))

(**	Remove a symbol from the namespace.
		@param name	The name of the symbol to remove. *)
let rm_symbol name=StringHashtbl.remove syms name

(**	Add a list of parameters to the namespace.
		@param l	The list of parameters to add.	*)
let param_stack l= List.iter add_param l

(**	Remove a list of parameters from the namespace.
		@param l	The list of parameters to remove.	*)
let param_unstack l= List.iter (StringHashtbl.remove syms) (List.map fst l)


(**	Add a attribute in the namespace.
		This function don't raise RedefinedSymbol if the name already exits.
		It is used to temporary overwrite existing symbols with the same name than an attribute
		@param name	name of the attribute
		@param attr	attribute to add.	*)
let add_attr attr =
	StringHashtbl.add syms (name_of (ATTR(attr))) (ATTR(attr))

(**	Add a list of attributes to the namespace.
		@param l	The list of attributes to add.	*)
let attr_stack l= List.iter add_attr l

(**	Remove a list of attributes from the namespace.
		@param l	The list of attributes to remove.	*)
let attr_unstack l= List.iter (StringHashtbl.remove syms) (List.map (fun x -> name_of (ATTR(x))) l)

(**	This function is used to make the link between an ENUM_POSS and the corresponding ENUM.
		It must be used because when the parser encounter an ENUM_POSS, it doesn't have reduce	(* a changer : stderr ? *)d the ENUM already.
		The ENUM can be reduced only when all the ENUM_POSS have been.
		So when reduced, the ENUM_POSS have an boolean attribute "completed" set at false and their enum attribute is empty.
		When the ENUM is reduced, we fill the enum attribute to make the link, and set the "completed" to true

		@param id	The id of the enum
*)
let complete_incomplete_enum_poss id =
	StringHashtbl.fold (fun e v d-> match v with
				ENUM_POSS (n,_,t,false)-> StringHashtbl.replace syms e (ENUM_POSS (n,id,t,true))
				|_->d
			) syms ()



(* --- canonical functions --- *)

type canon_name_type=
	 UNKNOW		(* this is used for functions not defined in the cannon_list *)
	|NAMED of string

(* canonical function table *)
module HashCanon =
struct
	type t = canon_name_type
	let equal (s1 : t) (s2 : t) = match (s1,s2) with
					(UNKNOW,UNKNOW)->true
					|(NAMED a,NAMED b) when a=b->true
					|_->false
	let hash (s : t) = Hashtbl.hash s
end
module CanonHashtbl = Hashtbl.Make(HashCanon)

type canon_fun={name : canon_name_type; type_fun : canon_type ; type_param : type_expr list ; type_res:type_expr}

(* the canonical functions space *)
let canon_table : canon_fun CanonHashtbl.t = CanonHashtbl.create 211

(* list of all defined canonical functions *)
let canon_list = [
			(* this is the "default" canonical function, used for unknown functions *)
			{ name=UNKNOW; type_fun=CANON_FUNC;type_param=[];type_res=UNKNOW_TYPE };
			{ name=NAMED "print";type_fun=CANON_FUNC;type_param=[STRING];type_res=NO_TYPE };
			
			(* for debugging generation *)
			{name=NAMED "GLISS_IDX"; type_fun=CANON_CNST; type_param=[]; type_res=CARD(32); };
			{name=NAMED "GLISS_I"; type_fun=CANON_CNST; type_param=[]; type_res=CARD(32); };
			{name=NAMED "GLISS_L"; type_fun=CANON_CNST; type_param=[]; type_res=CARD(64); };
			{name=NAMED "GLISS_F"; type_fun=CANON_CNST; type_param=[]; type_res=FLOAT(23, 9); };
			{name=NAMED "GLISS_D"; type_fun=CANON_CNST; type_param=[]; type_res=FLOAT(52, 12); };
			{name=NAMED "GLISS_GET_I"; type_fun=CANON_FUNC; type_param=[CARD(32)]; type_res=NO_TYPE; };			
			{name=NAMED "GLISS_GET_L"; type_fun=CANON_FUNC; type_param=[CARD(65)]; type_res=NO_TYPE; };			
			{name=NAMED "GLISS_GET_F"; type_fun=CANON_FUNC; type_param=[FLOAT(23, 9)]; type_res=NO_TYPE; };			
			{name=NAMED "GLISS_GET_D"; type_fun=CANON_FUNC; type_param=[FLOAT(52, 12)]; type_res=NO_TYPE; };			
		 ]

(* we add all the defined canonical functions to the canonical functions space *)
let _ =	List.iter (fun e->CanonHashtbl.add canon_table e.name e) canon_list

(** Check if a canonical function is defined
	@param name	The name of the function to check *)
let is_defined_canon name = CanonHashtbl.mem canon_table (NAMED name)

(** Get a canonincal function
	@param name	The name of the canonical function to get
	@return A canon_fun element, it can be the function with the attribute name UNKNOW if the name given is not defined *)
let rec get_canon name=
	try
		CanonHashtbl.find canon_table (NAMED name)
	with Not_found -> CanonHashtbl.find canon_table UNKNOW

(** Add a canonical definition to the namespace.
	@param sym	Canonical specification (Irg.CANON_DEF(...)).
	@param fun_name	Canonical name.
	@raise RedefinedSymbol	If the symbol is already defined. *)
let add_canon fun_name sym =
	let canon_def_sym =
		match sym with
		| CANON_DEF(n, typ, res, prms) -> {name = NAMED n; type_fun = typ; type_param = prms; type_res = res}
		| _ -> failwith "irg.ml::add_canon: shouldn't happen!\n"
	in
	let name = canon_def_sym.name
	in
	if CanonHashtbl.mem canon_table name
	(* symbol already exists *)
	then raise (RedefinedSymbol fun_name)
	(* add the symbol to the hashtable *)
	else CanonHashtbl.add canon_table name canon_def_sym

(* --- end canonical functions --- *)


(* --- display functions --- *)

(** Used to print a position
	Debug only
	@param e	Element of which we want to display the position
*)
let print_pos e=
	(Printf.fprintf stdout "%s->%s:%d\n" e.ident e.file e.line)


(** Output a constant.
	@param out	Channel to output to.
	@param cst	Constant to output. *)
let output_const out cst =
	match cst with
	  NULL ->
	    output_string out "<null>"
	| CARD_CONST v ->
		output_string out (Int32.to_string v);
		output_string out "L"
	| CARD_CONST_64 v->
		output_string out (Int64.to_string v);
		output_string out "LL"
	| STRING_CONST(v, _, _) ->
		Printf.fprintf out "\"%s\"" v
	| FIXED_CONST v ->
		Printf.fprintf out "%f" v

(** Print a constant.
	@param cst	Constant to display. *)
let print_const cst = output_const stdout cst


(** Print a type expression.
	@param out	Channel to output to.
	@param t	Type expression to display. *)
let output_type_expr out t =
	match t with
	  NO_TYPE ->
		output_string out "<no type>"
	| BOOL ->
		output_string out "bool"
	| INT s ->
		Printf.fprintf out "int(%d)" s
	| CARD s ->
		Printf.fprintf out "card(%d)" s
	| FIX(s, f) ->
		Printf.fprintf out "fix(%d, %d)" s f
	| FLOAT(s, f) ->
		Printf.fprintf out "float(%d, %d)" s f
	| RANGE(l, u) ->
		Printf.fprintf out "[%s..%s]" (Int32.to_string l) (Int32.to_string u)
	| STRING ->
		output_string out "string"
	| ENUM l->
		output_string out "enum (";
		Printf.fprintf out "%s" (List.hd (List.rev l));
		List.iter (fun i->(Printf.fprintf out ",%s" i)) (List.tl (List.rev l));
		output_string out ")"
	| UNKNOW_TYPE -> output_string out "unknow_type"


(** Print a type expression.
	@param t	Type expression to display. *)
let print_type_expr t = output_type_expr stdout t


(** Print the unary operator.
	@param op	Operator to print. *)
let string_of_unop op =
	match op with
	  NOT		-> "!"
	| BIN_NOT	-> "~"
	| NEG		-> "-"

(** Print a binary operator.
	@param op	Operator to print. *)
let string_of_binop op =
	match op with
	  ADD		-> "+"
	| SUB		-> "-"
	| MUL		-> "*"
	| DIV		-> "/"
	| MOD		-> "%"
	| EXP		-> "**"
	| LSHIFT	-> "<<"
	| RSHIFT	-> ">>"
	| LROTATE	-> "<<<"
	| RROTATE	-> ">>>"
	| LT		-> "<"
	| GT		-> ">"
	| LE		-> "<="
	| GE		-> ">="
	| EQ		-> "=="
	| NE		-> "!="
	| AND		-> "&&"
	| OR		-> "||"
	| BIN_AND	-> "&"
	| BIN_OR	-> "|"
	| BIN_XOR	-> "^"
	| CONCAT	-> "::"


(** Print an expression.
	@param out	Channel to output to.
	@param expr	Expression to print. *)
let rec output_expr out e =

	let print_arg fst arg =
		if not fst then output_string out ", ";
		output_expr out arg;
		false in
	match e with
	  NONE ->
	  	output_string out "<none>"
	| COERCE (t, e) ->
		output_string out "coerce(";
		output_type_expr out t;
		output_string out ", ";
		output_expr out e;
		output_string out ")"
	| FORMAT (fmt, args) ->
		output_string out "format(\"";
		output_string out fmt;
		output_string out "\", ";
		let _ = List.fold_left print_arg true args in
		output_string out ")"
	| CANON_EXPR (_, n, args) ->
		output_string out "\"";
		output_string out n;
		output_string out "\" (";
		let _ = List.fold_left print_arg true args in
		output_string out ")"
	| FIELDOF(t, e, n) ->
		output_string out e;
		output_string out ".";
		output_string out n
	| REF id ->
		output_string out id
	| ITEMOF (t, name, idx) ->
		output_string out name;
		output_string out "[";
		output_expr out idx;
		output_string out "]";
	| BITFIELD (t, e, l, u) ->
		output_expr out e;
		output_string out "<";
		output_expr out l;
		output_string out "..";
		output_expr out u;
		output_string out ">";
		output_string out "(: ";
		output_type_expr out t;
		output_string out ")"
	| UNOP (_,op, e) ->
		output_string out (string_of_unop op);
		output_expr out e
	| BINOP (_,op, e1, e2) ->
		output_string out "(";
		output_expr out e1;
		output_string out ")";
		output_string out (string_of_binop op);
		output_string out "(";
		output_expr out e2;
		output_string out ")"
	| IF_EXPR (_,c, t, e) ->
		output_string out "if ";
		output_expr out c;
		output_string out " then ";
		output_expr out t;
		output_string out " else ";
		output_expr out e;
		output_string out " endif"
	| SWITCH_EXPR (_,c, cases, def) ->
		output_string out "switch(";
		output_expr out c;
		output_string out ")";
		output_string out "{ ";
		List.iter (fun (c, e) ->
				output_string out "case ";
				output_expr out c;
				output_string out ": ";
				output_expr out e;
				output_string out " "
			) (List.rev cases);
		output_string out "default: ";
		output_expr out def;
		output_string out " }"
	| ELINE (file, line, e) ->
		if !dump_lines then Printf.fprintf out "(%s:%d: " file line;
		output_expr out e;
		if !dump_lines then output_string out ")"
	| CONST (t, c) ->
		if !dump_type then
			(output_string out "const(";
			output_type_expr out t;
			output_string out ", ");
		output_const out c;
		if !dump_type then output_string out ")"
	| EINLINE s ->
		Printf.fprintf out "inline(%s)" s
	| CAST (typ, expr) ->
		output_string out "cast<";
		output_type_expr out typ;
		output_string out ">(";
		output_expr out expr;
		output_string out ")"


(** Print an expression.
	@param expr	Expression to print. *)
let rec print_expr e = output_expr stdout e


(** Print a location.
	@param out	Channel to output to.
	@param loc	Location to print. *)
let rec output_location out loc =
	match loc with
	| LOC_NONE ->
		output_string out "<none>"
	| LOC_REF (t, id, idx, lo, up) ->
	  	output_string out id;
		if idx <> NONE then
			begin
				output_string out "[";
				output_expr out idx;
				output_string out "]"
			end;
		if lo <> NONE then
			begin
				output_string out "<";
				output_expr out lo;
				output_string out "..";
				output_expr out up;
				output_string out ">"
			end;
		output_string out ": ";
		output_type_expr out t
	| LOC_CONCAT (_, l1, l2) ->
		output_location out l1;
		output_string out "::";
		output_location out l2


(** Print a location.
	@param loc	Location to print. *)
let rec print_location loc = output_location stdout loc


(** Print a statement
	@param out	Channel to output to.
	@param stat	Statement to print.*)
let rec output_statement out stat =
	match stat with
	  NOP ->
	  	output_string out "\t\t <NOP>;\n"
	| SEQ (stat1, stat2) ->
		output_statement out stat1;
		output_statement out stat2
	| EVAL ch ->
		Printf.fprintf out "\t\t%s;\n" ch
	| EVALIND (ch1, ch2) ->
		Printf.fprintf out "\t\t%s.%s;\n" ch1 ch2
	| SET (loc, exp) ->
		output_string out "\t\t";
		output_location out loc;
		output_string out "=";
		output_expr out exp;
		output_string out ";\n"
	| CANON_STAT (ch, expr_liste) ->
		Printf.fprintf out "\t\t\"%s\" (" ch;
		ignore (List.fold_left
			(fun f e ->
				if not f then output_string out ", ";
				output_expr out e; false)
			true
			expr_liste);
		output_string out ");\n"
	| ERROR ch ->
		Printf.fprintf out "\t\t error %s;\n" ch
	| IF_STAT (exp,statT,statE) ->
		output_string out "\t\t if ";
		output_expr out exp;
		output_string out "\n";
		output_string out "\t\t then \n";
		output_statement out statT;
		output_string out "\t\t else \n";
		output_statement out statE;
		output_string out "\t\t endif;\n"
	| SWITCH_STAT (exp,stat_liste,stat) ->
		output_string out "\t\t switch (";
		output_expr out exp;
		output_string out ") {\n";
		List.iter (fun (v,s)->
			output_string out "\t\t\t case";
			output_expr out v;
			output_string out " : \n\t\t";
			output_statement out s) (List.rev stat_liste);
		output_string out "\t\t\t default : \n\t\t";
		output_statement out stat;
		output_string out "\t\t }; \n"
	| SETSPE (loc, exp) ->
		output_string out "\t\t";
		output_location out loc;
		(* !!DEBUG!! *)
		output_string out "=";(*"=[[SETSPE]]";*)
		output_expr out exp;
		output_string out ";\n"
	| LINE (file, line, s) ->
		(*Printf.fprintf out "#line \"%s\" %d\n" file line;*)
		output_statement out s
	| INLINE s ->
		Printf.fprintf out "inline(%s)\n" s


(** Print a statement
	@param stat	Statement to print.*)
let rec print_statement stat= output_statement stdout stat


(** Print a type.
	@param typ	Type to print. *)
let output_type out typ =
	match typ with
	  TYPE_ID id -> output_string out id
	| TYPE_EXPR te -> output_type_expr out te

(** Print a type.
	@param typ	Type to print. *)
let print_type typ =
	output_type stdout typ

(** Print an attribute.
	@param attr	Attribute to print. *)
let output_attr out attr =
	match attr with
	| ATTR_EXPR (id, expr) ->
	  	Printf.fprintf out "\t%s = " id;
		output_expr out expr;
		output_char out '\n'

	| ATTR_STAT (id, stat) -> Printf.printf "\t%s = {\n" id ;
				  output_statement out stat;
				  Printf.fprintf out "\t}\n";
		()
	| ATTR_USES ->
		()
	| ATTR_LOC (id, l) ->
		Printf.fprintf out "\t%s = " id;
		output_location out l;
		output_char out '\n'


(** Print an attribute.
	@param attr	Attribute to print. *)
let print_attr attr =
	output_attr stdout attr

(** Print a memory attibute.
	@param attr	Memory attribute to print. *)
let output_mem_attr out attr =
	let rec print_call id args =
		print_string id;
		if args <> [] then
			begin
				ignore(List.fold_left (fun sep arg ->
						output_string out sep;
						print_arg arg;
						", "
					) "(" args);
				print_string ")"
			end
	and print_arg arg =
		match arg with
		| ATTR_ID (id, args) -> print_call id args
		| ATTR_VAL cst -> output_const out cst in

	match attr with
	| ATTR_EXPR("volatile", CONST(_, CARD_CONST n)) ->
		Printf.fprintf out "volatile(%d)" (Int32.to_int n)
	| ATTR_LOC("alias", l) ->
		output_string out "alias "; output_location out l
	| ATTR_EXPR("init", CONST(_, v)) ->
		output_string out "init = ";
		output_const out v
	| _ ->
		output_attr out attr

(** Print a memory attibute.
	@param attr	Memory attribute to print. *)
let print_mem_attr attr =
	output_mem_attr stdout attr

(** Print a list of memory attributes.
	@param attrs	List of attributes. *)
let output_mem_attrs out attrs =
	List.iter (fun attr -> output_string out " "; output_mem_attr out attr) attrs

(** Print a list of memory attributes.
	@param attrs	List of attributes. *)
let print_mem_attrs attrs =
	output_mem_attrs stdout attrs

(** Print a specification item.
	@param out	Stream to output to.
	@param spec	Specification item to print. *)
let output_spec out spec =
	let print_newline _ = output_char out '\n' in
	let print_string = output_string out in
	match spec with
	  LET (name, cst) ->
	  	Printf.fprintf out "let %s = " name;
		output_const out cst;
		print_newline ()
	| TYPE (name, t) ->
		Printf.fprintf out "type %s = " name;
		output_type_expr out t;
		print_newline ()
	| MEM (name, size, typ, attrs) ->
		Printf.fprintf out "mem %s [%d, " name size;
		output_type_expr out typ;
		print_string "]";
		output_mem_attrs out attrs;
		print_newline ()
	| REG (name, size, typ, attrs) ->
		Printf.fprintf out "reg %s [%d, " name size;
		output_type_expr out typ;
		print_string "]";
		output_mem_attrs out attrs;
		print_newline ()
	| VAR (name, size, typ) ->
		Printf.fprintf out "var %s [%d, " name size;
		output_type_expr out typ;
		print_string "]\n";
	| RES name ->
		Printf.fprintf out "resource %s\n" name
	| EXN name ->
		Printf.fprintf out "exception %s\n" name
	| AND_MODE (name, pars, res, attrs) ->
		print_string "mode ";
		print_string name;
		print_string " (";
		let _ = List.fold_left
			(fun fst (id, typ) ->
				if not fst then print_string ", ";
				print_string id;
				print_string ": ";
				output_type out typ;
				false
			)
			true pars in
		print_string ")";
		if res <> NONE then begin
			print_string " = ";
			output_expr out res
		end;
		print_string "\n";
		List.iter (output_attr out) (List.rev attrs) ;
		print_newline ();
	| OR_MODE (name, modes) -> Printf.printf "mode %s = " name ;
				   List.iter (fun a -> Printf.fprintf out " %s | " a) (List.rev (List.tl modes)) ;
				   Printf.fprintf out "%s" (List.hd (modes));
				   Printf.fprintf out "\n";
		()
	| AND_OP (name, pars, attrs) -> Printf.fprintf out "op %s (" name ;
					if (List.length pars)>0
					then begin
						List.iter (fun a -> begin 	Printf.fprintf out "%s : " (fst a) ;
										output_type out (snd a);
										Printf.fprintf out ", ";
								   end) (List.rev (List.tl pars));
						Printf.fprintf out "%s : " (fst (List.hd pars));
						output_type out (snd (List.hd pars));
					end;
					Printf.fprintf out ")\n";
					List.iter (output_attr out) (List.rev attrs) ;
		()
	| OR_OP (name, modes) -> Printf.fprintf out "op %s = " name ;
				 List.iter (fun a -> Printf.fprintf out " %s | " a) (List.rev (List.tl modes));
				 Printf.fprintf out "%s" (List.hd modes);
				 Printf.fprintf out "\n";
		()

	| PARAM (name,t)->
		Printf.fprintf out "param %s (" name;
		output_type out t;
		output_string out ")\n";

	| ATTR (a) -> print_attr a;
		()

	| ENUM_POSS (name,s,_,_)->
		Printf.fprintf out "possibility %s of enum %s\n" name s;

	| CANON_DEF(name, kind, type_res, type_prms_list) ->
		Printf.fprintf out "canon ";
		if kind = CANON_FUNC then
			(output_type_expr out type_res;
			Printf.fprintf out " \"%s\"(" name;
			ignore (List.fold_left
				(fun f e ->
					if not f then output_string out ", ";
					output_type_expr out e; false)
				true
				type_prms_list);
			output_string out ")\n")
		else
			Printf.fprintf out "\"%s\"\t: " name;
			output_type_expr out type_res;
			output_string out "\n"


	| UNDEF ->
		output_string out "<UNDEF>"

(** Print a specification item.
	@param spec	Specification item to print. *)
let print_spec spec =
	output_spec stdout spec


(**
    return the gliss_isize defined in nmp sources
*)
let get_isize _ =
	let s = try get_symbol "gliss_isize" with Symbol_not_found _ -> UNDEF
	in
	match s with
	(* if gliss_isize not defined, we assume we have a cisc isa *)
	UNDEF ->
		[]
	| LET(st, cst) ->
		(match cst with
		STRING_CONST(nums, _, _) ->
			List.map
			(fun x ->
				try
				int_of_string x
				with
				Failure "int_of_string" ->
					failwith "gliss_isize must be a string containing integers seperated by commas."
			)
			(Str.split (Str.regexp ",") nums)
		| _ ->
			failwith "gliss_isize must be defined as a string constant (let gliss_size = \"...\")"
		)
	| _ ->
		failwith "gliss_isize must be defined as a string constant (let gliss_size = \"...\")"

(** Get the type of a parameter.
	@param p	Parameter.
	@return		Parameter type. *)
let get_type_param p =
	match p with
	(str, TYPE_ID(n)) ->
		n
	| _ ->
		"[err741]"

(** Get the parameter name.
	@param p	Parameter.
	@return		Parameter name. *)
let get_name_param p =
	match p with
	(str, _) ->
		str

(** Display a parameter.
	@param p	Parameter to display. *)
let print_param p =
	match p with
	(str, TYPE_ID(n)) ->
		Printf.printf " (%s:%s) " (get_name_param p) (get_type_param p)
	| (str, TYPE_EXPR(t)) ->
		begin
		Printf.printf " (%s:" (get_name_param p);
		print_type (TYPE_EXPR(t));
		print_string ") "
		end


(** Display a parameter list.
	@param l	List of parameters. *)
let rec print_param_list l =
begin
	match l with
	[] ->
		print_string "\n"
	| h::q ->
		begin
		print_param h;
		print_param_list q
		end
end


(**	Run nmp2nml on the given file.
	@param file	File to run on.
	@return		NMP output. *)
let run_nmp2nml file =

	(* find the command *)
	let cmd =
		let cmd = Config.source_dir ^ "/gep/gliss-nmp2nml.pl" in
		if Sys.file_exists cmd then cmd else
		let cmd = Config.install_dir ^ "/bin/gliss-nmp2nml.pl" in
		if Sys.file_exists cmd then cmd else
		begin
			Printf.fprintf stderr "ERROR: cannot find gliss-nmp2nml.pl to process %s\n" file;
			exit 1
		end in

	(* run it *)
	Unix.open_process_in (Printf.sprintf "%s %s" cmd file)


(**	Save the current IRG definition to a file.
	@param path			Path of the file to save to.
	@raise	Sys_error	If there is an error during the write. *)
let save path =
	let out = open_out path in
	Marshal.to_channel out (syms, pos_table) []


(** Load an NML description either NMP, NML or IRG.
	@param 	path		Path of the file to read from.
	@raise	Sys_error	If there is an error during the read. *)
let load path =
	let input = open_in path in
	let (new_syms, new_pt) =
		(Marshal.from_channel input :  spec StringHashtbl.t * pos_type StringHashtbl.t) in
	StringHashtbl.clear syms;
	StringHashtbl.clear pos_table;
	StringHashtbl.iter (fun key spec -> StringHashtbl.add syms key spec) new_syms;
	StringHashtbl.iter (fun key pos -> StringHashtbl.add pos_table key pos) new_pt


(** Test if an attribute is defined.
	@param id		Identifier of the attribute.
	@param attrs	List of attributes.
	@return			True if the attribute is found, false else. *)
let rec attr_defined id attrs =
	match attrs with
	| [] -> false
	| (ATTR_EXPR (id', _))::_ when id = id' -> true
	| (ATTR_STAT (id', _))::_ when id = id' -> true
	| (ATTR_LOC (id', _))::_ when id = id' -> true
	| _::tl -> attr_defined id tl


(** Get an attribute as an expression.
	@param id		Identifier of the looked attribute.
	@param attrs	List of attributes.
	@param def		Default value if the attribute is not found.
	@return			Found attribute value or the default.
	@raise Error _	If the attribute exists but does not have the right type. *)
let rec attr_expr id attrs def =
	let error _ =
		raise (IrgError (Printf.sprintf "attribute \"%s\" should be an expression" id)) in
	match attrs with
	| [] -> def
	| (ATTR_EXPR (id', e))::_ when id = id' -> e
	| (ATTR_STAT (id', _))::_ when id = id' -> error ()
	| (ATTR_LOC (id', _))::_ when id = id' -> error ()
	| _::tl -> attr_expr id tl def


(** Get an attribute as a statement.
	@param id		Identifier of the looked attribute.
	@param attrs	List of attributes.
	@param def		Default value if the attribute is not found.
	@return			Found attribute value or the default.
	@raise Error _	If the attribute exists but does not have the right type. *)
let rec attr_stat id attrs def =
	let error _ =
		raise (IrgError (Printf.sprintf "attribute \"%s\" should be a statement" id)) in
	match attrs with
	| [] -> def
	| (ATTR_STAT (id', s))::_ when id = id' -> s
	| (ATTR_EXPR (id', _))::_ when id = id' -> error ()
	| (ATTR_LOC (id', _))::_ when id = id' -> error ()
	| _::tl -> attr_stat id tl def


(** Apply the given functions to all specifications.
	@param f	Function to apply. *)
let iter f =
	StringHashtbl.iter f syms


(** Apply the given function to all specification and with the given data.
	@param f	Function to apply.
	@param d	Initial data.
	@return 	Result data *)

let fold f d =
	StringHashtbl.fold f syms d
