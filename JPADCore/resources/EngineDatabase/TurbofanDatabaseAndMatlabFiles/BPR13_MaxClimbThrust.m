clc; clear all; close all;

%% BPR 3
load('BPR3_MaxClimbThrustData.mat');

nPoints = 30;
myMachVector = transpose(linspace(0, 0.888, nPoints));
myAltitudeVector_FT = [ ...
    0;10000;15000;25000;30000;36000;40000;45000 ...
    ];

%% 45k
thrustVsMachSpline45 = pchip(Mach45,Thrust45);
myThrustVsMach45 = ppval(thrustVsMachSpline45,myMachVector);

%% 40k
thrustVsMachSpline40 = pchip(Mach40,Thrust40);
myThrustVsMach40 = ppval(thrustVsMachSpline40,myMachVector);

%% 30k
thrustVsMachSpline30 = pchip(Mach30,Thrust30);
myThrustVsMach30 = ppval(thrustVsMachSpline30,myMachVector);

%% 20k
thrustVsMachSpline20 = pchip(Mach20,Thrust20);
myThrustVsMach20 = ppval(thrustVsMachSpline20,myMachVector);

%% 10k
thrustVsMachSpline10 = pchip(Mach10,Thrust10);
myThrustVsMach10 = ppval(thrustVsMachSpline10,myMachVector);

%% 0k
thrustVsMachSplineSL = pchip(MachSL,ThrustSL);
myThrustVsMachSL = ppval(thrustVsMachSplineSL,myMachVector);

%% 36k
myThrustVsMach36 = myThrustVsMach30*0.4 + myThrustVsMach40*0.6;

%% GRAFICA

figure(1)
plot(...
    myMachVector, myThrustVsMachSL, '.-k',...
    myMachVector, myThrustVsMach10, '.-r',...
    myMachVector, myThrustVsMach20, '.-c',...
    myMachVector, myThrustVsMach30, '.-b',...
    myMachVector, myThrustVsMach40, '.-g',...
    myMachVector, myThrustVsMach45, '.-m'...
    );

xlabel('Mach no.');   ylabel('Thrust ratio');
title('Bypass ratio 3.0 - maximum climb thrust.');
legend('0 ft','10000 ft','20000 ft','30000 ft','40000 ft','45000 ft');

%% preparing output to HDF

%increasing altitudes
myAltitudeVector_FT = [ ...
    0;10000;20000;30000;40000;45000 ...
    ];

%columns --> curves
myData(1,:,:) = [ ...
    myThrustVsMachSL,...
	myThrustVsMach10,...
	myThrustVsMach20,...
    myThrustVsMach30,...
	myThrustVsMach36,...
	myThrustVsMach40,...
	myThrustVsMach45 ...
    ];


%% BPR 6.5
load('BPR65_MaximumClimbThrustData.mat');
nPoints = 30;
myMachVector = transpose(linspace(0, 1, nPoints));

%% 0 ft
MachVsThrustRatioSplineSL=pchip(MachSL,ThrustRatioSL);
myThrustVsMachSL=ppval(MachVsThrustRatioSplineSL,myMachVector);

%% 10k ft
MachVsThrustRatioSpline10=pchip(Mach10,ThrustRatio10);
myThrustVsMach10=ppval(MachVsThrustRatioSpline10,myMachVector);

%% 15k ft
MachVsThrustRatioSpline15=pchip(Mach15,ThrustRatio15);
myThrustVsMach15=ppval(MachVsThrustRatioSpline15,myMachVector);

%% 25k ft
MachVsThrustRatioSpline25=pchip(Mach25,ThrustRatio25);
myThrustVsMach25=ppval(MachVsThrustRatioSpline25,myMachVector);

%% 30k ft
MachVsThrustRatioSpline30=pchip(Mach30,ThrustRatio30);
myThrustVsMach30=ppval(MachVsThrustRatioSpline30,myMachVector);

%% 36k ft
MachVsThrustRatioSpline36=pchip(Mach36,ThrustRatio36);
myThrustVsMach36=ppval(MachVsThrustRatioSpline36,myMachVector);

%% 40k ft
MachVsThrustRatioSpline40=pchip(Mach40,ThrustRatio40);
myThrustVsMach40=ppval(MachVsThrustRatioSpline40,myMachVector);

%% 45k ft
MachVsThrustRatioSpline45=pchip(Mach45,ThrustRatio45);
myThrustVsMach45=ppval(MachVsThrustRatioSpline45,myMachVector);

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
 title('Bypass ratio 6.5 - maximum climb thrust.');
 legend('45000 ft','40000 ft','36000 ft','30000 ft','25000 ft','15000 ft','10000 ft','0 ft');
 
axis([0 1 0 0.9]);

%columns --> curves
myData(2,:,:) = [ ...
    myThrustVsMachSL,...
	myThrustVsMach10,...
	myThrustVsMach15,...
	myThrustVsMach25,...
    myThrustVsMach30,...
	myThrustVsMach36,...
	myThrustVsMach40,...
	myThrustVsMach45 ...
    ];


%% BPR 8.0
load('BPR8_MaxClimbThrustData.mat');
nPoints = 30;
myMachVector = transpose(linspace(0, 1.0, nPoints));

%% 45k ft
thrustVsMachMCSpline45 = pchip(MachMC45, ThrustMC45);
myThrustVsMachMC45 = ppval(thrustVsMachMCSpline45, myMachVector);

