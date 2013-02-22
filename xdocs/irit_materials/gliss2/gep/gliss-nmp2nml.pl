#!/usr/bin/perl
#/****************************************************************************
#                                  nmp2nml.pl                                 
#                              ------------------
#    date            : March 2003    
#    copyright       : Institut de Recherche en Informatique de Toulouse
#    author          : Marc Finet
#    email           : finet@irit.fr, sainrat@irit.fr
# ****************************************************************************/
#
#/****************************************************************************
# *                                                                          *
# *   This program is free software; you can redistribute it and/or modify   *
# *   it under the terms of the GNU General Public License as published by   *
# *   the Free Software Foundation; either version 2 of the License, or      *
# *   (at your option) any later version.                                    *
# *                                                                          *
# ****************************************************************************/



# entree : nom de fichier (description nmp avec macro)
# sortie : sortie standard (description nml)
# Author : Marc Finet
# LastVersion : 31/03/2003
# Todo : piles d'appel pour les include (pour eviter les inclusions recursives)
#        definition des anciennes macro sauvees (fichier/ligne) pour re-definitions
#
# History (partial): 
# 27/06/2003:marc:check_macro: +check that parameter are not in string/comment
#                              +check that comments are alone on a line
# 22/05/2003:marc:parse_file: bug corrected on multi-line parameters macro 
#                 substitution


use strict;
# Global Variables
my %macro;                  # key : name of macro, val : (code,param1,param2,param3...)
my $nom_fichier_courrant;
my $num_ligne_courrante;
my $nom_fichier=$ARGV[0];
my $nb_line_parsed;
my $nb_lines_written;
my $nb_macro_subtitions;
my $nom_fichier_out;
my @list_macro_fp;

# !!HKC!!
my %macro_line;
my @line_stack = [];

if (!$nom_fichier){
    print STDERR "\nUsage : $0 <nml_file> [<output_file>]\n" . 
                 "      Parses an nmp file to output a nml file. That is,\n".
                 "      replaces the macro definitions, and changes some features\n" .
                 "      like includes or Floating Point Interface.\n\n";
    exit(1);
}


if ($ARGV[1]){
    $nom_fichier_out=$ARGV[1];
    open(STDOUT,">$nom_fichier_out");
}
    
# sur tout le fichier
open(FICHIER,$nom_fichier) or &nMP_erreur("File '$nom_fichier` not found !\n");

# ajoute les macros predefinies pour les flottants 
#@list_macro_fp=("fpi_setround","fpi_getround",                      # fonction d'arrondit
#    "FPI_TONEAREST","FPI_UPWARD","FPI_DOWNWARD","FPI_TOWARDZERO",   # valeurs d'arrondi
#    "fpi_clearexcept","fpi_raiseexcept","fpi_testexcept",           # fonction d'exception
#    "FPI_ALLEXCEPT","FPI_INEXACT","FPI_DIVBYZERO","FPI_UNDERFLOW",  # valeurs d'exception
#    "FPI_OVERFLOW","FPI_INVALID");                                  # cont'd

&parse_file(\*FICHIER,$nom_fichier);
print STDERR "nmp2nml succeeded, $nb_line_parsed line(s) parsed, $nb_lines_written line(s) written, $nb_macro_subtitions macro substitution(s) done !\n";
exit(0);



######################################################
#                       FONCTIONS 
######################################################

