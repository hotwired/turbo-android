(() => {
    function TLWebView(controller) {
        this.controller = controller
        controller.adapter = this

        var isReady = typeof Turbolinks !== "undefined" && Turbolinks !== null
        TurboSession.turboIsReady(isReady)
    }

    TLWebView.prototype = {
        // TurboSession calls this as the starting point

        visitLocationWithOptionsAndRestorationIdentifier: function(location, options, restorationIdentifier) {
            if (this.controller.startVisitToLocation) {
                this.controller.startVisitToLocation(location, restorationIdentifier, JSON.parse(options))
            } else {
                // Temporarily support old API
                this.controller.startVisitToLocationWithAction(location, JSON.parse(options).action, restorationIdentifier)
            }
        },

        // Functions available to TurboSession to call directly into Turbo Core

        issueRequestForVisitWithIdentifier: function(identifier) {
            if (identifier == this.currentVisit.identifier) {
                this.currentVisit.issueRequest()
            }
        },

        changeHistoryForVisitWithIdentifier: function(identifier) {
            if (identifier == this.currentVisit.identifier) {
                this.currentVisit.changeHistory()
            }
        },

        loadCachedSnapshotForVisitWithIdentifier: function(identifier) {
            if (identifier == this.currentVisit.identifier) {
                this.currentVisit.loadCachedSnapshot()
            }
        },

        loadResponseForVisitWithIdentifier: function(identifier) {
            if (identifier == this.currentVisit.identifier) {
                this.currentVisit.loadResponse()
            }
        },

        cancelVisitWithIdentifier: function(identifier) {
            if (identifier == this.currentVisit.identifier) {
                this.currentVisit.cancel()
            }
        },

        visitRenderedForColdBoot: function(visitIdentifier) {
            this.afterNextRepaint(function() {
                TurboSession.visitRendered(visitIdentifier)
            })
        },

        // Callbacks to TurboSession from Turbo Core

        pageLoaded: function() {
            var restorationIdentifier = this.controller.restorationIdentifier
            this.afterNextRepaint(function() {
                TurboSession.pageLoaded(restorationIdentifier)
            })
        },

        // Temporary adapter for new API
        visitProposedToLocationWithAction: function(location, action) {
            this.visitProposedToLocation(location, { action })
        },

        visitProposedToLocation: function(location, options) {
            TurboSession.visitProposedToLocation(location.absoluteURL, JSON.stringify(options))
        },

        visitStarted: function(visit) {
            TurboSession.visitStarted(visit.identifier, visit.hasCachedSnapshot(), visit.location.absoluteURL)
            this.currentVisit = visit
            this.issueRequestForVisitWithIdentifier(visit.identifier)
            this.changeHistoryForVisitWithIdentifier(visit.identifier)
            this.loadCachedSnapshotForVisitWithIdentifier(visit.identifier)
        },

        visitRequestStarted: function(visit) {
            // Purposely left unimplemented. visitStarted covers most cases.
        },

        visitRequestCompleted: function(visit) {
            TurboSession.visitRequestCompleted(visit.identifier)
            this.loadResponseForVisitWithIdentifier(visit.identifier)
        },

        visitRequestFailedWithStatusCode: function(visit, statusCode) {
            TurboSession.visitRequestFailedWithStatusCode(visit.identifier, visit.hasCachedSnapshot(), statusCode)
        },

        visitRequestFinished: function(visit) {
            TurboSession.visitRequestFinished(visit.identifier)
        },

        visitRendered: function(visit) {
            this.afterNextRepaint(function() {
                TurboSession.visitRendered(visit.identifier)
            })
        },

        visitCompleted: function(visit) {
            this.afterNextRepaint(function() {
                TurboSession.visitCompleted(visit.identifier, visit.restorationIdentifier)
            })
        },

        pageInvalidated: function() {
            TurboSession.pageInvalidated()
        },

        // Private

        afterNextRepaint: function(callback) {
            if (document.hidden) {
                callback()
            } else {
                requestAnimationFrame(function() {
                    requestAnimationFrame(callback)
                })
            }
        }
    }

    try {
        window.webView = new TLWebView(Turbolinks.controller)
        window.webView.pageLoaded()
    } catch (e) {
        // Most likely reached a page where Turbolinks.controller returned
        // "Uncaught ReferenceError: Turbolinks is not defined"
        TurboSession.turboFailedToLoad()
    }
})()
