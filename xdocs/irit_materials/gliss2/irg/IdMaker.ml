(*
 * $Id: IdMaker.ml,v 1.3 2009/03/07 13:06:17 casse Exp $
 * Copyright (c) 2008-09, IRIT - UPS <casse@irit.fr>
 *
 * This file is part of OGliss.
 *
 * OGliss is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * OGliss is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGliss; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *)

module HashChar =
struct
	type t = char
	let equal (s1 : t) (s2 : t) = s1 = s2
	let hash (s : t) = Hashtbl.hash s
end
module CharHashtbl = Hashtbl.Make(HashChar)

let cmap : string CharHashtbl.t = CharHashtbl.create 211
let specs = [
	(' ', "_");
	('\t', "_");
	('#', "_I");
	(',', "_");
	('{', "_LB_");
	('}', "_RB_");
	('[', "_LT_");
	(']', "_RT_");
	('(', "_LP_");
	(')', "_RP_");
	('+', "_P_");
	('-', "_M_");
	('_', "_");
	('%', "");
	('.', "_")
]
let _ =
	List.iter (fun (c, s) -> CharHashtbl.add cmap c s) specs;
	for i = Char.code 'A' to Char.code 'Z' do
		let c = (Char.chr i) in
		let l = Char.lowercase c in
		CharHashtbl.add cmap c (String.make 1 c);
		CharHashtbl.add cmap l (String.make 1 c)
	done;
	for i = Char.code '0' to Char.code '9' do
		let c = (Char.chr i) in
		CharHashtbl.add cmap c (String.make 1 c)
	done

module Make(H: Hashtbl.HashedType) = struct
	module InstMap = Hashtbl.Make(H)
	let imap: string InstMap.t = InstMap.create 211

	module HashedString = struct
		type t = string
		let equal (s1 : t) (s2 : t) = s1 = s2
		let hash (s : t) = Hashtbl.hash s
	end
	module NameMap = Hashtbl.Make(HashedString)
	let nmap: H.t NameMap.t = NameMap.create 211

	type 'a t = 'a NameMap.t

	let make i id =
		let buf = Buffer.create 32 in

		let make_char c =
			try
				Buffer.add_string buf (CharHashtbl.find cmap c)
			with Not_found ->
				Buffer.add_string buf (Printf.sprintf "_%02x" (Char.code c)) in
		
		let rec find_name name i =
			let nname = Printf.sprintf "%s_%d" name i in
			if not (NameMap.mem nmap nname)
			then nname
			else find_name name (i + 1) in
		
		try InstMap.find imap i
		with Not_found ->
			begin
				String.iter make_char id;
				let name = Buffer.contents buf in
				let name =
					if not (NameMap.mem nmap name)
					then name
					else find_name name 0 in
				InstMap.add imap i name;
				NameMap.add nmap name i;
				name
			end
end



	
