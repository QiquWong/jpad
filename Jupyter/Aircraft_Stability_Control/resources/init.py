# set the style of the notebook
from IPython.core.display import HTML
def css_styling():
    styles = open('./resources/nbstyle.css', 'r').read()
    return HTML(styles)
css_styling()

# load libraries and set plot parameters
import math
import numpy as np
import pandas as pd
import tables as pt
from sympy import *
import scipy
from scipy.interpolate import interp1d, interp2d

import h5py

# https://docs.python.org/3.5/library/shelve.html
import shelve

import sympy

from IPython.display import display, Math, Latex, SVG

from cycler import cycler

import matplotlib.pyplot as plt
from matplotlib.patches import Polygon
from matplotlib.ticker import MultipleLocator, FormatStrFormatter
from IPython.display import set_matplotlib_formats
set_matplotlib_formats('pdf', 'png')
plt.rcParams['savefig.dpi'] = 75

plt.rcParams['figure.autolayout'] = False
plt.rcParams['figure.figsize'] = 10, 6
plt.rcParams['axes.labelsize'] = 18
plt.rcParams['axes.titlesize'] = 20
plt.rcParams['font.size'] = 16
plt.rcParams['lines.linewidth'] = 2.0
plt.rcParams['lines.markersize'] = 8
plt.rcParams['legend.fontsize'] = 14

plt.rcParams['text.usetex'] = True
plt.rcParams['font.family'] = "serif"
plt.rcParams['font.serif'] = "cm"
# plt.rcParams['text.latex.preamble'] = "\usepackage{subdepth}, \usepackage{type1cm}"

#----------------------------------------------------------
def plot_planform(c_r, c_t, b):
    xLineWing = [0,b/2,b/2,0]
    yLineWing = [0,0.25*c_r-0.25*c_t,0.25*c_r+0.75*c_t,c_r]

    # planform
    lineWing, = plt.plot(xLineWing, yLineWing, 'k-')
    # centerline
    centerLine, = plt.plot([0,0], [-1.1*c_r,2.1*c_r], 'b')
    centerLine.set_dashes([8, 4, 2, 4]) 
    # c/4 line
    quarterChordLine, = plt.plot([0,1.05*b], [0.25*c_r,0.25*c_r], 'k--')

    plt.axis('equal')
    plt.axis([-0.1*b/2, 1.1*b/2, -0.1*c_r, 1.1*c_r])
    plt.gca().invert_yaxis()
    plt.title('Wing planform', fontsize=22)
    plt.xlabel('$y$ (m)', fontsize=22)
    plt.ylabel('$X$ (m)', fontsize=22)
    # Moving spines
    ax = plt.gca()  # gca stands for 'get current axis'
    ax.spines['right'].set_color('none')
    ax.spines['top'].set_color('none')
    ax.xaxis.set_ticks_position('bottom')
    ax.spines['bottom'].set_position(('data',2*c_r))
    ax.yaxis.set_ticks_position('left')
    ax.spines['left'].set_position(('data',-0.1*b/2))

    plt.show()

    
#----------------------------------------------------------
def plot_planform_2(c_r, c_t, b,b1,b2,AR,AR1,AR2,CLalpha,CLalpha1,CLalpha2):
    xLineWing = [0,b/2,b/2,0]
    yLineWing = [0,0.25*c_r-0.25*c_t,0.25*c_r+0.75*c_t,c_r]

    # planform
    lineWing, = plt.plot(xLineWing, yLineWing, 'y-', linewidth=2.5, linestyle="-", label=r"b=" +r"{0}".format(b) + r"\,m" +r"\,\,\,\,AR=" +r"{0:.2}" .format(AR)+ r"$\,\,\,\,C_{L\alpha}$=" +r"{0:.3}" .format(CLalpha) +r"$\, rad^{-1}$" )
    # centerline
    centerLine, = plt.plot([0,0], [-1.1*c_r,2.1*c_r], 'b')
    centerLine.set_dashes([8, 4, 2, 4]) 
    # c/4 line
    quarterChordLine, = plt.plot([0,1.05*b], [0.25*c_r,0.25*c_r], 'k--')


    

    xLineWing1= [0,b1/2,b1/2,0]
    yLineWing1= [0,0.25*c_r-0.25*c_t,0.25*c_r+0.75*c_t,c_r]

    # planform
    lineWing1, = plt.plot(xLineWing1, yLineWing1, 'r-',linewidth=2.5, linestyle="-",label= r"b=" +r"{0}".format(b1) + r"\,m" +r"\,\,\,\,AR=" +r"{0:.2}," .format(AR1)+ r"$\,\,\,\,C_{L\alpha}$=" +r"{0:.3}" .format(CLalpha1) +r"$\, rad^{-1}$" )
    # centerline
    centerLine, = plt.plot([0,0], [-1.1*c_r,2.1*c_r], 'b1')
    centerLine.set_dashes([8, 4, 2, 4]) 
    # c/4 line
    quarterChordLine1, = plt.plot([0,1.05*b1], [0.25*c_r,0.25*c_r], 'r--' )

     
    xLineWing2= [0,b2/2,b2/2,0]
    yLineWing2= [0,0.25*c_r-0.25*c_t,0.25*c_r+0.75*c_t,c_r]

    # planform
    lineWing2, = plt.plot(xLineWing2, yLineWing2, 'g-',linewidth=2.5, linestyle="-",label=r"b=" +r"{0:.3}".format(b2) +r"\,m"+r"\,\,\,\,AR=" +r"{0:.2}" .format(AR2)+ r"$\,\,\,\,C_{L\alpha}$=" +r"{0:.3}" .format(CLalpha2) +r"$\, rad^{-1}$" )
    # centerline
    centerLine, = plt.plot([0,0], [-1.1*c_r,2.1*c_r], 'b2')
    centerLine.set_dashes([8, 4, 2, 4]) 
    # c/4 line
    quarterChordLine1, = plt.plot([0,1.05*b1], [0.25*c_r,0.25*c_r], 'g--')

    
    plt.legend(loc='upper center', fontsize=18)

    plt.axis('equal')
    plt.axis([-0.1*max(b/2,b1/2,b2/2), 1.1*max(b/2,b1/2,b2/2), -0.1*c_r, 1.1*c_r])
    plt.gca().invert_yaxis()
    plt.title('Wing planform', fontsize=22)
    plt.xlabel('$y$ (m)', fontsize=22)
    plt.ylabel('$X$ (m)', fontsize=22)
    
    # Moving spines
    ax = plt.gca()  # gca stands for 'get current axis'
    ax.spines['right'].set_color('none')
    ax.spines['top'].set_color('none')
    ax.xaxis.set_ticks_position('bottom')
    ax.spines['bottom'].set_position(('data',2*c_r))
    ax.yaxis.set_ticks_position('left')
    ax.spines['left'].set_position(('data',-0.1*b/2))
    
    plt.show()
    
#----------------------------------------------------------
def plot_planform_cw(c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2, *args, **kwargs):

    fig = plt.subplots(figsize=(9, 9))
    
    # optional arguments
    c_mac = kwargs.get('mac', None)
    X_le_mac = kwargs.get('X_le_mac', None)
    Y_mac = kwargs.get('Y_mac', None)
    X_ac = kwargs.get('X_ac', None)
    
    xLineWing = [0, b_k/2, b/2, b/2, b_k/2, 0]
    dy_k = (b_k/2)*math.tan(Lambda_le_1)
    dy = dy_k + (b/2 - b_k/2)*math.tan(Lambda_le_2)
    yLineWing = [
        0, 
        dy_k, 
        dy,
        dy + c_t, 
        dy_k + c_k, 
        c_r]
        
    # planform
    lineWing, = plt.plot(xLineWing, yLineWing, 'k-')

    plt.scatter(xLineWing, yLineWing, marker='o', s=40)    
    
    # centerline
    centerLine, = plt.plot([0,0], [-0.2*c_r,2.1*c_r], 'b')
    centerLine.set_dashes([8, 4, 2, 4]) 
    # c/4 line
    pC4r = [0, 0.25*c_r]
    pC4k = [b_k/2, dy_k + 0.25*c_k]
    pC4t = [b/2, dy + 0.25*c_t]
    quarterChordLine, = plt.plot([pC4r[0],pC4k[0],pC4t[0]], [pC4r[1],pC4k[1],pC4t[1]], 'k--')
    plt.scatter([pC4r[0],pC4k[0],pC4t[0]], [pC4r[1],pC4k[1],pC4t[1]], marker='o', s=40)

    if ('mac' in kwargs) and ('X_le_mac' in kwargs) and ('Y_mac' in kwargs):
        c_mac = kwargs['mac']
        X_le_mac = kwargs['X_le_mac']
        Y_mac = kwargs['Y_mac']
        #print(mac)
        #print(X_le_mac)
        #print(Y_mac)
        lineMAC, = plt.plot([Y_mac, Y_mac], [X_le_mac, X_le_mac + c_mac], color="red", linewidth=2.5, linestyle="-")
        lineMAC.set_dashes([1000,1]) # HUUUUGE
        lineLEMAC, = plt.plot([0,b/2], [X_le_mac,X_le_mac], color="orange", linewidth=1.5, linestyle="-")
        lineLEMAC.set_dashes([10,2])
        lineTEMAC, = plt.plot([0,b/2], [X_le_mac + c_mac, X_le_mac + c_mac], color="orange", linewidth=1.5, linestyle="-")
        lineTEMAC.set_dashes([10,2])
        plt.scatter(Y_mac, X_le_mac, marker='o', s=40)
        ax = plt.gca()  # gca stands for 'get current axis'
        ax.annotate(
            r'$(Y_{\bar{c}},X_{\mathrm{le},\bar{c}}) = '
                +r'( {0:.3}'.format(Y_mac) + r'\,\mathrm{m}'+r',\,{0:.3}'.format(X_le_mac) + r'\,\mathrm{m} )$',
                         xy=(Y_mac, X_le_mac), xycoords='data',
                         xytext=(20, 30), textcoords='offset points', fontsize=12,
                         arrowprops=dict(arrowstyle="->", connectionstyle="arc3,rad=.2")) # 

    if ('X_le_r_eq' in kwargs) and ('c_r_eq' in kwargs):
        X_le_r_eq = kwargs['X_le_r_eq']
        c_r_eq = kwargs['c_r_eq']
        vertices = [(0, X_le_r_eq)] + [(b/2, dy)] + [(b/2, dy+c_t)] + [(0, X_le_r_eq + c_r_eq)]
        poly = Polygon(vertices, facecolor="yellow", alpha=0.5)
        poly.set_edgecolor("brown")
        poly.set_linewidth(2)
        ax0 = plt.gca()  # gca stands for 'get current axis'
        ax0.add_patch(poly)
        
    if 'X_ac' in kwargs:
        X_ac = kwargs['X_ac']
        #print(X_ac)
        plt.scatter(0, X_ac, marker='o', s=40)
        lineAC, = plt.plot([0,b/2], [X_ac,X_ac], color="brown", linewidth=3.5, linestyle="-")
        lineAC.set_dashes([10,2.5,3,2.5])
        ax = plt.gca()  # gca stands for 'get current axis'
        ax.annotate(r'$X_{\mathrm{ac,W}} = '+r'{0:.3}'.format(X_ac)+r'\,\mathrm{m} $',
                         xy=(b/2, X_ac), xycoords='data',
                         xytext=(20, 30), textcoords='offset points', fontsize=12,
                         arrowprops=dict(arrowstyle="->", connectionstyle="arc3,rad=.2")) # 

    plt.axis('equal')
    
    #    xmajorLocator = MultipleLocator(2.0)
    #    xmajorFormatter = FormatStrFormatter('%.1f')
    #    xminorLocator = MultipleLocator(4)
    #    ax = plt.gca()  # gca stands for 'get current axis'
    #    ax.xaxis.set_major_locator(xmajorLocator)
    #    ax.xaxis.set_major_formatter(xmajorFormatter)
    #    # for the minor ticks, use no labels; default NullFormatter
    #    ax.xaxis.set_minor_locator(xminorLocator)
    
    plt.axis([-0.02*b/2, 1.1*b/2, -0.05*c_r, 1.1*(dy + c_t)])
    plt.gca().invert_yaxis()
    plt.title('Wing planform', fontsize=16)
    plt.xlabel('$y$ (m)', fontsize=16)
    plt.ylabel('$X$ (m)', fontsize=16)
    # Moving spines
    ax = plt.gca()  # gca stands for 'get current axis'
    ax.spines['right'].set_color('none')
    ax.spines['top'].set_color('none')
    ax.xaxis.set_ticks_position('bottom')
    ax.spines['bottom'].set_position(('outward',10)) # outward by 10 points
    ax.yaxis.set_ticks_position('left')
    ax.spines['left'].set_position(('data',-0.07*b/2))

    plt.show()
    
