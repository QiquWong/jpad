function rpt = generateReport(reportName, typeName, templateName)
%generateReport bla bla
%   Detailed explanation goes here

import mlreportgen.report.*
import mlreportgen.dom.*

rpt = Report(reportName, typeName, templateName);
rpt.Locale = 'en';
end