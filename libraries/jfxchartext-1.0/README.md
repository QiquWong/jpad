# JavaFX Chart Extensions #
* Author: Bruce Schubert
* Version: 1.0
* License: BSD 3-Clause

## Summary ##

The JavaFX Chart Extensions project was created to make porting of JFreeChart-based charts to 
to JavaFX charts easier.  The JFreeChart library is very rich and comprehensive; this project
contains several classes that can be added to JavaFX chart derived classes to provide a limited 
set of JFreeChart-like functionality. This functionality includes:

* Subtitles
* ValueMarkers
* XYTextAnnotations
* XYLineAnnotations
* XYImageAnnotations
* XYPolygonAnnotations
* LogarithmicAxis

Also included are several chart types that include the aforementioned functionality:

* **EnhancedScatterChart**: An enhanced version of the ScatterChart with subtitles, markers and annotations.
* **EnhancedLineChart**: An enhanced version of the LineChart with subtitles, markers and annotations.
* **LogScatterChart**: A version the the EnhancedScatterChart with LogarithmicAxis support.
* **LogLineChart**: A version the the EnhancedLineChart with LogarithmicAxis support.

## How do I get set up? ##

* Review the [JavaFX Chart Extensions Wiki](https://bitbucket.org/emxsys/javafx-chart-extensions/wiki/).
* [Download the code](https://bitbucket.org/emxsys/javafx-chart-extensions/downloads), or clone the repository, or [fork the repository](https://bitbucket.org/emxsys/javafx-chart-extensions/fork).
* Compile and run the DemoApp found in the **com.emxsys.demo** package. 
* Simply add the **com.emxsys.chart** package to your project and include the chart extensions in your charts.
* Configuration: Maven
* Dependencies: Java8, JavaFX 
* Original developer IDE: NetBeans 8.0.2

## Contribution guidelines ##

* Forking and Pull Requests are encouraged. 
* Please file bugs and feature requests in the Issue Tracker.

## Who do I talk to? ##

* Bruce Schubert: bruce at emxsys.com