clc; close all; clear all;

load('BPR13_DescentThrustData.mat');

nPoints = 30;
myMachVector = transpose(linspace(0, 0.94, nPoints));

%% 0 ft
ThrustVsMachSplineSL=pchip(MachSL,ThrustRatioSL);

myThrustVsMachSL=ppval(ThrustVsMachSplineSL,myMachVector);

%% 10k ft
ThrustVsMachSpline10=pchip(Mach10,ThrustRatio10);

myThrustVsMach10=ppval(ThrustVsMachSpline10,myMachVector);

%% 15k ft
ThrustVsMachSpline15=pchip(Mach15,ThrustRatio15);

myThrustVsMach15=ppval(ThrustVsMachSpline15,myMachVector);

%% 25k ft
ThrustVsMachSpline25=pchip(Mach25,ThrustRatio25);

myThrustVsMach25=ppval(ThrustVsMachSpline25,myMachVector);

%% 30k ft
ThrustVsMachSpline30=pchip(Mach30,ThrustRatio30);

myThrustVsMach30=ppval(ThrustVsMachSpline30,myMachVector);

%% 40k ft
ThrustVsMachSpline40=pchip(Mach40,ThrustRatio40);

myThrustVsMach40=ppval(ThrustVsMachSpline40,myMachVector);

%% 45k ft
ThrustVsMachSpline45=pchip(Mach45,ThrustRatio45);

myThrustVsMach45=ppval(ThrustVsMachSpline45,myMachVector);

%% GRAFICA
figure(1)
plot ( ...
    myMachVector, myThrustVsMach45, '-*b', ...
    myMachVector, myThrustVsMach40, '-*r', ...
    myMachVector, myThrustVsMach30, '-*c', ...
    myMachVector, myThrustVsMach25, '-*m', ...
    myMachVector, myThrustVsMach15, '-*g', ...
    myMachVector, myThrustVsMach10, '-*k', ...
    myMachVector, myThrustVsMachSL, '-*b' ...
 );
 xlabel('Mach No'); ylabel('Thrust Ratio');
 title('Bypass ratio 13.0 - descent thrust.');
 legend('45000 ft','40000 ft','30000 ft','25000 ft','15000 ft','10000 ft','0 ft');
 
axis([0 0.95 -0.08 0.04]);

%% preparing output to HDF

%increasing altitudes
myAltitudeVector_FT = [ ...
    0;10000;15000;25000;30000;40000;45000 ...
    ];

%columns --> curves
myData = [ ...
    myThrustVsMachSL,myThrustVsMach10,myThrustVsMach15,myThrustVsMach25,...
    myThrustVsMach30,myThrustVsMach40,myThrustVsMach45 ...
    ];

hdfFileName = 'BPR13_DescentThrust_Mach_Altitude.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR13_DescentThrust_Mach_Altitude/data', size(myData'));
h5write(hdfFileName, '/BPR13_DescentThrust_Mach_Altitude/data', myData');

h5create(hdfFileName, '/BPR13_DescentThrust_Mach_Altitude/var_0', size(myAltitudeVector_FT'));
h5write(hdfFileName, '/BPR13_DescentThrust_Mach_Altitude/var_0', myAltitudeVector_FT');

h5create(hdfFileName, '/BPR13_DescentThrust_Mach_Altitude/var_1', size(myMachVector'));
h5write(hdfFileName, '/BPR13_DescentThrust_Mach_Altitude/var_1', myMachVector');
