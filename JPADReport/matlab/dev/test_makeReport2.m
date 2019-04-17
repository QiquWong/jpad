%% MAKE REPORT

rpt = makeReport('Airbus A320', 'pdf', 'C:/Users/Prince/Desktop/fabrizio/DAF_template', 'en', ...
                   'WikiPage: Airbus_A320 ', '_figures/Airbus_A320_prova.jpg','UNINA', 'Prince94');
%% MAKE CHAPTER 1
[chapt]= makeChapter('Airbus A320 Family');
makeParagraph(chapt,['The Airbus A320 family consists of short-to medium-range,'...
                     'narrow body,commercial passenger twin-engine jet airliners'...
                     'manifactured by Airbus.']); %to add a paragraph to the current chapter
makeFigure(chapt,'C:/Users/Prince/Desktop/fabrizio/Airbus_A320_prova.jpg','Airbus A320');
makeParagraph(chapt,['The first member of the A320 family was launched in March 1984,'...
                     'first flew on 22 February 1987, and was first delivered in' ...
                     'March 1988 to launch customer Air France']);
header={'Geometrical parameters','value'};
% Spec = {'Length', 37.57, 'm'; 'wingspan',34.1,'m';...
%          'Fus_Diameter',3.95,'m'; 'Freccia',25,'deg';...
%          'S wing',122.6,'m^2'};
Spec = struct();
Spec.Length = "37.57 m ";
Spec.WingSpan = "34.1 m ";
Spec.WingArea = "122.6 m^2 ";



makeTab(chapt,Spec,header,'Specification');
closeChapter(rpt,chapt);%to close the chapter 

 %% CHAPTER 2 
[chapt2]= makeChapter('Developments');
[chapt2]= makeSection(chapt2,'Origins');
makeParagraph(chapt2,['When Airbus designed the Airbus A300 during the late 1960s'...
                      'it envisaged a broad family of airliners with which to compete'...
                      'against Boeing and Douglas, two established US aerospace manufacturers.']);
[chapt2]= makeSection(chapt2,'Design effort'); 
makeFigure(chapt2,'_figures/Airbus_A320_design_effort.jpg','Airbus A320_Design effort');
makeParagraph(chapt2,['In june 1977 was set up a new Joint European Transport JET programme.'...
                      'It was based at the British Aerospace site in Weybridge,Surrey,UK.']);





closeChapter(rpt,chapt2);%to close the chapter 


%% CLOSE REPORT
closeReport(rpt); 