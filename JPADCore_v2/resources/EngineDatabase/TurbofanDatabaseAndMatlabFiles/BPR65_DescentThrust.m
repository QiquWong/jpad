%% Descend Thrust ByPass Ratio 6.5
close all; clear all;

load('BPR65_DescentThrustData.mat');

nPoints = 30;
myMachVector = transpose(linspace(0, 0.888, nPoints));
%% SL

ThrustVsMachSplineDSL = pchip(MachDSL, ThrustDSL);

myThrustVsMachDSL = ppval(ThrustVsMachSplineDSL, myMachVector);

%% 10k ft.

ThrustVsMachSplineD10 = pchip(MachD10, ThrustD10);

myThrustVsMachD10 = ppval(ThrustVsMachSplineD10, myMachVector);

%% 15k ft.

ThrustVsMachSplineD15 = pchip(MachD15, ThrustD15);

myThrustVsMachD15 = ppval(ThrustVsMachSplineD15, myMachVector);

%% 25k ft.

ThrustVsMachSplineD25 = pchip(MachD25, ThrustD25);

myThrustVsMachD25 = ppval(ThrustVsMachSplineD25, myMachVector);
%% 36k ft.

ThrustVsMachSplineD36 = pchip(MachD36, ThrustD36);

myThrustVsMachD36 = ppval(ThrustVsMachSplineD36, myMachVector);

%% 40k ft.

ThrustVsMachSplineD40 = pchip(MachD40, ThrustD40);

myThrustVsMachD40 = ppval(ThrustVsMachSplineD40, myMachVector);


figure(1)
plot ( ...
    myMachVector, myThrustVsMachDSL, '.-m', ...
    myMachVector, myThrustVsMachD10, '.-c', ...
    myMachVector, myThrustVsMachD15, '.-b', ...
    myMachVector, myThrustVsMachD25, '.-g', ...
    myMachVector, myThrustVsMachD36, '.-y', ...
    myMachVector, myThrustVsMachD40, '.-r' ...
    ); 
  title('Bypass ratio 6.5 - Descend thrust.');
  xlabel('Mach No'); ylabel('Thrust ratio (FN/FN*)');
  legend('Sea Level', '10000 ft', '15000 ft', '25000 ft', '36000 ft', '40000 ft'); 
axis([0 0.88 -0.06 0.04]);


%% Preparing output to HDF
% increasing altitudes
myAltitudeVector_FT = flipud([ ...
   40000;36000;25000;15000;100000;0 ...
    ]);

% columns --> curves
myData = [ ...
          myThrustVsMachDSL, ...
          myThrustVsMachD10, ...
          myThrustVsMachD15, ...
          myThrustVsMachD25, ...
          myThrustVsMachD36, ... 
          myThrustVsMachD40 ...
    ];

hdfFileName = 'BPR65_DescentThrust_Mach_Altitude.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR65_DescentThrust_Mach_Altitude/data', size(myData'));
h5write(hdfFileName, '/BPR65_DescentThrust_Mach_Altitude/data', myData');

h5create(hdfFileName, '/BPR65_DescentThrust_Mach_Altitude/var_0', size(myAltitudeVector_FT'));
h5write(hdfFileName, '/BPR65_DescentThrust_Mach_Altitude/var_0', myAltitudeVector_FT');

h5create(hdfFileName, '/BPR65_DescentThrust_Mach_Altitude/var_1', size(myMachVector'));
h5write(hdfFileName, '/BPR65_DescentThrust_Mach_Altitude/var_1', myMachVector');