#----------------------------------------------------
def plot_planform_sweep(c_r, c_t, b, Lambda_le, *args, **kwargs):



    # optional arguments

    c_mac = kwargs.get('mac', None)

    X_le_mac = kwargs.get('X_le_mac', None)

    Y_mac = kwargs.get('Y_mac', None)

    X_ac = kwargs.get('X_ac', None)

    

    xLineWing = [0,b/2,b/2,0]

    dy = (b/2)*math.tan(Lambda_le)

    yLineWing = [

        0, 0.25*c_r - 0.25*c_t + dy,

        0.25*c_r + 0.75*c_t + dy, c_r]



    # planform

    lineWing, = plt.plot(xLineWing, yLineWing, 'k-')

    # centerline

    centerLine, = plt.plot([0,0], [-1.1*c_r,2.1*c_r], 'b')

    centerLine.set_dashes([8, 4, 2, 4]) 

    # c/4 line

    pC4r = [0,0.25*c_r]

    pC4t = [b/2,dy + 0.25*c_r]

    quarterChordLine, = plt.plot([pC4r[0],pC4t[0]], [pC4r[1],pC4t[1]], 'k--')



    if ('mac' in kwargs) and ('X_le_mac' in kwargs) and ('Y_mac' in kwargs):

        c_mac = kwargs['mac']

        X_le_mac = kwargs['X_le_mac']

        Y_mac = kwargs['Y_mac']

        #print(mac)

        #print(X_le_mac)

        #print(Y_mac)

        lineMAC, = plt.plot([Y_mac, Y_mac], [X_le_mac, X_le_mac + c_mac], color="red", linewidth=2.5, linestyle="-")

        lineMAC.set_dashes([1000,1]) # HUUUUGE

        lineLEMAC, = plt.plot([0,b/2], [X_le_mac,X_le_mac], color="orange", linewidth=1.5, linestyle="-")

        lineLEMAC.set_dashes([10,2])

        lineTEMAC, = plt.plot([0,b/2], [X_le_mac + c_mac, X_le_mac + c_mac], color="orange", linewidth=1.5, linestyle="-")

        lineTEMAC.set_dashes([10,2])

        plt.scatter(Y_mac, X_le_mac, marker='o', s=40)

        ax = plt.gca()  # gca stands for 'get current axis'

        ax.annotate(

            r'$(Y_{\bar{c}},X_{\mathrm{le},\bar{c}}) = '

                +r'( {0:.3}'.format(Y_mac) + r'\,\mathrm{m}'+r',\,{0:.3}'.format(X_le_mac) + r'\,\mathrm{m} )$',

                         xy=(Y_mac, X_le_mac), xycoords='data',

                         xytext=(20, 30), textcoords='offset points', fontsize=12,

                         arrowprops=dict(arrowstyle="->", connectionstyle="arc3,rad=.2")) # 

        

    if 'X_ac' in kwargs:

        X_ac = kwargs['X_ac']

        #print(X_ac)

        plt.scatter(0, X_ac, marker='o', s=40)

        lineAC, = plt.plot([0,b/2], [X_ac,X_ac], color="brown", linewidth=3.5, linestyle="-")

        lineAC.set_dashes([10,2.5,3,2.5])

        ax = plt.gca()  # gca stands for 'get current axis'

        ax.annotate(r'$X_{\mathrm{ac,W}} = '+r'{0:.3}'.format(X_ac)+r'\,\mathrm{m} $',

                         xy=(b/2, X_ac), xycoords='data',

                         xytext=(20, 30), textcoords='offset points', fontsize=12,

                         arrowprops=dict(arrowstyle="->", connectionstyle="arc3,rad=.2")) # 



    plt.axis('equal')

    

    #    xmajorLocator = MultipleLocator(2.0)

    #    xmajorFormatter = FormatStrFormatter('%.1f')

    #    xminorLocator = MultipleLocator(4)

    #    ax = plt.gca()  # gca stands for 'get current axis'

    #    ax.xaxis.set_major_locator(xmajorLocator)

    #    ax.xaxis.set_major_formatter(xmajorFormatter)

    #    # for the minor ticks, use no labels; default NullFormatter

    #    ax.xaxis.set_minor_locator(xminorLocator)

    

    plt.axis([-0.02*b/2, 1.1*b/2, -0.1*c_r, c_r + 1.1*dy])

    plt.gca().invert_yaxis()

    plt.title('Wing planform', fontsize=16)

    plt.xlabel('$y$ (m)', fontsize=16)

    plt.ylabel('$X$ (m)', fontsize=16)

    # Moving spines

    ax = plt.gca()  # gca stands for 'get current axis'

    ax.spines['right'].set_color('none')

    ax.spines['top'].set_color('none')

    ax.xaxis.set_ticks_position('bottom')

    ax.spines['bottom'].set_position(('data',c_r + 1.15*dy))

    ax.yaxis.set_ticks_position('left')

    ax.spines['left'].set_position(('data',-0.1*b/2))



    plt.show()    
    
#----------------------------------------------------------

def plot_wing_functions(c_r, c_k, c_t, 

                    eps_k, eps_t, alpha0l_r, alpha0l_k, alpha0l_t,

                    b_k, b, Lambda_le_1, Lambda_le_2, 

                    *args, **kwargs):

    """

    

    See: http://www.scipy-lectures.org/intro/matplotlib/matplotlib.html

    

    """



    # optional arguments

    f_chord = kwargs.get('f_chord', None)

    f_Xle = kwargs.get('f_Xle', None)

    f_twist = kwargs.get('f_twist', None)

    f_alpha0l = kwargs.get('f_alpha0l', None)

    f_S_integral = kwargs.get('f_S_integral', None)

    f_mac_integral = kwargs.get('f_mac_integral', None)

    f_Xle_mac_integral = kwargs.get('f_Xle_mac_integral', None)

    f_Y_mac_integral = kwargs.get('f_Y_mac_integral', None)

    f_alpha0L_integral_indefinite = kwargs.get('f_alpha0L_integral_indefinite', None)



    n_points = kwargs.get('f_chord', None)

    if ('n_points' in kwargs):

        n_points = kwargs['n_points']

    else:

        n_points = 20



    # define vectors

    vY0 = np.linspace(0, b/2, n_points, endpoint=True)

    vY1 = np.concatenate([vY0,[b_k/2]])

    vY = np.sort(np.unique(vY1))



    the_figsize = kwargs.get('figsize', None)

    if ('figsize' in kwargs):

        the_figsize = kwargs['figsize']

    else:

        the_figsize = (11,12)





    # Create a figure of size WxH inches, DPI dots per inch

    fig = plt.figure(figsize=the_figsize, dpi=300)

    

    # Create a new subplot from a grid of 1x1

    ax0 = plt.subplot(1, 1, 1)

    

    #fig, ax0 = plt.subplots()

    

    if ('f_chord' in kwargs):

        y = sympy.Symbol('y')

        f_chord = kwargs['f_chord']

        vChord = []

        for y in vY:

            vChord.append(f_chord(y,c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2))

        vChord = np.asarray(vChord)

        plt.plot(vY, vChord, color="red", linewidth=2.5, linestyle="-", label=r'local chord $c$ (m)')



    if ('f_Xle' in kwargs):

        y = sympy.Symbol('y')

        f_Xle = kwargs['f_Xle']

        vXle = []

        for y in vY:

            vXle.append(f_Xle(y, b_k, b, Lambda_le_1, Lambda_le_2))

        vXle = np.asarray(vXle)

        plt.plot(vY, vXle, color="green", linewidth=2.5, linestyle="-", label=r'local l.e. coordinate $X_{\mathrm{le}}$ (m)')

        

    if ('f_twist' in kwargs):

        y = sympy.Symbol('y')

        f_twist = kwargs['f_twist']

        vTwist = []

        for y in vY:

            vTwist.append(f_twist(y, eps_k, eps_t, b_k, b))

        vTwist = np.asarray(vTwist)

        plt.plot(vY, vTwist*180/np.pi, color="blue",  linewidth=2.5, linestyle="-", label=r"local $\epsilon_{\mathrm{g}}$ (deg)")



    if ('f_alpha0l' in kwargs):

        y = sympy.Symbol('y')

        f_alpha0l = kwargs['f_alpha0l']

        vAlpha0l = []

        for y in vY:

            vAlpha0l.append(f_alpha0l(y, alpha0l_r, alpha0l_k, alpha0l_t, b_k, b))

        vAlpha0l = np.asarray(vAlpha0l)

        plt.plot(vY, vAlpha0l*180/np.pi, color="brown",  linewidth=2.5, linestyle="-", label=r"local $\alpha_{0\ell}$ (deg)")



    if ('f_S_integral' in kwargs):

        f_S_integral = kwargs['f_S_integral']

        vS_integrand = []

        for y_ in vY:

            #print(y_)

            vS_integrand.append(f_S_integral(y_, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2))

        vS_integrand = np.asarray(vS_integrand)

        plt.plot(vY, vS_integrand,  linewidth=2.5, linestyle="-", dashes=[1000,1], marker="." ,

                 label=r"function $c(y)$ (m)")         

        # shaded region --> http://matplotlib.org/examples/showcase/integral_demo.html

        vertices = [(0, 0)] + list(zip(vY, vS_integrand)) + [(b/2, 0)]

        poly = Polygon(vertices, facecolor="green", alpha=0.3, edgecolor="none")

        ax0.add_patch(poly)



    if ('f_mac_integral' in kwargs):

        f_mac_integral = kwargs['f_mac_integral']

        vMac_integrand = []

        for y_ in vY:

            vMac_integrand.append(f_mac_integral(y_, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2))

        vMac_integrand = np.asarray(vMac_integrand)

        plt.plot(vY, vMac_integrand, linewidth=2.5, linestyle="-", dashes=[1000,1], marker="." ,

                 label=r"function $c^2(y)$ (m${}^2$)")         

        # shaded region --> http://matplotlib.org/examples/showcase/integral_demo.html

        vertices = [(0, 0)] + list(zip(vY, vMac_integrand)) + [(b/2, 0)]

        poly = Polygon(vertices, facecolor="orange", alpha=0.3, edgecolor="none")

        ax0.add_patch(poly)



    if ('f_Xle_mac_integral' in kwargs):

        f_Xle_mac_integral = kwargs['f_Xle_mac_integral']

        vXle_mac_integrand = []

        for y_ in vY:

            vXle_mac_integrand.append(f_Xle_mac_integral(y_, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2))

        vXle_mac_integrand = np.asarray(vXle_mac_integrand)

        plt.plot(vY, vXle_mac_integrand, linewidth=2.5, linestyle="-", dashes=[1000,1], marker="." ,

                 label=r"function $X_{\mathrm{le}}(y)\,c(y)$ (m${}^2$)")         

        # shaded region --> http://matplotlib.org/examples/showcase/integral_demo.html

        vertices = [(0, 0)] + list(zip(vY, vXle_mac_integrand)) + [(b/2, 0)]

        poly = Polygon(vertices, facecolor="blue", alpha=0.3, edgecolor="none")

        ax0.add_patch(poly)



    if ('f_Y_mac_integral' in kwargs):

        f_Y_mac_integral = kwargs['f_Y_mac_integral']

        vY_mac_integrand = []

        for y_ in vY:

            vY_mac_integrand.append(f_Y_mac_integral(y_, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2))

        vY_mac_integrand = np.asarray(vY_mac_integrand)

        plt.plot(vY, vY_mac_integrand, linewidth=2.5, linestyle="-", dashes=[1000,1], marker="." ,

                 label=r"function $y\,c(y)$ (m${}^2$)")         

        # shaded region --> http://matplotlib.org/examples/showcase/integral_demo.html

        vertices = [(0, 0)] + list(zip(vY, vY_mac_integrand)) + [(b/2, 0)]

        poly = Polygon(vertices, facecolor="brown", alpha=0.3, edgecolor="none")

        ax0.add_patch(poly)



    if ('f_alpha0L_integral' in kwargs):

        f_alpha0L_integral = kwargs['f_alpha0L_integral']

        vAlpha0L_integrand = []

        for y_ in vY:

            vAlpha0L_integrand.append(

                f_alpha0L_integral(y_, c_r, c_k, c_t, 

                                   eps_k, eps_t, alpha0l_r, alpha0l_k, alpha0l_t, 

                                   b_k, b, Lambda_le_1, Lambda_le_2)

                )

        vAlpha0L_integrand = np.asarray(vAlpha0L_integrand)

        plt.plot(vY, vAlpha0L_integrand*180/np.pi, linewidth=2.5, linestyle="-", dashes=[1000,1], marker="." ,

                 label=r"function $\big(\alpha_{0\ell} - \epsilon_{\mathrm{g}}\big) c$ (deg$\,$m)")         

        # shaded region --> http://matplotlib.org/examples/showcase/integral_demo.html

        vertices = [(0, 0)] + list(zip(vY, vAlpha0L_integrand*180/np.pi)) + [(b/2, 0)]

        poly = Polygon(vertices, facecolor="red", alpha=0.3, edgecolor="none")

        ax0.add_patch(poly)



    # shaded region --> http://matplotlib.org/examples/showcase/integral_demo.html

    #vertices = [(0, 0)] + list(zip(vY, vIntegrand*180/np.pi)) + [(b/2, 0)]

    #poly = Polygon(vertices, facecolor="orange", alpha=0.5, edgecolor="none")

    #ax0.add_patch(poly)

    

    plt.legend(loc='upper center', fontsize=18)

    

    if ('xmin' in kwargs):

        xmin = kwargs['xmin']

    else:

        xmin = 0



    if ('xmax' in kwargs):

        xmax = kwargs['xmax']

    else:

        xmax = 1.1*b/2



    if ('ymin' in kwargs):

        ymin = kwargs['ymin']

    else:

        ymin = -5



    if ('ymax' in kwargs):

        ymax = kwargs['ymax']

    else:

        ymax = 8

    

    plt.axis([xmin, xmax, ymin, ymax])

    

    # some annotations

    tipLine, = plt.plot([b/2,b/2], [0.9*ymin, 0.8*ymax], color="gray", linewidth=1.0, linestyle="-")

    tipLine.set_dashes([8, 4]) 

    plt.annotate(r'$y=\frac{1}{2}\,b$',

                 xy=(b/2, 0.65*ymin), xycoords='data',

                 xytext=(40, -40), textcoords='offset points', fontsize=22,

                 arrowprops=dict(arrowstyle="->", connectionstyle="arc3,rad=.5"))



    kinkLine, = plt.plot([b_k/2,b_k/2], [0.9*ymin, 0.8*ymax], color="gray", linewidth=1.0, linestyle="-")

    kinkLine.set_dashes([8, 4]) 

    plt.annotate(r'$y=\frac{1}{2}\,b_{\mathrm{k}}$',

                 xy=(b_k/2, 0.65*ymin), xycoords='data',

                 xytext=(40, -40), textcoords='offset points', fontsize=22,

                 arrowprops=dict(arrowstyle="->", connectionstyle="arc3,rad=.5"))

    

    zeroYLine, = plt.plot([xmin,xmax], [0,0],  color="gray", linewidth=1.0, linestyle="-")

    zeroYLine.set_dashes([8, 4]) 

    

    plt.title('Wing functions', fontsize=22)

    plt.xlabel('$y$ (m)', fontsize=22)

    #plt.ylabel('$X$ (m)', fontsize=22)

    

    # Moving spines

    ax = plt.gca()  # gca stands for 'get current axis'

    ax.spines['right'].set_color('none')

    ax.spines['top'].set_color('none')

    ax.xaxis.set_ticks_position('bottom')



    vshift_xaxis = kwargs.get('vshift_xaxis', None)

    if ('vshift_xaxis' in kwargs):

        vshift_xaxis = kwargs['vshift_xaxis']

    else:

        vshift_xaxis = 10



    ax.spines['bottom'].set_position(('outward',vshift_xaxis))

    

    hshift_yaxis = kwargs.get('hshift_yaxis', None)

    if ('hshift_yaxis' in kwargs):

        hshift_yaxis = kwargs['hshift_yaxis']

    else:

        hshift_yaxis = 10

        

    ax.yaxis.set_ticks_position('left')

    ax.spines['left'].set_position(('outward',hshift_yaxis))    



    plt.show()

#----------------------------------------------------------

