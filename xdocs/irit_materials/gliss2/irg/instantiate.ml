open Irg

exception Error of  (Irg.spec * string)

(* functions dealing with instruction instantiation *)


(**
	check if a statement attribute of a given instruction is recursive,
	ie, if it calls itself
	@param	sp	the spec of the instruction
	@param	name	the name of the statement attribute to check
	@return		true if the attribute is recursive,
			false otherwise
*)
let is_stat_attr_recursive sp name =
	let get_attr sp n =
		let rec aux al =
			match al with
			| [] ->
				raise (Error (sp, (Printf.sprintf "attribute %s not found" name)))
			| ATTR_STAT(nm, s)::t ->
				if (String.compare nm n) == 0 then
					s
				else
					aux t
			| _::t -> aux t
		in
		match sp with
		| AND_OP(_, _, attrs) ->
			aux attrs
		| AND_MODE(_, _, _, a_l) ->
			aux a_l
		| _ ->
			failwith "shouldn't happen (instantiate.ml::is_stat_attr_recursive::get_attr)"
	in
	(* return true if there is a call to a stat attr whose name is str in the st stat,
	we look for things like 'EVAL(str)' *)
	let rec find_occurence str st =
		match st with
		| NOP ->
			false
		| SEQ(s1, s2) ->
			(find_occurence str s1) || (find_occurence str s2)
		| EVAL(s) ->
			((String.compare s str) == 0)
		| EVALIND(n, attr) ->
			(* recursivity occurs only when we refer to oneself, 'EVALIND' always refers to another spec *)
			false
		| SET(l, e) ->
			false
		| CANON_STAT(n, el) ->
			false
		| ERROR(s) ->
			false
		| IF_STAT(e, s1, s2) ->
			(find_occurence str s1) || (find_occurence str s2)
		| SWITCH_STAT(e, es_l, s) ->
			(find_occurence str s) || (List.exists (fun (ex, st) -> find_occurence str st) es_l)
		| SETSPE(l, e) ->
			false
		| LINE(s, i, st) ->
			find_occurence str st
		| INLINE _ ->
			false
	in
	let a = get_attr sp name
	in
	(* !!DEBUG!! *)
	(*print_string ("is_attr_rec, sp="^(Irg.name_of sp)^", name="^name^"\n");*)
	find_occurence name a


(**
	get the statement associated with some statement attribute in a spec
	@param	name_attr	name of the wanted attribute
	@param	sp	spec supposed to contain the attribute
	@return		the correct attribute if found, Irg.NOP if not or if it is not a statement attribute
	@raise	failwith	fails if sp doesn't refer to a mode or an op
*)
let get_stat_from_attr_from_spec sp name_attr =
	let rec get_attr n a =
		match a with
		[] ->
		(* if attr not found => means an empty attr (?) *)
			NOP
		| ATTR_STAT(nm, s)::t ->
			if (String.compare nm n) == 0 then
				s
			else
				get_attr n t
		| _::t -> get_attr n t
	in
	(* !!DEBUG!! *)
	(*print_string ("get_s_a_from_e, sp="^(Irg.name_of sp)^", name_attr="^name_attr^"\n");*)
		match sp with
		  AND_OP(_, _, attrs) ->
			get_attr name_attr attrs
		| AND_MODE(_, _, _, attrs) ->
			get_attr name_attr attrs
		| _ ->
			failwith ("trying to access attribute " ^ name_attr ^ " of spec " ^ (name_of sp) ^ " which is neither an OP or a MODE (instantiate.ml::get_stat_from_attr_from_spec)")



(*******************************************)
(* var substitution and renaming functions *)
(*******************************************)



(* different possibilities for var instantiation,
prefix the new name by the old name,
replace the old name by the new name,
keep the old name *)
(*type var_inst_type =
	Prefix_name
	| Replace_name
	| Keep_name
*)
(** look a given spec (mode or op), return true if the new vars from a substition of a var (with the given name) of this spec type should be prefixed,
false if the var name can simply be replaced,
top_params is the list of params of a spec in which we want to instantiate a var of sp type (string * typ list)

sp = 1 param mode => Keep_name (unicity in the spec to instantiate => we can simply keep the name)
sp = * param mode => Prefix_name
sp = 1 param op   => Replace_name, or Prefix_name if more than 1 param of sp type in the top spec or if the new name would conflict
sp = * param op   => Replace_name, or Prefix_name if more than 1 param of sp type in the top spec or if one of the new names would conflict
*)
(*let check_if_prefix_needed sp name top_params =
	let given_spec_name =
		match sp with
		AND_OP(n, _, _) ->
			n
		| AND_MODE(n, _, _, _) ->
			n
		| _ ->
			failwith "shouldn't happen? (instantiate.ml::check_if_prefix_needed::spec_name)"
	in
	let is_param_of_given_type p =
		match p with
		(_, t) ->
			(match t with
			TYPE_ID(n) ->
				n = given_spec_name
			| _ ->
				false
			)
	in
	let rec count_same_type_params p_l =
		match p_l with
		[] ->
			0
		| a::b ->
			if is_param_of_given_type a then
				1 + (count_same_type_params b)
			else
				count_same_type_params b
	in
	let name_of_param p =
		match p with
		(n, _) ->
			n
	in
	let sp_param_names =
		match sp with
		AND_OP(_, p_l, _) ->
			List.map name_of_param p_l
		| AND_MODE(_, p_l, _, _) ->
			List.map name_of_param p_l
		| _ ->
			failwith "shouldn't happen? (instantiate.ml::check_if_prefix_needed::get_sp_param_names)"
	in*)
	(* return true if a param whose name is in n_l is existing in the given param list *)
	(*let rec is_name_conflict p_l n_l =
		match p_l with
		[] ->
			false
		| a::b ->
			((name_of_param a) = n) || (is_conflict_with_name b)
	in*)
	(*match sp with
	AND_OP(_, params, _) ->
		if ((count_same_type_params top_params) > 1) || (is_conflict_with_name top_params) then
				Prefix_name
			else
				Replace_name
	| AND_MODE(_, params, _, _) ->
		if List.length params > 1 then
			Prefix_name
		else
			Keep_name
	| _ ->
		failwith "shouldn't happen? (instantiate.ml::check_if_prefix_needed)"
*)



(**
	get the expression associated with some expression attribute in a spec
	@param	name_attr	name of the wanted attribute
	@param	sp	spec supposed to contain the attribute
	@return		the correct attribute if found, Irg.NONE if not or if it is not an expression attribute
	@raise	failwith	fails if sp doesn't refer to a mode or an op
*)
let get_expr_from_attr_from_op_or_mode sp name_attr =
	let rec get_attr n a =
		match a with
		[] ->
		(* if attr not found => means an empty attr (?) *)
		(* !!DEBUG!! *)
		(*print_string ("attr of name " ^ name_attr ^ " not found in spec=\n");
		print_spec sp;*)
			NONE
		| ATTR_EXPR(nm, e)::t ->
			if (String.compare nm n) == 0 then
				e
			else
				get_attr n t
		| _::t -> get_attr n t
	in
	(* !!DEBUG!! *)
	(*print_string ("get_e_from_a_from_o|m, sp="^(Irg.name_of sp)^", name_attr="^name_attr^"\n");*)
		match sp with
		AND_OP(_, _, attrs) ->
			get_attr name_attr attrs
		| AND_MODE(_, _, _, a_l) ->
			get_attr name_attr a_l
		| _ ->
			failwith "cannot get an expr attribute from not an AND OP or an AND MODE (instantiate.ml::get_expr_from_attr_from_op_or_mode)"

