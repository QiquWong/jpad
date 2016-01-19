clear all; close all;

load('BPR3_MaxCruiseThrustData.mat');

myAltitudeVector_FT = [ ...
    0;10000;15000;25000;30000;36000;40000;45000 ...
    ];

nPoints = 60;
myMachVector = transpose(linspace(0, 1.0, nPoints));

%% 45k ft
thrustVsMachSpline45 = pchip(MachNo45, ThrustRatio45);
myThrustVsMach45 = ppval(thrustVsMachSpline45, myMachVector);

%% 40k ft
thrustVsMachSpline40 = pchip(MachNo40, ThrustRatio40);
myThrustVsMach40 = ppval(thrustVsMachSpline40, myMachVector);

%% 30k ft
thrustVsMachSpline30 = pchip(MachNo30, ThrustRatio30);
myThrustVsMach30 = ppval(thrustVsMachSpline30, myMachVector);

%% 20k ft
thrustVsMachSpline20 = pchip(MachNo20, ThrustRatio20);
myThrustVsMach20 = ppval(thrustVsMachSpline20, myMachVector);

%% 10k ft
thrustVsMachSpline10 = pchip(MachNo10, ThrustRatio10);
myThrustVsMach10 = ppval(thrustVsMachSpline10, myMachVector);

%% SL
thrustVsMachSplineSL = pchip(MachNoSL, ThrustRatioSL);
myThrustVsMachSL = ppval(thrustVsMachSplineSL, myMachVector);

%% 36k ft
myThrustVsMach40 = myThrustVsMach30*0.4 + myThrustVsMach40*0.6;

%% 25k ft
myThrustVsMach25 = myThrustVsMach20*0.5 + myThrustVsMach30*0.5;

%% 15k ft
myThrustVsMach15 = myThrustVsMach10*0.5 + myThrustVsMach20*0.5;

%% GRAFICA
figure(1)
plot ( ...
    myMachVector, myThrustVsMach45, '.-b', ...
    myMachVector, myThrustVsMach40, '.-r', ...
    myMachVector, myThrustVsMach30, '.-y', ...
    myMachVector, myThrustVsMach20, '.-k', ...
    myMachVector, myThrustVsMach10, '.-g', ...
    myMachVector, myThrustVsMachSL, '.-m' ...
    );
  
xlabel('Mach No');  ylabel('Thrust Ratio');
title('Bypass ratio 3.0 - maximum cruise thrust.');
legend('45000 ft','40000 ft','30000 ft','20000 ft','10000 ft', '0 ft');
axis([0 max(MachNo45) 0 0.8]);

% columns --> curves
myData = [ ...
    myThrustVsMachSL,...
	myThrustVsMach10,...
	myThrustVsMach15,...
	myThrustVsMach25,...
	myThrustVsMach30, ...
	myThrustVsMach36,...
        myThrustVsMach40,...
		myThrustVsMach45 ...
    ];


%% BPR 6.5

load('BPR65_MaxCruiseThrustData.mat');
nPoints = 60;
myMachVector = transpose(linspace(0, 1.0, nPoints));

%% 45k
thrustVsMachSpline45 = pchip(Mach45,Thrust45);
myThrustVsMach45 = ppval(thrustVsMachSpline45,myMachVector);

%% 40k
thrustVsMachSpline40 = pchip(Mach40,Thrust40);
myThrustVsMach40 = ppval(thrustVsMachSpline40,myMachVector);

%% 36k
thrustVsMachSpline36 = pchip(Mach36,Thrust36);
myThrustVsMach36 = ppval(thrustVsMachSpline36,myMachVector);

%% 30k
thrustVsMachSpline30 = pchip(Mach30,Thrust30);
myThrustVsMach30 = ppval(thrustVsMachSpline30,myMachVector);

%% 25k
thrustVsMachSpline25 = pchip(Mach25,Thrust25);
myThrustVsMach25 = ppval(thrustVsMachSpline25,myMachVector);

