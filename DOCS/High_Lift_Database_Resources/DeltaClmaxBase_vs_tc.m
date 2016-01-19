clc; close all; clear all;

%% Import data
DeltaClmaxBase_1Slot = importdata('DeltaClmaxBaseC.mat');
DeltaClmaxBase_2Slot = importdata('DeltaClmaxBaseA.mat');
DeltaClmaxBase_Plain = importdata('DeltaClmaxBaseD.mat');
DeltaClmaxBase_Fowler = importdata('DeltaClmaxBaseB.mat');
DeltaClmaxBase_3Slot = importdata('DeltaClmaxBaseA.mat');

nPoints = 50;
airfoilTicknessVector = transpose(linspace(6, 18, nPoints));

%% 1-Slot  (1)
smoothingParameter = 0.999999;
DeltaClmaxBase_1Slot_SplineStatic = csaps( ...
    DeltaClmaxBase_1Slot(:,1), ...
    DeltaClmaxBase_1Slot(:,2), ...
    smoothingParameter ...
    );

DeltaClmaxBase_1Slot_Static = ppval( ...
    DeltaClmaxBase_1Slot_SplineStatic, ...
    airfoilTicknessVector ...
    );

%% 2-Slot  (2)
smoothingParameter = 0.999999;
DeltaClmaxBase_2Slot_SplineStatic = csaps( ...
    DeltaClmaxBase_2Slot(:,1), ...
    DeltaClmaxBase_2Slot(:,2), ...
    smoothingParameter ...
    );

DeltaClmaxBase_2Slot_Static = ppval( ...
    DeltaClmaxBase_2Slot_SplineStatic, ...
    airfoilTicknessVector ...
    );

%% Plain Flap  (4)
smoothingParameter = 0.999999;
DeltaClmaxBase_Plain_SplineStatic = csaps( ...
    DeltaClmaxBase_Plain(:,1), ...
    DeltaClmaxBase_Plain(:,2), ...
    smoothingParameter ...
    );

DeltaClmaxBase_Plain_Static = ppval( ...
    DeltaClmaxBase_Plain_SplineStatic, ...
    airfoilTicknessVector ...
    );

%% Fowler  (5)
smoothingParameter = 0.999999;
DeltaClmaxBase_Fowler_SplineStatic = csaps( ...
    DeltaClmaxBase_Fowler(:,1), ...
    DeltaClmaxBase_Fowler(:,2), ...
    smoothingParameter ...
    );

DeltaClmaxBase_Fowler_Static = ppval( ...
    DeltaClmaxBase_Fowler_SplineStatic, ...
    airfoilTicknessVector ...
    );

%% 3-Slot  (6)
smoothingParameter = 0.999999;
DeltaClmaxBase_3Slot_SplineStatic = csaps( ...
    DeltaClmaxBase_3Slot(:,1), ...
    DeltaClmaxBase_3Slot(:,2), ...
    smoothingParameter ...
    );

DeltaClmaxBase_3Slot_Plain_Static = ppval( ...
    DeltaClmaxBase_3Slot_SplineStatic, ...
    airfoilTicknessVector ...
    );

%% Plots
figure(1)
plot ( ...
    airfoilTicknessVector, DeltaClmaxBase_1Slot_Static, 'b-*' ... , ...
 );

hold on;

plot ( ...
    airfoilTicknessVector, DeltaClmaxBase_2Slot_Static, 'b-+' ... , ...
 );

hold on;

plot ( ...
    airfoilTicknessVector, DeltaClmaxBase_Plain_Static, 'b-.' ... , ...
 );

hold on;

plot ( ...
    airfoilTicknessVector, DeltaClmaxBase_Fowler_Static, 'b-^' ... , ...
 );

hold on;

plot ( ...
    airfoilTicknessVector, DeltaClmaxBase_3Slot_Plain_Static, 'b' ... , ...
 );

xlabel('Airfoil t/c %'); ylabel('\Deltac_lmax_{base}');
 title('\Deltac_lmax_{base} variation with slat airfoil tickness');
  legend('1-Slot', '2-Slot', 'Plain Flap', 'Fowler', '3-Slot');
 
 axis([6 18 0.8 1.8]);
 grid on;
 %% preparing output to HDF
 
% FlapType (see number for each section)
flapTypeVector = [ ...
    1;2;3;4;5 ...
    ]; 
 
%columns --> curves
myData = [ ...
    DeltaClmaxBase_1Slot_Static, ...
    DeltaClmaxBase_2Slot_Static, ...
    DeltaClmaxBase_Plain_Static, ...
    DeltaClmaxBase_Fowler_Static, ...
    DeltaClmaxBase_3Slot_Plain_Static
    ];    
hdfFileName = 'DeltaClmaxBase_vs_airfoilThickness.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

% Dataset: data
h5create(hdfFileName, '/DeltaClmaxBase_vs_airfoilThickness/data', size(myData'));
h5write(hdfFileName, '/DeltaClmaxBase_vs_airfoilThickness/data', myData');

% Dataset: var_0
h5create(hdfFileName, '/DeltaClmaxBase_vs_airfoilThickness/var_0', size(flapTypeVector'));
h5write(hdfFileName, '/DeltaClmaxBase_vs_airfoilThickness/var_0', flapTypeVector');

% Dataset: var_1
h5create(hdfFileName, '/DeltaClmaxBase_vs_airfoilThickness/var_1', size(airfoilTicknessVector'));
h5write(hdfFileName, '/DeltaClmaxBase_vs_airfoilThickness/var_1', airfoilTicknessVector');