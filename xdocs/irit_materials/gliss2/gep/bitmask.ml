type bitmask =
	(* value of the mask as a '0' '1' char only string *)
	| BITMASK of string


let void_mask = BITMASK("")


let get_intern_val m =
	match m with
	| BITMASK(s) -> s




(**************************************************************)
(*                some iterators on strings                   *)
(**************************************************************)


let string_map f s =
	let l = String.length s in
	let res = String.create l in
	let rec aux n =
		if n < l then
			(res.[n] <- f s.[n];
			aux (n + 1))
		else
			()
	in
	aux 0;
	res


let string_map2 f s1 s2 =
	let l = String.length s1 in
	let l2 = String.length s2 in
	let res = String.create l in
	let rec aux n =
		if n < l then
			(res.[n] <- (f s1.[n] s2.[n]);
			aux (n + 1))
		else
			()
	in
	if l <> l2 then
		failwith "shouldn't happen (bitmask.ml::string_map2)"
	else
		aux 0;
	res


let string_iter f s =
	let l = String.length s in
	let rec aux n =
		if n < l then
			(f s.[n];
			aux (n + 1))
		else
			()
	in
	aux 0


let string_iter2 f s1 s2 =
	let l = String.length s1 in
	let l2 = String.length s2 in
	let rec aux n =
		if n < l then
			(f s1.[n] s2.[n];
			aux (n + 1))
		else
			()
	in
	if l <> l2 then
		failwith "shouldn't happen (bitmask.ml::string_iter2)"
	else
		aux 0


let string_fold_left f a s =
	let l = String.length s in
	let rec aux accu n =
		if n < l then
			aux (f accu s.[n]) (n + 1)
		else
			accu
	in
	aux a 0


let string_fold_right f a s =
	let l = String.length s in
	let rec aux accu n =
		if n < l then
			f s.[n] (aux accu (n + 1))
		else
			accu
	in
	aux a 0


let string_fold_left2 f a s1 s2 =
	let l = String.length s1 in
	let l2 = String.length s2 in
	let rec aux accu n =
		if n < l then
			aux (f accu s1.[n] s2.[n]) (n + 1)
		else
			accu
	in
	if l <> l2 then
		failwith "shouldn't happen (bitmask.ml::string_fold_left2)"
	else
		aux a 0


let string_fold_right2 f a s1 s2 =
	let l = String.length s1 in
	let l2 = String.length s2 in
	let rec aux accu n =
		if n < l then
			f s1.[n] s2.[n] (aux accu (n + 1))
		else
			accu
	in
	if l <> l2 then
		failwith "shouldn't happen (bitmask.ml::string_fold_right2)"
	else
		aux a 0

let string_rev s =
	let l = String.length s in
	let res = String.create l in
	let aux i c =
		res.[i] <- c;
		i - 1
	in
	ignore (string_fold_left aux (l - 1) s);
	res



(***************************************************)
(*                mask manipulation                *)
(***************************************************)

	
(** returns the amount of set bits in a mask *)
let bit_count m =
	let v = get_intern_val m in
	let aux accu c =
		accu + (if c == '1' then 1 else 0)
	in
	string_fold_left aux 0 v


(** returns the length in bit of a mask *)
let length m =
	String.length (get_intern_val m)


(** reverse all bits in a mask *)
let reverse m =
	let v = get_intern_val m in
	BITMASK(string_rev v)


(** returns a submask of m from position pos and with length bits *)
let sub m pos length =
	let v = get_intern_val m in
	try
		(*Printf.printf "sub, pos=%d, length=%d\n" pos length;*)
		BITMASK(String.sub v pos length)
	with
	| Invalid_argument m -> failwith ("shouldn't happen (bitmask.ml::sub): " ^ m)


(**
 * extends the shorter mask with 0s on the side indicated by
 * left_or_right, left (true) or right (false),
 * returns the 2 resulting mask (with only one extended) in a couple
 *)
