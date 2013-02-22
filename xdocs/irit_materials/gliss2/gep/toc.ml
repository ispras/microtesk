(*
 * GLISS V2 -- translator from SimNML action to C
 * Copyright (c) 2008-10, IRIT - UPS <casse@irit.fr>
 *
 * GLISS V2 is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * GLISS V2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GLISS V2; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *)

exception UnsupportedType of Irg.type_expr
exception UnsupportedExpression of Irg.expr
exception Error of string
exception PreError of (out_channel -> unit)
exception LocError of string * int * (out_channel -> unit)

let trace id = () (*Printf.printf "TRACE: %s\n" id; flush stdout*)

(** Threshold which integer as suffixed under *)
let int_threshold = Int32.of_int 255

(* KNOWN BUGS
	reg r[1, t] alias rp[i]	is not rightly generated
 *)

(* StringHash module *)
module HashedString = struct
	type t = string
	let equal s1 s2 = s1 = s2
	let hash s = Hashtbl.hash s
end
module StringHashtbl = Hashtbl.Make(HashedString)


(** Get a statement attribute.
	@param name		Name of the attribute. *)
let get_stat_attr name =
	match Irg.get_symbol name with
	| Irg.ATTR (Irg.ATTR_STAT (_, stat)) -> stat
	| _ -> failwith "Not a statement attribute !"


(** Execute the function f, capturing PreError exception and
	adding error location to re-raise them as LocError.
	@param file	Source file.
	@param line	Source line.
	@param f	Function execution.
	@param arg	Argument to apply on f*)
let locate_error file line f arg =
	try
		f arg
	with PreError f ->
		raise (LocError (file, line, f))


(** Raise an error with the given message.
	@param msg	Message to display. *)
let error msg =
	raise (PreError (fun out -> output_string out msg))


(** Raise an unsupported type error.
	@param t	Unsupported type. *)
let unsupported_type t =
	raise (PreError (fun out ->
		output_string out "unsupported type: ";
		Irg.output_type_expr out t))


(** Generate an error exception with the given message
	from the given expression.
	@param msg	Message to display.
	@param expr	Expression causing the error. *)
let error_on_expr msg expr =
	raise (PreError (fun out ->
		output_string out msg;
		output_string out ": ";
		Irg.output_expr out expr))


(** C type in the generated code. *)
type c_type =
	  INT8
	| UINT8
	| INT16
	| UINT16
	| INT32
	| UINT32
	| INT64
	| UINT64
	| FLOAT
	| DOUBLE
	| LONG_DOUBLE
	| CHAR_PTR

type bit_order = UPPERMOST | LOWERMOST


(** Gather information useful for the generation. *)
type info_t = {
	mutable out: out_channel;		(** out channel *)
	mutable proc: string;			(** processor name *)
	mutable state: string;			(** state variable name *)
	mutable bpath: string;			(** build path *)
	mutable ipath: string;			(** include path *)
	mutable hpath: string;			(** header path include/proc *)
	mutable spath: string;			(** source path *)
	mutable bito: bit_order;		(** define bit order for bit fields *)
	mutable iname: string;			(** current  integrated instruction name *)
	mutable inst: Iter.inst;		(** current instruction *)
	mutable temp: int;				(** index for a new temporary *)
	mutable temps: (string * Irg.type_expr) list;		(** list of temporaries *)
	mutable vars: (string * (int * Irg.type_expr)) list;(** list of used variables *)
	mutable calls: (string * string) list;				(** list of performed attributes call *)
	mutable recs: string list;		(** list of recursive actions *)
	mutable lab: int;				(** index of a new label *)
	mutable attrs: Irg.stat StringHashtbl.t;			(** list of prepared attributes *)
	mutable pc_name: string;			(** name of the register used as PC (marked as __attr(pc)) *)
	mutable ppc_name: string;		(** name of the register used as previous instruction PC (marked as __attr(ppc)) *)
	mutable npc_name: string;		(** name of the register used as next instruction PC (marked as __attr(npc)) *)
	mutable indent: int;			(** identation level *)
}



(** Empty information record. *)
let info _ =
	let p = Irg.get_proc_name () in
	let b =
		match Irg.get_symbol "bit_order" with
		  Irg.UNDEF -> UPPERMOST
		| Irg.LET(_, Irg.STRING_CONST(id, _, _)) ->
			if (String.uppercase id) = "UPPERMOST" then UPPERMOST
			else if (String.uppercase id) = "LOWERMOST" then LOWERMOST
			else raise (Error "'bit_order' must contain either 'uppermost' or 'lowermost'")
		| _ -> raise (Error "'bit_order' must be defined as a string let") in

	let get_attr_regs name_attr f =
		let rec search_in_mem_attr_list reg_name ma_l =
			match ma_l with
			| [] -> None
			| Irg.ATTR_EXPR(n, v) :: _ when n = name_attr && (f v) -> Some reg_name
			| _::tl -> search_in_mem_attr_list reg_name tl in
		let aux key sp accu =
			if accu <> None then accu else
			match sp with
			| Irg.REG(name, _, _, m_a_l) -> search_in_mem_attr_list name m_a_l
			| _ -> accu in
		Irg.StringHashtbl.fold aux Irg.syms None in

	let is_true e =
		try Sem.is_true (Sem.eval_const e)
		with Sem.SemError msg -> raise (Error msg) in

	let get_reg_name name =
		match get_attr_regs name is_true with
		| None -> ""
		| Some n -> n in

	let pc =
		match get_attr_regs "pc" is_true with
		| None -> raise (Error "PC not defined, one register must have the \"pc\" attribute ( __attr(pc) )")
		| Some n -> n in
	let path = Sys.getcwd () in {
		out = stdout;
		proc = p;
		state = "state";
		iname = "";
		inst = Iter.null;
		bpath = path;
		ipath = path ^ "/include";
		hpath = path ^ "/include/" ^ p;
		spath = path ^ "/src";
		bito = b;
		temp = 0;
		temps = [];
		vars = [];
		calls = [];
		recs = [];
		lab = 0;
		attrs = StringHashtbl.create 211;
		pc_name = pc;
		npc_name = get_reg_name "npc";
		ppc_name = get_reg_name "ppc";
		indent = 1;
	}


(** Reset indenting.
	@param info		Information structure. *)
let indent_reset info =
	info.indent <- 1

(** Set the current instruction.
	@param info		Current generation information.
	@param inst		Current instruction. *)
let set_inst info inst =
	info.inst <- inst;
	info.iname <- Iter.get_name inst


(** Generate a new label name.
	@param info	Current generation information.
	@return		New label name. *)
let new_label info =
	let res = Printf.sprintf "gliss_%s_%d_" info.iname info.lab in
	info.lab <- info.lab + 1;
	res


(** Compute the power of two whose exponent is the lowest
	value greater or equal to n.
	@param n	N to compute with.
	@return		Lowest power of 2. *)
let ceil_log2 n =
	int_of_float (ceil ((log (Int32.to_float n)) /. (log 2.)))


(** Convert an NML type to C type.
	@param t	Type to convert.
	@return		Matching C type.
	@raise UnsupportedType	If the type is not supported. *)
