% Function per il calcolo delle matrici necessarie all'interpolazione dei valori tabulari
% Tmax(M) e Tmax(h) al fine di ottenere un'unica funzione interpolata Tmax(M,h).
% La spinta è trattata in kgf

function thrust = interpTgivenM_h(machOrig, thrustOrig, altitudeOrig, nMach, machMax, altitudeCurrent, graph, p)

global meshmatrix

if nargin==4
    p=0.999999999;
end

graph = 0;

% infittimento
v_M = linspace( ...
    0, ... % Mach minimo
    machMax, ... % Mach max
    nMach);

v_h = altitudeOrig;

for i=1:numel(v_h)
    
    % Spline interpolante i valori di Tmax alla quota i-esima in funzione di M
    pp_T_mach{i,2} = csaps(machOrig, thrustOrig(i,:), p);
    
    % Valutazione della spline in corrispondenza dei punti v_M
    meshmatrix.m_Tmax(i,:) = ppval(pp_T_mach{i,2}, v_M); % Tmax
    
    % Plot di verifica acquisizione dati
    if graph==1
        figure(97)
        hold on;
        plot((0:0.01:machMax),ppval(pp_T_mach{i,2},(0:0.01:machMax)),'k-');
        xlabel('M');
        ylabel('T/T_0');
    end
end

[meshmatrix.m_M, meshmatrix.m_h] = meshgrid(v_M, v_h);

% Preparazione delle variabili
% valori in cui si vuole conoscere la funzione di (M,h)
v_h_I = linspace(v_h(1), v_h(end), nMach); % infittimenti
[m_M_I, m_h_I] = meshgrid(v_M, v_h_I);

% Chiamata alla funzione interpolante

if graph==1
    Tmax_M_h_graph = interp2( ...
        meshmatrix.m_M, meshmatrix.m_h, ...       % nodi di supporto
        meshmatrix.m_Tmax, ...                    % valori tabulati
        m_M_I, m_h_I, ...                         % vedi chiamata a meshgrid
        'spline');
    
    % Disegno della superficie
    figure(99)
    mesh(m_M_I, m_h_I*10^-3, Tmax_M_h_graph*9.81)
    xlabel('M');
    ylabel('h (ft)');
    zlabel('T/T_0')
    view(50,10);
    
    figure(98)
    hold on;
    for i=1:numel(v_h)
    plot(v_h_I, Tmax_M_h_graph(:,i));
    end
end

thrust = interp2( ...
        meshmatrix.m_M, meshmatrix.m_h, ...       
        meshmatrix.m_Tmax, ...                   
        machOrig, altitudeCurrent, ...                         
        'spline');

drawnow;

end