
open Irg
open Graphirg

(* argument list *)
let irg = ref ""
let out = ref "" 
let tree = ref false
let options = [("-t", Arg.Set tree, "Make graph as a tree.")]

(* argument decoding *)
let free_arg arg =
	if !irg = "" then irg := arg else
	if !out = "" then out := arg else
	raise (Arg.Bad "only IRG and dot files required") 
let usage_msg = "SYNTAX: graphirg [-t] IRG_SRC DOT_DEST\n\tGenerate graphviz dot file from IRG."

let arg_error msg =
		Printf.fprintf stderr "ERROR: %s\n" msg;
		Arg.usage options usage_msg;
		exit 1

let _ =
	Arg.parse options free_arg usage_msg;
	if !irg = "" then arg_error "one IRG source file must be given !\n";
	if !out = "" then arg_error "one dot dest file must be given !\n"

let _ =
	try	
		begin
			Irg.load !irg;
			let graph = if(!tree) then Graphirg.mk_tree_graph else Graphirg.mk_graph in
			Graphirg.write (graph "instruction") !out; (* Entry point : need to be changed ! *)
		end
	with
	  Parsing.Parse_error ->
		Lexer.display_error "syntax error"; exit 2
	| Lexer.BadChar chr ->
		Lexer.display_error (Printf.sprintf "bad character '%c'" chr); exit 2
	| Sem.SemError msg ->
		Lexer.display_error (Printf.sprintf "semantics error : %s" msg); exit 2
	| Irg.IrgError msg ->
		Lexer.display_error (Printf.sprintf "ERROR: %s" msg); exit 2
	| Sem.SemErrorWithFun (msg, fn) ->
		Lexer.display_error (Printf.sprintf "semantics error : %s" msg);
		fn (); exit 2