let rec convert_type t =
	match t with
	  Irg.NO_TYPE -> assert false
	| Irg.BOOL -> UINT8
	| Irg.INT n when n <= 8 -> INT8
	| Irg.INT n when n <= 16 -> INT16
	| Irg.INT n when n <= 32 -> INT32
	| Irg.INT n when n <= 64 -> INT64
	| Irg.CARD n when n <= 8 -> UINT8
	| Irg.CARD n when n <= 16 -> UINT16
	| Irg.CARD n when n <= 32 -> UINT32
	| Irg.CARD n when n <= 64 -> UINT64
	| Irg.FLOAT (23, 9) -> FLOAT
	| Irg.FLOAT (52, 12) -> DOUBLE
	| Irg.FLOAT (64, 16) -> LONG_DOUBLE
	| Irg.STRING -> CHAR_PTR
	| Irg.ENUM _ -> UINT32
	| Irg.RANGE (_, m) ->
		convert_type (Irg.INT (int_of_float (ceil ((log (Int32.to_float m)) /. (log 2.)))))
	| Irg.UNKNOW_TYPE ->
		(* we have some of this type in bitfield expr, we can't determine the real type and mem size
		so let's patch it up for the moment, uint32 should be the least worst choice *)
		UINT32
	| _ -> unsupported_type t


(** Compute the size of type in bits.
	@param t	Type to size.
	@return		Size of the type. *)
let type_size t =
	match t with
	| Irg.BOOL -> 1
	| Irg.INT n -> n
	| Irg.CARD n -> n
	| Irg.FLOAT (n, m) -> n + m
	| Irg.RANGE (_, m) ->
		int_of_float (ceil ((log (Int32.to_float m)) /. (log 2.)))
	| Irg.UNKNOW_TYPE -> 32
	| _ -> unsupported_type t


(** Compute size of a C type (in bits).
	@param t	C type.
	@return		Size ib bits. *)
let ctype_size t =
	match t with
	| INT8			-> 8
	| UINT8			-> 8
	| INT16			-> 16
	| UINT16		-> 16
	| INT32			-> 32
	| UINT32		-> 32
	| INT64			-> 64
	| UINT64		-> 64
	| FLOAT			-> 32
	| DOUBLE		-> 64
	| _				-> failwith "ctype_size"


(** Convert a C type to a string.
	@param t	C type to convert.
	@return		Matching string. *)
let rec type_to_string t =
	match t with
	  INT8 -> "int8_t"
	| UINT8 -> "uint8_t"
	| INT16 -> "int16_t"
	| UINT16 -> "uint16_t"
	| INT32 -> "int32_t"
	| UINT32 -> "uint32_t"
	| INT64 -> "int64_t"
	| UINT64 -> "uint64_t"
	| FLOAT -> "float"
	| DOUBLE -> "double"
	| LONG_DOUBLE -> "long double"
	| CHAR_PTR -> "char *"

(** return the printf-like format associated with a C type, integer numbers are supposedly output in hexadecimal format
	@param t	C type to convert.
	@return		the printf format matching the given type *)
let rec type_to_printf_format t =
	match t with
	  INT8 -> "%02X"
	| UINT8 -> "%02X"
	| INT16 -> "%04X"
	| UINT16 -> "%04X"
	| INT32 -> "%08X"
	| UINT32 -> "%08X"
	| INT64 -> "%016LX"
	| UINT64 -> "%016LX"
	| FLOAT -> "%f"
	| DOUBLE -> "%f"
	| LONG_DOUBLE -> "%Lf"
	| CHAR_PTR -> "%s"


(** Convert a C type to a parameter name.
	@param t	C type to convert.
	@return		Matching parameter name. *)
let rec type_to_field t =
	match t with
	  INT8 -> "int8"
	| UINT8 -> "uint8"
	| INT16 -> "int16"
	| UINT16 -> "uint16"
	| INT32 -> "int32"
	| UINT32 -> "uint32"
	| INT64 -> "int64"
	| UINT64 -> "uint64"
	| FLOAT -> "_float"
	| DOUBLE -> "_double"
	| LONG_DOUBLE -> "_long_double"
	| CHAR_PTR -> "string"


(** Convert a C type to a memory access name.
	@param t	C type to convert.
	@return		Matching memory access name. *)
let rec type_to_mem t =
	match t with
	  INT8 -> "8"
	| UINT8 -> "8"
	| INT16 -> "16"
	| UINT16 -> "16"
	| INT32 -> "32"
	| UINT32 -> "32"
	| INT64 -> "64"
	| UINT64 -> "64"
	| FLOAT -> "f"
	| DOUBLE -> "d"
	| LONG_DOUBLE -> "ld"
	| CHAR_PTR -> assert false


(** Convert an NML type to his size.
	@param t	Type to convert.
	@return		Matching size.
	@raise UnsupportedType	If the type is not supported. *)
let rec type_to_int t =
	match t with
	  Irg.NO_TYPE -> assert false
	| Irg.BOOL -> 8
	| Irg.INT n -> n
	| Irg.CARD n -> n
	| _ -> unsupported_type t



(** Get the name of a state macro, or returns the name as it is.
	@param info	Generation information.
	@param name	Register or memory name.
	@param prfx	prefix or not by PROC_NAME and uppercase or not *)
let state_macro info name prfx =
	if prfx then
		Printf.sprintf "%s_%s" (String.uppercase info.proc) (String.uppercase name)
	else
		name


(** Get the name of a parameter macro.
	@param info	Generation information.
	@param name	Parameter name. *)
let param_macro info name =
	Printf.sprintf "%s_%s_%s" (String.uppercase info.proc) (String.uppercase info.iname) name


(** Generate the name of a temporary of index i.
	@param i	Index of the temporary.
	@return		Temporary name. *)
let temp_name i =
	Printf.sprintf "_gtmp%d" i


(** Test if the given statement is a nop.
	@param stat		Statement to test.
	@return			True if it is a nop, false else. *)
let rec is_nop stat =
	match stat with
	| Irg.NOP -> true
	| Irg.LINE (_, _, s) -> is_nop s
	| _ -> false

(** Build a new temporary.
	@param info	Current generation information.
	@param t	Type of the temporary.
	@return		Temporary name. *)
let new_temp info typ =
	let var = Printf.sprintf "__gtmp_%d" info.temp in
	info.temp <- info.temp + 1;
	info.temps <- (var, typ)::info.temps;
	var


(** Add a used variable (and only if it has not been declared).
	@param info	Generation information.
	@param name	Name of the variable.
	@param cnt	Count of array.
	@param typ	Type of the variable. *)
let add_var info name cnt typ =
	if not (List.mem_assoc name info.vars)
	then info.vars <- (name, (cnt, typ)) :: info.vars


(** Declare temporaries variables.
	@param	Generation information. *)
let declare_temps info =
	List.iter
		(fun (name, typ) ->
			Irg.add_symbol name (Irg.VAR (name, 1, typ));
			Printf.fprintf info.out "\t%s %s;\n"
				(type_to_string (convert_type typ))
				name
		)
		info.temps;
	List.iter
		(fun (name, (cnt, typ)) ->
			Printf.fprintf info.out "\t%s %s%s;\n"
				(type_to_string (convert_type typ))
				name
				(if cnt = 1 then "" else Printf.sprintf "[%d]" cnt)
		)
		info.vars


(** cleanup temporaries.
	@param info	Generation information. *)
let cleanup_temps info =
	List.iter
		(fun (name, _) -> Irg.rm_symbol name)
		info.temps;
	info.temp <- 0;
	info.temps <- [];
	info.vars <- []


(** Convert unary operator to C operator.
	@param out	Channel to output to.
	@param op	Unary operator to convert. *)
let convert_unop out op =
	match op with
	  Irg.NOT	-> Printf.fprintf out "!"
	| Irg.BIN_NOT	-> Printf.fprintf out "~"
	| Irg.NEG	-> Printf.fprintf out "-"


(** Convert binary operator to C operator.
	@param out	Channel to output to.
	@param op	Binary operator to convert. *)
let convert_binop out op =
	match op with
	  Irg.ADD	-> Printf.fprintf out "+"
	| Irg.SUB	-> Printf.fprintf out "-"
	| Irg.MUL	-> Printf.fprintf out "*"
	| Irg.DIV	-> Printf.fprintf out "/"
	| Irg.MOD	-> Printf.fprintf out "%%"
	| Irg.LSHIFT	-> Printf.fprintf out "<<"
	| Irg.RSHIFT	-> Printf.fprintf out ">>"
	| Irg.LT	-> Printf.fprintf out "<"
	| Irg.GT	-> Printf.fprintf out ">"
	| Irg.LE	-> Printf.fprintf out "<="
	| Irg.GE	-> Printf.fprintf out ">="
	| Irg.EQ	-> Printf.fprintf out "=="
	| Irg.NE	-> Printf.fprintf out "!="
	| Irg.AND	-> Printf.fprintf out "&&"
	| Irg.OR	-> Printf.fprintf out "||"
	| Irg.BIN_AND	-> Printf.fprintf out "&"
	| Irg.BIN_OR	-> Printf.fprintf out "|"
	| Irg.BIN_XOR	-> Printf.fprintf out "^"
	| Irg.CONCAT	-> Printf.fprintf out ""
	| Irg.RROTATE	-> Printf.fprintf out ""
	| Irg.LROTATE	-> Printf.fprintf out ""
	| Irg.EXP	-> Printf.fprintf out ""


(** Convert an OCAML string to generated C string code.
	Replace '"' and '\', respectively, by '\"' and '\\'.
	@param str	String to transform.
	@return		Transformed string. *)
let cstring str =
	let rec aux str i res =
		if i >= String.length str then res else
		match String.get str i with
		  '\"' -> aux str (i + 1) (res ^ "\\\"")
		| '\\' -> aux str (i + 1) (res ^ "\\\\")
		| '\n' -> aux str (i + 1) (res ^ "\\n")
		| '\t' -> aux str (i + 1) (res ^ "\\t")
		| '\r' -> aux str (i + 1) (res ^ "\\r")
		| c when c < ' ' -> aux str (i + 1) (res ^ (Printf.sprintf "\\x%02x" (Char.code c)))
		| c -> aux str (i + 1) (res ^ (String.make 1 c)) in

	aux str 0 ""


(** Get the alias from the list of attributes.
	@param attrs	Attributes to get alias from.
	@return			Alias relocation expression found
					(LOC_NONE if there is no alias) *)
let rec get_alias attrs =
	match attrs with
	| [] -> Irg.LOC_NONE
	| (Irg.ATTR_LOC ("alias", loc))::_ -> loc
	| _::tl -> get_alias tl


(** Get the unaliased memory name.
	@param name		Memory name.
	@return			Unaliased memory name. *)
let rec unaliased_mem_name name =
	match Irg.get_symbol name with
	| Irg.MEM (_, _, _, attrs) ->
		(match get_alias attrs with
		| Irg.LOC_NONE -> name
		| Irg.LOC_REF (_, n, _, _, _) -> unaliased_mem_name n
		| _ -> failwith "no concat !")
	| _ -> failwith "not memory !"

let print_alias msg (r, i, il, ub, lb, t) =
	Printf.printf "\t%s(%s [" msg r;
	Irg.print_expr i;
	Printf.printf ":%d] < " il;
	Irg.print_expr ub;
	print_string " .. ";
	Irg.print_expr lb;
	print_string " > : ";
	Irg.print_type_expr t;
	print_string ")\n"


(** Perform alias resolution, that is, translate a state read/write into
	a tuple of unaliased states.
	@param name		Name of the accessed state resource.
	@param idx		Index of the accessed state resource.
	@param ub		Upper bit.
	@param lb		Lower bit.
	@return			(unaliased state name, first state index,
					state resource count, upper bit, lower bit,
					resource type) *)
let resolve_alias name idx ub lb =

	let t = Irg.CARD(32) in
	let const c =
		Irg.CONST (t, Irg.CARD_CONST (Int32.of_int c)) in
	let add e1 e2 =
		if e1 = Irg.NONE then e2 else
		Irg.BINOP (t, Irg.ADD, e1, e2) in
	let mul e1 e2 =
		if e1 = Irg.NONE then Irg.NONE else
		Irg.BINOP (t, Irg.MUL, e1, e2) in
	let div e1 e2 =
		if e1 = Irg.NONE then Irg.NONE else
		Irg.BINOP (t, Irg.DIV, e1, e2) in
	let rem e1 e2 =
		if e1 = Irg.NONE then Irg.NONE else
		Irg.BINOP (t, Irg.MOD, e1, e2) in

	let convert tr v =
		let (r, i, il, ub, lb, ta) = v in
		if ta = Irg.NO_TYPE then (r, i, il, ub, lb, tr) else
		let sa = Sem.get_type_length ta in
		let sr = Sem.get_type_length tr in
		if sa = sr then v else
		if sa < sr then
			let f = const (sr / sa) in
			(r, div i f, 1, add (rem i f) ub, add (rem i f) lb, t)
		else
			let f = sa / sr in
			(r, mul i (const f), il * f, ub, lb, t) in

	let shift s v =
		let (r, i, il, ub, lb, t) = v in
		(r, add i s, il, ub, lb, t) in

	let field u l v =
		let (r, i, il, ub, lb, t) = v in
		if ub = Irg.NONE
		then (r, i, il, u, l, t)
		else (r, i, il, add ub l, add lb l, t) in

	let set_name name v =
		let (_, i, il, ub, lb, t) = v in
		(name, i, il, ub, lb, t) in

	let rec process_alias tr attrs v =
		let v = convert tr v in
		match get_alias attrs with
		| Irg.LOC_NONE -> v
		| Irg.LOC_CONCAT _ -> failwith "bad relocation alias (LOC_CONCAT)"
		| Irg.LOC_REF (tr, r, idxp, ubp, lbp) ->
			let v = set_name r v in
			let v = if idxp = Irg.NONE then v else shift idxp v in
			let v = if ubp = Irg.NONE then v else field ubp lbp v in
			process v

	and process v =
		let (r, i, il, ub, lb, t) = v in
		match Irg.get_symbol r with
		| Irg.REG (_, _, tr, attrs) ->
			process_alias tr attrs v
		| Irg.VAR (_, _, tr) ->
			process_alias tr [] v
		| Irg.LET _
		| Irg.CANON_DEF _ ->
			(name, Irg.NONE, 1, Irg.NONE, Irg.NONE, Irg.NO_TYPE)
		| Irg.MEM (_, _, tr, attrs) ->
			process_alias tr attrs v
		(* this should happen only when using gliss1 predecode *)
		| Irg.PARAM (_, typ) ->
			(match typ with
			| Irg.TYPE_EXPR(tt) -> (name, i, il, ub, lb, tt)
			| _ -> failwith "OUPS!\n")
		| _ ->
			failwith "bad alias" in
	let res = process (name, idx, 1, ub, lb, Irg.NO_TYPE) in
	res


(** Unalias an expression.
	@param name		Name of the accessed state resource.
	@param idx		Index (may be NONE)
	@param ub		Upper bit number (may be NONE)
	@param lb		Lower bit number (may be NONE)
	@param typ		Type of name[idx]<lb..ub> (for a mem access)
	@return			Unaliased expression. *)
let unalias_expr name idx ub lb typ =
	let (r, i, il, ubp, lbp, t) = resolve_alias name idx ub lb in
	let t32 = Irg.CARD(32) in
	let const c =
		Irg.CONST (t32, Irg.CARD_CONST (Int32.of_int c)) in
	let add e1 e2 =
		if e1 = Irg.NONE then e2 else
		Irg.BINOP (t, Irg.ADD, e1, e2) in
	let rec concat l tt =
		if l = 0 then
			if i = Irg.NONE then Irg.REF r
			else Irg.ITEMOF (t, r, i)
		else
			Irg.BINOP(tt, Irg.CONCAT,
				Irg.ITEMOF (t, r, add i (const l)),
				concat (l - 1) tt) in
	let field e ub lb tt =
		if ub = Irg.NONE then e
		else Irg.BITFIELD(tt, e, ub, lb) in
	match Irg.get_symbol name with
	| Irg.REG (_, _, tt, _) ->
		field (concat (il - 1) tt) ubp lbp tt
	| Irg.MEM (_, _, tt, _) ->
		field (Irg.ITEMOF(typ, r, idx)) ub lb tt
	| s ->
		failwith "unalias_expr"


(** Build a sequence, optimizing the result if one is a nop.
	@param s1	First statement.
	@param s2	Second statement.
	@return		Sequenced statements. *)
let seq s1 s2 =
		if s1 = Irg.NOP then s2 else
		if s2 = Irg.NOP then s1 else
		Irg.SEQ (s1, s2)


(** Build a sequence from a list of statements.
	@param list		List of statements.
	@return			Sequence of statements. *)
let rec seq_list list =
	match list with
	| [] -> Irg.NOP
	| [s] -> s
	| s::t -> seq s (seq_list t)


(** Unalias a reference.
	@param info			Current generation information.
	@param expr			Current expression.
	@return				unaliased expression. *)
let rec unalias_ref info expr stats =
	let unalias name idx typ unalias_mem =
		match Irg.get_symbol name with
		(* IRg.MEM added makes everything goes badly (with ppc2 and arm at least) *)
		| Irg.REG _ ->
			unalias_expr name idx Irg.NONE Irg.NONE typ
		| Irg.MEM _ ->
			if unalias_mem then
				unalias_expr name idx Irg.NONE Irg.NONE typ
			else
				expr
		| Irg.VAR (_, cnt, Irg.NO_TYPE) ->
			expr
		| Irg.VAR (_, cnt, t) ->
			add_var info name cnt t; expr
		| _ ->
			expr in
	match expr with
	| Irg.REF name ->
		(* if mem ref, leave it this way, it surely is a parameter for a canonical *)
		(stats, unalias name Irg.NONE Irg.BOOL false)
	| Irg.ITEMOF (typ, tab, idx) ->
		let (stats, idx) = prepare_expr info stats idx in
		(stats, unalias tab idx typ true)
	| _ -> failwith "toc:unalias_ref"


(** Prepare expression for generation.
	@param info		Generation information.
	@param stats	Prefix statements  	@param expr		Expression to prepare.
	@return			(new prefix statements, prepared expression) *)
and prepare_expr info stats expr =

	let set typ var expr =
		Irg.SET (Irg.LOC_REF (typ, var, Irg.NONE, Irg.NONE, Irg.NONE), expr) in
	match expr with
	| Irg.REF name -> unalias_ref info expr stats
	| Irg.NONE
	| Irg.CONST _ -> (stats, expr)
	| Irg.COERCE (typ, expr) ->
		let (stats, expr) = prepare_expr info stats expr in
		(stats, Irg.COERCE (typ, expr))
	| Irg.FORMAT (fmt, args) ->
		let (stats, args) = prepare_exprs info stats args in
		(stats, Irg.FORMAT (fmt, List.rev args))
	| Irg.CANON_EXPR (typ, name, args) ->
		let (stats, args) = prepare_exprs info stats args in
		(stats, Irg.CANON_EXPR (typ, name, List.rev args))
	| Irg.FIELDOF (typ, base, id) ->
		(stats, Irg.FIELDOF (typ, base, id))
	| Irg.ITEMOF (typ, tab, idx) -> unalias_ref info expr stats
	| Irg.BITFIELD (typ, expr, lo, up) ->
		let (stats, expr) = prepare_expr info stats expr in
		let (stats, lo) = prepare_expr info stats lo in
		let (stats, up) = prepare_expr info stats up in
		(stats, Irg.BITFIELD (typ, expr, lo, up))
	| Irg.UNOP (typ, op, arg) ->
		let (stats, arg) = prepare_expr info stats arg in
		(stats, Irg.UNOP (typ, op, arg))
	| Irg.BINOP (typ, op, arg1, arg2) ->
		let (stats, arg1) = prepare_expr info stats arg1 in
		let (stats, arg2) = prepare_expr info stats arg2 in
		(stats, Irg.BINOP (typ, op, arg1, arg2))

	| Irg.IF_EXPR (typ, cond, tpart, epart) ->
		let (stats, cond) = prepare_expr info stats cond in
		let (tstats, tpart) = prepare_expr info Irg.NOP tpart in
		let (estats, epart) = prepare_expr info Irg.NOP epart in
		let tmp = new_temp info typ in
		(seq stats (Irg.IF_STAT (cond, seq tstats (set typ tmp tpart), seq estats (set typ tmp epart))),
		Irg.REF tmp)

	| Irg.SWITCH_EXPR (typ, cond, cases, def) ->
		let tmp = new_temp info typ in
		let (stats, cond) = prepare_expr info stats cond in
		let (dstats, def) = prepare_expr info Irg.NOP def in
		let cases = List.fold_left
			(fun cases (case, expr) ->
				let (stats, expr) = prepare_expr info stats expr in
				(case, seq stats (set typ tmp expr)) :: cases)
			[] cases in
		(seq
			stats
			(Irg.SWITCH_STAT (
				cond,
				cases,
				if def = Irg.NONE then Irg.NOP else (seq dstats (set typ tmp def)))),
		Irg.REF tmp)

	| Irg.ELINE (file, line, expr) ->
		let (stats, expr) = prepare_expr info stats expr in
		(stats, Irg.ELINE (file, line, expr))

	| Irg.EINLINE _ ->
		(stats, expr)

	| Irg.CAST(size, expr) ->
		let stats, expr = prepare_expr info stats expr in
		(stats, Irg.CAST(size, expr))

and prepare_exprs info (stats: Irg.stat) (args: Irg.expr list) =
	List.fold_left
		(fun (stats, args) arg ->
			let (stats, arg) = prepare_expr info stats arg in
			(stats, arg::args))
		(stats, [])
		args


(** Unalias an assignement.
	@param info		Generation information.
	@param stats	Side-effect statements.
	@param name		Name of the accessed state resource.
	@param idx		Index (may be NONE)
	@param ub		Upper bit number (may be NONE)
	@param lb		Lower bit number (may be NONE)
	@param			Expression to assign.
	@return			statements *)
let unalias_set info stats name idx ub lb expr =
	let (r, i, il, ubp, lbp, t) = resolve_alias name idx ub lb in

	let index_t = Irg.CARD(32) in
	let index c = Irg.CONST (index_t, Irg.CARD_CONST (Int32.of_int c)) in
	let addi e1 e2 = Irg.BINOP (index_t, Irg.ADD, e1, e2) in
	let subi e1 e2 = Irg.BINOP (index_t, Irg.SUB, e1, e2) in
	let shr e1 e2 = Irg.BINOP (index_t, Irg.RSHIFT, e1, e2) in
	let field e ub lb  = Irg.BITFIELD (t, e, ub, lb) in

	let set_full i ub lb e =
		Irg.SET (Irg.LOC_REF (t, r, i, ub, lb), e) in
	(*let set _ = set_full Irg.NONE Irg.NONE Irg.NONE in*)
	let set_field ub lb e = set_full Irg.NONE ub lb e in
	let set_item i e = set_full i Irg.NONE Irg.NONE e in
	let sett t n e =
		Irg.SET (Irg.LOC_REF (t, n, Irg.NONE, Irg.NONE, Irg.NONE), e) in

	let rec set_concat l s expr =
		let e = field expr (index (l * s + s - 1)) (index (l * s)) in
		if l = 0 then set_item i e else
		seq
			(set_item (addi i (index l)) e)
			(set_concat (l - 1) s expr) in

	let rec set_concat_field l s =
		if l = 0 then set_field ub lb expr else
		seq
			(set_full
				(addi i (index l))
				(subi ub (index (l * s)))
				(subi lb (index (l * s)))
				(shr expr (index (l * s))))
			(set_concat_field (l - 1) s) in

	let process tt =
		if (il = 1 && ubp == Irg.NONE) || il > 1 then
			if il = 1 then seq stats (set_item i expr) else
			let name = new_temp info tt in
			seq (seq stats (sett tt name expr)) (set_concat (il - 1) (Sem.get_type_length t) (Irg.REF name)) 
		else
			if il = 1 then seq stats (set_full i ubp lbp expr) else
			let name = new_temp info tt in
			seq (seq stats (sett tt name expr)) (set_concat_field (il - 1) (Sem.get_type_length t)) in

	match Irg.get_symbol name with
	| Irg.REG (_, _, tt, _) ->
		process tt
	| Irg.MEM (_, _, tt, _) ->
		seq stats (set_full (i) ub lb expr)
	| Irg.VAR (_, cnt, tt) ->
		add_var info name cnt tt;
		seq stats (set_full i ub lb expr)
	| Irg.CANON_DEF _ ->
		seq stats (set_full i ub lb expr)
	(* this should happen only when using gliss1 predecode *)
	| Irg.PARAM (_, typ) ->
		(match typ with
		| Irg.TYPE_EXPR(tt) -> process tt
		| _ -> failwith "OUPS!\n")
	| _ -> 
		Irg.print_spec (Irg.get_symbol name);
		failwith "unalias_set"


(* !!TODO!! move to Sem *)
let get_loc_size l =
	Sem.get_type_length (Sem.get_loc_type l)

(** Prepare a statement before generation. It includes:
	- preparation of expressions,
	- split of concatenation location in assignment.
	@param info		Generation information.
	@param stat		Statement to prepare.
	@return			Prepared statement. *)
let rec prepare_stat info stat =
	trace "prepare_stat 1";
	let set t n e =
		Irg.SET (Irg.LOC_REF (t, n, Irg.NONE, Irg.NONE, Irg.NONE), e) in
	let refto n = Irg.REF n in
	let rshift t v s = Irg.BINOP (t, Irg.RSHIFT, v, s) in
	let index c = Irg.CONST (Irg.CARD(32), Irg.CARD_CONST (Int32.of_int c)) in

	let rec prepare_set stats loc expr =
		match loc with
		| Irg.LOC_NONE ->
			failwith "no location to set (3)"
		| Irg.LOC_REF (t, r, i, u, l) ->
			let (stats, i) = prepare_expr info stats i in
			let (stats, u) = prepare_expr info stats u in
			let (stats, l) = prepare_expr info stats l in
			(match Irg.get_symbol r with
			| Irg.MEM _ -> seq stats (Irg.SET ((Irg.LOC_REF (t, r, i, u, l)), expr))
			| _ -> unalias_set info stats r i u l expr)
		| Irg.LOC_CONCAT (t, l1, l2) ->
			let tmp = new_temp info t in
			let stats = seq stats (set t tmp expr) in
			let stats = prepare_set stats l1
				(rshift t (refto tmp) (index (get_loc_size l2))) in
			prepare_set stats l2 (refto tmp) in

	match stat with
	| Irg.NOP
	| Irg.ERROR _ ->
		stat
	| Irg.SEQ (s1, s2) ->
		Irg.SEQ (prepare_stat info s1, prepare_stat info s2)
	| Irg.SET (loc, expr) ->
		let (stats, expr) = prepare_expr info Irg.NOP expr in
		prepare_set stats loc expr
	| Irg.CANON_STAT (name, args) ->
		let (stats, args) = prepare_exprs info Irg.NOP args in
		seq stats (Irg.CANON_STAT (name, List.rev args))

	| Irg.IF_STAT (cond, tpart, epart) ->
		let (stats, cond) = prepare_expr info Irg.NOP cond in
		seq stats (Irg.IF_STAT (cond, prepare_stat info tpart, prepare_stat info epart))

	| Irg.SWITCH_STAT (cond, cases, def) ->
		let (stats, cond) = prepare_expr info Irg.NOP cond in
		let cases = List.map
			(fun (case, stat) -> (case, prepare_stat info stat))
			cases in
		Irg.SEQ (stats, Irg.SWITCH_STAT (cond, cases, prepare_stat info def))

	| Irg.LINE (file, line, stat) ->
		Irg.LINE (file, line, prepare_stat info stat)

	| Irg.SETSPE (l, e) ->
		(* !!TODO!! fix type here *)
		prepare_stat info (Irg.SET (l, e))

	| Irg.EVAL name ->
		prepare_call info name;
		stat

	| Irg.EVALIND _ ->
		failwith "prepare_stat: must have been removed !"

	| Irg.INLINE _ ->
		stat


and prepare_call info name =
	if not (StringHashtbl.mem info.attrs name) then
		begin
			StringHashtbl.add info.attrs name Irg.NOP;
			StringHashtbl.replace info.attrs name (prepare_stat info (get_stat_attr name))
		end


(** Generate a prepared expression.
	@param info		Generation information.
	@param expr		Expression to generate.
	@param prfx		Boolean indicating if state members (reg, mem, ...) should be prefixed by PROC_NAME,
				transmitted to most of the other gen_xxx expr related functions *)
let rec gen_expr info (expr: Irg.expr) prfx =
(*!!DEBUG!!*)
(*let level = !ge in
Printf.printf "**gen_expr(%d), prfx=%b, expr=" level prfx; ge := !ge + 1;
Irg.print_expr expr; print_char '\n';*)

	let out = output_string info.out in

	match expr with
	| Irg.NONE -> ()
	| Irg.CONST (typ, cst) -> gen_const info typ cst
	| Irg.REF name -> gen_ref info name prfx
	| Irg.ITEMOF (typ, name, idx) -> gen_itemof info typ name idx prfx
	| Irg.BITFIELD (typ, expr, lo, up) -> gen_bitfield info typ expr lo up prfx
	| Irg.UNOP (t, op, e) -> gen_unop info t op e prfx
	| Irg.BINOP (t, op, e1, e2) -> gen_binop info t op e1 e2 prfx
	| Irg.COERCE (typ, sube) -> coerce info typ sube expr prfx
	| Irg.CANON_EXPR (_, name, args) ->
		Printf.fprintf info.out "%s(" name;
		ignore (List.fold_left
			(fun com arg -> if com then out ", "; gen_expr info arg prfx; true)
			false args);
		out ")"
	| Irg.EINLINE s ->
		out s
	| Irg.ELINE (file, line, expr) ->
		(try gen_expr info expr prfx
		with PreError f -> raise (LocError (file, line, f)))
	| Irg.FORMAT _ -> failwith "format out of image/syntax attribute"
	| Irg.IF_EXPR _
	| Irg.SWITCH_EXPR _
	| Irg.FIELDOF _ ->
		error_on_expr "should have been reduced" expr
	| Irg.CAST (size, expr) -> gen_cast info size expr prfx


(** Apply a mask for a type that does not match a C type.
	@param info	Generation information.
	@param t	Result type.
	@param f	Function displaying the expression. *)
and mask info t f =
	let _32_or_64 size =
		if size <= 32 then "32"
		else if size <= 64 then "64"
		else failwith "(toc.ml::mask::_32_or_64) trying to manipulate a number of more than 64 bits" in
	match t with
	| Irg.CARD _ when (ctype_size (convert_type t)) <> (type_size t) ->
		Printf.fprintf info.out "__%s_MASK%s(%d, "
			(String.uppercase info.proc)
			(_32_or_64 (type_size t))
			(type_size t);
		f ();
		output_char info.out ')'
	| _ ->
		f ()


(** Apply a sign extension for a type that does not match a C type.
	@param info	Generation information.
	@param t	Result type.
	@param f	Function displaying the expression. *)
and exts info t f =
	match t with
	| Irg.INT _ when (ctype_size (convert_type t)) <> (type_size t) ->
		let shift = (ctype_size (convert_type t)) - (type_size t) in
		Printf.fprintf info.out "__%s_EXTS(%d, " (String.uppercase info.proc) shift;
		f ();
		Printf.fprintf info.out ")"
	| _ ->
		f ()


(** Generate a constant.
	@param info		Generation information.
	@param typ		Constant type.
	@param cst		Constant. *)
and gen_const info typ cst =
	match typ, cst with
	| _, Irg.NULL -> failwith "null constant"
	| Irg.CARD _, Irg.CARD_CONST v ->
		if (Int32.compare v Int32.zero) < 0 then
			Printf.fprintf info.out "0x%lxLU" v
		else if (Int32.compare (Int32.abs v) int_threshold) < 0 then
			Printf.fprintf info.out "%ld" v
		else
			Printf.fprintf info.out "%ldLU" v
	| _, Irg.CARD_CONST v ->
		if (Int32.compare (Int32.abs v) int_threshold) < 0 then
			Printf.fprintf info.out "%ld" v
		else
			Printf.fprintf info.out "%ldL" v
	| Irg.CARD _, Irg.CARD_CONST_64 v -> Printf.fprintf info.out "0x%LxLLU" v
	| _, Irg.CARD_CONST_64 v -> Printf.fprintf info.out "%LdLL" v
	| _, Irg.STRING_CONST(s, b, _) ->
		if b then
			(* canonical const *)
			Printf.fprintf info.out "%s" (cstring s)
		else
			(* simple string const *)
			Printf.fprintf info.out "\"%s\"" (cstring s)
	| _, Irg.FIXED_CONST v -> Printf.fprintf info.out "%f" v


(** Generate a reference to a state item.
	@param name		Name of the state item. *)
and gen_ref info name prfx =
	match Irg.get_symbol name with
	| Irg.LET (_, cst) -> gen_expr info (Irg.CONST (Irg.NO_TYPE, cst)) prfx
	| Irg.VAR _ -> output_string info.out name
	| Irg.REG _ -> output_string info.out (state_macro info name prfx)
	| Irg.MEM _ -> output_string info.out (state_macro info name prfx)
	| Irg.PARAM _ -> output_string info.out (param_macro info name)
	| Irg.ENUM_POSS (_, _, v, _) -> output_string info.out (Int32.to_string v)
	| s -> output_string info.out name (*failwith "expression form must have been removed")*)


