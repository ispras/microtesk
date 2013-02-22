(**
Irgp : Irg Libray complement 

Rémi Dubot <dubot@irit.fr>
Yaya Ndongo <ndongo@irit.fr>

Gliss2::optirg

July 2009.

*)

open Irg

type nodeType = NotANode|Mode | Op

(** 
	Provide the list of all sons's name for a parent node.  
	@param s
		the name of the parent node
	@return 
		the string list that contains the names
*)
let next (s:string) : (string list) = match (get_symbol s) with
	|AND_MODE(_,pl,_,_) | AND_OP(_,pl,_) 
		-> List.fold_right (fun (_,t) r ->match t with TYPE_ID(s)->s::r | _ -> r) pl []
	|OR_MODE(_,sl) | OR_OP(_,sl) 
		-> sl
	| _ 
		-> []


(** Iterator on symboles canvas. *)


let rec syms_fold_right (f:string->'a->'a) (father:string) (res:'a) :'a = 
	let sons: string list = next father in 
	List.fold_right (syms_fold_right f) sons (f father res)

let iter (f:string->unit) :unit = 
	let rec loop father = 
		(f father);
		List.iter (loop) (next father)
	in
	loop "instruction"

(** Replace the spec associated with a name in syms *)
let replace_symbol name spec = Irg.rm_symbol name; Irg.add_symbol name spec


