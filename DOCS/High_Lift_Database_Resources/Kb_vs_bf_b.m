clc; close all; clear all;

%% Import data
Kb = importdata('Kb.mat');

nPoints = 50;
bf_b_Vector = transpose(linspace(0, 1, nPoints));

%% Eta Delta v.s. delta Slat
smoothingParameter = 0.999999;
Kb_SplineStatic = csaps( ...
    Kb(:,1), ...
    Kb(:,2), ...
    smoothingParameter ...
    );

Kb_Static = ppval( ...
    Kb_SplineStatic, ...
    bf_b_Vector ...
    );

%% Plots
figure(1)
plot ( ...
    bf_b_Vector, Kb_Static, 'b' ... , ...
 );

xlabel('b_f/b'); ylabel('Kb');
 title('Kb coefficient variation with flap span ratio');
 
 axis([0 1 0 1]);
 grid on;
 %% preparing output to HDF 
 
%columns --> curves
myData = Kb_Static;    
hdfFileName = 'Kb_vs_flapSpanRatio.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

% Dataset: data
h5create(hdfFileName, '/Kb_vs_flapSpanRatio/data', size(myData'));
h5write(hdfFileName, '/Kb_vs_flapSpanRatio/data', myData');

% Dataset: var_1
h5create(hdfFileName, '/Kb_vs_flapSpanRatio/var_0', size(bf_b_Vector'));
h5write(hdfFileName, '/Kb_vs_flapSpanRatio/var_0', bf_b_Vector');