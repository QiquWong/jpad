function []= makeTable(ch,C,header,table_title)

%% Check if this is running as a Matlab code or as deployed executable via Matlab Runtime Compiler
% https://it.mathworks.com/help/rptgen/ug/compile-a-report-program.html
% If compiling, make the DOM compilable
if ismcc || isdeployed
    % Make sure DOM is compilable
    makeDOMCompilable()
end

%% Import base classes
import mlreportgen.report.*  % import rcol_name1eport API(report related methods 
                             % @see https://it.mathworks.com/help/rptgen/ug/mlreportgen.report.report-class.html?searchHighlight=mlreportgen.report&s_tid=doc_srchtitle#mw_63820826-78dc-459b-a646-67d4d77f91e5 )
import mlreportgen.dom.*     % import document object model DOM API (DOM related method
                             % @see https://it.mathworks.com/help/search.html?qdoc=mlreportgen.dom&submitsearch=)                                     

%%
tbl = FormalTable(header,C);
tbl.Style = {... 
RowSep('solid','black','2px'),... 
ColSep('solid','black','1px'),...
Border('ridge','black','5px')...
}; 
tbl.Header.Style = {... 
RowSep('solid','black','2px'),... 
ColSep('solid','black','2px'),...
Border('ridge','black','5px')...
}; 
tbl.TableEntriesStyle = {HAlign('left')};
% In order to put a table with a caption, the API Report denomination should
% be used, the other options are from API DOM. In order to solve the problem,
% the table is created as FormalTable (DOM) but it is inserted in a BaseTable (Report).
tbl = BaseTable(tbl);
tbl.Title = table_title;
tbl.LinkTarget = 'tlarTableRef';

add(ch,tbl);
end
