 clc; clear all; close all;
% %Script that plot the Aeroperformance Map as JSBSim table,force and moments evaluated along
% %JSBSim body axis
% %
% %                           / Y
% %                          /
% %                         /
% %             X          /
% %             <---------|
% %                       |
% %                       |
% %                       |
% %                       |
% %                       |
% %                       V Z
% %
% %Cf_x
filename = 'Data\Cf_x.txt';

% rmdir Results
mkdir Results
machVector = importRow(filename,1); machLength = length(machVector);
reynoldsVector = importRow(filename,2); reynoldsLength = length(reynoldsVector);
betaVector = importRow(filename,3); betaLength = length(betaVector);
for i = 1:betaLength
betaLegendString{i} = strcat ('\beta = ', num2str(betaVector(i)), '°');
end
alphaVector = importRow(filename,4); alphaLength = length(alphaVector);
counter = 6; %6 is the first row of the jsbsim table
counterPlot = 1;%Counter for Plot
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);

if(betaLength>1) %More than 1 Beta
    for k = 1 : machLength
        for i = 1:reynoldsLength
            matrix = importMatrix(filename,counter,alphaLength,betaLength);
            counter = counter + alphaLength + 2;
            subplot(machLength,reynoldsLength,counterPlot);
            counterPlot = counterPlot + 1;
            for j = 1 : betaLength
                title(['M = ',num2str(machVector(k)),...
                    ' Re = ',num2str(reynoldsVector(i))])
                plot(alphaVector,matrix(:,j))
                xlabel('\alpha (deg)'); ylabel('Cf_x');
                hold on
            end
        end
    end
else % if there is 1 beta delete a variable
    for k = 1 : machLength
        matrix = importMatrix(filename,counter,alphaLength,reynoldsLength);
        
        for i = 1:reynoldsLength
            subplot(machLength,1,counterPlot);
            title(['M = ',num2str(machVector(k))])
            plot(alphaVector,matrix(:,i));
            xlabel('\alpha (deg)'); ylabel('Cf_x');
            hold on
            
        end
        counter = counter + alphaLength + 2;
         counterPlot = counterPlot + 1;
    end
end
 
createlegend(betaLegendString,1)
print('Results\cfx','-dpng')
close all;

%Cf_y
filename = 'Data\Cf_y.txt';
counter = 6; %6 is the first row of the jsbsim table
counterPlot = 1;%Counter for Plot
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
if(betaLength>1) %More than 1 Beta
    for k = 1 : machLength
        for i = 1:reynoldsLength
            matrix = importMatrix(filename,counter,alphaLength,betaLength);
            counter = counter + alphaLength + 2;
            subplot(machLength,reynoldsLength,counterPlot);
            counterPlot = counterPlot + 1;
            for j = 1 : betaLength
                title(['M = ',num2str(machVector(k)),...
                    ' Re = ',num2str(reynoldsVector(i))])
                plot(alphaVector,matrix(:,j));
                xlabel('\alpha (deg)'); ylabel('Cf_y');
                hold on
            end
           
        end
    end
else % if there is 1 beta delete a variable
    for k = 1 : machLength
        matrix = importMatrix(filename,counter,alphaLength,reynoldsLength);
        
        for i = 1:reynoldsLength
            subplot(machLength,1,counterPlot);
            title(['M = ',num2str(machVector(k))])
            plot(alphaVector,matrix(:,i));
            xlabel('\alpha (deg)'); ylabel('Cf_y');
            hold on
           
        end
        counter = counter + alphaLength + 2;
         counterPlot = counterPlot + 1;
    end
end
createlegend(betaLegendString,1)
print('Results\cfy','-dpng')
close all;


%Cf_z
filename = 'Data\Cf_z.txt';
counter = 6; %6 is the first row of the jsbsim table
counterPlot = 1;%Counter for Plot
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
if(betaLength>1) %More than 1 Beta
    for k = 1 : machLength
        for i = 1:reynoldsLength
            matrix = importMatrix(filename,counter,alphaLength,betaLength);
            counter = counter + alphaLength + 2;
            subplot(machLength,reynoldsLength,counterPlot);
            counterPlot = counterPlot + 1;
            for j = 1 : betaLength
                title(['M = ',num2str(machVector(k)),...
                    ' Re = ',num2str(reynoldsVector(i))])
                plot(alphaVector,matrix(:,j));
                xlabel('\alpha (deg)'); ylabel('Cf_z');
                hold on
            end
           
        end
    end
