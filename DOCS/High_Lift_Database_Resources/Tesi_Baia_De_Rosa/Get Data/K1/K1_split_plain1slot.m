% Script for importing data from the following text file:
function k1=K1_split_plain1slot(CFsuC)
%% Initialize variables.
filename = 'K1_split_plain1slot.txt';
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
%% Allocate imported array to column variable names
X = dataArray{:, 1};
Y = dataArray{:, 2};
%% Clear temporary variables
clearvars filename delimiter startRow formatSpec fileID dataArray ans;
%% Smoothing
x0 = linspace(0,24,50);
pp0 = csaps(X,Y,0.9999);
c0 = ppval(pp0,x0);

data(:,1) = c0;
CFsuC=100*CFsuC;
k1 = interp1(x0,data(:,1),CFsuC); % ,'linear' ,'spline'
end
