{

(** Templater allows to generate files from templates.
	This templates may contains special token that are replaced
	by values retrieved from a dictionnary.

	Basically, an expression "$(identifier)" is replaced by a text
	found in the dictionnary.

	Templates language support support conditional statement
	in form "$(if identifier) ... $(end)" or
	"$(if identifier) ... $(else) ... $(end)". The identifier is looked
	in the dictionnary and must be resolved as a boolean.

	Loops are allowed using "$(foreach identifier) ... $(end)". In this
	case, the identifier must be resolved to a collection and the loop body
	is generated as many times there is elements in the collection.
	Identifiers contained in the body are resolved against special
	dictionnaries associated with each collection element.

	Finally, notes that "$$" expression is reduceded to "$$".
	*)

(** Type of dictionnaries. *)
type dict_t = (string * value_t) list

(** Values of a dictionnary *)
and  value_t =
	  TEXT of (out_channel -> unit)						(** function called when identifier is found *)
	| COLL of ((dict_t -> unit) -> dict_t -> unit)		(** collection : argument function must be called for each element
															with a dictionnary fixed for the current element. *)
	| BOOL of (unit -> bool)							(** boolean value *)
	| FUN of (out_channel -> string -> unit)			(** function value *)


type state_t =
	| TOP
	| THEN
	| ELSE
	| FOREACH


(** Perform text evaluation (and function if any)
	@param out	Out channel.
	@param dict	Used dictionnary.
	@param id	Text identifier. *)
let do_text out dict id =
	let p =  try String.index id ':' with Not_found -> -1 in
	try
	
	(* function call *)
	if p >= 0 then
		let id = String.sub id 0 p in
		(match List.assoc id dict with
		  FUN f ->
			(try f out (String.sub id (p + 1) ((String.length id) - p - 1))
			with Not_found -> failwith ("error in generation with "^id))
		| _ -> failwith (id ^ " is not a text !"))
	
	(* simple variable eveluation *)	
	else
		match List.assoc id dict with
		  TEXT f ->
			(try f out
			with Not_found -> failwith (Printf.sprintf "uncaught Not_found in generation with \"%s\"" id))
		| _ -> failwith (id ^ " is not a text !")
	
	(* not existing symbol *)
	with Not_found ->
		List.iter (fun (n, _) -> Printf.printf "=>[%s]\n" n) dict;
		Printf.printf "<=[%s]\n" id;
		failwith (id ^ " is undefined !")


(** Get a collection.
	@param dict	Dictionnary to look in.
	@param id	Identifier.
	@return		Found collection function. *)
let do_coll dict id =
	try
		(match List.assoc id dict with
		  COLL f -> f
		| _ -> failwith (id ^ " is not a collection"))
	with Not_found ->
		failwith (id ^ " is undefined !")


(** Get a boolean value.
	@param dict		Dictionnary to look in.
	@param id		Identifier.
	@return			Boolean value. *)
let do_bool dict id =
	try
		(match List.assoc id dict with
		  BOOL f -> f ()
		| _ -> failwith (id ^ " is not a boolean"))
	with Not_found ->
		false


}

let blank = [' ' '\t']
let id = [^ ' ' '\t' ')']+

rule scanner out dict state = parse
  "$$"
  	{ output_char out '$'; scanner out dict state lexbuf }

|  "$(foreach" blank (id as id) ")" '\n'?
  	{
		let buf = Buffer.contents (scan_end (Buffer.create 1024) 0 lexbuf) in
		let f = do_coll dict id in
		f (fun dict -> scanner out dict FOREACH (Lexing.from_string buf)) dict;
		scanner out dict state lexbuf
	}

| "$(end)" '\n'?
	{ () }

| "$(else)" '\n'?
	{	if state = THEN then skip out dict 0 lexbuf
		else failwith "'else' out of 'if'" }

| "$(if" blank '!' (id as id) ')' '\n'?
	{	if not (do_bool dict id) then scanner out dict THEN lexbuf
		else skip out dict 0 lexbuf;
		scanner out dict state lexbuf }

| "$(if" blank (id as id) ')' '\n'?
	{	if do_bool dict id then scanner out dict THEN lexbuf
		else skip out dict 0 lexbuf;
		scanner out dict state lexbuf }

| "$(" ([^ ')']+ as id) ")"
	{ do_text out dict id; scanner out dict state lexbuf }

| _ as c
	{ output_char out c; scanner out dict state lexbuf }

| eof
	{ () }


and skip out dict cnt = parse
  "$$"
  	{ skip out dict cnt lexbuf }
| "$(foreach" blank?
  	{ skip out dict (cnt + 1) lexbuf }
| "$(if" blank?
	{ skip out dict (cnt + 1) lexbuf }
| "$(end)" '\n'?
	{ if cnt = 0 then () else skip out dict (cnt -1) lexbuf }
| "$(else)" '\n'?
	{	if cnt = 0 then scanner out dict ELSE lexbuf
		else skip out dict cnt lexbuf }
| _
	{ skip out dict cnt lexbuf }
| eof
	{ failwith "unclosed if" }


and scan_end buf cnt = parse
  "$$" as s
  	{ Buffer.add_string buf s; scan_end buf cnt lexbuf }
| "$(foreach" blank as s
  	{ Buffer.add_string buf s; scan_end buf (cnt + 1) lexbuf }
| "$(if" blank as s
	{ Buffer.add_string buf s; scan_end buf (cnt + 1) lexbuf }
| "$(end)" as s
	{ if cnt = 0 then buf
	else (Buffer.add_string buf s; scan_end buf (cnt - 1) lexbuf) }
| _ as c
	{ Buffer.add_char buf c; scan_end buf cnt lexbuf }
| eof
	{ failwith "unclosed foreach" }


{
(** Perform a template generation.
	@param dict		Dictionnary to use.
	@param in_path	Input template path.
	@param out_path	Path of the output file. *)
let generate_path dict in_path out_path =
	let output = open_out out_path in
	let input = open_in in_path in
	scanner output dict TOP (Lexing.from_channel input);
	close_in input;
	close_out output


(** Perform a template generation.
	@param dict		Dictionnary to use.
	@param template	Template name (take from SOURCE_DIRECTORY/templates)
	@param out_path	Path of the output file. *)
let generate dict template out_path =
	generate_path dict (Config.source_dir ^ "/templates/" ^ template) out_path
}

