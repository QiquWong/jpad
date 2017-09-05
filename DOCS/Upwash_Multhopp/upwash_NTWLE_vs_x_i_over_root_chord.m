clc; close all; clear all;

%% Import data
deltaEpsilonDeltaAlphaNear = importdata('deltaEpsilonDeltaAlphaNear.mat');

nPoints = 50;
xiOverRootChordVector = transpose(linspace(0.1, 2, nPoints));

%% Eta Delta v.s. delta Slat
smoothingParameter = 0.999999;
deltaEpsilonDeltaAlphaNear_SplineStatic = csaps( ...
    deltaEpsilonDeltaAlphaNear(:,1), ...
    deltaEpsilonDeltaAlphaNear(:,2), ...
    smoothingParameter ...
    );

deltaEpsilonDeltaAlphaNear_Static = ppval( ...
    deltaEpsilonDeltaAlphaNear_SplineStatic, ...
    xiOverRootChordVector ...
    );

%% Plots
figure(1)
plot ( ...
    xiOverRootChordVector, deltaEpsilonDeltaAlphaNear_Static, 'b' ... , ...
 );

xlabel('xi/c_root'); ylabel('d_\epsilon/d_\alpha');
 title('d_\epsilon/d_\alpha variation with xi/c_root (NEAR stations)');
 
 axis([0 4 0 5]);
 grid on;
 %% preparing output to HDF 
 
%columns --> curves
myData = deltaEpsilonDeltaAlphaNear_Static;    
hdfFileName = '(C_m_alpha_b)_upwash_(NTWLE)_vs_x_i_over_root_chord.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

% Dataset: data
h5create(hdfFileName, '/(C_m_alpha_b)_upwash_(NTWLE)_vs_x_i_over_root_chord/data', size(myData'));
h5write(hdfFileName, '/(C_m_alpha_b)_upwash_(NTWLE)_vs_x_i_over_root_chord/data', myData');

% Dataset: var_1
h5create(hdfFileName, '/(C_m_alpha_b)_upwash_(NTWLE)_vs_x_i_over_root_chord/var_0', size(xiOverRootChordVector'));
h5write(hdfFileName, '/(C_m_alpha_b)_upwash_(NTWLE)_vs_x_i_over_root_chord/var_0', xiOverRootChordVector');