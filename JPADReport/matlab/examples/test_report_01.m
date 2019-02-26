% see: mathworks.com/help/rptgen/ug/create-a-report-generator.html

%% import the base classes
import mlreportgen.report.* 
import mlreportgen.dom.* 

%% Create a report object
rpt = Report('test_report_01','html');

rpt.Locale = 'en';

%% Add a title page
tp = TitlePage; 
tp.Title = 'Magic Squares'; 
tp.Subtitle = 'Columns, Rows, Diagonals: All Equal Sums'; 
tp.Author = 'Albrecht Durer'; 
add(rpt,tp); 

%% Add a chapter and chapter sections
ch1 = Chapter; 
ch1.Title = 'Introduction'; 
sec1 = Section; 
sec1.Title = 'What is a Magic Square?'; 
para = Paragraph(['A magic square is an N-by-N matrix '... 
'constructed from the integers 1 through N^2 '... 
'with equal row, column, and diagonal sums.']); 
add(sec1,para) 
add(ch1,sec1) 
sec2 = Section; 
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

%% Add chapter to report
add(rpt,ch1)

%% Close the report object (file)
close(rpt)