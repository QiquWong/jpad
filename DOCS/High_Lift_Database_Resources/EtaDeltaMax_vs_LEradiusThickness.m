clc; close all; clear all;

%% Import data
EtamaxDATCOMcorrection = importdata('EtamaxDATCOMcorrection.mat');

nPoints = 50;
LEradius_ticknessVector = transpose(linspace(0, 0.2, nPoints));

%% Eta Delta v.s. delta Slat
smoothingParameter = 0.999999;
EtamaxDATCOMcorrection_SplineStatic = csaps( ...
    EtamaxDATCOMcorrection(:,1), ...
    EtamaxDATCOMcorrection(:,2), ...
    smoothingParameter ...
    );

EtamaxDATCOMcorrection_Static = ppval( ...
    EtamaxDATCOMcorrection_SplineStatic, ...
    LEradius_ticknessVector ...
    );

%% Plots
figure(1)
plot ( ...
    LEradius_ticknessVector, EtamaxDATCOMcorrection_Static, 'b' ... , ...
 );

xlabel('LE Radius/tickness'); ylabel('\eta_{max}');
 title('\eta_{max} variation with LE Radius/tickness ratio - DATCOM Correction');
 
 axis([0 0.2 0 2]);
 grid on;
 %% preparing output to HDF 
 
%columns --> curves
myData = EtamaxDATCOMcorrection_Static;    
hdfFileName = 'EtaDeltaMax_vs_LEradius_thickness.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

% Dataset: data
h5create(hdfFileName, '/EtaDeltaMax_vs_LEradius_thickness/data', size(myData'));
h5write(hdfFileName, '/EtaDeltaMax_vs_LEradius_thickness/data', myData');

% Dataset: var_1
h5create(hdfFileName, '/EtaDeltaMax_vs_LEradius_thickness/var_0', size(LEradius_ticknessVector'));
h5write(hdfFileName, '/EtaDeltaMax_vs_LEradius_thickness/var_0', LEradius_ticknessVector');