function g=graficob_slat(CL_alpha,CL_max, CL0,Alpha_star,Alpha0 )

N=50;

 Alpha01=-CL0/CL_alpha;
 Alpha_star=Alpha_star+Alpha01-Alpha0;
%Non-linear part of the lift curve
a=-(CL_alpha)^2/[4*(CL_max-CL0-CL_alpha*Alpha_star)];
b=CL_alpha-2*a*Alpha_star;
c=CL_max+((b^2)/(4*a));

g=(-b+(b^2-4*a*(c-CL_max)).^(0.5))/(2*a);
x=linspace(Alpha0-15,g+4,N);
%the complete equation of the lift curve
for i=1:N
    if     x(i)<=Alpha_star                                     %curve is linear
        y(i)=CL_alpha*x(i) + CL0;  
  
    else                                                         %curve is parabolic
        y(i)=a*x(i)^2+b*x(i)+c;
      
 
    end
end



plot(x,y,'-b','linewidth',2);
xlabel('\alpha');
ylabel('CL');
axis([Alpha0-20 g+10 CL0-3 CL_max+2]);
end