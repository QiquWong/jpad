function rowData = importRow(filename, row)
fid = fopen(filename);
if (row>1)
    for i=1:(row-1)
        fgetl(fid);
    end
end
line = fgetl(fid);
rowData = str2num(line);
fclose(fid);


