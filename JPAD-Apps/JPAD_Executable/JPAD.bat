setlocal
set PATH=%PATH%;..\lib;
java -jar JPAD.jar^
 -i in\Template_Aircraft\aircraft.xml^
 -ia in\Template_Analyses\analysis.xml^
 -ioc in\Template_Analyses\operating_conditions.xml^
 -da in\Template_Aircraft\lifting_surfaces\airfoils^
 -df in\Template_Aircraft\fuselages^
 -dls in\Template_Aircraft\lifting_surfaces^
 -de in\Template_Aircraft\engines^
 -dn in\Template_Aircraft\nacelles^
 -dlg in\Template_Aircraft\landing_gears^
 -ds in\Template_Aircraft\systems^
 -dcc in\Template_Aircraft\cabin_configurations^
 -d data