function [rpt] = makeReport02(fileName, type, reportData)
%% import the base classes

%% https://it.mathworks.com/help/rptgen/ug/compile-a-report-program.html
% If compiling, make the DOM compilable
if ismcc || isdeployed
    % Make sure DOM is compilable
    makeDOMCompilable()
end

import mlreportgen.report.* 
import mlreportgen.dom.* 

%% Create a report object
rpt = Report(fileName, type);
rpt.Locale = 'en';

tp = TitlePage; 
tp.Title = reportData.title;
tp.Subtitle = reportData.subtitle;
tp.Author = reportData.author;
add(rpt,tp); 
add(rpt,TableOfContents); 

ch1 = Chapter; 
ch1.Title = 'Introduction'; 
sec1 = Section; 
sec1.Title = 'What is a Magic Square?'; 
para = Paragraph(['A magic square is an N-by-N matrix '... 
'constructed from the integers 1 through N^2 '... 
'with equal row, column, and diagonal sums.']); 
add(sec1,para) 
add(ch1,sec1) 

sec2=Section; 
sec2.Title = 'Albrect Durer and the Magic Square'; 
para = Paragraph([ ... 
'The German artist Albrecht Durer (1471-1528) created '... 
'many woodcuts and prints with religious and '... 
'scientific symbolism. One of his most famous works, '... 
'Melancholia I, explores the depressed state of mind '... 
'which opposes inspiration and expression. '... 
'Renaissance astrologers believed that the Jupiter '... 
'magic square (shown in the upper right portion of '... 
'the image) could aid in the cure of melancholy. The '... 
'engraving''s date (1514) can be found in the '... 
'lower row of numbers in the square.']); 
add(sec2,para) 
add(ch1,sec2) 
add(rpt,ch1) 

ch2 = Chapter(); 
ch2.Title = sprintf('10 x 10 Magic Square'); 
square = magic(2); 
%tbl = Table(square); 
tbl = Table(2); 
tbl.Style = {... 
RowSep('solid','black','1px'),... 
ColSep('solid','black','1px'),}; 
tbl.Border = 'double'; 
tbl.TableEntriesStyle = {HAlign('center')}; 

tableRow1 = TableRow();
te12 = TableEntry();
append(te12, Text('Wing span'));
te22 = TableEntry();
append(te22, Text(num2str(reportData.wingSpan)));
append(tableRow1,te12);
append(tableRow1,te22);
append(tbl,tableRow1);
% append(rpt, table1);

add(ch2,tbl); 
add(rpt,ch2); 


ch3 = Chapter(); 
ch3.Title = sprintf('25 x 25 Magic Square'); 
square = magic(25); 
clf; 
imagesc(square) 
set(gca,'Ydir','normal') 
axis equal 
axis tight 
fig = Figure(gcf); 
fig.Snapshot.Height = '4in'; 
fig.Snapshot.Width = '6in'; 
fig.Snapshot.Caption = sprintf('25 x 25 Magic Square'); 
add(ch3,fig); 
add(rpt,ch3); 

delete(gcf) 
close(rpt)

end

