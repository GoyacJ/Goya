
$.sm2 = {
    cipherMode: 1,
    createKeyPair: () => {
        return SmCryptoV2.sm2.generateKeyPairHex();
    },

    encrypt: (content, publicKey) => {
        return '04' + SmCryptoV2.sm2.doEncrypt(content, publicKey, this.cipherMode);
    },

    decrypt: (content, privateKey) => {
        let data = content.substring(2).toLocaleLowerCase();
        return SmCryptoV2.sm2.doDecrypt(data, privateKey, this.cipherMode, { output: 'string' });
    }
}

$.sm4 = {
    encrypt: (content, publicKey) => {
        return SmCryptoV2.sm4.encrypt(content, publicKey, { output: 'string' });
    },

    decrypt: (content, privateKey) => {
        return SmCryptoV2.sm4.decrypt(content, privateKey, { output: 'string' });
    }
}

$.security = {
    exchange: (url, sessionId, backendPublicKey) => {
        const pair = $.sm2.createKeyPair();
        const encryptData = $.sm2.encrypt(pair.publicKey, backendPublicKey);

        return new Promise((resolve, reject) => {
            $.http.post(url, {publicKey: encryptData, sessionId: sessionId}, "json")
                .then(result => {
                    console.log("------exchange data------", result)
                    const confidential = result.data;
                    const key = $.sm2.decrypt(confidential, pair.privateKey);
                    console.log("------key------", key)
                    resolve(key);
                })
                .catch(error => {
                    reject(error);
                });
        })
    },

    captcha: (url, sessionId, category) => {
        return new Promise((resolve, reject) => {
            $.http.get(url, {identity:sessionId, category: category})
                .then(result => {
                    let src = result.data.graphicImageBase64;
                    resolve(src)
                })
                .catch(error => {
                    reject(error);
                });
        })
    },

    encryptSignInFormData: function (username, password, missile, symmetric) {
        return {
            encryptUsername: $.sm4.encrypt(username, symmetric),
            encryptPassword: $.sm4.encrypt(password, symmetric),
            encryptMissile: $.sm4.encrypt(missile, symmetric)
        };
    }
}