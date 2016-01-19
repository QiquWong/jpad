function slat=slat(p,nis,nos,cs,bc,freccia2)

N=50;
q=0;
x=linspace(0,bc,N);
y=(p*bc+q)+tan(-freccia2*pi/180)*(x-bc);
%slat chord at nis:
y=linspace((p*bc+q)+tan(-freccia2*pi/180)*(nis-bc),(p*bc+q)+tan(-freccia2*pi/180)*(nis-bc)-cs,N);
p1=0; q1=nis; 
x=p1*y+q1;
plot(x,y,'k-','linewidth',2);

%slat chord at nis:
y=linspace((p*bc+q)+tan(-freccia2*pi/180)*(nos-bc),(p*bc+q)+tan(-freccia2*pi/180)*(nos-bc)-cs,N);
p1=0; q1=nos; 
x=p1*y+q1;
plot(x,y,'k-','linewidth',2);

%lower part of the slat:
x=linspace(nis,nos,N);
y=(x-nis)/(nos-nis)*(((p*bc+q)+tan(-freccia2*pi/180)*(nos-bc))-((p*bc+q)+tan(-freccia2*pi/180)*(nis-bc)))+((p*bc+q)+tan(-freccia2*pi/180)*(nis-bc)-cs);
plot(x,y,'k-','linewidth',2);

end