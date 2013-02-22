    (*
 * $Id: gep.ml,v 1.39 2009/11/26 09:01:16 casse Exp $
 * Copyright (c) 2008, IRIT - UPS <casse@irit.fr>
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
open Lexing


(* (** module structure *) *)
type gmod = {
	mutable iname: string;				(** interface name *)
	mutable aname: string;				(** actual name *)
	mutable path: string;				(** path to the module *)
	mutable libadd: string;				(** linkage options *)
	mutable cflags: string;				(** library compilation option *)
	mutable code_header: string;		(** to put on code header *)
}


(** Build a new module.
	@param _iname	Interface name.
	@param _aname	Actual name. *)
let new_mod _iname _aname = {
		iname = _iname;
		aname = _aname;
		path = "";
		libadd = "";
		cflags = "";
		code_header = "";
	}

(** list of modules *)
let modules = ref [
	new_mod "mem" "fast_mem";
	new_mod "grt" "grt";
	new_mod "error" "error";
	new_mod "gen_int" "gen_int"
]

(** Add a module to the list of module from arguments.
	@param text		Text of the ragument. *)
let add_module text =
	let new_mod =
		try
			let idx = String.index text ':' in
			new_mod
				(String.sub text 0 idx)
				(String.sub text (idx + 1) ((String.length text) - idx - 1))
		with Not_found ->
			new_mod text text in
	let rec set lst =
		match lst with
		  m::tl ->
			if m.iname = new_mod.iname then new_mod::tl
			else m::(set tl)
		| _ -> [new_mod] in
	modules := set !modules


(* options *)
let paths = [
	Config.install_dir ^ "/lib/gliss/lib";
	Config.source_dir ^ "/lib";
	Sys.getcwd ()]
let check				 = ref false
let sim                  = ref false
let decode_arg           = ref false
let gen_with_trace       = ref false
let memory               = ref "fast_mem"
let size                 = ref 0
let sources : string list ref = ref []
let switches: (string * bool) list ref = ref []
let options = [
	("-m",   Arg.String  add_module, "add a module (module_name:actual_module)]");
	("-s",   Arg.Set_int size, "for fixed-size ISA, size of the instructions in bits (to control NMP images)");
	("-a",   Arg.String (fun a -> sources := a::!sources), "add a source file to the library compilation");
	("-S",   Arg.Set     sim, "generate the simulator application");
	("-D",   Arg.Set     decode_arg, "activate complex arguments decoding");
	("-gen-with-trace", Arg.Set gen_with_trace, 
        "Generate simulator with decoding of dynamic traces of instructions (faster). module decode_dtrace must be used with this option" );
	("-p",   Arg.String (fun a -> Iter.instr_stats := Profile.read_profiling_file a),
		"Optimized generation with a profiling file given it's path. Instructions handlers are sorted to optimized host simulator cache" );
	("-PJ",  Arg.Int (fun a -> (App.profiled_switch_size := a; switches := ("GLISS_PROFILED_JUMPS", true)::!switches)), 
		"Stands for profiled jumps : enable better branch prediction if -p option is also activated");
	("-off", Arg.String (fun a -> switches := (a, false)::!switches), "unactivate the given switch");
	("-on",  Arg.String (fun a -> switches := (a, true)::!switches), "activate the given switch");
	("-fstat", Arg.Set Fetch.output_fetch_stat, "generates stats about fetch tables in <proc_name>_fetch_tables.stat");
	("-c", Arg.Set check, "only check if the NML is valid for generation")
] @ Stot.opts


(** Build an environment for a module.
	@param f	Function to apply to the environment.
	@param dict	Embedding environment.
	@param m	Module to process. *)
let get_module f dict m =
	f (
		("name", App.out (fun _ -> m.iname)) ::
		("NAME", App.out (fun _ -> String.uppercase m.iname)) ::
		("is_mem", Templater.BOOL (fun _ -> m.iname = "mem")) ::
		("PATH", App.out (fun _ -> m.path)) ::
		("LIBADD", App.out (fun _ -> m.libadd)) ::
		("CFLAGS", App.out (fun _ -> m.cflags)) ::
		("CODE_HEADER", App.out (fun _ -> m.code_header)) ::
		dict
	)


let get_source f dict source =
	f (("path", App.out (fun _ -> source)) :: dict)


(** find the first least significant bit set to one 
    @param mask      the mask to parse 
    @return the indice of the first least significant bit 
*)
let find_first_bit mask =
  let rec aux index shifted_mask =
    if (Int64.logand shifted_mask 1L) <> 0L || index >= 32
    then index
    else aux (index+1) (Int64.shift_right shifted_mask 1)
  in
    aux 0 mask


(* thrown when not succeeding to find the smallest C type uintN_t for a given size *)
exception BadCSize


(* will contain the result of decode_arg.decode_parameters so that
 * it is done once for all params of one spec,
 * the current spec is also given to know which instr we have decoded for *)
let inst_decode_arg = ref (Irg.UNDEF, [])


(** Generate the decode code for the parameter of a RISC instruction (16 or 32 bits).
	@param info		Generation information.
	@param inst		Current instruction
	@param idx		Parameter index.
	@param sfx		Multi-instruction set ?
	@param size		Size of the instruction (in bits).
	@param out		Stream to output to. *)
let decoder info inst idx sfx size out =
	let string_mask = Decode.get_mask_for_param inst idx in
	let cst_suffix = Bitmask.c_const_suffix string_mask in
	let mask = Bitmask.to_int64 string_mask in
	let suffix = if sfx then Printf.sprintf "_%d" size else "" in
	let suffix_code = if sfx then Printf.sprintf "->u%d" size else "" in
	let extract _ =
		Printf.fprintf out "__EXTRACT%s(0x%08LX%s, %d, code_inst%s)"  suffix mask cst_suffix (find_first_bit mask) suffix_code in
	let exts    n =
		Printf.fprintf out "__EXTS%s(0x%08LX%s, %d, code_inst%s, %d)" suffix mask cst_suffix (find_first_bit mask) suffix_code n in
	match Sem.get_type_ident (fst (List.nth (Iter.get_params inst) idx)) with
	| Irg.INT n when n <> 8 && n <> 16 && n <> 32 -> exts n
	| _ -> extract ()


(** Decoder for CISC instruction (variable number of bytes).
	@param info		Generation information.
	@param inst		Current instruction
	@param idx		Parameter index.
	@param sfx		Multi-instruction set ?
	@param out		Stream to output to. *)
let decoder_CISC info inst idx sfx out =
	let suffix = if sfx then "_CISC" else "" in
	let suffix_code = if sfx then "->mask" else "" in
	let extract _ =
		Printf.fprintf out "__EXTRACT%s(&mask%d, code_inst%s)" suffix idx suffix_code in
	let exts n =
		Printf.fprintf out "__EXTS%s(&mask%d, code_inst%s, %d)" suffix idx suffix_code n in
	match Sem.get_type_ident (fst (List.nth (Iter.get_params inst) idx)) with
	| Irg.INT n when n <> 8 && n <> 16 && n <> 32 -> exts n
	| _ -> extract () 


(** Decoder for complex parameters.
	@param info		Generation information.
	@param inst		Current instruction
	@param idx		Parameter index.
	@param sfx		Multi-instruction set ?
	@param size		Size of the instruction (in bits).
	@param is_risc	RISC instruction set ?
	@param out		Stream to output to. *)
let output_decoder_complex info inst idx sfx size is_risc out =
	let get_expr_from_iter_value v  =
		match v with
		| Iter.EXPR(e) -> e
		| _ -> Irg.NONE
	in
	let image_attr = get_expr_from_iter_value (Iter.get_attr inst "image") in
	let rec get_frmt_params e =
		match e with
		| Irg.FORMAT(_, params) -> params
		| Irg.ELINE(_, _, e) -> get_frmt_params e
		| _ -> failwith "(Decode) can't find the params of a given (supposed) format expr"
	in
	let frmt_params = get_frmt_params image_attr in

	if not (Decode_arg.is_complex frmt_params) then
		if is_risc then decoder info inst idx sfx size out
		else decoder_CISC info inst idx sfx out
	else
		let num_frmt_params = List.length frmt_params in
		let spec_params = Iter.get_params inst in
		let spec_params_name = List.map fst spec_params in
		let get_nth_expr n = snd (List.nth (snd !inst_decode_arg) n) in
		let output_expr e =
			let info = Toc.info () in
			let o = info.Toc.out in
			info.Toc.out <- out;
			Toc.gen_expr info (snd (Toc.prepare_expr info Irg.NOP e)) false;
			info.Toc.out <- o
		in
		let rec get_str e =
			match e with
			| Irg.FORMAT(str, _) -> str
			| Irg.CONST(Irg.STRING, Irg.STRING_CONST(str, false, _)) -> str
			| Irg.ELINE(_, _, e) -> get_str e
			| _ -> ""
		in
		(* decode every format param once in one pass for each instr *)
		if (fst !inst_decode_arg) <> inst then
			(let rec aux n =
				if n < num_frmt_params then
					(Irg.EINLINE (Decode.get_decode_for_format_param inst n))::(aux (n + 1))
				else
					[]
			in
			let expr_frmt_params = aux 0 in
			inst_decode_arg := (inst, Decode_arg.decode_fast spec_params_name frmt_params expr_frmt_params inst));
		output_expr (get_nth_expr idx);
		output_char out '\n'


(** Output a mask declaration.
	@param inst		Instruction.
	@param idx		Parameter index.
	@param is_risc	RISC instruction.
	@param out		Stream to output to. *)
let output_mask_decl inst idx is_risc out =
	let to_C_list mask =
		let rec aux comma l =
			match l with
			| [] -> ""
			| a::b ->
				((if comma then ", " else "") ^ (Printf.sprintf "0X%lX" a)) ^ (aux true b) in
			aux false mask in
	let string_mask = Decode.get_mask_for_param inst idx in
	let mask = Bitmask.to_int32_list string_mask
	in
		if not is_risc then
			(Printf.fprintf out "static uint32_t tab_mask%d[%d] = {" idx (List.length mask);
			Printf.fprintf out "%8s}; /* %s */\n" (to_C_list mask) (Bitmask.to_string string_mask);
			Printf.fprintf out "\tstatic mask_t mask%d = {tab_mask%d, %d};\n" idx idx (Bitmask.length string_mask))


(** Declare the masks in case of a complex parameter decoding.
	@param inst		Instruction to process.
	@param is_risc	Is a RISC instruction ?
	@param out		Stream to output to. *)
let mask_decl_all inst is_risc out =

	(* find format parameters *)
	let get_expr_from_iter_value v  =
		match v with
		| Iter.EXPR(e) -> e
		| _ -> Irg.NONE
	in
	let image_attr = get_expr_from_iter_value (Iter.get_attr inst "image") in
	let rec get_frmt_params e =
		match e with
		| Irg.FORMAT(_, params) -> params
		| Irg.ELINE(_, _, e) -> get_frmt_params e
		| _ -> failwith "(Decode) can't find the params of a given (supposed) format expr"
	in
	let frmt_params = get_frmt_params image_attr in

	(* switch between complex and not *)
	if Decode_arg.is_complex frmt_params then
		List.iter (fun x -> output_string out x) (Decode.get_mask_decl_all_format_params inst)
	else
		ignore(List.fold_left
			(fun idx _ -> output_char out '\t'; output_mask_decl inst idx is_risc out; idx + 1)
			0
			(Iter.get_params inst))


(** Build a template environment.
	@param info		Information for generation.
	@return			Default template environement. *)
let make_env info =
	let min_size =
		Iter.iter
			(fun min inst ->
				let size = Iter.get_instruction_length inst
				in if size < min then size else min)
			1024 
	in
	let max_size =
		Iter.iter
			(fun max inst ->
				let size = Iter.get_instruction_length inst
				in if size > max then size else max)
			0
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
	let instr_sets = !Iter.multi_set in
	let rec suppress_double l =
		match l with
		| [] -> []
		| a::b -> if List.mem a b then suppress_double b else a::(suppress_double b)
	in
	let instr_sets_sizes_map = List.map (Fetch.find_fetch_size) instr_sets in
	let instr_sets_sizes = suppress_double instr_sets_sizes_map in
	let find_iset_size_of_inst inst =
		let member = List.map (List.mem inst) instr_sets in
		let member_size = List.map2 (fun x y -> if x then [y] else []) member instr_sets_sizes_map in
		List.hd (List.flatten member_size)
	in
	let get_msb_mask n =
		try
			(match get_C_size n with
			| 8 -> "0x80"
			| 16 -> "0x8000"
			| 32 -> "0x80000000"
			| 64 -> "0x8000000000000000LL"
			| _ -> raise BadCSize)
		with
			BadCSize -> raise (Sys_error "template $(msb_mask) should be used only with a RISC ISA")
	in
	let max_op_nb = Iter.get_params_max_nb () in
	let inst_count = (Iter.iter (fun cpt inst -> cpt+1) 0) + 1 (* plus one because I'm counting the UNKNOW_INST as well *)
	in

	let add_mask_32_to_param inst idx name _ dict =
		let isize = find_iset_size_of_inst inst in
		let is_risc = isize <> 0 in
		let is_multi = (List.length instr_sets) > 1 in
		("decoder", Templater.TEXT (
			if is_risc then (decoder info inst idx is_multi isize)
			else (decoder_CISC info inst idx is_multi))) ::
		("decoder_complex", Templater.TEXT (fun out ->
			if not !decode_arg then
				raise (Sys_error "template $(decoder_complex) should be used only if complex argument decoding is activated (option -D)")
			else
				output_decoder_complex info inst idx is_multi isize is_risc out)) ::
		("mask_decl", Templater.TEXT (output_mask_decl inst idx is_risc)) ::
		(*("mask_decl_all", Templater.TEXT (fun out -> List.iter (fun x -> output_string out x) (Decode.get_mask_decl_all_format_params inst))) ::*)
		dict in
	let add_size_to_inst inst dict =
		let iset_size = find_iset_size_of_inst inst in
		let is_risc = iset_size <> 0 in
		("size", Templater.TEXT (fun out -> Printf.fprintf out "%d" (Iter.get_instruction_length inst))) ::
		("gen_code", Templater.TEXT (fun out ->
			let info = Toc.info () in
			info.Toc.out <- out;
			Toc.set_inst info inst;
			(*!!WARNING!!*)
			(* gliss1 compatibility, predecode generation before action *)
			(try
				let _ = Iter.get_attr inst "predecode" in
				Toc.gen_action info "predecode"
			with
			| Not_found -> ());
			Toc.gen_action info "action")) ::
		("gen_pc_incr", Templater.TEXT (fun out ->
			let info = Toc.info () in
			info.Toc.out <- out;
			Toc.set_inst info inst;
			Toc.gen_stat info (Toc.gen_pc_increment info))) ::
		(* true if inst belongs to a RISC instr set *)
		("is_RISC_inst", Templater.BOOL (fun _ -> iset_size <> 0)) ::
		(* instr size of the instr set where belongs inst (meaning stg only if instr set is RISC) *)
		("iset_size", Templater.TEXT (fun out -> Printf.fprintf out "%d" iset_size)) ::
		("mask_decl_all", Templater.TEXT (fun out -> mask_decl_all inst is_risc out)) ::
		dict
	in
	let get_instr_set_size f dict size =
	f (
		("is_RISC_size", (Templater.BOOL (fun _ -> size <> 0))) ::
		("C_size", Templater.TEXT(fun out ->
			if size = 0 then
				raise (Sys_error "template $(C_size) in $(instr_sets_sizes) collection should be used only with RISC ISA")
			else Printf.fprintf out "%d" size)) ::
		("msb_size_mask", Templater.TEXT(fun out -> 
			if size = 0 then
				raise (Sys_error "template $(msb_size_mask) in $(instr_sets_sizes) collection should be used only with RISC ISA")
			else output_string out (get_msb_mask size))) ::
		dict
	)
	in
	let print_name n out info =
		let o = info.Toc.out in
		info.Toc.out <- out;
		Toc.gen_expr info (snd (Toc.prepare_expr info Irg.NOP (Irg.REF n))) false;
		info.Toc.out <- o
	in
	let maker = App.maker() in
	maker.App.get_params <- add_mask_32_to_param;
	maker.App.get_instruction <- add_size_to_inst;

	("modules", Templater.COLL (fun f dict -> List.iter (get_module f dict) !modules)) ::
	("sources", Templater.COLL (fun f dict -> List.iter (get_source f dict) !sources)) ::

	("is_complex_decode", Templater.BOOL (fun _ -> !decode_arg)) ::
	(* declarations of fetch tables *)
	("INIT_FETCH_TABLES", Templater.TEXT(fun out -> Fetch.output_all_table_C_decl out)) ::
(* create a iss coll were these attr will have meaning *)
	("min_instruction_size", Templater.TEXT (fun out -> Printf.fprintf out "%d" min_size)) ::
	("max_instruction_size", Templater.TEXT (fun out -> Printf.fprintf out "%d" max_size)) ::
	("is_RISC", Templater.BOOL (fun _ ->
		if (List.length instr_sets) > 1 then
			raise (Sys_error "template $(is_RISC) should be used only when only one instruction set is defined")
		else is_RISC)) ::
	("is_CISC_present", Templater.BOOL (fun _ -> List.exists (fun x -> x = 0) instr_sets_sizes)) ::
	(* next 2 things have meaning only if a RISC ISA is considered as we use min_size *)
	(* stands for the most appropriate standard C size (uintN_t) *)
	("C_inst_size",
		Templater.TEXT (fun out ->
			if (List.length instr_sets) > 1 then
				raise (Sys_error "template $(C_inst_size) should be used only when only one instruction set is defined")
			else
				(Printf.fprintf out "%d"
					(try (get_C_size min_size) with
					| BadCSize -> raise (Sys_error "template $(C_inst_size) should be used only with RISC ISA"))))) ::
	(* return a mask for the most significant bit, size depends on the C size needed *)
	("msb_mask",
		(App.out (fun _ ->
			if (List.length instr_sets) > 1 then
				raise (Sys_error "template $(msb_mask) should be used only when only one instruction set is defined")
			else (get_msb_mask min_size)))) ::

	("is_multi_set", Templater.BOOL (fun _ -> ((List.length instr_sets) > 1))) ::
	(* quantity of instruction sets *)
	("num_instr_sets", Templater.TEXT (fun out -> Printf.fprintf out "%d" (List.length instr_sets)) ) ::
	(* different sizes of the instr sets (no double) *)
	("instr_sets_sizes", Templater.COLL (fun f dict -> List.iter (get_instr_set_size f dict) instr_sets_sizes)) ::
	(* declaration of the value type that holds an instr code in read only, used in decode, should be concat directly to var name in C code *)
	("code_read_param_decl", Templater.TEXT (fun out ->
		Printf.fprintf out "%s"
			(if (List.length instr_sets) > 1 then
				"code_t *"
			else (if is_RISC then
				(Printf.sprintf "uint%d_t " (get_C_size min_size))
			else "mask_t *")))) ::
	(* declaration of the value type that holds an instr code in write mode, used in decode, should be concat directly to var name in C code *)
	("code_write_param_decl", Templater.TEXT (fun out ->
		Printf.fprintf out "%s"
			(if (List.length instr_sets) > 1 then
				"code_t *"
			else (if is_RISC then
				(Printf.sprintf "uint%d_t *" (get_C_size min_size))
			else "mask_t *")))) ::
	(* declaration of the value type that holds an instr code in read only, used in decode, should be concat directly to var name in C code *)
	("code_read_decl", Templater.TEXT (fun out ->
		Printf.fprintf out "%s"
			(if (List.length instr_sets) > 1 then
				"code_t "
			else (if is_RISC then
				(Printf.sprintf "uint%d_t " (get_C_size min_size))
			else "mask_t ")))) ::
	("total_instruction_count", Templater.TEXT (fun out -> Printf.fprintf out "%d" inst_count)  ) ::
 	("max_operand_nb", Templater.TEXT (fun out -> Printf.fprintf out "%d" max_op_nb)  ) ::
	("gen_init_code", Templater.TEXT (fun out ->
			let info = Toc.info () in
			let spec_init = Irg.get_symbol "init" in
			let init_action_attr =
				match Iter.get_attr spec_init "action" with
				| Iter.STAT(s) -> Irg.ATTR_STAT("action", s)
				| _ -> failwith "(gep.ml::make_env::$(gen_init_code)) attr action for op init must be a stat"
			in
			info.Toc.out <- out;
			info.Toc.inst <- spec_init;
			info.Toc.iname <- "init";
			(* stack params (none) and attrs (only action) for "init" op *)
			Irg.attr_stack [init_action_attr];
			let _ = Toc.get_stat_attr "action" in
			Toc.gen_action info "action";
			Irg.attr_unstack [init_action_attr])) ::
	("gen_pc_incr_unknown", Templater.TEXT (fun out ->
			let info = Toc.info () in
			info.Toc.out <- out;
			Toc.gen_stat info (Toc.gen_pc_increment info))) ::
	("NPC_NAME", Templater.TEXT (fun out -> print_name (String.uppercase info.Toc.npc_name) out info)) ::
	("npc_name", Templater.TEXT (fun out -> print_name (info.Toc.npc_name) out info)) ::
	("has_npc", Templater.BOOL (fun _ -> (String.compare info.Toc.npc_name "") != 0)) ::
	("PC_NAME", Templater.TEXT (fun out -> print_name (String.uppercase info.Toc.pc_name) out info)) ::
	(*("pc_name", Templater.TEXT (fun out -> output_string out  (info.Toc.pc_name))) ::*)
	("pc_name", Templater.TEXT (fun out -> print_name info.Toc.pc_name out info)) ::
	("PPC_NAME", Templater.TEXT (fun out -> print_name (String.uppercase info.Toc.ppc_name) out info)) ::
	("ppc_name", Templater.TEXT (fun out -> print_name (info.Toc.ppc_name) out info)) ::
	("bit_image_inversed", Templater.BOOL (fun _ -> Bitmask.get_bit_image_order ())) ::
	("declare_switch", Templater.TEXT (fun out -> info.Toc.out <- out; Stot.declare info)) ::
	(App.make_env info maker)


(** Perform a symbolic link.
	@param src	Source file to link.
	@param dst	Destination to link to. *)
let link src dst =
	if Sys.file_exists dst then Sys.remove dst;
	Unix.symlink src dst


(** Regular expression for LIBADD *)
let libadd_re = Str.regexp "^LIBADD=\\(.*\\)"


(** regular expression for CFLAGS *)
let cflags_re = Str.regexp "^CFLAGS=\\(.*\\)"


(** regular expression for CODE_HEADER *)
let code_header_re = Str.regexp "^CODE_HEADER=\\(.*\\)"



(** Find a module and set the path.
	@param m	Module to find. *)
let find_mod m =

	let rec find_lib paths =
		match paths with
		| [] ->  raise (Sys_error ("cannot find module " ^ m.aname))
		| path::tail ->
			let source_path = path ^ "/" ^ m.aname ^ ".c" in
			if Sys.file_exists source_path then m.path <- path
		else find_lib tail in

	let rec read_lines input =
		let line = input_line input in
		if Str.string_match libadd_re line 0 then
			m.libadd <- Str.matched_group 1 line
		else if Str.string_match cflags_re line 0 then
			m.cflags <- Str.matched_group 1 line
		else if Str.string_match code_header_re line 0 then
			m.code_header <- m.code_header ^ (Str.matched_group 1 line) ^ "\n";
		read_lines input in

	find_lib paths;
	let info_path = m.path ^ "/" ^ m.aname ^ ".info" in
	if Sys.file_exists info_path then
		try
			read_lines (open_in info_path)
		with End_of_file ->
			()


(** Link a module for building.
	@param info	Generation information.
	@param m	Module to process. *)
let process_module info m =
	let source = info.Toc.spath ^ "/" ^ m.iname ^ ".c" in
	let header = info.Toc.hpath ^ "/" ^ m.iname ^ ".h" in
	if not !App.quiet then Printf.printf "creating \"%s\"\n" source;
	App.replace_gliss info (m.path ^ "/" ^ m.aname ^ ".c") source;
	if not !App.quiet then Printf.printf "creating \"%s\"\n" header;
	App.replace_gliss info (m.path ^ "/" ^ m.aname ^ ".h") header


(* main program *)
let _ =
	App.run
		options
		"SYNTAX: gep [options] NML_FILE\n\tGenerate code for a simulator"
		(fun info ->
			if !check then
				let _ = Iter.get_insts () in
				()
			else
			
				(* optimisations *)
				Stot.transform ();
			
				(* prepare environment *)
				let dict = make_env info in
				let dict = List.fold_left (fun d (n, v) -> App.add_switch n v d) dict !switches in

				(* include generation *)
				List.iter find_mod !modules;

				if not !App.quiet then Printf.printf "creating \"include/\"\n";
				App.makedir "include";
				if not !App.quiet then Printf.printf "creating \"%s\"\n" info.Toc.hpath;
				App.makedir info.Toc.hpath;
				App.make_template "id.h" ("include/" ^ info.Toc.proc ^ "/id.h") dict;
				App.make_template "api.h" ("include/" ^ info.Toc.proc ^ "/api.h") dict;
				App.make_template "debug.h" ("include/" ^ info.Toc.proc ^ "/debug.h") dict;
				App.make_template "macros.h" ("include/" ^ info.Toc.proc ^ "/macros.h") dict;

				(* source generation *)
				if not !App.quiet then Printf.printf "creating \"include/\"\n";
				App.makedir "src";

				App.make_template "Makefile" "src/Makefile" dict;
				App.make_template "gliss-config" ("src/" ^ info.Toc.proc ^ "-config") dict;
				App.make_template "api.c" "src/api.c" dict;
				App.make_template "debug.c" "src/debug.c" dict;
				App.make_template "platform.h" "src/platform.h" dict;
				App.make_template "fetch_table.h" "src/fetch_table.h" dict;
				App.make_template "fetch.h" ("include/" ^ info.Toc.proc ^ "/fetch.h") dict;
				App.make_template "fetch.c" "src/fetch.c" dict;
				(if not !gen_with_trace 
				 then
					(App.make_template "decode_table.h" "src/decode_table.h" dict;
					App.make_template "decode.h" ("include/" ^ info.Toc.proc ^ "/decode.h") dict;
					App.make_template "decode.c" "src/decode.c" dict)
				 else 
					(* now decode files are in templates directory (unlike a module) *)
					(* !!TODO!! outut the correct decoder, not only decode_dtrace 
					 * design a fun : name_module instance -> output correct module
					 * whereever the files are (lib or templates) *)
					if (Iter.iter (fun e inst -> (e || Iter.is_branch_instr inst)) false)
					then
						(* dtrace has a different decode table organization ==> different table template *)
						(App.make_template "decode_dtrace_table.h" "src/decode_table.h" dict;
						App.make_template "decode_dtrace.h" ("include/" ^ info.Toc.proc ^ "/decode.h") dict;
						App.make_template "decode_dtrace.c" "src/decode.c" dict )
					else failwith ("Attributes 'set_attr_branch = 1' are mandatory with option -gen-with-trace "^
								   "but gep was not able to find a single one while parsing the NML")
				);
				App.make_template "code_table.h" "src/code_table.h" dict;

				(* module linking *)
				List.iter (process_module info) !modules;

				(* generate application *)
				if !sim then
					try
						let path = App.find_lib "sim/sim.c" paths in
						App.makedir "sim";
						App.replace_gliss info
							(path ^ "/" ^ "sim/sim.c")
							("sim/" ^ info.Toc.proc ^ "-sim.c" );
						Templater.generate_path
							[ ("proc", Templater.TEXT (fun out -> output_string out info.Toc.proc)) ]
							(path ^ "/sim/Makefile")
							"sim/Makefile"
					with Not_found ->
						raise (Sys_error "no template to make sim program")
		)
