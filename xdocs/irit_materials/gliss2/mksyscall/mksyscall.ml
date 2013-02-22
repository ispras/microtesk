(*
 * $Id: mksyscall.ml,v 1.5 2009/08/13 08:12:55 abbal Exp $
 * Copyright (c) 2008, IRIT - UPS <casse@irit.fr>
 *
 * This file is part of OGliss.
 *
 * OGliss is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * OGliss is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGliss; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *)

open Frontc


(* Options *)
let banner =
	"mksyscall V1.0 (01/07/09)\n" ^
	"Copyright (c) 2009, IRIT - UPS <hugues.casse@laposte.net>\n\n" ^
	"SYNTAX:\tmksyscall [options] API\n" ^
	"\tmksyscall [options] --\n"
let args: parsing_arg list ref = ref []
let files: string list ref = ref []
let out_file = ref ""
let from_stdin = ref false
let api = ref ""

(* Options scanning *)
let opts = [
	("-o", Arg.Set_string out_file,
		"Output to the given file.");
	("-nogcc", Arg.Unit (fun _ -> args := (GCC_SUPPORT false) :: !args),
		"Do not use the GCC extensions.");
	("-i", Arg.String (fun file -> args := (INCLUDE file) :: !args),
		"Include the given file.");
	("-I", Arg.String (fun dir -> args := (INCLUDE_DIR dir) :: !args),
		"Include retrieval directory");
	("-D", Arg.String (fun def -> args := (DEF def) :: !args),
		"Pass this definition to the preprocessor.");
	("-U", Arg.String (fun undef -> args := (UNDEF undef) :: !args),
		"Pass this undefinition to the preprocessor.");
	("-s", Arg.String (fun sys -> args := (PREPROC (sys ^ "-gcc -E")) :: !args),
		"Emulated system");
	("-a", Arg.String (fun a ->
			api := a;
			args := USE_CPP :: (FROM_FILE (a ^ ".c")) :: !args),
		"Emulated API")
]


(* Named types *)
module HashString =
struct
	type t = string
	let equal (s1 : t) (s2 : t) = s1 = s2
	let hash (s : t) = Hashtbl.hash s
end
module StringHashtbl = Hashtbl.Make(HashString)
let named : Cabs.base_type StringHashtbl.t = StringHashtbl.create 211

let get_named name =
	StringHashtbl.find named name

let fill_named defs =
	List.iter
		(fun def ->
			match def with
			  Cabs.TYPEDEF ((_, _, names), _) ->
				List.iter (fun (name, typ, _, _) -> StringHashtbl.add named name typ) names
			| _ -> ())
		defs

(** Type of parameters *)
type param =
	  VOID
	| SIMPLE of Cabs.base_type


let rec get_type typ =
	match typ with
	  Cabs.NO_TYPE -> SIMPLE (Cabs.INT (Cabs.NO_SIZE, Cabs.NO_SIGN))
	| Cabs.VOID -> VOID
 	| Cabs.CHAR _ -> SIMPLE typ
 	| Cabs.INT _ -> SIMPLE typ
 	| Cabs.NAMED_TYPE name -> get_type (get_named name)
 	| Cabs.CONST typ -> get_type typ
 	| Cabs.VOLATILE typ -> get_type typ
 	| Cabs.GNU_TYPE (_, typ) -> get_type typ
 	| Cabs.TYPE_LINE (_, _, typ) -> get_type typ
	| _ -> failwith "unsupported type"


(** Find the syscall for the given *)
let rec get_syscall (attrs : Cabs.gnu_attrs) =
	match attrs with
	  [] -> -1
	| (Cabs.GNU_CALL ("syscall", [Cabs.GNU_CST (Cabs.CONST_INT n)]))::_ -> int_of_string n
	| _::tl -> get_syscall tl


(** Get the list of system calls found.
	@param defs		C definition
	@return			List of system calls. *)
let rec get_calls defs =
	match defs with
	  [] -> []
	| (Cabs.FUNDEF ((_, _, (name, Cabs.PROTO (rtype, params, _), attrs, _)), body))::tl ->
		(name, rtype, params, attrs)::(get_calls tl)
	| _::tl -> get_calls tl






(** Perform declaration of a parameter.
	@param out		Out channel.
	@param param	Parameter to declare. *)
let declare_param out param =
	let (_, _, (name, typ, attrs, _)) = param in
	()



