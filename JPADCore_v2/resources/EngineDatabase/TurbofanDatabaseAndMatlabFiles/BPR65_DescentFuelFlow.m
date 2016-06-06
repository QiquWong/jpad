
clear all; close all;

load('BPR65_DescentFuelFlowData.mat');

nPoints = 30;

myMachVector = transpose(linspace(0, 0.888, nPoints));
%% 45k
FuelFlowVsMachSpline45 = pchip(Mach45,FuelFlow45);


myFuelFlowVsMach45 = ppval(FuelFlowVsMachSpline45,myMachVector);



%% 40k 
FuelFlowVsMachSpline40 = pchip(Mach40,FuelFlow40);


myFuelFlowVsMach40 = ppval(FuelFlowVsMachSpline40,myMachVector);


%% 36k
FuelFlowVsMachSpline36 = pchip(Mach36,FuelFlow36);


myFuelFlowVsMach36 = ppval(FuelFlowVsMachSpline36,myMachVector);



%% 30k
FuelFlowVsMachSpline30 = pchip(Mach30,FuelFlow30);


myFuelFlowVsMach30 = ppval(FuelFlowVsMachSpline30,myMachVector);



%% 25k
FuelFlowVsMachSpline25 = pchip(Mach25,FuelFlow25);


myFuelFlowVsMach25 = ppval(FuelFlowVsMachSpline25,myMachVector);



%% 15k 
FuelFlowVsMachSpline15 = pchip(Mach15,FuelFlow15);


myFuelFlowVsMach15 = ppval(FuelFlowVsMachSpline15,myMachVector);



%% 10 k
FuelFlowVsMachSpline10 = pchip(Mach10,FuelFlow10);


myFuelFlowVsMach10 = ppval(FuelFlowVsMachSpline10,myMachVector);



%% 0
FuelFlowVsMachSplineSL = pchip(MachSL,FuelFlowSL);

myFuelFlowVsMachSL = ppval(FuelFlowVsMachSplineSL,myMachVector);



%% GRAFICA

figure(1)
plot(...
    myMachVector, myFuelFlowVsMachSL, '.-k',...
    myMachVector, myFuelFlowVsMach10, '.-r',...
    myMachVector, myFuelFlowVsMach15, '.-c',...
    myMachVector, myFuelFlowVsMach25, '.-b',...
    myMachVector, myFuelFlowVsMach30, '.-g',...
    myMachVector, myFuelFlowVsMach36, '.-y',...
    myMachVector, myFuelFlowVsMach40, '.-r',...
    myMachVector, myFuelFlowVsMach45, '.-m'...
    );

xlabel('Mach no.');   ylabel('Fuel Flow');
axis([0 0.9 0.01 0.06]);
set(gca,'YDir','Reverse');
title('Bypass ratio 6.5 - descent fuel flow.');
legend('0 ft','10000 ft','15000 ft','25000 ft','30000 ft','36000 ft','40000 ft', '45000 ft');

%% preparing output to HDF

%increasing altitudes
myAltitudeVector_FT = [ ...
    0;10000;15000;25000;30000;36000;40000;45000 ...
    ];

%columns --> curves
myData = [ ...
    myFuelFlowVsMachSL,myFuelFlowVsMach10,myFuelFlowVsMach15,...
    myFuelFlowVsMach25,myFuelFlowVsMach30,myFuelFlowVsMach36,...
    myFuelFlowVsMach40,myFuelFlowVsMach45...
    ];

hdfFileName = 'BPR65_DescentFuelFlow_Mach_Altitude.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR65_DescentFuelFlow_Mach_Altitude/data', size(myData'));
h5write(hdfFileName, '/BPR65_DescentFuelFlow_Mach_Altitude/data', myData');

h5create(hdfFileName, '/BPR65_DescentFuelFlow_Mach_Altitude/var_0', size(myAltitudeVector_FT'));
h5write(hdfFileName, '/BPR65_DescentFuelFlow_Mach_Altitude/var_0', myAltitudeVector_FT');

h5create(hdfFileName, '/BPR65_DescentFuelFlow_Mach_Altitude/var_1', size(myMachVector'));
h5write(hdfFileName, '/BPR65_DescentFuelFlow_Mach_Altitude/var_1', myMachVector');