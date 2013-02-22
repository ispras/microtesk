(**
Image attribute size computing. 
Used to see if a node can be optimized.

Use : 
{!modules: Irg String_of_expr}

Use a very simple ocamllex lexer.
@see 'formatlexer.mll' Lexer

Gliss2/optirg/image_attr_size.ml

@author Remi Dubot
@author Yaya Ndongo 

@since July 2009.

*)

open Irg
open String_of_expr

(**
	Raised if the attribute image can have different (inconsistent) sizes.
*)
exception InconsistentSize;;



(**
	Parse an string to extract its size and the positions of expressions in the format.
	@param chaine
		the string that describe the format of expressions.
	@return	
		-the number of expressions
		-the list of positions.
	@see 'formatlexer.mll' Lexer
		
*)
let parse (chaine:string):(int * int list)  = 
  let lexbuf = Lexing.from_string  chaine in
   Formatlexer.formatSize 0 0 [] lexbuf

(**
	Parse an string to extract its format class.
	@param chaine
		the string that describe the format of expressions.
	@return	
		format string with x instead of constant bits.
	@see 'formatlexer.mll' Lexer
		
*)
let fparse (chaine:string) = 
  let lexbuf = Lexing.from_string  chaine in
   Formatlexer.formatClass lexbuf

(**
	Parse an format string to extract a ~unique~ code.
	@param chaine
		the string that describe the format of expressions.
	@return	
		format string with x instead of constant bits.
	@see 'formatlexer.mll' Lexer
		
*)
let parse_format_to_code (chaine:string) = 
  let lexbuf = Lexing.from_string  chaine in
   Formatlexer.formatCode 0 lexbuf

exception NoImage

(**
	Extract the expression of an image attribute from the list of attribute.
	@param attr_list
		the list of attributes.
	@return
		the expression of the image attribute.
*)
let rec get_expr_of_image attr_list =
                match attr_list with
                [] -> 
                (* if attr not found => means an empty attr (?) *)
						raise NoImage
                     (* failwith "get_expr_of_image: No image found in this list"  *)
                | ATTR_EXPR(nm, e)::t when nm="image" -> e
                | _::t -> get_expr_of_image t

(**
	Calculate an expression's size. 
	@param e
		the expresions that describes the attribute
	@return
		the size
	@raise InconsistentSize when size depends on derivation. 
*)
let rec sizeOfExpr 
	(listeParam:(string * typ) list)  
	(e:Irg.expr)
	:int = 
	begin
	match e with 
			FIELDOF(STRING, objName, _) | REF(objName)-> 
				(* Aller chercher dans liste param le nom du mode/op correspondant à n *)
				let modop = match (List.assoc objName listeParam) with 
					|	TYPE_ID(name) -> name
					|	_ -> failwith "sizeOfExpr: Field access atemp on simple type."
				in
				sizeOfNodeKey modop 
		|	ELINE(_,_,e) ->  sizeOfExpr listeParam e
		| 	FORMAT(st, expr_list) -> sizeOfFormat listeParam st expr_list
		| 	IF_EXPR (_, _,  eThen,  eElse) -> 
				let sizeThen = sizeOfExpr listeParam eThen 
				and sizeElse = sizeOfExpr listeParam eElse in 
				if(sizeThen <> sizeElse) then 
					raise InconsistentSize
				else 
					sizeThen
		| 	SWITCH_EXPR (type_expr, e , ((_,t)::liste),_) -> 
				let sizeCmp = fun (_,e) size -> 
					if(sizeOfExpr listeParam e <> size) then 
						raise  InconsistentSize 
					else 
						size 
				in 
				List.fold_right (sizeCmp) liste (sizeOfExpr listeParam t) 
		| 	CONST(_,STRING_CONST(st, false, _))-> String.length st
		|	_ -> failwith ("sizeOfExpr : Constructor "^(name_of_expr e)^" of expr is not yet implemented. ")
	end
and