(** Test if the attribute unsupported is set.
	@param attrs	Attribute to look in.
	@return			True if unsupported is set, false else. *)
let rec is_unsupported attrs =
	match attrs with
	  [] -> false
	| (Cabs.GNU_ID "unsupported")::_ -> true
	| _::tl -> is_unsupported tl


(** Test if the attribute unsupported is set.
	@param attrs	Attribute to look in.
	@return			True if unsupported is set, false else. *)
let rec is_only_args attrs =
	match attrs with
	  [] -> false
	| (Cabs.GNU_ID "only_args")::_ -> true
	| _::tl -> is_only_args tl


(** Test if the attribute unsupported is set.
	@param attrs	Attribute to look in.
	@return			True if unsupported is set, false else. *)
let rec is_only_emulate attrs =
	match attrs with
	  [] -> false
	| (Cabs.GNU_ID "only_emulate")::_ -> true
	| _::tl -> is_only_emulate tl







(** Test if the attribute use_return is set.
	@param attrs	Attribute to look in.
	@return			True if use_return is set, false else. *)
let rec is_use_return attrs =
	match attrs with
	  [] -> false
	| (Cabs.GNU_ID "use_return")::_ -> true
	| _::tl -> is_use_return tl


(** Test if the attribute fd is set.
	@param attrs	Attribute to look in.
	@return			True if fd is set, false else. *)
let rec is_fd attrs =
	match attrs with
	  [] -> false
	| (Cabs.GNU_ID "fd")::_ -> true
	| _::tl -> is_fd tl



(** Test if the attribute string is set.
	@param attr_typ	Attribute to look in.
	@return			True if string is set, false else. *)
let is_string attr_typ = match attr_typ with
	(Cabs.GNU_TYPE(attr_list, _)) ->
		let rec attr_is_string attrs = match attrs with
			 [] -> false
			|(Cabs.GNU_ID "string")::_ -> true
			|_::tl -> attr_is_string tl
		in
		attr_is_string attr_list
	| _ -> false


(** Return true if the string typ begin with "struct" *)
let is_structure typ =
	(((String.length typ) > 6 ) 
	&& (String.sub typ 0 6) = "struct")


(** Return true if the string typ contains '*' *)
let is_pointer typ =
	typ = "0x%08x"
	|| String.contains typ '*'

(** Return the name list of a  (name, type) list *)
let get_name_list name_type_list =
	List.map
		(fun (n, t) -> n)
		name_type_list

(** Return the type list of a  (name, type) list *)
let get_type_list name_type_list =
	List.map
		(fun (n, t) -> t)
		name_type_list

(** Return  the pair (name, type) list of a name list *)
let get_name_type_list name_list=
	List.map
		(fun (n, t, _, _) -> (n, t))
		name_list
		
(** Return the pair (name, type) list of a name_group list *)
let rec get_name name_group_lst =
	match name_group_lst with
	 [] -> []
	|(_, _, name)::tl -> (get_name_type_list name)
					 @
					 (get_name tl)

(** Return an associative (name_structure, (fields_names, types_fields)) list
	if another structure is declared for one field,
	this definition is appenned at the beginning of the list *)
let rec get_struct typ = match typ with
	 Cabs.STRUCT(name_struct, name_group_lst) -> 
	 	let l = get_name_list (get_name name_group_lst) in
	 	let t = get_type_list (get_name name_group_lst) in
	 	if(l = [])
	 	then []
	 	else
	 	(List.flatten
	 		(
		 		List.map
		 			(fun t -> get_struct t)
		 			t
		 	)
	 	) @ [(name_struct, (l, t))]
	|_ -> []



(** Build the list of the structures definitions in a list of variables
	@param out		(string * (string list, base_type list)) list
					 -> this pair refers to the structure name and the lists of its fields and of their types
	@param in			list of variables *)
let rec find_struct name_list =
match name_list with
 [] -> []
|(_, Cabs.STRUCT(name_struct, name_group_lst), _, _)::tl ->	
	(get_struct(Cabs.STRUCT(name_struct, name_group_lst)))@(find_struct tl)
|_::tl -> find_struct tl



(** Build the list of the structures definitions in DECDEF, TYPEDEF and ONLYTYPEDEF definitions
	@param out		(string * (string list, base_type)) list
					 -> this pair refers to the structure name and the list of its fields and their types
	@param in			C definitions*)
