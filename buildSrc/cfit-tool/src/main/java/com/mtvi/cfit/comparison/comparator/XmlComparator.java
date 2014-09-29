package com.mtvi.cfit.comparison.comparator;

import com.google.common.io.Files;
import com.mtvi.cfit.comparison.ComparisonException;
import com.mtvi.cfit.comparison.ResponseComparisonResult;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Comparator for XML files.
 *
 * @author zenind.
 */
@Deprecated
public class XmlComparator implements FileComparator {

    static {
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Override
    public ResponseComparisonResult compare(File left, File right) throws ComparisonException {
        try {
            BufferedReader leftReader = Files.newReader(left, Charset.defaultCharset());
            BufferedReader rightReader = Files.newReader(right, Charset.defaultCharset());
            leftReader.readLine();
            rightReader.readLine();

            Diff diff = new Diff(leftReader, rightReader);
            return diff.identical() ? ResponseComparisonResult.IDENTICAL : ResponseComparisonResult.DIFFERENT;
        } catch (IOException e) {
            return ResponseComparisonResult.DIFFERENT;
        } catch (SAXException e) {
            throw new ComparisonException(e);
        }
    }
}
