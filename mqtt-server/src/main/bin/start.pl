#!/usr/bin/perl -w
use Cwd;
use File::Basename;
$pathBase = dirname(getcwd);

$opts = " -server -Xms2148m -Xmx2148m -verbose:gc -Xloggc:gc.log:$pathBase/logs/gc.log  -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseParNewGC -XX:+UseConcMarkSweepGC ";

if(-e "$pathBase/bin/pid"){
	open(PID_FILE,"$pathBase/bin/pid");
	@pidFile = <PID_FILE>;
	die "mqtt server already runing pid is @pidFile\n";
}

if(-e "$pathBase/conf/logback.xml"){
	$opts = $opts." -Dlogback.configurationFile=$pathBase/conf/logback.xml";
}

if(!-e "$pathBase/logs"){
	mkdir "$pathBase/logs";
}

print("mqtt base dir is $pathBase\n");

chdir($pathBase);

open(MQTT_CONFIG,"$pathBase/conf/mqtt.cfg") or die "mqtt config file not existes $pathBase/conf/mqtt.cfg";

while(<MQTT_CONFIG>){
	chomp;
	next if /^#|^$/;
	my($k,$v) = split /\s*\=\s*/;
	$conf{$k} = $v;
}

close MQTT_CONFIG;

$cfg = "";

if( !defined $conf{"zk"}){
	die "zk properties shoul not null or empty!"
}else{
	$cfg = $cfg." -Dzk=".$conf{"zk"};
}

if(!defined $conf{"host"}){
	$cfg = $cfg." -Dhost=localhost"
}else{
	$cfg = $cfg." -Dhost=".$conf{"host"};
}

if(!defined $conf{"mqttPort"}){
	$cfg = $cfg." -DmqttPort=1883"
}else{
	$cfg = $cfg." -DmqttPort=".$conf{"mqttPort"};
}

if(!defined $conf{"rmiPort"}){
	$cfg = $cfg." -DrmiPort=1088";
}else{
	$cfg = $cfg." -DrmiPort=".$conf{"rmiPort"};
}

$opts = $opts.$cfg;

print("nohup java -jar $opts mqtt-server.jar >> $pathBase/logs/mqtt.out 2>&1\n");
system("nohup java -jar $opts mqtt-server.jar 2>&1>> $pathBase/logs/mqtt.out &");

$ps = `ps | grep '$cfg'`;

@pses = split(/\n/,$ps);

foreach $p (@pses){
	if($p !~ /grep/){
	  @s = split(/ /,$p);
	  $pid = $s[0];	
	}
}

print "pid is $pid \n";
open(PID,">$pathBase/bin/pid") or die "pid file open error";
print PID $pid;
close pid;