let rec get_struct_def defs =
match defs with
 [] -> []
|(Cabs.DECDEF (typ, _, _))::tl -> (get_struct typ) @ (get_struct_def tl)
|(Cabs.TYPEDEF ((typ, _, _), _))::tl -> (get_struct typ) @ (get_struct_def tl)
|(Cabs.ONLYTYPEDEF (typ, _, _))::tl -> (get_struct typ) @ (get_struct_def tl)
|(Cabs.FUNDEF ((typ, _, _), (def_list, _)))::tl -> (get_struct typ) @ (get_struct_def def_list) @ (get_struct_def tl)
|(Cabs.OLDFUNDEF ((typ, _, _), name_group, (def_list, _)))::tl -> 
	let rec get_name_list name_group_list = match name_group_list with
		 [] -> []
		|(_, _, name_list)::tail -> (find_struct name_list) @ (get_name_list tail)
	in
	(get_struct typ) @ (get_name_list name_group) @ (get_struct_def def_list) @ (get_struct_def tl)


(** Look for the reference given by the alias,
	 if the reference is an alias itself,
	 the function looks for its reference *)
let rec get_base_type name typedef_list =
	try
		get_base_type (List.assoc name typedef_list) typedef_list
	with
	Not_found -> name


(** return the format in function of a type given in the string t_name
	this function is usefull for typedefs because the reference type in a string *)
let rec type_string_to_format t_name =
if(t_name = "void")
then ""
else if(t_name = "int")
then "%d"
else if(t_name = "char")
then "%c"
else if(t_name = "float")
then "%f"
else if(t_name = "double")
then "%lf"
else if(is_structure t_name) (* if the type is a structure, we return t_name *)
then t_name
else if(is_pointer t_name)
then "0x%08x"
else (* we try to found a simple type (int, char, ...) with or without attributes (long, ...) *)
	(* This function make a string list composed by the words of the string given in parameter	*)
	(* For instance, if s = "unsigned long int" the function returns ["unsigned"; "long"; "int"]	*)
	(* To know which type, we only have to take the last element							*)
	let rec make_word_list s =
		try
			let i = String.index s ' ' in
				(String.sub s 0 i)::(make_word_list(String.sub s (i+1) ((String.length s)-(i+1))))
		with
		Not_found -> [s]
	in
	let word_list = make_word_list t_name in
	type_string_to_format (List.nth word_list ((List.length word_list)-1))



(** Return the format for a printf . if t_string = "struct structName",
	the function returns t_string to get the fields of the structure *)	
let rec format_to_string typ typedef_list = match typ with
	 Cabs.VOID -> ""
	|Cabs.INT(_, _) -> "%d"
	|Cabs.CHAR(_) -> "%c"
	|Cabs.FLOAT(_) -> "%f"
	|Cabs.DOUBLE(_) -> "%lf"
	|Cabs.STRUCT(name, _) -> "struct "^name
	|Cabs.NAMED_TYPE(name) -> type_string_to_format (get_base_type name typedef_list)
	|Cabs.PTR(t) -> "0x%08x"
	|Cabs.RESTRICT_PTR(t) -> "0x%08x"
	|Cabs.GNU_TYPE(_, t) -> format_to_string t typedef_list
	|_ -> ""

(** Return the type (in a string) *)
let rec type_to_string typ = match typ with
	 Cabs.VOID -> "void"
	|Cabs.INT(size, sign) -> (Cprint.get_sign sign)^(Cprint.get_size size)^"int"
	|Cabs.CHAR(sign) -> (Cprint.get_sign sign)^"char"
	|Cabs.FLOAT(size) -> (if size then "long " else "")^"float"
	|Cabs.DOUBLE(size) -> (if size then "long " else "")^"double"
	|Cabs.STRUCT(name, name_group_list) -> "struct "^name
	|Cabs.NAMED_TYPE(name) -> name
	|Cabs.PTR(t) -> (type_to_string t)^" *"
	|Cabs.RESTRICT_PTR(t) -> (type_to_string t)^" * restrict "
	|Cabs.GNU_TYPE(_, t) -> type_to_string t
	|_ -> ""



(** Return the (source, alias) list of a typedef definitions
	@param out			(string * string) list for the pair (name_type_source, name_alias)
	@param in				name list of a name group
						type of the source
						list of the typedefs already existing
							(or [] if this is the first call) *)
