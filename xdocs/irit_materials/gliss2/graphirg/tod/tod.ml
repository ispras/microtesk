(** ToD allow to build and manage .dot graphs.
	
	ToD stands for "something To Dot".
	
	@author Florian Birée <florian\@biree.name>
	@version 0.1
	@see <http://www.graphviz.org/> the official .dot website for more
		informations on .dot graphs.
*)

(** Types to define a .dot graph *)

(** Every kind of graphs *)
type dot =
	  Graph of string * bool * stmt list
		(** undirected graphs (name, is_strict, statements) *)
	| Digraph of string * bool * stmt list
		(** directed graphs (name, is_strict, statements) *)

(** Subgraph *)
and subgraph = string * stmt list

(** Element of an edge *)
and edge_element =
	  NodeID of node_id
	| Subgraph of subgraph

(** Statement of the graph *)
and stmt =
	  Node of node_id * node_attribute list
		(** Node *)
	| Edge of edge_element list * edge_attribute list
		(** Edge *)
	| Attribute of graph_attribute
	| GraphAttr of graph_attribute list
	| NodeAttr of node_attribute list
	| EdgeAttr of edge_attribute list

(** Node identifiers *)
and node_id = string

(** Graph attribute *)
and graph_attribute =
	  GBgColor of string
		(** Background color for drawing, plus initial fill color *)
	| GCenter of bool
		(** Center drawing on page *)
	| GClusterRank of string
		(** May be global or none *)
	| GColor of string
		(** For clusters, outline color, and fill color if fillcolor
			not defined *)
	| GComment of string
		(** any string (format-dependent) *)
	| GCompound of bool
		(** Allow edges between clusters *)
	| GConcentrate of bool
		(** Enables edge concentrators *)
	| GFillColor of string
		(** Cluster fill color *)
	| GFontColor of string
		(** Type face color *)
	| GFontName of string
		(** Font family *)
	| GFontPath of string
		(** List of directories to search for fonts **)
	| GFontSize of int
		(** Point size of label *)
	| GLabel of string
		(** Any string *)
	| GLabelJust of string
		(** "l" and "r" for left- and right-justified cluster labels,
			respectively *)
	| GLabelLoc of string
		(** "t" and "b" for top- and bottom-justified cluster labels,
			respectively *)
	| GLayers of string
		(** id:id:id... *)
	| GMargin of float
		(** Margin included in page, inches *)
	| GMcLimit of float
		(** Scale factor for mincross iterations *)
	| GNodeSep of float
		(** Separation between nodes, in inches. *)
	| GNSLimit of int
		(** If set to f, bounds network simplex iterations by
			(f)(number of nodes) when setting x-coordinates *)
	| GNSLimit1 of int
		(** If set to f, bounds network simplex iterations by
			(f)(number of nodes) when ranking nodes *)
	| GOrdering of string
		(** If out out edge order is preserved *)
	| GOrientation of string
		(** If rotate is not used and the value is landscape, use landscape
			orientation *)
	| GPage of string
		(** Unit of pagination, e.g. "8.5,11" *)
	| GPageDir of string
		(** Traversal order of pages *)
	| GQuantum of float
		(** If quantum ? 0.0, node label dimensions will be rounded to integral
			multiples of quantum *)
	| GRank of string
		(** same, min, max, source or sink *)
	| GRankDir of string
		(** LR (left to right) or TB (top to bottom) *)
	| GRankSep of float
		(** Separation between ranks, in inches. *)
	| GRatio of string
		(** Approximate aspect ratio desired, fill or auto *)
	| GReminCross of bool
		(** If true and there are multiple clusters, re-run crossing
			minimization *)
	| GRotate of int
		(** If 90, set orientation to landscape *)
	| GSamplePoints of int
		(** Number of points used to represent ellipses and circles on output *)
	| GSearchSize of int
		(** Maximum edges with negative cut values to check when looking for a
			minimum one during network simplex *)
	| GSize of float
		(** Maximum drawing size, in inches *)
	| GStyle of string
		(** graphics options, e.g. filled for clusters *)
	| GURL of string
		(** URL associated with graph (format-dependent) *)

