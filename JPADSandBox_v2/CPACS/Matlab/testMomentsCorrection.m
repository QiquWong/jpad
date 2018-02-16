clc;clear all;close all;
%% Initialize variables.
filename = 'D150_AGILECambioMomenti.csv';

dataTable = importfileMomentsCorrection(filename);

% %% Treat only the first lastElementIdx elements in data
% 
% lastElementIdx = 100;
% fnames = fieldnames(dataTable);
% for k = 1:length(fnames)-3
%     F = getfield(dataTable,fnames{k});
%     F(lastElementIdx+1:end) = [];
%     dataTable0.(fnames{k}) = F;
% end
% dataTable = dataTable0;

%% Plot
subplot(4,1,1)
plot( ...
    dataTable.Time, dataTable.fdmjsbsimvelocitiesmach ...
    )
xlabel('t (s)');
ylabel('Mach');
subplot(4,1,2)
plot( ...
    dataTable.Time, dataTable.fdmjsbsimpropulsionenginethrustlbs, ...
    dataTable.Time, dataTable.fdmjsbsimpropulsionengine1thrustlbs ...
    )
xlabel('t (s)');
ylabel('Thrust (lbs)');

subplot(4,1,3)
plot( ...
    dataTable.Time, dataTable.fdmjsbsimpositionhslmeters ...
    )
xlabel('t (s)');
ylabel('Altitude (m)');

subplot(4,1,4)
plot( ...
    dataTable.Time, dataTable.fdmjsbsimvelocitiesvtruekts ...
    )
xlabel('t (s)');
ylabel('V_T (kts)');
figure()
plot( ...
    dataTable.Time, dataTable.fdmjsbsimaeroalphadeg, ...
    dataTable.Time, dataTable.fdmjsbsimaerobetadeg ...
    )
xlabel('t (s)');
ylabel('angle (°)');
legend('\alpha','\beta');



figure()
plot( ...
    dataTable.Time, dataTable.fdmjsbsimattitudephideg, ...
    dataTable.Time, dataTable.fdmjsbsimattitudethetadeg, ...
    dataTable.Time, dataTable.fdmjsbsimattitudepsideg ...
    )
xlabel('t (s)');
ylabel('angle (°)');
legend('\phi','\theta', '\psi');
