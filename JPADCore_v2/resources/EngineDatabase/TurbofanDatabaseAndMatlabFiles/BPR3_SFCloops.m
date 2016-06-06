
clear all; close all;

load('BPR3_SFCloopsData.mat');

nPoints = 30;
myFNVector = transpose(linspace(0.3, 1, nPoints));

%%
SFCVsFnStatic = pchip(FnStatic,SFCStatic);
mySFCVsFnStatic = ppval(SFCVsFnStatic,myFNVector);

%%
SFCVsFn02 = pchip(Fn02,SFC02);
mySFCVsFn02 = ppval(SFCVsFn02,myFNVector);

%%
SFCVsFn05 = pchip(Fn05,SFC05);
mySFCVsFn05 = ppval(SFCVsFn05,myFNVector);

%%
SFCVsFn07 = pchip(Fn07,SFC07);
mySFCVsFn07 = ppval(SFCVsFn07,myFNVector);

%%
SFCVsFn08 = pchip(Fn08,SFC08);
mySFCVsFn08 = ppval(SFCVsFn08,myFNVector);

%%
SFCVsFn09 = pchip(Fn09,SFC09);
mySFCVsFn09 = ppval(SFCVsFn09,myFNVector);

%% GRAFICA

plot(...
    myFNVector, mySFCVsFnStatic, '.-k',...
    myFNVector, mySFCVsFn02, '.-r',...
    myFNVector, mySFCVsFn05, '.-c',...
    myFNVector, mySFCVsFn07, '.-b',...
    myFNVector, mySFCVsFn08, '.-g',...
    myFNVector, mySFCVsFn09, '.-m'...
    );
    
xlabel('FN/\deltaFN*.');   ylabel('SFC');
title('Bypass ratio 3.0 - SFC loops.');
legend('Mach 0','Mach 0.2','Mach 0.5','Mach 0.7','Mach 0.8','Mach 0.9');
axis([0.2 1 0.3 1.2]);

%columns --> curves
myData = [ ...
    mySFCVsFnStatic,...
	mySFCVsFn02,...
	mySFCVsFn05,...
    mySFCVsFn07,...
	mySFCVsFn08,...
	mySFCVsFn09...
    ];
   

%% BPR 6.5

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


%columns --> curves
myData = [ ...
    myFNVsSFCStatic,...
	myFNVsSFC025,...
	myFNVsSFC045, ...
    myFNVsSFC065,...
	myFNVsSFC085,...
	myFNVsSFC095 ...
    ];


%% Bypass Ratio 8.0 SFC Loops (Al variare del Mach).

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
    mySFCVsFNStatic,...
	mySFCVsFN025,...
	mySFCVsFN045,...
	mySFCVsFN065, ...
        mySFCVsFN085,...
		mySFCVsFN095 ...
    ];


%% BPR 13

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

