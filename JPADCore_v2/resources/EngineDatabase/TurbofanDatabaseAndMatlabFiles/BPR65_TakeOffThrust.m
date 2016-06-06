close all; clear all;

load('BPR65_TakeOffThrustData.mat');

nPoints = 30;
myMachVector = transpose(linspace(0, 0.4, nPoints));

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


%% Preparing output to HDF
% increasing altitudes
myAltitudeVector_FT = flipud([ ...
   10000;5000;0 ...
    ]);

% columns --> curves
myData = [ ...
          myThrustVsMachSL, ...
          myThrustVsMach5, ...
          myThrustVsMach10, ...
          ];

hdfFileName = 'BPR65_TakeOffThrust_Mach_Altitude.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR65_TakeOffThrust_Mach_Altitude/data', size(myData'));
h5write(hdfFileName, '/BPR65_TakeOffThrust_Mach_Altitude/data', myData');

h5create(hdfFileName, '/BPR65_TakeOffThrust_Mach_Altitude/var_0', size(myAltitudeVector_FT'));
h5write(hdfFileName, '/BPR65_TakeOffThrust_Mach_Altitude/var_0', myAltitudeVector_FT');

h5create(hdfFileName, '/BPR65_TakeOffThrust_Mach_Altitude/var_1', size(myMachVector'));
h5write(hdfFileName, '/BPR65_TakeOffThrust_Mach_Altitude/var_1', myMachVector');



