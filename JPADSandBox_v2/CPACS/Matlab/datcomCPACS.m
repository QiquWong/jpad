clc; clear all; close all;
x = [0.172770;0.102270;0.039667;-0.034926;-0.121130;-0.218620;-0.327180;-0.446680;-0.577180;-0.793200]
y = [-5;-2; 0; 2; 4; 6; 8;10;12;15];
alphaDatcom = 57.3*[-.8727E-01
-.3491E-01
  .000    
 .3491E-01
 .6981E-01
 .1396    
 .1571    
 .1745    
 .2182 ];   

%C_Roll 
filename = 'C_Roll.txt';
j = 3;
machVector = importRow(filename,1); machLength = length(machVector);
altitudeVector = importRow(filename,2); altitudeLength = length(altitudeVector);
betaVector = importRow(filename,3)/57.3; betaLength = length(betaVector);
alphaVector = importRow(filename,4)/57.3; alphaLength = length(alphaVector);
datcomCL = -.09;%rad
counter = 6;
figure()
matrixCL = importMatrix(filename,counter,alphaLength,betaLength);
plot(betaVector,matrixCL(j,:),'b',betaVector, datcomCL*betaVector,'k');
legend('CPACS','Datcom')
xlabel('\beta (rad)'); ylabel('C_{Roll}');
disp((matrixCL(3,2)/(datcomCL*betaVector(2))));
%C_M
filename = 'C_M.txt';
counter = 6;
j = 1;
figure()
cmDatcom = -0.6;%Rad
matrixCM = importMatrix(filename,counter,alphaLength,betaLength);
plot(alphaVector,matrixCM(:,j),'b', alphaVector, cmDatcom*alphaVector, 'k',y/57.3,x,'g');
xlabel('\alpha (rad)'); ylabel('C_M');
legend('CPACS','Datcom')
disp((x(5,1)/(cmDatcom*alphaVector(5))));
disp((matrixCM(5,1)/(cmDatcom*alphaVector(5))));


%C_N 
filename = 'C_N.txt';
cnDatcom = 0.26;%rad
j = 3;
counter = 6;
counterPlot = 1;
figure()
matrixCN = importMatrix(filename,counter,alphaLength,betaLength);
plot(betaVector,matrixCN(j,:),'b',betaVector, cnDatcom*betaVector,'k');
xlabel('\beta (rad)'); ylabel('C_N');
legend('CPACS','Datcom')
disp((matrixCN(3,2)/(cnDatcom*betaVector(2))));

