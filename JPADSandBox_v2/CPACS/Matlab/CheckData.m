clc; clear all; close all;
% Import the XPath classes
import javax.xml.*
import javax.xml.xpath.*

fileName = 'aircraft/D150_JSBSim/D150_JSBSim.xml';

% Construct the DOM.
doc = xmlread(which(fileName));

% Create an XPath expression.
factoryOuter = XPathFactory.newInstance;
xpath = factoryOuter.newXPath;
% expression = xpath.compile('//aero/function/lift_coeff_basic_M0');
expression = xpath.compile('//aerodynamics/function/@name');
% Apply the expression to the DOM.
nodeList = expression.evaluate(doc,XPathConstants.NODESET);

% Iterate through the nodes that are returned.
for i = 1:nodeList.getLength
    node = nodeList.item(i-1);
    disp(char(node.getFirstChild.getNodeValue))
%     if ( ...
%         strcmp( ...
%             char(node.getFirstChild.getNodeValue), ...
%             'aero/function/lift_coeff_basic_M0') ...
%             )
%         disp(i)
%         % look for data
%            childNodes = node.getChildNodes;
%            numberOfChild = childNodes.getLength;
%            expressionData = ...
%                xpath.compile('//aerodynamics/function/@name');
%            for count = 1:numberOfChild
%                theChild = childNodes.item(count-1);
%                if ( ...
%                        strcmp( ...
%                        char(theChild.getNodeValue),...
%                        'dataTable') ...
%                    )
%                    
%                    
%                end
%            end
%            
% %          factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
% %          factory.setNamespaceAware(true);
% %          builder = factory.newDocumentBuilder();
% %          newDocument = builder.newDocument();
% %          importedNode = newDocument.importNode(node, true);
% %          expression = xpath.compile('table/tableData');
% %          tdList = expression.evaluate(newDocument,XPathConstants.NODESET);
% %          newDocument.appendChild(importedNode);
% %          fprintf('N. tableData found: %d\n', tdList.getLength)
%     end
 childNodes = node.getChildNodes;
 numberOfChild = childNodes.getLength;
 expressionData = ...
 xpath.compile('//aerodynamics/function/@name');




end