(** Generate ITEMOF expression, r[idx].
	@param info		Generation information.
	@param t		type of the expression
	@param name		Name of the state item.
	@param idx		Index. *)
and gen_itemof info t name idx prfx =
	match Irg.get_symbol name with
	| Irg.VAR _ ->
		Printf.fprintf info.out "%s[" name;
		gen_expr info idx prfx;
		output_string info.out "]"
	| Irg.REG _ ->
		Printf.fprintf info.out "%s[" (state_macro info name prfx);
		gen_expr info idx prfx;
		output_string info.out "]"
	| Irg.MEM (_, _, _, _)  ->
		Printf.fprintf info.out "%s_mem_read%s(%s, "
			info.proc
			(type_to_mem (convert_type t))
			(state_macro info (unaliased_mem_name name) prfx);
		gen_expr info idx prfx;
		output_string info.out ")"
	| _ -> failwith "invalid itemof"


(** Generate code for unary operation.
	@param info		Generation information.
	@param t		Type of result.
	@param op		Operation.
	@param e		Operand. *)
and gen_unop info t op e prfx =
	match op with
	| Irg.NOT		-> Printf.fprintf info.out "!"; gen_expr info e prfx
	| Irg.BIN_NOT	-> mask info t (fun _ -> Printf.fprintf info.out "~"; gen_expr info e prfx)
	| Irg.NEG		-> mask info t (fun _ -> Printf.fprintf info.out "-"; gen_expr info e prfx)


