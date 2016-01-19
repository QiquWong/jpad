clear all; close all; clc

nPoints = 60;
machMax = 1.0;
myAltitudeVector_FT = [ ...
    0;10000;15000;25000;30000;36000;40000;45000 ...
    ];


%% BPR 3
load('BPR3_DescentFuelFlowData.mat');
myMachVector = transpose(linspace(0, machMax, nPoints));

%% 45k ft
% fflowVsMachSpline45 = pchip([Mach45,Mach45(end)*ones(10)], [FFlow45,FFlow45(end)*ones(10)]);
% myfflowVsMach45 = ppval(fflowVsMachSpline45, myMachVector);
myfflowVsMach45 = extendMach(machMax,Mach45,FFlow45);

%% 40k ft
% fflowVsMachSpline40 = pchip(Mach40, FFlow40);
myfflowVsMach40 = extendMach(machMax,Mach40,FFlow40);

%% 30k ft
% fflowVsMachSpline30 = pchip(Mach30, FFlow30);
myfflowVsMach30 = extendMach(machMax,Mach30,FFlow30);

%% 20k ft
% fflowVsMachSpline20 = pchip(Mach20, FFlow20);
myfflowVsMach20 = extendMach(machMax,Mach20,FFlow20);

%% SL
% fflowVsMachSplineSL = pchip(MachSL, FFlowSL);
myfflowVsMachSL = extendMach(machMax,MachSL,FFlowSL);

fflowMatrix(1,:) = myfflowVsMachSL;
fflowMatrix(2,:) = myfflowVsMach20;
fflowMatrix(3,:) = myfflowVsMach30;
fflowMatrix(4,:) = myfflowVsMach40;
fflowMatrix(5,:) = myfflowVsMach45;

altitudes = [0,20,30,40,45]*1000;

%% 36k ft
myfflowVsMach36 = interpTgivenM_h(myMachVector,fflowMatrix,altitudes,nPoints,machMax,36000,1,1.)';

%% 25k ft
myfflowVsMach25 = interpTgivenM_h(myMachVector,fflowMatrix,altitudes,nPoints,machMax,25000,1,1.)';

%% 15k ft
myfflowVsMach15 = interpTgivenM_h(myMachVector,fflowMatrix,altitudes,nPoints,machMax,15000,1,1.)';

%% 10k ft
myfflowVsMach10 = interpTgivenM_h(myMachVector,fflowMatrix,altitudes,nPoints,machMax,10000,1,1.)';

%% GRAFICA
figure(1)
plot ( ...
    myMachVector, myfflowVsMach45, '-b', ...
    myMachVector, myfflowVsMach40, '-r', ...
    myMachVector, myfflowVsMach36, '-c', ...
    myMachVector, myfflowVsMach30, '-y', ...
    myMachVector, myfflowVsMach25, '-k', ...
    myMachVector, myfflowVsMach15, '-g', ...
    myMachVector, myfflowVsMach10, '-b', ...
    myMachVector, myfflowVsMachSL, '-m' ...
    );
xlabel('Mach No'); ylabel('Fuel Flow');
title('Bypass ratio 3.0 - descent fuel flow.');
legend('45000 ft','40000 ft','30000 ft','20000 ft', '0 ft');

axis([0 1.0 0.02 0.06]);

% columns --> curves
myData(1,:,:) = [ ...
    myfflowVsMachSL, ...
    myfflowVsMach10, ...
    myfflowVsMach15, ...
    myfflowVsMach25, ...
    myfflowVsMach30, ...
    myfflowVsMach36, ...
    myfflowVsMach40, ...
    myfflowVsMach45 ...
    ]';

%% BPR 6.5

load('BPR65_DescentFuelFlowData.mat');
myMachVector = transpose(linspace(0, machMax, nPoints));
% 
% Mach45 = [0.0 0.0262569 0.0651878 0.115405 0.16311 0.24597 0.337613 0.430512 0.540951 0.663924 0.778122 0.855909];
% FuelFlow45 = [];
% 
% Mach40 = [0.0 0.0900566 0.161639 0.252051 0.37257 0.441608 0.562085 0.662478 0.761598 0.854406];
% FuelFlow40 = [];
% scale = (0.005/0.05);
FuelFlow45 = -FuelFlow45 + 2*FuelFlow45(end);
FuelFlow40 = -FuelFlow40 + 2*FuelFlow40(end);
FuelFlow36 = -FuelFlow36 + 2*FuelFlow36(end);
FuelFlow30 = -FuelFlow30 + 2*FuelFlow30(end);
FuelFlow25 = -FuelFlow25 + 2*FuelFlow25(end);
FuelFlow15 = -FuelFlow15 + 2*FuelFlow15(end);
FuelFlow10 = -FuelFlow10 + 2*FuelFlow10(end);
FuelFlowSL = -FuelFlowSL + 2*FuelFlowSL(end);

