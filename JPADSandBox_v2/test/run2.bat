setlocal
rem set PATH=%PATH%; h:\Java\jpad\JPADDatabasesIO\test\lib
rem java -jar test2.jar h:\Java\jpad\JPADDatabasesIO\in\VeDSC.xml
:: -classpath "H:\Java\jpad\JPADDatabasesIO\test\test_lib"

set PATH=%PATH%;.\lib
java -jar test2.jar VeDSC.xml

:: ; h:\Java\jpad\JPADDatabasesIO\test\database\data\VeDSC_database.h5
:: h:\Java\jpad\JPADDatabasesIO\in\VeDSC.xml