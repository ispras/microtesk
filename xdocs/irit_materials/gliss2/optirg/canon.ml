(**
Vertical fusion of Or_Nodes.

Gliss2/optirg/canon.ml

@author Rémi Dubot 
@author Yaya Ndongo 

@since July 2009.

*)


open Irg
open Irgp




let or_fusion () = 
	let replace name spec = Irg.rm_symbol name; Irg.add_symbol name spec in 
	(** call loop on each son *)
	let rec flat_sons sons = List.fold_right (fun son res -> (loop son)@res) sons []
	(** 
	loop 
	@param : node name
	@return : 
		if it's an OR then the treated name list of its sons and replace the node in the hshtbl
		if it's an AND then its name and call recursively on its sons
		else return its name.
	*)
	and loop name = match get_symbol name with
		| OR_MODE(_,sons) -> 
			replace name (OR_MODE(name, flat_sons sons)) ; flat_sons sons
		| OR_OP(_,sons) -> 
			replace name (OR_OP(name, flat_sons sons)) ; flat_sons sons
		| _ -> 
			ignore (flat_sons (next name)) ; [name]
	in
loop "instruction"




let canon () = 
	begin
	print_string "Vertical : " ;
	ignore (or_fusion ());
	print_string "OK\n" 
	end

