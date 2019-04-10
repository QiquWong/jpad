function plotTrajectoryAndBody2(h_fig,vXe,vYe,vZe,vQuat,scale_factor,step,theView,varargin)
%
%   function plotTrajectoryAndBody(vx,vy,vz,vtheta,vphi,vpsi,scale_factor,step,varargin)
%   INPUT:
%   vXe, vYe, vXe       vectors of CG coordinates (m)
%   vQuat               vector of orientation's quaternion (-)
%   scale_factor        normalization factor (scalar)
%                       (related to body aircraft dimension, <1 magnifies the body)
%   step                attitude sampling factor (scalar)
%                       (the body is shown each <step> points along the flight path)
%
%   OPTIONAL INPUT: 
%   ???                 ???
%
%   *******************************
%   Author: Agostino De Marco, Università di Napoli Federico II
%
%   Function inspired by "trajectory3" function on Matlab Central File Exchange: 
%   https://www.mathworks.com/matlabcentral/fileexchange/5656-trajectory-and-attitude-plot-version-3
%

%% Sanity checks
if (length(vXe)~=length(vYe))||(length(vXe)~=length(vZe))||(length(vYe)~=length(vZe))
    disp('  Error:');
    disp('      Wrong dimensions of CG coordinate vectors. Check the size.');
    M = 0;
    return;
end
% if ((length(vtheta)~=length(vphi))||(length(vtheta)~=length(vpsi))||(length(vphi)~=length(vpsi)))
%     disp('  Error:');
%     disp('      Wrong dimensions of the Euler''s angle vectors. Check the size.');
%     M = 0;
%     return;
% end
% if length(vtheta)~=length(vx)
if size(vQuat,1)~=length(vXe)
    disp('  Error:');
    disp('      Size mismatch between Euler''s angle vectors and CG coordinate vectors.');
    M=0;
    return
end
if isnumeric(step)
    if step>=length(vXe)
        disp('  Error:');
        disp('      Attitude sampling factor out of range. Reduce step.');
        M=0;
        return
    end
    if step<1
        step = 1;
    end
end
if isvector(step)
    if ~isempty(find(step > length(vXe)))
        disp('  Error:');
        disp('      Some indices out of range. Check.');
        M=0;
        return
    end
end


% % Trick: to visualize correctly the coordinates %%%%%%%% l'ho ricommentato io
% vXe = -vXe - min(-vXe);
% vYe = -vYe - min(-vYe);

%% From quaternion components to Euler angles
% for kk=1:size(vQuat,1)
%     dcm = quat2dcm(vQuat(kk,:));
%     for ir=1:3
%         for ic=1:3
%             vDCM(kk,ir,ic) = dcm(ir,ic);
%         end
%     end     
%     [vpsi(kk), vtheta(kk), vphi(kk)] = quat2angle(vQuat(kk,:));
% end

%% Set the camera view angle
% theView = [1,1,0.5]; % 3

%% Misc.
movie   = nargout;
cur_dir = pwd;

%% Read aircraft data file 'aircraft.mat' and prepare vertices
% see: http://www.mathworks.com/matlabcentral/fileexchange/3642
%      for functions that translate STL files into .mat with Vertices,
%      Faces and Connectivity infos

load aircraft.mat; % Coords of vertices matche with body-axes definitions

V(:,1) = V(:,1)-round(sum(V(:,1))/size(V,1));
V(:,2) = V(:,2)-round(sum(V(:,2))/size(V,1));
V(:,3) = V(:,3)-round(sum(V(:,3))/size(V,1));

Xb_nose_tip = max(abs(V(:,1)));
V = V./(scale_factor*Xb_nose_tip);

