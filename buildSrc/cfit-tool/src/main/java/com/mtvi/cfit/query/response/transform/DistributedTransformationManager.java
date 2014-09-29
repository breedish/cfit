package com.mtvi.cfit.query.response.transform;

import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.query.common.SizeAwareIterable;
import com.mtvi.cfit.util.exec.WorkProcessor;

import java.io.File;

/**
 * Class DistributedTransformationManager.
 *
 * @author zenind
 */
public class DistributedTransformationManager implements ResponseTransformationManager {

    /** Decorated transformation manager.*/
    private final ResponseTransformationManager source;
    /** Cfit configuration.*/
    private final CfitConfiguration configuration;

    /**
     * Constructor.
     * @param configuration - configuration.
     * @param source - original transformation manager.
     */
    public DistributedTransformationManager(CfitConfiguration configuration, ResponseTransformationManager source) {
        if (configuration == null || source == null) {
            throw new IllegalArgumentException(
                String.format("Null value has been passed in as required argument ['configuration'=%s, 'source'=%s]", configuration, source));
        }
        this.configuration = configuration;
        this.source = source;
    }

    @Override
    public void transformAll(File responseDir) {
        WorkProcessor.<File>newWorkProcessor(
            getResponses(responseDir),
            new WorkProcessor.Action<File>() {
                @Override
                public void doAction(File subject) throws CfitException {
                    transform(subject);
                }
            },
            0.667f
        ).process();
    }

    @Override
    public SizeAwareIterable<File> getResponses(File responseDir) {
        return source.getResponses(responseDir);
    }

    @Override
    public void transform(File response) {
        source.transform(response);
    }
}
