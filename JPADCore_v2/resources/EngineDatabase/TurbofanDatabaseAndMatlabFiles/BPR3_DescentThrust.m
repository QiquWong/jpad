clear all; close all; clc;

load('BPR3_DescentThrustData.mat');

nPoints=30;
myMachVector=transpose(linspace(0,0.9,nPoints));

%% 45k ft
MachVsThrustRatioSpline45=pchip(Mach45,ThrustRatio45);

myThrustVsMach45=ppval(MachVsThrustRatioSpline45,myMachVector);

%% 40k ft
MachVsThrustRatioSpline40=pchip(Mach40,ThrustRatio40);

myThrustVsMach40=ppval(MachVsThrustRatioSpline40,myMachVector);

%% 30k ft
MachVsThrustRatioSpline30=pchip(Mach30,ThrustRatio30);

myThrustVsMach30=ppval(MachVsThrustRatioSpline30,myMachVector);

%% 20k ft
MachVsThrustRatioSpline20=pchip(Mach20,ThrustRatio20);

myThrustVsMach20=ppval(MachVsThrustRatioSpline20,myMachVector);

%% 10k ft
MachVsThrustRatioSpline10=pchip(Mach10,ThrustRatio10);

myThrustVsMach10=ppval(MachVsThrustRatioSpline10,myMachVector);

%% 0 ft
MachVsThrustRatioSplineSL=pchip(MachSL,ThrustRatioSL);

myThrustVsMachSL=ppval(MachVsThrustRatioSplineSL,myMachVector);

%% GRAFICA
figure(1)
plot ( ...
    myMachVector, myThrustVsMach45, '-*b', ...
    myMachVector, myThrustVsMach40, '-*r', ...
    myMachVector, myThrustVsMach30, '-*c', ...
    myMachVector, myThrustVsMach20, '-*y', ...
    myMachVector, myThrustVsMach10, '-*k', ...
    myMachVector, myThrustVsMachSL, '-*g' ...
 );
 xlabel('Mach No'); ylabel('Thrust Ratio');
 title('Bypass ratio 3.0 - descent thrust.');
 legend('45000 ft','40000 ft','30000 ft','20000 ft','10000 ft','0 ft');
 
axis([0 0.9 -0.15 0.10]);


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

hdfFileName = 'BPR3_DescentThrust_Mach_Altitude.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR3_DescentThrust_Mach_Altitude/data', size(myData'));
h5write(hdfFileName, '/BPR3_DescentThrust_Mach_Altitude/data', myData');

h5create(hdfFileName, '/BPR3_DescentThrust_Mach_Altitude/var_0', size(myAltitudeVector_FT'));
h5write(hdfFileName, '/BPR3_DescentThrust_Mach_Altitude/var_0', myAltitudeVector_FT');

h5create(hdfFileName, '/BPR3_DescentThrust_Mach_Altitude/var_1', size(myMachVector'));
h5write(hdfFileName, '/BPR3_DescentThrust_Mach_Altitude/var_1', myMachVector');
