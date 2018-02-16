clc;clear all;close all;
%% Initialize variables.
filename = 'D150 AGILE.csv';

dataTable = importJSBSimCSV(filename);

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
ylabel('angle (�)');
legend('\alpha','\beta');



figure()
plot( ...
    dataTable.Time, dataTable.fdmjsbsimattitudephideg, ...
    dataTable.Time, dataTable.fdmjsbsimattitudethetadeg, ...
    dataTable.Time, dataTable.fdmjsbsimattitudepsideg ...
    )
xlabel('t (s)');
ylabel('angle (�)');
legend('\phi','\theta', '\psi');



figure()
% Start Force
plot( ...           
    dataTable.Time, dataTable.fdmjsbsimaeroforcesaileron_drag_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforceselevator_drag_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesrudder_drag_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesflap_outer_drag_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesflap_inner_drag_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesdrag_basic_Mach ...
    )
xlabel('t (s)');
ylabel('Drag (psf)');
legend('aileron','elevator','rudder','outerFlap','innerFlap', 'clean');

figure()
plot( ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesaileron_lift_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforceselevator_lift_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesrudder_lift_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesflap_outer_lift_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesflap_inner_lift_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforceslift_basic_Mach ...
    )
xlabel('t (s)');
ylabel('lift (psf)');
legend('aileron','elevator','rudder','outerFlap','innerFlap', 'clean');

figure()
plot( ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesaileron_side_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforceselevator_side_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesrudder_side_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesflap_outer_side_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesflap_inner_side_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesside_basic_Mach ...
    )
xlabel('t (s)');
ylabel('side (psf)');
legend('aileron','elevator','rudder','outerFlap','innerFlap', 'clean');

figure()
plot( ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesaileron_roll_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforceselevator_roll_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesrudder_roll_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesflap_outer_roll_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesflap_inner_roll_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesroll_basic_Mach ...
    )
xlabel('t (s)');
ylabel('roll (psf*ft)');
legend('aileron','elevator','rudder','outerFlap','innerFlap', 'clean');

figure()
plot( ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesaileron_pitch_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforceselevator_pitch_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesrudder_pitch_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesflap_outer_pitch_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesflap_inner_pitch_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcespitch_basic_Mach ...
    )
xlabel('t (s)');
ylabel('pitch (psf*ft)');
legend('aileron','elevator','rudder','outerFlap','innerFlap', 'clean');

figure()
plot( ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesaileron_yaw_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforceselevator_yaw_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesrudder_yaw_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesflap_outer_yaw_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesflap_inner_yaw_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaeroforcesyaw_basic_Mach ...
    )
xlabel('t (s)');
ylabel('yaw (psf*ft)');
legend('aileron','elevator','rudder','outerFlap','innerFlap', 'clean');
% %Start Force coefficient
figure()

plot( ...           
    dataTable.Time, dataTable.fdmjsbsimaerofunctionaileron_drag_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionelevator_drag_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionrudder_drag_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionflap_outer_drag_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionflap_inner_drag_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctiondrag_coeff_basic_Mach ...
    )
xlabel('t (s)');
ylabel('C_D');
legend('aileron','elevator','rudder','outerFlap','innerFlap', 'clean');

figure()
plot( ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionaileron_lift_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionelevator_lift_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionrudder_lift_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionflap_outer_lift_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionflap_inner_drag_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionlift_coeff_basic_Mach ...
    )
xlabel('t (s)');
ylabel('C_L');
legend('aileron','elevator','rudder','outerFlap','innerFlap', 'clean');

figure()
plot( ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionaileron_side_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionelevator_side_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionrudder_side_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionflap_outer_side_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionflap_inner_side_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionside_coeff_basic_Mach ...
    )
xlabel('t (s)');
ylabel('C_Y');
legend('aileron','elevator','rudder','outerFlap','innerFlap', 'clean');

figure()
plot( ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionaileron_roll_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionelevator_roll_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionrudder_roll_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionflap_outer_roll_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionflap_inner_roll_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionroll_coeff_basic_Mach ...
    )
xlabel('t (s)');
ylabel('C_{roll}');
legend('aileron','elevator','rudder','outerFlap','innerFlap', 'clean');

figure()
plot( ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionaileron_pitch_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionelevator_pitch_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionrudder_pitch_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionflap_outer_pitch_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionflap_inner_pitch_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionpitch_coeff_basic_Mach ...
    )
xlabel('t (s)');
ylabel('C_M');
legend('aileron','elevator','rudder','outerFlap','innerFlap', 'clean');

figure()
plot( ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionaileron_yaw_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionelevator_yaw_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionrudder_yaw_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionflap_outer_yaw_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionflap_inner_yaw_coeff_basic_Mach, ...
    dataTable.Time, dataTable.fdmjsbsimaerofunctionyaw_coeff_basic_Mach ...
    )
xlabel('t (s)');
ylabel('C_N');
legend('aileron','elevator','rudder','outerFlap','innerFlap', 'clean');