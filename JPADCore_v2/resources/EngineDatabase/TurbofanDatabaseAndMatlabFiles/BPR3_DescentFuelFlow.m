clear all; close all; clc;

load('BPR3_DescentFuelFlowData.mat');

nPoints = 30;
myMachVector = transpose(linspace(0, 0.888, nPoints));


%% 45k ft
fflowVsMachSpline45 = pchip(Mach45, FFlow45);

myfflowVsMach45 = ppval(fflowVsMachSpline45, myMachVector);

%% 40k ft
fflowVsMachSpline40 = pchip(Mach40, FFlow40);

myfflowVsMach40 = ppval(fflowVsMachSpline40, myMachVector);

%% 30k ft
fflowVsMachSpline30 = pchip(Mach30, FFlow30);

myfflowVsMach30 = ppval(fflowVsMachSpline30, myMachVector);

%% 20k ft
fflowVsMachSpline20 = pchip(Mach20, FFlow20);

myfflowVsMach20 = ppval(fflowVsMachSpline20, myMachVector);

%% SL
fflowVsMachSplineSL = pchip(MachSL, FFlowSL);

myfflowVsMachSL = ppval(fflowVsMachSplineSL, myMachVector);

%% GRAFICA
figure(1)
plot ( ...
      myMachVector, myfflowVsMach45, '.-b', ...
      myMachVector, myfflowVsMach40, '.-r', ...
      myMachVector, myfflowVsMach30, '.-y', ...
      myMachVector, myfflowVsMach20, '.-k', ...
      myMachVector, myfflowVsMachSL, '.-m' ...
      );
 xlabel('Mach No'); ylabel('Fuel Flow');
 title('Bypass ratio 3.0 - descent fuel flow.');
 legend('45000 ft','40000 ft','30000 ft','20000 ft', '0 ft');
 
axis([0 0.88 0.02 0.06]);


%% Preparing output to HDF
% increasing altitudes
myAltitudeVector_FT = flipud([ ...
   45000;40000;30000;20000; 0 ...
    ]);

% columns --> curves
myData = [ ...
          myfflowVsMachSL, ...
          myfflowVsMach20, ...
          myfflowVsMach30, ...
          myfflowVsMach40, ... 
          myfflowVsMach45 ...
    ];

hdfFileName = 'BPR3_DescentFuelFlow_Mach_Altitude.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR3_DescentFuelFlow_Mach_Altitude/data', size(myData'));
h5write(hdfFileName, '/BPR3_DescentFuelFlow_Mach_Altitude/data', myData');

h5create(hdfFileName, '/BPR3_DescentFuelFlow_Mach_Altitude/var_0', size(myAltitudeVector_FT'));
h5write(hdfFileName, '/BPR3_DescentFuelFlow_Mach_Altitude/var_0', myAltitudeVector_FT');

h5create(hdfFileName, '/BPR3_DescentFuelFlow_Mach_Altitude/var_1', size(myMachVector'));
h5write(hdfFileName, '/BPR3_DescentFuelFlow_Mach_Altitude/var_1', myMachVector');
