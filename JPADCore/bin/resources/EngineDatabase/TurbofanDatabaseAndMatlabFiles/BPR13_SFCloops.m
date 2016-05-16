
clc; clear all; close all;

load('BPR13_SFCloopsData.mat');

nPoints = 30;
myFNVector = transpose(linspace(0.2, 1.2, nPoints));
%%
SFCVsFnSplineStatic = pchip(FnStatic,SFCStatic);


mySFCVsFnStatic = ppval(SFCVsFnSplineStatic,myFNVector);

%%
SFCVsFnSpline025 = pchip(Fn025,SFC025);


mySFCVsFn025 = ppval(SFCVsFnSpline025,myFNVector);


%%
SFCVsFnSpline045 = pchip(Fn045,SFC045);


mySFCVsFn045 = ppval(SFCVsFnSpline045,myFNVector);

%%
SFCVsFnSpline065 = pchip(Fn065,SFC065);


mySFCVsFn065 = ppval(SFCVsFnSpline065,myFNVector);
%%
SFCVsFnSpline085 = pchip(Fn085,SFC085);


mySFCVsFn085 = ppval(SFCVsFnSpline085,myFNVector);

%%
SFCVsFnSpline095 = pchip(Fn095,SFC095);


mySFCVsFn095 = ppval(SFCVsFnSpline095,myFNVector);



%% GRAFICA

plot(...
    myFNVector, mySFCVsFnStatic, '.-k',...
    myFNVector, mySFCVsFn025, '.-r',...
    myFNVector, mySFCVsFn045, '.-c',...
    myFNVector, mySFCVsFn065, '.-b',...
    myFNVector, mySFCVsFn085, '.-g',...
    myFNVector, mySFCVsFn095, '.-m'...
    );
    
xlabel('FN/\deltaFN*');   ylabel('SFC');
title('Bypass ratio 13.0 - SFC loops.');
legend('Mach 0','Mach 0.25','Mach 0.45','Mach 0.65','Mach 0.85','Mach 0.95');
axis([0 1.2 0.1 0.8]);

%% Preparing output to HDF
% increasing Mach
myMachVector = [ ...
    0;0.25;0.45;0.65;0.85;0.95 ...
    ];

% columns --> curves
myData = [ ...
    mySFCVsFnStatic,mySFCVsFn025,mySFCVsFn045,...
    mySFCVsFn065,mySFCVsFn085,mySFCVsFn095 ...
    ];

hdfFileName = 'BPR13_SFCloops_FN_Mach.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR13_SFCloops_FN_Mach/data', size(myData'));
h5write(hdfFileName, '/BPR13_SFCloops_FN_Mach/data', myData');

h5create(hdfFileName, '/BPR13_SFCloops_FN_Mach/var_0', size(myMachVector'));
h5write(hdfFileName, '/BPR13_SFCloops_FN_Mach/var_0', myMachVector');

h5create(hdfFileName, '/BPR13_SFCloops_FN_Mach/var_1', size(myFNVector'));
h5write(hdfFileName, '/BPR13_SFCloops_FN_Mach/var_1', myFNVector');

% print -dpng 'plots.png';
% cdata = print('-dpng');
% 
% imwrite(cdata,hdfFileName,'HDF');

% h5create(hdfFileName, '/DescentFluentFlow_Mach_Altitude/plots', size(cdata));
% h5write(hdfFileName, '/DescentFluentFlow_Mach_Altitude/plots', cdata');
