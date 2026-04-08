//costanti

const BASE = 'http://localhost:8080'; //url api-gateway
const ids = ['userName', 'userSurname', 'pickupAddress', 'pickupDate', 'pickupTime', 'deliveryAddress', 'deliveryTimeLimit', 'weight', 'fragile'];

//verifiche che tutti i campi non sono vuoti e abilita il bottone
function validate() {
    const ok = ids.every(id => document.getElementById(id).value.trim() !== '');
    document.getElementById('submitBtn').disabled = !ok;
}

//chiama l'API di "Nominatim" (un servizio geografico) passando l'indirizzo come parametro
async function geocode(address) {
    const res = await fetch(`https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(address)}&format=json&limit=1`);
    const data = await res.json();
    if (!data.length) throw new Error('Indirizzo non trovato: ' + address);
    return {latitude: parseFloat(data[0].lat), longitude: parseFloat(data[0].lon)};
}

//invia il form
async function submitForm() {
    const btn = document.getElementById('submitBtn');
    btn.disabled = true;
    const el = document.getElementById('formMsg');
    try {
        const [pickup, delivery] = await Promise.all([geocode(v('pickupAddress')), geocode(v('deliveryAddress'))]); //esegue le chiamate a Nominatim

        //costruisce il messaggio da inviare

        const body = {
            userId: crypto.randomUUID(),
            userName: v('userName'),
            userSurname: v('userSurname'),
            pickupDate: v('pickupDate'),
            pickupTime: v('pickupTime'),
            deliveryTimeLimit: parseInt(v('deliveryTimeLimit')),
            pickupLocation: pickup,
            deliveryLocation: delivery,
            package: {
                weight: parseFloat(v('weight')),
                fragile: v('fragile') === 'true'
            }
        };

        const res = await fetch(BASE + '/shipments', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body)
        });

        //recupera la risposta

        const text = await res.text();
        if (res.status === 201) {
            el.innerHTML = `<div class="msg msg-success">Richiesta inviata con successo<div>`;
            setTimeout(() => {
                window.location.href = 'tracking.html?id=' + text; //passa alla pagina del tracking
            }, 1000);
        } else if (res.status === 400) {
            el.innerHTML = `<div class="msg msg-error">Dati non validi. Controlla i campi e riprova</div>`;
        } else if (res.status === 503) {
            el.innerHTML = `<div class="msg msg-error">Nessun drone disponibile per questa spedizione</div>`;
        } else {
            el.innerHTML = `<div class="msg msg-error">Errore nell'invio della richiesta (${res.status})</div>`;
        }
    } catch (e) {
        el.innerHTML = `<div class="msg msg-error">${e.message.includes('Indirizzo') ? e.message : 'Errore di connessione'}</div>`;
        btn.disabled = false;
        btn.textContent = 'Richiedi drone';
    }
}

//rimuove eventuali spazi iniziali e finali
function v(id) {
    return document.getElementById(id).value.trim();
}

//costruisce la data a partire da quella inserita
const today = new Date();
document.getElementById('pickupDate').value = today.getFullYear() + '-' + String(today.getMonth() + 1).padStart(2, '0') + '-' + String(today.getDate()).padStart(2, '0');

//costruisce l'orario a partitre da quello inserito
document.getElementById('pickupTime').value = new Date().toLocaleTimeString('it-IT', {
    hour: '2-digit',
    minute: '2-digit'
});
