open Irg
(**
	Describe an expression in a nml format.
	@param expr
		the expression
	@return	
		the describtion
*)
let rec string_of_expr (expr:Irg.expr) : string = match expr with 
          NONE ->
                  "<none>"
        | COERCE (t, e) ->
                "coerce("^ 
                ", "^ 
                string_of_expr e^ 
                ")"
        | FORMAT (fmt, args) ->
                "format(\""^ 
                fmt^ 
                "\", "^ 
                ")"
        | CANON_EXPR (_, n, args) ->
                "\""^ 
                n^ 
                "\" ("^ 
                ")"
        | FIELDOF(t, e, n) -> "[FIELDOF]"^
                e^ 
                "."^ 
                n
        | REF id ->
                id
        | ITEMOF (t, name, idx) ->
                name^ 
                "["^ 
                string_of_expr idx^ 
                "]"^ 
                "(typ="^ 
                ")"
        | BITFIELD (t, e, l, u) ->
                string_of_expr e^ 
                "<"^ 
                string_of_expr l^ 
                ".."^ 
                string_of_expr u^ 
                ">"
        | UNOP (_,op, e) ->
                (string_of_unop op)^ 
                string_of_expr e
        | BINOP (_,op, e1, e2) ->
                "("^ 
                string_of_expr e1^ 
                ")"^ 
                (string_of_binop op)^ 
                "("^ 
                string_of_expr e2^ 
                ")"
        | IF_EXPR (_,c, t, e) ->
                "if "^ 
                string_of_expr c^ 
                " then "^ 
                string_of_expr t^ 
                " else "^ 
                string_of_expr e^ 
                " endif"
        | SWITCH_EXPR (_,c, cases, def) ->
                "switch("^ 
                string_of_expr c^ 
                ")"^ 
                "{ "^ 
                (List.fold_right (fun (c, e) r ->
                                "case "^ 
                                string_of_expr c^ 
                                ": "^ 
                                string_of_expr e^ 
                                " "^r
                        ) (List.rev cases) "")^ 
                "default: "^ 
                string_of_expr def^ 
                " }"
        | ELINE (_, _, e) ->
                string_of_expr e
        | CONST (_,c) ->
                "CONST"
        | EINLINE s ->
                "inline()"
	| CAST(t, e) ->
		"coerce("^
		", "^ 
                string_of_expr e^ 
                ")"
		
(**
	Describe an expression by showing its ocaml type.
	@param expr
		the expression
	@return	
		the describtion
*)
let rec name_of_expr (expr:Irg.expr) : string = match expr with 
          NONE ->
                  "<none>"
        | COERCE (t, e) ->
                "coerce("^ 
                ", "^ 
                name_of_expr e^ 
                ")"
        | FORMAT (fmt, args) ->
                "format(\""^ 
                fmt^ 
                "\", "^ 
                ")"
        | CANON_EXPR (_, n, args) ->
                "[CANON_EXPR]\""^ 
                n^ 
                "\" ("^ 
                ")"
        | FIELDOF(t, e, n) -> "[FIELDOF]"^
                e^ 
                "."^ 
                n
        | REF id ->
                "[REF]"^id
        | ITEMOF (t, name, idx) ->"[ITEMOF]"^
                name^ 
                "["^ 
                name_of_expr idx^ 
                "]"^ 
                "(typ="^ 
                ")"
        | BITFIELD (t, e, l, u) ->"[BITFIELD]"^
                name_of_expr e^ 
                "<"^ 
                name_of_expr l^ 
                ".."^ 
                name_of_expr u^ 
                ">"
        | UNOP (_,op, e) ->"[UNOP]"^
                (string_of_unop op)^ 
                name_of_expr e
        | BINOP (_,op, e1, e2) ->"[BINOP]"^
                "("^ 
                name_of_expr e1^ 
                ")"^ 
                (string_of_binop op)^ 
                "("^ 
                name_of_expr e2^ 
                ")"
        | IF_EXPR (_,c, t, e) ->
                "if "^ 
                name_of_expr c^ 
                " then "^ 
                name_of_expr t^ 
                " else "^ 
                name_of_expr e^ 
                " endif"
        | SWITCH_EXPR (_,c, cases, def) ->
                "switch("^ 
                name_of_expr c^ 
                ")"^ 
                "{ "^ 
                (List.fold_right (fun (c, e) r ->
                                "case "^ 
                                name_of_expr c^ 
                                ": "^ 
                                name_of_expr e^ 
                                " "^r
                        ) (List.rev cases) "")^ 
                "default: "^ 
                name_of_expr def^ 
                " }"
        | ELINE (s, i, e) ->"[ELINE](\""^s^"\", "^(string_of_int i)^
                name_of_expr e
        | CONST (_,c) ->
                "CONST"
        | EINLINE s ->
                "inline()"
        | CAST (t, e) ->
                "coerce[CAST]("^ 
                ", "^ 
                name_of_expr e^ 
                ")"

