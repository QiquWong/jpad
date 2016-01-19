clc; close all; clear all;

%% Import data
DeltaAlphaMax = importdata('Delta_Aplha_Max.mat');

nPoints = 50;
deltasVector = transpose(linspace(0, 60, nPoints));

%% Eta Delta v.s. delta Slat
smoothingParameter = 0.999999;
DeltaAlphaMax_SplineStatic = csaps( ...
    DeltaAlphaMax(:,1), ...
    DeltaAlphaMax(:,2), ...
    smoothingParameter ...
    );

DeltaAlphaMax_Static = ppval( ...
    DeltaAlphaMax_SplineStatic, ...
    deltasVector ...
    );

%% Plots
figure(1)
plot ( ...
    deltasVector, DeltaAlphaMax_Static, 'b' ... , ...
 );

xlabel('\delta_f'); ylabel('\Delta_{\alpha stall}');
 title('\Delta_{\alpha stall} variation with \delta_f');
 
 set(gca,'Ydir','reverse')
 axis([0 60 -6 0]);
 grid on;
 %% preparing output to HDF 
 
%columns --> curves
myData = DeltaAlphaMax_Static;    
hdfFileName = 'DeltaAlphaMax_vs_DeltaFlap.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

% Dataset: data
h5create(hdfFileName, '/DeltaAlphaMax_vs_DeltaFlap/data', size(myData'));
h5write(hdfFileName, '/DeltaAlphaMax_vs_DeltaFlap/data', myData');

% Dataset: var_1
h5create(hdfFileName, '/DeltaAlphaMax_vs_DeltaFlap/var_0', size(deltasVector'));
h5write(hdfFileName, '/DeltaAlphaMax_vs_DeltaFlap/var_0', deltasVector');