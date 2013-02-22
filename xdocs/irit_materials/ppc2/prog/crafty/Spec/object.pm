$benchnum  = '186';
$benchname = 'crafty';
$exename   = 'crafty';
$benchlang = 'C';
@base_exe  = ($exename);
#$obiwan    = 1;

@sources=qw(searchr.c search.c repeat.c next.c nextr.c history.c nexte.c
	    quiesce.c evaluate.c movgen.c make.c unmake.c lookup.c store.c
	    attacks.c swap.c boolean.c draw.c utility.c valid.c drawn.c edit.c
	    enprise.c init.c input.c interupt.c iterate.c main.c option.c
	    output.c phase.c ponder.c preeval.c root.c setboard.c time.c
	    validate.c);
$need_math='yes';
$bench_flags='-DSPEC_CPU2000';

sub invoke {
    my ($me) = @_;
    my $name;
    my @rc;

    my $exe = $me->exe_file;
    for ($me->input_files_base) {
	if (($name) = m/(.*).in$/) {
	    push (@rc, { 'command' => $exe, 
			 'args'    => [], 
			 'output'  => "$name.out",
			 'input'   => "$name.in",
			 'error'   => "$name.err",
			});
	}
    }
    @rc;
}

1;
