close all; clear all; clc
delete('TurbopropEngineDatabase.h5');

%% PW2750 data
database = 'pw2750.xlsx';
nsheets = 5;

nmach = 30;
nalt = 7;

altitude = linspace(0, 30000, nalt);

mach = xlsread(database, 1, 'G7:G19');
machNew = linspace(0,1,nmach)';

tt0 = zeros(13,nalt,nsheets);
tt0New = zeros(nmach,nalt,nsheets);

sfc = zeros(13,nalt,nsheets);
sfcNew = zeros(nmach,nalt,nsheets);

idx = 7*(1:2:13);
lastValidIdx = 0;

%% T/T0
for s=1:nsheets
    for i=1:numel(idx)
        temp = xlsread(database, s, strcat('U', num2str(idx(i)), ':', 'U', num2str(idx(i)+12)));
        if (numel(temp) == 0)
            temp = tt0(:,lastValidIdx,s);
        else
            lastValidIdx = i;
        end
        tt0(:,i,s) = temp;
        pp = pchip(mach, temp);
        %         pp = csaps(mach, temp,0.99999);
        tt0New(:,i,s) = ppval(pp, machNew)';
        
        %         figure(s)
        %         hold on; grid on;
        %         plot (machNew, tt0New(:,i,s), 'k-');
        %         plot (mach, tt0(:,i,s), 'r-');
    end
end

%% SFC
for s=1:nsheets
    for i=1:numel(idx)
        temp = xlsread(database, s, strcat('M', num2str(idx(i)), ':', 'M', num2str(idx(i)+12)));
        if (numel(temp) == 0)
            temp = sfc(:,lastValidIdx,s);
        else
            lastValidIdx = i;
        end
        sfc(:,i,s) = temp;
        pp = pchip(mach, temp);
        %         pp = csaps(mach, temp,0.99999);
        sfcNew(:,i,s) = ppval(pp, machNew)';
        
        %         figure(s+10)
        %         hold on; grid on;
        %         plot (machNew, sfcNew(:,i,s), 'k-');
        %         plot (mach, sfc(:,i,s), 'r-');
    end
end

%% PLOTS
for s=1:nsheets
    for i=1:numel(idx)
        figure(s)
        hold on; grid on;
        plot (machNew, tt0New(:,i,s), 'k-');
        plot (mach, tt0(:,i,s), 'r-');
    end
end

for s=1:nsheets
    for i=1:numel(idx)
        figure(s+10)
        hold on; grid on;
        plot (machNew, sfcNew(:,i,s), 'k-');
        plot (mach, sfc(:,i,s), 'r-');
    end
end

%% WRITE EVERYTHING TO DATABASE
myHDFFile = matfile('TurbopropEngineDatabase.h5','Writable',true);

% Thrust

IdleThrust.data = tt0New(:,:,5)';
IdleThrust.var_0 = altitude;
IdleThrust.var_1 = machNew';
myHDFFile.IdleThrust = IdleThrust;

MaximumCruiseThrust.data = tt0New(:,:,4)';
MaximumCruiseThrust.var_0 = altitude;
MaximumCruiseThrust.var_1 = machNew';
myHDFFile.MaximumCruiseThrust = MaximumCruiseThrust;

MaximumClimbThrust.data = tt0New(:,:,3)';
MaximumClimbThrust.var_0 = altitude;
MaximumClimbThrust.var_1 = machNew';
myHDFFile.MaximumClimbThrust = MaximumClimbThrust;

MaximumContinuousThrust.data = tt0New(:,:,2)';
MaximumContinuousThrust.var_0 = altitude;
MaximumContinuousThrust.var_1 = machNew';
myHDFFile.MaximumContinuousThrust = MaximumContinuousThrust;

TakeOffThrust.data = tt0New(:,:,1)';
TakeOffThrust.var_0 = altitude;
TakeOffThrust.var_1 = machNew';
myHDFFile.TakeOffThrust = TakeOffThrust;

% SFC

IdleSFC.data = sfcNew(:,:,5)';
IdleSFC.var_0 = altitude;
IdleSFC.var_1 = machNew';
myHDFFile.IdleSFC = IdleSFC;

MaximumCruiseSFC.data = sfcNew(:,:,4)';
MaximumCruiseSFC.var_0 = altitude;
MaximumCruiseSFC.var_1 = machNew';
myHDFFile.MaximumCruiseSFC = MaximumCruiseSFC;

MaximumClimbSFC.data = sfcNew(:,:,3)';
MaximumClimbSFC.var_0 = altitude;
MaximumClimbSFC.var_1 = machNew';
myHDFFile.MaximumClimbSFC = MaximumClimbSFC;

MaximumContinuousSFC.data = sfcNew(:,:,2)';
MaximumContinuousSFC.var_0 = altitude;
MaximumContinuousSFC.var_1 = machNew';
myHDFFile.MaximumContinuousSFC = MaximumContinuousSFC;

TakeOffSFC.data = sfcNew(:,:,1)';
TakeOffSFC.var_0 = altitude;
TakeOffSFC.var_1 = machNew';
myHDFFile.TakeOffSFC = TakeOffSFC;


