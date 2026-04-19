const BASE = 'http://localhost:8080';
const params = new URLSearchParams(window.location.search);
const shipmentId = params.get('id');
let map = null, droneMarker = null, trackInterval = null;
const mapPlaceholder = document.getElementById('map-placeholder');
mapPlaceholder.style.display = 'flex';

document.getElementById('shipmentId').textContent = shipmentId ? 'Codice spedizione: ' + shipmentId : 'ID non trovato';

const statusLabels = {
    SCHEDULED: 'In attesa',
    IN_PROGRESS: 'In consegna',
    COMPLETED: 'Completata',
    CANCELLED: 'Annullata'
};

const statusClasses = {
    SCHEDULED: 'badge-scheduled',
    IN_PROGRESS: 'badge-in_progress',
    COMPLETED: 'badge-completed',
    CANCELLED: 'badge-cancelled'
};

function initMap(lat, lon) {
    document.getElementById('map').style.display = 'block';
    mapPlaceholder.style.display = 'none';

    map = L.map('map').setView([lat, lon], 14);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {attribution: '© OpenStreetMap'}).addTo(map);

    const droneIcon = L.divIcon({
        html: '<div style="width:16px;height:16px;border-radius:50%;background:#378ADD;border:2.5px solid #fff"></div>',
        className: '',
        iconAnchor: [8, 8]
    });
    droneMarker = L.marker([lat, lon], {icon: droneIcon}).addTo(map).bindPopup('Drone');
}

async function update() {
    if (!shipmentId) return;
    try {
        const [sRes, pRes, tRes] = await Promise.all([
            fetch(BASE + '/shipments/' + shipmentId + '/status'),
            fetch(BASE + '/shipments/' + shipmentId + '/position'),
            fetch(BASE + '/shipments/' + shipmentId + '/remaining-time')
        ]);

        if (sRes.status === 503 || pRes.status === 503 || tRes.status === 503) {
            document.getElementById('updateInfo').innerHTML = '<div class="msg msg-error">Servizio temporaneamente non disponibile</div>';
            return;
        } else if (!sRes.ok && sRes.status !== 404) {
            document.getElementById('updateInfo').innerHTML = `<div class="msg msg-error">Errore nel recupero delle informazioni (${sRes.status})</div>`;
            return;
        }

        const status = await sRes.json();
        const label = statusLabels[status.status] || status.status;
        const cls = statusClasses[status.status] || '';

        document.getElementById('statStatus').innerHTML = `<span class="badge ${cls}">${label}</span>`;

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

        if (tRes.ok) {
            const t = await tRes.json();
            document.getElementById('statTime').textContent = t.remainingMinutes + ' min';
        }

        document.getElementById('updateInfo').textContent = 'Aggiornato alle ' + new Date().toLocaleTimeString();

        if (status.status === 'COMPLETED' || status.status === 'CANCELLED') {
            stopTracking();
        }

    } catch (e) {
        document.getElementById('updateInfo').textContent = '';
        mapPlaceholder.style.display = 'flex';    }
}

function stopTracking() {
    if (trackInterval) {
        clearInterval(trackInterval);
        trackInterval = null;
    }
}

if (shipmentId) {
    update();
    trackInterval = setInterval(update, 2000);
}
