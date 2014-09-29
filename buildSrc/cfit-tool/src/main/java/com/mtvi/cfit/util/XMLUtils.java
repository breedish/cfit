package com.mtvi.cfit.util;

import com.mtvi.cfit.ConversionException;
import com.mtvi.cfit.comparison.report.ComparisonReport;
import com.mtvi.cfit.exec.ExecutionPhaseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * XML Utilities class.
 *
 * @author zenind
 */
public final class XMLUtils {

    /** Logger.*/
    private static final Logger LOG = LoggerFactory.getLogger(XMLUtils.class);
    /** JAXB Context.*/
    private static final JAXBContext JAXB_CONTEXT;
    /** Unmarshaller.*/
    private static final Unmarshaller UNMARSHALLER;
    /** Marshaller.*/
    private static final Marshaller MARSHALLER;

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(ExecutionPhaseResult.class, ComparisonReport.class);
            MARSHALLER = JAXB_CONTEXT.createMarshaller();
            MARSHALLER.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            UNMARSHALLER = JAXB_CONTEXT.createUnmarshaller();
        } catch (JAXBException e) {
            LOG.error("Error initialization JAXB context.");
            throw new IllegalStateException("Error initialization JAXB context.", e);
        }
    }

    /** Hidden constructor.*/
    private XMLUtils() { }

    /**
     * Does conversion of given instance to target storage.
     * @param instance - instance
     * @param storage - storage.
     * @param <T> - instance type.
     * @return - saved instance.
     * @throws ConversionException - in case of issues during conversion.
     */
    public static <T> T convertToXML(T instance, File storage) throws ConversionException {
        try {
            MARSHALLER.marshal(instance, storage);
        } catch (JAXBException e) {
            throw new ConversionException(
                String.format("Error during storing '%s' instance to '%s' file.",
                    instance.getClass().getSimpleName(), storage),
                e
            );
        }

        return instance;
    }

    /**
     * Does conversion of instance from source storage.
     * @param storage - source storage.
     * @param clazz - instance type.
     * @param <T> - converted instance type.
     * @return - instance.
     * @throws ConversionException - in case of issues during conversion.
     */
    public static <T> T convertFromXML(File storage, Class<T> clazz) throws ConversionException {
        try {
            Object instance = UNMARSHALLER.unmarshal(storage);
            if (clazz.isInstance(instance)) {
                return clazz.cast(instance);
            }

            throw new IllegalStateException(
                String.format("Source storage '%s' is wrong for given instance type '%s'.", storage, clazz)
            );
        } catch (JAXBException e) {
            throw new ConversionException(String.format("Error during conversion from '%s' file.", storage), e);
        }
    }

}