let set_same_length m1 m2 left_or_right =
	let v1 = get_intern_val m1 in
	let v2 = get_intern_val m2 in
	let l1 = String.length v1 in
	let l2 = String.length v2 in
	let l_ext = abs (l1 - l2) in
	let v_ext = String.make l_ext '0' in
	if l1 == l2 then
		(m1, m2)
	else if l1 < l2 then
		(BITMASK(if left_or_right then v_ext ^ v1 else v1 ^ v_ext), m2)
	else
		(* l1 > l2 *)
		(m1, BITMASK(if left_or_right then v_ext ^ v2 else v2 ^ v_ext))


(**
 * logical AND between 2 masks,
 * length will be the min between m1 and m2 lengths
 *)
let logand m1 m2 =
	let v1 = get_intern_val m1 in
	let v2 = get_intern_val m2 in
	let bit_and c1 c2 =
		if c1 == 'X' || c2 == 'X' then
			failwith "shouldn't happen (bitmask.ml::logor::bit_or)"
		else
			(if c1 == '1' then c2 else '0')
	in
	if (String.length v1) != (String.length v2) then
		failwith "Bitmask.logand: both mask should have same length, use set_same_length to extend the shorter"
	else
		BITMASK(string_map2 bit_and v1 v2)


(** logical OR between 2 masks,
    length will be the max between m1 and m2 lengths,
    as if the smaller one is extended with 0s
*)
let logor m1 m2 =
	let v1 = get_intern_val m1 in
	let v2 = get_intern_val m2 in
	let bit_or c1 c2 =
		if c1 == 'X' || c2 == 'X' then
			failwith "shouldn't happen (bitmask.ml::logor::bit_or)"
		else
			(if c1 == '1' then
				'1'
			else
				c2
			)
	in
	if (String.length v1) != (String.length v2) then
		failwith "Bitmask.logor: both mask should have same length, use set_same_length to extend the shorter"
	else
		BITMASK(string_map2 bit_or v1 v2)


(** left shift, just add trailing 0s *)
let shift_left m sh =
	let s = get_intern_val m in
	if sh < 0 then
		failwith "shouldn't happen (bitmask.ml::shift_left)"
	else
		(*Printf.printf "<<<<shift_left, m=%s(%d), sh=%d\n" s (String.length s) sh;*)
		BITMASK(s ^ (String.make sh '0'))


(** right shift logical, suppress the last sh bits on the right, no sign extension *)
let shift_right_logical m sh =
	let s = get_intern_val m in
	let l = String.length s in
	if sh < 0 then
		failwith "shouldn't happen (bitmask.ml::shift_right_logical)"
	else
		(if sh >= l then
			void_mask
		else
			BITMASK(String.sub s 0 (l - sh)))


(** arithmetic right shift, sign is extended,
 suppress the last sh bits on the right,
 extend sign over sh bits on the left *)
let shift_right m sh =
	let s = get_intern_val m in
	let l = String.length s in
	if sh < 0 then
		failwith "shouldn't happen (bitmask.ml::shift_right)"
	else
		(if l == 0 or sh >= l then
			void_mask
		else
			(let s_ext = String.make sh s.[0] in
			BITMASK(s_ext ^ (String.sub s 0 (l - sh)))))


(** returns a new mask from m, the bits returned are those from m indicated by the bits set in mask,
    bits are concatenated and length will be the min between v length and mask number of 1s
*)
let masked_value m mask =
	let v = get_intern_val m in
	let vmask = get_intern_val mask in
	let l = min (String.length v) (String.length vmask) in
	let lr = min l (bit_count mask) in
	let res = String.create lr in
	let set_res n n_res =
		if v.[n] <> '0' && v.[n] <> '1' then
			failwith "shouldn't happen (bitmask.ml::masked_value)"
		else
			res.[n_res] <- v.[n]
	in		
	let rec aux step step_res =
		if step >= l then
			()
		else
			(if vmask.[step] == '1' then
				(set_res step step_res;
				aux (step + 1) (step_res + 1))
			else
				aux (step + 1) step_res
			)
	in
	aux 0 0;
	(*!!DEBUG!!*)
	(*Printf.printf "masked_value, v=%s, vmask=%s, l=%d, lr=%d\n" v vmask l lr;
	Printf.printf "masked_value, m= [%s](%d), mask= [%s](%d), res = [%s](%d)\n"
	v (String.length v) vmask (String.length vmask) res (String.length res);*)
	BITMASK(res)


