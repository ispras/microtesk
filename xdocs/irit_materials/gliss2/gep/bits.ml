(*
 * GLISS2 -- bit operation management
 * Copyright (c) 2011, IRIT - UPS <casse@irit.fr>
 *
 * This file is part of GLISS2.
 *
 * GLISS2 is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * GLISS2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GLISS2; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *)

module type Word = sig
	type t
	val size: int
	val join: t -> t -> t
	val shl: t -> int -> t
	val shr: t -> int -> t
	val mask: t -> int -> t
end

module Word32: Word = struct
	type t = Int32.t
	let size = 32
	let join v1 v2 = Int32.logor v1 v2
	let shl v s = Int32.shift_left v s
	let shr v s = Int32.shift_right v s
	let mask v s = Int32.logand v (Int32.shift_right Int32.minus_one (32 - s))
end

module Bits(W: Word) = struct

	(** Elements of a bit string. *)
	type bit =
		| REG of int * int *int		(** register access *)
		| IREG of int * int *int	(** register access *)
		| CST of W.t * int			(** constant *)
		| BLK of Irg.expr			(** undefined bit bound expression *)

	(** Type of bit operation, list of bit string that are composed
		in reverse order: [b1, b2, ..., bn] -> bn :: ... :: b2 :: b1. *)
	type bitstring = bit list

	(** Attempt to simplify the given bit expression.
		@param bits		Bits to simplify.
		@return			Simplified bit string. *)
	let rec join bits =
		match bits with
		| [] | [_] -> bits 
		| (CST (v1, s1)) :: t when s1 >= W.size -> (List.hd bits) :: (join t)
		| (CST (v1, s1)) :: (CST (v2, s2)) :: t ->
			if s1 + s2 <= W.size
			then join ((CST (W.join (W.shl v2 s1) v1, s1 + s2)) :: t)
			else
				(CST (W.join (W.shl v2 s1) v1, W.size))
				:: (join ((CST (W.shr v2 (W.size - s1), s1 + s2 - W.size)) :: t))
		| (REG (i1, u1, l1)) :: (REG (i2, u2, l2)) :: t when i1 == i2 && l1 = u2 + 1 ->
			join ((REG(i1, u1, l2)) :: t)
		| (IREG (i1, u1, l1)) :: (IREG (i2, u2, l2)) :: t when i1 == i2 && l1 = u2 + 1 ->
			join ((IREG(i1, u1, l2)) :: t)
		| d :: t -> d :: (join t)
	
	(** Concatenate two bit string.
		@param b1	First bit string.
		@param b2	Second bit string.
		@return		Concatenated bit string. *)
	let concat b1 b2 =
		join (b2 @ b1)
	
	(** Apply a bit selection of the given bit string.
		@param b	Bit string to apply selection to.
		@param u	Upper bit bound.
		@param l	Lower bit bound.
		@return		Bit string after selection. *)
	let select b u l =
		let rec process b u l =
			match b with
			| (CST (v, s)) :: t ->
				if u < s	(* faux et archi-faux *)
				then (CST (W.shr v l, s - l)) :: (process t (u - s) 0)
				else (CST (W.shr (W.mask v u) l , u -l + 1)) :: t
			| _ -> b in
		join (process b u l)
end

module Bits32 = Bits(Word32)



