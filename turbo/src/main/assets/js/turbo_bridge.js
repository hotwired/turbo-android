(() => {
  const TURBO_LOAD_TIMEOUT = 4000

  // Bridge between Turbo JS and native code. Built for Turbo 7
  // with backwards compatibility for Turbolinks 5
  class TurboNative {
    registerAdapter() {
      if (window.Turbo) {
        Turbo.registerAdapter(this)
        TurboSession.turboIsReady(true)
      } else if (window.Turbolinks) {
        Turbolinks.controller.adapter = this
        TurboSession.turboIsReady(true)
      } else {
        throw new Error("Failed to register the TurboNative adapter")
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

    visitLocationWithOptionsAndRestorationIdentifier(location, optionsJSON, restorationIdentifier) {
      let options = JSON.parse(optionsJSON)
      let action = options.action

      if (window.Turbo) {
        if (Turbo.navigator.locationWithActionIsSamePage(new URL(location), action)) {
          // Skip the same-page anchor scrolling behavior for visits initiated from the native
          // side. The page content may be stale and we want a fresh request from the network.
          Turbo.navigator.startVisit(location, restorationIdentifier, { "action": "replace" })
        } else {
          Turbo.navigator.startVisit(location, restorationIdentifier, options)
        }
      } else if (window.Turbolinks) {
        if (Turbolinks.controller.startVisitToLocationWithAction) {
          // Turbolinks 5
          Turbolinks.controller.startVisitToLocationWithAction(location, action, restorationIdentifier)
        } else {
          // Turbolinks 5.3
          Turbolinks.controller.startVisitToLocation(location, restorationIdentifier, options)
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
      if (window.Turbo && typeof Turbo.navigator.locationWithActionIsSamePage === "function") {
        if (Turbo.navigator.locationWithActionIsSamePage(location, options.action)) {
          Turbo.navigator.view.scrollToAnchorFromLocation(location)
          return
        }
      }

      TurboSession.visitProposedToLocation(location.toString(), JSON.stringify(options))
    }

    // Turbolinks 5
    visitProposedToLocationWithAction(location, action) {
      this.visitProposedToLocation(location, { action })
    }

    visitStarted(visit) {
      TurboSession.visitStarted(visit.identifier, visit.hasCachedSnapshot(), visit.location.toString())
      this.currentVisit = visit
      this.issueRequestForVisitWithIdentifier(visit.identifier)
      this.changeHistoryForVisitWithIdentifier(visit.identifier)
      this.loadCachedSnapshotForVisitWithIdentifier(visit.identifier)
    }

    visitRequestStarted(visit) {
      TurboSession.visitRequestStarted(visit.identifier)
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

    formSubmissionStarted(formSubmission) {
      TurboSession.formSubmissionStarted(formSubmission.location.toString())
    }

    formSubmissionFinished(formSubmission) {
      TurboSession.formSubmissionFinished(formSubmission.location.toString())
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

  const setup = function() {
    window.turboNative.registerAdapter()
    window.turboNative.pageLoaded()

    document.removeEventListener("turbo:load", setup)
    document.removeEventListener("turbolinks:load", setup)
  }

  const setupOnLoad = () => {
    document.addEventListener("turbo:load", setup)
    document.addEventListener("turbolinks:load", setup)

    setTimeout(() => {
      if (!window.Turbo && !window.Turbolinks) {
        TurboSession.turboIsReady(false)
        window.turboNative.pageLoadFailed()
      }
    }, TURBO_LOAD_TIMEOUT)
  }

  if (window.Turbo || window.Turbolinks) {
    setup()
  } else {
    setupOnLoad()
  }
})()
