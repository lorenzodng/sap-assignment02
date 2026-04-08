//costanti

const BASE = 'http://localhost:8080'; //url api-gateway
const params = new URLSearchParams(window.location.search);
const shipmentId = params.get('id'); //legge i parametri dell'url della pagina (l'id della spedizione)
let map = null, droneMarker = null, trackInterval = null; //variabili per il tracking
const mapPlaceholder = document.getElementById('map-placeholder');
mapPlaceholder.style.display = 'flex'; // visibile all'inizio

document.getElementById('shipmentId').textContent = shipmentId ? 'Codice spedizione: ' + shipmentId : 'ID non trovato';

//stato in italiano
const statusLabels = {
    SCHEDULED: 'In attesa',
    IN_PROGRESS: 'In consegna',
    COMPLETED: 'Completata',
    CANCELLED: 'Annullata'
};

//classi corrispondenti agli stati
const statusClasses = {
    SCHEDULED: 'badge-scheduled',
    IN_PROGRESS: 'badge-in_progress',
    COMPLETED: 'badge-completed',
    CANCELLED: 'badge-cancelled'
};

//inizializza la mappa Leaflet (è una libreria usata per mostrare la mappa)
function initMap(lat, lon) {
    document.getElementById('map').style.display = 'block';
    mapPlaceholder.style.display = 'none';

    map = L.map('map').setView([lat, lon], 14);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {attribution: '© OpenStreetMap'}).addTo(map);

    //mostra il marker del drone sulla mappa
    const droneIcon = L.divIcon({
        html: '<div style="width:16px;height:16px;border-radius:50%;background:#378ADD;border:2.5px solid #fff"></div>',
        className: '',
        iconAnchor: [8, 8]
    });
    droneMarker = L.marker([lat, lon], {icon: droneIcon}).addTo(map).bindPopup('Drone');
}

//funzione di aggiornamento dello stato, della posizione del drone sulla mappa, del tempo rimanente e dell'orario dell'ultimo aggiornamento
async function update() {
    if (!shipmentId) return;
    try {
        //esegue le tre chiamate in parallelo e attende che siano completate
        const [sRes, pRes, tRes] = await Promise.all([
            fetch(BASE + '/shipments/' + shipmentId + '/status'),
            fetch(BASE + '/shipments/' + shipmentId + '/position'),
            fetch(BASE + '/shipments/' + shipmentId + '/remaining-time')
        ]);

        //crea l'etichetta di stato in italiano
        const status = await sRes.json();
        const label = statusLabels[status.status] || status.status;
        const cls = statusClasses[status.status] || '';

        //aggiorna lo stato con il colore appropriato
        document.getElementById('statStatus').innerHTML = `<span class="badge ${cls}">${label}</span>`;

        //aggiorna la posizione del drone sulla mappa
        if (pRes.ok) {
            const pos = await pRes.json();
            document.getElementById('statPos').textContent = pos.latitude.toFixed(4) + ', ' + pos.longitude.toFixed(4);
            if (!map) {
                initMap(pos.latitude, pos.longitude);
            } else {
                droneMarker.setLatLng([pos.latitude, pos.longitude]);
                map.panTo([pos.latitude, pos.longitude]);
            }
            mapPlaceholder.style.display = 'none';
        } else {
            mapPlaceholder.style.display = 'flex';
        }

        //aggiorna il tempo rimanente
        if (tRes.ok) {
            const t = await tRes.json();
            document.getElementById('statTime').textContent = t.remainingMinutes + ' min';
        }

        document.getElementById('updateInfo').textContent = 'Aggiornato alle ' + new Date().toLocaleTimeString();

        //termina la spedizione se completata o cancellata
        if (status.status === 'COMPLETED' || status.status === 'CANCELLED') {
            stopTracking();
        }

    } catch (e) {
        document.getElementById('updateInfo').textContent = '';
        mapPlaceholder.style.display = 'flex';    }
}

//ferma l'aggiornamento
function stopTracking() {
    if (trackInterval) {
        clearInterval(trackInterval);
        trackInterval = null;
    }
}

//avvia l'aggiornamento
if (shipmentId) {
    update();
    trackInterval = setInterval(update, 2000);
}
