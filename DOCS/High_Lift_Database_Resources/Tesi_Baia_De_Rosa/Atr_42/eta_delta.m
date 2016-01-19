function eta_delta_s=eta_delta(Delta_s)
% Import data from text file.
% Initialize variables.
filename = 'eta_delta.txt';
delimiter = '*';
startRow = 5;

%% Format string for each line of text:
formatSpec = '%f%f%[^\n\r]';

%% Open the text file.
fileID = fopen(filename,'r');

%% Read columns of data according to format string.
textscan(fileID, '%[^\n\r]', startRow-1, 'ReturnOnError', false);
dataArray = textscan(fileID, formatSpec, 'Delimiter', delimiter, 'ReturnOnError', false);

%% Close the text file.
fclose(fileID);

%% Post processing for unimportable data.
% Allocate imported array to column variable names
X = dataArray{:, 1};
Y = dataArray{:, 2};

%% Clear temporary variables
clearvars filename delimiter startRow formatSpec fileID dataArray ans;

%% Smoothing
x0 = linspace(0,35,50);
pp0 = csaps(X,Y,0.9999);
c0 = ppval(pp0,x0);
data(:,1) = c0;
Delta_s=Delta_s*180/pi;
eta_delta_s = interp1(x0,data(:,1),Delta_s); % ,'linear' ,'spline'
end
