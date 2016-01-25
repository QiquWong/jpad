clc; close all; clear all;

%% Import data
Delta2Plain = importdata('Delta2Plain.mat');

nPoints = 50;
delta_flap_Vector = transpose(linspace(0, 63, nPoints));

%% Eta Delta v.s. delta Slat
smoothingParameter = 0.999999;
Delta2Plain_SplineStatic = csaps( ...
    Delta2Plain(:,1), ...
    Delta2Plain(:,2), ...
    smoothingParameter ...
    );

Delta2Plain_Static = ppval( ...
    Delta2Plain_SplineStatic, ...
    delta_flap_Vector ...
    );

%% Plots
figure(1)
plot ( ...
    delta_flap_Vector, Delta2Plain_Static, 'b' ... , ...
 );

xlabel('\delta_f'); ylabel('\delta_2');
 title('\delta_2 variation with \delta_f for Plain Flap');
 
 axis([0 70 0 0.2]);
 grid on;
 %% preparing output to HDF 
 
%columns --> curves
myData = Delta2Plain_Static;    
hdfFileName = 'Delta2_vs_DeltaFlap_Plain.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

% Dataset: data
h5create(hdfFileName, '/Delta2_vs_DeltaFlap_Plain/data', size(myData'));
h5write(hdfFileName, '/Delta2_vs_DeltaFlap_Plain/data', myData');

% Dataset: var_1
h5create(hdfFileName, '/Delta2_vs_DeltaFlap_Plain/var_0', size(delta_flap_Vector'));
h5write(hdfFileName, '/Delta2_vs_DeltaFlap_Plain/var_0', delta_flap_Vector');