let rec get_typedef name_list typ typedef_list =
match name_list with
 [] -> []
|(name, t, _, _)::tl -> (name, type_to_string t)::(get_typedef tl typ typedef_list)

(** Return the (source, alias) list of the typedef definitions list
	@param out			(string * string) list for the pair (name_type_source, name_alias)
	@param in				definition list
						list of the typedefs already existing
							(or [] if this is the first call) *)
let rec get_typedef_def defs typedef_list =
match defs with
 [] -> []
|(Cabs.TYPEDEF ((typ, _, name_list), _))::tl -> (get_typedef name_list typ typedef_list)
										   @ (get_typedef_def tl typedef_list)
| _::tl -> get_typedef_def tl typedef_list



(** Print the declarations for the parameters of the function
	@param out			List of the parameters name (without ret witch is just a variable)
						List of the formats corresponding to each parameter (just for the verbose condition)
						Boolean which equals true if the variable ret is declared
	@param in				Out Channel
						List of parameters
						Type of the returned variable
						list of the typedef descriptions *)
let rec print_param_decl out parlist rtype typedef_list=
match parlist with
 [] -> let t = type_to_string rtype in					(* No more parameters, print the return variable ret *)
 	  let return_struct = (((String.length t) > 6) && (String.sub t 0 6) = "struct") in
 	  	if(t <> "void" && t <> "" && (not return_struct))
 		then Printf.fprintf out "\t%s ret;\n" t;
		([], [], (t<>"void" && t<>"" && (not return_struct)))
|(attr_typ, _, (name, typ, _, _))::tl -> 
							let t = (type_to_string typ) in
							let f = (format_to_string typ typedef_list) in
							let (n, form, ret_declared) = print_param_decl out tl rtype typedef_list in
								if(t <> "void" && t <> "")
								then
								(
									if(is_pointer f)
									then
									(
										(		(* if the parameter is a pointer, it needs a ' ' *)
											try	(* because it is defined by a tyedef and have no '*' *)
												if(List.exists 
													(fun (alias, _) -> alias = (type_to_string typ))
													typedef_list
												  )
												then Printf.fprintf out "\t%s %s;\n" t name
												else Printf.fprintf out "\t%s%s;\n" t name
											with
											Not_found -> Printf.fprintf out "\t%s%s;\n" t name;
										);
										(* declare a ppc_address variable to get the address *)
										Printf.fprintf out "\tppc_address_t %s_addr;\n" name;
										if(is_string attr_typ)
										then	Printf.fprintf out "\tint %s_length;\n" name
									)
									else Printf.fprintf out "\t%s %s;\n" t name;
									(name::n, f::form, ret_declared)
								)
								else (n, form, ret_declared)




(** Make the list of the extensions (fields) of a structure
	ex : for this structure : struct C {int a; int b;} c;
		we'll get the list ["c.a"; "c.b"]
		for this structure : struct S {struct C c; double d;} s;
		we'll get the list ["s.c.a"; "s.c.b"; "s.d"]
	@param out			string list
	@param in				variable name
						variable type
						list of the structures descriptions
						boolean which equals true if we have
						  to append the variable name("c.a" or ".a")
						   it is usefull for the lines like 
						   "c.a = PARM(0).a" *)
let rec ext_to_string elt typ struct_list typedef_list print_name_var =
	if(is_structure typ)
	then
		let struct_name = String.sub typ 7 ((String.length typ)-7) in
	 	let elt_list = (List.assoc struct_name struct_list) in
		let elt_name_list = fst elt_list in
		let elt_typ_list = snd elt_list in
			let ext_list =							(* List.map build a list, so we have a string list list *)
				List.flatten						(* We only want a string list so we call List.flatten *)
					(List.map2 
						(fun e t -> ext_to_string e 
											(format_to_string t typedef_list)
											struct_list typedef_list true
						)
						elt_name_list
						elt_typ_list
					)
			in
			List.map
				(fun e -> if(print_name_var)
						then elt^"."^e
						else "."^e
				)
				ext_list
	else [elt]





(** Make the list of the formats to print all the fields of a structure
	@param out			string list
	@param in				variable name
						variable type
						list of the structures descriptions
						list of the typedefs descriptions *)