def plot_wing_functions_1(c_r, c_t, b, A_c, B_c, A_alpha, B_alpha, A_eps):

    """

    

    See: http://www.scipy-lectures.org/intro/matplotlib/matplotlib.html

    

    """

    

    # define vectors

    vY = np.linspace(0, b/2, 10, endpoint=True)

    vChord = A_c*vY + B_c

    vAlpha0L = A_alpha*vY + B_alpha

    vEpsilon = A_eps*vY

    vIntegrand = (vAlpha0L - vEpsilon)*vChord

    

    # Create a figure of size WxH inches, DPI dots per inch

    fig = plt.figure(figsize=(9, 8), dpi=300)

    

    # Create a new subplot from a grid of 1x1

    ax0 = plt.subplot(1, 1, 1)

    

    #fig, ax0 = plt.subplots()

    

    

    plt.plot(vY, vChord, color="red", linewidth=2.5, linestyle="-", label="local chord (m)")

    plt.plot(vY, vAlpha0L*180/np.pi, color="green",  linewidth=2.5, linestyle="-", label=r"local $\alpha_{0\ell}$ (deg)")

    plt.plot(vY, vEpsilon*180/np.pi, color="blue",  linewidth=2.5, linestyle="-", label=r"local $\epsilon_{\mathrm{g}}$ (deg)")

    plt.plot(vY, vIntegrand*180/np.pi, color="brown",  linewidth=2.5, linestyle="-", 

             label=r"Integrand function $\big(\alpha_{0\ell}$ - \epsilon_{\mathrm{g}}\big) c$ (deg m)")

    

    # shaded region --> http://matplotlib.org/examples/showcase/integral_demo.html

    vertices = [(0, 0)] + list(zip(vY, vIntegrand*180/np.pi)) + [(b/2, 0)]

    poly = Polygon(vertices, facecolor="orange", alpha=0.5, edgecolor="none")

    ax0.add_patch(poly)

    

    plt.legend(loc='upper center', fontsize=18)

    

    tipLine, = plt.plot([b/2,b/2], [-4, 2], color="gray", linewidth=1.0)

    tipLine.set_dashes([8, 4]) 

    plt.annotate(r'$y=\frac{b}{2}$',

                 xy=(b/2, -4), xycoords='data',

                 xytext=(40, -40), textcoords='offset points', fontsize=22,

                 arrowprops=dict(arrowstyle="->", connectionstyle="arc3,rad=.5"))

    

    zeroYLine, = plt.plot([-1,b/2], [0,0],  color="gray", linewidth=1.0)

    zeroYLine.set_dashes([8, 4]) 

    

    plt.axis([0, 1.1*b/2, -5, 6])

    plt.title('Wing functions', fontsize=22)

    plt.xlabel('$y$ (m)', fontsize=22)

    #plt.ylabel('$X$ (m)', fontsize=22)

    

    # Moving spines

    ax = plt.gca()  # gca stands for 'get current axis'

    ax.spines['right'].set_color('none')

    ax.spines['top'].set_color('none')

    ax.xaxis.set_ticks_position('bottom')

    ax.spines['bottom'].set_position(('data',-6))

    ax.yaxis.set_ticks_position('left')

    ax.spines['left'].set_position(('data',-0.1))    

    plt.show()


#----------------------------------------------------------
def display_workflow_S(b, A_c, B_c, c_law_integral_indefinite, c_law_integral_definite):
    c_law_integral_indefinite_latex = latex(c_law_integral_indefinite)
    return Latex(
        r'\begin{align*}'
            r'S & {}= 2 \int_0^{' + '{0:.3}'.format(b/2) + '}' 
            + r'\Big(' 
            +   r'{0:.4}'.format(A_c) + r'\, y + ' + '{0:.4}'.format(B_c) + r'\,\text{m}'
            + r'\Big)\,\text{d}y' 
            + r'\\[1em]' 
            + r'& {}= 2 \big(' 
            +   c_law_integral_indefinite_latex 
            + r'\big)\Bigr|_0^{' + '{0:.4}'.format(b/2) + r'} \,\text{m}^2'
            + '=' + '{0:.4}'.format(c_law_integral_definite) + r'\,\text{m}^2'
        + r'\end{align*}'
    )

#----------------------------------------------------------
def display_workflow_alpha0L(b, S_ref, A_c, B_c, A_alpha, B_alpha, A_eps,
                             alpha0L_law_integral_indefinite,
                             alpha0L):
    alpha0L_law_integral_indefinite_latex = latex(alpha0L_law_integral_indefinite)
    return Latex(
        r'\begin{multline*}'
            + r'\alpha_{0L,\mathrm{W}} = \frac{2}{' + '{0:.4}'.format(S_ref) + r'\,\text{m}^2}'
            + r'\int_0^{' + '{0:.3}'.format(b/2) + r'}' 
            + r'\Big(' 
            +   r'{0:.4}'.format(A_alpha) + r'\, \frac{\text{rad}}{\text{m}}\,y ' 
            +     '{0:.4}'.format(B_alpha) + r'\,\text{rad} +'
            +     '{0:.4}'.format(A_eps) + r'\,\frac{\text{rad}}{\text{m}}y'            
            + r'\Big)\,'
            + r'\Big(' 
            +   r'{0:.4}'.format(A_c) + r'\, y + ' + '{0:.4}'.format(B_c) + r'\,\text{m}'
            + r'\Big)\,'
            + r'\text{d}y' 
            + r'\\[1em]' 
            + r'= \frac{2}{' + '{0:.4}'.format(S_ref) + r'} \big(' 
            +   alpha0L_law_integral_indefinite_latex 
            + r'\big)\Bigr|_0^{' + '{0:.4}'.format(b/2) + r'} \,\text{rad}'
            + r'\\[1em]' 
            + r'= {0:.4}'.format(alpha0L) + r'\,\text{rad}'
            + r'= {0:.4}'.format(alpha0L*180/math.pi) + r'\,\text{deg}'
         + r'\end{multline*}'
        )
#---------------------------------------------------------------
def display_workflow_alpha0L_cw(b, b1, S_ref, A_c1, B_c1, A_alpha1, B_alpha1, A_eps1,B_eps1,
                             A_c2, B_c2, A_alpha2, B_alpha2, A_eps2, B_eps2,
                             alpha0L_law_integral_indefinite_1,alpha0L_law_integral_indefinite_2,
                             alpha0L_1,alpha0L_2,alpha0L):
    alpha0L_law_integral_indefinite_latex_1 = latex(alpha0L_law_integral_indefinite_1)
    alpha0L_law_integral_indefinite_latex_2 = latex(alpha0L_law_integral_indefinite_2)
    return Latex(
        r'\begin{multline*}'
            + r'\alpha_{0L,\mathrm{W}} = \frac{2}{' + '{0:.4}'.format(S_ref) + r'\,\text{m}^2}'
            + r'\int_0^{' + '{0:.3}'.format(b1/2) + r'}' 
            + r'\Big(' 
            +   r'{0:.4}'.format(A_alpha1) + r'\, \frac{\text{rad}}{\text{m}}\,y ' 
            +     '{0:.4}'.format(B_alpha1) + r'\,\text{rad} +'
            +     '{0:.4}'.format(A_eps1) + r'\,\frac{\text{rad}}{\text{m}}y'            
            + r'\Big)\, \cdot'
            + r'\Big(' 
            +   r'{0:.4}'.format(A_c1) + r'\, y + ' + '{0:.4}'.format(B_c1) + r'\,\text{m}'
            + r'\Big)\,'
            + r'\text{d}y' +r'\\+'
            +r'\frac{2}{' + '{0:.4}'.format(S_ref) + r'\,\text{m}^2}'
            + r'\int_{'+'{0:.3}'.format(b1/2) + r'}^{' + '{0:.3}'.format(b/2) + r'}' 
                + r'\Big[' 
            +   r'{0:.4}'.format(A_alpha2) + r'\, \frac{\text{rad}}{\text{m}}\,(Y - '+'{0:.3}'.format(b1/2)+r'\, \text{m}\,)'
            +     '{0:.4}'.format(B_alpha2) + r'\,\text{rad}'
            +     '{0:.4}'.format(A_eps2) + r'\,\frac{\text{rad}}{\text{m}}(Y - '+'{0:.3}'.format(b1/2)+r'\,\text{m}\,)'+' + {0:.3}'.format(B_eps2)             
            + r'\Big]\,'
            + r'\Big[' 
            +   r'{0:.4}'.format(A_c2) + r'\, (Y - '+'{0:.3}'.format(b1/2)+r'\, \text{m}\,) +'  + '{0:.4}'.format(B_c2) + r'\,\text{m}'
            + r'\Big]\,'
            + r'\text{d}y' 
            + r'\\[1em]' 
            + r'= \frac{2}{' + '{0:.4}'.format(S_ref) + r'\,\text{m}^2} \big(' 
            +   alpha0L_law_integral_indefinite_latex_1
            + r'\big)\Bigr|_0^{' + '{0:.4}'.format(b1/2) + r'} \,\text{rad} + '
            + r'\frac{2}{' + '{0:.4}'.format(S_ref) + r'\,\text{m}^2} \big(' 
            +   alpha0L_law_integral_indefinite_latex_2
            + r'\big)\Bigr|_{'+r'{0:.4}'.format(b1/2)+r'}^{' + '{0:.4}'.format(b/2) + r'} \,\text{rad}'
            + r'\\[1em]' 
            + r'= {0:.4}'.format(alpha0L_1) + r'\,\text{rad}' + r' {0:.4}'.format(alpha0L_2) + r'\,\text{rad}'
            + r'= {0:.4}'.format(alpha0L*180/math.pi) + r'\,\text{deg}'
        + r'\end{multline*}'
        )
#----------------------------------------------------------
def display_workflow_alpha0L_bar(b, S_ref, A_c, B_c, A_alpha, B_alpha,
                             alpha0L_law_integral_indefinite,
                             alpha0L):
    alpha0L_law_integral_indefinite_latex = latex(alpha0L_law_integral_indefinite)
    return Latex(
        r'\begin{multline*}'
            + r'\bar{\alpha}_{0\ell} = \frac{2}{' + '{0:.4}'.format(S_ref) + r'\,\text{m}^2}'
            + r'\int_0^{' + '{0:.3}'.format(b/2) + r'}' 
            + r'\Big(' 
            +   r'{0:.4}'.format(A_alpha) + r'\, \frac{\text{rad}}{\text{m}}\,y ' 
            +     '{0:.4}'.format(B_alpha) + r'\,\text{rad}'         
            + r'\Big)\,'
            + r'\Big(' 
            +   r'{0:.4}'.format(A_c) + r'\, y + ' + '{0:.4}'.format(B_c) + r'\,\text{m}'
            + r'\Big)\,'
            + r'\text{d}y' 
            + r'\\[1em]' 
            + r'= \frac{2}{' + '{0:.4}'.format(S_ref) + r'} \big(' 
            +   alpha0L_law_integral_indefinite_latex 
            + r'\big)\Bigr|_0^{' + '{0:.4}'.format(b/2) + r'} \,\text{rad}'
            + r'\\[1em]' 


            + r'= {0:.4}'.format(alpha0L) + r'\,\text{rad}'
            + r'= {0:.4}'.format(alpha0L*180/math.pi) + r'\,\text{deg}'
        + r'\end{multline*}'
        )
#-------------------------------------------------------------------------------
def display_workflow_alpha0L_thick(b, S_ref, A_c, B_c, A_t, B_t,
                             thick_law_integral_indefinite,
                             mean_thick):
    thick_law_integral_indefinite_latex = latex(thick_law_integral_indefinite)
    return Latex(
        r'\begin{multline*}'
            + r'\overline{(t/c)}  = \frac{2}{' + '{0:.4}'.format(S_ref) + r'\,\text{m}^2}'
            + r'\int_0^{' + '{0:.3}'.format(b/2) + r'}' 
            + r'\Big(' 
            +   r'{0:.4}'.format(A_t) + r'\,\text{m}^{-1}\,y + ' 
            +     '{0:.4}'.format(B_t)          
            + r'\Big)\,'
            + r'\Big(' 
            +   r'{0:.4}'.format(A_c) + r'\, y + ' + '{0:.4}'.format(B_c) + r'\,\text{m}'
            + r'\Big)\,'
            + r'\text{d}y' 
            + r'\\[1em]' 
            + r'= \frac{2}{' + '{0:.4}'.format(S_ref) + r'} \big(' 
            +   thick_law_integral_indefinite_latex 
            + r'\big)\Bigr|_0^{' + '{0:.4}'.format(b/2) + r'} \,'
            + r'\\[1em]' 


            + r'= {0:.4}'.format(mean_thick) 
        + r'\end{multline*}'
        )


#--------------------------------------------------------
def display_workflow_c_mac(S_ref, b, A_c, B_c, c_mac_law_integral_indefinite, mac):

    c_mac_law_integral_indefinite_latex = latex(c_mac_law_integral_indefinite)

    return Latex(

        r'\begin{align*}'

            + r'\bar{c} & {}=\frac{2}{S} \int_{0}^{b/2} c^2(y) \, \mathrm{d}y'

            + r'\\[1em]' 

            + r'& {}= \frac{2}{'+ '{0:.4}'.format(S_ref) +r'\,\mathrm{m}^2} \int_0^{' + '{0:.3}'.format(b/2) + '}' 

            + r'\Big(' 

            +   r'{0:.4}'.format(A_c) + r'\, y + ' + '{0:.4}'.format(B_c) + r'\,\text{m}'

            + r'\Big)^2\,\text{d}y' 

            + r'\\[1em]' 

            + r'& {}= \frac{2}{'+ '{0:.4}'.format(S_ref) +r'} \big(' 

            +   c_mac_law_integral_indefinite_latex 

            + r'\big)\Bigr|_0^{' + '{0:.4}'.format(b/2) + r'}'

            + '=' + '{0:.4}'.format(mac) + r'\,\text{m}'

        + r'\end{align*}'

    )



#----------------------------------------------------------
def display_workflow_c_maca(S_ref, b,A_CM,B_CM, A_c, B_c, c_mac_a_law_integral_indefinite,c_mac_a):
    c_mac_a_law_integral_indefinite_latex = latex(c_mac_a_law_integral_indefinite)
    return Latex(
        r'\begin{align*}'
            + r'C_\mathrm{M_\mathrm{ac,a}} & {}=\frac{2}{S} \int_{0}^{b/2} C_{m_{ac},2D}(Y)c^2(y) \, \mathrm{d}y'
            + r'\\[1em]' 
            + r'& {}= \frac{2}{'+ '{0:.4}'.format(S_ref) +r'\,\mathrm{m}^2} \int_0^{' + '{0:.3}'.format(b/2) + '}' 
            + r'\Big(' 
            +   r'{0:.4}'.format(A_CM)+ r'\,\text{m}' + r'\, y + ' + '{0:.4}'.format(B_CM) 
            + r'\Big)' 
           + r'\Big(' 
            +   r'{0:.4}'.format(A_c) + r'\, y + ' + '{0:.4}'.format(B_c) + r'\,\text{m}'
            + r'\Big)^2\,\text{d}y' 
            + r'\\[1em]' 
            + r'& {}= \frac{2}{'+ '{0:.4}'.format(S_ref) +r'} \big(' 
            +   c_mac_a_law_integral_indefinite_latex 
            + r'\big)\Bigr|_0^{' + '{0:.4}'.format(b/2) + r'}'
            + '=' + '{0:.4}'.format(c_mac_a) 
        + r'\end{align*}'
    )
