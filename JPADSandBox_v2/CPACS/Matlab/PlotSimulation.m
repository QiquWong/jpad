clc; clear all; close all;
filename = 'CPACSTurboprop.csv';
CPACSTurboprop = importJSBSIMSimulation(filename);
time = CPACSTurboprop.Time;
dt = time(2)-time(1);
thrust = CPACSTurboprop.fdmjsbsimpropulsionenginethrustlbs;
thrust1 = CPACSTurboprop.fdmjsbsimpropulsionengine1thrustlbs;
deltaAileron = CPACSTurboprop.fdmjsbsimfcsrightaileronposdeg;
deltaRudder = CPACSTurboprop.fdmjsbsimfcsrudderposdeg;
deltaElevator = CPACSTurboprop.fdmjsbsimfcselevatorposdeg;
deltaFlap = CPACSTurboprop.fdmjsbsimfcsflapposdeg;
vtr = CPACSTurboprop.fdmjsbsimvelocitiesvtruekts;
uVet = CPACSTurboprop.fdmjsbsimvelocitiesufps;
vVet = CPACSTurboprop.fdmjsbsimvelocitiesvfps;
wVet = CPACSTurboprop.fdmjsbsimvelocitieswfps;
psi = CPACSTurboprop.fdmjsbsimattitudepsideg;
theta = CPACSTurboprop.fdmjsbsimattitudethetadeg;
phi = CPACSTurboprop.fdmjsbsimattitudephideg;
alpha = CPACSTurboprop.fdmjsbsimaeroalphadeg;
beta = CPACSTurboprop.fdmjsbsimaerobetadeg;
pVet = CPACSTurboprop.fdmjsbsimvelocitiesprad_sec;
qVet = CPACSTurboprop.fdmjsbsimvelocitiesqrad_sec;
rVet = CPACSTurboprop.fdmjsbsimvelocitiesrrad_sec;
altitude = CPACSTurboprop.fdmjsbsimpositionhslmeters;
cmclean = CPACSTurboprop.fdmjsbsimaeroforcespitch_basic_Mach;
czclean = CPACSTurboprop.fdmjsbsimaeroforcesz_basic_Mach;
cxclean = CPACSTurboprop.fdmjsbsimaeroforcesx_basic_Mach;
clclean = CPACSTurboprop.fdmjsbsimaeroforcesroll_basic_Mach;
cnclean = CPACSTurboprop.fdmjsbsimaeroforcesyaw_basic_Mach;
cyclean = CPACSTurboprop.fdmjsbsimaeroforcesy_basic_Mach;

cmelevator = CPACSTurboprop.fdmjsbsimaeroforceselevator_pitch_basic_Mach;
czelevator = CPACSTurboprop.fdmjsbsimaeroforceselevator_z_basic_Mach;
cxelevator = CPACSTurboprop.fdmjsbsimaeroforceselevator_x_basic_Mach;
clelevator = CPACSTurboprop.fdmjsbsimaeroforceselevator_roll_basic_Mach;
cnelevator = CPACSTurboprop.fdmjsbsimaeroforceselevator_yaw_basic_Mach;
cyelevator = CPACSTurboprop.fdmjsbsimaeroforceselevator_y_basic_Mach;

cmaileron = CPACSTurboprop.fdmjsbsimaeroforcesaileron_pitch_basic_Mach;
czaileron = CPACSTurboprop.fdmjsbsimaeroforcesaileron_z_basic_Mach;
cxaileron = CPACSTurboprop.fdmjsbsimaeroforcesaileron_x_basic_Mach;
claileron = CPACSTurboprop.fdmjsbsimaeroforcesaileron_roll_basic_Mach;
cnaileron = CPACSTurboprop.fdmjsbsimaeroforcesaileron_yaw_basic_Mach;
cyaileron = CPACSTurboprop.fdmjsbsimaeroforcesaileron_y_basic_Mach;

