clc; close all; clear all;

myAltitudeVector_FT = [ ...
    0;10000;15000;25000;30000;36000;40000;45000 ...
    ];

%% BPR 3
load('BPR3_DescentThrustData.mat');
nPoints = 60;
machMax = 1.0;
myMachVector=transpose(linspace(0,machMax,nPoints));

%% 45k ft
% MachVsThrustRatioSpline45=pchip(Mach45,ThrustRatio45);
myThrustVsMach45=extendMach(machMax,Mach45,ThrustRatio45);

%% 40k ft
% MachVsThrustRatioSpline40=pchip(Mach40,ThrustRatio40);
myThrustVsMach40=extendMach(machMax,Mach40,ThrustRatio40);

%% 30k ft
% MachVsThrustRatioSpline30=pchip(Mach30,ThrustRatio30);
myThrustVsMach30=extendMach(machMax,Mach30,ThrustRatio30);

%% 20k ft
% MachVsThrustRatioSpline20=pchip(Mach20,ThrustRatio20);
myThrustVsMach20=extendMach(machMax,Mach20,ThrustRatio20);

%% 10k ft
% MachVsThrustRatioSpline10=pchip(Mach10,ThrustRatio10);
myThrustVsMach10=extendMach(machMax,Mach10,ThrustRatio10);

%% 0 ft
% MachVsThrustRatioSplineSL=pchip(MachSL,ThrustRatioSL);
myThrustVsMachSL=extendMach(machMax,MachSL,ThrustRatioSL);

thrustMatrix(1,:) = myThrustVsMachSL;
thrustMatrix(2,:) = myThrustVsMach10;
thrustMatrix(3,:) = myThrustVsMach20;
thrustMatrix(4,:) = myThrustVsMach30;
thrustMatrix(5,:) = myThrustVsMach40;
thrustMatrix(6,:) = myThrustVsMach45;

altitudes = [0,10,20,30,40,45]*1000;

%% 15k ft
myThrustVsMach15=interpTgivenM_h(myMachVector,thrustMatrix,altitudes,nPoints,machMax,15000,1,1.)';

%% 25k ft
myThrustVsMach25=interpTgivenM_h(myMachVector,thrustMatrix,altitudes,nPoints,machMax,25000,1,1.)';

%% 36k ft
myThrustVsMach36=interpTgivenM_h(myMachVector,thrustMatrix,altitudes,nPoints,machMax,36000,1,1.)';

%% GRAFICA
figure(1)
hold on;
plot ( ...
    myMachVector, myThrustVsMach45, '-b', ...
    myMachVector, myThrustVsMach40, '-r', ...
    myMachVector, myThrustVsMach36, '-c', ...
    myMachVector, myThrustVsMach30, '-y', ...
    myMachVector, myThrustVsMach25, '-k', ...
    myMachVector, myThrustVsMach15, '-g', ...
    myMachVector, myThrustVsMach10, '-b', ...
    myMachVector, myThrustVsMachSL, '-m' ...
    );
xlabel('Mach No'); ylabel('Thrust Ratio');
title('Bypass ratio 3.0 - descent thrust.');
legend('45000 ft','40000 ft','30000 ft','20000 ft','10000 ft','0 ft');

axis([0 1.0 -0.15 0.10]);

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
load('BPR65_DescentThrustData.mat');
nPoints = 60;
myMachVector = transpose(linspace(0, machMax, nPoints));

%% SL
% ThrustVsMachSplineDSL = pchip(MachDSL, ThrustDSL);
myThrustVsMachDSL = extendMach(machMax, MachDSL, ThrustDSL);

%% 10k ft.
% ThrustVsMachSplineD10 = pchip(MachD10, ThrustD10);
myThrustVsMachD10 = extendMach(machMax, MachD10, ThrustD10);

%% 15k ft.
% ThrustVsMachSplineD15 = pchip(MachD15, ThrustD15);
myThrustVsMachD15 = extendMach(machMax, MachD15, ThrustD15);

%% 25k ft.
% ThrustVsMachSplineD25 = pchip(MachD25, ThrustD25);
myThrustVsMachD25 = extendMach(machMax, MachD25, ThrustD25);

%% 30k ft
myThrustVsMachD30=(myThrustVsMach25*(1-0.5454) + myThrustVsMach36*0.5454)';

