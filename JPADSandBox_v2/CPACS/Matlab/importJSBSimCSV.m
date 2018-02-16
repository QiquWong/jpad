function D150AGILE = importJSBSimCSV(filename, startRow, endRow)
%IMPORTFILE1 Import numeric data from a text file as a matrix.
%   D150AGILE = IMPORTFILE1(FILENAME) Reads data from text file FILENAME
%   for the default selection.
%
%   D150AGILE = IMPORTFILE1(FILENAME, STARTROW, ENDROW) Reads data from
%   rows STARTROW through ENDROW of text file FILENAME.
%
% Example:
%   D150AGILE = importfile1('D150 AGILE.csv', 2, 1002);
%
%    See also TEXTSCAN.

% Auto-generated by MATLAB on 2018/02/15 11:58:42

%% Initialize variables.
delimiter = ',';
if nargin<=2
    startRow = 2;
    endRow = inf;
end

%% Format for each line of text:
%   column1: double (%f)
%	column2: double (%f)
%   column3: double (%f)
%	column4: double (%f)
%   column5: double (%f)
%	column6: double (%f)
%   column7: double (%f)
%	column8: double (%f)
%   column9: double (%f)
%	column10: double (%f)
%   column11: double (%f)
%	column12: double (%f)
%   column13: double (%f)
%	column14: double (%f)
%   column15: double (%f)
%	column16: double (%f)
%   column17: double (%f)
%	column18: double (%f)
%   column19: double (%f)
%	column20: double (%f)
%   column21: double (%f)
%	column22: double (%f)
%   column23: double (%f)
%	column24: double (%f)
%   column25: double (%f)
%	column26: double (%f)
%   column27: double (%f)
%	column28: double (%f)
%   column29: double (%f)
%	column30: double (%f)
%   column31: double (%f)
%	column32: double (%f)
%   column33: double (%f)
%	column34: double (%f)
%   column35: double (%f)
%	column36: double (%f)
%   column37: double (%f)
%	column38: double (%f)
%   column39: double (%f)
%	column40: double (%f)
%   column41: double (%f)
%	column42: double (%f)
%   column43: double (%f)
%	column44: double (%f)
%   column45: double (%f)
%	column46: double (%f)
%   column47: double (%f)
%	column48: double (%f)
%   column49: double (%f)
%	column50: double (%f)
%   column51: double (%f)
%	column52: double (%f)
%   column53: double (%f)
%	column54: double (%f)
%   column55: double (%f)
%	column56: double (%f)
%   column57: double (%f)
%	column58: double (%f)
%   column59: double (%f)
%	column60: double (%f)
%   column61: double (%f)
%	column62: double (%f)
%   column63: double (%f)
%	column64: double (%f)
%   column65: double (%f)
%	column66: double (%f)
%   column67: double (%f)
%	column68: double (%f)
%   column69: double (%f)
%	column70: double (%f)
%   column71: double (%f)
%	column72: double (%f)
%   column73: double (%f)
%	column74: double (%f)
%   column75: double (%f)
%	column76: double (%f)
%   column77: double (%f)
%	column78: double (%f)
%   column79: double (%f)
%	column80: double (%f)
%   column81: double (%f)
%	column82: double (%f)
%   column83: double (%f)
%	column84: double (%f)
%   column85: double (%f)
%	column86: double (%f)
%   column87: double (%f)
%	column88: double (%f)
% For more information, see the TEXTSCAN documentation.
formatSpec = '%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%f%[^\n\r]';

%% Open the text file.
fileID = fopen(filename,'r');

%% Read columns of data according to the format.
% This call is based on the structure of the file used to generate this
% code. If an error occurs for a different file, try regenerating the code
% from the Import Tool.
dataArray = textscan(fileID, formatSpec, endRow(1)-startRow(1)+1, 'Delimiter', delimiter, 'TextType', 'string', 'HeaderLines', startRow(1)-1, 'ReturnOnError', false, 'EndOfLine', '\r\n');
for block=2:length(startRow)
    frewind(fileID);
    dataArrayBlock = textscan(fileID, formatSpec, endRow(block)-startRow(block)+1, 'Delimiter', delimiter, 'TextType', 'string', 'HeaderLines', startRow(block)-1, 'ReturnOnError', false, 'EndOfLine', '\r\n');
    for col=1:length(dataArray)
        dataArray{col} = [dataArray{col};dataArrayBlock{col}];
    end
end

%% Close the text file.
fclose(fileID);

%% Post processing for unimportable data.
% No unimportable data rules were applied during the import, so no post
% processing code is included. To generate code which works for
% unimportable data, select unimportable cells in a file and regenerate the
% script.