let rec make_format_list elt typ struct_list typedef_list =
	if(is_structure typ)
	then
		let struct_name = String.sub typ 7 ((String.length typ)-7) in
	 	let elt_list = (List.assoc struct_name struct_list) in
		let elt_name_list = fst elt_list in
		let elt_typ_list = snd elt_list in
			List.flatten
				(List.map2 
					(fun e t -> make_format_list e (format_to_string t typedef_list) struct_list typedef_list)
					elt_name_list
					elt_typ_list
				)		 
		else [typ]

(** Print the affectations to pop the structures parameters
	@param out			None (only print the code)
	@param in				Out Channel
						base_type of the parameter to print
						list of the structures descriptions
						list of the typedefs descriptions
						parameter position *)
let print_pop_struct out (_, _, (name, typ, _, _)) struct_list typedef_list i =
List.iter
	(fun e -> Printf.fprintf out "\t\t%s%s = PARM(%d)%s;\n" name e i e)
	(ext_to_string name (format_to_string typ typedef_list) struct_list typedef_list false)


(** Print the affectations to pop the pointers parameters
	@param out			None (only print the code)
	@param in				Out Channel
						base_type of the parameter to print
						parameter position *)
let print_pop_ptr out (typ, _, (name, _, _, _)) i =
	Printf.fprintf out "\t\t%s_addr = (uint32_t) PARM(%d);\n" name i;
	if(is_string typ)
	then	Printf.fprintf out "\t\t%s_length = STRLEN(%s_addr);\n" name name


(** Return the type of the pointer given
	if the pointer is defined by a typedef, the function looks for the reference *)
let get_type_alloc typ typedef_list =
	match typ with
	 Cabs.NAMED_TYPE name ->
		(
			try (String.sub (get_base_type name typedef_list) 0  				(* Get the type of the pointer (int, char, ...) *)
				 		   (try
				 		   	(String.index (get_base_type name typedef_list) '*')-1
				 		    with Not_found -> String.length (get_base_type name typedef_list)
				 		   ))
			with
			Invalid_argument _ -> (get_base_type name typedef_list)
		)
	|_ -> try (String.sub (get_base_type (type_to_string typ) typedef_list) 0  	(* Get the type of the pointer (int, char, ...) *)
				 		   (try
				 		   	(String.index (get_base_type (type_to_string typ) typedef_list) '*')-1
				 		    with Not_found -> String.length (get_base_type (type_to_string typ) typedef_list)
				 		   ))
			with
			Invalid_argument _ -> (get_base_type (type_to_string typ) typedef_list)


(** Return the name of the variable giving the size to allocate
	if the variable called "name" has an attribute STRING,
	the function returns "name_length".
	If a parameter is associated to a pointer,
	the function returns the name of the parameter associated
	Return "XXX if error" *)   
let get_size_name name attr_typ size_ptr_list =
	try List.assoc name size_ptr_list with
	(* The size is not found, it is a string -> STRLEN) *)
	Not_found -> "XXX"
	 	
(** print allocations for a pointer
	@param out			None (only print the code)
	@param in				Out Channel
						base_type of the parameter to print
						list of the pair (pointer_name, size_name)
						typedef list
						list of the parameters
						position of the element in the list *)
let print_ptr_alloc out (attr_typ, _, (name, typ, _, _)) ptr_size_list typedef_list name_list i =
	let size_name = get_size_name name attr_typ ptr_size_list in
	if(size_name = "XXX")
	then Printf.fprintf out "\t/** ERROR : size unknown for the pointer %s **/\n" name
	else
	(
		Printf.fprintf out "\t/* Allocation of the variable %s */\n" name;
		Printf.fprintf out "\t%s = (%s) malloc(%s*sizeof(%s));\n"
					 name (type_to_string typ) size_name
					 (get_type_alloc typ typedef_list)
		 ;
		Printf.fprintf out "\tif(%s == NULL) {\n" name;
		Printf.fprintf out "\t\tfprintf(stderr, \"Erreur Allocation %s\\n\");\n" name;
		(* Free the memory already allocated *)
		for i = 1 to i do
			(* the parameter must be a pointer *)
			if((get_size_name (List.nth name_list (i-1)) attr_typ ptr_size_list) <> "XXX")
			then Printf.fprintf out "\t\tfree(%s);\n" (List.nth name_list (i-1))
		done;
		Printf.fprintf out "\t\tRETURN(-1);\n";
		Printf.fprintf out "\t\treturn FALSE;\n";
		Printf.fprintf out "\t}\n"
	)
	


