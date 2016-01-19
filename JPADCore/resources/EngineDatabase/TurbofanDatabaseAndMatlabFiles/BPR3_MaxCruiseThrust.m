clear all; close all;

load('BPR3_MaxCruiseThrustData.mat');

nPoints = 30;
myMachVector = transpose(linspace(0, 0.888, nPoints));


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

%% Preparing output to HDF
% increasing altitudes
myAltitudeVector_FT = flipud([ ...
    45000;40000;30000;20000;10000;0 ...
    ]);

% columns --> curves
myData = [ ...
    myThrustVsMachSL,myThrustVsMach10,myThrustVsMach20,myThrustVsMach30, ...
        myThrustVsMach40,myThrustVsMach45 ...
    ];

hdfFileName = 'BPR3_MaxCruiseThrust_Mach_Altitude.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR3_MaxCruiseThrust_Mach_Altitude/data', size(myData'));
h5write(hdfFileName, '/BPR3_MaxCruiseThrust_Mach_Altitude/data', myData');

h5create(hdfFileName, '/BPR3_MaxCruiseThrust_Mach_Altitude/var_0', size(myAltitudeVector_FT'));
h5write(hdfFileName, '/BPR3_MaxCruiseThrust_Mach_Altitude/var_0', myAltitudeVector_FT');

h5create(hdfFileName, '/BPR3_MaxCruiseThrust_Mach_Altitude/var_1', size(myMachVector'));
h5write(hdfFileName, '/BPR3_MaxCruiseThrust_Mach_Altitude/var_1', myMachVector');