###########
#  Parse
sub parse_file{
    my @include_at_end;         # list of file to include at the end
    my $num_ligne;
    my $ligne;
    my $handle=shift(@_);
    my $file=shift(@_);
    my $nom_macro;
    my $pos;
    my $def_macro;
    my @lignes;
    my $file_included;
    my @param;
    my $continued_ligne;
    my $macro_found;
    my $debut;
    my $fin;
    my $nb_param;
    my $str_param;
    my $nb_paren;
    my @params;
    $num_ligne=0;
    @lignes=<$handle>;
    $nom_fichier_courrant=$file;
    BIG_BOUCLE:
    foreach $ligne (@lignes) {
        $num_ligne++;
        $num_ligne_courrante=$num_ligne;
        $nb_line_parsed++;
        # si c'est une definition de macro
        if ($ligne =~ /^\s*macro\s*(\w+)\s*\(?\s*/g) { 
            $nom_macro=$1;
			
			# !!HKC!!
			$macro_line{$nom_macro} = $num_ligne;
			
            if (&is_in($nom_macro,keys %macro)){
                $_=$ligne;
                chomp($_);
                &nMP_warning("Macro `$nom_macro' defined in file '$file`, line '$_`, is a re-definition\n".
                             "         Old definition is lost !\n");
            }
            push(@{$macro{$nom_macro}},"");
            $pos=pos($ligne);
            undef @param;
            # recupere les noms des parametres
            while ($ligne =~ /\G(\w+)\s*[,]?\s*/g) {
                push(@param,$1);
                push(@{$macro{$nom_macro}},$1);
                $pos=pos($ligne);
            }
            pos($ligne)=$pos;
            if (!($ligne =~ /\G\)?\s*=\s*(.*)\s*\n$/g)) {
                &nMP_erreur("Parse error, cannot recognize correct macro definition. Colone $pos : {$ligne}");
            }
            @{$macro{$nom_macro}}[0].=$1;
            @{$macro{$nom_macro}}[0] =~ s/\\$//;
            # check s'il y a encore des lignes
            $def_macro=($ligne=~/\\(\s*)$/); 
            $def_macro and ($1 ne "\n") and nMP_warning("backslash and newline separated by space\n");
            if(!$def_macro) {
				check_macro($nom_macro);
				
				# HKC
				$num_ligne = $num_ligne + 1;
				print "#line $num_ligne \"$nom_fichier_courrant\"\n";
				$num_ligne = $num_ligne - 1;
			}
        }
        
        # si c'est un commentaire dans la definition 
        elsif ($def_macro && $ligne =~ /^\s*\/\//){
            @{$macro{$nom_macro}}[0].=$ligne;
        }

        # si ca fait encore partie de la definition '\' a la fin 
        elsif ($def_macro && $ligne =~ s/\\(\s*)$//) { 
            $1 ne "\n" and print STDERR "backslash and newline separated by space\n";            
            @{$macro{$nom_macro}}[0].=$ligne."\n";
        } 


        # sinon, c'est donc la derniere ligne de la definition 
        elsif ($def_macro) {
            $def_macro=0;
            # remove \n 
            chomp($ligne);
            @{$macro{$nom_macro}}[0].=$ligne;
            check_macro($nom_macro);
            
            # !!HKC!!
            $num_ligne = $num_ligne + 1;
            
			print "#line $num_ligne \"$nom_fichier_courrant\"\n";
			
			# !!HKC!!
			$num_ligne = $num_ligne - 1;
        } 

        # si c'est un include (au debut)
        elsif ($ligne =~ /^\s*include\s*"([^"]*)"/) {
            if ($1 eq $file) {
                chomp($ligne);
                nMP_erreur("Cannot include himself {$ligne}\n");
            }
			
			# !!HKC!!
			push(@line_stack, $num_ligne)
			
            &include_file($1,$file,$num_ligne);
            $nom_fichier_courrant=$file;
			
			# !!HKC!!
			$num_ligne = pop(@line_stack) + 1;
			print "#line $num_ligne \"$nom_fichier_courrant\"\n"
			
			
        }

        # si c'est un include (a la fin)
        elsif ($ligne =~ /^\s*include([_-]?|\s*)op\s*"([^"]*)"/){
            if ($2 eq $file) {
                chomp($ligne);
                nMP_erreur("Cannot include himself {$ligne} !\n");
            }
            push(@include_at_end,$2,$file,$num_ligne);
        }

        # sinon (ligne normale)
        else {
		
			# !!HKC!! 
			my $one = 0;
            if (!$continued_ligne) {
                ($pos,$macro_found,$debut,$nb_paren)=&search_macro($ligne,0);
            }
            while ($pos!=0 || $continued_ligne){
                my $str;
				
				# !!HKC!!
				$one = 1;
				push(@line_stack, $num_ligne);
				
                # macro found (now or before) -> get parameters
                pos($ligne)=$pos;
                $fin = substr($ligne,$pos);
                if ($continued_ligne || $ligne =~ /\G\s*\(/mg){
                    my $pos2;
                    while ($ligne =~ /\G(\s*[^,\(\)]*)([,\(\)])/mg && $nb_paren>=0){
                        #print STDERR "Here we check : $1 / $2 ($nb_paren)\n";
                        $str_param.=$1;
                        ($2 eq '(') and $nb_paren++;
                        ($2 eq ')') and $nb_paren--;
                        if ( ($2 eq ',') and ($nb_paren==0) ){
                            #    print STDERR "Add $str_param\n";
                            push(@params,$str_param);
                            $str_param='';
                        } elsif ($nb_paren>=0){
                            $str_param.=$2;
                        }
                        $pos2=pos($ligne);
                    }
                    # check if all parameters read
                    if ($nb_paren>=0){            # no -> need more lines 
                        $pos=0;                 # next search will begin from 0
                        $continued_ligne=1;
                        $nb_lines_written+=count_cr($debut);
			print $debut;
                        undef $debut;
                        next BIG_BOUCLE;
                    }
                    $fin=substr($ligne,$pos2);
                    if ($str_param ne ''){
                        $str_param =~ s/^\s*//g;
                        push(@params,$str_param);
                    }
                }
                $continued_ligne=0;
                #print STDERR "($nom_fichier_courrant".":$num_ligne_courrante)Parmaters for $macro_found : @params / debut $debut / macro $macro_found\n";
                # replace parameters 
                $str=replace_parameters($macro_found,@params);
                #print STDERR "Macro substitution : '$str' ---> fin '$fin'\n";
                $nb_macro_subtitions++;
                #print STDERR "{$ligne"."[$debut$str\n";
                $ligne=$debut.$str.$fin;
				
				
                ($pos,$macro_found,$debut,$nb_paren)=&search_macro($ligne,$pos-length($macro_found));
                undef @params;
                undef $str_param;
            }

			# !!HKC!!
			if($one) {
				$ligne =~ s/\n/\n#line $num_ligne_courrante \"$nom_fichier_courrant\"\n/g;
			}

            $nb_lines_written+=count_cr($ligne);
		print $ligne;
			
			# !!HKC!!
			if($one) {
				$num_ligne = pop(@line_stack);
				my $next_line = $num_ligne + 1;
				print "#line $next_line \"$nom_fichier_courrant\"\n";
				$nb_lines_written += 1;
			}
        }
    }

    # traite les include op (dans l'ordre)
    while (@include_at_end){
        $num_ligne=pop(@include_at_end);
        $file=pop(@include_at_end);
        $file_included=pop(@include_at_end);
        &include_file($file_included,$file,$num_ligne);


    }
    return;
}

