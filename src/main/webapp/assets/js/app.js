/*
 * CampusLoop client-side behaviour.
 *
 * Everything here is progressive enhancement driven by HTML attributes, so no
 * page needs inline JavaScript (the Content-Security-Policy forbids it).
 * The server re-validates every value; these checks only give faster feedback.
 *
 *   <form data-validate>                client-side validation before submit
 *     required / minlength / maxlength / type=email / pattern / min / max
 *     data-match="#otherId"             must equal another field (confirm password)
 *     data-password                     8-64 chars, at least one letter and one number
 *     data-money                        positive amount, max 2 decimals
 *     data-max-bytes="5242880"          file size limit (with accept="image/...")
 *     data-min-date / data-max-date     yyyy-mm-dd bounds for date inputs
 *     data-min-time / data-max-time     HH:mm bounds for time inputs
 *     data-msg-required="..."           custom message
 *   <form data-confirm="Are you sure?">  confirmation dialog
 *   <textarea maxlength data-counter>   live character counter (needs .counter sibling)
 *   <button data-toggle-password="#id"> show / hide password
 *   <select data-autosubmit>            submits its form on change
 *   .file-drop > input[type=file]       live image preview
 */
(function () {
    'use strict';

    var IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

    function $(sel, root) { return (root || document).querySelector(sel); }
    function $all(sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); }

    function formatMb(bytes) { return Math.round(bytes / (1024 * 1024)) + ' MB'; }

    /* ---------------- navigation ---------------- */
    function initNav() {
        var header = $('.site-header');
        var toggle = $('.nav-toggle');
        if (header && toggle) {
            toggle.addEventListener('click', function () {
                var open = header.classList.toggle('nav-open');
                toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
            });
        }
        document.addEventListener('click', function (e) {
            $all('details.user-menu[open]').forEach(function (menu) {
                if (!menu.contains(e.target)) { menu.removeAttribute('open'); }
            });
        });
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') {
                $all('details.user-menu[open]').forEach(function (menu) { menu.removeAttribute('open'); });
            }
        });
    }

    /* ---------------- alerts ---------------- */
    function initAlerts() {
        $all('.alert-close').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var alert = btn.closest('.alert');
                if (alert) { alert.parentNode.removeChild(alert); }
            });
        });
    }

    /* ---------------- field error helpers ---------------- */
    function fieldWrapper(input) { return input.closest('.field') || input.parentNode; }

    function setError(input, message) {
        var wrap = fieldWrapper(input);
        wrap.classList.add('has-error');
        input.setAttribute('aria-invalid', 'true');
        var el = $('.field-error[data-client]', wrap);
        if (!el) {
            $all('.field-error', wrap).forEach(function (old) { old.parentNode.removeChild(old); });
            el = document.createElement('p');
            el.className = 'field-error';
            el.setAttribute('data-client', '');
            el.setAttribute('role', 'alert');
            wrap.appendChild(el);
        }
        el.textContent = message;
    }

    function clearError(input) {
        var wrap = fieldWrapper(input);
        wrap.classList.remove('has-error');
        input.removeAttribute('aria-invalid');
        $all('.field-error', wrap).forEach(function (el) { el.parentNode.removeChild(el); });
    }

    function labelOf(input) {
        var wrap = fieldWrapper(input);
        var label = wrap ? $('.label', wrap) : null;
        var text = label ? label.childNodes[0].textContent : (input.name || 'This field');
        return text.replace(/\s+/g, ' ').trim();
    }

    /* ---------------- validation rules ---------------- */
    function validateInput(input, form) {
        if (input.disabled || input.type === 'hidden' || input.type === 'submit') { return null; }
        var value = input.type === 'file' ? '' : (input.value || '').trim();
        var name = labelOf(input);

        if (input.type === 'radio') {
            if (!input.required) { return null; }
            var group = $all('input[type=radio][name="' + input.name + '"]', form);
            var checked = group.some(function (r) { return r.checked; });
            return checked ? null : (input.getAttribute('data-msg-required') || 'Please choose an option.');
        }

        if (input.type === 'file') {
            var files = input.files;
            if (!files || files.length === 0) {
                return input.required ? (input.getAttribute('data-msg-required') || 'Please choose an image.') : null;
            }
            var file = files[0];
            if (IMAGE_TYPES.indexOf(file.type) === -1) {
                return 'Please choose a JPG, PNG or WebP image.';
            }
            var max = parseInt(input.getAttribute('data-max-bytes') || '0', 10);
            if (max > 0 && file.size > max) {
                return 'This image is ' + (file.size / (1024 * 1024)).toFixed(1) + ' MB. The limit is ' + formatMb(max) + '.';
            }
            if (file.size === 0) { return 'That file is empty. Please choose another image.'; }
            return null;
        }

        if (input.required && value === '') {
            return input.getAttribute('data-msg-required') || ('Please enter ' + name.toLowerCase() + '.');
        }
        if (value === '') { return null; }

        var min = input.getAttribute('minlength');
        if (min && value.length < parseInt(min, 10)) {
            return name + ' must be at least ' + min + ' characters.';
        }
        var maxLen = input.getAttribute('maxlength');
        if (maxLen && value.length > parseInt(maxLen, 10)) {
            return name + ' must be at most ' + maxLen + ' characters.';
        }
        if (input.type === 'email' && !/^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$/.test(value)) {
            return 'Please enter a valid email address.';
        }
        if (input.hasAttribute('data-password')) {
            if (value.length < 8) { return 'Password must be at least 8 characters.'; }
            if (value.length > 64) { return 'Password must be at most 64 characters.'; }
            if (!/[A-Za-z]/.test(value) || !/[0-9]/.test(value)) {
                return 'Password must contain at least one letter and one number.';
            }
        }
        if (input.hasAttribute('data-money')) {
            if (!/^\d{1,7}(\.\d{1,2})?$/.test(value.replace(/,/g, ''))) {
                return 'Enter an amount in rupees, e.g. 450 or 450.50.';
            }
            var amount = parseFloat(value.replace(/,/g, ''));
            var lo = input.getAttribute('min');
            var hi = input.getAttribute('max');
            if (lo !== null && amount < parseFloat(lo)) { return 'The minimum is \u20B9' + lo + '.'; }
            if (hi !== null && amount > parseFloat(hi)) { return 'The maximum is \u20B9' + Number(hi).toLocaleString('en-IN') + '.'; }
        }
        var pattern = input.getAttribute('pattern');
        if (pattern && !(new RegExp('^(?:' + pattern + ')$')).test(value)) {
            return input.getAttribute('data-msg-pattern') || ('Please check ' + name.toLowerCase() + '.');
        }
        var match = input.getAttribute('data-match');
        if (match) {
            var other = $(match, form);
            if (other && other.value !== input.value) {
                return input.getAttribute('data-msg-match') || 'The values do not match.';
            }
        }
        var minDate = input.getAttribute('data-min-date');
        var maxDate = input.getAttribute('data-max-date');
        if (minDate && value < minDate) { return 'Please choose today or a later date.'; }
        if (maxDate && value > maxDate) { return 'Please choose a date within the next 30 days.'; }
        var minTime = input.getAttribute('data-min-time');
        var maxTime = input.getAttribute('data-max-time');
        if ((minTime && value < minTime) || (maxTime && value > maxTime)) {
            return 'Please choose a time between ' + (minTime || '00:00') + ' and ' + (maxTime || '23:59') + '.';
        }
        return null;
    }

    function initValidation() {
        $all('form[data-validate]').forEach(function (form) {
            form.setAttribute('novalidate', 'novalidate');
            var inputs = $all('input, select, textarea', form);

            var submitting = false;
            form.addEventListener('pointerdown', function (e) {
                if (e.target.closest && e.target.closest('button[type=submit], input[type=submit]')) {
                    submitting = true;
                    window.setTimeout(function () { submitting = false; }, 600);
                }
            });

            inputs.forEach(function (input) {
                var evt = (input.type === 'file' || input.tagName === 'SELECT' || input.type === 'radio'
                    || input.type === 'checkbox') ? 'change' : 'blur';
                input.addEventListener(evt, function (e) {
                    // If focus is leaving because the student pressed Submit, let the submit
                    // handler validate everything; inserting an error now would shift the
                    // layout and make the click miss the button.
                    var next = e.relatedTarget;
                    if (submitting || (next && next.type === 'submit')) { return; }
                    var target = input.type === 'radio'
                        ? $('input[type=radio][name="' + input.name + '"]', form) : input;
                    window.setTimeout(function () {
                        if (submitting) { return; }
                        var msg = validateInput(target, form);
                        if (msg) { setError(target, msg); } else { clearError(target); }
                    }, 150);
                });
                input.addEventListener('input', function () {
                    if (fieldWrapper(input).classList.contains('has-error')) {
                        if (!validateInput(input, form)) { clearError(input); }
                    }
                });
            });

            form.addEventListener('submit', function (e) {
                var firstBad = null;
                var seenRadio = {};
                inputs.forEach(function (input) {
                    if (input.type === 'radio') {
                        if (seenRadio[input.name]) { return; }
                        seenRadio[input.name] = true;
                    }
                    var msg = validateInput(input, form);
                    if (msg) {
                        setError(input, msg);
                        if (!firstBad) { firstBad = input; }
                    } else if (!input.disabled && input.type !== 'hidden') {
                        clearError(input);
                    }
                });
                if (firstBad) {
                    e.preventDefault();
                    e.stopImmediatePropagation();
                    var focusTarget = firstBad.type === 'file' ? fieldWrapper(firstBad) : firstBad;
                    if (focusTarget.scrollIntoView) { focusTarget.scrollIntoView({ block: 'center' }); }
                    if (firstBad.focus) { firstBad.focus({ preventScroll: true }); }
                }
            }, false);
        });
    }

    /* ---------------- confirm + double-submit protection ---------------- */
    function initSubmitGuards() {
        $all('form').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                if (e.defaultPrevented) { return; }
                var question = form.getAttribute('data-confirm');
                if (question && !window.confirm(question)) {
                    e.preventDefault();
                    return;
                }
                if (form.method && form.method.toLowerCase() === 'post') {
                    $all('button[type=submit], input[type=submit]', form).forEach(function (btn) {
                        window.setTimeout(function () {
                            btn.disabled = true;
                            var busy = btn.getAttribute('data-busy');
                            if (busy) { btn.textContent = busy; }
                        }, 0);
                    });
                }
            });
        });
        // Re-enable buttons if the page is restored from the back/forward cache.
        window.addEventListener('pageshow', function (e) {
            if (e.persisted) {
                $all('button[type=submit][disabled], input[type=submit][disabled]').forEach(function (b) {
                    b.disabled = false;
                });
            }
        });
    }

    /* ---------------- image upload preview ---------------- */
    function initFileDrops() {
        $all('.file-drop').forEach(function (drop) {
            var input = $('input[type=file]', drop);
            var preview = $('.file-preview', drop);
            var nameEl = $('.file-name', drop.parentNode);
            if (!input) { return; }
            var objectUrl = null;

            input.addEventListener('change', function () {
                if (objectUrl) { URL.revokeObjectURL(objectUrl); objectUrl = null; }
                var file = input.files && input.files[0];
                var ok = file && IMAGE_TYPES.indexOf(file.type) !== -1;
                if (file && ok && preview && window.URL) {
                    objectUrl = URL.createObjectURL(file);
                    preview.src = objectUrl;
                    drop.classList.add('has-file');
                } else if (!drop.hasAttribute('data-has-existing')) {
                    drop.classList.remove('has-file');
                    if (preview) { preview.removeAttribute('src'); }
                }
                if (nameEl) {
                    nameEl.textContent = file ? file.name + ' \u00B7 ' + (file.size / 1024 < 1024
                        ? Math.max(1, Math.round(file.size / 1024)) + ' KB'
                        : (file.size / (1024 * 1024)).toFixed(1) + ' MB') : '';
                }
            });
            ['dragenter', 'dragover'].forEach(function (evt) {
                drop.addEventListener(evt, function () { drop.classList.add('dragover'); });
            });
            ['dragleave', 'drop'].forEach(function (evt) {
                drop.addEventListener(evt, function () { drop.classList.remove('dragover'); });
            });
        });
    }

    /* ---------------- counters, password toggles, autosubmit ---------------- */
    function initCounters() {
        $all('[data-counter]').forEach(function (field) {
            var max = parseInt(field.getAttribute('maxlength') || '0', 10);
            var wrap = fieldWrapper(field);
            var counter = $('.counter', wrap);
            if (!counter || !max) { return; }
            var update = function () {
                var n = field.value.length;
                counter.textContent = n + ' / ' + max;
                counter.classList.toggle('over', n > max);
            };
            field.addEventListener('input', update);
            update();
        });
    }

    function initPasswordToggles() {
        $all('[data-toggle-password]').forEach(function (btn) {
            var target = $(btn.getAttribute('data-toggle-password'));
            if (!target) { return; }
            btn.addEventListener('click', function () {
                var show = target.type === 'password';
                target.type = show ? 'text' : 'password';
                btn.textContent = show ? 'Hide' : 'Show';
                btn.setAttribute('aria-pressed', show ? 'true' : 'false');
            });
        });
    }

    function initAutosubmit() {
        $all('select[data-autosubmit]').forEach(function (select) {
            select.addEventListener('change', function () {
                if (select.form) { select.form.submit(); }
            });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        initNav();
        initAlerts();
        initValidation();
        initSubmitGuards();
        initFileDrops();
        initCounters();
        initPasswordToggles();
        initAutosubmit();
    });
})();
