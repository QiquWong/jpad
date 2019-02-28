function [report] = makeReport02(reportFileName, reportType, reportData)
% MAKEREPORT02 create a report of type reportType and save it as
%   reportFileName (extension will be appended depending on document type)
%
% Usage:
%   reportData.title         = 'My title';
%   reportData.subtitle      = 'My subtitle';
%   reportData.author        = 'A. U. Thor';
%   reportData.wingSpan      = '34.5'; % m
%   reportData.aspectRatio   = '8.4';
%   rpt = makeReport02('MyReport', 'docx', reportData);

%% Check if this is running as a Matlab code or as deployed executable via Matlab Runtime Compiler
% https://it.mathworks.com/help/rptgen/ug/compile-a-report-program.html
% If compiling, make the DOM compilable
if ismcc || isdeployed
    % Make sure DOM is compilable
    makeDOMCompilable()
end

%% Import base classes
import mlreportgen.report.* 
import mlreportgen.dom.* 

%% Create a report object
report = Report(reportFileName, reportType);

%% Set Locale to English
report.Locale = 'en';

%% Title page
titlePage          = TitlePage; 
titlePage.Title    = reportData.title;
titlePage.Subtitle = reportData.subtitle;
titlePage.Author   = reportData.author;
add(report,titlePage);

%% TOC
add(report,TableOfContents); 

%% Chapter 1
chapter_1       = Chapter; 
chapter_1.Title = 'Introduction'; 

%% Section 1
section_1            = Section; 
section_1.Title      = 'Aircraft geometry'; 

paragraph = Paragraph([ ...
    'TO DO: insert '... 
    'a paragraph here.']); 

%% add paragraph to super-structure
add(section_1,paragraph) 

%% Finalize Section 1 and add to super-structure
add(chapter_1,section_1) 

%% Section 2
section_2            = Section; 
section_2.Title      = 'Wing geometry'; 

paragraph = Paragraph([ ...
    'TO DO: insert '... 
    'a paragraph here.']); 

%% add paragraph to super-structure
add(section_2,paragraph) 

%% Table 1
table_1 = Table(2); 
table_1.Style = {... 
    RowSep('solid','black','1px'),... 
    ColSep('solid','black','1px'),}; 
table_1.Border = 'double'; 
table_1.TableEntriesStyle = {HAlign('left')}; 

% row 0, headings
tableRow = TableRow();
te_c1 = TableEntry();
append(te_c1, Text('Quantity'));
append(tableRow, te_c1);
te_c2 = TableEntry();
append(te_c2, Text('Value'));
append(tableRow, te_c2);
append(table_1, tableRow);

% row 1
tableRow = TableRow();
te_c1 = TableEntry();
append(te_c1, Text('Wing span'));
append(tableRow, te_c1);
te_c2 = TableEntry();
append(te_c2, Text(num2str(reportData.wingSpan))); % assume a String?
append(tableRow, te_c2);
append(table_1, tableRow);

% row 1
tableRow = TableRow();
te_c1 = TableEntry();
append(te_c1, Text('Wing aspect ratio'));
append(tableRow, te_c1);
te_c2 = TableEntry();
append(te_c2, Text(num2str(reportData.wingAspectRatio))); % assume a String?
append(tableRow, te_c2);
append(table_1, tableRow);

%% Finalize Table 1 and add to super-structure
add(section_2,table_1); 

%% Finalize Section 2 and add to super-structure
add(chapter_1,section_2) 

%% Finalize Chapter 1 and add to super-structure
add(report,chapter_1) 

%% Chapter 2
chapter_2 = Chapter(); 
chapter_2.Title = sprintf('A magic square 25 x 25');

%% Figure 1
square = magic(25); 
clf; 
imagesc(square) 
set(gca,'Ydir','normal') 
axis equal 
axis tight 
hfig_1 = Figure(gcf); 
hfig_1.Snapshot.Height = '4in'; 
hfig_1.Snapshot.Width = '6in'; 
hfig_1.Snapshot.Caption = sprintf('A magic square 25 x 25');

%% Finalize Figure 1 and add to super-structure
add(chapter_2, hfig_1); 

%% Finalize Chapter 1 and add to super-structure
add(report,chapter_2); 

%% Finalize report construction
delete(gcf) 
close(report)

end