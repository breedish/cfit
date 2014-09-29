package com.mtvi.cfit

import com.mtvi.cfit.comparison.ComparisonResultChecker
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * @author voitaua
 */
@Slf4j
class CompareTask extends DefaultTask {
    String originalResultsDirName
    String rcResultsDirName
    String rcVersion
    String originalVersion

    CfitConfiguration configuration = new CfitConfiguration()

    @TaskAction
    def run() {
        configuration.originalResultsDir = originalResultsDirName
        configuration.rcResultsDir = rcResultsDirName
        configuration.cfitProperties.rcArtifactVersion = rcVersion
        configuration.cfitProperties.originalArtifactVersion = originalVersion

        Cfit cfit = new Cfit(configuration);
        cfit.compare();

        ComparisonResultChecker stateManager = new ComparisonResultChecker(configuration);
        try {
            stateManager.checkState();
        } finally {
            log.info("CFIT finished work at : " + new Date());
        }
    }
}
