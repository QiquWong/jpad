% Script for importing data from the following text file:
function alfadelta=adelta(CfsuC);
filename = 'adelta.txt';
delimiter = '*';
startRow = 5;


formatSpec = '%f%f%[^\n\r]';

%% Open the text file.
fileID = fopen(filename,'r');


textscan(fileID, '%[^\n\r]', startRow-1, 'ReturnOnError', false);
dataArray = textscan(fileID, formatSpec, 'Delimiter', delimiter, 'ReturnOnError', false);

%% Close the text file.
fclose(fileID);

%% Allocate imported array to column variable names
X = dataArray{:, 1};
Y = dataArray{:, 2};
%% Clear temporary variables
clearvars filename delimiter startRow formatSpec fileID dataArray ans;

x0 = linspace(0,0.5,50);
pp0 = csaps(X,Y,0.9999);
c0 = ppval(pp0,x0);

data(:,1) = c0;

alfadelta = interp1(x0,data(:,1),CfsuC); % ,'linear' ,'spline'
end