(** Returns true if the parameter has the gnu_attribute MEM_RD *)
let is_typ_read typ = match typ with
	Cabs.GNU_TYPE(attrs, _) -> 
		let rec arg_read attrs = match attrs with
		 [] -> false
		|(Cabs.GNU_ID "mem_rd")::_ -> true
		|_::tl -> arg_read tl
		in
		arg_read attrs
	|_ -> false
	
(** print the primitive calling with the MEM_READ and MEM_WRITE needed
	@param out			None (only print the code)
	@param in				Out Channel
						base_type of the parameter to print *)
let print_ptr_mem_read out params size_ptr_list = 
	List.iter
		( fun (attr_typ, _, (name, _, _, _)) ->
			if(is_typ_read attr_typ)
			then Printf.fprintf out "\tMEM_READ(%s, %s_addr, %s);\n"
							name name (get_size_name name attr_typ size_ptr_list);
		)
		params




(** Returns true if the parameter has the gnu_attribute MEM_WR *)
let is_typ_write typ = match typ with
Cabs.GNU_TYPE(attrs, _) -> 
	let rec arg_write attrs = match attrs with
	 [] -> false
	|(Cabs.GNU_ID "mem_wr")::_ -> true
	|_::tl -> arg_write tl
	in
		arg_write attrs
|_ -> false


(** print the primitive calling with the MEM_READ and MEM_WRITE needed
	@param out			None (only print the code)
	@param in				Out Channel
						base_type of the parameter to print *)
let print_ptr_mem_write out params use_return = 
		List.iter
			( fun (typ, _, (name, _, _, _)) -> 
				if(is_typ_write typ)
				then if(use_return)
					then Printf.fprintf out "\t/* You try to call MEM_WRITE(%s_addr, %s, ret) but ret does not exist */\n"
						name
						name
					else Printf.fprintf out "\tMEM_WRITE(%s_addr, %s, ret);\n" name name
			)
			params
	
	



(** Print the value of a variable "variable_name = value" for a fprintf
where value is the format corresponding to the type ("%d", "%c", ...).
If the variable is a structure, the function print the value for each field 
	@param out			None (only print the code)
	@param in				Out Channel
						base_type of the parameter to print
						list of the structures descriptions
						list of the typedefs descriptions *)
let print_format out (_, _, (name, typ, _, _)) struct_list typedef_list =
	let elt_list = ext_to_string name (format_to_string typ typedef_list) struct_list typedef_list true in
	let form_list = make_format_list name (format_to_string typ typedef_list) struct_list typedef_list in
		for i = 0 to (List.length elt_list) -1 do
			if( i < (List.length elt_list) -1)
			then Printf.fprintf out "%s = %s, " (List.nth elt_list i) (List.nth form_list i)
			else Printf.fprintf out "%s = %s" (List.nth elt_list i) (List.nth form_list i)
		done
		
(** Print the name of a variable to give the arguent to the fprintf function
If the variable is a structure, the function print the name of each field 
	@param out			None (only print the code)
	@param in				Out Channel
						base_type of the parameter to print
						list of the structures descriptions
						list of the typedefs descriptions *)		
let print_arg_form out (_, _, (name, typ, _, _)) struct_list typedef_list =
	let elt_list = ext_to_string name (format_to_string typ typedef_list) struct_list typedef_list true in
		for i = 0 to (List.length elt_list) -1 do
			Printf.fprintf out ", %s" (List.nth elt_list i)
		done


(** Auxiliar funtion for get_ptr_size *)
let rec get_pair_size name t attrs =
	match attrs with
	 [] -> []
	|(Cabs.GNU_CALL ("length", [Cabs.GNU_CST(Cabs.CONST_STRING s)]))::tl->
		(s,name)::(get_pair_size name t tl)
	|_::tl -> if(((String.length (type_to_string t)) > 5)
				&& (String.sub (type_to_string t) 0 6) = "char *")
			then(name,  name^"_length")::(get_pair_size name t tl)
			else get_pair_size name t tl


(** Return the list of (pointer, size) where
	pointer and size are parameters
	In the function prototype, the parameter size
	is declared with the attribute LENGTH("ptr_name") *)