(** Generate code for binary operation.
	@param info		Generation information.
	@param t		Type of result.
	@param op		Operation.
	@param op1		First operand
	@param e2		Second operand. *)
and gen_binop info t op e1 e2 prfx =
	let out pref sep suff =
		output_string info.out pref;
		gen_expr info e1 prfx;
		output_string info.out sep;
		gen_expr info e2 prfx;
		output_string info.out suff in
	match op with
	| Irg.ADD 		-> mask info t (fun _ -> out "(" " + " ")")
	| Irg.SUB 		-> mask info t (fun _ -> out "(" " - " ")")
	| Irg.MUL 		-> mask info t (fun _ -> out "(" " * " ")")
	| Irg.DIV 		-> mask info t (fun _ -> out "(" " / " ")")
	| Irg.MOD 		-> mask info t (fun _ -> out "(" " % " ")")
	| Irg.EXP		->
		mask info t (fun _ -> out (Printf.sprintf "%s_exp%s(" info.proc (type_to_mem(convert_type t))) ", " ")")
	| Irg.LSHIFT	->  mask info t (fun _ -> out "(" " << " ")")
	| Irg.RSHIFT	-> out "(" " >> " ")"
	| Irg.LROTATE	->
		let size = Sem.get_type_length (Sem.get_type_expr e1) in
		out  (Printf.sprintf "%s_rotate_left%s(%d, " info.proc (type_to_mem(convert_type t)) size) ", " ")"(* (Printf.sprintf ", %d)" (ctype_size (convert_type t))) *)
	| Irg.RROTATE	->
		let size = Sem.get_type_length (Sem.get_type_expr e1) in
		out  (Printf.sprintf "%s_rotate_right%s(%d, " info.proc (type_to_mem(convert_type t)) size) ", " ")"(* (Printf.sprintf ", %d)" (ctype_size (convert_type t))) *)
	| Irg.LT		-> out "(" " < " ")"
	| Irg.GT		-> out "(" " > " ")"
	| Irg.LE		-> out "(" " <= " ")"
	| Irg.GE		-> out "(" " >= " ")"
	| Irg.EQ		-> out "(" " == " ")"
	| Irg.NE		-> out "(" " != " ")"
	| Irg.AND		-> out "(" " && " ")"
	| Irg.OR		-> out "(" " || " ")"
	| Irg.BIN_AND	-> exts info t (fun _ -> out "(" " & " ")")
	| Irg.BIN_OR	-> exts info t (fun _ -> out "(" " | " ")")
	| Irg.BIN_XOR	-> exts info t (fun _ -> out "(" " ^ " ")")
	| Irg.CONCAT	->
		let l1 = Sem.get_type_length (Sem.get_type_expr e1) in
		let l2 = Sem.get_type_length (Sem.get_type_expr e2) in
		Printf.fprintf info.out "%s_concat%s(" info.proc (type_to_mem(convert_type t));
		out "" ", " ", ";
		Printf.fprintf info.out "%d, %d)" l1 l2


