%% Bypass Ratio 8.0 SFC Loops (Al variare del Mach).
close all; clear all; clc;

load('BPR8_SFCloopsData.mat');
nPoints = 30;
myFNVector_N = transpose(linspace(0.15, 1.2, nPoints));

%% Mach 0.95
SFCVsFN0Spline095 = pchip(FN095, SFC095);

mySFCVsFN095 = ppval(SFCVsFN0Spline095, myFNVector_N);

%% Mach 0.85
SFCVsFN0Spline085 = pchip(FN085, SFC085);

mySFCVsFN085 = ppval(SFCVsFN0Spline085, myFNVector_N);

%% Mach 0.65

SFCVsFN0Spline065 = pchip(FN065, SFC065);

mySFCVsFN065 = ppval(SFCVsFN0Spline065, myFNVector_N);

%% Mach 0.45

SFCVsFN0Spline045 = pchip(FN045, SFC045);

mySFCVsFN045 = ppval(SFCVsFN0Spline045, myFNVector_N);

%% Mach 0.25

SFCVsFN0Spline025 = pchip(FN025, SFC025);

mySFCVsFN025 = ppval(SFCVsFN0Spline025, myFNVector_N);

%% Static

SFCVsFN0SplineStatic = pchip(FNStatic, SFCStatic);

mySFCVsFNStatic = ppval(SFCVsFN0SplineStatic, myFNVector_N);


%% GRAFICA
figure(1)
plot(...
     myFNVector_N, mySFCVsFN095, '.-b', ...
     myFNVector_N, mySFCVsFN085, '.-r', ...
     myFNVector_N, mySFCVsFN065, '.-y', ...
     myFNVector_N, mySFCVsFN045, '.-g', ...
     myFNVector_N, mySFCVsFN025, '.-c', ...
     myFNVector_N, mySFCVsFNStatic, '.-m');
 xlabel('FN/\deltaFN*');  ylabel('SFC');
 title('Bypass ratio 8.0 - SFC Loops.');
 legend('Mach 0.95','Mach 0.85','Mach 0.65', 'Mach 0.45', 'Mach 0.25', 'Mach 0');
 axis([0 1.3 0 0.85]);
 
 %% Preparing output to HDF
% increasing altitudes
myMachVector = [ ...
    0.95;0.85;0.65;0.45;0.25;0 ...
    ];

% columns --> curves
myData = [ ...
    mySFCVsFNStatic,mySFCVsFN025,mySFCVsFN045,mySFCVsFN065, ...
        mySFCVsFN085,mySFCVsFN095 ...
    ];

hdfFileName = 'BPR8_SFCloops_FN_Mach.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR8_SFCloops_Thrust_Mach/data', size(myData'));
h5write(hdfFileName, '/BPR8_SFCloops_Thrust_Mach/data', myData');

h5create(hdfFileName, '/BPR8_SFCloops_Thrust_Mach/var_0', size(myMachVector'));
h5write(hdfFileName, '/BPR8_SFCloops_Thrust_Mach/var_0', myMachVector');

h5create(hdfFileName, '/BPR8_SFCloops_Thrust_Mach/var_1', size(myFNVector_N'));
h5write(hdfFileName, '/BPR8_SFCloops_Thrust_Mach/var_1', myFNVector_N');
