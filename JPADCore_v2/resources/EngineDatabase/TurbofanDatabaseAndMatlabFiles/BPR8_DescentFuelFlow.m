clc; close all; clear all;

load('BPR8_DescentFuelFlowData.mat');

nPoints = 30;
myMachVector = transpose(linspace(0, 0.94, nPoints));

%% 0 ft
MachVsFuelFlowSplineSL=pchip(MachSL,FuelFlowSL);

myMachVsFuelFlowSL=ppval(MachVsFuelFlowSplineSL,myMachVector);

%% 10k ft
MachVsFuelFlowSpline10=pchip(Mach10,FuelFlow10);

myMachVsFuelFlow10=ppval(MachVsFuelFlowSpline10,myMachVector);

%% 15k ft
MachVsFuelFlowSpline15=pchip(Mach15,FuelFlow15);

myMachVsFuelFlow15=ppval(MachVsFuelFlowSpline15,myMachVector);

%% 25k ft
MachVsFuelFlowSpline25=pchip(Mach25,FuelFlow25);

myMachVsFuelFlow25=ppval(MachVsFuelFlowSpline25,myMachVector);

%% 30k ft
MachVsFuelFlowSpline30=pchip(Mach30,FuelFlow30);

myMachVsFuelFlow30=ppval(MachVsFuelFlowSpline30,myMachVector);

%% 36k ft
MachVsFuelFlowSpline36=pchip(Mach36,FuelFlow36);

myMachVsFuelFlow36=ppval(MachVsFuelFlowSpline36,myMachVector);

%% 40k ft
MachVsFuelFlowSpline40=pchip(Mach40,FuelFlow40);

myMachVsFuelFlow40=ppval(MachVsFuelFlowSpline40,myMachVector);

%% 45k ft
MachVsFuelFlowSpline45=pchip(Mach45,FuelFlow45);

myMachVsFuelFlow45=ppval(MachVsFuelFlowSpline45,myMachVector);

%% GRAFICA
figure(1)
plot ( ...
    myMachVector, myMachVsFuelFlow45, '-*b', ...
    myMachVector, myMachVsFuelFlow40, '-*r', ...
    myMachVector, myMachVsFuelFlow36, '-*m', ...
    myMachVector, myMachVsFuelFlow30, '-*c', ...
    myMachVector, myMachVsFuelFlow25, '-*y', ...
    myMachVector, myMachVsFuelFlow15, '-*g', ...
    myMachVector, myMachVsFuelFlow10, '-*k', ...
    myMachVector, myMachVsFuelFlowSL, '-*b' ...
 );
 xlabel('Mach No'); ylabel('Fuel Flow');
 title('Bypass ratio 8 - descent fuel flow.');
 legend('45000 ft','40000 ft','36000 ft','30000 ft','25000 ft','15000 ft','10000 ft','0 ft');
 
axis([0 0.95 0 0.02]);

%% preparing output to HDF

%increasing altitudes
myAltitudeVector_FT = [ ...
    0;10000;15000;25000;30000;36000;40000;45000 ...
    ];

%columns --> curves
myData = [ ...
    myMachVsFuelFlowSL,myMachVsFuelFlow10,myMachVsFuelFlow15,myMachVsFuelFlow25,...
    myMachVsFuelFlow30,myMachVsFuelFlow36,myMachVsFuelFlow40,myMachVsFuelFlow45 ...
    ];

hdfFileName = 'BPR8_DescentFuelFlow_Mach_Altitude.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR8_DescentFuelFlow_Mach_Altitude/data', size(myData'));
h5write(hdfFileName, '/BPR8_DescentFuelFlow_Mach_Altitude/data', myData');

h5create(hdfFileName, '/BPR8_DescentFuelFlow_Mach_Altitude/var_0', size(myAltitudeVector_FT'));
h5write(hdfFileName, '/BPR8_DescentFuelFlow_Mach_Altitude/var_0', myAltitudeVector_FT');

h5create(hdfFileName, '/BPR8_DescentFuelFlow_Mach_Altitude/var_1', size(myMachVector'));
h5write(hdfFileName, '/BPR8_DescentFuelFlow_Mach_Altitude/var_1', myMachVector');

