clc; clear all; close all;

figIdx = 1;

%% BPR 3
load('BPR3_MaxCruiseThrustData.mat');

myAltitudeVector_FT = [ ...
    0;10;30;40;45 ...
    ]*1000;

nPoints = 60;
machMax = 1.0;
myMachVector = transpose(linspace(0, machMax, nPoints));

%% 45k ft
% thrustVsMachSpline45 = pchip(MachNo45, ThrustRatio45);
myThrustVsMach45 = extendMach(machMax,MachNo45, ThrustRatio45);

%% 40k ft
% thrustVsMachSpline40 = pchip(MachNo40, ThrustRatio40);
myThrustVsMach40 = extendMach(machMax,MachNo40, ThrustRatio40);

%% 30k ft
% thrustVsMachSpline30 = pchip(MachNo30, ThrustRatio30);
myThrustVsMach30 = extendMach(machMax,MachNo30, ThrustRatio30);

%% 20k ft
% thrustVsMachSpline20 = pchip(MachNo20, ThrustRatio20);
myThrustVsMach20 = extendMach(machMax,MachNo20, ThrustRatio20);

%% 10k ft
% thrustVsMachSpline10 = pchip(MachNo10, ThrustRatio10);
myThrustVsMach10 = extendMach(machMax,MachNo10, ThrustRatio10);

%% SL
% thrustVsMachSplineSL = pchip(MachNoSL, ThrustRatioSL);
myThrustVsMachSL = extendMach(machMax,MachNoSL, ThrustRatioSL);

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
hold on
plot ( ...
    myMachVector, myThrustVsMach45, '-b', ...
    myMachVector, myThrustVsMach40, '-r', ...
    myMachVector, myThrustVsMach36, '-y', ...
    myMachVector, myThrustVsMach25, '-k', ...
    myMachVector, myThrustVsMach15, '-g', ...
    myMachVector, myThrustVsMachSL, '-m' ...
    );

xlabel('Mach No');  ylabel('Thrust Ratio');
title('Bypass ratio 3.0 - maximum cruise thrust.');
legend('45000 ft','40000 ft','30000 ft','20000 ft','10000 ft', '0 ft');
axis([0 max(MachNo45) 0 1.0]);

% columns --> curves
myData(1,:,:) = [ ...
    myThrustVsMachSL,...
    myThrustVsMach10,...
    myThrustVsMach30, ...
    myThrustVsMach40,...
    myThrustVsMach45 ...
    ]';


%% BPR 6.5

load('BPR65_MaxCruiseThrustData.mat');
nPoints = 60;
myMachVector = transpose(linspace(0, machMax, nPoints));

%% 45k
% thrustVsMachSpline45 = pchip(Mach45,Thrust45);
myThrustVsMach45 = extendMach(machMax,Mach45,Thrust45);

%% 40k
% thrustVsMachSpline40 = pchip(Mach40,Thrust40);
myThrustVsMach40 = extendMach(machMax,Mach40,Thrust40);

%% 36k
% thrustVsMachSpline36 = pchip(Mach36,Thrust36);
myThrustVsMach36 = extendMach(machMax,Mach36,Thrust36);

%% 30k
% thrustVsMachSpline30 = pchip(Mach30,Thrust30);
myThrustVsMach30 = extendMach(machMax,Mach30,Thrust30);

%% 25k
% thrustVsMachSpline25 = pchip(Mach25,Thrust25);
myThrustVsMach25 = extendMach(machMax,Mach25,Thrust25);

%% 15k
% thrustVsMachSpline15 = pchip(Mach15,Thrust15);
myThrustVsMach15 = extendMach(machMax,Mach15,Thrust15);

%% 10k
% thrustVsMachSpline10 = pchip(Mach10,Thrust10);
myThrustVsMach10 = extendMach(machMax,Mach10,Thrust10);

%% 0
% thrustVsMachSplineSL = pchip(MachSL,ThrustSL);
myThrustVsMachSL = extendMach(machMax,MachSL,ThrustSL);


%% GRAFICA
figure(figIdx)
figIdx=figIdx + 1;
hold on
plot(...
    myMachVector, myThrustVsMachSL, '-k',...
    myMachVector, myThrustVsMach10, '-r',...
    myMachVector, myThrustVsMach15, '-c',...
    myMachVector, myThrustVsMach25, '-b',...
    myMachVector, myThrustVsMach30, '-b',...
    myMachVector, myThrustVsMach36, '-b',...
    myMachVector, myThrustVsMach40, '-g',...
    myMachVector, myThrustVsMach45, '-m'...
    );

xlabel('Mach no.');   ylabel('Thrust ratio');
title('Bypass ratio 6.5 - maximum climb thrust.');
legend('0 ft','10000 ft','15000 ft','25000 ft','30000 ft','36000 ft','40000 ft','45000 ft');

%columns --> curves
myData(2,:,:) = [ ...
    myThrustVsMachSL,...
    myThrustVsMach10,...
    myThrustVsMach30,...
    myThrustVsMach40, ...
    myThrustVsMach45...
    ]';


%% BPR 8

load('BPR8_MaximumCruiseThrustData');
nPoints = 60;
myMachVector = transpose(linspace(0, machMax, nPoints));

%% 0 ft
% ThrustVsMachSplineSL=pchip(MachSL,ThrustRatioSL);
myThrustVsMachSL=extendMach(machMax,MachSL,ThrustRatioSL);

%% 10k ft
% ThrustVsMachSpline10=pchip(Mach10,ThrustRatio10);
myThrustVsMach10=extendMach(machMax,Mach10,ThrustRatio10);

