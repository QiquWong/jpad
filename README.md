# JPAD, a Java Program toolchain for Aircraft Designers

JPAD is a Java software library, a toolbox providing a set of classes and utility functions that you can use to build software systems. Some features of JPAD are dependent on some native libraries that are provided only for the Windows 64-bit platform. These libraries are open source and users of other platforms, such as Linux or Mac OSX, must build their own version.

The typical user of JPAD is the aircraft designer, one who is interested in aerodynamic or performance predictions and in parametric studies. The principal focus of the library is the overall aircraft model, conceived as a set of interconnected and parametrized submodels: the wing and its variants (horizontal tail, vertical tail, canard), the fuselage, nacelles, the propulsion system.

## JPAD Main modules

- `JPADConfigs`
- `JPADCore_v2`
- `JPADCAD`
- `JPADSandbox_v2`
- `JPADCADSandbox`

The modules `JPADSandbox_v2` and `JPADCADSandbox` are used for testing and development of new features.

# How to use JPAD

__Caveat:__ JPAD is under heavy development and is about to reach a beta state. The codebase is provided here because we believe in the open source philosophy. Be advised the whole repository is approximately 10 GiB. In future all the unnecessary files will be stripped off in order to reduce the codebase footprint.

__Caveat:__ We provide instructions for Windows users. Some dependencies use native libraries that have been tested on Win64 only. A Linux version of JPAD will be provided when the library is declared stable and feature complete (release candidate 1.0). 

## Tools

Download and install:

- [Java SE Development Kit 8](http://www.oracle.com/technetwork/pt/java/javase/downloads/jdk8-downloads-2133151.html)
- [Git bash for Windows](https://git-scm.com/downloads)
- [Eclipse IDE (Oxygen, as of January 2018)](http://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/oxygen2)

## Downloading

You can use a Git bash shell to clone the repository. Open the shell, establish a path where you want to download the files, e.g. `C:\Users\John\Dev`. At the shell prompt move to this position and issue the command:

```
git clone https://github.com/Aircraft-Design-UniNa/jpad.git
```

if you use HTTPS, or issue the command:

```
git clone git@github.com:Aircraft-Design-UniNa/jpad.git
```

if you have set up your shell to use SSH. [See this guide to learn more.](https://help.github.com/articles/which-remote-url-should-i-use/)
Alternatively, you can download the repository as a zip archive [from this link.](https://github.com/Aircraft-Design-UniNa/jpad/archive/master.zip)

If you have cloned successfully the JPAD repository from `C:\Users\John\Dev` you end up with the folder `C:\Users\John\Dev\jpad` populated with all the necessary files to use the library. We call this folder `<JPAD_ROOT>`.


## Importing the projects into Eclipse

Open Eclipse and prepare to import a couple of projects that live in `JPAD_ROOT`.

### Classpath variables

From the menu `Window -> Preferences` open the dialog and navigate in the left pane to `Java -> Build Path -> Classpath Variables`. In the right pane define the following new variables:

- `JPAD_ROOT` pointing to the `jpad` folder, e.g. `C:\Users\John\Dev\jpad`
- `LIBRARY_FOLDER` pointing to the path `<JPAD_ROOT>/libraries`, e.g. `C:\Users\John\Dev\jpad\libraries`
- `ECLIPSE_FOLDER` pointing to the path `<JPAD_ROOT>/libraries/eclipse/plugins`, e.g. `C:\Users\John\Dev\jpad\libraries\eclipse\plugins`

### Import existing Projects into Workspace

#### JPADConfigs

From the menu `File -> Import` select the import wizard `General -> Existing Projects into Workspace`. Click `Next`. Chose `Select root directory` and browse to `<JPAD_ROOT>/JPADConfigs`. The *JPADConfigs* project will be recognized by the wizard. Select the project and click `Finish`.

#### JPADCore_v2

Similarly, from the import wizard select the existing project `JPADCore_v2` by setting the root directory as `<JPAD_ROOT>/JPADCore_v2`. The *JPADCore_v2* project will be recognized by the wizard. Select the project and click `Finish`.

The JPAD API is now set up in place in your IDE. The project JPADCore_v2 depends on JPADConfigs. The API exposes a number of interdependent data structures that model an aircraft and all its subcomponents.

#### JPADCAD

The JPAD repository includes also a module that features advanced CAD functions to model 3D geometric entities, such as NURB surfaces and solids. This project is named *JPADCAD* and employs a Java binding of the C++ [Open CASCADE Technology (OCCT) SDK](https://www.opencascade.com/content/overview) [(v7.0.0)](https://www.opencascade.com/doc/occt-7.0.0/overview/html/index.html).
The JAR file containing the Java wrapper and the supporting native libraries compiled for Win64 are found in the folder `<JPAD_ROOT>libraries/occjava/OCCJavaWrapper_OCCT_v7.0.0_vc14_Win64`. This path will be called `<OCCJAVA>`.

From the Eclipse import wizard select the existing project `JPADCAD` by setting the root directory as `<JPAD_ROOT>/JPADCAD`. The *JPADCAD* project will be recognized by the wizard. Select the project and click `Finish`.

#### Sandboxes

TBD

---
[DAF Research Group at University Naples Federico II](http://www.daf.unina.it/)

**Design of Aircraft and Flight technologies**

<img src="https://github.com/Aircraft-Design-UniNa/jpad/wiki/images/Logo_DAF_Flat-Elevator.png" width="400"/>
