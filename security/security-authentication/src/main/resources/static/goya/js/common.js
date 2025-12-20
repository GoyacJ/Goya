$.http = {
    request: function (url, body, method, type, dataType) {
        let data = body;
        let contentType = "application/x-www-form-urlencoded";
        if (type && type.toLowerCase() === 'json') {
            contentType = 'application/json';
            if (method !== 'GET') {
                data = JSON.stringify(body);
            }
        }

        return new Promise((resolve, reject) => {
            $.ajax({
                url: url,
                data: data,
                type: method,
                contentType: contentType,
                dataType: dataType || "json",
                async: true,
                success: function (result) {
                    resolve(result);
                },
                error: function (error) {
                    reject(error);
                }
            });
        });
    },
    get: function (url, params = {}) {
        let that = this;
        return that.request(url, params, 'GET');
    },
    put: function (url, data, type) {
        let that = this;
        return that.request(url, data, 'PUT', type);
    },
    post: function (url, data = {}, type) {
        return this.request(url, data, 'POST', type);
    },
    delete: function (url, data = {}, type) {
        let that = this;
        return that.request(url, data, 'DELETE', type);
    },
    postWithNoResposeBody: function(url, data = {}) {
        let that = this;
        return that.request(url, data, 'POST', 'json', 'text');
    }
};

$.information = {
    configuration: function (colorName, text, placementFrom, placementAlign, animateEnter, animateExit) {
        if (colorName === null || colorName === '') {
            colorName = 'bg-black';
        }
        if (text === null || text === '') {
            text = 'Turning standard Bootstrap alerts';
        }
        if (animateEnter === null || animateEnter === '') {
            animateEnter = 'animated fadeInDown';
        }
        if (animateExit === null || animateExit === '') {
            animateExit = 'animated fadeOutUp';
        }
        let allowDismiss = true;

        $.notify(
            {
                message: text
            },
            {
                type: colorName,
                allow_dismiss: allowDismiss,
                newest_on_top: true,
                timer: 1000,
                placement: {
                    from: placementFrom,
                    align: placementAlign
                },
                animate: {
                    enter: animateEnter,
                    exit: animateExit
                },
                template: '<div data-notify="container" class="bootstrap-notify-container alert alert-dismissible {0} ' + (allowDismiss ? "p-r-35" : "") + '" role="alert">' +
                    '<button type="button" aria-hidden="true" class="close" data-notify="dismiss">×</button>' +
                    '<span data-notify="icon"></span> ' +
                    '<span data-notify="title">{1}</span> ' +
                    '<span data-notify="message">{2}</span>' +
                    '<div class="progress" data-notify="progressbar">' +
                    '<div class="progress-bar progress-bar-{0}" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;"></div>' +
                    '</div>' +
                    '<a href="{3}" target="{4}" data-notify="url"></a>' +
                    '</div>'
            });
    },
    success: function (message) {
        this.configuration('alert-success', message, 'top', 'center', 'animated fadeInDown', 'animated fadeOutUp')
    },
    warning: function (message) {
        this.configuration('alert-warning', message, 'top', 'center', 'animated fadeInDown', 'animated fadeOutUp')
    },
    error: function (message) {
        this.configuration('alert-danger', message, 'top', 'center', 'animated fadeInDown', 'animated fadeOutUp')
    },
    info: function (message) {
        this.configuration('alert-info', message, 'top', 'center', 'animated fadeInDown', 'animated fadeOutUp')
    },
    notify: function (type, message) {
        switch (type) {
            case 'success':
                this.success(message);
                break;
            case 'warning':
                this.warning(message);
                break;
            case 'error':
                this.error(message);
                break;
            case 'info':
                this.info(message);
                break;
            default:
                this.info(message);
        }
    }
};