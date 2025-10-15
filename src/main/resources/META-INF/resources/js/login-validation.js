// Client-side password validation and live indicators for login/create dialogs
(function(){
    function validatePasswordAndConfirm(newPwClientId, confirmPwClientId) {
        try {
            var newPwEl = document.getElementById(newPwClientId) || document.getElementById(newPwClientId + '_input');
            var confirmPwEl = document.getElementById(confirmPwClientId) || document.getElementById(confirmPwClientId + '_input');

            if (!newPwEl) {
                newPwEl = document.querySelector("[id$='new-password']") || document.querySelector("[id$='new-password_input']");
            }
            if (!confirmPwEl) {
                confirmPwEl = document.querySelector("[id$='confirm-password']") || document.querySelector("[id$='confirm-password_input']");
            }

            var newPw = newPwEl ? newPwEl.value : '';
            var confirmPw = confirmPwEl ? confirmPwEl.value : '';

            var reUpper = /[A-Z]/;
            var reLower = /[a-z]/;
            var reDigit = /[0-9]/;
            var reSpecial = /[^A-Za-z0-9]/;

            var okLength = newPw.length >= 8;
            var okUpper = reUpper.test(newPw);
            var okLower = reLower.test(newPw);
            var okDigit = reDigit.test(newPw);
            var okSpecial = reSpecial.test(newPw);
            var okMatch = (newPw === confirmPw) && newPw.length > 0;

            updateReqIndicator('req-length', okLength);
            updateReqIndicator('req-upper', okUpper);
            updateReqIndicator('req-lower', okLower);
            updateReqIndicator('req-digit', okDigit);
            updateReqIndicator('req-special', okSpecial);

            var matchEl = document.getElementById('req-match');
            if (matchEl) {
                updateReqIndicator('req-match', okMatch);
            }

            return okLength && okUpper && okLower && okDigit && okSpecial && okMatch;
        } catch (e) {
            return true;
        }
    }

    // Live validation: attach listeners to password fields when dialog opens
    function attachLiveValidation() {
        var newPw = document.querySelector("[id$='new-password']") || document.querySelector("[id$='new-password_input']");
        var confirmPw = document.querySelector("[id$='confirm-password']") || document.querySelector("[id$='confirm-password_input']");
        if (!newPw || !confirmPw) return;

        function handler() {
            validatePasswordAndConfirm(newPw.id, confirmPw.id);
        }
        if (!newPw.__listenerAttached) { newPw.addEventListener('input', handler); newPw.__listenerAttached = true; }
        if (!confirmPw.__listenerAttached) { confirmPw.addEventListener('input', handler); confirmPw.__listenerAttached = true; }
    }

    function updateReqIndicator(id, valid) {
        var el = document.getElementById(id);
        if (!el) return;
        if (valid) {
            el.classList.remove('invalid');
            el.classList.add('valid');
        } else {
            el.classList.remove('valid');
            el.classList.add('invalid');
        }
    }

    var attachInterval = setInterval(function(){
        attachLiveValidation();
    }, 500);
    setTimeout(function(){ clearInterval(attachInterval); }, 30000);

    function attachCreatePwValidation() {
        try {
            // Find the create-account panel and then the real input inside it (PrimeFaces may render _input)
            var panel = document.querySelector("[id$='create-account-content']");
            if (!panel) return;
            var pwEl = panel.querySelector("input[id$='password'], input[id$='password_input'], input[type='password']");
            if (!pwEl) return;
            // Avoid attaching multiple listeners
            if (pwEl.__createPwListenerAttached) return;
            pwEl.__createPwListenerAttached = true;
            console.log('attachCreatePwValidation: attached to', pwEl.id || pwEl);
            pwEl.addEventListener('input', function(){
                try {
                    var val = pwEl.value || '';
                    var reUpper = /[A-Z]/;
                    var reLower = /[a-z]/;
                    var reDigit = /[0-9]/;
                    var reSpecial = /[^A-Za-z0-9]/;
                    updateReqIndicator('create-req-length', val.length >= 8);
                    updateReqIndicator('create-req-upper', reUpper.test(val));
                    updateReqIndicator('create-req-lower', reLower.test(val));
                    updateReqIndicator('create-req-digit', reDigit.test(val));
                    updateReqIndicator('create-req-special', reSpecial.test(val));
                } catch(e){}
            });
        } catch(e) {}
    }
    var attachCreateInterval = setInterval(function(){ attachCreatePwValidation(); }, 500);
    setTimeout(function(){ clearInterval(attachCreateInterval); }, 30000);

    // Expose validation function for inline use if needed
    window.validatePasswordAndConfirm = validatePasswordAndConfirm;
})();