#-------------------------------------------------------
def display_workflow_c_macb(S_ref, b,A_alpha,B_alpha, A_c, B_c,Acl_alpha,B_clalpha ,A_eps,c_mac_b_law_integral_indefinite, c_mac_b,c_mean,alpha0L,X_ac,Lambda_le):
    c_mac_b_law_integral_indefinite_latex = latex(c_mac_b_law_integral_indefinite)
    return Latex(
        r'\begin{align*}'
            + r'C_\mathrm{M_\mathrm{ac,b}} & {}=\frac{1}{S\bar{c}} \int_{0}^{b/2} \big(cC_\ell\big)_b\,\big(y\big)\,x_b\big(y\big) \, \mathrm{d}y'
        + r'\\'
       + r'& {}= \frac{1}{S\bar{c}} \int_{0}^{b/2} \big(A_cy + B_c\big)\,\big(A_\mathrm{C_{\ell\alpha}}y + B\mathrm{C_{\ell\alpha}}\big)\, \big\{\alpha_\mathrm{0L} - \big[\big(A_{\alpha}y + B_{\alpha} \big)- A_{\epsilon}y\big]\big\}\cdot\big\{X_\mathrm{ac} -\big[y\,\mathrm{tan}\Lambda_{le} + \dfrac{1}{4}\big( A_c y + B_c\big)\big]\big\} \, \mathrm{d}y' 
+r'\\'
+ r'& {}= \frac{1}{'+ '{0:.4}'.format(S_ref) +r'\,\mathrm{m}^2'+r'{0:.4}'.format(c_mean)+r'\, \mathrm{m} }'+r' \int_0^{' + '{0:.3}'.format(b/2) + '}' 
            + r'\Big(' 
            +   r'{0:.4}'.format(A_c) + r'\, y + ' + '{0:.4}'.format(B_c) + r'\,\mathrm{m}' 
            + r'\Big)' 
           + r'\Big(' 
            +   r'{0:.4}'.format(Acl_alpha) + r'\,(\mathrm{rad}\,\mathrm{m})^{-1}'+ r'\, y + ' + '{0:.4}'.format(B_clalpha) + r'\,\mathrm{rad}^{-1} \big)'
              + r'\Big\{'
              + r'{0:.4}'.format(alpha0L)+r'\,\mathrm{rad}'+ r'\, - '
                + r'\Big['
                   +r'\big('
                   + r'{0:.4}'.format(A_alpha) + r'\,\dfrac{\mathrm{rad}}{\mathrm{m}}'+r'\, y + \big( ' 
                   + '{0:.4}'.format (B_alpha) + r'\,\mathrm{rad}\big)'
                   +r'\big)'
                + r'-\big(\,'+r'{0:.4}'.format(A_eps)+ r'\,\dfrac{\mathrm{rad}}{\mathrm{m}}'+ r'\, y \big) '
                + r'\big]'
              +r'\big\}'
              +r'\cdot\big\{'
              +   r'{0:.4}'.format(X_ac)+ r'\,\mathrm{m}-'
              +r'\big['
                 +r'y \,\mathrm{tan} \,('+'{0:.4}'.format(Lambda_le)+r'\,\textrm{rad})\,+ \dfrac{1}{4}\big('
                    +   r'{0:.4}'.format(A_c) + r'\, y + ' + '{0:.4}'.format(B_c) + r'\,\mathrm{m}'
                 +r'\big)'
              +r'\big]'
              +r'\big\}\, \mathrm{d}y'      
            + r'\\[1em]' 
            + r'& {}= \frac{1}{'+ '{0:.4}'.format(S_ref) +r'\,\mathrm{m}^2'+r'{0:.4}'.format(c_mean)+r'\, \mathrm{m}} \big('
            +   c_mac_b_law_integral_indefinite_latex 
            + r'\big)\Bigr|_0^{' + '{0:.4}'.format(b/2) + r'}'
            + '=' + '{0:.4}'.format(c_mac_b) 
            
        + r'\end{align*}'
    )
    
#----------------------------------------------------------
def display_workflow_c_lalphabar(S,b,Acl_alpha,Bcl_alpha, A_c, B_c,c_lalphabar_law_integral_indefinite, c_lalphabar):
    c_lalphabar_law_integral_indefinite_latex = latex(c_lalphabar_law_integral_indefinite)
    return Latex(
        r'\begin{align*}'
            + r'\bar{C_\mathrm{\ell_\mathrm{\alpha}}} & {}=\frac{2}{S} \int_{0}^{b/2} c(y)C_\mathrm{\ell_\alpha}(y) \, \mathrm{d}y'
            + r'\\'
            + r'& {}= \frac{2}{S} \int_{0}^{b/2} \big(A_cy + B_c\big)\,\big(A_\mathrm{C_{\ell\alpha}}y + B_\mathrm{C_{\ell\alpha}}\big)\, \mathrm{d}y' 
            +r'\\'
            + r'& {}= \frac{2}{'+ '{0:.4}'.format(S) +r'\,\mathrm{m}^2'+r'}'+r' \int_0^{' + '{0:.3}'.format(b) + '}'
            + r'\Big(' 
           +   r'{0:.4}'.format(A_c) + r'\, y + ' + '{0:.4}'.format(B_c) + r'\,\mathrm{m}' 
            + r'\Big)' 
            + r'\Big(' 
            + r'{0:.4}'.format(Acl_alpha) + r'\,(\mathrm{rad}\,\mathrm{m})^{-1}'+ r'\, y + ' + '{0:.4}'.format(Bcl_alpha) + r'\,\mathrm{rad}^{-1} \big)'
            +r'\, \mathrm{d}y'      
            + r'\\[1em]' 
            + r'& {}= \frac{2}{'+ '{0:.4}'.format(S) +r'\,\mathrm{m}^2' +r'} \big('
            +  c_lalphabar_law_integral_indefinite_latex 
             + r'\big)\Bigr|_0^{' + '{0:.4}'.format(b/2) + r'}'
            + '=' + '{0:.4}'.format(c_lalphabar)   + r'\,\mathrm{rad}^{-1}'
            
        + r'\end{align*}'
    )
#----------------------------------------------------------
def retrieve_stored_data(file_name):
    store = shelve.open(file_name, flag='r')
    
    #print('-------- Database content, key => value --------')
    #for key in store:
    #    print(key, '=> {0}'.format(store[key]))
    
    #store.close()
    return store # not closed!

#----------------------------------------------------------
def data_summary(store):

    #print('-------- Database content, key => value --------')
    #for key in store:
    #    print(key, '=> {0}'.format(store[key]))

    c_r = store['c_r']
    c_k = store['c_k']
    c_t = store['c_t']
    span = store['b']
    inner_span = store['b_k']
    outer_span = span - inner_span
    S = store['S_ref']
    Lambda_le_1 = store['Lambda_le_1']
    Lambda_le_2 = store['Lambda_le_2']
    return Latex(

        r'\begin{array}{rl}'

        +  r'\text{root chord,}\, c_{\mathrm{r}}: & ' + r'{0}'.format(c_r) + r'\,\text{m}'

        +  r'\\'

        +  r'\text{kink chord,}\, c_{\mathrm{k}}: & ' + r'{0}'.format(c_k) + r'\,\text{m}'

        +  r'\\'

        +  r'\text{tip chord,}\, c_{\mathrm{t}}: & ' + r'{0}'.format(c_t) + r'\,\text{m}'

        +  r'\\'

        +  r'\text{semispan,}\, \frac{1}{2}b: & ' + r'{0}'.format(span/2) + r'\,\text{m}'
 
        +  r'\\'
 
        +  r'\text{semispan, inner panel}\, \frac{1}{2}b_{\mathrm{k}}: & ' + r'{0}'.format(inner_span/2) + r'\,\text{m}'

        +  r'\\'
        
        +  r'\text{semispan, outer panel}\, \frac{1}{2}b_{\mathrm{k}}: & ' + r'{0}'.format(outer_span/2) + r'\,\text{m}'

        +  r'\\'
     
        + r'\text{Reference Surface,}\, S: & ' + r'{0:.4}'.format(S) + r'\,\text{m}'
       
        + r'\\'
 
        +  r'\text{leading edge sweep, inner panel,}\, \Lambda_{\mathrm{le},1}: &' 

        +    r'{0}'.format(Lambda_le_1*180/math.pi) + r'\,\text{deg}'

        +  r'\\'

        +  r'\text{leading edge sweep, outer panel,}\, \Lambda_{\mathrm{le},2}: &' 

        +    r'{0}'.format(Lambda_le_2*180/math.pi) + r'\,\text{deg}'

        +  r'\\'

        

        +r'\end{array}'

    )   

#-------------------------------------Wing Functions ---------------------------------------------------------



# c(y)

def f_chord(y, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2):

    if y >= 0 and y <= b_k/2:

        A_ = 2*(c_k - c_r)/b_k; B_ = c_r;

        return A_*y + B_

    elif y > b_k/2 and y <= b/2:

        A_ = (c_t - c_k)/(b/2 - b_k/2); B_ = c_k;

        return A_*(y - b_k/2) + B_

    

def f_chord_1(y, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2):

    A_ = 2*(c_k - c_r)/b_k; B_ = c_r;

    return A_*y + B_



def f_chord_2(y, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2):

    A_ = (c_t - c_k)/(b/2 - b_k/2); B_ = c_k;

    return A_*(y - b_k/2) + B_



# X_le(y)

def f_Xle(y, b_k, b, Lambda_le_1, Lambda_le_2):

    if y >= 0 and y <= b_k/2:

        A_ = math.tan(Lambda_le_1);

        return A_*y

    elif y > b_k/2 and y <= b/2:

        A_ = math.tan(Lambda_le_2);

        return (b_k/2)*math.tan(Lambda_le_1) + A_*(y - b_k/2)

    

def f_Xle_1(y, b_k, b, Lambda_le_1, Lambda_le_2):

    A_ = math.tan(Lambda_le_1);

    return A_*y

    

def f_Xle_2(y, b_k, b, Lambda_le_1, Lambda_le_2):

    A_ = math.tan(Lambda_le_2);

    return (b_k/2)*math.tan(Lambda_le_1) + A_*(y - b_k/2)



# X_le(y) * c(y)

def f_Xle_c(y, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2):

    return f_Xle(y, b_k, b, Lambda_le_1, Lambda_le_2)*f_chord(y, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2)



# y * c(y)

def f_y_c(y, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2):

    return y*f_chord(y, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2)



# eps_g(y)

def f_twist(y, eps_k, eps_t, b_k, b):

    if y >= 0 and y <= b_k/2:

        A_ = 2*eps_k/b_k; B_ = 0;

        return A_*y + B_

    elif y > b_k/2 and y <= b/2:

        A_ = (eps_t - eps_k)/(b/2 - b_k/2); B_ = eps_k;

        return A_*(y - b_k/2) + B_

    

def f_twist_1(y, eps_k, eps_t, b_k, b):

    A_ = 2*eps_k/b_k; B_ = 0;

    return A_*y + B_



def f_twist_2(y, eps_k, eps_t, b_k, b):

    A_ = (eps_t - eps_k)/(b/2 - b_k/2); B_ = eps_k;

    return A_*(y - b_k/2) + B_



# alpha0l(y)

def f_alpha0l(y, alpha0l_r, alpha0l_k, alpha0l_t, b_k, b):

    if y >= 0 and y <= b_k/2:

        A_ = 2*(alpha0l_k - alpha0l_r)/b_k; B_ = alpha0l_r;

        return A_*y + B_

    elif y > b_k/2 and y <= b/2:

        A_ = (alpha0l_t - alpha0l_k)/(b/2 - b_k/2); B_ = alpha0l_k;

        return A_*(y - b_k/2) + B_

    

def f_alpha0l_1(y, alpha0l_r, alpha0l_k, alpha0l_t, b_k, b):

    A_ = 2*(alpha0l_k - alpha0l_r)/b_k; B_ = alpha0l_r;

    return A_*y + B_



def f_alpha0l_2(y, alpha0l_r, alpha0l_k, alpha0l_t, b_k, b):

    A_ = (alpha0l_t - alpha0l_k)/(b/2 - b_k/2); B_ = alpha0l_k;

    return A_*(y - b_k/2) + B_



# [ alpha_0l(y) - eps_g(y) ] * c(y)

def f_alpha0l_epsg_c(y, c_r, c_k, c_t, 

                     eps_k, eps_t,

                     alpha0l_r, alpha0l_k, alpha0l_t, 

                     b_k, b, Lambda_le_1, Lambda_le_2):

    return (f_alpha0l(y, alpha0l_r, alpha0l_k, alpha0l_t, b_k, b)

               - f_twist(y, eps_k, eps_t, b_k, b)

           )*f_chord(y, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2)
 #----------------------------------------------------------
def import_database_aerodynamic_center():
    fileName = "./resources/wing_aerodynamic_center.h5"
    f = h5py.File(fileName,'r',libver='latest')
    # K1
    data_K1 = f["(x_bar_ac_w)_k1_vs_lambda/data"]
    var0_K1 = f["(x_bar_ac_w)_k1_vs_lambda/var_0"]
    # K2
    data_K2 = f["(x_bar_ac_w)_k2_vs_L_LE_(AR)_(lambda)/data"]
    var0_K2 = f["(x_bar_ac_w)_k2_vs_L_LE_(AR)_(lambda)/var_0"]
    var1_K2 = f["(x_bar_ac_w)_k2_vs_L_LE_(AR)_(lambda)/var_1"]
    var2_K2 = f["(x_bar_ac_w)_k2_vs_L_LE_(AR)_(lambda)/var_2"]
    # xac/cr
    data_XacCr = f["(x_bar_ac_w)_x'_ac_over_root_chord_vs_tan_(L_LE)_over_beta_(AR_times_tan_(L_LE))_(lambda)/data"]
    var0_XacCr = f["(x_bar_ac_w)_x'_ac_over_root_chord_vs_tan_(L_LE)_over_beta_(AR_times_tan_(L_LE))_(lambda)/var_0"]
    var1_XacCr = f["(x_bar_ac_w)_x'_ac_over_root_chord_vs_tan_(L_LE)_over_beta_(AR_times_tan_(L_LE))_(lambda)/var_1"]
    var2_XacCr = f["(x_bar_ac_w)_x'_ac_over_root_chord_vs_tan_(L_LE)_over_beta_(AR_times_tan_(L_LE))_(lambda)/var_2"]
    return {
        'data_K1':data_K1, 
        'var0_K1':var0_K1, 
        'data_K2':data_K2, 
        'var0_K2':var0_K2, 
        'var1_K2':var1_K2, 
        'var2_K2':var2_K2,
        'data_XacCr':data_XacCr,
        'var0_XacCr':var0_XacCr,
        'var1_XacCr':var1_XacCr,
        'var2_XacCr':var2_XacCr
        }

