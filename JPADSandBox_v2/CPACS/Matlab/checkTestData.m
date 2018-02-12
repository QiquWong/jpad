clc; clear all; close all;

fileName = 'aircraft/D150_JSBSim/D150_JSBSim.xml';

str = xml_read(fileName,'Pref.PreserveSpace');
fileID = fopen('prova.txt','w');
fprintf(fileID,str.aerodynamics.function(3).table.tableData.CONTENT);
fclose(fileID);
