:- module( scalar ).
:- lib( ic ).
:- lib( tentative ).
:- lib( ic_kernel ).


:- export exp2/2.
:- export rnd_result/1.
:- export getbitFromNumber/3.
:- export getbitsFromNumber/4.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Result := 2^X, X >= 0
exp2( Result, X ) :-
	( X #= 0, Result #= 1
	; ( X #= 1, Result #= 2
	  ; X #> 1,
	           ( X #=< 32, exp2XlessThan33( Result, X )
		   ; X #> 32, X1 #= X - 32, exp2XlessThan33( R, X1), Result #= 4294967296 * R
		   )
	  )
	).

exp2XlessThan33( 1, 0 ).
exp2XlessThan33( Result, X ) :-
	X #> 0, X #=< 32,
	integers([Result, X]),
	( X #= 2 * N,
		Result $< sqr(exp( 1e-10 + N * ln(2.0) ) + 1e-6) ,
		Result $> sqr(exp( -1e-10 + N * ln(2.0) ) - 1e-6)
	; X #= 2 * N + 1,
		X1 #= X - 1,
		exp2XlessThan33( R, X1 ),
		Result #= 2 * R	
	).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
rnd_result( X ) :-
	get_domain(X, L),
	( compound(L), 
	random_element(L, Fs1),
	X #>= Fs1,
	get_min(X, X)
	; integer(L) )
.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Result := X div A,  A == 2^N
div2exp2( Result, X, A ) :-
	integers([Result, X, A]), 
	Result $< X/A + 1, 
	Result $> X/A - 1, 
	D #= X - Result * A, 0 #=< D, D #< A .

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

getbitFromNumber( Result, Number, Index ) :-
	Result :: [0..1],
	Number #>= 0,
	Index #>= 0, 
	% Index #< SizeOfNumber,
	integers([Number, Index]),
	exp2( A, Index ),

	div2exp2( C, Number, A ),
	( C #= 2*_, Result #= 0 ; C#= 2*_ + 1, Result #= 1).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% A := 2^StartIndex
% C := Number/A  (Number >> StartIndex)
% D := 2^(EndIndex - StartIndex + 1)
% Result >= 0, Result < D, integers([Result]), C = _ * D + Result
getbitsFromNumber( Result, Number, EndIndex, StartIndex ) :-
	Number #>= 0,
	integers([Result, Number, EndIndex, StartIndex]),
	EndIndex #>= StartIndex,

	exp2( A, StartIndex ),
	div2exp2( C, Number, A ),

% D := 2^(EndIndex - StartIndex + 1)
	Pow #= EndIndex - StartIndex + 1,
	exp2( D, Pow ),

	Result #>= 0, Result #< D,
	integers([Result]), 
	C #= _ * D + Result .