FuelFlow45 = FuelFlow45 - FuelFlow45(end) + 0.005463;
FuelFlow40 = FuelFlow40 - FuelFlow40(end) + 0.006407;
FuelFlow36 = FuelFlow36 - FuelFlow36(end) + 0.007158;
FuelFlow30 = FuelFlow30 - FuelFlow30(end) + 0.008556;
FuelFlow25 = FuelFlow25 - FuelFlow25(end) + 0.009646;
FuelFlow15 = FuelFlow15 - FuelFlow15(end) + 0.01231;
FuelFlow10 = FuelFlow10 - FuelFlow10(end) + 0.01349;
FuelFlowSL = FuelFlowSL - FuelFlowSL(end) + 0.01573;

% FuelFlow45 = fliplr(FuelFlow45);
% FuelFlow40 = fliplr(FuelFlow40);
% FuelFlow36 = fliplr(FuelFlow36);
% FuelFlow30 = fliplr(FuelFlow30);
% FuelFlow25 = fliplr(FuelFlow25);
% FuelFlow15 = fliplr(FuelFlow15);
% FuelFlow10 = fliplr(FuelFlow10);
% FuelFlowSL = fliplr(FuelFlowSL);


%% 45k
% FuelFlowVsMachSpline45 = pchip(Mach45,FuelFlow45);
myFuelFlowVsMach45 = extendMach(machMax,Mach45,FuelFlow45);

%% 40k
% FuelFlowVsMachSpline40 = pchip(Mach40,FuelFlow40);
myFuelFlowVsMach40 = extendMach(machMax,Mach40,FuelFlow40);

%% 36k
% FuelFlowVsMachSpline36 = pchip(Mach36,FuelFlow36);
myFuelFlowVsMach36 = extendMach(machMax,Mach36,FuelFlow36);

%% 30k
% FuelFlowVsMachSpline30 = pchip(Mach30,FuelFlow30);
myFuelFlowVsMach30 = extendMach(machMax,Mach30,FuelFlow30);

%% 25k
% FuelFlowVsMachSpline25 = pchip(Mach25,FuelFlow25);
myFuelFlowVsMach25 = extendMach(machMax,Mach25,FuelFlow25);

%% 15k
% FuelFlowVsMachSpline15 = pchip(Mach15,FuelFlow15);
myFuelFlowVsMach15 = extendMach(machMax,Mach15,FuelFlow15);

%% 10 k
% FuelFlowVsMachSpline10 = pchip(Mach10,FuelFlow10);
myFuelFlowVsMach10 = extendMach(machMax,Mach10,FuelFlow10);

%% 0
% FuelFlowVsMachSplineSL = pchip(MachSL,FuelFlowSL);
myFuelFlowVsMachSL = extendMach(machMax,MachSL,FuelFlowSL);


%% GRAFICA

figure(2)
plot(...
    myMachVector, myFuelFlowVsMachSL, '-k',...
    myMachVector, myFuelFlowVsMach10, '-r',...
    myMachVector, myFuelFlowVsMach15, '-c',...
    myMachVector, myFuelFlowVsMach25, '-b',...
    myMachVector, myFuelFlowVsMach30, '-g',...
    myMachVector, myFuelFlowVsMach36, '-y',...
    myMachVector, myFuelFlowVsMach40, '-r',...
    myMachVector, myFuelFlowVsMach45, '-m'...
    );

xlabel('Mach no.');   ylabel('Fuel Flow');
% axis([0 1.0 0.0 0.02]);
% set(gca,'YDir','Reverse');
title('Bypass ratio 6.5 - descent fuel flow.');
legend('0 ft','10000 ft','15000 ft','25000 ft','30000 ft','36000 ft','40000 ft', '45000 ft');


%columns --> curves
myData(2,:,:) = [ ...
    myFuelFlowVsMachSL,...
    myFuelFlowVsMach10,...
    myFuelFlowVsMach15,...
    myFuelFlowVsMach25,...
    myFuelFlowVsMach30,...
    myFuelFlowVsMach36,...
    myFuelFlowVsMach40,...
    myFuelFlowVsMach45...
    ]';


%% BPR 8

load('BPR8_DescentFuelFlowData.mat');
nPoints = 60;
myMachVector = transpose(linspace(0, machMax, nPoints));

%% 0 ft
% MachVsFuelFlowSplineSL=pchip(MachSL,FuelFlowSL);
myMachVsFuelFlowSL=extendMach(machMax,MachSL,FuelFlowSL);

%% 10k ft
% MachVsFuelFlowSpline10=pchip(Mach10,FuelFlow10);
myMachVsFuelFlow10=extendMach(machMax,Mach10,FuelFlow10);

%% 15k ft
% MachVsFuelFlowSpline15=pchip(Mach15,FuelFlow15);
myMachVsFuelFlow15=extendMach(machMax,Mach15,FuelFlow15);

%% 25k ft
% MachVsFuelFlowSpline25=pchip(Mach25,FuelFlow25);
myMachVsFuelFlow25=extendMach(machMax,Mach25,FuelFlow25);

%% 30k ft
% MachVsFuelFlowSpline30=pchip(Mach30,FuelFlow30);
myMachVsFuelFlow30=extendMach(machMax,Mach30,FuelFlow30);

