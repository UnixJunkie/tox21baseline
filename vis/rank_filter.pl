#!/usr/bin/perl -w

use strict;

if ($#ARGV < 0) {
    print "Usage: $0 PREFIX\n";
    exit 1;
}

my $PREFIX = $ARGV[0];

my ($assays, $data) = read_assay ($PREFIX);
my $smiles = read_smiles ($PREFIX);
my ($f2s, $s2f) = read_fragment ($PREFIX);
my @assays = sort {$assays->{$b} <=> $assays->{$a}} keys %$assays;

my %assays = (); # overall assay active pct
foreach my $key (@assays) {
    $assays{$key} = $assays->{$key}*100/(keys %$data);
}

my @fragments = filter_rank ($PREFIX);

foreach my $f (@fragments) {
    my $sampl = $f2s->{$f->{id}};
    my $r = "";
    foreach my $ass (@assays) {
	$r .= "." if $r ne "";
	my $na = 0; # number of active
	my $nd = 0; # number of undetermined
	foreach my $s (@$sampl) {
	    my $d = $data->{$s}->{$ass};
	    if (defined $d and $d ne "") {
		$na++ if $d == 1;
	    }
	    else {
		++$nd;
	    }
	}
	$r.="$ass:$smiles->{$f->{id}}:$na";
    }
    print "$r,$f->{n}\n";
}

#foreach my $key (keys %$data) {
#    print "$key ";
#    foreach my $a (keys %{$data->{$key}}) {
#	print "$a $data->{$key}->{$a}\n";
#    }
#    print "\n";
#}


##
sub read_assay {
    my $prefix = shift;
    open (F, $prefix.".assays.txt") or die;
    my $head = <F>;
    chomp $head;
    my @header = split /\t/, $head;
    my %sample = ();
    my %assay = ();
    for (my $i = 1; $i < @header; ++$i) {
	$assay{$header[$i]} = 0;
    }
    
    my $lines = 1;
    while (<F>) {
	chomp;
	my @fs = split /\t/, $_, -1;
	if (@fs != @header) {
	    print STDERR "$lines: column mismatch!\n";
	}
	else {
	    my %d = ();
	    for (my $i = 1; $i < @fs; ++$i) {
		my $ass = $header[$i];
		$d{$ass} = $fs[$i];
		if ($fs[$i] ne "" and $fs[$i] == 1) {
		    $assay{$ass} = $assay{$ass}+1;
		}
	    }
	    $sample{$fs[0]} = \%d;
	}
	++$lines;
    }
    close F;
    return (\%assay, \%sample);
}

sub filter_rank {
    my $prefix = shift;
    my @rank = ();
    
    open (F, $prefix.".rank.txt") or die;
    <F>;     # skip header
    while (<F>) {
	chomp;
	my @fs = split /\s/;
	my $n = $fs[2];
	my $in = $fs[3];
	my $out = $fs[4];
	if ($in > 5 && $out > 5 && $n > 10) {
	    #print "$_\n";
	    my %f = (id => $fs[0], n => $n, in => $in, out => $out);
	    push @rank, \%f;
	}
    }
    close F;
    return @rank;
}

sub read_fragment {
    my $prefix = shift;
    my %f2sampl = ();
    my %sampl2f = ();
    open (F, $prefix.".fmap.txt") or die;
    while (<F>) {
	chomp;
	my @fs = split /\s/;
	my @samples = ();
	for (my $i = 2; $i < @fs; ++$i) {
	    push @samples, $fs[$i];
	    my $sampl = $sampl2f{$fs[$i]};
	    if (!defined $sampl) {
		my @s = ();
		$sampl2f{$fs[$i]} = $sampl = \@s;
	    }
	    push @$sampl, $fs[0];
	}
	$f2sampl{$fs[0]} = \@samples;
    }
    close F;
    return (\%f2sampl, \%sampl2f);
}

sub read_smiles {
    my $prefix = shift;
    my %smiles = ();
    open (F, $prefix.".fragments.txt") or die;
    while (<F>) {
	chomp;
	my ($hash,$smi) = split /\t/;
	$smiles{$hash} = $smi;
    }
    return \%smiles;
}
