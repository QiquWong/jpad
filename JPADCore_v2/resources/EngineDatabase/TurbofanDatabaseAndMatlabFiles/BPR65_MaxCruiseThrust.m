clear all; close all;

load('BPR65_MaxCruiseThrustData.mat');

nPoints = 30;

myMachVector = transpose(linspace(0, 0.888, nPoints));
%% 45k
thrustVsMachSpline45 = pchip(Mach45,Thrust45);

myThrustVsMach45 = ppval(thrustVsMachSpline45,myMachVector);

%% 40k
thrustVsMachSpline40 = pchip(Mach40,Thrust40);

myThrustVsMach40 = ppval(thrustVsMachSpline40,myMachVector);

%% 36k
thrustVsMachSpline36 = pchip(Mach36,Thrust36);

myThrustVsMach36 = ppval(thrustVsMachSpline36,myMachVector);

%% 30k
thrustVsMachSpline30 = pchip(Mach30,Thrust30);

myThrustVsMach30 = ppval(thrustVsMachSpline30,myMachVector);

%% 25k
thrustVsMachSpline25 = pchip(Mach25,Thrust25);

myThrustVsMach25 = ppval(thrustVsMachSpline25,myMachVector);
%% 15k
thrustVsMachSpline15 = pchip(Mach15,Thrust15);

myThrustVsMach15 = ppval(thrustVsMachSpline15,myMachVector);


%% 10k
thrustVsMachSpline10 = pchip(Mach10,Thrust10);



myThrustVsMach10 = ppval(thrustVsMachSpline10,myMachVector);


%% 0
thrustVsMachSplineSL = pchip(MachSL,ThrustSL);



myThrustVsMachSL = ppval(thrustVsMachSplineSL,myMachVector);


%% GRAFICA

figure(1)
plot(...
    myMachVector, myThrustVsMachSL, '.-k',...
    myMachVector, myThrustVsMach10, '.-r',...
    myMachVector, myThrustVsMach15, '.-c',...
    myMachVector, myThrustVsMach25, '.-b',...
    myMachVector, myThrustVsMach30, '.-b',...
    myMachVector, myThrustVsMach36, '.-b',...
    myMachVector, myThrustVsMach40, '.-g',...
    myMachVector, myThrustVsMach45, '.-m'...
    );

xlabel('Mach no.');   ylabel('Thrust ratio');
title('Bypass ratio 3.0 - maximum climb thrust.');
legend('0 ft','10000 ft','15000 ft','25000 ft','30000 ft','36000 ft','40000 ft','45000 ft');

%% preparing output to HDF

%increasing altitudes
myAltitudeVector_FT = [ ...
    0;10000;15000;25000;30000;36000;40000;45000 ...
    ];

%columns --> curves
myData = [ ...
    myThrustVsMachSL,myThrustVsMach10,myThrustVsMach25,...
    myThrustVsMach30,myThrustVsMach36,myThrustVsMach40, ...
    myThrustVsMach45...
    ];

hdfFileName = 'BPR65_MaxCruiseThrust_Mach_Altitude.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR65_MaxCruiseThrust_Mach_Altitude/data', size(myData'));
h5write(hdfFileName, '/BPR65_MaxCruiseThrust_Mach_Altitude/data', myData');

h5create(hdfFileName, '/BPR65_MaxCruiseThrust_Mach_Altitude/var_0', size(myAltitudeVector_FT'));
h5write(hdfFileName, '/BPR65_MaxCruiseThrust_Mach_Altitude/var_0', myAltitudeVector_FT');

h5create(hdfFileName, '/BPR65_MaxCruiseThrust_Mach_Altitude/var_1', size(myMachVector'));
h5write(hdfFileName, '/BPR65_MaxCruiseThrust_Mach_Altitude/var_1', myMachVector');