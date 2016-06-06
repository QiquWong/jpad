clear all; close all;

load('BPR13_DescentFuelFlowData.mat');

nPoints = 30;
myMachVector = transpose(linspace(0, 0.888, nPoints));


%% 45k ft
fflowVsMachFSpline45 = pchip(MachF45, FF45);

myfflowVsMachF45 = ppval(fflowVsMachFSpline45, myMachVector);

%% 40k ft
fflowVsMachFSpline40 = pchip(MachF40, FF40);

myfflowVsMachF40 = ppval(fflowVsMachFSpline40, myMachVector);

%% 36k ft
fflowVsMachFSpline36 = pchip(MachF36, FF36);

myfflowVsMachF36 = ppval(fflowVsMachFSpline36, myMachVector);

%% 30k ft
fflowVsMachFSpline30 = pchip(MachF30, FF30);

myfflowVsMachF30 = ppval(fflowVsMachFSpline30, myMachVector);

%% 25k ft
fflowVsMachFSpline25 = pchip(MachF25, FF25);

myfflowVsMachF25 = ppval(fflowVsMachFSpline25, myMachVector);

%% 15k ft
fflowVsMachFSpline15 = pchip(MachF15, FF15);

myfflowVsMachF15 = ppval(fflowVsMachFSpline15, myMachVector);

%% 10k ft
fflowVsMachFSpline10 = pchip(MachF10, FF10);

myfflowVsMachF10 = ppval(fflowVsMachFSpline10, myMachVector);
%% SL
fflowVsMachFSplineSL = pchip(MachFSL, FFSL);

myfflowVsMachFSL = ppval(fflowVsMachFSplineSL, myMachVector);

%% GRAFICA
figure(1)
plot ( ...
       myMachVector, myfflowVsMachF45, '.-b', ...
       myMachVector, myfflowVsMachF40, '.-r', ...
       myMachVector, myfflowVsMachF36, '.-c', ...
       myMachVector, myfflowVsMachF30, '.-y', ...
       myMachVector, myfflowVsMachF25, '.-k', ...
       myMachVector, myfflowVsMachF15, '.-g', ...
       myMachVector, myfflowVsMachF10, '.-b', ...
       myMachVector, myfflowVsMachFSL, '.-m' ...
       );
 xlabel('Mach No'); ylabel('Fuel Flow');
 title('Bypass ratio 13.0 - descent fuel flow.');
 legend('45000 ft','40000 ft','36000 ft','30000 ft','25000 ft','15000 ft', '10000 ft','0 ft');
 
axis([0 0.88 0.003 0.016]);

%% Preparing output to HDF
% increasing altitudes
myAltitudeVector_FT = flipud([ ...
    45000;40000;36000;30000;25000;15000;10000;0 ...
    ]);

% columns --> curves
myData = [ ...
    myfflowVsMachFSL,myfflowVsMachF10,myfflowVsMachF15,myfflowVsMachF25, ...
    myfflowVsMachF30,myfflowVsMachF36,myfflowVsMachF40,myfflowVsMachF45 ...
    ];

hdfFileName = 'BPR13_DescentFuelFlow_Mach_Altitude.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR13_DescentFuelFlow_Mach_Altitude/data', size(myData'));
h5write(hdfFileName, '/BPR13_DescentFuelFlow_Mach_Altitude/data', myData');

h5create(hdfFileName, '/BPR13_DescentFuelFlow_Mach_Altitude/var_0', size(myAltitudeVector_FT'));
h5write(hdfFileName, '/BPR13_DescentFuelFlow_Mach_Altitude/var_0', myAltitudeVector_FT');

h5create(hdfFileName, '/BPR13_DescentFuelFlow_Mach_Altitude/var_1', size(myMachVector'));
h5write(hdfFileName, '/BPR13_DescentFuelFlow_Mach_Altitude/var_1', myMachVector');

