clc; close all; clear all;

%% Import data
Mu1PitchingMoment = importdata('Mu1PitchingMoment.mat');

nPoints = 50;
cf_c_First_Vector = transpose(linspace(0.15, 0.4, nPoints));

%% Mu_1 v.s cf_c'
smoothingParameter = 0.999999;
Mu1PitchingMoment_SplineStatic = csaps( ...
    Mu1PitchingMoment(:,1), ...
    Mu1PitchingMoment(:,2), ...
    smoothingParameter ...
    );

Mu1PitchingMoment_Static = ppval( ...
    Mu1PitchingMoment_SplineStatic, ...
    cf_c_First_Vector ...
    );

%% Plots
figure(1)
plot ( ...
    cf_c_First_Vector, Mu1PitchingMoment_Static, 'b' ... , ...
 );

xlabel('c_f/c'''); ylabel('\mu_1');
 title('\mu_1 variation with c_f/c'' - Slotted and fowler flaps');
 
 axis([0.15 0.4 0.1 0.3]);
 grid on;
 %% preparing output to HDF 
 
%columns --> curves
myData = Mu1PitchingMoment_Static;    
hdfFileName = 'Mu_1_pitching_moment_Slotted_Fowler.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

% Dataset: data
h5create(hdfFileName, '/Mu_1_pitching_moment_Slotted_Fowler/data', size(myData'));
h5write(hdfFileName, '/Mu_1_pitching_moment_Slotted_Fowler/data', myData');

% Dataset: var_1
h5create(hdfFileName, '/Mu_1_pitching_moment_Slotted_Fowler/var_0', size(cf_c_First_Vector'));
h5write(hdfFileName, '/Mu_1_pitching_moment_Slotted_Fowler/var_0', cf_c_First_Vector');