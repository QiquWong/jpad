clc;clear all;close all;
%Script for variation of CL in clean configuration due to high-lift systems

%Input geometric data for wing 
c1=5.2;                                                     %root chord (1st station--> y=0)
c2=3.2;                                                     %chord at second station (--> y=bg)
c3=1.6;                                                     %tip chord (3rd station --> Y=b/2
b=26.8;                                                     %wing span
bg=5;                                                       %second station

t=0.3;                                                      %thickness
e=0.89;                                                     %Oswald factor 
lambda_cquarti=25.02;
Lambda_Le=27;
%%
%Equivalent wing data
S1=(c1+c2)*bg; S2=(c2+c3)*((b/2)-bg);
ct=c3;                                                      %tip chord
cr=(S1+S2)/(b/2)-ct;                                        %root chord

%Input aerodynamic data for airfoil
Cl_alpha=0.1007;                                            %curve slope
Cl0=0.5421;                                                 %Cl at alpha zero lift
Cl_max=1.7104;                                              %max value of Cl
alpha_star=11;                                              %angle of attack at the end of linearity
alpha0=-5;                                                  %angle of attack at zero lift

%%

%Input aerodynamic data for wing                                            
CL0=0.1;                                                    %Cl at alpha zero lift
CL_max=1.378;
Alpha_star=9.685;
Alpha0=-1;
%%


%Input data for flap 1
type_flap=1;
deltaf1=20;
deltaf2=0;
deltaf3=0;
cf=0.5;                                                     %mean flap chord
deltaf=deltaf1+deltaf2+deltaf3;                             %flap deflection
deltaf_ref=30;
ni=0.1;                                                       %flap inboard
no=0.3;                                                  %flap outboard
Delta_alpha_max=Delta_alpha_max(deltaf);                    %variation of stall-angle due to flap deflection
%%
%Flap 2
cf2=0;
ni2=0;
no2=0;
%Input data for slat 1
Delta_s=20;                                                 %slat deflection
cs=0.5;                                                     %slat chord
c_ext_suC=1.1;                                              %extended chord to airfoil chord ratio
Le_radius=0.0097;                                           %Leading edge radius
ni_s=0.2;                                                   %slat inboard
no_s=0.4;                                                   %slat outboard

%slat 2
cs2=0;
nis2=0;
nos2=0;
%slat 3
cs3=0;
nis3=0;
nos3=0;
%slat 2
cs4=0;
nis4=0;
nos4=0;


%%

%Output geometric data (wing)
S=((cr+ct)*(b/2));                                          %wing surface
AR=(b^2)/S;                                                 %Aspect Ratio
lambda=ct/cr;
c=2/3*cr*(1+lambda+lambda^2)/(1+lambda);                    %mean aerodynamic chord
TsuC=t/c;                                                   
CfsuC=cf/c;



%%

%Output data for flap
Swf=b/2*cr*(2-(1-lambda)*(no-ni))*(no-ni);                  %wing surface where flap is located
bf=(no-ni)*b/2;
%Output data for slat
CSsuC=cs/c;
Le_radius_su_TsuC=Le_radius/TsuC;                           %Leading edge radius
Sws=b/2*cr*(2-(1-lambda)*(no_s-ni_s))*(no_s-ni_s);          %wing surface where slat is located

%Wing design
figure(1)
Wing_design(Lambda_Le,b,c1,c2,c3,bg,cf,ni,no,cf2,ni2,no2,cs,ni_s,no_s,cs2,nis2,nos2,cs3,nis3,nos3,cs4,nis4,nos4);pause(5);


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
delta_Cl0=etadelta*alphadelta*Cl_alpha*deltaf;
%Calculation of the extended chord for flap:
dCsuCf=deltaC_su_Cf(type_flap,deltaf);
c_ext=c*(1+dCsuCf*cf/c);
%Parameter variation due to flap:
Cl_alpha1=Cl_alpha*(c_ext/c *(1- cf/c_ext * (sin(deltaf))^2));
Cl_max1=Cl_max+Delta_Clmax;
Cl01=Cl0+delta_Cl0;

%Variation of wing parameters (3D)


CL_alpha=Cl_alpha/(1+(Cl_alpha)/(pi*AR*e));                 %Clean configuration slope


%Variation of CL0:
Kc=1.06;
Kb=kb2(bf/b);
delta_CL0=delta_Cl0*Cl_alpha/CL_alpha*Kc*Kb;

CL01=CL0+delta_CL0;

%Variation of CL_max:
K_lambda=(1-0.08*(cos(lambda_cquarti))^2)*(cos(lambda_cquarti))^(3/4);
Delta_CLmax=Delta_Clmax*Swf/S*K_lambda;

CL_max1=CL_max+Delta_CLmax;
%Slope variation
CL_alpha1=CL_alpha*(1+(Delta_CLmax/Delta_Clmax)*((c_ext/c *(1- cf/c_ext * (sin(deltaf))^2))-1));


figure(2) %Flapped and unflapped wing CL-\alpha graph
graficor(-20,20,CL_alpha,CL_max, CL0,Alpha_star);hold on
graficob(-20,20,CL_alpha1,CL_max1, CL01,Alpha_star,Delta_alpha_max);axis([-30 30 -3 3]);
legend('flap up','flap down');
title('Flapped and unflapped wing CL-\alpha graph');
figure(3) %Flapped and unflapped airfoil Cl-\alpha graph
graficor(-20,15,Cl_alpha,Cl_max, Cl0,alpha_star);hold on
graficob(-20,18,Cl_alpha1,Cl_max1, Cl01,alpha_star,Delta_alpha_max);axis([-30 30 -3 3.5]);
title('Flapped and unflapped airfoil Cl-\alpha graph');
legend('flap up','flap down');

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

figure(4) %Slatted and unslatted airfoil Cl-\alpha graph
graficor(-20,20,CL_alpha,CL_max, CL0, Alpha_star);hold on
graficob(-20,25,CL_alpha,CL_max1_s, CL0, Alpha_star,Delta_alpha_max);axis([-30 30 -3 3]);
title('slatted and unslatted airfoil CL-\alpha graph');
legend('slat up','slat down');

figure(5) %Flapped and unflapped airfoil Cl-\alpha graph
graficor(-20,15,Cl_alpha,Cl_max, Cl0, alpha_star);hold on
graficob(-20,30,Cl_alpha,Cl_max1_s, Cl0, alpha_star,Delta_alpha_max);axis([-30 40 -3 3.5]);
title('slatted and unslatted wing Cl-\alpha graph');
legend('slat up','slat down');


%Wing ultimate computations
CL_max_w=CL_max+Delta_CLmax1_s+Delta_CLmax;
figure(6)
graficor(-20,20,CL_alpha,CL_max, CL0,Alpha_star);hold on;
graficob(-20,18,CL_alpha1,CL_max1, CL01,Alpha_star,Delta_alpha_max);
graficov(-20,20,CL_alpha1,CL_max_w,CL01,Alpha_star,Delta_alpha_max);axis([-30 30 -3 3]);
legend('Clean configuration','flap down, slat up','flap and slat down');
title('CL-\alpha graph for a falpped and slatted wing');


