function f = extendMach(machMax, mach,y)

% y(end)
% xx = linspace(mach(end),1,11)';
% spl = pchip([mach; xx(2:end)], [y; y(end)*ones(10,1)]);
% f = ppval(spl, myMach);

newMach = mach;
newY = y;

n = numel(mach);
if (n > 60)
    for i=1:round(n/2)
        newMach(i) = mach(i);
    end
else
    newMach = mach;
    newY = y;
end

n = numel(y);
if (n > 60)
    for i=1:round(n/2)
        newY(i) = y(i);
    end
else
    newMach = mach;
    newY = y;
end

smoothingParameter = 0.999999;
xx1 = linspace(0,max(mach),50);
% xx2 = linspace(xx1(end),1,11);

spl = csaps(newMach, newY, smoothingParameter);
f = [fnval(spl, xx1), fnval(spl, xx1(end))*ones(1,10)];
spl2 = csaps([xx1(1:end-1), linspace(xx1(end),machMax,11)], f, smoothingParameter);
f = fnval(spl2, linspace(0,machMax,60));
f = f(:);

