(**
Horizontal fusion by class.

Gliss2/optirg/classfusion.ml

@author Remi Dubot
@author Yaya Ndongo 

@since July 2009.

*)


(** {2 Attribute description} *)

(**
	And node attribute description
*)
type attr_desc = 
	| ATTR_EXPR_d of string
	| ATTR_STAT_d of string
	| ATTR_USES_d
	| ATTR_LOC of string

(**
	Give attribute description from attribut
*)
let get_attr_desc (a:Irg.attr) :attr_desc = 
	match a with
	| Irg.ATTR_EXPR(s,_) -> ATTR_EXPR_d(s)
	| Irg.ATTR_STAT(s,_) -> ATTR_STAT_d(s)
	| Irg.ATTR_USES -> ATTR_USES_d
	| Irg.ATTR_LOC (n, _) -> ATTR_LOC n


(** {2 Nodes classes and partitions in classes} *)

(** 
	{1 The differents class of optimizable nodes}
*)

(** Type of format string in an image attribute *)
type formatClass = string * (string list) (** Where "x" replace a constant bit *)

(** Type representing the differents classes of node *)
type nodeOptClass = 
	| NotOptimizable
	| Class of 
			formatClass 				(* Class of image format string *)
		* 	(string * Irg.typ) list 	(* arg list *)
		* 	attr_desc list 				(* attr list *)
		*	Irgp.nodeType				(* Mode/Op*)

let string_of_nodeOptClass = function 
	| NotOptimizable -> "NotOptimizable"
	| Class((form_str,arg_list),_,_,_) -> 
	let form_param = String.concat ", " (form_str::arg_list) in 
	"Class(\n\t("^form_param^"), \n\t_, \n\t_, \n\t_\n) "

(**
	Assoc list representing the partition of a set of node in nodeOptClass
*)
type partition = (nodeOptClass * (Irg.spec list)) list

let string_of_partition part = 
	let disp = fun (noc,spec_l) -> "("^(string_of_nodeOptClass noc)^",["^(String.concat "," (List.map (Irg.name_of) spec_l))^"]) " in 
	String.concat "\n\n" (List.map (disp) part)


(**
	Add an allready classified node in the partition.
*)
let add_to_partition 
	((node,nclass):(Irg.spec * nodeOptClass)) 
	(part:partition)
	 = 
	let rec loop = function 
		| [] -> [(nclass, [node])]
		| (c, nl)::r when c=nclass -> (c, node::nl)::r
		| h::t -> h::(loop t)
	in
	loop part

(**
	Compute node class
*)
let get_class 
	(node:Irg.spec) 
	:nodeOptClass 
	 = 
	let name = Irg.name_of node in 
	let format_class = Image_attr_size.fclassOfNodeKey name in 
	let mk_class arg_list att_list t = 
		try
			let attr_desc_list = List.map (get_attr_desc) att_list in 
			Class(format_class, arg_list, attr_desc_list, t) 
		with _ -> NotOptimizable
	in 
	match node with 
	|	Irg.AND_MODE(name, arg_list, _, attr_list) ->
		mk_class arg_list attr_list Irgp.Mode
	| 	Irg.AND_OP(name, arg_list, attr_list) ->
		mk_class arg_list attr_list Irgp.Op
	|	_ -> NotOptimizable

(** 
	Create partition from a list of nodes
*)
let classify (nodes:Irg.spec list) :partition = 
	List.fold_right (fun node part -> add_to_partition (node,get_class node) part) nodes []