%% 40k ft
thrustVsMachSplineMC40 = pchip(MachMC40, ThrustMC40);
myThrustVsMachMC40 = ppval(thrustVsMachSplineMC40, myMachVector);

%% 36k ft
thrustVsMachSplineMC36 = pchip(MachMC36, ThrustMC36);
myThrustVsMachMC36 = ppval(thrustVsMachSplineMC36, myMachVector);

%% 30k ft
thrustVsMachSplineMC30 = pchip(MachMC30, ThrustMC30);
myThrustVsMachMC30 = ppval(thrustVsMachSplineMC30, myMachVector);

%% 25k ft
thrustVsMachMCSpline25 = pchip(MachMC25, ThrustMC25);
myThrustVsMachMC25 = ppval(thrustVsMachMCSpline25, myMachVector);

%% 15k ft
thrustVsMachMCSpline15 = pchip(MachMC15, ThrustMC15);
myThrustVsMachMC15 = ppval(thrustVsMachMCSpline15, myMachVector);

%% 10k ft
thrustVsMachMCSpline10 = pchip(MachMC10, ThrustMC10);
myThrustVsMachMC10 = ppval(thrustVsMachMCSpline10, myMachVector);

%% SL
thrustVsMachMCSplineSL = pchip(MachMCSL, ThrustMCSL);
myThrustVsMachMCSL = ppval(thrustVsMachMCSplineSL, myMachVector);

%% GRAFICA
figure(1)
plot ( ...
       myMachVector, myThrustVsMachMC45, '.-b', ...
       myMachVector, myThrustVsMachMC40, '.-r', ...
       myMachVector, myThrustVsMachMC36, '.-c', ...
       myMachVector, myThrustVsMachMC30, '.-y', ...
       myMachVector, myThrustVsMachMC25, '.-k', ...
       myMachVector, myThrustVsMachMC15, '.-b', ...
       myMachVector, myThrustVsMachMC10, '.-g', ... 
       myMachVector, myThrustVsMachMCSL, '.-m'  ...
       );
  xlabel('Mach No');  ylabel('Thrust Ratio');
  title('Bypass ratio 3.0 - maximum cruise thrust.');
  legend('45000 ft','40000 ft','36000 ft', '30000 ft','25000 ft','15000', '10000 ft', '0 ft');
axis([0 1.0 0 0.9]);

% columns --> curves
myData(3,:,:) = [ ...
    myThrustVsMachMCSL,...
	myThrustVsMachMC10,...
	myThrustVsMachMC15,...
	myThrustVsMachMC25,...
	myThrustVsMachMC30,...
	myThrustVsMachMC36, ...
        myThrustVsMachMC40,
		myThrustVsMachMC45 ...
    ];


%% BPR 13
load('BPR13_MaxClimbThrustData.mat');
myMachVector = transpose(linspace(0, 1.0, nPoints));

%%
ThrustVsMachSpline45 = pchip(Mach45,Thrust45);
myThrustVsMach45 = ppval(ThrustVsMachSpline45,myMachVector);

%%
ThrustVsMachSpline40 = pchip(Mach40,Thrust40);
myThrustVsMach40 = ppval(ThrustVsMachSpline40,myMachVector);

%%
ThrustVsMachSpline30 = pchip(Mach30,Thrust30);
myThrustVsMach30 = ppval(ThrustVsMachSpline30,myMachVector);

%%
ThrustVsMachSpline20 = pchip(Mach20,Thrust20);
myThrustVsMach20 = ppval(ThrustVsMachSpline20,myMachVector);

%%
ThrustVsMachSpline10 = pchip(Mach10,Thrust10);
myThrustVsMach10 = ppval(ThrustVsMachSpline10,myMachVector);

%%
ThrustVsMachSplineSL = pchip(MachSL,ThrustSL);
myThrustVsMachSL = ppval(ThrustVsMachSplineSL,myMachVector);

%%
ThrustVsMachSpline36 = pchip(Mach36,Thrust36);
myThrustVsMach36 = ppval(ThrustVsMachSpline36,myMachVector);

%%
ThrustVsMachSpline15 = pchip(Mach15,Thrust15);
myThrustVsMach15 = ppval(ThrustVsMachSpline15,myMachVector);

%% GRAFICA
figure(1)
plot(...
    myMachVector, myThrustVsMachSL, '.-k',...
    myMachVector, myThrustVsMach10, '.-r',...
    myMachVector, myThrustVsMach15, '.-c',...
    myMachVector, myThrustVsMach20, '.-y',...
    myMachVector, myThrustVsMach30, '.-r',...
    myMachVector, myThrustVsMach36, '.-b',...
    myMachVector, myThrustVsMach40, '.-g',...
    myMachVector, myThrustVsMach45, '.-m'...
    );

xlabel('Mach no.');   ylabel('Thrust ratio');
title('Bypass ratio 13.0 - maximum climb thrust.');
legend('0 ft','10000 ft','15000 ft','20000 ft','30000 ft','36000 ft','40000 ft','45000 ft');

%columns --> curves
myData = [ ...
    myThrustVsMachSL,...
	myThrustVsMach10,...
	myThrustVsMach15,...
	myThrustVsMach20,...
    myThrustVsMach30,...
	myThrustVsMach36,...
	myThrustVsMach40,...
	myThrustVsMach45 ...
    ];

