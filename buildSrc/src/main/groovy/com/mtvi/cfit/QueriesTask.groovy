package com.mtvi.cfit

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * @author voitaua
 */
class QueriesTask extends DefaultTask {

    def String responseDir

    CfitConfiguration configuration = new CfitConfiguration()

    @TaskAction
    def run() {
        configuration.cfitProperties.extraReqParams = "indent=true&plugin.timeTravel=2013-12-31T04:00:00.000Z&timeTravel=2013-12-31T04:00:00.000Z"

        File dir = new File(responseDir)
        dir.mkdirs()
        Cfit cfit = new Cfit(configuration);

        cfit.run(dir)
        cfit.processResponses(dir)
    }
}
