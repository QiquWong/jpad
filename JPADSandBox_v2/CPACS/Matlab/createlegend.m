function createlegend(axes1,~)
%Forced legend position in the subplot
%1 input for Control surface
%2 input clean
% Create legend
legend1 = legend(axes1);
if nargin == 1
set(legend1,...
    'Position',[0.492169005330029 0.943396237468884 0.0499999993015074 0.0585898693055357],...
    'FontSize',7);
end

if nargin == 2

set(legend1,...
    'Position',[0.461960671996696 0.940913615776031 0.0499999993015077 0.0556107234161682],...
    'FontSize',10);
end

