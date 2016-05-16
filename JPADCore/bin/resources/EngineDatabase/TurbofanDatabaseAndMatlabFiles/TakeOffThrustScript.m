clc; close all; clear all;

load('BPR3_TakeOffThrustData.mat');

nPoints=60;
machMax = 0.6;
myMachVector=transpose(linspace(0,machMax,nPoints));

myAltitudeVector_FT = [ ...
    0;5000;10000;20000 ...
    ];

%% 0 ft
% MachVsThrustRatioSplineSL=pchip(MachSL,ThrustRatioSL);
myThrustVsMachSL=extendMach(machMax,MachSL,ThrustRatioSL);

%% 10k ft
% MachVsThrustRatioSpline10=pchip(Mach10,ThrustRatio10);
myThrustVsMach10=extendMach(machMax,Mach10,ThrustRatio10);

%% 20k ft
% MachVsThrustRatioSpline20=pchip(Mach20,ThrustRatio20);
myThrustVsMach20=extendMach(machMax,Mach20, ThrustRatio20);

%% 5k ft
thrustMatrix(1,:) = myThrustVsMachSL;
thrustMatrix(2,:) = myThrustVsMach10;
thrustMatrix(3,:) = myThrustVsMach20;

altitudes = [0,10,20]*1000;

myThrustVsMach05 = interpTgivenM_h(myMachVector,thrustMatrix,altitudes,nPoints,machMax,5000,1,1.)';

%% GRAFICA
figure(1)
plot ( ...
    myMachVector, myThrustVsMach20, '-b', ...
    myMachVector, myThrustVsMach10, '-k', ...
    myMachVector, myThrustVsMachSL, '-r' ...
 );
 xlabel('Mach No'); ylabel('Thrust Ratio');
 title('Bypass ratio 3.0 - take off thrust.');
 legend('20000 ft','10000 ft','0 ft');
 
axis([0 0.6 0.3 1]);

%columns --> curves
myData(1,:,:) = [ ...
    myThrustVsMachSL,...
	myThrustVsMach05,...
	myThrustVsMach10,...
	myThrustVsMach20 ...
    ]';

	
%% BPR 6.5

load('BPR65_TakeOffThrustData.mat');

nPoints = 60;
myMachVector = transpose(linspace(0, 0.6, nPoints));

%% SL
% ThrustVsMachSplineSL = pchip(MachNSL, ThrustSL);
myThrustVsMachSL = extendMach(machMax,MachNSL, ThrustSL);

%% 5k ft
% ThrustVsMachSpline5 = pchip(MachN5, Thrust5);
myThrustVsMach5 = extendMach(machMax,MachN5, Thrust5);

%% 10k ft
% ThrustVsMachSpline10 = pchip(MachN10, Thrust10);
extendMach(machMax,MachN10, Thrust10);

figure(2)
plot ( ...
        myMachVector, myThrustVsMachSL, '-m', ...
        myMachVector, myThrustVsMach5, '-c', ...
        myMachVector, myThrustVsMach10, '-g' ...
        ); 

xlabel('Mach No'); ylabel('Thrust Ratio');
title('Bypass ratio 6.5 - takeoff thrust');
legend('0 ft','5000 ft','10000 ft');
axis([0 0.6 0.50 1]);

% columns --> curves
myData(2,:,:) = [ ...
          myThrustVsMachSL, ...
          myThrustVsMach5, ...
          myThrustVsMach10, ...
		  myThrustVsMach10, ...
          ]';


%% BPR 8

load('BPR8_TakeOffThrustData.mat');

nPoints = 60;
myMachVector = transpose(linspace(0, 0.6, nPoints));

%% 0
% ThrustVsMachSplineSL = pchip(MachSL,ThrustSL);
myThrustVsMachSL = extendMach(machMax,MachSL, ThrustSL);

%% 5k
% ThrustVsMachSpline5000 = pchip(Mach5000,Thrust5000);
myThrustVsMach5000 = extendMach(machMax,Mach5000, Thrust5000);

%% 10k
% ThrustVsMachSpline10000 = pchip(Mach10000,Thrust10000);
myThrustVsMach10000 = extendMach(machMax,Mach10000, Thrust10000);

%% GRAFICA
figure(3)
plot(...
    myMachVector, myThrustVsMachSL, '-k',...
    myMachVector, myThrustVsMach5000, '-r',...
    myMachVector, myThrustVsMach10000, '-c'...
    );

xlabel('Mach no.');   ylabel('Thrust ratio');
axis([0 0.6 0.5 1]);
title('Bypass ratio 8.0 - take-off thrust.');
legend('0 ft','5000 ft','10000 ft');

%columns --> curves
myData(3,:,:) = [ ...
    myThrustVsMachSL,...
	myThrustVsMach5000,...
	myThrustVsMach10000,...
	myThrustVsMach10000,...
    ]';


%% BPR 13

load('BPR13_TakeOffThrustData.mat');

nPoints = 60;
myMachVector = transpose(linspace(0, 0.6, nPoints));

%% 0 ft
% ThrustVsMachSplineSL=pchip(MachSL,ThrustRatioSL);
myThrustVsMachSL=extendMach(machMax,MachSL, ThrustRatioSL);

%% 5k ft
% ThrustVsMachSpline5=pchip(Mach5,ThrustRatio5);
myThrustVsMach5=extendMach(machMax,Mach5, ThrustRatio5);

%% 10k ft
% ThrustVsMachSpline10=pchip(Mach10,ThrustRatio10);
myThrustVsMach10=extendMach(machMax,Mach10, ThrustRatio10);

%% GRAFICA
figure(4)
plot ( ...
    myMachVector, myThrustVsMach10, '-r', ...
    myMachVector, myThrustVsMach5, '-k', ...
    myMachVector, myThrustVsMachSL, '-b' ...
 );
 xlabel('Mach No'); ylabel('Thrust Ratio');
 title('Bypass ratio 13.0 - take off thrust.');
 legend('10000 ft','5000 ft','0 ft');
 
axis([0 0.6 0.45 1]);

%columns --> curves
myData(4,:,:) = [ ...
    myThrustVsMachSL,...
	myThrustVsMach5,...
	myThrustVsMach10, ...
	myThrustVsMach10 ...
    ]';

%% WRITE EVERYTHING TO DATABASE
myHDFFile = matfile('TurbofanEngineDatabase.h5','Writable',true);

TakeOffThrust.data = myData;
TakeOffThrust.var_0 = [3.0,6.5,8.0,13.0];
TakeOffThrust.var_1 = myAltitudeVector_FT';
TakeOffThrust.var_2 = myMachVector';

myHDFFile.TakeOffThrust = TakeOffThrust;

