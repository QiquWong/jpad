setlocal
set PATH=%PATH%;.\lib;..\..\JPAD-Apps\lib;..\..\JPAD-Apps\lib\JPADAPPS_LIBRARY_FOLDER;..\..\libraries\occjava\OCCJavaWrapper_OCCT_v7.3.0_vc14_Win64;..\..\libraries\occjava\OCCJavaWrapper_OCCT_v7.3.0_vc14_Win64\jre1.8.0_171\bin;

set JAVA_EXEC="%JAVA_HOME%\bin\java.exe"

%JAVA_EXEC% -jar CaWiX.jar^
 -i in\Template_Aircraft\aircraft_IRON_CANARD_LOOP2_FINAL.xml^ 
 --case-config-file in\CaWiX-case-config.xml^
 --database-dir data^ 
 -da in\Template_Aircraft\lifting_surfaces\airfoils^ 
 -df in\Template_Aircraft\fuselages^ 
 -dls in\Template_Aircraft\lifting_surfaces^ 
 -de in\Template_Aircraft\engines^ 
 -dn in\Template_Aircraft\nacelles^ 
 -dlg in\Template_Aircraft\landing_gears^
 -dcc in\Template_Aircraft\cabin_configurations^