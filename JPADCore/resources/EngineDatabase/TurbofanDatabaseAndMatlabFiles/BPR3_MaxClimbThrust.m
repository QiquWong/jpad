clc; clear all; close all;

load('BPR3_MaxClimbThrustData.mat');

nPoints = 30;

myMachVector = transpose(linspace(0, 0.888, nPoints));
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
myData = [ ...
    myThrustVsMachSL,myThrustVsMach10,myThrustVsMach20,...
    myThrustVsMach30,myThrustVsMach40,myThrustVsMach45 ...
    ];

hdfFileName = 'BPR3_MaxClimbThrust_Mach_Altitude.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR3_MaxClimbThrust_Mach_Altitude/data', size(myData'));
h5write(hdfFileName, '/BPR3_MaxClimbThrust_Mach_Altitude/data', myData');

h5create(hdfFileName, '/BPR3_MaxClimbThrust_Mach_Altitude/var_0', size(myAltitudeVector_FT'));
h5write(hdfFileName, '/BPR3_MaxClimbThrust_Mach_Altitude/var_0', myAltitudeVector_FT');

h5create(hdfFileName, '/BPR3_MaxClimbThrust_Mach_Altitude/var_1', size(myMachVector'));
h5write(hdfFileName, '/BPR3_MaxClimbThrust_Mach_Altitude/var_1', myMachVector');
