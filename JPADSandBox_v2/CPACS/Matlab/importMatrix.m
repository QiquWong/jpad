function matrix = importMatrix(filename, row , rowLength, columnLength)
fid = fopen(filename);
matrix = zeros(rowLength,columnLength);
if (row>1)
    for i=0:(row-1)
        fgetl(fid);
    end
end
line = fgetl(fid);
for i = 1:rowLength
  rowVector = str2num(line);
  matrix(i,:) = rowVector(2:end);
  line = fgetl(fid);
end
fclose(fid);
