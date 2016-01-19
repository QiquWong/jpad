function disegno=Wing_design(freccia,freccia2,b,cr,cc,ct,bc,cf1,nif1,nof1,cf2,nif2,nof2,cs1,nis1,nos1,cs2,nis2,nos2,cs3,nis3,nos3,cs4,nis4,nos4)

%Wing drawing:
nis1=nis1*b/2;
nis2=nis2*b/2;
nis3=nis3*b/2;
nis4=nis4*b/2;
nos1=nos1*b/2;
nos2=nos2*b/2;
nos3=nos3*b/2;
nos4=nos4*b/2;
nif1=nif1*b/2;
nif2=nif2*b/2;
nof1=nof1*b/2;
nof2=nof2*b/2;

if nof1<nif1
    error('ERROR: the correct input is ni<=no')
end
if nof2<nif2
    error('ERROR: the correct input is ni<=no')
end
if nos1<nis1
    error('ERROR: the correct input is ni<=no')
end
if nos2<nis2
    error('ERROR: the correct input is ni<=no')
end
if nos3<nis3
    error('ERROR: the correct input is ni<=no')
end
%The upper part of the wing:
N=100;

freccia=-freccia;
x=linspace(0,bc,N);
q=0; p=tan(freccia);
y=p*x+q;
figure(1)
plot(x,y,'k-','linewidth',2);
hold on;

x=linspace(bc,b/2,N);
y=(p*bc+q)+tan(-freccia2*pi/180)*(x-bc);
plot(x,y,'k-','linewidth',2);
hold on;

%root chord:

y=linspace(-cr,0,N);
p1=0;q1=0;
x=p1*y+q1;
plot(x,y,'k-','linewidth',2);
hold on;
%lower part of the wing, from 1st to 2nd station:

x=linspace(0,bc,N);
y=x/bc*((p*bc+q)-cc+cr)-cr;
plot(x,y,'k-','linewidth',2);
hold on;
%tip chord:
y=linspace((p*bc+q)+tan(-freccia2*pi/180)*(b/2-bc),((p*bc+q)+tan(-freccia2*pi/180)*(b/2-bc))-ct,N);
p1=0; q1=b/2;
x=p1*y+q1;
plot(x,y,'k-','linewidth',2);
hold on;
%lower part of the wing, from 2nd to 3rd station:
x=linspace(bc,b/2,N);
y=(x-bc)/(b/2-bc)*(((p*bc+q)+tan(-freccia2*pi/180)*(b/2-bc))-((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))+cc-ct)+((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))-cc;
plot(x,y,'k-','linewidth',2);
hold on;
%flap1:
%flap chord at nif:
y=linspace(nif1/bc*((p*bc+q)-cc+cr)-cr,nif1/bc*((p*bc+q)-cc+cr)-cr+cf1,N);
p1=0; q1=nif1; 
x=p1*y+q1;
plot(x,y,'k-','linewidth',2);
%flap chord at nof:
y=linspace(nof1/bc*((p*bc+q)-cc+cr)-cr,nof1/bc*((p*bc+q)-cc+cr)-cr+cf1,N);
p1=0; q1=nof1; 
x=p1*y+q1;
plot(x,y,'k-','linewidth',2);
hold on;
%upper part of flap 1:
x=linspace(nif1,nof1,N);
y=(x-nif1)/(nof1-nif1)*((nof1/bc*((p*bc+q)-cc+cr)-cr)-(nif1/bc*((p*bc+q)-cc+cr)-cr))+(nif1/bc*((p*bc+q)-cc+cr)-cr)+cf1
plot(x,y,'k-','linewidth',2);
hold on;
%flap2:
%flap chord at nif:
y=linspace((nif2-bc)/(b/2-bc)*(((p*bc+q)+tan(-freccia2*pi/180)*(b/2-bc))-((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))+cc-ct)+((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))-cc,(nif2-bc)/(b/2-bc)*(((p*bc+q)+tan(-freccia2*pi/180)*(b/2-bc))-((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))+cc-ct)+((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))-cc+cf2,N);
p1=0; q1=nif2; 
x=p1*y+q1;
plot(x,y,'k-','linewidth',2);
%flap chord at nof:
y=linspace((nof2-bc)/(b/2-bc)*(((p*bc+q)+tan(-freccia2*pi/180)*(b/2-bc))-((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))+cc-ct)+((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))-cc,(nof2-bc)/(b/2-bc)*(((p*bc+q)+tan(-freccia2*pi/180)*(b/2-bc))-((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))+cc-ct)+((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))-cc+cf2,N);
p1=0; q1=nof2; 
x=p1*y+q1;
plot(x,y,'k-','linewidth',2);
hold on;
%upper part of flap 1:
x=linspace(nif2,nof2,N);
y=(x-nif2)/(nof2-nif2)*(((nof2-bc)/(b/2-bc)*(((p*bc+q)+tan(-freccia2*pi/180)*(b/2-bc))-((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))+cc-ct)+((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))-cc)-((nif2-bc)/(b/2-bc)*(((p*bc+q)+tan(-freccia2*pi/180)*(b/2-bc))-((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))+cc-ct)+((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))-cc))+(nif2-bc)/(b/2-bc)*(((p*bc+q)+tan(-freccia2*pi/180)*(b/2-bc))-((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))+cc-ct)+((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))-cc+cf2;
plot(x,y,'k-','linewidth',2);
hold on;
%slat 1:
slat(p,nis1,nos1,cs1,bc,freccia2);
hold on;
%slat2:
slat(p,nis2,nos2,cs2,bc,freccia2);
%slat 3:
slat(p,nis3,nos3,cs3,bc,freccia2);
hold on;
%slat 4:
slat(p,nis4,nos4,cs4,bc,freccia2);
hold on;

axis([-2 b/2+2 (b/2-bc)/(b/2-bc)*(((p*bc+q)+tan(-freccia2*pi/180)*(b/2-bc))-((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))+cc-ct)+((p*bc+q)+tan(-freccia2*pi/180)*(bc-bc))-cc-4 4]);
end