##################
# count_cr
sub count_cr{
    my $line=pop(@_);
    my @tab=split(/\n/,$line);
    my $nb=@tab;
    if ((@tab==0) && ($line =~ /\n/)) {
         $nb++;
     } elsif ($line !~ /\n/){
         $nb=0;
         
     }
    return $nb;
}

######################
# search_macro
sub search_macro{
    my ($ligne,$pos)=@_;
    my $work;
    my $rest;
    my $macro_found;
    pos($ligne)=$pos;
    #print STDERR "Search pos : $pos {$ligne}\n";
    #$ligne =~ /\G(.*)/mg;
    $rest=substr($ligne,$pos);
    $work=$rest;
    #print STDERR "Rest {$rest}\n";
    #suppress strings (macro (& comment) can't be on strings)
    $work=~ s/\"[^\"]*\"//mg;
    #suppress comment (macro can't be on a comment)
    #print STDERR "WorkBeforeCommentSuppr {$work}\n";
    $work=~ s/\/\/.*//mg;
    #parse to find macros
    #print STDERR "Work {$work}\n";
    while ($work =~ /\b(\w+)\b/mg){
        $macro_found=$1;
        #print STDERR "Trying $1";
        if (is_in($macro_found,keys %macro)){
            #print STDERR "Found $macro_found\n";
            # find (the first) macro found in the rest
            if ($rest =~ /^(?:\"[^\"]*\"|(?!\/\/)[^\"\n]|(?:\s*\/\/.*)?\n)*?\b$macro_found\b/mg){
                return (pos($rest)+$pos,$macro_found,substr($ligne,0,pos($rest)+$pos-length($macro_found)),0);
            }            
        }
    }
    return (0);
}

######################
# check_macro 
sub check_macro{
    my $nom=pop(@_);
    my ($txt,@params)=@{$macro{$nom}};
    my $param;
    # remove strings in macro
    $txt =~ s/"[^"]*"//mg;
    # check that comments are alone on lines
    while ($txt =~ /\G(.*)(\/\/.*)/g){
        my $remo=$1;
        my $rest=$2;
        if ($remo !~ /^\s*$/) {
            &nMP_erreur("Macro '$nom`, comments on macro are supported only if they are alone in the line {$remo}{$rest}");
        }
    }
    # remove all comments
    $txt =~ s/\/\/.*//mg;
    # check that all parameters are used
    foreach $param (@params){
        ($txt !~ /\b$param\b/) && nMP_warning("Macro '$nom` doesn't use parameter '$param` ! \n");
    }
    #print STDERR "Macro : $nom, params @params, code $txt\n";
}

