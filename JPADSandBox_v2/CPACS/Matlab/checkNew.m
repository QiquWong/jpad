clc; clear all; close all;
cmx = [0.05969;0;-0.05969;];
cmz = [-0.032000;0.0;0.032000];
beta = [-10;0;10];
delta = [-3;0;3];
cmxda = [-0.0041725; 0; 0.0043685];
cmyda = [-0.0039165; 0; 0.0043334];
cmzda = [0.012593; 0; -0.013216];
cmxde = [-0.0041725; 0; 0.0043685];
cmyde = [-0.0039165; 0; 0.0043334];
cmzde = [0.012593; 0; -0.013216];
cmxdr = [-0.0041725; 0; 0.0043685];
cmydr = [-0.0039165; 0; 0.0043334];
cmzdr = [0.012593; 0; -0.013216];
figure()
plot(beta,cmx)
xlabel('beta deg'); ylabel('C_{mx}');
title('clean')
figure()
plot(beta,cmz)
xlabel('beta deg'); ylabel('C_{mz}');
title('clean')
figure()
plot(delta,cmxda)
xlabel('\delta_{a} deg'); ylabel('\Delta C_{mx}');
title('Aileron')
figure()
plot(delta,cmyda)
xlabel('\delta_{a} deg'); ylabel('\Delta C_{my}');
title('Aileron')
figure()
plot(delta,cmzda)
xlabel('\delta_{a} deg'); ylabel('\Delta C_{mz}');
title('Aileron')
%Elevator
figure()
plot(delta,cmxde)
xlabel('\delta_{e} deg'); ylabel('\Delta C_{mx}');
title('Elevator')
figure()
plot(delta,cmyde)
xlabel('\delta_{e} deg'); ylabel('\Delta C_{my}');
title('Elevator')
figure()
plot(delta,cmzde)
xlabel('\delta_{e} deg'); ylabel('\Delta C_{mz}');
title('Elevator')
%Rudder
figure()
plot(delta,cmxdr)
xlabel('\delta_{r} deg'); ylabel('\Delta C_{mx}');
title('Rudder')
figure()
plot(delta,cmydr)
xlabel('\delta_{r} deg'); ylabel('\Delta C_{my}');
title('Rudder')
figure()
plot(delta,cmzdr)
xlabel('\delta_{r} deg'); ylabel('\Delta C_{mz}');
title('Rudder')
