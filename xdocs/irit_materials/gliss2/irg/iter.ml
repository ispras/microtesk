(*
 * $Id$
 * Copyright (c) 2010, IRIT - UPS <casse@irit.fr>
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

(** Type of integrated instructions. *)
type inst = Irg.spec

(** Null instruction. *)
let null = Irg.UNDEF

type value = STAT of Irg.stat | EXPR of Irg.expr

let print_value v =
	match v with
	STAT(s) ->
		Irg.print_statement s
	| EXPR(e) ->
		Irg.print_expr e

(** Check if all SET or SETSPE statements in a spec are coerced if needed,
it happens when location and rvalue have different scalar types (card or int)
	@param spec	spec of the instrution or the mode
*)
let check_coerce spec =
	let rec check_stat sta =
		match sta with
		| Irg.SEQ(s1, s2) ->
			Irg.SEQ(check_stat s1, check_stat s2)
		| Irg.SET(l, e) ->
			Irg.SET(l, Sem.check_set_stat l e)
		| Irg.IF_STAT(e, s1, s2) ->
			Irg.IF_STAT(e, check_stat s1, check_stat s2)
		| Irg.SWITCH_STAT(e, es_l, s) ->
			Irg.SWITCH_STAT(e, List.map (fun (ex, st) -> (ex, check_stat st)) es_l, check_stat s)
		| Irg.SETSPE(l, e) ->
			Irg.SETSPE(l, Sem.check_set_stat l e)
		| Irg.LINE(s, i, st) ->
			Irg.LINE(s, i, check_stat st)
		| _ ->
			sta

	in
	let check_attr a =
		match a with
		Irg.ATTR_STAT(s, st) ->
			Irg.ATTR_STAT(s, check_stat st)
		| _ ->
			a
	in
	match spec with
	Irg.AND_OP(s, st_l, a_l) ->
		Irg.param_stack st_l;
		let res = Irg.AND_OP(s, st_l, List.map check_attr a_l)
		in
			Irg.param_unstack st_l;
			res
	| _ ->
		(* shouldn't happen *)
		spec


(** structure containing the specifications of all instantiated instructions,
initialised with something meaningless to help determine type of ref *)
let instr_set = ref [Irg.UNDEF]

(** will contains instr sorted by instruction set if multi, each set in a list *)
let multi_set = ref []

(** List of instruction names sorted by ascending order of call number
	This list is initialize if -p option is activated when calling GEP *)
let instr_stats : (string list ref) = ref []


(** return the ID of the given instruction spec, 0 is for unknown instr
	@param instr	the spec of the instruction whose ID is needed *)
let get_id instr =
	let rec search_in_list l i num =
		match l with
		[] -> (* return 0 if instr not found (unknown) *)
			0
		| a::b ->
			if a = i then
				num
			else
				search_in_list b i (num+1)
	in
	search_in_list !instr_set instr 1


(** return an attr from an instruction or mode specification
	@param instr		spec of the instrution or the mode
	@param name			name of the attr to return.
	@raise Not_found	If the attribute cannot be found. *)
let get_attr instr name =
	let rec search_attr_in_list n a_l =
		match a_l with
		| [] -> raise Not_found
		| (Irg.ATTR_STAT(nm, s))::t when nm = n-> STAT(s)
		| (Irg.ATTR_EXPR(nm, e))::t when nm = n-> EXPR(e)
		| (Irg.ATTR_STAT(nm, s))::t -> search_attr_in_list n t
		| (Irg.ATTR_EXPR(nm, e))::t -> search_attr_in_list n t
		| _::t -> search_attr_in_list n t in
	match instr with
	| Irg.AND_OP(n, _, a_l) -> search_attr_in_list name a_l
	| Irg.AND_MODE(n, _, _, a_l) -> search_attr_in_list name a_l
	| _ -> assert false


(** return true if the instruction is a branch *)
let is_branch_instr instr =
	try
		let _ = get_attr instr "set_attr_branch"
		in
		   true
	with Not_found -> false


(* name cache *)
module HashInst =
struct
	type t = Irg.spec
	let equal (s1 : t) (s2 : t) = s1 == s2
	let hash (s : t) = Hashtbl.hash s
end
module NameTable = IdMaker.Make(HashInst)


(** Get a MUST expression attribute or raise an error.
	@param inst		Instruction to get attribute from.
	@param attr		Attribute to get. *)
let must_expr_attr inst attr =
	let error m =
		match inst with
		| Irg.AND_OP (name, _, _) ->
			raise (Irg.Error (fun out -> Printf.fprintf out "%s: %s\n" (Irg.pos_of name) m))
		| _ -> failwith "should be an AND_OP" in
	try
		(match get_attr inst "syntax" with
		  EXPR(e) -> e
		| _ -> error "syntax attribute should be an expression")
	with Not_found -> error "no syntax attribute"


(**
 * Get C identifier for the current instruction.
 * This name may be used to build other valid C names.
 * @param instr		Instruction to get name for.
 * @return			C name for the instruction.
 *)
let get_name instr =
	let rec to_string e =
		match e with
		  Irg.FORMAT(s, e_l) -> s
		| Irg.CONST(Irg.STRING, Irg.STRING_CONST(str, false, _)) -> str
		| Irg.ELINE(_, _, e) -> to_string e
		| Irg.IF_EXPR (_, _, _, e) -> to_string e
		| Irg.SWITCH_EXPR (_, _, cases, def) ->
			to_string (if (List.length cases) >= 1 then snd (List.hd cases) else def)
		| _ -> failwith "unsupported operator in syntax" in

	let syntax = to_string (must_expr_attr instr "syntax") in
	NameTable.make instr syntax


(** Get instruction identification for the user.
	@param inst		Instruction to get user name for.
	@return			User name for the instruction. *)
let get_user_id inst =
	let rec make e =
		match e with
		| Irg.FORMAT(str, _) -> str
		| Irg.CONST(Irg.STRING, c) ->
				(match c with
				| Irg.STRING_CONST(str, false, _) -> str
				| _ -> "")
		| Irg.ELINE(_, _, e) -> make e
		| _ -> "" in
	make (must_expr_attr inst "syntax")
	(*match get_attr inst "syntax" with
	| EXPR(e)	-> make e
	| _			-> failwith "syntax does not reduce to a string"*)


(** return the params (with their types) of an instruction specification
	@param instr	spec of the instrution *)
let get_params instr =
	match instr with
	Irg.AND_OP(_, param_list, _) ->
		param_list
	| _ ->
		assert false

(** Return the number of params of an instruction *)
let get_params_nb instr =
	match instr with
	Irg.AND_OP(_, param_list, _) ->
		List.length param_list
	| _ ->
		assert false


(** instantiate all known vars in a given expr
	@param instr	the spec whose params will give the vars to instantiate
	@param e	the expr to reduce *)
let reduce instr e =
	Instantiate.instantiate_in_expr e (get_params instr)

(** return the type of a symbol appearing in the spec of an instruction
	@param instr	spec of the instruction
	@param var_name	name of the symbol whose type is required *)
let get_type instr var_name =
	let rec search_param_list nam p_l =
	match p_l with
	[] ->
		raise Not_found
	| (str, t)::q ->
		if (String.compare str nam) == 0 then
			t
		else
			search_param_list nam q
	in
	match instr with
	Irg.AND_OP(_, p_l, _) ->
		search_param_list var_name p_l
	| _ ->
		assert false

module OrderedString = struct
	type t = string
	let compare v1 v2 = compare v1 v2
end
module StringMap = Map.Make(OrderedString)

(** Sort instructions according the order of the passed statistic list.
	@param instr_list	Instructions to sort.
	@param stat_list	Statistic list (instruction names sorted from
						less used to more used).
	@return				Sorted instruction list. *)
let sort_instr_set instr_list stat_list =
(*match stat_list with*)
	let rec assign lst map n =
		match lst with
		| [] -> map
		| name::t -> assign t (StringMap.add name n map) (n + 1) in
	let map = assign stat_list StringMap.empty 0 in
	let get i =
		try StringMap.find (get_name i)  map
		with Not_found -> -1 in
	
	List.sort (fun a b -> (get b) - (get a)) instr_list


(** sort instr into instr sets if multi defined *)
let enumerate_instr_sets i_l =
	let is_same_i_set sp1 sp2 =
		let a1 = try get_attr sp1 "instruction_set_select" with | Not_found -> EXPR Irg.NONE in
		let a2 = try get_attr sp2 "instruction_set_select" with | Not_found -> EXPR Irg.NONE in
		a1 = a2
	in
	let add_to_list l sp =
		match l with
		| [] -> []
		| a::b -> if is_same_i_set a sp then sp::l else l
	in
	let sort_inst sp l =
		match l with
		| [] -> [[sp]]
		| a::b ->
			(let res = List.map (fun x -> add_to_list x sp) l in
			if res = l then
				(* we found an instr from a new instr set *)
				[sp]::l
			else
				res)
	in(*
	let print_list l =
		match l with
		| [] -> print_string "[0] "
		| a::b -> 
			let cond =
				try
					(match (get_attr a "instruction_set_select") with
					| EXPR(e) -> e
					| _ -> failwith "should not happen ()")
				with | Not_found -> Irg.NONE
			in
			Printf.printf "[%d, cond=" (List.length l);
			Irg.print_expr cond;
			print_string "]\n"
	in*)
	let res = List.fold_left (fun a sp -> sort_inst sp a) [] i_l
	in
	(*  !!DEBUG!!
	print_string "[";List.iter print_list res;print_string "]\n"; *)
	multi_set := res


(** Get the list of instructions. If it has not been computed,
	compute it. *)
let get_insts _ =
	let is_defined id =
		try (ignore (Irg.get_symbol id); true)
		with Irg.Symbol_not_found _ -> false in

	let root_inst =
		if is_defined "multi" then "multi"
		else if is_defined "instruction" then "instruction"
		else raise (Sys_error "you must define a root for your instruction tree\n \"instruction\" for a single ISA\n \"multi\" for a proc with several ISA (like ARM/THUMB)")
	in

	(* initialization *)
	if !instr_set = [Irg.UNDEF] then
		(try
			instr_set :=  List.map check_coerce (Instantiate.instantiate_instructions root_inst);
			if !multi_set = [] then
				enumerate_instr_sets !instr_set;			
		with Instantiate.Error (sp, msg) ->
			raise (Irg.IrgError (Printf.sprintf "%s in instruction %s" msg (get_user_id sp))));
	
	(* return result *)
	!instr_set
  

(** Iteration over actual instruction using profiling order.
	@param fun_to_iterate	function to apply to each instr with an accumulator as 1st param
	@param init_val		the accumulator, initial value
     val iter : ('a -> Irg.spec -> 'a) -> 'a -> 'a
	*)
let iter_ext fun_to_iterate init_val with_profiling =
				
	(* if a profiling file is loaded instructions are sorted with the loaded profile_stats *)
	let old_inst_set = get_insts () in
	let insts = 
		if with_profiling && (List.length !instr_stats) <> 0
		then sort_instr_set old_inst_set !instr_stats
		else old_inst_set in

	(* actual instruction iterator *)
	let rec rec_iter f init instrs params_to_unstack attrs_to_unstack =

		(* unstack preivous attributes and parameters *)
		Irg.param_unstack params_to_unstack;
		Irg.attr_unstack attrs_to_unstack;

		(* look the list *)
		match instrs with
		[] ->
			init
		| a::b ->
			match a with
			
			| Irg.AND_OP(_, param_l, attr_l) ->
				Irg.param_stack param_l;
				Irg.attr_stack attr_l;
				rec_iter f (f init a) b param_l attr_l;
				
			| _ ->
				Printf.printf "nb inst %d, " (List.length !instr_set);
				print_string "should failwith:\n";
				Irg.print_spec a;
				rec_iter f (f init a) b [] [] in

	rec_iter fun_to_iterate init_val insts [] []


(** iterator (or fold) on the structure containing all the instructions specs
	@param fun_to_iterate	function to apply to each instr with an accumulator as 1st param
	@param init_val		the accumulator, initial value
     val iter : ('a -> Irg.spec -> 'a) -> 'a -> 'a
*)
let iter fun_to_iterate init_val = iter_ext fun_to_iterate init_val false


(** Perform a transformation on the attributes of the instructions.
	@param f	Function to apply (data -> parameter list -> attribute list -> parameter list x attribute list)
	@param d	Data used by the transformation.
	@return		(new data, new parameter list, new attribute list) *)
let transform f d =
	let d, insts =
		List.fold_left
			(fun (d, l) i ->
				match i with
				| Irg.AND_OP(n, pl, al) ->
					let d, pl, al = f d pl al in (d, (Irg.AND_OP (n, pl, al))::l)
				| _ -> (d, l))
			(d, [])
			(get_insts ()) in
		instr_set := List.rev insts;
		enumerate_instr_sets !instr_set;
		d

	
(** Compute the maximum params numbers of all instructions
	from the current loaded IRG
*)
let get_params_max_nb () =
	let aux acc i =
		let nb_params = get_params_nb i
		in
		  if nb_params > acc
		  then nb_params
		  else acc
	in
	  iter aux 0



(** returns the length of a given instruction, based on the image description *)
let get_instruction_length sp =
	(* return the string of a given Irg.expr which is supposed to be an image attribute *)
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
	let get_expr_from_iter_value v  =
		match v with
		| EXPR(e) -> e
		| _ -> failwith "shouldn't happen (iter.ml::get_instruction_length::get_expr_from_iter_value)"
	in
	(* return the length (in bits) of an argument whose param code (%8b e.g.) is given as a string *)
	let get_length_from_format f =
		let l = String.length f in
		let new_f =
			if l<=2 then
				raise (Sys_error (Printf.sprintf "forbidden format string \"%s\" in image \"%s\" for instruction \"%s\""
					f
					(get_str (get_expr_from_iter_value (get_attr sp "image")))
					(get_str (get_expr_from_iter_value (get_attr sp "syntax"))) 
				))
			else String.sub f 1 (l-2)
		in
		Scanf.sscanf new_f "%d" (fun x->x)
	in
	(* remove any space (space or tab char) in a string, return a string as result *)
	let remove_space s =
		Str.global_replace (Str.regexp "[ \t]+") "" s
	in
	let rec get_length_from_regexp_list l =
		match l with
		| [] -> 0
		| h::t ->
			(match h with
			Str.Text(txt) ->
				(* here we assume that an image contains only %.. , 01, X or x *)
				(String.length txt) + (get_length_from_regexp_list t)
			| Str.Delim(d) ->
				(get_length_from_format d) + (get_length_from_regexp_list t)
			)
	in
	get_length_from_regexp_list (Str.full_split (Str.regexp "%[0-9]*[bdfxs]") (remove_space (get_str (get_expr_from_iter_value (get_attr sp "image")))))
