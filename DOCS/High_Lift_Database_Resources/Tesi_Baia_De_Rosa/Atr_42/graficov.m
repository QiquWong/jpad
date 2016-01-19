function graficov(CL_alpha,CL_max, CL0,Alpha_star,Alpha0,Delta_alpha_max,alpha_max)

N=18;

alpha_max1=alpha_max+Delta_alpha_max;
alpha_star1=Alpha_star+Delta_alpha_max;

%Non-linear part of the lift curve:
a=(CL_alpha*alpha_star1+CL0-CL_max)/(alpha_star1-alpha_max1)^2;
b=-2*a*alpha_max1;
c=CL_max+a*alpha_max1^2;


g=alpha_max;
x=linspace(Alpha0-10,g+2.5,N);
%the complete equation of the lift curve
for i=1:N
    if     x(i)<=alpha_star1                                     %curve is linear
        y(i)=CL_alpha*x(i) + CL0;  
  
    else                                                         %curve is parabolic
        y(i)=a*x(i)^2+b*x(i)+c;
      
 
    end
end

  plot(x,y,'-g','linewidth',2);
xlabel('\alpha');
ylabel('CL');
axis([Alpha0-30 g+30 CL0-2 CL_max+1]);
end