%% 15k
thrustVsMachSpline15 = pchip(Mach15,Thrust15);
myThrustVsMach15 = ppval(thrustVsMachSpline15,myMachVector);

%% 10k
thrustVsMachSpline10 = pchip(Mach10,Thrust10);
myThrustVsMach10 = ppval(thrustVsMachSpline10,myMachVector);

%% 0
thrustVsMachSplineSL = pchip(MachSL,ThrustSL);
myThrustVsMachSL = ppval(thrustVsMachSplineSL,myMachVector);


%% GRAFICA

figure(1)
plot(...
    myMachVector, myThrustVsMachSL, '.-k',...
    myMachVector, myThrustVsMach10, '.-r',...
    myMachVector, myThrustVsMach15, '.-c',...
    myMachVector, myThrustVsMach25, '.-b',...
    myMachVector, myThrustVsMach30, '.-b',...
    myMachVector, myThrustVsMach36, '.-b',...
    myMachVector, myThrustVsMach40, '.-g',...
    myMachVector, myThrustVsMach45, '.-m'...
    );

xlabel('Mach no.');   ylabel('Thrust ratio');
title('Bypass ratio 3.0 - maximum climb thrust.');
legend('0 ft','10000 ft','15000 ft','25000 ft','30000 ft','36000 ft','40000 ft','45000 ft');



%columns --> curves
myData = [ ...
    myThrustVsMachSL,
	myThrustVsMach10,
	myThrustVsMach25,...
    myThrustVsMach30,
	myThrustVsMach36,
	myThrustVsMach40, ...
    myThrustVsMach45...
    ];


%% BPR 8

load('BPR8_MaximumCruiseThrustData');
nPoints = 60;
myMachVector = transpose(linspace(0, 1.0, nPoints));

%% 0 ft
ThrustVsMachSplineSL=pchip(MachSL,ThrustRatioSL);
myThrustVsMachSL=ppval(ThrustVsMachSplineSL,myMachVector);

%% 10k ft
ThrustVsMachSpline10=pchip(Mach10,ThrustRatio10);
myThrustVsMach10=ppval(ThrustVsMachSpline10,myMachVector);

%% 15k ft
ThrustVsMachSpline15=pchip(Mach15,ThrustRatio15);
myThrustVsMach15=ppval(ThrustVsMachSpline15,myMachVector);

%% 25k ft
ThrustVsMachSpline25=pchip(Mach25,ThrustRatio25);
myThrustVsMach25=ppval(ThrustVsMachSpline25,myMachVector);

%% 30k ft
ThrustVsMachSpline30=pchip(Mach30,ThrustRatio30);
myThrustVsMach30=ppval(ThrustVsMachSpline30,myMachVector);

%% 36k ft
ThrustVsMachSpline36=pchip(Mach36,ThrustRatio36);
myThrustVsMach36=ppval(ThrustVsMachSpline36,myMachVector);

%% 40k ft
ThrustVsMachSpline40=pchip(Mach40,ThrustRatio40);
myThrustVsMach40=ppval(ThrustVsMachSpline40,myMachVector);

%% 45k ft
ThrustVsMachSpline45=pchip(Mach45,ThrustRatio45);
myThrustVsMach45=ppval(ThrustVsMachSpline45,myMachVector);

%% GRAFICA
figure(1)
plot ( ...
    myMachVector, myThrustVsMach45, '-*b', ...
    myMachVector, myThrustVsMach40, '-*r', ...
    myMachVector, myThrustVsMach36, '-*m', ...
    myMachVector, myThrustVsMach30, '-*c', ...
    myMachVector, myThrustVsMach25, '-*y', ...
    myMachVector, myThrustVsMach15, '-*g', ...
    myMachVector, myThrustVsMach10, '-*k', ...
    myMachVector, myThrustVsMachSL, '-*b' ...
 );
 xlabel('Mach No'); ylabel('Thrust Ratio');
 title('Bypass ratio 8.0 - maximum cruise thrust.');
 legend('45000 ft','40000 ft','36000 ft','30000 ft','25000 ft','15000 ft','10000 ft','0 ft');
 