cmrudder = CPACSTurboprop.fdmjsbsimaeroforcesrudder_pitch_basic_Mach;
czrudder = CPACSTurboprop.fdmjsbsimaeroforcesrudder_z_basic_Mach;
cxrudder = CPACSTurboprop.fdmjsbsimaeroforcesrudder_x_basic_Mach;
clrudder = CPACSTurboprop.fdmjsbsimaeroforcesrudder_roll_basic_Mach;
cnrudder = CPACSTurboprop.fdmjsbsimaeroforcesrudder_yaw_basic_Mach;
cyrudder = CPACSTurboprop.fdmjsbsimaeroforcesrudder_y_basic_Mach;


cminner = CPACSTurboprop.fdmjsbsimaeroforcesflap_inner_pitch_basic_Mach;
czinner = CPACSTurboprop.fdmjsbsimaeroforcesflap_inner_z_basic_Mach;
cxinner = CPACSTurboprop.fdmjsbsimaeroforcesflap_inner_x_basic_Mach;
clinner = CPACSTurboprop.fdmjsbsimaeroforcesflap_inner_roll_basic_Mach;
cninner = CPACSTurboprop.fdmjsbsimaeroforcesflap_inner_yaw_basic_Mach;
cyinner = CPACSTurboprop.fdmjsbsimaeroforcesflap_inner_y_basic_Mach;

cmouter = CPACSTurboprop.fdmjsbsimaeroforcesflap_outer_pitch_basic_Mach;
czouter = CPACSTurboprop.fdmjsbsimaeroforcesflap_outer_z_basic_Mach;
cxouter = CPACSTurboprop.fdmjsbsimaeroforcesflap_outer_x_basic_Mach;
clouter = CPACSTurboprop.fdmjsbsimaeroforcesflap_outer_roll_basic_Mach;
cnouter = CPACSTurboprop.fdmjsbsimaeroforcesflap_outer_yaw_basic_Mach;
cyouter = CPACSTurboprop.fdmjsbsimaeroforcesflap_outer_y_basic_Mach;



cmq = CPACSTurboprop.fdmjsbsimaeroforcesqpitch_basic_Mach;
% czq = CPACSTurboprop.fdmjsbsimaeroforcesqz_basic_Mach;
clp = CPACSTurboprop.fdmjsbsimaeroforcesproll_basic_Mach;
cnp = CPACSTurboprop.fdmjsbsimaeroforcespyaw_basic_Mach;
cyp = CPACSTurboprop.fdmjsbsimaeroforcespy_basic_Mach;


clr = CPACSTurboprop.fdmjsbsimaeroforcesrroll_basic_Mach;
cnr = CPACSTurboprop.fdmjsbsimaeroforcesryaw_basic_Mach;
cyr = CPACSTurboprop.fdmjsbsimaeroforcesry_basic_Mach;

figure();
plot(time,alpha,'k',time,beta,'r')
legend('\alpha','\beta')
figure()
plot(time,cxclean,'b',time,cxaileron,'y',time,cxelevator,'g', ...
    time,cxrudder,'r', time,cxinner,'k', time,cxouter)
xlabel('time'); ylabel('Force (lbs)');
legend('fx','fxa','fxe','fxr','fxif','fxofe')
figure()
plot(time,cyclean,time,cyaileron,time,cyelevator, ...
    time,cyrudder, time,cyinner, time,cyouter)
legend('fy','fya','fye','fyr','fyif','fyofe')
xlabel('time'); ylabel('Force (lbs)');
figure()
plot(time,czclean,time,czaileron,time,czelevator, ...
    time,czrudder, time,czinner, time,czouter)
legend('fz','fza','fze','fzr','fzif','fzofe')
xlabel('time'); ylabel('Force (lbs)');
figure()
plot(time,cmclean,time,cmaileron,time,cmelevator, ...
    time,cmrudder, time,cminner, time,cmouter, time,cmq,time,cmclean+cmelevator)
legend('My','Mya','Mye','Myr','Myif','Myofe','Myq','totale')
xlabel('time'); ylabel('Moments (lbs*ft)');


% plot(time,cmclean,time,cmelevator,time,cmclean+cmelevator)
% legend('My','Mye','totale')
% xlabel('time'); ylabel('Moments (lbs*ft)');


