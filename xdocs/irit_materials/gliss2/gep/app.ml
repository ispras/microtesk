(*
 * $Id: app.ml,v 1.16 2009/11/26 09:01:16 casse Exp $
 * Copyright (c) 2009-10, IRIT - UPS <casse@irit.fr>
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

module OrderedType =
struct
	type t = Toc.c_type
	let compare s1 s2 = if s1 = s2 then 0 else if s1 < s2 then (-1) else 1
end

module TypeSet = Set.Make(OrderedType);;

(** Gather information useful for the generation. *)
type maker_t = {
	mutable get_params: Iter.inst -> int -> string -> Irg.type_expr -> Templater.dict_t -> Templater.dict_t;
	mutable get_instruction: Iter.inst -> Templater.dict_t -> Templater.dict_t;
	mutable get_instruction_set: Iter.inst list -> Templater.dict_t -> Templater.dict_t;
	mutable get_register: Irg.spec -> Templater.dict_t -> Templater.dict_t
}



(** Build the given directory.
	@param path			Path of the directory.
	@raise Sys_error	If there is an error. *)
let rec makedir path =
	if not (Sys.file_exists path) then
		try
			(try
				let p = String.rindex path '/' in
				makedir (String.sub path 0 p)
			with Not_found -> ());
			Printf.printf "creating \"%s\"\n" path;
			Unix.mkdir path 0o740
		with Unix.Unix_error (code, _, _) ->
			raise (Sys_error (Printf.sprintf "cannot create \"%s\": %s" path (Unix.error_message code)))
	else
		let stats = Unix.stat path in
		if stats.Unix.st_kind <> Unix.S_DIR
		then raise (Sys_error (Printf.sprintf "cannot create directory \"%s\": file in the middle" path))



(* regular expressions *)
let lower_re = Str.regexp "gliss_"
let upper_re = Str.regexp "GLISS_"
let path_re = Str.regexp "gliss/"

(** Replace the "gliss" and "GLISS" words in the input file
	to create the output file.
	@param info		Generation information.
	@param in_file	Input file.
	@param out_file	Output file. *)
let replace_gliss info in_file out_file =
	let in_stream = open_in in_file in
	let out_stream = open_out out_file in
	let lower = info.Toc.proc ^ "_" in
	let upper = String.uppercase lower in
	let rec trans _ =
		let line = input_line in_stream in
		output_string out_stream
			(Str.global_replace path_re (info.Toc.proc ^ "/")
			(Str.global_replace upper_re upper
			(Str.global_replace lower_re lower line)));
		output_char out_stream '\n';
		trans () in
	try
		trans ()
	with End_of_file ->
		close_in in_stream;
		close_out out_stream


(* Test if memory or register attributes contains ALIAS.
	@param attrs	Attributes to test.
	@return			True if it contains "alias", false else. *)
let rec contains_alias attrs =
	match attrs with
	  [] -> false
	| (Irg.ATTR_LOC ("alias", _))::_ -> true
	| _::tl -> contains_alias tl


(** Format date (in seconds) and return a stirng.
	@param date	Date to format.
	@return		Date formatted as a string. *)
let format_date date =
	let tm = Unix.localtime date in
	Printf.sprintf "%0d/%02d/%02d %02d:%02d:%02d"
		tm.Unix.tm_year tm.Unix.tm_mon tm.Unix.tm_mday
		tm.Unix.tm_hour tm.Unix.tm_min tm.Unix.tm_sec


(** Evaluates an attribute by its name inside an instruction.
	@param inst		Current instruction.
	@param out		Output channel.
	@param arg		Argument: ID or ID:DEFAULT. *)
let eval_attr info inst out arg =
	let id, def =
		try
			let p = String.index arg ':' in
			(String.sub arg 0 p), (String.sub arg (p + 1) ((String.length arg) - p - 1))
		with Not_found -> arg, "" in
	try
		let params = Iter.get_params inst in
		Irg.param_stack params;
		(match Iter.get_attr inst id with
		
		| Iter.EXPR e ->
			let (s, e) = Toc.prepare_expr info Irg.NOP e in
			Toc.declare_temps info;
			Toc.gen_stat info s;
			Toc.gen_expr info e true;
			output_string info.Toc.out "\n";
			
		| Iter.STAT s ->
			let s = Toc.prepare_stat info s in
			Toc.declare_temps info;
			Toc.gen_stat info s);
			
		Toc.cleanup_temps info;
		Irg.param_unstack params
	with Not_found -> output_string out def


(** Test if an attribute is defined.
	@param inst		Current instruction.
	@param id		Attribute identifier. *)
let defined_attr inst id =
	try
		ignore (Iter.get_attr inst id);
		true
	with Not_found -> false


(** Shortcut for templater text output from a string.
	f	Function to get string from. *)
let out f = Templater.TEXT (fun out -> output_string out (f ()))


let get_params maker inst f dict =

	let get_type t =
		match t with
		  Irg.TYPE_EXPR t -> t
		| Irg.TYPE_ID n ->
			(match (Irg.get_symbol n) with
			  Irg.TYPE (_, t) -> t
			| _ -> Irg.NO_TYPE) in

	ignore (List.fold_left
		(fun (i: int) (n, t) ->
			let t = get_type t in
			(if t <> Irg.NO_TYPE then
				f (maker.get_params inst i n t (
					("PARAM", out (fun _ -> n)) ::
					("INDEX", out (fun _ -> string_of_int i)) ::
					("TYPE", out (fun _ -> Toc.type_to_string (Toc.convert_type t))) ::
					("PARAM_TYPE", out (fun _ -> String.uppercase (Toc.type_to_field (Toc.convert_type t)))) ::
					("param_type", out (fun _ -> Toc.type_to_field (Toc.convert_type t))) ::
					dict)));
			i + 1)
		0
		(Iter.get_params inst))

let get_instruction info maker f dict _ i =
	let gen_predecode out =
		try
			let _ = Iter.get_attr i "predecode" in
			info.Toc.out <- out;
			Toc.set_inst info i;
			Toc.gen_action info "predecode"
		with Not_found -> () in

	f (maker.get_instruction  i
		(("IDENT", out (fun _ -> String.uppercase (Iter.get_name i))) ::
		("ident", out (fun _ -> Iter.get_name i)) ::
		("ICODE", Templater.TEXT (fun out -> Printf.fprintf out "%d" (Iter.get_id i))) ::
		("params", Templater.COLL (get_params maker i)) ::
		("has_param", Templater.BOOL (fun _ -> (List.length (Iter.get_params  i)) > 0)) ::
		("num_params", Templater.TEXT (fun out -> Printf.fprintf out "%d" (List.length (Iter.get_params i)))) ::
		("is_inst_branch", Templater.BOOL (fun _ -> Iter.is_branch_instr i )) ::
		("attr", Templater.FUN (eval_attr info i)) ::
		("predecode", Templater.TEXT gen_predecode) ::
		dict))


(** Get the nth first instructions defined by nb_inst
	Only if a instruction profile is loaded i.e : (Iter.instr_stats <> [])
*)
let get_ninstruction info maker f dict nb_inst cpt i =
	if (Iter.instr_stats = ref [])
	then (prerr_string "WARNING a profiling option is being used without a loaded '.profile'\n";cpt)
	else
		if (cpt < nb_inst)
		then let _ = get_instruction info maker f dict () i in cpt+1
		else cpt


exception BadCSize

let get_instruction_set maker f dict i_set =
	let min_size =
		List.fold_left
			(fun min inst ->
				let size = Iter.get_instruction_length inst
				in if size < min then size else min)
			1024 i_set
	in
	let max_size =
		List.fold_left
			(fun max inst ->
				let size = Iter.get_instruction_length inst
				in if size > max then size else max)
			0 i_set
	in
	let is_RISC =
		if min_size == max_size then
			(match min_size with
			| 8
			| 16
			| 32
			| 64 -> true
			| _ -> false
			)
		else
			false
	in
	let get_C_size n =
		match n with
		| _ when n > 0 && n <= 8 -> 8
		| _ when n > 8 && n <= 16 -> 16
		| _ when n > 16 && n <= 32 -> 32
		| _ when n > 32 && n <= 64 -> 64
		| _ -> raise BadCSize
	in
	let get_msb_mask n =
		try
			(match get_C_size min_size with
			| 8 -> "0x80"
			| 16 -> "0x8000"
			| 32 -> "0x80000000"
			| 64 -> "0x8000000000000000LL"
			| _ -> raise BadCSize)
		with
			BadCSize -> raise (Sys_error "template $(msb_mask_iset) should be used only with a RISC ISA")
	in
	let find_idx _ =
		let l_mem = List.map (fun x -> x = i_set) !Iter.multi_set in
		let rec aux l i =
			match l with
			| [] -> failwith "shouldn't happen (app.ml::get_instruction_set::find_idx)"
			| a::b -> if a then i else aux b (i + 1)
		in
		aux l_mem 0
	in f
	(maker.get_instruction_set i_set
		(("is_RISC_iset", Templater.BOOL (fun _ -> is_RISC)) ::
		("C_size_iset", Templater.TEXT (fun out -> Printf.fprintf out "%d" (try (get_C_size min_size) with BadCSize -> raise (Sys_error "template $(C_inst_size_iset) should be used only with a RISC ISA")))) ::
		(* return a mask for the most significant bit, size depends on the C size needed *)
		("msb_mask_iset", out (fun _ -> (get_msb_mask (min_size)))) ::
		(* iset select condition *)
		("select_iset", Templater.TEXT (fun out ->
			let info = Toc.info () in
			let spec_ = List.hd i_set in
			let select_attr =
				match Iter.get_attr spec_ "instruction_set_select" with
				| Iter.EXPR(e) -> e
				| _ -> failwith "(app.ml::get_instruction_set::$(iset_select)) attr instruction_set_select for op init must be an expr"
			in
			info.Toc.out <- out;
			info.Toc.inst <- spec_;
			info.Toc.iname <- "";
			(* stack params and attrs for the chosen instr *)
			match spec_ with
			| Irg.AND_OP(_, param_l, attr_l) ->
				Irg.param_stack param_l;
				Irg.attr_stack attr_l;
				(*Toc.gen_expr info select_attr true;*)
				Toc.gen_expr info (snd (Toc.prepare_expr info Irg.NOP select_attr)) true;
				Irg.param_unstack param_l;
				Irg.attr_unstack attr_l
			| _ -> failwith "(app.ml::get_instruction_set::$(iset_select)) shouldn't happen.")) ::
		(* index, 0 to n, as in !Iter.multi_set *)
		("idx", Templater.TEXT (fun out -> Printf.fprintf out "%d" (find_idx ()))) ::
		(* as described in nmp attr "instruction_set_name" *)
		("iset_name", Templater.TEXT (fun out ->
			let spec_ = List.hd i_set in
			let name_attr =
				match Iter.get_attr spec_ "instruction_set_name" with
				| Iter.EXPR(e) -> e
				| _ -> failwith "(app.ml::get_instruction_set::$(name)) attr instruction_set_name must be an expr"
			in
			match name_attr with
			(* name should be just a string *)
			| (Irg.CONST(Irg.STRING, Irg.STRING_CONST(n, _, _))) ->
				output_string out n
			| _ -> failwith "(app.ml::get_instruction_set::$(name)) attr instruction_set_name must be a const string")) ::
		dict))


let rec is_pc attrs =
	match attrs with
	| [] -> false
	| Irg.ATTR_EXPR ("pc", _) :: _ -> true
	| _ :: tl -> is_pc tl

let is_float t =
	match t with
	| Irg.FLOAT _ -> true
	| _ -> false

let rec reg_format id size attrs =
	match attrs with
	| [] -> if size > 1 then id ^ "%d" else id
	| (Irg.ATTR_EXPR ("fmt", e))::_ ->
		(try Sem.to_string e
		with Sem.SemError m -> Toc.error "bad \"fmt\" attribute: should evaluate to string")
	| _ :: tl -> reg_format id size tl


(** Generate the code for accessing a register.
let gen_reg_access name size type attrs out make attr =
	@param name		Name of the register.
	@param size		Size of the register bank.
	@param typ		Type of the register.
	@param attrs	List of attributes.
	@param out		Output channel to use.
	@param make		Maker if the attribute is not available.
	@param attr		Name of the attribute. *)
let gen_reg_access name size typ attrs out attr make =
	let s = Sem.get_type_length typ in
	let v =
		if not (is_float typ)
		then if s <= 32 then "I" else "L"
		else if s <= 32 then "F" else "D" in
	let attrs =
		if Irg.attr_defined attr attrs then attrs else
		(Irg.ATTR_STAT (attr, make v))::attrs in
	let info =  Toc.info () in
	info.Toc.out <- out;
	info.Toc.inst <- (Irg.AND_OP ("instruction", [], attrs));
	info.Toc.iname <- "";
	Irg.attr_stack attrs;
	Toc.gen_action info attr;
	Irg.attr_unstack attrs


(** Make a canonic variable access.
	@param t	Type of variable.
	@param v	Name of the variable. *)
let make_canon_var t v =
	Irg.CONST (t, Irg.STRING_CONST(v, true, t))
	

(** Generate the setter of a register value for a debugger.
	@param name		Name of the register.
	@param size		Size of the register bank.
	@param typ		Type of the register.
	@param attrs	List of attributes.
	@param out		Output channel to use. *)
let gen_reg_setter name size typ attrs out =
	gen_reg_access name size typ attrs out "set"
		(fun v -> Irg.SET(
			Irg.LOC_REF (typ, name, (if size == 1 then Irg.NONE else make_canon_var typ "GLISS_IDX"), Irg.NONE, Irg.NONE),
			make_canon_var typ (Printf.sprintf "GLISS_%s" v)
		))


(** Generate the getter of a register value for a debugger.
	@param name		Name of the register.
	@param size		Size of the register bank.
	@param typ		Type of the register.
	@param attrs	List of attributes.
	@param out		Output channel to use. *)
let gen_reg_getter name size typ attrs out =
	gen_reg_access name size typ attrs out "get"
		(fun v -> Irg.CANON_STAT(
			Printf.sprintf "GLISS_GET_%s" v,
			[ if size == 1 then Irg.REF name else Irg.ITEMOF (typ, name, make_canon_var typ "GLISS_IDX")]))


(** Get the label of a register bank.
	@param name		Name of the register banks.
	@param attrs	Attributes of the register bank.
	@return			Label of the register bank. *)
let get_label name attrs =
	let l = Irg.attr_expr "label" attrs Irg.NONE in
	if l = Irg.NONE then name else
	try Sem.to_string l
	with Sem.SemError _ -> Toc.error "\"label\" attribute should be a string constant !"


let get_register id f dict maker _ sym =
	match sym with
	  Irg.REG (name, size, t, attrs) ->
		let is_debug _ = 
			if Irg.is_defined "gliss_debug_only"
			then Irg.attr_defined "debug" attrs
			else not (contains_alias attrs) in
		incr id; f (maker.get_register sym (
			("type", out (fun _ -> Toc.type_to_string (Toc.convert_type t))) ::
			("name", out (fun _ -> name)) ::
			("NAME", out (fun _ -> String.uppercase name)) ::
			("aliased", Templater.BOOL (fun _ -> contains_alias attrs)) ::
			("is_debug", Templater.BOOL (fun _ -> is_debug ())) ::
			("array", Templater.BOOL (fun _ -> size > 1)) ::
			("size", out (fun _ -> string_of_int size)) ::
			("id", out (fun _ -> string_of_int !id)) ::
			("type_size", out (fun _ -> string_of_int (Sem.get_type_length t))) ::
			("is_pc", Templater.BOOL (fun _ -> is_pc attrs)) ::
			("is_float", Templater.BOOL (fun _ -> is_float t)) ::
			("format", out (fun _ -> "\"" ^ (reg_format name size attrs) ^ "\"")) ::
			("printf_format", out (fun _ -> Toc.type_to_printf_format (Toc.convert_type t))) ::
			("get", Templater.TEXT (fun out -> gen_reg_getter name size t attrs out)) ::
			("set", Templater.TEXT (fun out -> gen_reg_setter name size t attrs out)) ::
			("label", out (fun _ -> get_label name attrs)) ::
			dict))	(* make_array size*)
	| _ -> ()

let get_value f dict t =
	f (
		("name", out (fun _ -> Toc.type_to_field t)) ::
		("type", out (fun _ -> Toc.type_to_string t)) ::
		dict
	)

let get_param f dict t =
	f (
		("NAME", out (fun _ -> String.uppercase (Toc.type_to_field t))) ::
		dict
	)

let get_memory f dict key sym =
	match sym with
	 | Irg.MEM (name, size, Irg.CARD(8), attrs)
	 (* you can find int(8) memories in some description (carcore, hcs12) *)
	 | Irg.MEM (name, size, Irg.INT(8), attrs) ->
	  	f (
			("NAME", out (fun _ -> String.uppercase name)) ::
			("name", out (fun _ -> name)) ::
			("aliased", Templater.BOOL (fun _ -> contains_alias attrs)) ::
			dict
		)
	| _ -> ()


(** Manage the list of exceptions.
	@param f	Function to iterate on.
	@param dict	Current dictionary. *)
let list_exceptions f dict =

	let is_irq attrs =
		if Irg.attr_defined "is_irq" attrs then "1" else "0" in

	let gen_action attrs out =
		let info =  Toc.info () in
		info.Toc.out <- out;
		info.Toc.inst <- (Irg.AND_OP ("exception", [], attrs));
		info.Toc.iname <- "";
		Irg.attr_stack attrs;
		Toc.gen_action info "action";
		Irg.attr_unstack attrs in

	let gen exn attrs =
		f (
			("name", out (fun _ -> exn)) ::
			("is_irq", out (fun _ -> is_irq attrs)) ::
			("action", Templater.TEXT (gen_action attrs)) ::
			dict
		) in

	let rec process exn =
		try
			(match Irg.get_symbol exn with
			| Irg.OR_OP (_, exns) -> List.iter process exns
			| Irg.AND_OP (_, _, attrs) -> gen exn attrs
			| _ -> Toc.error (Printf.sprintf "%s exception should an AND or an OR operation" exn))
		with Irg.Symbol_not_found n -> Toc.error (Printf.sprintf "exception %s is undefined" exn) in

	try
		(match Irg.get_symbol "exceptions" with
		| Irg.OR_OP (_, exns) -> List.iter process exns
		| _ -> ())
	with Irg.Symbol_not_found _ -> ()



let maker _ = {
	get_params = (fun _ _ _ _ dict -> dict);
	get_instruction = (fun _ dict -> dict);
	get_instruction_set = (fun _ dict -> dict);
	get_register = (fun _ dict -> dict)
}

let profiled_switch_size = ref 0


(** Traverse all debug registers (see documentation for more details).
	@param maker	Current maker.
	@param f		Function to call with each registers.
	@param dict		Current dictionary. *)
let debug_registers maker f dict =
	let id = ref 0 in

	let process n r = get_register id f dict maker n r in
	
	let process_only k r =
		match r with
		| Irg.REG(_, _, _, attrs) when Irg.attr_defined "debug" attrs -> process k r
		| _ -> () in

	if Irg.is_defined "gliss_debug_only"
	then Irg.StringHashtbl.iter process_only Irg.syms
	else Irg.StringHashtbl.iter process Irg.syms
	
	
(*fun f dict -> reg_id := 0; Irg.StringHashtbl.iter (get_register reg_id f dict maker) Irg.syms)*)

