clear all; close all;

load('BPR8_TakeOffThrustData.mat');

nPoints = 30;

myMachVector = transpose(linspace(0, 0.4, nPoints));
%% 0
ThrustVsMachSplineSL = pchip(MachSL,ThrustSL);


myThrustVsMachSL = ppval(ThrustVsMachSplineSL,myMachVector);

% plot(myMachSL,myThrustRatioVsMachSL); hold on;
% axis([0 0.5 0.5 1]);
% %set(gca,'YDir','Reverse');
% xlabel('Mach no.'); ylabel('Fuel Flow');
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

%% preparing output to HDF

%increasing altitudes
myAltitudeVector_FT = [ ...
    0;5000;10000 ...
    ];

%columns --> curves
myData = [ ...
    myThrustVsMachSL,myThrustVsMach5000,myThrustVsMach10000,...
    ];

hdfFileName = 'BPR8_TakeOffThrust_Mach_Altitude.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR8_TakeOffThrust_Mach_Altitude/data', size(myData'));
h5write(hdfFileName, '/BPR8_TakeOffThrust_Mach_Altitude/data', myData');

h5create(hdfFileName, '/BPR8_TakeOffThrust_Mach_Altitude/var_0', size(myAltitudeVector_FT'));
h5write(hdfFileName, '/BPR8_TakeOffThrust_Mach_Altitude/var_0', myAltitudeVector_FT');

h5create(hdfFileName, '/BPR8_TakeOffThrust_Mach_Altitude/var_1', size(myMachVector'));
h5write(hdfFileName, '/BPR8_TakeOffThrust_Mach_Altitude/var_1', myMachVector');