(** Generate code for coercition.
	@param typ	Type to coerce to.
	@param expr	Expression to coerce. *)
and coerce info t1 expr parent prfx =
	let asis _ = gen_expr info expr prfx in

	(* simple equality *)
	let t2 = Sem.get_type_expr expr in
	let t1c = convert_type t1 in
	let t2c = convert_type t2 in
	if t1 = t2 or t1c = t2c then asis () else

	(* generation *)
	let eq0 f _ =
		output_string info.out "((";
		f ();
		output_string info.out ") == 0)" in
	let cast f _ =
		Printf.fprintf info.out "((%s)(" (type_to_string t1c);
		f ();
		output_string info.out "))" in
	let mask n f _ =
		output_string info.out "((";
		f ();
		Printf.fprintf info.out ") &  ((1 << %d) - 1))" n in
	let to_range lo up f _ =
		Printf.fprintf info.out "%s_check_range(" info.proc;
		f ();
		Printf.fprintf info.out ", %ld, %ld)" lo up in
	let to_enum vals f _ =
		Printf.fprintf info.out "%s_check_enum(" info.proc;
		f ();
		Printf.fprintf info.out ", %d)" (List.length vals) in

	(* special cases *)
	match (t1, t2) with

	(* conversion to bool *)
	| Irg.BOOL, Irg.CARD _
	| Irg.BOOL, Irg.INT _
	| Irg.BOOL, Irg.RANGE _
	| Irg.BOOL, Irg.ENUM _
	| Irg.BOOL, Irg.FLOAT _ -> eq0 asis ()

	(* conversion to card *)
	| Irg.CARD n, Irg.CARD m when n < m -> mask n asis ()
	| Irg.CARD n, Irg.CARD m when n > m -> cast asis ()
	| Irg.CARD n, Irg.INT m when n = m -> cast asis ()
	| Irg.CARD n, Irg.INT m when n < m -> cast (mask n asis) ()
	| Irg.CARD n, Irg.INT m when n > m -> cast (mask m asis) ()
	| Irg.CARD _, Irg.BOOL -> cast asis ()
	| Irg.CARD _, Irg.RANGE _
	| Irg.CARD _, Irg.ENUM _ -> cast asis ()
	| Irg.CARD _, Irg.FLOAT (23, 9) -> cast asis ()

	(* conversion to int *)
	| Irg.INT n, Irg.INT _
	| Irg.INT n, Irg.CARD _
	| Irg.INT n, Irg.BOOL
	| Irg.INT n, Irg.RANGE _
	| Irg.INT n, Irg.ENUM _
	| Irg.INT n, Irg.FLOAT _ -> cast asis ()

	(* conversion to float *)
	| Irg.FLOAT (23, 9), _
	| Irg.FLOAT (52, 12), _ -> cast asis ()

	(* conversion to range *)
	| Irg.RANGE (lo, up), _ -> to_range lo up asis ()

	(* conversion to enum *)
	| Irg.ENUM vals, _ -> to_enum vals asis ()

	| _ ->
		raise (PreError (fun out ->
			output_string out "unsupported coercition for ";
			Irg.output_expr out expr;
			output_string out " from ";
			Irg.output_type_expr out t2;
			output_string out " to ";
			Irg.output_type_expr out t1))


