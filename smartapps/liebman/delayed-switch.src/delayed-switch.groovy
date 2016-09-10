/**
 *  Delayed Switch
 *
 *  Copyright 2016 Chris Liebman
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Delayed Switch",
    namespace: "liebman",
    author: "Chris Liebman",
    description: "Turn switch on/off after delay with redundant button press.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    input "master", "capability.switch", title: "Select switch:", required: true
    input "action", "enum", title: "Action:", multiple: false, required: true, options: ["on", "off"], defaultValue: "off"
    input "delay", "number" , title: "Action delay:", required: false, defaultValue: 900
    input "feedback", "capability.switch", title: "Select feedback switch:", required: false
    input "feedbackDuration", "number" , title: "feedback duration (ms):", required: false, defaultValue: 1000
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    subscribe(master, "switch", switchHandler, [filterEvents: false])
}

def switchHandler(evt) {
	log.info "CURRENT: value: ${evt.value} phys:${evt.isPhysical()} change:${evt.isStateChange()} date:${evt.date} id:${evt.id}"
    def history = master.events(max: 10)
    // dumpEvents(history)
    // we must have some history
    if (history && history.size() > 1) {
	    // find the previous event
    	def prev = history.get(0)
        if (prev.id.equals(evt.id)) {
            log.warn "first history event was the current one, using the one before that!"
            prev = history.get(1)
        }
		// make sure the last two events were physical
        log.info "PREVIOUS: value: ${prev.value} phys: ${prev.isPhysical()} change:${prev.isStateChange()} date:${prev.date} id:${prev.id}"
        
        if (evt.isPhysical() && !evt.isStateChange() && !action.equalsIgnoreCase(evt.value) && prev.isPhysical()) {
            if (feedback) {
                startFeedback(evt)
            }
            log.info "scheduling delayed ${action} in ${delay} seconds"
            runIn(delay, delayedActionHandler)
            log.info "back from runIn()"
        } else if (evt.isPhysical() && evt.isStateChange() && action.equalsIgnoreCase(evt.value)) {
            // cancel if someone physically changes it.
            log.info "unscheduling incase we were active!"
            unschedule()
        }
    }
}

def dumpEvents(events) {
   log.warn "****** start event history size:${events.size()} ******"
   for(def i = 0; i < events.size(); ++i) {
       def e = events.get(i)
       log.warn "index:${i} date:${e.date} value:${e.value} phys:${e.isPhysical()} dgtl:${e.isDigital()} id:${e.id}"
   }
//   for (def e : events) {
//       log.info "date:${e.date} value:${e.value} phys:${e.isPhysical()} dgtl:${e.isDigital()} id:${e.id}"
//   }
  log.warn "****** end event history ******"
}

def delayedActionHandler() {
    log.info "delayedActionHandler called!"
    
    if (action == "off") {
        log.info "turning it off"
        master.off()
    } else {
        log.info "turning it on"
        master.on()
    }
    log.info "delayedActionHandler complete"
}

def startFeedback(evt) {
    log.info "starting feedback"
    def current = feedback.latestState("switch").value
    if (feedback == master) {
       log.info "feedback is same as master - using event info for state"
       current = master.latestState("switch").value
    }
    
    if (current == 'on') {
        log.info "feedback current state is 'on' so turning it off"
        feedback.off()
        feedback.on(delay: feedbackDuration)
    } else {
        log.info "feedback current state is 'off' so turning it on"
        feedback.on()
        feedback.off(delay: feedbackDuration)
    }
    
    /* runIn(feedbackDuration, endFeedback) */
    log.info "feedback started!"
}

def endFeedback() {
    log.info "ending feedback"
    if (feedback.currentState == 'on') {
        log.info "ending feedback state is 'on' so turning it off"
        feedback.off()
    } else {
        log.info "ending feedback state is 'off' so turning it on"
        feedback.on()
    }
    log.info "feedback ended!"
}

