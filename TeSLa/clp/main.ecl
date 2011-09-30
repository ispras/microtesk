:- module( main ).
:- lib( ic ).
:- use_module( numbers ).
:- use_module( predicates ).

:- export go/3.
go( _, _BASE, _offset ) :-
numbers:nstring2nlist( _BASE, _1BASE, 32),
numbers:nstring2nlist( _offset, _1offset, 32),
numbers:sizeof( _1vAddr, 64),
numbers:signExtend( _0, _1offset, 32, 64 ),
numbers:signExtend( _1, _1BASE, 32, 64 ),
numbers:sum( _2, _0, _1, 64 ),
_1vAddr = _2,
'0'( _1BASE, _1offset, _1vAddr ) ,
numbers:random_result( _1BASE ),
numbers:random_result( _1offset ),
numbers:nlist2nstring( _1BASE, _BASE, 32 ),
numbers:nlist2nstring( _1offset, _offset, 32 ),
true.

'0'( _1BASE, _1offset, _1vAddr )  :-
numbers:getbits( _3, _1vAddr, 64, 2, 0 ),
numbers:equal( _3, [ 0 ], 3 ).
