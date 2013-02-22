(*
 * GLISS2 -- fetch module
 * Copyright (c) 2008, IRIT - UPS <casse@irit.fr>
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

(*
  Here is the module in charge of the generation of the fetch_table.h
*)
(*
  Useful list of dependencies in order to work with the interactive Ocaml toplevel :
  (Do not forget to do make to have the latest version of the cmo binaries)

  #directory "../irg";;
  #directory "../gep";;
  
  #load "unix.cma";;
  #load "str.cma";;
  #load "config.cmo";;
  #load "irg.cmo";;
  #load "instantiate.cmo";;
  #load "lexer.cmo";;
  #load "sem.cmo";;
  #load "IdMaker.cmo";;
  #load "iter.cmo";;
  #load "toc.cmo";;
*)

(* flag: output or not fetch tables stats *)
let output_fetch_stat    = ref false



(*********************************************************************
 **    mask calculation, generic, for RISC or CISC
 ********************************************************************)


(** perform an AND between the mask of all the instrs in spec_list *)
let rec spec_list_mask sp_list =
	let rec aux sl =
	match sl with
	| [] -> Bitmask.void_mask
	| [a] -> Bitmask.get_mask a
	| h::t ->
		let (m1, m2) = Bitmask.set_same_length (Bitmask.get_mask h) (aux t) false in
		Bitmask.logand m1 m2
	in
	aux sp_list


(* "name" of the tree (list of mask int : all vals on mask beginning from the top), list of the instr, local mask, global mask (from ancestors), list of sons *)
type dec_tree = DecTree of Bitmask.bitmask list * Irg.spec list * Bitmask.bitmask * Bitmask.bitmask * dec_tree list


let print_dec_tree tr =
	let name_of t =
		let rec aux l s =
			match l with
			[] ->
				s
			| a::b ->
				let sa = Bitmask.to_string a in
				aux b (if (String.length s)=0 then sa else (s ^ "_" ^ sa))
		in
		match t with
		DecTree(i, s, m, g, d) ->
			aux i ""
	in
	match tr with
	DecTree(int_l, sl, msk, gm, dt_l) ->
		(Printf.printf "[[TREE, name : %s, " (name_of tr);
		Printf.printf "mask : %s, " (Bitmask.to_string msk);
		Printf.printf "global : %s\n" (Bitmask.to_string gm);
		Printf.printf "spec list: ";
		if sl == [] then print_string "<none>\n" else
			print_char '\n';
			List.iter (fun x -> Printf.printf "\t%12s%20s, mask=%s, val=%s, val_mask=%s\n"
			(Irg.name_of x)
			(Iter.get_name x)
			(Bitmask.to_string (Bitmask.get_mask x))
			(Bitmask.to_string (Bitmask.get_value_mask x))
			(Bitmask.to_string (Bitmask.get_value x)))
			sl;
		Printf.printf "]]\n")



let print_dec_tree_list tl =
	List.iter (fun x -> begin print_char '\n'; print_dec_tree x end) tl


let get_local_mask_length dt =
	match dt with
	DecTree(_, _, ml, _, _) ->
		Bitmask.bit_count ml


let get_global_mask_length dt =
	match dt with
	DecTree(_, _, _, mg, _) ->
		Bitmask.bit_count mg

let get_instr_list dt =
	match dt with
	DecTree(_, sl, _, _, _) ->
		sl


(*!!DEBUG!!*)
let get_image sp =
	match Iter.get_attr sp "image" with
	| Iter.EXPR(e) -> e
	| _ -> failwith "should not happen (fetch.ml::get_image)"


let create_son_list_of_dec_node dt =
	let rec aux msk sl =
		(match sl with
		[] ->
			(* one instr => terminal node *)
			[]
		| a::b ->
			((Bitmask.masked_value (Bitmask.get_value_mask a) msk), a)::(aux msk b)
		)
	in
	match dt with
	DecTree(i_l, s_l, msk, gm, dt_l) ->
		(* !!DEBUG!! *)
		(*Printf.printf "create_son_list_of_dec_node, msk=%s, gm=%s\n" (Bitmask.to_string msk) (Bitmask.to_string gm);

		List.iter (fun a -> print_string ("creating dec_node son, val_on_mask=");
			Printf.printf "%s, (inst_mask=%s, inst_val=%s), spec="
				(Bitmask.to_string (Bitmask.masked_value (Bitmask.get_value_mask a) msk))
				(Bitmask.to_string (Bitmask.get_mask a))
				(Bitmask.to_string (Bitmask.get_value_mask a));
			Irg.print_expr (get_image a);
			print_char '\n')
			s_l;*)
		
		aux msk s_l


let sort_son_list vl =
	(* add one instr with a given value on mask to a tuple list (with inst list like the top result expected) *)
	let rec add_instr_in_tuple_list l (v, sp) =
		match l with
		[] ->
			[(v, [sp])]
		| a::b ->
			(match a with
			(vv, sl) ->
				if Bitmask.is_equals v vv then
					(vv, sp::sl)::b
				else
					a::(add_instr_in_tuple_list b (v,sp))
			)
	in
	(*!!DEBUG!!*)
	(*print_string "sort_son_list, sons to sort:\n";
	List.iter (fun a -> Printf.printf " mask=%s, spec=" (Bitmask.to_string (fst a)); Irg.print_expr (get_image (snd a)); print_char '\n')
		vl;
	let res =*)
	List.fold_left add_instr_in_tuple_list [] vl
	(*in
	print_string "sons sorted=[\n";
	List.iter (fun x -> Printf.printf "v=%s\n" (Bitmask.to_string (fst x)); List.iter (fun y -> print_string "    "; Irg.print_expr (get_image y); print_char '\n') (snd x); print_string "]\n")
		res;
	res*)


(* with the result of the previous function we can build the list of dec_tree associated,
we just have to calculate the local masks to do this we need the father's masks,
we also need the father's vals on mask to add the new one,
by default all trees will be created with no link between them (no tree structure) *)
let rec build_dectrees vl msk gm il =
	match vl with
	| [] -> []
	| a::b ->
			let v = fst a in
			let sl = snd a in
			let common_mask = spec_list_mask sl in
			(*!!DEBUG!!*)
			(*print_string "build_dectrees\n";
			Printf.printf "common mask=%s, %d instr\n" (Bitmask.to_string common_mask) (List.length sl);*)

			let dt = DecTree(il@[v], sl, Bitmask.unmask common_mask gm, common_mask, [])(* ::(build_dectrees b msk gm il) *)
			in
			(* !!DEBUG!! *)
			(*print_string "build_dectrees res = ";
			print_dec_tree dt;
			print_string "build_tree, sl=[\n";
			List.iter (fun x -> print_string "    "; Irg.print_expr (get_image x); print_char '\n') sl;
			print_string "]\n";*)
			
			dt::(build_dectrees b msk gm il)


let build_sons_of_tree tr =
	match tr with
	DecTree(int_l, sl, msk, gm, dt_l) ->
		let res = build_dectrees (sort_son_list (create_son_list_of_dec_node tr)) msk gm int_l
		in
		if (List.length res) == 1 && (List.length (get_instr_list (List.hd res))) > 1 then
			(output_string stderr "ERROR: some instructions seem to have same opcode:\n";
			(*List.iter (fun x -> Printf.fprintf stderr "%s, " (Irg.name_of x)) (get_instr_list (List.hd res));*)
			let expr_from_value v =
				match v with
				| Iter.EXPR(e) -> e
				| _ -> failwith "should not happen (fetch.ml::build_sons_of_tree::expr_from_value)"
			in
			List.iter
				(fun x ->
					Printf.fprintf stderr "\t%s: image=" (Iter.get_user_id x);
					Irg.output_expr stderr (expr_from_value (Iter.get_attr x "image"));
					output_char stderr '\n')
				(get_instr_list (List.hd res));
			output_string stderr "\n";
			raise (Sys_error "cannot continue with 2 instructions with same image"))
		else
			res


(**
	@param sp_l		List of instruction specifications.
	@return			*)
let build_dec_nodes sp_l =
	let node_cond (DecTree(_, sl, lmask, gmask, _)) =
		((List.length sl)<=1)
	in
	let rec stop_cond l =
		match l with
		| [] ->
			(*print_string "stop_cond=true\n";flush stdout;*)
			true
		| a::b ->
			if node_cond a then
			((*print_string "stop_cond=[rec]\n";flush stdout;*)
				stop_cond b )
			else
			((*print_string "stop_cond=false\n";flush stdout;*)
				false )
	in
	let get_sons x =
		match x with
		| DecTree(int_l, sl, msk, gm, dt_l) ->
			if (List.length sl)>1 then
				DecTree(int_l, [], msk, gm, dt_l)::(build_sons_of_tree x)
			else
				[x]
	in
	let rec aux dl =
		if stop_cond dl then
			dl
		else
			aux (List.flatten (List.map get_sons dl))
	in
	let specs = sp_l in
	let mask = spec_list_mask specs in
	(*!!DEBUG!!*)
	let res =
	
	aux [DecTree([], specs, mask, mask, [])]

	in
	Printf.printf ", res: %d nodes\n" (List.length res);
	res


(* returns a list of the direct sons of a given DecTree among a given list *)
let find_sons_of_node node d_l =
	let get_name d =
		match d with
		DecTree(name, _, _, _, _) ->
			name
	in
	let length_of_name d =
		List.length (get_name d)
	in
	(* return true if l1 is a sub list at the beginning of l2,
	ie if l2 can be the name of a son (direct or not) of a DecTree whose name is l1,
	l1 and l2 are supposed to be two Bitmask list representing a name of a DecTree *)
	let rec is_sub_name l1 l2 =
		match l1 with
		[] ->
			true
		| a1::b1 ->
			(match l2 with
			[] ->
				false
			| a2::b2 ->
				if Bitmask.is_equals a1 a2 then
					(is_sub_name b1 b2)
				else
					false
			)
	in
	(* return true if d1 is a direct son of d2, false otherwise *)
	let is_son d1 d2 =
		if (length_of_name d1) = ((length_of_name d2) + 1) then
			if (is_sub_name (get_name d2) (get_name d1)) then
				true
			else
				false
		else
			false
	in
	List.flatten (List.map (fun d -> if (is_son d node) then [d] else [] ) d_l)


exception CheckIsizeException
(* special value for fetch size, represent generic fetch (CISC) *)
let fetch_generic = 0


(* outputs the declaration of all structures related to the given DecTree dt in C language,
all needed Decode_Ent and Table_Decodage structures will be output and already initialised,
everything will be output in the given channel,
dl is the global list of all nodes, used to find sons for instance *)
let output_table_C_decl fetch_size suffix out fetch_stat dt dl =
(*!!DEBUG!!*)
(*Printf.printf "output_table_C_decl, fsize=%d, suffix=%s, #dl=%d\n" fetch_size suffix (List.length dl);
print_dec_tree dt;
print_string "<<<<<<<<<<<<<<<<<<<<\n";*)
	let name_of t =
		let correct_name s =
			if s = "" then
				s
			else
				"_" ^ s
		in
		let rec aux l s =
			match l with
			| [] -> s
			| a::b ->
				let sa = Bitmask.to_string a in
				aux b (if (String.length s) == 0 then sa else (s ^ "_" ^ sa))
		in
		match t with
		| DecTree(i, s, m, g, d) -> correct_name (aux i "")
	in
	let sz_l_mask = get_local_mask_length dt in
	let num_dec_ent =
	(* we hope we never have a too big mask, we don't want to produce
	 * fetch tables with millions of entries (most of them void).
	 * if that unfortunate case happen => change the algorithm *)
	(* don't forget also that caml int are 31 bit long,
	 * let's limit ourself below this limit *)
	 (* deactivated temporarily for testing with nml with only 1 instr (==> full length mask) *)
		if sz_l_mask > 32 then
			failwith "shouldn't happen? mask too big (fetch.ml::output_table_C_decl::num_dec_ent)"
		else
			1 lsl sz_l_mask
	in
	let name = (suffix ^ (name_of dt)) in
	let type_suffix =
		if (List.length !Iter.multi_set) > 1 then
			(if fetch_size != 0 then Printf.sprintf "_%d" fetch_size else "_CISC")
		else
			""
	in
	let l_mask =
		match dt with
		| DecTree(_, _, lm, _, _) -> lm
	in
	let info = Toc.info () in
	let sons = find_sons_of_node dt dl in
	(* is i the last element of i_l? i is an int, i_l is an string int list *)
	let rec is_suffix i i_l =
		match i_l with
		| a::b ->
			if b=[] then
				i == (Bitmask.to_int a)
			else
				is_suffix i b
		| [] -> false
	in
	let exists_in i d_l =
		let predicate x =
			match x with
			| DecTree(i_l, _, _, _, _) -> is_suffix i i_l
		in
		List.exists predicate d_l
	in
	let get_i_th_son i d_l =
		let predicate x =
			match x with
			| DecTree(i_l, _, _, _, _) -> is_suffix i i_l
		in
		List.find predicate d_l
	in (* the way the nodes are built implies that a terminal node is a node containing spec of 1 instruction, the other nodes being "empty" *)
	let is_terminal_node d =
		match d with
		| DecTree(_, s, _, _, _) -> s != []
	in
	(* returns the spec of a supposed terminal node *)
	let get_spec_of_term d =
		match d with
		| DecTree(_, s, _, _, _) -> List.hd s
	in
	(* returns the number of instruction nodes produced *)
	let produce_i_th_son i =
		if exists_in i sons then
			if is_terminal_node (get_i_th_son i sons) then
			(* TODO: decode or not decode ? *)
				(let x = get_spec_of_term (get_i_th_son i sons)
				in
				Printf.fprintf out "/* 0X%X,%d */\t{INSTRUCTION, (void *)%s}" i i ((String.uppercase info.Toc.proc) ^ "_" ^ (String.uppercase (Iter.get_name x)));
				Printf.fprintf out "\t/* %s, %d bits, mask=%s, val=%s */" (String.uppercase (Iter.get_name x)) (Iter.get_instruction_length x) (Bitmask.to_string (Bitmask.get_mask x)) (Bitmask.to_string (Bitmask.get_value_mask x));
				1)
			else
				(Printf.fprintf out "/* 0X%X,%d */\t{TABLEFETCH, &_table%s}" i i (suffix ^ (name_of (get_i_th_son i sons)));
				0)
		else
			(Printf.fprintf out "{INSTRUCTION, %s_UNKNOWN}" (String.uppercase info.Toc.proc);
			0)
	in
	let rec produce_decode_ent i nb_nodes =
		if i >= num_dec_ent then
			nb_nodes
		else
			(Printf.fprintf out "\t";
			let nb = produce_i_th_son i in
			if i = (num_dec_ent-1) then
				Printf.fprintf out "\n"
			else
				Printf.fprintf out ",\n";
			produce_decode_ent (i+1) (nb_nodes + nb))
	in
	let to_C_list mask =
		let list = Bitmask.to_int32_list mask in
		let rec aux comma l =
			match l with
			| [] -> ""
			| a::b ->
				((if comma then ", " else "") ^ (Printf.sprintf "0X%lX" a)) ^ (aux true b)
		in
			aux false list
	in
	(* !!DEBUG!! *)(*
	match dt with
	DecTree(i_l, s_l, lm, gm, ss) ->
	print_string ("node treated[" ^ (name_of dt) ^ "], spec=[");
	List.iter (fun x -> (Printf.printf "(%s), " (Iter.get_name x))) s_l;
	print_string  "], sons=[";
	List.iter (fun x -> (Printf.printf "(%s), " (name_of x))) ss;
	print_string "]\n";*)
	if is_terminal_node dt then
		(* !!DEBUG!! *)
		(*print_string ((name_of dt) ^ ": [[terminal node]]\n")*)
		()
	else
		(* !!DEBUG!! *)
		(*print_string ((name_of dt) ^ ": [[normal node]]\n");*)
( (*print_string "tree_to_C :"; print_dec_tree dt;*)
		Printf.fprintf out "static Decode_Ent table_table%s[%d] = {\n" name num_dec_ent;
		let nb_nodes = produce_decode_ent 0 0 in
		Printf.fprintf out "};\n";
		if fetch_size != fetch_generic then
			Printf.fprintf out "static Table_Decodage%s _table%s = {0X%LX%s, table_table%s};\n" type_suffix name (Bitmask.to_int64 l_mask) (Bitmask.c_const_suffix l_mask) name
		else
			(Printf.fprintf out "static uint32_t tab_mask%s[%d] = {%s};\n" name (List.length (Bitmask.to_int32_list l_mask)) (to_C_list l_mask);
			Printf.fprintf out "static mask_t mask%s = {\n\ttab_mask%s," name name;
			Printf.fprintf out "\t%d};\n" (Bitmask.length l_mask);
			Printf.fprintf out "static Table_Decodage%s _table%s = {&mask%s, table_table%s};\n" type_suffix name name name
			);
		Printf.fprintf out "Table_Decodage%s *%s_table%s = &_table%s;\n" type_suffix info.Toc.proc name name;
		Printf.fprintf out "\n";

		if !output_fetch_stat then
			Printf.fprintf fetch_stat "%8d/%8d, name=%s\n" nb_nodes num_dec_ent name)


(* sort the DecTree in a given list according to a reverse pseudo-lexicographic order among the name of the DecTrees *)
let sort_dectree_list d_l =
	let name_of t =
		match t with
		DecTree(i, _, _, _, _) ->
			i
	in
	(* same specification as a standard compare function, x>y means x is "bigger" than y (eg: 12_3 < 13_3_5, "nothing" < x for any x) *)
	let rec comp_gen_int_list x y =
		match x with
		| [] -> -1
		| x1::x2 ->
			(match y with
			| [] -> 1
			| y1::y2 ->
				let diff = Bitmask.compare x1 y1 in
				if diff == 0 then
					comp_gen_int_list x2 y2
				else
					diff
			)
	in
	let comp_fun x y =
		comp_gen_int_list (name_of y) (name_of x)
	in
	(*!!DEBUG!!*)
	(*Printf.printf "sort, %d nodes\n" (List.length d_l);
	List.iter print_dec_tree d_l;
	print_string "sort, end\n";*)
	List.sort comp_fun d_l


(** Compute list of fetch sizes.
	@param spec_list	List of instructions.
	@return				Lits of existing sizes. *)	
let find_fetch_size spec_list =
	let isize = Irg.get_isize () in
	let is_isize = isize != [] in

	(* returns (min(l), max(l)) for a list l *)
	let get_min_max_from_list l =
		let min_fun a b_i = if b_i < a then b_i else a in
		let max_fun a b_i = if b_i > a then b_i else a in
		(List.fold_left min_fun (List.hd l) (List.tl l),
		 List.fold_left max_fun (List.hd l) (List.tl l))
	in

	(* return list of different inst sizes for a given inst list *)
	let get_sizes sp_l =
		let rec aux l accu =
			match l with
			| [] -> accu
			| a::b ->
				let s = Iter.get_instruction_length a in
				if is_isize  && not (List.mem s isize) then
					raise (Toc.Error (Printf.sprintf "bad size for instruction %s (%d bits)" (Iter.get_user_id a) s));
				if List.exists (fun x -> x == s) accu then
					aux b accu
				else
					aux b (s::accu)
		in
		aux sp_l []
	in

	(* list of the specialized fixed fetch sizes (for RISC ISA),
	 * they correspond to the size of C's standard integer types (uintN_t)
	 * other or variable sizes imply use of generic fetch and decode *)
	let fetch_sizes = [8; 16; 32; 64] in

	(* find a standard fetch size from bounds of instr length *)
	let get_fetch_size_from_min_max min_max =
		let (min_size, max_size) = min_max
		in
		if min_size == max_size then
			(* constant size instrs (RISC) *)
			(try
				List.find (fun x -> min_size == x) fetch_sizes
			with
			Not_found ->
				(* constant size but not implemented => generic *)
				fetch_generic)
		else
			(* variable size or diff *)
			fetch_generic
	in
	
	let choose_fetch_size sp_l =
		let sizes = get_sizes sp_l in
		let min_max = get_min_max_from_list sizes in
		get_fetch_size_from_min_max min_max
	in

	choose_fetch_size spec_list


(* output table struct C decl, if idx >= 0 struct name will be suffixed *)
let output_struct_decl out fetch_size idx =
	let suffix = if idx < 0 then "" else (string_of_int idx) in
	if fetch_size == fetch_generic then
		(* generic, mask is not an uintN_t here *)
		output_string out ("typedef struct {\n\tmask_t\t*mask;\n\tDecode_Ent\t*table;\n} Table_Decodage" ^ suffix ^ ";\n\n")
	else
		(Printf.fprintf out "typedef struct {\n\tuint%d_t\tmask;\n" fetch_size;
		output_string out ("\tDecode_Ent\t*table;\n} Table_Decodage" ^ suffix ^ ";\n\n"))


(** output a table C decl, if idx >= 0 table name will be suffixed *)
let output_table out sp_l fetch_size idx fetch_stat =
	let suffix = if idx < 0 then "" else ("_" ^ (string_of_int idx)) in
	let aux dl dt = output_table_C_decl fetch_size suffix out fetch_stat dt dl in
	let dl = 
		(* !!DEBUG!! *)
		(*Printf.printf "output_table, #sp_l=%d, fetch_size=%d, idx=%d\n" (List.length sp_l) fetch_size idx;*)
		sort_dectree_list (build_dec_nodes sp_l)
	in
	List.iter (aux dl) dl


(** output all C struct declarations and fetch tables *)
let output_all_table_C_decl out =
	let iss = !Iter.multi_set in
	let iss_sizes = List.map (fun x -> (find_fetch_size x, x)) iss in
	let num_iss = List.length iss in
	let fetch_stat = if !output_fetch_stat then open_out ((Irg.get_proc_name ()) ^ "_fetch_tables.stat") else stdout in
	(* table and struct names must be suffixed if several tables generated *)
	let idx = ref (-1) in
	(*List.iter (fun x -> (output_struct_decl out (fst x) !idx); idx := !idx + 1) iss_sizes;*)
	idx := if num_iss > 1 then 0 else -1;
	List.iter
		(fun x ->
			if num_iss > 1 then
				(Printf.fprintf out "\n/* Table set number %d */\n\n\n" !idx;
				if !output_fetch_stat then
					Printf.fprintf fetch_stat "Table set number %d\n" !idx);
			output_table out (snd x) (fst x) !idx fetch_stat;
			idx := !idx + 1)
		iss_sizes;
	if !output_fetch_stat then close_out fetch_stat


(*************************************************
 **                 some testing
 *************************************************)


let test_build_dec_nodes n =
	match n with
	0 ->
		begin
		Printf.printf "\n\ntest build decode nodes\n";
		print_dec_tree_list (build_dec_nodes [])
		end
	| _ ->
		()

let test_sort _ =
	let name_of t =
		let rec aux l s =
			match l with
			[] ->
				s
			| a::b ->
				let sa = Bitmask.to_string a in
				aux b (if (String.length s) == 0 then sa else (s^"_"^sa))
		in
		match t with
		DecTree(i, s, m, g, d) ->
			aux i "name:"
	in
	let dl = (*sort_dectree_list ( *)build_dec_nodes []
	in
	let aux x =
		Printf.printf "%s\n" (name_of x)
	in
	print_string "let's test!\n";
	List.iter aux dl



(************************************************************************)
(**                         dot related                                **)
(************************************************************************)


(* print a node's informations to be used in dot format *)
let print_dot_dec_tree tr =
	let name_of t =
		let rec aux l s =
			match l with
			[] ->
				s
			| a::b ->
				let sa = Bitmask.to_string a in
				aux b (s^"_"^sa)
		in
		match t with
		DecTree(i, s, m, g, d) ->
			aux i ""
	in
	let spec_list_of t =
		let rec aux l s =
			match l with
			[] ->
				s
			| a::b ->
				aux b (s^"\\l"^(Iter.get_name a))
		in
		match t with
		DecTree(i, s, m, g, d) ->
			aux s ""
	in
	match tr with
	DecTree(int_l, sl, msk, gm, dt_l) ->
		begin
		print_string "\"";
		print_string (name_of tr);
		print_string "\" ";
		print_string "[label=\"{";
		print_string (name_of tr);
		print_string " | ";
		print_string (spec_list_of tr);
		print_string "}\"]"
		end

(* returns a list of all edges that would be present if the given list of DecTree
was to be represented as a tree, used to build the graph in dot format,
the result is a list of sub-lists symbolizing each edge containing 2 elements, the head and the tail of an edge *)
let get_edges_between_dec_tables dl =
	let make_edge src sons_list =
		List.map (fun x -> [src; x]) sons_list
	in
	List.flatten (List.map (fun x -> make_edge x (find_sons_of_node x dl)) dl)


let print_dot_edges edge =
	let name_of t =
		let rec aux l s =
			match l with
			[] ->
				s
			| a::b ->
				let sa = Bitmask.to_string a in
				aux b (s^"_"^sa)
		in
		match t with
		DecTree(i, s, m, g, d) ->
			aux i ""
	in
	match edge with
	[a; b] ->
		begin
		Printf.printf "\"%s\"" (name_of a);
		Printf.printf " -> ";
		Printf.printf "\"%s\"" (name_of b);
		end
	| _ ->
		()

		
let print_dot_dec_tree_list tl =
	begin
	print_string "digraph DEC {\n";
	print_string "node [shape=Mrecord, labeljust=1, fontsize=10];\n";
	List.iter (fun x -> begin print_dot_dec_tree x; print_string "\n" end) tl;
	List.iter (fun x -> begin print_dot_edges x; print_string "\n" end) (get_edges_between_dec_tables tl);
	print_string "}\n"
	end