else % if there is 1 beta delete a variable
    for k = 1 : machLength
        matrix = importMatrix(filename,counter,alphaLength,reynoldsLength);
        
        for i = 1:reynoldsLength
            subplot(machLength,1,counterPlot);
            title(['M = ',num2str(machVector(k))])
            plot(alphaVector,matrix(:,i));
            xlabel('\alpha (deg)'); ylabel('Cf_z');
            hold on
           
        end
        counter = counter + alphaLength + 2;
         counterPlot = counterPlot + 1;
    end
end
createlegend(betaLegendString,1)
print('Results\cfz','-dpng')
close all;
%Moments
%Cm_x
filename = 'Data\Cm_x.txt';
counter = 6; %6 is the first row of the jsbsim table
counterPlot = 1;%Counter for Plot
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
if(betaLength>1) %More than 1 Beta
    for k = 1 : machLength
        for i = 1:reynoldsLength
            matrix = importMatrix(filename,counter,alphaLength,betaLength);
            counter = counter + alphaLength + 2;
            subplot(machLength,reynoldsLength,counterPlot);
            counterPlot = counterPlot + 1;
            for j = 1 : betaLength
                title(['M = ',num2str(machVector(k)),...
                    ' Re = ',num2str(reynoldsVector(i))])
                plot(alphaVector,matrix(:,j));
                xlabel('\alpha (deg)'); ylabel('Cm_x');
                hold on
            end
           
        end
    end
    
else % if there is 1 beta delete a variable
    for k = 1 : machLength
        matrix = importMatrix(filename,counter,alphaLength,reynoldsLength);
        
        for i = 1:reynoldsLength
            subplot(machLength,1,counterPlot);
            title(['M = ',num2str(machVector(k))])
            plot(alphaVector,matrix(:,i));
            xlabel('\alpha (deg)'); ylabel('Cm_x');
            hold on
           
        end
        counter = counter + alphaLength + 2;
         counterPlot = counterPlot + 1;
    end
end
createlegend(betaLegendString,1)
print('Results\cmx','-dpng')
close all;
%Cm_y
filename = 'Data\Cm_y.txt';
counter = 6; %6 is the first row of the jsbsim table
counterPlot = 1;%Counter for Plot
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
if(betaLength>1) %More than 1 Beta
    for k = 1 : machLength
        for i = 1:reynoldsLength
            matrix = importMatrix(filename,counter,alphaLength,betaLength);
            counter = counter + alphaLength + 2;
            subplot(machLength,reynoldsLength,counterPlot);
            counterPlot = counterPlot + 1;
            for j = 1 : betaLength
                title(['M = ',num2str(machVector(k)),...
                    ' Re = ',num2str(reynoldsVector(i))])
                plot(alphaVector,matrix(:,j));
                xlabel('\alpha (deg)'); ylabel('Cm_y');
                hold on
            end
           
        end
    end
else % if there is 1 beta delete a variable
    for k = 1 : machLength
        matrix = importMatrix(filename,counter,alphaLength,reynoldsLength);
        
        for i = 1:reynoldsLength
            subplot(machLength,1,counterPlot);
            title(['M = ',num2str(machVector(k))])
            plot(alphaVector,matrix(:,i));
            xlabel('\alpha (deg)'); ylabel('Cm_y');
            hold on
           
        end
        counter = counter + alphaLength + 2;
         counterPlot = counterPlot + 1;
    end
end
createlegend(betaLegendString,1)
print('Results\cmy','-dpng')
close all;
%Cm_z
filename = 'Data\Cm_z.txt';
counter = 6; %6 is the first row of the jsbsim table
counterPlot = 1;%Counter for Plot
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
if(betaLength>1) %More than 1 Beta
    for k = 1 : machLength
        for i = 1:reynoldsLength
            matrix = importMatrix(filename,counter,alphaLength,betaLength);
            counter = counter + alphaLength + 2;
            subplot(machLength,reynoldsLength,counterPlot);
            counterPlot = counterPlot + 1;
            for j = 1 : betaLength
                title(['M = ',num2str(machVector(k)),...
                    ' Altitude = ',num2str(reynoldsVector(i))])
                plot(alphaVector,matrix(:,j));
                xlabel('\alpha (deg)'); ylabel('Cm_z');
                hold on
            end
           
        end
    end