(** Generate a cast expression.
	@param info		Generation information.
	@param size		Size in bits.
	@param expr		Expression to cast. *)
and gen_cast info typ expr prfx =
	let etyp = Sem.get_type_expr expr in

	let do_cast _ =
		Printf.fprintf info.out "((%s)(" (type_to_string (convert_type typ));
		gen_expr info expr prfx;
		output_string info.out "))" in

	match typ, etyp with
	| Irg.FLOAT _, Irg.FLOAT _-> do_cast ()
	| Irg.FLOAT _, _
	| _, Irg.FLOAT _ ->
		let etyp = convert_type etyp in
		let ctyp = convert_type typ in
		Printf.fprintf info.out "%s_cast_%sto%s(" info.proc (type_to_mem etyp) (type_to_mem ctyp);
		gen_expr info expr prfx;
		output_char info.out ')'
	| Irg.INT n, _
	| Irg.CARD n, _ -> do_cast ()
	| _ -> failwith "unsupported CAST"


(** Generate code for a bit field.
	@param info		Generation information.
	@param typ		Result type.
	@param expr		Accessed expression.
	@param lo		Lower bit index.
	@param hi		Higher bit index. *)
and gen_bitfield info typ expr lo up prfx =

	let rec is_const e =
		match e with
		| Irg.CONST(_, _) -> true
		| Irg.ELINE(_, _, ee) -> is_const ee
		| Irg.CAST(_, expr) -> is_const expr
		| _ -> false in

	let bitorder_to_int bo =
		match bo with
		| LOWERMOST -> 0
		| UPPERMOST -> 1 in

	(* stop printing after the expr and the bounds, a closing parens or the bit_order param has to be added apart *)
	let output_field_common_C_code sufx a b arg3 b_o =
		Printf.fprintf info.out "%s_field%s%s("
			info.proc
			(type_to_mem (convert_type (Sem.get_type_expr expr)))
			sufx;
		gen_expr info expr prfx;
		output_string info.out ", ";
		gen_expr info a prfx;
		output_string info.out ", ";
		gen_expr info b prfx;
		if arg3 then Printf.fprintf info.out ", %s )" (string_of_int b_o)
		else output_string info.out ")" in

	let output_bit b =
		Printf.fprintf info.out "%s_bit%s(" info.proc (type_to_mem (convert_type (Sem.get_type_expr expr)));
		gen_expr info expr prfx;
		output_string info.out ", ";
		gen_expr info b prfx;
		output_char info.out ')' in

	try

		(* check constant bounds *)
		let up, lo = if info.bito = UPPERMOST then lo, up else up, lo in
		let uc = Sem.to_int32 (Sem.eval_const up) in
		let lc = Sem.to_int32 (Sem.eval_const lo) in
		let cmp = Int32.compare uc lc in

		(* generate ad-hoc field access *)
		if cmp = 0 then output_bit up else
		let inv, up, lo =
			if (Int32.compare uc lc) >= 0 then "", up, lo else "_inverted", lo, up in
		output_field_common_C_code inv up lo false 0

	(* no constant bounds *)
	with Sem.SemError _ ->
		output_field_common_C_code "_generic" lo up true (bitorder_to_int info.bito)


