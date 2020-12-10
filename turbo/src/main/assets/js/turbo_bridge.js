(() => {
    function TLWebView(controller) {
        this.controller = controller
        controller.adapter = this

        if (window.Turbo) {
            var isReady = typeof Turbo !== "undefined" && Turbo !== null
            TurboSession.turboIsReady(isReady)
        } else {
            // Turbolinks 5
            var isReady = typeof Turbolinks !== "undefined" && Turbolinks !== null
            TurboSession.turboIsReady(isReady)
        }
    }

    TLWebView.prototype = {
        // TurboSession calls this as the starting point

        visitLocationWithOptionsAndRestorationIdentifier: function(location, options, restorationIdentifier) {
            if (this.controller.startVisitToLocation) {
                this.controller.startVisitToLocation(location, restorationIdentifier, JSON.parse(options))
            } else {
                // Turbolinks 5
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

        // Turbolinks 5 compatibility

        visitProposedToLocationWithAction: function(location, action) {
            this.visitProposedToLocation(location, { action })
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
        // Prefer Turbo 7, but support Turbolinks 5
        const webController = window.Turbo ? Turbo.controller : Turbolinks.controller
        window.webView = new TLWebView(webController)
        window.webView.pageLoaded()
    } catch (e) {
        // Most likely reached a page where Turbolinks.controller returned
        // "Uncaught ReferenceError: Turbolinks is not defined"
        TurboSession.turboFailedToLoad()
    }
})()
