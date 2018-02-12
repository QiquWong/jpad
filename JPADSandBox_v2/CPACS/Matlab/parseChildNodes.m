function theChild = parseChildNodes(theNode)
% Recurse over node children.
if theNode.hasChildNodes
   childNodes = theNode.getChildNodes;
   numChildNodes = childNodes.getLength;
    for count = 1:numChildNodes
        theChild = childNodes.item(count-1);
    end
end