clc; close all; clear all;

%% Import data
deltaEpsilonDeltaAlpha = importdata('deltaEpsilonDeltaAlpha.mat');

nPoints = 50;
xiOverRootChordVector = transpose(linspace(0, 4, nPoints));

%% Eta Delta v.s. delta Slat
smoothingParameter = 0.999999;
deltaEpsilonDeltaAlpha_SplineStatic = csaps( ...
    deltaEpsilonDeltaAlpha(:,1), ...
    deltaEpsilonDeltaAlpha(:,2), ...
    smoothingParameter ...
    );

deltaEpsilonDeltaAlpha_Static = ppval( ...
    deltaEpsilonDeltaAlpha_SplineStatic, ...
    xiOverRootChordVector ...
    );

%% Plots
figure(1)
plot ( ...
    xiOverRootChordVector, deltaEpsilonDeltaAlpha_Static, 'b' ... , ...
 );

xlabel('xi/c_root'); ylabel('d_\epsilon/d_\alpha');
 title('d_\epsilon/d_\alpha variation with xi/c_root (FAR stations)');
 
 axis([0 4 0 5]);
 grid on;
 %% preparing output to HDF 
 
%columns --> curves
myData = deltaEpsilonDeltaAlpha_Static;    
hdfFileName = '(C_m_alpha_b)_upwash_vs_x_i_over_root_chord.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

% Dataset: data
h5create(hdfFileName, '/(C_m_alpha_b)_upwash_vs_x_i_over_root_chord/data', size(myData'));
h5write(hdfFileName, '/(C_m_alpha_b)_upwash_vs_x_i_over_root_chord/data', myData');

% Dataset: var_1
h5create(hdfFileName, '/(C_m_alpha_b)_upwash_vs_x_i_over_root_chord/var_0', size(xiOverRootChordVector'));
h5write(hdfFileName, '/(C_m_alpha_b)_upwash_vs_x_i_over_root_chord/var_0', xiOverRootChordVector');