%% How many body visualizations along the flight path
if isscalar(step)
    usr_modulo = mod(length(vXe),step);

    %% The visualization loop
    frame = 0;
    for i=1:step:(length(vXe)-usr_modulo)

        if movie || (i == 1)
            clf(h_fig);
            h_fig = plot3(vXe,vYe,vZe);
            % grid on;
            hold on;
            light;
        end

        % Trick: conjugate quaternion to invert sign of rotations %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%L'ho scommentato io
        vQuat(i,2) = -vQuat(i,2); vQuat(i,3) = -vQuat(i,3); vQuat(i,4) = -vQuat(i,4); 

        % Transf. matrix from Earth- to body-axes 
        Tbe = quat2dcm(vQuat(i,:));
        % Transf. matrix from Body- to Earth-axes 
        Teb = Tbe';

        %% Vertices in body-axis coordinates
        Vb = Tbe*V';
        Vb = Vb';

        P_on_traj = [vXe(i) vYe(i) vZe(i)];
        X0 = repmat(P_on_traj,size(Vb,1),1);
        Vb = Vb + X0;

        %% plot body-axes
        % Xb = transpose( (2/scale_factor)*Tbe*[1;0;0] ); % CG-to-right wing
        Xb = transpose( ...
            (2/scale_factor)*quat2dcm( ...
                vQuat(i,:) ... % angle2quat( psi, theta, phi) ...
                )  * [1;0;0]...
            );
        % Yb = transpose( (2/scale_factor)*Tbe*[0;1;0] ); % CG-to-fuselage nose
        Yb = transpose( ...
            (2/scale_factor)*quat2dcm( ...
                vQuat(i,:) ... % angle2quat( -psi, -theta, -phi) ...
                )  * [0;1;0]...
            );
        % Zb = transpose( (1/scale_factor)*Tbe*[0;0;1] ); % Pilot's head-to-feet direction
        Zb = transpose( ...
            (2/scale_factor)*quat2dcm( ...
                vQuat(i,:) ... % angle2quat( -psi, -theta, -phi) ...
                )  * [0;0;1]...
            );
        quiver3( ...
            P_on_traj(1),P_on_traj(2),P_on_traj(3), ...
            Xb(1),Xb(2),Xb(3), ...
            'r','linewidth',2.5 ...
        ); hold on
        quiver3( ...
            P_on_traj(1),P_on_traj(2),P_on_traj(3), ...
            Yb(1),Yb(2),Yb(3), ...
            'g','linewidth',2.5 ...
        ); hold on
        quiver3( ...
            P_on_traj(1),P_on_traj(2),P_on_traj(3), ...
            Zb(1),Zb(2),Zb(3), ...
            'b','linewidth',2.5 ...
        ); hold on

        %% Display aircraft shape
        p = patch('faces', F, 'vertices' ,Vb);
        set(p, 'facec', [1 0 0]);          
        set(p, 'EdgeColor','none');
        if movie | (i == 1)
            view(theView);
            axis equal;
        end

        if movie
            if i == 1
                ax = axis;
            else
                axis(ax);
            end
            lighting phong
            frame = frame + 1;
            M(frame) = getframe;
        end
    end % end-of-for
end % end-of-if step is scalar

%% given indices

if isvector(step)
    usr_modulo = mod(length(vXe),length(step));
    