(**
	instantiate in an expression, the given var name is supposed
	to represent an entity of the given spec op. each occurence of
	name (Irg.FIELDOF(_, name, _) or Irg.REF(name) ) is replaced respectively by
	an attribute of op or its mode value (if op is a mode)
	@param	name	name of the var to instantiate
	@param	op	spec of the entity represented by the var of given name
	@param	ex	expression in which we instantiate the given var
	@return		the instantiated expression
*)
let rec substitute_in_expr name op ex =
	let is_and_mode sp =
		match sp with
		AND_MODE(_, _, _, _) -> true
		| _ -> false
	in
	let get_mode_value sp =
		match sp with
		AND_MODE(_, _, v, _) -> v
		| _-> NONE
	in
	(* !!DEBUG!! *)
	(*print_string ("subs_in_e name="^name^", op="^(Irg.name_of op)^", expr=");
	Irg.print_expr ex; print_char '\n';*)
	match ex with
	NONE ->
		NONE
	| COERCE(te, e) ->
		COERCE(te, substitute_in_expr name op e)
	| FORMAT(s, e_l) ->
		(* apply same treatment to any expr, then reduce formats elsewhere *)
		FORMAT(s, List.map (substitute_in_expr name op) e_l)
	| CANON_EXPR(te, s, e_l) ->
		CANON_EXPR(te, s, List.map (substitute_in_expr name op) e_l )
	| REF(s) ->
		(* change if op is a AND_MODE and s refers to it *)
		if (name=s)&&(is_and_mode op) then
			get_mode_value op
		else
			(* ? change also if s refers to an ATTR_EXPR of the same spec, does it have this form ? *)
			REF(s)
	| FIELDOF(te, s1, s2) ->
		if (String.compare s1 name) == 0 then
			get_expr_from_attr_from_op_or_mode op s2
		else
			FIELDOF(te, s1, s2)
	| ITEMOF(te, e1, e2) ->
		ITEMOF(te, e1, substitute_in_expr name op e2)
	| BITFIELD(te, e1, e2, e3) ->
		BITFIELD(te, substitute_in_expr name op e1, substitute_in_expr name op e2, substitute_in_expr name op e3)
	| UNOP(te, un_op, e) ->
		UNOP(te, un_op, substitute_in_expr name op e)
	| BINOP(te, bin_op, e1, e2) ->
		BINOP(te, bin_op, substitute_in_expr name op e1, substitute_in_expr name op e2)
	| IF_EXPR(te, e1, e2, e3) ->
		IF_EXPR(te, substitute_in_expr name op e1, substitute_in_expr name op e2, substitute_in_expr name op e3)
	| SWITCH_EXPR(te, e1, ee_l, e2) ->
		SWITCH_EXPR(te, substitute_in_expr name op e1, List.map (fun (x,y) -> (substitute_in_expr name op x, substitute_in_expr name op y)) ee_l, substitute_in_expr name op e2)
	| CONST(te, c)
		-> CONST(te, c)
	| ELINE (file, line, e) ->
		ELINE (file, line, substitute_in_expr name op e)
	| EINLINE _ ->
		ex
	| CAST(size, expr) ->
		CAST(size, substitute_in_expr name op expr)


(**
	rename any occcurences of the given name in an expression
	@param	ex	the expression in which we rename
	@param	var_name	name of the var to be renamed
	@param	new_name	new name for the var
	@return		the expression with the given var renamed
*)
let rec change_name_of_var_in_expr ex var_name new_name =
	let get_name_param s =
		if (String.compare s var_name) == 0 then
			new_name
		else
			s
	in
	(* !!DEBUG!! *)
	(*print_string ("chg_var_in_e var_name="^var_name^", new_name="^new_name^", expr=");
	Irg.print_expr ex; print_char '\n';*)
	match ex with
	NONE ->
		NONE
	| COERCE(t_e, e) ->
		COERCE(t_e, change_name_of_var_in_expr e var_name new_name)
	| FORMAT(s, e_l) ->
		FORMAT(s, List.map (fun x -> change_name_of_var_in_expr x var_name new_name) e_l)
	| CANON_EXPR(t_e, s, e_l) ->
		CANON_EXPR(t_e, s, List.map (fun x -> change_name_of_var_in_expr x var_name new_name) e_l)
	| REF(s) ->
		REF (get_name_param s)
	| FIELDOF(t_e, e, s) ->
		FIELDOF(t_e, (*change_name_of_var_in_expr*) get_name_param e (*var_name new_name*), s)
	| ITEMOF(t_e, e1, e2) ->
		ITEMOF(t_e, e1, change_name_of_var_in_expr e2 var_name new_name)
	| BITFIELD(t_e, e1, e2, e3) ->
		BITFIELD(t_e, change_name_of_var_in_expr e1 var_name new_name, change_name_of_var_in_expr e2 var_name new_name, change_name_of_var_in_expr e3 var_name new_name)
	| UNOP(t_e, u, e) ->
		UNOP(t_e, u, change_name_of_var_in_expr e var_name new_name)
	| BINOP(t_e, b, e1, e2) ->
		BINOP(t_e, b, change_name_of_var_in_expr e1 var_name new_name, change_name_of_var_in_expr e2 var_name new_name)
	| IF_EXPR(t_e, e1, e2, e3) ->
		IF_EXPR(t_e, change_name_of_var_in_expr e1 var_name new_name, change_name_of_var_in_expr e2 var_name new_name, change_name_of_var_in_expr e3 var_name new_name)
	| SWITCH_EXPR(te, e1, ee_l, e2) ->
		SWITCH_EXPR(te, change_name_of_var_in_expr e1 var_name new_name, List.map (fun (x,y) -> (change_name_of_var_in_expr x var_name new_name, change_name_of_var_in_expr y var_name new_name)) ee_l, change_name_of_var_in_expr e2 var_name new_name)
	| CONST(t_e, c) ->
		CONST(t_e, c)
	| ELINE(file, line, e) ->
		ELINE (file, line, change_name_of_var_in_expr e var_name new_name)
	| EINLINE _ ->
		ex
	| CAST(size, expr) ->
		CAST(size, change_name_of_var_in_expr expr var_name new_name)