######################
# replace_parameters
sub replace_parameters{
    my ($nom_macro,@params)=@_;
    my $expr=@{$macro{$nom_macro}}[0];
    my $i=1;
    my $param;
    my $name_param;
    if (@params != @{$macro{$nom_macro}}-1) {
        nMP_erreur("Wrong number of parameter for macro '$nom_macro` ".(@params+0)." given, ".(@{$macro{$nom_macro}}-1)." expected\n");
    }
    foreach $param (@params){
        $name_param=@{$macro{$nom_macro}}[$i++];
        $expr =~ s/\b$name_param\b/$param/g;
    }
    return $expr;
}

####################
# Is In
sub is_in {
    my $val=shift(@_);
    my @tableau=@_;
    my $tmp;
    foreach $tmp (@tableau){
        if ($tmp eq $val) {
            return 1;
        }
    }
    return 0;
}

############
# Include File
sub include_file {
    my $file=shift(@_);
    my $file_up=shift(@_);
    my $ligne=shift(@_);
    open (FILE,$file) or nMP_erreur("Cannot open \'$file\` included file from '$file_up`, line $ligne\n");
	
	# !!HKC!!
	print "#line 1 \"$file\"\n";
	$nb_lines_written += 1;
	
    return &parse_file(\*FILE,$file);
}

####################
# Substitute Param
sub substitute_param {
    my $ligne=pop(@_);
    my $num_param;
    my $param;
    # vire le \ final
    $ligne =~ s/\s*\\\s*(\n?)$/\n/;  
    # remplace les noms des params par leur numero 
    $num_param=1;
    foreach $param (@_) {
        $ligne =~ s/\b$param\b/\$$num_param/g;
        $num_param++;
    }
    return $ligne;
}
    


####################
# nMP Warning
sub nMP_warning {
    print STDERR "Warning: $nom_fichier_courrant: $num_ligne_courrante: @_";
    return 1;
}


####################
# nMP Erreur
sub nMP_erreur {
    print STDERR "Error: $nom_fichier_courrant: $num_ligne_courrante: @_\n";
    exit(1);
}