(**	Build the basic environment for GEP generation.
	@param info		Generation environment.
	@param maker	Maker functions (as produced by maker constructor).
*)
let make_env info maker =
	let reg_id = ref 0 in

	let param_types =
		let collect_field set (name, t) =
			match t with
			  Irg.TYPE_EXPR t -> TypeSet.add (Toc.convert_type t) set
			| Irg.TYPE_ID n ->
				(match (Irg.get_symbol n) with
				  Irg.TYPE (_, t) -> TypeSet.add (Toc.convert_type t) set
				| _ -> set) in

		let collect_fields set params =
			List.fold_left collect_field set params in
		Iter.iter (fun set i -> collect_fields set (Iter.get_params i)) TypeSet.empty in

	("instructions", Templater.COLL (fun f dict -> Iter.iter (get_instruction info maker f dict) ())) ::
	("mapped_instructions", Templater.COLL (fun f dict -> Iter.iter_ext (get_instruction info maker f dict) () true)) ::
	("profiled_instructions", Templater.COLL (fun f dict -> 
	  let _ = Iter.iter_ext (get_ninstruction info maker f dict (!profiled_switch_size)) 0 true in () )) ::
	("instruction_sets", Templater.COLL (fun f dict -> List.iter (get_instruction_set maker f dict) !Iter.multi_set )) ::
	("registers", Templater.COLL (fun f dict -> reg_id := 0; Irg.StringHashtbl.iter (get_register reg_id f dict maker) Irg.syms)) ::
	("values", Templater.COLL (fun f dict -> TypeSet.iter (get_value f dict) param_types)) ::
	("params", Templater.COLL (fun f dict -> TypeSet.iter (get_param f dict) param_types)) ::
	("memories", Templater.COLL (fun f dict -> Irg.StringHashtbl.iter (get_memory f dict) Irg.syms)) ::
	("date", out (fun _ -> format_date (Unix.time ()))) ::
	("proc", out (fun _ -> info.Toc.proc)) ::
	("PROC", out (fun _ -> String.uppercase info.Toc.proc)) ::
	("version", out (fun _ -> "GLISS V2.0 Copyright (c) 2009 IRIT - UPS")) ::
	("SOURCE_PATH", out (fun _ -> info.Toc.spath)) ::
	("INCLUDE_PATH", out (fun _ -> info.Toc.ipath)) ::
	("exceptions", Templater.COLL (fun f dict -> list_exceptions f dict)) ::
	[]


