%% Report format can be: 'pdf', 'docx', 'html'.                      
function [rpt] = makeReport(doc_name, type, template_name, language, ...
                   title,cover_image, publisher, authors)
import mlreportgen.report.*  % import report API(report related methods 
                             % @see https://it.mathworks.com/help/rptgen/ug/mlreportgen.report.report-class.html?searchHighlight=mlreportgen.report&s_tid=doc_srchtitle#mw_63820826-78dc-459b-a646-67d4d77f91e5 )
import mlreportgen.dom.*     % import document object model DOM API (DOM related method
                             % @see https://it.mathworks.com/help/search.html?qdoc=mlreportgen.dom&submitsearch=)                                     

rpt = Report(doc_name, type, template_name);
disp('Writing report...')
rpt.Locale = language;

%% TITLE PAGE
%Title
tp = TitlePage('TemplateSrc', template_name, 'TemplateName', 'TitlePage');
tp.Title = title;
tp.Image = cover_image;
tp.Publisher = publisher; 
tp.Author = authors; 
tp.PubDate = date();
add(rpt,tp);

% TABLE OF CONTENT
toc = TableOfContents('TemplateSrc',template_name,'TemplateName','TableOfContents');
add(rpt,toc);
end