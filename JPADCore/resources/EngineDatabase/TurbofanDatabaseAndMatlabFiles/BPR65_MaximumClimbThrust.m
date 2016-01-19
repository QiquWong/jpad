clc; close all; clear all;

load('BPR65_MaximumClimbThrustData.mat');

nPoints = 30;
myMachVector = transpose(linspace(0, 1, nPoints));

%% 0 ft
MachVsThrustRatioSplineSL=pchip(MachSL,ThrustRatioSL);

myThrustVsMachSL=ppval(MachVsThrustRatioSplineSL,myMachVector);

%% 10k ft
MachVsThrustRatioSpline10=pchip(Mach10,ThrustRatio10);

myThrustVsMach10=ppval(MachVsThrustRatioSpline10,myMachVector);

%% 15k ft
MachVsThrustRatioSpline15=pchip(Mach15,ThrustRatio15);

myThrustVsMach15=ppval(MachVsThrustRatioSpline15,myMachVector);

%% 25k ft
MachVsThrustRatioSpline25=pchip(Mach25,ThrustRatio25);

myThrustVsMach25=ppval(MachVsThrustRatioSpline25,myMachVector);

%% 30k ft
MachVsThrustRatioSpline30=pchip(Mach30,ThrustRatio30);

myThrustVsMach30=ppval(MachVsThrustRatioSpline30,myMachVector);

%% 36k ft
MachVsThrustRatioSpline36=pchip(Mach36,ThrustRatio36);

myThrustVsMach36=ppval(MachVsThrustRatioSpline36,myMachVector);

%% 40k ft
MachVsThrustRatioSpline40=pchip(Mach40,ThrustRatio40);

myThrustVsMach40=ppval(MachVsThrustRatioSpline40,myMachVector);

%% 45k ft
MachVsThrustRatioSpline45=pchip(Mach45,ThrustRatio45);

myThrustVsMach45=ppval(MachVsThrustRatioSpline45,myMachVector);

%% GRAFICA
figure(1)
plot ( ...
    myMachVector, myThrustVsMach45, '-*b', ...
    myMachVector, myThrustVsMach40, '-*r', ...
    myMachVector, myThrustVsMach36, '-*m', ...
    myMachVector, myThrustVsMach30, '-*c', ...
    myMachVector, myThrustVsMach25, '-*y', ...
    myMachVector, myThrustVsMach15, '-*g', ...
    myMachVector, myThrustVsMach10, '-*k', ...
    myMachVector, myThrustVsMachSL, '-*b' ...
 );
 xlabel('Mach No'); ylabel('Thrust Ratio');
 title('Bypass ratio 6.5 - maximum climb thrust.');
 legend('45000 ft','40000 ft','36000 ft','30000 ft','25000 ft','15000 ft','10000 ft','0 ft');
 
axis([0 1 0 0.9]);

%% preparing output to HDF

%increasing altitudes
myAltitudeVector_FT = [ ...
    0;10000;15000;25000;30000;36000;40000;45000 ...
    ];

%columns --> curves
myData = [ ...
    myThrustVsMachSL,myThrustVsMach10,myThrustVsMach15,myThrustVsMach25,...
    myThrustVsMach30,myThrustVsMach36,myThrustVsMach40,myThrustVsMach45 ...
    ];

hdfFileName = 'BPR65_MaximumClimbThrust_Mach_Altitude.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR65_MaximumClimbThrust_Mach_Altitude/data', size(myData'));
h5write(hdfFileName, '/BPR65_MaximumClimbThrust_Mach_Altitude/data', myData');

h5create(hdfFileName, '/BPR65_MaximumClimbThrust_Mach_Altitude/var_0', size(myAltitudeVector_FT'));
h5write(hdfFileName, '/BPR65_MaximumClimbThrust_Mach_Altitude/var_0', myAltitudeVector_FT');

h5create(hdfFileName, '/BPR65_MaximumClimbThrust_Mach_Altitude/var_1', size(myMachVector'));
h5write(hdfFileName, '/BPR65_MaximumClimbThrust_Mach_Altitude/var_1', myMachVector');