else % if there is 1 beta delete a variable
    for k = 1 : machLength
        matrix = importMatrix(filename,counter,alphaLength,reynoldsLength);
        
        for i = 1:reynoldsLength
            subplot(machLength,1,counterPlot);
            title(['M = ',num2str(machVector(k))])
            plot(alphaVector,matrix(:,i));
            xlabel('\alpha (deg)'); ylabel('Cm_z');
            hold on
           
        end
        counter = counter + alphaLength + 2;
         counterPlot = counterPlot + 1;
    end
end
createlegend(betaLegendString,1)
print('Results\cmz','-dpng')
close all;

%Control Surface
%Aileron
%Cfx
filename = 'Data/Cf_x_Aileron.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
for i = 1:deflectionLength
deflectionLegendAileron{i} = strcat ('\delta_{a} = ', num2str(deflectionVector(i)), '°');
end
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Aileron']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cf_x');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendAileron)
print('Results\cfxAileron','-dpng')
close all; 
%Cf_y

filename = 'Data/Cf_y_Aileron.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Aileron']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cf_y');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendAileron)
print('Results\cfyAileron','-dpng')
close all;
%Cf_z
filename = 'Data/Cf_z_Aileron.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Aileron']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cf_z');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendAileron)
print('Results\cfzAileron','-dpng')
close all;
%Cm_x
filename = 'Data/Cm_x_Aileron.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Aileron']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cm_x');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendAileron)
print('Results\cmxAileron','-dpng')
close all;
%Cm_y
filename = 'Data/Cm_y_Aileron.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Aileron']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cm_y');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendAileron)
print('Results\cmyAileron','-dpng')
close all;
%Cm_z
filename = 'Data/Cm_z_Aileron.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Aileron']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cm_z');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendAileron)
print('Results\cmzAileron','-dpng')
close all; clear deflectionLegendAileron
%Elevator
%Cfx
filename = 'Data/Cf_x_Elevator.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
for i = 1:deflectionLength
deflectionLegendElevator{i} = strcat ('\delta_{e} = ', num2str(deflectionVector(i)), '°');
end
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Elevator']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cf_x');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendElevator)
print('Results\cfxElevator','-dpng')
close all;
%Cf_y
filename = 'Data/Cf_y_Elevator.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Elevator']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cf_y');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendElevator)
print('Results\cfyElevator','-dpng')
close all;
%Cf_z
filename = 'Data/Cf_z_Elevator.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Elevator']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cf_z');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendElevator)
print('Results\cfzElevator','-dpng')
close all;
%Cm_x
filename = 'Data/Cm_x_Elevator.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Elevator']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cm_x');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendElevator)
print('Results\cmxElevator','-dpng')
close all;
%Cm_y
filename = 'Data/Cm_y_Elevator.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Elevator']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cm_y');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendElevator)
print('Results\cmyElevator','-dpng')
close all;
%Cm_z
filename = 'Data/Cm_z_Elevator.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Elevator']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cm_z');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendElevator)
print('Results\cmzElevator','-dpng')
close all; clear deflectionLegendElevator
%Rudder
%Cfx
filename = 'Data/Cf_x_Rudder.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
for i = 1:deflectionLength
deflectionLegendRudder{i} = strcat ('\delta_{r} = ', num2str(deflectionVector(i)), '°');
end
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Rudder']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cf_x');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendRudder)
print('Results\cfxRudder','-dpng')
close all;
%Cf_y
filename = 'Data/Cf_y_Rudder.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Rudder']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cf_y');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendRudder)

print('Results\cfyRudder','-dpng')
close all;
%Cf_z
filename = 'Data/Cf_z_Rudder.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Rudder']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cf_z');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendRudder)

print('Results\cfzRudder','-dpng')
close all;

%Cm_x
filename = 'Data/Cm_x_Rudder.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Rudder']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cm_x');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendRudder)

print('Results\cmxRudder','-dpng')
close all;
%Cm_y
filename = 'Data/Cm_y_Rudder.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Rudder']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cm_y');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendRudder)

print('Results\cmyRudder','-dpng')
close all;
%Cm_z
filename = 'Data/Cm_z_Rudder.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Rudder']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cm_z');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendRudder)