(** returns m (representing a mask) with the mask "removed", if a bit is set in the mask it will be 0 in the result,
    otherwise it will be the corresponding bit of m, the result will have same length as m
*)
let unmask m mask =
	let v = get_intern_val m in
	let vm = get_intern_val mask in
	let lv = String.length v in
	let l = min lv (String.length vm) in
	let res = String.create lv in
	let v_suffix = String.sub v l (lv - l) in
	let set_res n c =
		if c <> '0' && c <> '1' then
			failwith "shouldn't happen (bitmask.ml::unmask)"
		else
			res.[n] <- c
	in
	let rec aux step =
		if step >= l then
			()
		else
			(if vm.[step] == '1' then
				set_res step '0'
			else
				set_res step v.[step];
			aux (step + 1)
			)
	in
	aux 0;
	(*!!DEBUG!!*)
	(*Printf.printf "unmask, m= [%s](%d), mask= [%s](%d), res= [%s](%d)\n" v lv vm (String.length vm) (res^v_suffix) (String.length (res^v_suffix));*)
	BITMASK(res ^ v_suffix)


(** simple equality between 2 masks
*)
let is_equals m1 m2 =
	let s1 = get_intern_val m1 in
	let s2 = get_intern_val m2 in
	((String.compare s1 s2) == 0)


exception Not_null_mask

let is_null m =
	let s = get_intern_val m in
	try
		string_iter (fun x -> if x <> '0' then raise Not_null_mask) s;
		true
	with
	| Not_null_mask -> false


let compare m1 m2 =
	let s1 = get_intern_val m1 in
	let s2 = get_intern_val m2 in
	String.compare s1 s2


let to_string m =
	get_intern_val m


(*takes a hex string s and returns binary equivalent*)
let string_hex_to_bin s =
	let convert c =
		let cc = Char.uppercase c in
		match cc with
		| '0' -> "0000"
		| '1' -> "0001"
		| '2' -> "0010"
		| '3' -> "0011"
		| '4' -> "0100"
		| '5' -> "0101"
		| '6' -> "0110"
		| '7' -> "0111"
		| '8' -> "1000"
		| '9' -> "1001"
		| 'A' -> "1010"
		| 'B' -> "1011"
		| 'C' -> "1100"
		| 'D' -> "1101"
		| 'E' -> "1110"
		| 'F' -> "1111"
		| _ -> failwith "shouldn't happen (bitmask.ml::string_hex_to_bin)"
	in
	let aux accu c =
		accu ^ (convert c)
	in
	string_fold_left aux "" s
	


(** convert the 1st 64 bits of a mask to an int64
the mask is supposed to be less or equal than 64 bits *)
let to_int64 m =
	let s = get_intern_val m in
	let size = String.length s in
	if size > 64 then
		failwith "mask too long, 64 bits max allowed (bitmask.ml::to_int64)"
	else
		try
			Int64.of_string ("0b" ^ s)
		with
		| Failure "int_of_string" -> failwith "cannot convert mask to int64 (bitmask.ml::to_int64)"


(** convert the 1st 32 bits of a mask to an int32
the mask is supposed to be less or equal than 32 bits *)
let to_int32 m =
	let s = get_intern_val m in
	let size = String.length s in
	if size > 32 then
		failwith "mask too long, 32 bits max allowed (bitmask.ml::to_int32)"
	else
		try
			Int32.of_string ("0b" ^ s)
		with
		| Failure "int_of_string" -> failwith "cannot convert mask to int32 (bitmask.ml::to_int32)"


let to_int m = Int32.to_int (to_int32 m)