(** Node attribute *)
and node_attribute = 
	  NColor of string
		(** Node shape color *)
	| NComment of string
		(** A comment *)
	| NDistortion of float
		(** Node distortion for shape=polygon *)
	| NFillColor of string
		(** Node fill color *)
	| NFixedSize of bool
		(** Label text has no effect on node size *)
	| NFontColor of string
		(** Type face color *)
	| NFontName of string
		(** Font family *)
	| NFontSize of int
		(** Point size of label *)
	| NGroup of string
		(** Name of node's group *)
	| NHeight of float
		(** Height in inches *)
	| NLabel of string
		(** Any string, default to node name *)
	| NLayer of string
		(** All, id or id:id *)
	| NOrientation of float
		(** Node orientation angle *)
	| NPeripheries of int
		(** Number of node boundaries *)
	| NRegular of bool
		(** Force polygon to be regular *)
	| NShape of string
		(** Node shape *)
	| NShapefile of string
		(** External EPSF or SVG custom shape file *)
	| NSides of int
		(** Number of sides for shape=polygon *)
	| NSkew of float
		(** Skewing of node for shape=polygon *)
	| NStyle of string
		(** Graphics options *)
	| NURL of string
		(** URL associated with node *)
	| NWidth of float
		(** Width in inches *)
	| NZ of float
		(** Z coordinate for VRML output *)

and edge_attribute =
	  EArrowHead of string
		(** Style of arrowhead at head end *)
	| EArrowSize of float
		(** Scaling factor for arrowheads *)
	| EArrowTail of string
		(** Style of arrowhead at tail end *)
	| EColor of string
		(** Edge stroke color *)
	| EComment of string
		(** Any string (format-dependent) *)
	| EConstraint of bool
		(** Use edge to affect node ranking *)
	| EDecorate of bool
		(** If set, draws a line connecting labels with their edges *)
	| EDir of string
		(** forward, back, both, or none *)
	| EFontColor of string
		(** type face color *)
	| EFontName of string
		(** font family *)
	| EFontSize of int
		(** Point size of label *)
	| EHeadLabel of string
		(** Label placed near head of edge *)
	| EHeadPort of string
		(** n,ne,e,se,s,sw,w,nw *)
	| EHeadURL of string
		(** URL attached to head label if output format is ismap *)
	| ELabel of string
		(** Edge label *)
	| ELabelAngle of float
		(** Angle in degrees which head or tail label is rotated off edge *)
	| ELabelDistance of float
		(** Scaling factor for distance of head or tail label from node *)
	| ELabelFloat of bool
		(** Lessen constraints on edge label placement *)
	| ELabelFontColor of string
		(** Type face color for head and tail labels *)
	| ELabelFontName of string
		(** Font family for head and tail labels *)
	| ELabelFontSize of int
		(** Point size for head and tail labels *)
	| ELayer of string
		(** all, id or id:id *)
	| ELHead of string
		(** Name of cluster to use as head of edge *)
	| ELTail of string
		(** Name of cluster to use as tail of edge *)
	| EMinLen of int
		(** Minimum rank distance between head and tail *)
	| ESameHead of string
		(** Tag for head node; edge heads with the same tag are
			merged onto the same port *)
	| ESameTail of string
		(** Tag for tail node; edge tails with the same tag are merged
			onto the same port *)
	| EStyle of string
		(** Graphics options *)
	| ETailLabel of string
		(** Label placed near tail of edge *)
	| ETailPort of string
		(** n,ne,e,se,s,sw,w,nw *)
	| ETailURL of string
		(** URL attached to tail label if output format is ismap *)
	| EWeight of int
		(** Integer cost of stretching an edge *)


(** Functions to manage a graph: *)

(** Add a statement to the graph.
	@param graph the graph to modify.
	@param stmt the statement to be add.
	@return the modified graph.
*)
let add_stmt graph stmt =
	match graph with
		| Graph(name, strict, stmt_list) ->
			Graph(name, strict, stmt_list @ [stmt])
		| Digraph(name, strict, stmt_list) ->
			Digraph(name, strict, stmt_list @ [stmt])

(** Add a node without attribute to the graph.
	@param graph the graph to modify.
	@param id the identifier of the node.
	@return the modified graph.
*)
let add_node graph id =
	add_stmt graph (Node(id, []))

(** Add a node with a label to the graph.
	@param graph the graph to modify.
	@param id the identifier of the node.
	@param label the label of the node.
	@return the modified graph.
*)
let add_node_l graph id label =
	add_stmt graph (Node(id, [NLabel(label)]))

(** Add a node with attributes to the graph.
	@param graph the graph to modify.
	@param id the identifier of the node.
	@param attrs the attribute list.
	@return the modified graph.
*)
let add_node_a graph id attrs =
	add_stmt graph (Node(id, attrs))

(** Add an edge without attribute to the graph.
	@param graph the graph to modify.
	@param source the identifier of the source node.
	@param target the identifier of the target node.
	@return the modified graph.
*)
let add_edge graph source target =
	add_stmt graph (Edge([NodeID(source); NodeID(target)], []))

(** Add an edge with a label to the graph.
	@param graph the graph to modify.
	@param source the identifier of the source node.
	@param target the identifier of the target node.
	@param label the label of the edge.
	@return the modified graph.
*)
let add_edge_l graph source target label =
	add_stmt graph (Edge([NodeID(source); NodeID(target)], [ELabel(label)]))

(** Add an edge with attributes to the graph.
	@param graph the graph to modify.
	@param source the identifier of the source node.
	@param target the identifier of the target node.
	@param attrs the attribute list.
	@return the modified graph.
*)
let add_edge_a graph source target attrs =
	add_stmt graph (Edge([NodeID(source); NodeID(target)], attrs))

(** Functions to output a graph: *)

(** Transform a ToD graph into a .dot string
	@param graph the ToD graph to be transformed.
	@return the .dot string of the graph.
*)
let string_of_graph graph =
	let is_directed = ref false in
	
	(* Transform in a pretty string a string list *)
	let pretty_list start stop sep str_list =
		let rec aux str_list =
			match str_list with
				| [str] -> str
				| str :: t -> str ^ sep ^ (aux t)
				| _ -> ""
		in		
		match str_list with
			| [] -> ""
			| _ -> start ^ (aux str_list) ^ stop
	in
	
	(* Protect a string by adding "" around it *)
	let protect str = "\"" ^ str ^ "\"" in
	
	let rec string_of_stmt_list stmt_list indent =
		
		(* Transform a node attribute into a string *)
		let string_of_node_attr attr =
			match attr with
				| NColor(col) -> "color=" ^ protect col
				| NComment(com) -> "comment=" ^ protect com
				| NDistortion(dist) -> "distortion=" ^ string_of_float(dist)
				| NFillColor(col) -> "fillcolor=" ^ protect col
				| NFixedSize(fix) -> "fixedsize=" ^ string_of_bool(fix)
				| NFontColor(col) -> "fontcolor=" ^ protect col
				| NFontName(font) -> "fontname=" ^ protect font
				| NFontSize(size) -> "fontsize=" ^ string_of_int(size)
				| NGroup(grp) -> "group=" ^ protect grp
				| NHeight(height) -> "height=" ^ string_of_float(height)
				| NLabel(lbl) -> "label=" ^ protect lbl
				| NLayer(lay) -> "layer=" ^ protect lay
				| NOrientation(orient) -> "orientation=" ^ string_of_float(orient)
				| NPeripheries(per) -> "peripheries=" ^ string_of_int(per)
				| NRegular(reg) -> "regular=" ^ string_of_bool(reg)
				| NShape(shape) -> "shape=" ^ protect shape
				| NShapefile(file) -> "shapefile=" ^ protect file
				| NSides(sides) -> "sides=" ^ string_of_int(sides)
				| NSkew(skew) -> "skew=" ^ string_of_float(skew)
				| NStyle(style) -> "style=" ^ protect style
				| NURL(url) -> "url=" ^ protect url
				| NWidth(width) -> "width=" ^ string_of_float(width)
				| NZ(z) -> "z=" ^ string_of_float(z)
		in
	
		(* Transform a node attribute list into a string *)
		let string_of_node_attr_list attr_list =
			(pretty_list "[" "]" ", " (List.map (string_of_node_attr) attr_list))
		in
	
		(* Transform a node into a string *)
		let string_of_node (id, attr_list) =
			indent ^ id ^ " " ^ (string_of_node_attr_list attr_list) ^ ";\n"
		in
	
		(* Transform an edge attribute into a string *)
		let string_of_edge_attr attr =
			match attr with
				| EArrowHead(arrowhead) -> "arrowhead=" ^ protect arrowhead
				| EArrowSize(size) -> "arrowsize=" ^ string_of_float(size)
				| EArrowTail(tail) -> "arrowtail=" ^ protect tail
				| EColor(col) -> "color=" ^ protect col
				| EComment(com) -> "comment=" ^ protect com
				| EConstraint(const) -> "constraint=" ^ string_of_bool(const)
				| EDecorate(decorate) -> "decorate=" ^ string_of_bool(decorate)
				| EDir(dir) -> "dir=" ^ protect dir
				| EFontColor(col) -> "fontcolor=" ^ protect col
				| EFontName(name) -> "fontname=" ^ protect name
				| EFontSize(size) -> "fontsize=" ^ string_of_int(size)
				| EHeadLabel(headlabel) -> "headlabel=" ^ protect headlabel
				| EHeadPort(headport) -> "headport=" ^ protect headport
				| EHeadURL(headurl) -> "headurl=" ^ protect headurl
				| ELabel(label) -> "label=" ^ protect label
				| ELabelAngle(angle) -> "labelangle=" ^ string_of_float(angle)
				| ELabelDistance(dist) -> "labeldistance=" ^ string_of_float(dist)
				| ELabelFloat(fl) -> "labelfloat=" ^ string_of_bool(fl)
				| ELabelFontColor(col) -> "labelfontcolor=" ^ protect col
				| ELabelFontName(name) -> "labelfontname=" ^ protect name
				| ELabelFontSize(size) -> "labelfontsize=" ^ string_of_int(size)
				| ELayer(layer) -> "layer=" ^ protect layer
				| ELHead(lhead) -> "lhead=" ^ protect lhead
				| ELTail(ltail) -> "ltail=" ^ protect ltail
				| EMinLen(minlen) -> "minlen=" ^ string_of_int(minlen)
				| ESameHead(samehead) -> "samehead=" ^ protect samehead
				| ESameTail(sametail) -> "sametail=" ^ protect sametail
				| EStyle(style) -> "style=" ^ protect style
				| ETailLabel(taillabel) -> "taillabel=" ^ protect taillabel
				| ETailPort(tailport) -> "tailport=" ^ protect tailport
				| ETailURL(tailurl) -> "tailurl=" ^ protect tailurl
				| EWeight(weight) -> "weight=" ^ string_of_int(weight)
		in
	
		(* Transform an edge attribute list into a string *)
		let string_of_edge_attr_list attr_list =
			(pretty_list "[" "]" ", " (List.map (string_of_edge_attr) attr_list))
		in
	
		(* Transform a list of edges into a string *)
		let string_of_edge (id_list, attr_list) =
			let op = (if !is_directed then " -> " else " -- ") in
			let id_as_str = List.map ( fun id ->
				match id with
					| NodeID(nodeid) -> nodeid
					| Subgraph(name, stmt_list) ->
						name ^ "{\n" ^
						(string_of_stmt_list stmt_list (indent ^ "\t")) ^
						indent ^ "}"
			) id_list in
			indent ^
			(pretty_list "" "" op id_as_str) ^
			(string_of_edge_attr_list attr_list) ^
			";\n"
		in
	
		(* Transform a graph attribute into a string *)
		let string_of_graph_attr attr =
			(match attr with
				| GBgColor(bgcolor) -> "bgcolor=" ^ protect(bgcolor)
				| GCenter(center) -> "center=" ^ string_of_bool(center)
				| GClusterRank(clusterrank) -> "clusterrank=" ^ protect(clusterrank)
				| GColor(color) -> "color=" ^ color
				| GComment(comment) -> "comment=" ^ protect(comment)
				| GCompound(compound) -> "compound=" ^ string_of_bool(compound)
				| GConcentrate(conc) -> "concentrate=" ^ string_of_bool(conc)
				| GFillColor(col) -> "fillcolor=" ^ protect(col)
				| GFontColor(col) -> "fontcolor=" ^ protect(col)
				| GFontName(name) -> "fontname=" ^ protect(name)
				| GFontPath(fontpath) -> "fontpath=" ^ protect(fontpath)
				| GFontSize(size) -> "fontsize=" ^ string_of_int(size)
				| GLabel(label) -> "label=" ^ protect(label)
				| GLabelJust(labeljust) -> "labeljust=" ^ protect(labeljust)
				| GLabelLoc(labelloc) -> "labelloc=" ^ protect(labelloc)
				| GLayers(layers) -> "layers=" ^ protect(layers)
				| GMargin(margin) -> "margin=" ^ string_of_float(margin)
				| GMcLimit(mclimit) -> "mclimit=" ^ string_of_float(mclimit)
				| GNodeSep(nodesep) -> "nodesep=" ^ string_of_float(nodesep)
				| GNSLimit(nslimit) -> "nslimit=" ^ string_of_int(nslimit)
				| GNSLimit1(nslimit1) -> "nslimit1=" ^ string_of_int(nslimit1)
				| GOrdering(gordering) -> "gordering=" ^ protect(gordering)
				| GOrientation(orient) -> "orientation=" ^ protect(orient)
				| GPage(page) -> "page=" ^ protect(page)
				| GPageDir(pagedir) -> "pagedir=" ^ protect(pagedir)
				| GQuantum(quantum) -> "quantum=" ^ string_of_float(quantum)
				| GRank(rank) -> "rank=" ^ protect(rank)
				| GRankDir(rankdir) -> "rankdir=" ^ protect(rankdir)
				| GRankSep(ranksep) -> "ranksep=" ^ string_of_float(ranksep)
				| GRatio(ratio) -> "ratio=" ^ protect(ratio)
				| GReminCross(rc) -> "remincross=" ^ string_of_bool(rc)
				| GRotate(rotate) -> "rotate=" ^ string_of_int(rotate)
				| GSamplePoints(sp) -> "samplepoints=" ^ string_of_int(sp)
				| GSearchSize(size) -> "searchsize=" ^ string_of_int(size)
				| GSize(size) -> "size=" ^ string_of_float(size)
				| GStyle(style) -> "style=" ^ protect(style)
				| GURL(url) -> "url=" ^ protect(url)			
			) ^ ";\n"
		in
		
		(* Transform a graph attribute list into a string *)
		let string_of_graph_attr_list attr_list =
			(pretty_list "[" "]" ", "
				(List.map (string_of_graph_attr) attr_list)
			)
		in
	
		(* Transform a statements list into a string *)
		let rec aux_string_of_stmt_list stmt_list =
			match stmt_list with
				| Node(id, attrs) :: t ->
					(string_of_node (id, attrs)) ^ (aux_string_of_stmt_list t)
				| Edge(ids, attrs) :: t ->
					(string_of_edge (ids, attrs)) ^ (aux_string_of_stmt_list t)
				| Attribute(attr) :: t ->
					(string_of_graph_attr attr) ^ (aux_string_of_stmt_list t)
				| GraphAttr(attrs) :: t ->
					indent ^ "graph " ^ (string_of_graph_attr_list attrs) ^
					(aux_string_of_stmt_list t)
				| NodeAttr(attrs) :: t ->
					indent ^ "node " ^ (string_of_node_attr_list attrs) ^
					(aux_string_of_stmt_list t)
				| EdgeAttr(attrs) :: t ->
					indent ^ "edge " ^ (string_of_edge_attr_list attrs) ^
					(aux_string_of_stmt_list t)
				| [] -> ""
		in aux_string_of_stmt_list stmt_list
	in
	
	match graph with
		| Graph(name, strict, stmt_list) ->
			is_directed := false;
			((if strict then "strict " else "") ^
			"graph " ^ name ^ " {\n" ^
			(string_of_stmt_list stmt_list "\t") ^
			"}\n")
		| Digraph(name, strict, stmt_list) ->
			is_directed := true;
			((if strict then "strict " else "") ^
			"digraph " ^ name ^ " {\n" ^
			(string_of_stmt_list stmt_list "\t") ^
			"}\n")
		
(** Print a .dot graph on stdout.
	@param graph the ToD graph to be printed.
*)
let print graph = (Printf.printf "%s" (string_of_graph graph))

(** Write a .dot file.
	@param graph the ToD graph to be saved.
	@param filename the file name of the .dot file.
*)
let write graph filename =
	let file = open_out filename
	in let _ = Printf.fprintf file "%s" (string_of_graph graph)
	in close_out file
