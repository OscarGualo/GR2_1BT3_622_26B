const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { resolve } = require('node:path');
const vm = require('node:vm');

const jsp = readFileSync(resolve(__dirname, '../../main/webapp/vistas/cliente/carrito.jsp'), 'utf8');
const script = jsp.match(/<script>([\s\S]*?)<\/script>/)[1];

// Ejecuta el JavaScript real del carrito con controles de formulario simulados.
function carrito(saldoSuficiente, admiteDialogo = true) {
    let modo = 'ahora';
    let enviados = 0;
    let abiertos = 0;
    function elemento(atributos = {}) {
        const eventos = {};
        return {
            disabled: false,
            addEventListener(tipo, accion) { (eventos[tipo] ??= []).push(accion); },
            getAttribute(nombre) { return atributos[nombre]; },
            focus() {},
            emitir(tipo) {
                const evento = { preventDefault() { this.defaultPrevented = true; } };
                for (const accion of eventos[tipo] ?? []) accion(evento);
                return evento;
            }
        };
    }
    const ids = {};
    for (const id of ['form-pagar', 'dialogo-pago', 'boton-pagar', 'boton-pagar-texto',
        'franjas-retiro', 'dialogo-pago-confirmar', 'dialogo-pago-titulo',
        'dialogo-pago-texto', 'dialogo-pago-cancelar']) ids[id] = elemento();
    ids['boton-pagar'] = elemento({
        'data-total': '5.00', 'data-saldo-suficiente': String(saldoSuficiente)
    });
    const modos = [elemento(), elemento()];
    const form = ids['form-pagar'];
    form.querySelectorAll = () => modos;
    form.querySelector = selector => selector.includes('modo')
        ? { value: modo } : elemento({ 'data-etiqueta': '13:00–13:15' });
    form.submit = () => enviados++;
    const dialogo = ids['dialogo-pago'];
    if (admiteDialogo) dialogo.showModal = () => abiertos++;
    dialogo.close = () => dialogo.emitir('close');
    vm.runInNewContext(script, { document: { getElementById: id => ids[id] } });
    return {
        ids,
        cambiarModo(valor) { modo = valor; modos[0].emitir('change'); },
        enviar() { return form.emitir('submit'); },
        get enviados() { return enviados; },
        get abiertos() { return abiertos; }
    };
}

test('HU07: saldo insuficiente bloquea pago inmediato incluso ante submit programático', () => {
    const pagina = carrito(false);
    assert.equal(pagina.ids['boton-pagar'].disabled, true);
    assert.equal(pagina.enviar().defaultPrevented, true);
    assert.equal(pagina.abiertos, 0);
    assert.equal(pagina.enviados, 0);
});

test('HU11: programar sigue enviando la solicitud para validar saldo en el servidor', () => {
    const pagina = carrito(false);
    pagina.cambiarModo('programar');
    assert.equal(pagina.ids['boton-pagar'].disabled, false);
    pagina.enviar();
    assert.equal(pagina.abiertos, 1);
    pagina.ids['dialogo-pago-confirmar'].emitir('click');
    assert.equal(pagina.enviados, 1);
});

test('volver al pago inmediato restablece el bloqueo con saldo insuficiente', () => {
    const pagina = carrito(false);
    pagina.cambiarModo('programar');
    pagina.cambiarModo('ahora');
    assert.equal(pagina.ids['boton-pagar'].disabled, true);
    pagina.enviar();
    assert.equal(pagina.abiertos, 0);
});

test('saldo suficiente o exacto permite confirmar el pago', () => {
    const pagina = carrito(true);
    assert.equal(pagina.ids['boton-pagar'].disabled, false);
    pagina.enviar();
    assert.equal(pagina.abiertos, 1);
    pagina.ids['dialogo-pago-confirmar'].emitir('click');
    assert.equal(pagina.enviados, 1);
});

test('cancelar la confirmación no envía el pago', () => {
    const pagina = carrito(true);
    pagina.enviar();
    pagina.ids['dialogo-pago-cancelar'].emitir('click');
    assert.equal(pagina.enviados, 0);
});

test('sin soporte de dialog se mantiene el bloqueo del pago inmediato sin saldo', () => {
    const pagina = carrito(false, false);
    assert.equal(pagina.ids['boton-pagar'].disabled, true);
    assert.equal(pagina.enviar().defaultPrevented, true);
    assert.equal(pagina.enviados, 0);
});