figure()
plot(time,cnclean,time,cnaileron,time,cnelevator, ...
    time,cnrudder, time,cninner, time,cnouter)
legend('Mz','Mza','Mze','Mzr','Mzif','Mzofe')
xlabel('time'); ylabel('Moments (lbs*ft)');

figure()
plot(time,clclean,time,claileron,time,clelevator, ...
    time,clrudder, time,clinner, time,clouter)
legend('Mx','Mxa','Mxe','Mxr','Mxif','Mxofe')
xlabel('time'); ylabel('Moments (lbs*ft)');

figure()
plot(time,phi,'r',time,theta,'b',time,psi,'g')
xlabel('time')
legend('\phi','\theta','\psi')
figure()
plot(time,altitude)
xlabel('time'); ylabel('Position');

figure()
plot(time,vtr)
xlabel('time'); ylabel('True Velocity kts');

figure();
plot(time,thrust,'k',time,thrust1,'r')
xlabel('time'); ylabel('thrust lbs');

legend('engine 1','engine 2')

figure();
plot(time,thrust,'k',time,thrust1,'r')
xlabel('time'); ylabel('thrust lbs');

legend('engine 1','engine 2')

figure();
plot(time,deltaAileron,time,deltaElevator,time,deltaRudder,time,deltaFlap)
xlabel('time'); ylabel('Deflection deg');

legend('\delta Aileron','\delta Elevator','\delta Rudder','\delta Flap')


