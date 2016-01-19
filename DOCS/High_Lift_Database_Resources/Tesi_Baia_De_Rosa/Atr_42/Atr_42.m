clc;clear all;close all;
%Script for variation of CL and CD in clean configuration due to high-lift systems

%How to use the program
%The user have to insert all input data for wing, all flaps and slats;
%All angles are entered in degrees;
%It's possible to insert the type of flap for computations.
%The parameter 'type_flap' can assume six different values:
%1) type_flap=1 ---> Single slotted flap
%2) type_flap=2 ---> Double slotted flap
%3) type_flap=3 ---> Split flap
%4) type_flap=4 ---> Plain flap
%5) type_flap=5 ---> Fowler flap
%6) type_flap=6 ---> Triple slotted flap

%According to the theory adopted, for all kind of leading edge devices (slats
%and Krueger flaps), computations are the same.




%Input geometric data for wing 
c1=2.6;  %root chord (1st station--> y=0)
c2=c1;  %chord at second station (--> y=bg)
c3=1.4;  %tip chord (3rd station --> Y=b/2
b=24.57;  %wing span
bg=4.80;  %second station
CD0=0.03;  %parasitic drag coefficient
t=0.3;  %thickness
e=0.80;  %Oswald factor 

Lambda_Le=0;  %sweep-angle at leading edge (1st part of the wing)  
Lambda_Le=Lambda_Le*pi/180;
Lambda_Le2=7;  %sweep-angle at leading edge (2nd part of the wing)

%%
%Equivalent wing data
S1=(c1+c2)*bg; S2=(c2+c3)*((b/2)-bg);
ct=c3;  %tip chord
cr=(S1+S2)/(b/2)-ct;  %root chord

%Input aerodynamic data for airfoil
Cl_alpha=0.113;  %curve slope
Cl0=0.3729;  %Cl at alpha zero lift
Cl_max=1.87;  %max value of Cl
alpha_star=11;  %angle of attack at the end of linearity
alpha0=-3.3;  %angle of attack at zero lift


%%

%Input aerodynamic data for wing    
CL_alpha=0.098;
CL0=0.196;                                                  
CL_max=1.6;
Alpha_star=9.685;
Alpha0=-2;


%Input data for flap 1
type_flap=1;
deltaf1=20;
deltaf1=deltaf1*pi/180;
deltaf2=00;
deltaf2=deltaf2*pi/180;
deltaf3=0;
deltaf3=deltaf3*pi/180;
cf=0.400;                                                 %mean flap chord
deltaf=deltaf1+deltaf2+deltaf3;                           %flap deflection
deltaf_ref=45;
deltaf_ref=deltaf_ref*pi/180;
ni=0.098;                                                 %flap inboard
no=bg/(b/2);                                              %flap outboard


%Input data for flap 2
type_flap_2=1;
deltaf1_2=20;
deltaf1_2=deltaf1_2*pi/180;
deltaf2_2=00;
deltaf2_2=deltaf2_2*pi/180;
deltaf3_2=0;
deltaf3_2=deltaf3_2*pi/180;
cf2=0.400;                                               %mean flap chord
deltaf_2=deltaf1_2+deltaf2_2+deltaf3_2;                  %flap deflection
deltaf_ref_2=45;
deltaf_ref_2=deltaf_ref_2*pi/180;
ni2=no;                                                  %flap inboard
no2=0.75;                                                %flap outboard


%Input data for slat 1
Delta_s=00;                                              %slat deflection
cs=0.000;                                                %slat chord
c_ext_suC=0.0;  %extended chord to airfoil chord ratio
Le_radius=0.0000; %Leading edge radius
ni_s=0.000;  %slat inboard
no_s=0.000;  %slat outboard

%Input data for slat 2
Delta_s_2=00;  %slat deflection
cs2=0.000;  %slat chord
c_ext_suC_2=0.0;  %extended chord to airfoil chord ratio
Le_radius_2=0.0000;  %Leading edge radius
nis2=0.000;  %slat inboard
nos2=0.00;  %slat outboard

%Input data for slat 3
Delta_s_3=00;  %slat deflection
cs3=0.000;   %slat chord
c_ext_suC_3=0.0;   %extended chord to airfoil chord ratio
Le_radius_3=0.0000;  %Leading edge radius
nis3=0.00;  %slat inboard
nos3=0.00;  %slat outboard
%Input data for slat 4
Delta_s_4=0;  %slat deflection
cs4=0;  %slat chord
c_ext_suC_4=0;  %extended chord to airfoil chord ratio
Le_radius_4=0;  %Leading edge radius
nis4=0;  %slat inboard
nos4=0;  %slat outboard

%%

%%

%Output geometric data (wing)
S=((cr+ct)*(b/2));  %wing surface
AR=(b^2)/S;  %Aspect Ratio
lambda=ct/cr;
c=2/3*cr*(1+lambda+lambda^2)/(1+lambda); %mean aerodynamic chord
TsuC=t/c;                                                   
CfsuC=cf/c;
lambda_cquarti=tan(Lambda_Le2*pi/180)-((1-lambda)/(AR*(1+lambda)));


%%

%Output data for flap1
Swf=abs(b/2*cr*(2-(1-lambda)*(ni-no))*(ni-no));  %wing surface where flap is located
bf=(no-ni)*b/2;
Delta_alpha_max_1=Delta_alpha_max(deltaf); %variation of stall-angle due to flap deflection
%Output data for flap 2
Swf_2=abs(b/2*cr*(2-(1-lambda)*(ni2-no2))*(ni2-no2));  %wing surface where flap is located
bf_2=(no2-ni2)*b/2;
Delta_alpha_max_2=Delta_alpha_max(deltaf_2);
%Output data for slat 1
CSsuC=cs/c;
Le_radius_su_TsuC=Le_radius/TsuC;                            %Leading edge radius
Sws=abs(b/2*cr*(2-(1-lambda)*(ni_s-no_s))*(ni_s-no_s));      %wing surface where slat is located
Delta_alpha_max_s1=Delta_alpha_max(Delta_s);  
%Output data for slat 2
CSsuC_2=cs2/c;
Le_radius_su_TsuC_2=Le_radius_2/TsuC;                        %Leading edge radius
Sws_2=abs(b/2*cr*(2-(1-lambda)*(nis2-nos2))*(nis2-nos2));    %wing surface where slat is located
Delta_alpha_max_s2=Delta_alpha_max(Delta_s_2); 
%Output data for slat 3
CSsuC_3=cs3/c;
Le_radius_su_TsuC_3=Le_radius_3/TsuC;                        %Leading edge radius
Sws_3=abs(b/2*cr*(2-(1-lambda)*(nis3-nos3))*(nis3-nos3));    %wing surface where slat is located
Delta_alpha_max_s3=Delta_alpha_max(Delta_s_3); 
%Output data for slat 4
CSsuC_4=cs4/c;
Le_radius_su_TsuC_4=Le_radius_4/TsuC;                        %Leading edge radius
Sws_4=abs(b/2*cr*(2-(1-lambda)*(nis4-nos4))*(nis4-nos4));    %wing surface where slat is located
Delta_alpha_max_s4=Delta_alpha_max(Delta_s_4); 

%Wing design
figure(1)
Wing_design(Lambda_Le,Lambda_Le2,b,c1,c2,c3,bg,cf,ni,no,cf2,ni2,no2,cs,ni_s,no_s,cs2,nis2,nos2,cs3,nis3,nos3,cs4,nis4,nos4);


%Flap 1 effects

%Variation of airfoil parameters (2D)
%Determination of Delta_Clmax:
k1=kappa1(type_flap,cf/c);
k2=kappa2(type_flap,deltaf);
k3=kappa3(type_flap,deltaf/deltaf_ref);
Delta_Clmax_base=Delta_Cl_maxbase(type_flap,t/c);
Delta_Clmax=k1*k2*k3*(Delta_Clmax_base);

%Determination of delta_Cl0:
thetaf=acos(2*(cf/c)-1);
alphadelta = 1-(thetaf-sin(thetaf))/pi;                     %rate of change of zero lift angle of attack due to flap deflection. 
etadelta=eta_delta_flap(type_flap,deltaf);
delta_Cl0=etadelta*alphadelta*Cl_alpha*deltaf*180/pi;
%Calculation of the extended chord for flap:
dCsuCf=deltaC_su_Cf(type_flap,deltaf);
c_ext=c*(1+dCsuCf*cf/c);
%Parameter variation due to flap:
Cl_alpha1=Cl_alpha*(c_ext/c *(1- cf/c_ext * (sin(deltaf))^2));
Cl_max1=Cl_max+Delta_Clmax;
Cl01=Cl0+delta_Cl0;

%Variation of wing parameters (3D)


%Variation of CL0:


Kc=kappac(alphadelta,AR);
Kb=kb2(bf/b);
delta_CL0=delta_Cl0*Cl_alpha/CL_alpha*Kc*Kb;

CL01=CL0+delta_CL0;

%Variation of CL_max:
K_lambda=(1-0.08*(cos(lambda_cquarti))^2)*(cos(lambda_cquarti))^(3/4);
Delta_CLmax=Delta_Clmax*Swf/S*K_lambda;

CL_max1=CL_max+Delta_CLmax;
%Slope variation
CL_alpha1=CL_alpha*(1+(Delta_CLmax/Delta_Clmax)*((c_ext/c *(1- cf/c_ext * (sin(deltaf))^2))-1));



%%
%Flap 2 effects

%Variation of airfoil parameters (2D)
%Determination of Delta_Clmax:
k1_2=kappa1(type_flap_2,cf2/c);
k2_2=kappa2(type_flap_2,deltaf_2);
k3_2=kappa3(type_flap_2,deltaf_2/deltaf_ref_2);
Delta_Clmax_base_2=Delta_Cl_maxbase(type_flap_2,t/c);
Delta_Clmax_2=k1_2*k2_2*k3_2*(Delta_Clmax_base_2);

%Determination of delta_Cl0:
thetaf_2=acos(2*(cf2/c)-1);
alphadelta_2 = 1-(thetaf_2-sin(thetaf_2))/pi;                     %rate of change of zero lift angle of attack due to flap deflection. 
etadelta_2=eta_delta_flap(type_flap_2,deltaf_2);
delta_Cl0_2=etadelta_2*alphadelta_2*Cl_alpha*deltaf_2*180/pi;
%Calculation of the extended chord for flap:
dCsuCf_2=deltaC_su_Cf(type_flap_2,deltaf_2);
c_ext_2=c*(1+dCsuCf_2*cf2/c);
%Parameter variation due to flap:
Cl_alpha2=Cl_alpha*(c_ext_2/c *(1- cf2/c_ext_2 * (sin(deltaf_2))^2));
Cl_max2=Cl_max+Delta_Clmax_2;
Cl02=Cl0+delta_Cl0_2;

%Variation of wing parameters (3D)





%Variation of CL0:
Kc_2=kappac(alphadelta_2,AR);
Kb_2=kb2(bf_2/b);
delta_CL0_2=delta_Cl0_2*Cl_alpha/CL_alpha*Kc_2*Kb_2;

CL02=CL0+delta_CL0_2;

%Variation of CL_max:
K_lambda=(1-0.08*(cos(lambda_cquarti))^2)*(cos(lambda_cquarti))^(3/4);
Delta_CLmax_2=Delta_Clmax_2*Swf_2/S*K_lambda;

CL_max2=CL_max+Delta_CLmax_2;
%Slope variation
CL_alpha2=CL_alpha*(1+(Delta_CLmax_2/Delta_Clmax_2)*((c_ext_2/c *(1- cf2/c_ext * (sin(deltaf_2))^2))-1));




%%



%Slat 1 
%Determination of airfoil parameters

eta_max_s=Datcom_corretto(Le_radius_su_TsuC);
etadelta_s=eta_delta(Delta_s);
dCl_su_dDelta_max=dCl(CSsuC);
Delta_Clmax_slat=dCl_su_dDelta_max*eta_max_s*etadelta_s*Delta_s*c_ext_suC;

Cl_max1_s=Cl_max+Delta_Clmax_slat;

%Determination of wing parameter 
K_lambda=(1-0.08*(cos(lambda_cquarti))^2)*(cos(lambda_cquarti))^(3/4);
Delta_CLmax1_s=Delta_Clmax_slat*(Sws/S)*K_lambda;
CL_max1_s=CL_max+Delta_CLmax1_s;




%Slat 2 
%Determination of airfoil parameters

eta_max_s_2=Datcom_corretto(Le_radius_su_TsuC_2);
etadelta_s_2=eta_delta(Delta_s_2);
dCl_su_dDelta_max_2=dCl(CSsuC_2);
Delta_Clmax_slat_2=dCl_su_dDelta_max_2*eta_max_s_2*etadelta_s_2*Delta_s_2*c_ext_suC_2;

Cl_max2_s=Cl_max+Delta_Clmax_slat_2;

%Determination of wing parameter 
K_lambda=(1-0.08*(cos(lambda_cquarti))^2)*(cos(lambda_cquarti))^(3/4);
Delta_CLmax2_s=Delta_Clmax_slat_2*(Sws_2/S)*K_lambda;
CL_max2_s=CL_max+Delta_CLmax2_s;




%Slat 3 
%Determination of airfoil parameters

eta_max_s_3=Datcom_corretto(Le_radius_su_TsuC_3);
etadelta_s_3=eta_delta(Delta_s_3);
dCl_su_dDelta_max_3=dCl(CSsuC_3);
Delta_Clmax_slat_3=dCl_su_dDelta_max_3*eta_max_s_3*etadelta_s_3*Delta_s_3*c_ext_suC_3;

Cl_max3_s=Cl_max+Delta_Clmax_slat_3;

%Determination of wing parameter 
K_lambda=(1-0.08*(cos(lambda_cquarti))^2)*(cos(lambda_cquarti))^(3/4);
Delta_CLmax3_s=Delta_Clmax_slat_3*(Sws_3/S)*K_lambda;
CL_max3_s=CL_max+Delta_CLmax3_s;




%Slat 4
%Determination of airfoil parameters

eta_max_s_4=Datcom_corretto(Le_radius_su_TsuC_4);
etadelta_s_4=eta_delta(Delta_s_4);
dCl_su_dDelta_max_4=dCl(CSsuC_4);
Delta_Clmax_slat_4=dCl_su_dDelta_max_4*eta_max_s_4*etadelta_s_4*Delta_s_4*c_ext_suC_4;

Cl_max4_s=Cl_max+Delta_Clmax_slat_4;

%Determination of wing parameter 
K_lambda=(1-0.08*(cos(lambda_cquarti))^2)*(cos(lambda_cquarti))^(3/4);
Delta_CLmax4_s=Delta_Clmax_slat_4*(Sws_4/S)*K_lambda;
CL_max4_s=CL_max+Delta_CLmax4_s;



%Wing ultimate computations
CL_max_w_flap=CL_max+Delta_CLmax+Delta_CLmax_2;
CL_max_w=CL_max+Delta_CLmax+Delta_CLmax_2+Delta_CLmax1_s+Delta_CLmax2_s+Delta_CLmax3_s;
figure(7)
alpha_max=graficor(CL_alpha,CL_max, CL0,Alpha_star,Alpha0);hold on;
alpha_max_1=graficob(CL_alpha1,CL_max1,CL01,Alpha_star,Alpha0,Delta_alpha_max_1,alpha_max)
graficov(CL_alpha1+CL_alpha2-CL_alpha,CL_max_w_flap,CL01+CL02-CL0,Alpha_star,Alpha0,Delta_alpha_max_2,alpha_max_1);

legend('cruise','flap 1','flap 2');
title('CL-\alpha graph for the falpped wing');

