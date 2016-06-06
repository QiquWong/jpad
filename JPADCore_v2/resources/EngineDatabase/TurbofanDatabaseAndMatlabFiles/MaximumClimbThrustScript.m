clc; clear all; close all;
figIdx = 1;

%% BPR 3
load('BPR3_MaxClimbThrustData.mat');
nPoints = 60;
machMax = 1.0;
myMachVector = transpose(linspace(0, machMax, nPoints));
myAltitudeVector_FT = [ ...
    0;10000;15000;25000;30000;36000;40000;45000 ...
    ];

%% 45k
% thrustVsMachSpline45 = pchip(Mach45,Thrust45);
myThrustVsMach45 = extendMach(machMax,Mach45,Thrust45);

%% 40k
% thrustVsMachSpline40 = pchip(Mach40,Thrust40);
myThrustVsMach40 = extendMach(machMax,Mach40,Thrust40);

%% 30k
% thrustVsMachSpline30 = pchip(Mach30,Thrust30);
myThrustVsMach30 = extendMach(machMax,Mach30,Thrust30);

%% 20k
% thrustVsMachSpline20 = pchip(Mach20,Thrust20);
myThrustVsMach20 = extendMach(machMax,Mach20,Thrust20);

%% 10k
% thrustVsMachSpline10 = pchip(Mach10,Thrust10);
myThrustVsMach10 = extendMach(machMax,Mach10,Thrust10);

%% 0k
% thrustVsMachSplineSL = pchip(MachSL,ThrustSL);
myThrustVsMachSL = extendMach(machMax,MachSL,ThrustSL);

%% 36k
thrustMatrix(1,:) = myThrustVsMachSL;
thrustMatrix(2,:) = myThrustVsMach10;
thrustMatrix(3,:) = myThrustVsMach20;
thrustMatrix(4,:) = myThrustVsMach30;
thrustMatrix(5,:) = myThrustVsMach40;
thrustMatrix(6,:) = myThrustVsMach45;

altitudes = [0,10,20,30,40,45]*1000;

%% 36k ft
myThrustVsMach36 = interpTgivenM_h(myMachVector,thrustMatrix,altitudes,nPoints,machMax,36000,1,1.)';

%% 25k ft
myThrustVsMach25 = interpTgivenM_h(myMachVector,thrustMatrix,altitudes,nPoints,machMax,25000,1,1.)';

%% 15k ft
myThrustVsMach15 = interpTgivenM_h(myMachVector,thrustMatrix,altitudes,nPoints,machMax,15000,1,1.)';


%% GRAFICA
figure(figIdx)
figIdx=figIdx + 1;
plot(...
    myMachVector, myThrustVsMachSL, '-k',...
    myMachVector, myThrustVsMach10, '-r',...
    myMachVector, myThrustVsMach20, '-c',...
    myMachVector, myThrustVsMach30, '-b',...
    myMachVector, myThrustVsMach40, '-g',...
    myMachVector, myThrustVsMach45, '-m'...
    );

xlabel('Mach no.');   ylabel('Thrust ratio');
title('Bypass ratio 3.0 - maximum climb thrust.');
legend('0 ft','10000 ft','20000 ft','30000 ft','40000 ft','45000 ft');

%columns --> curves
myData(1,:,:) = [ ...
    myThrustVsMachSL,...
    myThrustVsMach10,...
    myThrustVsMach15,...
    myThrustVsMach25,...
    myThrustVsMach30,...
    myThrustVsMach36,...
    myThrustVsMach40,...
    myThrustVsMach45 ...
    ]';


%% BPR 6.5
load('BPR65_MaximumClimbThrustData.mat');
nPoints = 60;
myMachVector = transpose(linspace(0, 1, nPoints));

%% 0 ft
% MachVsThrustRatioSplineSL=pchip(MachSL,ThrustRatioSL);
myThrustVsMachSL=extendMach(machMax,MachSL,ThrustRatioSL);

