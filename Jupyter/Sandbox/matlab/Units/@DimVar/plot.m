function [h, xLabelUnitString, yLabelUnitString] = plot(varargin)
% DimVar.plot plotting method that automatically labels axes with units of
% DimVar inputs.
%   Used the same as plot, but accepts DimVar inputs. Multiple inputs of X
%   and Y must be compatible units. The workaround for this is the use the
%   hold function.
% 
%   [h, xLabelUnitString, yLabelUnitString] = plot(...) in addition to the
%   handle, returns the strings used by the function for labeling the
%   axes. The string is empty if an axis did not have a DimVar input.
% 
%   Example:
%     units
%     edgeL = (0:5)*u.in;
%     squareA = edgeL.^2;
%     triangleA = edgeL.^2*sqrt(3)/4;
%     [~,xStr,yStr] = plot(edgeL,squareA,'s',edgeL,triangleA,'^');
% 
%   See also plot, xlabel, ylabel, text, units, DimVar.num2str.

%   Copyright 2014 Sky Sartorius
%   www.mathworks.com/matlabcentral/fileexchange/authors/101715 

specInd = strncmp('',varargin,0);
args = varargin(~specInd);

nArgs = length(args);
nPairs = nArgs/2;
    
xs = '';
ys = '';

if length(args) == 1
    % Special case of only one argument.
    [~, ys] = num2str(args{1});
    args{1} = args{1}.value;
else
    firstX = args{1};
    if isa(firstX,'DimVar')
    [~, xs, firstX] = num2str(firstX);
    args{1} = firstX.value;
    end
    firstY = args{2};
    if isa(firstY,'DimVar')
    [~, ys, firstY] = num2str(firstY);
    args{2} = firstY.value;
    end
    

    % Check to make sure nPairs is an integer.
    if nPairs ~= round(nPairs)
        error('Data must be a single matrix Y or a list of pairs X,Y.')
    end
end

for i = 2:nPairs
    x = args{2*i-1};
    y = args{2*i};
    
    % Check compatibility.
    if isa(x,'DimVar')
        compatible(firstX, x);
        [~, ~, x] = display(x);
        args{2*i-1} = x.value;
    end
    if isa(y,'DimVar')
        compatible(firstY, y);
        [~, ~, y] = display(y);
        args{2*i} = y.value;
    end
end
   
varargin(~specInd) = args;

s = regexprep({xs ys},{'(' ')'},{'{' '}'});
xLabelUnitString = s{1};
yLabelUnitString = s{2};


try
    h_ = plot(varargin{:});
    a = gca;
    if ~isempty(xLabelUnitString)
        a.XAxis.TickLabelFormat = ['%g ' xLabelUnitString]; % R2015b
        %     xlabel(xLabelUnitString) % Prior versions.
    end
    if ~isempty(yLabelUnitString)
        a.YAxis.TickLabelFormat = ['%g ' yLabelUnitString]; % R2015b
        %     ylabel(yLabelUnitString) % Prior versions.
    end
    
catch
    warning(['DimVar.plot in development and designed for R2015b. '...
        'Convert DimVar to double then use normal plot function.'])
end

if nargout
    h = h_;
end

end