%% The visualization loop
    frame = 0;
    for is=1:length(step)
        i = step(is);
        if movie || (i == 1)
            clf(h_fig);
            h_fig = plot3(vXe,vYe,vZe);
            % grid on;
            hold on;
            light;
        end

        % Trick: conjugate quaternion to invert sign of rotations %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%L'ho scommentato io
        vQuat(i,2) = -vQuat(i,2); vQuat(i,3) = -vQuat(i,3); vQuat(i,4) = -vQuat(i,4); 

        % Transf. matrix from Earth- to body-axes 
        Tbe = quat2dcm(vQuat(i,:));
        % Transf. matrix from Body- to Earth-axes 
        Teb = Tbe';

        %% Vertices in body-axis coordinates
        Vb = Tbe*V';
        Vb = Vb';

        P_on_traj = [vXe(i) vYe(i) vZe(i)];
        X0 = repmat(P_on_traj,size(Vb,1),1);
        Vb = Vb + X0;

        %% plot body-axes
        % Xb = transpose( (2/scale_factor)*Tbe*[1;0;0] ); % CG-to-right wing
        Xb = transpose( ...
            (2/scale_factor)*quat2dcm( ...
                vQuat(i,:) ... % angle2quat( psi, theta, phi) ...
                )  * [1;0;0]...
            );
        % Yb = transpose( (2/scale_factor)*Tbe*[0;1;0] ); % CG-to-fuselage nose
        Yb = transpose( ...
            (2/scale_factor)*quat2dcm( ...
                vQuat(i,:) ... % angle2quat( -psi, -theta, -phi) ...
                )  * [0;1;0]...
            );
        % Zb = transpose( (1/scale_factor)*Tbe*[0;0;1] ); % Pilot's head-to-feet direction
        Zb = transpose( ...
            (2/scale_factor)*quat2dcm( ...
                vQuat(i,:) ... % angle2quat( -psi, -theta, -phi) ...
                )  * [0;0;1]...
            );
        quiver3( ...
            P_on_traj(1),P_on_traj(2),P_on_traj(3), ...
            Xb(1),Xb(2),Xb(3), ...
            'r','linewidth',2.5 ...
        ); hold on
        quiver3( ...
            P_on_traj(1),P_on_traj(2),P_on_traj(3), ...
            Yb(1),Yb(2),Yb(3), ...
            'g','linewidth',2.5 ...
        ); hold on
        quiver3( ...
            P_on_traj(1),P_on_traj(2),P_on_traj(3), ...
            Zb(1),Zb(2),Zb(3), ...
            'b','linewidth',2.5 ...
        ); hold on

        %% Display aircraft shape
        p = patch('faces', F, 'vertices' ,Vb);
        set(p, 'facec', [1 0 0]);          
        set(p, 'EdgeColor','none');
        if movie | (i == 1)
            view(theView);
            axis equal;
        end

        if movie
            if i == 1
                ax = axis;
            else
                axis(ax);
            end
            lighting phong
            frame = frame + 1;
            M(frame) = getframe;
        end
    end % end-of-for
end % end-of-if step is vector

hold on;

%% Plot the flight path
% some useful lines
h_fig = plot3([min(vXe) max(vXe)]*1.1, [max(vYe) max(vYe)]*1.1, [0 0], 'color', ones(3,1)*.8);
h_fig = plot3([min(vXe) max(vXe)]*1.1, [min(vYe) min(vYe)]*1.1, [0 0], 'color', ones(3,1)*.8);

h_fig = plot3([max(vXe) max(vXe)]*1.1, [min(vYe) max(vYe)]*1.1, [0 0], 'color', ones(3,1)*.8);
h_fig = plot3([min(vXe) min(vXe)]*1.1, [min(vYe) max(vYe)]*1.1, [0 0], 'color', ones(3,1)*.8);


% curves
plot3(vXe, vYe, vZe./vZe*max(vZe), '-', 'color', [.5 .5 .5]) % Ground Track
plot3(vXe, vYe./vYe*min(vYe), vZe, '-', 'color', [.5 .5 .5]) % Trajectory in a vertical plane
h_fig = plot3(vXe, vYe, vZe, 'k', 'linewidth', 1.4); % 3D trajectory

lighting phong;
% grid on;
% view(theView);
daspect([1 1 1]);

xlabel('x_E'); ylabel('y_E'); zlabel('z_E');

%% Plot Earth axes
quiver3( ...
    0,0,0, ...
    abs(1.1*max(vXe)),0,0, ...
    'r','linewidth',2.5 ...
); hold on;
quiver3( ...
    0,0,0, ...
    0,abs(1.1*max(vYe)),0, ...
    'g','linewidth',2.5 ...
); hold on;
quiver3( ...
    0,0,0, ...
    0,0,max([abs(max(vZe)),0.18*abs(max(vXe))]), ...
    'b','linewidth',2.5 ...
); hold on;

%xlim([min([-0.05*abs(max(vXe)),1.1*min(vXe)]), 1.1*max(vXe)]);
%ylim([min([-0.05*abs(max(vYe)),1.1*min(vYe)]), 1.1*max(vYe)]);
%zlim([min([-0.05*abs(max(vZe)),1.1*min(vZe)]), max([abs(max(vZe)),0.2*abs(max(vXe))])]);

set(gca,'XDir','reverse');
set(gca,'ZDir','reverse');

cd (cur_dir);
     
end