(*
 * GLISS2 -- decode module
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



(** Returns the syntax string (if syntax = string_const)
	or the syntax format string s (if syntax = format(s, ...))
	or "" in case of error.
	@param sp	Current instruction.
	@return		Image string. *)
let get_format_string sp =
	let remove_space s =
		let rec concat_str_list s_l =
			match s_l with
			[] ->
				""
			| h::q ->
				h ^ (concat_str_list q)
		in
		concat_str_list (Str.split (Str.regexp "[ \t]+") s)
	in
	let get_expr_from_iter_value v  =
		match v with
		Iter.EXPR(e) ->
			e
		| _ -> Irg.NONE
	in
	let image_attr =
		get_expr_from_iter_value (Iter.get_attr sp "image")
	in
	let rec get_str e =
		match e with
		| Irg.FORMAT(str, _) -> str
		| Irg.CONST(t_e, c) ->
			if t_e=Irg.STRING then
				match c with
				Irg.STRING_CONST(str, false, _) ->
					str
				| _ -> ""
			else
				""
		| Irg.ELINE(_, _, e) -> get_str e
		| _ -> ""
	in
	remove_space (get_str image_attr)


(* f is a format (%nb, n > 0) from a splitted format string, returns n (length of format) *)
let get_length_from_format f =
	let l = String.length f in
	let new_f =
		if l<=2 then
		(* shouldn't happen, we should have only formats like %[0-9]*b, not %d or %f *)
			"0"
		else
			String.sub f 1 (l-2)
	in
	Scanf.sscanf new_f "%d" (fun x->x)


(* f is a splitted space removed format string from a syntax attr, returns its length in bits *)
let get_format_length f =
	let aux accu ff =
		match ff with
		| Str.Delim(d) -> accu + get_length_from_format d
		| Str.Text(txt) -> accu + String.length txt
	in
	List.fold_left aux  0 f


(* s is supposed to be a format syntax string (a string with only 0, 1 and %nb formats with n >= 0, spaces removed),
 * returns the mask for the nth format of s (n beginning at 0) *)
let get_mask_for_format_param s n =
	let s_cut = Str.full_split (Str.regexp "%[0-9]+b") s in
	let rec find_pos ss pos i =
		match ss with
		| [] ->
			failwith "shouldn't happen (decode.ml::get_mask_for_format_param::find_pos)"
		| a::b ->
			(match a with
			| Str.Delim(d) -> if i == n then (pos, d) else find_pos b (pos + get_length_from_format d) (i + 1)
			| Str.Text(txt) -> find_pos b (pos + (String.length txt)) i)
	in
	let (pos, f) = find_pos s_cut 0 0 in
	let lf = get_length_from_format f in
	let l = get_format_length s_cut in
	Bitmask.BITMASK((String.make pos '0') ^ (String.make lf '1') ^ (String.make (l - pos - lf) '0'))


(** return the mask for the nth param (counting from 0) of an instr of the given spec sp,
	the result will be a string with only '0' or '1' chars representing the bits of the mask,
	the params' order is the one given by the Iter.get_params method
	@param sp	Current instruction. 
	@param n	Parameter number.
	@return		Parameter mask. *)
let get_mask_for_param sp n =

	let rec change_i_th_param l i =
		match l with
		| [] -> ""
		| (Str.Delim(d))::b when i = 0 ->
			(String.make (get_length_from_format d) '1') ^ (change_i_th_param b (i - 1))
		| (Str.Delim(d))::b ->
			(String.make (get_length_from_format d) '0') ^ (change_i_th_param b (i - 1))
		| (Str.Text(txt))::b ->
			(String.make (String.length txt) '0') ^ (change_i_th_param b i) in

	let rec get_frmt_params e =
		match e with
		| Irg.FORMAT(_, params) -> params
		| Irg.ELINE(_, _, e) -> get_frmt_params e
		| _ -> failwith "(Decode) can't find the params of a given (supposed) format expr" in
			
	let get_expr_from_iter_value v  =
		match v with
		| Iter.EXPR(e) -> e
		| _ -> Irg.NONE in

	let rec get_name_of_param e =
		match e with
		| Irg.FIELDOF(_, ee, _) -> ee
		| Irg.REF(name) -> name
		| Irg.ELINE (_, _, ee) -> get_name_of_param ee
		| _ -> failwith "(Decode) parameter in image format is too complex to process" in

	let image_attr = get_expr_from_iter_value (Iter.get_attr sp "image") in
	let frmt_params = get_frmt_params image_attr in
	let str_params = get_format_string sp in

	let get_rank_of_named_param n =
		let rec aux nn i p_l =
			match p_l with
			| [] -> failwith ("(Decode) can't find rank of param "^nn^" in the format params")
			| a::b when nn = (get_name_of_param a) -> i
			| a::b -> aux nn (i + 1) b in
		aux n 0 frmt_params in

	let rec get_i_th_param_name i l =
		match l with
		| [] -> failwith "(Decode) can't find name of i_th param of a spec"
		| a::b when i = 0 -> Irg.get_name_param a
		| a::b -> get_i_th_param_name (i - 1) b in

	Bitmask.BITMASK(change_i_th_param
		(Str.full_split (Str.regexp "%[0-9]*[bdfxs]") (str_params))
		(get_rank_of_named_param (get_i_th_param_name n (Iter.get_params sp))))


(* stucture with some useful infos about an inst *)
type instr_info_t = {
	mutable is_risc  : bool;	(* is this inst belonging to a RISC instr set? *)
	mutable isize    : int;		(* size of the inst if is_risc, 0 otherwise *)
	mutable is_multi : bool;	(* are there more than one instr set defined? *)
}

(** return an instr_info_t structure filled correctly with infos of the given inst
	@param inst		Current instruction.
	@return			Instruction information. *)
let get_instr_info inst = 
	let instr_sets = !Iter.multi_set in
	let instr_sets_sizes_map = List.map (Fetch.find_fetch_size) instr_sets in
	let find_iset_size_of_inst inst =
		let member = List.map (List.mem inst) instr_sets in
		let member_size = List.map2 (fun x y -> if x then [y] else []) member instr_sets_sizes_map in
		List.hd (List.flatten member_size)
	in
	let isize = find_iset_size_of_inst inst in
	let is_risc = isize <> 0 in
	let is_multi = (List.length instr_sets) > 1 in {
		is_risc = is_risc;
		isize = isize;
		is_multi = is_multi;
	}


let get_decode_for_format_param inst idx =
	let inst_info = get_instr_info inst in
	let find_first_bit mask =
		let rec aux index shifted_mask =
			if (Int64.logand shifted_mask 1L) <> 0L || index >= 32
			then index
			else aux (index+1) (Int64.shift_right shifted_mask 1)
		in
		aux 0 mask
	in
	let rec get_frmt_params e =
		match e with
		| Irg.FORMAT(_, params) -> params
		| Irg.ELINE(_, _, e) -> get_frmt_params e
		| _ -> failwith "(Decode) can't find the params of a given (supposed) format expr"
	in
	let get_expr_from_iter_value v  =
		match v with
		| Iter.EXPR(e) -> e
		| _ -> Irg.NONE
	in
	let image_attr =
		get_expr_from_iter_value (Iter.get_attr inst "image")
	in
	let frmt_params = get_frmt_params image_attr in
	let nth_frmt_param i = List.nth frmt_params i in
	let decoder inst idx sfx size =
		let string_mask = get_mask_for_format_param (get_format_string inst) idx in
		let cst_suffix = Bitmask.c_const_suffix string_mask in
		let mask = Bitmask.to_int64 string_mask in
		let suffix = if sfx then Printf.sprintf "_%d" size else "" in
		let suffix_code = if sfx then Printf.sprintf "->u%d" size else "" in
		let extract _ = Printf.sprintf "__EXTRACT%s(0x%LX%s, %d, code_inst%s)"  suffix mask cst_suffix (find_first_bit mask) suffix_code in
		let exts    n = Printf.sprintf "__EXTS%s(0x%LX%s, %d, code_inst%s, %d)" suffix mask cst_suffix (find_first_bit mask) suffix_code n in
		(* !!BUG!! faut le type du param de format lu, pas de spec *)
		(*match Sem.get_type_ident (fst (List.nth (Iter.get_params inst) idx)) with*)
		match Sem.get_type_expr (nth_frmt_param idx) with
		| Irg.INT n when n <> 8 && n <> 16 && n <> 32 -> exts n
		| _ -> extract () in
	let decoder_CISC inst idx sfx =
		let suffix = if sfx then "_CISC" else "" in
		let suffix_code = if sfx then "->mask" else "" in
		let extract _ = Printf.sprintf "__EXTRACT%s(&mask%d, code_inst%s)" suffix idx suffix_code in
		let exts    n = Printf.sprintf "__EXTS%s(&mask%d, code_inst%s, %d)" suffix idx suffix_code n in
		(* !!BUG!! faut le type du param de format lu, pas de spec *)
		(* match Sem.get_type_ident (fst (List.nth (Iter.get_params inst) idx)) with *)
		match Sem.get_type_expr (nth_frmt_param idx) with
		| Irg.INT n when n <> 8 && n <> 16 && n <> 32 -> exts n
		| _ -> extract () in
	if inst_info.is_risc then
		decoder inst idx inst_info.is_multi inst_info.isize
	else
		decoder_CISC inst idx inst_info.is_multi
	

(* transform a mask into a C hexa uint32_t const list like in a C array initialization *)
let to_C_list mask =
		let rec aux comma l =
			match l with
			| [] -> ""
			| a::b ->
				((if comma then ", " else "") ^ (Printf.sprintf "0X%lX" a)) ^ (aux true b)
		in
			aux false mask

(* gets mask for idx-th param of the given inst *)
let get_mask_decl inst idx =
	let inst_info = get_instr_info inst in
	let string_mask = get_mask_for_param inst idx in
	let mask = Bitmask.to_int32_list string_mask in
		if not inst_info.is_risc then
			Printf.sprintf "static uint32_t tab_mask%d[%d] = {%8s}; /* %s */\n\tstatic mask_t mask%d = {tab_mask%d, %d};\n"
				idx (List.length mask) (to_C_list mask) (Bitmask.to_string string_mask) idx idx (Bitmask.length string_mask)
		else
			failwith "shouldn't happen (decode.ml::get_mask_decl)"


(** gets mask for idx-th param of the image format expr attr of the given inst
	@param inst		Current instruction.
	@param idx		Index of the parameter. *)
let get_mask_decl_for_format_param inst idx =
	let inst_info = get_instr_info inst in
	let string_mask = get_mask_for_format_param (get_format_string inst) idx in
	let mask = Bitmask.to_int32_list string_mask in
		if not inst_info.is_risc then
			Printf.sprintf "\tstatic uint32_t tab_mask%d[%d] = {%8s}; /* %s */\n\tstatic mask_t mask%d = {tab_mask%d, %d};\n"
				idx (List.length mask) (to_C_list mask) (Bitmask.to_string string_mask) idx idx (Bitmask.length string_mask)
		else
			failwith "shouldn't happen (decode.ml::get_mask_decl_for_format_param)"


(** return a list with all mask declarations for each image format param of the given spec
	@param inst		Current instruction. *)
let get_mask_decl_all_format_params inst =
	let get_expr_from_iter_value v  =
		match v with
		| Iter.EXPR(e) -> e
		| _ -> Irg.NONE
	in
	let image_attr = get_expr_from_iter_value (Iter.get_attr inst "image") in
	let rec get_frmt_params e =
		match e with
		| Irg.FORMAT(_, params) -> params
		| Irg.ELINE(file, line, e) -> Toc.locate_error file line get_frmt_params e
		| _ -> Toc.error_on_expr "unsupported expression in image format" e
	in
	let frmt_params = get_frmt_params image_attr in
	let frmt_params_numbered =
		let rec aux pl i =
			match pl with
			| [] -> []
			| a::b -> i::(aux b (i + 1))
		in
		aux frmt_params 0
	in
	let aux i =
		get_mask_decl_for_format_param inst i
	in
		List.map aux frmt_params_numbered


(* returns a list of format param with their indexes (indexes start at 0, couples (param, index) returned)
 * refering to the params of the format string from the given spec's image
 * which "contain" a reference to the given spec's nth param *)
let get_param_format_arg_list sp n =
	let get_expr_from_iter_value v  =
		match v with
		| Iter.EXPR(e) -> e
		| _ -> Irg.NONE
	in
	let image_attr = get_expr_from_iter_value (Iter.get_attr sp "image") in
	let rec get_frmt_params e =
		match e with
		| Irg.FORMAT(_, params) -> params
		| Irg.ELINE(_, _, e) -> get_frmt_params e
		| _ -> failwith "(Decode) can't find the params of a given (supposed) format expr"
	in
	let frmt_params = get_frmt_params image_attr in
	let spec_params = Iter.get_params sp in
	let param = List.nth spec_params n in
	let param_name = fst param in
	let frmt_params_numbered =
		let rec aux pl i =
			match pl with
			| [] -> []
			| a::b -> (a, i)::(aux b (i + 1))
		in
		aux frmt_params 0
	in
	let rec contain_param p =
		match p with
		| Irg.CANON_EXPR(te, s, e_l) -> List.fold_left (fun b x -> if b then true else contain_param x) false e_l
		| Irg.REF(s) -> if (String.compare s param_name) == 0 then true else false
		| Irg.BITFIELD(te, e1, e2, e3) -> (contain_param e1) || (contain_param e2) || (contain_param e3)
		| Irg.UNOP(te, u, e) -> contain_param e
		| Irg.BINOP(te, b, e1, e2) -> (contain_param e1) || (contain_param e2)
		| Irg.IF_EXPR(te, e1, e2, e3) -> (contain_param e1) || (contain_param e2) || (contain_param e3)
		| Irg.SWITCH_EXPR(te, e1, ee_l, e2) -> (contain_param e1) || (contain_param e2) || (List.fold_left (fun b x -> if b then true else (contain_param (fst x)) || (contain_param (snd x)) ) false ee_l)
		| Irg.ELINE(s, i, e) -> contain_param e
		| Irg.CAST(te, e) -> contain_param e
		| Irg.COERCE(te, e) -> contain_param e
		| Irg.NONE -> false
		| Irg.EINLINE(s) -> false
		| Irg.CONST(te, c) -> false
		| Irg.FORMAT(s, e_l) -> failwith "Decode: bad expr for a param, too complex to decode"
		| Irg.FIELDOF(e, s1, s2) -> failwith "Decode: bad expr for a param, shouldn't see a fieldof here"
		| Irg.ITEMOF(te, s, e) -> failwith "Decode: bad expr for a param, too complex to decode"
	in
	List.filter (fun x -> contain_param (fst x)) frmt_params_numbered


(* tells if the nth param of a given spec needs to be decoded in a non trivial way,
 * ie, if it only occurs once in the image and only as a ref (no complex expr)
 *)
let is_complex_param sp n =
	let rec get_frmt_params e =
		match e with
		| Irg.FORMAT(_, params) -> params
		| Irg.ELINE(_, _, e) -> get_frmt_params e
		| _ -> failwith "(Decode) can't find the params of a given (supposed) format expr"
	in
	let spec_params = Iter.get_params sp in
	let param = List.nth spec_params n in
	let param_name = fst param in
	let param_occurrences = get_param_format_arg_list sp n in
	let rec is_complex p =
		match p with
		| Irg.REF(s) -> if (String.compare s param_name) == 0 then false else true (*does the else case really happens???*)
		| Irg.CAST(_, e) -> is_complex e
		| Irg.COERCE(_, e) -> is_complex e
		| Irg.ELINE(_, _, e) -> is_complex e
		| _ -> true
	in
	if (List.length param_occurrences) > 1 then
		(* if a spec's param appears more than once in a format it needs a complex decoding *)
		true
	else
		is_complex (fst (List.hd param_occurrences))
