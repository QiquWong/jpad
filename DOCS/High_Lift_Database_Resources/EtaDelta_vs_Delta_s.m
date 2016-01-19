clc; close all; clear all;

%% Import data
EtaDeltaSlat = importdata('EtaDeltaSlat.mat');

nPoints = 50;
deltasVector = transpose(linspace(0, 35, nPoints));

%% Eta Delta v.s. delta Slat
smoothingParameter = 0.999999;
EtaDeltaSlat_SplineStatic = csaps( ...
    EtaDeltaSlat(:,1), ...
    EtaDeltaSlat(:,2), ...
    smoothingParameter ...
    );

EtaDeltaSlat_Static = ppval( ...
    EtaDeltaSlat_SplineStatic, ...
    deltasVector ...
    );

%% Plots
figure(1)
plot ( ...
    deltasVector, EtaDeltaSlat_Static, 'b' ... , ...
 );

xlabel('\delta_s'); ylabel('\eta_\delta');
 title('\eta_\delta variation with \delta_s');
 
 axis([0 35 0 1.2]);
 grid on;
 %% preparing output to HDF 
 
%columns --> curves
myData = EtaDeltaSlat_Static;    
hdfFileName = 'EtaDelta_vs_DeltaSlat.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

% Dataset: data
h5create(hdfFileName, '/EtaDelta_vs_DeltaSlat/data', size(myData'));
h5write(hdfFileName, '/EtaDelta_vs_DeltaSlat/data', myData');

% Dataset: var_1
h5create(hdfFileName, '/EtaDelta_vs_DeltaSlat/var_0', size(deltasVector'));
h5write(hdfFileName, '/EtaDelta_vs_DeltaSlat/var_0', deltasVector');