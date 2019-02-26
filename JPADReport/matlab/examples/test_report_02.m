clear all; close all; clc;

%% Create data struct

reportData.title = 'Title coming from reportData';
reportData.subtitle = 'test_report_02';
reportData.author = 'agodemar';

reportData.wingSpan = 13.5;

%% Create a report object
rpt = makeReport02('test_report_02', 'html', reportData);