% %Calcolo Traiettoria
% psi0 = theta(1); theta0 = theta(1); phi0 = phi(1); %-->Condizione iniziale sugli angoli Eulero
% x0 = 0; y0 = 0; z0 = 0; zh = z; %-> Condizione iniziale sulla posizione
% t_fin = time(end); 
% 
% %Definiamo massimo delle velocità  e creiamo le funzioni anonime
% %u(t), v(t), w(t).
% 
% 
% %Costruzione delle funzioni : Definiione dei nodi e della mesh
% 
% %Costruzione delle funzioni anonime
% u = @(t) ...
%     interp1(time,uVet,t,'pchip');
% 
% %Costruzione delle funzioni anonime
% v = @(t) ...
%     interp1(time,vVet,t,'pchip');
% %Costruzione delle funzioni anonime
% w = @(t) ...
%     interp1(time,wVet,t,'pchip');
% 
% 
% %Definiamo massimo delle velocità di rotazione tramite le funzioni anonime
% %p(t), q(t), r(t).
% 
% 
% %Costruzione delle funzioni : Definiione dei nodi e della mesh
% %Si parte da p
% %Costruzione delle funzioni anonime
% p = @(t) ...
%     interp1(time,pVet,t,'pchip');
% 
% %q
% %Costruzione delle funzioni anonime
% q = @(t) ...
%     interp1(time,qVet,t,'pchip');
% % t = 0:0.1:t_fin;
% % q =interp1(ascissaq,ordinataq,t,'pchip');
% % plot(t,q)
% %r
% %Costruzione delle funzioni anonime
% r = @(t) ...
%     interp1(time,rVet,t,'pchip');
% 
% %Costruite le funzioni armoniche dobbiamo risolvere il problema
% %differenziale:
% %dq/dt = A*q --> equazione 3.32 appunti nel codice  dQuatdt = A*q 
% %Per cui costruiamo la funzione anonima da passare al toolset ODE45
% dQuatdt = @(t,Q)...
%     0.5*[0,     -p(t), -q(t), -r(t);...
%          p(t),   0,     r(t), -q(t);...
%          q(t), -r(t),   0,     p(t);...
%          r(t),  q(t),   -p(t),   0]*Q;
% %Costruiamo il quaternione iniziale a partire da psi0,theta0 e psi0
% Q0 = angle2quat(psi0,theta0,phi0);
% %Applichiamo il tool ODE45
% options = odeset( ...
%     'RelTol', 1e-9, ...
%     'AbsTol', 1e-9*ones(1,4) ...
%     );
% [vTime, vQuat] = ode45(dQuatdt, [0 t_fin], Q0, options);
% %In questo modo otteniamo la storia nel tempo dei quaternioni che plottiamo
% %%% Prova per il plot
% timeplot = linspace(0,t_fin,55);
% q0p = interp1(vTime,vQuat(:,1),timeplot,'pchip');
% q1p = interp1(vTime,vQuat(:,2),timeplot,'pchip');
% q2p = interp1(vTime,vQuat(:,3),timeplot,'pchip');
% q3p = interp1(vTime,vQuat(:,4),timeplot,'pchip');
% 
% % plot(timeplot,q0p,'.-k',vTime,vQuat(:,1),'.-k')
% figure (1)
%  plot(timeplot,q0p,'.-',timeplot,q1p,timeplot,q2p,'g',...
%      timeplot,q3p,'o k')
% legend('q_0','q_1','q_2','q_3')
% xlabel('t (s)'); ylabel('q_0,  q_1,  q_2,  q_3')
% title('Componenti Quatenrnioni nel tempo')
% print('QuaternioneLooping','-dpng')
% 
% 
% %Plot delle velocità di rotazioni 
% figure (2)
% plot( ...
%     vTime, convangvel(p(vTime),'rad/s','deg/s'),'-- k', ...
%     vTime, convangvel(q(vTime),'rad/s','deg/s'),'.- b', ...
%     vTime, convangvel(r(vTime),'rad/s','deg/s'),'-- g' ...
%     );
% legend('p','q','r')
% xlabel('t(s)'); ylabel('p(t), q(t), r(t) (Gradi/s)' )
% title('Componenti velocità angolare negli assi body')
% print('OmegaLooping','-dpng')
% 
% 
% 
% %Tramite le function di Matlab ricaviamo gli angoli di Eulero a partire
% %dalle componenti del quaternione. e plottiamo
% figure (3)
% [vpsi vtheta vphi] = quat2angle(vQuat);
% vpsip = convang(vpsi,'rad','deg');
% vthetap = convang(vtheta,'rad','deg');
% vphip = convang(vphi,'rad','deg');
% plot(vTime,vpsip,'.- ',vTime,vthetap,vTime,vphip,'--' )
% title('Valore angoli di Eulero nel tempo')
% xlabel('t (s)'); ylabel('\psi \theta \phi (°)');
% legend('\psi','\theta','\phi')
% print('EuleroLooping','-dpng')
% 
% %Passo successivo è quello di diagrammare la traiettoria del baricentro per
% %farlo si  sfruttano le equazioni cinematiche ausiliari --> equazione 3.69,
% %la matrice di trasformazione è ottenuta sfruttando la function del Matlab
% %quat2dcm, la risoluzione dell'equazione è effetuata tramite il tool ODE45
% %Dobbiamo prima ricostruire il quaternione
% Quat = @(t)...
%     [interp1(vTime,vQuat(:,1),t,'pchip'),...
%      interp1(vTime,vQuat(:,2),t,'pchip'),...
%      interp1(vTime,vQuat(:,3),t,'pchip'),...
%      interp1(vTime,vQuat(:,4),t,'pchip')];
%  %Costruiamo adesso una function anonima per la matrice T_BE
%  T_BE = @(Q)...
%      quat2dcm(Q);
%  
%   dPosEdt = @(t,Pos)...
%     transpose(quat2dcm(Quat(t)))*[u(t); v(t); w(t)];
% 
% Pos0 = [x0; y0; z0];
% %Applichiamo il tool ODE45
% % options = odeset( ...
% %     'RelTol', 1e-9, ...
% %     'AbsTol', 1e-9*ones(3,1) ...
% %     );
% % [vTime1, vPos] = ode45(dPosdt, [0 t_fin], Pos0, options);
% % 
% % 
% % timeplot1 = linspace(0,t_fin,55);
% % pos1p = interp1(vTime1,vPos(:,1),timeplot1,'pchip');
% % pos2p = interp1(vTime1,vPos(:,2),timeplot1,'pchip');
% % pos3p = interp1(vTime1,vPos(:,3),timeplot1,'pchip');
% % 
% % % plot(timeplot,q0p,'.-k',vTime,vQuat(:,1),'.-k')
% % figure (4)
% %  plot(timeplot1,pos1p,'.-k',timeplot1,pos2p,': k',...
% %      timeplot1,pos3p,'o k')
% % legend('x','y','z')
% % xlabel('t (s))'); ylabel('X  Y  Z (m)')
% % title('Componenti Quatenrnioni nel tempo')
% 
% 
% 
% %% Solution of navigation equations
% options = odeset( ...
%     'RelTol', 1e-4, ...
%     'AbsTol', 1e-4*ones(3,1) ...
%     );
% [vTime2, vPosE] = ode45(dPosEdt, vTime, Pos0, options);
%            
% vXe = vPosE(:,1); vYe = vPosE(:,2); vZe = zh+vPosE(:,3); 
% % in vZe abbiamo inserito la quota iniziale !!!
% 
% % Coordinate del baricentro
% figure(5)
% plot( ...
%     vTime2,vXe, '- ',...
%     vTime2,vYe, '-. ', ...
%     vTime2,vZe ...
%     )
% 
% legend('x_{G,E}(t)','y_{G,E}(t)','z_{G,E}(t)')
% xlabel('t (s)'); ylabel('x_{G,E}(t), y_{G,E}(t), z_{G,E}(t)   (m)');
% title('CG coordinates in Earth axes');
% print('CGLooping','-dpng')
% 
% h_fig6= figure(6);
% title('Flight path with body');
% theView = [1,10,1];
% plotTrajectoryAndBodyModLoop(h_fig6,vXe,vYe,vZe,vQuat,0.003,25,theView);
% print('TraiettoriaLooping','-dpng')
% 
% %% Setup the figure/scene for 3D visualization
% h_fig10 = figure(10);
% grid on;
% hold on;
% light('Position',[1 0 -4],'Style','local');
% % Trick to have Ze pointing downward and correct visualization
% set(gca,'XDir','reverse');
% set(gca,'ZDir','reverse');
% daspect([1 1 1]);
% 
% %% Load aircraft shape
% shapeScaleFactor = 350.0;
% %shape = loadAircraftMAT('aircraft_pa24-250.mat', scale_factor);
% shape = loadAircraftMAT('aircraft_mig29.mat', shapeScaleFactor);
% 
% mXYZe = [vPosE(:,1),vPosE(:,2),vPosE(:,3)+zh];
% mEulerAngles = [vpsi,vtheta,vphi];
% 
% %% Settings
% % General settings
% options.samples = [1,61,111,141:50:numel(vTime)]; 
% options.theView = [20 15];
% 
% % body axes settings
% options.bodyAxes.show = true;
% options.bodyAxes.magX = 1.5*shapeScaleFactor;
% options.bodyAxes.magY = 2.0*shapeScaleFactor;
% options.bodyAxes.magZ = 2.0*shapeScaleFactor;
% options.bodyAxes.lineWidth = 2.5;
% 
% % helper lines
% options.helperLines.show = true;
% options.helperLines.lineStyle = ':';
% options.helperLines.lineColor = 'k';
% options.helperLines.lineWidth = 1.5;
% 
% % trajectory
% options.trajectory.show = true;
% options.trajectory.lineStyle = '-';
% options.trajectory.lineColor = 'k';
% options.trajectory.lineWidth = 1.5;
% 
% %% Plot body and trajectory
% plotTrajectoryAndBodyE(h_fig10, shape, mXYZe, mEulerAngles, options);
% 
% %% Plot Earth axes
% hold on;
% xMax = max([max(abs(mXYZe(:,1))),5]);
% yMax = max([max(abs(mXYZe(:,2))),5]);
% zMax = 0.05*xMax; % max([abs(max(vXYZe(1))),0.18*xMax]);
% vXYZ0 = [0,0,0];
% vExtent = [xMax,yMax,zMax];
% plotEarthAxes(h_fig10, vXYZ0, vExtent);
% xlabel('x_E (m)'); ylabel('y_E (m)'); zlabel('z_E (m)')
% hold off
% print('3DLooping','-dpng')















