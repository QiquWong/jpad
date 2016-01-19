function CD=graficov_CD(CD0,e,AR,deltaCD1,deltaCD2,CL_max)


N=100;
k=1/(pi*AR*e);
CL=linspace(0,CL_max,N)
CD=CD0+k*CL.^2+deltaCD1+deltaCD2;
CD_max=CD0+k*(CL_max)^2+deltaCD1+deltaCD2
plot(CL,CD,'-g','linewidth',2);
axis([0 CL_max+0.2 CD0-0.01 CD_max+0.01])
xlabel('CL'); ylabel('CD');
end
