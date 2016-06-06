clc; close all; clear all;

load('BPR13_TakeOffThrustData.mat');

nPoints = 30;
myMachVector = transpose(linspace(0, 0.48, nPoints));

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

%% preparing output to HDF

%increasing altitudes
myAltitudeVector_FT = [ ...
    0;5000;10000; ...
    ];

%columns --> curves
myData = [ ...
    myThrustVsMachSL,myThrustVsMach5,myThrustVsMach10 ...
    ];

hdfFileName = 'BPR13_TakeOffThrust_Mach_Altitude.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR13_TakeOffThrust_Mach_Altitude/data', size(myData'));
h5write(hdfFileName, '/BPR13_TakeOffThrust_Mach_Altitude/data', myData');

h5create(hdfFileName, '/BPR13_TakeOffThrust_Mach_Altitude/var_0', size(myAltitudeVector_FT'));
h5write(hdfFileName, '/BPR13_TakeOffThrust_Mach_Altitude/var_0', myAltitudeVector_FT');

h5create(hdfFileName, '/BPR13_TakeOffThrust_Mach_Altitude/var_1', size(myMachVector'));
h5write(hdfFileName, '/BPR13_TakeOffThrust_Mach_Altitude/var_1', myMachVector');
