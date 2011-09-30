:- module( predicates ).
:- lib( ic ).
:- use_module( numbers ).

:- export 'WordValue'/2.
:- export 'SNaN_single'/2.
:- export 'QNaN_single'/2.
:- export 'SNaN_double'/2.
:- export 'QNaN_double'/2.

% X[63..32] == X[31]^32
'WordValue'( X, 64 ) :-
	numbers:sizeof( X, 64 ),
	numbers:getbits( Bits, X, 64, 63, 32 ),
	numbers:getbit( Bit, X, 64, 31 ),
	numbers:pow( Bits2, Bit, 1, 32 ),
	Bits = Bits2.

% x111111110xxxxxxxxxxxxxxxxxxxxxx
% X[30..22]=510  /\  X[21..0]#0
'SNaN_single'( X, 32 ) :-
	numbers:sizeof( X, 32 ),
	numbers:nstring2nlist( "510", Degree, 9 ),
	numbers:getbits( Degree, X, 32, 30, 22 ),
	numbers:getbits( A, X, 32, 21, 0 ),
	numbers:nstring2nlist( "0", Nol, 22 ),
	numbers:notequal( A, Nol, 22 ). 

% x111111111xxxxxxxxxxxxxxxxxxxxxx
% X[30..22]=511
'QNaN_single'( X, 32 ) :-
	numbers:sizeof( X, 32 ),
	numbers:nstring2nlist( "511", Degree, 9 ),
	numbers:getbits( Degree, X, 32, 30, 22 ).

% X[62..51]=4094  /\  X[50..0]#0
'SNaN_double'( X, 64 ) :-
	numbers:sizeof( X, 64 ),
	numbers:nstring2nlist( "4094", Degree, 12 ),
	numbers:getbits( Degree, X, 64, 62, 51 ),
	numbers:getbits( A, X, 64, 50, 0 ),
	numbers:nstring2nlist( "0", Nol, 51 ),
	numbers:notequal( A, Nol, 51 ). 

% X[62..51]=4095
'QNaN_double'( X, 64 ) :-
	numbers:sizeof( X, 64 ),
	numbers:nstring2nlist( "4095", Degree, 12 ),
	numbers:getbits( Degree, X, 64, 62, 51 ).