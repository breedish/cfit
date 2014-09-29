package com.mtvi.cfit;

import com.mtvi.cfit.comparison.ComparisonManager;
import com.mtvi.cfit.comparison.FileBasedComparisonManager;
import com.mtvi.cfit.comparison.report.ComparisonReport;
import com.mtvi.cfit.exec.DistributedPhaseExecutor;
import com.mtvi.cfit.exec.PhaseExecutor;
import com.mtvi.cfit.query.ExecutableQueryProvider;
import com.mtvi.cfit.query.QueryProvider;
import com.mtvi.cfit.query.response.QueryResponseManager;
import com.mtvi.cfit.query.response.ToFileQueryResponseManager;
import com.mtvi.cfit.query.response.transform.BasicTransformationManager;
import com.mtvi.cfit.query.response.transform.DistributedTransformationManager;
import com.mtvi.cfit.query.response.transform.ResponseTransformationManager;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Cfit launcher.
 *
 * @author zenind.
 */
public final class Cfit {

    /** Logger.*/
    private static final Logger LOG = LoggerFactory.getLogger(Cfit.class);
    /** Connection timeout default value.*/
    private static final int DEFAULT_CONNECTION_TIMEOUT = 60000;
    /** Queries provider.*/
    private final QueryProvider queryProvider;
    /** Response processing manager.*/
    private final ResponseTransformationManager responseTransformationManager;
    /** Query response manager.*/
    private final QueryResponseManager<File> responseManager;
    /** Configuration.*/
    private final CfitConfiguration configuration;

    /**
     * Default constructor.
     * @param configuration - cfit configuration.
     * @throws IOException - in case of i/o related issues
     */
    public Cfit(CfitConfiguration configuration) throws IOException {
        this.configuration = configuration;
        this.queryProvider = new ExecutableQueryProvider(buildHttpClient(), configuration);
        this.responseManager = new ToFileQueryResponseManager();
        this.responseTransformationManager = new DistributedTransformationManager(
            configuration,
            new BasicTransformationManager(configuration, responseManager)
        );
    }

    private HttpClient buildHttpClient() {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();

        manager.setDefaultMaxPerRoute(configuration.getCfitProperties().getWorkersNumber());
        manager.setMaxTotal(configuration.getCfitProperties().getWorkersNumber());
        return HttpClientBuilder.create()
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .setConnectionRequestTimeout(DEFAULT_CONNECTION_TIMEOUT)
                    .setSocketTimeout(DEFAULT_CONNECTION_TIMEOUT)
                    .setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT)
                    .build()
            )
            .disableAutomaticRetries()
            .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy())
            .setConnectionManager(manager)
            .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
            .build();
    }

    /**
     * Does comparison of result of two directories.
     *
     * @return - comparison report.
     * @throws IOException - in case of i/o exception.
     * @throws CfitException - in case of comparison/conversion exception.
     */
    public ComparisonReport compare() throws IOException, CfitException {
        LOG.info("Compare responses");
        ComparisonManager comparisonManager = new FileBasedComparisonManager(
            configuration, responseManager
        );
        return comparisonManager.compareResponses();
    }

    /**
     * Runs cfit (queries execution).
     * @param responsesDir - response dir.
     * @throws java.io.IOException - in case of I/O during cleaning up of given responses dir.
     * @throws CfitException - in case of issue during queries execution.
     */
    public void run(File responsesDir) throws IOException, CfitException {
        LOG.info("Run queries for {}", responsesDir.getAbsolutePath());
        FileUtils.cleanDirectory(responsesDir);
        runQueries(responsesDir);
    }

    /**
     * Does responses pre processing.
     * @param responsesDir - responses dir.
     */
    public void processResponses(File responsesDir) {
        LOG.info("Process responses");
        responseTransformationManager.transformAll(responsesDir);
    }

    private void runQueries(File responsesDir) throws CfitException {
        PhaseExecutor executor = new DistributedPhaseExecutor(configuration, responseManager, responsesDir);
        try {
            executor.executeQueries(queryProvider);
        } finally {
            executor.shutdown();
        }
    }
}
