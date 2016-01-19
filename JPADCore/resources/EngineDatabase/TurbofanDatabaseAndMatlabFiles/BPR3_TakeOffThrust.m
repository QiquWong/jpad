clc; close all; clear all;

load('BPR3_TakeOffThrustData.mat');

nPoints=60;
myMachVector=transpose(linspace(0,0.6,nPoints));

myAltitudeVector_FT = [ ...
    0;10000;20000 ...
    ];

%% 0 ft
MachVsThrustRatioSplineSL=pchip(MachSL,ThrustRatioSL);
myThrustVsMachSL=ppval(MachVsThrustRatioSplineSL,myMachVector);

%% 10k ft
MachVsThrustRatioSpline10=pchip(Mach10,ThrustRatio10);
myThrustVsMach10=ppval(MachVsThrustRatioSpline10,myMachVector);

%% 20k ft
MachVsThrustRatioSpline20=pchip(Mach20,ThrustRatio20);
myThrustVsMach20=ppval(MachVsThrustRatioSpline20,myMachVector);

%% 5k ft
myThrustVsMach05 = myThrustVsMach10*0.5 + myThrustVsMachSL*0.5;

%% GRAFICA
figure(1)
plot ( ...
    myMachVector, myThrustVsMach20, '-*b', ...
    myMachVector, myThrustVsMach10, '-*k', ...
    myMachVector, myThrustVsMachSL, '-*r' ...
 );
 xlabel('Mach No'); ylabel('Thrust Ratio');
 title('Bypass ratio 3.0 - take off thrust.');
 legend('20000 ft','10000 ft','0 ft');
 
axis([0 0.6 0.3 1]);

%columns --> curves
myData = [ ...
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
ThrustVsMachSplineSL = pchip(MachNSL, ThrustSL);
myThrustVsMachSL = ppval(ThrustVsMachSplineSL, myMachVector);

%% 5k ft
ThrustVsMachSpline5 = pchip(MachN5, Thrust5);
myThrustVsMach5 = ppval(ThrustVsMachSpline5, myMachVector);

%% 10k ft
ThrustVsMachSpline10 = pchip(MachN10, Thrust10);
myThrustVsMach10 = ppval(ThrustVsMachSpline10, myMachVector);


figure(1)
plot ( ...
        myMachVector, myThrustVsMachSL, '.-m', ...
        myMachVector, myThrustVsMach5, '.-c', ...
        myMachVector, myThrustVsMach10, '.-g' ...
        ); 

xlabel('Mach No'); ylabel('Thrust Ratio');
title('Bypass ratio 6.5 - takeoff thrust');
legend('0 ft','5000 ft','10000 ft');
axis([0 0.4 0.50 1]);


% columns --> curves
myData = [ ...
          myThrustVsMachSL, ...
          myThrustVsMach5, ...
          myThrustVsMach10, ...
		  myThrustVsMach10, ...
          ];


%% BPR 8

load('BPR8_TakeOffThrustData.mat');

nPoints = 60;
myMachVector = transpose(linspace(0, 0.6, nPoints));

%% 0
ThrustVsMachSplineSL = pchip(MachSL,ThrustSL);
myThrustVsMachSL = ppval(ThrustVsMachSplineSL,myMachVector);

%% 5k
ThrustVsMachSpline5000 = pchip(Mach5000,Thrust5000);
myThrustVsMach5000 = ppval(ThrustVsMachSpline5000,myMachVector);

%% 10k
ThrustVsMachSpline10000 = pchip(Mach10000,Thrust10000);
myThrustVsMach10000 = ppval(ThrustVsMachSpline10000,myMachVector);

%% GRAFICA
figure(1)
plot(...
    myMachVector, myThrustVsMachSL, '.-k',...
    myMachVector, myThrustVsMach5000, '.-r',...
    myMachVector, myThrustVsMach10000, '.-c'...
    );

xlabel('Mach no.');   ylabel('Thrust ratio');
axis([0 0.5 0.5 1]);
title('Bypass ratio 8.0 - take-off thrust.');
legend('0 ft','5000 ft','10000 ft');

%columns --> curves
myData = [ ...
    myThrustVsMachSL,...
	myThrustVsMach5000,...
	myThrustVsMach10000,...
	myThrustVsMach10000,...
    ];


%% BPR 13

clc; close all; clear all;

load('BPR13_TakeOffThrustData.mat');

nPoints = 60;
myMachVector = transpose(linspace(0, 0.6, nPoints));

%% 0 ft
ThrustVsMachSplineSL=pchip(MachSL,ThrustRatioSL);
myThrustVsMachSL=ppval(ThrustVsMachSplineSL,myMachVector);

%% 5k ft
ThrustVsMachSpline5=pchip(Mach5,ThrustRatio5);
myThrustVsMach5=ppval(ThrustVsMachSpline5,myMachVector);

%% 10k ft
ThrustVsMachSpline10=pchip(Mach10,ThrustRatio10);
myThrustVsMach10=ppval(ThrustVsMachSpline10,myMachVector);

%% GRAFICA
figure(1)
plot ( ...
    myMachVector, myThrustVsMach10, '-*r', ...
    myMachVector, myThrustVsMach5, '-*k', ...
    myMachVector, myThrustVsMachSL, '-*b' ...
 );
 xlabel('Mach No'); ylabel('Thrust Ratio');
 title('Bypass ratio 13.0 - take off thrust.');
 legend('10000 ft','5000 ft','0 ft');
 
axis([0 0.5 0.45 1]);

%columns --> curves
myData = [ ...
    myThrustVsMachSL,...
	myThrustVsMach5,...
	myThrustVsMach10, ...
	myThrustVsMach10 ...
    ];

