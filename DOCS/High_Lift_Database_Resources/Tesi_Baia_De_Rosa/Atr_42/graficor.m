function da_max=graficor(CL_alpha,CL_max, CL0,Alpha_star,Alpha0 )

N=20;

                        
alpha_max1=22;
alpha_star1=Alpha_star-2;
%Non-linear part of the lift curve:
a=(CL_alpha*alpha_star1+CL0-CL_max)/(alpha_star1-alpha_max1)^2;
b=-2*a*alpha_max1;
c=CL_max+a*alpha_max1^2;

g=(-b+(b^2-4*a*(c-CL_max)).^(0.5))/(2*a);
x=linspace(Alpha0-10,g+3,N);
%the complete equation of the lift curve
for i=1:N
    if     x(i)<=alpha_star1                                     %curve is linear
        y(i)=CL_alpha*x(i) + CL0;  
  
    else                                                         %curve is parabolic
        y(i)=a*x(i)^2+b*x(i)+c;
      
 
    end
end

da_max=(-b+sqrt(b^2 - 4*a*(c-CL_max)))/(2*a);

plot(x,y,'-r','linewidth',2);
xlabel('\alpha');
ylabel('CL');
axis([Alpha0-30 g+30 CL0-2 CL_max+1]);
end