let get_ptr_size params =
	List.flatten
	(
		List.map
			(fun (typ, _, (name, t, _, _)) ->
				let make_size_list typ =
					match typ with
					(Cabs.GNU_TYPE(attrs, _))-> get_pair_size name t attrs
					|_-> if(((String.length (type_to_string t)) > 5)
 		 					&& (String.sub (type_to_string t) 0 6) = "char *")
	 					then[(name,  name^"_length")]
	 					else []
				in
				make_size_list typ
			)
			params
	)
		

(** Generate the code of the corpus of the function given
	@param out			None (only print the code)
	@param in				Out Channel
						List of system calls
						List of structures definitions
						list of the typedefs descriptions *)
let gen_fct_corpus out (name, rtype, params, attrs) struct_list typedef_list = 
	(* Declarations *)
	Printf.fprintf out "\t/* Variables Declaration */\n";
	let (name_list, form_list, ret_declared) = print_param_decl out params rtype typedef_list in
	
		Printf.fprintf out "\n\t/* Pop the parameters */\n";
		if(List.length name_list > 0)
		then (
			Printf.fprintf out "\tPARM_BEGIN\n";
			for i = 0 to (List.length name_list) -1 do
				if(is_pointer (List.nth form_list i))
				then (* if this is a pointer, we get the addres in PARM(i) *)
					(* in the ppc_addess corrsponding to the variable *)
					print_pop_ptr out (List.nth params i) i
				else
					if(is_structure (List.nth form_list i))
					then (* if this is a structure, we have to print the affectation component by component *)
					print_pop_struct out (List.nth params i) struct_list typedef_list i
					else Printf.fprintf out "\t\t%s = PARM(%d);\n" (List.nth name_list i) i
			done;
			Printf.fprintf out "\tPARM_END\n";
		)
		else Printf.fprintf out "\t/* Useless because the primitive needs no arguments */\n";


		(* get the size of the poitners in the parameters list *)
		let size_ptr_list = get_ptr_size params in
		(* Memory Allocations *)
		Printf.fprintf out "\n\t/* Memory Allocations */\n";
		for i = 0 to (List.length name_list) -1 do
			if(is_pointer (List.nth form_list i))
			then print_ptr_alloc out (List.nth params i) size_ptr_list typedef_list name_list i
		done;
	
		(* Print the primitive calling, to debbug *)
		Printf.fprintf out "\n\tif(verbose)\n";
		Printf.fprintf out "\t\tfprintf(verbose, \"%s(" name;
		(* Print the parameters and their values (in the string argument) *)
		for i = 0 to (List.length name_list) -1 do
			print_format out (List.nth params i) struct_list typedef_list;
			if(i <> (List.length name_list) -1)
			then Printf.fprintf out ", "
		done;
		if(List.length name_list > 0)
		then (
			Printf.fprintf out ");\\n\"";
			(* Print the others arguments given to the function *)
			for i = 0 to (List.length name_list) -1 do
				print_arg_form out (List.nth params i) struct_list typedef_list;
			done;
		)
		else Printf.fprintf out ");\\n\"";
		Printf.fprintf out ");\n";
		
		(* Primitive Calling *)
		Printf.fprintf out "\n\t/* Primitive Calling */\n";
		print_ptr_mem_read out params size_ptr_list;
		if(ret_declared)
		then Printf.fprintf out "\tret = %s(" name					(* Print the return of the primitive *)
		else Printf.fprintf out "\t%s(" name;						(* if it exists *)
		(* Print the arguments of the primitive if they exist *)
		for i = 0 to (List.length name_list) -1 do
			if(i <> (List.length name_list) -1)
			then Printf.fprintf out "%s, " (List.nth name_list i)
			else Printf.fprintf out "%s" (List.nth name_list i)
		done;
		Printf.fprintf out ");\n";
		print_ptr_mem_write out params ((is_use_return attrs) or not ret_declared);
		
		(* Free the memory *)
		for i = 0 to (List.length name_list) -1 do
			if(is_pointer (List.nth form_list i))
			then Printf.fprintf out "\tfree(%s);\n" (List.nth name_list i)
		done;
		
		(* Returns *)
		if is_use_return attrs
		then Printf.fprintf out "\n\t/* Attr USE_RETURN : no RETURN calling */\n\treturn TRUE;\n"
		else
			if(ret_declared)
			then if(is_fd attrs)
				then Printf.fprintf out "\n\tRETURN(fd_new(ret));\n\treturn ret != -1;\n"
				else Printf.fprintf out "\n\tRETURN(ret);\n\treturn ret != -1;\n"
			else Printf.fprintf out "\n\tRETURN(1);\n\treturn TRUE;\n"

	



