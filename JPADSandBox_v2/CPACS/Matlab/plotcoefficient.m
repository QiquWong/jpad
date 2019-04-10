clc; clear all; close all;
%C_x
filename = 'C_x.txt';
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
            title(['Drag coefficient at M = ',num2str(machVector(k)),...
                ' Altitude = ',num2str(altitudeVector(i))])
            plot3(alphaVector,betaVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\beta'); zlabel('C_D')
            hold on
        end
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
            title(['Side force coefficient at M = ',num2str(machVector(k)),...
                ' Altitude = ',num2str(altitudeVector(i))])
            plot3(betaVector,alphaVector(j)*vectorPlotLatero,matrix(j,:));
            xlabel('\beta'); ylabel('\alpha'); zlabel('C_Y')
            hold on
        end
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
            title(['Lift coefficient at M = ',num2str(machVector(k)),...
                ' Altitude = ',num2str(altitudeVector(i))])            
            plot3(alphaVector,betaVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\beta'); zlabel('C_L')
            hold on
        end
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
             title(['Roll moments coefficient at M = ',num2str(machVector(k)),...
                ' Altitude = ',num2str(altitudeVector(i))])           
            plot3(betaVector,alphaVector(j)*vectorPlotLatero,matrix(j,:));
            xlabel('\beta'); ylabel('\alpha'); zlabel('C_{Roll}')
            hold on
        end
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
            title(['Pitch coefficient at M = ',num2str(machVector(k)),...
                ' Altitude = ',num2str(altitudeVector(i))])
            plot3(alphaVector,betaVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\beta'); zlabel('C_M')
            hold on
        end
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
            title(['Yaw coefficient at M = ',num2str(machVector(k)),...
                ' Altitude = ',num2str(altitudeVector(i))])
            plot3(betaVector,alphaVector(j)*vectorPlotLatero,matrix(j,:));
            xlabel('\beta'); ylabel('\alpha'); zlabel('C_N')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Drag coefficient Aileron at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_D')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Side coefficient Aileron at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_Y')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Lift coefficient Aileron at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_L')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Roll coefficient Aileron at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_Roll')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Pitch coefficient Aileron at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_M')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Yaw coefficient Aileron at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_N')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Drag coefficient Rudder at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_D')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Side coefficient Rudder at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_Y')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Lift coefficient Rudder at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_L')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Roll coefficient Rudder at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_Roll')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Pitch coefficient Rudder at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_M')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Yaw coefficient Rudder at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_N')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Drag coefficient Elevator at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_D')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Side coefficient Elevator at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_Y')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Lift coefficient Elevator at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_L')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Roll coefficient Elevator at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_Roll')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Pitch coefficient Elevator at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_M')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Yaw coefficient Elevator at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_N')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Drag coefficient InnerFlap at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_D')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Side coefficient InnerFlap at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_Y')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Lift coefficient InnerFlap at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_L')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Roll coefficient InnerFlap at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_Roll')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Pitch coefficient InnerFlap at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_M')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Yaw coefficient InnerFlap at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_N')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Drag coefficient OuterFlap at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_D')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Side coefficient OuterFlap at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_Y')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Lift coefficient OuterFlap at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_L')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Roll coefficient OuterFlap at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_Roll')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Pitch coefficient OuterFlap at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_M')
            hold on
        end
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
        counterPlot = counterPlot + 1;
        for j = 1 : deflectionLength
            title(['Yaw coefficient OuterFlap at M = ',num2str(machVector(k)),...
                ' beta = ',num2str(betaVector(i))])
            plot3(alphaVector,deflectionVector(j)*vectorPlot,matrix(:,j));
            xlabel('\alpha'); ylabel('\delta'); zlabel('C_N')
            hold on
        end
    end
    counter = counter + 2*altitudeLength*(alphaLength+2);
end