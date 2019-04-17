function []= makeSection(ch,sec_title)

%% Check if this is running as a Matlab code or as deployed executable via Matlab Runtime Compiler
% https://it.mathworks.com/help/rptgen/ug/compile-a-report-program.html
% If compiling, make the DOM compilable
if ismcc || isdeployed
    % Make sure DOM is compilable
    makeDOMCompilable()
end

%% Import base classes
import mlreportgen.report.*  % import report API(report related methods 
                             % @see https://it.mathworks.com/help/rptgen/ug/mlreportgen.report.report-class.html?searchHighlight=mlreportgen.report&s_tid=doc_srchtitle#mw_63820826-78dc-459b-a646-67d4d77f91e5 )
import mlreportgen.dom.*     % import document object model DOM API (DOM related method
 % @see https://it.mathworks.com/help/search.html?qdoc=mlreportgen.dom&submitsearch=)

%%
sec = Section('TemplateSrc','DAF_template','TemplateName','Section'); 
sec.Title = sec_title;
add(ch,sec)
end