#----------------------------------------------------------
def report_database_dimensions(database):
    shape_data_K1 = database['data_K1'].shape
    shape_var0_K1 = database['var0_K1'].shape
    
    shape_data_K2 = database['data_K2'].shape
    shape_var0_K2 = database['var0_K2'].shape
    shape_var1_K2 = database['var1_K2'].shape
    shape_var2_K2 = database['var2_K2'].shape
    
    shape_data_XacCr = database['data_XacCr'].shape
    shape_var0_XacCr = database['var0_XacCr'].shape
    shape_var1_XacCr = database['var1_XacCr'].shape
    shape_var2_XacCr = database['var2_XacCr'].shape
    
    print('(x_bar_ac_w)_k1_vs_lambda/var_0')
    print('shape of var0: {0}'.format(shape_var0_K1))
    print('(x_bar_ac_w)_k1_vs_lambda/data')
    print('shape of data: {0}'.format(shape_data_K1))
    """
    print('lambda --> K1')
    for i in range(shape_var0_K1[0]):
        print('{0}\t{1}'.format(dset_var0_K1[i],dset_data_K1[i]))
    """
    print('=====================================')
    print('(x_bar_ac_w)_k2_vs_L_LE_(AR)_(lambda)/var_0')
    print('shape of var0: {0}'.format(shape_var0_K2))
    print('(x_bar_ac_w)_k2_vs_L_LE_(AR)_(lambda)/var_1')
    print('shape of data: {0}'.format(shape_var1_K2))
    print('(x_bar_ac_w)_k2_vs_L_LE_(AR)_(lambda)/var_2')
    print('shape of data: {0}'.format(shape_var2_K2))
    print('(x_bar_ac_w)_k2_vs_L_LE_(AR)_(lambda)/data')
    print('shape of data: {0}'.format(shape_data_K2))
    print('=====================================')
    print("(x_bar_ac_w)_x'_ac_over_root_chord_vs_tan_(L_LE)_over_beta_(AR_times_tan_(L_LE))_(lambda)/var_0")
    print('shape of var0: {0}'.format(shape_var0_XacCr))
    print("(x_bar_ac_w)_x'_ac_over_root_chord_vs_tan_(L_LE)_over_beta_(AR_times_tan_(L_LE))_(lambda)/var_1")
    print('shape of data: {0}'.format(shape_var1_XacCr))
    print("(x_bar_ac_w)_x'_ac_over_root_chord_vs_tan_(L_LE)_over_beta_(AR_times_tan_(L_LE))_(lambda)/var_2")
    print('shape of data: {0}'.format(shape_var2_XacCr))
    print('(x_bar_ac_w)_k2_vs_L_LE_(AR)_(lambda)/data')
    print('shape of data: {0}'.format(shape_data_XacCr))

#----------------------------------------------------------
def plot_K1(var0_K1, data_K1):
    fig, ax = plt.subplots()
    plt.plot(var0_K1, data_K1, color="red", linewidth=2.5, linestyle="-")
    plt.title('Wing aerodynamic center --- effect of $\lambda$', fontsize=16)
    plt.xlabel('$\lambda$', fontsize=16)
    plt.ylabel('$K_1$', fontsize=16)
    plt.axis([0, 1, 0.8, 1.6])
    # Moving spines
    ax = plt.gca()  # gca stands for 'get current axis'
    ax.spines['right'].set_color('none')
    ax.spines['top'].set_color('none')
    ax.xaxis.set_ticks_position('bottom')
    ax.spines['bottom'].set_position(('data',0.78))
    ax.yaxis.set_ticks_position('left')
    ax.spines['left'].set_position(('data',-0.01))
    
    plt.show()

#----------------------------------------------------------
def plot_K2(var0_K2, var1_K2, var2_K2, data_K2, j_lambda=0):

    if j_lambda > 5 :
        print('Index j_lambda={0} out of range. Maximum allowed value 5'.format(j_lambda))
        return
    
    fig, ax = plt.subplots()
    
    #plt.gca().set_prop_cycle(
    #    cycler('color', ['c', 'm', 'y', 'k']) + cycler('lw', [1, 2, 3, 4]))
    
    idx_max_LambdaLE = 9
    
    for i_AR in range(0, 6):
        slice_ij = None
        slice_ij = data_K2[:,i_AR,j_lambda]
        line, = plt.plot(var2_K2, slice_ij, linewidth=2.5, linestyle="-")
        line.set_dashes([1000,1]) # HUUUUGE
        plt.annotate(r'$\mathrm{AR} = \,$'+r'{0}'.format(var1_K2[i_AR]),
                     xy=(var2_K2[idx_max_LambdaLE], slice_ij[idx_max_LambdaLE]), xycoords='data',
                     xytext=(40, 0), textcoords='offset points', fontsize=22,
                     arrowprops=dict(arrowstyle="->")) # , connectionstyle="arc3,rad=.5"
    plt.title(
        'Wing aerodynamic center --- effect of $(\Lambda_{\mathrm{le}},\mathrm{AR})$, '
        +r'$\lambda = {0:.3}$'.format(var0_K2[j_lambda]),
        fontsize=22)
    
    plt.axis([0, 45, 0, 1.1*max(data_K2[:,5,j_lambda])])
    
    # Moving spines
    ax = plt.gca()  # gca stands for 'get current axis'
    ax.spines['right'].set_color('none')
    ax.spines['top'].set_color('none')
    ax.xaxis.set_ticks_position('bottom')
    ax.spines['bottom'].set_position(('data',-0.05))
    ax.yaxis.set_ticks_position('left')
    ax.spines['left'].set_position(('data',-0.5))
    
    plt.xlabel('$\Lambda_{\mathrm{le}}$ (deg)', fontsize=22)
    plt.ylabel('$K_2$', fontsize=22)
    plt.show()

#----------------------------------------------------------
def multiplot_K2(var0_K2, var1_K2, var2_K2, data_K2):

    # j_lambda max = 5
    
    idx_max_LambdaLE = 9

    # fig, ax = plt.subplots()
    fig, axes = plt.subplots(3,2,figsize=(9, 11))
    
    j_lambda = 0
    for i, ax in enumerate(axes.flat, start=1):
        for i_AR in range(0, 6):
            slice_ij = None
            slice_ij = data_K2[:,i_AR,j_lambda]
            line, = ax.plot(var2_K2, slice_ij, linewidth=2.5, linestyle="-")
            line.set_dashes([1000,1]) # HUUUUGE
            ax.annotate(r'$\mathrm{AR} = \,$'+r'{0}'.format(var1_K2[i_AR]),
                         xy=(var2_K2[idx_max_LambdaLE], slice_ij[idx_max_LambdaLE]), xycoords='data',
                         xytext=(20, 0), textcoords='offset points', fontsize=12,
                         arrowprops=dict(arrowstyle="->")) # , connectionstyle="arc3,rad=.5"
        ax.set_title(
            #'Wing aerodynamic center --- effect of $(\Lambda_{\mathrm{le}},\mathrm{AR})$, '
            r'$\lambda = {0:.3}$'.format(var0_K2[j_lambda]),
            fontsize=12)
        
        ymajorLocator = MultipleLocator(0.5)
        ymajorFormatter = FormatStrFormatter('%.1f')
        yminorLocator = MultipleLocator(5)
        ax.yaxis.set_major_locator(ymajorLocator)
        ax.yaxis.set_major_formatter(ymajorFormatter)
        # for the minor ticks, use no labels; default NullFormatter
        ax.yaxis.set_minor_locator(yminorLocator)

        ax.axis([0, 45, 0, 1.1*max(data_K2[:,5,j_lambda])])
        
        # Moving spines
        #ax = plt.gca()  # gca stands for 'get current axis'
        ax.spines['right'].set_color('none')
        ax.spines['top'].set_color('none')
        ax.xaxis.set_ticks_position('bottom')
        ax.spines['bottom'].set_position(('data',-0.05))
        ax.yaxis.set_ticks_position('left')
        ax.spines['left'].set_position(('data',-0.5))
        
        ax.set_xlabel('$\Lambda_{\mathrm{le}}$ (deg)', fontsize=12)
        ax.set_ylabel('$K_2$', fontsize=12)
        #ax[0,0].show()
        j_lambda += 1 

    plt.tight_layout(pad=0.4, w_pad=0.5, h_pad=1.0)
    plt.subplots_adjust(wspace=0.78)
    plt.show()
    

#----------------------------------------------------------
def plot_XacCr(var0_XacCr, var1_XacCr, var2_XacCr, data_XacCr, j_lambda=0):
    
    if j_lambda > 5 :
        print('Index j_lambda={0} out of range. Maximum allowed value 5'.format(j_lambda))
        return

    fig, ax = plt.subplots()
    
    #plt.gca().set_prop_cycle(
    #    cycler('color', ['c', 'm', 'y', 'k']) + cycler('lw', [1, 2, 3, 4]))
    
    idx_max_TanLambdaLE = 10
    
    for i_AR in range(0, 7):
        slice_ij = None
        slice_ij = data_XacCr[:,i_AR,j_lambda]
        line, = plt.plot(var2_XacCr, slice_ij, linewidth=2.5, linestyle="-")
        line.set_dashes([1000,1]) # HUUUUGE
        plt.annotate(r'$\mathrm{AR} \tan\Lambda_{\mathrm{le}} =\,$'+r'{0}'.format(var1_XacCr[i_AR,0]),
                     xy=(var2_XacCr[idx_max_TanLambdaLE], slice_ij[idx_max_TanLambdaLE]), xycoords='data',
                     xytext=(40, 0), textcoords='offset points', fontsize=22,
                     arrowprops=dict(arrowstyle="->")) # , connectionstyle="arc3,rad=.5"
    plt.title(
        r'Wing aerodynamic center --- effect of $(\tan\Lambda_{\mathrm{le}}/\sqrt{1-M^2},\mathrm{AR}\tan\Lambda_{\mathrm{le}})$, '
        +'$\lambda = {0:.3}$'.format(var0_XacCr[j_lambda]),
        fontsize=22)
    
    plt.axis([0, 2.2, -0.05, 1.1*max(data_XacCr[:,6,j_lambda])])
    
    # Moving spines
    ax = plt.gca()  # gca stands for 'get current axis'
    ax.spines['right'].set_color('none')
    ax.spines['top'].set_color('none')
    ax.xaxis.set_ticks_position('bottom')
    ax.spines['bottom'].set_position(('data',-0.07))
    ax.yaxis.set_ticks_position('left')
    ax.spines['left'].set_position(('data',-0.05))
    
    plt.xlabel(r'$\tan\Lambda_{\mathrm{le}}/\sqrt{1-M^2}$', fontsize=22)
    plt.ylabel('$X_{\mathrm{ac}}\'/c_{\mathrm{r}}$', fontsize=22)
    plt.show()

#----------------------------------------------------------
def multiplot_XacCr(var0_XacCr, var1_XacCr, var2_XacCr, data_XacCr):

    # j_lambda max = 5
    
    idx_max_TanLambdaLE = 10

    # fig, ax = plt.subplots()
    fig, axes = plt.subplots(3,2,figsize=(9, 11))
    
    j_lambda = 0
    for i, ax in enumerate(axes.flat, start=1):

        for i_AR in range(0, 7):
            slice_ij = None
            slice_ij = data_XacCr[:,i_AR,j_lambda]
            line, = ax.plot(var2_XacCr, slice_ij, linewidth=2.5, linestyle="-")
            line.set_dashes([1000,1]) # HUUUUGE
            ax.annotate(r'$\mathrm{AR}\tan\Lambda_{\mathrm{le}} = \,$'+r'{0}'.format(var1_XacCr[i_AR,0]),
                         xy=(var2_XacCr[idx_max_TanLambdaLE], slice_ij[idx_max_TanLambdaLE]), xycoords='data',
                         xytext=(20, 0), textcoords='offset points', fontsize=12,
                         arrowprops=dict(arrowstyle="->")) # , connectionstyle="arc3,rad=.5"
        ax.set_title(
            #'Wing aerodynamic center --- effect of $(\Lambda_{\mathrm{le}},\mathrm{AR})$, '
            r'$\lambda = {0:.3}$'.format(var0_XacCr[j_lambda]),
            fontsize=12)

        ymajorLocator = MultipleLocator(0.25)
        ymajorFormatter = FormatStrFormatter('%.1f')
        yminorLocator = MultipleLocator(1)
        ax.yaxis.set_major_locator(ymajorLocator)
        ax.yaxis.set_major_formatter(ymajorFormatter)
        # for the minor ticks, use no labels; default NullFormatter
        ax.yaxis.set_minor_locator(yminorLocator)

        ax.axis([0, 2, -0.05, 1.1*max(data_XacCr[:,6,j_lambda])])
        
        # Moving spines
        #ax = plt.gca()  # gca stands for 'get current axis'
        ax.spines['right'].set_color('none')
        ax.spines['top'].set_color('none')
        ax.xaxis.set_ticks_position('bottom')
        ax.spines['bottom'].set_position(('data',-0.07))
        ax.yaxis.set_ticks_position('left')
        ax.spines['left'].set_position(('data',-0.05))
        
        ax.set_xlabel(r'$\tan\Lambda_{\mathrm{le}}/\sqrt{1-M^2}$', fontsize=12)
        ax.set_ylabel('$X_{\mathrm{ac}}\'/c_{\mathrm{r}}$', fontsize=12)

        #ax[0,0].show()
        j_lambda += 1 

    plt.tight_layout(pad=0.4, w_pad=0.5, h_pad=1.0)
    plt.subplots_adjust(wspace=0.85)
    plt.show()
    


#----------------------------------------------------------
def plot_interpolate_K1(var0_K1, data_K1, lam):
    g = interp1d(var0_K1, data_K1)
    K1 = g(lam)
    print('lambda = {0} --> K_1 = {1}'.format(lam,K1))
    fig, ax = plt.subplots()
    plt.plot(var0_K1, data_K1, color="brown", linewidth=2.5, linestyle="-")

    # interpolated data
    plt.scatter(lam, K1, marker='o', s=40)
    help_line, = plt.plot([lam,lam,0],[0,K1,K1], color="red", linewidth=1.5, linestyle="--")
    help_line.set_dashes([8, 4]) 
    
    plt.title('Wing aerodynamic center --- effect of $\lambda$', fontsize=22)
    plt.xlabel('$\lambda$', fontsize=16)
    plt.ylabel('$K_1$', fontsize=16)
    plt.axis([0, 1, 0.8, 1.6])
    # Moving spines
    ax = plt.gca()  # gca stands for 'get current axis'
    ax.spines['right'].set_color('none')
    ax.spines['top'].set_color('none')
    ax.xaxis.set_ticks_position('bottom')
    ax.spines['bottom'].set_position(('data',0.78))
    ax.yaxis.set_ticks_position('left')
    ax.spines['left'].set_position(('data',-0.01))
    
    plt.show()