%% 36k ft.
% ThrustVsMachSplineD36 = pchip(MachD36, ThrustD36);
myThrustVsMachD36 = extendMach(machMax, MachD36, ThrustD36);

%% 40k ft.
% ThrustVsMachSplineD40 = pchip(MachD40, ThrustD40);
myThrustVsMachD40 = extendMach(machMax, MachD40, ThrustD40);

%% 45k ft. = 40k ft
% ThrustVsMachSplineD45 = pchip(MachD45, ThrustD45);
myThrustVsMachD45 = extendMach(machMax, MachD40, ThrustD40);

figure(2)
hold on;
plot ( ...
    myMachVector, myThrustVsMachDSL, '-m', ...
    myMachVector, myThrustVsMachD10, '-c', ...
    myMachVector, myThrustVsMachD15, '-b', ...
    myMachVector, myThrustVsMachD25, '-g', ...
    myMachVector, myThrustVsMachD36, '-y', ...
    myMachVector, myThrustVsMachD40, '-r' ...
    );
title('Bypass ratio 6.5 - Descend thrust.');
xlabel('Mach No'); ylabel('Thrust ratio (FN/FN*)');
legend('Sea Level', '10000 ft', '15000 ft', '25000 ft', '36000 ft', '40000 ft');
axis([0 1.0 -0.06 0.04]);


% columns --> curves
myData(2,:,:) = [ ...
    myThrustVsMachDSL, ...
    myThrustVsMachD10, ...
    myThrustVsMachD15, ...
    myThrustVsMachD25, ...
    myThrustVsMachD30', ...
    myThrustVsMachD36, ...
    myThrustVsMachD40, ...
    myThrustVsMachD45
    ]';


%% BPR 8
load('BPR8_DescentThrustData.mat');
nPoints = 60;
myMachVector = transpose(linspace(0, machMax, nPoints));

%% 0
% thrustVsMachSplineSL = pchip(MachSL,ThrustSL);
myThrustVsMachSL = extendMach(machMax, MachSL,ThrustSL);

%% 10k
% thrustVsMachSpline10 = pchip(Mach10,Thrust10);
myThrustVsMach10 = extendMach(machMax, Mach10,Thrust10);

%% 15k
% thrustVsMachSpline15 = pchip(Mach15,Thrust15);
myThrustVsMach15 = extendMach(machMax, Mach15,Thrust15);

%% 25k
% thrustVsMachSpline25 = pchip(Mach25,Thrust25);
myThrustVsMach25 = extendMach(machMax, Mach25,Thrust25);

%% 30k
% thrustVsMachSpline30 = pchip(Mach30,Thrust30);
myThrustVsMach30 = extendMach(machMax, Mach30,Thrust30);

%% 40k
% thrustVsMachSpline40 = pchip(Mach40,Thrust40);
myThrustVsMach40 = extendMach(machMax, Mach40,Thrust40);

%% 45k
% thrustVsMachSpline45 = pchip(Mach45,Thrust45);
myThrustVsMach45 = extendMach(machMax, Mach45,Thrust45);

%% 36k ft
thrustMatrix(1,:) = myThrustVsMachSL;
thrustMatrix(2,:) = myThrustVsMach10;
thrustMatrix(3,:) = myThrustVsMach15;
thrustMatrix(4,:) = myThrustVsMach25;
thrustMatrix(5,:) = myThrustVsMach30;
thrustMatrix(6,:) = myThrustVsMach40;
thrustMatrix(7,:) = myThrustVsMach45;

altitudes = [0,10,15,25,30,40,45]*1000;
myThrustVsMach36=interpTgivenM_h(myMachVector,thrustMatrix,altitudes,nPoints,machMax,36000,1,1.)';

%% GRAFICA

figure(3)
hold on
plot(...
    myMachVector, myThrustVsMachSL, '-k',...
    myMachVector, myThrustVsMach10, '-r',...
    myMachVector, myThrustVsMach15, '-c',...
    myMachVector, myThrustVsMach25, '-b',...
    myMachVector, myThrustVsMach30, '-y',...
    myMachVector, myThrustVsMach36,...
    myMachVector, myThrustVsMach40, '-g',...
    myMachVector, myThrustVsMach45, '-m'...
    );

