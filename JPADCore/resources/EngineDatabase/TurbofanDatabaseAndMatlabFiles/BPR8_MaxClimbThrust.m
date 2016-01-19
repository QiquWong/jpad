%% Bypass 8.0 maximum climb Thrust.

load('BPR8_MaxClimbThrustData.mat');

nPoints = 30;
myMachVector = transpose(linspace(0, 0.888, nPoints));
%% 45k ft
thrustVsMachMCSpline45 = pchip(MachMC45, ThrustMC45);

myThrustVsMachMC45 = ppval(thrustVsMachMCSpline45, myMachVector);

%% 40k ft
thrustVsMachSplineMC40 = pchip(MachMC40, ThrustMC40);

myThrustVsMachMC40 = ppval(thrustVsMachSplineMC40, myMachVector);

%% 36k ft
thrustVsMachSplineMC36 = pchip(MachMC36, ThrustMC36);

myThrustVsMachMC36 = ppval(thrustVsMachSplineMC36, myMachVector);

%% 30k ft
thrustVsMachSplineMC30 = pchip(MachMC30, ThrustMC30);

myThrustVsMachMC30 = ppval(thrustVsMachSplineMC30, myMachVector);



%% 25k ft
thrustVsMachMCSpline25 = pchip(MachMC25, ThrustMC25);

myThrustVsMachMC25 = ppval(thrustVsMachMCSpline25, myMachVector);

%% 15k ft
thrustVsMachMCSpline15 = pchip(MachMC15, ThrustMC15);

myThrustVsMachMC15 = ppval(thrustVsMachMCSpline15, myMachVector);

%% 10k ft
thrustVsMachMCSpline10 = pchip(MachMC10, ThrustMC10);

myThrustVsMachMC10 = ppval(thrustVsMachMCSpline10, myMachVector);

%% SL
thrustVsMachMCSplineSL = pchip(MachMCSL, ThrustMCSL);

myThrustVsMachMCSL = ppval(thrustVsMachMCSplineSL, myMachVector);

%% GRAFICA
figure(1)
plot ( ...
       myMachVector, myThrustVsMachMC45, '.-b', ...
       myMachVector, myThrustVsMachMC40, '.-r', ...
       myMachVector, myThrustVsMachMC36, '.-c', ...
       myMachVector, myThrustVsMachMC30, '.-y', ...
       myMachVector, myThrustVsMachMC25, '.-k', ...
       myMachVector, myThrustVsMachMC15, '.-b', ...
       myMachVector, myThrustVsMachMC10, '.-g', ... 
       myMachVector, myThrustVsMachMCSL, '.-m'  ...
       );
  xlabel('Mach No');  ylabel('Thrust Ratio');
  title('Bypass ratio 3.0 - maximum cruise thrust.');
  legend('45000 ft','40000 ft','36000 ft', '30000 ft','25000 ft','15000', '10000 ft', '0 ft');
axis([0 0.888 0 0.9]);

%% Preparing output to HDF
% increasing altitudes
myAltitudeVector_FT = flipud([ ...
    45000;40000;36000;30000;25000;15000;10000;0 ...
    ]);

% columns --> curves
myData = [ ...
    myThrustVsMachMCSL,myThrustVsMachMC10,myThrustVsMachMC15,myThrustVsMachMC25,myThrustVsMachMC30,myThrustVsMachMC36, ...
        myThrustVsMachMC40,myThrustVsMachMC45 ...
    ];

hdfFileName = 'BPR8_MaxClimbThrust_Mach_Altitude.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/BPR8_MaxClimbThrust_Mach_Altitude/data', size(myData'));
h5write(hdfFileName, '/BPR8_MaxClimbThrust_Mach_Altitude/data', myData');

h5create(hdfFileName, '/BPR8_MaxClimbThrust_Mach_Altitude/var_0', size(myAltitudeVector_FT'));
h5write(hdfFileName, '/BPR8_MaxClimbThrust_Mach_Altitude/var_0', myAltitudeVector_FT');

h5create(hdfFileName, '/BPR8_MaxClimbThrust_Mach_Altitude/var_1', size(myMachVector'));
h5write(hdfFileName, '/BPR8_MaxClimbThrust_Mach_Altitude/var_1', myMachVector');