%% 36k ft
% MachVsFuelFlowSpline36=pchip(Mach36,FuelFlow36);
myMachVsFuelFlow36=extendMach(machMax,Mach36,FuelFlow36);

%% 40k ft
% MachVsFuelFlowSpline40=pchip(Mach40,FuelFlow40);
myMachVsFuelFlow40=extendMach(machMax,Mach40,FuelFlow40);

%% 45k ft
% MachVsFuelFlowSpline45=pchip(Mach45,FuelFlow45);
myMachVsFuelFlow45=extendMach(machMax,Mach45,FuelFlow45);

%% GRAFICA
figure(3)
plot ( ...
    myMachVector, myMachVsFuelFlow45, '-b', ...
    myMachVector, myMachVsFuelFlow40, '-r', ...
    myMachVector, myMachVsFuelFlow36, '-m', ...
    myMachVector, myMachVsFuelFlow30, '-c', ...
    myMachVector, myMachVsFuelFlow25, '-y', ...
    myMachVector, myMachVsFuelFlow15, '-g', ...
    myMachVector, myMachVsFuelFlow10, '-k', ...
    myMachVector, myMachVsFuelFlowSL, '-b' ...
    );
xlabel('Mach No'); ylabel('Fuel Flow');
title('Bypass ratio 8 - descent fuel flow.');
legend('45000 ft','40000 ft','36000 ft','30000 ft','25000 ft','15000 ft','10000 ft','0 ft');

axis([0 1.0 0 0.02]);

%columns --> curves
myData(3,:,:) = [ ...
    myMachVsFuelFlowSL,...
    myMachVsFuelFlow10,...
    myMachVsFuelFlow15,...
    myMachVsFuelFlow25,...
    myMachVsFuelFlow30,...
    myMachVsFuelFlow36,...
    myMachVsFuelFlow40,...
    myMachVsFuelFlow45 ...
    ]';

%% BPR 13

load('BPR13_DescentFuelFlowData.mat');
myMachVector = transpose(linspace(0, machMax, nPoints));

%% 45k ft
% fflowVsMachFSpline45 = pchip(MachF45, FF45);
myfflowVsMachF45 = extendMach(machMax,MachF45, FF45);

%% 40k ft
% fflowVsMachFSpline40 = pchip(MachF40, FF40);
myfflowVsMachF40 = extendMach(machMax,MachF40, FF40);

%% 36k ft
% fflowVsMachFSpline36 = pchip(MachF36, FF36);
myfflowVsMachF36 = extendMach(machMax,MachF36, FF36);

%% 30k ft
% fflowVsMachFSpline30 = pchip(MachF30, FF30);
myfflowVsMachF30 = extendMach(machMax,MachF30, FF30);

%% 25k ft
% fflowVsMachFSpline25 = pchip(MachF25, FF25);
myfflowVsMachF25 = extendMach(machMax,MachF25, FF25);

%% 15k ft
% fflowVsMachFSpline15 = pchip(MachF15, FF15);
myfflowVsMachF15 = extendMach(machMax,MachF15, FF15);

%% 10k ft
% fflowVsMachFSpline10 = pchip(MachF10, FF10);
myfflowVsMachF10 = extendMach(machMax,MachF10, FF10);

%% SL
% fflowVsMachFSplineSL = pchip(MachFSL, FFSL);
myfflowVsMachFSL = extendMach(machMax,MachFSL, FFSL);

%% GRAFICA
figure(4)
plot ( ...
    myMachVector, myfflowVsMachF45, '-b', ...
    myMachVector, myfflowVsMachF40, '-r', ...
    myMachVector, myfflowVsMachF36, '-c', ...
    myMachVector, myfflowVsMachF30, '-y', ...
    myMachVector, myfflowVsMachF25, '-k', ...
    myMachVector, myfflowVsMachF15, '-g', ...
    myMachVector, myfflowVsMachF10, '-b', ...
    myMachVector, myfflowVsMachFSL, '-m' ...
    );
xlabel('Mach No'); ylabel('Fuel Flow');
title('Bypass ratio 13.0 - descent fuel flow.');
legend('45000 ft','40000 ft','36000 ft','30000 ft','25000 ft','15000 ft', '10000 ft','0 ft');

axis([0 1.0 0.003 0.016]);

% columns --> curves
myData(4,:,:) = [ ...
    myfflowVsMachFSL,...
    myfflowVsMachF10,...
    myfflowVsMachF15,...
    myfflowVsMachF25, ...
    myfflowVsMachF30,...
    myfflowVsMachF36,...
    myfflowVsMachF40,...
    myfflowVsMachF45 ...
    ]';


%% WRITE EVERYTHING TO DATABASE
myHDFFile = matfile('TurbofanEngineDatabase.h5','Writable',true);

DescentFuelFlow.data = myData;
DescentFuelFlow.var_0 = [3.0,6.5,8.0,13.0];
DescentFuelFlow.var_1 = myAltitudeVector_FT';
DescentFuelFlow.var_2 = myMachVector';

myHDFFile.DescentFuelFlow = DescentFuelFlow;

