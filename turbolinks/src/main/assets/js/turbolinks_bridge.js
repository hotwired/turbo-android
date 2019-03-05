(() => {
    function TLWebView(controller) {
        this.controller = controller
        controller.adapter = this

        var isReady = typeof Turbolinks !== "undefined" && Turbolinks !== null
        TurbolinksSession.turbolinksIsReady(isReady)
    }

    TLWebView.prototype = {
        // TurbolinksSession calls this as the starting point

        visitLocationWithActionAndRestorationIdentifier: function(location, action, restorationIdentifier) {
            this.controller.startVisitToLocationWithAction(location, action, restorationIdentifier)
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

        // Callbacks to TurbolinksSession from Turbolinks Core

        pageLoaded: function() {
            var restorationIdentifier = this.controller.restorationIdentifier
            this.afterNextRepaint(function() {
                TurbolinksSession.pageLoaded(restorationIdentifier)
            })
        },

        visitProposedToLocationWithAction: function(location, action) {
            TurbolinksSession.visitProposedToLocationWithAction(location.absoluteURL, action)
        },

        visitStarted: function(visit) {
            this.currentVisit = visit
            TurbolinksSession.visitStarted(visit.identifier, visit.hasCachedSnapshot(), visit.location.absoluteURL, visit.restorationIdentifier)
        },

        visitRequestStarted: function(visit) {
            // Purposely left unimplemented. visitStarted covers most cases.
        },

        visitRequestCompleted: function(visit) {
            TurbolinksSession.visitRequestCompleted(visit.identifier)
        },

        visitRequestFailedWithStatusCode: function(visit, statusCode) {
            TurbolinksSession.visitRequestFailedWithStatusCode(visit.identifier, statusCode)
        },

        visitRequestFinished: function(visit) {
            // Purposely left unimplemented. visitRequestCompleted covers most cases.
        },

        visitRendered: function(visit) {
            this.afterNextRepaint(function() {
                TurbolinksSession.visitRendered(visit.identifier)
            })
        },

        visitRenderedForColdBoot: function(visitIdentifier) {
            this.afterNextRepaint(function() {
                TurbolinksSession.visitRendered(visitIdentifier)
            })
        },

        visitCompleted: function(visit) {
            this.afterNextRepaint(function() {
                TurbolinksSession.visitCompleted(visit.identifier)
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
