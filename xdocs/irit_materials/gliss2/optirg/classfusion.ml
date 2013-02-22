(**
Horizontal fusion by class.

Gliss2/optirg/classfusion.ml

@author Remi Dubot
@author Yaya Ndongo 

@since July 2009.

*)

open Node_classes


(** 
	Fusion of and nodes
*)
let fusion_nodes
	(and_nodes:Irg.spec list) 
	(name:string) 
	(size:int)
	(t:Irgp.nodeType)
	:Irg.spec
	= 
	let val_attr = Irg.ATTR_EXPR("__val",Irg.REF("__code")) in 
	let new_attr_list = val_attr::(Sw_fun.attr_list_from_and_node and_nodes size) in
	match t with
		| Irgp.NotANode -> failwith ("optirg/classfusion.ml::fusion_nodes : "^name^" is not a node class. ")
		(* Case in which we have a MODE *)
		| Irgp.Mode -> 
			let val_expr = 
				Irg.SWITCH_EXPR(
					Irg.STRING, 
					Irg.REF("__code"), 
					(List.map (Sw_fun.case_from_value_expr size) and_nodes), 
					Irg.NONE
				) 
			in
			Irg.AND_MODE(name,[("__code",Irg.TYPE_EXPR(Irg.CARD(size)))], val_expr, new_attr_list)

		(* Case in which we have a MODE *)
		| Irgp.Op -> 
			Irg.AND_OP(name,[("__code",Irg.TYPE_EXPR(Irg.CARD(size)))], new_attr_list)



(**
	Fusion by classes
	(or_node  <-> partition)
	(and_node <-> class)
	
*)
let fusion_by_classes (node_to_optimize:Irg.spec) :unit = 
	let t = match node_to_optimize with 
		| Irg.OR_MODE(_,_) 	-> Irgp.Mode
		| Irg.OR_OP(_,_) 	-> Irgp.Op
		| _ 			-> Irgp.NotANode
	in
	if t=Irgp.NotANode then 
		() (* Not Optimizable here, so nothing to do. *)
	else 
		let (name, nodes_name_list) = match node_to_optimize with 
			| Irg.OR_MODE(name, nodes_list) | Irg.OR_OP(name, nodes_list) 
				-> (name, nodes_list)
			| _ 	-> failwith "Impossible"
		in
		let nodes_list = 
			try
				List.map (Irg.get_symbol) nodes_name_list
			with Irg.Symbol_not_found(n) -> failwith ("fusion_by_classes : A son (\""^n^"\") is not present in syms. ")
		in
		let part = classify nodes_list in 
		let _ = print_string (string_of_partition part) in 
		(** Create node if possible then add it to Irg.syms and return name of the node in a list. *)
		let and_node_from = function 
			(* Nodes not optimizable, so we keep them as they are : no changes in syms and names stays the sames. *)
			| (NotOptimizable, spec_list) | (_, ([_] as spec_list))		->  List.map (Irg.name_of) spec_list
			| (Class(_,args,attrs,t), nodes)	-> 
				let size = Image_attr_size.sizeOfSpec (List.hd nodes) in
				let name = String.concat "_" (List.map (Irg.name_of) nodes) in 
				let and_node = fusion_nodes nodes name size t in (* --Fusion-- *)
				Irg.add_symbol name and_node ; 
				[name]
		in
		let sons_name_list = List.fold_right (fun part res -> (and_node_from part)@res ) part [] in 
		let or_node = match t with
			| Irgp.Op 	-> Irg.OR_OP(name,sons_name_list)
			| Irgp.Mode 	-> Irg.OR_MODE(name,sons_name_list)
			| Irgp.NotANode -> Irg.UNDEF (* should never happen as we filter this out in a higher if *)
		in 
		Irgp.replace_symbol name or_node
(** 
	h_optimize
	
*)
let h_optimize () = 
	print_string "Horizontal : " ;
	Irgp.iter (fun n -> fusion_by_classes (Irg.get_symbol n)) ; 
	print_string "OK\n" 