(* Main Program *)
let _ =
	let on_error msg =
		Printf.fprintf stderr "ERROR: %s\n" msg;
		exit 1 in

	(* Parse arguments *)
	Arg.parse opts (fun arg -> on_error ("bad argument: " ^ arg)) banner;
	if !api = "" then on_error "select an API";

	(* read the C definitions *)
	let defs = match Frontc.parse !args with
		  PARSING_ERROR ->  on_error "syntax erro"
		| PARSING_OK defs -> defs in

	(* get system calls *)
	fill_named defs;
	let calls = get_calls defs in

	(* open the output *)
	let (out, close) =
		if !out_file = "" then (stdout,false)
		else ((open_out !out_file), true) in

	(* look for structure descriptions *)
	let struct_list = get_struct_def defs in
	(* print the structures descriptions (only usefull to debug) *)
	(* Unomment those lines to print the structures definitions *)
(*	List.iter
		(fun (n, (component_list, type_list)) ->
			Printf.fprintf out "structure %s : " n;
			List.iter
			(fun elt ->
				Printf.fprintf out "%s, " elt;
			)
			component_list;
			Printf.fprintf out "\n";
		)
		struct_list;
*)
	(* look for aliases (typedef) *)
	let typedef_list = get_typedef_def defs [] in
	(* print the typedef descriptions (only usefull to debug) *)
	(* Unomment those lines to print the typedef definitions *)
(*	List.iter
		(fun (alias_typedef_list, base_typedef_list) ->
			Printf.fprintf out "typedef : %s = %s\n" alias_typedef_list base_typedef_list 
		)
		typedef_list;
*)	
	
	
	(* generacorpste syscall identifiers *)
	Printf.fprintf out "\n/* syscall identifiers */\n";
	List.iter
		(fun (name, _, _, attrs) ->
			Printf.fprintf out "#define __SYSCALL_%s %d\n" name (get_syscall attrs))
		calls;



	(* generate emulation functions *)
	List.iter
		(fun (name, rtype, params, attrs) ->
			Printf.fprintf out "\nBOOL gliss_syscall_%s(gliss_state_t *state) {\n" name;
			if is_unsupported attrs
			then Printf.fprintf out "\tRETURN(-1); return FALSE;\n"
			else gen_fct_corpus out (name, rtype, params, attrs) struct_list typedef_list;
			Printf.fprintf out "}\n"
		)
		calls;



	(* generate the gliss_syscall function *)
	Printf.fprintf out "\n\n/* gliss_syscall function */\n";
	Printf.fprintf out "void gliss_syscall(gliss_inst_t *inst, gliss_state_t *state) {\n";
	Printf.fprintf out "	int syscall_num;\n";
	Printf.fprintf out "	BOOL ret = FALSE;\n\n";
	Printf.fprintf out "	syscall_num = GLISS_SYSCALL_CODE(inst, state);\n";
	Printf.fprintf out "	if(verbose)\n";
	Printf.fprintf out "		fprintf(verbose, \"got a system call (number : %%u; name : %%s)\\n\", syscall_num, ppc_get_syscall_name(syscall_num));\n";
	Printf.fprintf out "	switch(syscall_num) {\n";
	List.iter (fun (name, _, _, _) ->
			Printf.fprintf out "	case __SYSCALL_%s: ref = gliss_%s(state); break;\n" name name)
		calls;
	Printf.fprintf out "	}\n";
	Printf.fprintf out "	if(!ret) {\n";
	Printf.fprintf out "		if(verbose)\n";
	Printf.fprintf out "			fprintf(verbose, \"Warning : system call returns an error (number : %%u, name : %%s)\\n\", syscall_num, ppc_get_syscall_name(syscall_num));\n";
	Printf.fprintf out "		gliss_sysparm_failed(state);\n";
	Printf.fprintf out "	}\n";
	Printf.fprintf out "	else\n";
	Printf.fprintf out "		gliss_sysparm_succeed(state);\n";
	Printf.fprintf out "}\n\n";

	(* close the output if needed *)
	if close then close_out out

