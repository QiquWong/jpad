function [ch] = makeChapter(chap_title)
import mlreportgen.report.*  % import report API(report related methods 
                             % @see https://it.mathworks.com/help/rptgen/ug/mlreportgen.report.report-class.html?searchHighlight=mlreportgen.report&s_tid=doc_srchtitle#mw_63820826-78dc-459b-a646-67d4d77f91e5 )
import mlreportgen.dom.*     % import document object model DOM API (DOM related method
                             % @see https://it.mathworks.com/help/search.html?qdoc=mlreportgen.dom&submitsearch=)                                     
                          
ch = Chapter('TemplateSrc','DAF_template','TemplateName','Section'); 
ch.Title = chap_title;


end
