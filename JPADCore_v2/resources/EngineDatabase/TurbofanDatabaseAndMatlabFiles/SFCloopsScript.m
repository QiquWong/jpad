
clear all; close all; clc

load('BPR3_SFCloopsData.mat');

nPoints = 60;
fnMax = 1.2;
myFNVector = transpose(linspace(0.0, fnMax, nPoints));

%% M = 0
% SFCVsFnStatic = pchip(FnStatic,SFCStatic);
mySFCVsFnStatic = extendMach(fnMax,FnStatic,SFCStatic);

%% M = 0.2
% SFCVsFn02 = pchip(Fn02,SFC02);
mySFCVsFn02 = extendMach(fnMax,Fn02,SFC02);

%% M = 0.5
% SFCVsFn05 = pchip(Fn05,SFC05);
mySFCVsFn05 = extendMach(fnMax,Fn05,SFC05);

%% M = 0.7
% SFCVsFn07 = pchip(Fn07,SFC07);
mySFCVsFn07 = extendMach(fnMax,Fn07,SFC07);

%% M = 0.8
% SFCVsFn08 = pchip(Fn08,SFC08);
mySFCVsFn08 = extendMach(fnMax,Fn08,SFC08);

%% M = 0.9
% SFCVsFn09 = pchip(Fn09,SFC09);
mySFCVsFn09 = extendMach(fnMax,Fn09,SFC09);

%% M = 0.25
sfcMatrix(1,:) = mySFCVsFnStatic;
sfcMatrix(2,:) = mySFCVsFn02;
sfcMatrix(3,:) = mySFCVsFn05;
sfcMatrix(4,:) = mySFCVsFn07;
sfcMatrix(5,:) = mySFCVsFn08;
sfcMatrix(6,:) = mySFCVsFn09;

machs = [0,0.2,0.5,0.7,0.8,0.9];

mySFCVsFn025 = interpTgivenM_h(myFNVector,sfcMatrix,machs,nPoints,fnMax,0.25,1,1.)';

%% M = 0.45
mySFCVsFn045 = interpTgivenM_h(myFNVector,sfcMatrix,machs,nPoints,fnMax,0.45,1,1.)';

%% M = 0.65
mySFCVsFn065 = interpTgivenM_h(myFNVector,sfcMatrix,machs,nPoints,fnMax,0.65,1,1.)';

%% M = 0.85
mySFCVsFn085 = interpTgivenM_h(myFNVector,sfcMatrix,machs,nPoints,fnMax,0.85,1,1.)';

%% M = 0.95
mySFCVsFn095 = mySFCVsFn09;

%% GRAFICA
figure(1)
plot(...
    myFNVector, mySFCVsFnStatic, '-k',...
    myFNVector, mySFCVsFn025, '-r',...
    myFNVector, mySFCVsFn045, '-c',...
    myFNVector, mySFCVsFn065, '-b',...
    myFNVector, mySFCVsFn085, '-g',...
    myFNVector, mySFCVsFn095, '-m'...
    );

xlabel('FN/\deltaFN*.');   ylabel('SFC');
title('Bypass ratio 3.0 - SFC loops.');
legend('Mach 0','Mach 0.2','Mach 0.5','Mach 0.7','Mach 0.8','Mach 0.9');
% axis([0.2 1 0.3 1.2]);

%columns --> curves
myData(1,:,:) = [ ...
    mySFCVsFnStatic,...
    mySFCVsFn025,...
    mySFCVsFn045,...
    mySFCVsFn065,...
    mySFCVsFn085,...
    mySFCVsFn095...
    ]';


%% BPR 6.5

load('BPR65_SFCloopsData.mat');
nPoints = 60;
myFNVector = transpose(linspace(0.0, fnMax, nPoints));

%% M = 0
% FNVsSFCSplineStatic=pchip(FNStatic,SFCStatic);
myFNVsSFCStatic=extendMach(fnMax,FNStatic,SFCStatic);

%% M = 0.25
% FNVsSFCSpline025=pchip(FN025,SFC025);
myFNVsSFC025=extendMach(fnMax,FN025,SFC025);

%% M = 0.45
% FNVsSFCSpline045=pchip(FN045,SFC045);
myFNVsSFC045=extendMach(fnMax,FN045,SFC045);

%% M = 0.65
% FNVsSFCSpline065=pchip(FN065,SFC065);
myFNVsSFC065=extendMach(fnMax,FN065,SFC065);

%% M = 0.85
% FNVsSFCSpline085=pchip(FN085,SFC085);
myFNVsSFC085=extendMach(fnMax,FN085,SFC085);

%% M = 0.95
% FNVsSFCSpline095=pchip(FN095,SFC095);
myFNVsSFC095=extendMach(fnMax,FN095,SFC095);