axis([0 0.97 0 0.8]);

%columns --> curves
myData = [ ...
    myThrustVsMachSL,
	myThrustVsMach10,
	myThrustVsMach15,
	myThrustVsMach25,...
    myThrustVsMach30,
	myThrustVsMach36,
	myThrustVsMach40,
	myThrustVsMach45 ...
    ];


%% BPR 13

load('BPR13_MaxCruiseThrustData.mat');
nPoints = 60;
myMachVector = transpose(linspace(0, 1.0, nPoints));

%% 45k ft
thrustVsMachSpline1345 = pchip(Mach1345, Thrust1345);
myThrustVsMach1345 = ppval(thrustVsMachSpline1345, myMachVector);

%% 40k ft
thrustVsMachSpline1340 = pchip(Mach1340, Thrust1340);
myThrustVsMach1340 = ppval(thrustVsMachSpline1340, myMachVector);

%% 30k ft
thrustVsMachSpline1330 = pchip(Mach1330, Thrust1330);
myThrustVsMach1330 = ppval(thrustVsMachSpline1330, myMachVector);

%% 25k ft
thrustVsMachSpline1325 = pchip(Mach1325, Thrust1325);
myThrustVsMach1325 = ppval(thrustVsMachSpline1325, myMachVector);

%% 15k ft
thrustVsMachSpline1315 = pchip(Mach1315, Thrust1315);
myThrustVsMach1315 = ppval(thrustVsMachSpline1315, myMachVector);

%% 10k ft
thrustVsMachSpline1310 = pchip(Mach1310, Thrust1310);
myThrustVsMach1310 = ppval(thrustVsMachSpline1310, myMachVector);

%% SL
thrustVsMachSpline13SL = pchip(Mach13SL, Thrust13SL);
myThrustVsMach13SL = ppval(thrustVsMachSpline13SL, myMachVector);

%% 36k ft
myThrustVsMach1336 = myThrustVsMach1330*0.4 + myThrustVsMach1340*0.6;

%% GRAFICA
figure(1)
plot ( ...
      myMachVector, myThrustVsMach1345, '.-b', ...
      myMachVector, myThrustVsMach1340, '.-r', ...
      myMachVector, myThrustVsMach1330, '.-y', ...
      myMachVector, myThrustVsMach1325, '.-k', ...
      myMachVector, myThrustVsMach1315, '.-c', ...
      myMachVector, myThrustVsMach1310, '.-g', ...
      myMachVector, myThrustVsMach13SL, '.-m' ...
      );
  xlabel('Mach No');  ylabel('Thrust Ratio');
  title('Bypass ratio 13.0 - maximum cruise thrust.');
  legend('45000 ft','40000 ft','30000 ft','25000 ft','15000 ft','10000 ft', '0 ft');
  axis([0 0.88 0 0.9]);
  
  %% Preparing output to HDF
% increasing altitudes
myAltitudeVector_FT = flipud([ ...
    45000;40000;30000;25000;15000;10000;0 ...
    ]);

% columns --> curves
myData = [ ...
    myThrustVsMach13SL,...
	myThrustVsMach1310,...
	myThrustVsMach1325,...
	myThrustVsMach1330, ...
	myThrustVsMach1336,...
        myThrustVsMach1340,...
		myThrustVsMach1345 ...
    ];

	
	%% WRITE EVERYTHING TO DATABASE
myHDFFile = matfile('EngineData.h5','Writable',true);

MaximumCruiseThrust.data = myData;
MaximumCruiseThrust.var0 = myAltitudeVector_FT';
MaximumCruiseThrust.var1 = myMachVector';

myHDFFile.MaximumCruiseThrust = MaximumCruiseThrust;