#----------------------------------------------------------
def plot_interpolate_K2(var0_K2, var1_K2, var2_K2, data_K2, j_lambda, 
                        LamLE_deg, AR):

    if j_lambda > 5 :
        print('Index j_lambda={0} out of range. Maximum allowed value 5'.format(j_lambda))
        return

    # interpolation in 2D
    g = interp2d(var1_K2, var2_K2, data_K2[:,:,j_lambda], kind='linear')
    K2 = g(AR, LamLE_deg)
    print('Lambda_LE = {0} deg, AR = {1} --> K_2 = {2}'.format(LamLE_deg, AR, K2[0]))
    
    fig, ax = plt.subplots()
    
    #plt.gca().set_prop_cycle(
    #    cycler('color', ['c', 'm', 'y', 'k']) + cycler('lw', [1, 2, 3, 4]))
    
    idx_max_LambdaLE = 9
    
    for i_AR in range(0, 6):
        slice_ij = None
        slice_ij = data_K2[:,i_AR,j_lambda]
        line, = plt.plot(var2_K2, slice_ij, linewidth=2.5, linestyle="-")
        line.set_dashes([1000,1]) # HUUUUGE
        plt.annotate(r'$\mathrm{AR} = \,$'+r'{0}'.format(var1_K2[i_AR]),
                     xy=(var2_K2[idx_max_LambdaLE], slice_ij[idx_max_LambdaLE]), xycoords='data',
                     xytext=(40, 0), textcoords='offset points', fontsize=16,
                     arrowprops=dict(arrowstyle="->")) # , connectionstyle="arc3,rad=.5"

    # interpolated data
    plt.scatter(LamLE_deg, K2, marker='o', s=40)
    help_line, = plt.plot([LamLE_deg,LamLE_deg,0],[0,K2,K2], color="red", linewidth=1.5, linestyle="--")
    help_line.set_dashes([8, 4]) 
    
    plt.title(
        'Wing aerodynamic center --- effect of $(\Lambda_{\mathrm{le}},\mathrm{AR})$, '
        +r'$\lambda = {0:.3}$'.format(var0_K2[j_lambda]),
        fontsize=16)
    
    plt.axis([0, 45, 0, 1.1*max(data_K2[:,5,j_lambda])])
    
    # Moving spines
    ax = plt.gca()  # gca stands for 'get current axis'
    ax.spines['right'].set_color('none')
    ax.spines['top'].set_color('none')
    ax.xaxis.set_ticks_position('bottom')
    ax.spines['bottom'].set_position(('data',-0.03))
    ax.yaxis.set_ticks_position('left')
    ax.spines['left'].set_position(('data',-0.5))
    
    plt.xlabel('$\Lambda_{\mathrm{le}}$ (deg)', fontsize=16)
    plt.ylabel('$K_2$', fontsize=16)
    plt.show()

#----------------------------------------------------------
def plot_interpolate_XacCr(var0_XacCr, var1_XacCr, var2_XacCr, data_XacCr, j_lambda, 
                           LamLE_deg, AR, Mach):
    
    if j_lambda > 5 :
        print('Index j_lambda={0} out of range. Maximum allowed value 5'.format(j_lambda))
        return

    # interpolation in 2D
    g = interp2d(var1_XacCr, var2_XacCr, data_XacCr[:,:,j_lambda], kind='linear')
    y_ = AR*math.tan(LamLE_deg*np.pi/180)
    x_ = math.tan(LamLE_deg*np.pi/180)/math.sqrt(1 - math.pow(Mach,2))
    XacCr = g(y_,x_)
    print('x_1 = tan(Lambda_LE)/sqrt(1-M^2) = {0}\nx_2 = AR*tan(Lambda_LE) = {1}\n --> Xac\'/c_r = {2}'
        .format(x_, y_, XacCr[0]))

    fig, ax = plt.subplots()
    
    #plt.gca().set_prop_cycle(
    #    cycler('color', ['c', 'm', 'y', 'k']) + cycler('lw', [1, 2, 3, 4]))
    
    idx_max_TanLambdaLE = 10
    
    for i_AR in range(0, 7):
        slice_ij = None
        slice_ij = data_XacCr[:,i_AR,j_lambda]
        line, = plt.plot(var2_XacCr, slice_ij, linewidth=2.5, linestyle="-")
        line.set_dashes([1000,1]) # HUUUUGE
        plt.annotate(r'$\mathrm{AR} \tan\Lambda_{\mathrm{le}} =\,$'+r'{0}'.format(var1_XacCr[i_AR,0]),
                     xy=(var2_XacCr[idx_max_TanLambdaLE], slice_ij[idx_max_TanLambdaLE]), xycoords='data',
                     xytext=(40, 0), textcoords='offset points', fontsize=16,
                     arrowprops=dict(arrowstyle="->")) # , connectionstyle="arc3,rad=.5"

    # interpolated data
    plt.scatter(x_, XacCr, marker='o', s=40)
    help_line, = plt.plot([x_,x_,0],[0,XacCr,XacCr], color="red", linewidth=1.5, linestyle="-")
    help_line.set_dashes([8, 4]) 
    
    plt.title(
        r'Wing aerodynamic center --- effect of $(\tan\Lambda_{\mathrm{le}}/\sqrt{1-M^2},\mathrm{AR}\tan\Lambda_{\mathrm{le}})$, '
        +'$\lambda = {0:.3}$'.format(var0_XacCr[j_lambda]),
        fontsize=16)
    
    plt.axis([0, 2.2, -0.05, 1.1*max(data_XacCr[:,6,j_lambda])])
    
    # Moving spines
    ax = plt.gca()  # gca stands for 'get current axis'
    ax.spines['right'].set_color('none')
    ax.spines['top'].set_color('none')
    ax.xaxis.set_ticks_position('bottom')
    ax.spines['bottom'].set_position(('data',-0.07))
    ax.yaxis.set_ticks_position('left')
    ax.spines['left'].set_position(('data',-0.05))
    
    plt.xlabel(r'$\tan\Lambda_{\mathrm{le}}/\sqrt{1-M^2}$', fontsize=16)
    plt.ylabel('$X_{\mathrm{ac}}\'/c_{\mathrm{r}}$', fontsize=16)
    plt.show()

#----------------------------------------------------------
def display_workflow_c_mac(S_ref, b, A_c, B_c, c_mac_law_integral_indefinite, mac):
    c_mac_law_integral_indefinite_latex = latex(c_mac_law_integral_indefinite)
    return Latex(
        r'\begin{align*}'
            + r'\bar{c} & {}=\frac{2}{S} \int_{0}^{b/2} c^2(y) \, \mathrm{d}y'
            + r'\\[1em]' 
            + r'& {}= \frac{2}{'+ '{0:.4}'.format(S_ref) +r'\,\mathrm{m}^2} \int_0^{' + '{0:.3}'.format(b/2) + '}' 
            + r'\Big(' 
            +   r'{0:.4}'.format(A_c) + r'\, y + ' + '{0:.4}'.format(B_c) + r'\,\text{m}'
            + r'\Big)^2\,\text{d}y' 
            + r'\\[1em]' 
            + r'& {}= \frac{2}{'+ '{0:.4}'.format(S_ref) +r'} \big(' 
            +   c_mac_law_integral_indefinite_latex 
            + r'\big)\Bigr|_0^{' + '{0:.4}'.format(b/2) + r'}'
            + '=' + '{0:.4}'.format(mac) + r'\,\text{m}'
        + r'\end{align*}'
    )

#----------------------------------------------------------
def display_workflow_X_le_mac(S_ref, b, A_c, B_c, A_xle, X_le_mac_law_integral_indefinite, X_le_mac):
    X_le_mac_law_integral_indefinite_latex = latex(X_le_mac_law_integral_indefinite)
    return Latex(
        r'\begin{align*}'
            + r'X_{\mathrm{le},\bar{c}} & {}=\frac{2}{S} \int_{0}^{b/2} X_{\mathrm{le}}(y) \, c(y) \, \mathrm{d}y'
            + r'\\[1em]' 
            + r'& {}= \frac{2}{'+ '{0:.4}'.format(S_ref) +r'\,\mathrm{m}^2} \int_0^{' + '{0:.3}'.format(b/2) + '}' 
            + r'{0:.4}'.format(A_xle) + r'\, y\,'            
            + r'\Big(' 
            +   r'{0:.4}'.format(A_c) + r'\, y + ' + '{0:.4}'.format(B_c) + r'\,\text{m}'
            + r'\Big)\,\text{d}y' 
            + r'\\[1em]' 
            + r'& {}= \frac{2}{'+ '{0:.4}'.format(S_ref) +r'} \big(' 
            +   X_le_mac_law_integral_indefinite_latex 
            + r'\big)\Bigr|_0^{' + '{0:.4}'.format(b/2) + r'}'
            + '=' + '{0:.4}'.format(X_le_mac) + r'\,\text{m}'
        + r'\end{align*}'
    )

#----------------------------------------------------------
def display_workflow_Y_mac(S_ref, b, A_c, B_c, Y_mac_law_integral_indefinite, Y_mac):
    Y_mac_law_integral_indefinite_latex = latex(Y_mac_law_integral_indefinite)
    return Latex(
        r'\begin{align*}'
            + r'Y_{\bar{c}} & {}=\frac{2}{S} \int_{0}^{b/2} y \, c(y) \, \mathrm{d}y'
            + r'\\[1em]' 
            + r'& {}= \frac{2}{'+ '{0:.4}'.format(S_ref) +r'\,\mathrm{m}^2} \int_0^{' + '{0:.3}'.format(b/2) + '}' 
            + r' y\,'            
            + r'\Big(' 
            +   r'{0:.4}'.format(A_c) + r'\, y + ' + '{0:.4}'.format(B_c) + r'\,\text{m}'
            + r'\Big)\,\text{d}y' 
            + r'\\[1em]' 
            + r'& {}= \frac{2}{'+ '{0:.4}'.format(S_ref) +r'} \big(' 
            +   Y_mac_law_integral_indefinite_latex 
            + r'\big)\Bigr|_0^{' + '{0:.4}'.format(b/2) + r'}'
            + '=' + '{0:.4}'.format(Y_mac) + r'\,\text{m}'
        + r'\end{align*}'
    )

#----------------------------------------------------------
def display_workflow_K2(lam_a, lam_b, K2_a, K2_b, taper_ratio, K2):
    return Latex(
        r'\begin{equation}'
        +  r'K_2 = K_2 \Big|_{\lambda=' + '{0:.3}'.format(lam_a) + r'}'
        +    r'+ \frac{'
        +      r'K_2 \Big|_{\lambda=' + '{0:.3}'.format(lam_b) + r'}'
        +      r'- K_2 \Big|_{\lambda=' + '{0:.3}'.format(lam_a) + r'}'
        +    r'}{'
        +      r'{0:.3}'.format(lam_b) + r'-{0:.3}'.format(lam_a)
        +    r'}'
        +    r'\,\big( {0:.3} - {1:.3} \big)'.format(taper_ratio,lam_a)
        +    r'= {0:.3}'.format(K2_a)
        +    r'+ \frac{'
        +      r'{0:.3}'.format(K2_b)
        +      r'- {0:.3}'.format(K2_a)
        +    r'}{'
        +      r'{0:.3}'.format(lam_b) + r'-{0:.3}'.format(lam_a)
        +    r'}'
        +    r'\,\big( {0:.3} - {1:.3} \big)'.format(taper_ratio,lam_a)
        +    r'=' + '{0:.3}'.format(K2)
        +r'\end{equation}'
    )

#----------------------------------------------------------
def display_workflow_XacCr(lam_a, lam_b, XacCr_a, XacCr_b, taper_ratio, XacCr):
    return Latex(
        r'\begin{equation}'
        +  r'\frac{X_{\mathrm{ac}}'+"'"+r'}{c_{\mathrm{r}}} = \frac{X_{\mathrm{ac}}'+"'"+r'}{c_{\mathrm{r}}} \Big|_{\lambda=' + '{0:.3}'.format(lam_a) + r'}'
        +    r'+ \frac{'
        +      r'\dfrac{X_{\mathrm{ac}}'+"'"+r'}{c_{\mathrm{r}}} \Big|_{\lambda=' + '{0:.3}'.format(lam_b) + r'}'
        +      r'- \dfrac{X_{\mathrm{ac}}'+"'"+r'}{c_{\mathrm{r}}} \Big|_{\lambda=' + '{0:.3}'.format(lam_a) + r'}'
        +    r'}{'
        +      r'{0:.3}'.format(lam_b) + r'-{0:.3}'.format(lam_a)
        +    r'}'
        +    r'\,\big( {0:.3} - {1:.3} \big)'.format(taper_ratio,lam_a)
        +    r'= {0:.3}'.format(XacCr_a)
        +    r'+ \frac{'
        +      r'{0:.3}'.format(XacCr_b)
        +      r'- {0:.3}'.format(XacCr_a)
        +    r'}{'
        +      r'{0:.3}'.format(lam_b) + r'-{0:.3}'.format(lam_a)
        +    r'}'
        +    r'\,\big( {0:.3} - {1:.3} \big)'.format(taper_ratio,lam_a)
        +    r'=' + '{0:.3}'.format(XacCr)
        +r'\end{equation}'
    )
