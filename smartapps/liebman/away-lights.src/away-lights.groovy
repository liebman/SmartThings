/**
 *  Away Lights
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
    name: "Away Lights",
    namespace: "liebman",
    author: "Chris Liebman",
    description: "Randomly control lights when away",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "Setup", title: "Setup", install: true, uninstall: true) {
        section("Settings") {
            input "switches", "capability.switch", title: "Control these switches...", multiple: true
            input "modes", "mode", title: "In these modes...", multiple: true
            input "modeDelay", "number", title: "Delay in minutes from mode change to first light change"
            input "active", "number", title: "Active switch count"
            input "interval", "number", title: "Minutes between changes"
            input "intervalVariation", "number", title: "Variation minutes for changes"
            input "intervalMinimum", "number", title: "Minumum interval in minutes"
            input "intervalMaximum", "number", title: "Maximum interval in minutes"
            input "starting", "time", title: "Start time", required: false
            input "ending", "time", title: "End time", required: false
        }
    }
}

def getVersion() {
   return "0.7"
}

def installed() {
	log.trace "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.trace "Updated with settings: ${settings}"

	unsubscribe()
    unschedule()
	initialize()
}

def initialize() {
    log.trace("initialize() version:${version}")
    
    if (modes) {
        log.debug("subscribing to mode changes")
        subscribe(location, "mode", modeChangeHandler)
    }
    
    if (starting) {
        log.debug("scheduling starting time: ${starting}")
        schedule(starting, startTimeHandler)
        if (ending) {
            log.debug("scheduling ending time: ${ending}")
            schedule(ending, endTimeHandler)
        }
    }
    
    doActivity()
}

// called when the mode changes
def modeChangeHandler(evt) {
    log.debug("modeChangeHandler(${evt.value})")
    if (modeDelay) {
        runIn(modeDelay*60, intervalHandler)
    } else {
        doActivity()
    }
}

// called when time window starts
def startTimeHandler(evt) {
    log.trace("startTimeHandler()!")
    doActivity()
}

// called when time window ends
def endTimeHandler(evt) {
    log.trace("endTimeHandler()!")
    // if we are still in the mode, turn all the lights off.
    if (inMode()) {
       switches.off()
    }
}

// called each interval to change lights
def intervalHandler(evt) {
    log.trace("intervalHandler() version:${version}")
    doActivity()
}

def doActivity() {
    if (shouldBeActive()) {
        randomLights()
        scheduleInterval()
    }
}

def randomLights() {
    log.trace("randomLights()")
    def off = switches
    log.debug("switches: ${off}")
    def on = getRandomElements(off, active, true)
    log.debug("on switches: ${on}")
    on.each {
        log.debug("${it} is '${it.currentSwitch}'")
        if (it.currentSwitch != "on") {
            log.debug("turning on: ${it}")
            it.on() 
        }
    }
    log.debug("off switches: ${off}")
    off.each {
        log.debug("${it} is '${it.currentSwitch}'")
        if (it.currentSwitch != "off") {
            log.debug("turning off: ${it}")
            it.off() 
        }
    }
}

def scheduleInterval() {
    log.trace("scheduleInterval()")

    def delay = computeVariation(interval, intervalVariation, minimum, maximum)
    log.debug("scheduling intervalHandler to run in ${delay} minutes")
    // must use runIn() as cron scheduling only works for smaller values
    runIn(delay*60, intervalHandler)
}

// compute a value +/- ramdome value up to variation
def computeVariation(value, variation, minimum, maximum) {
    log.trace("computeVariation(value:${value}, variation:${variation}, minimum:${minimum}, maximum:${maximum})")
    def random = new Random().nextInt(variation*2) - variation
    log.debug("random variation: ${random}")
    def result = value + random
    if (result < minimum) {
        result = minimum
    } else if (result > maximum) {
        result = maximum
    }
    log.trace("variation result: ${result}")
    return result
}

// fetch and maybe remove <number> random items from <delagate>
def getRandomElements(delegate, number, remove) {
    log.trace("getRandomElements: delegate:${delegate}, number:${number}, remove:${remove}")
    
    def rnd = new Random()
    
    if(number < 1) {
        number = 1
    }
	
    def tempList = []
    def counter = 0

    if (number >= delegate.size()) {
        log.debug("asking for same item count as orig, returning clone")
        tmpList.addAll(delegate)
        delegate.removeAll()
        return tmpList
    }
    
    
    while(counter < number) {
        log.trace("count:${counter}, index:${index}")

        def index = rnd.nextInt(delegate.size())
        log.trace("index:${index}")
        tempList.add(delegate[index])
        if (remove) {
            delegate.remove(index)
        }
        counter++
    }
    return tempList
}

def shouldBeActive() {
    def result = inMode() && inTimeWindow()
    log.debug("shouleBeActive() returning ${result}")
    return result
}

def inMode() {
    def result = true
    if (modes) {
        result = modes.contains(location.mode)
    }
    log.debug("inMode() returning ${result}")
    return result
}

def inTimeWindow() {
    def now = now()
    def result = true
    if (starting && ending) {
        result = now >= startTime &&
                 now < endTime
    }
    log.debug("inTimeWindow() returning ${result}")
	return result
}

def getStartTime() {
    def date = timeToday(starting, location.timeZone)
    log.debug("getStartTime() returning: ${date}")
    return date.time
}

def getEndTime() {
    def date = timeTodayAfter(starting, ending, location.timeZone)
    log.debug("getEndTime() returning: ${date}")
    return date.time
}