%% Create output variable
D150AGILE = table(dataArray{1:end-1}, 'VariableNames', {'Time','fdmjsbsimaerofunctiondrag_coeff_basic_Mach','fdmjsbsimaeroforcesdrag_basic_Mach','fdmjsbsimaerofunctionside_coeff_basic_Mach','fdmjsbsimaeroforcesside_basic_Mach','fdmjsbsimaerofunctionlift_coeff_basic_Mach','fdmjsbsimaeroforceslift_basic_Mach','fdmjsbsimaerofunctionroll_coeff_basic_Mach','fdmjsbsimaeroforcesroll_basic_Mach','fdmjsbsimaerofunctionpitch_coeff_basic_Mach','fdmjsbsimaeroforcespitch_basic_Mach','fdmjsbsimaerofunctionyaw_coeff_basic_Mach','fdmjsbsimaeroforcesyaw_basic_Mach','fdmjsbsimaerofunctionflap_inner_drag_coeff_basic_Mach','fdmjsbsimaeroforcesflap_inner_drag_basic_Mach','fdmjsbsimaerofunctionflap_inner_side_coeff_basic_Mach','fdmjsbsimaeroforcesflap_inner_side_basic_Mach','fdmjsbsimaerofunctionflap_inner_lift_coeff_basic_Mach','fdmjsbsimaeroforcesflap_inner_lift_basic_Mach','fdmjsbsimaerofunctionflap_inner_roll_coeff_basic_Mach','fdmjsbsimaeroforcesflap_inner_roll_basic_Mach','fdmjsbsimaerofunctionflap_inner_pitch_coeff_basic_Mach','fdmjsbsimaeroforcesflap_inner_pitch_basic_Mach','fdmjsbsimaerofunctionflap_inner_yaw_coeff_basic_Mach','fdmjsbsimaeroforcesflap_inner_yaw_basic_Mach','fdmjsbsimaerofunctionflap_outer_drag_coeff_basic_Mach','fdmjsbsimaeroforcesflap_outer_drag_basic_Mach','fdmjsbsimaerofunctionflap_outer_side_coeff_basic_Mach','fdmjsbsimaeroforcesflap_outer_side_basic_Mach','fdmjsbsimaerofunctionflap_outer_lift_coeff_basic_Mach','fdmjsbsimaeroforcesflap_outer_lift_basic_Mach','fdmjsbsimaerofunctionflap_outer_roll_coeff_basic_Mach','fdmjsbsimaeroforcesflap_outer_roll_basic_Mach','fdmjsbsimaerofunctionflap_outer_pitch_coeff_basic_Mach','fdmjsbsimaeroforcesflap_outer_pitch_basic_Mach','fdmjsbsimaerofunctionflap_outer_yaw_coeff_basic_Mach','fdmjsbsimaeroforcesflap_outer_yaw_basic_Mach','fdmjsbsimaerofunctionaileron_drag_coeff_basic_Mach','fdmjsbsimaeroforcesaileron_drag_basic_Mach','fdmjsbsimaerofunctionaileron_side_coeff_basic_Mach','fdmjsbsimaeroforcesaileron_side_basic_Mach','fdmjsbsimaerofunctionaileron_lift_coeff_basic_Mach','fdmjsbsimaeroforcesaileron_lift_basic_Mach','fdmjsbsimaerofunctionaileron_roll_coeff_basic_Mach','fdmjsbsimaeroforcesaileron_roll_basic_Mach','fdmjsbsimaerofunctionaileron_pitch_coeff_basic_Mach','fdmjsbsimaeroforcesaileron_pitch_basic_Mach','fdmjsbsimaerofunctionaileron_yaw_coeff_basic_Mach','fdmjsbsimaeroforcesaileron_yaw_basic_Mach','fdmjsbsimaerofunctionelevator_drag_coeff_basic_Mach','fdmjsbsimaeroforceselevator_drag_basic_Mach','fdmjsbsimaerofunctionelevator_side_coeff_basic_Mach','fdmjsbsimaeroforceselevator_side_basic_Mach','fdmjsbsimaerofunctionelevator_lift_coeff_basic_Mach','fdmjsbsimaeroforceselevator_lift_basic_Mach','fdmjsbsimaerofunctionelevator_roll_coeff_basic_Mach','fdmjsbsimaeroforceselevator_roll_basic_Mach','fdmjsbsimaerofunctionelevator_pitch_coeff_basic_Mach','fdmjsbsimaeroforceselevator_pitch_basic_Mach','fdmjsbsimaerofunctionelevator_yaw_coeff_basic_Mach','fdmjsbsimaeroforceselevator_yaw_basic_Mach','fdmjsbsimaerofunctionrudder_drag_coeff_basic_Mach','fdmjsbsimaeroforcesrudder_drag_basic_Mach','fdmjsbsimaerofunctionrudder_side_coeff_basic_Mach','fdmjsbsimaeroforcesrudder_side_basic_Mach','fdmjsbsimaerofunctionrudder_lift_coeff_basic_Mach','fdmjsbsimaeroforcesrudder_lift_basic_Mach','fdmjsbsimaerofunctionrudder_roll_coeff_basic_Mach','fdmjsbsimaeroforcesrudder_roll_basic_Mach','fdmjsbsimaerofunctionrudder_pitch_coeff_basic_Mach','fdmjsbsimaeroforcesrudder_pitch_basic_Mach','fdmjsbsimaerofunctionrudder_yaw_coeff_basic_Mach','fdmjsbsimaeroforcesrudder_yaw_basic_Mach','fdmjsbsimaeroalphadeg','fdmjsbsimaerobetadeg','fdmjsbsimaeroRe','fdmjsbsimvelocitiesmach','fdmjsbsimpropulsionenginethrustlbs','fdmjsbsimpropulsionengine1thrustlbs','fdmjsbsimpositionhslmeters','fdmjsbsimattitudephideg','fdmjsbsimattitudethetadeg','fdmjsbsimattitudepsideg','fdmjsbsimvelocitiesvtruekts','fdmjsbsimvelocitieshdotfps','fdmjsbsimvelocitiesprad_sec','fdmjsbsimvelocitiesqrad_sec','fdmjsbsimvelocitiesrrad_sec'});

