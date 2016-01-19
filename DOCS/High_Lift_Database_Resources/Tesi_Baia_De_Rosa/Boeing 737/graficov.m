function g=graficov(CL_alpha,CL_max, CL0,Alpha_star,Alpha0,Delta_alpha_max,alpha_max)

N=50;


 Alpha_star=Alpha_star+Delta_alpha_max;
%Non-linear part of the lift curve
a=-(CL_alpha)^2/[4*(CL_max-CL0-CL_alpha*Alpha_star)];
b=CL_alpha-2*a*Alpha_star;
c=CL_max+((b^2)/(4*a));

g=(-b+(b^2-4*a*(c-CL_max)).^(0.5))/(2*a);
x=linspace(Alpha0-10,g+5,N);
%the complete equation of the lift curve
for i=1:N
    if     x(i)<=Alpha_star                                     %curve is linear
        y(i)=CL_alpha*x(i) + CL0;  
  
    else                                                         %curve is parabolic
        y(i)=a*x(i)^2+b*x(i)+c;
      
 
    end
end

  plot(x,y,'-g','linewidth',2);
xlabel('\alpha');
ylabel('CL');

axis([Alpha0-35 g+30 CL0-2 CL_max+1]);
end