print('Results\cmzRudder','-dpng')
close all; clear deflectionLegendRudder
%InnerFlap
%Cfx
filename = 'Data/Cf_x_InnerFlap.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
for i = 1:deflectionLength
deflectionLegendInner{i} = strcat ('\delta_{f_i} = ', num2str(deflectionVector(i)), '°');
end
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['InnerFlap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cf_x');
            hold on
        end
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendInner)
print('Results\cfxInner','-dpng')
close all;
%Cf_y
filename = 'Data/Cf_y_InnerFlap.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['InnerFlap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cf_y');
            hold on
        end
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendInner)
print('Results\cfyInner','-dpng')
close all;
%Cf_z
filename = 'Data/Cf_z_InnerFlap.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['InnerFlap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cf_z');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendInner)
print('Results\cfzInner','-dpng')
close all;
%Cm_x
filename = 'Data/Cm_x_InnerFlap.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['InnerFlap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cm_x');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendInner)
print('Results\cmxInner','-dpng')
close all;
%Cm_y
filename = 'Data/Cm_y_InnerFlap.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['InnerFlap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cm_y');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendInner)
print('Results\cmyInner','-dpng')
close all;
%Cm_z
filename = 'Data/Cm_z_InnerFlap.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['InnerFlap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cm_z');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendInner)
print('Results\cmzInner','-dpng')
close all; clear deflectionLegendInner
%OuterFlap
%Cfx
filename = 'Data/Cf_x_OuterFlap.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
for i = 1:deflectionLength
deflectionLegendOuter{i} = strcat ('\delta_{f_o} = ', num2str(deflectionVector(i)), '°');
end

counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['OuterFlap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cf_x');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendOuter)
print('Results\cfxOuter','-dpng')
close all;
%Cf_y
filename = 'Data/Cf_y_OuterFlap.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['OuterFlap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cf_y');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendOuter)
print('Results\cfyOuter','-dpng')
close all;
%Cf_z
filename = 'Data/Cf_z_OuterFlap.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['OuterFlap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cf_z');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendOuter)
print('Results\cfzOuter','-dpng')
close all;

%Cm_x
filename = 'Data/Cm_x_OuterFlap.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['OuterFlap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cm_x');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendOuter)
print('Results\cmxOuter','-dpng')
close all;
%Cm_y
filename = 'Data/Cm_y_OuterFlap.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['OuterFlap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cm_y');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendOuter)
print('Results\cmyOuter','-dpng')
close all;
%Cm_z
filename = 'Data/Cm_z_OuterFlap.txt';
counter = 7;
counterPlot = 1;
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['OuterFlap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j));
            xlabel('\alpha (deg)'); ylabel('\Delta Cm_z');
            hold on
        end
       
    end
    counter = counter + 2*reynoldsLength*(alphaLength+2);
end
createlegend(deflectionLegendOuter)
print('Results\cmzOuter','-dpng')
close all; clear deflectionLegendOuter



%Engine
filename = 'Data\Idle.txt';
counter = 4; %4 is the first row of the jsbsim table
machVector = importRow(filename,2); machLength = length(machVector);
altitudeVector = importRow(filename,1); altitudeLength = length(altitudeVector);
matrix = importMatrix(filename,counter,machLength,altitudeLength);
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for i = 1:altitudeLength
plot(machVector,matrix(:,i),'DisplayName',['Altitude (m) =  ' num2str(altitudeVector(i))]);
xlabel('Mach'); ylabel('C_T');
hold on;
legend('show');
end
print('Results\Idle','-dpng')
close all;
filename = 'Data\Mil.txt';
counter = 4; %4 is the first row of the jsbsim table
machVector = importRow(filename,2); machLength = length(machVector);
altitudeVector = importRow(filename,1); altitudeLength = length(altitudeVector);
matrix = importMatrix(filename,counter,machLength,altitudeLength);
figure(1); screenSize = get(0, 'ScreenSize'); set(gcf, 'Position', screenSize);
for i = 1:altitudeLength
plot(machVector,matrix(:,i),'DisplayName',['Altitude (m) =  ' num2str(altitudeVector(i))]);
xlabel('Mach'); ylabel('C_T');
hold on;
legend('show');
end
print('Results\Mil','-dpng')
close all;