(** Test if a statement is composed of multiple statements.
	@param stat		Statement to test.
	@return			True if multiple, false else. *)
let rec multiple_stats stat =
	match stat with
	| Irg.NOP
	| Irg.SET _
	| Irg.CANON_STAT _
	| Irg.ERROR _
	| Irg.SWITCH_STAT _
	| Irg.SETSPE _
	| Irg.INLINE _ -> false
	| Irg.EVAL _
	| Irg.EVALIND _
	| Irg.SEQ _
	| Irg.IF_STAT _ -> true
	| Irg.LINE (_, _, stat) -> multiple_stats stat


(** Generate a prepared statement.
	@param info		Generation information.
	@param stat		Statement to generate. *)
let rec gen_stat info stat =
	trace "gen_stat 1";
	let out = output_string info.out in
	let line f =
		for i = 1 to info.indent do
			out "\t"
		done;
		f();
		out "\n" in
	let indented f =
		info.indent <- info.indent + 1;
		f ();
		info.indent <- info.indent - 1 in

	let iter_args args =
		ignore(List.fold_left
			(fun first arg ->
				if not first then out ", ";
				ignore (gen_expr info arg true); false)
			true
			args) in

	let loc_to_expr t id idx =
		if idx = Irg.NONE then Irg.REF id
		else Irg.ITEMOF(t, id, idx) in
	
	match stat with
	| Irg.NOP -> ()

	| Irg.SEQ (s1, s2) ->
		gen_stat info s1; gen_stat info s2

	| Irg.SET (Irg.LOC_REF(typ, id, idx, lo, up), expr) ->
		line (fun _ ->
			match Irg.get_symbol id with
			| Irg.VAR (_, _, t)
			| Irg.REG (_, _, t, _)
			| Irg.PARAM (_, Irg.TYPE_EXPR(t))
			| Irg.CANON_DEF (_, _, t, _) ->
				gen_ref info id true;
				if idx <> Irg.NONE then
					(out "["; gen_expr info idx true; out "]");
				out " = ";
				gen_expr info (set_field info typ id idx lo up expr) true;
				out ";"
			| Irg.MEM (_, _, t, _) ->
				out (Printf.sprintf "%s_mem_write%s(" info.proc
					(type_to_mem (convert_type typ)));
				out (state_macro info (unaliased_mem_name id) true);
				out ", ";
				gen_expr info idx true;
				out ", ";
				gen_expr info (set_field info typ id Irg.NONE lo up expr) true;
				out ");"
			| s ->
				Printf.printf "==> %s\n" id;
				Irg.print_spec s;
				failwith "gen stat 1")

	| Irg.CANON_STAT (name, args) ->
		line (fun _ ->
			out name;
			out "(";
			iter_args args;
			out ");")

	| Irg.ERROR msg ->
		line (fun _ ->
			Printf.fprintf info.out "%s_error(state, inst, \"%s\");"
				info.proc
				(cstring msg))

	| Irg.IF_STAT (cond, tpart, epart) ->
		let tmult = multiple_stats tpart in
		let emult = multiple_stats epart in
		line (fun _ ->
			out "if(";
			gen_expr info cond true;
			out (if tmult then ") {" else ")"));
		indented (fun _ -> if (is_nop tpart) then line (fun _ -> out ";") else gen_stat info tpart);
		if tmult then line (fun _ -> out "}");
		if not (is_nop epart) then
			begin
				line (fun _ -> out (if emult then "else {" else "else"));
				indented (fun _ -> gen_stat info epart);
				if emult then line (fun _ -> out "}")
			end

	| Irg.SWITCH_STAT (cond, cases, def) ->
		line (fun _ ->
			out "switch(";
			gen_expr info cond true;
			out ") {");
		List.iter
			(fun (case, stat) ->
				line (fun _ ->
					out "case ";
					gen_expr info case true;
					out ":");
				indented (fun _ -> gen_stat info stat; line (fun _ -> out "break;"))
				(*line (fun _ -> out "break;")*))
			cases;
		if def <> Irg.NOP then
			begin
				line (fun _ -> out "default:");
				indented (fun _ -> gen_stat info def)
			end;
		line (fun _ -> out "}")

	| Irg.LINE (_, _, stat) ->
		gen_stat info stat

	| Irg.EVAL name ->
		gen_call info name

	| Irg.INLINE s ->
		line (fun _ -> out s)

	| Irg.SET _
	| Irg.EVALIND _
	| Irg.SETSPE _ ->
		failwith "must have been removed"