#----------------------------------------------------------
def    display_parameters_list(c_r,c_k,c_t,b1,b2,b,eps_r,eps_k,eps_t,Lambda_le,Lambda_le_1,Lambda_le_2,Lambda_te,Lambda_te_1,Lambda_te_2,cl_alpha_root,cl_alpha_tip,cl_alpha_r,cl_alpha_k,cl_alpha_t,alpha0l_r,alpha0l_r_1,alpha0l_t,alpha0l_k,alpha0l_t_2,Cmac_r,Cmac_t,AR,M):
     return Latex(r'\begin{array}{ll}'
       +  r'\text{Root chord,}\, c_{\mathrm{r}}: & ' + r'{0}'.format(c_r) + r'\,\text{m}'
       +  r'\\'
       +  r'\text{Kink chord,}\, c_{\mathrm{k}}: & ' + r'{0}'.format(c_k) + r'\,\text{m}'
       +  r'\\'
       +  r'\text{Tip chord,}\, c_{\mathrm{t}}: & ' + r'{0}'.format(c_t) + r'\,\text{m}'
       +  r'\\'
       +  r'\text{Semispan, inner panel}\, \frac{1}{2}b_{\mathrm{k}}: & ' + r'{0}'.format(b1/2) + r'\,\text{m}'
       +  r'\\'
       +  r'\text{Semispan, outer panel}\, \frac{1}{2}b_{\mathrm{k,2}}: & ' + r'{0}'.format(b2/2) + r'\,\text{m}'
       +  r'\\'
       +  r'\text{Semispan,}\, \frac{1}{2}b: & ' + r'{0}'.format(b/2) + r'\,\text{m}'
       +  r'\\'
       +  r'\text{Root section geometric twist,}\, \epsilon_{\mathrm{g,r}}: & ' 
       +     r'{0:.4}'.format(eps_r*180/math.pi) + r'\,\text{deg}'
       +  r'\\'
       +  r'\text{kink section geometric twist,}\, \epsilon_{\mathrm{g,k}}: & ' 
       +     r'{0:.4}'.format(eps_k*180/math.pi) + r'\,\text{deg}'
       +  r'\\'
       +  r'\text{tip section geometric twist,}\, \epsilon_{\mathrm{g,t}}: & ' 
       +     r'{0:.4}'.format(eps_t*180/math.pi) + r'\,\text{deg}'
       +  r'\\'
       +  r'\text{Leading edge sweep angle,}\, \Lambda_{\mathrm{le},1}: &' 
       +     r'{0}'.format(Lambda_le*180/math.pi) + r'\,\text{deg}'
       +  r'\text{Leading edge sweep, inner panel,}\, \Lambda_{\mathrm{le},1}: &' 
       +     r'{0:.4}'.format(Lambda_le_1*180/math.pi) + r'\,\text{deg}'
       +  r'\\'
       +  r'\text{Leading edge sweep, outer panel,}\, \Lambda_{\mathrm{le},2}: &' 
       +     r'{0:.4}'.format(Lambda_le_2*180/math.pi) + r'\,\text{deg}'
       +  r'\\'
       +  r'\text{Trailing edge sweep angle,}\, \Lambda_{\mathrm{le},1}: &' 
       +     r'{0}'.format(Lambda_te*180/math.pi) + r'\,\text{deg}'
       +  r'\text{Traling edge sweep, inner panel,}\, \Lambda_{\mathrm{te},1}: &' 
       +     r'{0:.4}'.format(Lambda_te_1*180/math.pi) + r'\,\text{deg}'
       +  r'\\'
       +  r'\text{Traling edge sweep, outer panel,}\, \Lambda_{\mathrm{te},2}: &' 
       +     r'{0:.4}'.format(Lambda_te_2*180/math.pi) + r'\,\text{deg}'
       +  r'\\'
       +  r'\text{Root profile lift gradient,}\, C_{\ell\alpha,\text{r}}: & ' 
       +     r'{0}'.format(cl_alpha_root) + r'\,\text{rad}^{-1}'
       +  r'\\'
       +  r'\text{Tip profile lift Gradient,}\, bar{C}_{\ell\alpha,\text{t}}: & ' 
       +     r'{0}'.format(cl_alpha_tip) + r'\,\text{rad}^{-1}'
       +  r'\\'
       +  r'\text{Root profile lift gradient, inner panel,}\, C_\mathrm{\ell_{\alpha},r,1}: &' 
       +     r'{0:.4}'.format(cl_alpha_r) + r'\,\text{rad}^{-1}'
       +  r'\\'
       +  r'\text{Tip profile lift gradient, inner panel,}\, C_\mathrm{\ell_{\alpha},t,1}: &' 
       +     r'{0:.4}'.format(cl_alpha_k) + r'\,\text{rad}^{-1}'
       +  r'\\'
       +  r'\text{Tip profile lift gradient, outer panel,}\, C_\mathrm{\ell_{\alpha},t,1}: &' 
       +     r'{0:.4}'.format(cl_alpha_t) + r'\,\text{rad}^{-1}'
       +  r'\\'
       +  r'\text{Root zero lift angle of attack,} \, \alpha_{0\ell,r}: &' 
       +     r'{0}'.format(alpha0l_r*180/math.pi) + r'\,\text{deg}'
       +  r'\\'
       +  r'\text{Root zero lift angle of attack,inner panel}\, \alpha_\mathrm{0\ell,r,1}: &' 
       +     r'{0:.4}'.format(alpha0l_r_1) + r'\,\text{deg}'
       +  r'\\'
       +  r'\text{Tip zero lift angle of attack,} \, \alpha_{0\ell,t}: &' 
       +     r'{0}'.format(alpha0l_t*180/math.pi) + r'\,\text{deg}'
       +  r'\\'
       +  r'\text{Tip zero lift angle of attack,inner panel}\, \alpha_\mathrm{0\ell,t,1}: &' 
       +     r'{0:.4}'.format(alpha0l_k) + r'\,\text{deg}'
       +  r'\\'
       +  r'\text{Tip zero lift angle of attack,outer panel}\, \alpha_\mathrm{0\ell,t,2}: &' 
       +     r'{0}'.format(alpha0l_t_2) + r'\,\text{deg}'
       +  r'\text{Root aerodynamic center moment coefficient,} \, C_{m_{ac},r}: &' 
       +     r'{0}'.format(Cmac_r) 
       +  r'\\'
       +  r'\text{Tip aerodynamic center moment coefficient,} \, C_{m_{ac},t}: &' 
       +     r'{0}'.format(Cmac_t) 
       +  r'\\'
       +  r'\text{Aspect Ratio,}\, AR: & ' + r'{0:.4}'.format(AR) 
       +  r'\\'
       +  r'\text{Flight condition,} \, M: &' 
       +     r'{0}'.format(M)
       +  r'\end{array}'
                  
                  
                  
                  
     )
#----------------------------------------------------------
def f_chord(y, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2):
    if y >= 0 and y <= b_k/2:
        A_ = 2*(c_k - c_r)/b_k; B_ = c_r;
        
        return A_*y + B_
    elif y > b_k/2 and y <= b/2:
        A_ = (c_t - c_k)/(b/2 - b_k/2); B_ = c_k;
        
        return A_*(y - b_k/2) + B_
    
#------------------------------------------    
def f_chord_1(y, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2):
    A_ = 2*(c_k - c_r)/b_k; B_ = c_r;
    
    return A_*y + B_
#---------------------------------------------------------
def f_chord_2(y, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2):
    A_ = (c_t - c_k)/(b/2 - b_k/2); B_ = c_k;
   
    return A_*(y - b_k/2) + B_   
#---------------------------------------------------------
def f_alpha0l(y, alpha0l_r, alpha0l_k, alpha0l_t, b_k, b):
    if y >= 0 and y <= b_k/2:
        A_ = 2*(alpha0l_k - alpha0l_r)/b_k; B_ = alpha0l_r;
        return A_*y + B_
    elif y > b_k/2 and y <= b/2:
        A_ = (alpha0l_t - alpha0l_k)/(b/2 - b_k/2); B_ = alpha0l_k;
        return A_*(y - b_k/2) + B_
#---------------------------------------------------------
def f_alpha0l_1(y, alpha0l_r, alpha0l_k, alpha0l_t, b_k, b):
    A_ = 2*(alpha0l_k - alpha0l_r)/b_k; B_ = alpha0l_r;
    return A_*y + B_
#--------------------------------------------------------
def f_alpha0l_2(y, alpha0l_r, alpha0l_k, alpha0l_t, b_k, b):
    A_ = (alpha0l_t - alpha0l_k)/(b/2 - b_k/2); B_ = alpha0l_k;
    return A_*(y - b_k/2) + B_
#--------------------------------------------------------
def f_twist(y, eps_k, eps_t, b_k, b):
    if y >= 0 and y <= b_k/2:
        A_ = 2*eps_k/b_k; B_ = 0;
        return A_*y + B_
    elif y > b_k/2 and y <= b/2:
        A_ = (eps_t - eps_k)/(b/2 - b_k/2); B_ = eps_k;
        return A_*(y - b_k/2) + B_
#------------------------------------------------------------    
def f_twist_1(y, eps_k, eps_t, b_k, b):
    A_ = 2*eps_k/b_k; B_ = 0;
    return A_*y + B_
#--------------------------------------------------------------
def f_twist_2(y, eps_k, eps_t, b_k, b):
    A_ = (eps_t - eps_k)/(b/2 - b_k/2); B_ = eps_k;
    return A_*(y - b_k/2) + B_
#-------------------------------------------------------------
def f_alpha0l_epsg_c(y, c_r, c_k, c_t, 
                     eps_k, eps_t,
                     alpha0l_r, alpha0l_k, alpha0l_t, 
                     b_k, b, Lambda_le_1, Lambda_le_2):
    return (f_alpha0l(y, alpha0l_r, alpha0l_k, alpha0l_t, b_k, b)
               - f_twist(y, eps_k, eps_t, b_k, b)
           )*f_chord(y, c_r, c_k, c_t, b_k, b, Lambda_le_1, Lambda_le_2)
#------------------------------------------------------------
def integrate_S(A_c,B_c,b):
    # symbolic variable
    y = Symbol('y')
    # symbolic integral
    c_law_integral_indefinite = integrate(A_c*y + B_c,y)
    # definite integral
    c_law_integral_definite= 2*integrate(A_c*y + B_c,(y,0,b/2))
    S=c_law_integral_definite
    return S,c_law_integral_indefinite
#----------------------------------------------------------
def equivalent_wing(c_r,c_k,c_t,b,b_k,b_k2,Lambda_le_1,Lambda_le_2 ) :
    #symbolic variable
    x = sympy.Symbol('x')
    # Equal areas
    xB = (b_k/2)*math.tan(Lambda_le_1)
    # print(xB)
    xC = xB + (b_k2/2)*math.tan(Lambda_le_2)
    # print(xC)
    xA1 = sympy.solve(xB*(b_k/2)/2 + (xC - xB)*(b_k/2) + (b/2 - b_k/2)*(xC - xB)/2 # A-B-C-P1
                  - (xC - x)*(b/2)/2, # A1-CP-1
                  x)#These are the plots of varius functions of spanwise variable .
    #print('x_A1 = {0:.4} m'.format(xA1[0]))
    xAp = c_r
    #print('x_Ap = {0:.4} m'.format(xAp))
    xBp = xB + c_k
    #print('x_Bp = {0:.4} m'.format(xBp))
    xCp = xC + c_t
    #print('x_Cp = {0:.4} m'.format(xCp))
    xA2 = sympy.solve(
    (xAp - xBp)*(b_k/2)/2 
    + (xCp - xAp)*(b_k/2) + (b_k2/2)*(xCp - xBp)/2 # A'-B'-C'-P2
    - (xCp - x)*(b/2)/2, # A2-C'-P2
    x)
    x_A1= xA1[0]
    x_A2= xA2[0]
    #print('x_A2 = {0:.4} m'.format(xA2[0]))
    X_le_r_eq = xA1[0]
    #print('X_le_r_eq = {0:.4} m'.format(X_le_r_eq))
    c_r_eq = xA2[0] - xA1[0]
    #print('c_r_eq = {0:.4} m'.format(c_r_eq))
    Lambda_le_eq = math.atan((xC - X_le_r_eq)/(b/2))
    #print('Lambda_le_eq = {0:.4} deg'.format(Lambda_le_eq*180/math.pi))
    taper_eq=c_t/c_r_eq
    return xA1[0],xA2[0],c_r_eq,Lambda_le_eq,taper_eq,X_le_r_eq
#-----------------------------------------------------------
def integrate_alfa_0l(A_alpha,B_alpha,A_eps,A_c,B_c,b,S_ref):
    # symbolic variable
    y = Symbol('y')
    #  symbolic integral
    alpha0L_law_integral_indefinite = integrate((A_alpha*y + B_alpha - A_eps*y)*(A_c*y +    B_c),y)
    alpha0L = (2/S_ref)*integrate((A_alpha*y + B_alpha - A_eps*y)*(A_c*y + B_c),(y,0,b/2))
    return alpha0L,alpha0L_law_integral_indefinite
#-----------------------------------------------------------
def integrate_alfa_0l_mean(A_alpha,B_alpha,A_c,B_c,b,S_ref):
    y = Symbol('y')
    alpha0L_law_integral_indefinite = integrate((A_alpha*y + B_alpha)*(A_c*y + B_c),y)
    alpha0L = (2/S_ref)*integrate((A_alpha*y + B_alpha)*(A_c*y + B_c),(y,0,b/2))
    return alpha0L,alpha0L_law_integral_indefinite
#-------------------------------------------------------
def integrate_alfa_0l_cw_outer(A_0l_2,B_0l_2,A_eps2,B_eps2,A_2,B_2,b_k,b,S):
    # symbolic variable
    y = Symbol('y')
    # symbolic integral
    alpha0L_law_integral_indefinite = integrate((A_0l_2*(y-b_k/2) + B_0l_2 - A_eps2*(y-b_k/2)-B_eps2)*(A_2*(y-b_k/2) + B_2),y)
    alpha0L_2 = (2/S)*integrate(((A_0l_2*(y-b_k/2)) + B_0l_2 - A_eps2*(y-b_k/2)-B_eps2)*(A_2*(y-b_k/2) + B_2),(y,b_k/2,b/2))
    return alpha0L_2,alpha0L_law_integral_indefinite
#-----------------------------------------------------------
def integrate_clalpha_mean(A_c,B_c,A_clalpha,B_clalpha,b,S):
    y = Symbol('y')
    # indefinite integrals
    c_lalphabar_law_integral_indefinite =  integrate( (A_c*y + B_c)*(A_clalpha*y+B_clalpha),y )
    c_lalphabar =  2/S*integrate((A_c*y + B_c)*(A_clalpha*y+B_clalpha),(y,0,b/2))
    return c_lalphabar,c_lalphabar_law_integral_indefinite
#-------------------------------------------------------------
def thickness(A_t,B_t,A_c,B_c,b,S_ref):
    y = Symbol('y')
    thick_law_integral_indefinite = integrate((A_t*y + B_t)*(A_c*y + B_c),y)
    mean_thick = (2/S_ref)*integrate((A_t*y + B_t)*(A_c*y + B_c),(y,0,b/2))
    return   mean_thick,thick_law_integral_indefinite



#-----------------------------------------------------------
def display_workflowACBC(c_r,c_t,A_c,B_c,b):
    return Latex(r'\begin{array}{ll}'
        +r'\\'
        + r'A_c=\dfrac{c_t-c_r}{b/2}=2'
        + r'\dfrac{' 
        + r'{0:.3}' .format(c_t) 
        + r'\,\text{m}-'
        + r'{0:.3}' .format(c_r)
        + r'\, \text{m}}{'
        + r'{0:.3}' .format(b)
        + r'\, \text{m}}='
        + r'{0:.3}' .format(A_c)
        + r'\\'     
        + r'B_c=c_r='
        + r'{0:.3}'.format(B_c)
        + r'\, \text{m}'
       + r'\end{array}'
        )
