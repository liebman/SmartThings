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
    section("Switch for delayed ON/OFF") {
        input "master", "capability.switch", title: "Select", required: true
    }

    section ("Delayed ON or OFF?") {
        input "action", "enum", title: "Action:", multiple: false, required: true, options: ["on", "off"]
    }

	section("Action delay in seconds") {
		input "delay", "number" , title: "delay:", required: true
	}
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
    // TODO: subscribe to attributes, devices, locations, etc.
    subscribe(master, "switch", switchHandler, [filterEvents: false])
}

def switchHandler(evt) {
	log.info "value: ${evt.value} action: ${action}"
    log.info "physical: ${evt.isPhysical()}"
    log.info "stateChange: ${evt.isStateChange()}"
    
    if (evt.isPhysical() && !evt.isStateChange() && !action.equalsIgnoreCase(evt.value)) {
        log.info "scheduling delayed ${action} in ${delay} seconds"
        runIn(delay, delayedActionHandler)
        log.info "back from runIn()"
    }
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