(* Activate a switch.
	@param name		Switch name.
	@param value	Value of the switch.
	@param dict		Dictionnary to activate in.
	@return			Result dictionnary. *)
let add_switch name value dict =
	(name, Templater.BOOL (fun _ -> value))::dict


(**
 * Load a NMP file and launch the given function on it
 * (and capture and display all exceptions).
 * @param file	File to process.
 * @param f		Function to work with definitions.
 * @param opti             set or unset instruction tree optimization (optirg) 
 *)
let process file f opti =

	(*let check _ =
		Irg.StringHashtbl.iter
			(fun n s -> match s with
				| Irg.ATTR _ -> failwith "attribute !"
				| _ -> ())
			Irg.syms in*)

	let find_irg_root_node _ =
		let is_defined id =
			try
				match Irg.get_symbol id with
				| _ -> true
			with Irg.Symbol_not_found _ -> false
		in
		if is_defined "multi" then
			"multi"
		else if is_defined "instruction" then
			"instruction"
		else
			raise (Sys_error "you must define a root for your instruction tree\n \"instruction\" for a single ISA\n \"multi\" for a proc with several ISA (like ARM/THUMB)")
	in
	try
		IrgUtil.load file;
		(*check ();*)
		if opti then
			Optirg.optimize (find_irg_root_node ());
		let info = Toc.info () in
		f info
	with
	  Parsing.Parse_error ->
		Lexer.display_error "syntax error"; exit 2
	| Irg.Error f ->
		output_string stderr "ERROR: ";
		f stderr;
		output_char stderr '\n';
		exit 2
	| Lexer.BadChar chr ->
		Lexer.display_error (Printf.sprintf "bad character '%c'" chr); exit 2
	| Sem.SemError msg ->
		Lexer.display_error (Printf.sprintf "%s" msg); exit 2
	| Irg.IrgError msg ->
		Lexer.display_error (Printf.sprintf "ERROR: %s" msg); exit 2
	| Irg.RedefinedSymbol s ->
		Lexer.display_error (Printf.sprintf "ERROR: redefined symbol \"%s\", firstly defined at %s" s (Irg.pos_of s)); exit 2
	| Irg.Symbol_not_found id ->
		Lexer.display_error (Printf.sprintf "can not find symbol \"%s\"" id); exit 2
	| Irg.RedefinedSymbol sym ->
		Lexer.display_error (Printf.sprintf "ERROR: redefined symbol \"%s\" (previous definition: %s)" sym (Irg.pos_of sym)); exit 2
	| Sem.SemErrorWithFun (msg, fn) ->
		Lexer.display_error (Printf.sprintf "semantics error : %s" msg);
		fn (); exit 2;
	| Toc.Error msg ->
		Printf.fprintf stderr "ERROR: %s\n" msg; exit 4
	| Toc.PreError f ->
		output_string stderr "ERROR: ";
		f stderr;
		output_char stderr '\n';
		exit 4
	| Toc.LocError (file, line, f) ->
		Printf.fprintf stderr "ERROR: %s:%d: " file line;
		f stderr;
		output_char stderr '\n';
		exit 1
	| Sys_error msg ->
		Printf.fprintf stderr "ERROR: %s\n" msg; exit 1
	| Unix.Unix_error (err, _, path) ->
		Printf.fprintf stderr "ERROR: %s on \"%s\"\n" (Unix.error_message err) path; exit 4