xlabel('Mach no.');   ylabel('Thrust ratio');
title('Bypass ratio 8.0 - descent thrust.');
legend('0 ft','10000 ft','15000 ft','25000 ft','30000 ft','40000 ft','45000 ft');

%% preparing output to HDF

%columns --> curves
myData(3,:,:) = [ ...
    myThrustVsMachSL,...
    myThrustVsMach10,...
    myThrustVsMach15,...
    myThrustVsMach25,...
    myThrustVsMach30,...
    myThrustVsMach36,...
    myThrustVsMach40,...
    myThrustVsMach45 ...
    ]';


%% BPR 13
load('BPR13_DescentThrustData.mat');
nPoints = 60;
myMachVector = transpose(linspace(0, machMax, nPoints));

%% 0 ft
% ThrustVsMachSplineSL=pchip(MachSL,ThrustRatioSL);
myThrustVsMachSL=extendMach(machMax, MachSL,ThrustRatioSL);

%% 10k ft
% ThrustVsMachSpline10=pchip(Mach10,ThrustRatio10);
myThrustVsMach10=extendMach(machMax, Mach10,ThrustRatio10);

%% 15k ft
% ThrustVsMachSpline15=pchip(Mach15,ThrustRatio15);
myThrustVsMach15=extendMach(machMax, Mach15,ThrustRatio15);

%% 25k ft
% ThrustVsMachSpline25=pchip(Mach25,ThrustRatio25);
myThrustVsMach25=extendMach(machMax, Mach25,ThrustRatio25);

%% 30k ft
% ThrustVsMachSpline30=pchip(Mach30,ThrustRatio30);
myThrustVsMach30=extendMach(machMax, Mach30,ThrustRatio30);

%% 40k ft
% ThrustVsMachSpline40=pchip(Mach40,ThrustRatio40);
myThrustVsMach40=extendMach(machMax, Mach40,ThrustRatio40);

%% 45k ft
% ThrustVsMachSpline45=pchip(Mach45,ThrustRatio45);
myThrustVsMach45=extendMach(machMax, Mach45,ThrustRatio45);

%% 36k ft
thrustMatrix(1,:) = myThrustVsMachSL;
thrustMatrix(2,:) = myThrustVsMach10;
thrustMatrix(3,:) = myThrustVsMach15;
thrustMatrix(4,:) = myThrustVsMach25;
thrustMatrix(5,:) = myThrustVsMach30;
thrustMatrix(6,:) = myThrustVsMach40;
thrustMatrix(7,:) = myThrustVsMach45;

altitudes = [0,10,15,25,30,40,45]*1000;
myThrustVsMach36=interpTgivenM_h(myMachVector,thrustMatrix,altitudes,nPoints,machMax,36000,1,1.)';

%% GRAFICA
figure(4)
hold on
plot ( ...
    myMachVector, myThrustVsMach45, '-b', ...
    myMachVector, myThrustVsMach40, '-r', ...
    myMachVector, myThrustVsMach36, ...
    myMachVector, myThrustVsMach30, '-c', ...
    myMachVector, myThrustVsMach25, '-m', ...
    myMachVector, myThrustVsMach15, '-g', ...
    myMachVector, myThrustVsMach10, '-k', ...
    myMachVector, myThrustVsMachSL, '-b' ...
    );
xlabel('Mach No'); ylabel('Thrust Ratio');
title('Bypass ratio 13.0 - descent thrust.');
legend('45000 ft','40000 ft','30000 ft','25000 ft','15000 ft','10000 ft','0 ft');

axis([0 1.0 -0.08 0.04]);


%columns --> curves
myData(4,:,:) = [ ...
    myThrustVsMachSL,...
    myThrustVsMach10,...
    myThrustVsMach15,...
    myThrustVsMach25,...
    myThrustVsMach30,...
    myThrustVsMach36,...
    myThrustVsMach40,...
    myThrustVsMach45 ...
    ]';


%% WRITE EVERYTHING TO DATABASE
myHDFFile = matfile('TurbofanEngineDatabase.h5','Writable',true);

DescentThrust.data = myData;
DescentThrust.var_0 = [3.0,6.5,8.0,13.0];
DescentThrust.var_1 = myAltitudeVector_FT';
DescentThrust.var_2 = myMachVector';

myHDFFile.DescentThrust = DescentThrust;

