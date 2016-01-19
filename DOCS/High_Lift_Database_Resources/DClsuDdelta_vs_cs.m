clc; close all; clear all;

%% Import data
DClsuDdelta_vs_SlatChordRatio = importdata('DeltaClDdelta.mat');

nPoints = 50;
slatChordRatioVector = transpose(linspace(0.0125, 0.4, nPoints));

%% Dcl/Ddelta
smoothingParameter = 0.999999;
DClsuDdelta_vs_SlatChordRatio_SplineStatic = csaps( ...
    DClsuDdelta_vs_SlatChordRatio(:,1), ...
    DClsuDdelta_vs_SlatChordRatio(:,2), ...
    smoothingParameter ...
    );

DClsuDdelta_vs_SlatChordRatio_Static = ppval( ...
    DClsuDdelta_vs_SlatChordRatio_SplineStatic, ...
    slatChordRatioVector ...
    );

%% Plots
figure(1)
plot ( ...
    slatChordRatioVector, DClsuDdelta_vs_SlatChordRatio_Static, 'b' ... , ...
 );

 xlabel('c_s/c'); ylabel('dc_l/d\delta');
 title('dc_l/d\delta variation with slat chord ratio');
 
 axis([0 0.4 0 0.035]);
 grid on;
 %% preparing output to HDF
 
%columns --> curves
myData = DClsuDdelta_vs_SlatChordRatio_Static;    
hdfFileName = 'DClsuDdelta_vs_SlatChordRatio.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

% Dataset: data
h5create(hdfFileName, '/DClsuDdelta_vs_SlatChordRatio/data', size(myData'));
h5write(hdfFileName, '/DClsuDdelta_vs_SlatChordRatio/data', myData');

% Dataset: var_1
h5create(hdfFileName, '/DClsuDdelta_vs_SlatChordRatio/var_0', size(slatChordRatioVector'));
h5write(hdfFileName, '/DClsuDdelta_vs_SlatChordRatio/var_0', slatChordRatioVector');