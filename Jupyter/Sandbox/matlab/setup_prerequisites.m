folder = fileparts(mfilename('fullpath'));

%% Add GetFullPath folder to MATLAB path
% see: http://www.mathworks.com/matlabcentral/fileexchange/28249-getfullpath
% see: http://blogs.mathworks.com/pick/2011/04/01/be-absolute-about-your-relative-path-with-getfullpath/
addpath(fullfile(folder,'GetFullPath'),'-end');

%% Add Units folder to MATLAB path
% see: http://www.mathworks.com/matlabcentral/fileexchange/38977-dimensioned-variable-class-units-support-for-matlab
addpath(fullfile(folder,'Units'),'-end');

%% Add export_fig folder to MATLAB path
% see: http://www.mathworks.com/matlabcentral/fileexchange/23629-export-fig
addpath(fullfile(folder,'export_fig'),'-end');

%% Save MATLAB path for successive work sessione
savepath