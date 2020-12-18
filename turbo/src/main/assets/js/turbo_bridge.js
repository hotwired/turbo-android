(() => {
  // Bridge between Turbo JS and native code. Built for Turbo 7
  // with backwards compatibility for Turbolinks 5
  class TurboNative {
    constructor() {
      this.registerAdapter()
    }

    registerAdapter() {
      if (window.Turbo) {
        Turbo.registerAdapter(this)
        TurboSession.turboIsReady(true)
      } else if (window.Turbolinks) {
        Turbolinks.controller.adapter = this
        TurboSession.turboIsReady(true)
      } else {
        TurboSession.turboIsReady(false)
        this.pageLoadFailed()
      }
    }

    pageLoaded() {
      let restorationIdentifier = ""

      if (window.Turbo) {
        restorationIdentifier = Turbo.navigator.restorationIdentifier
      } else if (window.Turbolinks) {
        restorationIdentifier = Turbolinks.controller.restorationIdentifier
      }

      this.afterNextRepaint(function() {
        TurboSession.pageLoaded(restorationIdentifier)
      })
    }

    pageLoadFailed() {
      TurboSession.turboFailedToLoad()
    }

    visitLocationWithOptionsAndRestorationIdentifier(location, options, restorationIdentifier) {
      if (window.Turbo) {
        Turbo.navigator.startVisit(location, restorationIdentifier, JSON.parse(options))
      } else if (window.Turbolinks) {
        if (Turbolinks.controller.startVisitToLocationWithAction) {
          // Turbolinks 5
          Turbolinks.controller.startVisitToLocationWithAction(location, JSON.parse(options).action, restorationIdentifier)
        } else {
          // Turbolinks 5.3
          Turbolinks.controller.startVisitToLocation(location, restorationIdentifier, JSON.parse(options))
        }
      }
    }

    // Current visit

    issueRequestForVisitWithIdentifier(identifier) {
      if (identifier == this.currentVisit.identifier) {
        this.currentVisit.issueRequest()
      }
    }

    changeHistoryForVisitWithIdentifier(identifier) {
      if (identifier == this.currentVisit.identifier) {
        this.currentVisit.changeHistory()
      }
    }

    loadCachedSnapshotForVisitWithIdentifier(identifier) {
      if (identifier == this.currentVisit.identifier) {
        this.currentVisit.loadCachedSnapshot()
      }
    }

    loadResponseForVisitWithIdentifier(identifier) {
      if (identifier == this.currentVisit.identifier) {
        this.currentVisit.loadResponse()
      }
    }

    cancelVisitWithIdentifier(identifier) {
      if (identifier == this.currentVisit.identifier) {
        this.currentVisit.cancel()
      }
    }

    visitRenderedForColdBoot(visitIdentifier) {
      this.afterNextRepaint(function() {
          TurboSession.visitRendered(visitIdentifier)
      })
    }

    // Adapter interface

    visitProposedToLocation(location, options) {
      TurboSession.visitProposedToLocation(location.absoluteURL, JSON.stringify(options))
    }

    // Turbolinks 5
    visitProposedToLocationWithAction(location, action) {
      this.visitProposedToLocation(location, { action })
    }

    visitStarted(visit) {
      TurboSession.visitStarted(visit.identifier, visit.hasCachedSnapshot(), visit.location.absoluteURL)
      this.currentVisit = visit
      this.issueRequestForVisitWithIdentifier(visit.identifier)
      this.changeHistoryForVisitWithIdentifier(visit.identifier)
      this.loadCachedSnapshotForVisitWithIdentifier(visit.identifier)
    }

    visitRequestStarted(visit) {
      // Purposely left unimplemented. visitStarted covers most cases.
    }

    visitRequestCompleted(visit) {
      TurboSession.visitRequestCompleted(visit.identifier)
      this.loadResponseForVisitWithIdentifier(visit.identifier)
    }

    visitRequestFailedWithStatusCode(visit, statusCode) {
      TurboSession.visitRequestFailedWithStatusCode(visit.identifier, visit.hasCachedSnapshot(), statusCode)
    }

    visitRequestFinished(visit) {
      TurboSession.visitRequestFinished(visit.identifier)
    }

    visitRendered(visit) {
      this.afterNextRepaint(function() {
        TurboSession.visitRendered(visit.identifier)
      })
    }

    visitCompleted(visit) {
      this.afterNextRepaint(function() {
        TurboSession.visitCompleted(visit.identifier, visit.restorationIdentifier)
      })
    }

    pageInvalidated() {
      TurboSession.pageInvalidated()
    }

    // Private

    afterNextRepaint(callback) {
      if (document.hidden) {
        callback()
      } else {
        requestAnimationFrame(function() {
          requestAnimationFrame(callback)
        })
      }
    }
  }

  window.turboNative = new TurboNative()
  window.turboNative.pageLoaded()
})()
