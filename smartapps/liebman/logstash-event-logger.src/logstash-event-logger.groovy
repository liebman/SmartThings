/**
 *  Logstash Event Logger
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
    name: "LogStash Event Logger",
    namespace: "liebman",
    author: "Chris Liebman",
    description: "Log events to logstash",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    input "logger", "capability.switch", title: "LogDevice:", required: false
    section("Log these presence sensors:") {
        input "presences", "capability.presenceSensor", multiple: true, required: false
    }
 	section("Log these switches:") {
    	input "switches", "capability.switch", multiple: true, required: false
    }
 	section("Log these switch levels:") {
    	input "levels", "capability.switchLevel", multiple: true, required: false
    }
	section("Log these motion sensors:") {
    	input "motions", "capability.motionSensor", multiple: true, required: false
    }
	section("Log these temperature sensors:") {
    	input "temperatures", "capability.temperatureMeasurement", multiple: true, required: false
    }
    section("Log these humidity sensors:") {
    	input "humidities", "capability.relativeHumidityMeasurement", multiple: true, required: false
    }
    section("Log these contact sensors:") {
    	input "contacts", "capability.contactSensor", multiple: true, required: false
    }
    section("Log these alarms:") {
		input "alarms", "capability.alarm", multiple: true, required: false
	}
    section("Log these indicators:") {
    	input "indicators", "capability.indicator", multiple: true, required: false
    }
    section("Log these CO detectors:") {
    	input "codetectors", "capability.carbonMonoxideDetector", multiple: true, required: false
    }
    section("Log these smoke detectors:") {
    	input "smokedetectors", "capability.smokeDetector", multiple: true, required: false
    }
    section("Log these water detectors:") {
    	input "waterdetectors", "capability.waterSensor", multiple: true, required: false
    }
    section("Log these acceleration sensors:") {
    	input "accelerations", "capability.accelerationSensor", multiple: true, required: false
    }
    section("Log these energy meters:") {
        input "energymeters", "capability.energyMeter", multiple: true, required: false
    }
    section("Log these weather:") {
        input "weathers", "capability.sensor", multiple: true, required: false
    }
    section("Log these batteries:") {
        input "batteries", "capability.battery", multiple: true, required: false
    }
    section("Log these illuminances:") {
        input "illuminances", "capability.illuminanceMeasurement", multiple: true, required: false
    }
    section("Log these thermostats:") {
        input "thermostats", "capability.thermostat", multiple: true, required: false
    }
    section("Log these uvIndex:") {
        input "uvindexes", "capability.ultravioletIndex", multiple: true, required: false
    }
}

def installed() {
	logger "debug", "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	logger "debug", "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    subscribe(alarms,		    "alarm",			        eventHandler, [filterEvents: false])
    subscribe(codetectors,	    "carbonMonoxideDetector",	eventHandler, [filterEvents: false])
    subscribe(contacts,		    "contact",      			eventHandler, [filterEvents: false])
    subscribe(humidities,       "humidity",                 eventHandler, [filterEvents: false])
    subscribe(indicators,	    "indicator",    			eventHandler, [filterEvents: false])
    subscribe(modes,		    "locationMode", 			eventHandler, [filterEvents: false])
    subscribe(motions,		    "motion",       			eventHandler, [filterEvents: false])
    subscribe(presences,	    "presence",     			eventHandler, [filterEvents: false])
    subscribe(relays,		    "relaySwitch",  			eventHandler, [filterEvents: false])
    subscribe(smokedetectors,	"smokeDetector",			eventHandler, [filterEvents: false])
    subscribe(switches,		    "switch",       			eventHandler, [filterEvents: false])
    subscribe(levels,		    "level",					eventHandler, [filterEvents: false])
    subscribe(temperatures,	    "temperature",  			eventHandler, [filterEvents: false])
    subscribe(waterdetectors,	"water",					eventHandler, [filterEvents: false])
    subscribe(accelerations,    "acceleration",             eventHandler, [filterEvents: false])
    subscribe(energymeters,     "power",                	eventHandler, [filterEvents: false])
	subscribe(location, 		"position", 				eventHandler, [filterEvents: false])
	subscribe(location, 		"sunset", 					eventHandler, [filterEvents: false])
	subscribe(location, 		"sunrise", 					eventHandler, [filterEvents: false])
    subscribe(location, 		null, 						eventHandler, [filterEvents: false])
    subscribe(illuminances,     "illuminance",              eventHandler, [filterEvents: false])
    subscribe(illuminances,     "battery",                  eventHandler, [filterEvents: false])
    subscribe(thermostats,      "thermostat",               eventHandler, [filterEvents: false])
    subscribe(uvindexes,        "ultravioletIndex",         eventHandler, [filterEvents: false])
    
    subscribe(weathers,         "weather",                  eventHandler, [filterEvents: false])
    subscribe(weathers,         "wind",                     eventHandler, [filterEvents: false])
    subscribe(weathers,         "windGust",                 eventHandler, [filterEvents: false])
    subscribe(weathers,         "windDir",                  eventHandler, [filterEvents: false])
    subscribe(weathers,         "ultravioletIndex",         eventHandler, [filterEvents: false])
    subscribe(weathers,         "dewpoint",                 eventHandler, [filterEvents: false])
    subscribe(weathers,         "feelsLike",                eventHandler, [filterEvents: false])
    
}

def locationEvent(evt) {
    log.debug "locationEvent()!"
    if (logger) {
	    logger.log("debug", app.name, evt.description)
	    logger.log(evt)
    }
}

def eventHandler(evt) {
    if (logger) {
        logger.log(evt)
    }
}

def logger(level, message) {
    if (logger) {
        logger.log(level, app.label, message)
    }
    switch(level) {
    	case "debug": log.debug(message); break;
    	case "info":  log.info(message);  break;
    	case "warn":  log.warn(message);  break;
    	case "error": log.error(message); break;
    	default:      log.info(message);  break;
    }
}