%% 10k ft
% MachVsThrustRatioSpline10=pchip(Mach10,ThrustRatio10);
myThrustVsMach10=extendMach(machMax,Mach10,ThrustRatio10);

%% 15k ft
% MachVsThrustRatioSpline15=pchip(Mach15,ThrustRatio15);
myThrustVsMach15=extendMach(machMax,Mach15,ThrustRatio15);

%% 25k ft
% MachVsThrustRatioSpline25=pchip(Mach25,ThrustRatio25);
myThrustVsMach25=extendMach(machMax,Mach25,ThrustRatio25);

%% 30k ft
% MachVsThrustRatioSpline30=pchip(Mach30,ThrustRatio30);
myThrustVsMach30=extendMach(machMax,Mach30,ThrustRatio30);

%% 36k ft
% MachVsThrustRatioSpline36=pchip(Mach36,ThrustRatio36);
myThrustVsMach36=extendMach(machMax,Mach36,ThrustRatio36);

%% 40k ft
% MachVsThrustRatioSpline40=pchip(Mach40,ThrustRatio40);
myThrustVsMach40=extendMach(machMax,Mach40,ThrustRatio40);

%% 45k ft
% MachVsThrustRatioSpline45=pchip(Mach45,ThrustRatio45);
myThrustVsMach45=extendMach(machMax,Mach45,ThrustRatio45);

%% GRAFICA
figure(figIdx)
figIdx=figIdx + 1;
plot ( ...
    myMachVector, myThrustVsMach45, '-b', ...
    myMachVector, myThrustVsMach40, '-r', ...
    myMachVector, myThrustVsMach36, '-m', ...
    myMachVector, myThrustVsMach30, '-c', ...
    myMachVector, myThrustVsMach25, '-y', ...
    myMachVector, myThrustVsMach15, '-g', ...
    myMachVector, myThrustVsMach10, '-k', ...
    myMachVector, myThrustVsMachSL, '-b' ...
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
    myThrustVsMach30...
    myThrustVsMach36,...
    myThrustVsMach40,...
    myThrustVsMach45 ...
    ]';


%% BPR 8.0
load('BPR8_MaxClimbThrustData.mat');
nPoints = 60;
myMachVector = transpose(linspace(0, machMax, nPoints));

%% 45k ft
% thrustVsMachMCSpline45 = pchip(MachMC45, ThrustMC45);
myThrustVsMachMC45 = extendMach(machMax,MachMC45, ThrustMC45);

%% 40k ft
% thrustVsMachSplineMC40 = pchip(MachMC40, ThrustMC40);
myThrustVsMachMC40 = extendMach(machMax,MachMC40, ThrustMC40);

%% 36k ft
% thrustVsMachSplineMC36 = pchip(MachMC36, ThrustMC36);
myThrustVsMachMC36 = extendMach(machMax,MachMC36, ThrustMC36);

%% 30k ft
% thrustVsMachSplineMC30 = pchip(MachMC30, ThrustMC30);
myThrustVsMachMC30 = extendMach(machMax,MachMC30, ThrustMC30);

%% 25k ft
% thrustVsMachMCSpline25 = pchip(MachMC25, ThrustMC25);
myThrustVsMachMC25 = extendMach(machMax,MachMC25, ThrustMC25);

%% 15k ft
% thrustVsMachMCSpline15 = pchip(MachMC15, ThrustMC15);
myThrustVsMachMC15 = extendMach(machMax,MachMC15, ThrustMC15);

%% 10k ft
% thrustVsMachMCSpline10 = pchip(MachMC10, ThrustMC10);
myThrustVsMachMC10 = extendMach(machMax,MachMC10, ThrustMC10);

%% SL
% thrustVsMachMCSplineSL = pchip(MachMCSL, ThrustMCSL);
myThrustVsMachMCSL = extendMach(machMax,MachMCSL, ThrustMCSL);

