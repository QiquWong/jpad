clc; close all; clear all;

load('BPR65_SFCloopsData.mat');

nPoints = 30;
myFNVector = transpose(linspace(0.16, 1, nPoints));

%% M = 0
FNVsSFCSplineStatic=pchip(FNStatic,SFCStatic);

myFNVsSFCStatic=ppval(FNVsSFCSplineStatic,myFNVector);

%% M = 0.25
FNVsSFCSpline025=pchip(FN025,SFC025);

myFNVsSFC025=ppval(FNVsSFCSpline025,myFNVector);

%% M = 0.45
FNVsSFCSpline045=pchip(FN045,SFC045);

myFNVsSFC045=ppval(FNVsSFCSpline045,myFNVector);

%% M = 0.65
FNVsSFCSpline065=pchip(FN065,SFC065);

myFNVsSFC065=ppval(FNVsSFCSpline065,myFNVector);

%% M = 0.85
FNVsSFCSpline085=pchip(FN085,SFC085);

myFNVsSFC085=ppval(FNVsSFCSpline085,myFNVector);

%% M = 0.95
FNVsSFCSpline095=pchip(FN095,SFC095);

myFNVsSFC095=ppval(FNVsSFCSpline095,myFNVector);

%% GRAFICA
figure(1)
plot ( ...
    myFNVector, myFNVsSFC095, '-*b', ...
    myFNVector, myFNVsSFC085, '-*r', ...
    myFNVector, myFNVsSFC065, '-*k', ...
    myFNVector, myFNVsSFC045, '-*c', ...
    myFNVector, myFNVsSFC025, '-*m', ...
    myFNVector, myFNVsSFCStatic, '-*g' ...
 );
 xlabel('FN'); ylabel('SFC');
 title('Bypass ratio 6.5 - SFC loops.');
 legend('M 0.95', 'M 0.85', 'M 0.65','M 0.45', 'M 0.25', 'M 0');
 
axis([0 1 0.15 1]);

%% preparing output to HDF

%increasing altitudes
myMachVector = [ ...
    0;0.25;0.45;0.65;0.85;0.95 ...
    ];

%columns --> curves
myData = [ ...
    myFNVsSFCStatic,myFNVsSFC025,myFNVsSFC045, ...
    myFNVsSFC065,myFNVsSFC085,myFNVsSFC095 ...
    ];

hdfFileName = 'BPR65_SFCloops_FN_Mach.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR65_SFCloops_FN_Mach/data', size(myData'));
h5write(hdfFileName, '/BPR65_SFCloops_FN_Mach/data', myData');

h5create(hdfFileName, '/BPR65_SFCloops_FN_Mach/var_0', size(myMachVector'));
h5write(hdfFileName, '/BPR65_SFCloops_FN_Mach/var_0', myMachVector');

h5create(hdfFileName, '/BPR65_SFCloops_FN_Mach/var_1', size(myFNVector'));
h5write(hdfFileName, '/BPR65_SFCloops_FN_Mach/var_1', myFNVector');