(* don't forget caml int are 31 bit long *)
let of_int i =
	let si = Printf.sprintf "%08X" i in
	(*!!DEBUG!!*)
	(*let res =*)
	BITMASK(string_hex_to_bin si)
	(*in Printf.printf "Bitmask.of_int, i=%d, res=[%s]\n" i (get_intern_val res);
	res*)


let of_int32 i =
	let si = Printf.sprintf "%08lX" i in
	BITMASK(string_hex_to_bin si)


(* should check if only 1, 0 and X *)
let of_string s =
	BITMASK(s)
	


let to_int32_list m =
	let s = get_intern_val m in
	let l = String.length s in
	let rec aux accu step =
		match l - step with
		| n when n == 0 -> accu (* end of string *)
		| n when n >= 32 -> aux (accu @ [(Int32.of_string ("0b" ^ (String.sub s step 32)))]) (step + 32)
		| n when n > 0 && n < 32 -> accu @ [Int32.shift_left (Int32.of_string ("0b" ^ (String.sub s step n))) (32 - n)] (* less than 32 bits from end of string *)
		| _ -> failwith "shouldn't happen (bitmask.ml::to_int32_list)"
	in
	aux [] 0


(** produces the suffix needed in C for the given mask translated in C constant,
currently only suffix for 64 bit const is returned *)
let c_const_suffix m =
	let s = get_intern_val m in
	let l = String.length s in
	if l > 32 && l <= 64 then "LL" else ""


(** Build a mask of n bits (initialized to one).
	@param n	Number of bits.
	@return		mask. *)
let mask_fill n =
	if n <= 0 then
		failwith "shouldn't happen (bitmask.ml::mask_fill)";
	BITMASK(String.make n '1')


(** Build a mask with ones from bit m to bit n (1st bit is bit 0).
	@param n	Upper mask bound.
	@param m	Lower mask bound.
	@return		Built mask, length will be n + 1 (no leading 0s in front of bit n). *)
let mask_range n m =
	if n < m then
		failwith "shouldn't happen (bitmask.ml::mask_range)";
	BITMASK((String.make (n - m + 1) '1') ^ (String.make m '0'))


(*******************************************************)
(*                  mask extraction                    *)
(*******************************************************)


(* return the string of a given Irg.expr which is supposed to be an image attribute *)
let rec get_str e =
	match e with
	| Irg.FORMAT(str, _) -> str
	| Irg.CONST(t_e, c) ->
		if t_e=Irg.STRING then
			match c with
			| Irg.STRING_CONST(str, false, _) -> str
			| _ -> ""
		else
			""
	| Irg.ELINE(_, _, e) -> get_str e
	| _ -> ""


exception Bad_bit_image_order of string
(**
returns the bit_image_order defined in nmp sources,
used to know if images descriptions are written with bytes reversed compared to the memory disposition,
this happens with the tricore processor for example.
@return		0 if bit_image_order is 0 or undefined
		1 if bit_image_order is defined and not 0
@raise	Bad_bit_image_order	when incorrectly defined
 *)
let get_bit_image_order _ =
	match Irg.get_symbol "bit_image_order" with
	| Irg.UNDEF -> false
	| Irg.LET(_, c) ->
		(match c with
		| Irg.CARD_CONST(i32) -> ((Int32.compare i32 Int32.zero) != 0)
		| _ -> raise (Bad_bit_image_order "bit_image_order can only be an boolean int constant.")
		)
	| _ -> raise (Bad_bit_image_order "bit_image_order must be defined as a let, if defined.")


(* return the length (in bits) of an argument whose param code (%8b e.g.) is given as a string *)
let get_length_from_format f =
	let l = String.length f in
	let new_f =
		if l<=2 then
		(* shouldn't happen, we should have only formats like %[0-9]+b, not %d or %f or %s *)
			failwith ("we shouldn't have something like [[" ^ f ^ "]] (bitmap.ml::get_length_from_format)")
		else
			String.sub f 1 (l-2)
	in
	Scanf.sscanf new_f "%d" (fun x->x)


(* remove any space (space or tab char) in a string, return a string as result *)
let remove_space s =
	Str.global_replace (Str.regexp "[ \t]+") "" s


(** returns the mask of an op from its spec, the result will be a string
with only '0' or '1' chars representing the bits of the mask *)
let get_mask sp =
	let transform_str s =
		let n = String.length s in
		let rec aux str pos accu =
			if pos >= n then
				accu
			else
				begin
					(* a 'X' or 'x' in an image means a useless bit => not in the mask *)
					if s.[pos]='x' || s.[pos]='X' then
						accu.[pos] <- '0'
					else
						();
					aux s (pos+1) accu
				end
		in
		aux s 0 (String.make n '1')
	in
	let rec get_mask_from_regexp_list l =
		match l with
		[] -> ""
		| h::t ->
			(match h with
			| Str.Text(txt) ->
				(* here we assume that an image contains only %.. , 01, X or x *)
				(transform_str txt) ^ (get_mask_from_regexp_list t)
			| Str.Delim(d) ->
				(String.make (get_length_from_format d) '0') ^ (get_mask_from_regexp_list t)
			)
	in
	let get_expr_from_iter_value v  =
		match v with
		| Iter.EXPR(e) ->
			e
		| _ ->
			failwith "shouldn't happen (bitmap.ml::get_string_mask_from_op::get_expr_from_iter_value)"
	in
	(* work only with strings with length multiple of 8, instructions have always length of 8*n bits so far *)
	let revert_bytes s =
		let rec aux length str =
			if length < 8 then
				""
			else
				(aux (length - 8) (String.sub str 8 (length - 8))) ^ (String.sub str 0 8)
		in
		if get_bit_image_order () then
			aux (String.length s) s
		else
			s
	in
	(* !!DEBUG!! *)
	(*let res =*)
	BITMASK(revert_bytes (get_mask_from_regexp_list (Str.full_split (Str.regexp "%[0-9]*[bdfxs]") (remove_space (get_str (get_expr_from_iter_value (Iter.get_attr sp "image")))))))
	(*in
	let s = get_intern_val res in
	Printf.printf "get_mask, [%s] (%d)\n" s (String.length s);
	res*)




(* returns the value of an instruction code considering only the bit set in the mask,
the result is a '0' or '1' string with the bits not set in the mask being marked with an 'X' *)
let get_value_mask sp =
	let rec get_mask_from_regexp_list l =
		match l with
		| [] -> ""
		| h::t ->
			(match h with
			| Str.Text(txt) ->
				(* here we assume that an image contains only %.., 01, x or X *)
				txt ^ (get_mask_from_regexp_list t)
			| Str.Delim(d) ->
				(* put an X for unused bits *)
				(String.make (get_length_from_format d) 'X') ^ (get_mask_from_regexp_list t)
			)
	in
	let get_expr_from_iter_value v =
		match v with
		| Iter.EXPR(e) ->
			e
		| _ ->
			failwith "shouldn't happen (fetch.ml::get_string_value_on_mask_from_op::get_expr_from_iter_value)"
	in
	(* work only with strings with length multiple of 8, instructions have always length of 8*n bits so far *)
	let revert_bytes s =
		let rec aux length str =
			if length < 8 then
				""
			else
				(aux (length - 8) (String.sub str 8 (length - 8))) ^ (String.sub str 0 8)
		in
		if get_bit_image_order () then
			aux (String.length s) s
		else
			s
	in
	(*!DEBUG!!*)
	(*let res =*)
	BITMASK(revert_bytes (get_mask_from_regexp_list (Str.full_split (Str.regexp "%[0-9]*[bdfxs]") (remove_space (get_str (get_expr_from_iter_value (Iter.get_attr sp "image")))))))
	(*in
	let s = get_intern_val res in
	Printf.printf "get_value_mask, [%s] (%d)\n" s (String.length s);
	res*)


(* returns the value of an instruction code considering only the bit set in the mask,
the result is a '0' or '1' string with the bits not set in the mask being discarded,
so the result will have as many bits as the number of set bits in the mask *)
let get_value sp =
	let vm = get_intern_val (get_value_mask sp) in
	(*!DEBUG!!*)
	(*let res =*)
	BITMASK(Str.global_replace (Str.regexp "X+") "" vm)
	(*in
	let s = get_intern_val res in
	Printf.printf "get_value, [%s] (%d)\n" s (String.length s);
	res*)

(** Print the mask.
	@param m	Mask to print. *)
let print m =
	match m with
	| BITMASK m -> print_string m


(** Convert bitmask to string.
	@param m	Mask to convert. *)
let to_string m =
	match m with
	| BITMASK m -> m