%% GRAFICA
figure(figIdx)
figIdx=figIdx + 1;
plot ( ...
    myMachVector, myThrustVsMachMC45, '-b', ...
    myMachVector, myThrustVsMachMC40, '-r', ...
    myMachVector, myThrustVsMachMC36, '-c', ...
    myMachVector, myThrustVsMachMC30, '-y', ...
    myMachVector, myThrustVsMachMC25, '-k', ...
    myMachVector, myThrustVsMachMC15, '-b', ...
    myMachVector, myThrustVsMachMC10, '-g', ...
    myMachVector, myThrustVsMachMCSL, '-m'  ...
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
    myThrustVsMachMC40,...
    myThrustVsMachMC45 ...
    ]';


%% BPR 13
load('BPR13_MaxClimbThrustData.mat');
nPoints = 60;
myMachVector = transpose(linspace(0, machMax, nPoints));

%%
% ThrustVsMachSpline45 = pchip(Mach45,Thrust45);
myThrustVsMach45 = extendMach(machMax,Mach45,Thrust45);

%%
% ThrustVsMachSpline40 = pchip(Mach40,Thrust40);
myThrustVsMach40 = extendMach(machMax,Mach40,Thrust40);

%%
% ThrustVsMachSpline30 = pchip(Mach30,Thrust30);
myThrustVsMach30 = extendMach(machMax,Mach30,Thrust30);

%%
% ThrustVsMachSpline20 = pchip(Mach20,Thrust20);
myThrustVsMach20 = extendMach(machMax,Mach20,Thrust20);

%%
% ThrustVsMachSpline10 = pchip(Mach10,Thrust10);
myThrustVsMach10 = extendMach(machMax,Mach10,Thrust10);

%%
% ThrustVsMachSplineSL = pchip(MachSL,ThrustSL);
myThrustVsMachSL = extendMach(machMax,MachSL,ThrustSL);

%%
% ThrustVsMachSpline36 = pchip(Mach36,Thrust36);
myThrustVsMach36 = extendMach(machMax,Mach36,Thrust36);

%%
% ThrustVsMachSpline15 = pchip(Mach15,Thrust15);
myThrustVsMach15 = extendMach(machMax,Mach15,Thrust15);

%% GRAFICA
figure(figIdx)
figIdx=figIdx + 1;
plot(...
    myMachVector, myThrustVsMachSL, '-k',...
    myMachVector, myThrustVsMach10, '-r',...
    myMachVector, myThrustVsMach15, '-c',...
    myMachVector, myThrustVsMach20, '-y',...
    myMachVector, myThrustVsMach30, '-r',...
    myMachVector, myThrustVsMach36, '-b',...
    myMachVector, myThrustVsMach40, '-g',...
    myMachVector, myThrustVsMach45, '-m'...
    );

xlabel('Mach no.');   ylabel('Thrust ratio');
title('Bypass ratio 13.0 - maximum climb thrust.');
legend('0 ft','10000 ft','15000 ft','20000 ft','30000 ft','36000 ft','40000 ft','45000 ft');

%columns --> curves
myData(4,:,:) = [ ...
    myThrustVsMachSL,...
    myThrustVsMach10,...
    myThrustVsMach15,...
    myThrustVsMach20,...
    myThrustVsMach30,...
    myThrustVsMach36,...
    myThrustVsMach40,...
    myThrustVsMach45 ...
    ]';

%% WRITE EVERYTHING TO DATABASE
myHDFFile = matfile('TurbofanEngineDatabase.h5','Writable',true);

MaximumClimbThrust.data = myData;
MaximumClimbThrust.var_0 = [3.0,6.5,8.0,13.0];
MaximumClimbThrust.var_1 = myAltitudeVector_FT';
MaximumClimbThrust.var_2 = myMachVector';

myHDFFile.MaximumClimbThrust = MaximumClimbThrust;

