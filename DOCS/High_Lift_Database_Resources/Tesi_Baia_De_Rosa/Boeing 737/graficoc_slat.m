function alpha_max1=graficoc_slat(CL_alpha,CL_max, CL0,Alpha_star,Alpha0,Delta_alpha_max,alpha_max )

N=50;

alpha_max1=alpha_max-Delta_alpha_max+4;
alpha_star1=Alpha_star+Delta_alpha_max;

%Non-linear part of the lift curve:
a=(CL_alpha*alpha_star1+CL0-CL_max)/(alpha_star1-alpha_max1)^2;
b=-2*a*alpha_max1;
c=CL_max+a*alpha_max1^2;


g=(-b+(b^2-4*a*(c-CL_max)).^(0.5))/(2*a);
x=linspace(Alpha0-10,g+4,N);
%the complete equation of the lift curve
for i=1:N
    if     x(i)<=alpha_star1                                     %curve is linear
        y(i)=CL_alpha*x(i) + CL0;  
  
    else                                                         %curve is parabolic
        y(i)=a*x(i)^2+b*x(i)+c;
      
 
    end
end



plot(x,y,'-c','linewidth',2);
xlabel('\alpha');
ylabel('CL');
axis([Alpha0-35 g+35 CL0-1.5 CL_max+1]);
end