(** Find a source from "lib/"
	@param source		Looked source.
	@param paths		List of paths to look in.
	@raise Not_found	If the source can not be found. *)
let rec find_lib source paths =
	match paths with
	| [] ->  raise Not_found
	| path::tail ->
		let source_path = path ^ "/" ^ source in
		if Sys.file_exists source_path then path
		else find_lib source tail


(* options *)
let nmp: string ref = ref ""
let quiet = ref false
let verbose = ref false
let optirg = ref false
let options = [
	("-v", Arg.Set verbose, "verbose mode");
	("-q", Arg.Set quiet, "quiet mode");
	("-O",   Arg.Set     optirg, "try to optimize instructions tree (see doc in optirg for description)");
]


(** Run a standard command using IRG. Capture and display all errors.
	@param f		Function to run once the IRG is loaded.
	@param args		Added arguments.
	@param help		Help text about the command. *)
let run args help f =
	let free_arg arg =
		if !nmp = ""
		then nmp := arg
		else Printf.fprintf stderr "WARNING: only one NML file required, %s ignored\n" arg in
	Arg.parse (options @ args) free_arg help;
	if !nmp = "" then
		begin
			prerr_string "ERROR: one NML file must be given !\n";
			Arg.usage options help;
			exit 1
		end
	else
		process !nmp f !optirg


(** Build a template, possibly informing the user.
	@param template		Template name.
	@param file			File path to output to.
	@param dict			Dictionary to use. *)
let make_template template file dict =
	if not !quiet then (Printf.printf "creating \"%s\"\n" file; flush stdout);
	Templater.generate dict template file

