(() => {
    function TLWebView(controller) {
        this.controller = controller
        controller.adapter = this

        var isReady = typeof Turbolinks !== "undefined" && Turbolinks !== null
        TurbolinksSession.turbolinksIsReady(isReady)
    }

    TLWebView.prototype = {
        // TurbolinksSession calls this as the starting point

        visitLocationWithOptionsAndRestorationIdentifier: function(location, options, restorationIdentifier) {
            if (this.controller.startVisitToLocation) {
                this.controller.startVisitToLocation(location, restorationIdentifier, JSON.parse(options))
            } else {
                // Temporarily support old API
                this.controller.startVisitToLocationWithAction(location, JSON.parse(options).action, restorationIdentifier)
            }
        },

        // Functions available to TurbolinksSession to call directly into Turbolinks Core

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
                TurbolinksSession.visitRendered(visitIdentifier)
            })
        },

        // Callbacks to TurbolinksSession from Turbolinks Core

        pageLoaded: function() {
            var restorationIdentifier = this.controller.restorationIdentifier
            this.afterNextRepaint(function() {
                TurbolinksSession.pageLoaded(restorationIdentifier)
            })
        },

        // Temporary adapter for new API
        visitProposedToLocationWithAction: function(location, action) {
            this.visitProposedToLocation(location, { action })
        },

        visitProposedToLocation: function(location, options) {
            TurbolinksSession.visitProposedToLocation(location.absoluteURL, JSON.stringify(options))
        },

        visitStarted: function(visit) {
            TurbolinksSession.visitStarted(visit.identifier, visit.hasCachedSnapshot(), visit.location.absoluteURL)
            this.currentVisit = visit
            this.issueRequestForVisitWithIdentifier(visit.identifier)
            this.changeHistoryForVisitWithIdentifier(visit.identifier)
            this.loadCachedSnapshotForVisitWithIdentifier(visit.identifier)
        },

        visitRequestStarted: function(visit) {
            // Purposely left unimplemented. visitStarted covers most cases.
        },

        visitRequestCompleted: function(visit) {
            TurbolinksSession.visitRequestCompleted(visit.identifier)
            this.loadResponseForVisitWithIdentifier(visit.identifier)
        },

        visitRequestFailedWithStatusCode: function(visit, statusCode) {
            TurbolinksSession.visitRequestFailedWithStatusCode(visit.identifier, statusCode)
        },

        visitRequestFinished: function(visit) {
            TurbolinksSession.visitRequestFinished(visit.identifier)
        },

        visitRendered: function(visit) {
            this.afterNextRepaint(function() {
                TurbolinksSession.visitRendered(visit.identifier)
            })
        },

        visitCompleted: function(visit) {
            this.afterNextRepaint(function() {
                TurbolinksSession.visitCompleted(visit.identifier, visit.restorationIdentifier)
            })
        },

        pageInvalidated: function() {
            TurbolinksSession.pageInvalidated()
        },

        // Private

        afterNextRepaint: function(callback) {
            // Explanation about document.hidden and the circumstances for evaluating 'true':
            // https://github.com/basecamp/turbolinks-android-kotlin/pull/11#discussion_r157035376
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
        TurbolinksSession.turbolinksFailedToLoad()
    }
})()
