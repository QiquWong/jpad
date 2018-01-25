function dataXYZ = getFuselageProfiles(doc)
import javax.xml.xpath.*

% Create an XPath expression.
factory = XPathFactory.newInstance;
xpath = factory.newXPath;
expression = xpath.compile('//profiles/fuselageProfiles/fuselageProfile');

% Apply the expression to the DOM.
nodeList = expression.evaluate(doc,XPathConstants.NODESET);
fprintf('Found %d fuselageProfile nodes\n',nodeList.getLength)
% Iterate through the nodes that are returned.
dataXYZ = [];
for i = 1:nodeList.getLength
    node = nodeList.item(i-1);
    pointListNodes = node.getElementsByTagName('pointList').item(0);
    xNode = pointListNodes.getElementsByTagName('x').item(0);
    yNode = pointListNodes.getElementsByTagName('y').item(0);
    zNode = pointListNodes.getElementsByTagName('z').item(0);
    strX = xNode.getTextContent;
    strY = yNode.getTextContent;
    strZ = zNode.getTextContent;
    x = str2num(strX);
    y = str2num(strY);
    z = str2num(strZ);
    dataXYZ{i} = [x,y,z];
end
end