(**
	rename any occcurences of the given name in a location
	@param	ex	the location in which we rename
	@param	var_name	name of the var to be renamed
	@param	new_name	new name for the var
	@return		the location with the given var renamed
*)
let rec change_name_of_var_in_location loc var_name new_name =
	(* !!DEBUG!! *)
	(*print_string ("chg_var_in_l var_name="^var_name^", new_name="^new_name^", loc=");
	Irg.print_location loc; print_char '\n';*)
	match loc with
	| LOC_NONE -> loc
	| LOC_REF(t, s, i, l, u) ->
		LOC_REF (
			t,
			(if (String.compare s var_name) == 0 then new_name else s),
			change_name_of_var_in_expr i var_name new_name,
			change_name_of_var_in_expr l var_name new_name,
			change_name_of_var_in_expr u var_name new_name)
	| LOC_CONCAT(t, l1, l2) ->
		LOC_CONCAT(t, change_name_of_var_in_location l1 var_name new_name, change_name_of_var_in_location l2 var_name new_name)


(**
	instantiate in an location, the given var name is supposed
	to represent an entity of the given spec op. each occurence of
	name (Irg.LOC_REF(_, name, _, _, _) ) is replaced respectively by
	op's mode value (if op is a mode) combined eventually with loc's index and bitfield bounds.
	combination is posssible only for certain types of mode value expression.
	@param	name	name of the var to instantiate
	@param	op	spec of the entity represented by the var of given name
	@param	loc	location in which we instantiate the given var
	@return		the instantiated location
	@raise	failwith	if instantiation is not possible
*)
let rec substitute_in_location name op loc =
	let get_mode_value sp =
		match sp with
		AND_MODE(_, _, v, _) -> v
		| _-> NONE
	in
	let is_and_mode sp =
		match sp with
		AND_MODE(_, _, _, _) -> true
		| _ -> false
	in(* !!DEBUG!! *)
	(*print_string ("subs_in_l name="^name^", op="^(Irg.name_of op)^", loc=");
	Irg.print_location loc; print_char '\n';*)
	match loc with
	LOC_NONE ->
		loc
	| LOC_REF(t, s, i, l, u) ->
		let rec subst_mode_value mv =
			match mv with
			REF(n) ->
				LOC_REF(t, n, substitute_in_expr name op i, substitute_in_expr name op l, substitute_in_expr name op u)
			| ITEMOF(typ, n, idx) ->
				(* can replace only if loc is "simple" (ie i = NONE), we can't express n[idx][i] *)
				if i = NONE then
					LOC_REF(typ, n, idx, substitute_in_expr name op l, substitute_in_expr name op u)
				else
					failwith "cannot substitute a var here (ITEMOF) (instantiate.ml::substitute_in_location)"
			| BITFIELD(typ, REF(nn), lb, ub) when i = NONE && u = NONE & l = NONE
				-> LOC_REF(typ, nn, NONE, lb, ub)
			| BITFIELD(t, ITEMOF(_, n, idx), lb, ub) when i = NONE && u = NONE & l = NONE
				-> LOC_REF(t, n, idx, lb, ub)
			| BITFIELD(t, ELINE(_, _, e), lb, ub) ->
				subst_mode_value (BITFIELD (t, e, lb, ub))
			| ELINE(str, lin, e) ->
				subst_mode_value e
			| _ ->
				print_string "loc=";Irg.print_location loc;
				print_string ("\name="^name^"\nmv=");
				Irg.print_expr mv;print_string "\nspec=";
				Irg.print_spec op;
				if i=NONE then
					if u=NONE && l=NONE then
						failwith "cannot substitute here (_ 1), (instantiate.ml::substitute_in_location)"
					else
						failwith "cannot substitute here (_ 2), (instantiate.ml::substitute_in_location)"
				else
					(* how could we express stg like (if .. then .. else .. endif)[i]<l..u>, it would be meaningless most of the time *)
					failwith "cannot substitute a var here (_ 3) (instantiate.ml::substitute_in_location)"
		in
		(* change if op is a AND_MODE and s refers to it *)
		if (name=s)&&(is_and_mode op) then
			subst_mode_value (get_mode_value op)
		else
			LOC_REF(t, s, substitute_in_expr name op i, substitute_in_expr name op l, substitute_in_expr name op u)
	| LOC_CONCAT(t, l1, l2) ->
		LOC_CONCAT(t, substitute_in_location name op l1, substitute_in_location name op l2)


(**
	instantiate in an statement, the given var name is supposed
	to represent an entity of the given spec op. each occurence of
	name (Irg.EVALIND(name, _) representing a statement attribute from another spec)
	is replaced by the statements of the refered attribute of op
	or, if recursive, by a reference to a new attribute (built in another function) of the same spec
	@param	name	name of the var to instantiate
	@param	op	spec of the entity represented by the var of given name
	@param	statement	statement in which we instantiate the given var
	@return		the instantiated statement
*)
let rec substitute_in_stat name op statement =
	(* !!DEBUG!! *)
	(*print_string ("subs_in_s name="^name^", op="^(Irg.name_of op)^", stat=");
	Irg.print_statement statement; print_char '\n';*)
	match statement with
	NOP ->
		NOP
	| SEQ(s1, s2) ->
		SEQ(substitute_in_stat name op s1, substitute_in_stat name op s2)
	| EVAL(s) ->
		EVAL(s)
	| EVALIND(n, attr) ->
		if (String.compare n name) == 0 then
		begin
			if is_stat_attr_recursive op attr then
				(*  transform x.action into x_action (this will be a new attr to add to the final spec) *)
				EVAL(n ^ "_" ^ attr)
			else
				get_stat_from_attr_from_spec op attr
		end
		else
			EVALIND(n, attr)
	| SET(l, e) ->
		SET(substitute_in_location name op l, substitute_in_expr name op e)
	| CANON_STAT(n, el) ->
		CANON_STAT(n, List.map (substitute_in_expr name op) el)
	| ERROR(s) ->
		ERROR(s)
	| IF_STAT(e, s1, s2) ->
		IF_STAT(substitute_in_expr name op e, substitute_in_stat name op s1, substitute_in_stat name op s2)
	| SWITCH_STAT(e, es_l, s) ->
		SWITCH_STAT(substitute_in_expr name op e, List.map (fun (ex, st) -> (ex, substitute_in_stat name op st)) es_l, substitute_in_stat name op s)
	| SETSPE(l, e) ->
		SETSPE(substitute_in_location name op l, substitute_in_expr name op e)
	| LINE(s, i, st) ->
		LINE(s, i, substitute_in_stat name op st)
	| INLINE _ ->
		statement


(**
	rename any occcurences of the given name in a statement
	@param	sta	the statement in which we rename
	@param	var_name	name of the var to be renamed
	@param	new_name	new name for the var
	@return		the statement with the given var renamed
*)
let rec change_name_of_var_in_stat sta var_name new_name =
	(* !!DEBUG!! *)
	(*print_string ("chg_var_in_s var_name="^var_name^", new_name="^new_name^", sta=");
	Irg.print_statement sta; print_char '\n';*)
	match sta with
	NOP ->
		NOP
	| SEQ(s1, s2) ->
		SEQ(change_name_of_var_in_stat s1 var_name new_name, change_name_of_var_in_stat s2 var_name new_name)
	| EVAL(str) ->
		EVAL(str)
	| EVALIND(v, attr_name) ->
		if (String.compare v var_name) == 0 then
			EVALIND(new_name, attr_name)
		else
			EVALIND(v, attr_name)
	| SET(l, e) ->
		SET(change_name_of_var_in_location l var_name new_name, change_name_of_var_in_expr e var_name new_name)
	| CANON_STAT(str, e_l) ->
		CANON_STAT(str, List.map (fun x -> change_name_of_var_in_expr x var_name new_name) e_l)
	| ERROR(str) ->
		ERROR(str)
	| IF_STAT(e, s1, s2) ->
		IF_STAT(change_name_of_var_in_expr e var_name new_name, change_name_of_var_in_stat s1 var_name new_name, change_name_of_var_in_stat s2 var_name new_name)
	| SWITCH_STAT(e, es_l, s) ->
		SWITCH_STAT(change_name_of_var_in_expr e var_name new_name, List.map (fun (x,y) -> (change_name_of_var_in_expr x var_name new_name, change_name_of_var_in_stat y var_name new_name)) es_l, change_name_of_var_in_stat s var_name new_name)
	| SETSPE(l, e) ->
		SETSPE(change_name_of_var_in_location l var_name new_name, change_name_of_var_in_expr e var_name new_name)
	| LINE(str, n, s) ->
		LINE(str, n, change_name_of_var_in_stat s var_name new_name)
	| INLINE _ ->
		sta


(**
	rename any occcurences of the given name in an attribute
	@param	a	the attribute in which we rename
	@param	var_name	name of the var to be renamed
	@param	new_name	new name for the var
	@return		the attribute with the given var renamed
*)
let change_name_of_var_in_attr a var_name new_name =
	(* !!DEBUG!! *)
	(*print_string ("chg_var_in_a var_name="^var_name^", new_name="^new_name^", a=");
	Irg.print_attr a; print_char '\n';*)
	match a with
	ATTR_EXPR(str, e) ->
		ATTR_EXPR(str, change_name_of_var_in_expr e var_name new_name)
	| ATTR_STAT(str, s) ->
		ATTR_STAT(str, change_name_of_var_in_stat s var_name new_name)
	| ATTR_USES ->
		ATTR_USES
	| ATTR_LOC(id, l) ->
		ATTR_LOC(id, change_name_of_var_in_location l var_name new_name)


(**
	prefix any occcurences of the given param's name in an attribute
	@param	a	the attribute in which we rename
	@param	param	param whose occurrences have to be prefixed
	@param	pfx	prefix used
	@return		the attribute with the var (given by param) prefixed
*)
let prefix_attr_var a param pfx =
	match param with
	| (str, _) -> change_name_of_var_in_attr a str (pfx^"_"^str)


(**
	prefix any occcurences of every param's name in an attribute
	@param	a	the attribute in which we rename
	@param	p_l	param list, every occurrences of every param have to be prefixed
	@param	pfx	prefix used
	@return		the attribute with all vars (given by p_l) prefixed
*)
let rec prefix_all_vars_in_attr a p_l pfx =
	match p_l with
	| h::q -> prefix_all_vars_in_attr (prefix_attr_var a h pfx) q pfx
	| [] -> a


(**
	prefix any occcurences of the given param's name in a mode value expression
	@param	mode_value	the mode value in which we rename
	@param	param	param whose occurrences have to be prefixed
	@param	pfx	prefix used
	@return		the mode value expression with the var (given by param) prefixed
*)
let prefix_var_in_mode_value mode_value param pfx =
	match param with
	| (str, _) -> change_name_of_var_in_expr mode_value str (pfx^"_"^str)


(**
	prefix any occcurences of every param's name in a mode value expression
	@param	a	the mode value in which we rename
	@param	p_l	param list, every occurrences of every param have to be prefixed
	@param	pfx	prefix used
	@return		the mode value expression with all vars (given by p_l) prefixed
*)
let rec prefix_all_vars_in_mode_value mode_value p_l pfx =
	match p_l with
	| h::q -> prefix_all_vars_in_mode_value (prefix_var_in_mode_value mode_value h pfx) q pfx
	| [] -> mode_value


(**
	prefix the name of every param of the given spec by a given prefix,
	every occurrences in every attributes must be prefixed
	@param	sp	spec in which we prefix
	@param	pfx	prefix used
	@return		the spec with all vars given by sp's params prefixed in every attributes
*)
let rec prefix_name_of_params_in_spec sp pfx =
	let prefix_param param =
		match param with
		| (name, t) -> (pfx^"_"^name, t)
	in
	match sp with
	| AND_OP(name, params, attrs) ->
		AND_OP(name, List.map prefix_param params, List.map (fun x -> prefix_all_vars_in_attr x params pfx) attrs)
	| AND_MODE(name, params, mode_val, attrs) ->
		AND_MODE(name, List.map prefix_param params, prefix_all_vars_in_mode_value mode_val params pfx, List.map (fun x -> prefix_all_vars_in_attr x params pfx) attrs)
	| _ -> sp



(***********************************************)
(* format expressions simplification functions *)
(***********************************************)


(**
	transform a regexp cut list (Str.split_result list) into
	a string list (no more difference between text and delimiters)
	@param	l	the Str.split_result list to transform
	@return		a string list with no more difference between text and delimiters
*)
let rec regexp_list_to_str_list l =
	match l with
	[] -> []
	| Str.Text(txt)::b -> txt::(regexp_list_to_str_list b)
	| Str.Delim(txt)::b -> txt::(regexp_list_to_str_list b)


(**
	cut a format expression string into format specifiers and simple text,
	format specifiers are similar to those in C language,
	syntax: %[width specifier]b|d|x|f | %s, %% is for printing '%' character
	@param	l	the Str.split_result list to transform
	@return		a list of delimiters (format specifiers) and text
*)
let string_to_regexp_list s =
	let process_double_percent e =
		match e with
		Str.Text(t) ->
			e
		| Str.Delim(t) ->
			if (String.compare t "%%") == 0 then
				Str.Text("%%")
			else
				e
	in
	List.map process_double_percent (Str.full_split (Str.regexp "%[0-9]*[ldbxsfu]\\|%%") s)


(**
	concatenate a list of strings to produce a single string
	@param	l	list of string to concatenate
	@return		the result concatenated string
*)
let str_list_to_str l =
	String.concat "" l


(**
	after instantiation, we have imbrications of format expressions(eg. FORMAT(FORMAT(_, _), _) ),
	this function reduce such imbrication into a single format
	@param	ex	an expression (potential format expression) whose formats must be reduced
	@return		the same expression with every format imbrication reduced, or the eaxct
			same expression if no format is found
*)
let rec simplify_format_expr ex =
	let rec insert_format f1 f_l p1 p_l =
		match p1 with
		| FORMAT(s, e_l) ->
			(* reduce the format, check compatibility type between format and param later *)
			let new_f1 = string_to_regexp_list s in
			let (a, b) = reduce f_l p_l in
				(new_f1 @ a, e_l @ b)
		| ELINE (file, line, expr) ->
			insert_format f1 f_l expr p_l
		| _ ->
			let (a, b) = reduce f_l p_l in
				(f1::a,  p1::b)
	and reduce f params =
		match f with
		[] ->
			if params = [] then
				([], [])
			else
				failwith "too much params here (instantiate.ml::simplify_format_expr::reduce)"
		| f1::f_l ->
			(match f1 with
			Str.Text(t) ->
				(* simple text *)
				let (a, b) = reduce f_l params
				in
					(f1::a, b)
			| Str.Delim(t) ->
				(* format *)
				(match params with
				| [] 		-> failwith "not enough params here (instantiate.ml::simplify_format_expr::reduce)"
				| p1::p_l	-> insert_format f1 f_l p1 p_l)
			)
	in
	match ex with
	| FORMAT(s, e_l) ->
		let str_format = string_to_regexp_list s in
		let simpl_e_l = List.map simplify_format_expr e_l in
		let (new_s, new_e_l) = reduce str_format simpl_e_l in
			FORMAT((str_list_to_str (regexp_list_to_str_list new_s)), new_e_l)
	| COERCE(t_e, e) ->
		COERCE(t_e, simplify_format_expr e)
	| CANON_EXPR(t_e, s, e_l) ->
		CANON_EXPR(t_e, s, List.map simplify_format_expr e_l)
	| ITEMOF(t_e, e1, e2) ->
		ITEMOF(t_e, e1, simplify_format_expr e2)
	| BITFIELD(t_e, e1, e2, e3) ->
		BITFIELD(t_e, simplify_format_expr e1, simplify_format_expr e2, simplify_format_expr e3)
	| UNOP(t_e, u, e) ->
		UNOP(t_e, u, simplify_format_expr e)
	| BINOP(t_e, b, e1, e2) ->
		BINOP(t_e, b, simplify_format_expr e1, simplify_format_expr e2)
	| IF_EXPR(t_e, e1, e2, e3) ->
		IF_EXPR(t_e, simplify_format_expr e1, simplify_format_expr e2, simplify_format_expr e3)
	| SWITCH_EXPR(te, e1, ee_l, e2) ->
		SWITCH_EXPR(te, simplify_format_expr e1, List.map (fun (x,y) -> (simplify_format_expr x, simplify_format_expr y)) ee_l, simplify_format_expr e2)
	| ELINE(file, line, e) ->
		ELINE (file, line, simplify_format_expr e)
	| CAST(size, expr) ->
		CAST(size, simplify_format_expr expr)
	| _ -> ex


(**
	after instantiation and imbrication suppression in format expression, there still are
	format with simple constants (eg. FORMAT("...%5b...", ..., 30, ...) ) which should be simplified (in the example -> FORMAT("...11110...", ...) )
	this function try to simplify as many constants as it can in a format expression
	@param	f	an expression (potential format expression) whose formats must be simplified
	@return		the same expression with every format simplified, or the eaxct
			same expression if no format is found
*)
let rec remove_const_param_from_format f =
	let get_length_from_format regexp =
		let f =
			match regexp with
			Str.Delim(t) -> t
			| _ -> failwith "shouldn't happen (instantiate.ml::remove_const_param_from_format::get_length_from_format::f)"
		in
		let l = String.length f in
		let new_f =
			if l<=2 then
			(* shouldn't happen, we should have only formats like %[0-9]*b, not %d or %f *)
				"0"
			else
				String.sub f 1 (l-2)
		in
		Scanf.sscanf new_f "%d" (fun x->x)
	in
	let rec int32_to_string01 i32 size accu =
		let bit = Int32.logand i32 Int32.one
		in
		let res = (Int32.to_string bit) ^ accu
		in
		if size > 0 then
			int32_to_string01 (Int32.shift_right_logical i32 1) (size - 1) res
		else
			accu
	in
	let rec int64_to_string01 i64 size accu =
		let bit = Int64.logand i64 Int64.one
		in
		let res = (Int64.to_string bit) ^ accu
		in
		if size > 0 then
			int64_to_string01 (Int64.shift_right_logical i64 1) (size - 1) res
		else
			accu
	in
	let is_string_format regexp =
		match regexp with
		Str.Delim(t) ->
			(String.compare t "%s") == 0
		| _ ->
			failwith "shouldn't happen (instantiate.ml::remove_const_param_from_format::is_string_format)"
	in
	let is_integer_format regexp =
		match regexp with
		Str.Delim(t) ->
			(String.compare t "%d") == 0
		| _ ->
			failwith "shouldn't happen (instantiate.ml::remove_const_param_from_format::is_integer_format)"
	in
	let is_binary_format regexp =
		match regexp with
		Str.Delim(t) ->
			if (String.length t) <= 2 then
				false
			else
				t.[(String.length t) - 1] = 'b'
		| _ ->
			failwith "shouldn't happen (instantiate.ml::remove_const_param_from_format::is_binary_format)"
	in
	let is_float_format regexp =
		match regexp with
		Str.Delim(t) ->
			(String.compare t "%f") == 0
		| _ ->
			failwith "shouldn't happen (instantiate.ml::remove_const_param_from_format::is_float_format)"
	in
	let replace_const_param_in_format_string regexp param =
		match param with
		CONST(t, c) ->
			(match c with
			CARD_CONST(i) ->
				if is_integer_format regexp then
					Str.Text(Int32.to_string i)
				else
					if is_binary_format regexp then
						Str.Text(int32_to_string01 i (get_length_from_format regexp) "")
					else
						failwith "bad format, a 32 bit integer constant can be displayed only with \"%d\" and \"%xxb\" (instantiate.ml::remove_const_param_from_format::replace_const_param_in_format_string)"
			| CARD_CONST_64(i) ->
				if is_integer_format regexp then
					Str.Text(Int64.to_string i)
				else
					if is_binary_format regexp then
						Str.Text(int64_to_string01 i (get_length_from_format regexp) "")
					else
						failwith "bad format, a 64 bit integer constant can be displayed only with \"%d\" and \"%xxb\" (instantiate.ml::remove_const_param_from_format::replace_const_param_in_format_string)"
			| STRING_CONST(s, b, _) ->
				if b then
					regexp
				else if is_string_format regexp then
					Str.Text(s)
				else
					failwith "bad format, a string constant can be displayed only with \"%s\" (instantiate.ml::remove_const_param_from_format::replace_const_param_in_format_string)"
			| FIXED_CONST(f) ->
				if is_float_format regexp then
					(* TODO: check if the output is compatible with C textual representation of floats *)
					Str.Text(string_of_float f)
				else
					failwith "bad format, a float constant can be displayed only with \"%f\" (instantiate.ml::remove_const_param_from_format::replace_const_param_in_format_string)"
			| NULL ->
				(* wtf!? isn't that supposed to be an error ? in the doubt... let it through *)
				regexp
			)
		| _ ->
			regexp
	in
	let replace_const_param_in_param regexp param =
		match param with
		CONST(t, c) ->
			[]
		| _ ->
			[param]
	in
	(* simplify the regexp list *)
	let rec r_aux r_l p_l =
		match r_l with
		[] ->
			[]
		| a::b ->
			(match a with
			Str.Text(t) ->
				a::(r_aux b p_l)
			| Str.Delim(d) ->
				(match p_l with
				[] ->
					(* not enough params ! *)
					failwith "shouldn't happen (instantiate.ml::remove_const_param_from_format::r_aux)"
				| t::u ->
					(replace_const_param_in_format_string a t)::(r_aux b u)
				)
			)
	in
	(* simplify the param list *)
	let rec p_aux r_l p_l =
		match r_l with
		[] ->
			if p_l = [] then
				[]
			else
				(* not enough formats ! *)
				failwith "shouldn't happen (instantiate.ml::remove_const_param_from_format::p_aux)"
		| a::b ->
			(match a with
			Str.Text(t) ->
				p_aux b p_l
			| Str.Delim(d) ->
				(match p_l with
				[] ->
					(* not enough params ! *)
					failwith "shouldn't happen (instantiate.ml::remove_const_param_from_format::p_aux)"
				| t::u ->
					(replace_const_param_in_param a t) @ (p_aux b u)
				)
			)
	in
	match f with
	| FORMAT(s, p) ->
		let r_l = string_to_regexp_list s in
		let new_s = str_list_to_str (regexp_list_to_str_list (r_aux r_l p)) in
		let new_p = p_aux r_l p in
		if new_p = [] then
			(* format with no arg => simplified into a string constant *)
			CONST(STRING, STRING_CONST(new_s, false, NO_TYPE))
		else
			FORMAT(new_s, new_p)
	| COERCE(t_e, e) ->
		COERCE(t_e, remove_const_param_from_format e)
	| CANON_EXPR(t_e, s, e_l) ->
		CANON_EXPR(t_e, s, List.map remove_const_param_from_format e_l)
	| ITEMOF(t_e, e1, e2) ->
		ITEMOF(t_e, e1, remove_const_param_from_format e2)
	| BITFIELD(t_e, e1, e2, e3) ->
		BITFIELD(t_e, remove_const_param_from_format e1, remove_const_param_from_format e2, remove_const_param_from_format e3)
	| UNOP(t_e, u, e) ->
		UNOP(t_e, u, remove_const_param_from_format e)
	| BINOP(t_e, b, e1, e2) ->
		BINOP(t_e, b, remove_const_param_from_format e1, remove_const_param_from_format e2)
	| IF_EXPR(t_e, e1, e2, e3) ->
		IF_EXPR(t_e, remove_const_param_from_format e1, remove_const_param_from_format e2, remove_const_param_from_format e3)
	| SWITCH_EXPR(te, e1, ee_l, e2) ->
		SWITCH_EXPR(te, remove_const_param_from_format e1, List.map (fun (x,y) -> (remove_const_param_from_format x, remove_const_param_from_format y)) ee_l, remove_const_param_from_format e2)
	| ELINE(file, line, e) ->
		ELINE (file, line, remove_const_param_from_format e)
	| CAST(size, expr) ->
		CAST(size, remove_const_param_from_format expr)
	| _ -> f



(*************************************************************)
(* top level instantiation and param instantiation functions *)
(*************************************************************)



(**
	instantiate all vars refering to given parameters in a statement, each original parameter
	in the statement' spec is instantiated (no more mode nor op) to basic types (and more parameters usually)
	@param	sta	statement in which we instantiate
	@param	param_list	list of instantiated parameters
	@return		the instantiated statement
*)
let rec instantiate_in_stat sta param_list =
(* !!DEBUG!! *)
(*print_string "inst_in_s, param_list=";Irg.print_param_list param_list;Irg.print_statement sta; print_char '\n';*)
	match param_list with
	[] ->
		sta
	| (name, TYPE_ID(t))::q ->
		instantiate_in_stat (substitute_in_stat name (prefix_name_of_params_in_spec (get_symbol t) name) sta) q
	| (name, TYPE_EXPR(e))::q ->
		instantiate_in_stat sta q


(**
	instantiate all vars refering to given parameters in an expression, each original parameter
	in the expression' spec is instantiated (no more mode nor op) to basic types (and more parameters usually)
	@param	ex	expression in which we instantiate
	@param	param_list	list of instantiated parameters
	@return		the instantiated expression
*)
let rec instantiate_in_expr ex param_list =
(* !!DEBUG!! *)
(*print_string "inst_in_e, param_list=";Irg.print_param_list param_list;Irg.print_expr ex; print_char '\n';*)
	let rec aux e p_l =
		match p_l with
		| [] ->
			e
		| (name, TYPE_ID(t))::q ->
			aux (substitute_in_expr name (prefix_name_of_params_in_spec (get_symbol t) name) e) q
		| (name, TYPE_EXPR(ty))::q ->
			aux e q
	in
	aux ex param_list


(**
	instantiate a given parameter to its basic modes or ops (terminal OR nodes),
	all possibilities are dealt with. If the param refers already to a simple type,
	it is returned unchanged
	@param	param	parameter to instantiate
	@return		list of all the instantiated possibilities for param
*)
let instantiate_param param =
	let rec aux p =
		match p with
		| (name, TYPE_ID(typeid))::q ->
			(match get_symbol typeid with
			| OR_OP(_, str_l) ->
				(List.flatten (List.map (fun x -> aux [(name, TYPE_ID(x))]) str_l)) @ (aux q)
			| OR_MODE(_, str_l) ->
				(List.flatten (List.map (fun x -> aux [(name, TYPE_ID(x))]) str_l)) @ (aux q)
			| AND_OP(_, _, _) ->
				p @ (aux q)
			| AND_MODE(_, _, _, _) ->
				p @ (aux q)
			| _ ->
				p @ (aux q) (* will this happen? *)
			)
		| [] ->
			[]
		| _ ->
			p
	in
	match param with
	| (name, TYPE_EXPR(te)) ->
		[param]
	| (_ , _) ->
		aux [param]



(**
	takes 2 list and return a list where elements
	represent all possible tuples with 1st element from l1 and 2nd from l2,
	these tuples are represented as lists of 2 element, so we return a list of list.
	If any input list is empty, so is the result
	@param	l1	list giving 1st elements of the resulting sublists
	@param	l2	list giving 2nd elements of the resulting sublists
	@return		the list of every possible couples (rendered as lists) [a; b] with a in l1 and b in l2
*)
let rec cross_prod l1 l2 =
	match l1 with
	| [] -> []
	| a::b ->
		if l2=[] then
			[]
		else
			(List.map (fun x -> [a;x]) l2) @ (cross_prod b l2)



(**
	takes a list and list of list and returns a list with each element of ll
	is prefixed by an element of l (all possibilities are produced),
	l or ll empty implies result also empty
	@param	l	list of elements to add at head of each elements of the list of list
	@param	ll	list of sublist to be prefixed
	@return		list of all possible combinations a::b with a in l and b in ll
*)
let rec list_and_list_list_prod l ll =
	match l with
	| [] -> []
	| a::b ->
		if ll=[] then
			[]
		else
			(List.map (fun x -> a::x) ll) @ (list_and_list_list_prod b ll)



(**
	takes a list of n list and returns a list of n-tuples (each rendered as a list)
	from those n sublists, each possibilities are taken care of.
	@param	p_ll	list of n sublists l_i (i from 1 to n)
	@return		list of all possible combinations [a_1; a_2; ...; a_n] with a_i in l_i
*)
let list_prod p_ll =
	let rec expand l =
		match l with
		| [] -> []
		| a::b -> [a]::(expand b)
	in
	match p_ll with
	| [] -> []
	| a::b ->
		(match b with
		| [] -> expand a
		| c::d ->
			if List.length d = 0 then
				cross_prod a c
			else
				List.fold_left (fun x y -> list_and_list_list_prod y x) (cross_prod a c) d
		)



(**
	takes a list of parameters (coming from a spec) and instantiate it by returning every possible
	resulting instantiated parameter list
	@param	p_l	a list of parameters coming from a spec
	@return		list of all possible instantiated parameter lists from p_l
*)
let instantiate_param_list p_l =
	let a = List.map instantiate_param p_l
	in
	list_prod a



(**
	instantiate all vars given by an instantiated parameter list in an attribute
	@param	a	the attribute where we instantiate
	@param	params	list of instantiated parameters giving the vars to instantiate
	@return		the attribute with vars indicated by params instantiated
*)
let instantiate_attr a params=
	match a with
	ATTR_EXPR(n, e) ->
		ATTR_EXPR(n, remove_const_param_from_format (simplify_format_expr (instantiate_in_expr e params)))
	| ATTR_STAT(n, s) ->
		ATTR_STAT(n, instantiate_in_stat s params)
	| ATTR_USES ->
		ATTR_USES
	| ATTR_LOC(n, l) ->
		(* TODO: fix like this ATTR_LOC(n, instantiate_in_location l params) *)
		a



(**
	when instantiating in a spec a param refering to an op,
	we must add to the spec the attributes of the param' spec which are not in the given spec
	@param	sp	spec on which we may add attributes
	@param	param	an instantiated parameter of sp
	@return		if param refers to an op which has attributes not present in sp
			we return sp with the extra attributes added. if not, sp is returned
*)
let add_attr_to_spec sp param =
	let get_attrs s =
		match s with
		| AND_OP(_, _, a_l) -> a_l
		| AND_MODE(_, _, _, a_l) -> a_l
		| _ -> []
	in
	let name_of_param p =
		match p with
		| (name, TYPE_ID(s)) -> name
		| (_, TYPE_EXPR(_)) ->
			failwith "shouldn't happen (instantiate.ml::add_attr_to_spec::name_of_param)"
	in
	let spec_of_param p =
		match p with
		| (name, TYPE_ID(s)) -> get_symbol s
		| (_, TYPE_EXPR(_)) ->
			failwith "shouldn't happen (instantiate.ml::add_attr_to_spec::spec_of_param)"
	in
	(* prefix the name of the attr and all the recursive calls to itself (EVAL(name) => EVAL(pfx ^ name) *)
	let prefix_recursive_attr a pfx =
		let rec aux st name =
			match st with
			| SEQ(s1, s2) ->
				SEQ(aux s1 name, aux s2 name)
			| EVAL(str) ->
				if (String.compare str name) == 0 then
					EVAL(pfx ^ "_" ^ name)
				else
					EVAL(str)
			| IF_STAT(e, s1, s2) ->
				IF_STAT(e, aux s1 name, aux s2 name)
			| SWITCH_STAT(e, es_l, s) ->
				SWITCH_STAT(e, List.map (fun (x,y) -> (x, aux s name)) es_l, aux s name)
			| LINE(str, n, s) ->
				LINE(str, n, aux s name)
			| _ -> st
		in
		match a with
		| ATTR_EXPR(n, at) ->
			failwith "shouldn't happen (instantiate.ml::add_attr_to_spec::prefix_recursive_attr::ATTR_EXPR)"
		| ATTR_LOC _ ->
			failwith "shouldn't happen (instantiate.ml::add_attr_to_spec::prefix_recursive_attr::ATTR_LOC)"
		| ATTR_STAT(n, at) ->
			ATTR_STAT(pfx ^ "_" ^ n, aux at n)
		| ATTR_USES ->
			failwith "shouldn't happen (instantiate.ml::add_attr_to_spec::prefix_recursive_attr::ATTR_USES)"
	in
	let compare_attrs a1 a2 =
		match a1 with
		| ATTR_EXPR(n, _) ->
			(match a2 with
			| ATTR_EXPR(nn, _) ->
				if (String.compare nn n) == 0 then
					true
				else
					false
			| _ -> false
			)
		| ATTR_LOC(n, _) ->
			(match a2 with
			| ATTR_LOC(nn, _) ->
				if (String.compare nn n) == 0 then
					true
				else
					false
			| _ -> false
			)
		| ATTR_STAT(n, _) ->
			(match a2 with
			| ATTR_STAT(nn, _) ->
				if (String.compare nn n) == 0 then
					true
				else
					false
			| _ -> false
			)
		| ATTR_USES ->
			if a2 = (ATTR_USES) then
				true
			else
				false
	in
	(* returns the attr in param not present in sp (or present but recursive in param and not in sp, a new fully renamed attr must be produced for sp) *)
	let rec search_in_attrs a_l a_l_param =
		match a_l_param with
		| [] -> []
		| a::b ->
			if List.exists (fun x -> compare_attrs a x) a_l then
				(match a with
				| ATTR_STAT(n, _) ->
					if is_stat_attr_recursive (spec_of_param param) n then
						(prefix_recursive_attr a (name_of_param param))::(search_in_attrs a_l b)
					else
						search_in_attrs a_l b
				| _ ->
					search_in_attrs a_l b)
			else
				a::(search_in_attrs a_l b)
	in
	let attr_spec = get_attrs sp
	in
	let attr_param =
		match param with
		| (name, TYPE_ID(s)) ->
			get_attrs (prefix_name_of_params_in_spec (get_symbol s) name)
		| (name, TYPE_EXPR(t)) -> []
	in
	match sp with
	| AND_OP(name, p_l, a_l) ->
		(match spec_of_param param with
		(* we shouldn't add attributes from a mode to an op spec *)
		(* may be we should *)
		(*| AND_MODE(_, _, _, _) -> sp*)
		| AND_MODE(n, pl, e, al) -> AND_OP(name, p_l, a_l@(search_in_attrs attr_spec attr_param))
		| _ -> AND_OP(name, p_l, a_l@(search_in_attrs attr_spec attr_param))
		)
	| AND_MODE(name, p_l, e, a_l) ->
		AND_MODE(name, p_l, e, a_l@(search_in_attrs attr_spec attr_param))
	| _ -> sp



(**
	add the attrs present in the params' specs but not in the main spec (sp) to the main spec
	when instantiating in a spec a param refering to an op,
	we must add to the spec the attributes of the param' spec which are not in the given spec,
	here we do this for each parameter in a given parameter list
	@param	sp	spec on which we may add attributes
	@param	param_list	a instantiated list of all the parameters of sp
	@return		if any parameter refers to an op which has attributes not present in sp
			we return sp with the extra attributes added by each parameter. if not, sp is returned
*)
let rec add_new_attrs sp param_list =
	match param_list with
	[] -> sp
	| a::b ->
		(match a with
		| (_, TYPE_ID(_)) -> add_new_attrs (add_attr_to_spec sp a) b
		| (name, TYPE_EXPR(t)) -> add_new_attrs sp b
		)



let get_param_of_spec s =
	match s with
	| AND_OP(_, l, _) -> l
	| AND_MODE(_, l, _, _) -> l
	| _ -> []



(**
	replace each parameter in a spec's parameter list by the parameter lists from each parameter's type spec.
	Each parameter refers to basic type or another spec, if it's another spec we replace the parameter by those of the spec
	@param	p_l	a list of parameters to replace
	@return		list of new replaced parameters
*)
let replace_param_list p_l =
	let prefix_name prfx param =
		match param with
		(name, t) ->
			(prfx^"_"^name, t)
	in
	let replace_param param =
		match param with
		(nm , TYPE_ID(s)) ->
			(* !!TODO!! prefix si on va vers plsrs params de type op (au moins 2 de type op),
			ou si on va d'un mode vers ses params (>1),
			remplace si d'un op vers un autre op,
			ou d'un mode vers un seul param *)
			List.map (prefix_name nm) (get_param_of_spec (get_symbol s))
		| (_, _) ->
			[param]
	in
	List.flatten (List.map replace_param p_l)



(**
	instantiate a spec with a fully instantiated parameter list (each parameter refering to a basic type)
	@param	sp	the spec to instantiate
	@param	param_list	the parameters to instantiate in the spec
	@return		a spec which is the result of the instantiation of all parameters from param_list in sp
*)
let instantiate_spec sp param_list =
	let is_type_def_spec sp =
		match sp with
		| TYPE(_, _) -> true
		| _ -> false
	in
	(* replace all types by basic types (replace type definitions) *)
	let simplify_param p =
		match p with
		| (str, TYPE_ID(n)) ->
			(* we suppose n can refer only to an OP or MODE, or to a TYPE *)
			let sp = get_symbol n in
			if is_type_def_spec sp then
				(match sp with
				| TYPE(_, t_e) -> (str, TYPE_EXPR(t_e))
				| _ -> p
				)
			else
				p
		| (_, _) -> p
	in
	let simplify_param_list p_l =
		List.map simplify_param p_l
	in
	let new_param_list = simplify_param_list param_list
	in
	match sp with
	| AND_OP(name, params, attrs) ->
		add_new_attrs (AND_OP(name, replace_param_list new_param_list, List.map (fun x -> instantiate_attr x new_param_list) attrs)) new_param_list
	| _ -> UNDEF



(**
	instantiate all possible parameter combinations in a spec
	@param	sp	the spec to instantiate
	@return		the list of all fully instantiated specs (one for each parameter combination) derived from the starting spec
*)
let instantiate_spec_all_combinations sp =
	let new_param_lists = instantiate_param_list (get_param_of_spec sp) in
	List.map (fun x -> instantiate_spec sp x) new_param_lists


(**
	indicates if a spec can be instantiated further, it is the case if there remains
	any parameter of mode or op type
	@param	sp	the eventually instantiable spec
	@return		true if the spec is instantiable, false otherwise
*)
let is_instantiable sp =
	let is_param_instantiable p =
		match p with
		(_, TYPE_ID(t)) ->
			true
		| (_, TYPE_EXPR(e)) ->
			false
	in
	let test param_list =
		List.exists is_param_instantiable param_list
	in
	match sp with
	AND_OP(_, p_l, _) ->
		test p_l
	| _ -> false



(**
	fully instantiate a list of specs, for each one every parameter combination is instantiated
	@param	s_l	list of specs to instantiate
	@return		the list of all fully instantiated specs derived from the starting list
*)
let rec instantiate_spec_list s_l =
	match s_l with
	[] ->
		[]
	| a::b ->
		if is_instantiable a then
			(instantiate_spec_all_combinations a)@(instantiate_spec_list b)
		else
			a::(instantiate_spec_list b)



(**
	this function instantiates all possible specs given by the spec with the given name in the hashtable,
	all modes and op are instantiated until we have only basic types (card, int ...)
	@param	name	name of the spec to instantiate
	@return		the list of all fully instantiated instructions derived from the spec of the given name
*)
let instantiate_instructions name =
	(* some AND_OP specs with empty attrs and no params are output, remove them
	TODO : see where they come from, this patch is awful.
	here, we assume we have an empty spec as soon as the syntax or the image is void *)
	let is_void_attr a =
		match a with
		| ATTR_EXPR(n, e) ->
			(match e with
			| ELINE(_, _, ee) ->
				if (String.compare n "syntax") == 0 && ee = NONE then
					true
				else
					if (String.compare n "image") == 0 && ee = NONE then
						true
					else
						false
			| _ -> false
			)
		| _ -> false
	in
	let is_void_spec sp =
		match sp with
		| AND_OP(_, _, attrs) -> List.exists is_void_attr attrs
		| _ -> false
	in
	let rec clean_attr a =
		match a with
		| ATTR_EXPR(n, e) ->
			(match e with
			| ELINE(f, l, ee) ->
				(match ee with
				| ELINE(_, _, eee) -> clean_attr (ATTR_EXPR(n, eee))
				| _ -> ATTR_EXPR(n, ee)
				)
			| _ -> a
			)
		| _ -> a
	in
	(* a try to fix over imbrication of ELINE *)
	let rec clean_eline s =
		match s with
		| AND_OP(n, st_l, al) -> AND_OP(n, st_l, List.map clean_attr al)
		| _ -> s
	in
	let rec clean_instructions s_l =
		match s_l with
		| [] -> []
		| h::t ->
			if is_void_spec h then
				clean_instructions t
			else
				(clean_eline h)::(clean_instructions t)
	in
	let rec aux s_l =
		if List.exists is_instantiable s_l then
			aux (instantiate_spec_list s_l)
		else
			s_l
	in
	let rec instantiate_to_andop s_l =
		let rec inst_one s =
			match s with
			| AND_OP(_, _, _) -> [s]
			| OR_OP(_, str_list) -> List.flatten (List.map (fun x -> inst_one (get_symbol x)) str_list)
			| _ -> failwith "shouldn't happen (instantiate.ml::instantiate_instructions::instantiate_to_andop::inst_one)"
		in
		List.flatten (List.map inst_one s_l)
	in
	clean_instructions (aux (instantiate_to_andop [get_symbol name]))





(* a few testing functions *)


let test_instant_spec name =
	let rec print_spec_list l =
		match l with
		[] ->
			()
		| a::b ->
			begin
			print_spec a;
			print_string "\n";
			print_spec_list b
			end
	in
	print_spec_list (instantiate_instructions name)

let test_instant_param p =
	let rec print_param_list_list l =
	begin
		match l with
		[] ->
			()
		| h::q ->
			begin
			print_string "[";
			print_param_list h;
			print_string "]";
			print_param_list_list q
			end
	end
	in
	print_param_list_list (instantiate_param_list [("z",TYPE_EXPR(CARD(5))); ("x",TYPE_ID("_A")); ("y",TYPE_ID("_D"))])

