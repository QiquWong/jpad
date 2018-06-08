clc; clear all; close all;
%C_D 
filename = 'C_D.txt';
machVector = importRow(filename,1); machLength = length(machVector);
altitudeVector = importRow(filename,2); altitudeLength = length(altitudeVector);
betaVector = importRow(filename,3); betaLength = length(betaVector);
alphaVector = importRow(filename,4); alphaLength = length(alphaVector);
counter = 6;
counterPlot = 1;
vectorPlot = ones(1,alphaLength);
vectorPlotLatero = ones(1,betaLength);
figure()
for k = 1 : machLength
    for i = 1:altitudeLength
        matrix = importMatrix(filename,counter,alphaLength,betaLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,altitudeLength,counterPlot);
        counterPlot = counterPlot + 1;
        for j = 1 : betaLength
            title(['M = ',num2str(machVector(k)),...
                ' Altitude = ',num2str(altitudeVector(i)),' ft'])
            plot(alphaVector,matrix(:,j),'DisplayName', ['\beta ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('C_D');
            hold on
        end
        legend('show');
    end
end
 %C_Y
filename = 'C_Y.txt';
counter = 6;
counterPlot = 1;
figure()
for k = 1 : machLength
    for i = 1:altitudeLength
        matrix = importMatrix(filename,counter,alphaLength,betaLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,altitudeLength,counterPlot);
        counterPlot = counterPlot + 1;
        for j = 1 : alphaLength
            title(['M = ',num2str(machVector(k)),...
                ' Altitude = ',num2str(altitudeVector(i)),' ft'])
            plot(betaVector,matrix(j,:),'DisplayName', ['\alpha ' num2str(alphaVector(j)) '°']);
            xlabel('\beta'); ylabel('C_Y');
            hold on
        end
        legend('show');
    end
end
%C_L 
filename = 'C_L.txt';
counter = 6;
counterPlot = 1;
figure()

for k = 1 : machLength
    for i = 1:altitudeLength
        matrix = importMatrix(filename,counter,alphaLength,betaLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,altitudeLength,counterPlot);
        counterPlot = counterPlot + 1;
        for j = 1 : betaLength
            title(['M = ',num2str(machVector(k)),...
                ' Altitude = ',num2str(altitudeVector(i)),' ft'])
            plot(alphaVector,matrix(:,j),'DisplayName', ['\beta ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('C_L');
            hold on
        end
        legend('show');
    end
end
 

%C_Roll 
filename = 'C_Roll.txt';
counter = 6;
counterPlot = 1;
figure()
for k = 1 : machLength
    for i = 1:altitudeLength
        matrix = importMatrix(filename,counter,alphaLength,betaLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,altitudeLength,counterPlot);
        counterPlot = counterPlot + 1;
        for j = 1 : alphaLength
            title(['M = ',num2str(machVector(k)),...
                ' Altitude = ',num2str(altitudeVector(i)),' ft'])
            plot(betaVector,matrix(j,:),'DisplayName', ['\alpha ' num2str(alphaVector(j)) '°']);
            xlabel('\beta'); ylabel('C_{Roll}');
            hold on
        end
        legend('show');
    end
end

%C_M 
filename = 'C_M.txt';
counter = 6;
counterPlot = 1;
figure()

for k = 1 : machLength
    for i = 1:altitudeLength
        matrix = importMatrix(filename,counter,alphaLength,betaLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,altitudeLength,counterPlot);
        counterPlot = counterPlot + 1;
        for j = 1 : betaLength
            title(['M = ',num2str(machVector(k)),...
                ' Altitude = ',num2str(altitudeVector(i)),' ft'])
            plot(alphaVector,matrix(:,j),'DisplayName', ['\beta ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('C_M');
            hold on
        end
        legend('show');
    end
end
 

%C_N 
filename = 'C_N.txt';
counter = 6;
counterPlot = 1;
figure()
for k = 1 : machLength
    for i = 1:altitudeLength
        matrix = importMatrix(filename,counter,alphaLength,betaLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,altitudeLength,counterPlot);
        counterPlot = counterPlot + 1;
        for j = 1 : alphaLength
            title(['M = ',num2str(machVector(k)),...
                ' Altitude = ',num2str(altitudeVector(i)),' ft'])
            plot(betaVector,matrix(j,:),'DisplayName', ['\alpha ' num2str(alphaVector(j)) '°']);
            xlabel('\beta'); ylabel('C_N');
            hold on
        end
        legend('show');
    end
end

%%Aileron
%CD
filename = 'C_D_Aileron.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{a} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_D');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CY
filename = 'C_Y_Aileron.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{a} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_Y');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CL
filename = 'C_L_Aileron.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{a} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_Y');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CRoll
filename = 'C_Y_Aileron.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{a} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_{Roll}');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CM
filename = 'C_M_Aileron.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{a} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_M');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CN
filename = 'C_N_Aileron.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{a} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_N');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end

%%Rudder
%CD
filename = 'C_D_Rudder.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{r} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_D');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CY
filename = 'C_Y_Rudder.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{r} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_Y');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CL
filename = 'C_L_Rudder.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{r} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_Y');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CRoll
filename = 'C_Y_Rudder.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{r} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_{Roll}');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CM
filename = 'C_M_Rudder.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{r} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_M');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CN
filename = 'C_N_Rudder.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{r} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_N');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end

%%Elevator
%CD
filename = 'C_D_Elevator.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{e} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_D');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CY
filename = 'C_Y_Elevator.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{e} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_Y');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CL
filename = 'C_L_Elevator.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{e} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_Y');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CRoll
filename = 'C_Y_Elevator.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{e} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_{Roll}');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CM
filename = 'C_M_Elevator.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{e} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_M');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CN
filename = 'C_N_Elevator.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
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
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{e} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_N');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end



%%InnerFlap
%CD
filename = 'C_D_InnerFlap.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Inner Flap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{f} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_D');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CY
filename = 'C_Y_InnerFlap.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Inner Flap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{f} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_Y');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CL
filename = 'C_L_InnerFlap.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Inner Flap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{f} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_Y');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CRoll
filename = 'C_Y_InnerFlap.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Inner Flap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{f} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_{Roll}');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CM
filename = 'C_M_InnerFlap.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Inner Flap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{f} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_M');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CN
filename = 'C_N_InnerFlap.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Inner Flap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{f} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_N');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end


%%OuterFlap
%CD
filename = 'C_D_OuterFlap.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Outer Flap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{f} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_D');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CY
filename = 'C_Y_OuterFlap.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Outer Flap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{f} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_Y');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CL
filename = 'C_L_OuterFlap.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Outer Flap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{f} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_Y');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CRoll
filename = 'C_Y_OuterFlap.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Outer Flap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{f} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_{Roll}');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CM
filename = 'C_M_OuterFlap.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Outer Flap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{f} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_M');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end
%CN
filename = 'C_N_OuterFlap.txt';
deflectionVector = importRow(filename,5);
deflectionLength = length(deflectionVector);
counter = 7;
counterPlot = 1;
figure()
for k = 1 : machLength
    for i = 1 : betaLength
        matrix = importMatrix(filename,counter,alphaLength,deflectionLength);
        counter = counter + alphaLength + 2;
        subplot(machLength,betaLength,counterPlot);
        title(['Outer Flap']);
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['M = ',num2str(machVector(k)),...
                ' \beta = ',num2str(betaVector(i)),' °'])
            plot(alphaVector,matrix(:,j),'DisplayName', ['\delta_{f} ' num2str(betaVector(j)) '°']);
            xlabel('\alpha (deg)'); ylabel('\Delta C_N');
            hold on
        end
        legend('show');
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end