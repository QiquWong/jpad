function Delta_CD=graficob_CD(CD0,e,AR,S,cf,c,Swf,deltaf,CL_max)


N=100;
k=1/(pi*AR*e);
CL=linspace(0,CL_max,N)
Delta_CD=0.9*((cf/c)^(1.38)) *(Swf/S)*(sin(deltaf))^2;
CD=CD0+k*CL.^2+Delta_CD;
CD_max=CD0+k*(CL_max)^2+Delta_CD;
plot(CL,CD,'-b','linewidth',2);axis([0 (CL_max+0.5) CD0-0.01 CD_max+0.01]);
xlabel('CL'); ylabel('CD');
end