(** Generate the code for setting a field.
	@param typ	Type of set state item.
	@param id	Identifier of state item.
	@param idx	Index for an array state item.
	@param lo	Lower bound.
	@param up	Upper bound.
	@param expr	Value to set. *)
and set_field info typ id idx lo up expr =

	let transform_expr sufx a b arg3 b_o =
		let e_bo = Irg.CONST(Irg.CARD(32), Irg.CARD_CONST(Int32.of_int b_o)) in
		let e = if idx = Irg.NONE then Irg.REF id else Irg.ITEMOF(typ, id, idx) in
		if lo = Irg.NONE then expr
		else

			Irg.CANON_EXPR (
				typ,
				Printf.sprintf "%s_set_field%s%s"
					info.proc (type_to_mem (convert_type (Sem.get_type_expr e))) sufx,
				if arg3 then
				[	if idx = Irg.NONE then Irg.REF id
					else Irg.ITEMOF (typ, id, idx);
					expr;
					a;
					b;
					e_bo
				]
				else
				[	if idx = Irg.NONE then Irg.REF id
					else Irg.ITEMOF (typ, id, idx);
					expr;
					a;
					b
				]
			)
	in

	try

		(* check constant bounds *)
		(*Printf.printf "DEBUG: lo=";
		Irg.print_expr lo;
		Printf.printf ", up=";
		Irg.print_expr up;
		Printf.printf "\n";*)
		let up, lo = if info.bito = UPPERMOST then lo, up else up, lo in
		(*let up, lo = if info.bito = UPPERMOST then up, lo else lo, up in*)
		let uc = Sem.to_int32 (Sem.eval_const up) in
		let lc = Sem.to_int32 (Sem.eval_const lo) in
		(*Printf.printf "DEBUG: lo=%ld up=%ld\n" uc lc;*)
		let inv, up, lo = 
			if (Int32.compare uc lc) >= 0 then "", up, lo
			else "_inverted", lo, up in

		(* generate ad-hoc field set *)
		transform_expr inv up lo false 0

	(* no constant bounds *)
	with Sem.SemError _ ->
		transform_expr "_generic" lo up true (if info.bito = LOWERMOST then 0 else 1);


and gen_call info name =

	(* recursive call ? *)
	if 	List.mem_assoc name info.calls then
		(Printf.fprintf info.out "goto %s;\n" (List.assoc name info.calls))

	(* normal call *)
	else
		begin
			let stat = StringHashtbl.find info.attrs name in
			let before = info.calls in
			if List.mem name info.recs then
				begin
					let lab = new_label info in
					Printf.fprintf info.out "%s:\n" lab;
					info.calls <- (name, lab)::info.calls
				end;
			gen_stat info stat;
			info.calls <- before
		end



(** Get the list if recursives attribute starting from the given one.
	@param info		Generation information.
	@param name		Name of the first attribute.
	@return			List of recursive attributes. *)
let find_recursives info name =

	let rec look_attr name stack recs =
		if List.mem name stack then
			if List.mem name recs then recs
			else name::recs
		else
			let stack = name::stack in

			let rec look_stat stat recs =
				match stat with
				| Irg.NOP -> recs
				| Irg.SEQ (s1, s2) -> look_stat s1 (look_stat s2 recs)
				| Irg.EVAL name -> look_attr name stack recs
				| Irg.EVALIND _ -> error "unsupported form"
				| Irg.SET _ -> recs
				| Irg.CANON_STAT _ -> recs
				| Irg.ERROR _ -> recs
				| Irg.IF_STAT (_, s1, s2) -> look_stat s1 (look_stat s2 recs)
				| Irg.SWITCH_STAT (_, cases, def) ->
					look_stat def (List.fold_left
						(fun recs (_, s) -> look_stat s recs)
						recs
						cases)
				| Irg.SETSPE (loc, expr) -> recs
				| Irg.LINE (file, line, s) -> locate_error file line (fun (s, r) -> look_stat s r) (s, recs)
				| Irg.INLINE _ -> recs in

			look_stat (get_stat_attr name) recs in
	
	info.recs <- look_attr name [] []


(** generate the instruction responsibe for the incrementation of PCs,
we return an Irg.STAT which has to be transformed, useful to resolve alias
	@param	info		Generation information (PCs name)
	@return			an Irg.STAT object representing the sequence of the desired instructions *)
let gen_pc_increment info =
	(*let size = Irg.CONST (Irg.CARD(32), Irg.CARD_CONST (Int32.of_int 4 (Fetch.get_instruction_length info.inst))) in *)
	let i_name = String.uppercase info.iname in
	let real_name = if String.compare i_name "" == 0 then "UNKNOWN" else i_name in
	let size = Irg.EINLINE (Printf.sprintf "((%s_%s___ISIZE + 7) >> 3)" (String.uppercase info.proc) real_name) in
	let ppc_stat =
		if info.ppc_name = "" then
			Irg.NOP
		else
			(* PPC = PC *)
			(* cannot retrieve the type easily, not needed for generation *)
			Irg.SET(Irg.LOC_REF(Irg.NO_TYPE, String.uppercase info.ppc_name, Irg.NONE, Irg.NONE, Irg.NONE), Irg.REF(String.uppercase info.pc_name))
	in
	let npc_stat =
		if info.npc_name = "" then
			Irg.NOP
		else
			(* NPC = NPC + (size of current instruction) *)
			(* cannot retrieve the type easily, not needed for generation *)
			Irg.SET(Irg.LOC_REF(Irg.NO_TYPE, String.uppercase info.npc_name, Irg.NONE, Irg.NONE, Irg.NONE),
				Irg.BINOP(Irg.NO_TYPE, Irg.ADD, Irg.REF(String .uppercase info.npc_name), size))
	in
	let pc_stat =
		if info.npc_name = "" then
			(* PC = PC + (size of current instruction) *)
			Irg.SET(Irg.LOC_REF(Irg.NO_TYPE, String.uppercase info.pc_name, Irg.NONE, Irg.NONE, Irg.NONE),
				Irg.BINOP(Irg.NO_TYPE, Irg.ADD, Irg.REF(String.uppercase info.pc_name), size))
		else
			(* PC = NPC *)
			Irg.SET(Irg.LOC_REF(Irg.NO_TYPE, String.uppercase info.pc_name, Irg.NONE, Irg.NONE, Irg.NONE), Irg.REF(String.uppercase info.npc_name))
	in
	Irg.SEQ(ppc_stat, Irg.SEQ(pc_stat, npc_stat))



(** Generate an action.
	@param info		Generation information.
	@param name		Name of the attribute. *)
let gen_action info name =
	info.indent <- 1;
	
	(* prepare statements *)
	find_recursives info name;
	prepare_call info name;

	(* generate the code *)
	declare_temps info;

	(* PCs incrementation *)
	(* !!DEBUG!! deactivated for the moment as it gives problems with leon *)
	(*gen_stat info (gen_pc_increment info);*)

	(* generate the code *)
	gen_call info name;

	(* cleanup at end *)
	cleanup_temps info;
	StringHashtbl.clear info.attrs


(** Return the code corresponding to what is found in the op init.
	@param info		Generation information.*)
let get_init_code _ =
	let init_sp = Irg.get_symbol "init"
	in
	match init_sp with
	| Irg.UNDEF ->
		Irg.NOP
	| Irg.AND_OP(n, p, al) ->
		(match Iter.get_attr init_sp "action" with
		Iter.STAT(s) ->
			s
		| _ ->
			failwith "shouldn't happen ! (toc.ml::get_init_code::init_action)"
		)
	| _ ->
		failwith "bad init, nML symbol init must be an AND OP only"