(**
	Calculate the size for an format expression.
	@param listeParam
		the node's list of parameters
	@param st
		the string which describe the format of expressions
	@param expr_l
		the list of expressions that are described.
	@return
		the size
*)
 sizeOfFormat 
	(listeParam:(string * typ) list) 
	(st:string) 
	(expr_l:(Irg.expr list)) 
	:int = 
	begin 
	let (size, op_l)=parse st in  
		match op_l with 
		[] -> size
		|_ -> size + List.fold_right (fun nop somme -> somme + ( sizeOfExpr listeParam (List.nth  expr_l nop) )) op_l 0
	end
and	
(**
	Return the size of a node's expression
	@param key
		the node's name which is the key in the hashtable.
	@return
		 the size
*)
 	sizeOfNodeKey  
	(key:string) 
	:int = 
	try
		sizeOfSpec (Irg.get_symbol key)
	with Irg.Symbol_not_found(n) -> failwith ("image_attr_size.ml::sizeOfNodeKey : \""^n^"\" is not present in syms. ")
and
(**
	Return the size of a node's expression.
	@param spec
		a spec which reprensent a node in the hashtable.
	@return
		 the size
*)
	sizeOfSpec 
	(spec:Irg.spec)
	:int = 
	begin
	match spec with 
	| 	AND_MODE (_,listeParam,_,attr_list) | AND_OP(_,listeParam, attr_list) -> sizeOfExpr listeParam (get_expr_of_image attr_list)
	| 	OR_MODE (_,st_list) | OR_OP (_,st_list) ->
			(match (List.map (sizeOfNodeKey) st_list) with
			| [] -> failwith "sizeOfSpec : OR node with no sons"
			| h::size_list ->
				List.fold_right (fun s size -> if(s <> size) then raise  InconsistentSize else size ) size_list h)
	| _ ->	failwith "sizeOfSpec : Node of spec not implemented "
	end



exception NotFormated

(**
	Calculate an expression's format class. 
	@param e
		the expresions that describes the attribute
	@return
		the format class
	@raise NotFormated when the image attr have not its own image. 
*)
let rec fclassOfExpr 
	(e:Irg.expr)
	:string * (string list) = 
	begin
	match e with 
		|	ELINE(_,_,e) ->  fclassOfExpr e
		| 	FORMAT(st, expr_list) -> fclassOfFormat st expr_list
		| 	CONST(_,STRING_CONST(st, false, _))-> (fparse st,[])
		|	_ ->  raise NotFormated
	end
and

(**
	Calculate the format class for an format expression.
	@param st
		the string which describe the format of expressions
	@param expr_l
		the list of expressions that are described.
	@return
		the format class
	@raise NotFormated when the image attr have not its own image. 
*)
 fclassOfFormat 
	(st:string) 
	(expr_list)
	:string * (string list) = 
	let gen_str=fparse st in  
	(gen_str,List.map (String_of_expr.string_of_expr) expr_list)
and	
(**
	Return the format class of a node's expression
	@param key
		the node's name which is the key in the hashtable.
	@return
		 the format class
	@raise NotFormated when the image attr have not its own image. 
*)
 fclassOfNodeKey  
	(key:string) 
	:string * (string list) = 
	try
		fclassOfSpec (Irg.get_symbol key)
		with Irg.Symbol_not_found(n) -> failwith ("image_attr_size.ml::fclassOfNodeKey : \""^n^"\" is not present in syms. ")
and
(**
	Return the format class of a node's expression.
	@param spec
		a spec which reprensent a node in the hashtable.
	@return
		 the format class
	@raise NotFormated when the image attr have not its own image. 
*)
	fclassOfSpec 
	(spec:Irg.spec)
	:string * (string list) = 
	begin
	match spec with 
	| 	AND_MODE (n,_,_,attr_list) | AND_OP(n,_, attr_list) -> 
			(try fclassOfExpr (get_expr_of_image attr_list) with 
				NoImage -> 
					(print_string ("gliss2/optirg/image_attr_size.ml::fclassOfSpec : \nWarning :No image attribute found for "^n^". \n");
					("",[]))
			)
	| _ ->	failwith "sizeOfSpec : Node of spec not implemented "
	end
