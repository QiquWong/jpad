function [ h_fig ] = plotTrajectoryAndBodyE(h_fig,shape,mXYZe,mEulerAngles,options)
%
%   function plotTrajectoryAndBodyE(h_fig,shape,mXYZe,mEulerAngles,scaleFactor,bodyAxesOptions,theView)
%   DESCRIPTION:        show a given aircraft shape in a sequence of positions/attitudes
%                       in Earth-axes; trajectory is also shown
%                       using CoG coordinates and Euler angles
%   INPUT:
%   h_fig               Matlab figure handle
%   shape               struct with fields shape.V, shape.F, shape.C
%                       (STL vertices and Face/Vertex connectivity infos)
%   mXYZe               triplets of CG coordinates (m)
%                       array Nx3
%   mEulerAngles        triplets of Euler angles (rad)
%                       array Nx3
%   options             Settings
%                       (related to body aircraft dimension, >1 magnifies the body)
%                       options.samples
%                                 the body is shown at (time) indices
%                                 sample(1), sample(2), etc
%                       options.theView
%                             [azimuth elevation], the viewpoint in the scene
%                       options.bodyAxes, body-axes settings
%                           bodyAxes.show, =1 (true) if want to draw
%                           bodyAxes.magX, magnifying factor of x-axis
%                           bodyAxes.magY, magnifying factor of y-axis
%                           bodyAxes.magZ, magnifying factor of z-axis
%                           bodyAxes.lineWidth, arrow line width
%                       options.helperLines, helper line settings
%                           helperLines.show
%                           helperLines.lineColor
%                           helperLines.lineWidth
%                           helperLines.lineStile
%                       options.trajectory, trajectory settings
%                           trajectory.show, =1 (true) if want to draw
%                           trajectory.lineColor, line color
%
%   *******************************
%   Author: Agostino De Marco, Università di Napoli Federico II
%

%% Sanity checks
if ( isempty(shape) || isempty(mXYZe) || isempty(mEulerAngles) )
    disp('  plotTrajectoryAndBodyE - Error:');
    disp('      Empty matrices.');
    return
end
if ( size(mXYZe,1)~=size(mEulerAngles,1) )
    disp('  plotTrajectoryAndBodyE - Error:');
    disp('      Size mismatch between Euler angle and CG coordinate arrays.');
    return
end
if ( (size(mXYZe,2) ~=3) || (size(mEulerAngles,2) ~= 3) )
    disp('  plotTrajectoryAndBodyE - Error:');
    disp('      Need 3 CoG coordinates and 3 Euler angles.');
    return
end

if ~isfield(options,'samples')
    disp('  plotTrajectoryAndBodyE - Error:');
    disp('      Check options.samples');
    return
end
if ~isfield(options,'theView')
    disp('  plotTrajectoryAndBodyE - Error:');
    disp('      Check options.theView');
    return
end
if ~isfield(options,'bodyAxes')
    disp('  plotTrajectoryAndBodyE - Error:');
    disp('      Check options.bodyAxes');
    return
end
if ~isfield(options,'helperLines')
    disp('  plotTrajectoryAndBodyE - Error:');
    disp('      Check options.helperLines');
    return
end
if ~isfield(options,'trajectory')
    disp('  plotTrajectoryAndBodyE - Error:');
    disp('      Check options.trajectory');
    return
end

if ~isnumeric(options.samples)
    disp('  plotTrajectoryAndBodyE - Error:');
    disp('      Check options.samples, must be a numeic vector.');
    return
end
if ~isvector(options.samples)
    disp('  plotTrajectoryAndBodyE - Error:');
    disp('      Check options.samples, must be a numeic vector.');
    return
else
    if ~isempty(find(options.samples > size(mXYZe,1)))
        disp('  plotTrajectoryAndBodyE - Error:');
        disp('      Some indices in options.samples out of range. Check.');
        return
    end
end

if ( ~isfield(options.helperLines,'show') )
    options.helperLines.show = false;
end

if ( ~isfield(options.trajectory,'show') )
    options.trajectory.show = false;
end

%------ end of sanity checks

%% The visualization loop
disp('plotTrajectoryAndBodyE -- Visualization loop:')
for is = 1:length(options.samples)
    i = options.samples(is);
    
    msg = sprintf('index: %s', num2str(i));
    disp(msg);
    
    hold on;
    plotBodyE(h_fig, ...
        shape,mXYZe(i,:),mEulerAngles(i,:), ...
        options.bodyAxes, options.theView);
    if ( options.helperLines.show)
        hold on
        plotPoint3DHelperLines(h_fig, mXYZe(i,:), options.helperLines);
    end
end % end-of-for

%% Plot the flight path
if ( options.trajectory.show )
    % some useful lines
%     hold on
%     plot3( ...
%         [min(mXYZe(:,1)) max(mXYZe(:,1))]*1.1,  ...
%         [max(mXYZe(:,2)) max(mXYZe(:,2))]*1.1, ...
%         [0 0], ...
%         'color', ones(3,1)*.8);
%     hold on
%     plot3( ...
%         [min(mXYZe(:,1)) max(mXYZe(:,1))]*1.1, ...
%         [min(mXYZe(:,2)) min(mXYZe(:,2))]*1.1, ...
%         [0 0], ...
%         'color', ones(3,1)*.8);
%     hold on
%     plot3( ...
%         [max(mXYZe(:,1)) max(mXYZe(:,1))]*1.1, ...
%         [min(mXYZe(:,2)) max(mXYZe(:,2))]*1.1, ...
%         [0 0], 'color', ones(3,1)*.8);    
    % Ground Track
    hold on
    plot3( ...
        mXYZe(:,1), mXYZe(:,2), 0.0.*mXYZe(:,3)./mXYZe(:,3)*max(mXYZe(:,3)), ...
        '-', 'color', [.5 .5 .5])
    % Trajectory in a vertical plane
    hold on
    plot3(mXYZe(:,1), mXYZe(:,2)./mXYZe(:,2)*max(mXYZe(:,2)), mXYZe(:,3), '-', 'color', [.5 .5 .5])
    % Trajectory in a vertical plane
    hold on
    plot3(mXYZe(:,1)./mXYZe(:,1)*max(mXYZe(:,1)), mXYZe(:,2), mXYZe(:,3), '-', 'color', [.5 .5 .5])
    % 3D trajectory
    hold on
    plot3(mXYZe(:,1), mXYZe(:,2), mXYZe(:,3), ...
        'LineStyle', options.trajectory.lineStyle, ...
        'Color', options.trajectory.lineColor, ...
        'LineWidth', options.trajectory.lineWidth);
end

end % end-of-function