%% GRAFICA
figure(2)
plot ( ...
    myFNVector, myFNVsSFC095, '-b', ...
    myFNVector, myFNVsSFC085, '-r', ...
    myFNVector, myFNVsSFC065, '-k', ...
    myFNVector, myFNVsSFC045, '-c', ...
    myFNVector, myFNVsSFC025, '-m', ...
    myFNVector, myFNVsSFCStatic, '-g' ...
    );
xlabel('FN'); ylabel('SFC');
title('Bypass ratio 6.5 - SFC loops.');
legend('M 0.95', 'M 0.85', 'M 0.65','M 0.45', 'M 0.25', 'M 0');

% axis([0 1 0.15 1]);


%columns --> curves
myData(2,:,:) = [ ...
    myFNVsSFCStatic,...
    myFNVsSFC025,...
    myFNVsSFC045, ...
    myFNVsSFC065,...
    myFNVsSFC085,...
    myFNVsSFC095 ...
    ]';


%% Bypass Ratio 8.0 SFC Loops (Al variare del Mach).

load('BPR8_SFCloopsData.mat');
nPoints = 60;
myFNVector = transpose(linspace(0.0, fnMax, nPoints));

%% Mach 0.95
SFCVsFN0Spline095 = pchip(FN095, SFC095);
mySFCVsFN095 = ppval(SFCVsFN0Spline095, myFNVector);

%% Mach 0.85
SFCVsFN0Spline085 = pchip(FN085, SFC085);
mySFCVsFN085 = ppval(SFCVsFN0Spline085, myFNVector);

%% Mach 0.65
SFCVsFN0Spline065 = pchip(FN065, SFC065);
mySFCVsFN065 = ppval(SFCVsFN0Spline065, myFNVector);

%% Mach 0.45
SFCVsFN0Spline045 = pchip(FN045, SFC045);
mySFCVsFN045 = ppval(SFCVsFN0Spline045, myFNVector);

%% Mach 0.25
SFCVsFN0Spline025 = pchip(FN025, SFC025);
mySFCVsFN025 = ppval(SFCVsFN0Spline025, myFNVector);

%% Static
SFCVsFN0SplineStatic = pchip(FNStatic, SFCStatic);
mySFCVsFNStatic = ppval(SFCVsFN0SplineStatic, myFNVector);


%% GRAFICA
figure(3)
plot(...
    myFNVector, mySFCVsFN095, '-b', ...
    myFNVector, mySFCVsFN085, '-r', ...
    myFNVector, mySFCVsFN065, '-y', ...
    myFNVector, mySFCVsFN045, '-g', ...
    myFNVector, mySFCVsFN025, '-c', ...
    myFNVector, mySFCVsFNStatic, '-m');
xlabel('FN/\deltaFN*');  ylabel('SFC');
title('Bypass ratio 8.0 - SFC Loops.');
legend('Mach 0.95','Mach 0.85','Mach 0.65', 'Mach 0.45', 'Mach 0.25', 'Mach 0');
% axis([0 1.3 0 0.85]);


% columns --> curves
myData(3,:,:) = [ ...
    mySFCVsFNStatic,...
    mySFCVsFN025,...
    mySFCVsFN045,...
    mySFCVsFN065, ...
    mySFCVsFN085,...
    mySFCVsFN095 ...
    ]';


%% BPR 13

load('BPR13_SFCloopsData.mat');

nPoints = 60;
myFNVector = transpose(linspace(0.0, fnMax, nPoints));

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
figure(4)
plot(...
    myFNVector, mySFCVsFnStatic, '-k',...
    myFNVector, mySFCVsFn025, '-r',...
    myFNVector, mySFCVsFn045, '-c',...
    myFNVector, mySFCVsFn065, '-b',...
    myFNVector, mySFCVsFn085, '-g',...
    myFNVector, mySFCVsFn095, '-m'...
    );

xlabel('FN/\deltaFN*');   ylabel('SFC');
title('Bypass ratio 13.0 - SFC loops.');
legend('Mach 0','Mach 0.25','Mach 0.45','Mach 0.65','Mach 0.85','Mach 0.95');
% axis([0 1.2 0.1 0.8]);

% columns --> curves
myData(4,:,:) = [ ...
    mySFCVsFnStatic,...
    mySFCVsFn025,...
    mySFCVsFn045,...
    mySFCVsFn065,...
    mySFCVsFn085,...
    mySFCVsFn095 ...
    ]';

%% WRITE EVERYTHING TO DATABASE
myHDFFile = matfile('TurbofanEngineDatabase.h5','Writable',true);

SFCloops.data = myData;
SFCloops.var_0 = [3.0,6.5,8.0,13.0];
SFCloops.var_1 = [0,0.25,0.45,0.65,0.85,0.95];
SFCloops.var_2 = myFNVector';

myHDFFile.SFCloops = SFCloops;

