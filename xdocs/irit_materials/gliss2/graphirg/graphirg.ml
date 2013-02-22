(**

*)

(** index for node name *)
let cpt = ref 0


(** 
	Provide the list of all sons's name for a parent node.
	It is a copy of Optirg.next built to avoid interdependency 
	between the optirg module and the graphirg module.
	@param s
		the name of the parent node
	@return 
		the string list that contains the names
*)
let next (s:string) : (string list) = match (Irg.get_symbol s) with
	|Irg.AND_MODE(_,pl,_,_) | Irg.AND_OP(_,pl,_) 
		-> List.fold_right (fun (_,t) r ->match t with Irg.TYPE_ID(s)->s::r | _ -> r) pl []
	|Irg.OR_MODE(_,sl) | Irg.OR_OP(_,sl) 
		-> sl
	| _ 
		-> []

(** 
	Provide attributes for a edge
	@return 
		the list of attributes
 *)
let edge_attr = [Tod.EColor("black");Tod.EArrowSize(2.)]

(** 
	Provide attributes for a node
	@param s
		the node
	@return 
		the list of attributes
*)
let node_attr_from_spec (s:Irg.spec) :(Tod.node_attribute list) = 
	match s with
	| Irg.AND_MODE(_,_,_,_) -> [Tod.NColor("green");Tod.NShape("box")]
	| Irg.OR_MODE(_,_) -> [Tod.NColor("red");Tod.NShape("box")]
	| Irg.AND_OP(_,_,_) -> [Tod.NColor("green");Tod.NShape("ellipse")]
	| Irg.OR_OP (_,_)-> [Tod.NColor("red");Tod.NShape("ellipse")]
	| _ -> []

(** 
	@param
	@param
	@return 
*)

(** 
	Create a graph node based on the spec node
	@param node_id
		the name of the node
	@param s
		the spec node
	@return 
		the graph node
*)
let node_of_spec (node_id:string) (s:Irg.spec) :(Tod.stmt) = 
	Tod.Node(node_id, node_attr_from_spec s)
(** 
	Create a graph egde based on the list of node's names
	@param nodeid_list
		name list of nodes 
	@return 
		the edge
*)
let edge (nodeid_list:string list) :Tod.stmt = 
	Tod.Edge(List.map (fun e -> Tod.NodeID(e)) nodeid_list,edge_attr)

(** 
	Create the tree that shows the link between one node and its descents.
	There is only one edge per node.
	@param father_name
		the name of the node in the hashtable
	@param father_nodeid
		the name of the node in the graph
	@return 
		the list of Tod.stmt which describe the tree
*)

let rec mk_tree_stmt_list (father_name:string) (father_nodeid:string) :(Tod.stmt list)= 
	let sons = next father_name in
	List.fold_right 
		(fun son_name sons_stmt -> 
			let son_id = son_name^"_"^string_of_int(cpt:=!cpt+1;!cpt) in
			(
				(node_of_spec son_id (Irg.get_symbol son_name)))::
				(edge [father_nodeid ; son_id])::
				(mk_tree_stmt_list son_name son_id)@
				sons_stmt
			) 
		sons []

(** 
	Create the canvas that shows the link between all nodes.
	@param father_name
		the name of the node in the hashtable
	@param father_nodeid
		the name of the node in the graph
	@return 
		the list of Tod.stmt which describe the tree
*)
let rec mk_stmt_list (father_name:string) (father_nodeid:string):(Tod.stmt list) = 
	let sons = next father_name in
	List.fold_right 
		(fun son_name sons_stmt -> 
			let son_id = son_name in
			(
				(node_of_spec son_id (Irg.get_symbol son_name)))::
				(edge [father_nodeid ; son_id])::
				(mk_stmt_list son_name son_id)@
				sons_stmt
			) 
		sons []

(** 
	Create the graph as a tree.
	@param root_name 
		the root of the tree
	@return 
		the graph
*)

let mk_tree_graph (root_name:string) :Tod.dot = 
	let root_id = root_name^"_"^string_of_int(cpt:=0;!cpt) in
	let root_node = node_of_spec root_id (Irg.get_symbol root_name) in 
	let stmt_list = root_node::(mk_tree_stmt_list root_name root_id) in 
	Tod.Digraph("IRG",false,stmt_list)

(** 
	Create the graph as a canvas.
	@param root_name 
		the root of the canvas
	@return 
		the graph
*)
let mk_graph (root_name:string) :Tod.dot = 
	let root_id = root_name in
	let root_node = node_of_spec root_id (Irg.get_symbol root_name) in 
	let stmt_list = root_node::(mk_stmt_list root_name root_id) in 
	Tod.Digraph("IRG",false,stmt_list)

(** 
	
	@see Tod.write
*)
let write = Tod.write