%% 15k ft
% ThrustVsMachSpline15=pchip(Mach15,ThrustRatio15);
myThrustVsMach15=extendMach(machMax,Mach15,ThrustRatio15);

%% 25k ft
% ThrustVsMachSpline25=pchip(Mach25,ThrustRatio25);
myThrustVsMach25=extendMach(machMax,Mach25,ThrustRatio25);

%% 30k ft
% ThrustVsMachSpline30=pchip(Mach30,ThrustRatio30);
myThrustVsMach30=extendMach(machMax,Mach30,ThrustRatio30);

%% 36k ft
% ThrustVsMachSpline36=pchip(Mach36,ThrustRatio36);
myThrustVsMach36=extendMach(machMax,Mach36,ThrustRatio36);

%% 40k ft
% ThrustVsMachSpline40=pchip(Mach40,ThrustRatio40);
myThrustVsMach40=extendMach(machMax,Mach40,ThrustRatio40);

%% 45k ft
% ThrustVsMachSpline45=pchip(Mach45,ThrustRatio45);
myThrustVsMach45=extendMach(machMax,Mach45,ThrustRatio45);

%% GRAFICA
figure(figIdx)
figIdx=figIdx + 1;
hold on
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
title('Bypass ratio 8.0 - maximum cruise thrust.');
legend('45000 ft','40000 ft','36000 ft','30000 ft','25000 ft','15000 ft','10000 ft','0 ft');

axis([0 0.97 0 0.8]);

%columns --> curves
myData(3,:,:) = [ ...
    myThrustVsMachSL,...
    myThrustVsMach10,...
    myThrustVsMach30,...
    myThrustVsMach40,...
    myThrustVsMach45 ...
    ]';


%% BPR 13

load('BPR13_MaxCruiseThrustData.mat');
nPoints = 60;
myMachVector = transpose(linspace(0, machMax, nPoints));

%% 45k ft
% thrustVsMachSpline1345 = pchip(Mach1345, Thrust1345);
myThrustVsMach1345 = extendMach(machMax,Mach1345, Thrust1345);

%% 40k ft
% thrustVsMachSpline1340 = pchip(Mach1340, Thrust1340);
myThrustVsMach1340 = extendMach(machMax,Mach1340, Thrust1340);

%% 30k ft
% thrustVsMachSpline1330 = pchip(Mach1330, Thrust1330);
myThrustVsMach1330 = extendMach(machMax,Mach1330, Thrust1330);

%% 25k ft
% thrustVsMachSpline1325 = pchip(Mach1325, Thrust1325);
myThrustVsMach1325 = extendMach(machMax,Mach1325, Thrust1325);

%% 15k ft
% thrustVsMachSpline1315 = pchip(Mach1315, Thrust1315);
myThrustVsMach1315 = extendMach(machMax,Mach1315, Thrust1315);

%% 10k ft
% thrustVsMachSpline1310 = pchip(Mach1310, Thrust1310);
myThrustVsMach1310 = extendMach(machMax,Mach1310, Thrust1310);

%% SL
% thrustVsMachSpline13SL = pchip(Mach13SL, Thrust13SL);
myThrustVsMach13SL = extendMach(machMax,Mach13SL, Thrust13SL);

%% 36k ft

thrustMatrix(1,:) = myThrustVsMach13SL;
thrustMatrix(2,:) = myThrustVsMach1310;
thrustMatrix(3,:) = myThrustVsMach1315;
thrustMatrix(4,:) = myThrustVsMach1325;
thrustMatrix(5,:) = myThrustVsMach1330;
thrustMatrix(6,:) = myThrustVsMach1340;
thrustMatrix(7,:) = myThrustVsMach1345;

altitudes = [0,10,15,25,30,40,45]*1000;

myThrustVsMach1336 = interpTgivenM_h(myMachVector,thrustMatrix,altitudes,nPoints,machMax,36000,1,1.)';

%% GRAFICA
figure(figIdx)
figIdx=figIdx + 1;
hold on
plot ( ...
    myMachVector, myThrustVsMach1345, '-b', ...
    myMachVector, myThrustVsMach1340, '-r', ...
    myMachVector, myThrustVsMach1336, '-y', ...
    myMachVector, myThrustVsMach1330, '-y', ...
    myMachVector, myThrustVsMach1325, '-k', ...
    myMachVector, myThrustVsMach1315, '-c', ...
    myMachVector, myThrustVsMach1310, '-g', ...
    myMachVector, myThrustVsMach13SL, '-m' ...
    );
xlabel('Mach No');  ylabel('Thrust Ratio');
title('Bypass ratio 13.0 - maximum cruise thrust.');
legend('45000 ft','40000 ft','30000 ft','25000 ft','15000 ft','10000 ft', '0 ft');
axis([0 1.0 0 0.9]);

% columns --> curves
myData(4,:,:) = [ ...
    myThrustVsMach13SL,...
    myThrustVsMach1310,...
    myThrustVsMach1330, ...
    myThrustVsMach1340,...
    myThrustVsMach1345 ...
    ]';


%% WRITE EVERYTHING TO DATABASE
myHDFFile = matfile('TurbofanEngineDatabase.h5','Writable',true);

MaximumCruiseThrust.data = myData;
MaximumCruiseThrust.var_0 = [3.0,6.5,8.0,13.0];
MaximumCruiseThrust.var_1 = myAltitudeVector_FT';
MaximumCruiseThrust.var_2 = myMachVector';

myHDFFile.MaximumCruiseThrust = MaximumCruiseThrust;

