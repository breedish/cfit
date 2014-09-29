package com.mtvi.cfit.exec;

import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.util.XMLUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manager for {@link com.mtvi.cfit.exec.ExecutionPhaseResult}.
 *
 * @author zenind.
 */
public final class ExecutionResultManager {

    /**
     * Configuration.
     */
    private CfitConfiguration configuration;

    /**
     * Constructor.
     *
     * @param configuration - cfit configuration.
     */
    public ExecutionResultManager(CfitConfiguration configuration) {
        this.configuration = configuration;

    }

    /**
     * Loads execution phase result from given file storage.
     *
     * @param resultStorage - results storage.
     * @return - execution phase result.
     * @throws CfitException - in case of issue during loading result from given storage.
     */
    public ExecutionPhaseResult load(File resultStorage) throws CfitException {
        return XMLUtils.convertFromXML(resultStorage, ExecutionPhaseResult.class);
    }

    /**
     * Saves result to storage.
     *
     * @param result       - result.
     * @param responsesDir - target storage directory for result.
     * @return - saved execution result.
     * @throws CfitException - in case of issue during saving results to storage.
     */
    public ExecutionPhaseResult save(final ExecutionPhaseResult result, final File responsesDir) throws CfitException {
        File storage = new File(responsesDir, configuration.getExecutionStatsFileName());

        return XMLUtils.convertToXML(result, storage);
    }

    /**
     * Custom adapter for {@link java.util.concurrent.atomic.AtomicInteger}.
     */
    public static class AtomicIntegerAdapter extends XmlAdapter<Integer, AtomicInteger> {

        @Override
        public AtomicInteger unmarshal(Integer v) throws Exception {
            return new AtomicInteger(v);
        }

        @Override
        public Integer marshal(AtomicInteger v) throws Exception {
            return v.get();
        }
    }

    /**
     * Adapter for conversion {@link com.mtvi.cfit.exec.ExecutionPhaseResult#groups} map.
     */
    public static class ByStatusResponseGroupAdapter extends XmlAdapter<ByStatusResponseGroups, Map<Integer, ByStatusResponseGroup>> {

        @Override
        public Map<Integer, ByStatusResponseGroup> unmarshal(ByStatusResponseGroups v) throws Exception {
            Map<Integer, ByStatusResponseGroup> groups = new ConcurrentHashMap<Integer, ByStatusResponseGroup>();

            for (ByStatusResponseGroup group : v.groups) {
                groups.put(group.getStatus(), group);
            }

            return groups;
        }

        @Override
        public ByStatusResponseGroups marshal(Map<Integer, ByStatusResponseGroup> v) throws Exception {
            ByStatusResponseGroups groups = new ByStatusResponseGroups();
            groups.groups = v.values().toArray(new ByStatusResponseGroup[v.size()]);
            return groups;
        }
    }

    /**
     * Helper class for conversion {@link com.mtvi.cfit.exec.ExecutionPhaseResult#groups} map.
     */
    private static final class ByStatusResponseGroups {
        /**
         * Groups array.
         */
        @XmlElement(name = "group")
        @SuppressWarnings("all")
        ByStatusResponseGroup[] groups;
    }

}