#-----------------------------------------------------------
def display_workflowACBC_cw(c_r,c_k,c_t,A_1,B_1,A_2,B_2,b_k,b_k2):
    return Latex(r'\begin{array}{ll}'
        +r'\\'
        + r'A_{c,1}=\dfrac{c_{t,1}-c_{r,1}}{b_1/2}=2'
        + r'\dfrac{' 
        + r'{0:.3}' .format(c_k) 
        + r'\,\text{m}-'
        + r'{0:.3}' .format(c_r)
        + r'\, \text{m}}{'
        + r'{0:.3}' .format(b_k)
        + r'\, \text{m}}='
        + r'{0:.3}' .format(A_1)
        + r'\\'     
        + r'B_{c,1}=c_{r,1}='
        + r'{0:.3}'.format(B_1)
        + r'\, \text{m}'
        + r'\\' 
        + r'A_{c,2}=\dfrac{c_{t,2}-c_{r,2}}{b_2/2}=2'
        + r'\dfrac{' 
        + r'{0:.3}' .format(c_t) 
        + r'\,\text{m}-'
        + r'{0:.3}' .format(c_k)
        + r'\, \text{m}}{'
        + r'{0:.3}' .format(b_k2)
        + r'\, \text{m}}='
        + r'{0:.3}' .format(A_2)
        + r'\\'     
        + r'B_{c,2}=c_{r,2}='
        + r'{0:.3}'.format(B_2)
        + r'\, \text{m}'
       + r'\end{array}'
        )
#-----------------------------------------------------------
def display_workflowAR(b,S,AR):
    return Latex(r'\begin{align*}'
        + r'AR=\frac{b^2}{S}='
        + r'\dfrac{('
        +r'{0:.3}' .format(b)
        +r'\,\text{m})^2}{'
        +r'{0:.3}' .format(S)
        +r'\,\text{m}^2}='
        + r'{0:.4}' .format(AR)   
      +r'\end{align*}'
             )

#-----------------------------------------------------------
def display_workflowc_mean(c_r,taper,c_mean):
    return Latex(r'\begin{align*}'
       + r'\bar{c} = \frac{2}{3}c_\mathrm{r}\frac{1 + \lambda + \lambda^2}{1+\lambda}='
       + r'0,67\cdot'
       + r'{0:.3}' .format(c_r)
       + r'\,\text{m} \cdot\dfrac{1+'
       + r'{0:.3}' .format(taper)
       + r'+'
       + r'{0:.3}^2' .format(taper)
       + r'}{1+'
       + r'{0:.3}' .format(taper)
       + r'}='
       + r'{0:.4}'.format(c_mean) +r'\,\text{m}'
     + r'\end{align*}'
                )
#-----------------------------------------------------------
def display_workflowXlec(b,taper,tan_lambda,x_lec):
    return Latex(r'\begin{align*}'
       + r'X_{le,\bar{c}}=\dfrac{'
       + r'{0:.3}' .format(b)
       + r' \, \text{m}}{6}\cdot'
       + r'\dfrac{1+2\cdot'
       + r'{0:.3}' .format(taper)
       + r'}{1+'
       + r'{0:.3}' .format(taper)
       + r'}\cdot \text{tan}('
       + r'{0:.3}' .format(tan_lambda)
       + r'\,\text{rad})='
       + r'{0:.3}' .format(x_lec)
       + r'\, \text{m}'
      + r'\end{align*}'
)
#-----------------------------------------------------------
def display_workflowYlec(b,taper,tan_lambda,y_lec):
    return Latex(r'\begin{align*}'
       + r'Y_{\bar{c}}=\dfrac{'
       + r'{0:.3}' .format(b)
       + r' \, \text{m}}{6}\cdot'
       + r'\dfrac{1+2\cdot'
       + r'{0:.3}' .format(taper)
       + r'}{1+'
       + r'{0:.3}' .format(taper)
       + r'}='
       + r'{0:.3}' .format(y_lec)
       + r'\, \text{m}'
      + r'\end{align*}'
    )
#-----------------------------------------------------------
def display_workflowAalpha(alpha0l_tip,alpha0l_root,A_alpha,B_alpha,b):
    return Latex(r'\begin{array}{ll}'
        +r'\\'
        + r'A_{\alpha}=\dfrac{\alpha_{0\ell,t}-\alpha_{0\ell,r}}{b/2}=2'
        + r'\dfrac{' 
        + r'{0:.3}' .format(alpha0l_tip) 
        + r'\,\text{rad}-('
        + r'{0:.3}' .format(alpha0l_root)
        + r'\, \text{rad})}{'
        + r'{0:.3}' .format(b)
        + r'\, \text{m}}='
        + r'{0:.3}' .format(A_alpha)
        + r'\\'     
        + r'B_{\alpha}=\alpha_{0\ell,r}='
        + r'{0:.3}'.format(B_alpha)
        + r'\, \text{rad}'
       + r'\end{array}'
        )
#-----------------------------------------------------------
def display_workflowAalpha_cw(alpha0l_tip,alpha0l_root,alpha0l_kink,A_alpha1,B_alpha1,A_alpha2,B_alpha2,b_k,b_k2):
    return Latex(r'\begin{array}{ll}'
        
        +r'A_{\alpha,1} = \dfrac{\alpha_\mathrm{0\ell,t,1}-\alpha_\mathrm{0\ell,r,1}}{ b_{\mathrm{1}}/2} ='
        + r'2\dfrac{' 
        + r'{0:.3}' .format(alpha0l_kink) 
        + r'\,\text{rad}-('
        + r'{0:.3}' .format(alpha0l_root)
        + r'\, \text{rad})}{'
        + r'{0:.3}' .format(b_k)
        + r'\, \text{m}}='
        + r'{0:.3}' .format(A_alpha1)
        + r'\,\text{rad/m}\\'     
         +r'B_{\alpha,1}=\alpha_\mathrm{0\ell,r,1}='
        + r'{0:.3}'.format(B_alpha1)
        + r'\, \text{rad}'
        + r'\\'
        +r'A_{\alpha,2} = \dfrac{\alpha_\mathrm{0\ell,t,2}-\alpha_\mathrm{0\ell,r,2}}{ b_{\mathrm{2}}/2} ='
        + r'2\dfrac{' 
        + r'{0:.3}' .format(alpha0l_tip) 
        + r'\,\text{rad}-('
        + r'{0:.3}' .format(alpha0l_kink)
        + r'\, \text{rad})}{'
        + r'{0:.3}' .format(b_k2)
        + r'\, \text{m}}='
        + r'{0:.3}' .format(A_alpha2)
        + r'\,\text{rad/m}\\'     
         +r'B_{\alpha,1}=\alpha_\mathrm{0\ell,r,1}='
        + r'{0:.3}'.format(B_alpha2)
        + r'\, \text{rad}'
       + r'\end{array}'
        )
#-----------------------------------------------------------
def display_workflowAepsilon(epsilon_t,epsilon_r,A_eps,B_eps,b):
    return Latex(r'\begin{array}{ll}'
        +r'\\'
        + r'A_{\epsilon}=\dfrac{\epsilon_{g,t}-\epsilon_{g,r}}{b/2}=2'
        + r'\dfrac{' 
        + r'{0:.3}' .format(epsilon_t) 
        + r'\,\text{rad}-('
        + r'{0:3}' .format(epsilon_r)
        + r'\, \text{rad})}{'
        + r'{0:3}' .format(b)
        + r'\, \text{m}}='
        + r'{0:.4}' .format(A_eps)
        + r'\,\text{rad/m}\\'     
        + r'B_{\epsilon}=\epsilon_{g,r}='
        + r'{0:3}'.format(B_eps)
        + r'\, \text{rad}'
       + r'\end{array}'
        )
#-----------------------------------------------------------
def display_workflowAepsilon_cw(epsilon_t,epsilon_k,epsilon_r,A_eps1,B_eps1,A_eps2,B_eps2,b_k,b_k2):
    return Latex(r'\begin{array}{ll}'
        + r'A_{\epsilon,1}=\dfrac{\epsilon_{g,t,1}-\epsilon_{g,r,1}}{b_1/2}=2'
        + r'\dfrac{' 
        + r'{0:.3}' .format(epsilon_k) 
        + r'\,\text{rad}-('
        + r'{0:3}' .format(epsilon_r)
        + r'\, \text{rad})}{'
        + r'{0:3}' .format(b_k)
        + r'\, \text{m}}='
        + r'{0:.4}' .format(A_eps1)
        + r'\,\text{rad/m}\\'     
        + r'B_{\epsilon,1}=\epsilon_{g,r,1}='
        + r'{0:3}'.format(B_eps1)
        + r'\, \text{rad}'
        + r'\\'         
        + r'A_{\epsilon,2}=\dfrac{\epsilon_{g,t,2}-\epsilon_{g,r,2}}{b_2/2}=2'
        + r'\dfrac{' 
        + r'{0:.3}' .format(epsilon_t) 
        + r'\,\text{rad}-('
        + r'{0:3}' .format(epsilon_k)
        + r'\, \text{rad})}{'
        + r'{0:.3}' .format(b_k2)
        + r'\, \text{m}}='
        + r'{0:.4}' .format(A_eps2)
        + r'\,\text{rad/m}\\'     
        + r'B_{\epsilon,2}=\epsilon_{g,r,2}='
        + r'{0:3}'.format(B_eps2)
        + r'\, \text{rad}'         
       + r'\end{array}'
        )
#-----------------------------------------------------------
def display_workflowsystemc(A_1,B_1,A_2,B_2,b_k,b):
    return  Latex(
    r'\begin{equation}'
     + r'c(y) ='
     + r'\begin{cases}'
     + r'c_1(Y)='
     + r'{0:.3}' .format(B_1)
     + r'\,\text{m}'
     + r'& \text{if $\;0\,\text{m} \le Y \le'
     + r'{0:.3}' .format(b_k/2)
     + r'$} \, \text{m}\\[0.5em]'
     + r'c_2(Y)='
     + r'{0:.3}' .format(A_2)
     + r'\big(Y-'
     + r'{0:.3}' .format(b_k/2)
     + r'\, \text{m}\big)' 
     + r'& \text{if $\;'
     + r'{0:.3}' .format(b_k/2)
     + r'\, \text{m}< Y \le'
     +r'{0:.3}' .format(b/2)
     + r'\,\text{m}$}'
     +r'\end{cases}'
    + r'\end{equation}'
      )
#-----------------------------------------------------------
def display_workflowsystemalpha(A_0l_1,B_0l_1,A_0l_2,B_0l_2,b_k,b):
    return  Latex( r'\begin{equation}'
     + r'\alpha_{0\ell}(Y) ='
     + r'\begin{cases}'
     + r'\alpha_{0\ell,1}(Y)='
     + r'{0:.3}' .format(B_0l_1)
     + r'\,\text{rad}'
     + r'& \text{if $\;0\,\text{m} \le Y \le'
     + r'{0:.3}' .format(b_k/2)
     + r'$} \, \text{m}\\[0.5em]'
     + r'\alpha_{0\ell,2}='
     + r'{0:.3}' .format(A_0l_2)
     +r'\, \text{rad/m}\big(Y-'
     + r'{0:.3}' .format(b_k/2)
     + r'\, \text{m}\big)'
     + r'{0:.3}' .format(B_0l_2)
     + r'\, \text{rad}'
     + r'& \text{if $\;'
     + r'{0:.3}' .format(b_k/2)
     + r'\, \text{m}< Y \le'
     +r'{0:.3}' .format(b/2)
     + r'\,\text{m}$}'
     +r'\end{cases}'
    + r'\end{equation}'
      )
#-----------------------------------------------------------
def display_workflowsystemalpha(A_eps1,B_eps1,A_eps2,B_eps2,b_k,b):
    return Latex(
    r'\begin{equation}'
     + r'\epsilon_{g}(Y) ='
     + r'\begin{cases}'
     + r'\epsilon_{g,1}(Y)='
     + r'{0:.3}' .format(A_eps1)
     + r'\, \text{rad/m}\,Y+'
     + r'{0:.3}' .format(B_eps2)
     + r'\, \text{rad}'
     + r'& \text{if $\;0\,\text{m} \le Y \le'
     + r'{0:.3}' .format(b_k/2)
     + r'$} \, \text{m}\\[0.5em]'
     + r'\epsilon_{g,2}(Y)='
     + r'{0:.3}' .format(A_eps2)
     + r'\,\text{rad/m}\big(Y-'
     + r'{0:.3}' .format(b_k/2)
     + r'\, \text{m}\big)+' 
     + r'{0:.3}' .format(B_eps2)
     +r' \, \text{rad}'
     + r'& \text{if $\;'
     + r'{0:.3}' .format(b_k/2)
     + r'\, \text{m}< Y \le'
     +r'{0:.3}' .format(b/2)
     + r'\,\text{m}$}'
     +r'\end{cases}'
    + r'\end{equation}'
      )
#-----------------------------------------------------
def integrate_cmac(A_c,B_c,b,S_ref):
    y = Symbol('y')
    c_mac_law_integral_indefinite =  integrate( (A_c*y + B_c)*(A_c*y + B_c),y )
    c_mac =  (2/S_ref)*integrate( (A_c*y + B_c)*(A_c*y + B_c),(y,0,b/2) )
    return c_mac,c_mac_law_integral_indefinite
#----------------------------------------
def integrate_x_le_mac(A_c,B_c,A_xle,b,S_ref):
    y = Symbol('y')
    X_le_mac_law_integral_indefinite = integrate( (A_xle*y)*(A_c*y + B_c), y )
    X_le_mac = (2/S_ref)*integrate( (A_xle*y)*(A_c*y + B_c),(y,0,b/2) )
    return X_le_mac,X_le_mac_law_integral_indefinite
#--------------------------------------------
def integrate_y_mac(A_c,B_c,b,S_ref):
    y = Symbol('y')
    Y_mac_law_integral_indefinite = integrate( y*(A_c*y + B_c), y )
    Y_mac =  (2/S_ref)*integrate( y*(A_c*y + B_c),(y,0,b/2) )
    return Y_mac,Y_mac_law_integral_indefinite
#----------------------------------------
def integrate_c_maca(A_CM,B_CM,A_c,B_c,b,S_ref):
    y = Symbol('y')
    c_mac_law_integral_indefinite =  integrate( (A_CM*y + B_CM)*(A_c*y + B_c),y )
    c_maca =  (2/S_ref)*integrate( (A_CM*y + B_CM)*(A_c*y + B_c),(y,0,b/2) )
    return c_maca,c_mac_law_integral_indefinite
#-------------------------------------
def integrate_c_macb(A_c,B_c,A_clalpha,B_clalpha,alpha0L,A_alpha,B_alpha,A_eps,B_eps,X_ac,b,S_ref,c_mean):
    y = Symbol('y')
    c_macb_law_integral_indefinite =  integrate( (A_c*y + B_c)*(A_clalpha*y+B_clalpha)*(alpha0L-(A_alpha*y+B_alpha - A_eps*y -B_eps ))*(X_ac - 1/4 *(A_c*y + B_c)),y )
    c_macb =  (1/S_ref*c_mean)*integrate((A_c*y + B_c)*(A_clalpha*y+B_clalpha)*(alpha0L-(A_alpha*y+B_alpha - A_eps*y -B_eps ))*(X_ac - 1/4 *(A_c*y + B_c)),(y,0,b/2) )
    return c_macb,c_macb_law_integral_indefinite
