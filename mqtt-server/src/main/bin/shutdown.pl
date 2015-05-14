#!/usr/bin/perl -w
use Cwd;
use File::Basename;

$pathBase = dirname(getcwd);

if(-e "$pathBase/bin/pid"){
        open(PID_FILE,"$pathBase/bin/pid");
        @pidFile = <PID_FILE>;
        system("kill @pidFile");
	system("rm $pathBase/bin/pid");
	print("kill mqtt server @pidFile successfull! \n");
	close PID_FILE;
}else{
	die("mqtt service not start up\n");
}

