(() => {
  class AppBridge {
    constructor() {
      this.supportedEvents = []
    }

    register(eventName) {
      if (Array.isArray(eventName)) {
        this.supportedEvents = this.supportedEvents.concat(eventName)
      } else {
        this.supportedEvents.push(eventName)
      }
    }

    unregister(eventName) {
      const index = this.supportedEvents.indexOf(eventName)
      if (index != -1) {
        this.supportedEvents.splice(index, 1)
      }
    }

    canHandleEvent(event) {
      return this.supportedEvents.includes(event)
    }

    // Send to JS Bridge
    send(event) {
      window.Bridge.receive(event)
    }

    // Receive from JS Bridge
    receive(event) {
      this.postMessage(event)
    }

    get platform() {
      return "android"
    }

    // Native handler

    postMessage(event) {
      NativeBridge.receive(event)
    }

    log(message) {
      NativeBridge.log(message)
    }
  }

  window.appBridge = new AppBridge()
  window.Bridge.setAdapter(appBridge)
  window.appBridge.log("AppBridge initialized...")
})()
