package io.infrastructor.cli.handlers

import io.infrastructor.cli.ApplicationProperties


public class VersionHandler {
    
    def options() {
        [:]
    }
    
    def usage() { 
        ["infrastructor version"] 
    }
    
    def description() { 
        "Prints version information."
    }

    def execute() { 
        println "Infrastructor ${ApplicationProperties.fullVersion()}" 
    }
}

