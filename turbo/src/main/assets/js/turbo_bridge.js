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
        if (Turbo.navigator.location?.href === location && action === "restore") {
          // A "restore" visit to the currently rendered location can occur when visiting
          // a web -> native -> back to web screen. In this situation, the connect()
          // callback (from Stimulus) in bridge component controllers will not be called,
          // since they are already connected. We need to notify the web bridge library
          // that a "restore" visit has occurred to manually trigger connect() and notify
          // the native app so the native bridge component view state can be restored.
          Turbo.navigator.startVisit(location, restorationIdentifier, options)
          document.dispatchEvent(new Event("native:restore"))
        } else if (Turbo.navigator.locationWithActionIsSamePage(new URL(location), action)) {
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

    cacheSnapshot() {
      if (window.Turbo) {
        Turbo.session.view.cacheSnapshot()
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
      if (window.Turbo && Turbo.navigator.locationWithActionIsSamePage(location, options.action)) {
        // Scroll to the anchor on the page
        TurboSession.visitProposalScrollingToAnchor(location.toString(), JSON.stringify(options))
        Turbo.navigator.view.scrollToAnchorFromLocation(location)
      } else if (window.Turbo && Turbo.navigator.location?.href === location.href) {
        // Refresh the page without native proposal
        TurboSession.visitProposalRefreshingPage(location.toString(), JSON.stringify(options))
        this.visitLocationWithOptionsAndRestorationIdentifier(location, JSON.stringify(options), Turbo.navigator.restorationIdentifier)
      } else {
        // Propose the visit
        TurboSession.visitProposedToLocation(location.toString(), JSON.stringify(options))
      }
    }

    // Turbolinks 5
    visitProposedToLocationWithAction(location, action) {
      this.visitProposedToLocation(location, { action })
    }

    visitStarted(visit) {
      TurboSession.visitStarted(visit.identifier, visit.hasCachedSnapshot(), visit.isPageRefresh || false, visit.location.toString())
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

    async visitRequestFailedWithStatusCode(visit, statusCode) {
      // Turbo does not permit cross-origin fetch redirect attempts and
      // they'll lead to a visit request failure. Attempt to see if the
      // visit request failure was due to a cross-origin redirect.
      const redirect = await this.fetchFailedRequestCrossOriginRedirect(visit, statusCode)
      const location = visit.location.toString()

      if (redirect != null) {
        TurboSession.visitProposedToCrossOriginRedirect(location, redirect.toString(), visit.identifier)
      } else {
        TurboSession.visitRequestFailedWithStatusCode(location, visit.identifier, visit.hasCachedSnapshot(), statusCode)
      }
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

    async fetchFailedRequestCrossOriginRedirect(visit, statusCode) {
      // Non-HTTP status codes are sent by Turbo for network
      // failures, including cross-origin fetch redirect attempts.
      if (statusCode <= 0) {
        try {
          const response = await fetch(visit.location, { redirect: "follow" })
          if (response.url != null && response.url.origin != visit.location.origin) {
            return response.url
          }
        } catch {}
      }

      return null
    }

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

  // Touch detection, allowing vertically scrollable elements
  // to scroll properly without triggering pull-to-refresh.

  const elementTouchStart = (event) => {
    if (!event.target) return

    var element = event.target

    while (element) {
      const canScroll = element.scrollHeight > element.clientHeight
      const overflowY = window.getComputedStyle(element).overflowY

      if (canScroll && (overflowY === "scroll" || overflowY === "auto")) {
        TurboSession.elementTouchStarted(true)
        break
      }

      element = element.parentElement
    }

    if (!element) {
      TurboSession.elementTouchStarted(false)
    }
  }

  const elementTouchEnd = () => {
    TurboSession.elementTouchEnded()
  }

  // Setup and register adapter

  window.turboNative = new TurboNative()

  const setup = function() {
    window.turboNative.registerAdapter()
    window.turboNative.pageLoaded()

    document.removeEventListener("turbo:load", setup)
    document.removeEventListener("turbolinks:load", setup)

    document.addEventListener("touchstart", elementTouchStart)
    document.addEventListener("touchend", elementTouchEnd)
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
