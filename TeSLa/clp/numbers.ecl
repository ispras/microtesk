:- module( numbers ).
:- lib( ic ).
:- use_module( scalar ).

:- export sizeof/2.

:- export greaterUnsigned/3.
:- export lessUnsigned/3.
:- export greaterORequalUnsigned/3.
:- export lessORequalUnsigned/3.
:- export greaterSigned/3.
:- export lessSigned/3.
:- export greaterORequalSigned/3.
:- export lessORequalSigned/3.
:- export notequal/3.
:- export equal/3.

:- export getbit/4.
:- export getbits/5.
:- export concat/5.
:- export pow/4.

:- export sum/4.
:- export sub/4.
:- export mulUnsigned/5.
%:- export mulSigned/5.

:- export signExtend/4.

:- export nstring2nlist/3.
:- export nlist2nstring/3.

:- export random_result/1.

chunksize( 51 ).  %parameter MUST BE EQUAL WITH THE SAME IN Solver class !!!

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

is_chunk( X ) :- X #>= 0, chunksize( C ), exp2( D2, C ), X #< D2 .

sizeof( [], 0 ).
sizeof( X, Size ) :-
	nonvar( Size ), Size > 0,
	chunksize( C ),
	H is Size mod C,
	( H = 0 -> Len is Size div C ; Len is Size div C + 1 ),
	length( X, Len ),
	checklist( is_chunk, X ),
	( H = 0 -> true ; X = [Xt|_], exp2(D, H), Xt #< D ) .
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

greaterUnsigned( X, Y, Size ) :-
	sizeof( X, Size ), sizeof( Y, Size ),
	
	X = [ Xh | Xt ], Y = [ Yh | Yt ],
	
	chunksize( C ),
	M is Size mod C,
	( M = 0 -> S is Size - C ; S is Size - M ),
	 
	( Xh #> Yh
	; Xh #= Yh, greaterUnsigned( Xt, Yt, S ) ) .
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

lessUnsigned( X, Y, Size ) :-
	sizeof( X, Size ), sizeof( Y, Size ),
	
	X = [ Xh | Xt ], Y = [ Yh | Yt ],
	
	chunksize( C ),
	M is Size mod C,
	( M = 0 -> S is Size - C ; S is Size - M ),
	 
	( Xh #< Yh
	; Xh #= Yh, lessUnsigned( Xt, Yt, S ) ) .
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

greaterORequalUnsigned( X, Y, Size ) :-
	sizeof( X, Size ), sizeof( Y, Size ),
	
	X = [ Xh | Xt ], Y = [ Yh | Yt ],
	
	chunksize( C ),
	M is Size mod C,
	( M = 0 -> S is Size - C ; S is Size - M ),
	 
	( Xh #> Yh
	; Xh #= Yh, greaterORequalUnsigned( Xt, Yt, S ) ) .
greaterORequalUnsigned( [], [], 0 ).
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

lessORequalUnsigned( X, Y, Size ) :-
	sizeof( X, Size ), sizeof( Y, Size ),
	
	X = [ Xh | Xt ], Y = [ Yh | Yt ],
	
	chunksize( C ),
	M is Size mod C,
	( M = 0 -> S is Size - C ; S is Size - M ),
	 
	( Xh #< Yh
	; Xh #= Yh, lessORequalUnsigned( Xt, Yt, S ) ) .
lessORequalUnsigned( [], [], 0 ).
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

greaterSigned( X, Y, Size ) :-
	sizeof( X, Size ), sizeof( Y, Size ),

	X = [ Xh | _ ], Y = [ Yh | _ ],
	
	chunksize( C ),
	M is Size mod C,
	( M = 0 -> C1 is C - 1 ; C1 is M - 1 ),
	exp2( DC1, C1 ),
	
	( Xh #< DC1,  % X>=0
		( Yh #>= DC1   % Y< 0
		; Yh #< DC1, greaterUnsigned( X, Y, Size ) )  %Y>=0 
	; Xh #>= DC1, % X< 0
		Yh #>= DC1, % Y< 0
		greaterUnsigned( Y, X, Size )
	).
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

lessSigned( X, Y, Size ) :-
	greaterSigned( Y, X, Size ).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

greaterORequalSigned( X, Y, Size ) :-
	sizeof( X, Size ), sizeof( Y, Size ),

	X = [ Xh | _ ], Y = [ Yh | _ ],
	
	chunksize( C ),
	M is Size mod C,
	( M = 0 -> C1 is C - 1 ; C1 is M - 1 ),
	exp2( DC1, C1 ),

	( Xh #< DC1,  % X>=0
		( Yh #>= DC1   % Y< 0
		; Yh #< DC1, greaterORequalUnsigned( X, Y, Size ) )  %Y>=0 
	; Xh #>= DC1, % X< 0
		Yh #>= DC1, % Y< 0
		greaterORequalUnsigned( Y, X, Size )
	).
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

lessORequalSigned( X, Y, Size ) :-
	greaterORequalSigned( Y, X, Size ).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

notequal( X, Y, Size ) :-
	sizeof( X, Size ), sizeof( Y, Size ),

	X = [ Xh | Xt ], Y = [ Yh | Yt ],
	
	chunksize( C ),
	M is Size mod C,
	( M = 0 -> S is Size - C ; S is Size - M ),
	 
	( Xh #\= Yh
	; Xh #= Yh, notequal( Xt, Yt, S ) ) .
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

equal( X, Y, Size ) :-
	sizeof( X, Size ), sizeof( Y, Size ),

	X = [ Xh | Xt ], Y = [ Yh | Yt ],
	
	chunksize( C ),
	M is Size mod C,
	( M = 0 -> S is Size - C ; S is Size - M ),
	 
	Xh #= Yh, equal( Xt, Yt, S ) .
equal( [], [], 0 ).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

getbit( [Bit], X, SizeOfX, Index ) :-
	sizeof( X, SizeOfX ),

% looking for endeed chunck of X
	chunksize( C ),
	N is Index div C,
	length( X2, N ),
	append( _, [ Xh | X2 ], X ),

	I is Index mod C,	
	getbitFromNumber( Bit, Xh, I ) .
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% учесть случаи EndIndex и StartIndex на границах, EndIndex и StartIndex в одном chunk'е !!!
getbits( Bits, X, SizeOfX, EndIndex, StartIndex ) :-
	sizeof( X, SizeOfX ),
	
	chunksize( C ), C1 is C - 1, 
%looking for chunk from edge to StartIndex
	NStart is StartIndex div C,
	NEnd is EndIndex div C,
	length( X2Start, NStart ),
	append( _, [ XhStart | X2Start ], X ),
	IStart is StartIndex mod C,
	IEnd is EndIndex mod C,
	
	( NStart #= NEnd,
		Bits = [ B ],
		getbitsFromNumber( B, XhStart, IEnd, IStart )
	; NStart #< NEnd,
		getbitsFromNumber( BitsStart, XhStart, C1, IStart ),
		length( X2End, NEnd ),
		append( _, [ XhEnd | X2End ], X ),
		getbitsFromNumber( BitsEnd, XhEnd, IEnd, 0 ),
		
		N is NEnd - NStart - 1,
		BitsLen is N + 2,
		length( X2, N ),
		append( X2, _ ,  X2End ),
		append( [ BitsEnd | X2 ], [ BitsStart ], X1 ),
		( IStart = 0 ->
			Bits = X1
		;
			CMIS is C - IStart, defragment( Bits, X1, CMIS, BitsLen )
		)
	).

% [ 00ABC| D | E | 000FG ] -> [ ABCD | EFG ] StartSize = length("FG")
% missing of 0s before '*' in the last element of a list with shifting to the right the rest of the list
defragment( [ A ], [ A ], _, _ ) .
defragment( Normalized, Unnormalized, StartSize, SizeOfNumber ) :-
	chunksize( C ),
	ExtraSize is C - StartSize,
	
	%prepare U1 - version of Unnormalized without young element and left-shifted 'before-young' element
	append( Untail, [ A, B ], Unnormalized ),
	exp2( DS, StartSize ),
	B #< DS,
	exp2( DES, ExtraSize ),
	Bits #>= 0, Bits #< DES,
	A #= Bs * DES + Bits,
	append( Untail, [ Bs ], U1 ),
	% and defragment U1 to N1
	SizeMC is SizeOfNumber - C,
	( SizeMC =< 0 -> N1 = U1 ; defragment( N1, U1, StartSize, SizeMC ) ),

	NEnd is Bits * DS + B,
	append( N1, [ NEnd ], Normalized ).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

concat( Z, X, SizeOfX, Y, SizeOfY ) :-
	sizeof( X, SizeOfX ),
	sizeof( Y, SizeOfY ),
	
	chunksize( C ),
	LastBitsCount is SizeOfY mod C,
	( LastBitsCount = 0 ->
		append( X, Y, Z )
	; SizeOfX + SizeOfY =< C ->
		X = [Xh], Y = [Yh],
		exp2( DSY, SizeOfY ),
		Zh #= Xh * DSY + Yh,
		Z = [ Zh ]
	;
		ExtraBitsCount is C - LastBitsCount,
		
		append( Xrevtail, [ Xrevh ], X ),
		
		exp2( DEBS, ExtraBitsCount ),
		B #>= 0, B #< DEBS,
		Xrevh #= A * DEBS + B,
		
		( ExtraBitsCount >= SizeOfX -> 
			X2 = []
		 ;
		 	append( Xrevtail, [ A ], X1 ),
			SizeMC is SizeOfX - ExtraBitsCount,
			( SizeMC =< 0 ->
				X2 = X1
			;
				defragment( X2, X1, LastBitsCount, SizeMC )
			)
		),
		
		Y = [ Yh | Ytail ],
		exp2( DLBC, LastBitsCount ),
		Y1 #= Yh + B * DLBC,
		append( X2, [ Y1 | Ytail ], Z )
	).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

pow( Y, X, SizeOfX, N ) :-
	( SizeOfX = 1 ->
		( X = [0],
			Y = [0]
		; X = [1],
			chunksize( C ),
			LastSize is N mod C,
			FullChunks is N div C,
			exp2( DLS, LastSize ),
			DLS1 is DLS - 1,
			exp2( DC, C ),
			DC1 is DC - 1,
			fillConst( Y1, FullChunks, DC1 ),
			Y = [ DLS1 | Y1 ]
		)
	;	sizeof( X, SizeOfX ),
		powCycle( Y, [], 0, X, SizeOfX, N, [] )
	).

powCycle( Result, CurrentY, SizeOfCurrentY, X, SizeOfX, N, [] ) :-
	( N = 0 -> Result = CurrentY %finish!
	; N1 is N - 1, powCycle( Result, CurrentY, SizeOfCurrentY, X, SizeOfX, N1, X )
	).
powCycle( Result, CurrentY, SizeOfCurrentY, X, SizeOfX, N, Tail ) :-
	append( TailTail, [ LastElement ], Tail ),
	chunksize( C ),
	( TailTail = [] ->
		S is SizeOfX mod C,
		( S = 0 -> SizeOfLastElement is C
		;	SizeOfLastElement is S
		)
	;	SizeOfLastElement is C
	),
	concat( Y, [LastElement], SizeOfLastElement, CurrentY, SizeOfCurrentY ),
	SizeOfY is SizeOfLastElement + SizeOfCurrentY,
	powCycle( Result, Y, SizeOfY, X, SizeOfX, N, TailTail ).

fillConst( [], 0, _	 ) .
fillConst( [ Const | L ], Len, Const ) :-
	Len > 0,
	Len1 is Len - 1,
	fillConst( L, Len1, Const ).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

sum( Z, X, Y, Size ) :-
	length( X, L ), length( Y, L ), length( Z, L ), 
	sumWithCurry( Z, X, Y, Size, 0 ).

sumWithCurry( [], [], [], 0, _ ).
sumWithCurry( Z, X, Y, Size, Curry ) :-
	sizeof( X, Size ), sizeof( Y, Size ), sizeof( Z, Size ),
	append( Xt, [ Xh ], X ),
	append( Yt, [ Yh ], Y ),
	% check sign( Xh + Yh - 2^[C] + Curry )
	chunksize( C ),
	( Size >= C -> S = C ; S = Size ),
	exp2( DC, S ),
	T #= DC - Xh - Curry,
	
	( Xt = [] -> SizeT = 0 ; SizeT is Size - C ),
	
	( T #> Yh,
		% overflow won't be occured
		sumWithCurry( Zt, Xt, Yt, SizeT, 0 ),
		T1 #= Xh + Yh + Curry
	; T #=< Yh,
		% overflow will be occured
		sumWithCurry( Zt, Xt, Yt, SizeT, 1 ),
		T1 #= Yh - T
	),
	append( Zt, [ T1 ], Z ) .

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

sub( Z, X, Y, Size ) :-
	sum( X, Z, Y, Size ).
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

mulUnsigned( Z, SizeOfZ, X, Y, SizeOfX ) :-
	sizeof( X, SizeOfX ),
	
	append( Yt, [ Yh ], Y ),
	mul2chunk( Z1, X, Yh, 0, SizeOfX ),

	( Yt = [] ->
		fitSize( Z, Z1, SizeOfZ, 0 )
	;
		mulUnsigned( Z2, SizeOfZ, X, Yt, SizeOfX ),
		append( Z2, [0], Z22 ),
		fitSize( ZZ1, Z1, SizeOfZ, 0 ),
		fitSize( ZZ22, Z22, SizeOfZ, 0 ),
		sum( Z, ZZ1, ZZ22, SizeOfZ )
	).
	
mul2chunk( Z, X, Ychunk, CurryChunk, SizeOfX ) :-
	sizeof( X, SizeOfX ),
	
	( X = [] ->
		( CurryChunk #= 0, Z = [] ; CurryChunk #> 0, Z = [ CurryChunk ] )
	;
		chunksize( C ),
		( C mod 2 = 0 -> L2 is C div 2, L1 is L2 - 1 ; L2 is ( C - 1 ) div 2, L1 = L2 ),
		exp2( DL1, L1 ),
		exp2( DL2, L2 ),
		
		append( Xt, [ Xh ], X ),
		X1 #>= 0, X2 #>= 0, X3 #>= 0,
		X1 #< DL1, X2 #< DL2, X3 #< 2,
		Y1 #>= 0, Y2 #>= 0, Y3 #>= 0,
		Y1 #< DL1, Y2 #< DL2, Y3 #< 2,
		Xh #= X1 + X2 * DL1 + X3 * DL1 * DL2,
		Ychunk #= Y1 + Y2 * DL1 + Y3 * DL1 * DL2,
		
		S1 #= X1 * Y1,
		S2 #= X1 * Y2 + X2 * Y1,
		S3 #= X2 * Y2,
		
		Z1 = [ 0, S1 ],
		
		CL1 is C - L1,
		exp2( DCL1, CL1 ),
		S21 #>= 0, S22 #>= 0, S21 #< DCL1,
		S2 #= S21 + S22 * DCL1,
		S23 #= S21 * DL1,
		Z2 = [ S22, S23 ],
		
		C2 is C * 2,
		sum( Z21, Z1, Z2, C2 ),
		
		CL2 is C - 2*L1,
		exp2( DCL2, CL2 ),
		S31 #>= 0, S32 #>= 0, S31 #< DCL2,
		S3 #= S31 + S32 * DCL2,
		S33 #= S31 * DL1 * DL1,
		Z3 = [ S32, S33 ],
		
		sum( Z31, Z21, Z3, C2 ),
		
		C1 is C - 1, exp2( DC1, C1 ),
		
		( X3 = 0 ->
			Z41 = Z31
		;
			Y4 #>= 0, Y4 < 2, Y5 #>= 0,
			Ychunk #= Y4 + 2 * Y5,
			Y6 #= Y4 * DC1,
			Z4 = [ Y5, Y6 ],
			sum( Z41, Z31, Z4, C2 )
		),
		
		( Y3 = 0 ->
			Z51 = Z41
		;
			X4 #>= 0, X4 < 2, X5 #>= 0,
			Xh #= X4 + 2 * X5,
			X6 #= X4 * DC1,
			Z5 = [ X5, X6 ],
			sum( Z51, Z41, Z5, C2 )
		),
		
		sum( Z61, Z51, [ 0, CurryChunk ], C2 ),
		
		Z61 = [ Z7, Z8 ],
		
		( Xt = [] -> S = Size ; S is Size - C ),
		mul2chunk( Z9, Xt, Ychunk, Z7, S ),
		
		append( Z9, [ Z8 ], Z )
	).
	
% if NewSize < sizeof(Xold) => trunk( high bits of Xold )
fitSize( X, Xold, NewSize, HighBit ) :-
	chunksize( C ),
	
	FullChunks is integer( ceiling( NewSize / C ) ),
	
	length( Xold, OldLength ),
	
	( OldLength #< FullChunks,
		D #= FullChunks - OldLength,
		fillConst( Add0List, D, HighBit ),
		append( Add0List, Xold, X )

	;
		( OldLength #= FullChunks,
			XN = Xold
		; OldLength #> FullChunks,
			D #= OldLength - FullChunks,
			length( XD, D ),
			append( XD, XN, Xold )
		),
		
		XN = [ Xh | Xt ],
		NS is NewSize mod C,
		(NS = 0 -> NS1 = C ; NS1 = NS ),
		exp2( DNS1, NS1 ),
		Xh1 #>= 0, Xh1 #< DNS1,
		Xh #= Xh1 + DNS1 * _,
		X = [ Xh1 | Xt ]
	).	
		
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

mulSigned( Z, SizeOfZ, X, Y, Size ) :-
	sizeof( X, Size ), sizeof( Y, Size ),
	
	X = [ Xh | _ ], Y = [ Yh | _ ],
	
	mulUnsigned( S1, SizeOfZ, X, Y, Size ),
	
	chunksize( C ),
	M is Size mod C,
	( M = 0 -> C1 is C - 1 ; C1 is M - 1 ),
	exp2( DC1, C1 ),
	
	( Xh #< DC1, %X >= 0,
		S2 = S1
	; Xh #>= DC1, %X < 0,
		% S2 = S1 - fit(Y << Size)
		shiftLeft( Y1, Y, Size ),
		fitSize( YS1, Y1, SizeOfZ, 0 ),
		sub( S2, S1, YS1, SizeOfZ )
	),	
	
	( Yh #< DC1, %Y >= 0,
		S3 = S2
	; Yh #>= DC1, %Y < 0,
		% S3 = S2 - fit(X << Size)
		shiftLeft( X1, X, Size ),
		fitSize( XS1, X1, SizeOfZ, 0 ),
		sub( S3, S2, XS1, SizeOfZ )
	),
	
	( ( Yh #>= DC1, Xh #>= DC1, SizeOfZ #> 2 * Size )
		-> Z = S3
	; % Z = S3 + fit(1 << 2*Size)
		Size2 is 2 * Size,
		shiftLeft( ES, [1], Size2 ),
		fitSize( DS, ES, SizeOfZ, 0 ),
		sum( Z, S3, DS, SizeOfZ )
	).
	
shiftLeft( X, Xold, ShiftLen ) :-
	chunksize( C ),
	
	FullChunks is ShiftLen div C,
	LowShiftLen is ShiftLen mod C,
	
	( LowShiftLen = 0 ->
		X1 = Xold
	;
		lowShiftCycle( X1, Xold, LowShiftLen, 0 )
	),
	
	fillConst( Zeros, FullChunks, 0 ),
	append( X1, Zeros, X ).
	
lowShiftCycle( X, Xold, ShiftLen, High ) :-
	% assert 0 < ShiftLen < C
	exp2( DL, ShiftLen ),
	( Xold = [] ->
		X1 #= High * DL,
		X = [X1]
	;
		Xold = [ Xh | Xt ],
		chunksize( C ),
		C1 is C - ShiftLen,
		exp2( DC1, C1 ),
		X1 #>= 0, X1 #< DC1, X2 #>= 0,
		Xh #= X1 + DC1 * X2,
		X3 #= X2 + DL * High,
		lowShiftCycle( Xt1, Xt, ShiftLen, X1 ),
		X = [ X3 | Xt1 ]
	).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

signExtend( Y, X, OldSize, NewSize ) :-
	sizeof( X, OldSize ), sizeof( Y, NewSize ),
	( OldSize =< NewSize -> 
		% copy high bit to fit a new size
		X = [ Xh | Xt ],
		chunksize( C ),
		S is OldSize mod C,
		( S = 0 -> M is C - 1 ; M is S - 1 ),
		exp2( DM, M ),
		( Xh #< DM, % x >- 0
			fitSize( Y, X, NewSize, 0 )
		; Xh #>= DM,  % x <- 0
			exp2( DC, C ),
			D is DC - 2 * DM,
			Xh1 #= Xh + D,
			fitSize( Y, [ Xh1 | Xt ], NewSize, 1 )
		)
	; 
		chunksize( C ),
		FullChunks is NewSize div C,
		length( Xh, FullChunks ),
		append( Xt, Xh, X ),
		New is NewSize - C * FullChunks,
		exp2( DN, New ),
		append( _, [ Xd ], Xt ),
		YdTail #>= 0, Yd #>= 0,
		Xd #= Yd + DN * YdTail,
		append( [Yd], Xh, Y )
	).
		
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

random_result( X ) :-
	checklist( rnd_result, X ).
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% связь строкового представления (для очень длинного числа)
% и представления в виде списка chunk'ов.
% возможно потребуется добавить параметр - систему счисления
% строкового представления
% Size - битовая (!) длина числа
nstring2nlist( XasString, XasNumber, Size ) :-
	var( XasString ), sizeof( XasNumber, Size ) .
nstring2nlist( XasString, XasNumber, Size ) :-
	nonvar( XasString ),

	Size > 0,
	number_string( N, XasString ),
	chunksize( C ),
	exp2( DC, C ),
	N1 is N mod DC,
	N2 is N div DC,
	( N2 > 0 ->
		number_string( N2, S2 ),
		Size2 is Size - C,
		nstring2nlist( S2, X2, Size2 ),
		append( X2, [ N1 ], XasNumber )
	;
		( Size >= C ; Size < C, exp2( DS, Size ), N1 < DS ),
		FullChunks is integer( ceiling( Size / C ) ) - 1,
		fillConst( X2, FullChunks, 0 ),
		append( X2, [ N1 ], XasNumber )
	).

nlist2nstring( XasNumber, _, _ ) :-
	var( XasNumber ) .
nlist2nstring( XasNumber, XasString, Size ) :-
	nonvar( XasNumber ),
	sizeof( XasNumber, Size ),
	
	( XasNumber = [] ->
		XasString = "0"
	;
		chunksize( C ),
		exp2( DC, C ),
		append( Xt, [ Xh ], XasNumber ),
		( Xt = [] ->
			X1number = 0
		;
			S is Size - C,
			nlist2nstring( Xt, X1string, S ),
			number_string( X1number, X1string )
		),
		X is X1number * DC + Xh,
		number_string( X, XasString )
	) .