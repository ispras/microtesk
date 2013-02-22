(**
SWitch FUNctions
Toolkit for switch generation in optimizations.

Gliss2/optirg/sw_fun.ml

@author Remi Dubot
@author Yaya Ndongo 

@since July 2009.

*)

open Irg


(**
	Provide the value of a node.
	@param and_node
		the and_node.
	@return 
		the value of the node.
*)

let get_expr_from_value_from_and_mode (and_node:Irg.spec) :Irg.expr = 
	match and_node with 
		| AND_MODE(_,_,e,_) -> e
		| _ -> failwith "get_expr_from_value_from_and_mode: 1st argument must be an AND MODE. "


(**
	Extract switch case number from an And Node spec. 
	We use here the image attribute value as case number. 
	@param  s 
		the node 
	@return 
		the code of the node
*)

let case_code_from_spec (s:Irg.spec) :int = match s with 
	| Irg.AND_MODE (_,_,_,attr_list)|Irg.AND_OP (_,_,attr_list) -> 
		let image_expr = (Image_attr_size.get_expr_of_image attr_list) in 
		let rec int_of_expr expr = 
			match expr with 
			| CONST(STRING,STRING_CONST(image_string, false, _)) -> 
				int_of_string ("0b"^image_string)
			| ELINE(_,_,e) -> int_of_expr e
			| FORMAT(f_str,_) -> Image_attr_size.parse_format_to_code f_str
			| _ -> failwith ("optirg/sw_fun.ml::case_code_from_spec: Optimization need constant string as image. ("^(String_of_expr.name_of_expr image_expr)^") ")
		in
			int_of_expr image_expr
	| _ -> failwith "optirg/sw_fun.ml::case_code_from_spec: require an and_node as argument."


(**
	Compute case structure of SWITCH_EXPR from an And Node  and the attribute name. 
	@param size 
		the image size of the attribute
	@param attr_name
		the name of the attribute
	@param and_node 
		the node that contains the attribute
	@return 
		the case for an switch expression
*)

let case_from_attr_expr size (attr_name:string) (and_node:Irg.spec) :(Irg.expr*Irg.expr) = 
	(
		Irg.CONST(Irg.CARD(size),Irg.CARD_CONST(Int32.of_int (case_code_from_spec and_node))), 
		Instantiate.get_expr_from_attr_from_op_or_mode and_node attr_name
	)


(**
	Compute case structure of SWITCH_STAT from an And Node spec and the attribute name. 
	param size=size of CARD parameter of the new and_node (in bit).
	@param size 
		the image size of the attribute
	@param attr_name
		the name of the attribute
	@param and_node 
		the node that contains the attribute
	@return 
		the case for an switch statement 
*)

let case_from_attr_stat size (attr_name:string) (and_node:Irg.spec) :(Irg.expr*Irg.stat) = 
	(
		Irg.CONST(Irg.CARD(size),Irg.CARD_CONST(Int32.of_int (case_code_from_spec and_node))), 
		Instantiate.get_stat_from_attr_from_spec and_node attr_name
	)


(**
	Compute case structure of SWITCH_EXPR to return value from an And Node spec. 
	@param and_node 
		the node that contains the attribute
	@return
		the case for an switch expression
*)

let case_from_value_expr size (and_node:Irg.spec) :(Irg.expr*Irg.expr) = 
	(
		Irg.CONST(Irg.CARD(size),Irg.CARD_CONST(Int32.of_int (case_code_from_spec and_node))), 
		get_expr_from_value_from_and_mode and_node
	)


(**
	Return the type of the expression
	@param expr
		An Irg.expr 
	@return 
		An Irg.type_expr
*)

let rec type_of_expr (expr:Irg.expr) : Irg.type_expr = match expr with 
	| 	Irg.NONE -> Irg.NO_TYPE
	| 	Irg.COERCE(type_expr,_) -> type_expr
	| 	Irg.FORMAT(_,_)-> Irg.STRING
	| 	Irg.CANON_EXPR( type_expr,_,_)-> type_expr
	| 	Irg.REF(_)-> Irg.UNKNOW_TYPE
	| 	Irg.FIELDOF(type_expr,_,_) -> type_expr
	| 	Irg.ITEMOF (type_expr,_,_)-> type_expr
	| 	Irg.BITFIELD (type_expr,_,_,_) -> type_expr
	| 	Irg.UNOP (type_expr,_,_)-> type_expr
	| 	Irg.BINOP (type_expr,_,_,_)-> type_expr
	| 	Irg.IF_EXPR (type_expr,_,_,_)-> type_expr
	| 	Irg.SWITCH_EXPR (type_expr,_,_,_)-> type_expr
	| 	Irg.CONST (type_expr,_)-> type_expr
	| 	Irg.ELINE (_,_,e)-> type_of_expr e
	| 	Irg.EINLINE(_)-> Irg.NO_TYPE
	|	Irg.CAST(type_expr, _) -> type_expr


(**
	Extract a list of attributes from a list of and nodes.
	@param and_list
		the liste of nodes
	@param size
		an attribute's size.
	@return 
		the list of attribute 
*)

let attr_list_from_and_node 
	(and_list : Irg.spec list)
	(size : int)
	:Irg.attr list = 
	match List.hd and_list with 
	| Irg.AND_OP(_,_,attr_list) 
	| Irg.AND_MODE(_,_,_,attr_list)
		-> List.map 
			(
			function 
			|Irg.ATTR_EXPR(name,e) when name="image"-> 
				ATTR_EXPR(
					name,
					Irg.FORMAT("%"^(string_of_int size)^"b",[Irg.REF("code")])
				)
			|Irg.ATTR_EXPR(name,e) -> 
				ATTR_EXPR(
					name,
					SWITCH_EXPR(
						(type_of_expr e), 
						REF("code"), 
						(List.map (case_from_attr_expr size name) and_list) , 
						Irg.NONE
					)
				)
			|Irg.ATTR_STAT(name,_) -> 
				ATTR_STAT(
					name,
					SWITCH_STAT(
						REF("code"), 
						List.map (case_from_attr_stat size name) and_list, 
						Irg.NOP
					)
				)
			|Irg.ATTR_LOC _ -> failwith "optirg : attr_list_from_and_node : ATTR_USES not implemented yet."
			|Irg.ATTR_USES -> failwith "optirg : attr_list_from_and_node : ATTR_USES not implemented yet."
			) 
			attr_list
	| _ -> failwith "optirg : attr_list_from_and_node : We must have AND Node here. "

