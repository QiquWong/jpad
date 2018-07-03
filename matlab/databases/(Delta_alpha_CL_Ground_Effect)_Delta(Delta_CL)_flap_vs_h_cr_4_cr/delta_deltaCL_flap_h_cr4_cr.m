clear all, close all, clc

%% load data file generated by Grabit
% https://it.mathworks.com/matlabcentral/fileexchange/7173-grabit

fileBaseNames = {'Delta_Delta_CL_flap_vs_h_cr_4_cr'};
nPoints = 21;

xx = linspace(0.0, 1, nPoints);
    
    fileName = sprintf('%s.mat',fileBaseNames{1});
    s = load(fileName, '-mat');

    % Allocate imported array to column variable names
    
    x = s.(fileBaseNames{1})(:, 1);
    x = flipud(x);
    x(end) = 1;
    
    y = s.(fileBaseNames{1})(:, 2);
    y = flipud(y);
    y(1) = -0.12;
    
    %% Smoothing
    pp = csaps(x, y, 0.999999);
    c = ppval(pp(1), xx');
    data(:,1) = c;

    plot (xx', data(:,1), '-*');
   
xlabel('$\frac{h_{{c_r}/4}}{c_r}$','interpreter','latex'); 
ylabel('$\Delta(\Delta C_L))$','interpreter','latex');
set(get(gca,'ylabel'),'rotation',0)

title('Effect of ground deflection on ground influence on lift');
axis([0 1 -0.12 .16]);


%% preparing output to HDF


h_cr_4_cr = xx';

 
%columns --> curves
 
hdfFileName = 'Delta_alpha_G_sigma_vs_2hfracb.h5';

if ( exist(hdfFileName, 'file') )
    fprintf('file %s exists, deleting and creating a new one\n', hdfFileName);
    delete(hdfFileName)
else
    fprintf('Creating new file %s\n', hdfFileName);
end

h5create(hdfFileName, '/(Delta_alpha_CL_Ground_Effect)_Delta(Delta_CL)_flap_vs_h_cr_4_cr/data', size(data'));
h5write(hdfFileName, '/(Delta_alpha_CL_Ground_Effect)_Delta(Delta_CL)_flap_vs_h_cr_4_cr/data', data');

h5create(hdfFileName, '/(Delta_alpha_CL_Ground_Effect)_Delta(Delta_CL)_flap_vs_h_cr_4_cr/var_0', size(h_cr_4_cr'));
h5write(hdfFileName, '/(Delta_alpha_CL_Ground_Effect)_Delta(Delta_CL)_flap_vs_h_cr_4_cr/var_0', h_cr_4_cr');





